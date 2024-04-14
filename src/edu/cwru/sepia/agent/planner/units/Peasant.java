package edu.cwru.sepia.agent.planner.units;

import edu.cwru.sepia.environment.model.state.Unit;

import java.util.HashMap;
import java.util.Map;

import javax.lang.model.type.IntersectionType;

import edu.cwru.sepia.environment.model.state.ResourceType;

public class Peasant extends StateUnit {
    public int maxCargo;
    public int currentCargo;
    public ResourceType cargoType;
    public int moveCost;
    public Map<ResourceType, Integer> gatherCost;
    public int depositCost;

    public Peasant(Peasant parent) {
        super(parent);
        this.maxCargo = parent.maxCargo;
        this.currentCargo = parent.currentCargo;
        this.cargoType = parent.cargoType;
        this.moveCost = parent.moveCost;
        this.gatherCost = parent.gatherCost;
        this.depositCost = parent.depositCost;
    }

    public Peasant(Unit.UnitView unit) {
        super(unit);
        this.gatherCost = new HashMap<ResourceType, Integer>();
        moveCost = unit.getTemplateView().getDurationMove();
        gatherCost.put(ResourceType.GOLD, unit.getTemplateView().getDurationGatherGold());
        gatherCost.put(ResourceType.WOOD, unit.getTemplateView().getDurationGatherWood());
        depositCost = unit.getTemplateView().getDurationDeposit();
        this.currentCargo = unit.getCargoAmount();
        this.cargoType = unit.getCargoType();
        this.maxCargo = 100;
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
