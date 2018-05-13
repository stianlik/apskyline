package no.ntnu.skyline;

import ifis.skysim2.data.sources.PointSource;

class DynamicPointDistributor implements Runnable {
	
	private PointSource[] partitions;
	private PointSource data;
	private int end;
	private int start;
	private DynamicPlusPartitioningStrategy strategy;

	DynamicPointDistributor(DynamicPlusPartitioningStrategy strategy, int start, int end, PointSource data, PointSource partitions[]) {
		this.strategy = strategy;
		this.start = start;
		this.end = end;
		this.data = data;
		this.partitions = partitions;
	}
	
	public void run() {
		float[] point;
		int index;
//		int fillerMax = strategy.maxPartitionSize;
		int max = (int) ((float)strategy.maxPartitionSize * 1.1);
//		int min = 0, cur = 0;
		int len = this.partitions.length;
		for (int i = start; i < end; ++i) {
			point = data.get(i);
			index = strategy.mapPointToPartition(point);
			if (partitions[index].size() > max) {
				index = (int) (Math.random() * len);
//				min = fillerMax;
//				for (int j = 0; j < partitions.length; ++j) {
//					cur = partitions[j].size();
//					if (cur < min) {
//						index = j;
//						min = cur;
//					}
//				}
			}
			synchronized(partitions[index]) {
				partitions[index].add(point);
			}
		}
	}
}