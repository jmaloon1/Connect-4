package hw4;

import java.util.Random;
import java.util.Scanner;

/**
 * This class holds the logic for a game to be played on console. There are several options for a console game including:
 * 		human vs. human game
 * 		human vs. AI game
 * 		AI vs. AI game (good for testing given AI)
 * 		AI vs. AI (same AI) game with given game moves by an array (good for debugging an AI)
*/
public class ConsoleCF extends CFGame{
	
	private boolean human_vs_human = false;		//true if game is an human vs human game
	private boolean human_vs_AI = false;		//true if game is an AI vs AI game
	private boolean AI_vs_AI = false;			//true if game is an AI vs AI game	
	private boolean debugging_game = false;		//true if game is an AI vs itself game for debugging
	private boolean ai1_first = false;			//true if AI1 is to go first
	private boolean hp1_first = false;		    //true if hp1 goes first in human_vs_human game
	private int[] game_moves;					//game moves to be played. True is debugging game is being played
	private int move_number = 0;				//number of move being played. Used in debugging game
	HumanPlayer hp1;							//instance of inner class HumanPlayer which allows a human player to play a move
	HumanPlayer hp2;							//instance of inner class HumanPlayer which allows a human player to play a move
	CFPlayer ai1;								//instance of a certain AI interface
	CFPlayer ai2;								//instance of a certain AI interface
	
	/**
     * This constructor sets up a human vs. human game. 2 instances of the HumanPlayer inner class are created.
     * @param none
    */
	public ConsoleCF () {
		
		human_vs_human = true;
		hp1 = new HumanPlayer();
		hp2 = new HumanPlayer();
		
		Random rand = new Random();
		if(rand.nextInt(2) == 0) {		//randomizes who starts
			hp1_first = true;
		}
		
		Scanner scan = new Scanner(System.in);
		
		System.out.println("What is player 1's name: ");
		hp1.player_name = scan.nextLine();
		
		System.out.println(" ");
		System.out.println("What is player 2's name: ");
		hp2.player_name = scan.nextLine();

		while(hp1.player_name.equals(hp2.player_name)) {
			System.out.println(" ");
			System.out.println("What is player 2's name (must be different than player 1's name: ");
			hp2.player_name = scan.nextLine();
		}

		System.out.println(" ");
		System.out.println("Press the numbers 1-7 to play a move in the associated column (leftmost is 1, rightmost is 7)");
		
		if(hp1_first) {
			boardPrint();
			play(hp1.nextMove(this)-1);
		}

	}
	
	/**
     * This constructor sets up a human vs. AI game. 1 instance of the HumanPlayer inner class is created to play against the passed in AI class.
     * @param ai: AI for human to play against
    */
	public ConsoleCF (CFPlayer ai) {
		
		human_vs_AI = true;
		hp1 = new HumanPlayer();
		this.ai1 = ai;
		
		Random rand = new Random();
		if(rand.nextInt(2) == 0) {		//randomizes who starts
			ai1_first = true;
		}
	}
	
	/**
     * This constructor sets up an AI vs. AI game. 2 AI classes are passed in to play against one another.
     * @param ai1: an AI class
     * @param ai2: another AI class
    */
	public ConsoleCF (CFPlayer ai1, CFPlayer ai2) {
		
		AI_vs_AI = true;
		this.ai1 = ai1;
		this.ai2 = ai2;
		
		Random rand = new Random();
		if(rand.nextInt(2) == 0) {		//randomizes who starts
			ai1_first = true;
		}
	}
	
	/**
     * This constructor sets up an AI vs. AI (itself) game. The moves are made not by the AI, but by an array of game moves. Used for debugging an AI.
     * @param ai1: an AI class
     * @param game_moves: moves that will be played
    */
	public ConsoleCF (CFPlayer ai, int[] game_moves) {		
		
		this.game_moves = game_moves;
		debugging_game = true;
		this.ai1 = ai;
	}
	
	/**
     * This method plays a game to completion.
     * @param none
     * @return void
    */
	public void playOut () {

		if(ai1_first && !human_vs_human) 	//plays ai1 move if ai1 determined to go first and not a human vs. human game
			play(ai1.nextMove(this));

		while(!isGameOver()) {
			
			if(human_vs_human) {

				boardPrint();			
				play(hp2.nextMove(this)-1);
				
				if(!isGameOver()) {
					boardPrint();			
					play(hp1.nextMove(this)-1);
				}
				if(isGameOver())
					boardPrint();	
			}
			else if(human_vs_AI) {
				boardPrint();		
				play(hp1.nextMove(this)-1);
				
				if(!isGameOver())
					play(ai1.nextMove(this));
				if(isGameOver())
					boardPrint();	
			}
			else if(AI_vs_AI) {
				play(ai2.nextMove(this));
				boardPrint();
				if(!isGameOver()) {
					play(ai1.nextMove(this));
					boardPrint();
				}
			
			}
			else if(debugging_game) {
				ai1.nextMove(this);
				play(game_moves[move_number]);
				boardPrint();
				move_number++;	
			}
		}
		
	}

	/**
     * This method returns the winner of game or "Draw" if the game ends in a draw.
     * @param none
     * @return String: name of winner or "Draw"
    */
	public String getWinner () {		//returns name of winner
		
		if(human_vs_human) {
			
			if(!isWinner())
				return "Draw";
			else if(!isRedTurn() && hp1_first || isRedTurn() && !hp1_first) { 
				return hp1.getName();
			}
			else {
				return hp2.getName();
			}
		}
		else if(human_vs_AI){
			
			if(!isWinner() )
				return "Draw";
			else if(!isRedTurn() && ai1_first || isRedTurn() && !ai1_first)
				return hp1.getName();
			else
				return ai1.getName();
		}	
		
		else if(AI_vs_AI) {
			if(!isWinner())
				return "Draw";
			else if(!isRedTurn() && ai1_first || isRedTurn() && !ai1_first) { 
				return ai1.getName();
			}
			else {
				return ai2.getName();
			}
		}
		else if(debugging_game){
			
			if(!isWinner() )
				return "Draw";
			else if(isRedTurn() && ai1_first || !isRedTurn() && !ai1_first)
				return "non-AI";
			else
				return ai1.getName();
		}
		else
			return "error";
	
	}
	
	/**
     * This method prints the current game board.
     * @param none
     * @return void
    */
	public void boardPrint() {		//prints board to user when called
		
		int x[][];
		
		for(int i=5; i>=0; i--) {
			for(int j = 0;j<getNumCols();j++) {
				x = getState();
				if(x[j][i] == -1)
					System.out.print(" " + x[j][i]);
				else
					System.out.print( "  " + x[j][i]);
			}
			System.out.println("");
		}
		System.out.println("");	
	}
	
	/**
	 * This inner class allows a human player to make legal moves and to play a game to completion.
	*/
	private class HumanPlayer implements CFPlayer{
	
		Scanner scan = new Scanner(System.in);
		public String player_name = "Human Player";
		
		/**
	     * This method returns a legal move (column) to be played.
	     * @param g: CFGame instance. CFGame is class with game logic and one instance of CFGame represents one game
	     * @return col: column to be played
	    */
		public int nextMove(CFGame g) {		//gets next column choice of human player
			
			int[][] gameboard = g.getState();
			
			System.out.println("What column would " + getName() + " like to play: ");
			while(!scan.hasNextInt()) {
				System.out.println("Type an integer: ");
				scan.next();
			}
			int col = scan.nextInt();
			
			while(col<1 || col>g.getNumCols() || gameboard[col-1][g.getNumRows()-1] != 0 && !g.isGameOver()) {	
				System.out.println("Illegal Move. What column would you like to play: ");
				col = scan.nextInt();
			}
			return col;	
		}
		
		/**
	     * This method returns name of this human player.
	     * @param none
	     * @return String: name of human player (default is "Human Player")
	    */
		public String getName() {
			return player_name;
		}
	}
}
