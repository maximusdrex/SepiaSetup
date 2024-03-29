package edu.cwru.sepia.agent.minimax;

import edu.cwru.sepia.action.Action;
import edu.cwru.sepia.agent.Agent;
import edu.cwru.sepia.environment.model.history.History;
import edu.cwru.sepia.environment.model.state.State;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MinimaxAlphaBeta extends Agent {

    private final int numPlys;

    public MinimaxAlphaBeta(int playernum, String[] args)
    {
        super(playernum);

        if(args.length < 1)
        {
            System.err.println("You must specify the number of plys");
            System.exit(1);
        }

        numPlys = Integer.parseInt(args[0]);
    }

    @Override
    public Map<Integer, Action> initialStep(State.StateView newstate, History.HistoryView statehistory) {
        return middleStep(newstate, statehistory);
    }

    @Override
    public Map<Integer, Action> middleStep(State.StateView newstate, History.HistoryView statehistory) {
        GameStateChild bestChild = alphaBetaSearch(new GameStateChild(newstate),
                numPlys,
                Double.NEGATIVE_INFINITY,
                Double.POSITIVE_INFINITY);

        return bestChild.action;
    }

    @Override
    public void terminalStep(State.StateView newstate, History.HistoryView statehistory) {

    }

    @Override
    public void savePlayerData(OutputStream os) {

    }

    @Override
    public void loadPlayerData(InputStream is) {

    }

    /**
     * You will implement this.
     *
     * This is the main entry point to the alpha beta search. Refer to the slides, assignment description
     * and book for more information.
     *
     * Try to keep the logic in this function as abstract as possible (i.e. move as much SEPIA specific
     * code into other functions and methods)
     *
     * @param node The action and state to search from
     * @param depth The remaining number of plys under this node
     * @param alpha The current best value for the maximizing node from this node to the root
     * @param beta The current best value for the minimizing node from this node to the root
     * @return The best child of this node with updated values
     */
    public GameStateChild alphaBetaSearch(GameStateChild node, int depth, double alpha, double beta)
    {
        // Approximate algorithm: Start with minimax
        // Recursively enumerate all possible game states and their minimax values
        // Each iteration will run both min and max nodes

        if (depth == 0) {
            return node;
        }

        GameStateChild returnChild = null;

        if (numPlys % 2 == depth % 2) {
            double u = Double.NEGATIVE_INFINITY;
            for (GameStateChild child : orderChildrenWithHeuristics(node.state.getChildren(true))) {
                double childUtility = alphaBetaSearch(child, depth - 1, alpha, beta).state.getUtility();
                if (childUtility > u) {
                    u = childUtility;
                    returnChild = child;
                }
                if (u >= beta) {
                    break;
                } else {
                    alpha = Math.max(u, alpha);
                }
            }
        } else {
            double u = Double.POSITIVE_INFINITY;
            for (GameStateChild child : orderChildrenWithHeuristics(node.state.getChildren(false))) {
                double childUtility = alphaBetaSearch(child, depth - 1, alpha, beta).state.getUtility();
                if (childUtility < u) {
                    u = childUtility;
                    returnChild = child;
                }
                if (u <= alpha) {
                    break;
                } else {
                    beta = Math.min(u, beta);
                }
            }
        }

        return returnChild;
    }

    

    private class SortDistance implements Comparator<GameStateChild> {
 
        // Method
        // Sorting in ascending order of roll number
        public int compare(GameStateChild a, GameStateChild b)
        {
    
            return (int) a.state.playerUnits.stream().mapToDouble(x -> a.state.enemyUnits.stream().mapToDouble(y -> a.state.distanceBetween(x.location, y.location)).min().orElse(Double.NEGATIVE_INFINITY)).sum() -
                (int) b.state.playerUnits.stream().mapToDouble(x -> b.state.enemyUnits.stream().mapToDouble(y -> a.state.distanceBetween(x.location, y.location)).min().orElse(Double.NEGATIVE_INFINITY)).sum();
        }
    }

    /**
     * You will implement this.
     *
     * Given a list of children you will order them according to heuristics you make up.
     * See the assignment description for suggestions on heuristics to use when sorting.
     *
     * Use this function inside of your alphaBetaSearch method.
     *
     * Include a good comment about what your heuristics are and why you chose them.
     *
     * @param children
     * @return The list of children sorted by your heuristic.
     */
    public List<GameStateChild> orderChildrenWithHeuristics(List<GameStateChild> children)
    {

        return children.stream().sorted(new SortDistance()).collect(Collectors.toList());
    }
}