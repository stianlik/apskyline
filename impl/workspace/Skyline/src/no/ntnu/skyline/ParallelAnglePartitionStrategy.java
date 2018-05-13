package no.ntnu.skyline;

import ifis.skysim2.algorithms.AbstractSkylineAlgorithm;
import ifis.skysim2.data.sources.PointSource;
import ifis.skysim2.data.sources.PointSourceRAM;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ParallelAnglePartitionStrategy extends EquiAnglePartitionStrategy {
	
	double sizeFactor = -1;

	public ParallelAnglePartitionStrategy(int dimensionCount, int partitionCount) {
		super(dimensionCount, partitionCount);
	}
	
	public ParallelAnglePartitionStrategy(int dimensionCount, int partitionCount, double sizeFactor) {
		super(dimensionCount, partitionCount);
	}
	
	@Override
	public PointSource[] exec(PointSource data, int partitionCount, AbstractSkylineAlgorithm algorithm) {
		int partSize = data.size() / partitionCount;
		int initialPartSize = 10;
		if (sizeFactor > 0) {
			initialPartSize = (int) (partSize * sizeFactor);
		}
		
		// Initiate partition containers
		PointSourceRAM partitions[] = new PointSourceRAM[this.partitionCount];
		for (int i = 0; i < partitions.length; ++i) {
			partitions[i] = new PointSourceRAM(this.dimensionCount, initialPartSize);
		}
		
		// Distribute points in parallel
		ExecutorService executor = Executors.newFixedThreadPool(partitions.length);
		int start = 0, end = partSize;
		for (int i = 0; i < partitions.length-1; ++i) {
			executor.execute(new PointDistributor(this, start, end, data, partitions));
			start = end;
			end += partSize;
		}
		executor.execute(new PointDistributor(this, start, data.size(), data, partitions));
		
		// Wait for all threads to complete
		executor.shutdown();
		try {
			executor.awaitTermination(1, TimeUnit.DAYS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		// Debugging info
//		System.out.println("## Partitions");
//		for (PointSource part : partitions) {
//			System.out.println("Partition size: " + part.size());
//		}
//		System.out.println();
		
		return partitions;
	}
	
}
