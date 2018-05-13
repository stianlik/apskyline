package no.ntnu.skyline;

import ifis.skysim2.algorithms.SkylineAlgorithmSskyline;
import ifis.skysim2.data.points.ArrayPointList;
import ifis.skysim2.data.points.ShiftingArrayPointList;
import ifis.skysim2.data.sources.PointSource;

import java.util.concurrent.Callable;

public class SSkylineTask implements Callable<ArrayPointList> {

	private final PointSource data;

	SSkylineTask(PointSource data) {
		this.data = data;
	}

	@Override
	public ArrayPointList call() {
		
		if (data.size() == 0) {
			return new ShiftingArrayPointList(data.getD());
		}
		
		SkylineAlgorithmSskyline alg = new SkylineAlgorithmSskyline();
		ArrayPointList result = alg.compute(data, 0, data.size());
		return result;
	}

}