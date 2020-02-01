
import java.util.Random;

public class RandomAI implements CFPlayer{
	//fix this to make it only play legal moves
	
		public int nextMove(CFGame g) {
		
		int[][] gameState = g.getState();
		Random rand = new Random();
		int c = rand.nextInt(7);
		
		while(gameState[c][g.getNumRows()-1] != 0 && !g.isGameOver()) {		//checks to see whether the move will be playable
			
			c = rand.nextInt(7);		//re-randomizes variable until it makes a legal move
		}	
		return c; 
	}
	
	public String getName() {
		
		return "Random Player";
	}
}