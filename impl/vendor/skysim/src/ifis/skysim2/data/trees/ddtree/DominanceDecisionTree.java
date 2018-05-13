package ifis.skysim2.data.trees.ddtree;

// Dominance decision tree as described in:

import ifis.skysim2.common.datastructures.ArrayListIntStack;
import ifis.skysim2.common.datastructures.ArrayListStack;
import ifis.skysim2.data.tools.PointComparator;
import ifis.skysim2.common.tools.StringUtils;
import ifis.skysim2.data.tools.PointRelationship;
import java.util.Arrays;
import java.util.Iterator;

// Oliver Schütze: A New Data Structure for the Nondominance Problem in Multi-Objective Optimization (2003)

public class DominanceDecisionTree implements Iterable<float[]> {

    private Node root;
    private long numComp = 0;

    // returns true if some point in the tree rooted at r dominates v
    private boolean detectDomination(Node r, float[] v) {
//	System.out.format("detectDomination (node %s), point %s%n", Arrays.toString(r.point), Arrays.toString(v));
	PointRelationship dom = PointComparator.compare(r.point, v);
	numComp++;
	if (dom == PointRelationship.DOMINATES) {
	    // r.point dominates v
	    return true;
	}
	for (int i = r.idOfFirstChild; i < r.d; i++) {
	    // check children of r
	    if ((r.point[i] >= v[i]) && (r.children[i] != null)) {
		if (detectDomination(r.children[i], v)) {
		    return true;
		}
	    }
	}
	return false;
    }

    // deletes all points in the subtree rooted at r that are dominated by v
    private void deleteDominated(Node r, Node parentOfR, int childIdOfR, float[] v) {
	for (int i = r.idOfFirstChild; i < r.d; i++) {
	    if (r.children[i] != null) {
		deleteDominated(r.children[i], r, i, v);
	    }
	    if (v[i] < r.point[i]) {
		break;
	    }
	}
	PointRelationship dom = PointComparator.compare(v, r.point);
	numComp++;
	if (dom == PointRelationship.DOMINATES) {
	    // r.point is dominated by v, delete it
	    if (r.numberOfChildren == 0) {
		// r is leaf node
		if (parentOfR == null) {
		    // r is the root node
		    root = null;
		} else {
//		    System.out.println("Deleting " + Arrays.toString(r.point));
		    parentOfR.children[childIdOfR] = null;
		    parentOfR.numberOfChildren--;
		    if ((childIdOfR == parentOfR.idOfFirstChild) && (parentOfR.numberOfChildren > 0)) {
			int i = parentOfR.idOfFirstChild + 1;
			while (parentOfR.children[i] == null) {
			    i++;
			}
			parentOfR.idOfFirstChild = i;
		    }
		}
		return;
	    }
	    // r is not a leaf node
	    // the first child of r will replace r
	    if (parentOfR == null) {
		// r is the root node
		root = r.children[r.idOfFirstChild];
	    } else {
		// r is an ordinary node
		parentOfR.children[childIdOfR] = r.children[r.idOfFirstChild];
	    }
//	    System.out.println("Deleting " + Arrays.toString(r.point));
	    // reinsert all points in remaining children
	    for (int i = r.idOfFirstChild + 1; i < r.d; i++) {
		if (r.children[i] != null) {
		    treeInsert(r.children[r.idOfFirstChild], r.children[i]);
		}
	    }
	}
    }

    // Inserts every point in the tree s into the tree r
    private static void treeInsert(Node r, Node s) {
	for (int i = s.idOfFirstChild; i < s.d; i++) {
	    if (s.children[i] != null) {
		treeInsert(r, s.children[i]);
	    }
	}
	insert(r, s.point);
    }

    // Inserts v into the tree r
    private static void insert(Node r, float[] v) {
//	System.out.format("insert (node %s), point %s%n", Arrays.toString(r.point), Arrays.toString(v));
	int i = 0;
	while (v[i] >= r.point[i]) {
	    i++;
	}
	// now, i is the first dimension in that r.point is greater than v
	if (r.children[i] != null) {
	    insert(r.children[i], v);
	} else {
	    r.children[i] = new Node(v);
	    if (r.numberOfChildren == 0) {
		r.idOfFirstChild = i;
	    } else {
		if (i < r.idOfFirstChild) {
		    r.idOfFirstChild = i;
		}
	    }
	    r.numberOfChildren++;
	}
    }

    // Updates the tree rooted at r with a new point v
    public void update(float[] v) {
	if (root == null) {
	    root = new Node(v);
	    return;
	}
	if (detectDomination(root, v)) {
	    return;
	}
	deleteDominated(root, null, -1, v);
	if (root == null) {
	    root = new Node(v);
	} else {
	    insert(root, v);
	}
    }

    @Override
    public Iterator<float[]> iterator() {
	return new DominanceDecisionTreeIterator(this);
    }

    public long getNumberOfComparisons() {
	return numComp;
    }

    public String deepToString() {
	StringBuffer result = new StringBuffer();
	DominanceDecisionTreeIterator iter = new DominanceDecisionTreeIterator(this);
	while (iter.hasNext()) {
	    int level = iter.getCurrentLevel();
	    int id = iter.getChildIdOfNextItem();
	    String idStr = "";
	    if (id != -1) {
		idStr = "(" + Integer.toString(id) + ")";
	    }
	    float[] next = iter.next();
	    result.append(String.format("%s%s %s%n", StringUtils.repeat(' ', level * 5), idStr, Arrays.toString(next)));
	}
	return result.toString();
    }

    private static class Node {
	private final int d;
	private final float[] point;
	private final Node[] children;
	private int numberOfChildren = 0;
	private int idOfFirstChild;

	// creates an empty node
	private Node(float[] point) {
	    this.d = point.length;
	    this.point = Arrays.copyOf(point, d);
	    children = new Node[d];
	}
    }

    private static class DominanceDecisionTreeIterator implements Iterator<float[]> {

	private ArrayListStack<Node> stack;
	private ArrayListIntStack<Node> stackInt;

	private DominanceDecisionTreeIterator(DominanceDecisionTree tree) {
	    stack = new ArrayListStack<Node>();
	    stackInt = new ArrayListIntStack<Node>();
	    stack.push(tree.root);
	}

	@Override
	public boolean hasNext() {
	    return !stack.isEmpty();
	}

	@Override
	public float[] next() {
	    Node currentNode = stack.pop();
	    float[] result = currentNode.point;
	    if (currentNode.numberOfChildren > 0) {
		// traverse to first child of currentNode
		stack.push(currentNode);
		stack.push(currentNode.children[currentNode.idOfFirstChild]);
		stackInt.push(currentNode.idOfFirstChild);
	    } else {
		// traverse to next sibling of currentNode
		while (!stack.isEmpty()) {
		    // find next child
		    Node parentOfCurrentNode = stack.pop();
		    int childIdOfCurrentNode = stackInt.pop();
		    int i = childIdOfCurrentNode + 1;
		    while ((i < parentOfCurrentNode.d) && (parentOfCurrentNode.children[i] == null)) {
			i++;
		    }
		    if (i == parentOfCurrentNode.d) {
			// there is no next child
			currentNode = parentOfCurrentNode;
		    } else {
			stack.push(parentOfCurrentNode);
			stack.push(parentOfCurrentNode.children[i]);
			stackInt.push(i);
			break;
		    }
		}
	    }
	    return result;
	}

	private int getCurrentLevel() {
	    return stackInt.size();
	}

	private int getChildIdOfNextItem() {
	    if (stackInt.isEmpty()) {
		return -1;
	    } else {
		int id = stackInt.pop();
		stackInt.push(id);
		return id;
	    }
	}

	@Override
	public void remove() {
	    throw new UnsupportedOperationException("Not supported yet.");
	}
    }
}
