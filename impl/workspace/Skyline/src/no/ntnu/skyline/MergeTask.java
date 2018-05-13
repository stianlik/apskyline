package no.ntnu.skyline;

import ifis.skysim2.data.points.ArrayPointList;
import ifis.skysim2.data.tools.PointRelationship;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;

public class MergeTask implements Callable<Object> {

	public static final int TRUE = 1;
	public static final int FALSE = 0;

	private final ArrayPointList s1;
	private final ArrayPointList s2;
	private final AtomicIntegerArray t1;
	private final AtomicIntegerArray t2;
	private final AtomicInteger ai2;
	private final int n1;
	private final int n2;

	MergeTask(ArrayPointList s1, ArrayPointList s2,
			AtomicIntegerArray t1, AtomicIntegerArray t2, AtomicInteger ai2) {
		this.s1 = s1;
		this.s2 = s2;
		this.t1 = t1;
		this.t2 = t2;
		this.ai2 = ai2;
		n1 = t1.length();
		n2 = t2.length();
	}

	@Override
	public Object call() {
		int i2;
		s2loop: while ((i2 = ai2.getAndIncrement()) < n2) {
			final float[] p2 = s2.get(i2);
			// compare p2 == s2[i2] to all currently non-dominated points in
			// s1
			// mark dominated points in s1
			// mark non-dominated points in s2
			for (int i1 = 0; i1 < n1; i1++) {
				if (t1.get(i1) == TRUE) {
					// s1[i1] is still undominated
					// compare s1[i1] to s2[i2]
					PointRelationship dom = s1.compare(i1, p2);
					switch (dom) {
					case DOMINATES:
						// skip to next point in s2
						continue s2loop;
					case IS_DOMINATED_BY:
						// s1[i1] is dominated by s2[i2]
						t1.set(i1, FALSE);
						break;
					default:
						break;
					}
				}
			}
			// s2[i2] is not dominated by s1
			t2.set(i2, TRUE);
		}
		return null;
	}
}