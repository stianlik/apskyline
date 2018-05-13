package ifis.skysim2.data.trees.domfreequadtree;

import ifis.skysim2.data.tools.PointComparator;
import ifis.skysim2.data.tools.PointRelationship;
import java.util.Arrays;
import java.util.Iterator;

// Primogenitary Linked Quad Tree (PLQT)
// Sun: A Priomogenitary Linked Quad Tree Data Structure and
//      Its Application to Discrete Multiple Criteria Optimization
public class DomFreeQuadTreePL implements Iterable<float[]> {

    private static int MAX_LEVELS_TRAVERSE = 1000;
    private Node rootNode = null;
    private int numComps = 0;

    public static void main(String[] args) {
//	int sizeBNL;
//	int sizeQuad;
//	DataGenerator dg = new DataGenerator();
//	dg.resetToDefaultSeed();
//	int d = 3;
//	int n = 100000;
//	PointSource pointSource;
//	do {
//	    System.out.println("\n\n\n\n");
//	    float[] data = dg.generate(d, n, null);
//	    pointSource = new PointSourceRAM(d, data);
//	    SkylineAlgorithm quad = new SkylineAlgorithmDomFreeQuadTree();
//	    SkylineAlgorithm bnl = new SkylineAlgorithmBNL();
//	    List<float[]> resultQuad = quad.compute(pointSource);
//	    List<float[]> resultBNL = bnl.compute(pointSource);
//	    sizeBNL = resultBNL.size();
//	    sizeQuad = resultQuad.size();
//	} while (sizeBNL == sizeQuad);
//	System.out.format("BNL:  %4d\nQuad: %4d\n", sizeBNL, sizeQuad);
//	for (float[] point: pointSource) {
//	    System.out.println(Arrays.toString(point));
//	}



	DomFreeQuadTreePL tree = new DomFreeQuadTreePL();
	float[][] data = {
	    {5, 1, 2},
	    {1, 2, 7},
	    {7, 3, 5},
	    {4, 5, 3},
	    {6, 4, 1},
	    {2, 6, 4},
	    {3, 7, 6},
	};

	for (float[] point: data) {
	    System.out.println(Arrays.toString(point));
	    tree.add(point);
	    System.out.println(tree);
	    System.out.println("\n");
	}
    }

    public DomFreeQuadTreePL() {
    }

    public boolean add(float[] point) {
//	System.out.format("add(%s)\n", Arrays.toString(point));
	float[] pointCopy = Arrays.copyOf(point, point.length);
	if (rootNode == null) {
	    rootNode = new Node(pointCopy, 0, null, null);
	} else {
	    process(rootNode, pointCopy);
	}
	return true;
    }

    // process point for possible insertion into tree rooted at root;
    private void process(Node root, float[] point) {
//	System.out.format("process(%s, %s)\n", Arrays.toString(root.point), Arrays.toString(point));
	Node currentRoot = null;
	Node nextRoot = root;

	do {
	    currentRoot = nextRoot;
	    nextRoot = null;

	    // check for domination between point and currentRoot
	    numComps++;
	    if (PointComparator.compare(currentRoot.point, point) == PointRelationship.DOMINATES) {
		// currentRoot dominates point, so discard point and terminate
		return;
	    }
	    long sPoint = PointComparator.getSuccessorship(currentRoot.point, point);
	    if (sPoint == 0) {
		// point dominates currentRoot, so replace currentRoot by point and terminte
		replace(currentRoot, point);
		return;
	    }

	    // no domination between point and currentRoot, so sequentially check the sons of currentRoot
	    Node currentSon = currentRoot.firstSon;
	    Node previousSon = null;

	    // check sons older than point, if any; only they can dominate point
	    while ((currentSon != null) && (currentSon.successorship < sPoint)) {
		if ((currentSon.successorship & sPoint) == currentSon.successorship) {
		    // currentSon is the root of a tree, which might contain nodes dominating point,
		    // so search this tree for a node dominating point
		    if (findNodesDominatingPoint(currentSon, point)) {
			// discard point if it is dominated
			return;
		    }
		}
		// check next son
		previousSon = currentSon;
		currentSon = currentSon.nextSibling;
	    }

	    if (currentSon == null) {
		// there only have been sons older than point, if any;
		// insert point as a new-born son of currentRoot
		Node nodePoint = new Node(point, sPoint, currentRoot, null);
		if (previousSon == null) {
		    currentRoot.firstSon = nodePoint;
		} else {
		    previousSon.nextSibling = nodePoint;
		}
		return;
	    }

	    // check if there is a sone of the same age as point
	    if (currentSon.successorship == sPoint) {
		// give order to traverse tree
		nextRoot = currentSon;
		// continue with next son
		previousSon = currentSon;
		currentSon = currentSon.nextSibling;
	    } else {
		// now, currentSon is the oldest son being younger than point,
		// so point has to be inserted between previousSon and currentSon
		Node nodePoint;
		if (previousSon == null) {
		    nodePoint = new Node(point, sPoint, currentRoot, currentRoot.firstSon);
		    currentRoot.firstSon = nodePoint;
		} else {
		    nodePoint = new Node(point, sPoint, currentRoot, previousSon.nextSibling);
		    previousSon.nextSibling = nodePoint;
		}
		previousSon = nodePoint;
	    }

	    // from now on, every currentSon is younger than point, if any;
	    // they can be dominated by point
	    if (currentSon != null) {
		// currentSon is the root of a tree, which might contain nodes dominated by point,
		// so search this tree for a node being dominated by point,
		// also search all trees rooted at younger siblings of currentSon
		findNodesDominatedByPoint(currentSon, point, previousSon, sPoint);
	    }
	} while (nextRoot != null);
    }

    // search the tree rooted at node root for a node dominating the input point;
    // return true if there is such a node;
    // assert that root satisfies the "\subseteq" criterion
    private boolean findNodesDominatingPoint(Node root, float[] point) {
//	System.out.format("findNodesDominatingPoint(%s, %s)\n", Arrays.toString(root.point), Arrays.toString(point));
	Node currentRoot = root;
	int levelOfCurrentRoot = 0;
	// sPoint[i]: successorship between the tree node of level i and point;
	// in particular, sPoint[levelOfCurrentRoot] is the successorship between currentRoot and point
	long[] sPoint = new long[MAX_LEVELS_TRAVERSE];

	do {
	    // currentRoot satisfies the "\subseteq" criterion;
	    // therefore, we must check if currentRoot dominates point
	    numComps++;
	    if (PointComparator.compare(currentRoot.point, point) == PointRelationship.DOMINATES) {
		// currentRoot dominates point
		return true;
	    }
	    // currentRoot does not dominate point but its sons and siblings might do ...
	    sPoint[levelOfCurrentRoot] = PointComparator.getSuccessorship(currentRoot.point, point);
	    // find the next node in tree order after currentNode satisfying the "\subseteq" criterion
	    do {
		// find the direct successor of currentRoot in tree order
		if (currentRoot.firstSon != null) {
		    // currentRoot is an internal node
		    currentRoot = currentRoot.firstSon;
		    levelOfCurrentRoot++;
		} else {
		    // currentRoot is a leaf node
		    if (levelOfCurrentRoot == 0) {
			return false;
		    }
		    while (currentRoot.nextSibling == null) {
			currentRoot = currentRoot.parent;
			levelOfCurrentRoot--;
			if (levelOfCurrentRoot == 0) {
			    // we have checked all subtrees of root
			    return false;
			}
		    }
		    currentRoot = currentRoot.nextSibling;
		}
		// currentRoot now is the successor (in tree traversal order) of the currentRoot we started with
		// check if currentRoot can dominate point using the ">" criterion
		while (currentRoot.successorship > sPoint[levelOfCurrentRoot - 1]) {
		    // currentRoot cannot dominate point, the same is true for all its following siblings
		    // skip all following siblings of currentRoot
		    do {
			currentRoot = currentRoot.parent;
			levelOfCurrentRoot--;
			if (levelOfCurrentRoot == 0) {
			    // we have checked all subtrees of root
			    return false;
			}
		    } while (currentRoot.nextSibling == null);
		    currentRoot = currentRoot.nextSibling;
		}
	    } while ((currentRoot.successorship & sPoint[levelOfCurrentRoot - 1]) != currentRoot.successorship);
	} while (true);
    }

    // search the tree rooted at node root for a node dominating point;
    // also search all trees rooted at younger siblings of root;
    // we know that root.parent != null
    private void findNodesDominatedByPoint(Node root, float[] point, Node previousSiblingOfRoot, long sPoint0) {
//	if (previousSiblingOfRoot != null) {
//	    System.out.format("findNodesDominatedByPoint(%s, %s, %s, %d)\n", Arrays.toString(root.point), Arrays.toString(point), Arrays.toString(previousSiblingOfRoot.point), sPoint0);
//	} else {
//	    System.out.format("findNodesDominatedByPoint(%s, %s, null, %d)\n", Arrays.toString(root.point), Arrays.toString(point), sPoint0);
//	}
	Node currentRoot = root;
	Node previousSiblingOfCurrentRoot = previousSiblingOfRoot;
	int levelOfCurrentRoot = 1;
	// sPoint[i]: successorship between the tree node of level i and point;
	// in particular, sPoint[levelOfCurrentRoot] is the successorship between currentRoot and point
	long[] sPoint = new long[MAX_LEVELS_TRAVERSE];
	sPoint[0] = sPoint0;
	sPoint[1] = PointComparator.getSuccessorship(root.point, point);


	gteloop:
	do {
//	    System.out.format("Check %s (succ: %d; succ point: %d)\n", Arrays.toString(currentRoot.point), currentRoot.successorship, sPoint[levelOfCurrentRoot - 1]);
	    // currentRoot satisfies the ">=" criterion,
	    // i.e. currentRoot.successorhip >= sPoint[levelOfCurrentRoot - 1];
	    // therefore, we must check the "\subseteq" criterion
	    if ((currentRoot.successorship | sPoint[levelOfCurrentRoot - 1]) == currentRoot.successorship) {
//		System.out.format("%s satisfies \\subseteq criterion\n", Arrays.toString(currentRoot.point));
		// currentRoot might be dominated by point since it satisfies the "\subseteq" criterion
		// check if point dominates currentRoot;
		numComps++;
		if (PointComparator.compare(currentRoot.point, point) == PointRelationship.IS_DOMINATED_BY) {
		    // point dominates currentRoot, so delete currentRoot
		    if (previousSiblingOfCurrentRoot == null) {
			// currentRoot is a first son
			Node parentOfCurrentRoot = currentRoot.parent;
			delete(currentRoot, null);
			if (parentOfCurrentRoot.firstSon != null) {
			    currentRoot = parentOfCurrentRoot.firstSon;
			    continue gteloop;
			} else {
			    currentRoot = parentOfCurrentRoot;
			    levelOfCurrentRoot--;
			    if (levelOfCurrentRoot == 0) {
				return;
			    }
			    // continue at (*)
			}
		    } else {
			// currentRoot is not a first son
			delete(currentRoot, previousSiblingOfCurrentRoot);
			currentRoot = previousSiblingOfCurrentRoot;
			// continue at (*)
		    }
		} else {
		    // point does not dominate currentRoot;
		    // but since the "\subseteq" criterion is satisfied,
		    // we must check the sons of currentRoot, if any;
		    // find the oldest son of currentRoot satisfing the ">=" criterion, if any
		    sPoint[levelOfCurrentRoot] = PointComparator.getSuccessorship(currentRoot.point, point);
//		    System.out.format("%s does not dominate %s; check sons of %s\n", Arrays.toString(point), Arrays.toString(currentRoot.point), Arrays.toString(currentRoot.point));
		    Node currentSon = currentRoot.firstSon;
		    Node previousSiblingOfCurrentSon = null;

		    // skip sons being older than point
		    while ((currentSon != null) && (currentSon.successorship < sPoint[levelOfCurrentRoot])) {
//			System.out.format("current son is: %s (succ son: %d; succ point:%d)\n", Arrays.toString(currentSon.point), currentSon.successorship, sPoint[levelOfCurrentRoot]);
			previousSiblingOfCurrentSon = currentSon;
			currentSon = currentSon.nextSibling;
		    }
		    if (currentSon != null) {
//			System.out.format("we found a candidate: %s; go down one level\n", Arrays.toString(currentSon.point));
			// there is a son satisfing the ">=" criterion
//			sPoint[levelOfCurrentRoot] = getSuccessorship(currentRoot.point, point);
			levelOfCurrentRoot++;
			currentRoot = currentSon;
			previousSiblingOfCurrentRoot = previousSiblingOfCurrentSon;
//			System.out.format("Succ between %s and %s is %d\n", Arrays.toString(currentRoot.point), Arrays.toString(point), sPoint[levelOfCurrentRoot]);
			continue gteloop;
		    }
		}
	    }

	    // (*)
	    // advance to next node in tree order, ignoring sons of currentRoot;
	    // this next node always will satisfy the ">=" criterion;
	    // first, check if currentRoot has any younger siblings
	    while (currentRoot.nextSibling == null) {
		// currentRoot does not have any younger siblings
		currentRoot = currentRoot.parent;
		levelOfCurrentRoot--;
		if (levelOfCurrentRoot == 0) {
		    // we have checked all younger siblings of root
		    return;
		}
	    }
	    // currentRoot has a next sibling; this we will check next
	    previousSiblingOfCurrentRoot = currentRoot;
	    currentRoot = currentRoot.nextSibling;
	} while (true);
    }

    // deletes root and fixes links to and from root;
    // returns the root of the resulting tree (null if root is a leaf node)
    private Node delete(Node root, Node previousSiblingOfRoot) {
//	if (previousSiblingOfRoot != null) {
//	    System.out.format("delete(%s, %s)\n", Arrays.toString(root.point), Arrays.toString(previousSiblingOfRoot.point));
//	} else {
//	    System.out.format("delete(%s, null)\n", Arrays.toString(root.point));
//	}
	if (root.firstSon == null) {
	    // root is a leaf node
	    if (previousSiblingOfRoot == null) {
		// root is a first son
		root.parent.firstSon = root.nextSibling;
	    } else {
		// root is not a first son
		previousSiblingOfRoot.nextSibling = root.nextSibling;
	    }
	    return null;
	}
	// root is an internal node
	Node newRoot = root.firstSon;
	if (previousSiblingOfRoot == null) {
	    // root is a first son
	    root.parent.firstSon = newRoot;
	} else {
	    // root is not a first son
	    previousSiblingOfRoot.nextSibling = newRoot;
	}
	Node nextNode = newRoot.nextSibling;
	newRoot.parent = root.parent;
	newRoot.nextSibling = root.nextSibling;
	newRoot.successorship = root.successorship;
	// re-insert old siblings of newRoot
	while ((nextNode != null) && (nextNode != root)) {
	    Node currentNode = nextNode;
	    // traverse tree to first leaf node; this will be the current node to be re-inserted
	    while (currentNode.firstSon != null) {
		currentNode = currentNode.firstSon;
	    }
	    // determine the next node to be re-inserted
	    if (currentNode.nextSibling != null) {
		nextNode = currentNode.nextSibling;
	    } else {
		nextNode = currentNode.parent;
		nextNode.firstSon = null;
	    }
	    // find position where currentNode must be inserted
	    Node currentRoot = newRoot;
	    Node currentSon;
	    Node previousSon;
	    long sCurrentNode;
	    do {
		// initialize search on this level
		currentSon = currentRoot.firstSon;
		previousSon = null;
		sCurrentNode = PointComparator.getSuccessorship(currentRoot.point, currentNode.point);
		while ((currentSon != null) && (currentSon.successorship < sCurrentNode)) {
		    // advance to next sibling
		    previousSon = currentSon;
		    currentSon = currentSon.nextSibling;
		}
		if ((currentSon == null) || (currentSon.successorship > sCurrentNode)) {
		    // we have found the insertion position: between previousSon and currentSon
		    break;
		} else {
		    // it is currentSon.successorship == sCurrentNode, so go down one level
		    currentRoot = currentSon;
		}
	    } while (true);
	    // insert currentNode between previousSon and currentSon
	    if (previousSon == null) {
		currentRoot.firstSon = currentNode;
	    } else {
		previousSon.nextSibling = currentNode;
	    }
	    currentNode.nextSibling = currentSon;
	    currentNode.firstSon = null;
	    currentNode.parent = currentRoot;
	    currentNode.successorship = sCurrentNode;
	}
	return newRoot;
    }

    // replace root's point by a new point
    private void replace(Node root, float[] point) {
//	if (previousSiblingOfRoot != null) {
//	    System.out.format("replace(%s, %s, %s)\n", Arrays.toString(root.point), Arrays.toString(point), Arrays.toString(previousSiblingOfRoot.point));
//	} else {
//	    System.out.format("replace(%s, %s, null)\n", Arrays.toString(root.point), Arrays.toString(point));
//	}
	Node firstSonOfOldRoot = root.firstSon;
	root.point = point;
	root.firstSon = null;
	if (firstSonOfOldRoot == null) {
	    return;
	}
	// root is an internal node
	Node nextNode = firstSonOfOldRoot;
	// re-insert old sons of root
//	while ((nextNode != null) && (nextNode != root)) {
	while (nextNode != root) {
	    Node currentNode = nextNode;
//	    System.out.format("Current node: %s\n", Arrays.toString(currentNode.point));
	    // traverse tree to first leaf node; this will be the current node to be re-inserted
	    while (currentNode.firstSon != null) {
		currentNode = currentNode.firstSon;
	    }
	    // determine the next node to be re-inserted
	    if (currentNode.nextSibling != null) {
		nextNode = currentNode.nextSibling;
	    } else {
		nextNode = currentNode.parent;
		if (nextNode != root) {
		    nextNode.firstSon = null;
		}
	    }
	    // check if currentNode is dominated by newRoot
	    numComps++;
	    if (PointComparator.compare(root.point, currentNode.point) == PointRelationship.DOMINATES) {
		continue;
	    }
	    // find position where currentNode must be inserted
	    Node currentRoot = root;
	    Node currentSon;
	    Node previousSon;
	    long sCurrentNode;
	    do {
		// initialize search on this level
		currentSon = currentRoot.firstSon;
//		System.out.format("Current root: %s\n", Arrays.toString(currentRoot.point));
//		if (currentSon != null) {
//		    System.out.format("Current son: %s\n", Arrays.toString(currentSon.point));
//		} else {
//		    System.out.println("Current son: null");
//		}
		previousSon = null;
		sCurrentNode = PointComparator.getSuccessorship(currentRoot.point, currentNode.point);
		// skip sons smaller than currentNode
		while ((currentSon != null) && (currentSon.successorship < sCurrentNode)) {
		    // advance to next sibling
		    previousSon = currentSon;
		    currentSon = currentSon.nextSibling;
		}
		if ((currentSon == null) || (currentSon.successorship > sCurrentNode)) {
		    // we have found the insertion position: between previousSon and currentSon
		    break;
		} else {
		    // it is currentSon.successorship == sCurrentNode, so go down one level
		    currentRoot = currentSon;
		}
	    } while (true);
	    // insert currentNode between previousSon and currentSon
	    if (previousSon == null) {
		currentRoot.firstSon = currentNode;
//		System.out.format("Insert %s as new son of %s\n", Arrays.toString(currentNode.point), Arrays.toString(currentRoot.point));
	    } else {
		previousSon.nextSibling = currentNode;
	    }
	    currentNode.nextSibling = currentSon;
	    currentNode.firstSon = null;
	    currentNode.parent = currentRoot;
	    currentNode.successorship = sCurrentNode;
	}
    }

    @Override
    public Iterator<float[]> iterator() {
	return new PLQuadTreeIterator(this);
    }

    @Override
    public String toString() {
	if (rootNode == null) {
	    return "(empty tree)";
	}
	return rootNode.toString();
    }

    public static String repeat(char c, int i) {
	StringBuffer out = new StringBuffer();
	for (int j = 0; j < i; j++) {
	    out.append(c);
	}
	return out.toString();
    }

    public int getNumberOfComparions() {
	return numComps;
    }

    private static class Node {

	public static int INDENT = 4;
	public float[] point;
	public Node parent;
	public long successorship;
	public Node nextSibling;
	public Node firstSon = null;

	public Node(float[] point, long successorship, Node parent, Node nextSibling) {
	    this.point = point;
	    this.successorship = successorship;
	    this.parent = parent;
	    this.nextSibling = nextSibling;
	}

	@Override
	public String toString() {
	    StringBuffer out = new StringBuffer();
	    Node currentNode = this;
	    int currentLevel = 0;
	    do {
		out.append(String.format("%s%d: %s\n", repeat(' ', currentLevel * INDENT), currentNode.successorship, Arrays.toString(currentNode.point)));
		if (currentNode.firstSon != null) {
		    currentNode = currentNode.firstSon;
		    currentLevel++;
		} else {
		    while (currentNode.nextSibling == null) {
			currentNode = currentNode.parent;
			currentLevel--;
			if (currentLevel < 0) {
			    return out.toString();
			}
		    }
		    currentNode = currentNode.nextSibling;
		}
	    } while (true);
	}
    }

    private static class PLQuadTreeIterator implements Iterator<float[]> {

	private Node nextNode;

	private PLQuadTreeIterator(DomFreeQuadTreePL tree) {
	    nextNode = tree.rootNode;
	}

	@Override
	public boolean hasNext() {
	    return (nextNode != null);
	}

	@Override
	public float[] next() {
	    float[] result = nextNode.point;
	    if (nextNode.firstSon != null) {
		nextNode = nextNode.firstSon;
	    } else {
		while (nextNode.nextSibling == null) {
		    nextNode = nextNode.parent;
		    if (nextNode == null) {
			return result;
		    }
		}
		nextNode = nextNode.nextSibling;
	    }
	    return result;
	}

	@Override
	public void remove() {
	    throw new UnsupportedOperationException("Not supported yet.");
	}
    }
}
