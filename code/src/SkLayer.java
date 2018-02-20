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
    void setLayerIdx(int idx) { layerIdx = idx; }
    int getLayerIdx() { return layerIdx; }
    void addLayerNodes(SkNode node) { layerNodes.add(node); }
    void addLayerNodes(ArrayList<SkNode> nodes) { layerNodes.addAll(nodes); }
    List<SkNode> getLayerNodes() { return layerNodes; }
    int getLayerSize() { return layerNodes.size(); }
}
