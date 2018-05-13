package no.ntnu.skyline;

import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import ifis.skysim2.algorithms.AbstractSkylineAlgorithm;
import ifis.skysim2.data.sources.PointSource;
import ifis.skysim2.data.sources.PointSourceRAM;

public class DynamicPlusPartitioningStrategy implements AnglePartitionStrategy {
	
	private int achievedPartitionCount = 1;
	private double boundsLow[][];
	private double boundsHigh[][];
	public int maxPartitionSize;
	private int nextAngularDimension = 0;
	private PointSourceRAM partitions[];
	double samplePercentage = 0.01;
	
	@Override
	public PointSource[] exec(PointSource data, int partitionCount, AbstractSkylineAlgorithm algorithm) {
		
		// Initiate partition bounds
		achievedPartitionCount = 1;
		boundsLow = new double[partitionCount][data.getD()-1];
		boundsHigh = new double[partitionCount][data.getD()-1];
		// Add a small number to bound high to account for the corner
		// case where an angle is equal to Math.PI / 2
		Arrays.fill(boundsHigh[0], Math.PI / 2 + 0.001);
		
		// Initiate partition containers
		partitions = new PointSourceRAM[partitionCount];
		for (int i = 0; i < partitions.length; ++i) {
			partitions[i] = new PointSourceRAM(data.getD());
		}
		
		algorithm.startTimer("sampling");
		
		// Dynamically create partitions based on a few sample points
		int sampleCount = (int) (samplePercentage * (double) data.size());
		int incr = data.size() / sampleCount;
		float[] point;
		maxPartitionSize = sampleCount / partitionCount;
		int partitionId = 0, a = data.getD() - 1;
		for (int i = 0; i < data.size() && achievedPartitionCount < partitionCount; i += incr) {
			point = data.get(i);
			partitionId = mapPointToPartition(point);
			partitions[partitionId].add(point);
			if (partitions[partitionId].size() > maxPartitionSize) {
				splitPartition(partitionId, a);
			}
		}
		
//		for (int i = 0; i < boundsLow.length; ++i) {
//			System.out.println("low " + Arrays.toString(boundsLow[i]));
//			System.out.println("high" + Arrays.toString(boundsHigh[i]));
//		}
		
		// Ensure that there are enough partitions
		while (achievedPartitionCount < partitionCount) {
			splitPartition(findBiggestPartition(), a);
		}
		
		// Reset partitions to prepare for parallel execution
		for (PointSourceRAM partition : partitions) {
			partition.clear();
		}
		maxPartitionSize = data.size() / partitionCount;
	
		algorithm.stopTimer("sampling");
		
		// Distribute points in parallel
		ExecutorService executor = Executors.newFixedThreadPool(partitions.length);
		int partSize = data.size() / partitionCount;
		int start = 0, end = partSize;
		for (int i = 0; i < partitions.length-1; ++i) {
			executor.execute(new DynamicPointDistributor(this, start, end, data, partitions));
			start = end;
			end += partSize;
		}
		executor.execute(new DynamicPointDistributor(this, start, data.size(), data, partitions));

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

	public void splitPartition(int partitionId, int angularDimensions) {
		
		// New partition is initially equal to the one we're splitting 
		int newPartId = achievedPartitionCount;
		for (int j = 0; j < angularDimensions; ++j) {
			boundsLow[achievedPartitionCount][j] = boundsLow[partitionId][j];
			boundsHigh[achievedPartitionCount][j] = boundsHigh[partitionId][j];
		}
		
		// Calculate new bounds for both partitions
		boundsHigh[achievedPartitionCount][nextAngularDimension] = boundsHigh[partitionId][nextAngularDimension];
		boundsHigh[partitionId][nextAngularDimension] = calculateMean(partitionId, nextAngularDimension);
		boundsLow[achievedPartitionCount][nextAngularDimension] = boundsHigh[partitionId][nextAngularDimension];
		
		// Update partitioning statistics (necessary for the mapping to be correct)
		nextAngularDimension = (nextAngularDimension + 1) % angularDimensions; 
		++achievedPartitionCount;
		
		// Move points
		PointSourceRAM tmp = new PointSourceRAM(partitions[partitionId]);
		partitions[partitionId].clear();
		for (float[] p: tmp) {
			partitions[mapPointToPartition(p, partitionId, newPartId)].add(p);
		}
	}

	private double calculateMean(int partitionId, int angularDimension) {
		double boundLow = 0;
		for (float[] p: partitions[partitionId]) {
			boundLow += EquiAnglePartitionStrategy.toAngular(p)[angularDimension];
		}
		boundLow /= (double) partitions[partitionId].size();
		if (Double.isNaN(boundLow)) {
			return 0.0;
		}
		return boundLow;
	}
	
	private int findBiggestPartition() {
		int partitionId = 0;
		int max = partitions[partitionId].size();
		for (int i = 1; i < achievedPartitionCount; i++) {
			if (partitions[i].size() > max) {
				partitionId = i;
				max = partitions[i].size();
			}
		}
		return partitionId;
	}
	
	public int mapPointToPartition(float[] point) {
		float[] angular = EquiAnglePartitionStrategy.toAngular(point);
		next_partition: for (int i = 0; i < achievedPartitionCount; ++i) {
			for (int j = 0; j < angular.length; ++j) {
				if (angular[j] < boundsLow[i][j] || angular[j] >= boundsHigh[i][j]) {
					continue next_partition;
				}
			}
			return i;
		}
		throw new RuntimeException("Unable to map point to partition");
	}
	
	/**
	 * Can be used if point is guaranteed to be placed in part1 or part2. This
	 * will perform less checks than the general method.
	 * @param point
	 * @param part1
	 * @param part2
	 * @return part1 or part2
	 */
	private int mapPointToPartition(float[] point, int part1, int part2) {
		float[] angular = EquiAnglePartitionStrategy.toAngular(point);
		boolean match = true;
		for (int j = 0; j < angular.length; ++j) {
			if (angular[j] < boundsLow[part1][j] || angular[j] >= boundsHigh[part1][j]) {
				match = false;
				break;
			}
		}
		return match ? part1 : part2;
	}
}