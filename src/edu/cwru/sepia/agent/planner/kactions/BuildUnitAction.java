package edu.cwru.sepia.agent.planner.kactions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.cwru.sepia.action.Action;
import edu.cwru.sepia.agent.planner.GameState;
import edu.cwru.sepia.agent.planner.units.Peasant;
import edu.cwru.sepia.environment.model.state.ResourceType;

public class BuildUnitAction implements StripsKAction {
    private int t_id;

    public BuildUnitAction(int townhall_id) {
        this.t_id = townhall_id;
    }

    public boolean preconditionsMet(GameState state) {
        return state.representation.buildPeasants && state.representation.collectedGold >= 400 && state.representation.supply_left > 0;
    }

    public boolean preconditionsMetExecution(GameState state, Map<Integer, Integer> idMap) {
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

    public Map<Integer, Action> createSepiaAction(Map<Integer, Integer> peasantIdMap) {
        Action a = Action.createCompoundProduction(this.t_id, Peasant.templateId);
        Map<Integer, Action> m = new HashMap<Integer, Action>();
        m.put(this.t_id, a);
        return m;
    }

    public List<Integer> getIds() {
        List<Integer> ids = new ArrayList<Integer>();
        ids.add(this.t_id);
        return ids;
    }

    public String toString() {
        return "BuildAction: townhall #" + this.t_id;
    }
    

    public boolean peasantAction() {
        return false;
    }
}
