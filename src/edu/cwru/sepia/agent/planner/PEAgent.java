package edu.cwru.sepia.agent.planner;

import edu.cwru.sepia.action.Action;
import edu.cwru.sepia.action.ActionFeedback;
import edu.cwru.sepia.action.ActionResult;
import edu.cwru.sepia.agent.Agent;
import edu.cwru.sepia.agent.planner.kactions.*;
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
import java.util.Map.Entry;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.List;

/**
 * This is an outline of the PEAgent. Implement the provided methods. You may add your own methods and members.
 */
public class PEAgent extends Agent {

    // The plan being executed
    private Stack<StripsKAction> plan;    // I changed this from: private Stack<StripsKAction> plan = null;

    // maps the real unit Ids to the plan's unit ids
    // when you're planning you won't know the true unit IDs that sepia assigns. So you'll use placeholders (1, 2, 3).
    // this maps those placeholders to the actual unit IDs.
    private Map<Integer, Integer> peasantIdMap;
    private int townhallId;
    private int peasantTemplateId;
    private boolean buildPeasants;

    private boolean townhall_busy;

    private Map<Integer, Action> peasantCurrentAction;

    public PEAgent(int playernum, Stack<StripsKAction> plan, boolean buildPeasants) {
        super(playernum);
        peasantIdMap = new HashMap<Integer, Integer>();
        this.plan = plan;
        this.townhall_busy = false;
        peasantCurrentAction = new HashMap<Integer, Action>();
        this.buildPeasants = buildPeasants;
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

        GameState state = new GameState(stateView, this.playernum, 0, 0, buildPeasants);

        // Check feedback on current actions first
        for(int unitId : stateView.getUnitIds(playernum)) {
            Unit.UnitView unit = stateView.getUnit(unitId);
            String unitType = unit.getTemplateView().getName().toLowerCase();
            if(unitType.equals("peasant")) {
                // Restart any failed actions
                ActionResult r = historyView.getCommandFeedback(this.playernum, OBSERVER_ID).get(unit.getID());
                ActionFeedback feedback = null;
                if (r != null) {
                    feedback = r.getFeedback();
                }

                if (feedback != null && feedback != ActionFeedback.COMPLETED && feedback != ActionFeedback.INCOMPLETE) {
                    actions.put(unitId, peasantCurrentAction.get(unitId));
                } else if (feedback == null || feedback == ActionFeedback.COMPLETED) {
                    peasantCurrentAction.remove(unitId);
                }
            } else {
                // Check if townhall still busy
                ActionResult r = historyView.getCommandFeedback(this.playernum, OBSERVER_ID).get(unit.getID());
                ActionFeedback feedback = null;
                /*if (r != null) {
                    feedback = r.getFeedback();
                }
                if (feedback == null || feedback == ActionFeedback.COMPLETED) {
                    if(feedback == ActionFeedback.COMPLETED) {
                        List<Integer> pIDs = stateView.getUnitIds(playernum)
                            .stream()
                            .filter(x -> stateView.getUnit(x).getTemplateView().getName().toLowerCase().equals("peasant"))
                            .collect(Collectors.toList());
                        int new_id = peasantIdMap.keySet().stream().mapToInt(x -> x).max().orElse(1) + 1;
                        Integer new_real_id = pIDs.stream().filter(x -> !peasantIdMap.values().contains(x)).findAny().orElse(null);
                        if (new_real_id == null) {
                            throw new RuntimeException();
                        } else {
                            peasantIdMap.put(new_id, new_real_id);
                        }
                    }
                    townhall_busy = false;
                } */
            }
        }

        List<Integer> pIDs = stateView.getUnitIds(playernum)
            .stream()
            .filter(x -> stateView.getUnit(x).getTemplateView().getName().toLowerCase().equals("peasant"))
            .collect(Collectors.toList());
        if(pIDs.size() > this.peasantIdMap.size()) {
            int new_id = peasantIdMap.keySet().stream().mapToInt(x -> x).max().orElse(1) + 1;
            Integer new_real_id = pIDs.stream().filter(x -> !peasantIdMap.values().contains(x)).findAny().orElse(null);
            if (new_real_id == null) {
                throw new RuntimeException();
            } else {
                peasantIdMap.put(new_id, new_real_id);
                this.townhall_busy = false;
            }
        }

        // Then add all actions which don't conflict
        
        while (!plan.isEmpty()) {
            StripsKAction next_action = plan.pop();
            // Check if this action is for peasants to execute
            if(next_action.peasantAction()) {
                if(next_action.getIds().size() > peasantIdMap.size()) {
                    // Wait for next peasant to be built
                    plan.push(next_action);
                    break;
                }
                List<Integer> unitIds = next_action.getIds()
                    .stream()
                    .mapToInt(x -> peasantIdMap.get(x))
                    .boxed()
                    .collect(Collectors.toList());

                boolean allFree = unitIds.stream().allMatch(x -> peasantCurrentAction.get(x) == null);

                if(allFree && next_action.preconditionsMetExecution(state, peasantIdMap)) {
                    Map<Integer, Action> new_action = next_action.createSepiaAction(peasantIdMap);
                    actions.putAll(new_action);
                    peasantCurrentAction.putAll(new_action);
                } else {
                    // Wait to start the action if it wasn't added
                    plan.push(next_action);
                    break;
                }
            } else {
                // Execute build actions
                // it just uses the townhall's known id so it can take a null
                if(this.townhall_busy) {
                    plan.push(next_action);
                    break;
                } else {
                    if (!next_action.preconditionsMetExecution(state, null)) {
                        plan.push(next_action);
                        break;
                    }
                    actions.putAll(next_action.createSepiaAction(peasantIdMap));
                    this.townhall_busy = true;
                }
            }
        }

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
     * @param action StripsKAction
     * @return SEPIA representation of same action
     */
    private Action createSepiaAction(int type, int unitId, Direction direction, int targetId, int x, int y) {
        // We ended up not using this function and instead chose to create actions from inside the action classes
        // as that seemed cleaner than checking their action types here 


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
