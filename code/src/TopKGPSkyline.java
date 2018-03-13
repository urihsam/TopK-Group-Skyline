/**
 * Created by mashiru on 2/10/18.
 */

import java.util.*;


public class TopKGPSkyline {
    protected int groupSize;
    protected int topK;
    protected boolean smallerPref;

    public TopKGPSkyline(int gSize, int topK) {
        groupSize = gSize;
        this.topK = topK;
        smallerPref = true;
    }

    public TopKGPSkyline(int gSize, int topK, boolean smaller) {
        groupSize = gSize;
        this.topK = topK;
        smallerPref = smaller;
    }

    public void setSmallerPref(boolean smaller) { smallerPref = smaller; }
    public boolean getSmallerPref() { return smallerPref; }
    public int getGroupSize() { return groupSize; }
    public void setGroupSize(int gSize) { groupSize = gSize; }
    public int getK() { return topK; }
    public void setK(int k) { topK = k; }

    // if point a could dominate point b
    public boolean isDominate(Double[] a, Double[] b) {
        int mark = 0;
        for (int i = 0; i < a.length; i++) {
            if (smallerPref?a[i] > b[i]:a[i] < b[i])
                return false;
            else if (a[i] == b[i])
                mark++;
        }
        return mark == a.length ? false: true;
    }

    public SkGraph createLayerGraph(List<Double[]> data) {
        return createLayerGraph(data, groupSize+1, false);
    }

    // create graph: Not brute force & Higher dimension
    public SkGraph createLayerGraph(List<Double[]> data, int maxNumOfLayer, boolean skyband) {
        // sort the points
        Collections.sort(data, new Comparator<Object>() {
            @Override
            public int compare(Object o1, Object o2) {
                if (o1 instanceof Double[])
                    return (compare((Double[]) o1, (Double[]) o2));
                return 0;
            }
            public int compare(Double[] o1, Double[] o2) {
                return (o1[0] < o2[0] ? (smallerPref?-1:1) : (o1[0] == o2[0] ? 0 : (smallerPref?1:-1)));
            }
        });


        SkNode pt0 = new SkNode(data.get(0), 0); // first point belongs to the first layer
        SkGraph graph = new SkGraph(groupSize);
        graph.addGraphLayerNode(pt0); // add pt0 to layer0

        System.out.println("Creating layer structure...");
        // step one: create the layer structure in graph
        for (int ptIdx = 1; ptIdx < data.size(); ptIdx++) {
            SkNode pt = new SkNode(data.get(ptIdx), 0);
            int minLayer = 0;
            boolean flagNext = false;
            for (int lIdx=graph.getNumOfLayers()-1; lIdx>=0; lIdx--) {
                SkLayer currLayer = graph.getGraphLayer(lIdx);
                for (int nIdx=currLayer.getLayerSize()-1; nIdx>=0; nIdx--) {
                    SkNode currNode = currLayer.getLayerNode(nIdx);
                    if (isDominate(currNode.getVal(), pt.getVal())) {
                        minLayer = currNode.getLayerIdx() + 1;
                        flagNext = true; break;
                    }
                }
                if (flagNext) break;
            }
            pt.setLayerIdx(minLayer<groupSize?minLayer:groupSize); // set layer index
            if (pt.getLayerIdx() < maxNumOfLayer)
                graph.addGraphLayerNode(pt);
        }

        System.out.println("Setting parents and children...");
        // step two: set parents and children
        for (int layerIdx=0; layerIdx<graph.getNumOfLayers(); layerIdx++) { // iterate each layer
            SkLayer currLayer = graph.getGraphLayer(layerIdx);
            for (int nodeIdx=0; nodeIdx<currLayer.getLayerNodes().size(); nodeIdx++) {// update each node in the current layer
                SkNode currNode = currLayer.getLayerNode(nodeIdx);
                if (skyband && currNode.getParents().size() > groupSize-1 ) {
                    // update parents' info
                    for (SkNode parent: currNode.getParents())
                        parent.getChildren().remove(currNode);
                    // update currLayer info
                    currLayer.getLayerNodes().remove(nodeIdx);
                    graph.setGraphSize(graph.getGraphSize()-1);
                    nodeIdx --;
                    continue;
                }
                // process each layer after current layer, note: when the current layer is the last layer, i.e., layerIdx = graph.getNumOfLayers()-1, ignore the following
                if (layerIdx<graph.getNumOfLayers()-1 ) {
                    for (int searchLayerIdx = layerIdx + 1; searchLayerIdx < graph.getNumOfLayers(); searchLayerIdx++) { // start searching from the next layer of current
                        SkLayer searchLayer = graph.getGraphLayer(searchLayerIdx);
                        for (SkNode searchNode : searchLayer.getLayerNodes()) // look for each node in the search layer
                            if (isDominate(currNode.getVal(), searchNode.getVal())) {
                                currNode.addChild(searchNode);
                                if (searchNode.getLayerIdx() < groupSize)
                                    searchNode.addParent(currNode);
                            }
                    }
                }
            }
        }
        // step three: set IDs following the order in layers
        int ID = 0;
        for (SkLayer layerElm : graph.getGraphLayers()) // for each layer in all layers
            for (SkNode ndElm : layerElm.getLayerNodes()) // for each node in this layer
                ndElm.setId(ID++);

        return graph;
    }


    public List<SkGroup> getTopKGroups(SkGraph graph, boolean universe, boolean silent) {
        System.out.println("Group-Point getTopKGroups");
        TopKGroup topKGroup = new TopKGroup(topK);
        List<SkNode> firstLayer = graph.graphLayers.get(0).getLayerNodes();
        Collections.sort(firstLayer, Collections.reverseOrder()); // sort the top layer of the graph
        // checkCombination(firstLayer, groupSize, new SkGroup(), topKGroup);
        List<SkGroup> universeGroup = universe?new ArrayList<SkGroup>():null;
        searchPostCombination(firstLayer, new GroupCandidates(groupSize), topKGroup, universe, universeGroup);
        if (!silent) topKGroup.print();
        List<SkGroup> groups4Check = universe?universeGroup:new ArrayList<SkGroup>(topKGroup.getTopKGroup());
        Collections.reverse(groups4Check);
        searchChildren4TopKG(groups4Check, groupSize, topKGroup);
        if (!silent) topKGroup.print();
        return topKGroup.getTopKGroup();
    }

    protected void searchChildren4TopKG(List<SkGroup> groups4Check, int g, TopKGroup topKG) {
        for (int depth=1; depth<g; depth++)
            for (SkGroup group: groups4Check) //  check certain group
                for (SkNode gNode: group.getGroupNodes()) // check certain gnode in the group
                    for (SkNode nChild: gNode.getChildren()) { // check certain nChild of gNode
                        if (nChild.getLayerIdx() == depth) {
                            if (nChild.getParents().size() < groupSize)
                                if (group.getGroupNodes().containsAll(nChild.getParents())) {
                                    List<SkNode> groupNodesRemain= new ArrayList<SkNode>(group.getGroupNodes());
                                    List<SkNode> groupNodesChecked = new ArrayList<SkNode>(nChild.getParents());
                                    groupNodesRemain.removeAll(groupNodesChecked);
                                    groupNodesChecked.add(nChild);
                                    SkGroup groupFound = new SkGroup("GP", groupNodesChecked, gNode.getChildren());
                                    // checkCombination(groupNodesRemain, g-groupNodesChecked.size(), groupFound, topKG);
                                    searchPostCombination(groupNodesRemain, new GroupCandidates(groupFound, g), topKG);
                                }
                        }else if (nChild.getLayerIdx() > depth)
                            break; // change to another node, skip the rest children
                    }
    }

    // Pre-combine, subgroup is combined first, then try to look for the rest
    protected void searchCombination(List<SkNode> nodes4Check, int g, SkGroup groupFound, TopKGroup topKG) {
        if (g == 0 && !topKG.getTopKGroup().contains(groupFound)) {
            topKG.addSkGroup(groupFound);
            return;
        }
        for (int idx=0; idx<nodes4Check.size(); idx++) {
            SkNode node = nodes4Check.get(idx);
            if (topKG.getTopKGroupSize() == topK && node.getSizeOfDominatedNodes() + groupFound.getSizeOfDominatedNodes() <= topKG.getMinSizeOfDominatedNodes())
                return;
            SkGroup newGroupFound = new SkGroup(groupFound); // copy of groupFound
            newGroupFound.addGroupNode(node);
            searchCombination(nodes4Check.subList(idx+1, nodes4Check.size()), g-1, newGroupFound, topKG);
            newGroupFound = null; //  remove the reference of newGroupFound to delete this object
        }
    }

    // Post-combine, first pick nodes by order, then try to group them when there are number of nodes picked
    protected void searchPostCombination(List<SkNode> nodes4Check, GroupCandidates candidates, TopKGroup topKG) {
        searchPostCombination(nodes4Check, candidates, topKG, false, null);
    }

    protected void searchPostCombination(List<SkNode> nodes4Check, GroupCandidates candidates, TopKGroup topKG, boolean universe, List<SkGroup> universeGroups) {
        if (candidates.getNumOfCandidates() == candidates.getMaxSize()) {
            int minDominates = topKG.getMinSizeOfDominatedNodes();
            SkGroup groupFound = new SkGroup("GP", new ArrayList<>(candidates.getGroupDeque())); // finely calculate
            if (universe)
                universeGroups.add(groupFound);
            if (topKG.getTopKGroupSize() != topK || (groupFound.getSizeOfDominatedNodes() > minDominates && !topKG.getTopKGroup().contains(groupFound)))
                topKG.addSkGroup(groupFound);
            return;
        }
        for (int nIdx=0; nIdx<nodes4Check.size(); nIdx++) {
            SkNode currNode = nodes4Check.get(nIdx);
            if (candidates.getNumOfCandidates() == candidates.getMaxSize()-1) { // is processing the last one
                int minDominates = topKG.getMinSizeOfDominatedNodes();
                // If topKG is full and rough result is not larger than the min of the topKG, skip it
                if (topKG.getTopKGroupSize() == topK && currNode.getSizeOfDominatedNodes() + candidates.getTotalSizeOfDominatedNodes() <= minDominates)
                    continue;
            }
            // push the currNode as a candidate
            candidates.pushGroupNode(currNode);
            searchPostCombination(nodes4Check.subList(nIdx + 1, nodes4Check.size()), candidates, topKG, universe, universeGroups);
            candidates.popGroupNode();
        }
    }

    public static void main(String[] args) {
        Experiment experimentBaseline = new Experiment("GP", true);
        Experiment experimentTopKGP = new Experiment("GP", false);
        if (args.length > 0) { // with arguments
            String spliter = "  ";
            String dir = "../data/";
            int gSize = Integer.parseInt(args[0]); // group size
            int topK = Integer.parseInt(args[1]); // top k
            int dims = Integer.parseInt(args[2]); // dimensions
            int numOfPts = Integer.parseInt(args[3]); // the exponent X of 1eX

            experimentTopKGP.argumentsTrial(gSize, topK, dims, numOfPts, dir, spliter);
            //experimentBaseline.argumentsTrial(gSize, topK, dims, numOfPts, dir, spliter);
        } else { // without arguments, grid testing
            String spliter = "  ";
            String dir = "../data/";
            //NBA
            int stdGSize = 5;
            int stdTopK = 20;
            int stdDims = 5;
            int stdNOPt = 3;
            double stdScal = 1;
            experimentTopKGP.setStandardParams(stdGSize, stdTopK, stdDims, stdNOPt, stdScal);
            experimentBaseline.setStandardParams(stdGSize, stdTopK, stdDims, stdNOPt, stdScal);
            //int[] gSizeList = {2, 3, 4, 5};
            //int[] topKList = {3, 4, 5};
            //int[] dimsList = {2, 3, 4, 5, 6, 7, 8};
            int[] numOfPtsList = {3};
            String resultsDir = "../results/";
            //experimentTopKGP.saveTrialResults("D", dimsList, dir, spliter,  resultsDir+"dimensionsChangesGP");
            //experimentTopKGP.saveTrialResults("GS", gSizeList, dir, spliter,  resultsDir+"groupSizeChangesGP");
            //experimentTopKGP.saveTrialResults("K", topKList, dir, spliter,  resultsDir+"topKChangesGP");
            //experimentTopKGP.saveTrialResults("PT", numOfPtsList, dir, spliter,  resultsDir+"numOfPointsChangesGP");
            experimentTopKGP.saveTrialResults("PT", numOfPtsList, dir, spliter,  resultsDir+"NBAGP");
            // baseline
            //int[] topKListBaseline = {3, 4, 5};
            //experimentBaseline.saveTrialResults("D", dimsListBaseline, dir, spliter,  resultsDir+"dimensionsChangesGP_Baseline");
            //experimentBaseline.saveTrialResults("GS", gSizeList, dir, spliter,  resultsDir+"groupSizeChangesGP_Baseline");
            //experimentBaseline.saveTrialResults("K", topKListBaseline, dir, spliter,  resultsDir+"topKChangesGP_Baseline");
            //experimentBaseline.saveTrialResults("PT", numOfPtsListBaseline, dir, spliter,  resultsDir+"numOfPointsChangesGP_Baseline");
        }

    }


}

