package edu.cwru.sepia.agent.planner;

import java.util.stream.Collector;
import java.util.stream.Collectors;

import javax.annotation.Resource;
import javax.annotation.Resources;
import javax.print.attribute.standard.PagesPerMinute;

import org.omg.PortableInterceptor.NON_EXISTENT;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import edu.cwru.sepia.agent.planner.kactions.*;
import edu.cwru.sepia.agent.planner.units.*;
import edu.cwru.sepia.environment.model.state.ResourceType;
import edu.cwru.sepia.environment.model.state.State.StateView;
import javafx.geometry.Pos;

public class StateRepresentation {
    public int playerNum;
    public StateView stateView;
    public int requiredGold;
    public int requiredWood;

    public Boolean buildPeasants;

    public int collectedGold;
    public int collectedWood;
    public int supply_left;

    public double cost;

    public List<Peasant> peasants;
    public List<ResourceState> resources;
    private TownHall townHall;

    public StateRepresentation(StateView state, int playernum, int requiredGold, int requiredWood, boolean buildPeasants) {
        this.cost = 0.0;

        this.peasants = state.getAllUnits().stream().filter(x -> x.getTemplateView().getName().equals("Peasant")).map(x -> new Peasant(x)).collect(Collectors.toList());
        this.resources = state.getAllResourceNodes().stream().map(x -> new ResourceState(x)).collect(Collectors.toList());
        // This assumes there is one and only one town hall
        this.townHall = state.getAllUnits().stream().filter(x -> x.getTemplateView().getName().equals("TownHall")).map(x -> new TownHall(x)).collect(Collectors.toList()).get(0);

        this.collectedGold = state.getResourceAmount(playernum, ResourceType.GOLD);
        this.collectedWood = state.getResourceAmount(playernum, ResourceType.WOOD);

        this.playerNum = playernum;
        this.requiredGold = requiredGold;
        this.requiredWood = requiredWood;
        this.buildPeasants = buildPeasants;

        this.supply_left = state.getSupplyCap(playernum) - state.getSupplyAmount(playernum);
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

        this.supply_left = parent.supply_left;
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

    public List<StripsKAction> generateActions() {
        // For each peasant generate all possible useful moves or other actions
        // First generate a move to the closest non-empty resource of a given type
        // Then generate a move to the town hall
        // Then generate any possible resource gathers
        // Then generate any possible deposits
        List<StripsKAction> children = generateActionsForAll();
        addBuildActions(children);
        return children;
    }

    public void addBuildActions(List<StripsKAction> children) {
        if (buildPeasants && collectedGold >= 400 && supply_left > 0) {
            children.add(0, new BuildUnitAction(townHall.getID()));
        }
    }

    public List<StripsKAction> generateActionsForAll() {
        List<StripsKAction> possible_moves = new ArrayList<StripsKAction>();
        // Generate moves to closest resources of each type
        possible_moves.addAll(generateResourceMoves());
        // Generate moves to the townhall
        possible_moves.addAll(generateTownHallMoves());
        // Generate deposit actions
        possible_moves.addAll(generateDeposits());
        // Generate gather actions
        possible_moves.addAll(generateGathers());

        return possible_moves;
    }

    public List<StripsKAction> generateResourceMoves() {
        List<StripsKAction> possible_moves = new ArrayList<StripsKAction>();
        for(ResourceType t : ResourceType.values()) {
            if((t == ResourceType.GOLD && collectedGold < requiredGold) || (t == ResourceType.WOOD && collectedWood < requiredWood)) {
                List<Position> filled = new ArrayList<Position>();
                Map<Integer, Position> dests = new HashMap<Integer, Position>();
                for (Peasant p : this.peasants) {
                    Position d = generateResourcePos(p, t, filled);
                    // Don't bother adding if unit already has cargo
                    if (d != null && p.currentCargo == 0) {
                        dests.put(p.getID(), d);
                        filled.add(d);
                    }
                }
                if(dests.size() > 0) {
                    MoveKAction resource_moves = new MoveKAction(dests);
                    possible_moves.add(resource_moves);
                }
            }
        }
        return possible_moves;
    }

    public List<StripsKAction> generateTownHallMoves() {
        List<StripsKAction> possible_moves = new ArrayList<StripsKAction>();

        List<Position> filled = new ArrayList<Position>();
        Map<Integer, Position> dests = new HashMap<Integer, Position>();
        for (Peasant p : this.peasants) {
            Position d = generateTownHallPos(p, filled);
            // Don't bother adding if there is no cargo
            if (d != null && p.currentCargo > 0) {
                dests.put(p.getID(), d);
                filled.add(d);
            }
        }
        if(dests.size() > 0){
            MoveKAction resource_moves = new MoveKAction(dests);
            possible_moves.add(resource_moves);
        }

        return possible_moves;
    }

    public Position generateResourcePos(Peasant p, ResourceType t, List<Position> filled_positions) {
        ResourceState r = closestResource(p, t);
        if (r == null) {
            return null;
        }
        Position resource_pos = r.getPosition();
        // Don't add the same positions as another peasant
        List<Position> fullList = new ArrayList<Position>(getAllObjectPositions());
        fullList.addAll(filled_positions);
        List<Position> adj_pos = resource_pos.getEmptyAdjacentPositions(fullList);
        Comparator<Position> comp = new Position.CompPositions(p.getPosition());
        // Add the closest position to the list
        return adj_pos.stream().min(comp).orElse(null);
    }

    public Position generateTownHallPos(Peasant p, List<Position> filled_positions) {
        Position resource_pos = this.townHall.getPosition();
        // Don't add the same positions as another peasant
        List<Position> fullList = new ArrayList<Position>(getAllObjectPositions());
        fullList.addAll(filled_positions);
        List<Position> adj_pos = resource_pos.getEmptyAdjacentPositions(fullList);
        Comparator<Position> comp = new Position.CompPositions(p.getPosition());
        // Add the closest position to the list
        return adj_pos.stream().min(comp).orElse(null);
    }

    public List<StripsKAction> generateGathers() {
        List<StripsKAction> possible_moves = new ArrayList<StripsKAction>();

        Map<Integer, Integer> peasant2resource = new HashMap<Integer, Integer>();
        for (Peasant p : this.peasants) {
            if (p.currentCargo == 0) {
                ResourceState r = getAdjacentUnitsIn(p, this.resources)
                    .stream()
                    .filter(x -> x.amountRemaining > 0)
                    .findAny()
                    .orElse(null);
                if (r != null) {
                    peasant2resource.put(p.getID(), r.getID());
                }
            }
        }
        if(peasant2resource.size() > 0) {
            GatherKAction resource_gathers = new GatherKAction(peasant2resource);
            possible_moves.add(resource_gathers);
        }

        return possible_moves;
    }

    public List<StripsKAction> generateDeposits() {
        List<StripsKAction> possible_moves = new ArrayList<StripsKAction>();

        Map<Integer, Integer> peasant2resource = new HashMap<Integer, Integer>();
        for (Peasant p : this.peasants) {
            if (p.currentCargo > 0 && p.getPosition().isAdjacent(this.townHall.getPosition())) {
                peasant2resource.put(p.getID(), this.townHall.getID());
            }
        }
        if(peasant2resource.size() > 0) {
            DepositKAction resource_gathers = new DepositKAction(peasant2resource);
            possible_moves.add(resource_gathers);
        }

        return possible_moves;
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

    public int getGatheredGold() {
        return this.peasants.stream().filter(x -> x.cargoType == ResourceType.GOLD).mapToInt(x -> x.currentCargo).sum();
    }

    public int getGatheredWood() {
        return this.peasants.stream().filter(x -> x.cargoType == ResourceType.WOOD).mapToInt(x -> x.currentCargo).sum();
    }

    public int getGatheredResources() {
        return this.getGatheredGold() + this.getGatheredWood();
    }

    public double averageDistanceToClosestResource() {
        List<Position> rs = this.resources.stream().map(x -> x.getPosition()).collect(Collectors.toList());
        return this.peasants.stream().mapToDouble(p -> p.getPosition().euclideanDistance(getClosest(p.getPosition(), rs))).average().orElse(0.0);
    }

    public double averageDistanceToTownHall() {
        return this.peasants.stream().mapToDouble(p -> p.getPosition().euclideanDistance(this.townHall.getPosition())).average().orElse(0.0);
    }


    /**
     * This will be necessary to use the GameState as a key in a Set or Map.
     *
     * @param o The game state to compare
     * @return True if this state equals the other state, false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        StateRepresentation cmp = (StateRepresentation) o;

        // Need to compare unit states as well
        return this.cost == cmp.cost &&
            this.collectedGold == cmp.collectedGold &&
            this.collectedWood == cmp.collectedWood &&
            this.playerNum == cmp.playerNum &&
            this.requiredGold == cmp.requiredGold &&
            this.requiredWood == cmp.requiredWood &&
            this.buildPeasants == cmp.buildPeasants &&
            this.peasants.containsAll(cmp.peasants) &&
            this.peasants.size() == cmp.peasants.size() &&
            this.resources.containsAll(cmp.resources) &&
            this.resources.size() == cmp.resources.size() &&
            this.townHall.equals(cmp.townHall);
    }

    /**
     * This is necessary to use the GameState as a key in a HashSet or HashMap. Remember that if two objects are
     * equal they should hash to the same value.
     *
     * @return An integer hashcode that is equal for equal states.
     */
    @Override
    public int hashCode() {
        int hash = 7; // A prime number used as a starting point
        hash = 31 * hash + this.playerNum;
        hash = 31 * hash + this.collectedGold;
        hash = 31 * hash + this.collectedWood;
        hash = 31 * hash + this.requiredGold;
        hash = 31 * hash + this.requiredWood;
        hash = 31 * hash + (this.buildPeasants ? 1 : 0);
        hash = 31 * hash + this.peasants.hashCode();
        hash = 31 * hash + this.resources.hashCode();
        hash = 31 * hash + (this.townHall == null ? 0 : this.townHall.hashCode());
        return hash;
    }

}
