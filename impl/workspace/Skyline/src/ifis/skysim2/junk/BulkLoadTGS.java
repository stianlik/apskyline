package ifis.skysim2.junk;

import ifis.skysim2.data.sources.PointSourceRAM;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

// Bulk loading by Top-Down Greedy Split (TGS) algorithm
// Garcia, Lopez, Leutenegger: A Greedy Algorithm for Bulk Loading R-Trees
// Alborzi, Samet: Execution Time Analysis of a Top-Down R-Tree Construction Algorithm

public class BulkLoadTGS {

    // Create a new r-tree containing a given list of points
    public static Node bulkLoad(List<float[]> data) {
	List<List<float[]>> sortedLists = sortAll(data);
	int n = data.size();
	// h: number of Node levels below the root node to be returned by bulkLoad
	int h = Math.max(0, (int)Math.ceil(Math.log(n) / Math.log(RTreeOld.capacityMax) - 1));
	return bulkLoadChunk(sortedLists, h);
    }

    public static Node bulkLoadChunk(List<List<float[]>> sortedLists, int h) {
	if (h == 0) {
	    return buildLeafNode(sortedLists.get(0));
	} else {
	    // m: desired number of points under each child of the node to be returned
	    //    (equals the number of points that can be stored within an r-tree of height h
	    int m = (int)Math.pow(RTreeOld.capacityMax, h);
	    List<List<List<float[]>>> partitions = partition(sortedLists, m);
	    int k = partitions.size();
	    Node[] nodes = new Node[k];
	    for (int i = 0; i < k; i++) {
		List<List<float[]>> partition = partitions.get(i);
		nodes[i] = bulkLoadChunk(partition, h - 1);
	    }
	    return buildInternalNode(nodes);
	}
    }

    // partition data into parts of size m
    public static List<List<List<float[]>>> partition(List<List<float[]>> sortedLists, int m) {
	int n = sortedLists.get(0).size();
	List<List<List<float[]>>> partitions = new ArrayList<List<List<float[]>>>();
	if (n <= m) {
	    partitions.add(sortedLists);
	} else {
	    List<List<List<float[]>>> lh = bestBinarySplit(sortedLists, m);
	    List<List<float[]>> l = lh.get(0);
	    List<List<float[]>> h = lh.get(1);
	    partitions.addAll(partition(l, m));
	    partitions.addAll(partition(h, m));
	}
	return partitions;
    }

    // find the best binary split of the sorted data lists, where m is the size of each partition
    public static List<List<List<float[]>>> bestBinarySplit(List<List<float[]>> sortedLists, int m) {
	int S = sortedLists.size();
	int n = sortedLists.get(0).size();
	// M: number of partitions
	int M = (int)Math.ceil((double)n / m);
	// cStar: best cost bound so far
	double cStar = Double.POSITIVE_INFINITY;
	// sStar: best sort order so far (corresponding to cStar)
	int sStar = -1;
	// iStar: best split position (split after block i) so far
	int iStar = -1;
	for (int s = 0; s < S; s++) {
	    Rectangle[][] fb = computeBoundingBoxes(sortedLists.get(s), m);
	    Rectangle[] f = fb[0];
	    Rectangle[] b = fb[1];
	    for (int i = 0; i < M - 1; i++) {
		double c = cost(f[i], b[i]);
		if (c < cStar) {
		    cStar = c;
		    sStar = s;
		    iStar = i;
		}
	    }
	}
	// split sortedLists according to sStar and iStar
	List<float[]> lStar = sortedLists.get(sStar).subList(0, (iStar + 1) * m);
	List<float[]> hStar = sortedLists.get(sStar).subList((iStar + 1) * m, n);
	List<List<float[]>> l = sortAll(lStar);
	List<List<float[]>> h = sortAll(hStar);
	List<List<List<float[]>>> lh = new ArrayList<List<List<float[]>>>(2);
	lh.add(l);
	lh.add(h);
	return lh;
    }

    // cost of split into rectangles mbr1 and mbr2
    public static double cost(Rectangle mbr1, Rectangle mbr2) {
	// minimize overlap: only useful for rectangular data in the tree ...
	// (will yield 0 for point data)
//	return mbr1.getOverlapWith(mbr2);
	// minimize total area
	return mbr1.getArea() + mbr2.getArea() - mbr1.getOverlapWith(mbr2);
    }

    // compute the bounding boxes of possible binary splits of data
    // into blocks of size m
    public static Rectangle[][] computeBoundingBoxes(List<float[]> data, int m) {
	int n = data.size();
	// M: number of blocks
	int M = (int)Math.ceil((double)n / m);
	// b[i]: MBR of the i-th block
	Rectangle[] b = new Rectangle[M];
	// l[i]: MBR of blocks 0, ..., i
	Rectangle[] l = new Rectangle[M - 1];
	// h[i]: MBR of blocks i + 1, ..., M - 1
	Rectangle[] h = new Rectangle[M - 1];

	for (int i = 0; i < M; i++) {
	    int start = i * m;
	    int end = Math.min((i + 1) * m - 1, n - 1);
	    b[i] = computeMBR(data, start, end);
	}

	l[0] = b[0];
	for (int i = 1; i < M - 1; i++) {
	    l[i] = computeMBR(l[i - 1], b[i]);
	}

	h[M - 2] = b[M - 1];
	for (int i = M - 3; i >= 0; i--) {
	    h[i] = computeMBR(h[i + 1], b[i + 1]);
	}

	Rectangle[][] result = new Rectangle[2][];
	result[0] = l;
	result[1] = h;
	return result;
    }

    // compute lower and higher edge of MBR for {data[start], ..., data[end]}
    public static Rectangle computeMBR(List<float[]> data, int start, int end) {
	int d = data.get(0).length;
	Rectangle mbr = new Rectangle(d);
	float[] pointStart = data.get(start);
	float[] lower = Arrays.copyOf(pointStart, d);
	float[] upper = Arrays.copyOf(pointStart, d);
	for (int i = start + 1; i <= end; i++) {
	    float[] point = data.get(i);
	    for (int j = 0; j < d; j++) {
		if (point[j] < lower[j]) {
		    lower[j] = point[j];
		} else if (point[j] > upper[j]) {
		    upper[j] = point[j];
		}
	    }
	}
	mbr.setLower(lower);
	mbr.setUpper(upper);
	return mbr;
    }

    public static Rectangle computeMBR(Rectangle mbr1, Rectangle mbr2) {
	int d = mbr1.getD();
	Rectangle mbr = new Rectangle(d);
	for (int i = 0; i < d; i++) {
	    mbr.setLower(i, Math.min(mbr1.getLower(i), mbr2.getLower(i)));
	    mbr.setUpper(i, Math.max(mbr1.getUpper(i), mbr2.getUpper(i)));
	}
	return mbr;
    }

    public static LeafNode buildLeafNode(List<float[]> data) {
	int d = data.get(0).length;
	LeafNode lnode = new LeafNode(d);
	for (float[] point: data) {
	    lnode.entries.add(point);
	}
	lnode.recomputeMBR();
	return lnode;
    }

    public static InternalNode buildInternalNode(Node[] nodes) {
	int d = nodes[0].getD();
	InternalNode inode = new InternalNode(d);
	for (Node node: nodes) {
	    inode.entries.add(node);
	}
	inode.recomputeMBR();
	return inode;
    }

    // Sort data according to its i-th coordinate
    public static List<float[]> sort(List<float[]> data, int i) {
	List<float[]> sortedData = new PointSourceRAM(data);
	Collections.sort(sortedData, new PointComparator(i));
	return sortedData;
    }

    // sort with respect to any dimension
    public static List<List<float[]>> sortAll(List<float[]> data) {
	int d = data.get(0).length;
	List<List<float[]>> sortedLists = new ArrayList<List<float[]>>(d);
	for (int i = 0; i < d; i++) {
	    List<float[]> sortedData = sort(data, i);
	    sortedLists.add(sortedData);
	}
	return sortedLists;
    }

    // Compares points using a fixed coordinate
    private static class PointComparator implements Comparator<float[]> {

	private int i;

	// compare by i-th coordinate
	public PointComparator(int i) {
	    this.i = i;
	}

	@Override
	public int compare(float[] point1, float[] point2) {
	    if (point1[i] > point2[i]) {
		return -1;
	    } else if (point1[i] < point2[i]) {
		return 1;
	    } else {
		return 0;
	    }
	}
    }
}
