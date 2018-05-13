package ifis.skysim2;

import ifis.skysim2.algorithms.parallelbnl.SkylineAlgorithmDistributedParallelBNL;
import ifis.skysim2.data.generator.DataGeneratorIndependent;
import ifis.skysim2.simulator.DataSource;
import ifis.skysim2.simulator.Simulator;
import ifis.skysim2.simulator.SimulatorConfiguration;

public class MyExperiment {

    public static void main(String[] args) {
        SimulatorConfiguration config = new SimulatorConfiguration();

	config.setD(4);
	config.setN(10000000);

//	config.setNumLevels(8);
//	config.setNumLevels(100);
//	config.setNumLevels(1001);

	config.setDataSource(DataSource.MEMORY);
//	config.setDataSource(DataSource.FILE);
//	config.setDataFile(new File("/home/selke/Desktop/sky/data.db"));
//	config.setBytesPerRecord(100);
//	config.setGenerateDataFile(true);

	config.setUseDefaultGeneratorSeed(true);

	config.setDataGenerator(new DataGeneratorIndependent());
//	config.setDataGenerator(new DataGeneratorHollowUnitBall(1));
//	config.setDataGenerator(new DataGeneratorBKS01Correlated());
//	config.setDataGenerator(new DataGeneratorBKS01Anticorrelated());
//	config.setDataGenerator(new DataGeneratorCorrelatedUniform(0.53));

	config.setNumTrials(10);

//	config.setSkylineAlgorithm(new SkylineAlgorithmBNL());
//	config.setSkylineAlgorithm(new SkylineAlgorithmSingleLevelQuadTreeBNL());
//	config.setSkylineAlgorithm(new SkylineAlgorithmDistributed());
//	config.setSkylineAlgorithm(new SkylineAlgorithmPQueueSync());
//	config.setSkylineAlgorithm(new SkylineAlgorithmPPackageSync());
//	config.setSkylineAlgorithm(new SkylineAlgorithmBBS());
//	config.setSkylineAlgorithm(new SkylineAlgorithmSskyline());
//	config.setSkylineAlgorithm(new SkylineAlgorithmPskyline());
//	config.setSkylineAlgorithm(new SkylineAlgorithmDomFreeQuadTree());
//	config.setSkylineAlgorithm(new SkylineAlgorithmZSearch());
//	config.setSkylineAlgorithm(new SkylineAlgorithmDominanceDecisionTree());
//	config.setSkylineAlgorithm(new SkylineAlgorithmParallelBNLStaticArray());
//	config.setSkylineAlgorithm(new SkylineAlgorithmParallelBNLLinkedListFineGrainedSync());
//	config.setSkylineAlgorithm(new SkylineAlgorithmParallelBNLLinkedListOptimisticSync());
//	config.setSkylineAlgorithm(new SkylineAlgorithmParallelBNLLinkedListLazySync());
//	config.setSkylineAlgorithm(new SkylineAlgorithmParallelBNLLinkedListLockFreeSync());
	config.setSkylineAlgorithm(new SkylineAlgorithmDistributedParallelBNL());

//	config.setPresortStrategy(PresortStrategy.FullPresort);
//	config.setPresortStrategy(PresortStrategy.PartialPresort);
//	config.setPartialPresortThreshold(0.001);

//	config.setBnlWindowPolicy(BNLWindowPolicy.MoveToFront);
//	config.setBnlWindowPolicy(BNLWindowPolicy.KeepSorted);
//	config.setBnlWindowPolicy(BNLWindowPolicy.BubbleUp);
//	config.setBnlWindowPolicy(BNLWindowPolicy.BubbleUpSimple);

	config.setNodeCapacities(50, 100);

	config.setNumberOfCPUs(2);
	config.setDistributedNumBlocks(1);

	config.setStaticArrayWindowSize(20000);

	config.setDeleteDuringCleaning(false);

	Simulator sim = new Simulator();
        sim.run(config);
    }
}