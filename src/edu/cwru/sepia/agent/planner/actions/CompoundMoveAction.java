package edu.cwru.sepia.agent.planner.actions;

import edu.cwru.sepia.action.Action;
import edu.cwru.sepia.agent.planner.GameState;
import edu.cwru.sepia.agent.planner.Position;
import edu.cwru.sepia.agent.planner.units.Peasant;

public class CompoundMoveAction implements StripsAction {

    public int id;
    private Position dest;

    public CompoundMoveAction(int peasantID, Position dest) {
        this.id = peasantID;
        this.dest = dest;
    }

    public boolean preconditionsMet(GameState state) {
        // This also returns false if the unit is already at the position
        // this should be fine still
        return state.representation.getUnitAtPos(this.dest) == null;
    }

    public GameState apply(GameState state) {
        GameState new_state = new GameState(state, this);

        Peasant unit = new_state.representation.getPeasantByID(this.id);
        double approx_cost = unit.getPosition().euclideanDistance(this.dest) * unit.moveCost;

        unit.setPosition(dest);
        new_state.representation.cost += approx_cost;

        return new_state;
    }

    public Action createSepiaAction(GameState state) {

    }
}
