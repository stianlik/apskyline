/*
 * sorts a List<float[]> prior to skyline computation
 */
package ifis.skysim2.data.tools;

import ifis.skysim2.data.sources.PointSource;
import ifis.skysim2.data.sources.PointSourceRAM;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class DataPresorter {

	public static PointSource sortByVolume(List<float[]> data) {
		System.out.println("Sorting data by volume ...");
		PointSource dataNew = new PointSourceRAM(data);

		Collections.sort(dataNew, new VolumeComparator());
//		ConcurrentSort.sort(dataNew, new VolumeComparator());

		return dataNew;
	}

	private static class VolumeComparator implements Comparator<float[]> {

		@Override
		public int compare(float[] point1, float[] point2) {
			float vol1 = PointComputations.getVolume(point1);
			float vol2 = PointComputations.getVolume(point2);
			if (vol1 > vol2) {
				return -1;
			} else if (vol1 < vol2) {
				return 1;
			} else {
				return 0;
			}
		}
	}

	public static PointSource sortBestToFront(List<float[]> data, double prob) {
		System.out.format("Sorting data partially, moving most probable skyline points to front (threshold: %.3f) ... ", prob);
		int numPoints = 0;
		// sort all points to front
		// having a uniform skyline probablity of at least prob
		if (!(data instanceof PointSourceRAM)) {
			throw new UnsupportedOperationException("Not supported yet.");
		}
		PointSourceRAM psr = (PointSourceRAM)data;
		final int d = psr.getD();
		final int n = psr.size();
		float[] dataArray = Arrays.copyOf(psr.toFlatArray(), d * n);
		float[] temp = new float[d];
		int pointerHead = 0;
		// pointerHead: where next "good" point will be inserted at head of data array
		// pointerData: current position in data array
		final double probNew = Math.pow(prob, 1 / (double)(n - 1));
		for (int pointerData = 0; pointerData < n * d; pointerData += d) {
			double dominatingVol = 1;
			for (int i = 0; i < d; i++) {
				dominatingVol *= 1 - dataArray[pointerData + i];
			}
			double probSkyUniformNew = 1 - dominatingVol;
			if (probSkyUniformNew >= probNew) {
				numPoints++;
				// swap it
				System.arraycopy(dataArray, pointerHead, temp, 0, d);
				System.arraycopy(dataArray, pointerData, dataArray, pointerHead, d);
				System.arraycopy(temp, 0, dataArray, pointerData, d);
				pointerHead += d;
			}
		}
		System.out.format("%d points moved%n", numPoints);
		return new PointSourceRAM(d, dataArray);
	}
}
