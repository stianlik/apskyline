package ifis.skysim2.junk;

import ifis.skysim2.algorithms.*;
import ifis.skysim2.data.points.LinkedPointList;
import ifis.skysim2.data.sources.PointSource;
import ifis.skysim2.simulator.SimulatorConfiguration;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class SkylineAlgorithmParallelScanner extends AbstractSkylineAlgorithm {

    private int numCPUs;

    @Override
    public void setExperimentConfig(SimulatorConfiguration config) {
	super.setExperimentConfig(config);
	numCPUs = config.getNumberOfCPUs();
    }

    @Override
    public List<float[]> compute(PointSource data) {
	return compute(data, null);
    }

    public List<float[]> compute(PointSource data, ExecutorService executor) {
	long startTime = System.nanoTime();

	final AtomicInteger dataCounter = new AtomicInteger(0);
	if (executor == null) {
	    executor = Executors.newFixedThreadPool(numCPUs);
	}
	for (int i = 0; i < numCPUs; i++) {
//	    Runnable scanner = new Scanner(data, dataCounter);
	    Runnable scanner = new Scanner2(data, numCPUs, i);
	    executor.execute(scanner);
	}
	executor.shutdown();
	try {
	    executor.awaitTermination(1, TimeUnit.DAYS);
	} catch (InterruptedException ex) {
	    throw new UnsupportedOperationException("Error handling not supported yet.");
	}

	totalTimeNS = System.nanoTime() - startTime;

	int d = data.getD();
	return new LinkedPointList(d);
    }

    @Override
    public String getShortName() {
	return String.format("Scanner (%d)", numCPUs);
    }

    @Override
    public String toString() {
	return String.format("Scanner (%d CPUs)", numCPUs);
    }

    private static class Scanner implements Runnable {

	private final int n;
	private final PointSource data;
	private final AtomicInteger dataCounter;

	private Scanner(final PointSource data, final AtomicInteger dataCounter) {
	    n = data.size();
	    this.data = data;
	    this.dataCounter = dataCounter;
	}

	@Override
	public void run() {
	    int iData;
	    dataloop:
	    while ((iData = dataCounter.getAndIncrement()) < n) {
		final float[] pData = data.get(iData);
		if (pData.length == 5) {
		    System.out.println("XXXXXXXXXXXXXXXXXXXXX");
		}
	    }
	}
    }

    private static class Scanner2 implements Runnable {

	private final int n;
	private final PointSource data;
	private int iData;
	private final int numCPUs;

	private Scanner2(final PointSource data, final int numCPUs, final int threadID) {
	    n = data.size();
	    this.data = data;
	    this.numCPUs = numCPUs;
	    iData = threadID;
	}

	@Override
	public void run() {
	    dataloop:
	    while (iData < n) {
		final float[] pData = data.get(iData);
		if (pData.length == 5) {
		    System.out.println("XXXXXXXXXXXXXXXXXXXXX");
		}
		iData += numCPUs;
	    }
	}
    }
}
