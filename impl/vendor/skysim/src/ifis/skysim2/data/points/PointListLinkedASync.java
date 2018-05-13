package ifis.skysim2.data.points;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class PointListLinkedASync implements List<float[]> {

	private final DataNode first;
	private final DataNode last;
	private int size = 0;

	public PointListLinkedASync() {
		first = new DataNode();
		last = new DataNode();
		first.next = last;
		last.prev = first;
	}

	private static class DataNode {

		public final float[] point;
		public DataNode prev;
		public DataNode next;

		public DataNode() {
			this.point = null;
		}

		public DataNode(final float[] point) {
			this.point = point;
		}

		public DataNode(final float[] point, final DataNode prev, final DataNode next) {
			this.point = point;
			this.prev = prev;
			this.next = next;
		}
	}

	public class PointListLinkedSyncListIterator implements ListIterator<float[]> {

		private DataNode cursor = first;

		@Override
		public boolean hasNext() {
			return cursor.next != last;
		}

		@Override
		public float[] next() {
			cursor = cursor.next;
			return cursor.point;
		}

		@Override
		public void remove() {
			if (cursor.prev != null) {
				cursor.prev.next = cursor.next;
			}
			if (cursor.next != null) {
				cursor.next.prev = cursor.prev;
			}
			cursor = cursor.prev;
			size--;
		}
		
		public void reset() {
			cursor = first;
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
	}

	@Override
	public int size() {
		return size;
	}

	@Override
	public boolean add(float[] point) {
		final DataNode newNode = new DataNode(point, last.prev, last);
		last.prev.next = newNode;
		last.prev = newNode;
		size++;
		return true;
	}

	@Override
	public ListIterator<float[]> listIterator() {
		return new PointListLinkedSyncListIterator();
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
}
