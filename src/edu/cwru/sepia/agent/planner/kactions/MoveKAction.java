package edu.cwru.sepia.agent.planner.kactions;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import edu.cwru.sepia.action.Action;
import edu.cwru.sepia.agent.planner.GameState;
import edu.cwru.sepia.agent.planner.Position;
import edu.cwru.sepia.agent.planner.units.Peasant;

public class MoveKAction implements StripsKAction {
    Map<Integer, Position> dests;

    
    public MoveKAction(Map<Integer, Position> dests) {
        this.dests = dests;
    }

    public boolean preconditionsMet(GameState state) {
        // The following expression returns true if all destination positions are empty
        // It is techinically possible for preconditions to be met if one of the moving peasants 
        // is in one of the destinations but I will ignore that case for simplicity
        if (state.representation.peasants.size() < dests.size()) {
            return false;
        }
        return this.dests
            .values()
            .stream()
            .allMatch(pos -> state.representation.getUnitAtPos(pos) == null);
    }

    public boolean preconditionsMetExecution(GameState state, Map<Integer, Integer> idMap) {
        return preconditionsMet(state);
    }

    public GameState apply(GameState state) {
        GameState new_state = new GameState(state, this);
        List<Double> costs = new ArrayList<Double>();

        for(Entry<Integer, Position> entry : this.dests.entrySet()) {
            Peasant unit = new_state.representation.getPeasantByID(entry.getKey());
            double approx_cost = unit.getPosition().euclideanDistance(entry.getValue()) * Peasant.moveCost;

            unit.setPosition(entry.getValue());
            costs.add(approx_cost);
        }

        // Since they execute in parallel, the cost is the longest one to execute
        new_state.representation.cost += costs.stream().max(Comparator.naturalOrder()).get();
        return new_state;
    }

    public Map<Integer, Action> createSepiaAction(Map<Integer, Integer> peasantIdMap) {
        Map<Integer, Action> actionsMap = new HashMap<Integer, Action>();
        for(Entry<Integer, Position> e : this.dests.entrySet()) {
            actionsMap.put(peasantIdMap.get(e.getKey()),
                Action.createCompoundMove(peasantIdMap.get(e.getKey()), e.getValue().x, e.getValue().y));
        }
        return actionsMap;
    }

    public List<Integer> getIds() {
        // Convert key set to list
        return this.dests.keySet().stream().collect(Collectors.toList());
    }

    public boolean peasantAction() {
        return true;
    }

    public String toString() {
        return "MoveKAction, k=" + Integer.toString(dests.size());
    }
}
