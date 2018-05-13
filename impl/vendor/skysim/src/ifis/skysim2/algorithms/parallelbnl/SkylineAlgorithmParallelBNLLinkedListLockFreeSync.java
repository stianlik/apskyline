package ifis.skysim2.algorithms.parallelbnl;

import ifis.skysim2.algorithms.*;
import ifis.skysim2.data.points.LinkedPointList;
import ifis.skysim2.data.points.PointList;
import ifis.skysim2.data.sources.PointSource;
import ifis.skysim2.data.tools.PointComparator;
import ifis.skysim2.data.tools.PointRelationship;
import ifis.skysim2.simulator.SimulatorConfiguration;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicMarkableReference;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SkylineAlgorithmParallelBNLLinkedListLockFreeSync extends AbstractSkylineAlgorithm {

    private int numCPUs;

    @Override
    public void setExperimentConfig(SimulatorConfiguration config) {
	super.setExperimentConfig(config);
	numCPUs = config.getNumberOfCPUs();
    }

    @Override
    public List<float[]> compute(PointSource data) {
	long startTime = System.nanoTime();

	// create window
	final int d = data.getD();
	final Node tail = new Node(new float[0], null);
	final Node head = new Node(new float[0], tail);

	// start parallel computation
//	final AtomicInteger dataCounter = new AtomicInteger(0);
	ExecutorService executor = Executors.newFixedThreadPool(numCPUs);
	final BNLProfiler[] profilers = new BNLProfiler[numCPUs];
	final BNLRunner[] bnlRunners = new BNLRunner[numCPUs];
	for (int i = 0; i < numCPUs; i++) {
	    BNLProfiler profiler = new BNLProfiler();
	    profilers[i] = profiler;
//	    BNLRunner bnlRunner = new BNLRunner(data, dataCounter, head, tail, profiler);
	    BNLRunner bnlRunner = new BNLRunner(data, numCPUs, i, head, tail, profiler);
	    bnlRunners[i] = bnlRunner;
	    executor.execute(bnlRunner);
	}
	executor.shutdown();
	try {
	    executor.awaitTermination(1, TimeUnit.DAYS);
	} catch (InterruptedException ex) {
	    Logger.getLogger(SkylineAlgorithmPskyline.class.getName()).log(Level.SEVERE, null, ex);
	    throw new UnsupportedOperationException("Error handling not supported yet.");
	}

	totalTimeNS = System.nanoTime() - startTime;

	int problemCountTotal = 0;
	for (BNLRunner bnlRunner : bnlRunners) {
	    problemCountTotal += bnlRunner.problemCount;
	}
	System.out.println("Problem count: " + problemCountTotal);

	// create result
	startTime = System.nanoTime();
	PointList result = new LinkedPointList(d);
	Node curr = head;
	while (curr.next.getReference() != tail) {
	    curr = curr.next.getReference();
	    result.addDirect(curr.point);
	}
	reorgTimeNS = System.nanoTime() - startTime;

	// collect profiling data
	BNLProfiler profiler = new BNLProfiler();
	for (int i = 0; i < numCPUs; i++) {
	    BNLProfiler profilerI = profilers[i];
	    long insertionsI = profilerI.getInsertions();
	    long deletionsI = profilerI.getDeletions();
	    long cpuCostI = profilerI.getCpuCost();
	    profiler.update(insertionsI, deletionsI, 0, 0, cpuCostI);
	}
	cpuCost = profiler.getCpuCost();
	ioCost = data.size();
//	System.out.println(profiler);

	return result;
    }

    @Override
    public String getShortName() {
	return String.format("ParBNLLFree (%d)", numCPUs);
    }

    @Override
    public String toString() {
	return String.format("ParallelBNLLockFree (%d CPUs)", numCPUs);
    }

    private static class BNLRunner implements Runnable {

	private final int n;
	private final PointSource data;
//	private final AtomicInteger dataCounter;
	private final int numCPUs;
	private int iData;
	private final Node head;
	private final Node tail;
	private final BNLProfiler profiler;
	private int problemCount = 0;

//	private BNLRunner(final PointSource data, final AtomicInteger dataCounter, final Node head, final Node tail, final BNLProfiler profiler) {
	private BNLRunner(final PointSource data, final int numCPUs, final int cpuID, final Node head, final Node tail, final BNLProfiler profiler) {
	    n = data.size();
	    this.data = data;
//	    this.dataCounter = dataCounter;
	    this.numCPUs = numCPUs;
	    iData = cpuID - numCPUs;
	    this.head = head;
	    this.tail = tail;
	    this.profiler = profiler;
	}

	@Override
	public void run() {
	    int cpuCost = 0;
	    int insertions = 0;
	    int deletions = 0;

//	    int iData;
	    dataloop:
//	    while ((iData = dataCounter.getAndIncrement()) < n) {
	    while ((iData += numCPUs) < n) {
		final float[] pData = data.get(iData);

		// compare pData to window points
		validateloop:
		while (true) {
		    boolean[] deleted = new boolean[1]; // will be filled by calling curr.next.get(marked)
		    Node pred = head;
		    Node curr = pred.next.getReference();

		    steploop:
		    while (curr != tail) {
			Node succ = curr.next.get(deleted);

			// delete curr if it is marked
			if (deleted[0]) {
			    boolean snip = pred.next.compareAndSet(curr, succ, false, false);
			    if (!snip) {
				// start over
				problemCount++;
				continue validateloop;
			    } else {
				// deletion will complete successfully
				curr = succ;
				continue steploop;
			    }
			} else {
			    // curr isn't marked
			    final float[] pWindow = curr.point;
			    PointRelationship dom = PointComparator.compare(pWindow, pData);
			    cpuCost++;
			    switch (dom) {
				case IS_DOMINATED_BY:
				    // if pWindow is dominated by pData then delete pWindow
				    boolean snip = curr.next.attemptMark(succ, true);
				    if (!snip) {
					// start over
					problemCount++;
					continue validateloop;
				    } else {
					// it does not matter whether this succeeds completely or not;
					// it will be cleaned up later anyway
					pred.next.compareAndSet(curr, succ, false, false);
					deletions++;
				    }
				    curr = succ;
				    break;
				case DOMINATES:
				case EQUALS:
				    // if pWindow dominates pData or pWindow equals pData then skip to next data point
				    continue dataloop;
				case IS_INCOMPARABLE_TO:
				default:
				    pred = curr;
				    curr = succ;
				    break;
			    }
			}
		    }

		    // pData is not dominated by any window point, so add pData to end of the window
		    // it is curr == tail
		    Node newNode = new Node(pData, tail);
		    boolean snip = pred.next.compareAndSet(tail, newNode, false, false);
		    if (!snip) {
			// start over
			problemCount++;
			continue validateloop;
		    } else {
			// insertion has been successful
			insertions++;
			continue dataloop;
		    }
		}
	    }
	    BNLProfiler.updateProfiler(profiler, insertions, deletions, 0, 0, cpuCost);
	}
    }

    private static class Node {
	private final float[] point;
	private final AtomicMarkableReference<Node> next;

	private Node(float[] point, Node next) {
	    // Make a copy for better memory locality
	    this.point = Arrays.copyOf(point, point.length);
	    this.next = new AtomicMarkableReference<Node>(next, false);
	}
    }
}
