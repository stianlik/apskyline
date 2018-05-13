package ifis.skysim2.algorithms.pPackageSync;

import ifis.skysim2.algorithms.pQueueSync.*;
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
public class SkylineWorkerPackageSync implements SkylineWorker {


    private DataPackage getNextPackage() throws InterruptedException {
        DataPackage dataPackage = inqueue.take();
        for (float[] data: dataPackage.getQueue()) {
            if (data==null) {
                System.out.print(" ");
            }
        }
        return dataPackage;
    }

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
    protected BlockingQueue<DataPackage> inqueue;
    /** A queue taking all objects surving all comparisons */
    protected BlockingQueue<DataPackage> outqueue;
    /** The PointList this thread is responsible for */
    protected List<float[]> window;
    /** The actual status of the thread. This is also used for thread control.
    If thread is petrified, set status to something else to continue. */
    protected volatile Status status = Status.IDLE;
    /** The parelellAlgorithm which created this worker */
    protected SkylineAlgorithmAbstractBarrierSync owner;
    protected DataPackage outPackage;

    /**
     * Creates a new worker.
     * @param inqueue queue providing new dataPoints
     * @param outqueue queue accepting surviving dataPoints. Set to null if
     * this is the last worker.
     * @param owner The parelellAlgorithm which created this worker
     */
    public SkylineWorkerPackageSync(int d, BlockingQueue<DataPackage> inqueue, BlockingQueue<DataPackage> outqueue, SkylineAlgorithmAbstractBarrierSync owner) {
        this.inqueue = inqueue;
        this.outqueue = outqueue;
//		this.window = new LinkedList<float[]>();
        this.window = new LinkedPointList(d);
        this.owner = owner;
        outPackage = new DataPackage();
    }

    /**
     * Compares all data in the inqueue to it's responsible range.
     * Consumes dataPoint from inputQueue whenever available. If the dataPoint
     * is a petrify-marker, the thread waits until continueWork is called. In
     * case of a poisoned-marker, the thread stops.
     */
    @Override
    public void run() {
        int counter = 0;
        int countZero = 0;
        //
        status = Status.RUNNING;
        try {
            while (true) {
                DataPackage dataPackage = getNextPackage();
                counter++;
                if (inqueue.size() == 0) {
                    countZero++;
                }

                //
                if (dataPackage == PointSourcePackageQueue.PETRIFY) {
                    this.addToOutQueue(PointSourcePackageQueue.PETRIFY);
                    status = Status.PETRIFIED;
                    // System.out.println("Petrified " + id);
                    try {
                        owner.barrier.await();
                    } catch (BrokenBarrierException ex) {
                        Logger.getLogger(SkylineWorkerPackageSync.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    cpuCostSinceLastReorg = 0;
                } else if (dataPackage == PointSourcePackageQueue.POISON) {
                    this.addToOutQueue(outPackage);
                    this.addToOutQueue(PointSourcePackageQueue.POISON);
                    status = Status.POISONED;
                    try {
                        owner.barrier.await();
                    } catch (BrokenBarrierException ex) {
                        Logger.getLogger(SkylineWorkerPackageSync.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    if (countZero > 0) {
                        System.out.format("Waiting time: %5.2f%%%n", 100 * (double) countZero / counter);
                    }
                    return;
                } else {
                    consume(dataPackage);
                }
            }
        } catch (InterruptedException ex) {
            System.out.println("Worker " + this + " was interrupted.");
        }
    }

    protected void consume(DataPackage dataPackage) throws InterruptedException {
        pointLoop:
        for (float[] dataPoint : dataPackage.getQueue()) {
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
                        continue pointLoop;
                }
            }
            // window.add(dataPoint);
            this.addToOutQueue(dataPoint);
        }
    }

    protected void addToOutQueue(float[] dataPoint) throws InterruptedException {
        if (outqueue != null) {
            // not last thread
            if (outPackage.getQueue().size() < SkylineAlgorithmPPackageSync.packageSize) {
                outPackage.getQueue().add(dataPoint);
            } else {
                addToOutQueue(outPackage);
                outPackage = new DataPackage();
            }
        } else {
            // last thread
            window.add(dataPoint);
        }
    }

    protected void addToOutQueue(DataPackage dataPackage) throws InterruptedException {
        if (outqueue != null) {
            outqueue.put(dataPackage);
        } else {
            if (dataPackage == PointSourcePackageQueue.POISON) {
            } else if (dataPackage == PointSourcePackageQueue.PETRIFY) {
            } else {
                for (float[] data : dataPackage.getQueue()) {
                    window.add(data);
                }
            }
        }
    }

    /**
     * Sets the id of this worker (only used for debug purposes).
     * @param id the new id
     */
    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    /**
     * Retuns the window of this worker.
     * @return the window
     */
    public List<float[]> getWindow() {
        return this.window;
    }

    /**
     * Returns the CPU costs (# comparisons) of this worker.
     * @return cpu cost
     */
    public long getCpuCost() {
        return this.cpuCost;
    }

    public long getCpuCostSinceLastReorg() {
        return this.cpuCostSinceLastReorg;
    }
}
