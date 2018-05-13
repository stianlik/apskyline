package ifis.skysim2.common.datastructures;

import java.util.Arrays;
import java.util.NoSuchElementException;

// A PriorityQueue, where entries are sorted with respect to a double value provided along with each object

// copied from java.util.PriorityQueue, modified
public class PriorityQueueDouble<E> {

	private static final int DEFAULT_INITIAL_CAPACITY = 11;
	private Object[] queue;
	private double[] queueKeys;
	private int size = 0;
	private int modCount = 0;

	public PriorityQueueDouble() {
		this(DEFAULT_INITIAL_CAPACITY);
	}

	public PriorityQueueDouble(int initialCapacity) {
		if (initialCapacity < 1) {
			throw new IllegalArgumentException();
		}
		this.queue = new Object[initialCapacity];
		this.queueKeys = new double[initialCapacity];
	}

	public boolean isEmpty() {
		return size() == 0;
	}

	public E remove() {
		if (size == 0) {
			return null;
		}
		int s = --size;
		modCount++;
		Object result = queue[0];
		Object x = queue[s];
		double xKey = queueKeys[s];
		queue[s] = null;
		if (s != 0) {
			siftDown(0, x, xKey);
		}
		if (result != null) {
			return (E) result;
		} else {
			throw new NoSuchElementException();
		}
	}

	private void grow(int minCapacity) {
		if (minCapacity < 0) {
			throw new OutOfMemoryError();
		}
		int oldCapacity = queue.length;
		int newCapacity = ((oldCapacity < 64) ? ((oldCapacity + 1) * 2)
				: ((oldCapacity / 2) * 3));
		if (newCapacity < 0) {
			newCapacity = Integer.MAX_VALUE;
		}
		if (newCapacity < minCapacity) {
			newCapacity = minCapacity;
		}
		queue = Arrays.copyOf(queue, newCapacity);
		queueKeys = Arrays.copyOf(queueKeys, newCapacity);
	}

	public boolean add(E e, double eKey) {
		if (e == null) {
			throw new NullPointerException();
		}
		modCount++;
		int i = size;
		if (i >= queue.length) {
			grow(i + 1);
		}
		size = i + 1;
		if (i == 0) {
			queue[0] = e;
			queueKeys[0] = eKey;
		} else {
			siftUp(i, e, eKey);
		}
		return true;
	}

	public int size() {
		return size;
	}

	private void siftUp(int k, Object x, double xKey) {
		while (k > 0) {
			int parent = (k - 1) >>> 1;
			Object e = queue[parent];
			double eKey = queueKeys[parent];
			if (xKey >= eKey) {
				break;
			}
			queue[k] = e;
			queueKeys[k] = eKey;
			k = parent;
		}
		queue[k] = x;
		queueKeys[k] = xKey;
	}

	private void siftDown(int k, Object x, double xKey) {
		int half = size >>> 1;
		while (k < half) {
			int child = (k << 1) + 1;
			Object c = queue[child];
			double cKey = queueKeys[child];
			int right = child + 1;
			if (right < size && (cKey > queueKeys[right])) {
				c = queue[child = right];
				cKey = queueKeys[child = right];
			}
			if (xKey <= cKey) {
				break;
			}
			queue[k] = c;
			queueKeys[k] = cKey;
			k = child;
		}
		queue[k] = x;
		queueKeys[k] = xKey;
	}
}