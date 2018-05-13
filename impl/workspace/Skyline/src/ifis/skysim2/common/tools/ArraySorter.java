package ifis.skysim2.common.tools;

import java.util.Arrays;

public class ArraySorter {

    // Sorts both arrays according to the values contained in the sortvals array
    public static void doubleSort(Object[] objs, double[] sortvals, int fromIndex, int toIndex) {
	sort2(sortvals, fromIndex, toIndex, objs);
    }

    public static void longSort(Object[] objs, long[] sortvals) {
        longSort(objs, sortvals, 0, objs.length);
    }

    public static void longSort(Object[] objs, long[] sortvals, int fromIndex, int toIndex) {
	sort1(sortvals, fromIndex, toIndex, objs);
    }

    // sorts a large blocked array, where each block consists of d array entries
    public static void longArraySort(float[] objs, int d, long[] sortvals) {
        longArraySort(objs, d, sortvals, 0, sortvals.length);
    }

    public static void longArraySort(float[] objs, int d, long[] sortvals, int fromIndex, int toIndex) {
	sort1Array(sortvals, fromIndex, toIndex, objs, d);
    }

    // Copied from java.util.Arrays and modified
    private static void sort2(double a[], int fromIndex, int toIndex, Object[] b) {
	final long NEG_ZERO_BITS = Double.doubleToLongBits(-0.0d);
	int numNegZeros = 0;
	int i = fromIndex, n = toIndex;
	while (i < n) {
	    if (a[i] != a[i]) {
		n--;
		swap(a, i, n);
		swap(b, i, n);
	    } else {
		if (a[i] == 0 && Double.doubleToLongBits(a[i]) == NEG_ZERO_BITS) {
		    a[i] = 0.0d;
		    numNegZeros++;
		}
		i++;
	    }
	}

	sort1(a, fromIndex, n - fromIndex, b);

	if (numNegZeros != 0) {
	    int j = binarySearch0(a, fromIndex, n, 0.0d);
	    do {
		j--;
	    } while (j >= fromIndex && a[j] == 0.0d);

	    for (int k = 0; k < numNegZeros; k++) {
		a[++j] = -0.0d;
	    }
	}
    }

    private static void sort1(double x[], int off, int len, Object[] y) {
	if (len < 7) {
	    for (int i = off; i < len + off; i++) {
		for (int j = i; j > off && x[j - 1] > x[j]; j--) {
		    swap(x, j, j - 1);
		    swap(y, j, j - 1);
		}
	    }
	    return;
	}

	int m = off + (len >> 1);
	if (len > 7) {
	    int l = off;
	    int n = off + len - 1;
	    if (len > 40) {
		int s = len / 8;
		l = med3(x, l, l + s, l + 2 * s);
		m = med3(x, m - s, m, m + s);
		n = med3(x, n - 2 * s, n - s, n);
	    }
	    m = med3(x, l, m, n);
	}
	double v = x[m];

	int a = off, b = a, c = off + len - 1, d = c;
	while (true) {
	    while (b <= c && x[b] <= v) {
		if (x[b] == v) {
		    swap(x, a, b);
		    swap(y, a, b);
		    a++;
		}
		b++;
	    }
	    while (c >= b && x[c] >= v) {
		if (x[c] == v) {
		    swap(x, c, d);
		    swap(y, c, d);
		    d--;
		}
		c--;
	    }
	    if (b > c) {
		break;
	    }
	    swap(x, b, c);
	    swap(y, b, c);
	    b++;
	    c--;
	}

	int s, n = off + len;
	s = Math.min(a - off, b - a);
	vecswap(x, off, b - s, s);
	vecswap(y, off, b - s, s);
	s = Math.min(d - c, n - d - 1);
	vecswap(x, b, n - s, s);
	vecswap(y, b, n - s, s);

	if ((s = b - a) > 1) {
	    sort1(x, off, s, y);
	}
	if ((s = d - c) > 1) {
	    sort1(x, n - s, s, y);
	}
    }

    private static void sort1(long x[], int off, int len, Object[] y) {
        // Insertion sort on smallest arrays
        if (len < 7) {
            for (int i=off; i<len+off; i++)
                for (int j=i; j>off && x[j-1]>x[j]; j--) {
                    swap(x, j, j - 1);
		    swap(y, j, j - 1);
		}
            return;
        }

        // Choose a partition element, v
        int m = off + (len >> 1);       // Small arrays, middle element
        if (len > 7) {
            int l = off;
            int n = off + len - 1;
            if (len > 40) {        // Big arrays, pseudomedian of 9
                int s = len/8;
                l = med3(x, l,     l+s, l+2*s);
                m = med3(x, m-s,   m,   m+s);
                n = med3(x, n-2*s, n-s, n);
            }
            m = med3(x, l, m, n); // Mid-size, med of 3
        }
        long v = x[m];

        // Establish Invariant: v* (<v)* (>v)* v*
        int a = off, b = a, c = off + len - 1, d = c;
        while(true) {
            while (b <= c && x[b] <= v) {
                if (x[b] == v) {
                    swap(x, a, b);
		    swap(y, a, b);
		    a++;
		}
                b++;
            }
            while (c >= b && x[c] >= v) {
                if (x[c] == v) {
                    swap(x, c, d);
		    swap(y, c, d);
		    d--;
		}
                c--;
            }
            if (b > c)
                break;
            swap(x, b, c);
	    swap(y, b, c);
	    b++;
	    c--;
        }

        // Swap partition elements back to middle
        int s, n = off + len;
        s = Math.min(a-off, b-a  );
	vecswap(x, off, b-s, s);
	vecswap(y, off, b-s, s);
        s = Math.min(d-c,   n-d-1);
	vecswap(x, b,   n-s, s);
	vecswap(y, b,   n-s, s);

        // Recursively sort non-partition-elements
        if ((s = b-a) > 1)
            sort1(x, off, s, y);
        if ((s = d-c) > 1)
            sort1(x, n-s, s, y);
    }

    private static void sort1Array(long x[], int off, int len, float[] y, int blocksize) {
	// Insertion sort on smallest arrays
        if (len < 7) {
            for (int i=off; i<len+off; i++)
                for (int j=i; j>off && x[j-1]>x[j]; j--) {
                    swap(x, j, j - 1);
		    swapArray(y, blocksize, j, j - 1);
		}
            return;
        }

        // Choose a partition element, v
        int m = off + (len >> 1);       // Small arrays, middle element
        if (len > 7) {
            int l = off;
            int n = off + len - 1;
            if (len > 40) {        // Big arrays, pseudomedian of 9
                int s = len/8;
                l = med3(x, l,     l+s, l+2*s);
                m = med3(x, m-s,   m,   m+s);
                n = med3(x, n-2*s, n-s, n);
            }
            m = med3(x, l, m, n); // Mid-size, med of 3
        }
        long v = x[m];

        // Establish Invariant: v* (<v)* (>v)* v*
        int a = off, b = a, c = off + len - 1, d = c;
        while(true) {
            while (b <= c && x[b] <= v) {
                if (x[b] == v) {
                    swap(x, a, b);
		    swapArray(y, blocksize, a, b);
		    a++;
		}
                b++;
            }
            while (c >= b && x[c] >= v) {
                if (x[c] == v) {
                    swap(x, c, d);
		    swapArray(y, blocksize, c, d);
		    d--;
		}
                c--;
            }
            if (b > c)
                break;
            swap(x, b, c);
	    swapArray(y, blocksize, b, c);
	    b++;
	    c--;
        }

        // Swap partition elements back to middle
        int s, n = off + len;
        s = Math.min(a-off, b-a  );
	vecswap(x, off, b-s, s);
	vecswapArray(y, blocksize, off, b-s, s);
        s = Math.min(d-c,   n-d-1);
	vecswap(x, b,   n-s, s);
	vecswapArray(y, blocksize, b,   n-s, s);

        // Recursively sort non-partition-elements
        if ((s = b-a) > 1)
            sort1Array(x, off, s, y, blocksize);
        if ((s = d-c) > 1)
            sort1Array(x, n-s, s, y, blocksize);
    }

    private static void swap(double x[], int a, int b) {
	double t = x[a];
	x[a] = x[b];
	x[b] = t;
    }

    private static void swap(long x[], int a, int b) {
        long t = x[a];
        x[a] = x[b];
        x[b] = t;
    }

    private static void swap(Object x[], int a, int b) {
	Object t = x[a];
	x[a] = x[b];
	x[b] = t;
    }

    private static void swapArray(float x[], int blocksize, int a, int b) {
	int posA = a * blocksize;
	int posB = b * blocksize;
	final float[] container = Arrays.copyOfRange(x, posA, posA + blocksize);
	System.arraycopy(x, posB, x, posA, blocksize);
	System.arraycopy(container, 0, x, posB, blocksize);
    }

    private static void vecswap(double x[], int a, int b, int n) {
	for (int i = 0; i < n; i++, a++, b++) {
	    swap(x, a, b);
	}
    }

    private static void vecswap(long x[], int a, int b, int n) {
        for (int i=0; i<n; i++, a++, b++)
            swap(x, a, b);
    }

    private static void vecswap(Object x[], int a, int b, int n) {
	for (int i = 0; i < n; i++, a++, b++) {
	    swap(x, a, b);
	}
    }

    private static void vecswapArray(float x[], int blocksize, int a, int b, int n) {
	for (int i = 0; i < n; i++, a++, b++) {
	    swapArray(x, blocksize, a, b);
	}
    }

    private static int med3(double x[], int a, int b, int c) {
	return (x[a] < x[b] ? (x[b] < x[c] ? b : x[a] < x[c] ? c : a) : (x[b] > x[c] ? b : x[a] > x[c] ? c : a));
    }

    private static int med3(long x[], int a, int b, int c) {
        return (x[a] < x[b] ?
                (x[b] < x[c] ? b : x[a] < x[c] ? c : a) :
                (x[b] > x[c] ? b : x[a] > x[c] ? c : a));
    }

    private static int binarySearch0(double[] a, int fromIndex, int toIndex, double key) {
	int low = fromIndex;
	int high = toIndex - 1;

	while (low <= high) {
	    int mid = (low + high) >>> 1;
	    double midVal = a[mid];

	    if (midVal < key) {
		low = mid + 1;
	    } else if (midVal > key) {
		high = mid - 1;
	    } else {
		long midBits = Double.doubleToLongBits(midVal);
		long keyBits = Double.doubleToLongBits(key);
		if (midBits == keyBits) {
		    return mid;
		} else if (midBits < keyBits) {
		    low = mid + 1;
		} else {
		    high = mid - 1;
		}
	    }
	}
	return -(low + 1);
    }
}