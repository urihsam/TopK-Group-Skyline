import java.io.FileNotFoundException;
import java.util.Comparator;
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
        return getTopKGroups(graph, true, silent);
    }

    public List<SkGroup> getTopKGroups(SkGraph graph, boolean refined, boolean silent) {
        System.out.println("Group-Group getTopKGroups");
        List<SkGroup> universeGroups = getUniverseGroups(graph); // get universe set of groups
        TopKGroup topKGroup = searchUniverseGroups4TopK(universeGroups, refined); // search for topK
        if (!silent) topKGroup.print();
        return topKGroup.getTopKGroup();
    }

    protected TopKGroup searchUniverseGroups4TopK(List<SkGroup> universeGroups, boolean refined) {
        TopKGroup topKGroup = new TopKGroup(topK, true);
        for (SkGroup ugroup: universeGroups) { // for each group in the universe
            // if topKGroup is full and the maximum size of dominated group of ugroup is smaller than the minimum in the topKGroup, then skip
            if (refined && topKGroup.getTopKGroupSize() == topK && ugroup.getMaxSizeOfDominatedGroups() < topKGroup.getMinSizeOfDominatedGroups())
                continue;
            ugroup.calculateDominatedGroups(); // calculate the dominated groups
            topKGroup.addSkGroup(ugroup); // add into the topKGroup
        }
        return topKGroup;
    }

    // using group-wise method
    protected List<SkGroup> getUniverseGroups(SkGraph graph) {
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
                    SkGroup group = new SkGroup(currNode.getParents());
                    group.addGroupNodes(currNode); // add its parents nodes
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
                    /*UnitGroup newUgroup = new UnitGroup(ugroup); // copy of ugroup
                    newUgroup.addUnitGroupNodes(checkedNode); // add the checkedNode into the unit group*/
                    UnitGroup newUgroup = new UnitGroup(checkedNode); // new a unit group with a unit group node - checkedNode
                    newUgroup.addUnitGroup(ugroup); // add unit group
                    if (newUgroup.getCoveredSkGroupSize() < groupSize)
                        newUnitGroups.add(newUgroup); // add the unit group into the list for next recursion
                    else if (newUgroup.getCoveredSkGroupSize() == groupSize)
                        universeGroups.add(newUgroup.getCoveredSkGroup()); // add the covered group into the universe groups
                }
            }
        searchGroupsByUnit(newUnitGroups, tailSet, universeGroups);
    }


    public static void main(String[] args) {
        Experiment experimentBaseline = new Experiment("GG", true);
        Experiment experimentTopKGG = new Experiment("GG", false);
        if (args.length > 0) { // with arguments
            String spliter = "  ";
            String dir = "../data/";
            int gSize = Integer.parseInt(args[0]); // group size
            int topK = Integer.parseInt(args[1]); // top k
            int dims = Integer.parseInt(args[2]); // dimensions
            int numOfPts = Integer.parseInt(args[3]); // the exponent X of 1eX

            experimentTopKGG.argumentsTrial(gSize, topK, dims, numOfPts, dir, spliter);
            experimentBaseline.argumentsTrial(gSize, topK, dims, numOfPts, dir, spliter);
        } else { // without arguments, grid testing
            String spliter = "  ";
            String dir = "../data/";
            int stdGSize = 3;
            int stdTopK = 3;
            int stdDims = 2;
            int stdNOPt = 3;
            experimentTopKGG.setStandardParams(stdGSize, stdTopK, stdDims, stdNOPt);
            experimentBaseline.setStandardParams(stdGSize, stdTopK, stdDims, stdNOPt);
            int[] gSizeList = {2, 3, 4, 5, 8};
            int[] topKList = {2, 4, 5, 8};
            int[] dimsList = {2, 3, 4, 5};
            int[] numOfPtsList = { 3, 4, 5, 6};
            String resultsDir = "../results/";
            experimentTopKGG.saveTrialResults("GS", gSizeList, dir, spliter,  resultsDir+"groupSizeChangesGG");
            experimentTopKGG.saveTrialResults("K", topKList, dir, spliter,  resultsDir+"topKChangesGG");
            experimentTopKGG.saveTrialResults("D", dimsList, dir, spliter,  resultsDir+"dimensionsChangesGG");
            experimentTopKGG.saveTrialResults("PT", numOfPtsList, dir, spliter,  resultsDir+"numOfPointsChangesGG");

            // baseline
            experimentBaseline.saveTrialResults("GS", gSizeList, dir, spliter,  resultsDir+"groupSizeChangesGG_Baseline");
            experimentBaseline.saveTrialResults("K", topKList, dir, spliter,  resultsDir+"topKChangesGG_Baseline");
            experimentBaseline.saveTrialResults("D", dimsList, dir, spliter,  resultsDir+"dimensionsChangesGG_Baseline");
            experimentBaseline.saveTrialResults("PT", numOfPtsList, dir, spliter,  resultsDir+"numOfPointsChangesGG_Baseline");
        }
    }
}


