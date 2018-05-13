package ifis.skysim2.algorithms;

import ifis.skysim2.data.points.LinkedPointList;
import ifis.skysim2.data.sources.PointSource;
import ifis.skysim2.data.trees.domfreequadtree.DomFreeQuadTreePL;
import java.util.List;

public class SkylineAlgorithmDomFreeQuadTree extends AbstractSkylineAlgorithm {

    private long totalTimeNS;
    private long cpuCost;
    private long ioCost;

    @Override
    public List<float[]> compute(PointSource data) {
	ioCost = 0;
	long startTime = System.nanoTime();

	DomFreeQuadTreePL tree = new DomFreeQuadTreePL();
	final int n = data.size();
	for (int i = 0; i < n; i++) {
	    float[] dataPoint = data.get(i);
	    ioCost++;
	    tree.add(dataPoint);
	}
	cpuCost = tree.getNumberOfComparions();
	
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
	return "QuadTree" ;
    }

    @Override
    public String getShortName() {
	return "QuadTree";
    }

}
