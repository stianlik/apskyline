package ifis.skysim2.data.sources;

import java.io.BufferedInputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class PointSourceDisk implements PointSource {

    // performance note:
    // - same performance as an implementation using NIO but IO is more elegant
    private final static int RECORDS_PER_PAGE = 82;
    private static final int BYTES_PER_FLOAT = Float.SIZE / 8;
    private static final boolean SKIP_FILLER = false;
    private final ListIterator<float[]> iter;
    private final int n;
    private final int d;
    private DataInput in;
    private int fillerSize;
    private byte[] filler;
    private int i = 0;

    public PointSourceDisk(File file, int bytesPerRecord, int n, int d) {
	this.n = n;
	this.d = d;
	try {
	    int bufferSize = RECORDS_PER_PAGE * bytesPerRecord;
	    in = new DataInputStream(new BufferedInputStream(new FileInputStream(file), bufferSize));
	} catch (FileNotFoundException e) {
	    e.printStackTrace();
	}
	fillerSize = bytesPerRecord - d * BYTES_PER_FLOAT;
	filler = new byte[fillerSize];

	iter = new PointSourceDiskListIterator();
    }

    @Override
    public int getD() {
	return d;
    }

    @Override
    public float[] toFlatArray() {
	if (n == 0) {
	    return new float[0];
	} else {
	    float[] result = new float[d * n];
	    int j = 0;
	    ListIterator<float[]> iterr = listIterator();
	    while (iter.hasNext()) {
		float[] point = iterr.next();
		System.arraycopy(point, 0, result, j * d, d);
		j++;
	    }
	    return result;
	}
    }

    @Override
    public ListIterator<float[]> listIterator() {
	return iter;
    }

    @Override
    public int size() {
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
    public boolean add(float[] e) {
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
    public float[] get(int index) {
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

    private class PointSourceDiskListIterator implements ListIterator<float[]> {

	@Override
	public boolean hasNext() {
	    return i < n;
	}

	@Override
	public float[] next() {
	    float[] next = new float[d];
	    for (int j = 0; j < d; j++) {
		try {
		    next[j] = in.readFloat();
		} catch (IOException e) {
		    e.printStackTrace();
		}
	    }
	    try {
		if (SKIP_FILLER) {
		    in.skipBytes(fillerSize);
		} else {
		    in.readFully(filler);
		}
	    } catch (IOException e) {
		e.printStackTrace();
	    }
	    i++;
	    return next;
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
	public void set(float[] e) {
	    throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void add(float[] e) {
	    throw new UnsupportedOperationException("Not supported yet.");
	}
    }
}