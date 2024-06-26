package edu.cwru.sepia.agent.planner;

import edu.cwru.sepia.action.Action;
import edu.cwru.sepia.agent.Agent;
import edu.cwru.sepia.agent.planner.kactions.StripsKAction;
import edu.cwru.sepia.environment.model.history.History;
import edu.cwru.sepia.environment.model.state.State;

import java.io.*;
import java.util.*;

/**
 * Created by Devin on 3/15/15.
 */
public class PlannerAgent extends Agent {

    final int requiredWood;
    final int requiredGold;
    final boolean buildPeasants;

    // Your PEAgent implementation. This prevents you from having to parse the text file representation of your plan.
    PEAgent peAgent;

    public PlannerAgent(int playernum, String[] params) {
        super(playernum);

        if(params.length < 3) {
            System.err.println("You must specify the required wood and gold amounts and whether peasants should be built");
        }

        requiredWood = Integer.parseInt(params[0]);
        requiredGold = Integer.parseInt(params[1]);
        buildPeasants = Boolean.parseBoolean(params[2]);


        System.out.println("required wood: " + requiredWood + " required gold: " + requiredGold + " build Peasants: " + buildPeasants);
    }

    @Override
    public Map<Integer, Action> initialStep(State.StateView stateView, History.HistoryView historyView) {
        Position.x_extent = stateView.getXExtent();
        Position.y_extent = stateView.getYExtent();

        Stack<StripsKAction> plan = AstarSearch(new GameState(stateView, playernum, requiredGold, requiredWood, buildPeasants));

        if(plan == null) {
            System.err.println("No plan was found");
            System.exit(1);
            return null;
        }

        // write the plan to a text file
        savePlan(plan);


        // Instantiates the PEAgent with the specified plan.
        peAgent = new PEAgent(playernum, plan, buildPeasants);

        return peAgent.initialStep(stateView, historyView);
    }

    @Override
    public Map<Integer, Action> middleStep(State.StateView stateView, History.HistoryView historyView) {
        if(peAgent == null) {
            System.err.println("Planning failed. No PEAgent initialized.");
            return null;
        }

        return peAgent.middleStep(stateView, historyView);
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

    /**
     * Perform an A* search of the game graph. This should return your plan as a stack of actions. This is essentially
     * the same as your first assignment. The implementations should be very similar. The difference being that your
     * nodes are now GameState objects not MapLocation objects.
     *
     * @param startState The state which is being planned from
     * @return The plan or null if no plan is found.
     */
    private Stack<StripsKAction> AstarSearch(GameState startState) {

        Set<GameState> openList = new HashSet<GameState>();
        Set<GameState> closedList = new HashSet<GameState>();

        //Add initial node to open list
        GameState initNode = startState;
        // Initial node's f doesn't matter (as long as it isn't null)
        openList.add(initNode);

        // Iterate until goal is found
        while (!openList.isEmpty()) {
            GameState currentNode = null;

            //get the node in the openList with the least f val
            for (GameState gameNode : openList) {
                if(currentNode == null) {
                    currentNode = gameNode;
                } else{
                    double gameNodeF = gameNode.getCost() + gameNode.heuristic();
                    double gameNodeH = gameNode.heuristic();
                    double currentNodeF = currentNode.getCost() + currentNode.heuristic();
                    double currentH = currentNode.heuristic();
                    if (gameNodeF < currentNodeF) {
                        currentNode = gameNode;
                    }
                }
            }

            //pop that node from the open set and add it to the closed set
            openList.remove(currentNode);
            closedList.add(currentNode);

            if(currentNode.isGoal()) {
                return backtrackGoal(currentNode);
            } else {
                openList.addAll(currentNode.generateChildren());
            }
        }

        // No possible path available
        System.out.println("No available path.");
        // Exit with code 0 (no error)
        System.exit(0);
        return null;
    }

    private Stack<StripsKAction> backtrackGoal(GameState goal_node) {
        Stack<StripsKAction> path = new Stack<StripsKAction>();
        GameState currentNode = goal_node;
        do {
            path.add(currentNode.getAction());
            currentNode = currentNode.getParent();
        } while (currentNode.getParent() != null);

        return path;
    }

    /**
     * This has been provided for you. Each strips action is converted to a string with the toString method. This means
     * each class implementing the StripsKAction interface should override toString. Your strips actions should have a
     * form matching your included Strips definition writeup. That is <action name>(<param1>, ...). So for instance the
     * move action might have the form of Move(peasantID, X, Y) and when grounded and written to the file
     * Move(1, 10, 15).
     *
     * @param plan Stack of Strips Actions that are written to the text file.
     */
    private void savePlan(Stack<StripsKAction> plan) {
        if (plan == null) {
            System.err.println("Cannot save null plan");
            return;
        }

        File outputDir = new File("saves");
        outputDir.mkdirs();

        File outputFile = new File(outputDir, "plan.txt");

        PrintWriter outputWriter = null;
        try {
            outputFile.createNewFile();

            outputWriter = new PrintWriter(outputFile.getAbsolutePath());

            Stack<StripsKAction> tempPlan = (Stack<StripsKAction>) plan.clone();
            while(!tempPlan.isEmpty()) {
                outputWriter.println(tempPlan.pop().toString());
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (outputWriter != null)
                outputWriter.close();
        }
    }
}
