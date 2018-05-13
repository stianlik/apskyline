package ifis.skysim2.data.points;

import ifis.skysim2.data.tools.PointComparator;
import ifis.skysim2.data.tools.PointRelationship;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/*
 * Shift/copy array contents on delete
 */

public class ShiftingArrayPointList implements ArrayPointList {

    private static final int DEFAULT_INITIAL_SIZE = 10;
    private static final int RESIZE_FACTOR = 2;

    private float[] points;
    private final int d;
    private int last;
    private int size;

    public ShiftingArrayPointList(final int initialSize, final int d) {
	points = new float[initialSize * d];
	last = -d; // the beginning of the last point
	this.d = d;
	size = 0;
    }

    public ShiftingArrayPointList(final int d) {
	this(DEFAULT_INITIAL_SIZE, d);
    }

    public ShiftingArrayPointList(float[] data, final int d, boolean copyData) {
	int l = data.length;
	if (l % d != 0) {
	    throw new IllegalArgumentException();
	}
	this.d = d;
	size = l / d;
	if (copyData) {
	    points = Arrays.copyOf(data, l);
	} else {
	    points = data;
	}
	last = l - d;
    }

    @Override
    public boolean add(final float[] point) {
	return addDirect(point);
    }

    @Override
    public boolean addDirect(final float[] point) {
	last += d;
	if (last == points.length) {
	    float[] pointsNew = new float[RESIZE_FACTOR * points.length];
	    System.arraycopy(points, 0, pointsNew, 0, points.length);
	    points = pointsNew;
	}
	System.arraycopy(point, 0, points, last, d);
	size++;
	return true;
    }

    @Override
    public void copy(int from, int to) {
	System.arraycopy(points, from * d, points, to * d, d);
    }

    @Override
    public int getD() {
	return d;
    }

    @Override
    public float[] get(int index) {
	return getDirect(index);
    }

    @Override
    public float[] getDirect(int index) {
	int start = d * index;
	return Arrays.copyOfRange(points, start, start + d);
    }

    @Override
    public float[] getSubarray(int from, int to) {
	return Arrays.copyOfRange(points, d * from, d * to);
    }

    @Override
    public PointRelationship compare(int indexA, int indexB) {
	return PointComparator.compare(points, d * indexA, d * indexB, d);
    }

//    public PointRelationship compare(int indexA, ShiftingArrayPointList listB, int indexB) {
//	return PointComparator.compare(this.points, d * indexA, listB.points, d * indexB, d);
//    }

    @Override
    public PointRelationship compare(int indexA, float[] pointB) {
	return PointComparator.compare(this.points, d * indexA, pointB, 0, d);
    }

    @Override
    public int size() {
	return size;
    }

    @Override
    public PointListIterator listIterator() {
	return new PointListArrayListIterator();
    }

    @Override
    public boolean isEmpty() {
	throw new UnsupportedOperationException("Not supported yet.");
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
    public ListIterator<float[]> listIterator(int index) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<float[]> subList(int fromIndex, int toIndex) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void addDirect(int index, float[] data) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public PointListIterator listIterator(float[] referencePoint) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    private class PointListArrayListIterator implements PointListIterator {

	private int cursor = -d;
	private final float[] referencePoint;

	public PointListArrayListIterator() {
	    this(null);
	}

	public PointListArrayListIterator(float[] referencePoint) {
	    this.referencePoint = referencePoint;
	}

	@Override
	public boolean hasNext() {
	    return cursor < last;
	}

	@Override
	public float[] next() {
	    return nextDirect();
	}

	@Override
	public float[] nextDirect() {
	    cursor += d;
	    return Arrays.copyOfRange(points, cursor, cursor + d);
	}

	@Override
	public PointRelationship nextAndCompareNextTo(float[] point) {
	    cursor += d;
	    return PointComparator.compare(points, cursor, point, d);
	}

	@Override
	public PointRelationship nextAndCompareNextToReferencePoint() {
	    cursor += d;
	    return PointComparator.compare(points, cursor, referencePoint, d);
	}

	@Override
	public void remove() {
	    System.arraycopy(points, cursor + d, points, cursor, last - cursor);
	    last -= d;
	    cursor -= d;
	    size--;
	}

	@Override
	public void moveToFront() {
	    if (cursor != 0) {
		float[] current = Arrays.copyOfRange(points, cursor, cursor + d);
		System.arraycopy(points, 0, points, d, cursor);
		System.arraycopy(current, 0, points, 0, d);
	    }
	}

	@Override
	public boolean hasPrevious() {
	    throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public float[] previous() {
	    throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public int nextIndex() {
	    throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public int previousIndex() {
	    throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void set(float[] e) {
	    throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void add(float[] e) {
	    throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void promotePoint() {
	    throw new UnsupportedOperationException("Not supported yet.");
	}
    }
}