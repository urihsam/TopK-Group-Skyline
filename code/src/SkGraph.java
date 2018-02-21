import java.util.List;
import java.util.ArrayList;
/**
 * Created by mashiru on 2/11/18.
 */
public class SkGraph {
    protected List<SkLayer> graphLayers;
    protected int graphSize;
    public SkGraph() {
        graphLayers = new ArrayList<SkLayer>();
        graphSize = 0;
    }

    public void addGraphLayer(SkLayer layer){
        int numOfLayer = getNumOfLayers();
        int layerIdx = layer.getLayerIdx();
        if ( numOfLayer <= layerIdx) {
            for (int idx=numOfLayer; idx<=layerIdx; idx++) // initialize SkLayer and add into graphLayers
                graphLayers.add(new SkLayer(idx));
        }
        graphLayers.set(layer.getLayerIdx(), layer);
    }

    public void addGraphLayerNode(int layerIdx, SkNode node) {
        getGraphLayer(layerIdx).addLayerNodes(node);
        graphSize += 1;
    }

    public SkLayer getGraphLayer(int layerIdx) {
        return graphLayers.get(layerIdx);
    }

    public int getNumOfLayers() { return graphLayers.size(); }

    public int getGraphSize() { return graphSize; }
}
