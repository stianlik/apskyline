package ifis.skysim2.data.generator;

import cern.jet.random.engine.MersenneTwister;
import cern.jet.random.engine.RandomEngine;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.Arrays;

public abstract class AbstractDataGenerator implements DataGenerator {

    protected RandomEngine re;
    protected static final int BYTES_PER_FLOAT = Float.SIZE / 8;

    protected AbstractDataGenerator() {
	this(new MersenneTwister(new java.util.Date()));
    }

    private AbstractDataGenerator(RandomEngine re) {
	this.re = re;
    }

    @Override
    public void resetToDefaultSeed() {
	re = new MersenneTwister();
    }

    protected float[] generateLeveledUniform(int d, int n, int[] numlevels) {
	float[] out = new float[d * n];
	FloatBuffer buf = FloatBuffer.wrap(out);
	int i = 0;
	while (buf.hasRemaining()) {
	    int value = re.nextInt() % numlevels[i];
	    if (value < 0) {
		value += numlevels[i];
	    }
	    float f = 1f * value / (numlevels[i] - 1);
	    buf.put(f);
	    i = (i + 1) % d;
	}
	return out;
    }

    protected float[] generateUniform(int d, int n) {
	float[] out = new float[d * n];
	FloatBuffer buf = FloatBuffer.wrap(out);
	while (buf.hasRemaining()) {
	    buf.put(re.nextFloat());
	}
	return out;
    }

    protected static boolean ordinalLevelsAreValid(int[] levels, int d) {
	if (levels == null) {
	    return false;
	}
	boolean error = false;
	if (levels.length != d) {
	    error = true;
	}
	for (int i = 0; i < d; i++) {
	    if (levels[i] <= 0) {
		error = true;
	    }
	}
	if (error) {
	    System.out.println("Wrong configuration of ordinal levels; ignoring ...");
	}
	return !error;
    }

    // nio bringt keine Performancesteigerung
    // leider benötigt man bereits zum Schreiben einer leeren Datei (auch mit nio)
    // nahezu die gesamte Zeit im Gesamtvorgang
    protected static void writeToFile(float[] data, int d, int n, File file, int bytesPerRecord) throws IOException {
	DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
	int fillerSize = bytesPerRecord - d * BYTES_PER_FLOAT;
	final byte[] filler = new byte[fillerSize];
	int pos = 0;
	for (int i = n; i > 0; i--) {
	    for (int j = d; j > 0; j--) {
		out.writeFloat(data[pos]);
		pos++;
	    }
	    out.write(filler);
	}
	out.close();
    }

    @Override
    public float[] generate(int d, int n, int[] levels) {
	if (levels == null) {
	    return generate(d, n);
	} else {
	    throw new UnsupportedOperationException("Not supported yet.");
	}
    }

    @Override
    public float[] generate(int d, int n, int levels) {
	int[] levelsArray = new int[d];
	Arrays.fill(levelsArray, levels);
	return generate(d, n, levelsArray);
    }

    @Override
    public abstract float[] generate(int d, int n);

    @Override
    public void generate(int d, int n, File file, int bytesPerRecord) throws IOException {
	float[] data = generate(d, n);
	writeToFile(data, d, n, file, bytesPerRecord);
    }

    @Override
    public void generate(int d, int n, int[] levels, File file, int bytesPerRecord) throws IOException {
	float[] data = generate(d, n, levels);
	writeToFile(data, d, n, file, bytesPerRecord);
    }

    @Override
    public void generate(int d, int n, int levels, File file, int bytesPerRecord) throws IOException {
	int[] levelsArray = new int[d];
	Arrays.fill(levelsArray, levels);
	generate(d, n, levelsArray, file, bytesPerRecord);
    }
}
