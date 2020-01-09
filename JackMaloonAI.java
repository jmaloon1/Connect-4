package hw4;
// make it so bad move is involved with touches
import java.util.*;

public class JackMaloonAI implements CFPlayer{
	
	private class moveFinder{		//inner class that doesn't actually play moves, but simulates moves to see if there is a winning one or one to prevent the opponent from winning
		
		CFGame g;
		private int [][] getState;
		private boolean winningMove;
		private boolean played;
		private int[] badMove;
		private int[] move_quality;
		private int adjacent_touches = 0;
		
		public moveFinder(CFGame g) {
			
			this.g = g;
			getState = g.getState();
			badMove = new int[g.getNumCols()];	
		}
		
		public void boardPrint(CFGame g) {		//prints board to user when called
			
			int x[][];
			
			for(int i = 5;i>=0;i--) {
				for(int j = 0;j<g.getNumCols();j++) {
					x = g.getState();
					if(x[j][i] == -1)
						System.out.print(" " + x[j][i]);
					else
						System.out.print( "  " + x[j][i]);
				}
				System.out.println("");
			}
			System.out.println("");	
		}

		public boolean pretendPlay(int column, boolean opp_turn) {				//simulates playing specific column. opp_turn true if opponents turn
			
			winningMove = false;   //true if a winning move is present
			  
			  if(column<0 || column>=g.getNumCols() || !g.notFullColumn(column)) 	//if move cannot be made
				  return false;
			  
			  for(int row=0; row<g.getNumRows(); row++) { 		//simulates move and what number will be played on move(1 or -1)
				  	played = false;			

					if(getState[column][row] == 0) {	
						  
						  if(g.isRedTurn() && !opp_turn) { 
							  getState[column][row] = 1;
							  played = true;
						  }
						  else if(!g.isRedTurn() && !played && !opp_turn) {
							  getState[column][row] = -1;
							  played = true;
						  }
	
						  else if(g.isRedTurn() && opp_turn && !played) { 
							  getState[column][row] = -1;
							  played = true;
						  }
						  else if(!g.isRedTurn() && !played && opp_turn && !played) {
							  getState[column][row] = 1;
							  played = true;
						  }
						  if(isMoveWinning() && ((row>0 && getState[column][row-1] != 0)||row==0))			//checks to see if there is a winning move
							  winningMove = true;
						  else if(isMoveWinning() && ((row==1 && getState[column][row-1] == 0) ||(row>1 && getState[column][row-1] == 0 && getState[column][row-2] !=0))) {  //checks to see whether a move 2 moves from now can win, and avoids it
							  badMove[column] = 1;
						  }
						  
						  adjacent_touches = numberTouching(column, row);
						  //System.out.println(column + " efef " + adjacent_touches);
							   
						  getState[column][row] = 0;
						  break;
					  } 
					  //boardPrint(g);

			  }	
			  
			  return true;
		  }
		
		public boolean isMoveWinning() {	//sees whether there is a winning move
			  
			for(int i=0; i<g.getNumCols(); i++) {
				  for(int j=0;j<g.getNumRows(); j++) {
					  if(getState[i][j] != 0 && (j+3<g.getNumRows() && getState[i][j] == getState[i][j+1] && getState[i][j] == getState[i][j+2] && getState[i][j] == getState[i][j+3] 
							  || i+3 < g.getNumCols() && getState[i][j] == getState[i+1][j] && getState[i][j] == getState[i+2][j] && getState[i][j] == getState[i+3][j]
							  || j+3<g.getNumRows() &&  i+3 < g.getNumCols() && getState[i][j] == getState[i+1][j+1] && getState[i][j] == getState[i+2][j+2] && getState[i][j] == getState[i+3][j+3]
							  ||  i+3 < g.getNumCols() && j-3 >= 0 && getState[i][j] == getState[i+1][j-1] && getState[i][j] == getState[i+2][j-2] && getState[i][j] == getState[i+3][j-3]))
						  
						  	  return true;
				  }  
			  }
			  return false;
		}
		
		public int numberTouching(int col, int row) {	//counts number of filled squares are adjacent to simulated move
			int num_touching = 0;
			
			if(row == 0) {
				if(col == 0) {
					if(getState[col+1][row+1] != 0) 
						num_touching++;
					
					if(getState[col+1][row] != 0) 
						num_touching++;
				}
				else if(col==g.getNumCols()-1){
					if(getState[col-1][row+1] != 0) 
						num_touching++;
					
					if(getState[col-1][row] != 0) 
						num_touching++;
				}	
				else {
					if(getState[col-1][row] != 0) 
						num_touching++;
					
					if(getState[col-1][row+1] != 0) 
						num_touching++;
					if(getState[col+1][row] != 0) 
						num_touching++;
					
					if(getState[col+1][row+1] != 0) 
						num_touching++;
				}
			}
			else if(row==g.getNumRows()-1) {
				num_touching++;    //if row>0, there will always be at least one filled square below simulated play
				
				if(col==0) {
					if(getState[col+1][row] != 0) 
						num_touching++;
					
					if(getState[col+1][row-1] != 0) 
						num_touching++;
				}
				else if(col==g.getNumCols()-1){
					if(getState[col-1][row] != 0) 
						num_touching++;
					
					if(getState[col-1][row-1] != 0) 
						num_touching++;
				}	
				else {
					if(getState[col-1][row] != 0) 
						num_touching++;
					
					if(getState[col-1][row-1] != 0) 
						num_touching++;
					
					if(getState[col+1][row] != 0) 
						num_touching++;
					
					if(getState[col+1][row-1] != 0) 
						num_touching++;
				}
			}
			else {
				num_touching++;    //if row>0, there will always be at least one filled square below simulated play
				
				if(col==0) {
					if(getState[col+1][row+1] != 0) 
						num_touching++;
					
					if(getState[col+1][row] != 0) 
						num_touching++;
					
					if(getState[col+1][row-1] != 0) 
						num_touching++;
				}
				else if(col==g.getNumCols()-1){
					if(getState[col-1][row+1] != 0) 
						num_touching++;
					
					if(getState[col-1][row] != 0) 
						num_touching++;
					
					if(getState[col-1][row-1] != 0) 
						num_touching++;
				}	
				else {
					if(getState[col-1][row+1] != 0) 
						num_touching++;
					
					if(getState[col-1][row] != 0) 
						num_touching++;
					
					if(getState[col-1][row-1] != 0) 
						num_touching++;
					
					if(getState[col+1][row+1] != 0) 
						num_touching++;
					
					if(getState[col+1][row] != 0) 
						num_touching++;
					
					if(getState[col+1][row-1] != 0) 
						num_touching++;
				}
			}
			//System.out.println(col + " touching " + num_touching);
			return num_touching;
		}
	}
	
	public int nextMove(CFGame g) {			//plays the next move with sound logic
		
		moveFinder m = new moveFinder(g);
		int[] touching_array = new int[g.getNumCols()];

		for(int col=0; col<g.getNumCols(); col++) {		
			m.badMove[col] = 0;
			touching_array[col] = 0;
		}

		for(int col=0; col<g.getNumCols(); col++) {   //plays winning move if possible
			if(m.pretendPlay(col, false)) {
				if(m.winningMove) 
					return col;	
			}		
		}
		
		for(int col=0; col<g.getNumCols(); col++) {  //blocks opponent winning move if possible
			if(m.pretendPlay(col, true)) {
				if(m.winningMove) 
					return col;	
				touching_array[col] = m.adjacent_touches;
				
			}
		}
		

		int most_touches = max_element_array(touching_array);
		return most_touches;
		
		/*
		java.util.Random r1 = new java.util.Random();
		java.util.Random r2 = new java.util.Random();
		
		int x = r1.nextInt(4);
		int y = r2.nextInt(4);
		
		for(int col=0; col<g.getNumCols(); col++) {
			System.out.print(m.badMove[col] + " ");  //displaying bad moves to console
			
		}
		
		int[][] gs = g.getState();
		int tries = 0;				//so the followjng while loop eventually terminates
		int max_tries = 100;		//max tries before going to random column
		
		while(m.badMove[x+y] != 0 && tries<max_tries || gs[x+y][g.getNumRows()-1] != 0) {
			System.out.println("x" + x);
			System.out.println("y" + y);
			x = r1.nextInt(4);
			y = r2.nextInt(4);
			tries++;
		}
			
			if(tries<max_tries) {
				//System.out.println(x+y+1);
				return x+y;
			}
			else
			{
				System.out.println("hiiii");
				RandomAI rand = new RandomAI();		//calls RandomAI if all else fails
				return(rand.nextMove(g));
			}*/
	}
	
	public int max_element_array(int[] arr) {
		
		int max = -1;
		int max_element = 0;
		ArrayList<Integer> duplicate_max = new ArrayList<>();
		
		for(int i=0; i<arr.length; i++) {  //0011100
			if(arr[i] > max) {
				max = arr[i];
				max_element = i;
				if(duplicate_max.size() > 0)
					duplicate_max.clear();
			}
			else if(arr[i] == max) {

				duplicate_max.add(i);
				if(duplicate_max.size() == 1) 
					duplicate_max.add(max_element);
			}
		}
		
		if(duplicate_max.size()>0) {
			Random rand = new Random();
			int r = rand.nextInt(duplicate_max.size());
			return duplicate_max.get(r);
		}
		
		return max_element;
	}

	public String getName() {
		
		return "Jack Maloon's AI";
	}
	
	
}