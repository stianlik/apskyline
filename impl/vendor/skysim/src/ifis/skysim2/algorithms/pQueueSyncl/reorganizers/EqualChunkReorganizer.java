package ifis.skysim2.algorithms.pQueueSyncl.reorganizers;

import ifis.skysim2.algorithms.pQueueSync.SkylineWorker;
import ifis.skysim2.data.points.LinkedPointList;
import ifis.skysim2.algorithms.pQueueSync.SkylineWorkerQueueSync;
import java.util.List;

/**
 * Distributes the windows of the workers to equally sized chunks.
 * @author Christoph
 */
public class EqualChunkReorganizer extends AbstractReorganizer {

    public EqualChunkReorganizer(List<SkylineWorker> workers) {
        super(workers);
    }

    public EqualChunkReorganizer() {
        super();
    }

    @Override
    protected void reorganize() {
        int overallSize = 0;

        for (SkylineWorker worker : workers) {
            overallSize += worker.getWindow().size();
        }

        for (int i = 0; i < workers.size() - 1; i++) {
            int targetSize = overallSize / workers.size();

            int nextListIndex = i + 1;
            LinkedPointList currentList = (LinkedPointList) workers.get(i).getWindow();
            LinkedPointList nextList = (LinkedPointList) workers.get(nextListIndex).getWindow();
            // if list to small, try to get more datapoints from next list
            while (currentList.size() < targetSize && nextList.size() > 0) {
                ((LinkedPointList) currentList).stealFromList((LinkedPointList) nextList, targetSize - currentList.size());
                if ((nextList.size() == 0) && (nextListIndex < workers.size())) {
                    // next list empty? try the next next...
                    nextListIndex++;
                    nextList = (LinkedPointList) workers.get(nextListIndex).getWindow();
                }
            }
            nextListIndex++;
        }
    }
    
        public String toString() {
	    return "EqualChunk Reorganizer";
    }
}
