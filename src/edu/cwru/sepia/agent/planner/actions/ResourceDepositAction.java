package edu.cwru.sepia.agent.planner.actions;

import edu.cwru.sepia.action.Action;
import edu.cwru.sepia.agent.planner.GameState;
import edu.cwru.sepia.agent.planner.units.Peasant;
import edu.cwru.sepia.environment.model.state.ResourceType;

public class ResourceDepositAction implements StripsAction{
    private int p_id;
    private int t_id;

    public ResourceDepositAction(int peasantID, int targetID) {
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

        Peasant unit = new_state.representation.getPeasantByID(this.p_id);
        double approx_cost = unit.depositCost;

        if(unit.cargoType == ResourceType.GOLD) {
            new_state.representation.collectedGold += unit.currentCargo;
        } else {
            new_state.representation.collectedWood += unit.currentCargo;
        }
        unit.currentCargo = 0;
        new_state.representation.cost += approx_cost;

        return new_state;
    }

    public Action createSepiaAction(int peasantID) {
        return Action.createCompoundDeposit(peasantID, t_id);
    }

    public Integer getId() {
        return this.p_id;
    }

    public String toString() {
        return "ResourceDepositAction: peasantId " + this.p_id + ", dest " + this.t_id;
    }
}
