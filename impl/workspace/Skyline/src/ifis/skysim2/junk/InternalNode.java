package ifis.skysim2.junk;

import java.util.ArrayList;
import java.util.List;

public class InternalNode extends Node {

    public InternalNode(int d) {
	super(d);
    }
    protected List<Node> entries = new ArrayList<Node>();

    @Override
    public void recomputeMBR() {
	int k = entries.size();
	Rectangle[] rects = new Rectangle[k];
	for (int i = 0; i < k; i++) {
	    rects[i] = entries.get(i).mbr;
	}
	mbr = Rectangle.getMBR(rects);
    }

    public Node getEntry(int i) {
	return entries.get(i);
    }

    @Override
    public int getNumberOfEntries() {
	return entries.size();
    }

    public void assignNode(Node node) {
	entries.add(node);
	node.parent = this;
    }

    @Override
    protected StringBuffer traverse(int level) {
	StringBuffer out = new StringBuffer();
	out.append(String.format("%s%s\n", repeat(' ', level), this));
	for (Node entry : entries) {
	    out.append(entry.traverse(level + TRAVERSE_INDENT));
	}
	return out;
    }
}