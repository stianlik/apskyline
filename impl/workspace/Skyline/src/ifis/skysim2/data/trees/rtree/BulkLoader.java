package ifis.skysim2.data.trees.rtree;

import java.util.Collection;

public interface BulkLoader {
    public RTree bulkLoad(Collection<float[]> data);
}
