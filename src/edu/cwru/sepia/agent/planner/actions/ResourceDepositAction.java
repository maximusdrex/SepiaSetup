package edu.cwru.sepia.agent.planner.actions;

import edu.cwru.sepia.agent.planner.GameState;
import edu.cwru.sepia.agent.planner.units.Peasant;

public class ResourceDepositAction implements StripsAction{
    private int p_id;
    private int t_id;

    public ResourceGatherAction(int peasantID, int targetID) {
        this.p_id = peasantID;
        this.t_id = targetID;
    }

    public boolean preconditionsMet(GameState state) {
        Peasant p = state.representation.getPeasantByID(this.p_id);
        if(p.currentCargo < 0) {
            return false;
        } else {
            return p.getPosition().isAdjacent(
                state.representation.getTownHall().getPosition());
        }
    }

    public GameState apply(GameState state) {
        GameState new_state = new GameState(state, this);

        Peasant unit = new_state.representation.getPeasantByID(this.id);
        double approx_cost = unit.getPosition().euclideanDistance(this.dest) * unit.moveCost;

        unit.setPosition(dest);
        new_state.representation.cost += approx_cost;

        return new_state;
    }
}
