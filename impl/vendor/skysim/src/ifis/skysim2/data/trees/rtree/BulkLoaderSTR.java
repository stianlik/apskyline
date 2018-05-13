package ifis.skysim2.data.trees.rtree;

// Bulk loading of R-trees using the Sort-Tile-Recursive (STR) algorithm
import ifis.skysim2.data.trees.Rectangle;
import ifis.skysim2.common.tools.ArraySorter;

// Leutenegger, Lopez, Edgington: STR: A Simple and Efficient Algorithm for R-Tree Packing (1997)
public class BulkLoaderSTR extends BulkLoaderPackingBottomUp {

    @Override
    void sortNodes(RTree.Node[] nodes, int begin, int end) {
	// Mögliche Verbesserungen gegenüber TAO:
	// - Splits nur entlang der Grenzen der späteren Parent-Nodes
	// - Blöcke sollten auf jedem Level möglichst nahe an
	//   der "Sollgröße" m^(d - i)/d liegen, wobei m die Gesamtanzahl der
	//   neuen Eltenknoten ist und i die Splitnummer
	//   (nach dem i-ten Split sollte jeder Slab etwa m^(d - i)/d Original-Knoten enthalten)
	// - Do it iteratively, not recursively



	// Yufei Tao
	int n = nodes.length;
	int d = nodes[0].getD();

	int num_nodes = (int) Math.ceil((float) n / RTree.CAPACITY_MAX);
	int stop_dim;
	int num_straps;
	if (num_nodes >= Math.pow(2, d)) {
	    num_straps = (int) Math.pow(num_nodes, (double) 1 / d);
	    stop_dim = d - 1;
	} else {
	    num_straps = 2;
	    stop_dim = (int) (Math.log(num_nodes) / Math.log(2));
	}

	double sortvals[] = new double[n];

	str_strap(nodes, begin, end, 0, stop_dim, num_straps, sortvals);
    }

    private void str_strap(RTree.Node[] nodes, int begin, int end, int active_dim, int stop_dim, int num_straps, double[] sortvals) {
	// sort nodes by active_dim
	for (int i = begin; i < end; i++) {
	    Rectangle mbr = nodes[i].getMBR();
	    sortvals[i] = mbr.getCentroid(active_dim);
	}
	ArraySorter.doubleSort(nodes, sortvals, begin, end);
	int num_nodes = end - begin;
	if (active_dim < stop_dim) {
	    int num_nodes_per_strap = (int) Math.floor((double) num_nodes / num_straps);
	    for (int i = 0; i < num_straps; i++) {
		int strapBegin = begin + i * num_nodes_per_strap;
		int strapEnd = begin + Math.min((i + 1) * num_nodes_per_strap, end);
		str_strap(nodes, strapBegin, strapEnd, active_dim + 1, stop_dim, num_straps, sortvals);
	    }
	}
    }
}
