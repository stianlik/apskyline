package ifis.skysim2.algorithms;

import ifis.skysim2.simulator.SimulatorConfiguration;

public abstract class AbstractSkylineAlgorithm implements SkylineAlgorithm {

    protected SimulatorConfiguration config;
    protected long totalTimeNS;
    protected long cpuCost;
    protected long ioCost;
    protected long preprocessTimeNS;
    protected long reorgTimeNS;

    @Override
    public SimulatorConfiguration getExperimentConfig() {
        return config;
    }

    @Override
    public void setExperimentConfig(SimulatorConfiguration config) {
        this.config = config;
    }

    @Override
    public long getIOcost() {
	return ioCost;
    }

    @Override
    public long getCPUcost() {
	return cpuCost;
    }

    @Override
    public long getTotalTimeNS() {
	return totalTimeNS;
    }

    @Override
    public long getReorgTimeNS() {
	return reorgTimeNS;
    }

    @Override
    public long getPreprocessTimeNS() {
	return preprocessTimeNS;
    }
}
