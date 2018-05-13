package ifis.skysim2.data.generator;

public class DataGeneratorIndependent extends AbstractDataGenerator {

    public DataGeneratorIndependent() {
        super();
    }

    @Override
    public float[] generate(int d, int n) {
        return generateUniform(d, n);
    }

    @Override
    public float[] generate(int d, int n, int[] levels) {
        if (ordinalLevelsAreValid(levels, d)) {
            return generateLeveledUniform(d, n, levels);
        } else {
            return generateUniform(d, n);
        }
    }

    @Override
    public String getShortName() {
        return "d_Indpndt";
    }
}
