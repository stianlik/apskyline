package ifis.skysim2.data.sources;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class PointSourceRAM implements PointSource {

    protected float[] data;
    private int d;
    private int size;
    protected int nextPoint;
    private static int DEFAULT_INITIAL_SIZE = 10;
    private static int RESIZE_FACTOR = 2;

    public PointSourceRAM(int d) {
	this(d, DEFAULT_INITIAL_SIZE);
    }

    public PointSourceRAM(int d, int initialSize) {
	this(d, new float[d * initialSize]);
	size = 0;
	nextPoint = 0;
    }

    public PointSourceRAM(int d, float[] data) {
	this.data = Arrays.copyOf(data, data.length);
	this.d = d;
	size = data.length / d;
	nextPoint = data.length;
    }

    public PointSourceRAM(List<float[]> data) {
	if (data instanceof PointSourceRAM) {
	    PointSourceRAM dataPSR = (PointSourceRAM) data;
	    this.data = Arrays.copyOf(dataPSR.data, dataPSR.nextPoint);
	    d = dataPSR.d;
	    size = dataPSR.size;
	    nextPoint = dataPSR.nextPoint;
	} else {
	    throw new UnsupportedOperationException("Not supported yet.");
	}
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
    public boolean isEmpty() {
	return (size == 0);
    }

    @Override
    public boolean add(float[] point) {
	if (nextPoint == data.length) {
	    float[] dataNew = new float[RESIZE_FACTOR * data.length];
	    System.arraycopy(data, 0, dataNew, 0, data.length);
	    data = dataNew;
	}
	System.arraycopy(point, 0, data, nextPoint, d);
	nextPoint += d;
	size++;
	return true;
    }

    @Override
    public ListIterator<float[]> listIterator() {
	return new PointSourceRAMIterator(data, d, size);
    }

    @Override
    public Iterator<float[]> iterator() {
	return listIterator();
    }

    @Override
    public float[] get(int index) {
	int rangeStart = d * index;
	int rangeEnd = rangeStart + d;
	return Arrays.copyOfRange(data, rangeStart, rangeEnd);
    }

    @Override
    public float[] set(int index, float[] point) {
	System.arraycopy(point, 0, data, d * index, d);
	return null;
    }

    @Override
    public Object[] toArray() {
	float[][] array = new float[size][];
	int position = 0;
	for (int i = 0; i < size; i++) {
	    int newPosition = position + d;
	    array[i] = Arrays.copyOfRange(data, position, newPosition);
	    position = newPosition;
	}
	return array;
    }

    @Override
    public boolean contains(Object o) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public <T> T[] toArray(T[] a) {
	float[][] b = new float[size][];
	int i = 0;
	for (float[] p : this) {
	    b[i] = p;
	    i++;
	}
	return (T[])b;
    }

    @Override
    public float[] toFlatArray() {
	return Arrays.copyOf(data, nextPoint);
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
	int rangeStart = d * fromIndex;
	int rangeEnd = d * toIndex;
	float[] subdata = Arrays.copyOfRange(data, rangeStart, rangeEnd);
	return new PointSourceRAM(d, subdata);
    }

    private static class PointSourceRAMIterator implements ListIterator<float[]> {
    //public class PointSourceRAMIterator extends UnimplementedListIterator<float[]> {

	private float[] data;
	private int d;
	private int border;
	private int position = 0;

	public PointSourceRAMIterator(float[] data, int d, int size) {
	    this.data = data;
	    this.d = d;
	    border = d * size;
	}

	@Override
	public boolean hasNext() {
	    return (position < border);
	}

	@Override
	public float[] next() {
	    int positionNew = position + d;
	    float[] next = Arrays.copyOfRange(data, position, positionNew);
	    position = positionNew;
	    return next;
	}

	@Override
	public void set(float[] point) {
	    System.arraycopy(point, 0, data, position - d, d);
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
	public void remove() {
	    throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void add(float[] e) {
	    throw new UnsupportedOperationException("Not supported yet.");
	}
    }
}