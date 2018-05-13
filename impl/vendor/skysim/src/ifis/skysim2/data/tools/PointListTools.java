package ifis.skysim2.data.tools;

import java.util.List;

public class PointListTools {
    public static float[] toFlatArray(List<float[]> list, int d) {
	int m = list.size();
	float[] result = new float[m * d];
	int i = 0;
	for (float[] point : list) {
	    System.arraycopy(point, 0, result, i, d);
	    i += d;
	}
	return result;
    }
}