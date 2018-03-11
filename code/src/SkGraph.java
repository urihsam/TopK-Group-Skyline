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
        if (layerIdx >= getNumOfLayers())
            for (int lIdx=getNumOfLayers(); lIdx<layerIdx+1; lIdx++)
                graphLayers.add(new SkLayer(lIdx));
        return graphLayers.get(layerIdx);
    }

    public int getGraphLayerSize(int layerIdx) {
        return graphLayers.get(layerIdx).getLayerSize();
    }

    public int getNumOfLayers() { return graphLayers.size(); }

    public int getGraphSize() { return graphSize; }

    public void setGraphSize(int size) { graphSize = size; }

    public void print() {
        System.out.println("\nGraph size: "+ graphSize + " Effective layer size: " + effectiveLayer);
        System.out.println("Graph layer info:");
        for (SkLayer layer: graphLayers)
            layer.print();
    }
}
