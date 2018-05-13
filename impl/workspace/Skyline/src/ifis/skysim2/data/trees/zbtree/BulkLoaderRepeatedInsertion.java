package ifis.skysim2.data.trees.zbtree;

import ifis.skysim2.data.sources.PointSource;

public class BulkLoaderRepeatedInsertion implements BulkLoader {

    @Override
    public ZBTree bulkLoad(PointSource data) {
	return bulkLoad(data, ZBTree.DEFAULT_NODE_CAPACITY_MAX);
    }

    @Override
    public ZBTree bulkLoad(PointSource data, int nodeCapacityMax) {
	int d = data.getD();
	int n = data.size();
	ZBTree tree = new ZBTree(d);
	for (int i = 0; i < n; i++) {
	    float[] p = data.get(i);
	    tree.insertPoint(p);
	}
	return tree;
    }
}
