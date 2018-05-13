package ifis.skysim2.algorithms;

import ifis.skysim2.simulator.SimulatorConfiguration;
import ifis.skysim2.data.sources.PointSource;
import java.util.List;

public interface SkylineAlgorithm {

    public List<float[]> compute(PointSource data);

    public long getIOcost();

    public long getCPUcost();

    public long getTotalTimeNS();

    public long getReorgTimeNS();

    public long getPreprocessTimeNS();

    public String getShortName();

    public SimulatorConfiguration getExperimentConfig();

    public void setExperimentConfig(SimulatorConfiguration config);
}