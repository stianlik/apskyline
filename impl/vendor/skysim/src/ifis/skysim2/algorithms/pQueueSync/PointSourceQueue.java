/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ifis.skysim2.algorithms.pQueueSync;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Wrappes a point source in a blocking queue. Only method which is implemented is take().
 * @author Christoph
 */
public class PointSourceQueue implements BlockingQueue<float[]> {

    private final List<float[]> pointSource;
    private final ListIterator<float[]> pointSourceIter;
    private final ListIterator<float[]> pointSourceIter1;
    private final ListIterator<float[]> pointSourceIter2;
    private int objectToPetrify = -1;
    private int counter = 0;
    public static final float[] POISON = new float[0];
    public static final float[] PETRIFY = new float[0];

    public void setObjectToPetrify(int number) {
        this.objectToPetrify = number;
    }

    /**
     * Wrappes a given pointSource in a queue.
     * @param pointSource the point source to wrap
     */
    public PointSourceQueue(List<float[]> pointSource) {
        this.pointSource = pointSource;
        pointSourceIter = pointSource.listIterator();
        pointSourceIter1 = pointSource.listIterator();
        pointSourceIter2 = pointSource.listIterator();
        if (pointSourceIter2.hasNext()) {
            pointSourceIter2.next();
        }
    }

    @Override
    public float[] take() throws InterruptedException {

        if (pointSourceIter.hasNext()) {
            counter++;
            if ((objectToPetrify != -1) && (counter % objectToPetrify == 0))  {
                return PETRIFY;
            } else {
                return pointSourceIter.next();
            }
        }
        return POISON;
    }

    public synchronized float[] takeSync() throws InterruptedException {
        return take();
    }

    public float[] take1() throws InterruptedException {
        float[] result = POISON;
        if (pointSourceIter1.hasNext()) {
            result = pointSourceIter1.next();
        }
        if (pointSourceIter1.hasNext()) {
            pointSourceIter1.next();
        }
        return result;
    }

    public float[] take2() throws InterruptedException {
        float[] result = POISON;
        if (pointSourceIter2.hasNext()) {
            result = pointSourceIter2.next();
        }
        if (pointSourceIter2.hasNext()) {
            pointSourceIter2.next();
        }
        return result;
    }

    @Override
    public boolean add(float[] e) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean offer(float[] e) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void put(float[] e) throws InterruptedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean offer(float[] e, long timeout, TimeUnit unit) throws InterruptedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public float[] poll(long timeout, TimeUnit unit) throws InterruptedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int remainingCapacity() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean contains(Object o) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int drainTo(Collection<? super float[]> c) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int drainTo(Collection<? super float[]> c, int maxElements) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public float[] remove() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public float[] poll() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public float[] element() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public float[] peek() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int size() {
        return -1;
    }

    @Override
    public boolean isEmpty() {
        return false;
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
    public boolean containsAll(Collection<?> c) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean addAll(Collection<? extends float[]> c) {
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
}
