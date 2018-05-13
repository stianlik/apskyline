package ifis.skysim2.data.points;

import ifis.skysim2.data.tools.PointRelationship;
import java.util.ListIterator;

public interface PointListIterator extends ListIterator<float[]> {
    public PointRelationship nextAndCompareNextTo(float[] point);
    public PointRelationship nextAndCompareNextToReferencePoint();

    /*
     * Non-copying next method,
     * which return the original references if possible
     */
    public float[] nextDirect();

    public void moveToFront();

    /*
     * Signals that the current point has a certain quality.
     * This signal invalidates the iterator.
     */
    public void promotePoint();
}