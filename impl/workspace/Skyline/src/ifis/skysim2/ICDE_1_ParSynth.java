/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ifis.skysim2;

import ifis.skysim2.algorithms.SkylineAlgorithm;
import ifis.skysim2.algorithms.parallelbnl.SkylineAlgorithmDistributedParallelBNL;
import ifis.skysim2.algorithms.parallelbnl.SkylineAlgorithmParallelBNLLinkedListFineGrainedSync;
import ifis.skysim2.algorithms.parallelbnl.SkylineAlgorithmParallelBNLLinkedListLazySync;
import ifis.skysim2.algorithms.parallelbnl.SkylineAlgorithmParallelBNLLinkedListLockFreeSync;
import ifis.skysim2.data.generator.DataGenerator;
import ifis.skysim2.data.generator.DataGeneratorBKS01Anticorrelated;
import ifis.skysim2.data.generator.DataGeneratorBKS01Correlated;
import ifis.skysim2.data.generator.DataGeneratorIndependent;
import ifis.skysim2.simulator.AlgorithmSummaryStatistics;
import ifis.skysim2.simulator.DataSource;
import ifis.skysim2.simulator.Simulator;
import ifis.skysim2.simulator.SimulatorConfiguration;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Christoph
 */
public class ICDE_1_ParSynth {

    private static final int numOfTrials = 10;
    private static final SkylineAlgorithm[] algorithm = {
        new SkylineAlgorithmParallelBNLLinkedListLockFreeSync(),
        new SkylineAlgorithmParallelBNLLinkedListLazySync(),
        new SkylineAlgorithmParallelBNLLinkedListFineGrainedSync(),
        new SkylineAlgorithmDistributedParallelBNL()};
    private static final DataGenerator[] generator = {
        new DataGeneratorIndependent(),
        new DataGeneratorBKS01Correlated(),
        new DataGeneratorBKS01Anticorrelated()
    };
    private static final int[] sourceSize = {1000000};
    private static final int[] dimensions = {10};
    private static final int[] numOfCores = { 1, 2, 3 };
    //private static final int[] sourceSize = {100000};
    //private static final int[] numOfCores = {1, 2};
    //private static final int[] dimensions = {7};
    private static Writer out;
    private static String outfilename = "1_ParSynth";

    private static void runExperiments(SimulatorConfiguration config, Simulator sim) throws IOException {
        // ALGORITHM

        out.write(AlgorithmSummaryStatistics.getDivider() + "\n");
        out.write(AlgorithmSummaryStatistics.getHeader() + "\n");

        // DIMENSIONS
        for (int dim : dimensions) {
            config.setD(dim);

            // SIZE
            for (int sSize : sourceSize) {
                config.setN(sSize);
                out.write(AlgorithmSummaryStatistics.getDivider() + "\n");
                // CORES
                for (int nCores : numOfCores) {
                    config.setNumberOfCPUs(nCores);
                    AlgorithmSummaryStatistics summary = sim.run(config);
                    out.write(summary.toString() + "\n");
                    out.flush();
                }

            }
        }

    }

    public static void main(String[] args) {
        try {
            System.out.println("Command Line Arguments : " + Arrays.toString(args));
            //
            SimulatorConfiguration config = new SimulatorConfiguration();
            config.setDataSource(DataSource.MEMORY);
            config.setUseDefaultGeneratorSeed(true);
            config.setDataGenerator(new DataGeneratorIndependent());
            config.setNumTrials(numOfTrials);
            config.setDeleteDuringCleaning(true);
            Simulator sim = new Simulator();

            // algorithm param
            int algNum = 0;
            try {
                algNum = Integer.parseInt(args[0]);
            } catch (RuntimeException ex) {
                Logger.getLogger(ICDE_1_ParSynth.class.getName()).log(Level.SEVERE, "Invalid algorithm parameter");
            }
            SkylineAlgorithm alg = algorithm[algNum];
            System.out.println("Using " + alg.getShortName());
            config.setSkylineAlgorithm(alg);

            // generator param
            int genNum = 0;
            try {
                genNum = Integer.parseInt(args[1]);
            } catch (RuntimeException ex) {
                Logger.getLogger(ICDE_1_ParSynth.class.getName()).log(Level.SEVERE, "Invalid generator parameter");
            }
            DataGenerator gen = generator[genNum];
            System.out.println("Using " + gen.getShortName());
            config.setDataGenerator(gen);

            //
            File outFile = ExperimentTools.getIncrementedOutFile(outfilename);
            out = new FileWriter(outFile);
            out.write("\nAlgorithm: " + alg + "    Data Generator: " + gen.getShortName() + "\n");

            runExperiments(config, sim);
            out.write(AlgorithmSummaryStatistics.getDivider() + "\n");
            out.write(AlgorithmSummaryStatistics.getHeader() + "\n");
            out.close();
            ExperimentTools.sendResultViaMail(outFile, "lofi@ifis.cs.tu-bs.de", true, true);
        } catch (Exception ex) {
            Logger.getLogger(ICDE_1_ParSynth.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
