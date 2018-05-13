package ifis.skysim2.algorithms.pQueueSyncl.reorganizers;

import ifis.skysim2.algorithms.pQueueSync.SkylineWorker;
import ifis.skysim2.data.points.LinkedPointList;
import java.util.List;

/**
 * Distributes the windows of the workers to equally sized chunks.
 * @author Christoph
 */
public class AdaptiveReorganizer extends AbstractReorganizer {

    public AdaptiveReorganizer(List<SkylineWorker> workers) {
        super(workers);
    }

    public AdaptiveReorganizer() {
        super();
    }

    @Override
    protected void reorganize() {
        if (workers.size() != 2) {
            throw new UnsupportedOperationException("Does only work for exactly two workers");
        }

        int c0 = (int) workers.get(0).getCpuCostSinceLastReorg();
        int c1 = (int) workers.get(1).getCpuCostSinceLastReorg();
        int s0 = workers.get(0).getWindow().size();
        int s1 = workers.get(1).getWindow().size();
        double alpha0 = (double) c0 / (s0 + 1);
        double alpha1 = (double) c1 / (s1 + 1);
        double alphaavg = (alpha0 + alpha1) / 2;
        double alphasmooth0 = (s0 * alpha0 + alphaavg) / (s0 + 1);
alphasmooth0 *=1.1;
        double alphasmooth1 = (s1 * alpha1 + alphaavg) / (s1 + 1);
        int[] targetSize = new int[2];
        targetSize[0] = (int) ((s0 + s1) * alphasmooth1 / (alphasmooth0 + alphasmooth1));
        targetSize[1] = s0 + s1 - targetSize[0];

//	System.out.format("Comparisons:  0: %d  ---  1: %d%n", c0, c1);
//	System.out.format("alpha0: %.1f  --- alpha1: %.1f --- alphaavg: %.1f --- alphasmooth0: %.1f --- alphasmooth1: %.1f%n", alpha0, alpha1, alphaavg, alphasmooth0, alphasmooth1);
//	System.out.format("Sizes:  0: %d --> %d  ---  1: %d --> %d%n%n", workers.get(0).getWindow().size(), targetSize[0],
//		                                                         workers.get(1).getWindow().size(), targetSize[1]);

        for (int i = 0; i < workers.size() - 1; i++) {
            int nextListIndex = i + 1;
            LinkedPointList currentList = (LinkedPointList) workers.get(i).getWindow();
            LinkedPointList nextList = (LinkedPointList) workers.get(nextListIndex).getWindow();
            // if list to small, try to get more datapoints from next list
            while (currentList.size() < targetSize[i] && nextList.size() > 0) {
                ((LinkedPointList) currentList).stealFromList((LinkedPointList) nextList, targetSize[i] - currentList.size());
                if ((nextList.size() == 0) && (nextListIndex < workers.size())) {
                    // next list empty? try the next next...
                    nextListIndex++;
                    nextList = (LinkedPointList) workers.get(nextListIndex).getWindow();
                }
            }
            nextListIndex++;
        }
    }

    @Override
    public String toString() {
        return "Adaptive Reorganizer";
    }
}
