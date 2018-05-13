package no.ntnu.skyline;

import ifis.skysim2.algorithms.AbstractSkylineAlgorithm;
import ifis.skysim2.data.sources.PointSource;
import ifis.skysim2.data.tools.PointComparator;
import ifis.skysim2.data.tools.PointRelationship;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class BNLAlgorithm extends AbstractSkylineAlgorithm {
	
	@Override
	public List<float[]> compute(PointSource data) {
		
		long startTime = System.nanoTime();
		
		List<float[]> window = new LinkedList<float[]>();
		Iterator<float[]> windowIter;
		PointRelationship dominance = null;
		float[] q = {};
		
		for (float[] p : data) {
			
			windowIter = window.listIterator();
			while (windowIter.hasNext()) {
				q = windowIter.next();
				dominance = PointComparator.compare(q, p);
				if (dominance == PointRelationship.DOMINATES) {
					windowIter.remove();
					window.add(0,q);
					break;
				}
				else if (dominance == PointRelationship.IS_DOMINATED_BY) {
					windowIter.remove();
				}
			}
			
			if (dominance == PointRelationship.IS_DOMINATED_BY) {
				window.add(0,p);
			}
			else if (dominance != PointRelationship.DOMINATES) {
				window.add(p);
			}
			
		}
		
		totalTimeNS = System.nanoTime() - startTime;
		
		return window;
	}
	
	@Override
	public String getShortName() {
		return "Basic BNL Algorithm";
	}
	
}
