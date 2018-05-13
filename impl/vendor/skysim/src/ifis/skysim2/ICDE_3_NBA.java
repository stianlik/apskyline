/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ifis.skysim2;

import ifis.skysim2.algorithms.SkylineAlgorithm;
import ifis.skysim2.algorithms.parallelbnl.SkylineAlgorithmParallelBNLLinkedListFineGrainedSync;
import ifis.skysim2.algorithms.parallelbnl.SkylineAlgorithmParallelBNLLinkedListLazySync;
import ifis.skysim2.algorithms.parallelbnl.SkylineAlgorithmParallelBNLLinkedListLockFreeSync;
import ifis.skysim2.data.generator.DataGeneratorIndependent;
import ifis.skysim2.data.generator.DumpFileGenerator;
import ifis.skysim2.simulator.AlgorithmSummaryStatistics;
import ifis.skysim2.simulator.DataSource;
import ifis.skysim2.simulator.Simulator;
import ifis.skysim2.simulator.SimulatorConfiguration;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Christoph
 */
public class ICDE_3_NBA {

    private static final SkylineAlgorithm[] algorithm = {
        new SkylineAlgorithmParallelBNLLinkedListLockFreeSync(),
        new SkylineAlgorithmParallelBNLLinkedListLazySync(),
        new SkylineAlgorithmParallelBNLLinkedListFineGrainedSync()};
    //private static final int[] numOfCores = {1, 2, 3, 4, 4, 6, 7, 8};
    private static final int[] numOfCores = {1, 2};
    private static Writer out = null;
    private static String outfilename = "3_NBA";

    private static void runExperiments(SimulatorConfiguration config, Simulator sim) throws IOException {
        // ALGORITHM

        out.write(AlgorithmSummaryStatistics.getDivider() + "\n");
        out.write(AlgorithmSummaryStatistics.getHeader() + "\n");

        // DIMENSIONS
        for (SkylineAlgorithm alg : algorithm) {
            config.setSkylineAlgorithm(alg);

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

    public static void main(String[] args) {
        try {
            //
            SimulatorConfiguration config = new SimulatorConfiguration();
            config.setDataSource(DataSource.MEMORY);
            config.setUseDefaultGeneratorSeed(true);
            config.setDataGenerator(new DataGeneratorIndependent());
            config.setNumTrials(2);
            Simulator sim = new Simulator();

            // generator param
            DumpFileGenerator gen = new DumpFileGenerator("nba_dump.txt");
            config.setDataGenerator(gen);
            config.setD(gen.getNumOfColumns());
            config.setN(gen.getNumOfRows());


            //
            File outFile = ExperimentTools.getIncrementedOutFile(outfilename);
            out = new FileWriter(outFile);
            runExperiments(config, sim);

            out.write(AlgorithmSummaryStatistics.getDivider() + "\n");
            out.write(AlgorithmSummaryStatistics.getHeader() + "\n");
            out.close();
            ExperimentTools.sendResultViaMail(outFile,"lofi@ifis.cs.tu-bs.de",  true, true);
        } catch (Exception ex) {
            Logger.getLogger(ICDE_3_NBA.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
