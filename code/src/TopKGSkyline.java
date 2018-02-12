/**
 * Created by mashiru on 2/10/18.
 */
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
    public ArrayList<SkNode> createLayerNodes(ArrayList<Integer[]> data) {
        ArrayList<SkNode> results = new ArrayList<SkNode>();

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
                    if (isDominate(ndElm.val, pt.val) && ndElm.getLayerIdx() > maxLayer)
                        maxLayer = ndElm.getLayerIdx();
            pt.setLayerIdx(++maxLayer);
            if (pt.getLayerIdx() < groupSize)
                layers.get(pt.getLayerIdx()).add(pt);
        }

        for (ArrayList<SkNode> layerElm : layers)
            for (SkNode ndElm : layerElm)
                results.add(ndElm);
        return results;
    }

    // normal create layers relation
    public SkGraph createLayerGraph(ArrayList<SkNode> nodes) {
        for (SkNode nodeI : nodes) {
            nodeI.setId(nodes.indexOf(nodeI)); // Set the Id of point according to the order in the list
            for (SkNode nodeJ : nodes)
                if (nodeI != nodeJ) {
                    if (isDominate(nodeJ.val, nodeI.val))
                        nodeI.parents.add(nodeJ);
                    else if (isDominate(nodeI.val, nodeJ.val))
                        nodeI.children.add(nodeJ);
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

    public PriorityQueue<SkGroup> getTopKGroups(SkGraph graph) {
        // lambda express: Descending order, if a > b, return negative, a stays before b ; else if a < b, return positive, a stays after b
        PriorityQueue<SkGroup> topKGroups = new PriorityQueue<SkGroup>(topK, Collections.reverseOrder());
        Collections.sort(graph.graphLayers.get(0).getLayerNodes()); // sort the top layer of the graph
        // TODO: Recursively call the permutation func to merge and calculate the dominates and update the topKGroup structure

        return topKGroups;
    }


        // Merge two group of points
    public ArrayList<SkNode> merge(ArrayList<SkNode> a, ArrayList<SkNode> b) {
        ArrayList<SkNode> result = new ArrayList<SkNode>();
        int aIdx = 0; int bIdx = 0;
        SkNode aEle, bEle;
        while (aIdx < a.size() && bIdx < b.size()) {
            if ((aEle=a.get(aIdx)).id < (bEle=b.get(bIdx)).id) {
                result.add(aEle);
                aIdx++;
            } else if (aEle.id > bEle.id) {
                result.add(bEle);
                bIdx++;
            } else {
                result.add(aEle);
                aIdx++; bIdx++;
            }
        }
        // append the rest
        result.addAll(new ArrayList<SkNode>(aIdx == a.size() ? b.subList(bIdx, b.size()) : a.subList(aIdx, a.size())));

        return result;
    }



    public static void main(String[] args) throws FileNotFoundException {
        int gSize = Integer.parseInt(args[0]); // group size
        int topK = Integer.parseInt(args[1]); // top k
        TopKGSkyline test = new TopKGSkyline(gSize, topK);

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

        long timeSumBaseline = 0;
        long timeSumTopK = 0;
        long creatLayerTime = 0;

        // create layers
        // twoD or higherD for computing layers
        long cStartT = System.nanoTime();
        //ArrayList<SkNode> layers = test.createLayerNodes(data);
        ArrayList<SkNode> nodesByLayer = test.createLayerNodes(data); // Construct the nodes in layers
        long cEndT = System.nanoTime();
        creatLayerTime = creatLayerTime + (cEndT - cStartT);

        SkGraph graph = test.createLayerGraph(nodesByLayer);// build the graph: parents and children

        ArrayList<SkNode> nodesBaseline = nodesByLayer;
        ArrayList<SkNode> nodesTopk = nodesByLayer;

        long start1 = System.nanoTime();
        // nodesBaseline
        long end1 = System.nanoTime();
        timeSumBaseline = timeSumBaseline + end1 - start1;

        System.out.println("DFS Unit Group Wise Time: " + timeSumBaseline);

        long start2 = System.nanoTime();
        // nodesTopk
        long end2 = System.nanoTime();
        timeSumTopK = timeSumTopK + end2 - start2;

        System.out.println("Unit Group Wise     Time: " + timeSumTopK);




    }
}

