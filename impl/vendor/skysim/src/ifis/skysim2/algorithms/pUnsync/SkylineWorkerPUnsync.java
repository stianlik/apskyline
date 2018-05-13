package ifis.skysim2.algorithms.pUnsync;

import ifis.skysim2.data.tools.PointComparator;
import ifis.skysim2.algorithms.pQueueSync.PointSourceQueue;

import ifis.skysim2.data.points.PointListLinkedSync_PartialLock.PointListLinkedSyncListIterator;
import ifis.skysim2.data.tools.PointRelationship;
import java.util.List;

public final class SkylineWorkerPUnsync implements Runnable {

    /** cost metric for cpu (# comparisons)  */
    private long cpuCost = 0;
    /** Verbose id of this worker for debug purposes */
    private long id = this.hashCode();
    /** The parelellAlgorithm which created this worker */
    private SkylineAlgorithmPUnsync owner;
    private PointSourceQueue pointSource;
    private List<float[]> window;
    PointListLinkedSyncListIterator windowIter;

    public SkylineWorkerPUnsync(SkylineAlgorithmPUnsync owner, List<float[]> window, PointSourceQueue initialDataQueue) {
        this.owner = owner;
        this.pointSource = initialDataQueue;
        this.window = window;

        windowIter = (PointListLinkedSyncListIterator) window.listIterator();
    }

    @Override
    public void run() {
        try {
            while (true) {
                float[] data = pointSource.takeSync();
//				float[] data = pointSource.take();
//				float[] data = null;
//				if (id == 0) {
//					data = pointSource.take1();
//				} else {
//					data = pointSource.take2();
//				}
                if (data == PointSourceQueue.POISON) {
                    return;
                }
                if (data == PointSourceQueue.PETRIFY) {
                    System.out.println("PET!");
                } else {
                    consume(data);
                }
            }
        } catch (InterruptedException ex) {
            System.out.println("Worker " + this + " was interrupted.");
        }
    }

    private void consume(float[] dataPoint) throws InterruptedException {
        windowIter.reset();

        while (windowIter.hasNext()) {
            final float[] windowPoint = windowIter.next();
            cpuCost++;
            PointRelationship dom = PointComparator.compare(dataPoint, windowPoint);
            switch (dom) {
                case DOMINATES:
                    windowIter.remove();
                    break;
                case IS_DOMINATED_BY:
                    return;
            }
        }
        window.add(dataPoint);
    }

    /**
     * Sets the id of this worker (only used for debug purposes).
     * @param id the new id
     */
    public void setId(int id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    /**
     * Returns the CPU costs (# comparisons) of this worker.
     * @return cpu cost
     */
    public long getCpuCost() {
        return this.cpuCost;
    }
}
