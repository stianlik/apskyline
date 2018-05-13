package ifis.skysim2;

import ifis.skysim2.algorithms.SkylineAlgorithmBNL;
import ifis.skysim2.algorithms.pDistributed.SkylineAlgorithmDistributed;
import ifis.skysim2.algorithms.pPackageSync.SkylineAlgorithmPPackageSync;
import ifis.skysim2.algorithms.pQueueSync.SkylineAlgorithmPQueueSync;
import ifis.skysim2.algorithms.pUnsync.SkylineAlgorithmPUnsync;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Christoph
 */
public class Experiment1 {

//    public static void main(String[] args) {
//        MyExperiment paras = new MyExperiment();
//        paras.setTrials(1);
//        List<ExperimentSummary> summaries = new LinkedList<ExperimentSummary>();
//        // All Algorithms
//        List<Class> algorithms = new LinkedList<Class>();
//   //     algorithms.add(SkylineAlgorithmBNL.class);
//        algorithms.add(SkylineAlgorithmPQueueSync.class);
//   //     algorithms.add(SkylineAlgorithmPUnsync.class);
//        algorithms.add(SkylineAlgorithmDistributed.class);
//        // All Skyline Sizes
//        List<Integer> sizes = new LinkedList<Integer>();
////        sizes.add(100000);
//        sizes.add(1000000);
//       // sizes.add(10000000);
//        //
//        for (int size : sizes) {
//            ExperimentSummary summary = new ExperimentSummary(String.valueOf(size));
////            paras.setPresort(false);
////
////            paras.setN(size);
////            for (Class algorithm : algorithms) {
////                Skysim2 skysim = new Skysim2();
////                paras.setSkylineAlgorithmClass(algorithm);
////                skysim.setExperimentParameters(paras);
////                summary.addAlgorithmStatistic(skysim.run());
////            }
//            //
//            paras.setPresort(true);
//            for (Class algorithm : algorithms) {
//                Skysim2 skysim = new Skysim2();
//                paras.setSkylineAlgorithmClass(algorithm);
//                skysim.setExperimentParameters(paras);
//                summary.addAlgorithmStatistic(skysim.run());
//            }
//            summaries.add(summary);
//            summary.printSummary();
//        }
//        // summaries
//        for (ExperimentSummary summary : summaries) {
//            summary.printSummary();
//        }
//
//    }
}
