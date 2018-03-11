import java.util.List;
import java.util.ArrayList;

/**
 * Created by mashiru on 2/11/18.
 */
public class SkLayer {
    protected List<SkNode> layerNodes;
    protected int layerIdx;

    public SkLayer(int idx) {
        layerIdx = idx;
        layerNodes = new ArrayList<SkNode>();
    }
    public SkLayer(int idx, ArrayList<SkNode> nodes) {
        layerIdx = idx;
        layerNodes = nodes;
    }
    public void setLayerIdx(int idx) { layerIdx = idx; }
    public int getLayerIdx() { return layerIdx; }
    public void addLayerNodes(SkNode node) {
        layerNodes.add(node);
    }
    public void addLayerNodes(ArrayList<SkNode> nodes) { layerNodes.addAll(nodes); }
    public List<SkNode> getLayerNodes() { return layerNodes; }
    public void setLayerNodes(List<SkNode> nodes) { layerNodes = nodes; }
    public SkNode getLayerNode(int nodeIdx) { return layerNodes.get(nodeIdx); }
    public int getLayerSize() { return layerNodes.size(); }

    public void print() {
        System.out.println("\n==========Layer index: "+ layerIdx + "==========");
        System.out.println("Layer nodes info:");
        for (SkNode node: layerNodes)
            node.print();
    }
}
