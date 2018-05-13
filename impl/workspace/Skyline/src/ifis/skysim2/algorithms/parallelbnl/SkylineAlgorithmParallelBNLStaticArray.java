package ifis.skysim2.algorithms.parallelbnl;

import ifis.skysim2.algorithms.*;
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
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SkylineAlgorithmParallelBNLStaticArray extends AbstractSkylineAlgorithm {

    private int numCPUs;

    private static final float[] DELETED = new float[0];
    private int windowSize;

    @Override
    public void setExperimentConfig(SimulatorConfiguration config) {
	super.setExperimentConfig(config);
	numCPUs = config.getNumberOfCPUs();
	windowSize = config.getStaticArrayWindowSize();
    }

    @Override
    public List<float[]> compute(PointSource data) {
	long startTime = System.nanoTime();

	// create window
	AtomicReferenceArray<float[]> window = new AtomicReferenceArray<float[]>(windowSize);

	// start parallel computation
	final AtomicInteger dataCounter = new AtomicInteger(0);
	ExecutorService executor = Executors.newFixedThreadPool(numCPUs);
	final BNLProfiler[] profilers = new BNLProfiler[numCPUs];
	for (int i = 0; i < numCPUs; i++) {
	    BNLProfiler profiler = new BNLProfiler();
	    profilers[i] = profiler;
	    Runnable bnlRunner = new BNLRunner(data, dataCounter, window, profiler);
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

	// clean up window
	startTime = System.nanoTime();
	float[][] result = new float[windowSize][];
	int iWindow = 0;
	int iResult = 0;
	float[] pWindow;
	while ((pWindow = window.get(iWindow)) != null) {
	    if (pWindow != DELETED) {
		result[iResult] = pWindow;
		iResult++;
	    }
	    iWindow++;
	}
	result = Arrays.copyOf(result, iResult);
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

	return Arrays.asList(result);
    }

    @Override
    public String getShortName() {
	return String.format("ParBNLStatArr (%d)", numCPUs);
    }

    @Override
    public String toString() {
	return String.format("ParallelBNLStaticArray (%d CPUs)", numCPUs);
    }

    private static class BNLRunner implements Runnable {

	private final int n;
	private final PointSource data;
	private final AtomicInteger dataCounter;
	private final AtomicReferenceArray<float[]> window;
	private final BNLProfiler profiler;

	private BNLRunner(final PointSource data, final AtomicInteger dataCounter, final AtomicReferenceArray<float[]> window, final BNLProfiler profiler) {
	    n = data.size();
	    this.data = data;
	    this.dataCounter = dataCounter;
	    this.window = window;
	    this.profiler = profiler;
	}

	@Override
	public void run() {
	    int cpuCost = 0;
	    int insertions = 0;
	    int deletions = 0;
	    
	    int iData;
	    dataloop:
	    while ((iData = dataCounter.getAndIncrement()) < n) {
		final float[] pData = data.get(iData);

		// compare pData to window points
		int iWindow = 0;
		validateloop:
		while (true) {
		    float[] pWindow;
		    while ((pWindow = window.get(iWindow)) != null) {
			if (pWindow == DELETED) {
			    iWindow++;
			    continue;
			}
			PointRelationship dom = PointComparator.compare(pWindow, pData);
			cpuCost++;
			switch (dom) {
			    case IS_DOMINATED_BY:
				// if pWindow is dominated by pData then delete pWindow
				window.lazySet(iWindow, DELETED);
				deletions++;
				iWindow++;
				break;
			    case DOMINATES:
				// if pWindow dominates pData then skip to next data point
				continue dataloop;
			    case EQUALS:
				// pWindow equals pData, let's ignore duplicates
				continue dataloop;
			    default:
				iWindow++;
				break;
			}
		    }

		    // pData is not dominated by any window point, so add pData to end of the window
		    if (window.compareAndSet(iWindow, null, pData)) {
			insertions++;
			continue dataloop;
		    } else {
			iWindow++;
			continue validateloop;
		    }
		}
	    }
	    BNLProfiler.updateProfiler(profiler, insertions, deletions, 0, 0, cpuCost);
	}
    }
}