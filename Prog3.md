# CSDS391 Programming Exercise 3:  Adversarial Search (100 points)
**Instructions:** Please log the last commit with "FINAL COMMIT" and **enter the final commit ID in canvas by the due date.** 

Each person in each group must commit and push their own work. **You will not get credit for work committed/pushed by someone else even if done by you.** Your commits should be clearly associated with your name or CWRU ID (abc123). Each person is expected to do an approximately equal share of the work, as shown by the git logs. **If we do not see evidence of equal contribution from the logs for someone, their individual grade will be reduced.** 

**Remember that submitting code which is not your own work constitutes an academic integrity violation.**

Please ask me or the TAs for help if uncertain or stuck.
======================================================================

The data for this exercise is in the ProgrammingExercise3.zip file on Canvas.

In this exercise, you will implement the alpha-beta algorithm for playing two player games to solve some SEPIA scenarios.

Provided are three maps and config files in data/, an opponent agent in archer_agent/ and skeleton files for your agent in src/. The archer agent is not part of any package, so add archer_agent/ as a package root in your IDE/classpath. The Minimax agent is part of the edu.cwru.sepia.agent.minimax package. The maps have two Footmen belonging to player 0 and one or two Archers belonging to player 1. Footmen are melee units and have to be adjacent to another unit to attack it, and they have lots of health. Archers are ranged units that attack at a distance. They do lots of damage but have little health. In these scenarios, your agent will control the Footmen while the provided agent, ArcherAgent, will control the Archers. The scenario will end when all the units belonging to one player are killed. So your goal is to write an agent that will quickly use the Footmen to destroy the Archers. However, these Archers will react to the Footmen and try to outmaneuver them and kill them if they can. You will use game trees to figure out what your Footmen should do.

Your agent should take one parameter as input. This is an integer that specifies the depth of the game tree in plys to look ahead. This is specified in the configuration XML file as the “Argument” parameter under the Minimax agent. At each level, the possible moves are the joint moves of both Footmen, and the joint moves of the Archers (if more than one). For this assignment, assume that the only possible actions of each Footman are to move up, down, left, right and attack if next to the Archer(s). Assume the Archers have the same set of actions: move up, down, left right and attack (which means they stay where they are). Thus when your agent is playing, there are 16 joint actions for the two Footmen you control (if you are next to an Archer, you also have the Attack action). When ArcherAgent is playing, it has either 5 or 25 (joint) actions depending on whether there are one or two Archers. You can see that even for this simple setting, the game tree is very large! 

Implement alpha-beta search to search the game tree up to the specified depth. Use linear evaluation functions to estimate the utilities of the states at the leaves of the game tree. To get these, you will need state features; use whatever state features you can think of that you think correlate with the goodness of a state. A state should have high utility if it is likely you will shortly trap and kill the Archer(s) from that state.

As part of your implementation, you will need to track how the state changes as you take actions. You should write your own state tracker for this. Don’t use SEPIA’s state cloning functions; the clones they produce are not modifiable and are intended for a different function.

Since the game tree is very large, the order of node expansion is critical. Use heuristics to determine good node orderings. For example, at a Footman level in the game tree, actions that move the Footmen away from the optimal path to the Archers are almost always guaranteed to have low utility and so should be expanded last. If adjacent to an Archer, a Footman should always attack. Similarly, if the Archer(s) is (are) very far away from your Footmen, they will not run but shoot your Footmen, so expand that action first, and so forth.

In the skeleton agent files, MinimaxAlphaBeta is the main class. It includes the main alphaBetaSearch method and a node reordering method (orderChildrenWithHeuristics). These are the methods you will fill in. A helper class, GameState, is provided to track SEPIA’s state, which can then be used to compute the utility (getUtility) and to find the possible results after taking an action from a state (getChildren). You will fill this in as well. You should not modify the GameStateChild class. It just pairs an action map with a GameState.

The code is structured in this way so that your alphaBetaSearch method can be implemented abstractly (i.e. it will not need to contain any SEPIA specific code). The SEPIA related code should reside in GameState. You can add fields and functions to GameState as needed (add comments to explain what they are doing). You can also write new constructors if you need to.

We will award up to 10 bonus points for well written code that is able to quickly search a large number of plies relative to the rest of the class (e.g. if you are in the top three runtimes to finish a scenario with a fixed large number of plies). Note that we will test your code with other maps than the ones provided with this assignment. 

Create a separate subfolder “P3” in your git repository, place your agent code in a src/ subfolder within it. You may include this file and a short README file containing your experience with the API and documentation, and anything you found confusing. Do not commit any other files.
 

Grading Rubric (same as P2)
========================

Your code must be efficient, cleanly written and easy to understand. Follow the [Google Style guide](https://google.github.io/styleguide/javaguide.html) as far as feasible. The code should handle errors and corner cases properly. You will lose points if the TAs cannot follow the logic easily or if your code breaks during testing. Note that the TAs will typically not have time to debug your code if it breaks. Your code should work on a standard installation of Java 8. **Please build modular and flexible code because the later exercises will use this code!**

Generally, point deductions will follow these criteria: 
1. Incomplete implementation/Not following assignment description: up to 100% 
2. Syntax Errors/Errors causing the code to fail to compile/run: 
   - Works with minor fix: 20% 
   - Does not work even with minor fixes: 75% 
3. Inefficient implementation: 20% 
   - Algorithm takes unreasonably long to run during grading: additional 10% 
4. Poor code design: 20% Examples:
   - Hard-to-follow logic 
   - Lack of modularity/encapsulation 
   - Ad-hoc/"spaghetti" code 
   - Duplicate code 
5. Poor UI: 
   - Bad input (inadequate exception handling, etc.): 10% 
   - Bad output (overly verbose `print` debugging statements, unclear program output): 10% 
6. Poor code style (substantially not Google Java style guide): 10% 
7. Poor documentation: 20% 
8. Non-code/README/Prog2.md files in repository: 5% per file. Examples:
   - Committing `.xml` files, `.jar` files 
   - Committing non-source files (`.idea` files, `.iml` files, etc.) 
     - Hint: use your .gitignore file! 
9. Not being able to identify git contributor by their name or CWRU ID: 5% per commit 
10.	Code not in the correct folder: 5% 
11. Basic repository errors (wrong name, not private, not accessible by TAs, no repository in cwru-courses): 20% per error

Bonus points may be awarded for the following at the grader's discretion: 
+ Exceptionally well-documented code 
+ Exceptionally well-written code 
+ Exceptionally efficient code 
