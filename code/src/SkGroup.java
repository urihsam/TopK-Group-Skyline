import java.util.ArrayList;

/**
 * Created by mashiru on 2/11/18.
 */
public class SkGroup implements Comparable{
    protected ArrayList<SkNode> gNodes;
    protected int gDominates;

    public SkGroup(ArrayList<SkNode> nodes, int dominates) {
        gNodes = nodes;
        gDominates = dominates;
    }
    void setGroupNodes(ArrayList<SkNode> nodes) { gNodes = nodes; }
    ArrayList<SkNode> getGroupNodes() { return gNodes; }
    void setGroupDominates(int dominates) { gDominates = dominates; }
    int getGroupDominates() { return gDominates; }
    int getGroupSize() { return gNodes.size(); }

    @Override
    public int compareTo(Object other) {
        /* For Ascending order*/
        return gDominates - ((SkGroup)other).getGroupDominates();
    }
}
