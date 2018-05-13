package ifis.skysim2.data.trees.zbtree;

import ifis.skysim2.data.sources.PointSource;
import ifis.skysim2.common.tools.ArraySorter;
import java.util.Arrays;

// Compact bulkloading
// Lee, Zheng, Li, Lee: Approaching the skyline in Z order

public class BulkLoaderCompact implements BulkLoader {
    @Override
    public ZBTree bulkLoad(PointSource data) {
	return bulkLoad(data, ZBTree.DEFAULT_NODE_CAPACITY_MAX);
    }

    @Override
    public ZBTree bulkLoad(PointSource data, int nodeCapacityMax) {
	final int d = data.getD();
	int n = data.size();
	long sortvals[] = new long[n];

	for (int i = 0; i < n; i++) {
	    final float[] point = data.get(i);
	    sortvals[i] = ZOrderHelper.getZAddress(point);
	}
	
	float[] pointArray = data.toFlatArray();
	ArraySorter.longArraySort(pointArray, d, sortvals);

	ZBTree.Node[] nodes = new ZBTree.Node[(int)Math.ceil((double)n / nodeCapacityMax)];

        int nextNode = 0; // index of node to be created
        int windowBegin = 0;
        int windowEnd = 0;
        while (windowBegin < n) {
	    // construct baseline window and RZ region
	    windowEnd += nodeCapacityMax;
	    if (windowEnd > n) {
		windowEnd = n;
	    }
	    ZBTree.Node node = ZBTree.getNewLeafNode(d, nodeCapacityMax);
	    for (int i = windowBegin; i < windowEnd; i++) {
		final float[] point = Arrays.copyOfRange(pointArray, i * d, i * d + d);
		long z = sortvals[i];
		ZBTree.appendPointToNonFullLeafNode(node, point, z);
	    }
	    nodes[nextNode] = node;
	    nextNode++;
	    windowBegin = windowEnd;
	}

	// we will re-use the array "nodes" to store parent nodes
	int depth = 0;
	n = nodes.length;
	while (n > 1) {
	    depth++;
	    int nextParentNode = 0; // index of next parent node to be created
	    // initialize sliding window to be empty
	    windowBegin = 0;
	    windowEnd = 0;
	    while (windowBegin < n) {
		// construct baseline window and RZ region
		windowEnd += nodeCapacityMax;
		if (windowEnd > n) {
		    windowEnd = n;
		}
		ZBTree.Node newParentNode = ZBTree.getNewInternalNode(d, nodeCapacityMax);
		for (int i = windowBegin; i < windowEnd; i++) {
		    ZBTree.Node currentNode = nodes[i];
		    ZBTree.appendNodeToNonFullInternalNode(newParentNode, currentNode);
		}
		nodes[nextParentNode] = newParentNode;
		nextParentNode++;
		windowBegin = windowEnd;
	    }
	    n = nextParentNode;
	}
	return new ZBTree(d, nodes[0], depth);
    }
}
