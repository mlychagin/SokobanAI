Our project is in java.
To compile navigate to src/ folder and run the following command.
javac PlayGame.java -Xlint
Run the following command to test mediumTests
java PlayGame ../levels/mediumTests

To switch between modes go to line 96 or 60 in PlayGame.java
engine.findSolution(solution, Util.ids, Util.hMinMatching, Util.hManhattan, true);
NOTE in order to run random properly cleanUpAll must have a consistent mode

To switch between modes replace Util.ids with
	Util.bfs
	Util.dfs
	Util.ids
	Util.heuristic
	Util.random
	Util.randomH

To switch between heuristic types switch Util.hMinMatching with
	Util.hBoxesOnGoal
    Util.hToAnyGoal
    Util.hSingleGoal
    Util.hMoveCost
    Util.hMinMatching

To switch between distance types switch Util.hManhattan with
	Util.hManhattan
	Util.hEuclidean
	Util.hRealCost

In GameEngine.java at line 22
	Toggle tunneling by using the tunneling variable
	Toggle zoneDetection by using the zoneDetection variable
	Toggle hminPruning by using the hminPruning variable
	Toggle deadLockDetection by using the deadLockDetection variable

NOTE : File inputs must have ; at the end of the level.
Input files are provided using the professors tests in the correct format.
To switch to sokoban files instead of level files uncomment 117 and comment 116
