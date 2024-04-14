package edu.cwru.sepia.agent.planner;

import java.util.stream.Collector;
import java.util.stream.Collectors;

import javax.annotation.Resource;
import javax.print.attribute.standard.PagesPerMinute;

import org.omg.PortableInterceptor.NON_EXISTENT;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import edu.cwru.sepia.agent.planner.actions.CompoundMoveAction;
import edu.cwru.sepia.agent.planner.actions.StripsAction;
import edu.cwru.sepia.agent.planner.units.*;
import edu.cwru.sepia.environment.model.state.ResourceType;
import edu.cwru.sepia.environment.model.state.State.StateView;
import javafx.geometry.Pos;

public class StateRepresentation {
    public int playerNum;
    public StateView stateView;
    public int requiredGold;
    public int requiredWood;

    private Boolean buildPeasants;

    public int collectedGold;
    public int collectedWood;

    public int cost;

    private List<Peasant> peasants;
    private List<ResourceState> resources;
    private TownHall townHall;

    public StateRepresentation(StateView state, int playernum, int requiredGold, int requiredWood, boolean buildPeasants) {
        this.cost = 0;

        this.peasants = state.getAllUnits().stream().filter(x -> x.getTemplateView().getName().equals("Peasant")).map(x -> new Peasant(x)).collect(Collectors.toList());
        this.resources = state.getAllResourceNodes().stream().map(x -> new ResourceState(x)).collect(Collectors.toList());
        // This assumes there is one and only one town hall
        this.townHall = state.getAllUnits().stream().filter(x -> x.getTemplateView().getName().equals("TownHall")).map(x -> new TownHall(x)).collect(Collectors.toList()).get(0);

        this.collectedGold = 0;
        this.collectedWood = 0;

        this.playerNum = playernum;
        this.requiredGold = requiredGold;
        this.requiredWood = requiredWood;
        this.buildPeasants = buildPeasants;
    }

    /**
     * Copy a child from its parent
     * This makes no changes, just copies all of the objects
     * @param parent
     */
    public StateRepresentation(StateRepresentation parent) {
        this.cost = parent.cost;

        this.peasants = parent.peasants.stream().map(x -> new Peasant(x)).collect(Collectors.toList());
        this.resources = parent.resources.stream().map(x -> new ResourceState(x)).collect(Collectors.toList());
        // Town hall can't move or change states so I'll ignore changing its state for now
        this.townHall = parent.townHall;

        this.collectedGold = parent.collectedGold;
        this.collectedWood = parent.collectedWood;

        this.playerNum = parent.playerNum;
        this.requiredGold = parent.requiredGold;
        this.requiredWood = parent.requiredWood;
        this.buildPeasants = parent.buildPeasants;
    }

    public Peasant getPeasantByID(int id) {
        return this.peasants.stream().filter(x -> x.getID() == id).findAny().orElse(null);
    }

    public ResourceState getResourceByID(int id) {
        return this.resources.stream().filter(x -> x.getID() == id).findAny().orElse(null);
    }

    public TownHall getTownHall() {
        return this.townHall;
    }

    public List<StateUnit> getAllObjects() {
        List<StateUnit> all_objects = new ArrayList<StateUnit>();
        all_objects.addAll(this.peasants);
        all_objects.addAll(this.resources);
        all_objects.add(this.townHall);
        return all_objects;
    }

    public List<Position> getAllObjectPositions() {
        return this.getAllObjects().stream().map(x -> x.getPosition()).collect(Collectors.toList());
    }

    public StateUnit getUnitAtPos(Position pos) {
        if (pos == null) {
            return null;
        }
        return getAllObjects().stream().filter(x -> x.getPosition().equals(pos)).findAny().orElse(null);
    }

    public List<StripsAction> generateActions() {
        // For each peasant generate all possible useful moves or other actions
        // First generate a move to the closest non-empty resource of a given type
        // Then generate a move to the town hall
        // Then generate any possible resource gathers
        // Then generate any possible deposits

        return null;
    }

    public List<StripsAction> generateResourceMoves(Peasant p) {
        List<Position> possible_moves = new ArrayList<Position>();
        // Generate moves for each resource type
        for(ResourceType t : ResourceType.values()) {
            Position resource_pos = closestResource(p, t).getPosition();
            List<Position> adj_pos = resource_pos.getEmptyAdjacentPositions(getAllObjectPositions());
            Comparator<Position> comp = new Position.CompPositions(p.getPosition());
            // Add the closest position to the list
            adj_pos.stream().min(comp).ifPresent(x -> possible_moves.add(x));
        }

        return possible_moves.stream().map(x -> new CompoundMoveAction(p.getID(), x)).collect(Collectors.toList());
    }

    public List<StripsAction> generateTownHallMoves(Peasant p) {
        List<Position> possible_moves = new ArrayList<Position>();
        Position resource_pos = this.townHall.getPosition();
        List<Position> adj_pos = resource_pos.getEmptyAdjacentPositions(getAllObjectPositions());
        Comparator<Position> comp = new Position.CompPositions(p.getPosition());

        adj_pos.stream().min(comp).ifPresent(x -> possible_moves.add(x));

        return possible_moves.stream().map(x -> new CompoundMoveAction(p.getID(), x)).collect(Collectors.toList());
    }

    public List<StripsAction> generateGathers(Peasant p) {
        if (p.currentCargo == 0) {

        } else {
            return new ArrayList<StripsAction>();
        }
    }

    public List<StripsAction> generateDeposits(Peasant p) {
        List<StripsAction> possible_actions = new ArrayList<StripsAction>();
        if (p.currentCargo > 0 && p.getPosition().isAdjacent(this.townHall.getPosition())) {

        }
        return possible_actions;
    }

    public <T extends StateUnit> List<T> getAdjacentUnitsIn(Peasant p, List<T> unit_list) {
        return unit_list.stream()
            .filter(resource -> p.getPosition().getAdjacentPositions().contains(resource.getPosition()))
            .collect(Collectors.toList());
    }

    public Position getClosest(Position p, List<Position> list_pos) {
        Comparator<Position> comp = new Position.CompPositions(p);
        return list_pos.stream().min(comp).orElse(null);

    }

    public ResourceState closestResource(Peasant p, ResourceType type) {
        Position position_of_closest = this.getClosest(
            p.getPosition(), 
            this.resources.stream()
                .filter(x -> x.nodeType.equals(type))
                .map(x-> x.getPosition())
                .collect(Collectors.toList()));
        if(position_of_closest == null) {
            return null;
        }
        return (ResourceState) getUnitAtPos(position_of_closest);
    }

}
