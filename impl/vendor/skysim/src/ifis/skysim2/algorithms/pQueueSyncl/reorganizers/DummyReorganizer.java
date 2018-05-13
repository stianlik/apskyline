package ifis.skysim2.algorithms.pQueueSyncl.reorganizers;

import ifis.skysim2.algorithms.pQueueSync.SkylineWorker;
import ifis.skysim2.algorithms.pQueueSync.SkylineWorkerQueueSync;
import java.util.List;

/**
 *
 * @author selke
 */
public class DummyReorganizer extends AbstractReorganizer {

    public DummyReorganizer(List<SkylineWorker> workers) {
        super(workers);
    }

    public DummyReorganizer() {
        super();
    }

    @Override
    protected void reorganize() {
    }
    
        public String toString() {
	    return "Dummy Reorganizer";
    }
}
