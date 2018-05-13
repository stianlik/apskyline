package no.ntnu.skyline;

import ifis.skysim2.algorithms.AbstractSkylineAlgorithm;
import ifis.skysim2.data.sources.PointSource;
import ifis.skysim2.data.sources.PointSourceRAM;

import java.util.Arrays;

/**
 * TODO Ser ut til å være bug ved flere enn 5 dimensjoner, 
 * blir veldig ujevn fordeling.
 * 
 * @author Stian Liknes
 *
 */
public class EquiAnglePartitionStrategy implements AnglePartitionStrategy {
	
	// Configuration
	final public static double THRESHOLD = 0.0005;
	final public double radiusHypersphere = 10000000;
	
	// Input data
	final public int dimensionCount;
	final public int partitionCount;

	// Partitioning state
	protected double[][] boundsLow;
	protected double[][] boundsHigh;
	final public double volumeTotal; // calculated based on number of dimensions
	
	public EquiAnglePartitionStrategy(int dimensionCount, int partitionCount) {
		this.dimensionCount = dimensionCount;
		this.partitionCount = calculatePartitionsPerDimension(dimensionCount, partitionCount);
		this.volumeTotal = calculateTotalVolume(dimensionCount, radiusHypersphere);
		this.calculateBounds();
	}
	
	@Override
	public PointSource[] exec(PointSource data, int partitionCount, AbstractSkylineAlgorithm algorithm) {
		
		// Initiate partition containers
		PointSourceRAM partitions[] = new PointSourceRAM[this.partitionCount];
		for (int i = 0; i < partitions.length; ++i) {
			partitions[i] = new PointSourceRAM(this.dimensionCount);
		}
		
		// Distribute points
		float[] point;
		for (int i = 0; i < data.size(); ++i) {
			point = data.get(i);
			partitions[mapPointToPartition(point)].add(point);
		}

		return partitions;
	}
	
	public int mapPointToPartition(float[] point) {
		float[] angular = toAngular(point);
		int partitionId = 0, offset = 1, angleCount = dimensionCount -1;
		for (int i = 0; i < angleCount; ++i) {
			for (int j = 0; j < boundsHigh[i].length; ++j) {
				if (boundsLow[i][j] < angular[i] && angular[i] <= boundsHigh[i][j]) {
					partitionId += j * offset;
					offset = boundsHigh[i].length * offset;
					break;
				}
			}		
		}					
		return partitionId;
	}
	
	/**
	 * Computes the angles/bounds for each dimension to split,
	 * in order to have equi-volume partitioning 
	 */
	public void calculateBounds() {
		int angleCount = dimensionCount-1;
		
		double boundHigh, boundLow, volume, volumePerPartition;
		for (int i = 0; i < angleCount; ++i) {
			
			// Set max limits for all partitions
			double l[] = new double[angleCount];
			double h[] = new double[angleCount];
			for (int f = 0; f < angleCount; f++) {
				h[f] = 0.5 * Math.PI;
			}
			
			boundHigh = 0; 
			boundLow = 0; 
			volume = 0;
			volumePerPartition = volumeTotal/(double)boundsHigh[i].length;
			for (int j = 0; j < boundsHigh[i].length - 1; j++) {
				boundLow = l[i] = boundHigh;
				boundHigh = h[i] = 0.5 * Math.PI;
				volume = calculateVolume(l, h);
				while (Math.abs(volumePerPartition - volume) >= volumeTotal*THRESHOLD) {
					if (volume < volumePerPartition) {
						boundHigh = h[i] = h[i] + (h[i]-boundLow)/2;
						volume = calculateVolume(l, h);
					} 
					else {
						boundHigh = h[i] = h[i] - (h[i]-boundLow)/2;
						volume = calculateVolume(l, h);
					}
				}
				boundsLow[i][j] = boundLow;
				boundsHigh[i][j] = boundHigh; 
			}
			
			// Last partition should include the rest
			if (boundsHigh[i].length > 1) {
				boundsLow[i][boundsHigh[i].length-1] = boundsHigh[i][boundsHigh[i].length-2];
			}
			boundsHigh[i][boundsHigh[i].length-1] = 0.5 * Math.PI;
		}
		
//		System.out.println(volumeTotal);
//		for (int i = 0; i < boundsLow.length; ++i) {
//			System.out.println("low " + Arrays.toString(boundsLow[i]));
//			System.out.println("high" + Arrays.toString(boundsHigh[i]));
//		}
	}
	
	/**
	 * Fills this.K with partition count for each angle and returns
	 * number of partitions achieved.
	 * @return Total number of partitions achieved (i.e. recommended thread count)
	 */
	private int calculatePartitionsPerDimension(int dimensionCount, int partitionCount) {
		int angleCount = dimensionCount - 1;
		int[] partitionCountPerAngle = new int[angleCount];
		
		// Give to each dimension an initial number of splits
		int k = (int)Math.floor(Math.pow(partitionCount, (1/(double)(angleCount))));
		Arrays.fill(partitionCountPerAngle, k);
		
		// Calculate minimal partition count
		int achievedPartitionCount = (int)Math.pow(k,angleCount);
		
		// Distribute the rest of the bins to the dimensions asymmetrical
		boolean increasedPartitionCount = true;
		while (increasedPartitionCount) {
			increasedPartitionCount = false;
			for (int i = 0; i < angleCount; ++i) {
				// Increase the splits for the i^th dimension if possible
				if ( (partitionCountPerAngle[i]+1) * achievedPartitionCount / partitionCountPerAngle[i] <= partitionCount ) {
					achievedPartitionCount = achievedPartitionCount / partitionCountPerAngle[i];
					partitionCountPerAngle[i] = partitionCountPerAngle[i] + 1;
					achievedPartitionCount = achievedPartitionCount * partitionCountPerAngle[i];
					increasedPartitionCount = true;
				}
			}
		}

		// Instantiate bound collections
		boundsLow = new double[angleCount][];
		boundsHigh = new double[angleCount][];
		for (int i=0 ; i < angleCount ; i++) {
			boundsLow[i] = new double[partitionCountPerAngle[i]];
			boundsHigh[i] = new double[partitionCountPerAngle[i]];
		}
		
		return achievedPartitionCount;
	}
	
	/**
	 * Calculate the volume of the hypersphere with radius: r for dimensionality up to 8
	 * (the part of the hypersphere that lies in the positive axes)
	 * @param d Dimension count
	 * @param r Radius of hypersphere
	 * @return Volume of positive part of hypersphere
	 */
	public static double calculateTotalVolume(int d, double r) {
		if (d < 2 || d > 8) {
			throw new RuntimeException("Volume calculation not implemented for " + 
				(d < 2 ? "less than 2" : "more than 8") + " dimensions");
		}
		int divisors[] = {4, 6, 32, 60, 384, 840, 6144};
		return Math.pow(Math.PI, Math.floor(d/2))*Math.pow(r, d)/divisors[d-2];
	}
	
	/**
	 * Computes the volume of the hypersphere at dimension d with radius r,
	 * for the angles in the arrays l and h.
	 * 
	 * @param l Angle low in radians: f(1)low, f2(low), ..., f(d-1)low
	 * @param h Angle high in radians: f(1)high,f(2)high,...,f(d-1)high
	 * @return
	 */
	protected double calculateVolume(double l[], double h[]) {
		switch (dimensionCount) {
		case 2:
			return Math.pow(radiusHypersphere, 2)/2 * (h[0] - l[0]);
		case 3:
			return Math.pow(radiusHypersphere, 3)/3 * (Math.cos(l[0]) - Math.cos(h[0])) * (h[1] - l[1]);
		case 4:
			return Math.pow(radiusHypersphere, 4)/4 * (h[0]/2-l[0]/2+Math.sin(2*l[0])/4-Math.sin(2*h[0])/4)
			* (Math.cos(l[1]) - Math.cos(h[1])) * (h[2] - l[2]);
		case 5:
			return Math.pow(radiusHypersphere, 5)/5 * 1/12 * (Math.cos(3*h[0])-9*Math.cos(h[0]) - Math.cos(3*l[0])+9*Math.cos(l[0]))
			* (h[1]/2-l[1]/2+Math.sin(2*l[1])/4-Math.sin(2*h[1])/4)
			* (Math.cos(l[2]) - Math.cos(h[2])) * (h[3] - l[3]);			
		case 6:
			return Math.pow(radiusHypersphere, 6)/6 
			* (12*h[0]-8*Math.sin(2*h[0])+Math.sin(4*h[0]) - 12*l[0]+8*Math.sin(2*l[0])-Math.sin(4*l[0]))
			* 1/12 * (Math.cos(3*h[1])-9*Math.cos(h[1]) - Math.cos(3*l[1])+9*Math.cos(l[1]))
			* (h[2]/2-l[2]/2+Math.sin(2*l[2])/4-Math.sin(2*h[2])/4)
			* (Math.cos(l[3]) - Math.cos(h[3])) * (h[4] - l[4]);						
		}
		throw new RuntimeException("Volume calculation only implemented for 2 - 6 dimensions");
	}
	
	/**
	 * Computes angular coordinates for point. The radius
	 * is discarded.
	 * @param point
	 * @return angular[point.length-1] = angle1, angle2, ...
	 */
	public static float[] toAngular(float[] point) {
		float[] result = new float[point.length-1];
		for (int i = 1; i < point.length; ++i) {
			for (int j = i; j < point.length; ++j) {
				result[i-1] += point[j] * point[j];
			}
			result[i-1] = (float) Math.atan(Math.sqrt(result[i-1]) / point[i-1]);
		}
		return result;
	}

}