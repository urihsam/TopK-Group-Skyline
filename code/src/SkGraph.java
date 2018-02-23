import java.util.List;
import java.util.ArrayList;
/**
 * Created by mashiru on 2/11/18.
 */
public class SkGraph {
    protected List<SkLayer> graphLayers;
    protected int graphSize;
    protected int effectiveLayer;
    public SkGraph(int effLayer) {
        graphSize = 0;
        effectiveLayer = effLayer;
        graphLayers = new ArrayList<SkLayer>();
        for (int idx=0; idx<effLayer+1; idx++) // initialize SkLayer and add into graphLayers
            graphLayers.add(new SkLayer(idx));
    }

    public void addGraphLayer(SkLayer layer){
        graphLayers.set(layer.getLayerIdx(), layer);
    }

    public void addGraphLayerNode(SkNode node) {
        getGraphLayer(node.getLayerIdx()).addLayerNodes(node);
        graphSize += 1;
    }

    public List<SkLayer> getGraphLayers() { return graphLayers; }

    public SkLayer getGraphLayer(int layerIdx) {
        return graphLayers.get(layerIdx);
    }

    public int getGraphLayerSize(int layerIdx) {
        return graphLayers.get(layerIdx).getLayerSize();
    }

    public int getNumOfLayers() { return graphLayers.size(); }

    public int getGraphSize() { return graphSize; }
}
