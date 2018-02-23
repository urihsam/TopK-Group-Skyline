import java.util.Comparator;
import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.MinMaxPriorityQueue;

/**
 * Created by mashiru on 2/20/18.
 */
public class TopKGroup {
    protected int k;
    protected MinMaxPriorityQueue<SkGroup> topKGroup;

    public TopKGroup(int topK) {
        k = topK;
        // lambda express: Descending order, a is in the PQ, b is new: if a > b, return negative, b stays after a ; else if a < b, return positive, b stays before a
        topKGroup = MinMaxPriorityQueue.orderedBy(new Comparator<SkGroup>() {
            public int compare(SkGroup g1, SkGroup g2) { return g1.compareTo(g2);} // g1 is new, g2 is in the PQ
        }).maximumSize(k).create();
    }

    public List<SkGroup> getTopKGroup() { return new ArrayList<SkGroup>(topKGroup); }
    public int getTopKGroupSize() { return topKGroup.size(); }
    public void addSkGroup(SkGroup group) {
        topKGroup.offer(group);
    }
    public int getMaxDominates() {
        if (topKGroup.size() == 0)
            return 0;
        return topKGroup.peekFirst().getGroupDominates();
    }
    public int getMinDominates() {
        if (topKGroup.size() == 0)
            return 0;
        return topKGroup.peekLast().getGroupDominates();
    }
}
