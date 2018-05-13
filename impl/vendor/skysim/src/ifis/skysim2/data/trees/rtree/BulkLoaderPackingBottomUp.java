package ifis.skysim2.data.trees.rtree;

import ifis.skysim2.data.trees.rtree.RTree.Node;
import java.util.Collection;

// Bulkload an R-Tree by using bottom-up packing, i.e.
// (0) Wrap the original data points into nodes (one node each)
// (1) BREAK if there is only one node left; this will be the root node of the RTree
// (2) Order the nodes according to some criterion
// (3) Group each block of CAPACITY_MAX nodes into a new parent node
//     (the last parent node might contain fewer nodes)
// (4) Return to (1), now processing the list of parent nodes

public abstract class BulkLoaderPackingBottomUp implements BulkLoader {
    @Override
    public RTree bulkLoad(Collection<float[]> data) {
	RTree.Node[] nodes = wrapData(data);
	// we will re-use the array "nodes" to store parent nodes
	int n = nodes.length;
	while (n > 1) {
	    sortNodes(nodes, 0, n);
	    RTree.Node currentParentNode = new RTree.Node(nodes[0]);
	    int j = 0; // index of current parent node
	    for (int i = 1; i < n; i++) {
		RTree.Node currentNode = nodes[i];
		boolean parentNodeIsFull = (! currentParentNode.appendChild(currentNode));
		if (parentNodeIsFull) {
		    nodes[j] = currentParentNode;
		    j++;
		    currentParentNode = new RTree.Node(currentNode);
		}
	    }
	    nodes[j] = currentParentNode;
	    n = j + 1;
	}
	return new RTree(nodes[0]);
    }

    private static RTree.Node[] wrapData(Collection<float[]> data) {
	int n = data.size();
	RTree.Node[] nodes = new RTree.Node[n];
	int i = 0;
	for (float[] point: data) {
	    nodes[i] = new RTree.Node(point);
	    i++;
	}
	return nodes;
    }

    abstract void sortNodes(Node[] nodes, int begin, int end);
}
