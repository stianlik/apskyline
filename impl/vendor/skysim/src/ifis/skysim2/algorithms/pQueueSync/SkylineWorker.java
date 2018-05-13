package ifis.skysim2.algorithms.pQueueSync;

import java.util.List;

public interface SkylineWorker extends Runnable {

    /**
     * Compares all data in the inqueue to it's responsible range.
     * Consumes dataPoint from inputQueue whenever available. If the dataPoint
     * is a petrify-marker, the thread waits until continueWork is called. In
     * case of a poisoned-marker, the thread stops.
     */
    @Override
    public abstract void run();

    /**
     * Sets the id of this worker (only used for debug purposes).
     * @param id the new id
     */
    public abstract void setId(int id);

    public abstract int getId();

    /**
     * Returns the CPU costs (# comparisons) of this worker.
     * @return cpu cost
     */
    public abstract long getCpuCost();

    /**
     * Retuns the window of this worker.
     * @return the window
     */
    public List<float[]> getWindow();

    public long getCpuCostSinceLastReorg();
}