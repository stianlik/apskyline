package ifis.skysim2.data.sources;

import java.util.List;

// currently, a point source simply is a list of float[],
// which may or may not fit our needs...

public interface PointSource extends List<float[]> {
    public int getD();
    public float[] toFlatArray();
}
 