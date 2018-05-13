package ifis.skysim2.junk;

// TODO: still, some caching/readahead seems to take place ...
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class PointSourceRAMOld implements List<float[]> {

	private class PointSourceRAMListIterator implements ListIterator<float[]> {
		private int i = 0;
		
		@Override
		public boolean hasNext() {
			return i < upperBound;
		}

		@Override
		public float[] next() {
			float[] next = Arrays.copyOfRange(data, i, i + d);
			i += d;
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

	private final float[] data;
	private final int n;
	private final int d;
	private int upperBound;

	public PointSourceRAMOld(float[] data, int n, int d) {
		this.data = data;
		this.n = n;
		this.d = d;
		upperBound = n * d;
	}

	@Override
	public ListIterator<float[]> listIterator() {
		return new PointSourceRAMListIterator();
	}
	
	@Override
	public List<float[]> subList(int fromIndex, int toIndex) {
		float[] dataSub = Arrays.copyOfRange(data, fromIndex * d, toIndex * d);
		return new PointSourceRAMOld(dataSub, toIndex - fromIndex, d);
	}

	@Override
	public int size() {
		return n;
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
}