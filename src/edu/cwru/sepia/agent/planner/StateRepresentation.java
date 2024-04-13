package edu.cwru.sepia.agent.planner;

import java.util.stream.Collectors;
import java.util.List;

import edu.cwru.sepia.agent.planner.units.Peasant;
import edu.cwru.sepia.agent.planner.units.TownHall;
import edu.cwru.sepia.agent.planner.units.ResourceState;
import edu.cwru.sepia.environment.model.state.State.StateView;

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

        this
    }

    public 
}
