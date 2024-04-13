package edu.cwru.sepia.agent.planner.units;

import edu.cwru.sepia.environment.model.state.Unit;

public class TownHall extends StateUnit{
    public TownHall(TownHall parent) {
        super(parent);
    }

    public TownHall(Unit.UnitView unit) {
        super(unit);
    }
}
