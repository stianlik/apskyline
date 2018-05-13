package ifis.skysim2.data.tools;

public class PointComputations {
    public static float getVolume(float[] p) {
	float vol = 1;
	int i = p.length;
	while (--i >= 0) {
		vol *= p[i];
	}
	return vol;
    }
}
