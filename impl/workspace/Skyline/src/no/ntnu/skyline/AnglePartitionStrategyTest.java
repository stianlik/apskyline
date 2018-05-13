package no.ntnu.skyline;

import static org.junit.Assert.assertTrue;

import java.util.Locale;

import org.junit.Before;
import org.junit.Test;

public class AnglePartitionStrategyTest {
	
	double angleMax = 0.5 * Math.PI;
	
	@Before
	public void setUp() {
		Locale.setDefault(Locale.ENGLISH);
	}

	@Test(expected=RuntimeException.class)
	public void calculateTotalVolume_should_fail_on_too_small_dimension() {
		EquiAnglePartitionStrategy.calculateTotalVolume(1, 100);
	}
	
	@Test(expected=RuntimeException.class)
	public void calculateTotalVolume_should_fail_on_too_big_dimension() {
		EquiAnglePartitionStrategy.calculateTotalVolume(9, 100);
	}
	
	@Test
	public void calculateTotalVolume_for_6_dimensions() {
		double expected = Math.pow(Math.PI, 3)*Math.pow(100, 6)/384;
		double actual = EquiAnglePartitionStrategy.calculateTotalVolume(6, 100);
		assertTrue(expected - 0.01 <= actual && actual <= expected + 0.01);
	}
	
	@Test
	public void instanciate_with_different_partition_sizes() {
		new EquiAnglePartitionStrategy(6, 2);
		new EquiAnglePartitionStrategy(6, 4);
		new EquiAnglePartitionStrategy(6, 6);
		new EquiAnglePartitionStrategy(6, 12);
		new EquiAnglePartitionStrategy(6, 24);
		new EquiAnglePartitionStrategy(6, 32);
		new EquiAnglePartitionStrategy(6, 64);
		new EquiAnglePartitionStrategy(6, 128);
		new EquiAnglePartitionStrategy(6, 512);
		new EquiAnglePartitionStrategy(6, 1024);
	}
	
	@Test
	public void calculateVolume() {
		EquiAnglePartitionStrategy s = new EquiAnglePartitionStrategy(2, 10);
		double max = 0.5 * Math.PI;
		
		assertEquals(s.volumeTotal, s.calculateVolume(new double[]{0}, new double[]{max}));
		assertEquals(s.volumeTotal/2, s.calculateVolume(new double[]{0}, new double[]{max/2}));
		assertEquals(s.volumeTotal/3, s.calculateVolume(new double[]{0}, new double[]{max/3}));
	}
	
	@Test
	public void calculateBounds_in_2_dimensions_1_partition() {
		EquiAnglePartitionStrategy s = new EquiAnglePartitionStrategy(2, 1);
		s.calculateBounds();
		assertEquals(1, s.partitionCount);
		assertEquals(1, s.boundsLow[0].length);
		assertEquals(0.0, s.boundsLow[0][0]);
		assertEquals(angleMax, s.boundsHigh[0][0]);
		assertEquals(s.volumeTotal, s.calculateVolume(s.boundsLow[0], s.boundsHigh[0]));

	}
	
	@Test
	public void calculateBounds_in_2_dimensions_2_partitions() {
		double angleMax = 0.5 * Math.PI;
		EquiAnglePartitionStrategy s = new EquiAnglePartitionStrategy(2, 2);
		s.calculateBounds();
		assertEquals(2, s.partitionCount);
		assertEquals(2, s.boundsLow[0].length);
		assertEquals(0.0, s.boundsLow[0][0]);
		assertEquals(angleMax/2, s.boundsHigh[0][0]);
		assertEquals(angleMax/2, s.boundsLow[0][1]);
		assertEquals(s.volumeTotal/2, s.calculateVolume(new double[]{s.boundsLow[0][0]}, new double[]{s.boundsHigh[0][0]}));
		assertEquals(s.volumeTotal/2, s.calculateVolume(new double[]{s.boundsLow[0][1]}, new double[]{s.boundsHigh[0][1]}));
	}
	
	@Test
	public void calculateBounds_in_2_dimensions_4_partitions() {
		EquiAnglePartitionStrategy s = new EquiAnglePartitionStrategy(2, 4);
		s.calculateBounds();
		
		// Verify number of partitions
		assertEquals(4, s.partitionCount);
		assertEquals(4, s.boundsLow[0].length);
		
		// Verify bounds
		assertEquals(0.0, s.boundsLow[0][0]);
		assertEquals(angleMax/4, s.boundsHigh[0][0]);
		assertEquals(angleMax/4, s.boundsLow[0][1]);
		assertEquals(angleMax/2, s.boundsHigh[0][1]);
		assertEquals(angleMax/2, s.boundsLow[0][2]);
		assertEquals(angleMax*3/4, s.boundsHigh[0][2]);
		assertEquals(angleMax*3/4, s.boundsLow[0][3]);
		assertEquals(angleMax, s.boundsHigh[0][3]);
		
		// Verify volumes
		assertEquals(s.volumeTotal/4, s.calculateVolume(new double[]{s.boundsLow[0][0]}, new double[]{s.boundsHigh[0][0]}), EquiAnglePartitionStrategy.THRESHOLD*s.volumeTotal);
		assertEquals(s.volumeTotal/4, s.calculateVolume(new double[]{s.boundsLow[0][1]}, new double[]{s.boundsHigh[0][1]}), EquiAnglePartitionStrategy.THRESHOLD*s.volumeTotal);
		assertEquals(s.volumeTotal/4, s.calculateVolume(new double[]{s.boundsLow[0][2]}, new double[]{s.boundsHigh[0][2]}), EquiAnglePartitionStrategy.THRESHOLD*s.volumeTotal);
		assertEquals(s.volumeTotal/4, s.calculateVolume(new double[]{s.boundsLow[0][3]}, new double[]{s.boundsHigh[0][3]}), EquiAnglePartitionStrategy.THRESHOLD*s.volumeTotal);
	}
	
	@Test
	public void calculateBounds_in_3_dimensions_1_partitions() {
		
		EquiAnglePartitionStrategy s = new EquiAnglePartitionStrategy(3, 1);
		s.calculateBounds();
		
		// Verify number of partitions
		assertEquals(1, s.partitionCount);
		assertEquals(1, s.boundsLow[0].length);
		
		// Verify bounds
		assertEquals(0.0, s.boundsLow[0][0]);
		assertEquals(angleMax, s.boundsHigh[0][0]);
		assertEquals(0.0, s.boundsLow[1][0]);
		assertEquals(angleMax, s.boundsHigh[1][0]);
		
		// Verify volumes
		assertEquals(s.volumeTotal, s.calculateVolume(
			new double[]{s.boundsLow[0][0], s.boundsLow[1][0]}, 
			new double[]{s.boundsHigh[0][0], s.boundsHigh[1][0]}), EquiAnglePartitionStrategy.THRESHOLD*s.volumeTotal);
	}
	
	@Test
	public void calculateBounds_in_3_dimensions_9_partitions() {
		
		EquiAnglePartitionStrategy s = new EquiAnglePartitionStrategy(3, 9);
		s.calculateBounds();
		
		// Verify number of partitions
		assertEquals(9, s.partitionCount);
		assertEquals(3, s.boundsLow[0].length);
		assertEquals(3, s.boundsLow[1].length);
		
		// Verify volumes
		for (int i = 0; i < 3; ++i) {
			for (int j = 0; j < 3; ++j) {
				assertEquals(s.volumeTotal/9, s.calculateVolume(
					new double[]{s.boundsLow[0][i], s.boundsLow[1][j]}, 
					new double[]{s.boundsHigh[0][i], s.boundsHigh[1][j]}),
					EquiAnglePartitionStrategy.THRESHOLD*s.volumeTotal
				);
			}
		}
	}
	
	@Test
	public void testCalculate_correct_angular_coordinates() {
		EquiAnglePartitionStrategy a = new EquiAnglePartitionStrategy(3, 9);

		double[] expected = {Math.atan(0.33)};
		float[] actual = a.toAngular(new float[]{0.75f, 0.25f});
		assertEquals(expected[0], actual[0]);
		
		expected = new double[]{Math.atan(1.25), Math.atan(2.5)};
		actual = a.toAngular(new float[]{0.43f, 0.2f, 0.5f});
		assertEquals(expected[0], actual[0]);
		assertEquals(expected[1], actual[1]);
	}
	
	@Test
	public void mapPointToPartition_2_dimensions() {
		EquiAnglePartitionStrategy a = new EquiAnglePartitionStrategy(2, 2);
		
		// Number of partitions
		assertEquals(1, a.boundsHigh.length); // one angle
		assertEquals(2, a.partitionCount);
		assertEquals(2, a.boundsHigh[0].length); // split angle
		
		// Boundaries
		assertEquals(0.0, a.boundsLow[0][0]);
		assertEquals(angleMax/2, a.boundsHigh[0][0]);
		assertEquals(angleMax/2, a.boundsLow[0][1]);
		assertEquals(angleMax, a.boundsHigh[0][1]);
		
		float[] p1 = new float[]{75f, 10f};
		float[] p2 = new float[]{25f, 75f};
		assertTrue(a.boundsLow[0][0] <= a.toAngular(p1)[0]);
		assertTrue("the value " + a.toAngular(p1)[0], a.toAngular(p1)[0] <= a.boundsHigh[0][0]);
		
		// Mapping
		int x = a.mapPointToPartition(p1);
		int y = a.mapPointToPartition(p2);
		assertNotEquals(x,y);
		assertEquals(0, x);
		assertEquals(1, y);
	}
	
	public void assertEquals(double expected, double actual) {
		assertEquals(expected, actual, 0.01);
	}
	
	public void assertNotEquals(double a, double b) {
		assertNotEquals(a, b, 0.01);
	}
	
	public void assertEquals(double expected, double actual, double precision) {
		assertTrue("Expected " + expected + " but was " + actual, expected - precision <= actual && actual <= expected + precision);
	}
	
	public void assertNotEquals(double a, double b, double precision) {
		assertTrue("Expected value not to be equal to " + b, !(a - precision <= b && b <= a + precision));
	}
}
