package ifis.skysim2.junk;

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class RTreeOld extends AbstractSet<float[]> {

    // each node can host between 2 <= capacityMin <= capacityMax / 2 and capacityMax entries
    public final static int capacityMin = 2;
    public final static int capacityMax = 5;
    
    private int d;
    private int size = 0;
    private Node rootNode = null;

    public RTreeOld(int d) {
	this.d = d;
    }

    public Node getRootNode() {
	return rootNode;
    }

    @Override
    public Iterator<float[]> iterator() {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int size() {
	System.out.println(rootNode.deepToString());
	return size;
    }

    // Insert a new point into the R-tree,
    // using the method originally proposed by Guttman
    @Override
    public boolean add(float[] point) {
	if (rootNode == null) {
	    // currently, the tree is empty; we must create a root node
	    LeafNode newRootNode = new LeafNode(d);
	    newRootNode.entries.add(point);
	    newRootNode.recomputeMBR();
	    rootNode = newRootNode;
	} else {
	    LeafNode l = chooseLeaf(point);
	    l.entries.add(point);
	    adjustNode(l);
	}
	size++;
	return true;
    }

    public boolean bulkLoad(List<float[]> data) {
	rootNode = BulkLoadTGS.bulkLoad(data);
	size = data.size();
	return true;
    }

    // check whether node is too large; if so, fix it by splitting
    private void adjustNode(LeafNode node) {
	if (node.entries.size() <= capacityMax) {
	    // everything is fine
	    node.recursivelyRecomputeMBRs();
	} else {
	    // node is already full; we must split it
	    LeafNode splitNode = splitNode(node);
	    if (node.parent == null) {
		// we just splitted the root node ...
		InternalNode newRootNode = new InternalNode(d);
		newRootNode.assignNode(node);
		newRootNode.assignNode(splitNode);
		newRootNode.recomputeMBR();
		rootNode = newRootNode;
	    } else {
		// we splitted a non-root node
		node.parent.assignNode(splitNode);
		node.parent.recomputeMBR();
		// now, node.parent might be too large ...
		adjustNode(node.parent);
	    }
	}
    }

    // check whether node is too large; if so, fix it by splitting
    private void adjustNode(InternalNode node) {
	if (node.entries.size() <= capacityMax) {
	    // everything is fine
	    node.recursivelyRecomputeMBRs();
	} else {
	    // we must split the node
	    InternalNode splitNode = splitNode(node);
	    if (node.parent == null) {
		// we just splitted the root node ...
		InternalNode newRootNode = new InternalNode(d);
		newRootNode.assignNode(node);
		newRootNode.assignNode(splitNode);
		newRootNode.recomputeMBR();
		rootNode = newRootNode;
	    } else {
		// we just splitted some non-root node ...
		node.parent.assignNode(splitNode);
		node.parent.recomputeMBR();
		// now, node.parent might be too large
		adjustNode(node.parent);
	    }
	}
    }

    // Select a leaf node in which to place a new point.
    private LeafNode chooseLeaf(float[] point) {
	assert rootNode != null;
	Node n = rootNode;
	while (!(n instanceof LeafNode)) {
	    double minEnlargement = Double.POSITIVE_INFINITY;
	    double minArea = Double.POSITIVE_INFINITY;
	    Node f = null;
	    // Let f be the entry in n whose MBR needs least enlargement to include the new point.
	    // Resolve ties by choosing the node with the MBR of smallest area.
	    for (Node node : ((InternalNode) n).entries) {
		double enlargement = node.getMBREnlargementBy(point);
		if (enlargement < minEnlargement) {
		    f = node;
		    minEnlargement = enlargement;
		    minArea = node.getMBRArea();
		} else if (enlargement == minEnlargement) {
		    double area = node.getMBRArea();
		    if (area < minArea) {
			f = node;
			minArea = area;
		    }
		}
	    }
	    n = f;
	}
	return (LeafNode) n;
    }

    // linear node split
    private LeafNode splitNode(LeafNode node) {
	assert node.entries.size() == capacityMax + 1;
	List<float[]> entriesToSplit = new LinkedList<float[]>(node.entries);
	node.entries.clear();
	float[][] splitNodes = linearPickSeeds(entriesToSplit);
	float[] seedNode = splitNodes[0];
	float[] seedSplitNode = splitNodes[1];
	entriesToSplit.remove(seedNode);
	entriesToSplit.remove(seedSplitNode);
	LeafNode splitNode = new LeafNode(d);
	node.entries.add(seedNode);
	splitNode.entries.add(seedSplitNode);
	node.recomputeMBR();
	splitNode.recomputeMBR();
	while (!entriesToSplit.isEmpty()) {
	    int pointsLeft = entriesToSplit.size();
	    int pointsRequiredNode = capacityMin - node.entries.size();
	    int pointsRequiredSplitNode = capacityMin - splitNode.entries.size();
	    if (pointsLeft == pointsRequiredNode) {
		node.entries.addAll(entriesToSplit);
		entriesToSplit.clear();
		node.recomputeMBR();
	    } else if (pointsLeft == pointsRequiredSplitNode) {
		splitNode.entries.addAll(entriesToSplit);
		entriesToSplit.clear();
		splitNode.recomputeMBR();
	    } else {
		float[] point = entriesToSplit.get(0);
		entriesToSplit.remove(0);
		double enlargementNode = node.getMBREnlargementBy(point);
		double enlargementSplitNode = splitNode.getMBREnlargementBy(point);
		if (enlargementNode < enlargementSplitNode) {
		    node.entries.add(point);
		    node.recomputeMBR();
		} else if (enlargementNode > enlargementSplitNode) {
		    splitNode.entries.add(point);
		    splitNode.recomputeMBR();
		} else {
		    double areaNode = node.getMBRArea();
		    double areaSplitNode = splitNode.getMBRArea();
		    if (areaNode <= areaSplitNode) {
			node.entries.add(point);
			node.recomputeMBR();
		    } else {
			splitNode.entries.add(point);
			splitNode.recomputeMBR();
		    }
		}
	    }
	}
	return splitNode;
    }

    // linear node split
    private InternalNode splitNode(InternalNode node) {
	assert node.entries.size() == capacityMax + 1;
	List<Node> entriesToSplit = new LinkedList<Node>(node.entries);
	node.entries.clear();
	Node[] splitNodes = linearPickSeeds(entriesToSplit);
	Node seedNode = splitNodes[0];
	Node seedSplitNode = splitNodes[1];
	entriesToSplit.remove(seedNode);
	entriesToSplit.remove(seedSplitNode);
	InternalNode splitNode = new InternalNode(d);
	node.assignNode(seedNode);
	splitNode.assignNode(seedSplitNode);
	node.recomputeMBR();
	splitNode.recomputeMBR();
	while (!entriesToSplit.isEmpty()) {
	    int entriesLeft = entriesToSplit.size();
	    int entriesRequiredNode = capacityMin - node.entries.size();
	    int entriesRequiredSplitNode = capacityMin - splitNode.entries.size();
	    if (entriesLeft == entriesRequiredNode) {
		for (Node entry : entriesToSplit) {
		    node.assignNode(entry);
		}
		entriesToSplit.clear();
		node.recomputeMBR();
	    } else if (entriesLeft == entriesRequiredSplitNode) {
		for (Node entry : entriesToSplit) {
		    splitNode.assignNode(entry);
		}
		entriesToSplit.clear();
		splitNode.recomputeMBR();
	    } else {
		Node entry = entriesToSplit.get(0);
		entriesToSplit.remove(0);
		double enlargementNode = node.getMBREnlargementBy(entry);
		double enlargementSplitNode = splitNode.getMBREnlargementBy(entry);
		if (enlargementNode < enlargementSplitNode) {
		    node.assignNode(entry);
		    node.recomputeMBR();
		} else if (enlargementNode > enlargementSplitNode) {
		    splitNode.assignNode(entry);
		    splitNode.recomputeMBR();
		} else {
		    double areaNode = node.getMBRArea();
		    double areaSplitNode = splitNode.getMBRArea();
		    if (areaNode <= areaSplitNode) {
			node.assignNode(entry);
			node.recomputeMBR();
		    } else {
			splitNode.assignNode(entry);
			splitNode.recomputeMBR();
		    }
		}
	    }
	}
	return splitNode;
    }

    // LinearPickSeeds: Select two points to be the first elements of the groups
    // since we work in the uni cube there is no need for a normalized separation
    private static Node[] linearPickSeeds(List<Node> entriesToSplit) {
	int d = entriesToSplit.get(0).getD();
	float maxSeparation = Float.NEGATIVE_INFINITY;
	Node seedNode = null;
	Node seedSplitNode = null;
	for (int i = 0; i < d; i++) {
	    Node entryMinHighSide = entriesToSplit.get(0);
	    Node entryMaxLowSide = entriesToSplit.get(0);
	    Node entryMinHighSideSecond = entriesToSplit.get(1);
	    Node entryMaxLowSideSecond = entriesToSplit.get(1);
	    if (entryMinHighSide.getMBRUpper(i) > entryMinHighSideSecond.getMBRUpper(i)) {
		Node temp = entryMinHighSide;
		entryMinHighSide = entryMinHighSideSecond;
		entryMinHighSideSecond = temp;
	    }
	    if (entryMaxLowSide.getMBRLower(i) < entryMaxLowSideSecond.getMBRLower(i)) {
		Node temp = entryMaxLowSide;
		entryMaxLowSide = entryMaxLowSideSecond;
		entryMaxLowSideSecond = temp;
	    }
	    for (int j = 2; j < entriesToSplit.size(); j++) {
		Node entry = entriesToSplit.get(j);
		if (entry.getMBRUpper(i) <= entryMinHighSide.getMBRUpper(i)) {
		    entryMinHighSideSecond = entryMinHighSide;
		    entryMinHighSide = entry;
		}
		if (entry.getMBRLower(i) >= entryMaxLowSide.getMBRLower(i)) {
		    entryMaxLowSideSecond = entryMaxLowSide;
		    entryMaxLowSide = entry;
		}
	    }
	    // compute separation and compare it to the largest separation observed so far
	    if (entryMinHighSide != entryMaxLowSide) {
		float separation = entryMaxLowSide.getMBRLower(i) - entryMinHighSide.getMBRUpper(i);
		if (separation > maxSeparation) {
		    maxSeparation = separation;
		    seedNode = entryMinHighSide;
		    seedSplitNode = entryMaxLowSide;
		}
	    } else {
		// the entry having the highest low side might be identical to
		// the entry having the lowest high side
		float separation1 = entryMaxLowSide.getMBRLower(i) - entryMinHighSideSecond.getMBRUpper(i);
		float separation2 = entryMaxLowSideSecond.getMBRLower(i) - entryMinHighSide.getMBRUpper(i);
		if ((separation1 > maxSeparation) || (separation2 > maxSeparation)) {
		    if (separation1 >= separation2) {
			maxSeparation = separation1;
			seedNode = entryMinHighSideSecond;
			seedSplitNode = entryMaxLowSide;
		    } else {
			maxSeparation = separation2;
			seedNode = entryMinHighSide;
			seedSplitNode = entryMaxLowSideSecond;
		    }
		}
	    }
	}
	Node[] seeds = {seedNode, seedSplitNode};
	return seeds;
    }

    // LinearPickSeeds: Select two points to be the first elements of the groups
    // since we work in the uni cube there is no need for a normalized separation
    private static float[][] linearPickSeeds(List<float[]> entriesToSplit) {
	int d = entriesToSplit.get(0).length;
	float[] seedNode = null;
	float[] seedSplitNode = null;
	float maxSeparation = Float.NEGATIVE_INFINITY;
	for (int i = 0; i < d; i++) {
	    // find points having minimum and maximum coordinate in dimension i
	    float min = Float.POSITIVE_INFINITY;
	    float max = Float.NEGATIVE_INFINITY;
	    float[] pointMin = null;
	    float[] pointMax = null;
	    for (float[] point : entriesToSplit) {
		if (point[i] < min) {
		    min = point[i];
		    pointMin = point;
		}
		if (point[i] > max) {
		    max = point[i];
		    pointMax = point;
		}
	    }
	    // compute separation and compare it to the largest separation observed so far
	    float separation = max - min;
	    if (separation > maxSeparation) {
		maxSeparation = separation;
		seedNode = pointMin;
		seedSplitNode = pointMax;
	    }
	}
	float[][] seeds = {seedNode, seedSplitNode};
	return seeds;
    }
}
