package ifis.skysim2.data.generator;

// Generates n points uniformly from [0, 1]^d and projects them
import java.util.Arrays;

// onto a hollow unit ball with radius r.
// For r = 1, all points are located on the unit hypersphere.
public class DataGeneratorHollowUnitBall extends AbstractDataGenerator {

    private double r;

    public DataGeneratorHollowUnitBall(double r) {
        super();
        this.r = r;
    }

    @Override
    public float[] generate(int d, int n) {
        // generate uniform
        float[] data = generateUniform(d, n);
        // map to ball
        for (int i = n - 1; i >= 0; i--) {
            double length = 0;
            for (int j = d - 1; j >= 0; j--) {
                int pos = d * i + j;
                double dataPosDouble = (double) data[pos];
                length += dataPosDouble * dataPosDouble;
            }
            // normalize to length 1 and scale randomly
            length = Math.sqrt(length);
            double scale = r + re.nextDouble() * (1 - r);
            for (int j = d - 1; j >= 0; j--) {
                int pos = d * i + j;
                data[pos] /= length;
                data[pos] *= scale;
            }
        }
        return data;
    }

    @Override
    public String getShortName() {
        return "d_Hollow";
    }
}
