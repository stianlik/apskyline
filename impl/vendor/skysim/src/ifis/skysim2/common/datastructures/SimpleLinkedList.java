package ifis.skysim2.common.datastructures;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class SimpleLinkedList<E> implements List<E> {

    private static class DataNode<E> {

	public final E data;
	public DataNode<E> prev;
	public DataNode<E> next;

	public DataNode() {
	    this.data = null;
	}

	public DataNode(final E point) {
	    this.data = point;
	}

	public DataNode(final E point, final DataNode prev, final DataNode next) {
	    this.data = point;
	    this.prev = prev;
	    this.next = next;
	}
    }

    private class LinkedListIterator implements ListIterator<E> {

	@Override
	public boolean hasNext() {
	    return cursor.next != last;
	}

	@Override
	public E next() {
	    cursor = cursor.next;
	    return cursor.data;
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

	@Override
	public boolean hasPrevious() {
	    throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public E previous() {
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
	public void set(E e) {
	    throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void add(E e) {
	    throw new UnsupportedOperationException("Not supported yet.");
	}
    }
    
    private DataNode<E> cursor;
    private final DataNode<E> first;
    private final DataNode<E> last;
    private int size = 0;
    private final ListIterator<E> iter;

    public SimpleLinkedList() {
	first = new DataNode();
	last = new DataNode();
	first.next = last;
	last.prev = first;
	iter = new LinkedListIterator();
    }

    @Override
    public boolean add(E data) {
	final DataNode<E> newNode = new DataNode(data, last.prev, last);
	last.prev.next = newNode;
	last.prev = newNode;
	size++;
	return true;
    }

    @Override
    public void add(int index, E data) {
	if (index != 0) {
	    throw new UnsupportedOperationException("Not supported yet.");
	} else {
	    final DataNode<E> newNode = new DataNode<E>(data, first, first.next);
	    first.next.prev = newNode;
	    first.next = newNode;
	    size++;
	}
    }

    @Override
    public E remove(int index) {
	if (index != 0) {
	    throw new UnsupportedOperationException("Not supported yet.");
	} else {
	    DataNode<E> node = first.next;
	    first.next = first.next.next;
	    if (first.next != null) {
		first.next.prev = first;
	    }
	    if (cursor == node) {
		cursor = first;
	    }
	    size--;
	    return node.data;
	}
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
	if (!(c instanceof SimpleLinkedList)) {
	    throw new UnsupportedOperationException("Not supported yet.");
	} else {
	    SimpleLinkedList<E> ll = (SimpleLinkedList<E>) c;
	    ListIterator<E> pllIter = ll.listIterator();
	    while (pllIter.hasNext()) {
		add(pllIter.next());
	    }
	    return true;
	}
    }

    @Override
    public int size() {
	return size;
    }

    /**
     * Steals a given number of elements from the head of the victim list and
     * add them to the tail of the current list.. Simply
     * steals all elements when you try to steal more than there is available
     * (no exceptions thrown).
     * @param victim the list from where to steal elements
     * @param elementsToMove the number of elements to steal
     */
    public void stealFromList(SimpleLinkedList<E> victim, int elementsToMove) {
	DataNode vc = victim.first;
	int num = Math.min(victim.size(), elementsToMove);
	for (int i = 0; i < num; i++) {
	    vc = vc.next;
	}
	//
	DataNode lp = last.prev;
	DataNode vfn = victim.first.next;
	DataNode vcn = vc.next;
	//
	if (num > 0) {
	    last.prev = vc;
	    vc.next = last;
	    //
	    victim.first.next = vcn;
	    vcn.prev = victim.first;
	    //
	    lp.next = vfn;
	    vfn.prev = lp;
	    //
	    size += num;
	    victim.size -= num;
	}
    // empty lists go here
    }

    @Override
    public ListIterator<E> listIterator() {
	cursor = first;
	return iter;
//		return new PointListLinkedListIterator();
    }

    @Override
    public Iterator<E> iterator() {
	return listIterator();
    }

    @Override
    public E get(int index) {
	if (index == 0) {
	    return first.next.data;
	} else {
	    throw new UnsupportedOperationException("Not supported yet.");
	}
    }

    @Override
    public boolean isEmpty() {
	return size == 0;
    }

    @Override
    public boolean contains(Object o) {
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
    public boolean addAll(int index, Collection<? extends E> c) {
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
    public E set(int index, E element) {
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
    public ListIterator<E> listIterator(int index) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<E> subList(int fromIndex, int toIndex) {
	throw new UnsupportedOperationException("Not supported yet.");
    }
}
