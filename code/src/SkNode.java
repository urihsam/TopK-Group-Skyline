/**
 * Created by mashiru on 2/10/18.
 */
import java.util.ArrayList;

public class SkNode implements Comparable{
    protected Integer[] val;
    protected int layerIdx;
    protected int id;
    protected ArrayList<SkNode> parents;
    protected ArrayList<SkNode> children;

    public SkNode(int d) {
        this.val = new Integer[d];
        this.layerIdx = 0;
        this.id = 0;
        this.parents = new ArrayList<SkNode>();
        this.children = new ArrayList<SkNode>();
    }

    public SkNode(Integer[] val, int layerIdx) {
        this.val = new Integer[val.length];
        this.layerIdx = layerIdx;
        this.id = 0;
        for(int i=0; i<val.length; i++)
            this.val[i] = val[i];
        this.parents = new ArrayList<SkNode>();
        this.children = new ArrayList<SkNode>();
    }

    public Integer[] getVal() {
        return val;
    }

    public void setVal(Integer[] val) {
        for(int i=0; i<val.length; i++)
            this.val[i] = val[i];
    }

    public int getLayerIdx() {
        return layerIdx;
    }

    public void setLayerIdx(int layerIdx) {
        this.layerIdx = layerIdx;
    }

    public ArrayList<SkNode> getParents() {
        return parents;
    }

    public ArrayList<SkNode> getChildren() {
        return children;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public int compareTo(Object other) {
        /* For Ascending order*/
        return this.getChildren().size() - ((SkNode)other).getChildren().size(); // the difference between dominating points
    }
}

