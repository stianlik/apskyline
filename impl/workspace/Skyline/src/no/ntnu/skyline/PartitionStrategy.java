package no.ntnu.skyline;
import ifis.skysim2.algorithms.AbstractSkylineAlgorithm;
import ifis.skysim2.data.sources.PointSource;

public interface PartitionStrategy {
	;
	public PointSource[] exec(PointSource p, int partitionCount, AbstractSkylineAlgorithm algorithm);
}