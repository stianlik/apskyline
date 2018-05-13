package ifis.skysim2;

import ifis.skysim2.data.generator.DataGeneratorIndependent;
import ifis.skysim2.junk.SkylineAlgorithmParallelScanner;
import ifis.skysim2.simulator.DataSource;
import ifis.skysim2.simulator.Simulator;
import ifis.skysim2.simulator.SimulatorConfiguration;

public class ICDEExperiment2 {
    public static void main(String[] args) {
	Simulator sim = new Simulator();
        SimulatorConfiguration config = new SimulatorConfiguration();

	config.setDataSource(DataSource.MEMORY);
	config.setUseDefaultGeneratorSeed(true);

	config.setDataGenerator(new DataGeneratorIndependent());
//	config.setDataGenerator(new DataGeneratorBKS01Correlated());
//	config.setDataGenerator(new DataGeneratorBKS01Anticorrelated());

//	config.setD(6);
	config.setD(1);
	config.setN(10000000);

	config.setNumTrials(3);

//	config.setSkylineAlgorithm(new SkylineAlgorithmPskyline());
	config.setSkylineAlgorithm(new SkylineAlgorithmParallelScanner());
//	config.setSkylineAlgorithm(new SkylineAlgorithmParallelBNLStaticArray());
//	config.setSkylineAlgorithm(new SkylineAlgorithmParallelBNLLinkedListFineGrainedSync());
//	config.setSkylineAlgorithm(new SkylineAlgorithmParallelBNLLinkedListLazySync());
//	config.setSkylineAlgorithm(new SkylineAlgorithmParallelBNLLinkedListLockFreeSync());
//	config.setSkylineAlgorithm(new SkylineAlgorithmDistributedParallelBNLLazy());

	config.setStaticArrayWindowSize(20000);
	config.setDeleteDuringCleaning(false);

	int[] threads = {1, 2, 7, 8};

	for (int thread: threads) {
	    config.setNumberOfCPUs(thread);
//	    config.setDistributedNumBlocks(thread);
//	    config.setDistributedNumBlocks(167 * thread);
//	    config.setDistributedNumBlocks((int)(Math.round(167 / 1.5) * thread));

	    sim.run(config);
	}
    }
}