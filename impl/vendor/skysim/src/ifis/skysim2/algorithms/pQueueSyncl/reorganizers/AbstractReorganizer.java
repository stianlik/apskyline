package ifis.skysim2.algorithms.pQueueSyncl.reorganizers;

import ifis.skysim2.algorithms.pQueueSync.SkylineWorker;
import ifis.skysim2.algorithms.pQueueSync.SkylineWorkerQueueSync;
import java.util.List;

/**
 *
 * @author selke
 */
public abstract class AbstractReorganizer implements Reorganizer {

	protected List<SkylineWorker> workers;
	private long reorgTimeNS;

	public AbstractReorganizer(List<SkylineWorker> workers) {
		this.workers = workers;
		reorgTimeNS = 0;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setWorkers(List<SkylineWorker> workers) {
		this.workers = workers;
	}

	/**
	 * Workers have to be set manually!
	 */
	public AbstractReorganizer() {
		reorgTimeNS = 0;
	}

	@Override
	public long getReorgTimeNS() {
		return reorgTimeNS;
	}

	@Override
	public void run() {
		long startTime = System.nanoTime();
		reorganize();
		reorgTimeNS = System.nanoTime() - startTime;
	}

	abstract protected void reorganize();
}
