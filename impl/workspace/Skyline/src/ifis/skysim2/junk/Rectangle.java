package ifis.skysim2.junk;

import java.util.Arrays;

public class Rectangle {
    private int d;
    private float[] lower;
    private float[] upper;

    public Rectangle(int d) {
	lower = new float[d];
	upper = new float[d];
	this.d = d;
    }

    public int getD() {
	return d;
    }

    public float[] getLower() {
	return Arrays.copyOf(lower, d);
    }

    public float[] getUpper() {
	return Arrays.copyOf(upper, d);
    }

    public void setLower(float[] lower) {
	this.lower = Arrays.copyOf(lower, d);
    }

    public void setUpper(float[] upper) {
	this.upper = Arrays.copyOf(upper, d);
    }

    public float getLower(int i) {
	return lower[i];
    }

    public float getUpper(int i) {
	return upper[i];
    }

    public void setLower(int i, float lowerI) {
	lower[i] = lowerI;
    }

    public void setUpper(int i, float upperI) {
	upper[i] = upperI;
    }

    public void setTo(Rectangle rect) {
	lower = rect.getLower();
	upper = rect.getUpper();
    }

    public static Rectangle getMBR(Rectangle[] rects) {
	int k = rects.length;
	int d = rects[0].d;
	Rectangle mbr = new Rectangle(d);
	mbr.lower = rects[0].getLower();
	mbr.upper = rects[0].getUpper();
	for (int i = 0; i < d; i++) {
	    for (int j = 1; j < k; j++) {
		Rectangle rectJ = rects[j];
		mbr.lower[i] = Math.min(mbr.lower[i], rectJ.lower[i]);
		mbr.upper[i] = Math.max(mbr.upper[i], rectJ.upper[i]);
	    }
	}
	return mbr;
    }

    public static Rectangle getMBR(float[][] points) {
	int k = points.length;
	int d = points[0].length;
	Rectangle mbr = new Rectangle(d);
	mbr.lower = Arrays.copyOf(points[0], d);
	mbr.upper = Arrays.copyOf(points[0], d);
	for (int i = 0; i < d; i++) {
	    for (int j = 1; j < k; j++) {
		float[] pointJ = points[j];
		mbr.lower[i] = Math.min(mbr.lower[i], pointJ[i]);
		mbr.upper[i] = Math.max(mbr.upper[i], pointJ[i]);
	    }
	}
	return mbr;
    }

    public double getOverlapWith(Rectangle mbr) {
	double overlap = 1;
	for (int i = 0; i < d; i++) {
	    float overlapLower = Math.max(lower[i], mbr.lower[i]);
	    float overlapUpper = Math.min(upper[i], mbr.upper[i]);
	    if (overlapLower >= overlapUpper) {
		return 0;
	    } else {
		overlap *= overlapUpper - overlapLower;
	    }
	}
	return overlap;
    }

    public double getArea() {
	double area = 1;
	for (int i = 0; i < d; i++) {
	    area *= upper[i] - lower[i];
	}
	return area;
    }

    public double getEnlargementBy(Rectangle rect) {
	double areaNew = 1;
	for (int i = 0; i < d; i++) {
	    float lowerNew = Math.min(lower[i], rect.lower[i]);
	    float upperNew = Math.max(upper[i], rect.upper[i]);
	    areaNew *= upperNew - lowerNew;
	}
	return areaNew - getArea();
    }

    public double getEnlargementBy(float[] point) {
	double areaNew = 1;
	for (int i = 0; i < d; i++) {
	    float lowerNew = Math.min(lower[i], point[i]);
	    float upperNew = Math.max(upper[i], point[i]);
	    areaNew *= upperNew - lowerNew;
	}
	return areaNew - getArea();
    }

    public void enlargeBy(Rectangle rect) {
	for (int i = 0; i < d; i++) {
	    lower[i] = Math.min(lower[i], rect.lower[i]);
	    upper[i] = Math.max(upper[i], rect.upper[i]);
	}
    }

    @Override
    public String toString() {
	return String.format("%s // %s", Arrays.toString(lower), Arrays.toString(upper));
    }
}
