package ifis.skysim2.algorithms.pQueueSync;

import ifis.skysim2.algorithms.pQueueSyncl.reorganizers.Reorganizer;
import ifis.skysim2.data.sources.PointSource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CyclicBarrier;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class SkylineAlgorithmPQueueSync extends SkylineAlgorithmAbstractBarrierSync {

    
    private PointSourceQueue pointListQueue;

    /**
     * Constructor.
     */
    public SkylineAlgorithmPQueueSync() {
    }

    /**
     * Creates all necesarry skyline workers and their repective threads.
     * @param numOfWorkers number of workers to create
     * @param initialDataQueue the data queue wrapping the point source
     */
    @Override
    protected void setupWorkers(int numOfWorkers, PointSource data) {
        // setup data source
        pointListQueue = new PointSourceQueue(data);
        pointListQueue.setObjectToPetrify(objectsToPetrify);
        //
        workers = new ArrayList<SkylineWorker>();
        threads = new ArrayList<Thread>();
        //
	int d = data.getD();
	//
        BlockingQueue<float[]> inQueue = pointListQueue;
        BlockingQueue<float[]> outQueue;
        for (int i = 0; i < numOfWorkers; i++) {
            if (i == numOfWorkers - 1) {
                outQueue = null;
            } else {
//               outQueue = new ArrayBlockingQueue<float[]>(queueCapacity);
                 outQueue = new NonBlockingBlockQueue<float[]>();
            }
            SkylineWorkerQueueSync worker = new SkylineWorkerQueueSync(d, inQueue, outQueue, this);
            worker.setId(i);
            workers.add(worker);
            threads.add(new Thread(worker, "Worker-" + i));
            inQueue = outQueue;
        }
    }

    @Override
    public synchronized List<float[]> compute(PointSource data) {
        long startTime = System.nanoTime();


        // create new worker threads and reorganizer
        this.setupWorkers(numOfWorkers, data);
        try {
            reorganizer = (Reorganizer) reorganizerClass.newInstance();
        //  System.out.println("Using Reorganizer: " + reorganizer);

        } catch (Exception ex) {
            Logger.getLogger(SkylineAlgorithmPQueueSync.class.getName()).log(Level.SEVERE, null, ex);
        }
        reorganizer.setWorkers(workers);

        // setup thread synchronization
        barrier = new CyclicBarrier(numOfWorkers, reorganizer);

        // run!
        for (Thread thread : threads) {
            thread.start();
        }

        // wait
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException ex) {
                Logger.getLogger(SkylineAlgorithmPQueueSync.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        // TODO: Join lists to form result list
        int size = 0;
        for (SkylineWorker worker : workers) {
            size += worker.getWindow().size();
        }

        totalTimeNS = System.nanoTime() - startTime;

        return Arrays.asList(new float[size][0]);
    }

    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("Parallel_QueueSync#");
        buf.append(numOfWorkers);
        buf.append(" (reorganizer: " + reorganizerClass.getSimpleName() + ")");
        return buf.toString();
    }

    @Override
    public String getShortName() {
        StringBuffer sb = new StringBuffer("Queue(");
        sb.append(numOfWorkers);
        return sb.append(")").toString();
    }
}