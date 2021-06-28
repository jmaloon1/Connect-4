# Connect-4


<img src="README Images/Sudoku Normal.jpg" align="right"
     alt="Sudoku" width="90" height="120">
     
     
     
This Repository has code for Connect 4 game in Java that can be played by users against one another or agiants an AI. This origianlly started out as a project for a Java class in college but was worked on after the course to make a highly capable AI.


### Connect 4

Connect 4 is a two player game where the goal is to get 4 pieces in a row (either horizontally, vertically, or diagonally). The game board is 7 wide by 6 tall and the players alternate turns making a move. A move consists of picking a column that is not full to make a move and the game piece will go to the lowest empty position in the column.

## Classes and Interfaces in the Code and what they do

### CFGame
This class holds the logic behind the game and keeps track of the game board, whose turn it is, and whether the game is over or not.

### CFPlayer
This interface defines a player. 

### ConsoleCF
This class holds logic behind the game being played on console (can be between human players, bots or both) 

### GUICF
This class holds logic behind the game being played on a GUI (can be between human players, bots or both) 

### JackMaloonAI

This is an AI bot that can be used to play against other bots or human players. It works by giving a score to each column based on how good a move would be. For example, if a certain column results in an immediate win, that column would be rewarded a lot of "points" (the more points the better) whereas a column that results in the opponent being able to immediately win would generate a lot of negative points. The logic fo the AI goes quite deep and looks at things like making 3 in a row, blocking opponents from gaining advantageous positions, and setting up itself for success in the endgame.

### MediumAI

This is an AI bot that was an early version of *JackMaloonAI* and is good for testing the performance of more advanced AI's

### RandomAI

This is an AI bot plays random legal moves and is good for testing the performance of more advanced AI's

### Test

This class is where the game is deployed (console game or GUI game)


