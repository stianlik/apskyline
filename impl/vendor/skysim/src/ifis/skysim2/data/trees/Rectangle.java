package ifis.skysim2.data.trees;

import java.util.Arrays;

public class Rectangle {
    private int d;
    private float[] low;
    private float[] high;

    // Copy rectangle
    public Rectangle(Rectangle mbr) {
	d = mbr.d;
	low = Arrays.copyOf(mbr.low, d);
	high = Arrays.copyOf(mbr.high, d);
    }

    public Rectangle(float[] low, float[] high) {
	d = low.length;
	this.low = Arrays.copyOf(low, d);
	this.high = Arrays.copyOf(high, d);
    }

    public void stretch(Rectangle rect) {
	for (int i = 0; i < d; i++) {
	    low[i] = Math.min(low[i], rect.low[i]);
	    high[i] = Math.max(high[i], rect.high[i]);
	}
    }

    public int getD() {
	return d;
    }

    public float getArea() {
	return getArea(low, high);
    }

    public static float getArea(float[] low, float[] high) {
	final int d = low.length;
	float area = 1;
	for (int i = 0; i < d; i++) {
	    area *= (high[i] - low[i]);
	}
	return area;
    }

    public float[] getHigh() {
	return Arrays.copyOf(high, d);
    }

    public float[] getLow() {
	return Arrays.copyOf(low, d);
    }

    public float getCentroid(int i) {
	return (low[i] + high[i]) / 2;
    }

    @Override
    public String toString() {
	return String.format("%s // %s", Arrays.toString(low), Arrays.toString(high));
    }
}