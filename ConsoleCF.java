
import java.util.Random;
import java.util.Scanner;

public class ConsoleCF extends CFGame{
	
	private boolean ai1First = false;
	private boolean AIvsAI = false;
	private boolean humanvsAI = false;
	private boolean gameChecker = false;
	private int[] game_moves;
	private int move_number = 0;
	HumanPlayer hp;
	CFPlayer ai1;
	CFPlayer ai2;
	
	public ConsoleCF (CFPlayer ai) {		//sets up human vs. ai game
		
		Random rand = new Random();
		ai1 = ai;
		hp = new HumanPlayer();
		humanvsAI = true;
		
		if(rand.nextInt(2) == 0) {		//randomizes who starts
			ai1First = true;
		}
	}
	
	public ConsoleCF (CFPlayer ai1, CFPlayer ai2) { 		//sets up ai vs. ai game
		
		Random rand = new Random();
		AIvsAI = true;
		this.ai1 = ai1;
		this.ai2 = ai2;
		
		if(rand.nextInt(2) == 0) {		//randomizes who starts
			ai1First = true;
		}
	}
	
	public ConsoleCF (CFPlayer ai, int[] game_moves, boolean aiStarts) {		//sets up human vs. ai game
		
		Random rand = new Random();
		this.game_moves = game_moves;
		gameChecker = true;
		ai1 = ai;
		
		if(aiStarts) {		//randomizes who starts
			ai1First = true;
		}
	}
	
	public void playOut () {		//plays game until it is over

		if(ai1First) 			
			play(ai1.nextMove(this));
		
		while(!isGameOver()) {
			
			if(AIvsAI) {
				play(ai2.nextMove(this));
				boardPrint();
				if(!isGameOver()) {
					play(ai1.nextMove(this));
					boardPrint();
				}
			
			}

			else if(humanvsAI) {
				boardPrint();			//prints board to human player
				play(hp.nextMove(this)-1);
				
				if(!isGameOver())
					play(ai1.nextMove(this));
				if(isGameOver())
					boardPrint();	
			}
			
			else if(gameChecker) {
				ai1.nextMove(this);
				play(game_moves[move_number]);
				boardPrint();
				move_number++;	
			}
		}
		
	}
	
	public String getWinner () {		//returns name of winner
		
		HumanPlayer hp = new HumanPlayer();
		
		if(AIvsAI) {
			if(!isWinner())
				return "Draw";
			else if(!isRedTurn() && ai1First || isRedTurn() && !ai1First) { 
				return ai1.getName();
			}
			else {
				return ai2.getName();
			}
		}
		else {
			
			if(!isWinner() )
				return "Draw";
			else if(isRedTurn() && ai1First || !isRedTurn() && !ai1First)
				return hp.getName();
			else
				return ai1.getName();
		}		
	}
	
	private class HumanPlayer implements CFPlayer{
	
		Scanner scan = new Scanner(System.in);
		
		public int nextMove(CFGame game) {		//gets next column choice of human player
			
			int[][] gameboard = game.getState();
			
			System.out.println("What column would you like to play: ");
			int column = scan.nextInt();
			
			while(column < 1 || column > game.getNumCols() || gameboard[column-1][game.getNumRows()-1] != 0 && !game.isGameOver()) {	//if illegal column chosen or full column chosen, gets new column from user 
				System.out.println("Illegal Move. What column would you like to play: ");
				column = scan.nextInt();
			}
			return column;	
		}
		
		public String getName() {
			return "Human Player";
		}
	}
	
	public void boardPrint() {		//prints board to user when called
		
		int x[][];
		
		for(int i = 5;i>=0;i--) {
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
}
