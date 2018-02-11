/**
 * Created by mashiru on 2/10/18.
 */
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Scanner;

public class TopKGSkyline {
    public int groupSize;
    public long numOfGroups1; // Unit group wise
    public long numOfGroups2; // Point wise
    public long numOfGroups3; // New brute force
    public long numOfGroups4; // Point wise with direct parents&children
    public long numOfGroups5; // Unit group wise of DFS version

    public int numOfChecked1; // How many times checked for Unit group wise
    public int numOfCheckDFS;
    public int numOfChecked2; // How many times checked for Point wise normal
    public int numOfChecked3; // How many times checked for Point wise direct

    public static int numOfNodes;

    public static long StempDtime;
    public static long Stemptime;
    public static long timeSum1;
    public static long timeSum2;
    public static long timeSum3;
    public static long timeSum4;
    public static long creatLayer;
    public static long creatRelation;
    public static long creatRelationD;
    public static long creatRelationDFS;
    public static int dataR;// data range

    public ArrayList<ArrayList<SkNode>> skGroupsP; // skyline groups generated
    // by point wise
    public ArrayList<ArrayList<SkNode>> skGroupsPD; // skyline groups generated
    // by point wise with direct
    // parents&children

    public int generateTimes;

    public TopKGSkyline() {
        groupSize = 0;
        numOfGroups1 = 0;
        numOfGroups2 = 0;
        numOfGroups3 = 0;
        numOfGroups4 = 0;
        numOfGroups5 = 0;

        numOfChecked1 = 0;
        numOfCheckDFS = 0;
        numOfChecked2 = 0;
        numOfChecked3 = 0;

        numOfNodes = 0;

        StempDtime = 0;
        Stemptime = 0;
        timeSum1 = 0;
        timeSum2 = 0;
        timeSum3 = 0;
        timeSum4 = 0;
        creatLayer = 0;
        creatRelation = 0;
        creatRelationD = 0;
        creatRelationDFS = 0;
        dataR = 10000000;

        skGroupsP = new ArrayList<ArrayList<SkNode>>();
        skGroupsPD = new ArrayList<ArrayList<SkNode>>();
    }

    // if point a could dominate point b
    public boolean isDominate(Integer[] a, Integer[] b) {
        int mark = 0;

        for (int i = 0; i < a.length; i++) {
            if (a[i] > b[i])
                return false;
            else if (a[i] == b[i])
                mark++;
        }

        if (mark == a.length)
            return false;
        else
            return true;
    }

    public void subPermutation(ArrayList<ArrayList<Integer[]>> groups,
                               ArrayList<Integer[]> data, ArrayList<Integer[]> tar, int groupSize) {
        if (groupSize == 0) {
            groups.add(tar);
            return;
        }

        for (Integer[] i : data)
            if (!tar.contains(i)) {
                ArrayList<Integer[]> aa = new ArrayList<Integer[]>();
                aa.addAll(tar);
                aa.add(i);
                subPermutation(groups, data, aa, groupSize - 1);
            }
    }

    // create full permutation list for list a
    public ArrayList<ArrayList<Integer[]>> createFullPermutation(
            ArrayList<Integer[]> a) {
        ArrayList<ArrayList<Integer[]>> fullPermutation = new ArrayList<ArrayList<Integer[]>>();

        for (int i = 0; i < a.size(); i++) {
            ArrayList<Integer[]> aa = new ArrayList<Integer[]>();
            aa.add(a.get(i));
            subPermutation(fullPermutation, a, aa, a.size() - 1);
        }
        return fullPermutation;
    }

    // if group of points a could dominate group of points b
    public boolean isGroupDominate(ArrayList<Integer[]> a,
                                   ArrayList<Integer[]> b) {
        // create the group of full permutation of a
        ArrayList<ArrayList<Integer[]>> fullPermutation = createFullPermutation(a);

        // check if all permutation of a could dominate b
        for (ArrayList<Integer[]> i : fullPermutation) {
            boolean mark = true;
            int equalMark = 0;

            for (int j = 0; j < a.size(); j++) {
                equalMark = 0;
                boolean equal = true;
                for (int groupSize = 0; groupSize < i.get(j).length; groupSize++)
                    if (i.get(j)[groupSize] != b.get(j)[groupSize]) {
                        equal = false;
                        break;
                    }
                // the points have the same value
                if (equal)
                    equalMark++;
                else {
                    if (!isDominate(i.get(j), b.get(j))) {
                        mark = false;
                        break;
                    }
                }
            }

            // one permutation of a could dominate b
            if (mark && (equalMark < a.size()))
                return true;
        }
        return false;
    }

    public void subGroups(ArrayList<ArrayList<Integer[]>> groups,
                          ArrayList<Integer[]> data, ArrayList<Integer[]> tar, int start,
                          int groupSize) {
        if (groupSize == 0) {
            groups.add(tar);
            return;
        }

        for (int i = start + 1; i < data.size(); i++) {
            ArrayList<Integer[]> a = new ArrayList<Integer[]>();
            a.addAll(tar);
            a.add(data.get(i));
            subGroups(groups, data, a, i, groupSize - 1);
        }
    }

    public void createSubs(ArrayList<SkNode> group, ArrayList<SkNode> layers,
                           int start, int num) {
        if (num == 0) {
            for (SkNode i : group)
                for (SkNode j : i.parents)
                    if (!group.contains(j))
                        return;
            numOfGroups3++;
            return;
        }

        for (int i = start + 1; i < layers.size(); i++) {
            ArrayList<SkNode> newGroup = new ArrayList<SkNode>();
            newGroup.addAll(group);
            newGroup.add(layers.get(i));
            createSubs(newGroup, layers, i, num - 1);
        }
    }

    // new brute force - using unit groups
    public void newBruteForce(ArrayList<SkNode> layers) {

        for (int i = 0; i < layers.size(); i++) {
            ArrayList<SkNode> group = new ArrayList<SkNode>();
            group.add(layers.get(i));
            createSubs(group, layers, i, groupSize - 1);
        }

        System.out.println("The number of skyline groups               by baseline method: "
                + numOfGroups3);
    }


    // create layers: Not brute force & Higher dimension
    public ArrayList<SkNode> createLayersD(ArrayList<Integer[]> data) {
        ArrayList<SkNode> results = new ArrayList<SkNode>();
        // ArrayList<Integer[]> sorted = new ArrayList<Integer[]>();

        // sort the points
        // sorted.addAll(data);
        Collections.sort(data, new Comparator<Object>() {
            @Override
            public int compare(Object o1, Object o2) {
                if (o1 instanceof Integer[])
                    return (compare((Integer[]) o1, (Integer[]) o2));
                return 0;
            }

            public int compare(Integer[] o1, Integer[] o2) {
                return (o1[0] < o2[0] ? -1 : (o1[0] == o2[0] ? 0 : 1));
            }

        });

        // first point belongs to the first layer
        SkNode pt0 = new SkNode(data.get(0), 0);
        ArrayList<ArrayList<SkNode>> layers = new ArrayList<ArrayList<SkNode>>();
        ArrayList<SkNode> layer = new ArrayList<SkNode>();
        layer.add(pt0);
        layers.add(layer);

        // no more than groupSize layers will be used
        for (int layerIdx = 1; layerIdx < groupSize; layerIdx++)
            layers.add(new ArrayList<SkNode>()); // add new layer

        for (int ptIdx = 1; ptIdx < data.size(); ptIdx++) {
            SkNode pt = new SkNode(data.get(ptIdx), 0);
            int maxLayer = -1;
            for (ArrayList<SkNode> layerElm : layers) // for each layer in all layers
                for (SkNode ndElm : layerElm) // for each node in this layer
                    if (isDominate(ndElm.val, pt.val) && ndElm.layer > maxLayer)
                        maxLayer = ndElm.layer;
            pt.layer = ++maxLayer;
            if (pt.layer < groupSize)
                layers.get(pt.layer).add(pt);
        }

        for (ArrayList<SkNode> layerElm : layers)
            for (SkNode ndElm : layerElm)
                results.add(ndElm);
        return results;
    }

    // normal create layers relation
    public void createLayerStruct(ArrayList<SkNode> nodes) {
        for (SkNode nodeI : nodes) {
            nodeI.setId(nodes.indexOf(nodeI)); // Set the Id of point according to the order in the list
            for (SkNode nodeJ : nodes)
                if (nodeI != nodeJ)
                    if (isDominate(nodeJ.val, nodeI.val))
                        nodeI.parents.add(nodeJ);
        }

        Iterator<SkNode> nodeIter = nodes.iterator();
        while (nodeIter.hasNext()) {
            SkNode pt = nodeIter.next();
            numOfNodes++;
            if (pt.parents.size() > groupSize - 1)
                nodeIter.remove(); // Removes from the underlying collection the last element returned by this iterator (optional operation).
        }

        for (SkNode nodeI : nodes) {
            nodeI.setId(nodes.indexOf(nodeI)); // ? repeat set ID?
            for (SkNode nodeJ : nodes)
                if (nodeI != nodeJ)
                    if (isDominate(nodeI.val, nodeJ.val)) // why did not add into line 460
                        nodeI.children.add(nodeJ);
        }
    }

    public void generateUnitsDFS(ArrayList<ArrayList<SkNode>> units,
                                 ArrayList<SkNode> std) {
        ArrayList<ArrayList<SkNode>> newUnits = new ArrayList<ArrayList<SkNode>>();

        if (units.size() == 0)
            return;

        // go through all units in current layer
        for (ArrayList<SkNode> i : units) {
            ArrayList<SkNode> total = new ArrayList<SkNode>();

            // get the whole group of current unit
            total.addAll(i.get(0).parents);
            total.add(i.get(0));
            for (int j = 1; j < i.size(); j++) {
                total = merge(total, i.get(j).parents);
                total.add(i.get(j));
            }

            for (int j = std.indexOf(i.get(i.size() - 1)) + 1; j < std.size(); j++) {

                if (!total.contains(std.get(j))) {
                    ArrayList<SkNode> newTotal = merge(total,
                            std.get(j).parents);

                    numOfCheckDFS++;

                    if (newTotal.size() < groupSize - 1) {
                        ArrayList<SkNode> newUnit = new ArrayList<SkNode>();
                        newUnit.addAll(i);
                        newUnit.add(std.get(j));
                        newUnits.add(newUnit);
                    } else if (newTotal.size() == groupSize - 1) {
                        numOfGroups1++;

                        // for(SkNode kk:newTotal)
                        // System.out.println(kk.val[0] + ", " + kk.val[1]+
                        // "  id is  " + (kk.id+1));
                        // System.out.println(std.get(j).val[0] + ", " +
                        // std.get(j).val[1]+ "  id is  " + (std.get(j).id+1) +
                        // '\n');

                    }
                }
            }
        }
        generateUnitsDFS(newUnits, std);
    }

    // Unit group wise method
    public void unitGroupWiseDFS(ArrayList<SkNode> layers) {
        ArrayList<ArrayList<SkNode>> unitGroups = new ArrayList<ArrayList<SkNode>>();
        ArrayList<SkNode> std = new ArrayList<SkNode>();

        for (int i = layers.size() - 1; i >= 0; i--)
            // for(SkNode i:layers)
            if (layers.get(i).parents.size() < groupSize - 1) {
                ArrayList<SkNode> unit = new ArrayList<SkNode>();
                // unit.addAll(i.parents);
                std.add(layers.get(i));
                unit.add(layers.get(i));
                unitGroups.add(unit);
            }
            // output skyline group
            else if (layers.get(i).parents.size() == groupSize - 1) {
                numOfGroups1++;
                // for(SkNode j:layers.get(i).parents)
                // System.out.println(j.val[0] + ", " + j.val[1] + "  id is  " +
                // (j.id+1));
                // System.out.println(layers.get(i).val[0] + ", "
                // +layers.get(i).val[1]+ "  id is  " + (layers.get(i).id+1) +
                // '\n');
            }

        generateUnitsDFS(unitGroups, std);

        System.out
                .println("The number of skyline groups generated by unit DFS group wise: "
                        + numOfGroups1);
    }

    public void generateUnits(ArrayList<ArrayList<SkNode>> units,
                              ArrayList<SkNode> std) {
        ArrayList<ArrayList<SkNode>> newUnits = new ArrayList<ArrayList<SkNode>>();

        if (units.size() == 0)
            return;

        // go through all units in current layer
        for (ArrayList<SkNode> i : units) {
            ArrayList<SkNode> total = new ArrayList<SkNode>();
            ArrayList<SkNode> allChildren = new ArrayList<SkNode>();

            // get the whole group of current unit
            total.addAll(i.get(0).parents);
            total.add(i.get(0));
            for (int j = 1; j < i.size(); j++) {
                total = merge(total, i.get(j).parents);
                total.add(i.get(j));
            }

            // get all children of current unit
            allChildren.addAll(i.get(0).children);
            for (int j = 1; j < i.size(); j++)
                allChildren = merge(allChildren, i.get(j).children);

            for (int j = std.indexOf(i.get(i.size() - 1)) + 1; j < std.size(); j++) {

                if (!allChildren.contains(std.get(j))) {
                    ArrayList<SkNode> newTotal = merge(total,
                            std.get(j).parents);

                    numOfChecked1++;

                    if (newTotal.size() < groupSize - 1) {
                        ArrayList<SkNode> newUnit = new ArrayList<SkNode>();
                        newUnit.addAll(i);
                        newUnit.add(std.get(j));
                        newUnits.add(newUnit);
                    } else if (newTotal.size() == groupSize - 1) {
                        numOfGroups5++;

                        // for(SkNode kk:newTotal)
                        // System.out.println(kk.val[0] + ", " + kk.val[1]);
                        // System.out.println(std.get(j).val[0] + ", " +
                        // std.get(j).val[1] + '\n');

                    }
                }
            }
        }
        generateUnits(newUnits, std);
    }

    // Unit group wise method
    public void unitGroupWise(ArrayList<SkNode> layers) {
        ArrayList<ArrayList<SkNode>> unitGroups = new ArrayList<ArrayList<SkNode>>();
        ArrayList<SkNode> std = new ArrayList<SkNode>();

        // ArrayList<SkNode> layers = createLayersB(data);
        // createLayerStruct(layers);

        for (SkNode i : layers)
            if (i.parents.size() < groupSize - 1) {
                ArrayList<SkNode> unit = new ArrayList<SkNode>();
                // unit.addAll(i.parents);
                std.add(i);
                unit.add(i);
                unitGroups.add(unit);
            }
            // output skyline group
            else if (i.parents.size() == groupSize - 1) {
                numOfGroups5++;
                // for(SkNode j:i.parents)
                // System.out.println(j.val[0] + ", " + j.val[1]);
                // System.out.println(i.val[0] + ", " + i.val[1] + '\n');
            }

        generateUnits(unitGroups, std);

        System.out
                .println("The number of skyline groups generated by unit     group wise: "
                        + numOfGroups5);
    }

    // Merge two group of points
    public ArrayList<SkNode> merge(ArrayList<SkNode> a, ArrayList<SkNode> b) {
        ArrayList<SkNode> result = new ArrayList<SkNode>();
        int left = 0;
        int right = 0;

        while (left < a.size() && right < b.size()) {
            if (a.get(left).id < b.get(right).id) {
                result.add(a.get(left));
                left++;
            } else if (a.get(left).id > b.get(right).id) {
                result.add(b.get(right));
                right++;
            } else {
                result.add(a.get(left));
                left++;
                right++;
            }
        }

        // append the rest
        if (left == a.size())
            for (int i = right; i < b.size(); i++)
                result.add(b.get(i));
        else
            for (int i = left; i < a.size(); i++)
                result.add(a.get(i));

        return result;
    }

    public boolean isSame(Integer[] a, Integer[] b) {
        int mark = 0;
        for (int i = 0; i < a.length; i++)
            if (a[i] == b[i])
                mark++;
            else
                break;
        return (mark == a.length ? true : false);
    }

    // Generate skyline groups for each groupSize
    public void generateGroups(ArrayList<SkNode> group, ArrayList<SkNode> std,
                               int num) {
        if (num == groupSize) {
            numOfGroups2++;
            // for(SkNode i:group)
            // System.out.println(i.val[0] + "	" + i.val[1]);
            // System.out.println();
            skGroupsP.add(group);
            return;
        }
        generateTimes++;
        // the first layer has point left

        if (std.contains(group.get(group.size() - 1))
                && std.indexOf(group.get(group.size() - 1)) != std.size() - 1) {

            for (int j = std.indexOf(group.get(group.size() - 1)) + 1; j < std
                    .size(); j++) {
                ArrayList<SkNode> newGroup = new ArrayList<SkNode>();

                newGroup.addAll(group);
                newGroup.add(std.get(j));

                generateGroups(newGroup, std, num + 1);
            }
        }

        // no first layer points can be used
        ArrayList<SkNode> children = new ArrayList<SkNode>();
        children.addAll(group.get(0).children);

        for (int i = 1; i < group.size(); i++) {

            children = merge(children, group.get(i).children);

        }

        // merge the old group with j's parents
        for (SkNode j : children)
            if (j.parents.size() < num + 1) {
                numOfChecked2++;

                ArrayList<SkNode> temp = merge(group, j.parents);

                if ((group.get(group.size() - 1).id < j.id)
                        && (temp.size() == num)) {
                    ArrayList<SkNode> newGroup = new ArrayList<SkNode>();

                    newGroup.addAll(group);
                    newGroup.add(j);

                    generateGroups(newGroup, std, num + 1);
                }
            }
    }

    // Normal point wise
    public void pointWise(ArrayList<SkNode> layers) {
        ArrayList<SkNode> std = new ArrayList<SkNode>();

        // ArrayList<SkNode> layers = createLayersB(data);
        // createLayerStruct(layers);

        // first layer points
        for (SkNode i : layers)
            if (i.layer == 0)
                std.add(i);
            else
                break;

        for (SkNode i : layers)
            if (i.layer == 0) {
                ArrayList<SkNode> group = new ArrayList<SkNode>();
                group.add(i);

                generateGroups(group, std, 1);

            } else
                break;

        System.out
                .println("The number of skyline groups generated   by normal point wise: "
                        + numOfGroups2);
    }

    // Generate skyline groups for each groupSize: Direct parents & children
    public void generateGroupsD(ArrayList<SkNode> group, ArrayList<SkNode> std,
                                int num) {
        if (num == groupSize) {
            numOfGroups4++;
            skGroupsPD.add(group);
            return;
        }

        // the first layer has point left
        if (std.contains(group.get(group.size() - 1))
                && std.indexOf(group.get(group.size() - 1)) != std.size() - 1) {
            for (int j = std.indexOf(group.get(group.size() - 1)) + 1; j < std
                    .size(); j++) {
                ArrayList<SkNode> newGroup = new ArrayList<SkNode>();
                newGroup.addAll(group);
                newGroup.add(std.get(j));
                generateGroupsD(newGroup, std, num + 1);
            }
        }
        // no first layer points can be used
        ArrayList<SkNode> children = new ArrayList<SkNode>();
        for (SkNode i : group)
            // choose from members' children
            for (SkNode j : i.children)
                if (!children.contains(j) && !group.contains(j)) {
                    children.add(j);

                    if (j.parents.size() < num + 1) {
                        numOfChecked3++;
                        // merge the old group with j's parents
                        ArrayList<SkNode> tempD = merge(group, j.parents);

                        if ((group.get(group.size() - 1).id < j.id)
                                && (tempD.size() == num)) {
                            ArrayList<SkNode> newGroup = new ArrayList<SkNode>();

                            newGroup.addAll(group);
                            newGroup.add(j);

                            generateGroupsD(newGroup, std, num + 1);
                        }

                    }
                }
    }

    public static void main(String[] args) throws FileNotFoundException {
        TopKGSkyline test = new TopKGSkyline();

        int fortimes = 1;
        int sfortimes = 1;
        for (int ii = 0; ii < fortimes; ii++) {
            System.out.println();
            System.out.println("the iith computing: " + ii);
            System.out.println();
            for (int jj = 0; jj < sfortimes; jj++) {

                // input data
                FileInputStream in = new FileInputStream("testdata");
                Scanner scanner = new Scanner(in);
                // ArrayList<String> rawData = new ArrayList<String>();
                ArrayList<Integer[]> data = new ArrayList<Integer[]>();

                while (scanner.hasNextLine()) {
                    String sline = scanner.nextLine();
                    // String[] sline = scanner.nextLine().split(",");
                    // if(!rawData.contains(sline)) {
                    // rawData.add(sline);

                    //String[] s = sline.split(",");
                    String[] s = sline.split("	");

                    Integer[] line = new Integer[s.length];
                    for (int i = 0; i < s.length; i++)
                        line[i] = Integer.parseInt(s[i].trim());
                    data.add(line);
                    // }
                }

                // rawData.clear();
                scanner.close();

                // input groupSize, groupSize is the group size
                // Scanner input = new Scanner(System.in);
                // test.setK(input.nextInt());
                // input.close();
                test.groupSize = 5;

                // create layers
                // twoD or higherD for computing layers
                long cStart1 = System.nanoTime();
                //ArrayList<SkNode> layers = test.createLayers(data);
                ArrayList<SkNode> nodesByLayer = test.createLayersD(data); // Construct the nodes in layers
                long cEnd1 = System.nanoTime();
                creatLayer = creatLayer + (cEnd1 - cStart1);


                test.createLayerStruct(nodesByLayer);// build the graph: parents and children

                ArrayList<SkNode> layers1 = nodesByLayer;
                ArrayList<SkNode> layers2 = nodesByLayer;
                ArrayList<SkNode> layers3 = nodesByLayer;
                ArrayList<SkNode> layers4 = nodesByLayer;


                long start1 = System.nanoTime();
                test.unitGroupWiseDFS(layers1); // ? DFS
                long end1 = System.nanoTime();
                timeSum1 = timeSum1 + end1 - start1;

                System.out.println("DFS Unit Group Wise Time: " + timeSum1);

                long start2 = System.nanoTime();
                test.unitGroupWise(layers2);
                long end2 = System.nanoTime();
                timeSum2 = timeSum2 + end2 - start2;

                System.out.println("Unit Group Wise     Time: " + timeSum2);

                long start3 = System.nanoTime();
                test.pointWise(layers3);
                long end3 = System.nanoTime();
                timeSum3 = timeSum3 + end3 - start3;

                System.out.println("Point Wise          Time: " + timeSum3);

                long start4 = System.nanoTime();
                test.newBruteForce(layers2);
                long end4 = System.nanoTime();
                timeSum4 = timeSum4 + end4 - start4;

                System.out.println("Point Wise directed Time: " + timeSum4);

            }
        }


        System.out.println();
        System.out.println("DFS Unit Group Wise Time: " + timeSum1 / fortimes/ sfortimes);
        System.out.println("Unit Group          Time: " + timeSum2 / fortimes/ sfortimes);
        System.out.println("Point Wise          Time: " + timeSum3 / fortimes/ sfortimes);
        System.out.println("Baseline            Time: " + timeSum4 / fortimes/ sfortimes);
    }

    public int getGroupSize() {
        return groupSize;
    }

    public void setGroupSize(int groupSize) {
        this.groupSize = groupSize;
    }
}

