package ifis.skysim2.common.tools;

public class ArraySearch {
    // If the key is contained in a:
    //   Returns the index of some key, if it is contained in a
    // Otherwise:
    //   Returns the index of the first element greater than the key or a.length
    //   or a.length if all elements in the array are less than the specified key
    public static int binarySearch(long[] a, int fromIndex, int toIndex, long key) {
	int low = fromIndex;
	int high = toIndex - 1;

	while (low <= high) {
	    int mid = (low + high) >>> 1;
	    long midVal = a[mid];

	    if (midVal < key) {
		low = mid + 1;
	    } else if (midVal > key) {
		high = mid - 1;
	    } else {
		return mid;
	    }
	}
	return low;
    }

    // If the key is contained in a:
    //   Returns the index of some key, if it is contained in a
    // Otherwise:
    //   Returns the index of the first element greater than the key or a.length
    //   or a.length if all elements in the array are less than the specified key
    public static int linearSearch(long[] a, int fromIndex, int toIndex, long key) {
	int low = fromIndex;
	int high = toIndex - 1;
	while ((low <= high) && (key > a[low])) {
	    low++;
	}
	return low;
    }
}
