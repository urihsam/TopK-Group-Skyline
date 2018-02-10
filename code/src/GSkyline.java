/**
 * Created by mashiru on 2/10/18.
 */
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Random;
import java.util.Scanner;

public class GSkyline {
    public int k;
    public long numOfGroups1; // Unit group wise
    public long numOfGroups2; // Point wise
    public long numOfGroups3; // New brute force
    public long numOfGroups4; // Point wise with direct parents&children
    public long numOfGroups5; // Unit group wise of DFS version

    public int numOfChecked1; // How many times checked for Unit group wise
    public int numOfCheckDFS;
    public int numOfChecked2; // How many times checked for Point wise normal
    public int numOfChecked3; // How many times checked for Point wise direct

    public static int nSk;

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

    public GSkyline() {
        k = 0;
        numOfGroups1 = 0;
        numOfGroups2 = 0;
        numOfGroups3 = 0;
        numOfGroups4 = 0;
        numOfGroups5 = 0;

        numOfChecked1 = 0;
        numOfCheckDFS = 0;
        numOfChecked2 = 0;
        numOfChecked3 = 0;

        nSk = 0;

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
                               ArrayList<Integer[]> data, ArrayList<Integer[]> tar, int k) {
        if (k == 0) {
            groups.add(tar);
            return;
        }

        for (Integer[] i : data)
            if (!tar.contains(i)) {
                ArrayList<Integer[]> aa = new ArrayList<Integer[]>();
                aa.addAll(tar);
                aa.add(i);
                subPermutation(groups, data, aa, k - 1);
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
                for (int k = 0; k < i.get(j).length; k++)
                    if (i.get(j)[k] != b.get(j)[k]) {
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
                          int k) {
        if (k == 0) {
            groups.add(tar);
            return;
        }

        for (int i = start + 1; i < data.size(); i++) {
            ArrayList<Integer[]> a = new ArrayList<Integer[]>();
            a.addAll(tar);
            a.add(data.get(i));
            subGroups(groups, data, a, i, k - 1);
        }
    }

    public ArrayList<ArrayList<Integer[]>> createGroups(
            ArrayList<Integer[]> data, int k) {
        ArrayList<ArrayList<Integer[]>> groups = new ArrayList<ArrayList<Integer[]>>();

        for (int i = 0; i < data.size(); i++) {
            ArrayList<Integer[]> a = new ArrayList<Integer[]>();
            a.add(data.get(i));
            subGroups(groups, data, a, i, k - 1);
        }
        return groups;
    }

    // original brute force method
    public void bruteForce(ArrayList<Integer[]> data) {
        ArrayList<ArrayList<Integer[]>> groups = this.createGroups(data,
                this.getK());
        int groupCount = 0;
        for (ArrayList<Integer[]> i : groups) {
            boolean mark = false;
            for (ArrayList<Integer[]> j : groups) {
                if (!i.containsAll(j)) {
                    if (this.isGroupDominate(j, i)) {
                        mark = true;
                        break;
                    }
                }
            }
            if (!mark) {
                groupCount++;
            }
        }
        System.out.println("The number of skyline groups: " + groupCount);
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
            createSubs(group, layers, i, k - 1);
        }

        System.out.println("The number of skyline groups               by baseline method: "
                + numOfGroups3);
    }

    // skyline algorithm for raw data
    public ArrayList<Integer[]> getSkyline(ArrayList<Integer[]> data) {
        ArrayList<Integer[]> skyline = new ArrayList<Integer[]>();
        ArrayList<Integer[]> sorted = new ArrayList<Integer[]>();

        sorted.addAll(data);

        Collections.sort(sorted, new Comparator<Object>() {

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

        skyline.add(sorted.get(0)); // add the first element
        int y = sorted.get(0)[1];
        for (int i = 1; i < sorted.size(); i++)
            if (sorted.get(i)[1] < y) {
                y = sorted.get(i)[1];
                skyline.add(sorted.get(i));
            }
//		// print
//		for (Integer[] i : skyline)
//			for (int j = 0; j < i.length; j++)
//				System.out.println(j + 1 + ":" + i[j]);

        return skyline;
    }

    // create layers: Brute force
    public ArrayList<SkNode> createLayersB(ArrayList<Integer[]> data) {
        ArrayList<SkNode> layers = new ArrayList<SkNode>();
        ArrayList<Integer[]> temp = new ArrayList<Integer[]>();
        temp.addAll(data);
        int layer = 0;

        for (int ii=0; ii<k; ii++)
        {
            //while (temp.size() != 0) {
            ArrayList<Integer[]> skyline = getSkyline(temp);
            for (Integer[] i : skyline) {
                SkNode sknode = new SkNode(i, layer);
                layers.add(sknode);
            }
            temp.removeAll(skyline);
            layer++;
        }
        return layers;
    }

    // create layers: Not brute force & cannot handle higher dimension
    public ArrayList<SkNode> createLayers(ArrayList<Integer[]> data) {
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
        SkNode p1 = new SkNode(data.get(0), 0);
        int maxLayer = 0;
        ArrayList<ArrayList<SkNode>> layers = new ArrayList<ArrayList<SkNode>>();
        ArrayList<SkNode> layer = new ArrayList<SkNode>();
        layer.add(p1);
        layers.add(layer);

        for (int i = 1; i < data.size(); i++) {
            SkNode pi = new SkNode(data.get(i), 0);
            if (!isDominate(layers.get(0).get(layers.get(0).size() - 1).val,
                    data.get(i)))
                layers.get(0).add(pi);
            else if (isDominate(
                    layers.get(maxLayer).get(layers.get(maxLayer).size() - 1).val,
                    data.get(i))) {
                if (maxLayer != k - 1) {
                    ArrayList<SkNode> layeri = new ArrayList<SkNode>();
                    pi.layer = ++maxLayer;
                    layeri.add(pi);
                    layers.add(layeri);
                }
            } else {
                // binary search
                int left = 0;
                int right = maxLayer;
                int mid = 0;
                while (true) {
                    mid = (left + right) / 2;
                    if (isDominate(
                            layers.get(mid).get(layers.get(mid).size() - 1).val,
                            pi.val))
                        left = mid + 1;
                    else if (!isDominate(
                            layers.get(mid - 1).get(
                                    layers.get(mid - 1).size() - 1).val, pi.val))
                        right = mid - 1;
                    else
                        break;
                }
                pi.layer = mid;
                layers.get(mid).add(pi);
            }
        }

        // generate the two dimension array to one dimension
        for (ArrayList<SkNode> i : layers)
            for (SkNode j : i)
                results.add(j);
        return results;
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
        SkNode p1 = new SkNode(data.get(0), 0);
        ArrayList<ArrayList<SkNode>> layers = new ArrayList<ArrayList<SkNode>>();
        ArrayList<SkNode> layer = new ArrayList<SkNode>();
        layer.add(p1);
        layers.add(layer);

        // no more than k layers will be used
        for (int i = 1; i < k; i++) {
            ArrayList<SkNode> newLayer = new ArrayList<SkNode>();
            layers.add(newLayer);
        }

        for (int j = 1; j < data.size(); j++) {
            SkNode pj = new SkNode(data.get(j), 0);
            int maxLayer = -1;
            for (ArrayList<SkNode> a : layers)
                for (SkNode i : a)
                    if (isDominate(i.val, pj.val) && i.layer > maxLayer)
                        maxLayer = i.layer;
            pj.layer = ++maxLayer;

            if (pj.layer < k)
                layers.get(pj.layer).add(pj);
        }

        for (ArrayList<SkNode> i : layers)
            for (SkNode j : i)
                results.add(j);
        return results;
    }

    // create layers relation: only parents
    public void createLayerStructDFS(ArrayList<SkNode> layers) {
        for (SkNode i : layers) {
            i.setId(layers.indexOf(i)); // indicate each point

            for (SkNode j : layers)
                if (i != j) {
                    // if(isDominate(i.val, j.val))
                    // i.children.add(j);
                    if (isDominate(j.val, i.val))
                        i.parents.add(j);
                }
        }

        Iterator<SkNode> i = layers.iterator();
        while (i.hasNext()) {
            SkNode p = i.next();
            nSk++;
            if (p.parents.size() > k - 1)
                i.remove();
        }
    }

    // normal create layers relation
    public void createLayerStruct(ArrayList<SkNode> layers) {
        for (SkNode i : layers) {
            i.setId(layers.indexOf(i)); // indicate each point

            for (SkNode j : layers)
                if (i != j) {
                    // if(isDominate(i.val, j.val))
                    // i.children.add(j);
                    if (isDominate(j.val, i.val))
                        i.parents.add(j);
                }
        }

        Iterator<SkNode> i = layers.iterator();
        while (i.hasNext()) {
            SkNode p = i.next();
            nSk++;
            if (p.parents.size() > k - 1)
                i.remove();
        }

        for (SkNode ii : layers) {
            ii.setId(layers.indexOf(ii)); // indicate each point

            for (SkNode j : layers)
                if (ii != j) {
                    if (isDominate(ii.val, j.val))
                        ii.children.add(j);
                }
        }
    }

    // Get the skyline points in input set: Higher dimension
    public ArrayList<SkNode> getSkyLineD(ArrayList<SkNode> a) {
        ArrayList<SkNode> results = new ArrayList<SkNode>();

        if (a.size() > 0) {
            ArrayList<ArrayList<SkNode>> layers = new ArrayList<ArrayList<SkNode>>();
            for (int i = 0; i < k; i++) {
                ArrayList<SkNode> layer = new ArrayList<SkNode>();
                layers.add(layer);
            }

            // list a is not sorted by x
            Collections.sort(a, new Comparator<Object>() {

                @Override
                public int compare(Object o1, Object o2) {
                    if (o1 instanceof SkNode)
                        return (compare((SkNode) o1, (SkNode) o2));
                    return 0;
                }

                public int compare(SkNode o1, SkNode o2) {
                    return (o1.val[0] < o2.val[0] ? -1
                            : (o1.val[0] == o2.val[0] ? 0 : 1));
                }

            });

            ArrayList<SkNode> temp = new ArrayList<SkNode>();
            layers.get(a.get(0).layer).add(a.get(0)); // the first point must be
            // a skyline point
            temp.add(a.get(0));

            // check the rest
            for (int i = 1; i < a.size(); i++) {
                boolean mark = true;

                // a skyline point cannot be dominated by any points in list
                // temp
                for (SkNode j : temp)
                    if (isDominate(j.val, a.get(i).val)) {
                        mark = false;
                        break;
                    }
                if (mark)
                    layers.get(a.get(i).layer).add(a.get(i));
            }

            for (ArrayList<SkNode> i : layers)
                for (SkNode j : i)
                    results.add(j);
        }

        return results;
    }

    // Get the skyline points in input set: Two dimension
    public ArrayList<SkNode> getSkyLine(ArrayList<SkNode> a) {
        ArrayList<SkNode> results = new ArrayList<SkNode>();

        if (a.size() > 0) {
            ArrayList<ArrayList<SkNode>> layers = new ArrayList<ArrayList<SkNode>>();
            for (int i = 0; i < k; i++) {
                ArrayList<SkNode> layer = new ArrayList<SkNode>();
                layers.add(layer);
            }

            // list a is not sorted by x
            Collections.sort(a, new Comparator<Object>() {

                @Override
                public int compare(Object o1, Object o2) {
                    if (o1 instanceof SkNode)
                        return (compare((SkNode) o1, (SkNode) o2));
                    return 0;
                }

                public int compare(SkNode o1, SkNode o2) {
                    return (o1.val[0] < o2.val[0] ? -1
                            : (o1.val[0] == o2.val[0] ? 0 : 1));
                }

            });

            // results.add(a.get(0)); //the first point must be a skyline point
            layers.get(a.get(0).layer).add(a.get(0));

            int y = a.get(0).val[1];
            for (int i = 1; i < a.size(); i++)
                if (a.get(i).val[1] < y) {
                    y = a.get(i).val[1];
                    layers.get(a.get(i).layer).add(a.get(i));
                    // results.add(a.get(i));
                }

            for (ArrayList<SkNode> i : layers)
                for (SkNode j : i)
                    results.add(j);
        }

        return results;
    }

    // Create layer relation: Direct parents & Children
    public void createLayerStructD(ArrayList<SkNode> layers) {

        // generate all direct parents
        for (SkNode i : layers) {
            i.setId(layers.indexOf(i)); // indicate each point

            for (SkNode j : layers)
                if (i != j) {
                    if (isDominate(j.val, i.val))
                        i.allparentsD.add(j);
                }
        }

        Iterator<SkNode> iii = layers.iterator();
        while (iii.hasNext()) {
            SkNode p = iii.next();
            nSk++;
            if (p.allparentsD.size() > k - 1) {
                iii.remove();
            }
        }

        // generate all direct children
        for (int i = 0; i < layers.size() - 1; i++) {
            ArrayList<SkNode> temp = new ArrayList<SkNode>();

            for (int j = i + 1; j < layers.size(); j++) {
                int mark = 0;
                for (int h = 0; h < layers.get(i).val.length; h++)
                    if (layers.get(j).val[h] > layers.get(i).val[h])
                        mark++;
                if (mark == layers.get(i).val.length)
                    temp.add(layers.get(j));
            }

            layers.get(i).children
                    .addAll(layers.get(0).val.length == 2 ? getSkyLine(temp)
                            : getSkyLineD(temp));
        }

        // generate all direct parents
        for (SkNode ii : layers) {
            ii.setId(layers.indexOf(ii)); // indicate each point

            for (SkNode j : ii.children)
                j.parents.add(ii);

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

                    if (newTotal.size() < k - 1) {
                        ArrayList<SkNode> newUnit = new ArrayList<SkNode>();
                        newUnit.addAll(i);
                        newUnit.add(std.get(j));
                        newUnits.add(newUnit);
                    } else if (newTotal.size() == k - 1) {
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
            if (layers.get(i).parents.size() < k - 1) {
                ArrayList<SkNode> unit = new ArrayList<SkNode>();
                // unit.addAll(i.parents);
                std.add(layers.get(i));
                unit.add(layers.get(i));
                unitGroups.add(unit);
            }
            // output skyline group
            else if (layers.get(i).parents.size() == k - 1) {
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

                    if (newTotal.size() < k - 1) {
                        ArrayList<SkNode> newUnit = new ArrayList<SkNode>();
                        newUnit.addAll(i);
                        newUnit.add(std.get(j));
                        newUnits.add(newUnit);
                    } else if (newTotal.size() == k - 1) {
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
            if (i.parents.size() < k - 1) {
                ArrayList<SkNode> unit = new ArrayList<SkNode>();
                // unit.addAll(i.parents);
                std.add(i);
                unit.add(i);
                unitGroups.add(unit);
            }
            // output skyline group
            else if (i.parents.size() == k - 1) {
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

    // Generate skyline groups for each k
    public void generateGroups(ArrayList<SkNode> group, ArrayList<SkNode> std,
                               int num) {
        if (num == k) {
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

    // Generate skyline groups for each k: Direct parents & children
    public void generateGroupsD(ArrayList<SkNode> group, ArrayList<SkNode> std,
                                int num) {
        if (num == k) {
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

    // Point wise method: Direct parents
    public void pointWiseD(ArrayList<SkNode> layers) {
        ArrayList<SkNode> std = new ArrayList<SkNode>();

        for (SkNode i : layers)
            if (i.layer == 0)
                std.add(i);
            else
                break;

        for (SkNode i : layers)
            if (i.layer == 0) {
                ArrayList<SkNode> group = new ArrayList<SkNode>();
                group.add(i);

                generateGroupsD(group, std, 1);

            } else
                break;

        System.out
                .println("The number of skyline groups generated by direct point wise: "
                        + numOfGroups4);
    }


    // only for testing
    public ArrayList<ArrayList<SkNode>> diff(ArrayList<ArrayList<SkNode>> a,
                                             ArrayList<ArrayList<SkNode>> b) {
        ArrayList<ArrayList<SkNode>> diff = new ArrayList<ArrayList<SkNode>>();
        int count = 0;

        if (a.size() > b.size()) {

            for (ArrayList<SkNode> i : a)
                for (ArrayList<SkNode> j : b)
                    for (int t = 0; t < i.size(); t++) {
                        int mark = 0;
                        if (i.get(t).val[0] == j.get(t).val[0]
                                && i.get(t).val[1] == j.get(t).val[1])
                            mark++;
                        if (mark == j.size())
                            count++;
                    }

            if (count == b.size())
                System.out.println("a contains b");

            for (ArrayList<SkNode> i : a)
                for (ArrayList<SkNode> j : diff) {
                    boolean mark = true;
                    for (int t = 0; t < i.size(); t++)
                        if (i.get(t).val[0] != j.get(t).val[0]
                                || i.get(t).val[1] != j.get(t).val[1]) {
                            mark = false;
                            break;
                        }
                    if (mark)
                        System.out.println("Yes");
                }
        } else {
            for (ArrayList<SkNode> i : b)
                for (ArrayList<SkNode> j : a)
                    for (int t = 0; t < i.size(); t++) {
                        int mark = 0;
                        if (i.get(t).val[0] == j.get(t).val[0]
                                && i.get(t).val[1] == j.get(t).val[1])
                            mark++;
                        if (mark == j.size())
                            count++;
                    }

            if (count == a.size())
                System.out.println("b contains a");
        }
        return diff;
    }

    public static void main(String[] args) throws FileNotFoundException {
        GSkyline test = new GSkyline();

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

                // input k, k is the group size
                // Scanner input = new Scanner(System.in);
                // test.setK(input.nextInt());
                // input.close();
                test.k = 5;

                // create layers
                // twoD or higherD for computing layers
                long cStart1 = System.nanoTime();
                //ArrayList<SkNode> layers = test.createLayers(data);
                ArrayList<SkNode> layers = test.createLayersD(data);
                long cEnd1 = System.nanoTime();
                creatLayer = creatLayer + (cEnd1 - cStart1);


                test.createLayerStruct(layers);// parents and children

                ArrayList<SkNode> layers1 = layers;
                ArrayList<SkNode> layers2 = layers;
                ArrayList<SkNode> layers3 = layers;
                ArrayList<SkNode> layers4 = layers;


                long start1 = System.nanoTime();
                test.unitGroupWiseDFS(layers1);
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

    public int getK() {
        return k;
    }

    public void setK(int k) {
        this.k = k;
    }
}

