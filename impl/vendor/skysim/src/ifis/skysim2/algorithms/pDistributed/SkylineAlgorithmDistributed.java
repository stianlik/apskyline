/*
 * given: p workers
 * (1) split data into p parts of equal size
 * (2) send parts to workers
 * (3) workers compute local skylines and send them back to coordinator
 * (4) coordinator sends union of local skyline to each worker
 * (5) workers eliminate points from local skylines that are dominated
 *     by some point in the union of local skylines
 * (6) workers send updated local skylines to coordinator
 * (7) result is the union of all updated local skylines
 */
package ifis.skysim2.algorithms.pDistributed;

import ifis.skysim2.algorithms.AbstractSkylineAlgorithm;
import ifis.skysim2.data.sources.PointSource;
import ifis.skysim2.simulator.SimulatorConfiguration;
import java.util.List;
import java.util.concurrent.CyclicBarrier;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SkylineAlgorithmDistributed extends AbstractSkylineAlgorithm {

    private int numOfWorkers;
    private CyclicBarrier barrier;
    private long integrationTimeNS;
    private final DistributionManager distmanager = new DistributionManagerGeneral();

    @Override
    public void setExperimentConfig(SimulatorConfiguration config) {
	super.setExperimentConfig(config);
	numOfWorkers = config.getNumberOfCPUs();
    }

    @Override
    public synchronized List<float[]> compute(PointSource data) {
	int d = data.getD();
	distmanager.init(d);
        DistributedWorker[] workers = new DistributedWorker[numOfWorkers];
        Thread[] threads = new Thread[numOfWorkers];

        for (int i = 0; i < numOfWorkers; i++) {
            DistributedWorker worker = new DistributedWorker(this, d, this.config);
            workers[i] = worker;
            threads[i] = new Thread(worker, String.format("Worker-%d", i));
        }

        distmanager.setWorkers(workers);

        barrier = new CyclicBarrier(numOfWorkers, distmanager);

        distmanager.distributeData(data);

	long startTime = System.nanoTime();

        for (int i = 0; i < numOfWorkers; i++) {
            threads[i].start();
        }

        for (int i = 0; i < numOfWorkers; i++) {
            try {
                threads[i].join();
            } catch (InterruptedException ex) {
                Logger.getLogger(SkylineAlgorithmDistributed.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        List<float[]> globalSkyline = distmanager.getGlobalSkyline();
        integrationTimeNS = 0;
        for (int i = 0; i < numOfWorkers; i++) {
            if (workers[i].getIntegrationCostNS() > integrationTimeNS) {
                integrationTimeNS = workers[i].getIntegrationCostNS();
            }
        }
        totalTimeNS = System.nanoTime() - startTime;
        return globalSkyline;
    }

    public CyclicBarrier getBarrier() {
        return barrier;
    }

    @Override
    public long getIOcost() {
        return -1;
    }

    @Override
    public long getCPUcost() {
        return distmanager.getCPUcost();
    }

    @Override
    public long getTotalTimeNS() {
        return totalTimeNS;
    }

    @Override
    public long getReorgTimeNS() {
        return distmanager.getReorgTimeNS()+integrationTimeNS;
    }

    @Override
    public String toString() {
        return String.format("Distributed#%d (distribution manager: %s)", numOfWorkers, distmanager);
    }

    @Override
    public String getShortName() {
        StringBuffer sb = new StringBuffer("Dist(");
        sb.append(numOfWorkers);
        sb.append(distmanager.getStatusFlags());
        return sb.append(")").toString();
    }
}
