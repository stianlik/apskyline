package ifis.skysim2.algorithms.parallelbnl;

import ifis.skysim2.algorithms.AbstractSkylineAlgorithm;
import ifis.skysim2.algorithms.BNLProfiler;
import ifis.skysim2.data.points.LinkedPointList;
import ifis.skysim2.data.points.PointList;
import ifis.skysim2.data.sources.PointSource;
import ifis.skysim2.data.tools.PointComparator;
import ifis.skysim2.data.tools.PointRelationship;
import ifis.skysim2.simulator.SimulatorConfiguration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SkylineAlgorithmDistributedParallelBNL extends AbstractSkylineAlgorithm {

    private int numCPUs;
    private int numBlocks;
    private boolean deleteDuringCleaning;

    @Override
    public void setExperimentConfig(SimulatorConfiguration config) {
	super.setExperimentConfig(config);
	numCPUs = config.getNumberOfCPUs();
	numBlocks = config.getDistributedNumBlocks();
	deleteDuringCleaning = config.isDeleteDuringCleaning();
    }

    @Override
    public List<float[]> compute(PointSource data) {
	long startTime = System.nanoTime();

	// create local windows
	final int d = data.getD();
	final Node[] heads = new Node[numBlocks];
	final Node[] tails = new Node[numBlocks];
	for (int i = 0; i < numBlocks; i++) {
	    Node tail = new Node(new float[0], null);
	    Node head = new Node(new float[0], tail);
	    heads[i] = head;
	    tails[i] = tail;
	}

	ExecutorService executor = Executors.newFixedThreadPool(numCPUs);

	// start parallel computation (there is no need to support concurrency within windows here)
//	final AtomicInteger dataCounter = new AtomicInteger(0);
	final BNLProfiler[] profilers = new BNLProfiler[numBlocks];
	final Collection<BNLRunner> bnlRunners = new ArrayList<BNLRunner>(numBlocks);
	for (int i = 0; i < numBlocks; i++) {
	    BNLProfiler profiler = new BNLProfiler();
	    profilers[i] = profiler;
//	    BNLRunner bnlRunner = new BNLRunner(data, dataCounter, heads[i], tails[i], profiler);
	    BNLRunner bnlRunner = new BNLRunner(data, numBlocks, i, heads[i], tails[i], profiler);
	    bnlRunners.add(bnlRunner);
	}
	try {
	    executor.invokeAll(bnlRunners);    
	} catch (InterruptedException ex) {
	    Logger.getLogger(SkylineAlgorithmDistributedParallelBNL.class.getName()).log(Level.SEVERE, null, ex);
	}

	int sizeTotal = 0;
	for (BNLRunner bnlRunner : bnlRunners) {
	    sizeTotal += bnlRunner.size;
	}
	System.out.println("Total size of local skylines: " + sizeTotal);

	// cleanup phase:
	// Thread i reads all other windows and deletes all dominated elements from its own window (BNL style)
	final Collection<Callable<Object>> bnlCleaners = new ArrayList<Callable<Object>>(numCPUs);
	for (int i = 0; i < numBlocks; i++) {
	    BNLProfiler profiler = profilers[i];
	    Callable<Object> bnlCleaner = new BNLCleaner(heads, tails, i, numBlocks, deleteDuringCleaning, profiler);
	    bnlCleaners.add(bnlCleaner);
	}
	try {
	    executor.invokeAll(bnlCleaners);
	} catch (InterruptedException ex) {
	    Logger.getLogger(SkylineAlgorithmDistributedParallelBNL.class.getName()).log(Level.SEVERE, null, ex);
	}

	// shutdown executor
	executor.shutdown();
	try {
	    executor.awaitTermination(1, TimeUnit.DAYS);
	} catch (InterruptedException ex) {
	    throw new UnsupportedOperationException("Error handling not supported yet.");
	}

	totalTimeNS = System.nanoTime() - startTime;

	// create result
	startTime = System.nanoTime();
	PointList result = new LinkedPointList(d);
	for (int i = 0; i < numBlocks; i++) {
	    Node head = heads[i];
	    Node tail = tails[i];
	    Node curr = head;
	    while (curr.next != tail) {
		curr = curr.next;
		if (!curr.deleted) {
		    result.addDirect(curr.point);
		}
	    }
	}
	reorgTimeNS = System.nanoTime() - startTime;

	// collect profiling data
	BNLProfiler profiler = new BNLProfiler();
	for (int i = 0; i < numBlocks; i++) {
	    BNLProfiler profilerI = profilers[i];
	    long  insertionsI = profilerI.getInsertions();
	    long deletionsI = profilerI.getDeletions();
	    long cpuCostI = profilerI.getCpuCost();
	    profiler.update(insertionsI, deletionsI, 0, 0, cpuCostI);
	}
	cpuCost = profiler.getCpuCost();
	ioCost = data.size();

	return result;
    }

    private static class Node {
	private final float[] point;
	private volatile Node next;
	private volatile boolean deleted;

	private Node(float[] point, Node next) {
	    // Make a copy for better memory locality
	    this.point = Arrays.copyOf(point, point.length);
	    this.next = next;
	    deleted = false;
	}
    }

    private static class BNLRunner implements Callable<Object> {

	private final int n;
	private final PointSource data;
//	private final AtomicInteger dataCounter;
	private final int numBlocks;
	private int iData;
	private final Node head;
	private final Node tail;
	private final BNLProfiler profiler;
	private int size = 0;

//	private BNLRunner(final PointSource data, final AtomicInteger dataCounter, final Node head, final Node tail, final BNLProfiler profiler) {
	private BNLRunner(final PointSource data, final int numBlocks, final int blockID, final Node head, final Node tail, final BNLProfiler profiler) {
	    n = data.size();
	    this.data = data;
//	    this.dataCounter = dataCounter;
	    this.numBlocks = numBlocks;
	    iData = blockID - numBlocks;
	    this.head = head;
	    this.tail = tail;
	    this.profiler = profiler;
	}

	@Override
	public Object call() {
	    int cpuCost = 0;
	    int insertions = 0;
	    int deletions = 0;

//	    int iData;
	    dataloop:
//	    while ((iData = dataCounter.getAndIncrement()) < n) {
	    while ((iData += numBlocks) < n) {
		final float[] pData = data.get(iData);

		// compare pData to window points
	        Node pred = head;
		Node curr = pred.next;
		while (curr != tail) {
		    final float[] pWindow = curr.point;
		    PointRelationship dom = PointComparator.compare(pWindow, pData);
		    cpuCost++;
		    switch (dom) {
			case IS_DOMINATED_BY:
			    // if pWindow is dominated by pData then delete pWindow
			    pred.next = curr.next;
			    deletions++;
			    size--;
			    curr = curr.next;
			    break;
			case DOMINATES:
			case EQUALS:
			    // if pWindow dominates pData or pWindow equals pData then skip to next data point
			    continue dataloop;
			case IS_INCOMPARABLE_TO:
			default:
			    // advance to next window point
			    pred = curr;
			    curr = curr.next;
			    break;
		    }
		}

		// pData is not dominated by any window point, so add pData to end of the window
		// it is curr == tail
		Node newNode = new Node(pData, tail);
		pred.next = newNode;
		insertions++;
		size++;
	    }
	    BNLProfiler.updateProfiler(profiler, insertions, deletions, 0, 0, cpuCost);
	    return null;
	}
    }

    private static class BNLCleaner implements Callable<Object> {

	private final Node[] heads;
	private final Node[] tails;
	private final int id;
	private final int numBlocks;
	private final boolean deleteDuringCleaning;
	private final Node head;
	private final Node tail;
	private final BNLProfiler profiler;

	private BNLCleaner(final Node[] heads, final Node[] tails, final int id, final int numBlocks, final boolean deleteDuringCleaning, final BNLProfiler profiler) {
	    this.heads = heads;
	    this.tails = tails;
	    this.id = id;
	    this.numBlocks = numBlocks;
	    this.deleteDuringCleaning = deleteDuringCleaning;
	    this.profiler = profiler;
	    head = heads[id];
	    tail = tails[id];
	}

	@Override
	public Object call() {
	    int cpuCost = 0;
	    int insertions = 0;
	    int deletions = 0;

	    // iterate over other windows
	    int currentDataWindow = (id + 1) % numBlocks;
	    while (currentDataWindow != id) {
		// iterate within each data window
		Node headCurrentDataWindow = heads[currentDataWindow];
		Node tailCurrentDataWindow = tails[currentDataWindow];
		Node predCurrentDataWindow = headCurrentDataWindow;
		Node currCurrentDataWindow = predCurrentDataWindow.next;
		dataloop:
		while (currCurrentDataWindow != tailCurrentDataWindow) {
		    final float[] pData = currCurrentDataWindow.point;

		    // compare pData to window points
		    Node pred = head;
		    Node curr = pred.next;
		    iterloop:
		    while (curr != tail) {
			final float[] pWindow = curr.point;
			PointRelationship dom = PointComparator.compare(pWindow, pData);
			cpuCost++;
			switch (dom) {
			    case IS_DOMINATED_BY:
				// if pWindow is dominated by pData then delete pWindow
				if (deleteDuringCleaning) {
				    // physically delete
				    pred.next = curr.next;
				    deletions++;
				    curr = curr.next;
				} else {
				    // logically delete
				    curr.deleted = true;
				    pred = curr;
				    curr = curr.next;
				}
				break;
			    case DOMINATES:
			    case EQUALS:
				// if pWindow dominates pData or pWindow equals pData then skip to next data point
				break iterloop;
			    case IS_INCOMPARABLE_TO:
			    default:
				// advance to next window point
				pred = curr;
				curr = curr.next;
				break;
			}
		    }

		    // step to next data window point
		    predCurrentDataWindow = currCurrentDataWindow;
		    currCurrentDataWindow = currCurrentDataWindow.next;
		}
		// step to next data window
		currentDataWindow = (currentDataWindow + 1) % numBlocks;
	    }
	    BNLProfiler.updateProfiler(profiler, insertions, deletions, 0, 0, cpuCost);
	    return null;
	}
    }

    @Override
    public String getShortName() {
	return String.format("DistParBNLLz (%d/%d)", numCPUs, numBlocks);
    }

    @Override
    public String toString() {
	return String.format("DistributedParallelBNLLinkedListLazySync (%d CPUs, %d blocks)", numCPUs, numBlocks);
    }
}
