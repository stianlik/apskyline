package ifis.skysim2.data.generator;

import java.io.File;
import java.io.IOException;

public interface DataGenerator {

    // Resets the data generator back to the default seed (used for running experiments on "standard" random data)
    public void resetToDefaultSeed();

    // generates n tuples of dimensionality d
    public float[] generate(int d, int n);

    // generates n tuples of dimensionality d, where the domain of dimension i consists of levels[i] values
    public float[] generate(int d, int n, int[] levels);

    // identical level sizes in all dimensions
    public float[] generate(int d, int n, int levels);

    // generates n tuples of dimensionality d and writes them to a file; each record is padded to reach a given record length
    public void generate(int d, int n, File file, int bytesPerRecord) throws IOException;

    // same as above, now with levels
    public void generate(int d, int n, int[] levels, File file, int bytesPerRecord) throws IOException;

    // identical level sizes in all dimensions
    public void generate(int d, int n, int levels, File file, int bytesPerRecord) throws IOException;

    /** Returns the identifying name of the implementing generator */
    public String getShortName();
    
}
