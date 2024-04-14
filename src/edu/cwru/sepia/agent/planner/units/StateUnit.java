package edu.cwru.sepia.agent.planner.units;

import edu.cwru.sepia.agent.planner.Position;
import edu.cwru.sepia.environment.model.state.State;
import edu.cwru.sepia.environment.model.state.Unit;
import javafx.geometry.Pos;
import edu.cwru.sepia.environment.model.state.ResourceNode;
import edu.cwru.sepia.environment.model.state.ResourceType;


public abstract class StateUnit {
    protected int id;
    protected Position pos;

    public StateUnit(StateUnit unit){
        this.id = unit.id;
        this.pos = new Position(unit.getPosition());
    }

    public StateUnit(int id, Position pos){
        this.id = id;
        this.pos = pos;
    }

    protected StateUnit(Unit.UnitView unitView){
        this.id = unitView.getID();
        this.pos = new Position(unitView.getXPosition(), unitView.getYPosition());
    }

    protected StateUnit(ResourceNode.ResourceView view) {
        this.id = view.getID();
        this.pos = new Position(view.getXPosition(), view.getYPosition());
    }

    /**
     * 
     * @return unit position
     */
    public Position getPosition() {
        return this.pos;
    }

    public void setPosition(Position new_pos) {
        this.pos = new Position(new_pos);
    }

    /**
     * 
     * @return unit id
     */
    public int getID() {
        return this.id;
    }

    @Override
    public boolean equals(Object o) {
        if(o.getClass() == this.getClass()){
            StateUnit cmp = (StateUnit) o;
            return this.id == cmp.id && this.pos == cmp.pos;
        }
        return false;
    }
}
