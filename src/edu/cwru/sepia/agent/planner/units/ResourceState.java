package edu.cwru.sepia.agent.planner.units;

import edu.cwru.sepia.environment.model.state.Unit;
import edu.cwru.sepia.agent.planner.Position;
import edu.cwru.sepia.environment.model.state.ResourceNode;
import edu.cwru.sepia.environment.model.state.ResourceType;

public class ResourceState extends StateUnit {
    public int amountRemaining;
    public ResourceType nodeType;

    public ResourceState(ResourceState parent) {
        super(parent);
    }

    public ResourceState(ResourceNode.ResourceView unit) {
        super(unit);
        if (unit.getType() == ResourceNode.Type.TREE) {
            this.nodeType = ResourceType.WOOD;
        } else {
            this.nodeType = ResourceType.GOLD;
        }
        this.amountRemaining = unit.getAmountRemaining();
    }

    @Override
    public boolean equals(Object o) {
        if(o.getClass() == this.getClass()){
            ResourceState cmp = (ResourceState) o;
            return this.id == cmp.id && this.pos == cmp.pos && this.nodeType == cmp.nodeType;
        }
        return false;
    }
}
