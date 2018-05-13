package ifis.skysim2.data.trees.zbtree;

import ifis.skysim2.data.sources.PointSource;

public interface BulkLoader {
    public ZBTree bulkLoad(PointSource data);
    public ZBTree bulkLoad(PointSource data, int nodeCapacityMax);
}
