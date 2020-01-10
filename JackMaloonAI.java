package hw4;
// make it so bad move is involved with touches
import java.util.*;

public class JackMaloonAI implements CFPlayer{
	
	private class moveFinder{		//inner class that doesn't actually play moves, but simulates moves to see if there is a winning one or one to prevent the opponent from winning
		
		CFGame g;
		private int [][] getState;
		private boolean winningMove;
		private boolean played;
		ArrayList<Integer> losing_moves = new ArrayList<>();		//losing move is considered move that will allow opponent to win on following turn
		ArrayList<Integer> unwise_moves = new ArrayList<>();		//unwise move is considered move that will allow opponent to block AI's winning move on following turn
		ArrayList<Integer> illegal_moves = new ArrayList<>();    //move that is illegal
		ArrayList<Integer> row_stopper = new ArrayList<>();

		private int adjacent_touches = 0;
		
		public moveFinder(CFGame g) {
			
			this.g = g;
			getState = g.getState();
		}
		
		public void boardPrint(CFGame g, int col, int row, int num) {		//prints board to user when called

			int x[][];
			
			for(int i=5; i>=0; i--) {
				for(int j=0; j<g.getNumCols(); j++) {
					x = g.getState();
					if(x[j][i] == -1)
						System.out.print(" " + x[j][i]);
					else if(j==col && i==row && num==-1)
						System.out.print(" " + -1);
					else if(j==col && i==row && num==1)
						System.out.print("  " + 1);
					else
						System.out.print("  " + x[j][i]);
				}
				System.out.println("");
			}
			System.out.println("");	
		}

		public boolean pretendPlay(int column, boolean opp_turn) {				//simulates playing specific column. opp_turn true if opponents turn
			
			winningMove = false;   //true if a winning move is present
			  
			if(column<0 || column>=g.getNumCols() || !g.notFullColumn(column)) { 	//if move cannot be made
				if(!illegal_moves.contains(column))
					illegal_moves.add(column);
				return false;
			}
			  
			for(int row=g.getNumRows()-1; row>=0; row--) { 		//simulates move and what number will be played on move(1 or -1)
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
				    	
				    	if((getState[column][row]==1 && g.isRedTurn() || getState[column][row]==-1 && !g.isRedTurn())) {
							  //System.out.println("unwise " + column);
							  unwise_moves.add(column);
						}
						else if(((getState[column][row]==-1 && g.isRedTurn() || getState[column][row]==1 && !g.isRedTurn()))) {
						 //System.out.println("losing " + column);
	
							losing_moves.add(column);
						}
				    }
				  
					  adjacent_touches = numberTouching(column, row);
					  getState[column][row] = 0;
				  } 
		          //boardPrint(g, column, row, getState[column][row]);
			  }
			  	
			  row_stopper = isRowWinnable();
			  return true;
		  }
			  
			  
		public boolean sim_play(CFGame g, boolean opp_turn, int column, int row) {
			
			boolean played = false;
			
			if(g.isRedTurn() && !opp_turn) {
				g.play(column);
				//getState[column][row] = 1;
			    played = true;
		    }
		    else if(!g.isRedTurn() && !opp_turn && !played) {
		    	g.play(column);
			    //getState[column][row] = -1;
			    played = true;
		    }
		    else if(g.isRedTurn() && opp_turn && !played) { 
		      g.play(column);
			  //getState[column][row] = -1;
			  played = true;
		    }
		    else if(!g.isRedTurn() && opp_turn && !played) {
		      g.play(column);
			  //getState[column][row] = 1;
			  played = true;
		    }
			boardPrint(g, column, row, getState[column][row]);
	
			return played;
		}
		
		public boolean isMoveWinning() {	//sees whether there is a winning move
			  
			for(int i=0; i<g.getNumCols(); i++) {
				  for(int j=0;j<g.getNumRows(); j++) {
					  if(getState[i][j] != 0 && (j+3<g.getNumRows() && getState[i][j] == getState[i][j+1] && getState[i][j] == getState[i][j+2] && getState[i][j] == getState[i][j+3] 
						 || i+3 < g.getNumCols() && getState[i][j] == getState[i+1][j] && getState[i][j] == getState[i+2][j] && getState[i][j] == getState[i+3][j]
						 || j+3<g.getNumRows() &&  i+3 < g.getNumCols() && getState[i][j] == getState[i+1][j+1] && getState[i][j] == getState[i+2][j+2] && getState[i][j] == getState[i+3][j+3]
						 || i+3 < g.getNumCols() && j-3 >= 0 && getState[i][j] == getState[i+1][j-1] && getState[i][j] == getState[i+2][j-2] && getState[i][j] == getState[i+3][j-3])) {
						  
						  return true;
					  }
				  }  
			  }
			  return false;
		}
		
		public ArrayList<Integer> isRowWinnable() {	//sees whether a row be guaranteed 4 in a row in 2 turns
			  
			ArrayList<Integer> rw = new ArrayList<>();    //move that is illegal

			
			for(int i=0; i<g.getNumCols()-4; i++) {
				for(int j=0;j<g.getNumRows(); j++) {
					if(getState[i+2][j]!=0 && (j==0 && (getState[i][j]==0 && getState[i+1][j]==0 && getState[i+2][j]==getState[i+3][j] && getState[i+4][j]==0) 
						|| (j>0 && (getState[i][j-1]!=0 && getState[i+1][j-1]!=0 && getState[i+4][j-1]!=0 && getState[i][j]==0 && getState[i+1][j]==0 && getState[i+2][j]==getState[i+3][j] && getState[i+4][j]==0)))) {
						rw.add(i+1);
					}
					else if(getState[i+2][j]!=0 && ((j==0 && getState[i][j]==0 && getState[i+1][j]==getState[i+2][j] && getState[i+3][j]==0 && getState[i+4][j]==0)
						    || (j>0 && getState[i][j-1]!=0 && getState[i+3][j-1]!=0 && getState[i+4][j-1]!=0 && getState[i][j]==0 && getState[i+1][j]==getState[i+2][j] && getState[i+3][j]==0 && getState[i+4][j]==0))) {
					   
						rw.add(i+3);
					}
				}
			}  
			  
			  return rw;
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
		
		int most_touches = max_element_array(touching_array, m.illegal_moves, m.losing_moves, m.unwise_moves, m.row_stopper);
		return most_touches;
	}
	
	public int max_element_array(int[] arr, ArrayList<Integer> illegal_moves, ArrayList<Integer> losing_moves, ArrayList<Integer> unwise_moves, ArrayList<Integer> row_stopper) {
		int max = -10;
		int max_element = 0;
		//System.out.println(losing_moves);
		//System.out.println(unwise_moves);
		//System.out.println(illegal_moves);

		ArrayList<Integer> duplicate_max = new ArrayList<>();
		
		for(int index:illegal_moves) {		//Setting values in arr to arbitrary negative value if move is illegal
			arr[index] = -3;
		}
		for(int index:losing_moves) {		//Setting values in arr to negative value if move is losing
			arr[index] = -2;
		}
		for(int index:unwise_moves) {		//Setting values in arr to negative value if move is unwise. Still greater than losing since this move is better
			arr[index] = -1;
		}
		for(int index:row_stopper) {		//If a row can be "stopped", a lot of value is added to that column that "stops" it
			arr[index] += 6;;
		}
		
		for(int i=0; i<arr.length; i++) { 
			//System.out.print(arr[i]);
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
		//System.out.println("");
		
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