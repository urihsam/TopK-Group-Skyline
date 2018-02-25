/**
 * Created by mashiru on 2/10/18.
 */
import java.util.List;
import java.util.ArrayList;

public class SkNode implements Comparable {
    protected Integer[] val;
    protected int layerIdx; // starting from 0
    protected int id;
    protected List<SkNode> parents;
    protected List<SkNode> children;

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

    public List<SkNode> getParents() {
        return parents;
    }

    public void addParent(SkNode parent) { parents.add(parent); }

    public List<SkNode> getChildren() { return children; }

    public void addChild(SkNode child) { children.add(child); }

    public int getSizeOfDominatedNodes() { return children.size(); }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void print() {
        System.out.println("\n----------Node ID: " + id + " Layer index: "+ layerIdx+"----------");
        System.out.print("Value info: [ ");
        for (int value: val)
            System.out.print(value + " ");
        System.out.println("]");
    }

    @Override
    public int compareTo(Object other) { // used for sorting first layer by size of children
        /* For Ascending order*/
        return this.getChildren().size() - ((SkNode)other).getChildren().size(); // the difference between dominating points
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!SkNode.class.isAssignableFrom(obj.getClass())) {
            return false;
        }
        final SkNode other = (SkNode) obj;

        if (this.id != other.getId()) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 64*(id + layerIdx);
        return hash;
    }
}

