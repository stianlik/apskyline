package ifis.skysim2.data.tools;

public final class PointComparator {

    // compares two data points a and b
    // returns DOMINATES if a > b
    // returns IS_DOMINATED_BY if a < b
    // returns IS_INCOMPARABLE_TO if a <> b
    // returns EQUALS if a == b
    public final static PointRelationship compare(final float[] pointA, final float[] pointB) {

// Only needed for equality handling:
//	int i = pointA.length;
//
//	while (--i >= 0) {
//	    if (pointA[i] != pointB[i]) {
//		break;
//	    }
//	}
//
//	if (i < 0) {
//	    return PointRelationship.EQUALS;
//	}

	int i = pointA.length - 1;

	if (pointA[i] >= pointB[i]) {
	    while (--i >= 0) {
		if (pointA[i] < pointB[i]) {
		    return PointRelationship.IS_INCOMPARABLE_TO;
		}
	    }
	    return PointRelationship.DOMINATES;
	} else {
	    while (--i >= 0) {
		if (pointA[i] > pointB[i]) {
		    return PointRelationship.IS_INCOMPARABLE_TO;
		}
	    }
	    return PointRelationship.IS_DOMINATED_BY;
	}
    }

    // compares two points located in a storage array
    public final static PointRelationship compare(float[] data, int indexA, int indexB, int d) {
	// this kind of indirection does not impose any performance penalty
	return compare(data, indexA, data, indexB, d);
    }

    public final static PointRelationship compare(float[] dataA, int indexA, float[] dataB, int indexB, int d) {

// Only needed for equality handling:
//	int i = d;
//	int positionA = indexA + d;
//	int positionB = indexB + d;
//
//	while (--i >= 0) {
//	    positionA--;
//	    positionB--;
//	    if (dataA[positionA] != dataB[positionB]) {
//		break;
//	    }
//	}
//
//	if (i < 0) {
//	    return PointRelationship.EQUALS;
//	}

	int i = d - 1;
	int positionA = indexA + d - 1;
	int positionB = indexB + d - 1;

	if (dataA[positionA] >= dataB[positionB]) {
	    while (--i >= 0) {
		positionA--;
		positionB--;
		if (dataA[positionA] < dataB[positionB]) {
		    return PointRelationship.IS_INCOMPARABLE_TO;
		}
	    }
	    return PointRelationship.DOMINATES;
	} else {
	    while (--i >= 0) {
		positionA--;
		positionB--;
		if (dataA[positionA] > dataB[positionB]) {
		    return PointRelationship.IS_INCOMPARABLE_TO;
		}
	    }
	    return PointRelationship.IS_DOMINATED_BY;
	}
    }

    public final static PointRelationship compare(float[] data, int index, float[] point, int d) {
	return compare(data, index, point, 0, d);
    }

    /*
     * A binary vector indicating the relationship between a's and b's coordinates.
     * result's (i + 1)-th least significant bit is 1  iff  a[i] > b[i]
     */
    public static long getSuccessorship(float[] a, float[] b) {
	long successorship = 0;
	int d = a.length;
	for (int i = d - 1; i >= 0; i--) {
	    successorship = successorship << 1;
	    if (a[i] > b[i]) {
		successorship++;
	    }
	}
	return successorship;
    }
}
