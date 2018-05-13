package ifis.skysim2.common.datastructures;

import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.List;

public class ArrayListStack<E> {
    private List<E> stack = new ArrayList<E>();

    public void push(E obj) {
	stack.add(obj);
    }

    public E pop() {
	if (stack.isEmpty()) {
	    throw new EmptyStackException();
	} else {
	    return stack.remove(stack.size() - 1);
	}
    }

    public boolean isEmpty() {
	return stack.isEmpty();
    }
}
