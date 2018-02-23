/**
 * Created by mashiru on 2/10/18.
 */
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Iterator;
import java.util.Scanner;


public class TopKGSkyline {
    protected int groupSize;
    protected int topK;

    public TopKGSkyline(int gSize, int topK) {
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

        // step one: create the layer structure in graph
        for (int ptIdx = 1; ptIdx < data.size(); ptIdx++) {
            SkNode pt = new SkNode(data.get(ptIdx), 0);
            int maxLayer = -1;
            for (SkLayer layerElm: graph.getGraphLayers())  // for each layer in all layers
                for (SkNode ndElm : layerElm.getLayerNodes()) // for each node in this layer
                    if (isDominate(ndElm.getVal(), pt.getVal()))
                        if (ndElm.getLayerIdx() > maxLayer)
                            maxLayer = ndElm.getLayerIdx();
            pt.setLayerIdx(maxLayer<groupSize?maxLayer+1:groupSize); // set layer index
            graph.addGraphLayerNode(pt);
        }

        // step two: set IDs following the order in layers
        int ID = 0;
        for (SkLayer layerElm : graph.getGraphLayers()) // for each layer in all layers
            for (SkNode ndElm : layerElm.getLayerNodes()) // for each node in this layer
                ndElm.setId(ID++);

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


    public List<SkGroup> getTopKGroups(SkGraph graph) {
        TopKGroup topKGroup = new TopKGroup(topK);
        // PriorityQueue<SkGroup> topKGroups = new PriorityQueue<SkGroup>(topK, Collections.reverseOrder());
        List<SkNode> firstLayer = graph.graphLayers.get(0).getLayerNodes();
        Collections.sort(firstLayer, Collections.reverseOrder()); // sort the top layer of the graph
        // TODO: Recursively call the permutation func to merge and calculate the dominates and update the topKGroup structure
        checkCombination(firstLayer, groupSize, new SkGroup(), topKGroup);
        checkChildren4TopKG(new ArrayList<SkGroup>(topKGroup.getTopKGroup()), groupSize, topKGroup);
        return topKGroup.getTopKGroup();
    }

    private void checkChildren4TopKG(List<SkGroup> groups4Check, int g, TopKGroup topKG) {
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
                                    checkCombination(groupNodesRemain, g-groupNodesChecked.size(), groupFound, topKG);
                                }
                        }else if (nChild.getLayerIdx() > depth)
                            break; // change to another node, skip the rest children
                    }
    }

    private void checkCombination(List<SkNode> nodes4Check, int g, SkGroup groupFound, TopKGroup topKG) {
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

    public static void main(String[] args) throws FileNotFoundException {
        int gSize = Integer.parseInt(args[0]); // group size
        int topK = Integer.parseInt(args[1]); // top k
        TopKGSkyline test = new TopKGSkyline(gSize, topK);

        // input data
        FileInputStream in = new FileInputStream("testdata");
        Scanner scanner = new Scanner(in);
        // List<String> rawData = new List<String>();
        List<Integer[]> data = new ArrayList<Integer[]>();


        while (scanner.hasNextLine()) {
            String sline = scanner.nextLine();
            // String[] sline = scanner.nextLine().split(",");
            // if(!rawData.contains(sline)) {
            // rawData.add(sline);

            //String[] s = sline.split(",");
            String[] s = sline.split("  ");

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

        long timeSumBaseline = 0;
        long timeSumTopK = 0;
        long creatLayerTime = 0;

        // create layers
        // twoD or higherD for computing layers
        long cStartT = System.nanoTime();
        SkGraph graph = test.createLayerGraph(data);// build the graph
        long cEndT = System.nanoTime();
        creatLayerTime = creatLayerTime + (cEndT - cStartT);



        SkGraph graphBaseline = graph;
        SkGraph graphTopk = graph;

        long start1 = System.nanoTime();
        // nodesBaseline
        long end1 = System.nanoTime();
        timeSumBaseline = timeSumBaseline + end1 - start1;

        System.out.println("DFS Unit Group Wise Time: " + timeSumBaseline);

        long start2 = System.nanoTime();
        // nodesTopk
        List<SkGroup> topKGroups = test.getTopKGroups(graphTopk);
        long end2 = System.nanoTime();
        timeSumTopK = timeSumTopK + end2 - start2;

        System.out.println("Unit Group Wise     Time: " + timeSumTopK);




    }
}

