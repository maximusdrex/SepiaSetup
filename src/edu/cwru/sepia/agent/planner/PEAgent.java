package edu.cwru.sepia.agent.planner;

import edu.cwru.sepia.action.Action;
import edu.cwru.sepia.action.ActionFeedback;
import edu.cwru.sepia.agent.Agent;
import edu.cwru.sepia.agent.planner.actions.*;
import edu.cwru.sepia.environment.model.state.*;
import edu.cwru.sepia.environment.model.history.*;
import edu.cwru.sepia.environment.model.state.ResourceType;
import edu.cwru.sepia.environment.model.state.State;
import edu.cwru.sepia.environment.model.state.Template;
import edu.cwru.sepia.environment.model.state.Unit;
import edu.cwru.sepia.util.Direction;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

/**
 * This is an outline of the PEAgent. Implement the provided methods. You may add your own methods and members.
 */
public class PEAgent extends Agent {

    // The plan being executed
    private Stack<StripsAction> plan;    // I changed this from: private Stack<StripsAction> plan = null;

    // maps the real unit Ids to the plan's unit ids
    // when you're planning you won't know the true unit IDs that sepia assigns. So you'll use placeholders (1, 2, 3).
    // this maps those placeholders to the actual unit IDs.
    private Map<Integer, Integer> peasantIdMap;
    private int townhallId;
    private int peasantTemplateId;

    public PEAgent(int playernum, Stack<StripsAction> plan) {
        super(playernum);
        peasantIdMap = new HashMap<Integer, Integer>();
        this.plan = plan;

    }

    @Override
    public Map<Integer, Action> initialStep(State.StateView stateView, History.HistoryView historyView) {
        // gets the townhall ID and the peasant ID
        for(int unitId : stateView.getUnitIds(playernum)) {
            Unit.UnitView unit = stateView.getUnit(unitId);
            String unitType = unit.getTemplateView().getName().toLowerCase();
            if(unitType.equals("townhall")) {
                townhallId = unitId;
            } else if(unitType.equals("peasant")) {
                peasantIdMap.put(unitId, unitId);
            }
        }

        // Gets the peasant template ID. This is used when building a new peasant with the townhall
        for(Template.TemplateView templateView : stateView.getTemplates(playernum)) {
            if(templateView.getName().toLowerCase().equals("peasant")) {
                peasantTemplateId = templateView.getID();
                break;
            }
        }

        return middleStep(stateView, historyView);
    }

    /**
     * This is where you will read the provided plan and execute it. If your plan is correct then when the plan is empty
     * the scenario should end with a victory. If the scenario keeps running after you run out of actions to execute
     * then either your plan is incorrect or your execution of the plan has a bug.
     *
     * For the compound actions you will need to check their progress and wait until they are complete before issuing
     * another action for that unit. If you issue an action before the compound action is complete then the peasant
     * will stop what it was doing and begin executing the new action.
     *
	 * To check a unit's progress on the action they were executing last turn, you can use the following:
     * historyView.getCommandFeedback(playernum, stateView.getTurnNumber() - 1).get(unitID).getFeedback()
     * This returns an enum ActionFeedback. When the action is done, it will return ActionFeedback.COMPLETED
     *
     * Alternatively, you can see the feedback for each action being executed during the last turn. Here is a short example.
     * if (stateView.getTurnNumber() != 0) {
     *   Map<Integer, ActionResult> actionResults = historyView.getCommandFeedback(playernum, stateView.getTurnNumber() - 1);
     *   for (ActionResult result : actionResults.values()) {
     *     <stuff>
     *   }
     * }
     * Also remember to check your plan's preconditions before executing!
     */
    @Override
    public Map<Integer, Action> middleStep(State.StateView stateView, History.HistoryView historyView) {
        Map<Integer, Action> actions = new HashMap<>();

        /*while (!plan.isEmpty()) {
            Map<String, Object> actionMap = plan.pop();
            int type = (int) actionMap.get("type");
            int unitId = (int) actionMap.get("unitId");
            Direction direction = (Direction) actionMap.get("direction"); //need Direction class, not sure how to import
            int targetId = (int) actionMap.get("targetId");
            int x = (int) actionMap.get("x");
            int y = (int) actionMap.get("y");

            // Check if the previous action is completed
            ActionFeedback feedback = historyView.getCommandFeedback(getPlayerNumber(), stateView.getTurnNumber() - 1).get(peasantIdMap.get(unitId)).getFeedback();
            if (feedback == ActionFeedback.COMPLETED) {
                // Create the SEPIA action and add it to the actions map
                Action sepiaAction = createSepiaAction(type, peasantIdMap.get(unitId), direction, targetId, x, y);
                actions.put(peasantIdMap.get(unitId), sepiaAction);
            } else {
                // Put the action back on the stack
                plan.push(actionMap);
                break;
            }
        }*/

        return actions;
    }

   
    /**
     * Returns a SEPIA version of the specified Strips Action.
     *
     * You can create a SEPIA deposit action with the following method
     * Action.createPrimitiveDeposit(int peasantId, Direction townhallDirection)
     *
     * You can create a SEPIA harvest action with the following method
     * Action.createPrimitiveGather(int peasantId, Direction resourceDirection)
     *
     * You can create a SEPIA build action with the following method
     * Action.createPrimitiveProduction(int townhallId, int peasantTemplateId)
     *
     * You can create a SEPIA move action with the following method
     * Action.createCompoundMove(int peasantId, int x, int y)
     * 
     * Hint:
     * peasantId could be found in peasantIdMap
     *
     * these actions are stored in a mapping between the peasant unit ID executing the action and the action you created.
     *
     * @param action StripsAction
     * @return SEPIA representation of same action
     */
    private Action createSepiaAction(int type, int unitId, Direction direction, int targetId, int x, int y) {
        switch (type) {
            case 0:
                return Action.createPrimitiveGather(unitId, direction);
            case 1:
                return Action.createPrimitiveDeposit(unitId, direction);
            case 2:
                return Action.createPrimitiveProduction(townhallId, peasantTemplateId);
            case 3:
                return Action.createCompoundMove(unitId, x, y);
            default:
                throw new IllegalArgumentException("Unsupported action type: " + type);
        }
    }
    @Override
    public void terminalStep(State.StateView stateView, History.HistoryView historyView) {

    }

    @Override
    public void savePlayerData(OutputStream outputStream) {

    }

    @Override
    public void loadPlayerData(InputStream inputStream) {

    }
}
