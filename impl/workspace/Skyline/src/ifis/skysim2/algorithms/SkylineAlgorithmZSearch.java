package ifis.skysim2.algorithms;

import ifis.skysim2.data.sources.PointSource;
import ifis.skysim2.data.trees.zbtree.BulkLoader;
import ifis.skysim2.data.trees.zbtree.BulkLoaderCompact;
import ifis.skysim2.data.trees.zbtree.ZBTree;
import ifis.skysim2.common.datastructures.ArrayListStack;
import ifis.skysim2.simulator.SimulatorConfiguration;
import ifis.skysim2.data.trees.zbtree.ZBTreeComparator;
import java.util.List;

/*
 * ZSearch skyline algorithm
 * Lee, Zheng, Li, Lee: Approaching the skyline in Z order
 */
public class SkylineAlgorithmZSearch extends AbstractSkylineAlgorithm implements SkylineAlgorithm {

    private long ioCost;
    private long cpuCost;
    private long totalTimeNS;

    private boolean nodeCapacitiesSet = false;
        private int nodeCapacityMax;

    public SkylineAlgorithmZSearch() {
    }

    @Override
    public void setExperimentConfig(SimulatorConfiguration config) {
	nodeCapacityMax = config.getNodeCapacityMax();
	nodeCapacitiesSet = true;
    }

    @Override
    public List<float[]> compute(PointSource data) {
	long start = System.nanoTime();
	int d = data.getD();
	BulkLoader bl = new BulkLoaderCompact();

	ZBTreeComparator tc = new ZBTreeComparator();

	ZBTree zbtree;
	if (nodeCapacitiesSet) {
	    zbtree = bl.bulkLoad(data, nodeCapacityMax);
	} else {
	    zbtree = bl.bulkLoad(data);
	}
	long stop = System.nanoTime();
	System.out.format("Generate ZB-tree: %.2f s\n", (double) (stop - start) / 1000000000);

	long startTime = System.nanoTime();
	ioCost = 0;
	cpuCost = 0;

	ArrayListStack<ZBTree.Node> stack = new ArrayListStack<ZBTree.Node>();
	ZBTree window = new ZBTree(d);

	ZBTree.Node root = zbtree.getRoot();
	stack.push(root);

	while (!stack.isEmpty()) {
	    ZBTree.Node node = stack.pop();
	    float[] rzRegionHigh = node.getUpperBound();
	    boolean nodeIsDominated = tc.isPointDominated(rzRegionHigh, window);
	    if (nodeIsDominated) {
		continue;
	    }
	    // node is not dominated by some window point
	    int size = node.getSize();
	    if (!node.isLeaf()) {
		// node is an internal node:
		// add all its children to stack
		ZBTree.Node[] children = node.getChildren();
		for (int i = 0; i < size; i++) {
		    ZBTree.Node child = children[i];
		    stack.push(child);
		}
	    } else {
		// node is a leaf node:
		// add all its child points to window that are not dominated by some window point
		for (int i = size - 1; i >= 0; i--) {
		    float[] point = node.getPoint(i);
		    boolean pointIsDominated = tc.isPointDominated(point, window);
		    if (!pointIsDominated) {
			window.insertPoint(point);
		    }
		}
	    }
	}
	cpuCost = tc.getNumberOfComparisons();
	totalTimeNS = System.nanoTime() - startTime;
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
	buf.append("ZSearch (List)");
	return buf.toString();
    }

    @Override
    public String getShortName() {
	StringBuffer sb = new StringBuffer("ZSearch (List)");
	return sb.toString();
    }
}