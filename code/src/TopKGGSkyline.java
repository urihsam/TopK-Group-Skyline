import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

/**
 * Created by mashiru on 2/24/18.
 */
public class TopKGGSkyline extends TopKGPSkyline {
    public TopKGGSkyline(int gSize, int topK) {
        super(gSize, topK);
    }

    public List<SkGroup> getTopKGroups(SkGraph graph, boolean universe, boolean silent) {
        TopKGroup topKGroup = new TopKGroup(topK);
        // PriorityQueue<SkGroup> topKGroups = new PriorityQueue<SkGroup>(topK, Collections.reverseOrder());
        List<SkNode> firstLayer = graph.graphLayers.get(0).getLayerNodes();
        Collections.sort(firstLayer, Collections.reverseOrder()); // sort the top layer of the graph
        // checkCombination(firstLayer, groupSize, new SkGroup(), topKGroup);
        List<SkGroup> universeGroup = universe?new ArrayList<SkGroup>():null;
        checkPostCombination(firstLayer, new GroupCandidates(groupSize), topKGroup, universe, universeGroup);
        if (!silent) topKGroup.print();
        List<SkGroup> groups4Check = universe?universeGroup:new ArrayList<SkGroup>(topKGroup.getTopKGroup());
        Collections.reverse(groups4Check);
        checkChildren4TopKG(groups4Check, groupSize, topKGroup);
        if (!silent) topKGroup.print();
        return topKGroup.getTopKGroup();
    }

    protected void checkChildren4TopKG(List<SkGroup> groups4Check, int g, TopKGroup topKG) {
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
                                    checkPostCombination(groupNodesRemain, new GroupCandidates(groupFound, g), topKG);
                                }
                        }else if (nChild.getLayerIdx() > depth)
                            break; // change to another node, skip the rest children
                    }
    }

    // Pre-combine, subgroup is combined first, then try to look for the rest
    protected void checkCombination(List<SkNode> nodes4Check, int g, SkGroup groupFound, TopKGroup topKG) {
        if (g == 0 && !topKG.getTopKGroup().contains(groupFound)) {
            topKG.addSkGroup(groupFound);
            return;
        }
        for (int idx=0; idx<nodes4Check.size(); idx++) {
            SkNode node = nodes4Check.get(idx);
            if (topKG.getTopKGroupSize() == topK && node.getDominates() + groupFound.getGroupDominates() <= topKG.getMinDominates())
                return;
            SkGroup newGroupFound = new SkGroup(groupFound); // copy of groupFound
            newGroupFound.addGroupNodes(node);
            checkCombination(nodes4Check.subList(idx+1, nodes4Check.size()), g-1, newGroupFound, topKG);
            newGroupFound = null; //  remove the reference of newGroupFound to delete this object
        }
    }

    // Post-combine, first pick nodes by order, then try to group them when there are number of nodes picked
    protected void checkPostCombination(List<SkNode> nodes4Check, GroupCandidates candidates, TopKGroup topKG) {
        checkPostCombination(nodes4Check, candidates, topKG, false, null);
    }

    protected void checkPostCombination(List<SkNode> nodes4Check, GroupCandidates candidates, TopKGroup topKG, boolean universe, List<SkGroup> universeGroups) {
        if (candidates.getNumOfCandidates() == candidates.getMaxSize()) {
            int minDominates = topKG.getMinDominates();
            SkGroup groupFound = new SkGroup(new ArrayList<>(candidates.getGroupDeque())); // finely calculate
            if (universe)
                universeGroups.add(groupFound);
            if (topKG.getTopKGroupSize() != topK || (groupFound.getGroupDominates() > minDominates && !topKG.getTopKGroup().contains(groupFound)))
                topKG.addSkGroup(groupFound);
            return;
        }
        for (int nIdx=0; nIdx<nodes4Check.size(); nIdx++) {
            SkNode currNode = nodes4Check.get(nIdx);
            if (candidates.getNumOfCandidates() == candidates.getMaxSize()-1) {
                int minDominates = topKG.getMinDominates();
                // If topKG is full and rough result is not larger than the min of the topKG, skip it
                if (topKG.getTopKGroupSize() == topK && currNode.getDominates() + candidates.getTotalChilldren() <= minDominates)
                    continue;
            }
            // push the currNode as a candidate
            candidates.pushGroupNode(currNode);
            checkPostCombination(nodes4Check.subList(nIdx + 1, nodes4Check.size()), candidates, topKG, universe, universeGroups);
            candidates.popGroupNode();
        }
    }

    public static void main(String[] args) throws FileNotFoundException {
        int gSize = Integer.parseInt(args[0]); // group size
        int topK = Integer.parseInt(args[1]); // top k
        TopKGGSkyline testGG = new TopKGGSkyline(gSize, topK);
        List<Integer[]> data = readData("testdata");

        long timeSumBaseline = 0;
        long timeSumTopK = 0;
        long creatGraphTime = 0;

        // create layers
        // twoD or higherD for computing layers
        long cStartT = System.nanoTime();
        SkGraph graph = testGG.createLayerGraph(data);// build the graph
        long cEndT = System.nanoTime();
        creatGraphTime = creatGraphTime + (cEndT - cStartT);


        boolean silent = true;
        SkGraph graphTopk = graph;

        long start2 = System.nanoTime();
        // nodesTopk
        List<SkGroup> topKGroups = testGG.getTopKGroups(graphTopk, true, silent);
        long end2 = System.nanoTime();
        timeSumTopK = timeSumTopK + end2 - start2;

        System.out.println("TopK Group-Group Skyline    Time: " + timeSumTopK);




    }
}


