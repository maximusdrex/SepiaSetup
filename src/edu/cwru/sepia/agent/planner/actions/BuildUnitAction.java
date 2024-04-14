package edu.cwru.sepia.agent.planner.actions;

import edu.cwru.sepia.action.Action;
import edu.cwru.sepia.agent.planner.GameState;
import edu.cwru.sepia.agent.planner.units.Peasant;
import edu.cwru.sepia.environment.model.state.ResourceType;

public class BuildUnitAction implements StripsAction {
    private int t_id;

    public BuildUnitAction(int townhall_id) {
        this.t_id = townhall_id;
    }

    public boolean preconditionsMet(GameState state) {
        return state.representation.buildPeasants && state.representation.collectedGold >= 400 && state.representation.supply_left > 0;
    }

    public GameState apply(GameState state) {
        GameState new_state = new GameState(state, this);

        double approx_cost = 1;

        new_state.representation.peasants.add(new Peasant(new_state.representation.getTownHall(), new_state.representation.peasants));
        new_state.representation.supply_left -= 1;
        new_state.representation.collectedGold -= 400;
        new_state.representation.cost += approx_cost;

        return new_state;
    }

    public Action createSepiaAction(int townhall_id) {
        return Action.createCompoundProduction(t_id, Peasant.templateId);
    }

    public Integer getId() {
        return this.t_id;
    }

    public String toString() {
        return "BuildAction: townhall #" + this.t_id;
    }
    

    public boolean peasantAction() {
        return false;
    }
}
