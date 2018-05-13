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
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class SkylineAlgorithmParallelBNLLinkedListLazySync extends AbstractSkylineAlgorithm {

    private int numCPUs;

    @Override
    public void setExperimentConfig(SimulatorConfiguration config) {
	super.setExperimentConfig(config);
	numCPUs = config.getNumberOfCPUs();
    }

    @Override
    public List<float[]> compute(PointSource data) {
	return compute(data, null);
    }

    public List<float[]> compute(PointSource data, ExecutorService executor) {
	long startTime = System.nanoTime();

	// create window
	final int d = data.getD();
	final Node tail = new Node(new float[0], null);
	final Node head = new Node(new float[0], tail);

	// start parallel computation
//	final AtomicInteger dataCounter = new AtomicInteger(0);
	if (executor == null) {
	    executor = Executors.newFixedThreadPool(numCPUs);
	}
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
	while (curr.next != tail) {
	    curr = curr.next;
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

	return result;
    }

    @Override
    public String getShortName() {
	return String.format("ParBNLLLLazy (%d)", numCPUs);
    }

    @Override
    public String toString() {
	return String.format("ParallelBNLLinkedListLazySync (%d CPUs)", numCPUs);
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
		    // repeat processing of pData as soon as some call of
		    // validate returns false
		    Node pred = head;
		    Node curr = pred.next;
		    while (curr != tail) {
			// skip marked nodes
			if (curr.deleted) {
			    pred = curr;
			    curr = curr.next;
			    continue;
			}
			final float[] pWindow = curr.point;
			PointRelationship dom = PointComparator.compare(pWindow, pData);
			cpuCost++;
			switch (dom) {
			    case IS_DOMINATED_BY:
				// if pWindow is dominated by pData then delete pWindow
				pred.lock();
				curr.lock();
				if (!pred.deleted && !curr.deleted && pred.next == curr) {
				    // removal will succeed
				    curr.deleted = true;
				    pred.next = curr.next;
				    curr.unlock();
				    pred.unlock();
				    deletions++;
				} else {
				    // start over
				    curr.unlock();
				    pred.unlock();
				    problemCount++;
				    continue validateloop;
				}
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
		    pred.lock();
		    if (!pred.deleted && pred.next == tail) {
			// insertion will succeed
			Node newNode = new Node(pData, tail);
			pred.next = newNode;
			pred.unlock();
			insertions++;
			continue dataloop; // processing completed successfully
		    } else {
			// start over
			pred.unlock();
			problemCount++;
			continue validateloop;
		    }
		}
	    }
	    BNLProfiler.updateProfiler(profiler, insertions, deletions, 0, 0, cpuCost);
	}
    }

    private static class Node {
	private final float[] point;
	private volatile Node next;
	private final Lock lock;
	private volatile boolean deleted;

	private Node(float[] point, Node next) {
	    // Make a copy for better memory locality
	    this.point = Arrays.copyOf(point, point.length);
	    this.next = next;
	    lock = new ReentrantLock();
	    deleted = false;
	}

	private void lock() {
	    lock.lock();
	}

	private void unlock() {
	    lock.unlock();
	}
    }
}
