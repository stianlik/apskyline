/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ifis.skysim2.algorithms.pPackageSync;

import ifis.skysim2.algorithms.pQueueSync.*;
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
public class PointSourcePackageQueue implements BlockingQueue<DataPackage> {

    private final List<float[]> pointSource;
    private final ListIterator<float[]> pointSourceIter;
    private int objectToPetrify = -1;
    private int counter = 0;
    private int tuples = 0;
    public static final DataPackage POISON = new DataPackage();
    public static final DataPackage PETRIFY = new DataPackage();

    public void setObjectToPetrify(int number) {
        this.objectToPetrify = number;
    }

    /**
     * Wrappes a given pointSource in a queue.
     * @param pointSource the point source to wrap
     */
    public PointSourcePackageQueue(List<float[]> pointSource) {
        this.pointSource = pointSource;
        pointSourceIter = pointSource.listIterator();

    }

    @Override
    public DataPackage take() throws InterruptedException {
        counter++;
        if (counter % objectToPetrify == 0) {
            return PETRIFY;
        } else {
            if (pointSourceIter.hasNext() && tuples > -1) {
                DataPackage result = new DataPackage();
                fillLoop:
                for (int i = 0; i < SkylineAlgorithmPPackageSync.packageSize; i++) {
                    if (pointSourceIter.hasNext()) {
                        float[] data = pointSourceIter.next();
                        result.getQueue().add(data);
                        tuples++;
                    } else {
                        break fillLoop;
                    }
                }
                return result;
            }
            return POISON;
        }
    }

    public synchronized DataPackage takeSync() throws InterruptedException {
        return take();
    }

    @Override
    public int size() {
        return -1;
    }

    @Override
    public boolean add(DataPackage e) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean offer(DataPackage e) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void put(DataPackage e) throws InterruptedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean offer(DataPackage e, long timeout, TimeUnit unit) throws InterruptedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public DataPackage poll(long timeout, TimeUnit unit) throws InterruptedException {
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
    public int drainTo(Collection<? super DataPackage> c) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int drainTo(Collection<? super DataPackage> c, int maxElements) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public DataPackage remove() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public DataPackage poll() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public DataPackage element() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public DataPackage peek() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isEmpty() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Iterator<DataPackage> iterator() {
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
    public boolean addAll(Collection<? extends DataPackage> c) {
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
