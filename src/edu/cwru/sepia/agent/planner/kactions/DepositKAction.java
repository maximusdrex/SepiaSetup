package edu.cwru.sepia.agent.planner.kactions;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import edu.cwru.sepia.action.Action;
import edu.cwru.sepia.agent.planner.GameState;
import edu.cwru.sepia.agent.planner.Position;
import edu.cwru.sepia.agent.planner.units.Peasant;
import edu.cwru.sepia.agent.planner.units.ResourceState;
import edu.cwru.sepia.environment.model.state.ResourceType;

public class DepositKAction implements StripsKAction {
    // Map of peasants to resources
    Map<Integer, Integer> p2t;
    
    public DepositKAction(Map<Integer, Integer> p2t) {
        // This supports multiple townhalls even though there is only one
        this.p2t = p2t;
    }

    public boolean preconditionsMet(GameState state) {
        // The following expression returns true if all destination resources are adjacent
        // and each peasant is carrying nothing
        if (state.representation.peasants.size() < p2t.size()) {
            return false;
        }
        for(Entry<Integer, Integer> entry : this.p2t.entrySet()) {
            Peasant p = state.representation.getPeasantByID(entry.getKey());
            if(p.currentCargo == 0) {
                return false;
            } else if (!p.getPosition().isAdjacent(state.representation.getTownHall().getPosition())) {
                return false;
            }
        }
        return true;
    }

    public boolean preconditionsMetExecution(GameState state, Map<Integer, Integer> idMap) {
        // The following expression returns true if all destination resources are adjacent
        // and each peasant is carrying nothing
        if (state.representation.peasants.size() < p2t.size()) {
            return false;
        }
        for(Entry<Integer, Integer> entry : this.p2t.entrySet()) {
            Peasant p = state.representation.getPeasantByID(idMap.get(entry.getKey()));
            if(p.currentCargo == 0) {
                return false;
            } else if (!p.getPosition().isAdjacent(state.representation.getTownHall().getPosition())) {
                return false;
            }
        }
        return true;
    }

    public GameState apply(GameState state) {
        GameState new_state = new GameState(state, this);
        List<Double> costs = new ArrayList<Double>();

        for(Entry<Integer, Integer> entry : this.p2t.entrySet()) {
            Peasant unit = new_state.representation.getPeasantByID(entry.getKey());
            double approx_cost = Peasant.depositCost;
    
            if(unit.cargoType == ResourceType.GOLD) {
                new_state.representation.collectedGold += unit.currentCargo;
            } else {
                new_state.representation.collectedWood += unit.currentCargo;
            }
            unit.currentCargo = 0;

            costs.add(approx_cost);
        }

        // Since they execute in parallel, the cost is the longest one to execute
        new_state.representation.cost += costs.stream().max(Comparator.naturalOrder()).get();
        return new_state;
    }

    public Map<Integer, Action> createSepiaAction(Map<Integer, Integer> peasantIdMap) {
        Map<Integer, Action> actionsMap = new HashMap<Integer, Action>();
        for(Entry<Integer, Integer> e : this.p2t.entrySet()) {
            actionsMap.put(peasantIdMap.get(e.getKey()),
                Action.createCompoundDeposit(peasantIdMap.get(e.getKey()), e.getValue()));
        }
        return actionsMap;
    }

    public List<Integer> getIds() {
        // Convert key set to list
        return this.p2t.keySet().stream().collect(Collectors.toList());
    }

    public boolean peasantAction() {
        return true;
    }

    public String toString() {
        return "DepositKAction, k=" + Integer.toString(this.p2t.size());
    }

}
