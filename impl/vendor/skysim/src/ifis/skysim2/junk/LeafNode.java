package ifis.skysim2.junk;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LeafNode extends Node {

    public LeafNode(int d) {
	super(d);
    }
    protected List<float[]> entries = new ArrayList<float[]>();

    public float[] getEntry(int i) {
	return entries.get(i);
    }

    @Override
    public int getNumberOfEntries() {
	return entries.size();
    }

    @Override
    public void recomputeMBR() {
	int k = entries.size();
	float[][] points = new float[k][];
	for (int i = 0; i < k; i++) {
	    points[i] = entries.get(i);
	}
	mbr = Rectangle.getMBR(points);
    }

    @Override
    protected StringBuffer traverse(int level) {
	StringBuffer out = new StringBuffer();
	out.append(String.format("%s%s\n", repeat(' ', level), this));
	for (float[] point : entries) {
	    out.append(String.format("%s%s\n", repeat(' ', level + TRAVERSE_INDENT), Arrays.toString(point)));
	}
	return out;
    }
}