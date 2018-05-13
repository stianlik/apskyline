package ifis.skysim2.algorithms.pQueueSync;

import ifis.skysim2.algorithms.AbstractSkylineAlgorithm;
import ifis.skysim2.algorithms.SkylineAlgorithmBNL.BNLWindowPolicy;
import java.util.List;
import java.util.concurrent.CyclicBarrier;
import ifis.skysim2.algorithms.pQueueSyncl.reorganizers.AdaptiveReorganizer;
import ifis.skysim2.algorithms.pQueueSyncl.reorganizers.Reorganizer;
import ifis.skysim2.data.sources.PointSource;
import ifis.skysim2.simulator.SimulatorConfiguration;

public abstract class SkylineAlgorithmAbstractBarrierSync extends AbstractSkylineAlgorithm {

        protected BNLWindowPolicy bnlWindowPolicy;
	protected int numOfWorkers;
	
	protected static final int queueCapacity = 100;
	protected static final int objectsToPetrify = 5000;
	protected static final Class reorganizerClass = AdaptiveReorganizer.class;
	protected List<SkylineWorker> workers;
	protected List<Thread> threads;
	protected Reorganizer reorganizer;
	public CyclicBarrier barrier;

	public SkylineAlgorithmAbstractBarrierSync() {
		super();
	}

	@Override
	public void setExperimentConfig(SimulatorConfiguration config) {
	    super.setExperimentConfig(config);
	    numOfWorkers = config.getNumberOfCPUs();
	    bnlWindowPolicy = config.getBnlWindowPolicy();
	}

	@Override
	public long getCPUcost() {
	    cpuCost = 0;
	    for (SkylineWorker worker : workers) {
	        cpuCost += worker.getCpuCost();
	    }
	    return cpuCost;
	}

	public long getReorgCost() {
	    return reorganizer.getReorgTimeNS();
	}

	@Override
	public long getReorgTimeNS() {
	    return reorganizer.getReorgTimeNS();
	}

	@Override
	public abstract List<float[]> compute(PointSource data);
	
	protected abstract void setupWorkers(int numOfWorkers, PointSource data);
}