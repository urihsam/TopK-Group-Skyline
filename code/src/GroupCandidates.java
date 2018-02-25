import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Created by mashiru on 2/23/18.
 */
public class GroupCandidates {
    protected int totalSizeOfDominatedNodes;
    protected Deque<SkNode> groupDeque;
    protected int maxSize;

    public GroupCandidates(int groupSize) {
        maxSize = groupSize;
        totalSizeOfDominatedNodes = 0;
        groupDeque = new ArrayDeque<>(maxSize);
    }

    public GroupCandidates(SkGroup groupUnit, int groupSize) { // take the whole group as a unit, the dominates of the group as the totalChildren
        maxSize = groupSize;
        totalSizeOfDominatedNodes = groupUnit.getSizeOfDominatedNodes();
        groupDeque = new ArrayDeque<>(maxSize);
        for (SkNode node: groupUnit.getGroupNodes())
            groupDeque.addLast(node);
    }

    public GroupCandidates(GroupCandidates another) {
        maxSize = another.getMaxSize();
        totalSizeOfDominatedNodes = another.getTotalSizeOfDominatedNodes();
        groupDeque = new ArrayDeque<>(another.getGroupDeque());
    }

    public int getMaxSize() { return maxSize; }

    public void pushGroupNode(SkNode node) { addGroupNode2Last(node); }

    public void addGroupNode2Last(SkNode node) {
        groupDeque.addLast(node);
        totalSizeOfDominatedNodes += node.getSizeOfDominatedNodes();
    }

    public void addGroupNode2First(SkNode node) {
        groupDeque.addFirst(node);
        totalSizeOfDominatedNodes += node.getSizeOfDominatedNodes();
    }

    public int getNumOfCandidates() { return groupDeque.size(); }

    public int getTotalSizeOfDominatedNodes() { return totalSizeOfDominatedNodes; }

    public Deque<SkNode> getGroupDeque() { return groupDeque; }

    public SkNode peekLastGroupNode() { return groupDeque.peekLast(); }

    public SkNode peekFirstGroupNode() { return groupDeque.peekFirst(); }

    public SkNode popGroupNode() { return pollLastGroupNode(); }

    public SkNode pollLastGroupNode() {
        SkNode node = groupDeque.pollLast();
        totalSizeOfDominatedNodes -= node.getSizeOfDominatedNodes();
        return node;
    }

    public SkNode pollFirstGroupNode() {
        SkNode node = groupDeque.pollFirst();
        totalSizeOfDominatedNodes -= node.getSizeOfDominatedNodes();
        return node;
    }


}
