import edu.cwru.sepia.agent.Agent;
import edu.cwru.sepia.environment.model.history.History;
import edu.cwru.sepia.environment.model.state.State;
import edu.cwru.sepia.action.Action;
import edu.cwru.sepia.environment.model.state.ResourceType;
import edu.cwru.sepia.environment.model.state.ResourceNode;
import edu.cwru.sepia.environment.model.state.Unit;
import edu.cwru.sepia.environment.model.state.Unit.UnitView;
import edu.cwru.sepia.environment.model.state.Template.TemplateView;


import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Logger;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;

public class ExampleAgent extends Agent {

    private static final Logger logger = Logger.getLogger(ExampleAgent.class.getCanonicalName());

    public ExampleAgent(int playernum) {
        super(playernum);
        this.setVerbose(true);
        Handler consoleHandler = new ConsoleHandler();
        consoleHandler.setLevel(Level.FINE);
        logger.addHandler(consoleHandler);
        logger.setLevel(Level.FINE);
    }

    public Map<Integer, Action> initialStep(State.StateView newstate, History.HistoryView statehistory) {
        logger.fine("initial step called");
        return middleStep(newstate, statehistory);
    }

    public Map<Integer, Action> middleStep(State.StateView newstate, History.HistoryView statehistory) {

        int GOLD_COUNT = newstate.getResourceAmount(0, ResourceType.GOLD);
        int WOOD_COUNT = newstate.getResourceAmount(0, ResourceType.WOOD);
        int FOOD_COUNT = 1;

        List<UnitView> units = newstate.getUnits(this.playernum);
        Map<Integer, Action> actions = new HashMap<Integer, Action>();

        int townHallID = -1;

        ArrayList<Integer[]> allLocations = new ArrayList<Integer[]>();

        for (UnitView unitView : units) {
            if (unitView.getTemplateView().getName().equals("TownHall")) {
                townHallID = unitView.getID();
            }
            
            Integer[] loc = {unitView.getXPosition(), unitView.getYPosition()};
            allLocations.add(loc);
        }

        for (ResourceNode.ResourceView resource : newstate.getAllResourceNodes()) {
            Integer[] loc = {resource.getXPosition(), resource.getYPosition()};
            allLocations.add(loc);
        }
        
        TemplateView peasantTemplate = newstate.getTemplate(this.playernum, "Peasant");
        int peasantCost = peasantTemplate.getGoldCost();
        int peasantTemplateID = peasantTemplate.getID();

        TemplateView farmTemplate = newstate.getTemplate(this.playernum, "Farm");
        int farmGoldCost = farmTemplate.getGoldCost();
        int farmWoodCost = farmTemplate.getWoodCost();
        int farmTemplateID = farmTemplate.getID();

        for (UnitView unitView : units) {
            int uID = unitView.getID();

            if (unitView.getTemplateView().getName().equals("Peasant")) {
                if (unitView.getCurrentDurativeAction() == null) {
                    if (unitView.getCargoAmount() > 0) {
                        actions.put(uID, Action.createCompoundDeposit(uID, townHallID));
                    } else {
                        ResourceNode.ResourceView closestGold = null;
                        ResourceNode.ResourceView closestWood = null;
                        int closestGoldDistance = Integer.MAX_VALUE;
                        int closestWoodDistance = Integer.MAX_VALUE;
                        for (ResourceNode.ResourceView resource : newstate.getAllResourceNodes()) {
                            if (resource.getType() == ResourceNode.Type.GOLD_MINE) {
                                int distance = Math.abs(resource.getXPosition() - unitView.getXPosition()) + Math.abs(resource.getYPosition() - unitView.getYPosition());
                                if (distance < closestGoldDistance) {
                                    closestGold = resource;
                                    closestGoldDistance = distance;
                                }
                            } else if (resource.getType() == ResourceNode.Type.TREE) {
                                int distance = Math.abs(resource.getXPosition() - unitView.getXPosition()) + Math.abs(resource.getYPosition() - unitView.getYPosition());
                                if (distance < closestWoodDistance) {
                                    closestWood = resource;
                                    closestWoodDistance = distance;
                                }
                            }
                        }

                        if (GOLD_COUNT >= farmGoldCost && WOOD_COUNT >= farmWoodCost) {
                            Integer[] loc = {unitView.getXPosition() + 1, unitView.getYPosition() + 1};
                            while (!checkLoc(loc[0], loc[1], allLocations)) {
                                logger.fine("Location " + loc[0].toString() + ", " + loc[1].toString() + " is full");
                                loc[0] += 1;
                                loc[1] += 1;
                            }
                            actions.put(uID, Action.createCompoundBuild(uID, farmTemplateID, loc[0], loc[1]));
                            //logger.fine("Building farm at " + loc[0] + ", " + loc[1]);
                        } else if (GOLD_COUNT < WOOD_COUNT && closestGold != null) {
                            actions.put(uID, Action.createCompoundGather(uID, closestGold.getID()));
                        } else if (closestWood != null) {
                            actions.put(uID, Action.createCompoundGather(uID, closestWood.getID()));
                        }
                    }
                }
            } else if (unitView.getTemplateView().getName().equals("TownHall")) {
                if (GOLD_COUNT >= peasantCost && FOOD_COUNT > 0) {
                    actions.put(uID, Action.createCompoundProduction(uID, peasantTemplateID));
                }
            }
        }
        return actions;
    }

    private Boolean checkLoc(int x, int y, ArrayList<Integer[]> allLocations) {
        for (Integer[] loc : allLocations) {
            if (loc[0] == x && loc[1] == y) {
                return false;
            }
        }
        return true;
    }

    public void terminalStep(State.StateView newstate, History.HistoryView statehistory) {
        logger.fine("game over");
    }

    public void savePlayerData(OutputStream os) {

    }

    public void loadPlayerData(InputStream is) {

    }
}