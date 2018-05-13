package no.ntnu.skyline;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import ifis.skysim2.data.sources.PointSource;
import ifis.skysim2.data.sources.PointSourceRAM;
import ifis.skysim2.simulator.SimulatorConfiguration;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class APSkylineAlgorithmTest {
	SimulatorConfiguration config;
	
	@Before
	public void setUp() {
		config = new SimulatorConfiguration();
		config.setSkylineAlgorithm(new APSkylineAlgorithm());
		config.setNumberOfCPUs(1);
		config.setDistributedNumBlocks(config.getNumberOfCPUs());
	}

	@Test
	public void should_return_valid_skyline_for_simple_case() {
		
		List<float[]> expected = new LinkedList<float[]>();
		float[] input = {
			50f, 3f, 2f, // skyline
			51f, 5f, 1f, // skyline
			51f, 5f, 2f, // dominated by 0
			52f, 4f, 2f, // dominated by 0
			53f, 4f, 2f, // dominated by 0
			50f, 1f, 3f, // dominated by 6
			50f, 0f, 3f, // skyline
			51f, 1f, 2f  // skyline
		};
		
		expected.add(new float[]{50f, 3f, 2f});
		expected.add(new float[]{51f, 5f, 1f});
		expected.add(new float[]{50f, 0f, 3f});
		expected.add(new float[]{51f, 1f, 2f});
		
		PointSource source = new PointSourceRAM(3, input);
		testAlgorithm(expected, source, 1);
		testAlgorithm(expected, source, 2);
		testAlgorithm(expected, source, 4);
		testAlgorithm(expected, source, 6);
		testAlgorithm(expected, source, 8);
		testAlgorithm(expected, source, 12);
		testAlgorithm(expected, source, 16);
		testAlgorithm(expected, source, 32);
		testAlgorithm(expected, source, 64);
	}

	private void testAlgorithm(List<float[]> expected, PointSource source, int threadcount) {
		config.setNumberOfCPUs(threadcount);
		config.setSkylineAlgorithm(new PSkylineAlgorithm());
		config.getSkylineAlgorithm().setExperimentConfig(config);
		assertSkylineEquivallent(threadcount + " threads", expected, config.getSkylineAlgorithm().compute(source));
	}
	
	private void assertSkylineEquivallent(String message, List<float[]> expected, List<float[]> actual) {
		assertEquals("skyline size", expected.size(), actual.size());
		boolean match;
		for (float[] p : expected) {
			match = false;
			for (float[] q : actual) {
				if (Arrays.equals(p, q)) {
					match = true;
					break;
				}
			}
			assertTrue(message + ": expected point " + p.toString() + " not found in result set", match);
		}
	}
}