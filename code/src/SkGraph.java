import java.util.ArrayList;
/**
 * Created by mashiru on 2/11/18.
 */
public class SkGraph {
    protected ArrayList<SkLayer> graphLayers;
    protected int graphSize;
    protected int numOfLayers; // num of layers
    public SkGraph(int numOfLayers) {
        graphLayers = new ArrayList<SkLayer>(numOfLayers);
        graphSize = 0;
        this.numOfLayers = numOfLayers;
    }
    void addGraphLayer(SkLayer layer){
        graphLayers.set(layer.getLayerIdx(), layer);
    }
    SkLayer getGraphLayer(int layerIdx) {
        return graphLayers.get(layerIdx);
    }
}
