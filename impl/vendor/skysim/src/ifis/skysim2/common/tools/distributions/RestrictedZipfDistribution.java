package ifis.skysim2.common.tools.distributions;

import cern.jet.random.AbstractDiscreteDistribution;
import cern.jet.random.AbstractDistribution;
import cern.jet.random.Empirical;
import cern.jet.random.EmpiricalWalker;
import ifis.skysim2.common.tools.RandomNumberUtils;


/**
 * A zipf distribution.
 */
@SuppressWarnings("serial")
public class RestrictedZipfDistribution extends AbstractDistribution {

	/** min value */
	private int min;
	/** max value */
	private int max;
	/** skew value */
	private double skew;
	/** mirror the thing? */
	private boolean inverted;
	/** Internal distribution */
	private AbstractDiscreteDistribution zipfDist;

	/**
	 * Constructor.
	 * 
	 * @param skew
	 *            degree of skewness
	 * @param min
	 *            smalles value
	 * @param max
	 *            bigges value
	 * @param inverted
	 *            true for inverted (== Peak near max)
	 */
	public RestrictedZipfDistribution(double skew, int min, int max,
			boolean inverted) {
		this.skew = skew;
		this.min = min;
		this.max = max;
		this.inverted = inverted;
		init();
	}

	/**
	 * Prepares the usage.
	 */
	private void init() {
		int size = max - min + 1;
		double[] schemaProbs = new double[size];
		double[] schemaRelProbs = new double[size];
		double schemaSumProb = 0;
		for (int i = 0; i < schemaRelProbs.length; i++) {
			schemaRelProbs[i] = Math.pow(i + 1, -(skew + 1.0));
			schemaSumProb += schemaRelProbs[i];
		}
		for (int i = 0; i < schemaProbs.length; i++) {
			schemaProbs[i] = schemaRelProbs[i] / schemaSumProb;
		}
		zipfDist = new EmpiricalWalker(schemaProbs, Empirical.NO_INTERPOLATION,
				RandomNumberUtils.getRandomEngine());
	}

	/**
	 * The next zipf-distributed integer.
	 * 
	 * @return an int
	 */
	@Override
	public int nextInt() {
		int result = -1;
		do {
			if (inverted) {
				result = max - zipfDist.nextInt();
			} else {
				result = zipfDist.nextInt();
			}
		} while (result < min || result > max);
		// @TODO This is pretty ugly
		return result;
	}

	/**
	 * The next zipf-distributed double.
	 * 
	 * @return a double
	 */
	@Override
	public double nextDouble() {
		double result = -1;
		do {
			if (inverted) {
				result = max - zipfDist.nextInt();
			} else {
				result = zipfDist.nextInt();
			}
		} while (result < min || result > max);

		return result;
	}

}
