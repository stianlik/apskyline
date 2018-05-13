package ifis.skysim2.common.tools.distributions;

import cern.jet.random.AbstractDistribution;
import cern.jet.random.Normal;
import ifis.skysim2.common.tools.RandomNumberUtils;


/**
 * A normal distribution which is cut at a given min and a given max.
 */
@SuppressWarnings("serial")
public class RestrictedNormalDistribution extends AbstractDistribution {

	/** The internal normal */
	private Normal normalDist;

	/** Min value to cut */
	private double min;

	/** Max value to cut */
	private double max;

	/**
	 * Constructor.
	 * 
	 * @param mean
	 *            normal mean
	 * @param variance
	 *            normal variance
	 * @param min
	 *            lower cut value
	 * @param max
	 *            higher cut value
	 */
	public RestrictedNormalDistribution(double mean, double variance,
			double min, double max) {
		normalDist = RandomNumberUtils.createNormalDistribution(mean, variance);
		this.min = min;
		this.max = max;
	}

	/**
	 * The next normal-distributed integer.
	 * 
	 * @return an int
	 */
	@Override
	public int nextInt() {
		int result = -1;
		do {
			result = normalDist.nextInt();
		} while (result < min || result > max);

		return result;
	}

	/**
	 * The next normal-distributed double.
	 * 
	 * @return a double
	 */
	@Override
	public double nextDouble() {
		return normalDist.nextDouble();
	}

}
