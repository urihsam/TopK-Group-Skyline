import java.util.List;
import java.util.ArrayList;
/**
 * Created by mashiru on 2/24/18.
 */
public class UnitGroup {
    protected List<SkNode> unitGroupNodes; // the nodes marked as group unit
    protected SkGroup coveredSkGroup; // the group of nodes covered by this unit group, containing the unitGroupNodes and the parents of unitGroupNodes

    public UnitGroup(SkNode node) {
        unitGroupNodes = new ArrayList<>();
        unitGroupNodes.add(node);
        coveredSkGroup = new SkGroup();
        updateCoverdGroup(node);
    }

    public UnitGroup(UnitGroup another) {
        unitGroupNodes = new ArrayList<>(another.getUnitGroupNodes());
        coveredSkGroup = new SkGroup(another.getCoveredSkGroup());
    }

    private void updateCoverdGroup(List<SkNode> nodes) {
        for (SkNode node: nodes)
            updateCoverdGroup(node);
    }

    private void updateCoverdGroup(SkNode node) {
        coveredSkGroup.addGroupNodes(node.getParents());
        coveredSkGroup.addGroupNodes(node);
    }

    public SkNode getLastNodeInUnit() { return unitGroupNodes.get(unitGroupNodes.size()-1); }

    public void addUnitGroup(UnitGroup ugroup) {
        unitGroupNodes.addAll(ugroup.getUnitGroupNodes());
        coveredSkGroup.addGroupNodes(ugroup.getCoveredSkGroupNodes());
    }

    public void addUnitGroupNodes(SkNode node) {
        unitGroupNodes.add(node);
        updateCoverdGroup(node);
    }

    public void addUnitGroupNodes(List<SkNode> nodes) {
        unitGroupNodes.addAll(nodes);
        updateCoverdGroup(nodes);
    }

    public List<SkNode> getUnitGroupNodes() { return unitGroupNodes; } // return the nodes in the unit group

    public SkGroup getCoveredSkGroup() { return coveredSkGroup; }

    public List<SkNode> getCoveredSkGroupNodes() { return coveredSkGroup.getGroupNodes(); } // return the nodes in the covered group

    public int getCoveredSkGroupSize() { return coveredSkGroup.getGroupSize(); }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!UnitGroup.class.isAssignableFrom(obj.getClass())) {
            return false;
        }
        final UnitGroup other = (UnitGroup) obj;

        if (!this.getUnitGroupNodes().equals(other.getUnitGroupNodes())) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        for (SkNode node: unitGroupNodes)
            hash += node.hashCode();
        return hash;
    }
}
