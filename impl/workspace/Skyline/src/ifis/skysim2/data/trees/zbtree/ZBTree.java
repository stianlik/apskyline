package ifis.skysim2.data.trees.zbtree;

// currently, only ascending sort order is supported (especially important for using binarySearch)

import ifis.skysim2.data.generator.DataGeneratorIndependent;
import ifis.skysim2.data.sources.PointSource;
import ifis.skysim2.data.sources.PointSourceRAM;
import ifis.skysim2.common.tools.ArraySearch;
import ifis.skysim2.common.tools.StringUtils;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;


public class ZBTree implements PointSource {

    public static void main(String[] args) {
	int n = 23;
	int d = 2;

	generatePagedTree(n, d);
    }

    private static void generatePagedTree(int n, int d) {
	DataGeneratorIndependent dg = new DataGeneratorIndependent();
	dg.resetToDefaultSeed();
	float[] data = dg.generate(d, n);
	List<float[]> ps = new PointSourceRAM(d, data);
	ZBTree tree = new ZBTree(d, 2, 4);
	for (float[] point : ps) {
	    System.out.format("To be inserted: %40s  (%d)\n", Arrays.toString(point), ZOrderHelper.getZAddress(point) / Node.STRING_SHORTENER);
	    System.out.println();
	    System.out.println();
	    tree.insertPoint(point);
	    System.out.println(tree.deepToString());
	    System.out.println();
	    System.out.println();
	}
    }

    // TODO: should be chosen to maximize page usage
    // Java's long: 64 bit
    // Java's float: 32 bit
    protected static final int DEFAULT_NODE_CAPACITY_MIN = 10;
    protected static final int DEFAULT_NODE_CAPACITY_MAX = 20;
    
    private Node root;
    private final int d;
    private int depth;
    private int size;

    public int nodeCapacityMin;
    public int nodeCapacityMax;

    public ZBTree(int d, int nodeCapacityMin, int nodeCapacityMax) {
	this.d = d;
	setNodeCapacities(nodeCapacityMin, nodeCapacityMax);
	root = getNewLeafNode();
	depth = 0;
	size = 0;
    }

    public ZBTree(int d) {
	this(d, DEFAULT_NODE_CAPACITY_MIN, DEFAULT_NODE_CAPACITY_MAX);
    }

    protected ZBTree(int d, int nodeCapacityMin, int nodeCapacityMax, Node root, int depth) {
	this.d = d;
	setNodeCapacities(nodeCapacityMin, nodeCapacityMax);
	this.root = root;
	this.depth = depth;
	size = 0;
    }

    protected ZBTree(int d, Node root, int depth) {
	this(d, DEFAULT_NODE_CAPACITY_MIN, DEFAULT_NODE_CAPACITY_MAX, root, depth);
    }

    private void setNodeCapacities(int nodeCapacityMin, int nodeCapacityMax) {
	this.nodeCapacityMin = nodeCapacityMin;
	this.nodeCapacityMax = nodeCapacityMax;
    }

    // TODO: this is no good design, we need "clever" iterators within this package
    public Node getRoot() {
	return root;
    }

    public void insertPoint(float[] p) {
	// Find a leaf node for p
	final long z = ZOrderHelper.getZAddress(p);

	final Node[] path = new Node[depth + 1];
	final int[] pathI = new int[depth + 1];
	findPathtoLeafNode(root, z, path, pathI);

	int level = depth;
	Node n = path[level];
	int i = pathI[level];

	// find parent of leaf node
	Node nParent = null;
	int iParent = -1;
	if (level > 0) {
	    nParent = path[level - 1];
	    iParent = pathI[level - 1];
	}

	// remember whether zRegions of parent nodes get changed by the update
	boolean updateLeftmost = false;
	boolean updateRightmost = false;
	if (i == 0) {
	    updateLeftmost = true;
	} else if (i == n.size) {
	    updateRightmost = true;
	}

	// insert into leaf node and account for splittings
	Node nNew = insertPointIntoLeafNode(n, nParent, iParent, p, z, i, nodeCapacityMin, nodeCapacityMax);
	while (nNew != null) {
	    // there is still work to do
	    level--;
	    if (level < 0) {
		// the root has been split
		root = getNewInternalNode();
		if (n.isLeaf) {
		    // n and nNew are leaf nodes
		    root.keysChildrenLow[0] = n.keys[0];
		    root.keysChildrenHigh[0] = n.keys[n.size - 1];
		    root.keysChildrenLow[1] = nNew.keys[0];
		    root.keysChildrenHigh[1] = nNew.keys[nNew.size - 1];
		} else {
		    // n and nNew are internal nodes
		    root.keysChildrenLow[0] = n.keysChildrenLow[0];
		    root.keysChildrenHigh[0] = n.keysChildrenHigh[n.size - 1];
		    root.keysChildrenLow[1] = nNew.keysChildrenLow[0];
		    root.keysChildrenHigh[1] = nNew.keysChildrenHigh[nNew.size - 1];
		}
		root.children[0] = n;
		root.children[1] = nNew;
		System.arraycopy(n.rzBoundLow, 0, root.rzBoundsChildrenLow, 0, n.d);
		System.arraycopy(n.rzBoundHigh, 0, root.rzBoundsChildrenHigh, 0, n.d);
		System.arraycopy(nNew.rzBoundLow, 0, root.rzBoundsChildrenLow, n.d, n.d);
		System.arraycopy(nNew.rzBoundHigh, 0, root.rzBoundsChildrenHigh, n.d, n.d);
		root.size = 2;
		updateRzBoundsOfInternalNode(root);
		depth++;
		nNew = null;
//		System.out.println("OOO " + root.children[1].toStringFull());
	    } else {
		n = path[level];
		i = pathI[level];
		if (level > 0) {
		    nParent = path[level - 1];
		    iParent = pathI[level - 1];
		} else {
		    nParent = null;
		}
		// account for zRegion changes in parents
		if ((updateLeftmost) && (i > 0)) {
		    updateLeftmost = false;
		} else if ((updateRightmost) && (i < n.size)) {
		    updateRightmost = false;
		}
		// nNew must be inserted as child i + 1 of node n
//		System.out.println("XXX n: " + n.toStringFull());
//		System.out.println("XXX nNew: " + nNew.toStringFull());
		nNew = insertNodeIntoInternalNode(n, nParent, iParent, nNew, i + 1, nodeCapacityMin, nodeCapacityMax);
	    }
	}
	// Now, nNew is null, i.e. no changes of the tree have been made above the node path[level]
	// Fix keys and rzBounds of parents, if needed
//	System.out.println("level: " + level);
//	System.out.println("path: " + Arrays.toString(path));
//	System.out.println("pathI: " + Arrays.toString(pathI));
//	System.out.println("updateLeftmost: " + updateLeftmost);
//	System.out.println("updateRightmost: " + updateRightmost);

	while ((updateLeftmost || updateRightmost) && (level > 0)) {
	    level--;
	    n = path[level];
	    i = pathI[level];
	    if (updateLeftmost) {
		n.keysChildrenLow[i] = z;
		if (i > 0) {
		    updateLeftmost = false;
		}
	    } else {
		n.keysChildrenHigh[i] = z;
		if (i < n.size) {
		    updateRightmost = false;
		}
	    }
	    updateRzBoundsOfInternalNode(n);
	    if (i > 0) {
		updateLeftmost = false;
	    }
	}
	size++;
    }

    // delete a point stored in some leaf node
    public void deletePoint() {
	// TODO: ...
    }

    protected static void appendNodeToNonFullInternalNode(Node n, Node nChild) {
	insertNodeIntoNonFullInternalNode(n, nChild, n.size);
    }

    private static Node insertPointIntoLeafNode(Node n, Node nParent, int iParent, float[] p, long z, int i, int nodeCapacityMin, int nodeCapacityMax) {
	if (n.size < nodeCapacityMax) {
	    insertPointIntoNonFullLeafNode(n, p, z, i);
	    return null;
	} else {
	    // node n has to be split
	    return splitFullLeafNodeAndInsert(n, nParent, iParent, p, z, i, nodeCapacityMin, nodeCapacityMax);
	}
    }

    // insert node nChild into node n at position i
    private static Node insertNodeIntoInternalNode(Node n, Node nParent, int iParent, Node nChild, int i, int nodeCapacityMin, int nodeCapacityMax) {
	if (n.size < nodeCapacityMax) {
	    insertNodeIntoNonFullInternalNode(n, nChild, i);
	    return null;
	} else {
	    // node n has to be split
	    return splitFullInternalNodeAndInsert(n, nParent, iParent, nChild, i, nodeCapacityMin, nodeCapacityMax);
	}
    }

    private static void insertPointIntoNonFullLeafNode(Node n, float[] p, long z, int i) {
	// shift data
	final int length = n.size - i;
	System.arraycopy(n.keys, i, n.keys, i + 1, length);
	System.arraycopy(n.points, n.d * i, n.points, n.d * (i + 1), n.d * length);
	// replace data
	n.keys[i] = z;
	System.arraycopy(p, 0, n.points, n.d * i, n.d);
	n.size++;
	if ((i == 0) || (i == n.size - 1)) {
//	    System.out.println("Update rzBound of leaf node " + n + " (i: " + i + ")");
	    updateRzBoundsOfLeafNode(n);
	}
    }

    private static void insertNodeIntoNonFullInternalNode(Node n, Node nChild, final int i) {
	// shift data
	final int length = n.size - i;
	System.arraycopy(n.children, i, n.children, i + 1, length);
	System.arraycopy(n.keysChildrenLow, i, n.keysChildrenLow, i + 1, length);
	System.arraycopy(n.keysChildrenHigh, i, n.keysChildrenHigh, i + 1, length);
	System.arraycopy(n.rzBoundsChildrenLow, n.d * i, n.rzBoundsChildrenLow, n.d * (i + 1), n.d * length);
	System.arraycopy(n.rzBoundsChildrenHigh, n.d * i, n.rzBoundsChildrenHigh, n.d * (i + 1), n.d * length);
	// replace data
	if (nChild.isLeaf) {
	    n.keysChildrenLow[i] = nChild.keys[0];
	    n.keysChildrenHigh[i] = nChild.keys[nChild.size - 1];
	} else {
	    // nChild is an internal node
	    n.keysChildrenLow[i] = nChild.keysChildrenLow[0];
	    n.keysChildrenHigh[i] = nChild.keysChildrenHigh[nChild.size - 1];
	}
	n.children[i] = nChild;
	System.arraycopy(nChild.rzBoundLow, 0, n.rzBoundsChildrenLow, n.d * i, n.d);
	System.arraycopy(nChild.rzBoundHigh, 0, n.rzBoundsChildrenHigh, n.d * i, n.d);
	n.size++;
	if ((i == 0) || (i == n.size - 1)) {
	    updateRzBoundsOfInternalNode(n);
	}
    }

    protected static void appendPointToNonFullLeafNode(Node n, float[] p, long z) {
	insertPointIntoNonFullLeafNode(n, p, z, n.size);
    }

    // splits a leaf node containing exactly CAPACITY_MAX points
    // and inserts a new point p into one of the two nodes
    private static Node splitFullLeafNodeAndInsert(Node n, Node nParent, int iParent, float[] p, long z, int i, int nodeCapacityMin, int nodeCapacityMax) {
	// find split such that total area covered is minimized
	// Index positions refer to {0, ..., nodeCapacityMax} as if p would have been inserted
	int splitFromIndexMin = nodeCapacityMin;
	int splitFromIndexMax = nodeCapacityMax - nodeCapacityMin + 1;
	float areaMin = Float.MAX_VALUE;
	int splitFromIndex = -1;
	long zLeft;
	if (i == 0) {
	    zLeft = z;
	} else {
	    zLeft = n.keys[0];
	}
	long zRight;
	if (i == n.size) {
	    zRight = z;
	} else {
	    zRight = n.keys[n.size - 1];
	}
	for (int j = splitFromIndexMin; j <= splitFromIndexMax; j++) {
	    float[] rzLowHighLeft;
	    float[] rzLowHighRight;
	    if (j < i) {
		// p will be inserted into the right node
		// p is not the first point in the right node
		rzLowHighLeft = ZOrderHelper.getRZLowHigh(zLeft, n.keys[j - 1], n.d);
		rzLowHighRight = ZOrderHelper.getRZLowHigh(n.keys[j], zRight, n.d);
	    } else if (j == i) {
		// p will be inserted into the right node
		// p is the first point in the right node
		rzLowHighLeft = ZOrderHelper.getRZLowHigh(zLeft, n.keys[j - 1], n.d);
		rzLowHighRight = ZOrderHelper.getRZLowHigh(z, zRight, n.d);
	    } else if (j == i + 1)  {
		// p will be inserted into the left node
		// p is the last point in the left node
		rzLowHighLeft = ZOrderHelper.getRZLowHigh(zLeft, z, n.d);
		rzLowHighRight = ZOrderHelper.getRZLowHigh(n.keys[j - 1], zRight, n.d);
	    } else {
		// p will be inserted into the left node
		// p is not the last point in the left node
		rzLowHighLeft = ZOrderHelper.getRZLowHigh(zLeft, n.keys[j - 2], n.d);
		rzLowHighRight = ZOrderHelper.getRZLowHigh(n.keys[j - 1], zRight, n.d);
	    }
	    float areaLeft = getArea(rzLowHighLeft, 0, rzLowHighLeft, n.d, n.d);
	    float areaRight = getArea(rzLowHighRight, 0, rzLowHighRight, n.d, n.d);
	    float area = areaLeft + areaRight;
	    if (area < areaMin) {
		areaMin = area;
		splitFromIndex = j;
	    }
//	    System.out.format("Split point %d  -->  Area left: %.3f  //  Area right: %.3f  // Area total: %.3f%n", j, areaLeft, areaRight, area);
	}
//	System.out.format("===> Choosing split point %d%n", splitFromIndex);
	// Split (without inserting p)
	Node nNew = getNewLeafNode(n.d, nodeCapacityMax);
	int sizeAfterSplitLeft;
	if (splitFromIndex <= i) {
	    // p will be inserted into the right node
	    sizeAfterSplitLeft = splitFromIndex;
	} else {
	    sizeAfterSplitLeft = splitFromIndex - 1;
	}
	int sizeAfterSplitRight = nodeCapacityMax - sizeAfterSplitLeft;
	int splitFromIndexReal;
	if (splitFromIndex <= i) {
	    // p will be inserted into the right node
	    splitFromIndexReal = splitFromIndex;
	} else {
	    // p will be inserted into the left node
	    splitFromIndexReal = splitFromIndex - 1;
	}
	System.arraycopy(n.keys, splitFromIndexReal, nNew.keys, 0, sizeAfterSplitRight);
	System.arraycopy(n.points, n.d * splitFromIndexReal, nNew.points, 0, n.d * sizeAfterSplitRight);
	n.size = sizeAfterSplitLeft;
	nNew.size = sizeAfterSplitRight;
	updateRzBoundsOfLeafNode(n);
	updateRzBoundsOfLeafNode(nNew);
	// Insert p
	if (i < splitFromIndex) {
	    // insert p into n
	    insertPointIntoNonFullLeafNode(n, p, z, i);
	} else {
	    // insert p into nNew
	    insertPointIntoNonFullLeafNode(nNew, p, z, i - sizeAfterSplitLeft);
	}
	// update parent of n
	if (nParent != null) {
	    final int rightKeyN = n.size - 1;
	    nParent.keysChildrenHigh[iParent] = n.keys[rightKeyN];
	    System.arraycopy(n.rzBoundLow, 0, nParent.rzBoundsChildrenLow, iParent, n.d);
	    System.arraycopy(n.rzBoundHigh, 0, nParent.rzBoundsChildrenHigh, iParent, n.d);
	}
	return nNew;
    }

    // splits an internal node containing exactly CAPACITY_MAX points
    // and inserts a new node nChild into one of the two nodes
    private static Node splitFullInternalNodeAndInsert(Node n, Node nParent, int iParent, Node nChild, int i, int nodeCapacityMin, int nodeCapacityMax) {
	// find split such that total area covered is minimized
	// Index positions refer to {0, ..., nodeCapacityMax} as if p would have been inserted
	int splitFromIndexMin = nodeCapacityMin;
	int splitFromIndexMax = nodeCapacityMax - nodeCapacityMin + 1;
	float areaMin = Float.MAX_VALUE;
	int splitFromIndex = -1;
	long zLeft;
	if (i == 0) {
	    zLeft = getLowZ(nChild, 0);
	} else {
	    zLeft = n.keysChildrenLow[0];
	}
	long zRight;
	if (i == n.size) {
	    zRight = getHighZ(nChild, nChild.size - 1);
	} else {
	    zRight = n.keysChildrenHigh[n.size - 1];
	}
	for (int j = splitFromIndexMin; j <= splitFromIndexMax; j++) {
	    float[] rzLowHighLeft;
	    float[] rzLowHighRight;
	    if (j < i) {
		// nChild will be inserted into the right node
		// nChild is not the first point in the right node
		rzLowHighLeft = ZOrderHelper.getRZLowHigh(zLeft, getHighZ(n, j - 1), n.d);
		rzLowHighRight = ZOrderHelper.getRZLowHigh(getLowZ(n, j), zRight, n.d);
	    } else if (j == i) {
		// nChild will be inserted into the right node
		// nChild is the first point in the right node
		rzLowHighLeft = ZOrderHelper.getRZLowHigh(zLeft, getHighZ(n, j - 1), n.d);
		rzLowHighRight = ZOrderHelper.getRZLowHigh(getLowZ(nChild, 0), zRight, n.d);
	    } else if (j == i + 1)  {
		// nChild will be inserted into the left node
		// nChild is the last point in the left node
		rzLowHighLeft = ZOrderHelper.getRZLowHigh(zLeft, getHighZ(nChild, nChild.size - 1), n.d);
		rzLowHighRight = ZOrderHelper.getRZLowHigh(getLowZ(n, j - 1), zRight, n.d);
	    } else {
		// nChild will be inserted into the left node
		// nChild is not the last point in the left node
		rzLowHighLeft = ZOrderHelper.getRZLowHigh(zLeft, getHighZ(n, j - 2), n.d);
		rzLowHighRight = ZOrderHelper.getRZLowHigh(getLowZ(n, j - 1), zRight, n.d);
	    }
	    float areaLeft = getArea(rzLowHighLeft, 0, rzLowHighLeft, n.d, n.d);
	    float areaRight = getArea(rzLowHighRight, 0, rzLowHighRight, n.d, n.d);
	    float area = areaLeft + areaRight;
	    if (area < areaMin) {
		areaMin = area;
		splitFromIndex = j;
	    }
//	    System.out.format("Split point %d  -->  Area left: %.3f  //  Area right: %.3f  // Area total: %.3f%n", j, areaLeft, areaRight, area);
	}
//	System.out.format("===> Choosing split point %d%n", splitFromIndex);
	// Split (without inserting nChild)
	Node nNew = getNewInternalNode(n.d, nodeCapacityMax);
	int sizeAfterSplitLeft;
	if (splitFromIndex <= i) {
	    // nChild will be inserted into the right node
	    sizeAfterSplitLeft = splitFromIndex;
	} else {
	    // nChild will be inserted into the left node
	    sizeAfterSplitLeft = splitFromIndex - 1;
	}
	int sizeAfterSplitRight = nodeCapacityMax - sizeAfterSplitLeft;
	int splitFromIndexReal;
	if (splitFromIndex <= i) {
	    // nChild will be inserted into the right node
	    splitFromIndexReal = splitFromIndex;
	} else {
	    // nChild will be inserted into the left node
	    splitFromIndexReal = splitFromIndex - 1;
	}
	System.arraycopy(n.children, splitFromIndexReal, nNew.children, 0, sizeAfterSplitRight);
	System.arraycopy(n.keysChildrenLow, splitFromIndexReal, nNew.keysChildrenLow, 0, sizeAfterSplitRight);
	System.arraycopy(n.keysChildrenHigh, splitFromIndexReal, nNew.keysChildrenHigh, 0, sizeAfterSplitRight);
	System.arraycopy(n.rzBoundsChildrenLow, n.d * splitFromIndexReal, nNew.rzBoundsChildrenLow, 0, n.d * sizeAfterSplitRight);
	System.arraycopy(n.rzBoundsChildrenHigh, n.d * splitFromIndexReal, nNew.rzBoundsChildrenHigh, 0, n.d * sizeAfterSplitRight);
	n.size = sizeAfterSplitLeft;
	nNew.size = sizeAfterSplitRight;
	updateRzBoundsOfInternalNode(n);
	updateRzBoundsOfInternalNode(nNew);
	// Insert nChild
	if (i < splitFromIndex) {
	    // insert nChild into n
	    insertNodeIntoNonFullInternalNode(n, nChild, i);
	} else {
	    // insert nChild into nNew
	    insertNodeIntoNonFullInternalNode(nNew, nChild, i - sizeAfterSplitLeft);
	}
	// update parent of n
	if (nParent != null) {
	    final int rightKeyN = n.size - 1;
	    nParent.keysChildrenHigh[iParent] = n.keysChildrenHigh[rightKeyN];
	    System.arraycopy(n.rzBoundLow, 0, nParent.rzBoundsChildrenLow, iParent, n.d);
	    System.arraycopy(n.rzBoundHigh, 0, nParent.rzBoundsChildrenHigh, iParent, n.d);
	}
	return nNew;
    }

    private static void updateRzBoundsOfLeafNode(Node n) {
	float[] rzBoundsUpdated = ZOrderHelper.getRZLowHigh(n.keys[0], n.keys[n.size - 1], n.d);
	System.arraycopy(rzBoundsUpdated, 0, n.rzBoundLow, 0, n.d);
	System.arraycopy(rzBoundsUpdated, n.d, n.rzBoundHigh, 0, n.d);
    }

    private static void updateRzBoundsOfInternalNode(Node n) {
	float[] rzBoundsUpdated = ZOrderHelper.getRZLowHigh(n.keysChildrenLow[0], n.keysChildrenHigh[n.size - 1], n.d);
	System.arraycopy(rzBoundsUpdated, 0, n.rzBoundLow, 0, n.d);
	System.arraycopy(rzBoundsUpdated, n.d, n.rzBoundHigh, 0, n.d);
    }

    // fills the arrays path and pathI with information about
    // the path from root to the leaf into which p would be inserted
    // path[0] is the root, pathI[level] is the index of the child chosen in node path [level]
    // pathI[depth] is the index where p would be inserted into the leaf node path[depth]
    private static void findPathtoLeafNode(Node root, long z, Node[] path, int[] pathI) {
	int level = 0;
	Node n = root;

	while (!n.isLeaf) {
	    // find the first lowKey or highKey that is larger than or equal to z
	    int i = keySearch(n.keysChildrenLow, 0, n.size, z);

	    if (i == 0) {
		// insert p left of leftmost child

		// TODO: optimize, see above
	    } else if ((i == n.size - 1) && (n.keysChildrenHigh[n.size - 1] < z)) {
		// insert p right of rightmost child
		
		// TODO: optimize, see above
	    } else if (i == n.size) {
		// insert p right of rightmost child
		i--;

		// TODO: optimize, see above
	    } else if (z <= n.keysChildrenHigh[i - 1]) {
		// insert p into child i - 1
		i--;
	    } else {
		// insert p between child i - 1 and child i
		// choose either i - 1 or i, depending on the new region sizes
		long zLeft = n.keysChildrenLow[0];
		long zRight = n.keysChildrenHigh[n.size - 1];
		float[] rzLeft = ZOrderHelper.getRZLowHigh(zLeft, z, n.d);
		float[] rzRight = ZOrderHelper.getRZLowHigh(z, zRight, n.d);
		float areaLeft = getArea(rzLeft, 0, rzLeft, n.d, n.d);
		float areaRight = getArea(rzRight, 0, rzRight, n.d, n.d);
//		System.out.format("Area left: %.3f  //  Area right: %.3f\n", areaLeft, areaRight);
		if (areaLeft < areaRight) {
		    // choose i - 1
		    i--;

		    // TODO: optimize, see above
		} else {
		    // choose i

		    // TODO: optimize, see above
		}
	    }
	    path[level] = n;
	    pathI[level] = i;
	    level++;
	    n = n.children[i];
	}
	// now, n is a leaf node
	path[level] = n;
	pathI[level] = keySearch(n.keys, 0, n.size, z);
    }

    // Compute the area of a rectangle, where a is the lower left point and b the upper right
    private static float getArea(float[] a, int fromIndexA, float[] b, int fromIndexB, int length) {
	float area = 1;
	do {
	    area *= b[fromIndexB] - a[fromIndexA];
	    fromIndexA++;
	    fromIndexB++;
	    length--;
	} while (length > 0);
	return area;
    }

    // If the key is contained in a:
    //   Returns the index of some key, if it is contained in a
    // Otherwise:
    //   Returns the index of the first element greater than the key or a.length
    //   or a.length if all elements in the array are less than the specified key
    private static int keySearch(long[] a, int fromIndex, int toIndex, long key) {
	// TODO: which one is faster?
	// looks like linear search is faster -> better caching behavior...

//	return ArraySearch.binarySearch(a, fromIndex, toIndex, key);
	return ArraySearch.linearSearch(a, fromIndex, toIndex, key);
    }

    private static long getLowZ(Node n, int i) {
	if (n.isLeaf) {
	    return n.keys[i];
	} else {
	    return n.keysChildrenLow[i];
	}
    }

    private static long getHighZ(Node n, int i) {
	if (n.isLeaf) {
	    return n.keys[i];
	} else {
	    return n.keysChildrenHigh[i];
	}
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

    public String deepToString() {
	return root.deepToString();
    }

    @Override
    public int getD() {
	return d;
    }

    @Override
    public int size() {
	return size;
    }

    @Override
    public boolean isEmpty() {
	return size == 0;
    }

    @Override
    public float[] toFlatArray() {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    // Node should exhibit memory locality, i.e. it should be stored
    // in a single memory page, which usually is 4096 bytes long
    // The design (internal nodes vs. leaf nodes) is ugly but avoids casting
    public static class Node {
	private static int TRAVERSE_INDENT = 4;
	private static long STRING_SHORTENER = 1000000000000000l;
//	private static long STRING_SHORTENER = 1l;

	private int size;
	private final int d;
	private final boolean isLeaf;
	private final float[] rzBoundLow;
	private final float[] rzBoundHigh;


	// Used by leaf nodes only
	private final float[] points;
	private final long[] keys;

	// Used by internal nodes only
	private final Node[] children;
	private final long[] keysChildrenLow;
	private final long[] keysChildrenHigh;
	private final float[] rzBoundsChildrenLow;
	private final float[] rzBoundsChildrenHigh;


	// TODO: Is it really necessary to store rzBounds?
	// They can be computed easily...


	// creates an empty node
	private Node(int d, boolean isLeaf, int nodeCapacityMax) {
	    this.d = d;
	    size = 0;
	    this.isLeaf = isLeaf;
	    rzBoundLow = new float[d];
	    rzBoundHigh = new float[d];
	    if (isLeaf) {
		keys = new long[nodeCapacityMax];
		keysChildrenLow = null;
		keysChildrenHigh = null;
		rzBoundsChildrenLow = null;
		rzBoundsChildrenHigh = null;
		points = new float[nodeCapacityMax * d];
		children = null;
	    } else {
		keys = null;
		keysChildrenLow = new long[nodeCapacityMax];
		keysChildrenHigh = new long[nodeCapacityMax];
		rzBoundsChildrenLow = new float[nodeCapacityMax * d];
		rzBoundsChildrenHigh = new float[nodeCapacityMax * d];
		points = null;
		children = new Node[nodeCapacityMax];
	    }
	}

	public boolean isLeaf() {
	    return isLeaf;
	}

	// returns the upper RZ bound
	public float[] getUpperBound() {
	    return Arrays.copyOf(rzBoundHigh, d);
	}

	// returns the lower RZ bound
	public float[] getLowerBound() {
	    return Arrays.copyOf(rzBoundLow, d);
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

	@Override
	public String toString() {
	    return Integer.toHexString(hashCode());
	}

	public String toStringFull() {
	    StringBuffer rzString = new StringBuffer();
	    rzString.append("[");
	    for (int i = 0; i < d; i++) {
		rzString.append(String.format("%f--%f", rzBoundLow[i], rzBoundHigh[i]));
		if (i != d - 1) {
		    rzString.append(", ");
		}
	    }
	    rzString.append("]");
	    if (isLeaf) {
		return String.format("%s: %s  (Size: %d)        RZ: %s        Points: %s",
			this,
			Arrays.toString(divideArray(Arrays.copyOfRange(keys, 0, size), STRING_SHORTENER)),
			size,
			rzString.toString(),
			Arrays.toString(Arrays.copyOfRange(points, 0, size * d)));
	    } else {
		StringBuffer keysString = new StringBuffer();
		StringBuffer chrzString = new StringBuffer();
		keysString.append("[");
		chrzString.append("[");
		for (int i = 0; i < size; i++) {
		    keysString.append((keysChildrenLow[i] / STRING_SHORTENER) + "--" + (keysChildrenHigh[i] / STRING_SHORTENER));
		    chrzString.append(String.format("%f--%f", rzBoundsChildrenLow[i], rzBoundsChildrenHigh[i]));
		    if (i != size - 1) {
			keysString.append(", ");
			chrzString.append(", ");
		    }
		}
		keysString.append("]");
		chrzString.append("]");
		return String.format("%s: %s  (Size: %d)        chRZ: %s        ch: %s        Bounds: %s",
			this,
			keysString.toString(),
			size,
			chrzString.toString(),
			Arrays.toString(Arrays.copyOf(children, size)),
			rzString.toString());
	    }
	}

	private static long[] divideArray(long[] a, long d) {
	    int n = a.length;
	    long[] b = new long[n];
	    for (int i = 0; i < n; i++) {
		b[i] = a[i] / d;
	    }
	    return b;
	}

	public String deepToString() {
	    return traverse(0).toString();
	}

	private StringBuffer traverse(int level) {
	    StringBuffer out = new StringBuffer();
	    out.append(String.format("%s%s\n", StringUtils.repeat(' ', level), this.toStringFull()));
	    if (!isLeaf) {
		for (int i = 0; i < size; i++) {
		    Node child = children[i];
		    out.append(child.traverse(level + TRAVERSE_INDENT));
		}
	    }
	    return out;
	}
    }

    @Override
    public boolean contains(Object o) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Iterator<float[]> iterator() {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Object[] toArray() {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public <T> T[] toArray(T[] a) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean add(float[] e) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean remove(Object o) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean containsAll(Collection<?> c) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean addAll(Collection<? extends float[]> c) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean addAll(int index, Collection<? extends float[]> c) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean removeAll(Collection<?> c) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean retainAll(Collection<?> c) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void clear() {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public float[] get(int index) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public float[] set(int index, float[] element) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void add(int index, float[] element) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public float[] remove(int index) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int indexOf(Object o) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int lastIndexOf(Object o) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ListIterator<float[]> listIterator() {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ListIterator<float[]> listIterator(int index) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<float[]> subList(int fromIndex, int toIndex) {
	throw new UnsupportedOperationException("Not supported yet.");
    }
}
