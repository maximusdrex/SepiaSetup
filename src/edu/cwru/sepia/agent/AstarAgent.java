package edu.cwru.sepia.agent;

import edu.cwru.sepia.action.Action;
import edu.cwru.sepia.environment.model.history.History;
import edu.cwru.sepia.environment.model.state.ResourceNode;
import edu.cwru.sepia.environment.model.state.State;
import edu.cwru.sepia.environment.model.state.Unit;
import edu.cwru.sepia.util.Direction;
import javafx.collections.MapChangeListener;

import java.io.Console;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.util.*;
import java.math.*;

public class AstarAgent extends Agent {

    class MapLocation
    {
        public int x, y;

        public MapLocation(int x, int y, MapLocation cameFrom, float cost)
        {
            this.x = x;
            this.y = y;
        }
    }

    Stack<MapLocation> path;
    int footmanID, townhallID, enemyFootmanID;
    MapLocation nextLoc;

    private long totalPlanTime = 0; // nsecs
    private long totalExecutionTime = 0; //nsecs

    public AstarAgent(int playernum)
    {
        super(playernum);

        System.out.println("Constructed AstarAgent");
    }

    @Override
    public Map<Integer, Action> initialStep(State.StateView newstate, History.HistoryView statehistory) {
        // get the footman location
        List<Integer> unitIDs = newstate.getUnitIds(playernum);

        if(unitIDs.size() == 0)
        {
            System.err.println("No units found!");
            return null;
        }

        footmanID = unitIDs.get(0);

        // double check that this is a footman
        if(!newstate.getUnit(footmanID).getTemplateView().getName().equals("Footman"))
        {
            System.err.println("Footman unit not found");
            return null;
        }

        // find the enemy playernum
        Integer[] playerNums = newstate.getPlayerNumbers();
        int enemyPlayerNum = -1;
        for(Integer playerNum : playerNums)
        {
            if(playerNum != playernum) {
                enemyPlayerNum = playerNum;
                break;
            }
        }

        if(enemyPlayerNum == -1)
        {
            System.err.println("Failed to get enemy playernumber");
            return null;
        }

        // find the townhall ID
        List<Integer> enemyUnitIDs = newstate.getUnitIds(enemyPlayerNum);

        if(enemyUnitIDs.size() == 0)
        {
            System.err.println("Failed to find enemy units");
            return null;
        }

        townhallID = -1;
        enemyFootmanID = -1;
        for(Integer unitID : enemyUnitIDs)
        {
            Unit.UnitView tempUnit = newstate.getUnit(unitID);
            String unitType = tempUnit.getTemplateView().getName().toLowerCase();
            if(unitType.equals("townhall"))
            {
                townhallID = unitID;
            }
            else if(unitType.equals("footman"))
            {
                enemyFootmanID = unitID;
            }
            else
            {
                System.err.println("Unknown unit type");
            }
        }

        if(townhallID == -1) {
            System.err.println("Error: Couldn't find townhall");
            return null;
        }

        long startTime = System.nanoTime();
        path = findPath(newstate);
        totalPlanTime += System.nanoTime() - startTime;

        return middleStep(newstate, statehistory);
    }

    @Override
    public Map<Integer, Action> middleStep(State.StateView newstate, History.HistoryView statehistory) {
        long startTime = System.nanoTime();
        long planTime = 0;

        Map<Integer, Action> actions = new HashMap<Integer, Action>();

        if(shouldReplanPath(newstate, statehistory, path)) {
            long planStartTime = System.nanoTime();
            path = findPath(newstate);
            planTime = System.nanoTime() - planStartTime;
            totalPlanTime += planTime;
        }

        Unit.UnitView footmanUnit = newstate.getUnit(footmanID);

        int footmanX = footmanUnit.getXPosition();
        int footmanY = footmanUnit.getYPosition();

        if(!path.empty() && (nextLoc == null || (footmanX == nextLoc.x && footmanY == nextLoc.y))) {

            // stat moving to the next step in the path
            nextLoc = path.pop();

            System.out.println("Moving to (" + nextLoc.x + ", " + nextLoc.y + ")");
        }

        if(nextLoc != null && (footmanX != nextLoc.x || footmanY != nextLoc.y))
        {
            int xDiff = nextLoc.x - footmanX;
            int yDiff = nextLoc.y - footmanY;

            // figure out the direction the footman needs to move in
            Direction nextDirection = getNextDirection(xDiff, yDiff);

            actions.put(footmanID, Action.createPrimitiveMove(footmanID, nextDirection));
        } else {
            Unit.UnitView townhallUnit = newstate.getUnit(townhallID);

            // if townhall was destroyed on the last turn
            if(townhallUnit == null) {
                terminalStep(newstate, statehistory);
                return actions;
            }

            if(Math.abs(footmanX - townhallUnit.getXPosition()) > 1 ||
                    Math.abs(footmanY - townhallUnit.getYPosition()) > 1)
            {
                System.err.println("Invalid plan. Cannot attack townhall");
                totalExecutionTime += System.nanoTime() - startTime - planTime;
                return actions;
            }
            else {
                System.out.println("Attacking TownHall");
                // if no more movements in the planned path then attack
                actions.put(footmanID, Action.createPrimitiveAttack(footmanID, townhallID));
            }
        }

        totalExecutionTime += System.nanoTime() - startTime - planTime;
        return actions;
    }

    @Override
    public void terminalStep(State.StateView newstate, History.HistoryView statehistory) {
        System.out.println("Total turns: " + newstate.getTurnNumber());
        System.out.println("Total planning time: " + totalPlanTime/1e9);
        System.out.println("Total execution time: " + totalExecutionTime/1e9);
        System.out.println("Total time: " + (totalExecutionTime + totalPlanTime)/1e9);
    }

    @Override
    public void savePlayerData(OutputStream os) {

    }

    @Override
    public void loadPlayerData(InputStream is) {

    }

    /**
     * You will implement this method.
     *
     * This method should return true when the path needs to be replanned
     * and false otherwise. This will be necessary on the dynamic map where the
     * footman will move to block your unit.
     * 
     * You can check the position of the enemy footman with the following code:
     * state.getUnit(enemyFootmanID).getXPosition() or .getYPosition().
     * 
     * There are more examples of getting the positions of objects in SEPIA in the findPath method.
     *
     * @param state
     * @param history
     * @param currentPath
     * @return
     */
    private boolean shouldReplanPath(State.StateView state, History.HistoryView history, Stack<MapLocation> currentPath)
    {
        return false;
    }

    /**
     * This method is implemented for you. You should look at it to see examples of
     * how to find units and resources in Sepia.
     *
     * @param state
     * @return
     */
    private Stack<MapLocation> findPath(State.StateView state)
    {
        Unit.UnitView townhallUnit = state.getUnit(townhallID);
        Unit.UnitView footmanUnit = state.getUnit(footmanID);

        MapLocation startLoc = new MapLocation(footmanUnit.getXPosition(), footmanUnit.getYPosition(), null, 0);

        MapLocation goalLoc = new MapLocation(townhallUnit.getXPosition(), townhallUnit.getYPosition(), null, 0);

        MapLocation footmanLoc = null;
        if(enemyFootmanID != -1) {
            Unit.UnitView enemyFootmanUnit = state.getUnit(enemyFootmanID);
            footmanLoc = new MapLocation(enemyFootmanUnit.getXPosition(), enemyFootmanUnit.getYPosition(), null, 0);
        }

        // get resource locations
        List<Integer> resourceIDs = state.getAllResourceIds();
        Set<MapLocation> resourceLocations = new HashSet<MapLocation>();
        for(Integer resourceID : resourceIDs)
        {
            ResourceNode.ResourceView resource = state.getResourceNode(resourceID);

            resourceLocations.add(new MapLocation(resource.getXPosition(), resource.getYPosition(), null, 0));
        }

        return AstarSearch(startLoc, goalLoc, state.getXExtent(), state.getYExtent(), footmanLoc, resourceLocations);
    }

    /**
     * Was unsure if we were supposed to edit MapLocation so subclass AstarNode has a parent
     * and cost to allow A* search to work.
     */
    class AstarNode extends MapLocation {
        // Cost of getting to this node so far
        public float g;
        // Estimated final cost for A*
        public float f;
        // Node that came before this one on optimal path
        public AstarNode parent;

        public AstarNode(int x, int y, AstarNode cameFrom, float cost) {
            // Create MapLocation
            super(x, y, cameFrom, cost);
            // Set cost and parent
            this.g = cost;
            this.parent = cameFrom;
        }
    }

    /**
     * This is the method you will implement for the assignment. Your implementation
     * will use the A* algorithm to compute the optimum path from the start position to
     * a position adjacent to the goal position.
     *
     * Therefore your you need to find some possible adjacent steps which are in range 
     * and are not trees or the enemy footman.
     * Hint: Set<MapLocation> resourceLocations contains the locations of trees
     *
     * You will return a Stack of positions with the top of the stack being the first space to move to
     * and the bottom of the stack being the last space to move to. If there is no path to the townhall
     * then return null from the method and the agent will print a message and do nothing.
     * The code to execute the plan is provided for you in the middleStep method.
     *
     * As an example consider the following simple map
     *
     * F - - - -
     * x x x - x
     * H - - - -
     *
     * F is the footman
     * H is the townhall
     * x's are occupied spaces
     *
     * xExtent would be 5 for this map with valid X coordinates in the range of [0, 4]
     * x=0 is the left most column and x=4 is the right most column
     *
     * yExtent would be 3 for this map with valid Y coordinates in the range of [0, 2]
     * y=0 is the top most row and y=2 is the bottom most row
     *
     * resourceLocations would be {(0,1), (1,1), (2,1), (4,1)}
     *
     * The path would be
     *
     * (1,0)
     * (2,0)
     * (3,1)
     * (2,2)
     * (1,2)
     *
     * Notice how the initial footman position and the townhall position are not included in the path stack
     *
     * @param start Starting position of the footman
     * @param goal MapLocation of the townhall
     * @param xExtent Width of the map
     * @param yExtent Height of the map
     * @param resourceLocations Set of positions occupied by resources
     * @return Stack of positions with top of stack being first move in plan
     */
    private Stack<MapLocation> AstarSearch(MapLocation start, MapLocation goal, int xExtent, int yExtent, MapLocation enemyFootmanLoc, Set<MapLocation> resourceLocations)
    {
        /* MAX: Approximate algorithm
         * init open and closed list of AstarNodes
         * create initial node at start location
         * add initial node to open list
         * while things in openList
         *   get node of least f
         *   if goal return backtrack
         *   else generate successor nodes and add to open list
         *   check if successor nodes are already on open/closed list
         *     if on closed list skip, dont add to open list
         *     if on open list skip, if g of new is < g of old, update f (and g) of node already on
         *      open set and change its parent to parent of new node
         */
        Set<AstarNode> openList = new HashSet<AstarNode>();
        Set<AstarNode> closedList = new HashSet<AstarNode>();

        //Add initial node to open list
        AstarNode initNode = new AstarNode(start.x, start.y, null, 0);
        // Initial node's f doesn't matter (as long as it isn't null)
        initNode.f = 0;
        openList.add(initNode);

        // Iterate until goal is found
        while (!openList.isEmpty()) {
            AstarNode currentNode = null;

            //get the node in the openList with the least f val
            for (AstarNode astarNode : openList) {
                if (currentNode == null || astarNode.f < currentNode.f) {
                    currentNode = astarNode;
                }
            }

            //pop that node from the open set and add it to the closed set
            openList.remove(currentNode);
            closedList.add(currentNode);

            if(isGoal(currentNode, goal)) {
                return backtrackGoal(currentNode);
            } else {
                openList.addAll(ValidSuccessors(currentNode, xExtent, yExtent, enemyFootmanLoc, resourceLocations, openList, closedList));
            }
        }

        // No possible path available
        System.out.println("No available path.");
        // Exit with code 0 (no error)
        System.exit(0);
        return null;
    }

    /**
     * Add all parent nodes to the stack iteratively until there are no parent nodes left on the final
     * node.
     * @param goal_node The node to backtrack from
     * @return Should return a stack of MapLocations starting from the start location and ending at the goal
     */
    private Stack<MapLocation> backtrackGoal(AstarNode goal_node) {
        Stack<MapLocation> path = new Stack<MapLocation>();
        AstarNode currentNode = goal_node;
        while(currentNode.parent != null) {
            path.add(currentNode);
            currentNode = currentNode.parent;
        }
        return path;
    }

    private Boolean isGoal(MapLocation node, MapLocation goal) {
        return node.x == goal.x && node.y == goal.y;
    }

    /**
     * This generates new nodes for each empty neighboring space from node with node as its parent
     * It then calculates their f values, and checks to see if they are already on the closed list
     * and then checks the open list. 
     * Then it modifies the existing overlapping node if necessary or ignores the child.
     *      
     * @return All new children to be added to the open set are returned in the set
     */
    private Set<AstarNode> ValidSuccessors(AstarNode node, int xExtent, int yExtent, MapLocation enemyFootmanLocation, Set<MapLocation> resourceLocations, Set<AstarNode> openSet, Set<AstarNode> closedSet) {
        // generate valid neighbors
        Set<AstarNode> neighbors = AllNeighbors(node, xExtent, yExtent, enemyFootmanLocation, resourceLocations);

        Set<AstarNode> newOpenNodes = new HashSet<AstarNode>();
        Iterator<AstarNode> neighborIterator = neighbors.iterator();

        while(neighborIterator.hasNext()) {
            AstarNode currentNode = neighborIterator.next();

            if(isIn(currentNode, closedSet) != null) {
                continue;
            }

            AstarNode openNode = isIn(currentNode, openSet);
            if(openNode != null && currentNode.f < openNode.f) {
                openNode.f = currentNode.f;
                openNode.g = currentNode.g;
                openNode.parent = currentNode.parent;
            } else {
                newOpenNodes.add(currentNode);
            }
        }

        return newOpenNodes;
    }

    /**
     * If the location of node is the location of one of the nodes in AstarNode
     * return that node, else return null
     * @param node Node which may exist in set already
     * @return AstarNode node in set
     */
    private AstarNode isIn(AstarNode node, Set<AstarNode> set) {
        Iterator<AstarNode> it = set.iterator();
        while(it.hasNext()) {
            AstarNode loc = it.next();
            if (loc.x == node.x && loc.y == node.y) {
                return loc;
            }
        }
        return null;
    }


    /**
     * Same as isIn except returns MapLocations only
     * @param node
     * @param set
     * @return
     */
    private MapLocation isInMap(AstarNode node, Set<MapLocation> set) {
        Iterator<MapLocation> it = set.iterator();
        while(it.hasNext()) {
            MapLocation loc = it.next();
            if (loc.x == node.x && loc.y == node.y) {
                return loc;
            }
        }
        return null;
    }


    /**
     * Generate all empty neighboring spaces
     * Then create AstarNode of that location, setting its parent to node
     * Then calculate its f score
     * @return Returns all neighboring empty spaces as AstarNodes
     */
    private Set<AstarNode> AllNeighbors(AstarNode node, int xExtent, int yExtent, MapLocation enemyFootmanLocation, Set<MapLocation> resourceLocations) {
        Set<MapLocation> allLocations = new HashSet<MapLocation>();
        allLocations.add(enemyFootmanLocation);
        allLocations.addAll(resourceLocations);

        Set<AstarNode> newNeighbors = new HashSet<AstarNode>(Arrays.asList(
            new AstarNode(node.x + 1, node.y, node, node.g + 1),
            new AstarNode(node.x - 1, node.y, node, node.g + 1),
            new AstarNode(node.x, node.y + 1, node, node.g + 1),
            new AstarNode(node.x, node.y - 1, node, node.g + 1)));
        
        // Remove any neighbors which overlap with objects on the map
        newNeighbors.removeIf(n -> isInMap(n, allLocations) != null);

        for (AstarNode astarNode : newNeighbors) {
            astarNode.f = AstarCalcF(astarNode, enemyFootmanLocation);
        }

        return newNeighbors;
    }

    /**
     * Calculates f of a node given the node's location, goal location, and the total cost on the
     * path so far, 'g'.
     * @param node Location of the node 
     * @param goal The goal location
     * @param g
     * @return Returns 'f' where 'f(node) = g(x) + h(x)'
     *
     */
    private float AstarCalcF(AstarNode node, MapLocation goal) {
        // f(n) = g(n) + h(n)
        return node.g + AstarHeuristic(node, goal);
    }

    /**
     * Calculates the heuristic used by the A* algoritm
     * Uses the Chebyshev distance h(node) = max(abs(goal.x-node.x), abs(goal.y-node.y))
     * @param node the map location being examined
     * @return Returns the heuristic calculated for the node
     */
    private float AstarHeuristic(AstarNode node, MapLocation goal) {
        // Return Chebyshev distance
        return Math.max(Math.abs(goal.x - node.x), Math.abs(goal.x - node.x));
    }

    /**
     * Primitive actions take a direction (e.g. Direction.NORTH, Direction.NORTHEAST, etc)
     * This converts the difference between the current position and the
     * desired position to a direction.
     *
     * @param xDiff Integer equal to 1, 0 or -1
     * @param yDiff Integer equal to 1, 0 or -1
     * @return A Direction instance (e.g. SOUTHWEST) or null in the case of error
     */
    private Direction getNextDirection(int xDiff, int yDiff) {

        // figure out the direction the footman needs to move in
        if(xDiff == 1 && yDiff == 1)
        {
            return Direction.SOUTHEAST;
        }
        else if(xDiff == 1 && yDiff == 0)
        {
            return Direction.EAST;
        }
        else if(xDiff == 1 && yDiff == -1)
        {
            return Direction.NORTHEAST;
        }
        else if(xDiff == 0 && yDiff == 1)
        {
            return Direction.SOUTH;
        }
        else if(xDiff == 0 && yDiff == -1)
        {
            return Direction.NORTH;
        }
        else if(xDiff == -1 && yDiff == 1)
        {
            return Direction.SOUTHWEST;
        }
        else if(xDiff == -1 && yDiff == 0)
        {
            return Direction.WEST;
        }
        else if(xDiff == -1 && yDiff == -1)
        {
            return Direction.NORTHWEST;
        }

        System.err.println("Invalid path. Could not determine direction");
        return null;
    }
}
