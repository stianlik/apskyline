/*
 * parallel sorting
 * Source: http://www.javaworld.com/javaworld/jw-09-2007/jw-09-multicoreprocessing.html
 */

package ifis.skysim2.common.tools;

import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConcurrentSort {

	/*
	 * BEGIN copied from java.util.Arrays
	 * (changed Object[] to
	 */
	private static final int INSERTIONSORT_THRESHOLD = 7;

	private static void mergeSort(Object[] src, Object[] dest,
		int low, int high, int off,
		Comparator c) {
		int length = high - low;

		// Insertion sort on smallest arrays
		if (length < INSERTIONSORT_THRESHOLD) {
			for (int i = low; i < high; i++) {
				for (int j = i; j > low && c.compare(dest[j - 1], dest[j]) > 0; j--) {
					swap(dest, j, j - 1);
				}
			}
			return;
		}

		// Recursively sort halves of dest into src
		int destLow = low;
		int destHigh = high;
		low += off;
		high += off;
		int mid = (low + high) >>> 1;
		mergeSort(dest, src, low, mid, -off, c);
		mergeSort(dest, src, mid, high, -off, c);

		// If list is already sorted, just copy from src to dest.  This is an
		// optimization that results in faster sorts for nearly ordered lists.
		if (c.compare(src[mid - 1], src[mid]) <= 0) {
			System.arraycopy(src, low, dest, destLow, length);
			return;
		}

		// Merge sorted halves (now in src) into dest
		for (int i = destLow, p = low, q = mid; i < destHigh; i++) {
			if (q >= high || p < mid && c.compare(src[p], src[q]) <= 0) {
				dest[i] = src[p++];
			} else {
				dest[i] = src[q++];
			}
		}
	}

	private static void swap(Object[] x, int a, int b) {
		Object t = x[a];
		x[a] = x[b];
		x[b] = t;
	}
	/*
	 * END copied from java.util.Arrays
	 */

	public static <T> void sort(List<T> list, Comparator<? super T> c) {
		Object[] a = list.toArray();
		sort(a, (Comparator) c);
		ListIterator i = list.listIterator();
		for (int j = 0; j < a.length; j++) {
			i.next();
			i.set(a[j]);
		}
	}

	public static <T> void sort(final T[] a, final Comparator<? super T> c) {
		final T[] aux = (T[]) a.clone();

		if (c == null) {
			throw new UnsupportedOperationException("Not supported yet.");
		} else {
			final CountDownLatch doneSignal = new CountDownLatch(2);
			ExecutorService e = Executors.newFixedThreadPool(2);

			class WorkerRunnable implements Runnable {

				int start;
				int end;

				WorkerRunnable(int start, int end) {
					this.start = start;
					this.end = end;
				}

				@Override
				public void run() {
					mergeSort(aux, a, start, end, 0, c);
					doneSignal.countDown();
				}
			}

			int mid = a.length >> 1;
			e.execute(new WorkerRunnable(0, mid));
			e.execute(new WorkerRunnable(mid, a.length));
			try {
				doneSignal.await();
			} catch (InterruptedException ex) {
				Logger.getLogger(ConcurrentSort.class.getName()).log(Level.SEVERE, null, ex);
			}
			e.shutdown();

			System.arraycopy(a, 0, aux, 0, a.length);
			// merge two halves
			for (int i = 0, p = 0, q = mid; i < a.length; i++) {
				if (q >= a.length || p < mid && (c.compare(aux[p], aux[q]) <= 0)) {
					a[i] = aux[p++];
				} else {
					a[i] = aux[q++];
				}
			}

		}
	}
}