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


    // create layers: Not brute force & Higher dimension
    public List<SkNode> createLayerNodes(List<Integer[]> data) {
        List<SkNode> results = new ArrayList<SkNode>();

        // sort the points
        Collections.sort(data, new Comparator<Object>() {
            @Override
            public int compare(Object o1, Object o2) {
                if (o1 instanceof Integer[]) return (compare((Integer[]) o1, (Integer[]) o2));
                return 0;
            }
            public int compare(Integer[] o1, Integer[] o2) { return (o1[0] < o2[0] ? -1 : (o1[0] == o2[0] ? 0 : 1)); }
        });

        // first point belongs to the first layer
        SkNode pt0 = new SkNode(data.get(0), 0);
        List<List<SkNode>> layers = new ArrayList<List<SkNode>>();
        List<SkNode> layer = new ArrayList<SkNode>();
        layer.add(pt0);
        layers.add(layer);

        // no more than groupSize layers will be used!
        for (int layerIdx = 1; layerIdx < groupSize; layerIdx++)
            layers.add(new ArrayList<SkNode>()); // add new layer

        for (int ptIdx = 1; ptIdx < data.size(); ptIdx++) {
            SkNode pt = new SkNode(data.get(ptIdx), 0);
            int maxLayer = -1;
            for (List<SkNode> layerElm : layers) // for each layer in all layers
                for (SkNode ndElm : layerElm) // for each node in this layer
                    if (isDominate(ndElm.val, pt.val) && ndElm.getLayerIdx() > maxLayer)
                        maxLayer = ndElm.getLayerIdx();
            pt.setLayerIdx(++maxLayer);
            if (pt.getLayerIdx() < groupSize)
                layers.get(pt.getLayerIdx()).add(pt);
        }

        int ID = 0;
        for (List<SkNode> layerElm : layers) {
            for (SkNode ndElm : layerElm) {
                ndElm.setId(ID++); // Set the Id of point according to the order in the list
                results.add(ndElm);
            }
        }
        return results;
    }

    // normal create layers relation
    public SkGraph createLayerGraph(List<SkNode> nodes) {
        for (SkNode nodeI : nodes) {
            for (SkNode nodeJ : nodes)
                if (nodeI != nodeJ)
                    if (isDominate(nodeI.val, nodeJ.val)) {
                        nodeI.children.add(nodeJ);
                        nodeJ.parents.add(nodeI);
                    }
        }

        int layerIdx = -1;
        SkGraph graph = new SkGraph(nodes.get(nodes.size()).getLayerIdx()+1); // get the layer idx of the last node
        Iterator<SkNode> nodeIter = nodes.iterator();
        while (nodeIter.hasNext()) {
            SkNode pt = nodeIter.next();
            if (pt.parents.size() > groupSize - 1) {
                nodeIter.remove(); // Removes from the underlying collection the last element returned by this iterator (optional operation).
                continue;
            }
            if (layerIdx != pt.getLayerIdx()) {
                layerIdx = pt.getLayerIdx();
                graph.addGraphLayer(new SkLayer(pt.getLayerIdx()));
            }
            graph.getGraphLayer(layerIdx).addLayerNodes(pt);
        }
        return graph;
    }

    public List<SkGroup> getTopKGroups(SkGraph graph) {
        TopKGroup topKGroup = new TopKGroup(topK);
        // PriorityQueue<SkGroup> topKGroups = new PriorityQueue<SkGroup>(topK, Collections.reverseOrder());
        List<SkNode> firstLayer = graph.graphLayers.get(0).getLayerNodes();
        Collections.sort(firstLayer); // sort the top layer of the graph
        // TODO: Recursively call the permutation func to merge and calculate the dominates and update the topKGroup structure
        checkCombination(firstLayer, topK, new SkGroup(), topKGroup);
        checkChildren4TopKG(new ArrayList<SkGroup>(topKGroup.getTopKGroup()), topK, topKGroup);
        return topKGroup.getTopKGroup();
    }

    private void checkChildren4TopKG(List<SkGroup> groups4Check, int k, TopKGroup topKG) {
        for (int depth=1; depth<=k; depth++)
            for (SkGroup group: groups4Check) //  check certain group
                for (SkNode gNode: group.getGroupNodes()) // check certain gnode in the group
                    for (SkNode nChild: gNode.getChildren()) { // check certain nChild of gNode
                        if (nChild.getLayerIdx() == depth) {
                            if (group.getGroupNodes().containsAll(nChild.getParents())) {
                                List<SkNode> groupNodesRemain= new ArrayList<SkNode>(group.getGroupNodes());
                                List<SkNode> groupNodesChecked = new ArrayList<SkNode>(nChild.getParents());
                                groupNodesRemain.removeAll(groupNodesChecked);
                                groupNodesChecked.add(nChild);
                                SkGroup groupFound = new SkGroup(groupNodesChecked, gNode.getChildren());
                                checkCombination(groupNodesRemain, k-groupNodesChecked.size(), groupFound, topKG);
                            }
                        }else if (nChild.getLayerIdx() > depth)
                            continue; // change to another node, skip the rest children
                    }
    }

    private void checkCombination(List<SkNode> nodes4Check, int k, SkGroup groupFound, TopKGroup topKG) {
        if (k == 0 && !topKG.getTopKGroup().contains(groupFound)) {
            topKG.addSkGroup(groupFound);
            return;
        }
        for (int idx=0; idx<nodes4Check.size(); idx++) {
            SkNode node = nodes4Check.get(idx);
            if (node.getDominates() + groupFound.getGroupDominates() <= topKG.getMinDominates())
                return;
            SkGroup newGroupFound = new SkGroup(groupFound); // copy of groupFound
            newGroupFound.addGroupNodes(node);
            checkCombination(nodes4Check.subList(idx+1, nodes4Check.size()), k-1, newGroupFound, topKG);
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
        //List<SkNode> layers = test.createLayerNodes(data);
        List<SkNode> nodesByLayer = test.createLayerNodes(data); // Construct the nodes in layers
        long cEndT = System.nanoTime();
        creatLayerTime = creatLayerTime + (cEndT - cStartT);

        SkGraph graph = test.createLayerGraph(nodesByLayer);// build the graph: parents and children

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

