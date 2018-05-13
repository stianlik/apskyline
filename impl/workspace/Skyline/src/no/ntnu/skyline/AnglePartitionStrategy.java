package no.ntnu.skyline;

public interface AnglePartitionStrategy extends PartitionStrategy {
	public int mapPointToPartition(float[] point);
}