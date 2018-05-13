/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ifis.skysim2;

import ifis.skysim2.algorithms.SkylineAlgorithm;
import ifis.skysim2.algorithms.SkylineAlgorithmPskyline;
import ifis.skysim2.algorithms.parallelbnl.SkylineAlgorithmDistributedParallelBNL;
import ifis.skysim2.algorithms.parallelbnl.SkylineAlgorithmParallelBNLLinkedListFineGrainedSync;
import ifis.skysim2.algorithms.parallelbnl.SkylineAlgorithmParallelBNLLinkedListLazySync;
import ifis.skysim2.algorithms.parallelbnl.SkylineAlgorithmParallelBNLLinkedListLockFreeSync;
import ifis.skysim2.algorithms.parallelbnl.SkylineAlgorithmParallelBNLStaticArray;
import ifis.skysim2.data.generator.DataGenerator;
import ifis.skysim2.data.generator.DataGeneratorBKS01Anticorrelated;
import ifis.skysim2.data.generator.DataGeneratorBKS01Correlated;
import ifis.skysim2.data.generator.DataGeneratorIndependent;
import ifis.skysim2.junk.SkylineAlgorithmParallelScanner;
import ifis.skysim2.simulator.AlgorithmSummaryStatistics;
import ifis.skysim2.simulator.DataSource;
import ifis.skysim2.simulator.Simulator;
import ifis.skysim2.simulator.SimulatorConfiguration;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Paramtrization: Algorithm, Distribution, sourcesize, dimensions
 * @author Christoph
 */
public class ICDE_1_ParSynth_Param {

    private static int numOfTrials = 15;
    private static final SkylineAlgorithm[] algorithm = {
        new SkylineAlgorithmParallelBNLLinkedListLockFreeSync(),
        new SkylineAlgorithmParallelBNLLinkedListLazySync(),
        new SkylineAlgorithmParallelBNLLinkedListFineGrainedSync(),
        new SkylineAlgorithmDistributedParallelBNL(),
        new SkylineAlgorithmPskyline(),
        new SkylineAlgorithmParallelBNLStaticArray(),
        new SkylineAlgorithmParallelScanner()};
    private static final DataGenerator[] generator = {
        new DataGeneratorIndependent(),
        new DataGeneratorBKS01Correlated(),
        new DataGeneratorBKS01Anticorrelated()
    };
    //private static final int[] sourceSize = {100000};
    //private static final int[] numOfCores = {1, 2};
    //private static final int[] dimensions = {7};
    private static Writer out;
    private static double pSkyScaleFactor = 1.5;
    private static String outfilename = "1_ParSynth";
    private static HashMap<Integer, int[]> skysizes = new HashMap<Integer, int[]>();

    private static void runExperiments(SimulatorConfiguration config, Simulator sim, int dim, int nSize, int minCore, int maxCore) throws IOException {
        // ALGORITHM
        if (config.getSkylineAlgorithm() instanceof SkylineAlgorithmPskyline) {
            out.write("ScaleFactor " + pSkyScaleFactor + "   Skysize " + skysizes.get(nSize)[dim - 1] + "\n");
        }
        out.write(AlgorithmSummaryStatistics.getDivider() + "\n");
        out.write(AlgorithmSummaryStatistics.getHeader() + "\n");

        // DIMENSIONS

        config.setD(dim);



        // SIZE

        config.setN(nSize);
        out.write(AlgorithmSummaryStatistics.getDivider() + "\n");
        // CORES
        for (int i = minCore; i <=
                maxCore; i++) {
            config.setNumberOfCPUs(i);
            // psky blocksizes
            if (config.getSkylineAlgorithm() instanceof SkylineAlgorithmPskyline) {
                int numBlock = Math.max(i, (int) Math.ceil(i * nSize / (pSkyScaleFactor * skysizes.get(nSize)[dim - 1])));
                config.setDistributedNumBlocks(numBlock);
                System.out.println("ScaleFactor " + pSkyScaleFactor + "   Skysize " + skysizes.get(nSize)[dim - 1]);
                System.out.println("Num of Blocks " + numBlock);
            }
            // distributed blocksize
            if (config.getSkylineAlgorithm() instanceof SkylineAlgorithmDistributedParallelBNL) {
                config.setDistributedNumBlocks(i);
                System.out.println("Num of Blocks " + i);
            }

            AlgorithmSummaryStatistics summary = sim.run(config);
            out.write(summary.toString() + "\n");
            out.flush();
        }

    }

    public static void main(String[] args) {
        try {
            System.out.println("Command Line Arguments : " + Arrays.toString(args));
            //
            setSkySizes();
            SimulatorConfiguration config = new SimulatorConfiguration();
            config.setDataSource(DataSource.MEMORY);
            config.setUseDefaultGeneratorSeed(true);
            config.setDataGenerator(new DataGeneratorIndependent());

            config.setDeleteDuringCleaning(false);
            Simulator sim = new Simulator();

            // algorithm param
            int algNum = 0;
            try {
                algNum = Integer.parseInt(args[0]);
            } catch (RuntimeException ex) {
                Logger.getLogger(ICDE_1_ParSynth_Param.class.getName()).log(Level.SEVERE, "Invalid algorithm parameter");
            }
            SkylineAlgorithm alg = algorithm[algNum];
            System.out.println("Using " + alg.getShortName());
            config.setSkylineAlgorithm(alg);

            // generator param
            int genNum = 0;
            try {
                genNum = Integer.parseInt(args[1]);
            } catch (RuntimeException ex) {
                Logger.getLogger(ICDE_1_ParSynth_Param.class.getName()).log(Level.SEVERE, "Invalid generator parameter");
            }
            DataGenerator gen = generator[genNum];
            System.out.println("Using " + gen.getShortName());
            int dim = Integer.parseInt(args[2]);
            int nSize = Integer.parseInt(args[3]);
            int minCore = Integer.parseInt(args[4]);
            int maxCore = Integer.parseInt(args[5]);
            try {
                numOfTrials = Integer.parseInt(args[6]);
            } catch (RuntimeException ex) {
                Logger.getLogger(ICDE_1_ParSynth_Param.class.getName()).log(Level.SEVERE, "Invalid numOfTrials parameter");
            }
            config.setNumTrials(numOfTrials);
            try {
                pSkyScaleFactor = Double.parseDouble(args[7]);
            } catch (RuntimeException ex) {
                Logger.getLogger(ICDE_1_ParSynth_Param.class.getName()).log(Level.SEVERE, "Invalid generator parameter");
            }

            config.setDataGenerator(gen);
            config.setStaticArrayWindowSize(40000);

            //
            File outFile = ExperimentTools.getIncrementedOutFile(outfilename);
            System.out.println("Writing to result file " + outFile);
            out =
                    new FileWriter(outFile);
            out.write("\nAlgorithm: " + alg + "    Data Generator: " + gen.getShortName() + "\n");

            runExperiments(config, sim, dim, nSize, minCore, maxCore);
            out.write(AlgorithmSummaryStatistics.getDivider() + "\n");
            out.write(AlgorithmSummaryStatistics.getHeader() + "\n");
            out.close();
            ExperimentTools.sendResultViaMail(outFile, "lofi@ifis.cs.tu-bs.de", true, true);
        } catch (Exception ex) {
            Logger.getLogger(ICDE_1_ParSynth_Param.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static void setSkySizes() {
        int[] ss100k = {1, 12, 74, 305, 956, 2432, 5239, 9845, 16501, 25113};
        int[] ss1M = {1, 14, 104, 509, 1880, 5606, 14087, 30701, 59308, 103300};
        int[] ss10M = {1, 17, 140, 790, 3359, 11514, 33145, 82479, 181249};
        skysizes.put(100000, ss100k);
        skysizes.put(1000000, ss1M);
        skysizes.put(10000000, ss10M);
    }
}
