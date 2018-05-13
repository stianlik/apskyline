package no.ntnu.skyline;

import ifis.skysim2.data.sources.PointSource;
import ifis.skysim2.data.sources.PointSourceRAM;
import ifis.skysim2.simulator.DataSource;
import ifis.skysim2.simulator.SimulatorConfiguration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class Experiment1 {
	public static void main(String args[]) {
        
        // Input arguments..
        if (args.length < 1) {
        	System.err.println("USAGE: CMD THREAD_COUNT");
        	System.exit(1);
        }
        
        // Configure experiment
        // TODO Can remove some choices here as the Simulator is not
        // in use
        SimulatorConfiguration config = new SimulatorConfiguration();
        config.setSkylineAlgorithm(new ParallelBNLAlgorithm());
        config.setNumTrials(10);
        config.setNumberOfCPUs(Integer.parseInt(args[0]));
    	config.setDistributedNumBlocks(config.getNumberOfCPUs());
        config.setDataSource(DataSource.MEMORY);
        config.setUseDefaultGeneratorSeed(true);
        config.setDataGenerator(new FileDataGenerator("../../data/10d_100k_uniform/data.txt"));
        config.setD(10);
		config.setN(100000); // 100k
        config.getSkylineAlgorithm().setExperimentConfig(config);
        PointSource source = new PointSourceRAM(config.getD(), config.getDataGenerator().generate(config.getD(), config.getN()));
        
        // Quick and dirty algorithm verification
        assertAlgorithmCorrect(config);
        
        // Perform experiment
        List<Float> time = new ArrayList<Float>();
        List<Integer> skylineSize = new ArrayList<Integer>();
        for (int i = -1; i < config.getNumTrials(); ++i) {
	        List<float[]> skyline = config.getSkylineAlgorithm().compute(source);
	        time.add((float) config.getSkylineAlgorithm().getTotalTimeNS() / 1000000000);
	        skylineSize.add(skyline.size());
        }
        
        // Statistics
        String name = "Java";
        String timestr = Arrays.toString(time.toArray()).replaceAll("[\\[\\],]", "");
        String skylinestr = Arrays.toString(skylineSize.toArray()).replaceAll("[\\[\\],]", "");
    	System.out.format("# name: test\n# type: scalar struct\n# length: 3\n\n");
    	System.out.format("# name: name\n# type: string\n# elements: 1\n# length: %d\n%s\n\n", name.length(), name);
    	System.out.format("# name: value\n# type: scalar\n%d\n\n", config.getNumberOfCPUs());
    	System.out.format("# name: result\n# type: matrix\n# rows: 1\n# columns %d\n%s\n\n", config.getNumTrials(), timestr);
    	System.out.format("# name: skyline_size\n# type: matrix\n# rows: 1\n# columns %d\n%s\n\n", config.getNumTrials(), skylinestr);
	}
	
	private static void assertAlgorithmCorrect(SimulatorConfiguration config) {
		List<float[]> expected = new LinkedList<float[]>();
		float[] input = {
			50f, 3f, 2f, // dominated by s1
			51f, 5f, 1f, // dominated by s1
			51f, 5f, 2f, // s1
			52f, 4f, 2f, // dominated by s2
			53f, 4f, 2f, // s2
			50f, 1f, 3f, // s3
			51f, 3f, 2f  // dominated by s1
		};
		expected.add(new float[]{51f, 5f, 2f});
		expected.add(new float[]{53f, 4f, 2f});
		expected.add(new float[]{50f, 1f, 3f});
		PointSource source = new PointSourceRAM(3, input);
		config.getSkylineAlgorithm().setExperimentConfig(config);
		List<float[]> result = config.getSkylineAlgorithm().compute(source);
		assertEquals(expected, result);
	}
	
	private static void assertEquals(List<float[]> expected, List<float[]> actual) {
		assertEquals(expected.size(), actual.size());
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
				throw new RuntimeException("Expected point to have match in result, but was not found");
			}
		}
	}

	public static void assertEquals(int expected, int actual) {
		if (expected != actual) {
			throw new RuntimeException("Expected " + expected + " but was " + actual);
		}
	}
}
