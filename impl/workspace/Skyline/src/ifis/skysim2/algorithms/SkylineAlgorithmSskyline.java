/*
 * sskyline algorithm presented in
 * Park, Kim, Park, Kim, Im: Parallel Skyline Computation on Multicore Architectures
 * (Figure 4)
 *
 * This algorithm is very similar to Ciaccia's Best algorithm
 * (which always holds the current maximum and returns it progressively after all comparisons).
 * But Sskyline uses are more clever memory management. Dominated elements already are replaced
 * by the current input list's tail. Therefore, the current maximum always lies at the first position,
 * the current comparison elements wanders along the list until the end is reached; then then first position
 * is returned.
 *
 * Sskyline is an in-place algorithm and relies on an array representation of the input data.
 */

package ifis.skysim2.algorithms;

import ifis.skysim2.data.points.ArrayPointList;
import ifis.skysim2.data.points.ShiftingArrayPointList;
import ifis.skysim2.data.sources.PointSource;
import ifis.skysim2.data.sources.PointSourceRAM;
import ifis.skysim2.data.tools.PointComparator;
import ifis.skysim2.data.tools.PointRelationship;

import java.util.Arrays;
import java.util.List;

public class SkylineAlgorithmSskyline extends AbstractSkylineAlgorithm {

	@Override
	public List<float[]> compute(PointSource data) {
		return compute(data, 0, data.size());
	}
	
	/**
	 * NB! Input needs to be an instance of PointSourceRAM and will be modified 
	 * (corrupted) by this algorithm.
	 * 
	 * @param dataArray
	 * @param from
	 * @param to
	 * @return
	 */
	public ArrayPointList compute(PointSource dataArray, int from, int to) {
		PointSourceRAM dataArrayX = (PointSourceRAM) dataArray;
		long startTime = System.nanoTime();

		int d = dataArrayX.getD();
		int head = from;
		int tail = to-1;

		while (head < tail) {
			int i = head + 1;
			while (i <= tail) {
				PointRelationship dom = PointComparator.compare(dataArrayX.data, d * head, d * i, d);
				switch (dom) {
				case DOMINATES:
					System.arraycopy(dataArrayX.data, tail * d, dataArrayX.data, i * d, d);
					tail--;
					break;
				case IS_DOMINATED_BY:
					System.arraycopy(dataArrayX.data, i * d, dataArrayX.data, head * d, d);
					System.arraycopy(dataArrayX.data, tail * d, dataArrayX.data, i * d, d);
					tail--;
					i = head + 1;
					break;
				default:
					i++;
					break;
				}
			}
			if (head < tail) {
				head++;
			}
		}

		totalTimeNS = System.nanoTime() - startTime;
		
		return new ShiftingArrayPointList(Arrays.copyOfRange(dataArrayX.data, d*from, d * (head+1)), d, false);
	}

	@Override
	public String toString() {
		return "sskyline";
	}

	@Override
	public String getShortName() {
		return "sskyline";
	}

}