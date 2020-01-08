package hw4;

public class JackMaloonAI implements CFPlayer{
	
	private class moveFinder{		//inner class that doesn't actually play moves, but simulates moves to see if there is a winning one or one to prevent the opponent from winning
		
		CFGame g;
		private int [][] getState;
		private boolean goodMove;
		private boolean played;
		private int[] badMove;
		
		public moveFinder(CFGame g) {
			
			this.g = g;
			getState = g.getState();
			badMove = new int[g.getNumCols()];			
		}

		public boolean pretendPlay(int column, boolean opp) {				//simulates playing specific column. opp is used to either simulate own move or opponent move
			 
			  goodMove = false;
			  
			  if(column < 1 || column > g.getNumCols() || !g.notFullColumn(column)) 	//if move cannot be made
				  return false;
			  
			  for(int i = g.getNumRows()-1; i>=0; i--) { 		//simulates move and what number will be played on move(1 or -1)
				  	played = false;			
					if(getState[column-1][i] == 0) {	
						  
						  if(g.isRedTurn() && !opp) { 
							  getState[column-1][i] = 1;
							  played = true;
						  }
						  else if(!g.isRedTurn() && !played && !opp) {
							  getState[column-1][i] = -1;
							  played = true;
						  }
	
						  else if(g.isRedTurn() && opp && !played) { 
							  getState[column-1][i] = -1;
							  played = true;
						  }
						  else if(!g.isRedTurn() && !played && opp && !played) {
							  getState[column-1][i] = 1;
							  played = true;
						  }
						  if(isMoveWinning() && ((i>0 && getState[column-1][i-1] != 0)||i==0))			//checks to see if there is a winning move
							  goodMove = true;
						  else if(isMoveWinning() && ((i==1 && getState[column-1][i-1] == 0) ||(i>1 && getState[column-1][i-1] == 0 && getState[column-1][i-2] !=0))) {  //checks to see whether a move 2 moves from now can win, and avoids it
							  badMove[column-1] = 1;
						  }
							   
						  getState[column-1][i] = 0;
						
					  }  

			  }		  
			  return true;
		  }
		
		public boolean isMoveWinning() {	//sees whether there is a winning move
			  
			for(int i=0;i<g.getNumCols();i++) {
				  for(int j = 0;j<g.getNumRows();j++) {
					  if(getState[i][j] != 0 && (j+3<g.getNumRows() && getState[i][j] == getState[i][j+1] && getState[i][j] == getState[i][j+2] && getState[i][j] == getState[i][j+3] 
							  || i+3 < g.getNumCols() && getState[i][j] == getState[i+1][j] && getState[i][j] == getState[i+2][j] && getState[i][j] == getState[i+3][j]
							  || j+3<g.getNumRows() &&  i+3 < g.getNumCols() && getState[i][j] == getState[i+1][j+1] && getState[i][j] == getState[i+2][j+2] && getState[i][j] == getState[i+3][j+3]
							  ||  i+3 < g.getNumCols() && j-3 >= 0 && getState[i][j] == getState[i+1][j-1] && getState[i][j] == getState[i+2][j-2] && getState[i][j] == getState[i+3][j-3]))
						  
						  	  return true;
				  }  
			  }
			  return false;
		}
	}
	
	public int nextMove(CFGame g) {			//plays the next move with sound logic
		
		moveFinder m = new moveFinder(g);

		for(int i = 0; i < g.getNumCols(); i++) {		
			m.badMove[i] = 0;
		}
		

		for(int i = 0; i < g.getNumCols(); i++) {

			if(m.pretendPlay(i+1, false)) {
				if(m.goodMove)
					return i+1;					
			}		
		}
		for(int i = 0; i < g.getNumCols(); i++) {
			
			if(m.pretendPlay(i+1, true)) {
				if(m.goodMove) 
					return i+1;
			}
		}
		
		java.util.Random r1 = new java.util.Random();
		java.util.Random r2 = new java.util.Random();
		
		int x = r1.nextInt(4);
		int y = r2.nextInt(4);
		
		for(int i = 0; i<7;i++) {
			System.out.print(m.badMove[i] + " ");
			
		}
		System.out.println("");
		int[][] gs = g.getState();
		int tries = 0;							//so the followjng while loop eventually terminates
		if(gs[x+y][g.getNumRows()-1] == 0) {		//plays towards the middle of the board
			while(m.badMove[x+y] != 0 && tries<100) {
				x = r1.nextInt(4);
				y = r2.nextInt(4);
				tries++;
			}
			System.out.println(tries);
			return x+y+1;
		}
		
		else {
			RandomAI rand = new RandomAI();		//calls RandomAI if all else fails
			return(rand.nextMove(g));
		}

	}

	public String getName() {
		
		return "Jack Maloon's AI";
	}
}