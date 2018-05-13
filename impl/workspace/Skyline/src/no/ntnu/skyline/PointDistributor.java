package no.ntnu.skyline;

import ifis.skysim2.data.sources.PointSource;

class PointDistributor implements Runnable {
	
	private PointSource[] partitions;
	private PointSource data;
	private int end;
	private int start;
	private AnglePartitionStrategy strategy;

	PointDistributor(AnglePartitionStrategy strategy, int start, int end, PointSource data, PointSource partitions[]) {
		this.strategy = strategy;
		this.start = start;
		this.end = end;
		this.data = data;
		this.partitions = partitions;
	}
	
	public void run() {
		float[] point;
		int index;
		for (int i = start; i < end; ++i) {
			point = data.get(i);
			index = strategy.mapPointToPartition(point);
			synchronized(partitions[index]) {
				partitions[index].add(point);
			}
		}
	}
}