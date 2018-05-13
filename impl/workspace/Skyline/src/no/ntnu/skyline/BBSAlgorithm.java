package no.ntnu.skyline;

import ifis.skysim2.algorithms.AbstractSkylineAlgorithm;
import ifis.skysim2.common.datastructures.PriorityQueueDouble;
import ifis.skysim2.data.points.LinkedPointList;
import ifis.skysim2.data.sources.PointSource;
import ifis.skysim2.data.tools.PointRelationship;
import ifis.skysim2.data.trees.rtree.BulkLoader;
import ifis.skysim2.data.trees.rtree.BulkLoaderSTR;
import ifis.skysim2.data.trees.rtree.RTree;

import java.util.List;
import java.util.ListIterator;

/*
 * R-tree-based BBS skyline algorithm
 * Papadias, Tao, Fu, Seeger: Progressive Skyline Computationin Database Systems
 */
public class BBSAlgorithm extends AbstractSkylineAlgorithm {

	@Override
	public List<float[]> compute(PointSource data) {
		
		startTimer("runtime");
		
		startTimer("partitioning");
		BulkLoader bl = new BulkLoaderSTR();
		RTree rtree = bl.bulkLoad(data);
		stopTimer("partitioning");


		PriorityQueueDouble<RTree.Node> heap = new PriorityQueueDouble<RTree.Node>();
		int d = data.getD();
		List<float[]> window = new LinkedPointList(d);

		RTree.Node root = rtree.getRoot();
		heap.add(root, -getMindist(root));

		while (!heap.isEmpty()) {
			RTree.Node node = heap.remove();
			if (!node.isLeaf()) {
				// next heap entry is a node
				// check if the node's mbr is dominated by some window point
				boolean nodeIsDominated = false;
				float[] mbrMaxNode = node.getMBR().getLow();
				ListIterator<float[]> windowIter = window.listIterator();
				while (windowIter.hasNext()) {
					final float[] windowPoint = windowIter.next();
					PointRelationship dom = ifis.skysim2.data.tools.PointComparator
							.compare(windowPoint, mbrMaxNode);
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
						heap.add(child, -getMindist(child));
					}
				}
			} else {
				// next heap entry is a point
				float[] point = node.getItem();
				ListIterator<float[]> windowIter2 = window.listIterator();
				boolean pointIsDominated = false;
				while (windowIter2.hasNext()) {
					final float[] windowPoint = windowIter2.next();
					PointRelationship dom = ifis.skysim2.data.tools.PointComparator
							.compare(windowPoint, point);
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
		totalTimeNS = stopTimer("runtime");
		return window;
	}

	private static double getMindist(RTree.Node node) {
		double mindist = 0;
		int d = node.getD();
		float[] mbrMin = node.getMBR().getLow();
		for (int i = 0; i < d; i++) {
			mindist += 1 - mbrMin[i];
		}
		return mindist;
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