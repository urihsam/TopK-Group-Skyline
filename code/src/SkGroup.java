import java.util.List;
import java.util.ArrayList;

/**
 * Created by mashiru on 2/11/18.
 */
public class SkGroup implements Comparable{
    protected List<SkNode> gNodes;
    protected List<SkNode> children;

    public SkGroup() {
        gNodes = new ArrayList<>();
        children = new ArrayList<>();
    }

    public SkGroup(SkGroup another) {
        this.gNodes = new ArrayList<>(another.getGroupNodes());
        this.children = new ArrayList<>(another.getChildren());
    }

    public SkGroup(List<SkNode> gnodes) {
        gNodes = gnodes;
        children = new ArrayList<>();
        for (SkNode node: gNodes)
            updateChildrenAndDominates(node);
    }

    // before invoking this construct, MAKE SURE that the kids is the merged result for children of gnodes
    public SkGroup(List<SkNode> gnodes, List<SkNode> kids) {
        gNodes = gnodes;
        children = kids;
    }

    public void addGroupNodes(List<SkNode> gnodes) {
        gNodes = gnodes;
        for (SkNode node: gNodes)
            updateChildrenAndDominates(node);
    }

    public void addGroupNodes(SkNode gnode) {
        gNodes.add(gnode);
        updateChildrenAndDominates(gnode);
    }

    private void updateChildrenAndDominates(SkNode node) {
        children  = merge(children, node.getChildren());
    }
    List<SkNode> getGroupNodes() { return gNodes; }
    List<SkNode> getChildren() { return children; }

    public int getGroupDominates() { return children.size(); }
    public int getGroupSize() { return gNodes.size(); }

    // Merge two group of points
    public List<SkNode> merge(List<SkNode> a, List<SkNode> b) {
        if (a.size() == 0) return b;
        if (b.size() == 0) return a;
        List<SkNode> result = new ArrayList<SkNode>();
        int aIdx = 0; int bIdx = 0;
        SkNode aEle, bEle;
        while (aIdx < a.size() && bIdx < b.size()) {
            if ((aEle=a.get(aIdx)).id < (bEle=b.get(bIdx)).id) {
                result.add(aEle);
                aIdx++;
            } else if (aEle.id > bEle.id) {
                result.add(bEle);
                bIdx++;
            } else {
                result.add(aEle);
                aIdx++; bIdx++;
            }
        }
        // append the rest
        result.addAll(new ArrayList<SkNode>(aIdx == a.size() ? b.subList(bIdx, b.size()) : a.subList(aIdx, a.size())));

        return result;
    }

    @Override
    public int compareTo(Object another) {
        /* For Ascending order*/
        return getGroupDominates() - ((SkGroup)another).getGroupDominates();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!SkGroup.class.isAssignableFrom(obj.getClass())) {
            return false;
        }
        final SkGroup other = (SkGroup) obj;

        if (!this.getGroupNodes().equals(other.getGroupNodes())) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        for (SkNode node: gNodes)
            hash += node.hashCode();
        return hash;
    }
}
