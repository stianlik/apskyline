package ifis.skysim2.data.trees.zbtree;

import ifis.skysim2.common.datastructures.SimpleLinkedListQueue;
import ifis.skysim2.data.tools.PointComparator;
import ifis.skysim2.data.tools.PointRelationship;

public class ZBTreeComparator {

    private int numcomps = 0;

    // compares a data point p to a ZB-tree t
    // returns true if some point in t dominates p
    // returns false otherwise
    public final boolean isPointDominated(final float[] point, final ZBTree tree) {
	SimpleLinkedListQueue<ZBTree.Node> queue = new SimpleLinkedListQueue<ZBTree.Node>();
	queue.enqueue(tree.getRoot());

	while (!queue.isEmpty()) {
	    ZBTree.Node node = queue.dequeue();
	    int size = node.getSize();
	    if (node.isLeaf()) {
		// leaf node:
		// check whether point is dominated by one of node's children
		// if so, return true
		for (int i = size - 1; i >= 0; i--) {
		    float[] q = node.getPoint(i);
		    PointRelationship dom = PointComparator.compare(q, point);
		    numcomps++;
		    if (dom == PointRelationship.DOMINATES) {
			return true;
		    }
		}
	    } else {
		// internal node:
		// check whether point is dominated by one of node's child nodes
		// for each nondominating child node,
		ZBTree.Node[] children = node.getChildren();
		for (int i = size - 1; i >= 0; i--) {
		    ZBTree.Node child = children[i];
		    float[] rzLowerChild = child.getLowerBound();
		    PointRelationship dom = PointComparator.compare(rzLowerChild, point);
		    numcomps++;
		    if (dom == PointRelationship.DOMINATES) {
			// child's lower RZ bound dominates point
			return true;
		    } else {
			// child's lower RZ bound does not dominate point
			float[] rzUpperChild = child.getUpperBound();
			PointRelationship dom2 = PointComparator.compare(rzUpperChild, point);
			numcomps++;
			if (dom2 == PointRelationship.DOMINATES) {
			    // child's upper RZ bound dominates point;
			    // if this wouldn't be the case, all points in child are incomparable to point
			    queue.enqueue(child);
			}
		    }
		}
	    }
	}
	return false;
    }

    public int getNumberOfComparisons() {
	return numcomps;
    }
}