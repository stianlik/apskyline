package ifis.skysim2.data.points;

import ifis.skysim2.data.tools.PointRelationship;

public interface ArrayPointList extends PointList {
    // Copies the data item at position from (0 <= from < size) to position to
    public void copy(int from, int to);

    public PointRelationship compare(int indexA, int indexB);

    public PointRelationship compare(int indexA, float[] pointB);

    public float[] getSubarray(int from, int to);
}
