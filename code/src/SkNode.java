/**
 * Created by mashiru on 2/10/18.
 */
import java.util.ArrayList;

public class SkNode {
    public Integer[] val;
    public int layer;
    public int id;
    public ArrayList<SkNode> parents;
    public ArrayList<SkNode> children;
    public ArrayList<SkNode> allparentsD;//only for directedPoint to record allpartents

    public SkNode(int d) {
        this.val = new Integer[d];
        this.layer = 0;
        this.id = 0;
        this.parents = new ArrayList<SkNode>();
        this.children = new ArrayList<SkNode>();
        this.allparentsD = new ArrayList<SkNode>();
    }

    public SkNode(Integer[] val, int layer) {
        this.val = new Integer[val.length];
        this.layer = layer;
        this.id = 0;
        for(int i=0; i<val.length; i++)
            this.val[i] = val[i];
        this.parents = new ArrayList<SkNode>();
        this.children = new ArrayList<SkNode>();
        this.allparentsD = new ArrayList<SkNode>();
    }

    public Integer[] getVal() {
        return val;
    }

    public void setVal(Integer[] val) {
        for(int i=0; i<val.length; i++)
            this.val[i] = val[i];
    }

    public int getLayer() {
        return layer;
    }

    public void setLayer(int layer) {
        this.layer = layer;
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
}

