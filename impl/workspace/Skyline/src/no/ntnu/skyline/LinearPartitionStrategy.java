package no.ntnu.skyline;

import ifis.skysim2.algorithms.AbstractSkylineAlgorithm;
import ifis.skysim2.data.sources.PointSource;
import ifis.skysim2.data.sources.PointSourceRAM;

public class LinearPartitionStrategy implements PartitionStrategy {

	@Override
	public PointSourceRAM[] exec(PointSource data, int partitionCount, AbstractSkylineAlgorithm algorithm) {
		int blocksize = data.size() / partitionCount;
		PointSourceRAM partitions[] = new PointSourceRAM[partitionCount];
		
		// Calculate boundaries
		int[] froms = new int[partitionCount];
		int[] tos = new int[partitionCount];
		for (int i = 0; i < partitionCount; ++i) {
			froms[i] = i * blocksize;
			tos[i] = froms[i] + blocksize;
		}
		tos[partitionCount-1] = data.size();
		
		// Distribute points
		for (int i = 0; i < partitionCount; ++i) {
			partitions[i] = new PointSourceRAM(data, froms[i], tos[i]);
		}
		
		return partitions;
	}

}
