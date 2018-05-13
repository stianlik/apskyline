package ifis.skysim2.data.points;

import ifis.skysim2.data.tools.PointComparator;
import ifis.skysim2.data.tools.PointRelationship;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/*
 * Mark array entries as dirty (and skip then afterwards) on deletion
 */

public class StaticArrayPointList implements ArrayPointList {

    private static final int DEFAULT_INITIAL_SIZE = 10;
    private static final int RESIZE_FACTOR = 2;
    private static final float DIRTY_FLAG = -1;

    private float[] points;
    private final int d;
    private int last;
    private int size = 0;

    public StaticArrayPointList(final int initialSize, final int d) {
	points = new float[initialSize * d];
	last = -d;
	this.d = d;
    }

    public StaticArrayPointList(final int d) {
	this(DEFAULT_INITIAL_SIZE, d);
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
    public int getD() {
	return d;
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
    public float[] get(int index) {
	return getDirect(index);
    }

    @Override
    public float[] getDirect(int index) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    public PointRelationship compare(int indexA, float[] pointB) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void add(int index, float[] element) {
	addDirect(index, element);
    }

    @Override
    public void copy(int from, int to) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void addDirect(int index, float[] element) {
	throw new UnsupportedOperationException("Not supported yet.");
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
    public PointListIterator listIterator(float[] referencePoint) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public PointRelationship compare(int indexA, int indexB) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public float[] getSubarray(int from, int to) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    private class PointListArrayListIterator implements PointListIterator {

	private int cursor = -d;
	private int seen = 0;
	private final float[] referencePoint;

	public PointListArrayListIterator() {
	    this(null);
	}

	public PointListArrayListIterator(float[] referencePoint) {
	    this.referencePoint = referencePoint;
	}

	@Override
	public boolean hasNext() {
	    return seen < size;
	}

	@Override
	public float[] next() {
	    return nextDirect();
	}

	@Override
	public float[] nextDirect() {
	    do {
		cursor += d;
	    } while (points[cursor] == DIRTY_FLAG);
	    seen++;
	    return Arrays.copyOfRange(points, cursor, cursor + d);
	}

	@Override
	public PointRelationship nextAndCompareNextTo(float[] point) {
	    do {
		cursor += d;
	    } while (points[cursor] == DIRTY_FLAG);
	    seen++;
	    return PointComparator.compare(points, cursor, point, d);
	}

	@Override
	public PointRelationship nextAndCompareNextToReferencePoint() {
	    do {
		cursor += d;
	    } while (points[cursor] == DIRTY_FLAG);
	    seen++;
	    return PointComparator.compare(points, cursor, referencePoint, d);
	}

	@Override
	public void remove() {
	    points[cursor] = DIRTY_FLAG;
	    seen--;
	    size--;
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
	public void moveToFront() {
	    throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void promotePoint() {
	    throw new UnsupportedOperationException("Not supported yet.");
	}
    }
}