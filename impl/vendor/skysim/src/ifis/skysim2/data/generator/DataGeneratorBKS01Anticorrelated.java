package ifis.skysim2.data.generator;

import java.util.Arrays;

public class DataGeneratorBKS01Anticorrelated extends DataGeneratorBKS01 {

    @Override
    public float[] generate(int d, int n) {
	float[] data = new float[d * n];
	for (int i = 0; i < n; i++) {
	    do {
		float v = randomPseudoNormal(0.5f, 0.25f);
		Arrays.fill(data, d * i, d * (i + 1), v);
		float l;
		if (v <= 0.5) {
		    l = v;
		} else {
		    l = 1 - v;
		}
		for (int j = 0; j < d; j++) {
		    float h = randomEqual(-l, l);
		    int pos = d * i;
		    int current = j;
		    int next = (j + 1) % d;
		    data[pos + current] += h;
		    data[pos + next] -= h;
		}
	    } while (!isVectorOK(data, d * i, d * (i + 1)));
	}
	return data;
    }

    @Override
    public String getShortName() {
        return "d_AntiCorr";
    }
}