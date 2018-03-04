/**
 * Created by mashiru on 2/10/18.
 */

import java.util.*;


public class TopKGPSkyline {
    protected int groupSize;
    protected int topK;

    public TopKGPSkyline(int gSize, int topK) {
        groupSize = gSize;
        this.topK = topK;
    }

    public int getGroupSize() { return groupSize; }
    public void setGroupSize(int gSize) { groupSize = gSize; }
    public int getK() { return topK; }
    public void setK(int k) { topK = k; }

    // if point a could dominate point b
    public boolean isDominate(Integer[] a, Integer[] b) {
        int mark = 0;
        for (int i = 0; i < a.length; i++) {
            if (a[i] > b[i])
                return false;
            else if (a[i] == b[i])
                mark++;
        }
        return mark == a.length ? false: true;
    }


    // create graph: Not brute force & Higher dimension
    public SkGraph createLayerGraph(List<Integer[]> data) {
        // sort the points
        Collections.sort(data, new Comparator<Object>() {
            @Override
            public int compare(Object o1, Object o2) {
                if (o1 instanceof Integer[]) return (compare((Integer[]) o1, (Integer[]) o2));
                return 0;
            }
            public int compare(Integer[] o1, Integer[] o2) { return (o1[0] < o2[0] ? -1 : (o1[0] == o2[0] ? 0 : 1)); }
        });


        SkNode pt0 = new SkNode(data.get(0), 0); // first point belongs to the first layer
        SkGraph graph = new SkGraph(groupSize);
        graph.addGraphLayerNode(pt0); // add pt0 to layer0

        System.out.println("Creating layer structure...");
        // step one: create the layer structure in graph
        for (int ptIdx = 1; ptIdx < data.size(); ptIdx++) {
            SkNode pt = new SkNode(data.get(ptIdx), 0);
            /*
            int maxLayer = -1;
            for (SkLayer currLayer: graph.getGraphLayers())  // for each layer in all layers
                for (SkNode currNode : currLayer.getLayerNodes()) // for each node in this layer
                    if (isDominate(currNode.getVal(), pt.getVal()))
                        if (currNode.getLayerIdx() > maxLayer)
                            maxLayer = currNode.getLayerIdx();
            pt.setLayerIdx(maxLayer<groupSize?maxLayer+1:groupSize); // set layer index
            graph.addGraphLayerNode(pt);
            */
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
            graph.addGraphLayerNode(pt);

        }

        // step two: set IDs following the order in layers
        int ID = 0;
        for (SkLayer layerElm : graph.getGraphLayers()) // for each layer in all layers
            for (SkNode ndElm : layerElm.getLayerNodes()) // for each node in this layer
                ndElm.setId(ID++);

        System.out.println("Setting parents and children...");
        // step three: set parents and children
        for (int layerIdx=0; layerIdx<graph.getNumOfLayers()-1; layerIdx++) { // iterate each layer except the last one
            SkLayer currLayer = graph.getGraphLayer(layerIdx);
            for (SkNode currNode: currLayer.getLayerNodes()) // update each node in the current layer
                for (int searchLayerIdx=layerIdx+1; searchLayerIdx<graph.getNumOfLayers(); searchLayerIdx++) { // start searching from the next layer of current
                    SkLayer searchLayer = graph.getGraphLayer(searchLayerIdx);
                    for (SkNode searchNode: searchLayer.getLayerNodes()) // look for each node in the search layer
                        if (isDominate(currNode.getVal(), searchNode.getVal())) {
                            currNode.addChild(searchNode);
                            if (searchNode.getLayerIdx() < groupSize)
                                searchNode.addParent(currNode);
                        }
                }
        }
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
                                    SkGroup groupFound = new SkGroup(groupNodesChecked, gNode.getChildren());
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
            newGroupFound.addGroupNodes(node);
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
            SkGroup groupFound = new SkGroup(new ArrayList<>(candidates.getGroupDeque())); // finely calculate
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
            experimentBaseline.argumentsTrial(gSize, topK, dims, numOfPts, dir, spliter);
        } else { // without arguments, grid testing
            String spliter = "  ";
            String dir = "../data/";
            int stdGSize = 3;
            int stdTopK = 3;
            int stdDims = 3;
            int stdNOPt = 4;
            experimentTopKGP.setStandardParams(stdGSize, stdTopK, stdDims, stdNOPt);
            experimentBaseline.setStandardParams(stdGSize, stdTopK, stdDims, stdNOPt);
            /*int[] gSizeList = {2, 3, 4, 5};
            int[] topKList = {2, 3, 4, 5};*/
            int[] dimsList = {2, 3, 4, 5, 6, 7, 8};
            /*int[] numOfPtsList = {6};*/
            String resultsDir = "../results/";
            experimentTopKGP.saveTrialResults("D", dimsList, dir, spliter,  resultsDir+"dimensionsChangesGP");
            /*experimentTopKGP.saveTrialResults("GS", gSizeList, dir, spliter,  resultsDir+"groupSizeChangesGP");
            experimentTopKGP.saveTrialResults("K", topKList, dir, spliter,  resultsDir+"topKChangesGP");
            experimentTopKGP.saveTrialResults("PT", numOfPtsList, dir, spliter,  resultsDir+"numOfPointsChangesGP");*/
            // baseline
            int[] dimsListBaseline = {2, 3};
            experimentBaseline.saveTrialResults("D", dimsListBaseline, dir, spliter,  resultsDir+"dimensionsChangesGP_Baseline");
            /*experimentBaseline.saveTrialResults("GS", gSizeList, dir, spliter,  resultsDir+"groupSizeChangesGP_Baseline");
            experimentBaseline.saveTrialResults("K", topKList, dir, spliter,  resultsDir+"topKChangesGP_Baseline");
            experimentBaseline.saveTrialResults("PT", numOfPtsListBaseline, dir, spliter,  resultsDir+"numOfPointsChangesGP_Baseline");*/
        }

    }


}

