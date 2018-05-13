package ifis.skysim2.algorithms.pDistributed;

import ifis.skysim2.algorithms.BNLProfiler;
import ifis.skysim2.algorithms.SkylineAlgorithmBNL;
import ifis.skysim2.algorithms.SkylineAlgorithmBNL.BNLWindowPolicy;
import ifis.skysim2.data.points.LinkedPointList;
import ifis.skysim2.data.points.PointList;
import ifis.skysim2.simulator.SimulatorConfiguration;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.BrokenBarrierException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DistributedWorker implements Runnable {

    private final SkylineAlgorithmDistributed coordinator;
    private List<float[]> data;
    private final PointList window;
    private long cpuCost = 0;
    private boolean insertDisabled = false;
    protected long integrationCostNS;
    private final BNLWindowPolicy bnlWindowPolicy;

    public DistributedWorker(SkylineAlgorithmDistributed coordinator, int d, SimulatorConfiguration config) {
        this.coordinator = coordinator;
	bnlWindowPolicy = config.getBnlWindowPolicy();
	window = new LinkedPointList(d);
    }

    public void setData(List<float[]> data) {
        this.data = data;
    }

    public List<float[]> getWindow() {
        return window;
    }

    @Override
    public void run() {
        compute();
        try {
            coordinator.getBarrier().await();
        } catch (InterruptedException ex) {
            Logger.getLogger(DistributedWorker.class.getName()).log(Level.SEVERE, null, ex);
        } catch (BrokenBarrierException ex) {
            Logger.getLogger(DistributedWorker.class.getName()).log(Level.SEVERE, null, ex);
        }
        long startTime = System.nanoTime();
        compute();
        integrationCostNS = System.nanoTime() - startTime;
    }

    private void compute() {
        final ListIterator<float[]> dataIter = data.listIterator();
	BNLProfiler profiler = new BNLProfiler();

        while (dataIter.hasNext()) {
            final float[] dataPoint = dataIter.next();
	    SkylineAlgorithmBNL.bnlOperation(window, dataPoint, !insertDisabled, profiler);
	}
	cpuCost = profiler.getCpuCost();
    }

    public long getIntegrationCostNS() {
        return integrationCostNS;
    }

    public long getCPUcost() {
        return cpuCost;
    }

    public void setInsertDisabled(boolean insertDisabled) {
        this.insertDisabled = insertDisabled;
    }
}
