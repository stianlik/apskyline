package ifis.skysim2.data.points;

import java.util.List;

public interface PointList extends List<float[]> {
    public int getD();
    
    /*
     * Non-copying get and set methods,
     * which return the original references if possible
     */
    public float[] getDirect(int i);
    public boolean addDirect(float[] data);
    public void addDirect(int index, float[] data);
    
    @Override
    public PointListIterator listIterator();
    
    public PointListIterator listIterator(float[] referencePoint);

    /*
     * Bubble: For each window tuple, count the number of dominated input tuples
     * BubbleUp: Swap during list traversal
     * BubbleUpSimple: Swap when domination takes place
     */
    public static enum ListOrder {
	Unsorted,
	SortedByVolume,
	BubbleUp,
	BubbleUpSimple,
	MoveToFront
    }
}