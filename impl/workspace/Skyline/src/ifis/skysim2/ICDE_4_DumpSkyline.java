/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ifis.skysim2;

import ifis.skysim2.algorithms.parallelbnl.SkylineAlgorithmParallelBNLLinkedListLazySync;
import ifis.skysim2.data.generator.DumpFileGenerator;
import ifis.skysim2.simulator.AlgorithmSummaryStatistics;
import ifis.skysim2.simulator.DataSource;
import ifis.skysim2.simulator.Simulator;
import ifis.skysim2.simulator.SimulatorConfiguration;

import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Christoph
 */
public class ICDE_4_DumpSkyline {

    private static Writer out = null;
    private static String outfilename = "4_DUMP";

    public static void main(String[] args) {
        try {
            //
            SimulatorConfiguration config = new SimulatorConfiguration();
            config.setDataSource(DataSource.MEMORY);
            config.setUseDefaultGeneratorSeed(true);
            config.setNumTrials(1);
            config.setSkylineAlgorithm(new SkylineAlgorithmParallelBNLLinkedListLazySync());
            config.setNumberOfCPUs(2);
            Simulator sim = new Simulator();

            // generator param
            DumpFileGenerator gen = new DumpFileGenerator("nba_dump.txt");
            config.setDataGenerator(gen);
            config.setD(gen.getNumOfColumns());
            config.setN(gen.getNumOfRows());


            //
            File outFile = ExperimentTools.getIncrementedOutFile(outfilename);
            out = new FileWriter(outFile);

            // run experiment

            out.write(AlgorithmSummaryStatistics.getDivider() + "\n");
            out.write(AlgorithmSummaryStatistics.getHeader() + "\n");
            AlgorithmSummaryStatistics summary = sim.run(config);
            out.write(summary.toString() + "\n");
            out.flush();


            // write file
            out.write(AlgorithmSummaryStatistics.getDivider() + "\n");
            out.write(AlgorithmSummaryStatistics.getHeader() + "\n");
            out.close();
            ExperimentTools.sendResultViaMail(outFile, "lofi@ifis.cs.tu-bs.de", true, true);
        } catch (Exception ex) {
            Logger.getLogger(ICDE_4_DumpSkyline.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
