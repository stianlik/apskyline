package ifis.skysim2.algorithms.pDistributed;

import java.util.List;

public interface DistributionManager extends Runnable {
	public void distributeData(List<float[]> data);
	public List<float[]> getGlobalSkyline();
	public long getReorgTimeNS();
	public int getCPUcost();
	public void setWorkers(DistributedWorker[] workers);
        public String getStatusFlags();
	public void init(int d);
}
