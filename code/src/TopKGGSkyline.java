import java.io.FileNotFoundException;
import java.util.Comparator;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by mashiru on 2/24/18.
 */
public class TopKGGSkyline extends TopKGPSkyline {
    protected int numOfUniverseGroups;
    // for information checking
    static long count = 0;
    static long calCount = 0;
    //
    public TopKGGSkyline(int gSize, int topK) {
        super(gSize, topK);
        numOfUniverseGroups = 0;
    }

    public TopKGGSkyline(int gSize, int topK, boolean smallerPref) {
        super(gSize, topK, smallerPref);
        numOfUniverseGroups = 0;
    }

    public int getNumOfUniverseGroups() { return numOfUniverseGroups; }

    public List<SkGroup> getTopKGroups(SkGraph graph, boolean silent) {
        return getTopKGroups(graph, true, false, silent);
    }

    public List<SkGroup> getTopKGroups(SkGraph graph, boolean refined, boolean stepwise, boolean silent) { // baseline refined: false; else true
        System.out.println("Group-Group getTopKGroups");
        TopKGroup topKGroup = searchTopKGroups(graph, refined, stepwise); // search for topK
        if (!silent) topKGroup.print();
        return topKGroup.getTopKGroup();
    }

    protected void checkGroups4TopK(SkGroup group4Checked, TopKGroup topKGroup, boolean refined, boolean stepwise) {
        if (topKGroup.getTopKGroup().contains(group4Checked))
            return;
        // if topKGroup is full and the maximum size of dominated group of ugroup is smaller than the minimum in the topKGroup, then skip
        if (refined && topKGroup.getTopKGroupSize() == topK && group4Checked.getMaxSizeOfDominatedGroups() <  10 * topKGroup.getMinSizeOfDominatedGroups())
            return;
        // calculate the dominated groups using approximation method, only care about the children in the first groupSize layers
        Collections.sort(group4Checked.getGroupNodes(), new Comparator<SkNode>() {
            @Override
            public int compare(SkNode node1, SkNode node2) {
                return node2.getId() - node1.getId();
            }
        });

        if (!topKGroup.getTopKGroup().contains(group4Checked)) { // re-check containing after sorting
            group4Checked.calculateDominatedGroups(stepwise);
            // System.out.println("Group-Group checkGroups4TopK calculated count: " + (calCount++));
            topKGroup.addSkGroup(group4Checked); // add into the topKGroup
        }
    }

    protected TopKGroup initialTopKGroups(SkGraph graph, boolean refined, boolean stepwise) { // baseline refined: false
        System.out.println("Group-Group initialTopKGroups");
        TopKGroup topKGroup = new TopKGroup(topK, true);
        TopKGPSkyline gp = new TopKGPSkyline(getGroupSize(), getK(), getSmallerPref());
        List<SkGroup> groups = gp.getTopKGroups(graph, !refined, true);
        for (SkGroup group: groups) {
            Collections.sort(group.getGroupNodes(), new Comparator<SkNode>() {
                @Override
                public int compare(SkNode node1, SkNode node2) {
                    return node2.getId() - node1.getId();
                }
            });

            if (!topKGroup.getTopKGroup().contains(group)) {
                group.calculateDominatedGroups(stepwise);
                topKGroup.addSkGroup(group);
            }
        }
        System.out.println("Group-Group initialTopKGroups done");
        return topKGroup;
    }

    // using group-wise method
    protected TopKGroup searchTopKGroups(SkGraph graph, boolean refined, boolean stepwise) {
        System.out.println("Group-Group searchTopKGroups");
        // TopKGroup topKGroup = new TopKGroup(topK, true);
        // TODO: prepare initial topK using results from pointed-dominated GSkyline
        TopKGroup topKGroup = initialTopKGroups(graph, refined, stepwise);
        List<UnitGroup> unitGroups = new ArrayList<>();
        List<SkNode> tailSet = new ArrayList<>();
        // start from the groupSize-1 th layer, since the nodes in the next layers must have more than groupSize-1 parents
        for (int LIdx = groupSize-1; LIdx>=0; LIdx--) {
            SkLayer currLayer = graph.getGraphLayer(LIdx);
            for (int NIdx = currLayer.getLayerSize()-1; NIdx>=0; NIdx--) {
                SkNode currNode = currLayer.getLayerNode(NIdx);
                int parentsSize = currNode.getParents().size();
                if (parentsSize < groupSize - 1) {// even the node in current layer still has the prob of having more than groupSize -1 parents
                    unitGroups.add(new UnitGroup(new SkNode(currNode))); //  mark the currNode as a unit group and add into the tail set
                    tailSet.add(currNode);
                } else if (parentsSize == groupSize - 1) { // find one group
                    numOfUniverseGroups ++;
                    System.out.println("number of Universe Groups: " + (numOfUniverseGroups));
                    SkGroup group = new SkGroup("GG", currNode.getParents()); // add its parents nodes
                    group.addGroupNode(currNode);
                    checkGroups4TopK(group, topKGroup, refined, stepwise);
                }
            }
        }
        searchGroupsByUnit(unitGroups, tailSet, topKGroup, refined, stepwise);
        return topKGroup;
    }

    protected void searchGroupsByUnit(List<UnitGroup> unitGroups, List<SkNode> tailSet, TopKGroup topKGroup, boolean refined, boolean stepwise) {
        System.out.println("Group-Group searchGroupsByUnit "+unitGroups.size());
        if (unitGroups.size() == 0) // no unit groups needs to be search
            return;
        List<UnitGroup> newUnitGroups = new ArrayList<>(); // the unit groups for next recursion
        for (UnitGroup ugroup: unitGroups) {
            List<SkNode> ugroupCover = ugroup.getCoveredSkGroupNodes();
            for (int UIdx = tailSet.indexOf(ugroup.getLastNodeInUnit()) + 1; UIdx < tailSet.size(); UIdx++) {
                SkNode checkedNode = tailSet.get(UIdx);
                if (!ugroupCover.contains(checkedNode)) { // if the unitGroup node is not contained in the covered group nodes
                    UnitGroup newUgroup = new UnitGroup(ugroup); // copy of ugroup
                    newUgroup.addUnitGroupNodes(checkedNode, true); // add the checkedNode into the unit group
                    /*UnitGroup newUgroup = new UnitGroup(checkedNode); // new a unit group with a unit group node - checkedNode
                    newUgroup.addUnitGroup(ugroup); // add unit group*/
                    if (newUgroup.getCoveredSkGroupSize() < groupSize-1) {
                        newUgroup.addSelf2GroupNode(checkedNode);
                        newUnitGroups.add(newUgroup); // add the unit group into the list for next recursion
                    } else if (newUgroup.getCoveredSkGroupSize() == groupSize-1) {
                        numOfUniverseGroups ++;
                        newUgroup.addSelf2GroupNode(checkedNode);
                        checkGroups4TopK(newUgroup.getCoveredSkGroup(), topKGroup, refined, stepwise); // check the covered group for topK
                    }
                }
            }
        }
        searchGroupsByUnit(newUnitGroups, tailSet, topKGroup, refined, stepwise);
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
            double scale = 1; // default scale is 1
            if (args.length > 4)
                scale = Double.parseDouble(args[4]); //  scale of the num of points
            experimentTopKGG.argumentsTrial(gSize, topK, dims, numOfPts, scale, dir, spliter);
            // experimentBaseline.argumentsTrial(gSize, topK, dims, numOfPts, scale, dir, spliter);
        } else { // without arguments, grid testing
            String spliter = "  ";
            String dir = "../data/";
            /*// scale test
            int stdGSize = 3;
            int stdTopK = 3;
            int stdDims = 3;
            int stdNOPt = 3;
            double stdScal = 1;*/

            //NBA
            int stdGSize = 5;
            int stdTopK = 3;
            int stdDims = 5;
            int stdNOPt = 3;
            double stdScal = 1;

            experimentTopKGG.setStandardParams(stdGSize, stdTopK, stdDims, stdNOPt, stdScal);
            experimentBaseline.setStandardParams(stdGSize, stdTopK, stdDims, stdNOPt, stdScal);
            /*int[] gSizeList = {2, 3, 4, 5};
            int[] topKList = {2, 3, 4, 5};
            int[] dimsList = {2, 3, 4, 5, 6, 7, 8};
            int[] numOfPtsList = { 3, 4, 5, 6};*/
            int[] numOfPtsList = {3}; //NBA
            //int[] numOfPtsList = {3}; //test
            String resultsDir = "../results/";
            /*experimentTopKGG.saveTrialResults("K", topKList, dir, spliter,  resultsDir+"topKChangesGG");
            experimentTopKGG.saveTrialResults("PT", numOfPtsList, dir, spliter,  resultsDir+"numOfPointsChangesGG");
            experimentTopKGG.saveTrialResults("GS", gSizeList, dir, spliter,  resultsDir+"groupSizeChangesGG");
            experimentTopKGG.saveTrialResults("D", dimsList, dir, spliter,  resultsDir+"dimensionsChangesGG");*/
            //experimentTopKGG.saveTrialResults("PT", numOfPtsList, dir, spliter,  resultsDir+"test4Results"); // test
            experimentTopKGG.saveTrialResults("PT", numOfPtsList, dir, spliter,  resultsDir+"NBAGG"); // NBA

            /*// baseline
            experimentBaseline.saveTrialResults("GS", gSizeList, dir, spliter,  resultsDir+"groupSizeChangesGG_Baseline");
            experimentBaseline.saveTrialResults("K", topKList, dir, spliter,  resultsDir+"topKChangesGG_Baseline");
            experimentBaseline.saveTrialResults("D", dimsList, dir, spliter,  resultsDir+"dimensionsChangesGG_Baseline");
            experimentBaseline.saveTrialResults("PT", numOfPtsList, dir, spliter,  resultsDir+"numOfPointsChangesGG_Baseline");*/
        }
    }
}


