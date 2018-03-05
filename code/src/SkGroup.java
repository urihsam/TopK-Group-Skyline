import java.util.Comparator;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by mashiru on 2/11/18.
 */
public class SkGroup { // implements Comparable{
    protected List<SkNode> gNodes;
    protected List<SkNode> dominatedNodes;
    protected long maxSizeOfDominatedGroups;
    protected List<SkGroup> dominatedGroups;

    public SkGroup() {
        gNodes = new ArrayList<>();
        dominatedNodes = new ArrayList<>();
        maxSizeOfDominatedGroups = 0;
        dominatedGroups = new ArrayList<>();
    }

    public SkGroup(SkNode gnode) {
        gNodes = new ArrayList<>();
        dominatedNodes = new ArrayList<>();
        maxSizeOfDominatedGroups = 0;
        dominatedGroups = new ArrayList<>();
        addGroupNodes(gnode);
    }

    public SkGroup(SkGroup another) {
        this.gNodes = new ArrayList<>(another.getGroupNodes());
        this.dominatedNodes = new ArrayList<>(another.getDominatedNodes());
        this.maxSizeOfDominatedGroups = another.getMaxSizeOfDominatedGroups();
        this.dominatedGroups = new ArrayList<>(another.getDominatedGroups());
    }

    public SkGroup(List<SkNode> gnodes) {
        gNodes = new ArrayList<>();
        dominatedNodes = new ArrayList<>();
        maxSizeOfDominatedGroups = 0;
        dominatedGroups = new ArrayList<>();
        addGroupNodes(gnodes);
    }

    // before invoking this construct, MAKE SURE that the kids is the merged result for dominatedNodes of gnodes
    public SkGroup(List<SkNode> gnodes, List<SkNode> dnodes) {
        gNodes = gnodes;
        dominatedNodes = dnodes;
        maxSizeOfDominatedGroups = 0;
        dominatedGroups = new ArrayList<>();
    }

    public void addGroupNodes(List<SkNode> gnodes) {
        gNodes.addAll(gnodes);
        maxSizeOfDominatedGroups += 1;
        for (SkNode gnode: gnodes) {
            updateChildrenAndDominates(gnode);
            maxSizeOfDominatedGroups *= (gnode.getSizeOfDominatedNodes() + 1);
        }
        maxSizeOfDominatedGroups -= 1;
    }

    public void addGroupNodes(SkNode gnode) {
        gNodes.add(gnode);
        maxSizeOfDominatedGroups += 1;
        updateChildrenAndDominates(gnode);
        maxSizeOfDominatedGroups *= (gnode.getSizeOfDominatedNodes() + 1);
        maxSizeOfDominatedGroups -= 1;
    }

    protected void updateChildrenAndDominates(SkNode node) {
        dominatedNodes  = merge(dominatedNodes, node.getChildren());
    }

    public List<SkNode> getGroupNodes() { return gNodes; }

    public List<SkNode> getDominatedNodes() { return dominatedNodes; }

    public long getMaxSizeOfDominatedGroups() { return maxSizeOfDominatedGroups; }

    public int getSizeOfDominatedGroups() { return dominatedGroups.size(); }

    public List<SkGroup> getDominatedGroups() { return dominatedGroups; }

    public int getSizeOfDominatedNodes() { return dominatedNodes.size(); }

    public int getGroupSize() { return gNodes.size(); }

    public void calculateDominatedGroups() {
        calculateDominatedGroups(getGroupSize(), 0);
    }

    public void calculateDominatedGroups(int endLayerIdx) {
        calculateDominatedGroups(endLayerIdx, 0);
    }

    public void calculateDominatedGroups(int endLayerIdx, float percent) {
        List<List<SkNode>> groupTrees4Check = new ArrayList<>();
        for (SkNode gnode: gNodes) {
            List<SkNode> nodes4Check = new ArrayList<>();
            nodes4Check.add(gnode); // add this group node into the check list
            // add the children in the first (endLayerIdx+1) layers or in the first (percent) percentage of this group node into the check list
            nodes4Check.addAll(gnode.getChildren(endLayerIdx, percent));
            groupTrees4Check.add(nodes4Check);
        }
        searchDominatedGroups(groupTrees4Check, new SkGroup()); // update dominatedGroups
    }

    protected void searchDominatedGroups(List<List<SkNode>> groupTrees4Check, SkGroup dominatedGroup) {
        if (dominatedGroup.getGroupSize() == getGroupSize()) { // if the dominated group has the same size as this group, then stop
            Collections.sort(dominatedGroup.getGroupNodes(), new Comparator<SkNode>() {
                @Override
                public int compare(SkNode node1, SkNode node2) {
                    return node1.getId() - node2.getId();
                }
            });
            if (!dominatedGroups.contains(dominatedGroup)) // if not contains
                dominatedGroups.add(dominatedGroup);
            return;
        }

        List<SkNode> nodes4Check = groupTrees4Check.get(0);
        for (SkNode node: nodes4Check) {
            if (!dominatedGroup.getGroupNodes().contains(node)) { // if current node is not contained in the dominated group, then add it
                SkGroup newDominatedGroup = new SkGroup(dominatedGroup);
                newDominatedGroup.addGroupNodes(node);
                searchDominatedGroups(groupTrees4Check.subList(1, groupTrees4Check.size()), newDominatedGroup);
            }
        }
    }

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

    public void print() {
        System.out.println("\n==========Group size: "+ getGroupSize() + " Number of dominatedNodes: " + getSizeOfDominatedNodes() +"==========");
        System.out.println("Group node info:");
        for (SkNode node: gNodes)
            node.print();
    }

    public void printGroups() {
        System.out.println("\n==========Group size: "+ getGroupSize() + " Number of dominatedGroups: " + getSizeOfDominatedGroups() +"==========");
        System.out.println("Group info:");
        for (SkNode node: gNodes)
            node.print();
    }

    /*@Override
    getGroupDominatedNodes*/

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
