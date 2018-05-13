/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ifis.skysim2.algorithms.pQueueSync;

import java.util.Collection;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;


public class NonBlockingBlockQueue<E> extends ConcurrentLinkedQueue<E> implements BlockingQueue<E> {
    
        /**
     * Creates a <tt>ConcurrentLinkedQueue</tt> that is initially empty.
     */
    public NonBlockingBlockQueue() {}

    /**
     * Creates a <tt>ConcurrentLinkedQueue</tt>
     * initially containing the elements of the given collection,
     * added in traversal order of the collection's iterator.
     * @param c the collection of elements to initially contain
     * @throws NullPointerException if the specified collection or any
     *         of its elements are null
     */
    public NonBlockingBlockQueue(Collection<? extends E> c) {
        super(c);
    }

    @Override
    public void put(E e) throws InterruptedException {
        add(e);
    }

    @Override
    public boolean offer(E e, long timeout, TimeUnit unit) throws InterruptedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public E take() throws InterruptedException {
        while (true) {
            E result=poll();
            if (result!=null) {
                return result;
            }
            Thread.sleep(0, 15);
        }
    }

    @Override
    public E poll(long timeout, TimeUnit unit) throws InterruptedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int remainingCapacity() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int drainTo(Collection<? super E> c) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int drainTo(Collection<? super E> c, int maxElements) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
