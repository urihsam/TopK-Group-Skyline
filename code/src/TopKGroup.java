import java.util.PriorityQueue;
import java.util.Comparator;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by mashiru on 2/20/18.
 */
public class TopKGroup {
    protected int k;
    protected boolean forGroup;
    protected PriorityQueue<SkGroup> topKGroup;

    public TopKGroup(int topK) {
        k = topK;
        forGroup = false;
        // lambda express: Ascending order, a is in the PQ, b is new: if a < b, return negative, b stays after a ; else if a > b, return positive, b stays before a
        topKGroup = new PriorityQueue<> (k, new Comparator<SkGroup>() { // new a topKGroup for point-dominated
            @Override
            public int compare(SkGroup thisone, SkGroup another) {
        /* For Ascending order*/
                return thisone.getSizeOfDominatedNodes() - another.getSizeOfDominatedNodes();
            }
        });
    }

    public TopKGroup(int topK, boolean forgroup) {
        k = topK;
        forGroup = forgroup;
        topKGroup = new PriorityQueue<>(k, forGroup?new Comparator<SkGroup>() { // new a topKGroup for group-dominated
            @Override
            public int compare(SkGroup thisone, SkGroup another) {
        /* For Ascending order*/
                return thisone.getSizeOfDominatedGroups() - another.getSizeOfDominatedGroups();
            }
        }:new Comparator<SkGroup>() { // new a topKGroup for point-dominated
            @Override
            public int compare(SkGroup thisone, SkGroup another) {
        /* For Ascending order*/
                return thisone.getSizeOfDominatedNodes() - another.getSizeOfDominatedNodes();
            }
        });
    }

    public List<SkGroup> getTopKGroup() { return new ArrayList<SkGroup>(topKGroup); }
    public int getTopKGroupSize() { return topKGroup.size(); }

    public void addSkGroup(SkGroup group) { // add new SkGroup for node dominates into the PQ
        if (getTopKGroupSize() == k) {
            if (forGroup?topKGroup.peek().getSizeOfDominatedGroups() < group.getSizeOfDominatedGroups(): // for group-dominated
                    topKGroup.peek().getSizeOfDominatedNodes() < group.getSizeOfDominatedNodes()) { // for point-dominated
                topKGroup.poll();
                topKGroup.add(group);
            }
        } else
            topKGroup.add(group);
    }

    public int getMinSizeOfDominatedNodes() {
        if (topKGroup.size() == 0)
            return -1;
        return topKGroup.peek().getSizeOfDominatedNodes();
    }

    public int getMinSizeOfDominatedGroups() {
        if (topKGroup.size() == 0)
            return -1;
        return topKGroup.peek().getSizeOfDominatedGroups();
    }


    public void print() {
        System.out.println("\nTop "+ k + " groups");
        System.out.println("Group info:");
        for (SkGroup group: topKGroup)
            group.print();
    }
}
