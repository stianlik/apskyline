package ifis.skysim2.data.trees.zbtree;

import ifis.skysim2.data.generator.DataGeneratorIndependent;
import ifis.skysim2.common.tools.StringUtils;
import java.util.Arrays;

public class ZOrderHelper {
    private final static long LONG_BIT_2 = 1l << 62;
    private final static int INT_BIT_1 = 1 << 31;
    private final static int INT_BIT_2 = 1 << 30;
    private final static int INT_BIT_9 = 0x00800000;
    private final static int INT_BITS_2_TO_9 = 0x7f800000;
    private final static int INT_BITS_10_TO_32 = 0x007fffff;
    private final static int INT_BITS_2_TO_32 = ~(1 << 31);
    private final static int EXPONENT_BIAS = 127;

    // converts a float value of range [0, 1] to an int value such that
    // f1 < f2 <=> floatToIntZ(f1) < floatToIntZ(F2),
    // for any f1, f2 \in (0, 1) that are not "too small"
    public static int floatToIntZ(float f) {
	// According to the IEEE standard, f is represented as
	//   (-1)^sign * 2^(exponent) * 1.(fraction)_2
	//     sign: bit 1
	//     exponent: bits 2 to 9
	//     fraction: bits 10 to 32
	//   --> Since our floats are between 0 and 1, we know that sign == 0
	//   --> We also know that exponent <= 0
	// Get f's bit sequence
	int fint = Float.floatToRawIntBits(f);
	// Determine exponent
	int exponentBiasedAndUnshifted = fint & INT_BITS_2_TO_9;
	int exponentBiased = exponentBiasedAndUnshifted >> 23;
	int exponent = exponentBiased - EXPONENT_BIAS;
	// Determine fraction
	int fraction = fint & INT_BITS_10_TO_32;
	// Determine 1.(fraction)_2
	int onePointFraction = INT_BIT_9 | fraction;
	// shift to get result (first bit cannot be used since this is the sign)
	int shiftLeft = 8 + exponent;
	if (shiftLeft >= 8) {
	    return INT_BITS_2_TO_32;
	} else if (shiftLeft >= 0) {
	    return onePointFraction << shiftLeft;
	} else if (shiftLeft < -23) {
	    return 0;
	} else {
	    return onePointFraction >> -shiftLeft;
	}
    }

    private static String checkFloatToIntZ(float f) {
	int z = floatToIntZ(f);
	return String.format("%f: |%1s|%8s|%23s| --> |%32s| (%d)", f,
			   				           Integer.toBinaryString((Float.floatToRawIntBits(f) & INT_BIT_1) >> 31),
							           Integer.toBinaryString((Float.floatToRawIntBits(f) & INT_BITS_2_TO_9) >> 23),
							           Integer.toBinaryString(Float.floatToRawIntBits(f) & INT_BITS_10_TO_32),
							           Integer.toBinaryString(z),
							           z);
    }

    // The Z address of a point is constructed by
    // interleaving the coordinates' integer representations
    public static long getZAddress(float[] point) {
	int d = point.length;
	// get the coordinates' integer representations
	int[] zaPoint = new int[d];
	for (int i = 0; i < d; i++) {
	    zaPoint[i] = floatToIntZ(point[i]);
	}
	// find out how many bits are available for each coordinate
	int numBitsPerCoordinate = Math.min(Integer.SIZE - 1, (Long.SIZE - 1) / d);
	// construct the point's Z address
	long za = 0;
	int currentMaskZAPoint = INT_BIT_2;
	long currentMaskZA = LONG_BIT_2;
	for (int i = 0; i < numBitsPerCoordinate; i++) {
	    for (int j = 0; j < d; j++) {
		// extract bit i + 1 from coordinate j
		long currentBitZAPoint = zaPoint[j] & currentMaskZAPoint;
		boolean currentBitZAPointSet = (currentBitZAPoint != 0);
		if (currentBitZAPointSet) {
		    za = za | currentMaskZA;
		}
		currentMaskZA = currentMaskZA >> 1;
	    }
	    currentMaskZAPoint = currentMaskZAPoint >> 1;
	}
	return za;
    }

    private static String checkGetZAddress(float[] point) {
	StringBuffer result = new StringBuffer();
	int d = point.length;
	for (int i = 0; i < d; i++) {
	    result.append(checkFloatToIntZ(point[i]) + "\n");
	}
	long za = getZAddress(point);
	result.append(String.format("=========>|%64s|", Long.toBinaryString(za)));
	return result.toString();
    }

    // Finds the longest common prefix of two Z addresses,
    // returns the length of the longest common prefix (the sign bit does not count)
    private static int longestCommonPrefix(long z1, long z2) {
	long xor = z1 ^ z2;
	return Long.numberOfLeadingZeros(xor) - 1;
    }

    private static String checkLongestCommonPrefix(long z1, long z2) {
	int x = longestCommonPrefix(z1, z2);
	StringBuffer result = new StringBuffer();
	result.append(String.format("|%64s|%n", Long.toBinaryString(z1)));
	result.append(String.format("|%64s|%n", Long.toBinaryString(z2)));
	result.append(StringUtils.repeat(' ', x + 1) + "X (" + x + ")");
	return result.toString();
    }

    private static int[] zAddressToIntZs(long z, int d) {
	return zAddressToIntZs(z, d, Long.SIZE - 1);
    }

    // Converts a Z address to the corresponding sequence of intZs,
    // taking only the first prefix bits into account
    // (the sign bit does not count here)
    private static int[] zAddressToIntZs(long z, int d, int prefix) {
	int[] zInts = new int[d];
	final int numBitsToProcess = Math.min((Integer.SIZE - 1) * d, prefix);
	long currentMaskZA = LONG_BIT_2;
	int currentMaskZAPoint = INT_BIT_2;
	int j = 0;
	for (int i = 0; i < numBitsToProcess; i++) {
	    long currentBitZA = z & currentMaskZA;
	    boolean currentBitZASet = (currentBitZA != 0);
	    if (currentBitZASet) {
		zInts[j] = zInts[j] | currentMaskZAPoint;
	    }
	    j++;
	    currentMaskZA = currentMaskZA >> 1;
	    if (j == d) {
		j = 0;
		currentMaskZAPoint = currentMaskZAPoint >> 1;
	    }
	}
	return zInts;
    }

    private static String checkZAddressToIntZs(long z, int d) {
	return checkZAddressToIntZs(z, d, Long.SIZE - 1);
    }

    private static String checkZAddressToIntZs(long z, int d, int prefix) {
	StringBuffer result = new StringBuffer();
	int[] zInts = zAddressToIntZs(z, d, prefix);
	result.append(String.format("|%64s|%n", Long.toBinaryString(z)));
	for (int i = 0; i < d; i++) {
	    result.append(String.format("|%32s|%n", Integer.toBinaryString(zInts[i])));
	}
	return result.toString();
    }

    private static float intZToFloat(int intZ) {
	return intZToFloat(intZ, Integer.SIZE - 1, false);
    }

    // Converts a zInt to the corresponding float, while taking only the first prefix
    // bits if intZ into account (the sign buit does not count here);
    // fill determines whether the remaining bits are filled with 0s or 1s
    private static float intZToFloat(int intZ, int prefix, boolean fill) {
	// 0 <= prefix <= 31
	int leadingZeros = Integer.numberOfLeadingZeros(intZ);
	if (leadingZeros - 1 >= prefix) {
	    // the prefix contains only zeros
	    if (fill) {
		// Set the first bit after the prefix to 1
		intZ = intZ | (INT_BIT_2 >> prefix);
//		System.out.println("intZ_fixed:");
//		System.out.format("|%32s|%n", Integer.toBinaryString(intZ));
		leadingZeros = prefix + 1;
		// handle special case prefix == 0
		// (identical to the prefix == 1 case due to the previous step)
		if (prefix == 0) {
		    prefix = 1;
		}
	    } else {
		return 0f;
	    }
	}
	// Determine exponent
	int exponent = -leadingZeros;
	int exponentBiased = exponent + EXPONENT_BIAS;
	int exponentBiasedAndUnshifted = exponentBiased << 23;
	// Determine fraction
	int shiftRight = 8 + exponent;
	int onePointFraction;
	if (shiftRight >= 0) {
	    onePointFraction = intZ >> shiftRight;
	} else {
	    onePointFraction = intZ << -shiftRight;
	}
	int fraction = onePointFraction & INT_BITS_10_TO_32;
	// Fill fraction after prefix
	int lastBitOfPrefix = 1 + prefix + shiftRight;
	if (lastBitOfPrefix < 32) {
	    if (fill) {
//		System.out.println("Prefix: " + prefix);
//		System.out.println("shiftRight: " + shiftRight);
		int fillMask = (INT_BIT_1 >>> (lastBitOfPrefix - 1)) - 1;
//		System.out.println("Fillmask:");
//		System.out.format("|%32s|%n", Integer.toBinaryString(fillMask));
		fraction = fraction | fillMask;
	    } else {
		int fillMask = INT_BIT_1 >> (lastBitOfPrefix - 1);
		fraction = fraction & fillMask;
	    }
	}
	// Construct float
	return Float.intBitsToFloat(exponentBiasedAndUnshifted | fraction);
    }

    private static String checkIntZToFloat(int intZ) {
	return checkIntZToFloat(intZ, Integer.SIZE - 1, false);
    }

    private static String checkIntZToFloat(int intZ, int prefix, boolean fill) {
	float f = intZToFloat(intZ, prefix, fill);
	return String.format("|%32s| --> %f: |%1s|%8s|%23s|", Integer.toBinaryString(intZ),
							      f,
			   				      Integer.toBinaryString((Float.floatToRawIntBits(f) & INT_BIT_1) >> 31),
							      Integer.toBinaryString((Float.floatToRawIntBits(f) & INT_BITS_2_TO_9) >> 23),
							      Integer.toBinaryString(Float.floatToRawIntBits(f) & INT_BITS_10_TO_32));
    }

    public static float[] getRZLowHigh(long zLow, long zHigh, int d) {
	float[] rzLowHigh = new float[d << 1];
	int prefixZ = longestCommonPrefix(zLow, zHigh);
	int[] intZsLow = zAddressToIntZs(zLow, d, prefixZ);
	int[] intZsHigh = zAddressToIntZs(zHigh, d, prefixZ);
	int prefix = prefixZ / d;
	int remainder = prefixZ % d;
	for (int i = 0; i < remainder; i++) {
	    rzLowHigh[i] = intZToFloat(intZsLow[i], prefix + 1, false);
	    rzLowHigh[d + i] = intZToFloat(intZsHigh[i], prefix + 1, true);
	}
	for (int i = remainder; i < d; i++) {
	    rzLowHigh[i] = intZToFloat(intZsLow[i], prefix, false);
	    rzLowHigh[d + i] = intZToFloat(intZsHigh[i], prefix, true);
	}
	return rzLowHigh;
    }

    public static float[] getRZLowHigh(long z, int d) {
	float[] rzLowHigh = new float[d << 1];
	int prefixZ = Long.SIZE - 1;
	int[] intZsLow = zAddressToIntZs(z, d, prefixZ);
	int[] intZsHigh = Arrays.copyOf(intZsLow, d);
	int prefix = prefixZ / d;
	int remainder = prefixZ % d;
	for (int i = 0; i < remainder; i++) {
	    rzLowHigh[i] = intZToFloat(intZsLow[i], prefix + 1, false);
	    rzLowHigh[d + i] = intZToFloat(intZsHigh[i], prefix + 1, true);
	}
	for (int i = remainder; i < d; i++) {
	    rzLowHigh[i] = intZToFloat(intZsLow[i], prefix, false);
	    rzLowHigh[d + i] = intZToFloat(intZsHigh[i], prefix, true);
	}
	return rzLowHigh;
    }

    private static String checkGetRZLowHigh(long zLow, long zHigh, int d) {
	if (zLow > zHigh) {
	    long temp = zLow;
	    zLow = zHigh;
	    zHigh = temp;
	}
	StringBuffer result = new StringBuffer();
	result.append(String.format("zLow:  |%64s| (%d)%n", Long.toBinaryString(zLow), zLow));
	result.append(String.format("zHigh: |%64s| (%d)%n", Long.toBinaryString(zHigh), zHigh));
	float[] rzLowHigh = getRZLowHigh(zLow, zHigh, d);
	result.append(String.format("Low:%n"));
	for (int i = 0; i < d; i++) {
	    float f = rzLowHigh[i];
	    result.append(String.format("  %f: |%1s|%8s|%23s|%n", f,
			   				          Integer.toBinaryString((Float.floatToRawIntBits(f) & INT_BIT_1) >> 31),
							          Integer.toBinaryString((Float.floatToRawIntBits(f) & INT_BITS_2_TO_9) >> 23),
							          Integer.toBinaryString(Float.floatToRawIntBits(f) & INT_BITS_10_TO_32)));
	}
	result.append(String.format("High:%n"));
	for (int i = 0; i < d; i++) {
	    float f = rzLowHigh[d + i];
	    result.append(String.format("  %f: |%1s|%8s|%23s|%n", f,
			   				          Integer.toBinaryString((Float.floatToRawIntBits(f) & INT_BIT_1) >> 31),
							          Integer.toBinaryString((Float.floatToRawIntBits(f) & INT_BITS_2_TO_9) >> 23),
							          Integer.toBinaryString(Float.floatToRawIntBits(f) & INT_BITS_10_TO_32)));
	}
	return result.toString();
    }


    public static void main(String[] args) {
//	float[] p = {0.75f, 0.75f / 2};
//	int d = p.length;

	int d = 2;
	DataGeneratorIndependent dg = new DataGeneratorIndependent();
	dg.resetToDefaultSeed();
	float[] data = dg.generate(d, 2);
	float[] p1 = Arrays.copyOfRange(data, 0, d);
	float[] p2 = Arrays.copyOfRange(data, d, 2 * d);

	System.out.println();
	System.out.println();

	System.out.println("Original point:");
	System.out.println(Arrays.toString(p1));
	System.out.println();
	System.out.println();

	System.out.println("Conversion to ZInts:");
	for (int i = 0; i < d; i++) {
	    System.out.println(checkFloatToIntZ(p1[i]));
	}
	System.out.println();
	System.out.println();

	System.out.println("Conversion to Z address:");
	System.out.println(checkGetZAddress(p1));
	System.out.println();
	System.out.println();

	int prefix = 25;
	boolean fill = false;
	System.out.format("Conversion back to ZInts (prefix = %d, fill = %s):%n", prefix, Boolean.toString(fill));
	for (int i = 0; i < d; i++) {
	    System.out.println(checkIntZToFloat(floatToIntZ(p1[i]), prefix, fill));
	}
	System.out.println();
	System.out.println();

	fill = true;
	System.out.format("Conversion back to ZInts (prefix = %d, fill = %s):%n", prefix, Boolean.toString(fill));
	for (int i = 0; i < d; i++) {
	    System.out.println(checkIntZToFloat(floatToIntZ(p1[i]), prefix, fill));
	}
	System.out.println();
	System.out.println();

	System.out.println("Conversion to RZ region:");
	long z1 = getZAddress(p1);
	long z2 = getZAddress(p2);
	System.out.println(Arrays.toString(p1));
	System.out.println(Arrays.toString(p2));
        System.out.println(checkGetRZLowHigh(z1, z2, d));
	System.out.println();
	System.out.println();
    }
}
