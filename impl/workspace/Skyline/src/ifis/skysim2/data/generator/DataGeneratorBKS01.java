package ifis.skysim2.data.generator;

// Generates data sets according to the method described in
// Börzsönyi/Kossmann/Stocker: The Skyline operator (technical report!)
// Also see http://randdataset.projects.postgresql.org for a more detailed description
//
// This implementation is equivalent to the one used by Börzsönyi/Kossmann/Stocker
// (we had a look into their source code)
public abstract class DataGeneratorBKS01 extends AbstractDataGenerator {
    public DataGeneratorBKS01() {
	super();
    }

    protected float randomPeak(float min, float max, int dim) {
	float sum = 0;
	for (int i = 0; i < dim; i++) {
	    sum += re.nextFloat();
	}
	sum /= dim;
	return sum * (max - min) + min;
    }

    protected float randomPseudoNormal(float med, float var) {
	return randomPeak(med - var, med + var, 12);
    }

    protected float randomEqual(float min, float max) {
	return re.nextFloat() * (max - min) + min;
    }

    protected static boolean isVectorOK(float[] data, int fromIndex, int toIndex) {
	for (int i = fromIndex; i < toIndex; i++) {
	    if ((data[i] < 0) || (data[i] > 1)) {
		return false;
	    }
	}
	return true;
    }
}
