package ifis.skysim2.algorithms;

import ifis.skysim2.data.points.LinkedPointList;
import ifis.skysim2.data.sources.PointSource;
import ifis.skysim2.data.trees.ddtree.DominanceDecisionTree;
import java.util.Arrays;
import java.util.List;

public class SkylineAlgorithmDominanceDecisionTree extends AbstractSkylineAlgorithm {

    private long totalTimeNS;
    private long cpuCost;
    private long ioCost;

    @Override
    public List<float[]> compute(PointSource data) {
	ioCost = 0;
	long startTime = System.nanoTime();

	DominanceDecisionTree tree = new DominanceDecisionTree();
	final int n = data.size();
	for (int i = 0; i < n; i++) {
	    float[] dataPoint = data.get(i);
//	    System.out.println("Inserting: " + Arrays.toString(dataPoint));
	    ioCost++;
	    tree.update(dataPoint);
//	    System.out.println(tree.deepToString());
//	    System.out.println("\n\n\n\n");
	}
	cpuCost = tree.getNumberOfComparisons();
	
	totalTimeNS = System.nanoTime() - startTime;

	int d = data.getD();
	LinkedPointList result = new LinkedPointList(d);
	for (float[] point: tree) {
	    result.add(point);
	}
	
	return result;
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
	return -1;
    }

    @Override
    public String toString() {
	return "DDTree" ;
    }

    @Override
    public String getShortName() {
	return "DDTree";
    }

}
