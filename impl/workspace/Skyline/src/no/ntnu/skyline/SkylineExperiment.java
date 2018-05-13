package no.ntnu.skyline;

import ifis.skysim2.algorithms.SkylineAlgorithm;
import ifis.skysim2.data.sources.PointSource;
import ifis.skysim2.data.sources.PointSourceRAM;
import ifis.skysim2.simulator.SimulatorConfiguration;

import java.util.Arrays;
import java.util.List;

public class SkylineExperiment {
	
	protected SimulatorConfiguration config;
	
	public SkylineExperiment() {
		config = new SimulatorConfiguration();
	}
	
	protected void run(String name, SkylineAlgorithm algorithm) {
		ExperimentReport report = new ExperimentReport(name, config);
        PointSource source = generateSource(config);
        execAlgorithm(algorithm, source, -1);
        for (int i = 0; i < config.getNumTrials(); ++i) {
        	report.addResult(execAlgorithm(algorithm, source, i));
        }
        report.summary();
	}

	protected List<float[]> execAlgorithm(SkylineAlgorithm algorithm, PointSource source, int i) {
		SkylineAlgorithm correctAlgorithm = new ParallelBNLAlgorithm();
		config.setSkylineAlgorithm(correctAlgorithm);
		List<float[]> expected = config.getSkylineAlgorithm().compute(source);
		config.setSkylineAlgorithm(algorithm);
		List<float[]> actual = config.getSkylineAlgorithm().compute(source);
		assertSkylineEquivallent("Trial number " + i, expected, actual);
		return actual;
	}

	protected PointSourceRAM generateSource(SimulatorConfiguration config) {
		return new PointSourceRAM(config.getD(), config.getDataGenerator().generate(config.getD(), config.getN()));
	}

	protected static void verifyInput(String[] args) {
		if (args.length < 2) {
        	System.err.println("USAGE: CMD ALGORITHM_NAME THREAD_COUNT");
        	System.exit(1);
        }
	}

	protected void assertSkylineEquivallent(String message, List<float[]> expected, List<float[]> actual) {
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
			if (!match) {
				throw new RuntimeException(message + ": expected point " + p.toString() + " not found in result set");
			}
		}
	}

	private void assertEquals(String message, int expected, int actual) {
		if (expected != actual) {
			throw new RuntimeException(message + ": expected " + expected + " but was " + actual);
		}
	}
}
