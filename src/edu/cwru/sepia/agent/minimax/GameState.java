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
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * This class stores all of the information the agent
 * needs to know about the state of the game. For example this
 * might include things like footmen HP and positions.
 *
 * Add any information or methods you would like to this class,
 * but do not delete or change the signatures of the provided methods.
 */
public class GameState {

    private List<StateLocation> obstacles;
    public List<StateUnit> playerUnits, enemyUnits;

    private class FootmanUnit extends StateUnit {

        public FootmanUnit(UnitView unit, StateView state) {
            super(unit, state);
        }

        public FootmanUnit(StateUnit unit, Map<Integer, Action> actionsMap, GameState state) {
            super(unit, actionsMap, state);
        }

        @Override
        public Boolean validAttack(GameState state, StateUnit enemy) {
            for(Direction d : Direction.values()) {
                if(new StateLocation(this.location, d).equals(enemy.location)) {
                    return true;
                }
            }
            return false;
        }
    }

    private class ArcherUnit extends StateUnit {

        public ArcherUnit(UnitView unit, StateView state) {
            super(unit, state);
        }

        public ArcherUnit(StateUnit unit, Map<Integer, Action> actionsMap, GameState state) {
            super(unit, actionsMap, state);
        }

        // TODO: Change this, it currently is the same as for footmen attacks but I'm not totally sure which
        // attacks are valid for archers
        @Override
        public Boolean validAttack(GameState state, StateUnit enemy) {
            return true;
        }
    }

    /**
     * You will implement this constructor. It will
     * extract all of the needed state information from the built in
     * SEPIA state view.
     *
     * You may find the following state methods useful:
     *
     * state.getXExtent() and state.getYExtent(): get the map dimensions
     * state.getAllResourceIds(): returns the IDs of all of the obstacles in the map
     * state.getResourceNode(int resourceID): Return a ResourceView for the given ID
     *
     * For a given ResourceView you can query the position using
     * resource.getXPosition() and resource.getYPosition()
     * 
     * You can get a list of all the units belonging to a player with the following command:
     * state.getUnitIds(int playerNum): gives a list of all unit IDs beloning to the player.
     * You control player 0, the enemy controls player 1.
     * 
     * In order to see information about a specific unit, you must first get the UnitView
     * corresponding to that unit.
     * state.getUnit(int id): gives the UnitView for a specific unit
     * 
     * With a UnitView you can find information about a given unit
     * unitView.getXPosition() and unitView.getYPosition(): get the current location of this unit
     * unitView.getHP(): get the current health of this unit
     * 
     * SEPIA stores information about unit types inside TemplateView objects.
     * For a given unit type you will need to find statistics from its Template View.
     * unitView.getTemplateView().getRange(): This gives you the attack range
     * unitView.getTemplateView().getBasicAttack(): The amount of damage this unit type deals
     * unitView.getTemplateView().getBaseHealth(): The initial amount of health of this unit type
     *
     * @param state Current state of the episode
     */
    public GameState(State.StateView state) {
        this.obstacles = state.getAllResourceIds().stream().map(x -> new StateLocation(state.getResourceNode(x), state)).collect(Collectors.toList());

        this.playerUnits = state.getUnitIds(0).stream().map(x -> new FootmanUnit(state.getUnit(x), state)).collect(Collectors.toList());
        this.enemyUnits = state.getUnitIds(1).stream().map(x -> new ArcherUnit(state.getUnit(x), state)).collect(Collectors.toList());
    }


    public GameState(GameState parent, Map<Integer, Action> actionsTaken) {
        this.obstacles = parent.obstacles;

        this.playerUnits = parent.playerUnits.stream().map(x -> new FootmanUnit(x, actionsTaken, parent)).filter(x -> x.hp > 0).collect(Collectors.toList());
        this.enemyUnits = parent.enemyUnits.stream().map(x -> new ArcherUnit(x, actionsTaken, parent)).filter(x -> x.hp > 0).collect(Collectors.toList());
    }

    /**
     * You will implement this function.
     *
     * You should use weighted linear combination of features.
     * The features may be primitives from the state (such as hp of a unit)
     * or they may be higher level summaries of information from the state such
     * as distance to a specific location. Come up with whatever features you think
     * are useful and weight them appropriately.
     *
     * It is recommended that you start simple until you have your algorithm working. Then watch
     * your agent play and try to add fekatures that correct mistakes it makes. However, remember that
     * your features should be as fast as possible to compute. If the features are slow then you will be
     * able to do less plys in a turn.
     *
     * Add a good comment about what is in your utility and why you chose those features.
     *
     * @return The weighted linear combination of the features
     */
    public double getUtility() {
        double playerHP = this.playerUnits.stream().mapToDouble(x -> (double) x.hp).sum();
        double enemyHP = this.enemyUnits.stream().mapToDouble(x -> (double) x.hp).sum();

        double numFootmen = (double) this.playerUnits.stream().count();
        double numEnemy = (double) this.enemyUnits.stream().count();

        double averageDistance = this.playerUnits.stream()
                .mapToDouble(x -> this.enemyUnits.stream().mapToDouble(y -> distanceBetween(x.location, y.location)).min().orElse(Double.NEGATIVE_INFINITY))
                .sum() / ((numFootmen > 0 && numEnemy > 0) ? numFootmen : 1);

        return (1 * playerHP) + (-2 * enemyHP) + (2 * numFootmen) + (-2 * numEnemy) + ((-1 / 2) * averageDistance);
    }

    public double distanceBetween(StateLocation x, StateLocation y) {
        return Math.sqrt(Math.pow(y.x - x.x, 2) + Math.pow(y.y - x.y, 2));
    }

    /**
     * You will implement this function.
     *
     * This will return a list of GameStateChild objects. You will generate all of the possible
     * actions in a step and then determine the resulting game state from that action. These are your GameStateChildren.
     * 
     * It may be useful to be able to create a SEPIA Action. In this assignment you will
     * deal with movement and attacking actions. There are static methods inside the Action
     * class that allow you to create basic actions:
     * Action.createPrimitiveAttack(int attackerID, int targetID): returns an Action where
     * the attacker unit attacks the target unit.
     * Action.createPrimitiveMove(int unitID, Direction dir): returns an Action where the unit
     * moves one space in the specified direction.
     *
     * You may find it useful to iterate over all the different directions in SEPIA. This can
     * be done with the following loop:
     * for(Direction direction : Directions.values())
     *
     * To get the resulting position from a move in that direction you can do the following
     * x += direction.xComponent()
     * y += direction.yComponent()
     * 
     * If you wish to explicitly use a Direction you can use the Direction enum, for example
     * Direction.NORTH or Direction.NORTHEAST.
     * 
     * You can check many of the properties of an Action directly:
     * action.getType(): returns the ActionType of the action
     * action.getUnitID(): returns the ID of the unit performing the Action
     * 
     * ActionType is an enum containing different types of actions. The methods given above
     * create actions of type ActionType.PRIMITIVEATTACK and ActionType.PRIMITIVEMOVE.
     * 
     * For attack actions, you can check the unit that is being attacked. To do this, you
     * must cast the Action as a TargetedAction:
     * ((TargetedAction)action).getTargetID(): returns the ID of the unit being attacked
     * 
     * @return All possible actions and their associated resulting game state
     */
    public List<GameStateChild> getChildren(Boolean player) {
        // If player true, then MAX node and footmen are moving
    
        if(player) {
            List<List<Map<Integer, Action>>> allactions = this.playerUnits.stream().map(x -> x.validActions(this, this.enemyUnits)).collect(Collectors.toList());
            List<Map<Integer, Action>> combinations = getCombinations(allactions);
            return combinations.stream().map(x -> new GameStateChild(x, new GameState(this, x))).collect(Collectors.toList());
        } else {
            List<List<Map<Integer, Action>>> allactions = this.enemyUnits.stream().map(x -> x.validActions(this, this.playerUnits)).collect(Collectors.toList());
            List<Map<Integer, Action>> combinations = getCombinations(allactions);
            return combinations.stream().map(x -> new GameStateChild(x, new GameState(this, x))).collect(Collectors.toList());
        }
    }

    private List<Map<Integer, Action>> getCombinations(List<List<Map<Integer, Action>>> actions) {
        List<List<Map<Integer, Action>>> combinations = generateCombinations(actions, new ArrayList<>(), 0);
        return combinations.stream().map(x -> mergeMaps(x)).collect(Collectors.toList());
    }

    private <T> List<List<T>> generateCombinations(List<List<T>> input, List<List<T>> output, int index) {
        if(index >= input.size()) {
            return output;
        }

        List<List<T>> newOut = new ArrayList<>();
        if(output.size() == 0) {
            for(T t : input.get(index)) {
                List<T> element = new ArrayList<>();
                element.add(t);
                newOut.add(element);
            }
        } else {
            for(List<T> oldList : output) {
                for(T t : input.get(index)) {
                    List<T> element = new ArrayList<>(oldList);
                    element.add(t);
                    newOut.add(element);
                }
            }
        }

        return generateCombinations(input, newOut, index + 1);
    }

    private Map<Integer, Action> mergeMaps(List<Map<Integer, Action>> map) {
        Map<Integer, Action> mergedMap = new HashMap<>();
        for (Map<Integer, Action> innerMap : map) {
            mergedMap.putAll(innerMap);
        }
        return mergedMap;
    }
}
