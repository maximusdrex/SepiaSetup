# CSDS391 Programming Exercise 2:  Pathfinding (100 points)
**Instructions:** Please log the last commit with "FINAL COMMIT" and **enter the final commit ID in canvas by the due date.** 

Each person in each group must commit and push their own work. **You will not get credit for work committed/pushed by someone else even if done by you.** Your commits should be clearly associated with your name or CWRU ID (abc123). Each person is expected to do an approximately equal share of the work, as shown by the git logs. **If we do not see evidence of equal contribution from the logs for someone, their individual grade will be reduced.** 

**Remember that submitting code which is not your own work constitutes an academic integrity violation.**

Please ask me or the TAs for help if uncertain or stuck.
======================================================================

The data for this exercise is in the ProgrammingExercise2.zip file on Canvas.

Write an agent that can move around a given SEPIA map by implementing the A\* search algorithm discussed in class (70 points). In the file AstarAgent.java, find the function AstarSearch and fill it in. This function should return a path from the starting location to the goal. The rest of the agent code has already been filled in so that once you implement the search, the agent will execute it in the game. During this step, it will output its progress with helpful messages that should let you debug your code. You can also watch VisualAgent (the GUI window showing the map) to see how your found path is being followed. The terminal output will also show the total time taken by the process. You should try to reduce this as much as possible (i.e. write efficient code!). For a proper estimate of the time taken, you can stop VisualAgent from running by deleting or commenting out the corresponding \<agentclass\> lines from the configuration file. 

For A\*, you will need a heuristic function. The Chebyshev distance is a good heuristic for this purpose. This is defined as follows: $D((x1,y1),(x2,y2))=\max( \vert x2−x1\vert , \vert y2−y1\vert )$. 

To test your algorithm, use the maze maps provided. In each map, a Footman is trapped in a maze. Somewhere in the maze is an enemy Townhall. The provided code will use your implementation to find a path that takes the Footman to the Townhall and attack it. When the Townhall is destroyed, the game will end. If it is not possible to guide the Footman to the Townhall, print “No available path.” in the terminal and quit (call System.exit(0)). 

You can use VisualAgent to check that your agent is behaving correctly. You can run the maze maps just using VisualAgent, left click on the Footman and right click on the Townhall to see the solution according to the built in pathfinding routines. 

A\* allows to you to do basic pathfinding, but in more complex scenarios, many units will be wandering around, and pathfinding is complicated. We will now simulate this using a simple scenario (30 points).

One of the provided maps (maze_16x16h_dynamic) is an environment with an enemy footman, controlled by the wicked EnemyBlockerAgent. This agent will try to prevent your agent from destroying the enemy Townhall by blocking their path (it won’t attack you otherwise). To get around this, use the “shouldReplanPath()” function in AstarAgent. Here, you can check to see if the current path is blocked. If so, you should return true and the agent will redo the search from that point. Try to write a nice function which is smart about when it needs to redo the search so you minimize the total time spent in searching and execution. 

We may award up to 10 bonus points if (i) your code is exceptionally well documented and well written and (ii) efficient so your total runtimes are among the top three in the class. However, please do NOT use map-specific or heuristic-specific optimizations in your A\* implementation. This means your implementation should be able to handle any map and any (consistent and admissible) heuristic.
 
Create a separate subfolder “P2” in your git repository, and place your agent code in a src/ subfolder within it. You may include this file and a short README file containing your experience with the API and documentation, and anything you found confusing. Do not commit any other files.

Grading Rubric 
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
