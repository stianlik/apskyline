package ifis.skysim2.junk;

public abstract class Node {

    protected static int TRAVERSE_INDENT = 4;
    protected int d;
    protected Rectangle mbr;
    InternalNode parent = null;

    public Node(int d) {
	this.d = d;
	mbr = new Rectangle(d);
    }

    private Node() {
    }

    @Override
    public String toString() {
	return String.format("%s: %s", Integer.toHexString(hashCode()), mbr);
    }

    public String deepToString() {
	return traverse(0).toString();
    }

    protected abstract StringBuffer traverse(int level);

    public abstract int getNumberOfEntries();

    public int getD() {
	return d;
    }

    public Rectangle getMBR() {
	return mbr;
    }

    public float getMBRLower(int i) {
	return mbr.getLower(i);
    }

    public float getMBRUpper(int i) {
	return mbr.getUpper(i);
    }

    public float[] getMBRLower() {
	return mbr.getLower();
    }

    public float[] getMBRUpper() {
	return mbr.getUpper();
    }

    public double getMBRArea() {
	return mbr.getArea();
    }

    public double getMBREnlargementBy(Node node) {
	return mbr.getEnlargementBy(node.mbr);
    }

    public double getMBREnlargementBy(float[] point) {
	return mbr.getEnlargementBy(point);
    }

    public abstract void recomputeMBR();

    // Also update parent nodes
    public void recursivelyRecomputeMBRs() {
	recomputeMBR();
	if (parent != null) {
	    parent.recursivelyRecomputeMBRs();
	}
    }

    public static String repeat(char c, int i) {
	StringBuffer out = new StringBuffer();
	for (int j = 0; j < i; j++) {
	    out.append(c);
	}
	return out.toString();
    }
}