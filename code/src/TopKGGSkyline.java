import java.io.FileNotFoundException;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by mashiru on 2/24/18.
 */
public class TopKGGSkyline extends TopKGPSkyline {
    public TopKGGSkyline(int gSize, int topK) {
        super(gSize, topK);
    }

    public List<SkGroup> getTopKGroups(SkGraph graph, boolean silent) {
        TopKGroup topKGroup = new TopKGroup(topK);
        List<SkGroup> universeGroups = genUniverseGroups(graph);
        return topKGroup.getTopKGroup();
    }

    // using group-wise method
    protected List<SkGroup> genUniverseGroups(SkGraph graph) {
        List<SkGroup> universeGroups = new ArrayList<>();
        List<UnitGroup> unitGroups = new ArrayList<>();
        List<SkNode> tailSet = new ArrayList<>();
        // start from the groupSize-1 th layer, since the nodes in the next layers must have more than groupSize-1 parents
        for (int LIdx = groupSize-1; LIdx>=0; LIdx--) {
            SkLayer currLayer = graph.getGraphLayer(LIdx);
            for (int NIdx = currLayer.getLayerSize()-1; NIdx>=0; NIdx--) {
                SkNode currNode = currLayer.getLayerNode(NIdx);
                int parentsSize = currNode.getParents().size();
                if (parentsSize < groupSize - 1) {// even the node in current layer still has the prob of having more than groupSize -1 parents
                    unitGroups.add(new UnitGroup(currNode)); //  mark the currNode as a unit group and add into the tail set
                    tailSet.add(currNode);
                } else if (parentsSize == groupSize - 1) { // find one group
                    SkGroup group = new SkGroup(currNode);
                    group.addGroupNodes(currNode.getParents()); // add its parents nodes
                    universeGroups.add(group); // add into the universe set
                }
            }
        }
        searchGroupsByUnit(unitGroups, tailSet, universeGroups);
        return universeGroups;
    }

    protected void searchGroupsByUnit(List<UnitGroup> unitGroups, List<SkNode> tailSet, List<SkGroup> universeGroups) {
        if (unitGroups.size() == 0) // no unit groups needs to be search
            return;
        List<UnitGroup> newUnitGroups = new ArrayList<>(); // the unit groups for next recursion
        for (UnitGroup ugroup: unitGroups)
            for (int UIdx=tailSet.indexOf(ugroup.getLastNodeInUnit())+1; UIdx<tailSet.size(); UIdx++) {
                SkNode checkedNode = tailSet.get(UIdx);
                if (!ugroup.getCoveredSkGroupNodes().contains(checkedNode)) { // if the unitGroup node is not contained in the covered group nodes
                    UnitGroup newUgroup = new UnitGroup(ugroup); // copy of ugroup
                    newUgroup.addUnitGroupNodes(checkedNode); // add the checkedNode into the unit group
                    if (newUgroup.getCoveredSkGroupSize() < groupSize)
                        newUnitGroups.add(newUgroup); // add the unit group into the list for next recursion
                    else if (newUgroup.getCoveredSkGroupSize() == groupSize)
                        universeGroups.add(newUgroup.getCoveredSkGroup()); // add the covered group into the universe groups
                }
            }
        searchGroupsByUnit(newUnitGroups, tailSet, universeGroups);
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
        List<SkGroup> topKGroups = testGG.getTopKGroups(graphTopk, silent);
        long end2 = System.nanoTime();
        timeSumTopK = timeSumTopK + end2 - start2;

        System.out.println("TopK Group-Group Skyline    Time: " + timeSumTopK);




    }
}


