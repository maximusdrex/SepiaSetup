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



public abstract class StateUnit {
    public int id, hp, damage;
    public StateLocation location;
    public StateUnit(UnitView unit, StateView state) {
        this.id = unit.getID();
        this.hp = unit.getHP();
        this.damage = unit.getTemplateView().getBasicAttack();
        this.location = new StateLocation(unit, state);
    }

    public StateUnit(StateUnit unit, Map<Integer, Action> actionsMap, GameState state) {
        this.id = unit.id;
        this.hp = unit.hp;
        this.damage = unit.damage;
        this.location = unit.location;

        for(Map.Entry<Integer, Action> actEntry: actionsMap.entrySet()) {
            switch (actEntry.getValue().getType()) {
                case PRIMITIVEMOVE:
                    if(actEntry.getKey() == this.id) this.location = new StateLocation(location, ((DirectedAction) actEntry.getValue()).getDirection());
                    break;
                case PRIMITIVEATTACK:
                    TargetedAction attackAction = (TargetedAction) actEntry.getValue();
                    if (attackAction.getTargetId() == this.id) {
                        List<StateUnit> allunits = new ArrayList<>();
                        allunits.addAll(state.playerUnits);
                        allunits.addAll(state.enemyUnits);
                        StateUnit attacker = allunits.stream()
                            .filter(x -> x.id == actEntry.getKey())
                            .findFirst()
                            .orElse(null);

                        this.hp -= attacker.damage;
                    }
                    break;
                default:
                    break;
            }
        }
    }

    public List<Action> validMoves(GameState state) {
        ArrayList<Action> actions = new ArrayList<Action>();
        List<Direction> move_directions = this.location.validDirections();
        for(Direction d : move_directions) {
            actions.add(Action.createPrimitiveMove(this.id, d));
        }
        
        return actions;
    }

    public abstract Boolean validAttack(GameState state, StateUnit enemyLocation);

    public List<Map<Integer, Action>> validActions(GameState state, List<StateUnit> enemyLocations) {
        List<Action> moves = this.validMoves(state);
        for(StateUnit enemy : enemyLocations) {
            if(this.validAttack(state, enemy)) {
                moves.add(Action.createPrimitiveAttack(this.id, enemy.id));
            }
        }

        List<Map<Integer, Action>> actions = new ArrayList<>();
        for (Action move : moves) {
            Map<Integer, Action> map = new HashMap<>();
            map.put(this.id, move);
            actions.add(map);
        }
        return actions;
    }
}