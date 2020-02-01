
import java.util.*;

public class MediumAI implements CFPlayer{
	
	private class moveFinder{		//inner class that doesn't actually play moves, but simulates moves to see if there is a winning one or one to prevent the opponent from winning
		
		CFGame g;
		private int [][] getState;
		private boolean winningMove;
		private boolean played;
		ArrayList<Integer> losing_moves = new ArrayList<>();		//losing move is considered move that will allow opponent to win on following turn
		ArrayList<Integer> unwise_moves = new ArrayList<>();		//unwise move is considered move that will allow opponent to block AI's winning move on following turn
		ArrayList<Integer> illegal_moves = new ArrayList<>();    //move that is illegal
		ArrayList<Integer> AI_three_consecutive = new ArrayList<>();
		ArrayList<Integer> opposing_three_consecutive = new ArrayList<>();
		ArrayList<Integer> row_stopper = new ArrayList<>();
		int[][] aaa = new int[7][6];

		private int adjacent_touches = 0;
		
		public moveFinder(CFGame g) {
			
			this.g = g;
			getState = g.getState();
			for(int i=0; i<g.getNumCols(); i++) {
				  for(int j=0;j<g.getNumRows(); j++) {
					  aaa[i][j] = 0;
				  }
				  
			}
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
				    	
				    	if((getState[column][row]==1 && g.isRedTurn() || getState[column][row]==-1 && !g.isRedTurn())) 
							  unwise_moves.add(column);
						else if(((getState[column][row]==-1 && g.isRedTurn() || getState[column][row]==1 && !g.isRedTurn()))) 
							losing_moves.add(column);						
				    }
				    
				    if(!threeInARow(column, row).isEmpty() && ((row>0 && getState[column][row-1] != 0)||row==0)) {
				    	if((getState[column][row]==1 && g.isRedTurn() || getState[column][row]==-1 && !g.isRedTurn())) { 
				    		AI_three_consecutive.addAll(threeInARow(column, row));
				    	}
				    
				    	else if(((getState[column][row]==-1 && g.isRedTurn() || getState[column][row]==1 && !g.isRedTurn()))) { 
				    		opposing_three_consecutive.addAll(threeInARow(column, row));
				    	}
				    }
				    if(threeSquares(column, row, aaa)[2] != 0)
				    	aaa[threeSquares(column, row, aaa)[0]][threeSquares(column, row, aaa)[1]] = threeSquares(column, row, aaa)[2];
				    
				  
					adjacent_touches = numberTouching(column, row);
					getState[column][row] = 0;
				} 
			}
			  	
			  row_stopper = isRowWinnable();
			  return true;
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
			  
			ArrayList<Integer> move = new ArrayList<>();    //arraylist thats hold columns that should be played

			
			for(int i=0; i<g.getNumCols()-4; i++) {
				for(int j=0;j<g.getNumRows(); j++) {
					if(getState[i+2][j]!=0 && (j==0 && (getState[i][j]==0 && getState[i+1][j]==0 && getState[i+2][j]==getState[i+3][j] && getState[i+4][j]==0) 
						|| (j>0 && (getState[i][j-1]!=0 && getState[i+1][j-1]!=0 && getState[i+4][j-1]!=0 && getState[i][j]==0 && getState[i+1][j]==0 && getState[i+2][j]==getState[i+3][j] && getState[i+4][j]==0)))) {
						move.add(i+1);
					}
					else if(getState[i+2][j]!=0 && ((j==0 && getState[i][j]==0 && getState[i+1][j]==getState[i+2][j] && getState[i+3][j]==0 && getState[i+4][j]==0)
						    || (j>0 && getState[i][j-1]!=0 && getState[i+3][j-1]!=0 && getState[i+4][j-1]!=0 && getState[i][j]==0 && getState[i+1][j]==getState[i+2][j] && getState[i+3][j]==0 && getState[i+4][j]==0))) {
					   
						move.add(i+3);
					}
					
					
					
				}
				
			}  
			  
			  return move;
		}
		
		public ArrayList<Integer> threeInARow(int col, int row) {	      //sees whether there is an opportunity to make three in a row or block opponent from doing so
			  
			ArrayList<Integer> three_connected = new ArrayList<>();    ////arraylist thats hold columns that make three in a row
			
			
			for(int i=0; i<g.getNumCols(); i++) {
				for(int j=0;j<g.getNumRows(); j++) {
					if(getState[i][j] != 0 && j<g.getNumRows()-3 && i == col && (j+2 == row) && getState[i][j] == getState[i][j+1] && getState[i][j] == getState[i][j+2] && getState[i][j+3] == 0) {
						three_connected.add(col);
					}
					if(getState[i][j] != 0 && i < g.getNumCols()-2 && (i == col || i+1 == col || i+2 == col) && j == row && getState[i][j] == getState[i+1][j] && getState[i][j] == getState[i+2][j] && (i < g.getNumCols()-3 && getState[i+3][j] == 0 || i>0 && getState[i-1][j] == 0)) {
						if(j>0 && i>0 && i<g.getNumCols()-3 && (getState[i-1][j-1]==0 || getState[i+3][j-1]==0)) {
							three_connected.add(col);
						    three_connected.add(col);
						}
				    }
				    if(getState[i][j] != 0 && i < g.getNumCols()-2 && j < g.getNumRows()-2 && (i == col && j == row || i+1 == col && j+1 == row || i+2 == col && j+2 == row) && getState[i][j] == getState[i+1][j+1] && getState[i][j] == getState[i+2][j+2] && (j < g.getNumRows()-3 &&  i < g.getNumCols()-3 && getState[i+3][j+3] == 0 || j>1 && i>1 && getState[i-1][j-1] == 0)) {
				    	if((i<g.getNumCols()-3 && j<g.getNumRows()-3 && getState[i+3][j+2]==0) || (j==2 && i<g.getNumCols()-3 && ((i>0 && getState[i-1][j-2]==0) || getState[i+3][j+2]==0)) || (i==g.getNumCols()-3 && j>1 && getState[i-1][j-2]==0) || (j==g.getNumRows()-3 && getState[i-1][j-2]==0)) {
				    		three_connected.add(col);
				    		three_connected.add(col);
				    		three_connected.add(col);
				    		}
				    }
				    if(getState[i][j] != 0 && i < g.getNumCols()-2 && j >= 2 && (i == col && j == row || i+1 == col && j-1 == row || i+2 == col && j-2 == row) &&getState[i][j] == getState[i+1][j-1] && getState[i][j] == getState[i+2][j-2] && (i > 1 && j < g.getNumRows()-1 && getState[i-1][j+1] == 0 || i < g.getNumCols()-3 && j>2 && getState[i+3][j-3] == 0)) {
				    	if(((j==3 || j==2) && i>0 && getState[i-1][j]==0) || (i>0 && i<g.getNumCols()-3 && j==4 && (getState[i-1][j]==0 || getState[i+3][j-4]==0)) || (i==0 && j>3 && getState[i+3][j-4]==0) || (j==g.getNumCols()-1 && getState[i+3][j-4]==0)) {
				    		three_connected.add(col);
				    		three_connected.add(col);
				    		three_connected.add(col);
				    	}
				    }
			    }  
			}
			  return three_connected;
		}
		
		public int[] threeSquares(int col, int row, int[][] aaa) {	      //sees whether there is an opportunity to make three in a row or block opponent from doing so
			  
			int[] a = new int[3];
			a[0]=0;
			a[1]=0;
			a[2]=0;
			
			
			
			for(int i=0; i<g.getNumCols(); i++) {
				for(int j=0;j<g.getNumRows(); j++) {
					if(getState[i][j] != 0 && j < g.getNumRows()-3 && i == col && (j+2 == row) && getState[i][j] == getState[i][j+1] && getState[i][j] == getState[i][j+2] && getState[i][j+3] == 0) {
						a[0] = col;
						a[1] = row;
						if(aaa[col][row] != 0)
							a[2] = 2;
						else
							a[2]= getState[col][row];
					}
					if(getState[i][j] != 0 && i < g.getNumCols()-2 && (i == col || i+1 == col || i+2 == col) && j == row && getState[i][j] == getState[i+1][j] && getState[i][j] == getState[i+2][j] && (i < g.getNumCols()-3 && getState[i+3][j] == 0 || i>0 && getState[i-1][j] == 0)) {
						a[0] = col;
						a[1] = row;
						if(aaa[col][row] != 0)
							a[2] = 2;
						else
							a[2]= getState[col][row];
				    }
				    if(getState[i][j] != 0 && i < g.getNumCols()-2 && j < g.getNumRows()-2 && (i == col && j == row || i+1 == col && j+1 == row || i+2 == col && j+2 == row) && getState[i][j] == getState[i+1][j+1] && getState[i][j] == getState[i+2][j+2] && (j < g.getNumRows()-3 &&  i < g.getNumCols()-3 && getState[i+3][j+3] == 0 || j>1 && i>1 && getState[i-1][j-1] == 0)) {
				    	a[0] = col;
						a[1] = row;
						if(aaa[col][row] != 0)
							a[2] = 2;
						else
							a[2]= getState[col][row];
				    }
				    if(getState[i][j] != 0 && i < g.getNumCols()-2 && j >= 2 && (i == col && j == row || i+1 == col && j-1 == row || i+2 == col && j-2 == row) && getState[i][j] == getState[i+1][j-1] && getState[i][j] == getState[i+2][j-2] && (i > 1 && j < g.getNumRows()-1 && getState[i-1][j+1] == 0 || i < g.getNumCols()-3 && j>2 && getState[i+3][j-3] == 0)) {
				    	a[0] = col;
						a[1] = row;
						if(aaa[col][row] != 0)
							a[2] = 2;
						else
							a[2]= getState[col][row];

				    }
			    }  
			}
			
			  return a;
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
			return num_touching;
		}
	}
	
	public int nextMove(CFGame g) {			//plays the next move with sound logic
		
		moveFinder m = new moveFinder(g);
		boolean empty_board = true;
		
		for(int col=0; col<g.getNumCols(); col++) {		//checks to see if board is empty
			for(int row=0; row<g.getNumRows(); row++) {
				if(m.getState[col][row]!= 0) 
					empty_board = false;
			}
		}
		
		if(empty_board) { 		//if board is empty, middle square is played
			return(g.getNumCols()/2);
		}
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
		
		
		int best_move = max_element_array(touching_array, m.illegal_moves, m.losing_moves, m.unwise_moves, m.row_stopper, m.AI_three_consecutive, m.opposing_three_consecutive, m.aaa);
		
		RandomAI rand = new RandomAI();		//calls RandomAI if all else fails
		return(rand.nextMove(g));
	}
	
	public int max_element_array(int[] arr, ArrayList<Integer> illegal_moves, ArrayList<Integer> losing_moves, ArrayList<Integer> unwise_moves, ArrayList<Integer> row_stopper, ArrayList<Integer> AI_three_consecutive, ArrayList<Integer> opposing_three_consecutive, int[][] aaa) {
		int max = -1000000;
		int max_element = 0;
		int illegal = -1000;
		int losing = -80;
		int unwise = -60;
		int row_stop = 30;
		int three_grouped = 4;
		int opponent_three = 3;
			


		ArrayList<Integer> duplicate_max = new ArrayList<>();
		
		for(int index:illegal_moves) {		//Setting values in arr to arbitrary negative value if move is illegal
			arr[index] = illegal;
		}
		for(int index:losing_moves) {		//Setting values in arr to negative value if move is losing
			arr[index] = losing;
		}
		for(int index:unwise_moves) {		//Setting values in arr to negative value if move is unwise. Still greater than losing since this move is better
			arr[index] = unwise;
		}
		for(int index:row_stopper) {		//If a row can be "stopped", a lot of value is added to that column that "stops" it
			arr[index] += row_stop;
		}
		for(int index:AI_three_consecutive) {		//Adding value to making three in a row
			arr[index] += three_grouped;
		}
		for(int index:opposing_three_consecutive) {		//Adding value to blocking three in a row
			arr[index] += opponent_three;
		}
		
		for(int i=0; i<arr.length; i++) { 
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
		
		return "Medium AI";
	}

}
