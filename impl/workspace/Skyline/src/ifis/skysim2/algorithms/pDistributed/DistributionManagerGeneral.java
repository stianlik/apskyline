package ifis.skysim2.algorithms.pDistributed;

import cern.colt.list.IntArrayList;
import ifis.skysim2.algorithms.SkylineAlgorithm;
import ifis.skysim2.algorithms.SkylineAlgorithmBNL;
import ifis.skysim2.data.points.LinkedPointList;
import ifis.skysim2.data.sources.PointSource;
import ifis.skysim2.data.sources.PointSourceRAM;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;

public class DistributionManagerGeneral implements DistributionManager {

    private static enum JoinStrategy {

	LOCAL,
	REMOTE
    };

    private static enum DistributionStrategy {

	RANDOM,
	SEQUENTIAL,
	ANGLE_BASED,
	ANGLE_BASED_SIMPLE
    };
    private static JoinStrategy joinStrategy = JoinStrategy.REMOTE;
//	private static JoinStrategy joinStrategy = JoinStrategy.LOCAL;
//	private static DistributionStrategy distributionStrategy = DistributionStrategy.RANDOM;
//	private static DistributionStrategy distributionStrategy = DistributionStrategy.SEQUENTIAL;
//	private static DistributionStrategy distributionStrategy = DistributionStrategy.ANGLE_BASED;
    private static DistributionStrategy distributionStrategy = DistributionStrategy.ANGLE_BASED_SIMPLE;
    private int numOfWorkers;
    private DistributedWorker[] workers;
    private long reorgTimeNS = 0;
    private int cpuCost = 0;
    private int[] sizes;
    private int d = -1;

    @Override
    public void init(int d) {
	this.d = d;
	cpuCost = 0;
	reorgTimeNS = 0;
    }

    // join phase
    @Override
    public void run() {
	for (int i = 0; i < numOfWorkers; i++) {
	    sizes[i] = workers[i].getWindow().size();
	}
	long startTime = System.nanoTime();
	switch (joinStrategy) {
	    case REMOTE:
		joinLocalSkylinesAtWorkers();
		break;
	    case LOCAL:
		joinLocalSkylinesCentrally();
		break;
	    default:
		throw new UnsupportedOperationException("Not supported yet.");
	}
	reorgTimeNS = System.nanoTime() - startTime;
    }

    private void joinLocalSkylinesAtWorkers() {
	for (int i = 0; i < numOfWorkers; i++) {
	    List<float[]> unionOfLocalSkylines = new LinkedPointList(d);
	    for (int j = 0; j < numOfWorkers; j++) {
		if (j != i) {
		    unionOfLocalSkylines.addAll(workers[j].getWindow());
		}
	    }
	    workers[i].setData(unionOfLocalSkylines);
	    workers[i].setInsertDisabled(true);
	}
    }

    private void joinLocalSkylinesCentrally() {
	List<float[]> emptyList = new ArrayList<float[]>();
	for (int i = 0; i < numOfWorkers; i++) {
	    workers[i].setData(emptyList);
	}
    }

    @Override
    public void distributeData(List<float[]> data) {
	switch (distributionStrategy) {
	    case RANDOM:
		distributeDataRandomly(data);
		break;
	    case SEQUENTIAL:
		distributeDataSequentially(data);
		break;
	    case ANGLE_BASED:
		distributeDataAngleBased(data);
		break;
	    case ANGLE_BASED_SIMPLE:
		distributeDataAngleBasedSimple(data);
		break;
	    default:
		throw new UnsupportedOperationException("Not supported yet.");
	}
    }

    public void distributeDataRandomly(List<float[]> data) {
	if (!(data instanceof PointSourceRAM)) {
	    throw new UnsupportedOperationException("Not supported yet.");
	} else {
	    int n = data.size();
	    int[] distPattern = new int[n];
	    List<List<float[]>> dataI = new ArrayList<List<float[]>>(numOfWorkers);
	    for (int i = 0; i < numOfWorkers; i++) {
		int fromIndex = i * n / numOfWorkers;
		int toIndex = (i + 1) * n / numOfWorkers;
		Arrays.fill(distPattern, fromIndex, toIndex, i);
		dataI.add(new PointSourceRAM(d, new float[d * (toIndex - fromIndex)]));
	    }
	    IntArrayList distPatternList = new IntArrayList(distPattern);
	    distPatternList.shuffle();
	    distPattern = distPatternList.elements();

	    int[] indexI = new int[numOfWorkers];
	    for (int k = 0; k < n; k++) {
		int i = distPattern[k];
		dataI.get(i).set(indexI[i], data.get(k));
		indexI[i]++;
	    }

	    for (int i = 0; i < numOfWorkers; i++) {
		workers[i].setData(dataI.get(i));
	    }
	}
    }

    public void distributeDataSequentially(List<float[]> data) {
	int n = data.size();
	for (int i = 0; i < numOfWorkers; i++) {
	    int fromIndex = i * n / numOfWorkers;
	    int toIndex = (i + 1) * n / numOfWorkers;
	    List<float[]> dataI = data.subList(fromIndex, toIndex);
	    workers[i].setData(dataI);
	}
    }

    // use distribution strategy from original paper
    public void distributeDataAngleBased(List<float[]> data) {
	if (!((numOfWorkers == 9) && (d == 3))) {
	    throw new UnsupportedOperationException("Not supported yet.");
	}
	List<List<float[]>> dataI = new ArrayList<List<float[]>>(numOfWorkers);
	for (int i = 0; i < numOfWorkers; i++) {
	    dataI.add(new PointSourceRAM(d));
	}
	// for case p = 9, d = 3
	final double phi11 = 48.24 / 360 * 2 * Math.PI;
	final double phi12 = 70.55 / 360 * 2 * Math.PI;
	final double phi21 = 30.0 / 360 * 2 * Math.PI;
	final double phi22 = 60.0 / 360 * 2 * Math.PI;

	ListIterator<float[]> iter = data.listIterator();
	while (iter.hasNext()) {
	    float[] point = iter.next();
	    double phi1 = Math.atan(Math.sqrt(point[d - 1] * point[d - 1] + point[d - 2] * point[d - 2]) / point[d - 3]);
	    double phi2 = Math.atan(point[d - 1] / point[d - 2]);
	    if (phi1 < phi11) {
		if (phi2 < phi21) {
		    dataI.get(0).add(point);
		} else if (phi2 < phi22) {
		    dataI.get(1).add(point);
		} else {
		    dataI.get(2).add(point);
		}
	    } else if (phi1 < phi12) {
		if (phi2 < phi21) {
		    dataI.get(3).add(point);
		} else if (phi2 < phi22) {
		    dataI.get(4).add(point);
		} else {
		    dataI.get(5).add(point);
		}
	    } else {
		if (phi2 < phi21) {
		    dataI.get(6).add(point);
		} else if (phi2 < phi22) {
		    dataI.get(7).add(point);
		} else {
		    dataI.get(8).add(point);
		}
	    }
	}
	for (int i = 0; i < numOfWorkers; i++) {
	    workers[i].setData(dataI.get(i));
	    System.out.format("Worker %d manages %d points%n", i, dataI.get(i).size());
	}
    }

    // use only one angle: phi_{d - 1}
    public void distributeDataAngleBasedSimple(List<float[]> data) {
	List<List<float[]>> dataI = new ArrayList<List<float[]>>(numOfWorkers);
	for (int i = 0; i < numOfWorkers; i++) {
	    dataI.add(new PointSourceRAM(d));
	}

	final double[] phis = new double[numOfWorkers];
	for (int i = 0; i < numOfWorkers; i++) {
	    if (i + 1 <= numOfWorkers / 2.0) {
		phis[i] = Math.atan(2.0 * (i + 1) / numOfWorkers);
	    } else {
		phis[i] = Math.PI / 2 - Math.atan(2.0 - 2.0 * (i + 1) / numOfWorkers);
	    }
	}

	ListIterator<float[]> iter = data.listIterator();
	while (iter.hasNext()) {
	    float[] point = iter.next();
	    double phi = Math.atan(point[d - 1] / point[d - 2]);
	    for (int i = 0; i < numOfWorkers; i++) {
		if (phi <= phis[i]) {
		    dataI.get(i).add(point);
		    break;
		}
	    }
	}
	for (int i = 0; i < numOfWorkers; i++) {
	    workers[i].setData(dataI.get(i));
	    System.out.format("Worker %d manages %d points%n", i, dataI.get(i).size());
	}
    }

    @Override
    public List<float[]> getGlobalSkyline() {
	for (int i = 0; i < numOfWorkers; i++) {
	    System.out.format("size before/after join at worker %d:  %d --> %d%n", i, sizes[i], workers[i].getWindow().size());
	}
	long startTime = System.nanoTime();
	cpuCost = 0;
	List<float[]> result;
	switch (joinStrategy) {
	    case REMOTE:
		result = new LinkedPointList(d);
		for (int i = 0; i < numOfWorkers; i++) {
		    result.addAll(workers[i].getWindow());
		    cpuCost += workers[i].getCPUcost();
		}
		break;
	    case LOCAL:
		PointSource unionOfLocalSkylines = new LinkedPointList(d);
		for (int i = 0; i < numOfWorkers; i++) {
		    unionOfLocalSkylines.addAll(workers[i].getWindow());
		    cpuCost += workers[i].getCPUcost();
		}
		SkylineAlgorithm skyalg = new SkylineAlgorithmBNL();
		result = skyalg.compute(unionOfLocalSkylines);
		cpuCost += skyalg.getCPUcost();
		break;
	    default:
		throw new UnsupportedOperationException("Not supported yet.");
	}
	reorgTimeNS += System.nanoTime() - startTime;
	return result;
    }

    @Override
    public long getReorgTimeNS() {
	return reorgTimeNS;
    }

    @Override
    public int getCPUcost() {
	return cpuCost;
    }

    @Override
    public void setWorkers(DistributedWorker[] workers) {
	this.workers = workers;
	this.numOfWorkers = workers.length;
	this.sizes = new int[this.numOfWorkers];
    }

    @Override
    public String toString() {
	return String.format("distribution strategy: %s, join strategy: %s", distributionStrategy, joinStrategy);
    }

    @Override
    public String getStatusFlags() {
	StringBuffer sb = new StringBuffer();
	if (joinStrategy == JoinStrategy.LOCAL) {
	    sb.append("L");
	} else {
	    sb.append("R");
	}
	if (distributionStrategy == DistributionStrategy.ANGLE_BASED) {
	    sb.append("A");
	} else if (distributionStrategy == DistributionStrategy.RANDOM) {
	    sb.append("R");
	} else if (distributionStrategy == DistributionStrategy.SEQUENTIAL) {
	    sb.append("S");
	}
	return sb.toString();
    }
}