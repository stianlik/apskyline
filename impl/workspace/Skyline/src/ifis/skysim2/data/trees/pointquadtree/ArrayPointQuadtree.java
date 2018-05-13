package ifis.skysim2.data.trees.pointquadtree;

import ifis.skysim2.data.points.LinkedPointList;
import ifis.skysim2.data.points.PointList;
import java.util.Arrays;
import java.util.Iterator;

public class ArrayPointQuadtree implements PointQuadtree {

    public Node root = null;
    final int d;
    final int splitDegree;
    final int bottomNodeLevel;

    /* - bottomNodeLevel is the level at which the listNodes are located
     * - the root level is 0
     */
    public ArrayPointQuadtree(float[] point, int bottomNodeLevel) {
	d = point.length;
	splitDegree = 1 << d;
	this.bottomNodeLevel = bottomNodeLevel;
	if (this.bottomNodeLevel > 0) {
	    root = new Node(point, 0);
	} else {
	    root = new Node(0);
	}
    }

    public class Node {
	public boolean isBottomNode;
	int level;

	// only for InternalNodes
	public final float[] point;
	public final Node[] children;
	
	// only for BottomNodes
	public final PointList points;

	public Node(float[] point, int level) {
	    this.level = level;
	    isBottomNode = (level == bottomNodeLevel);
	    if (isBottomNode) {
		this.point = null;
		children = null;
		points = new LinkedPointList(d);
		if (point != null) {
		    points.add(point);
		}
	    } else {
		// internal node
		if (point == null) {
		    throw new UnsupportedOperationException("Internal nodes must contain a split point.");
		}
		this.point = Arrays.copyOf(point, d);
		this.children = new Node[splitDegree];
		points = null;
	    }
	}

	public Node(int level) {
	    this(null, level);
	}

	public boolean isChildEmpty(int i) {
	    if (isBottomNode) {
		throw new UnsupportedOperationException("Not supported yet.");
	    }
	    return children[i] == null;
	}

	public Node getChild(int i) {
	    if ((isBottomNode) || (children[i] == null)) {
		throw new UnsupportedOperationException("Not supported yet.");
	    }
	    return children[i];
	}

	public void addToChild(int i, float[] point) {
	    if (children[i] != null) {
		throw new UnsupportedOperationException("Not supported yet.");
	    }
	    children[i] = new Node(point, level + 1);
	}

	public Iterator<Node> childrenIterator() {
	    if (isBottomNode) {
		throw new UnsupportedOperationException("Not supported yet.");
	    }
	    return new ChildrenIterator();
	}

	public Iterator<Node> childrenIteratorMoreZerosThanMask(int mask) {
	    if (isBottomNode) {
		throw new UnsupportedOperationException("Not supported yet.");
	    }
	    return new ChildrenIteratorMoreZerosThanMask(mask);
	}

	public Iterator<Node> childrenIteratorMoreOnesThanMask(int mask) {
	    if (isBottomNode) {
		throw new UnsupportedOperationException("Not supported yet.");
	    }
	    return new ChildrenIteratorMoreOnesThanMask(mask);
	}


	private abstract class AbstractChildrenIterator implements Iterator<Node> {
	    int currentIndex = -1;
	    int nextIndex;

	    @Override
	    public boolean hasNext() {
		return nextIndex != -1;
	    }

	    @Override
	    public Node next() {
		currentIndex = nextIndex;
		nextIndex = findNext();
		return children[currentIndex];
	    }

	    @Override
	    public void remove() {
		children[currentIndex] = null;
	    }

	    abstract int findNext();
	}

	/*
	 * Iterates over all children whose successorshipMask
	 * satisfies successorshipMask | mask == successorshipMask and successorshipMask != mask
	 */
	private class ChildrenIteratorMoreOnesThanMask extends AbstractChildrenIterator {
	    final int mask;

	    private ChildrenIteratorMoreOnesThanMask(int mask) {
		this.mask = mask;
		nextIndex = -1;
		nextIndex = findNext();
	    }

	    @Override
	    int findNext() {
		int current = nextIndex + 1;
		while (current < mask) {
		    if (((mask | current) == mask) && (children[current] != null)) {
			return current;
		    }
		    current++;
		}
		return -1;
	    }
	}
	

	/*
	 * Iterates over all children whose successorshipMask
	 * satisfies successorshipMask & mask == successorshipMask and successorshipMask != mask
	 */
	private class ChildrenIteratorMoreZerosThanMask extends AbstractChildrenIterator {
	    final int mask;

	    private ChildrenIteratorMoreZerosThanMask(int mask) {
		this.mask = mask;
		nextIndex = mask;
		nextIndex = findNext();
	    }

	    @Override
	    int findNext() {
		int current = nextIndex + 1;
		while (current < splitDegree) {
		    if (((mask & current) == mask) && (children[current] != null)) {
			return current;
		    }
		    current++;
		}
		return -1;
	    }
	}

	/*
	 * Iterates over all nonempty children of an internal node
	 */
	private class ChildrenIterator extends AbstractChildrenIterator {

	    private ChildrenIterator() {
		nextIndex = -1;
		nextIndex = findNext();
	    }

	    @Override
	    int findNext() {
		int current = nextIndex + 1;
		while (current < splitDegree) {
		    if (children[current] != null) {
			return current;
		    }
		    current++;
		}
		return -1;
	    }
	}
    }
}
