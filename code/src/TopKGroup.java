import java.util.PriorityQueue;
import java.util.Comparator;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by mashiru on 2/20/18.
 */
public class TopKGroup {
    protected int k;
    protected PriorityQueue<SkGroup> topKGroup;

    public TopKGroup(int topK) {
        k = topK;
        // lambda express: Ascending order, a is in the PQ, b is new: if a < b, return negative, b stays after a ; else if a > b, return positive, b stays before a
        topKGroup = new PriorityQueue(k);
    }

    public List<SkGroup> getTopKGroup() { return new ArrayList<SkGroup>(topKGroup); }
    public int getTopKGroupSize() { return topKGroup.size(); }
    public void addSkGroup(SkGroup group) { // add new SkGroup into the PQ
        if (getTopKGroupSize() == k) {
            if (topKGroup.peek().getGroupDominates() < group.getGroupDominates()) {
                topKGroup.poll();
                topKGroup.add(group);
            }
        } else
            topKGroup.add(group);
    }
    public int getMinDominates() {
        if (topKGroup.size() == 0)
            return -1;
        return topKGroup.peek().getGroupDominates();
    }

    public void print() {
        System.out.println("\nTop "+ k + " groups");
        System.out.println("Group info:");
        for (SkGroup group: topKGroup)
            group.print();
    }
}
