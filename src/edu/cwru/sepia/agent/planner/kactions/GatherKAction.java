package edu.cwru.sepia.agent.planner.kactions;

import java.util.ArrayList;
import java.util.Comparator;
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

public class GatherKAction implements StripsKAction {
    // Map of peasants to resources
    Map<Integer, Integer> p2r;
    
    public GatherKAction(Map<Integer, Integer> p2r) {
        this.p2r = p2r;
    }

    public boolean preconditionsMet(GameState state) {
        // The following expression returns true if all destination resources are adjacent
        // and each peasant is carrying nothing
        if (state.representation.peasants.size() < p2r.size()) {
            return false;
        }
        for(Entry<Integer, Integer> entry : this.p2r.entrySet()) {
            Peasant p = state.representation.getPeasantByID(entry.getKey());
            if(p.currentCargo > 0) {
                return false;
            } else if (!p.getPosition().isAdjacent(state.representation.getResourceByID(entry.getValue()).getPosition())) {
                return false;
            }
        }
        return true;
    }

    public GameState apply(GameState state) {
        GameState new_state = new GameState(state, this);
        List<Double> costs = new ArrayList<Double>();

        for(Entry<Integer, Integer> entry : this.p2r.entrySet()) {
            Peasant unit = new_state.representation.getPeasantByID(entry.getKey());
            ResourceState r = new_state.representation.getResourceByID(entry.getValue());
    
            ResourceType t = r.nodeType;
            double approx_cost = Peasant.gatherCost.get(t);
    
            unit.cargoType = t;
            if(r.amountRemaining >= 100) {
                r.amountRemaining -= 100;
                unit.currentCargo += 100;
            } else {
                unit.currentCargo += r.amountRemaining;
                r.amountRemaining = 0;
            }
    
            if(r.amountRemaining == 0){
                new_state.representation.resources.remove(r);
            }

            costs.add(approx_cost);
        }

        // Since they execute in parallel, the cost is the longest one to execute
        new_state.representation.cost += costs.stream().max(Comparator.naturalOrder()).get();
        return new_state;
    }

    public Map<Integer, Action> createSepiaAction(List<Integer> peasantIDs) {
        return null;
    }

    public List<Integer> getIds() {
        // Convert key set to list
        return this.p2r.keySet().stream().collect(Collectors.toList());
    }

    public boolean peasantAction() {
        return true;
    }

}
