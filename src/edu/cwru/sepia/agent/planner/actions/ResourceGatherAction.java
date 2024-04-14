package edu.cwru.sepia.agent.planner.actions;

import edu.cwru.sepia.agent.planner.GameState;
import edu.cwru.sepia.agent.planner.Position;
import edu.cwru.sepia.agent.planner.units.Peasant;

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

}
