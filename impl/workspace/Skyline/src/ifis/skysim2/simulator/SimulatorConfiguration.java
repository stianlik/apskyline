package ifis.skysim2.simulator;

import ifis.skysim2.algorithms.SkylineAlgorithm;
import ifis.skysim2.algorithms.SkylineAlgorithmBNL;
import ifis.skysim2.data.generator.DataGenerator;
import java.io.File;
import java.util.Arrays;

public class SimulatorConfiguration {
    // number of dimensions
    private int d = -1;

    // number of data points
    private int n = -1;

    // number of levels per attribute
    private int[] numLevels = null;
    private int numLevelsAll = -1;

    // data generator
    private DataGenerator dataGenerator = null;
    private boolean useDefaultGeneratorSeed = false;

    // data source
    private DataSource dataSource = null;

    // file-based data
    private File dataFile = null;
    private int bytesPerRecord = -1;
    private boolean generateDataFile = false;

    // presorting
    private PresortStrategy presortStrategy = PresortStrategy.NoPresort;
    private double partialPresortThreshold = Double.NaN;

    // trials
    private int numTrials = -1;

    // algorithm
    private SkylineAlgorithm skylineAlgorithm = null;

    // algorithm-specific stuff
    private SkylineAlgorithmBNL.BNLWindowPolicy bnlWindowPolicy = SkylineAlgorithmBNL.BNLWindowPolicy.SimpleAppend;

    // tree-based algorithms
    private int nodeCapacityMin = 50;
    private int nodeCapacityMax = 100;

    // parallel algorithms
    private int numberOfCPUs = 1;

    // distributed algorithms
    private int distributedNumBlocks = -1;

    // static array windows
    private int staticArrayWindowSize = -1;

    // distributedParBNL
    private boolean deleteDuringCleaning = true;


    public void setD(int d) {
	if (d <= 0) {
	    throw new IllegalArgumentException();
	}
	this.d = d;
	updateNumLevels();
    }

    public int getD() {
	return d;
    }


    public void setN(int n) {
	if (n <= 0) {
	    throw new IllegalArgumentException();
	}
	this.n = n;
    }

    public int getN() {
	return n;
    }


    public void setNumLevels(int[] numLevels) {
	if ((numLevels != null) && (numLevels.length != d)) {
	    throw new IllegalArgumentException();
	}
	this.numLevels = numLevels;
    }
    
    public void setNumLevels(int numLevelsAll) {
	if (numLevelsAll <= 0) {
	    throw new IllegalArgumentException();
	}
	this.numLevelsAll = numLevelsAll;
	updateNumLevels();
    }

    private void updateNumLevels() {
	if (numLevelsAll > 0) {
	    this.numLevels = new int[d];
	    Arrays.fill(this.numLevels, numLevelsAll);
	} else if ((numLevels != null) && (numLevels.length != d)) {
	    throw new IllegalArgumentException();
	}
    }

    public int[] getNumLevels() {
	if (numLevels != null) {
	    return Arrays.copyOf(numLevels, d);
	} else {
	    return numLevels;
	}
    }


    public void setDataGenerator(DataGenerator dataGenerator) {
	if (dataGenerator == null) {
	    throw new IllegalArgumentException();
	}
	this.dataGenerator = dataGenerator;
    }

    public DataGenerator getDataGenerator() {
	return dataGenerator;
    }


    public boolean isUseDefaultGeneratorSeed() {
	return useDefaultGeneratorSeed;
    }

    public void setUseDefaultGeneratorSeed(boolean useDefaultGeneratorSeed) {
	this.useDefaultGeneratorSeed = useDefaultGeneratorSeed;
    }


    public DataSource getDataSource() {
	return dataSource;
    }

    public void setDataSource(DataSource dataSource) {
	if (dataSource == null) {
	    throw new IllegalArgumentException();
	}
	this.dataSource = dataSource;
    }

    public int getNumTrials() {
	return numTrials;
    }

    public void setNumTrials(int numTrials) {
	if (numTrials <= 0) {
	    throw new IllegalArgumentException();
	}
	this.numTrials = numTrials;
    }

    public SkylineAlgorithm getSkylineAlgorithm() {
	return skylineAlgorithm;
    }

    public void setSkylineAlgorithm(SkylineAlgorithm skylineAlgorithm) {
		if (skylineAlgorithm == null) {
		    throw new IllegalArgumentException();
		}
		this.skylineAlgorithm = skylineAlgorithm;
		skylineAlgorithm.setExperimentConfig(this);
    }

    public File getDataFile() {
	return dataFile;
    }

    public void setDataFile(File dataFile) {
	if (dataFile == null) {
	    throw new IllegalArgumentException();
	}
	this.dataFile = dataFile;
    }

    public int getBytesPerRecord() {
	return bytesPerRecord;
    }

    public void setBytesPerRecord(int bytesPerRecord) {
	if (bytesPerRecord <= 0) {
	    throw new IllegalArgumentException();
	}
	this.bytesPerRecord = bytesPerRecord;
    }

    public boolean isGenerateDataFile() {
	return generateDataFile;
    }

    public void setGenerateDataFile(boolean generateDataFile) {
	this.generateDataFile = generateDataFile;
    }

    public double getPartialPresortThreshold() {
	return partialPresortThreshold;
    }

    public void setPartialPresortThreshold(double partialPresortThreshold) {
	if ((partialPresortThreshold < 0) || (partialPresortThreshold > 1)) {
	    throw new IllegalArgumentException();
	}
	this.partialPresortThreshold = partialPresortThreshold;
    }

    public void setNodeCapacities(int nodeCapacityMin, int nodeCapacityMax) {
	if ((nodeCapacityMin <= 0) || (nodeCapacityMax < nodeCapacityMin)) {
	    throw new IllegalArgumentException();
	}
	this.nodeCapacityMin = nodeCapacityMin;
	this.nodeCapacityMax = nodeCapacityMax;
    }

    public int getNodeCapacityMin() {
	return nodeCapacityMin;
    }

    public int getNodeCapacityMax() {
	return nodeCapacityMax;
    }

    public int getNumberOfCPUs() {
	return numberOfCPUs;
    }

    public void setNumberOfCPUs(int numberOfCPUs) {
	if (numberOfCPUs < 1) {
	    throw new IllegalArgumentException();
	}
	this.numberOfCPUs = numberOfCPUs;
    }

    public SkylineAlgorithmBNL.BNLWindowPolicy getBnlWindowPolicy() {
	return bnlWindowPolicy;
    }

    public void setBnlWindowPolicy(SkylineAlgorithmBNL.BNLWindowPolicy bnlWindowPolicy) {
	this.bnlWindowPolicy = bnlWindowPolicy;
    }

    public PresortStrategy getPresortStrategy() {
	return presortStrategy;
    }

    public void setPresortStrategy(PresortStrategy presortStrategy) {
	this.presortStrategy = presortStrategy;
    }

    public int getDistributedNumBlocks() {
	return distributedNumBlocks;
    }

    public void setDistributedNumBlocks(int distributedNumBlocks) {
	this.distributedNumBlocks = distributedNumBlocks;
    }

    public int getStaticArrayWindowSize() {
	return staticArrayWindowSize;
    }

    public void setStaticArrayWindowSize(int staticArrayWindowSize) {
	this.staticArrayWindowSize = staticArrayWindowSize;
    }

    public boolean isDeleteDuringCleaning() {
	return deleteDuringCleaning;
    }

    public void setDeleteDuringCleaning(boolean deleteDuringCleaning) {
	this.deleteDuringCleaning = deleteDuringCleaning;
    }

    public static enum PresortStrategy {
	NoPresort,
	FullPresort,
	PartialPresort
    }
}