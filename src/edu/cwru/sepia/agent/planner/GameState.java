package edu.cwru.sepia.agent.planner;

import edu.cwru.sepia.agent.planner.kactions.StripsKAction;
import edu.cwru.sepia.environment.model.state.ResourceType;
import edu.cwru.sepia.environment.model.state.State;

import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * This class is used to represent the state of the game after applying one of the avaiable actions. It will also
 * track the A* specific information such as the parent pointer and the cost and heuristic function. Remember that
 * unlike the path planning A* from the first assignment the cost of an action may be more than 1. Specifically the cost
 * of executing a compound action such as move can be more than 1. You will need to account for this in your heuristic
 * and your cost function.
 *
 * The first instance is constructed from the StateView object (like in PA2). Implement the methods provided and
 * add any other methods and member variables you need.
 *
 * Some useful API calls for the state view are
 *
 * state.getXExtent() and state.getYExtent() to get the map size
 *
  * Note that SEPIA saves the townhall as a unit. Therefore when you create a GameState instance,
 * you must be able to distinguish the townhall from a peasant. This can be done by getting
 * the name of the unit type from that unit's TemplateView:
 * state.getUnit(id).getTemplateView().getName().toLowerCase(): returns "townhall" or "peasant"
 * 
 * You will also need to distinguish between gold mines and trees.
 * state.getResourceNode(id).getType(): returns the type of the given resource
 * 
 * You can compare these types to values in the ResourceNode.Type enum:
 * ResourceNode.Type.GOLD_MINE and ResourceNode.Type.TREE
 * 
 * You can check how much of a resource is remaining with the following:
 * state.getResourceNode(id).getAmountRemaining()
 *
 * I recommend storing the actions that generated the instance of the GameState in this class using whatever
 * class/structure you use to represent actions.
 */
public class GameState implements Comparable<GameState> {

    private GameState parent;
    private StripsKAction action;
    public StateRepresentation representation;

    /**
     * Construct a GameState from a stateview object. This is used to construct the initial search node. All other
     * nodes should be constructed from the another constructor you create or by factory functions that you create.
     *
     * @param state The current stateview at the time the plan is being created
     * @param playernum The player number of agent that is planning
     * @param requiredGold The goal amount of gold (e.g. 200 for the small scenario)
     * @param requiredWood The goal amount of wood (e.g. 200 for the small scenario)
     * @param buildPeasants True if the BuildPeasant action should be considered
     */
    public GameState(State.StateView state, int playernum, int requiredGold, int requiredWood, boolean buildPeasants) {
        this.parent = null;
        this.action = null;
        this.representation = new StateRepresentation(state, playernum, requiredGold, requiredWood, buildPeasants);
    }

    public GameState(GameState parent, StripsKAction action) {
        this.parent = parent;
        this.action = action;
        this.representation = new StateRepresentation(parent.representation);
    }

    /**
     * Unlike in the first A* assignment there are many possible goal states. As long as the wood and gold requirements
     * are met the peasants can be at any location and the capacities of the resource locations can be anything. Use
     * this function to check if the goal conditions are met and return true if they are.
     *
     * @return true if the goal conditions are met in this instance of game state.
     */
    public boolean isGoal() {
        return representation.collectedGold >= representation.requiredGold && representation.collectedWood >= representation.requiredWood;
    }

    /**
     * The branching factor of this search graph are much higher than the planning. Generate all of the possible
     * successor states and their associated actions in this method.
     *
     * @return A list of the possible successor states and their associated actions
     */
    public List<GameState> generateChildren() {
        // For each peasant generate all possible useful moves
        // First generate a move to the closest non-empty resource of a given type
        // Then generate a move to the town hall
        // Then generate any possible resource gathers
        // Then generate any possible deposits

        // For all StripsActions apply them to get the new game states
        // Also check to make sure their preconditions are met
        List<GameState> children = representation.generateActions()
            .stream()
            .filter(action -> action.preconditionsMet(this))
            .map(action -> action.apply(this))
            .collect(Collectors.toList());
        return children;
    }

    /**
     * Write your heuristic function here. Remember this must be admissible for the properties of A* to hold. If you
     * can come up with an easy way of computing a consistent heuristic that is even better, but not strictly necessary.
     *
     * This heuristic encourages the algorithm get as much of each of the resources as is necessary without going over
     * It also slightly encourages building units (if available)
     *
     * @return The value estimated remaining cost to reach a goal state from this state.
     */
    public double heuristic() {
        double resource_mult = 4.0;
        double gathered_mult = 1.0;
        double gold_mult = 1.0;
        double peasant_mult = 2.0;
        double r_dist_mult = 5;
        double t_dist_mult = 5;
        double h = 3.0;
        ResourceType neededR = ResourceType.GOLD;
        if (representation.requiredGold - representation.collectedGold > 0) {
            h += resource_mult * (representation.requiredGold - representation.collectedGold);
        }
        if (representation.requiredWood - representation.collectedWood > 0) {
            h += resource_mult * (representation.requiredWood - representation.collectedWood);
        }

        if (representation.requiredGold - representation.collectedGold > 0) {
            h -= gathered_mult * (representation.getGatheredGold());
        } else if (representation.requiredWood - representation.collectedWood > 0) {
            h -= gathered_mult * (representation.getGatheredWood());
            neededR = ResourceType.WOOD;
        }

        h -= peasant_mult * (representation.peasants.size() - 1) * (400 * resource_mult);

        // Incentivise moving towards a reasonable goal
        if (representation.getGatheredResources() > 0) {
            h += t_dist_mult * representation.averageDistanceToTownHall();
        } else {
            h += r_dist_mult * representation.averageDistanceToClosestResourceType(neededR);
        }
        return h;
    }

    /**
     *
     * Write the function that computes the current cost to get to this node. This is combined with your heuristic to
     * determine which actions/states are better to explore.
     *
     * @return The current cost to reach this goal
     */
    public double getCost() {
        return representation.cost;
    }

    /**
     * This is necessary to use your state in the Java priority queue. See the official priority queue and Comparable
     * interface documentation to learn how this function should work.
     *
     * @param o The other game state to compare
     * @return 1 if this state costs more than the other, 0 if equal, -1 otherwise
     */
    @Override
    public int compareTo(GameState o) {
        if(this.getCost() > o.getCost()) {
            return 1;
        } else if(this.getCost() == o.getCost()) {
            return 0;
        } else{
            return -1;
        }
    }

    /**
     * This will be necessary to use the GameState as a key in a Set or Map.
     *
     * @param o The game state to compare
     * @return True if this state equals the other state, false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        return representation.equals(((GameState) o).representation);
    }

    /**
     * This is necessary to use the GameState as a key in a HashSet or HashMap. Remember that if two objects are
     * equal they should hash to the same value.
     *
     * @return An integer hashcode that is equal for equal states.
     */
    @Override
    public int hashCode() {
        return this.representation.hashCode();
    }

    /**
     * This returns the state's parent
     * @return Gamestate parent
     */
    public GameState getParent() {
        return this.parent;
    }

    /**
     * This returns the state's parent
     * @return StripsAction action
     */
    public StripsKAction getAction() {
        return this.action;
    }

}
