# CSDS391 Programming Exercise 4:  Planning for Automated Resource Collection (100 points)
**Instructions:** Please log the last commit with "FINAL COMMIT" and **enter the final commit ID in canvas by the due date.** 

Each person in each group must commit and push their own work. **You will not get credit for work committed/pushed by someone else even if done by you.** Your commits should be clearly associated with your name or CWRU ID (abc123). Each person is expected to do an approximately equal share of the work, as shown by the git logs. **If we do not see evidence of equal contribution from the logs for someone, their individual grade will be reduced.** 

**Remember that submitting code which is not your own work constitutes an academic integrity violation.**

Please ask me or the TAs for help if uncertain or stuck.
======================================================================

The data for this exercise is in the ProgrammingExercise4.zip file on Canvas.

In this exercise you will write a forward state space planner to solve resource collection scenarios in SEPIA. 

The scenarios you will solve are built around the “rc_3m5t.xml” map and the “midasSmall” / “midasLarge” configuration files in ProgrammingExercise4.zip. In this map, there is a townhall, a peasant, three goldmines and five forests. Assume the peasant can only move between these locations. When the peasant is next to a goldmine, it can execute a HarvestGold operation. This requires the peasant to be carrying nothing and the goldmine to have some gold. If successful, it removes 100 gold from the goldmine and results in the peasant carrying 100 gold. The three goldmines in this map have capacities 100 (nearest to townhall), 500 and 5000 (farthest from townhall) respectively. When the peasant is next to a forest, it can execute a HarvestWood operation. This requires the peasant to be carrying nothing and the forest to have some wood. If successful, it removes 100 wood from the forest and results in the peasant carrying 100 wood. The five forests in this map each contain 400 wood. Finally, when the peasant is next to the townhall, it can perform a Deposit[Gold/Wood] operation. This requires the peasant to be carrying something. If successful, it results in the peasant being emptyhanded, and adds to the total quantity of gold or wood available by the amount carried by the peasant. 

Note that this description goes beyond the STRIPS language in that we are describing numeric quantities. For this assignment, you should ignore this aspect and write a planner to handle this scenario assuming a “STRIPS-like” semantics according to the description above. Your code should consist of two parts: a planner and a plan execution agent (PEA). The planner will take as input the action specification above, the starting state and the goal and output a plan. The PEA will read in the plan and execute it in SEPIA. 

Implement a forward state space planner using the A\* algorithm that finds minimum makespan plans to achieve a given goal. Feel free to reuse code from programming assignment 2. Here makespan is time taken by the action sequence when executed. Most actions take unit time, but note that for some actions, such as compound moves, you will only be able to estimate the makespan---that is fine for this assignment. Design good heuristics for the planner to guide it towards good actions. However, it is important NOT to “pre-plan” by using your knowledge of the game. For example, do not decide to first instantiate the Gold actions followed by the Wood actions, though we know the order does not matter. Similarly, the planner needs to figure out that Deposits should follow Harvests; you should not hardcode this. Once a plan is found, write it out to a plain text file as a list of actions, one per line, along with any parameters. Your PEA can then execute this plan in SEPIA. (The PEA does not have to read the text file, you can just directly pass it the plan.)

In order to execute the found plans, you will have to translate the plan actions into SEPIA actions. Since you will be planning at a fairly high level, you may need to write some code to automatically choose target objects if needed so actions can execute properly. You are welcome to do this in a heuristic manner. If at some point the plan read in by the PEA is not executable in the current state, it should terminate with an error. Else, if all actions could be executed, it should terminate with a “success” output.

Use the midasSmall and midasLarge config files for this assignment. Set the initial state to be: the peasant is emptyhanded, the gold and wood tallies are zero and the capacities of all mines and forests are as above. Write STRIPS-like descriptions of  the actions. (a) Set the goal state to be a gold tally of 200 and a wood tally of 200. Produce a plan and execute it in Sepia. (b) Set the goal state to be a gold tally of 1000 and a wood tally of 1000. Produce a plan and execute it in SEPIA. In each case, output the total number of steps taken to actually execute the plan.

In the files provided, there is an included StripsAction interface which has two functions. preconditionsMet(GameState) takes in a GameState and returns true if that state satisfies all of the preconditions of the action. apply(GameState) takes in a GameState and applies that action’s effects, returning the resulting game state. You can use this to define different classes that implement actions like Move and Harvest if you like. This is similar to SEPIA's Action class, but specific to this assignment.

The GameState class is similar to the previous assignment. It is intended to capture the abstract state the planner reasons over, computed from SEPIA’s state.

The PlannerAgent class contains an empty AstarSearch function that takes in a GameState and returns a Stack of objects implementing the StripsAction Interface. The PlannerAgent includes a predefined method that writes the stack to a file. It calls the toString method on each Strips Action in the plan and writes the output to a line. The PlannerAgent also includes an instance of the PEAgent which is instantiated with the plan found by AstarSearch. There is a createSepiaAction function in PEAgent that takes in a StripsAction and returns a SEPIA Action where you will construct an implementable action corresponding to your plan actions.

The Position class abstracts the position of a unit.

The grading rubric we will follow is the same as that provided in Programming Exercise 2, with a modificationn that pre-planning your solution will result in a penalty up to 75%.

Create a separate subfolder “P4” in your git repository, place your agent code in a src/ subfolder within it. You may include this file and a short README file containing your experience with the API and documentation, and anything you found confusing. Do not commit any other files.
 

Grading Rubric (same as P2)
========================

Your code must be efficient, cleanly written and easy to understand. Follow the [Google Style guide](https://google.github.io/styleguide/javaguide.html) as far as feasible. The code should handle errors and corner cases properly. You will lose points if the TAs cannot follow the logic easily or if your code breaks during testing. Note that the TAs will typically not have time to debug your code if it breaks. Your code should work on a standard installation of Java 8. **Please build modular and flexible code because the later exercises will use this code!**

Generally, point deductions will follow these criteria: 
1. Incomplete implementation/Not following assignment description: up to 100% 
   - Pre-planning the solution without searching: up to 75% depending on extent.
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
