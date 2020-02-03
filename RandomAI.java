package hw4;
import java.util.Random;

/**
 * This class is an AI that plays legal columns at random.
 * It implements the two methods from the CFPlayer interface
*/
public class RandomAI implements CFPlayer{

	/**
     * This method returns a legal move (column) to be played.
     * @param g: CFGame instance. CFGame is class with game logic and one instance of CFGame represents one game
     * @return col: column to be played
    */
	public int nextMove(CFGame g) {
		
		int[][] gameState = g.getState();
		Random rand = new Random();
		int col = rand.nextInt(g.getNumCols());
		
		while(gameState[col][g.getNumRows()-1]!=0 && !g.isGameOver()) {		//checks to see whether the move will be playable
			
			col = rand.nextInt(g.getNumCols());		//re-randomizes variable until it makes a legal move
		}	
		return col; 
	}
	
	/**
     * This method returns name of this AI.
     * @param none
     * @return String: name of AI
    */
	public String getName() {
		
		return "Random Player";
	}
}
