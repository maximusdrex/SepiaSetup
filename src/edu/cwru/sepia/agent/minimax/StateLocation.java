package edu.cwru.sepia.agent.minimax;

import edu.cwru.sepia.action.Action;
import edu.cwru.sepia.action.ActionType;
import edu.cwru.sepia.action.DirectedAction;
import edu.cwru.sepia.action.TargetedAction;
import edu.cwru.sepia.environment.model.state.ResourceNode;
import edu.cwru.sepia.environment.model.state.ResourceNode.ResourceView;
import edu.cwru.sepia.environment.model.state.Unit.UnitView;
import edu.cwru.sepia.environment.model.state.State;
import edu.cwru.sepia.environment.model.state.State.StateView;
import edu.cwru.sepia.environment.model.state.Unit;
import edu.cwru.sepia.util.Direction;

import java.util.*;
import java.util.stream.Collectors;


public class StateLocation {

    public int x, y, id;
    private int x_extent, y_extent;
    public StateLocation(UnitView unit, StateView state) {
        this.x_extent = state.getXExtent();
        this.y_extent = state.getYExtent();

        this.x = unit.getXPosition();
        this.y = unit.getYPosition();
        this.id = unit.getID();
    }

    public StateLocation(ResourceView resource, StateView state) {
        this.x_extent = state.getXExtent();
        this.y_extent = state.getYExtent();

        this.x = resource.getXPosition();
        this.y = resource.getYPosition();
        this.id = resource.getID();
    }

    public StateLocation(StateLocation l, Direction d) {
        this.x_extent = l.x_extent;
        this.y_extent = l.y_extent;

        this.x = l.x + d.xComponent();
        this.y = l.y + d.yComponent();
        this.id = l.id;
    }

    public List<Direction> validDirections() {
        List<Direction> validList = new ArrayList<Direction>();
        for(Direction direction : Direction.values()) {
            if(this.x + direction.xComponent() >= 0 &&
                this.x + direction.xComponent() < this.x_extent &&
                this.y + direction.yComponent() >= 0 &&
                this.y + direction.yComponent() < this.y_extent) {
                    validList.add(direction);
                }
        }

        return validList;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StateLocation other = (StateLocation) o;
        return other.x == this.x && other.y == this.y;
    }

}