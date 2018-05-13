package ifis.skysim2.algorithms.pQueueSyncl.reorganizers;

import ifis.skysim2.algorithms.pQueueSync.SkylineWorker;
import ifis.skysim2.algorithms.pQueueSync.SkylineWorkerQueueSync;
import java.util.List;

/**
 *
 * @author selke
 */
public interface Reorganizer extends Runnable {

    /**
     * Returns the summed costs of reorganization (currently in milliseconds).
     * @return
     */
    public long getReorgTimeNS();

    /**
     * Sets the workers to use before (!) the re-organization.
     * @param workers
     */
    public void setWorkers(List<SkylineWorker> workers);
    
}
