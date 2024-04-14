package edu.cwru.sepia.agent.planner.actions;

import edu.cwru.sepia.action.Action;
import edu.cwru.sepia.agent.planner.GameState;
import edu.cwru.sepia.agent.planner.Position;
import edu.cwru.sepia.agent.planner.units.Peasant;
import edu.cwru.sepia.agent.planner.units.ResourceState;
import edu.cwru.sepia.environment.model.state.ResourceType;

public class ResourceGatherAction implements StripsAction{
    private int p_id;
    private int r_id;

    public ResourceGatherAction(int peasantID, int resource_id) {
        this.p_id = peasantID;
        this.r_id = resource_id;
    }

    public boolean preconditionsMet(GameState state) {
        Peasant p = state.representation.getPeasantByID(this.p_id);
        if(p.currentCargo > 0) {
            return false;
        } else {
            return p.getPosition().isAdjacent(
                state.representation.getResourceByID(this.r_id).getPosition());
        }
    }

    public GameState apply(GameState state) {
        GameState new_state = new GameState(state, this);

        Peasant unit = new_state.representation.getPeasantByID(this.p_id);
        ResourceState r = new_state.representation.getResourceByID(this.r_id);

        ResourceType t = r.nodeType;
        double approx_cost = unit.gatherCost.get(t);

        unit.cargoType = t;
        if(r.amountRemaining >= 100) {
            r.amountRemaining -= 100;
            unit.currentCargo += 100;
        } else {
            unit.currentCargo += r.amountRemaining;
            r.amountRemaining = 0;
        }

        new_state.representation.cost += approx_cost;

        return new_state;
    }

    public Action createSepiaAction(int peasantID) {
        return Action.createCompoundGather(peasantID, r_id);
    }

    public Integer getId() {
        return this.p_id;
    }

    public String toString() {
        return "ResourceGatherAction: peasantId " + this.p_id + ", dest " + this.r_id;
    }
}
