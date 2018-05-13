package ifis.skysim2.algorithms;

import ifis.skysim2.algorithms.SkylineAlgorithmBNL.BNLWindowPolicy;
import ifis.skysim2.simulator.SimulatorConfiguration;
import ifis.skysim2.data.points.LinkedPointList;
import ifis.skysim2.data.points.PointList;
import ifis.skysim2.data.sources.PointSource;
import ifis.skysim2.data.tools.PointComparator;
import ifis.skysim2.data.trees.pointquadtree.ArrayPointQuadtree;
import ifis.skysim2.data.trees.pointquadtree.ArrayPointQuadtree.Node;
import ifis.skysim2.simulator.SimulatorConfiguration.PresortStrategy;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/*
 * Use a single-leveled quad tree for splitting the skyline window into 2^d parts.
 * Choose 0.5^d as the splitting point.
 * For details, see
 * xxx: Scalable Skyline Computation using Object-Based Space Partitioning (2009)
 *
 */
public class SkylineAlgorithmSingleLevelQuadTreeBNL extends AbstractSkylineAlgorithm {

    private BNLWindowPolicy bnlWindowPolicy;
    private boolean presorted;

    private ArrayPointQuadtree tree;

    @Override
    public void setExperimentConfig(SimulatorConfiguration config) {
	super.setExperimentConfig(config);
	bnlWindowPolicy = config.getBnlWindowPolicy();
	presorted = (config.getPresortStrategy() == PresortStrategy.FullPresort);
    }

    @Override
    public List<float[]> compute(PointSource data) {
	long startTime = System.nanoTime();

	int d = data.getD();

	// create the split point
	float[] splitPoint = new float[d];
	Arrays.fill(splitPoint, 0.5f);

	tree = new ArrayPointQuadtree(splitPoint, 1);
	Node root = tree.root;

	ioCost = 0;
	BNLProfiler profiler = new BNLProfiler();

	final int n = data.size();

	int insertions = 0;
	int comparisons = 0;

	dataloop:
	for (int i = 0; i < n; i++) {
	    final float[] dataPoint = data.get(i);
	    ioCost++;
	    int dataPointPartition = (int)PointComparator.getSuccessorship(dataPoint, splitPoint);
	    comparisons++;

	    // Iterate over all space partitions that could contains point being dominated by the input point
	    // Only required if input stream is not sorted!
	    if (!presorted) {
		Iterator<Node> iter = root.childrenIteratorMoreOnesThanMask(dataPointPartition);
		while (iter.hasNext()) {
		    Node node = iter.next();
		    SkylineAlgorithmBNL.bnlOperation(node.points, dataPoint, false, profiler);
		    // unlink node if all points have been removed
		    if (node.points.isEmpty()) {
			iter.remove();
		    }
		}
	    }

	    // Iterate over all space partitions that could contain points dominating the input point
	    // Required in any case!
	    Iterator<Node> iter = root.childrenIteratorMoreZerosThanMask(dataPointPartition);

	    while (iter.hasNext()) {
		Node node = iter.next();
		boolean notDominated = SkylineAlgorithmBNL.bnlOperation(node.points, dataPoint, false, profiler);
		if (!notDominated) {
		    // dataPoint is dominated
		    continue dataloop;
		}
	    }

	    // if dataPoint was dominated yet, check its own partition
	    if (root.isChildEmpty(dataPointPartition)) {
		insertions++;
		root.addToChild(dataPointPartition, dataPoint);
	    } else {
		Node node = root.getChild(dataPointPartition);
		SkylineAlgorithmBNL.bnlOperation(node.points, dataPoint, true, profiler);
	    }
	}

	// combine all sublists
	PointList window = new LinkedPointList(d);
	Iterator<Node> iter = root.childrenIterator();
	int numNonemptyPartitions = 0;
	while (iter.hasNext()) {
	    Node node = iter.next();
	    window.addAll(node.points);
	    numNonemptyPartitions++;
	}

	profiler.update(insertions, 0, 0, 0, comparisons);

	cpuCost = profiler.getCpuCost();
	totalTimeNS = System.nanoTime() - startTime;

	int numPartitions = 1 << d;  // 2^d
	int emptyPartitions = numPartitions - numNonemptyPartitions;

	System.out.format("Ratio of empty partitions: %2.1f%%  (%d of %d)%n", 100.0 * emptyPartitions / numPartitions, emptyPartitions, numPartitions);

	return window;
    }

    @Override
    public long getIOcost() {
	return ioCost;
    }

    @Override
    public long getCPUcost() {
	return cpuCost;
    }

    @Override
    public long getTotalTimeNS() {
	return totalTimeNS;
    }

    @Override
    public long getReorgTimeNS() {
	return 0;
    }

    @Override
    public String toString() {
	StringBuffer buf = new StringBuffer();
	buf.append("Single-leveled quad tree BNL");
	buf.append(" (Policy: " + bnlWindowPolicy + ")");
	return buf.toString();
    }

    @Override
    public String getShortName() {
	return "SL-QT-BNL";
    }

}