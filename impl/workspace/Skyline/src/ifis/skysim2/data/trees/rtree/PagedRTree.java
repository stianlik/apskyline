package ifis.skysim2.data.trees.rtree;

import java.util.Arrays;

public class PagedRTree {
    protected static final int DEFAULT_NODE_CAPACITY_MIN = 10;
    protected static final int DEFAULT_NODE_CAPACITY_MAX = 20;

    private Node root;
    private final int d;
    private int depth;

    public int nodeCapacityMin;
    public int nodeCapacityMax;

    public PagedRTree(int d, int nodeCapacityMin, int nodeCapacityMax) {
	this.d = d;
	setNodeCapacities(nodeCapacityMin, nodeCapacityMax);
	root = getNewLeafNode();
	depth = 0;
    }

    public PagedRTree(int d) {
	this(d, DEFAULT_NODE_CAPACITY_MIN, DEFAULT_NODE_CAPACITY_MAX);
    }

    protected PagedRTree(int d, int nodeCapacityMin, int nodeCapacityMax, Node root, int depth) {
	this.d = d;
	setNodeCapacities(nodeCapacityMin, nodeCapacityMax);
	this.root = root;
	this.depth = depth;
    }

    protected PagedRTree(int d, Node root, int depth) {
	this(d, DEFAULT_NODE_CAPACITY_MIN, DEFAULT_NODE_CAPACITY_MAX, root, depth);
    }

    private void setNodeCapacities(int nodeCapacityMin, int nodeCapacityMax) {
	this.nodeCapacityMin = nodeCapacityMin;
	this.nodeCapacityMax = nodeCapacityMax;
    }

    private Node getNewLeafNode() {
	return getNewLeafNode(d, nodeCapacityMax);
    }

    protected static Node getNewLeafNode(int d, int nodeCapacityMax) {
	return new Node(d, true, nodeCapacityMax);
    }

    private Node getNewInternalNode() {
	return getNewInternalNode(d, nodeCapacityMax);
    }

    protected static Node getNewInternalNode(int d, int nodeCapacityMax) {
	return new Node(d, false, nodeCapacityMax);
    }

//    public String deepToString() {
//	return root.deepToString();
//    }

    // Node should exhibit memory locality, i.e. it should be stored
    // in a single memory page, which usually is 4096 bytes long
    public static class Node {
	private static int TRAVERSE_INDENT = 4;

	private int size;
	private final int d;
	private final boolean isLeaf;


	// Used by leaf nodes only
	private final float[] points;
	private final float[] mbrLow;
	private final float[] mbrHigh;

	// Used by internal nodes only
	private final Node[] children;
	private final float[] mbrsChildrenLow;
	private final float[] mbrsChildrenHigh;



	// creates an empty node
	private Node(int d, boolean isLeaf, int nodeCapacityMax) {
	    this.d = d;
	    size = 0;
	    this.isLeaf = isLeaf;
	    mbrLow = new float[d];
	    mbrHigh = new float[d];
	    if (isLeaf) {
		mbrsChildrenLow = null;
		mbrsChildrenHigh = null;
		points = new float[nodeCapacityMax * d];
		children = null;
	    } else {
		mbrsChildrenLow = new float[nodeCapacityMax * d];
		mbrsChildrenHigh = new float[nodeCapacityMax * d];
		points = null;
		children = new Node[nodeCapacityMax];
	    }
	}

	public boolean isLeaf() {
	    return isLeaf;
	}

	public float[] getUpperBound() {
	    return Arrays.copyOf(mbrHigh, d);
	}

	public Node[] getChildren() {
	    return Arrays.copyOf(children, size);
	}

	public int getSize() {
	    return size;
	}

	public float[] getPoint(int i) {
	    return Arrays.copyOfRange(points, d * i, d * (i + 1));
	}

//	@Override
//	public String toString() {
//	    if (isLeaf) {
//		return String.format("%s: %s  (Size: %d)        RZ: %s / %s        Points: %s",
//			Integer.toHexString(hashCode()),
//			Arrays.toString(divideArray(Arrays.copyOfRange(keys, 0, size), STRING_SHORTENER)),
//			size,
//			Arrays.toString(rzBoundLow),
//			Arrays.toString(rzBoundHigh),
//			Arrays.toString(Arrays.copyOfRange(points, 0, size * d)));
//	    } else {
//		return String.format("%s: %s / %s  (Size: %d)        RZ_low: %s   RZ_high: %s        Bounds: %s / %s",
//			Integer.toHexString(hashCode()),
//			Arrays.toString(divideArray(Arrays.copyOfRange(keysChildrenLow, 0, size), STRING_SHORTENER)),
//			Arrays.toString(divideArray(Arrays.copyOfRange(keysChildrenHigh, 0, size), STRING_SHORTENER)),
//			size,
//			Arrays.toString(Arrays.copyOfRange(rzBoundsChildrenLow, 0, size * d)),
//			Arrays.toString(Arrays.copyOfRange(rzBoundsChildrenHigh, 0, size * d)),
//			Arrays.toString(rzBoundLow),
//			Arrays.toString(rzBoundHigh));
//	    }
//	}
//
//	private static long[] divideArray(long[] a, long d) {
//	    int n = a.length;
//	    long[] b = new long[n];
//	    for (int i = 0; i < n; i++) {
//		b[i] = a[i] / d;
//	    }
//	    return b;
//	}
//
//	public String deepToString() {
//	    return traverse(0).toString();
//	}
//
//	private StringBuffer traverse(int level) {
//	    StringBuffer out = new StringBuffer();
//	    out.append(String.format("%s%s\n", StringUtils.repeat(' ', level), this));
//	    if (!isLeaf) {
//		for (int i = 0; i < size; i++) {
//		    Node child = children[i];
//		    out.append(child.traverse(level + TRAVERSE_INDENT));
//		}
//	    }
//	    return out;
//	}
    }
}
