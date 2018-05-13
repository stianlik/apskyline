package ifis.skysim2.algorithms;

import ifis.skysim2.data.points.LinkedPointList;
import ifis.skysim2.data.sources.PointSource;
import ifis.skysim2.data.trees.rtree.BulkLoader;
import ifis.skysim2.data.trees.rtree.BulkLoaderSTR;
import ifis.skysim2.data.trees.rtree.RTree;
import ifis.skysim2.common.datastructures.PriorityQueueDouble;
import ifis.skysim2.data.tools.PointRelationship;
import java.util.List;
import java.util.ListIterator;

/*
 * R-tree-based BBS skyline algorithm
 * Papadias, Tao, Fu, Seeger: Progressive Skyline Computationin Database Systems
 */
public class SkylineAlgorithmBBS extends AbstractSkylineAlgorithm implements SkylineAlgorithm {

    private long ioCost;
    private long cpuCost;
    private long totalTimeNS;

    public SkylineAlgorithmBBS() {
    }

    @Override
    public List<float[]> compute(PointSource data) {
	long start = System.nanoTime();
	BulkLoader bl = new BulkLoaderSTR();
	RTree rtree = bl.bulkLoad(data);
	long stop = System.nanoTime();
	System.out.format("Generate R-tree: %.2f s\n", (double) (stop - start) / 1000000000);

	long startTime = System.nanoTime();
	ioCost = 0;
	cpuCost = 0;

	PriorityQueueDouble<RTree.Node> heap = new PriorityQueueDouble<RTree.Node>();
	int d = data.getD();
	List<float[]> window = new LinkedPointList(d);

	RTree.Node root = rtree.getRoot();
	heap.add(root, getMaxdist(root));

	while (!heap.isEmpty()) {
	    RTree.Node node = heap.remove();
	    if (!node.isLeaf()) {
		// next heap entry is a node
		// check if the node's mbr is dominated by some window point
		boolean nodeIsDominated = false;
		float[] mbrMaxNode = node.getMBR().getHigh();
		ListIterator<float[]> windowIter = window.listIterator();
		while (windowIter.hasNext()) {
		    final float[] windowPoint = windowIter.next();
		    PointRelationship dom = ifis.skysim2.data.tools.PointComparator.compare(windowPoint, mbrMaxNode);
		    cpuCost++;
		    if (dom == PointRelationship.DOMINATES) {
			nodeIsDominated = true;
			break;
		    }
		}
		// if so, discard node; otherwise, process node
		if (!nodeIsDominated) {
		    // add all children of node to queue
		    RTree.Node[] children = node.getChildren();
		    for (RTree.Node child : children) {
			heap.add(child, getMaxdist(child));
		    }
		}
	    } else {
		// next heap entry is a point
		float[] point = node.getItem();
		ListIterator<float[]> windowIter2 = window.listIterator();
		boolean pointIsDominated = false;
		while (windowIter2.hasNext()) {
		    final float[] windowPoint = windowIter2.next();
		    PointRelationship dom = ifis.skysim2.data.tools.PointComparator.compare(windowPoint, point);
		    cpuCost++;
		    if (dom == PointRelationship.DOMINATES) {
			pointIsDominated = true;
			break;
		    }
		}
		if (!pointIsDominated) {
		    window.add(point);
		}
	    }
	}
	totalTimeNS = System.nanoTime() - startTime;
	return window;
    }

    private static double getMaxdist(RTree.Node node) {
	double maxdist = 0;
	int d = node.getD();
	float[] mbrMax = node.getMBR().getHigh();
	for (int i = 0; i < d; i++) {
	    maxdist += 1 - mbrMax[i];
	}
	return maxdist;
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
	return 0;
    }

    @Override
    public String toString() {
	StringBuffer buf = new StringBuffer();
	buf.append("BBS");
	return buf.toString();
    }

    @Override
    public String getShortName() {
	StringBuffer sb = new StringBuffer("BBS");
	return sb.toString();
    }
}