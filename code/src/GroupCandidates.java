import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Created by mashiru on 2/23/18.
 */
public class GroupCandidates {
    protected int totalChilldren;
    protected Deque<SkNode> groupDeque;
    protected int maxSize;

    public GroupCandidates(int groupSize) {
        maxSize = groupSize;
        totalChilldren = 0;
        groupDeque = new ArrayDeque<>(maxSize);
    }

    public GroupCandidates(SkGroup groupUnit, int groupSize) { // take the whole group as a unit, the dominates of the group as the totalChildren
        maxSize = groupSize;
        totalChilldren = groupUnit.getGroupDominates();
        groupDeque = new ArrayDeque<>(maxSize);
        for (SkNode node: groupUnit.getGroupNodes())
            groupDeque.addLast(node);
    }

    public GroupCandidates(GroupCandidates another) {
        maxSize = another.getMaxSize();
        totalChilldren = another.getTotalChilldren();
        groupDeque = new ArrayDeque<>(another.getGroupDeque());
    }

    public int getMaxSize() { return maxSize; }

    public void pushGroupNode(SkNode node) { addGroupNode2Last(node); }

    public void addGroupNode2Last(SkNode node) {
        groupDeque.addLast(node);
        totalChilldren += node.getDominates();
    }

    public void addGroupNode2First(SkNode node) {
        groupDeque.addFirst(node);
        totalChilldren += node.getDominates();
    }

    public int getNumOfCandidates() { return groupDeque.size(); }

    public int getTotalChilldren() { return totalChilldren; }

    public Deque<SkNode> getGroupDeque() { return groupDeque; }

    public SkNode peekLastGroupNode() { return groupDeque.peekLast(); }

    public SkNode peekFirstGroupNode() { return groupDeque.peekFirst(); }

    public SkNode popGroupNode() { return pollLastGroupNode(); }

    public SkNode pollLastGroupNode() {
        SkNode node = groupDeque.pollLast();
        totalChilldren -= node.getDominates();
        return node;
    }

    public SkNode pollFirstGroupNode() {
        SkNode node = groupDeque.pollFirst();
        totalChilldren -= node.getDominates();
        return node;
    }


}
