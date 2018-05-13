package ifis.skysim2.algorithms.pUnsync;

import ifis.skysim2.algorithms.AbstractSkylineAlgorithm;
import ifis.skysim2.algorithms.SkylineAlgorithm;
import ifis.skysim2.algorithms.pQueueSync.PointSourceQueue;
import ifis.skysim2.data.points.PointListLinkedASync;
import ifis.skysim2.data.points.PointListLinkedSync_FullLock;
import ifis.skysim2.data.points.PointListLinkedSync_PartialLock;
import ifis.skysim2.data.sources.PointSource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SkylineAlgorithmPUnsync extends AbstractSkylineAlgorithm implements SkylineAlgorithm {

    private static final int numOfWorkers = 2;
    private List<SkylineWorkerPUnsync> workers;
    private List<Thread> threads;
    private PointSourceQueue pointListQueue;
    private long totalTimeNS;
    private List<float[]> window;

    public SkylineAlgorithmPUnsync() {
    }

    private void setupWorkers(int numOfWorkers, PointSourceQueue initialDataQueue) {
        // setup window
//		window = Collections.synchronizedList(new LinkedList<float[]>());
        window = new PointListLinkedSync_PartialLock();
        //
        workers = new ArrayList<SkylineWorkerPUnsync>();
        threads = new ArrayList<Thread>();
        //

        for (int i = 0; i < numOfWorkers; i++) {

            SkylineWorkerPUnsync worker = new SkylineWorkerPUnsync(this, window, initialDataQueue);
            worker.setId(i);
            workers.add(worker);
            threads.add(new Thread(worker, "Worker-" + i));
        }
    }

    @Override
    public List<float[]> compute(PointSource data) {
        long startTime = System.nanoTime();

        // setup data source
        pointListQueue = new PointSourceQueue(data);
        pointListQueue.setObjectToPetrify(-1);
        setupWorkers(numOfWorkers, pointListQueue);

        // run!
        for (Thread thread : threads) {
            thread.start();
        }

        // wait
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException ex) {
                Logger.getLogger(SkylineAlgorithmPUnsync.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        totalTimeNS = System.nanoTime() - startTime;

        return window;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getIOcost() {
        return -1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getCPUcost() {
        long cpuCost = 0;
        for (SkylineWorkerPUnsync worker : workers) {
            cpuCost += worker.getCpuCost();
        }
        return cpuCost;
    }

    /**
     * The time spent reorganizing in milliseconds.
     * @return the reorg time
     */
    public long getReorgCost() {
        return 0;
    }

    @Override
    public long getTotalTimeNS() {
        return totalTimeNS;
    }

    @Override
    public long getReorgTimeNS() {
        return 0;
    }

    @Override
    public String toString() {
        return String.format("Parallel_Unsync#%d", numOfWorkers);
    }

    @Override
    public String getShortName() {
        StringBuffer sb = new StringBuffer("Unsync(");
        sb.append(numOfWorkers);
        return sb.append(")").toString();
    }
}
