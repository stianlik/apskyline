package ifis.skysim2.algorithms;

import java.util.HashMap;
import java.util.Map;

import ifis.skysim2.simulator.SimulatorConfiguration;

public abstract class AbstractSkylineAlgorithm implements SkylineAlgorithm {

	protected Map<String,long[]> timers =  new HashMap<String, long[]>();
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
	
	public long stopTimer(String name) {
		long timer[] = timers.get(name);
		timer[1] = System.nanoTime();
		timer[2] = timer[1] - timer[0];
		return timer[2];
	}

	public void startTimer(String name) {
		timers.put(name, new long[]{System.nanoTime(),0,0});
	}
	
	public double getEllapsedTimeInSeconds(String name) {
		if (!timers.containsKey(name)) {
			return 0.0;
		}
		return (double) timers.get(name)[2] / 1000000000;
	}
}
