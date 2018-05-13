package ifis.skysim2.algorithms;

import ifis.skysim2.data.points.LinkedPointList;
import ifis.skysim2.data.points.PointList.ListOrder;
import ifis.skysim2.simulator.SimulatorConfiguration;
import ifis.skysim2.data.points.PointList;
import ifis.skysim2.data.points.PointListIterator;
import ifis.skysim2.data.sources.PointSource;
import ifis.skysim2.data.tools.PointRelationship;
import java.util.List;

public final class SkylineAlgorithmBNL extends AbstractSkylineAlgorithm {

    private BNLWindowPolicy bnlWindowPolicy = BNLWindowPolicy.SimpleAppend;
    private final static double PROB_THRESHOLD = 0;

    @Override
    public void setExperimentConfig(SimulatorConfiguration config) {
	super.setExperimentConfig(config);
	bnlWindowPolicy = config.getBnlWindowPolicy();
    }

    // notes on optimization:
    // - PointListLinked is twice as fast as PointListArray
    // - PointListLinked is 30% faster than LinkedList<float[]>
    // externalization:
    // - data is read from file (block-wise)
    // - multiple iterations:
    //   if window is full and (3) occurs then store dataPoint in a new temporary file,
    //   this file will be processed as input in the next iteration;
    //   after every iteration, output all window points that have been compared to all tuples
    //   in the temporary file (these must be in the skyline)
    // - to find out which window points have been compared to all tuples in the temporary file:
    //   Starting at the time where a point is added to the window, it gets compared to all
    //   data points from then on. Therefore, store this time (i. e. corresponding dataPoint rank)
    //   for each window point and use bookkeeping to find out when each window point
    //   has been compared to all remaining data points (across iterations)
    //   (alternative: store a timestamp for each window point and point in the temporary file
    //   indicating when the point has been added to the window or temporary file. If we read
    //   a point with timestamp t from the temporary file, we can output all points from the window
    //   with timestamp less than t)
    // - note: this only needs to be one if the window gets "full" sometime
    // further tuning: keep tuples in the window having the largest domination volume
    // (problem: possibly more bookkeeping and comparisons)
    // TODO: implement handling of finite window size
    @Override
    public synchronized List<float[]> compute(PointSource data) {
	long startTime = System.nanoTime();

	int d = data.getD();
	ListOrder listOrder;
	switch (bnlWindowPolicy) {
	    case KeepSorted:
		listOrder = ListOrder.SortedByVolume;
		break;
	    case BubbleUp:
		listOrder = ListOrder.BubbleUp;
		break;
	    case BubbleUpSimple:
		listOrder = ListOrder.BubbleUpSimple;
		break;
	    case MoveToFront:
		listOrder = ListOrder.MoveToFront;
		break;
	    default:
		listOrder = ListOrder.Unsorted;
		break;
	}
	final PointList window = new LinkedPointList(d, listOrder);

	// number of disk accesses
	ioCost = 0;

	BNLProfiler profiler = new BNLProfiler();
	final int n = data.size();

	datalist:
	for (int i = 0; i < n; i++) {
	    final float[] dataPoint = data.get(i);
	    ioCost++;
	    bnlOperation(window, dataPoint, true, profiler);
	}

	cpuCost = profiler.getCpuCost();

	totalTimeNS = System.nanoTime() - startTime;
	return window;
    }

    /*
     * Processes point with respect to window.
     * Returns true if point is not dominated.
     */
    public static boolean bnlOperation(PointList window, float[] dataPoint, boolean insertionAllowed, BNLProfiler profiler) {
	int cpuCost = 0;
	int insertions = 0;
	int deletions = 0;
	int moves = 0;
	int drops = 0;
	// there possible cases:
	// (1) dataPoint is dominated by at least one window point
	//     --> skip dataPoint
	// (2) dataPoint dominates at least one window point
	//     --> remove these window points and add dataPoint to the list
	//         (replace first of these points)
	// (3) dataPoint is incomparable to all window points
	//     --> append dataPoint to window

	// It seemed to be a good idea to replace this iterator by
	// an iterator that returns only points being comparable to
	// dataPoint. However, this approach did not yield any noteworthy
	// performance gain. Therefore, we use the classical iteration.
	PointListIterator windowIter = window.listIterator(dataPoint);
	while (windowIter.hasNext()) {
	    PointRelationship dom = windowIter.nextAndCompareNextToReferencePoint();
	    cpuCost++;
	    switch (dom) {
		case IS_DOMINATED_BY:
		    // if list[j] is dominated by point then delete list[j]
		    windowIter.remove();
		    deletions++;
		    break;
		case DOMINATES:
		    // if list[j] dominates point then skip to next point
		    // apply promotion heuristic
		    windowIter.promotePoint();
		    moves++;
		    BNLProfiler.updateProfiler(profiler, insertions, deletions, moves, drops, cpuCost);
		    return false;
		case EQUALS:
		    // point equals windowPoint, ignore duplicates
		    BNLProfiler.updateProfiler(profiler, insertions, deletions, moves, drops, cpuCost);
		    return false;
	    }
	}
	if (insertionAllowed) {
	    // compute P(dataPoint is not dominated by any of the remaining data points),
	    // assuming uniform data distribution
//	    double dominatingVol = 1;
//	    for (int j = 0; j < d; j++) {
//		dominatingVol *= 1 - dataPoint[j];
//	    }
//	    double probSkyUniform = Math.pow(1 - dominatingVol, remaining);
//			System.out.format("%.4f%n", probSkyUniform);
//	    if (probSkyUniform >= PROB_THRESHOLD) {
		// because point is not dominated by any list[j], add it to end of list
		window.addDirect(dataPoint);
		insertions++;
//	    } else {
//		drops++;
//	    }
	}
	BNLProfiler.updateProfiler(profiler, insertions, deletions, moves, drops, cpuCost);
	return true;
    }

    @Override
    public String toString() {
	StringBuffer buf = new StringBuffer();
	buf.append("BNL");
	buf.append(" (Policy: " + bnlWindowPolicy + ")");
	return buf.toString();
    }

    @Override
    public String getShortName() {
	return "BNL";
    }

    public static enum BNLWindowPolicy {
	SimpleAppend,
	MoveToFront,
	KeepSorted,
	BubbleUp,
	BubbleUpSimple
    }
}