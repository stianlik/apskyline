package ifis.skysim2.algorithms.pQueueSync;

import ifis.skysim2.data.tools.PointComparator;
import ifis.skysim2.data.points.LinkedPointList;
import ifis.skysim2.data.tools.PointRelationship;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.BrokenBarrierException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A skyline worker is responsible for comparing given objects (provided by the 
 * inqueue) to all objects within a the current window restricted to a specific
 * range. For each range, there only one responsible worker.
 * 
 * @author Christoph
 */
public class SkylineWorkerQueueSync implements Runnable, SkylineWorker {

    public enum Status {

        IDLE,
        RUNNING,
        PETRIFIED,
        POISONED
    };
    /** cost metric for cpu (# comparisons)  */
    protected long cpuCost = 0;
    protected long cpuCostSinceLastReorg = 0;
    /** Verbose id of this worker for debug purposes */
    protected int id = this.hashCode();
    /** A queue providing the next object for comparison */
    protected BlockingQueue<float[]> inqueue;
    /** A queue taking all objects surving all comparisons */
    protected BlockingQueue<float[]> outqueue;
    /** The PointList this thread is responsible for */
    protected List<float[]> window;
    /** The actual status of the thread. This is also used for thread control.
    If thread is petrified, set status to something else to continue. */
    protected volatile Status status = Status.IDLE;
    /** The parelellAlgorithm which created this worker */
    protected SkylineAlgorithmAbstractBarrierSync owner;

    /**
     * Creates a new worker.
     * @param inqueue queue providing new dataPoints
     * @param outqueue queue accepting surviving dataPoints. Set to null if
     * this is the last worker.
     * @param owner The parelellAlgorithm which created this worker
     */
    public SkylineWorkerQueueSync(int d, BlockingQueue<float[]> inqueue, BlockingQueue<float[]> outqueue, SkylineAlgorithmAbstractBarrierSync owner) {
        this.inqueue = inqueue;
        this.outqueue = outqueue;
//		this.window = new LinkedList<float[]>();
        this.window = new LinkedPointList(d);
        this.owner = owner;
    }

    /* (non-Javadoc)
     * @see ifis.skysim2.algorithms.pQueueSync.SkylineWorker#run()
     */
    @Override
    public void run() {
        int counter = 0;
        int countZero = 0;
        //
        status = Status.RUNNING;
        try {
            while (true) {
                float[] data = inqueue.take();
                counter++;
                if (inqueue.isEmpty()) {
                    countZero++;
                }
                if (data == PointSourceQueue.PETRIFY) {
                    this.addToOutQueue(PointSourceQueue.PETRIFY);
                    status = Status.PETRIFIED;
                    // System.out.println("Petrified " + id);
                    try {
                        owner.barrier.await();
                    } catch (BrokenBarrierException ex) {
                        Logger.getLogger(SkylineWorkerQueueSync.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    cpuCostSinceLastReorg = 0;
                } else if (data == PointSourceQueue.POISON) {
                    this.addToOutQueue(PointSourceQueue.POISON);
                    status = Status.POISONED;
                    try {
                        owner.barrier.await();
                    } catch (BrokenBarrierException ex) {
                        Logger.getLogger(SkylineWorkerQueueSync.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    // DEBUG
                    System.out.print(" CPU " + this.id + " : " + (this.getCpuCost() / 1000) + "K    ");
                    if (countZero > 0) {
                        System.out.format("Waiting time: %5.2f%%%n", 100 * (double) countZero / counter);
                    }
                    return;
                } else {
                    consume(data);
                }
            }
        } catch (InterruptedException ex) {
            System.out.println("Worker " + this + " was interrupted.");
        }
    }

    /**
     * Scans through the worker's windows and compares each object to the
     * current dataPoint. If a window object is dominated, it is removed.
     * If the dataPoint is domiated, the method exits.
     * Surviving dataPoints are added to the out queue.
     * @param dataPoint the current data point
     * @throws java.lang.InterruptedException imported by addToOutQueue
     */
    protected void consume(float[] dataPoint) throws InterruptedException {
        ListIterator<float[]> windowIter = window.listIterator();
        while (windowIter.hasNext()) {
            final float[] windowPoint = windowIter.next();
            cpuCost++;
            cpuCostSinceLastReorg++;
            PointRelationship dom = PointComparator.compare(dataPoint, windowPoint);
            switch (dom) {
                case DOMINATES:
                    windowIter.remove();
                    break;
                case IS_DOMINATED_BY:
                    return;
            }
        }
        // window.add(dataPoint);
        this.addToOutQueue(dataPoint);
    }

    /**
     * Passes the datapoint to the outqueue unless this is the last worker.
     * If it is the last worker, dataPint is added to window (markers are
     * discarted).
     * @param dataPoint the dataPoint to add
     * @throws java.lang.InterruptedException inherited by BlockingQueue
     */
    protected void addToOutQueue(float[] dataPoint) throws InterruptedException {
        if (outqueue != null) {
            outqueue.put(dataPoint);
        } else {
            if (dataPoint == PointSourceQueue.POISON) {
//		System.out.println("POISON");
            } else if (dataPoint == PointSourceQueue.PETRIFY) {
//		System.out.println("PERTIFY");
            } else {
//		System.out.println(data + " survived, gets added to window");
                window.add(dataPoint);
            }
        }
    }

    /* (non-Javadoc)
     * @see ifis.skysim2.algorithms.pQueueSync.SkylineWorker#setId(int)
     */
    @Override
    public void setId(int id) {
        this.id = id;
    }

    /* (non-Javadoc)
     * @see ifis.skysim2.algorithms.pQueueSync.SkylineWorker#getId()
     */
    @Override
    public int getId() {
        return id;
    }

    /**
     * Retuns the window of this worker.
     * @return the window
     */
    @Override
    public List<float[]> getWindow() {
        return this.window;
    }

    /* (non-Javadoc)
     * @see ifis.skysim2.algorithms.pQueueSync.SkylineWorker#getCpuCost()
     */
    @Override
    public long getCpuCost() {
        return this.cpuCost;
    }

    @Override
    public long getCpuCostSinceLastReorg() {
        return this.cpuCostSinceLastReorg;
    }
}
