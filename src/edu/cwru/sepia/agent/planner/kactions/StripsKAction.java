package edu.cwru.sepia.agent.planner.kactions;

import java.util.List;
import java.util.Map;

import edu.cwru.sepia.action.Action;
import edu.cwru.sepia.agent.planner.GameState;

public interface StripsKAction {
    
    
    public boolean preconditionsMet(GameState state);

    // Make sure preconditions are met during execution when ids may not line up with the planner-generated ids
    public boolean preconditionsMetExecution(GameState state, Map<Integer, Integer> idMap);

    /**
     * Applies the action instance to the given GameState producing a new GameState in the process.
     *
     * As an example consider a Move action that moves peasant 1 in the NORTH direction. The partial game state
     * might specify that peasant 1 is at location (3, 3). The returned GameState should specify
     * peasant 1 at location (3, 2).
     *
     * In the process of updating the peasant state you should also update the GameState's cost and parent pointers.
     *
     * @param state State to apply action to
     * @return State resulting from successful action appliction.
     */
    public GameState apply(GameState state);

    public Map<Integer, Action> createSepiaAction(Map<Integer, Integer> peasantIdMap);

    public List<Integer> getIds();

    public boolean peasantAction();
    
}
