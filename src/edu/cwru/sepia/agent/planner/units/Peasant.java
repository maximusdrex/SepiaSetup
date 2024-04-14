package edu.cwru.sepia.agent.planner.units;

import edu.cwru.sepia.environment.model.state.Unit;
import edu.cwru.sepia.util.Direction;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.lang.model.type.IntersectionType;

import edu.cwru.sepia.agent.planner.Position;
import edu.cwru.sepia.environment.model.state.ResourceType;

public class Peasant extends StateUnit {
    public static int templateId;
    public static int maxCargo;
    public int currentCargo;
    public ResourceType cargoType;
    public static int moveCost;
    public static Map<ResourceType, Integer> gatherCost;
    public static int depositCost;

    public Peasant(Peasant parent) {
        super(parent);
        this.currentCargo = parent.currentCargo;
        this.cargoType = parent.cargoType;
    }

    public Peasant(Unit.UnitView unit) {
        super(unit);
        gatherCost = new HashMap<ResourceType, Integer>();
        moveCost = unit.getTemplateView().getDurationMove();
        gatherCost.put(ResourceType.GOLD, unit.getTemplateView().getDurationGatherGold());
        gatherCost.put(ResourceType.WOOD, unit.getTemplateView().getDurationGatherWood());
        depositCost = unit.getTemplateView().getDurationDeposit();
        currentCargo = unit.getCargoAmount();
        cargoType = unit.getCargoType();
        maxCargo = 100;
        templateId = unit.getTemplateView().getID();
    }

    public Peasant(TownHall townhall, List<Peasant> peasants) {
        super(peasants.size() + 1, new Position(townhall.getPosition().move(Direction.WEST)));
        currentCargo = 0;
        cargoType = null;
    }

    @Override
    public boolean equals(Object o) {
        if(o.getClass() == this.getClass()){
            Peasant cmp = (Peasant) o;
            return this.id == cmp.id && this.pos == cmp.pos && this.currentCargo == cmp.currentCargo && (this.cargoType == cmp.cargoType || this.currentCargo == 0);
        }
        return false;
    }
}
