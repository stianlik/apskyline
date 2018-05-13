package ifis.skysim2.common.datastructures;

import java.util.EmptyStackException;
import java.util.List;

public class SimpleLinkedListQueue<E> {
    private final List<E> list = new SimpleLinkedList<E>();

    public void enqueue(E obj) {
	list.add(obj);
    }

    public E dequeue() {
	if (list.isEmpty()) {
	    throw new EmptyStackException();
	} else {
	    return list.remove(0);
	}
    }

    public boolean isEmpty() {
	return list.isEmpty();
    }
}