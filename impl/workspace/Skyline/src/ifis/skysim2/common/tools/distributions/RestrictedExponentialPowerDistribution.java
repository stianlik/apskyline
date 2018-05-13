package ifis.skysim2.common.tools.distributions;

import cern.jet.random.AbstractDistribution;
import cern.jet.random.ExponentialPower;
import ifis.skysim2.common.tools.RandomNumberUtils;


/**
 * A normal distribution which is cut at a given min and a given max.
 */
@SuppressWarnings("serial")
public class RestrictedExponentialPowerDistribution extends
		AbstractDistribution {

	/** The internal normal */
	private ExponentialPower dist;

	/** Min value to cut */
	private double min;

	/** Max value to cut */
	private double max;

	/**
	 * Constructor.
	 * 
	 * @param tau
	 *            tau>=1
	 * @param min
	 *            lower cut value
	 * @param max
	 *            higher cut value
	 */
	public RestrictedExponentialPowerDistribution(double tau, double min,
			double max) {
		dist = RandomNumberUtils.createExponentialPowerDistribution(tau);
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
			result = dist.nextInt();
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
		return dist.nextDouble();
	}

}
