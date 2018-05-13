package ifis.skysim2.data.tools;

public final class PointComparator {

	/**
	 * Compares two data points a and b, a and be must be of same length.
	 * @param p
	 * @param q
	 * @return
	 *  <dl>
	 * 		<dt>DOMINATES</dt><dd> if a > b</dd>
	 * 		<dt>IS_DOMINATED_BY</dt><dd> if a < b</dd>
	 * 		<dt>IS_INCOMPARABLE_TO</dt><dd> if a < b</dd>
	 * 		<dt>EQUALS</dt><dd>if a == b</dd>
	 *  </dl>
	 */
	public static PointRelationship compare(float[] p, float[] q) {
		return compare(p, 0, q, 0, p.length);
	}

	// compares two points located in a storage array
	public final static PointRelationship compare(float[] data, int indexA,
			int indexB, int d) {
		// this kind of indirection does not impose any performance penalty
		return compare(data, indexA, data, indexB, d);
	}

	public final static PointRelationship compare(float[] dataA, int indexA,
			float[] dataB, int indexB, int d) {

		PointRelationship r = PointRelationship.EQUALS;
		
		for (int i = 0; i < d; ++i) {
			if (dataA[indexA+i] < dataB[indexB+i]) {
				if (r == PointRelationship.EQUALS) {
					r = PointRelationship.DOMINATES;
				}
				else if (r == PointRelationship.IS_DOMINATED_BY) {
					return PointRelationship.IS_INCOMPARABLE_TO;
				}
			}
			else if (dataA[indexA+i] > dataB[indexB+i]) {
				if (r == PointRelationship.EQUALS) {
					r = PointRelationship.IS_DOMINATED_BY;
				}
				else if (r == PointRelationship.DOMINATES) {
					return PointRelationship.IS_INCOMPARABLE_TO;
				}
			}
		}
		
		return r;
	}

	public final static PointRelationship compare(float[] data, int index,
			float[] point, int d) {
		return compare(data, index, point, 0, d);
	}

	/*
	 * A binary vector indicating the relationship between a's and b's
	 * coordinates. result's (i + 1)-th least significant bit is 1 iff a[i] >
	 * b[i]
	 */
	public static long getSuccessorship(float[] a, float[] b) {
		long successorship = 0;
		int d = a.length;
		for (int i = d - 1; i >= 0; i--) {
			successorship = successorship << 1;
			if (a[i] > b[i]) {
				successorship++;
			}
		}
		return successorship;
	}
}
