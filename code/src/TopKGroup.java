import java.util.Collections;
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
        // lambda express: Descending order, if a > b, return negative, a stays before b ; else if a < b, return positive, a stays after b
        topKGroup = MinMaxPriorityQueue.orderedBy(Collections.reverseOrder()).maximumSize(k).create();
    }

    public List<SkGroup> getTopKGroup() { return new ArrayList<SkGroup>(topKGroup); }
    public void addSkGroup(SkGroup group) {
        topKGroup.add(group);
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
