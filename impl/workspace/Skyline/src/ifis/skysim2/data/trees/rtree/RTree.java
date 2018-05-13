package ifis.skysim2.data.trees.rtree;

import ifis.skysim2.data.trees.Rectangle;

public class RTree {
    // each node can host between 2 <= CAPACITY_MIN <= CAPACITY_MAX / 2 and CAPACITY_MAX entries

    public final static int CAPACITY_MIN = 10;
    public final static int CAPACITY_MAX = 20;
    private Node root = null;

    RTree(Node node) {
	root = node;
    }

    public Node getRoot() {
	return root;
    }

    public String deepToString() {
	return root.deepToString();
    }

    public static class Node {

	private static int TRAVERSE_INDENT = 4;
	private Node firstChild = null;
	private Node lastChild = null;
	private int numberOfChildren = 0;
	private Node nextSibling = null;
	private Node parent = null;
	private Rectangle mbr;
	private float[] item;

	public Node(float[] item, float[] mbrLow, float[] mbrHigh) {
	    this.item = item;
	    mbr = new Rectangle(mbrLow, mbrHigh);
	}

	public Node(float[] item) {
	    this(item, item, item);
	}

	public Node(Node node) {
	    firstChild = node;
	    lastChild = node;
	    numberOfChildren = 1;
	    node.parent = this;
	    node.nextSibling = null;
	    mbr = new Rectangle(node.mbr);
	}

	// Append node as new child; returns true if successful
	public boolean appendChild(Node node) {
	    if (numberOfChildren == CAPACITY_MAX) {
		return false;
	    } else {
		node.nextSibling = null;
		node.parent = this;
		lastChild.nextSibling = node;
		lastChild = node;
		numberOfChildren++;
		mbr.stretch(node.mbr);
		return true;
	    }
	}

	public int getD() {
	    return mbr.getD();
	}

	public Rectangle getMBR() {
	    return mbr;
	}

	public boolean isLeaf() {
	    return numberOfChildren == 0;
	}

	public float[] getItem() {
	    return item;
	}

	public Node[] getChildren() {
	    Node[] children = new Node[numberOfChildren];
	    int i = 0;
	    for (Node child = firstChild; child != null; child = child.nextSibling, i++) {
		children[i] = child;
	    }
	    return children;
	}

	@Override
	public String toString() {
	    return String.format("%s: %s", Integer.toHexString(hashCode()), mbr);
	}

	public String deepToString() {
	    return traverse(0).toString();
	}

	private StringBuffer traverse(int level) {
	    StringBuffer out = new StringBuffer();
	    out.append(String.format("%s%s\n", repeat(' ', level), this));
	    Node child = firstChild;
	    while (child != null) {
		out.append(child.traverse(level + TRAVERSE_INDENT));
		child = child.nextSibling;
	    }
	    return out;
	}

	private static String repeat(char c, int i) {
	    StringBuffer out = new StringBuffer();
	    for (int j = 0; j < i; j++) {
		out.append(c);
	    }
	    return out.toString();
	}
    }
}
