package no.ntnu.skyline;

import ifis.skysim2.algorithms.AbstractSkylineAlgorithm;
import ifis.skysim2.simulator.SimulatorConfiguration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ExperimentReport {
	SimulatorConfiguration config;
    List<Double> timeTotal = new ArrayList<Double>();
    List<Double> timePartitioning = new ArrayList<Double>();
    List<Double> timeSampling = new ArrayList<Double>();
    List<Double> timeLocalSkyline = new ArrayList<Double>();
    List<Double> timeMerge = new ArrayList<Double>();
    List<Integer> skylineSize = new ArrayList<Integer>();
    
    String name;
    
    public ExperimentReport(String name, SimulatorConfiguration config) {
    	this.name = name;
    	this.config = config;
    }
    
	public void addResult(List<float[]> skyline) {
		timeTotal.add((double) config.getSkylineAlgorithm().getTotalTimeNS() / 1000000000);
		timePartitioning.add(((AbstractSkylineAlgorithm)config.getSkylineAlgorithm()).getEllapsedTimeInSeconds("partitioning"));
		timeSampling.add(((AbstractSkylineAlgorithm)config.getSkylineAlgorithm()).getEllapsedTimeInSeconds("sampling"));
		timeLocalSkyline.add(((AbstractSkylineAlgorithm)config.getSkylineAlgorithm()).getEllapsedTimeInSeconds("local skylines"));
		timeMerge.add(((AbstractSkylineAlgorithm)config.getSkylineAlgorithm()).getEllapsedTimeInSeconds("merge"));
		skylineSize.add(skyline.size());
	}
    
	public void summary() {
		String timestr = Arrays.toString(timeTotal.toArray()).replaceAll("[\\[\\],]", "");
        String partitioningstr = Arrays.toString(timePartitioning.toArray()).replaceAll("[\\[\\],]", "");
        String samplingstr = listToString(timeSampling);
        String localskylinestr = Arrays.toString(timeLocalSkyline.toArray()).replaceAll("[\\[\\],]", "");
        String mergestr = Arrays.toString(timeMerge.toArray()).replaceAll("[\\[\\],]", "");
        String skylinestr = Arrays.toString(skylineSize.toArray()).replaceAll("[\\[\\],]", "");
        
    	System.out.format("# name: test\n# type: scalar struct\n# length: %d\n\n", 8);
    	System.out.format("# name: name\n# type: string\n# elements: 1\n# length: %d\n%s\n\n", name.length(), name);
    	System.out.format("# name: value\n# type: scalar\n%d\n\n", config.getNumberOfCPUs());
    	
    	// Arrays
    	System.out.format("# name: result\n# type: matrix\n# rows: 1\n# columns %d\n%s\n\n", config.getNumTrials(), timestr);
    	System.out.format("# name: partitioning\n# type: matrix\n# rows: 1\n# columns %d\n%s\n\n", config.getNumTrials(), partitioningstr);
    	System.out.format("# name: local_skylines\n# type: matrix\n# rows: 1\n# columns %d\n%s\n\n", config.getNumTrials(), localskylinestr);
    	System.out.format("# name: sampling\n# type: matrix\n# rows: 1\n# columns %d\n%s\n\n", config.getNumTrials(), samplingstr);
    	System.out.format("# name: merge\n# type: matrix\n# rows: 1\n# columns %d\n%s\n\n", config.getNumTrials(), mergestr);
    	System.out.format("# name: skyline_size\n# type: matrix\n# rows: 1\n# columns %d\n%s\n\n", config.getNumTrials(), skylinestr);
	}
	
	public static String listToString(List list) {
		return Arrays.toString(list.toArray()).replaceAll("[\\[\\],]", "");
	}
	
}
