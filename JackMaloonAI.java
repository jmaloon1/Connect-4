package connect4;

import java.util.*;

/**
 * This class is an AI that utilizes many factors in order to generate a column that will help it win.
*/
public class JackMaloonAI implements CFPlayer{
	
	/**
	 * This inner class holds most of the logic behind the AI. Different moves are simulated and different criteria
	 * are tested to determine good and bad columns to play
	*/
	private class moveFinder{		
		
		CFGame g;																	//instance of CFGame class. Used so logic behind game is updated
		private int [][] getState;													//current board
		private int AI_color = -1;													//color of AI (1 for red, -1 for black), will change depending on who goes first			
		private int opp_color = 1;													//color of opponent (1 for red, -1 for black), will change depending on who goes first	
		private ArrayList<Integer> losing_moves = new ArrayList<>();				//ArrayList of moves that will allow opponent to win on following turn
		private ArrayList<Integer> unwise_moves = new ArrayList<>();				//ArrayList of moves that will allow opponent to block Ai's potential winning move on following turn
		private ArrayList<Integer> sacrifice_moves = new ArrayList<>();				//ArrayList of moves that will allow sacrifice one potential four in a row for a better setup
		private ArrayList<Integer> bad_setup = new ArrayList<>();					//ArrayList of moves that will allow opponent to block Ai's potential winning setup
		private ArrayList<Integer> illegal_moves = new ArrayList<>();      			//ArrayList of moves that are illegal (column is full)
		private ArrayList<Integer> best_AI_unblockable = new ArrayList<>();			//ArrayList of moves that will give AI three in a row that can't be immediately blocked
		private ArrayList<Integer> best_opp_unblockable = new ArrayList<>();		//ArrayList of moves that will give opponent three in a row that can't be immediately blocked
		private ArrayList<Integer> ok_AI_unblockable = new ArrayList<>();			//ArrayList of moves that will give AI three in a row that can't be immediately blocked
		private ArrayList<Integer> ok_opp_unblockable = new ArrayList<>();			//ArrayList of moves that will give opponent three in a row that can't be immediately blocked
		private ArrayList<Integer> AI_blockable = new ArrayList<>();				//ArrayList of moves that will give AI three in a row that can be immediately blocked
		private ArrayList<Integer> opp_blockable = new ArrayList<>();				//ArrayList of moves that will give opponent three in a row that can't be immediately blocked
		private ArrayList<Integer> loss_avoider = new ArrayList<>();				//ArrayList of moves that block opponent from developing a winning game board
		private ArrayList<Integer> loss_creator = new ArrayList<>();				//ArrayList of moves that allow opponent from developing a winning game board
		private ArrayList<Integer> three_preventer = new ArrayList<>();				//ArrayList of moves allow opponent to make three in a row or stop AI from doing so
		private ArrayList<Integer> AI_winning_column= new ArrayList<>();			//ArrayList of moves in a column that allows for AI to win
		private ArrayList<Integer> opp_winning_column= new ArrayList<>();			//ArrayList of moves in a column that allows for opp to win
		private ArrayList<Integer> bad_position = new ArrayList<>();				//ArrayList of moves block opponent from three in a row unblockable in two directions
		private ArrayList<Integer> winning_index = new ArrayList<>();				//ArrayList of columns thought to be winning columns
		private ArrayList<Integer> not_winning_column = new ArrayList<>();			//ArrayList of columns thought to be winning columns
		private HashMap<Integer, Integer> AI_winnable_columns = new HashMap<>();   	//HashMap of columns that are already considered winning
		private HashMap<Integer, Integer> opp_winnable_columns = new HashMap<>();   //HashMap of columns that are already considered winning
		private int[][] three_map;													//map of where three in a row is possible at some point in game		
		private int[][] initial_four_map;											//initial map of where four in a row is possible at some point in game, 	
		private int[][] four_map;													//map of where four in a row is possible at some point in game			
		private boolean already_winning_column = false;								//true if a winning column already exists
		private int num_touching;													//integer representing number of squares each legal move will touch that have already been played
		
		
		/**
	     * This constructor initializes numerous arrays that will be used to map the board and track criteria
	     * @param g: instance of CFGame that holds logic for current game being played
	    */
		public moveFinder(CFGame g) {
			
			this.g = g;
			getState = g.getState();
			three_map = new int[g.getNumCols()][g.getNumRows()];
			initial_four_map = new int[g.getNumCols()][g.getNumRows()];
			four_map = new int[g.getNumCols()][g.getNumRows()];		
			
			if(g.isRedTurn()) {
				opp_color = -1;
				AI_color = 1;
			}  
			
			for(int i=0; i<g.getNumCols(); i++) {
				  for(int j=0;j<g.getNumRows(); j++) {
					  three_map[i][j] = 0;
					  initial_four_map[i][j] = 0;
					  four_map[i][j] = 0;
				  } 
			}
		}
		
		/**
	     * This method simulates a game move and tests different criteria based on simulate move
	     * @param column: column of move to be simulated
	     * @param opp_turn: boolean, true if simulating Ai's opponent's turn, false otherwise
	     * @return boolean: true if move can be played, false otherwise
	    */
		public boolean pretendPlay(int column, boolean opp_turn) {				//simulates playing specific column. opp_turn true if opponents turn
			
			if(column<0 || column>=g.getNumCols() || g.fullColumn(column)) { 	//if move cannot be made
				if(!illegal_moves.contains(column))
					illegal_moves.add(column);
				return false;
			}
			
			initial_four_map = fourMap();
			
			for(int row=g.getNumRows()-1; row>=0; row--) { 		//simulates move and what number will be played on move(1 or -1)

				if(getState[column][row] == 0) {	
						  
				    if((g.isRedTurn() && !opp_turn) || (!g.isRedTurn() && opp_turn))   //simulating a red move
					    getState[column][row] = 1;
				    else if((!g.isRedTurn() && !opp_turn) || (g.isRedTurn() && opp_turn))   //simulating a black move
					    getState[column][row] = -1;
				    
				    if(!opp_turn)
				    	threeInARow(column, row, opp_turn, best_AI_unblockable, ok_AI_unblockable, AI_blockable);		//checks to see if three out of four can be created for AI
				    else
				    	threeInARow(column, row, opp_turn, best_opp_unblockable, ok_opp_unblockable, opp_blockable);	//checks to see if three out of four can be created for opponent 
				    	
				    if(row==0 || (row>0 && getState[column][row-1]!=0)) {   //checking conditions on simulated moves that can actually be played
				    	
				    	if(already_winning_column && opp_turn)      //checking to see if a column that is deemed winning will still be so after opponent goes
				    		destroyWinningColumn(column, opp_turn);
				    	
				    	if(!already_winning_column) 		//if no winning columns exists from previous turns, looking to find one
				    		winningColumnCreator(column, row, opp_turn);
				    	
				    	if(!opp_turn) 			//checking to see if AI's move will cause opponent to be able to win
					    	losingBoardCreator(column, row);	
				    	
					}
					getState[column][row] = 0;    //resetting the simulated move back to 0
				} 
			}  
			  
			if(!AI_winnable_columns.isEmpty())		//checking to see if AI's winning columns are truly winning
    			testWinningColumn(column, true);
			if(!opp_winnable_columns.isEmpty())		//checking to see if opponent's winning columns are truly winning
    			testWinningColumn(column, false);	
			
			avoidLoss();   			//sees is a row can be won horizontally in two moves
			avoidAllowingThree();  //checks to see is a certain move would allow three out of four to be created

			return true;
		}
		
		/**
	     * This method looks for moves that can win the game for either player
	     * @param none
	     * @return int: if int = -1, no direct winning moves found, otherwise, int returned is column to be played
	    */
		public int findWinningColumn() {
			
			four_map = fourMap();
			ArrayList<Integer> losing_columns = new ArrayList<>();			//holds columns that would cause loss
			
			for(int i=0; i<g.getNumCols(); i++) {    //finds winning moves, moves to avoid losing, and moves that give direct advantage to opponent
				
				boolean opponent_can_win = false;
				
				for(int j=0;j<g.getNumRows(); j++) {
					
					if((four_map[i][j]==AI_color || four_map[i][j]==2) && (j==0 || getState[i][j-1]!=0)) {   //finding a winning move
						return(i);
					}
					else if(four_map[i][j]==opp_color && (j==0 || getState[i][j-1]!=0)) {		//finding opponents winning moves
						
						if(!losing_columns.contains(i))
							losing_columns.add(i);
					}
					
					if((j==1 && getState[i][j-1]==0) ||(j>1 && getState[i][j-1]==0 && getState[i][j-2]!=0)) {  //checks to see whether a move 2 moves from now can win, and avoids it
						
				    	if((four_map[i][j]==1 && g.isRedTurn() || four_map[i][j]==-1 && !g.isRedTurn())) {   //finding moves that would allow opponent to block AI's four in a row
				    		
				    		if(j%2==1 && !g.isRedTurn() || j%2==0 && g.isRedTurn()) {		//finding whether AI's potential winning move is in good row
				    			unwise_moves.add(i);
				    		}
				    		else {		//if potential winning column is in bad row, seeing whether it can be sacrificed for better winning move
				    			
				    			if(j<g.getNumRows()-1) {
				    				
				    				ArrayList<Integer> best_unblockable = new ArrayList<>();		//temporary arrayList needed for threeInARow function
				    				ArrayList<Integer> ok_unblockable = new ArrayList<>();			//temporary arrayList needed for threeInARow function
				    				ArrayList<Integer> blockable = new ArrayList<>();				//temporary arrayList needed for threeInARow function
				    				
				    				getState[i][j-1] = AI_color;		
				    				getState[i][j] = opp_color;
				    				
				    				threeInARow(i, j+1, false, best_unblockable, ok_unblockable, blockable);
				    				if(!best_unblockable.isEmpty()) {
				    					sacrifice_moves.add(i);
				    					throw new ArithmeticException();
				    				}
				    				getState[i][j-1] = 0;
				    				getState[i][j] = 0;
				    				
				    			}
				    		}
				    	}
						else if(four_map[i][j]==2 || (four_map[i][j]==-1 && g.isRedTurn() || four_map[i][j]==1 && !g.isRedTurn())) 
							losing_moves.add(i);			//moves that will cause AI loss			
					}
					
					
					if(j<g.getNumRows()-1 && four_map[i][j]==AI_color && (four_map[i][j+1]==AI_color || four_map[i][j+1]==2)) {
						//looking for columns that have two stacked winning moves
						System.out.println("already winning column " + i);	
						int empty_squares = 0;
						for(int num=0; num<j; num++) {

							if(four_map[i][num]==opp_color || four_map[i][num]==2) {
								System.out.println("opp can win " + i);
								opponent_can_win = true;
							}
							
							if(getState[i][num]==0)
								empty_squares++;
						}
							
						if(!opponent_can_win) {
							already_winning_column = true;
							if(!AI_winnable_columns.containsKey(i)) {
								AI_winnable_columns.put(i, empty_squares);
							}
						}
					}
				}
			}
			
			if(!losing_columns.isEmpty()) {  //return random losing column if they exists

				Random rand = new Random();
				int index = rand.nextInt(losing_columns.size());
				return(losing_columns.get(index));
			}

			return(-1);   //if no winning moves or opponent winning moves are found, -1 returned to indicate to try out other criteria
		}
		
		/**
	     * This method maps the game as a 2D array. Will store 1 or -1 in a given position is that spot would create a 4 
	     * in a row for one player, wil hold 2 if both players can win in given spot, 0 otherwise.
	     * @param none
	     * @return temp_array: 2D array with same dimensions a game board with potential 4 in a row spots numbered with -1, 1, or 2
	    */
		public int[][] fourMap() {
			
			int[] move_types = {-1,1};
			int[][] temp_array = new int[g.getNumCols()][g.getNumRows()];
			
			for(int i=0; i<g.getNumCols(); i++) {
				  for(int j=0;j<g.getNumRows(); j++) {
					  temp_array[i][j] = 0;
				  }
			}
			
			for(int i=0; i<g.getNumCols(); i++) {
				for(int j=0;j<g.getNumRows(); j++) {
					for(int move:move_types) {
						if(getState[i][j]==0) {
			
							getState[i][j]=move;
							
							for(int col=0; col<g.getNumCols(); col++) {
								for(int row=0; row<g.getNumRows(); row++) {	
									if(getState[col][row]!=0 && ((row<g.getNumRows()-3 && getState[col][row]==getState[col][row+1] && getState[col][row]==getState[col][row+2] 
									   && getState[col][row]==getState[col][row+3]) || (col<g.getNumCols()-3 && getState[col][row]==getState[col+1][row] 
									   && getState[col][row]==getState[col+2][row] && getState[col][row]==getState[col+3][row]) || (row<g.getNumRows()-3 
									   && col<g.getNumCols()-3 && getState[col][row]==getState[col+1][row+1] && getState[col][row]==getState[col+2][row+2] 
									   && getState[col][row]==getState[col+3][row+3]) || (row>=3 && col<g.getNumCols()-3 && getState[col][row]==getState[col+1][row-1] 
									   && getState[col][row]==getState[col+2][row-2] && getState[col][row]==getState[col+3][row-3]))) {

										  if(temp_array[i][j]==move)
											  continue;
										  else if(temp_array[i][j]==0)
									    	  temp_array[i][j] = move;
										  else if(temp_array[i][j]!=move) { 
											  temp_array[i][j] = 2;
										  }
									}
								}
							}
							getState[i][j] = 0;
						  }
					  }
				  }  
			  }
			  return temp_array;
		}
		
		
		/**
	     * This method finds move that create three out of four in a given row, column, or diagonal
	     * @param column: column of move to be simulated
	     * @param column: row of simulated move
	     * @param opp_turn: boolean, true if simulating Ai's opponent's turn, false otherwise
	     * @param best_unblockable _arr: ArrayList of moves that will create a good 4 in a row chance
	     * @param ok_unblockable _arr: ArrayList of moves that will create an okay 4 in a row chance
	     * @param blockable _arr: ArrayList of moves that will create blockable 4 in a row opportunities
	     * @return void
	    */
		public void threeInARow(int column, int row, boolean opp_turn, ArrayList<Integer> best_unblockable_arr, ArrayList<Integer> ok_unblockable_arr, ArrayList<Integer> blockable_arr) {
			  
			HashMap<Integer, Integer> three_unblockable = new HashMap<>();    	//HashMap thats hold columns that make three in a row that aren't blockable and rows of winning move
			ArrayList<Integer> three_blockable = new ArrayList<>();    			//ArrayList thats hold columns that make three in a row that are blockable
			
			for(int i=0; i<g.getNumCols(); i++) {
				for(int j=0;j<g.getNumRows(); j++) {
					if(getState[i][j]!=0 && j<g.getNumRows()-3 && i==column && (j+2==row) && getState[i][j]==getState[i][j+1]    
					   && getState[i][j]==getState[i][j+2] && getState[i][j+3]==0) {
						//looks for three vertically
						
						three_blockable.add(column);
						
						if(three_map[column][row]==0)
							three_map[column][row] = getState[i][j];
						else if(three_map[column][row]!=getState[i][j])
							three_map[column][row] = 2;
					}
					
					if(getState[i][j]!=0 && i<g.getNumCols()-2 && (i==column || i+1==column || i+2==column) && j==row && getState[i][j]==getState[i+1][j]   
					   && getState[i][j]==getState[i+2][j] && (i<g.getNumCols()-3 && getState[i+3][j]==0 || i>0 && getState[i-1][j]==0)) {
						//looks for three connected horizontally
						
						if(j>0 && i>0 && i<g.getNumCols()-3 && (getState[i-1][j-1]==0 || getState[i+3][j-1]==0)) {	
							
							if(getState[i-1][j-1]==0)
								three_unblockable.put(column, j);
							if(getState[i+3][j-1]==0) 		
								three_unblockable.put(column, j);
							
							if(getState[i][j]==1 && j%2==0 || getState[i][j]==-1 && j%2==1) 
				    			three_unblockable.put(column, j);
						}
						else 
							three_blockable.add(column);
						
						if(three_map[column][row]==0)
							three_map[column][row] = getState[i][j];
						else if(three_map[column][row]!=getState[i][j])
							three_map[column][row] = 2;
				    }
					
					if(getState[i][j]!=0 && i<g.getNumCols()-3 && initial_four_map[i+1][j]!=getState[i][j] && initial_four_map[i+1][j]!=2 
					   && (i==column || i+2==column || i+3==column) &&  j==row && getState[i][j]==getState[i+2][j]   
					   && getState[i][j]==getState[i+3][j] && getState[i+1][j]==0) {		
						//looks for three out of four horizontally with middle left empty
						
						if(j>0 && getState[i+1][j-1]==0) {
							three_unblockable.put(column, j);
							
							if(getState[i][j]==1 && j%2==0 || getState[i][j]==-1 && j%2==1) 
								three_unblockable.put(column, j);
						}
						else 
							three_blockable.add(column);	

						if(three_map[column][row]==0)
							three_map[column][row] = getState[i][j];
						else if(three_map[column][row]!=getState[i][j])
							three_map[column][row] = 2;
					}
					
					if(getState[i][j]!=0 && i<g.getNumCols()-3 && initial_four_map[i+2][j]!=getState[i][j] && initial_four_map[i+2][j]!=2 
					   && (i==column || i+1==column || i+3==column) && j==row && getState[i][j]==getState[i+1][j] 
					   && getState[i][j]==getState[i+3][j] && getState[i+2][j]==0) {    
						//looks for three out of four horizontally with middle right empty
						
						if(j>0 && getState[i+2][j-1]==0) { 
							three_unblockable.put(column, j);
							
							if(getState[i][j]==1 && j%2==0 || getState[i][j]==-1 && j%2==1)
								three_unblockable.put(column, j);
						}
						else 
							three_blockable.add(column);
						
						if(three_map[column][row]==0)
							three_map[column][row] = getState[i][j];
						else if(three_map[column][row]!=getState[i][j])
							three_map[column][row] = 2;
					}
	
				    if(getState[i][j]!=0 && i<g.getNumCols()-2 && j<g.getNumRows()-2 && (i==column && j==row || i+1==column && j+1==row || i+2==column && j+2==row)  
				       && getState[i][j]==getState[i+1][j+1] && getState[i][j]==getState[i+2][j+2] && (j<g.getNumRows()-3 &&  i<g.getNumCols()-3 
				       && getState[i+3][j+3]==0 || j>1 && i>1 && getState[i-1][j-1]==0)) {   
				    	//looks for three in a row in upper right diagonal
				    	
				    	if((i<g.getNumCols()-3 && j<g.getNumRows()-3 && getState[i+3][j+2]==0) || (j==2 && i<g.getNumCols()-3 
				    		&& ((i>0 && getState[i-1][j-2]==0) || getState[i+3][j+2]==0)) || (i==g.getNumCols()-3 && j>1 && getState[i-1][j-2]==0) 
				    		|| (j==g.getNumRows()-3 && getState[i-1][j-2]==0)) {
				    		
				    		three_unblockable.put(column, j+1);
				    		
				    		if(getState[i][j]==1 && j%2==1 || getState[i][j]==-1 && j%2==0) {
				    			three_unblockable.put(column, j+1);
				    		}
				    	}
				    	else 
				    		three_blockable.add(column);
				    	
				    	if(three_map[column][row]==0)
				    		three_map[column][row] = getState[i][j];
						else if(three_map[column][row]!=getState[i][j])
							three_map[column][row] = 2;
				    }
				    
				    if(getState[i][j]!=0 && i<g.getNumCols()-3 && j<g.getNumRows()-3 && initial_four_map[i+1][j+1]!=getState[i][j] && initial_four_map[i+1][j+1]!=2   
				       && (i==column && j==row || i+2==column && j+2==row || i+3==column && j+3==row)&& getState[i][j]==getState[i+2][j+2] 
				       && getState[i][j]==getState[i+3][j+3] && getState[i+1][j+1]==0) {   
				    	//looks for 3/4 diagonally middle left empty
				    	
				    	if(getState[i+1][j]==0) {
				    		three_unblockable.put(column, j+1);
				    		
				    		if(getState[i][j]==1 && j%2==1 || getState[i][j]==-1 && j%2==0) 
				    			three_unblockable.put(column, j+1);
				    	}
				    	else 
				    		three_blockable.add(column);
				    	
				    	if(three_map[column][row]==0)
				    		three_map[column][row] = getState[i][j];
						else if(three_map[column][row]!=getState[i][j])
							three_map[column][row] = 2;
				    }
				    
				    if(getState[i][j]!=0 && i<g.getNumCols()-3 && j<g.getNumRows()-3 && initial_four_map[i+2][j+2]!=getState[i][j] && initial_four_map[i+2][j+2]!=2   
				       && (i==column && j==row || i+1==column && j+1==row || i+3==column && j+3==row) && getState[i][j]==getState[i+1][j+1] 
				       && getState[i][j]==getState[i+3][j+3] && getState[i+2][j+2]==0) {
				        //looks for 3/4 diagonally middle right empty
				    	
				    	if(getState[i+2][j+1]==0) {
				    		three_unblockable.put(column, j);
				    		
				    		if(getState[i][j]==1 && j%2==0 || getState[i][j]==-1 && j%2==1) 
				    			three_unblockable.put(column, j);
				    	}
				    	else 
				    		three_blockable.add(column);
				    	
				    	if(three_map[column][row]==0)
				    		three_map[column][row] = getState[i][j];
						else if(three_map[column][row]!=getState[i][j])
							three_map[column][row] = 2;
				    }
				    
				    if(getState[i][j]!=0 && i<g.getNumCols()-2 && j>=2 && (i==column && j==row || i+1==column && j-1==row || i+2==column && j-2==row) 
				       && getState[i][j]==getState[i+1][j-1] && getState[i][j]==getState[i+2][j-2] && (i>1 && j<g.getNumRows()-1 
				       && getState[i-1][j+1]==0 || i<g.getNumCols()-3 && j>2 && getState[i+3][j-3]==0)) {   
				    	//looks for three in a row in lower right diagonal
				    	
				    	if(((j==3 || j==2) && i>0 && getState[i-1][j]==0) || (i>0 && i<g.getNumCols()-3 && j==4 && (getState[i-1][j]==0 || getState[i+3][j-4]==0)) 
				    		|| (i==0 && j>3 && getState[i+3][j-4]==0) || (j==g.getNumCols()-1 && getState[i+3][j-4]==0)) {
				    		
				    		three_unblockable.put(column, j+1);
				    		
				    		if(getState[i][j]==1 && j%2==1 || getState[i][j]==-1 && j%2==0) 
				    			three_unblockable.put(column, j+1);
				    	}
				    	else 
				    		three_blockable.add(column);
				    	
				    	if(three_map[column][row]==0)
				    		three_map[column][row] = getState[i][j];
						else if(three_map[column][row]!=getState[i][j])
							three_map[column][row] = 2;
				    }
				    
				    if(getState[i][j]!=0 && i<g.getNumCols()-3 && j>2 && initial_four_map[i+1][j-1]!=getState[i][j] && initial_four_map[i+1][j-1]!=2   
				       && (i==column && j==row || i+2==column && j-2==row || i+3==column && j-3==row) && getState[i][j]==getState[i+2][j-2] 
				       && getState[i][j]==getState[i+3][j-3] && getState[i+1][j-1]==0) {  
				    	//looks for 3/4 diagonally middle left empty
				    	
				    	if(getState[i+1][j-2]==0) {
				    		three_unblockable.put(column, j-1);
				    		
				    		if(getState[i][j]==1 && j%2==0 || getState[i][j]==-1 && j%2==1) 
				    			three_unblockable.put(column, j-1);
				    	}
				    	else 
				    		three_blockable.add(column);
				    	
				    	if(three_map[column][row]==0)
				    		three_map[column][row] = getState[i][j];
						else if(three_map[column][row]!=getState[i][j])
							three_map[column][row] = 2;
				    }
				    
				    if(getState[i][j]!=0 && i<g.getNumCols()-3 && j>2 && initial_four_map[i+2][j-2]!=getState[i][j] && initial_four_map[i+2][j-2]!=2   
				       && (i==column && j==row || i+1==column && j-1==row || i+3==column && j-3==row) && getState[i][j]==getState[i+1][j-1] 
				       && getState[i][j]==getState[i+3][j-3] && getState[i+2][j-2]==0) {  
				    	//looks for 3/4 diagonally middle right empty
				    	
				    	if(getState[i+2][j-3]==0) {
				    		three_unblockable.put(column, j);
				    		
				    		if(getState[i][j]==1 && j%2==1 || getState[i][j]==-1 && j%2==0) 
				    			three_unblockable.put(column, j);
				    	}
				    	else 
				    		three_blockable.add(column);
				    	
				    	if(three_map[column][row]==0)
				    		three_map[column][row] = getState[i][j];
						else if(three_map[column][row]!=getState[i][j])
							three_map[column][row] = 2;
				    }
			    }  
			}
		
			if((row>0 && getState[column][row-1]!=0)||row==0) {  //adding three blockable and unblockable to ArrayLists, in 'best' list if winning move is on a an opponent's row.
																//the player who goes first's row is the even number rows (0,2,4), and the second player's are the odd ones (1,3,5)
				
				blockable_arr.addAll(three_blockable);
				
				for(int num: three_unblockable.keySet()) {
		    		
					int r = three_unblockable.get(num);
					if(opp_turn && (opp_color==1 && r%2==0 || opp_color==-1 && r%2==1) || !opp_turn && (AI_color==1 && r%2==0 || AI_color==-1 && r%2==1)) 
						best_unblockable_arr.add(num);
					else 
						ok_unblockable_arr.add(num);
				}
		    }
			else if(getState[column][row-1]==0 && (row==1 || (row>1 && getState[column][row-2]!=0))) {  //if move would create bad setup, indicate this
				
				for(int num: three_unblockable.keySet()) {
		    		if(!best_AI_unblockable.contains(num) && !best_opp_unblockable.contains(num) && !bad_position.contains(num)) {
						int r = three_unblockable.get(num);
						if(opp_turn && (opp_color==1 && r%2==0 || opp_color==-1 && r%2==1)) {
							bad_position.add(num);
						}
		    		}
				}
			}
		}
		
		/**
	     * This method looks if a 4 in a row can be made in 2 turns and updates values of different ArrayLists.
	     * Comments under 'if' statements indicate which win type (in a row, upper diagonal, lower diagonal) is being looked at.
	     * Upper diagonal refers to diagonal that moves up as you move from left to right.
	     * @param none
	     * @return void
	    */
		public void avoidLoss() {
			
			for(int i=0; i<g.getNumCols()-4; i++) {
				for(int j=0;j<g.getNumRows(); j++) {
					
					if(getState[i+2][j]!=0 && (getState[i][j]==0 && getState[i+1][j]==0 && getState[i+2][j]==getState[i+3][j] && getState[i+4][j]==0)) {
						// in a row
						if(j==0 || (j>0 && (getState[i][j-1]!=0 && getState[i+1][j-1]!=0 && getState[i+4][j-1]!=0))) {
							System.out.println('a');
							if(!loss_avoider.contains(i+1))
								loss_avoider.add(i+1);
						}
						else if(j>0 && getState[i][j-1]!=0 && getState[i+1][j-1]!=0 && getState[i+4][j-1]==0 && (j==1 || j>1 && getState[i+4][j-2]!=0)) {
							System.out.println('b');
							if(!loss_creator.contains(i+4) && getState[i+2][j]==opp_color)
								loss_creator.add(i+4);
							else if(!bad_setup.contains(i+4) && getState[i+2][j]==AI_color)
								bad_setup.add(i+4);
						}
						else if(j>0 && getState[i][j-1]!=0 && getState[i+1][j-1]==0 && getState[i+4][j-1]!=0 && (j==1 || j>1 && getState[i+1][j-2]!=0)) {
							System.out.println('c');
							if(!loss_creator.contains(i+1) && getState[i+2][j]==opp_color)
								loss_creator.add(i+1);
							else if(!bad_setup.contains(i+1) && getState[i+2][j]==AI_color)
								bad_setup.add(i+1);
						}
						else if(j>0 && getState[i][j-1]==0 && getState[i+1][j-1]!=0 && getState[i+4][j-1]!=0 && (j==1 || j>1 && getState[i][j-2]!=0)) {
							System.out.println('d');
							if(!loss_creator.contains(i) && getState[i+2][j]==opp_color)
								loss_creator.add(i);
							else if(!bad_setup.contains(i) && getState[i+2][j]==AI_color)
								bad_setup.add(i);
							
						}
					}
					
					if(getState[i+2][j]!=0 && (getState[i][j]==0 && getState[i+1][j]==getState[i+2][j] && getState[i+3][j]==0 && getState[i+4][j]==0)) {  
						// in a row
						if(j==0|| (j>0 && (getState[i][j-1]!=0 && getState[i+3][j-1]!=0 && getState[i+4][j-1]!=0))) {
							System.out.println('e');	
							if(!loss_avoider.contains(i+3))
								loss_avoider.add(i+3);
						}
						else if(j>0 && getState[i][j-1]!=0 && getState[i+3][j-1]!=0 && getState[i+4][j-1]==0 && (j==1 || j>1 && getState[i+4][j-2]!=0)) {
							System.out.println('f');
							if(!loss_creator.contains(i+4) && getState[i+2][j]==opp_color)
								loss_creator.add(i+4);
							else if(!bad_setup.contains(i+4) && getState[i+2][j]==AI_color)
								bad_setup.add(i+4);
						}
						else if(j>0 && getState[i][j-1]!=0 && getState[i+3][j-1]==0 && getState[i+4][j-1]!=0 && (j==1 || j>1 && getState[i+3][j-2]!=0)) {
							System.out.println('g');
							if(!loss_creator.contains(i+3) && getState[i+2][j]==opp_color)
								loss_creator.add(i+3);
							else if(!bad_setup.contains(i+3) && getState[i+2][j]==AI_color)
								bad_setup.add(i+3);
						}
						else if(j>0 && getState[i][j-1]==0 && getState[i+3][j-1]!=0 && getState[i+4][j-1]!=0 && (j==1 || j>1 && getState[i][j-2]!=0)) {
							System.out.println('h');
							if(!loss_creator.contains(i) && getState[i+2][j]==opp_color)
								loss_creator.add(i);
							else if(!bad_setup.contains(i) && getState[i+2][j]==AI_color)
								bad_setup.add(i);
						}
					}
					
					if(i>0 && i<g.getNumCols()-3 && getState[i][j]!=0 && getState[i+1][j]==0 && getState[i-1][j]==0 && getState[i+3][j]==0 && getState[i][j]==getState[i+2][j]) { 
						// in a row
						if(j==0|| (j>0 && (getState[i-1][j-1]!=0 && getState[i+1][j-1]!=0 && getState[i+3][j-1]!=0))) {
							System.out.println('i');
							if(!loss_avoider.contains(i+1))
								loss_avoider.add(i+1);
						}
						else if(j>0 && getState[i-1][j-1]!=0 && getState[i+1][j-1]!=0 && getState[i+3][j-1]==0 && (j==1 || j>1 && getState[i+3][j-2]!=0)) {
							System.out.println('j');
							if(!loss_creator.contains(i+3) && getState[i][j]==opp_color)
								loss_creator.add(i+3);
							else if(!bad_setup.contains(i+3) && getState[i][j]==AI_color)
								bad_setup.add(i+3);
						}
						else if(j>0 && getState[i-1][j-1]!=0 && getState[i+1][j-1]==0 && getState[i+3][j-1]!=0 && (j==1 || j>1 && getState[i+1][j-2]!=0)) {
							System.out.println('k');
							if(!loss_creator.contains(i+1) && getState[i][j]==opp_color)
								loss_creator.add(i+1);
							else if(!bad_setup.contains(i+1) && getState[i][j]==AI_color)
								bad_setup.add(i+1);
						}
						else if(j>0 && getState[i-1][j-1]==0 && getState[i+1][j-1]!=0 && getState[i+3][j-1]!=0 && (j==1 || j>1 && getState[i-1][j-2]!=0)) {
							System.out.println('l');
							if(!loss_creator.contains(i-1) && getState[i][j]==opp_color)
								loss_creator.add(i-1);
							else if(!bad_setup.contains(i-1) && getState[i][j]==AI_color)
								bad_setup.add(i-1);
						}
					}
					
					if((i==0 || i==1) && getState[i][j]!=0 && getState[i+1][j]==0 && (getState[i+2][j]==getState[i][j] && getState[i+3][j]==0 || (getState[i+3][j]==getState[i][j] && getState[i+2][j]==0))  
						&& getState[i+4][j]==0 && getState[i+5][j]==getState[i][j]) {   // in a row
						
						if(j==0 || (getState[i+1][j-1]!=0 && (getState[i+2][j]==0 && getState[i+2][j-1]!=0 || getState[i+3][j]==0 && getState[i+3][j-1]!=0) && getState[i+4][j-1]!=0)){
							System.out.println('m');
							if(!loss_avoider.contains(i+1))
								loss_avoider.add(i+1);
							if(!loss_avoider.contains(i+2) && getState[i+2][j]==0)
								loss_avoider.add(i+2);
							if(!loss_avoider.contains(i+3) && getState[i+3][j]==0)
								loss_avoider.add(i+3);
						}
						else if(j>0 && getState[i+1][j-1]==0 && (getState[i+2][j]==0 && getState[i+2][j-1]!=0 || getState[i+3][j]==0 && getState[i+3][j-1]!=0) 
							    && getState[i+4][j-1]!=0 && (j==1 || j>1 && getState[i+1][j-2]!=0)){
							System.out.println('n');
							if(!loss_creator.contains(i+1) && getState[i][j]==opp_color)
								loss_creator.add(i+1);
							else if(!bad_setup.contains(i+1) && getState[i][j]==AI_color)
								bad_setup.add(i+1);
						}
						else if(j>0 && getState[i+1][j-1]!=0 && getState[i+2][j]==0 && getState[i+2][j-1]==0 && getState[i+4][j-1]!=0 && (j==1 || j>1 && getState[i+2][j-2]!=0)){
							System.out.println('o');
							if(!loss_creator.contains(i+2) && getState[i][j]==opp_color)
								loss_creator.add(i+2);
							else if(!bad_setup.contains(i+2) && getState[i][j]==AI_color)
								bad_setup.add(i+2);
						}
						else if(j>0 && getState[i+1][j-1]!=0 && getState[i+3][j]==0 && getState[i+3][j-1]==0 && getState[i+4][j-1]!=0 && (j==1 || j>1 && getState[i+3][j-2]!=0)){
							System.out.println('p');
							if(!loss_creator.contains(i+3) && getState[i][j]==opp_color)
								loss_creator.add(i+3);
							else if(!bad_setup.contains(i+3) && getState[i][j]==AI_color)
								bad_setup.add(i+3);
						}
						else if(j>0 && getState[i+1][j-1]!=0 && (getState[i+2][j]==0 && getState[i+2][j-1]!=0 || getState[i+3][j]==0 && getState[i+3][j-1]!=0) 
							    && getState[i+4][j-1]==0 && (j==1 || j>1 && getState[i+4][j-2]!=0)){
							System.out.println('q');
							if(!loss_creator.contains(i+4) && getState[i][j]==opp_color)
								loss_creator.add(i+4);
							else if(!bad_setup.contains(i+4) && getState[i][j]==AI_color)
								bad_setup.add(i+4);
						}
					}

					if(i<g.getNumCols()-3 && j<g.getNumRows()-3 && getState[i][j]==0 && getState[i+1][j+1]!=0 && getState[i+1][j+1]==getState[i+2][j+2] 
					   && getState[i+3][j+3]==0) {   //two open on left upper diagonal
						
						if(i>0 && j>0 && getState[i-1][j-1]==0 && (j==1 || j>1 && getState[i-1][j-2]!=0) && getState[i][j-1]!=0 && getState[i+3][j+2]!=0) {    
							System.out.println('r' + i);
							if(!loss_avoider.contains(i))
								loss_avoider.add(i);
						}
						else if(i>0 && j>1 && getState[i-1][j-2]==0 && (j==2 || getState[i-1][j-3]!=0) && getState[i][j-1]!=0 && getState[i+3][j+2]!=0) {
							System.out.println('s');
							if(!loss_creator.contains(i-1) && getState[i+1][j+1]==opp_color)
								loss_creator.add(i-1);
							else if(!bad_setup.contains(i-1) && getState[i+1][j+1]==AI_color)
								bad_setup.add(i-1);
						}
						else if(i>0 && j>0 && getState[i-1][j-1]==0 && (j==1 || j>1 && getState[i-1][j-2]!=0 && getState[i][j-2]!=0) && getState[i][j-1]==0 && getState[i+3][j+2]!=0) {
							System.out.println('t');
							if(!loss_creator.contains(i) && getState[i+1][j+1]==opp_color)
								bad_setup.add(i);
							else if(!bad_setup.contains(i) && getState[i+1][j+1]==AI_color)
								bad_setup.add(i);
						}
						else if(i>0 && (j==1 || j>1 && getState[i-1][j-2]!=0) && getState[i][j-1]!=0 && getState[i+3][j+2]==0 && getState[i+3][j+1]!=0) {
							System.out.println('u');
							if(!loss_creator.contains(i+3) && getState[i+1][j+1]==opp_color)
								loss_creator.add(i+3);
							else if(!bad_setup.contains(i+3) && getState[i+1][j+1]==AI_color)
								bad_setup.add(i+3);
						}
						
						if(j<g.getNumRows()-4 && (j==0 || getState[i][j-1]!=0) && getState[i+3][j+2]!=0 && getState[i+4][j+3]!=0) {    //two open on right upper diagonal
							System.out.println('v');
							if(!loss_avoider.contains(i+3))
								loss_avoider.add(i+3);
						}
						else if(j<g.getNumRows()-4 && j>0 && getState[i][j-1]==0 && (j==1 || getState[i][j-2]!=0) && getState[i+3][j+2]!=0 && getState[i+4][j+3]!=0) {
							System.out.println('w');
							if(!loss_creator.contains(i) && getState[i+1][j+1]==opp_color)
								loss_creator.add(i);
							else if(!bad_setup.contains(i) && getState[i+1][j+1]==AI_color)
								bad_setup.add(i);
						}
						else if(j<g.getNumRows()-4 && (j==0 || getState[i][j-1]!=0) && getState[i+3][j+2]==0 && getState[i+3][j+1]!=0 && getState[i+4][j+3]!=0) {
							System.out.println('x');
							if(!loss_creator.contains(i+3) && getState[i+1][j+1]==opp_color)
								loss_creator.add(i+3);
							else if(!bad_setup.contains(i+3) && getState[i+1][j+1]==AI_color)
								bad_setup.add(i+3);
						}
						else if(j<g.getNumRows()-4 && (j==0 || getState[i][j-1]!=0) && getState[i+3][j+2]!=0 && getState[i+4][j+3]==0 && getState[i+4][j+2]!=0) {
							System.out.println('y');
							if(!loss_creator.contains(i+4) && getState[i+1][j+1]==opp_color)
								loss_creator.add(i+4);
							else if(!bad_setup.contains(i+4) && getState[i+1][j+1]==AI_color)
								bad_setup.add(i+4);
						}		
					}
					
					if(j<g.getNumRows()-4 && getState[i][j]==0 && getState[i+1][j+1]!=0 && getState[i+2][j+2]==0   //upper diagonal middle open
					   && getState[i+1][j+1]==getState[i+3][j+3] && getState[i+4][j+4]==0) {

						if((j==0 || getState[i][j-1]!=0) && getState[i+2][j+1]!=0 && getState[i+4][j+3]!=0) {
							System.out.println('z');
							if(!loss_avoider.contains(i+2))
								loss_avoider.add(i+2);
						}
						else if(j>0 && getState[i][j-1]==0 && (j==1 || getState[i][j-2]!=0) && getState[i+2][j+1]!=0 && getState[i+4][j+3]!=0) {
							System.out.println("aa");
							if(!loss_creator.contains(i) && getState[i+1][j+1]==opp_color)
								loss_creator.add(i);
							else if(!bad_setup.contains(i) && getState[i+1][j+1]==AI_color)
								bad_setup.add(i);
						}
						else if((j==0 || getState[i][j-1]!=0) && getState[i+2][j+1]==0 && getState[i+2][j]!=0 && getState[i+4][j+3]!=0) {
							System.out.println("bb");
							if(!loss_creator.contains(i+2) && getState[i+1][j+1]==opp_color)
								loss_creator.add(i+2);
							else if(!bad_setup.contains(i+2) && getState[i+1][j+1]==AI_color)
								bad_setup.add(i+2);
						}
						else if((j==0 || getState[i][j-1]!=0) && getState[i+2][j+1]!=0 && getState[i+4][j+3]==0 && getState[i+4][j+2]!=0) {
							System.out.println("cc");
							if(!loss_creator.contains(i+4) && getState[i+1][j+1]==opp_color)
								loss_creator.add(i+4);
							else if(!bad_setup.contains(i+4) && getState[i+1][j+1]==AI_color)
								bad_setup.add(i+4);
						}		
					}
					
					if(j>2 && getState[i][j]==0 && getState[i+1][j-1]!=0 && getState[i+1][j-1]==getState[i+2][j-2] && getState[i+3][j-3]==0) {
						
						if(i>0 && (j==3 || getState[i+3][j-4]!=0) && j<g.getNumRows()-1 && getState[i-1][j]!=0 && getState[i][j-1]!=0) {  //lower diagonal two open left
							System.out.println("dd");
							if(!loss_avoider.contains(i))
								loss_avoider.add(i);
						}
						else if(i>0 && j<g.getNumRows()-1 && getState[i-1][j]==0 && getState[i-1][j-1]!=0 
								&& getState[i-1][j-1]!=0 && getState[i][j-1]!=0 && (j==3 || getState[i+3][j-4]!=0)) {
							System.out.println("ee");
							if(!loss_creator.contains(i-1) && getState[i+1][j-1]==opp_color)
								loss_creator.add(i-1);
							else if(!bad_setup.contains(i-1) && getState[i+1][j-1]==AI_color)
								bad_setup.add(i-1);
						}
						else if(i>0 && j<g.getNumRows()-1 && getState[i-1][j]!=0 && getState[i][j-1]==0 
								&& getState[i][j-2]!=0 && (j==3 || getState[i+3][j-4]!=0)) {
							System.out.println("ff");
							if(!loss_creator.contains(i) && getState[i+1][j-1]==opp_color)
								loss_creator.add(i);
							else if(!bad_setup.contains(i) && getState[i+1][j-1]==AI_color)
								bad_setup.add(i);
						}
						else if(i>0 && j>3 && j<g.getNumRows()-1 && getState[i-1][j]!=0 && getState[i-1][j+1]==0 
								&& getState[i][j-1]!=0 && getState[i+3][j-4]==0 && (j==4 || getState[i+3][j-5]!=0)) {
							System.out.println("gg");
							if(!loss_creator.contains(i+3) && getState[i+1][j-1]==opp_color)
								loss_creator.add(i+3);
							else if(!bad_setup.contains(i+3) && getState[i+1][j-1]==AI_color)
								bad_setup.add(i+3);
						}
						
						if(i<3 && j>3 && getState[i][j-1]!=0 && getState[i+3][j-4]!=0 
						   && getState[i+4][j-4]==0 && (j==4 || getState[i+4][j-5]!=0)) {   //lower diagonal two open right
							System.out.println("hh");
							if(!loss_avoider.contains(i+3))
								loss_avoider.add(i+3);
						}
						else if(i<3 && j>3 && getState[i][j-1]==0 && getState[i][j-2]!=0 && getState[i+3][j-4]!=0 
								&& getState[i+4][j-4]==0 && (j==4 || getState[i+4][j-5]!=0)) {
							System.out.println("ii");
							if(!loss_creator.contains(i) && getState[i+1][j-1]==opp_color)
								loss_creator.add(i);
							else if(!bad_setup.contains(i) && getState[i+1][j-1]==AI_color)
								bad_setup.add(i);
						}
						else if(i<3 && j>3 && getState[i][j-1]!=0 && getState[i+3][j-4]==0 && getState[i+4][j-4]==0 
								&& (j==4 || getState[i+3][j-5]!=0 && getState[i+4][j-5]!=0)) {
							System.out.println("jj");
							if(!loss_creator.contains(i+3) && getState[i+1][j-1]==opp_color)
								loss_creator.add(i+3);
							else if(!bad_setup.contains(i+3) && getState[i+1][j-1]==AI_color)
								bad_setup.add(i+3);
						}
						else if(j>4 && getState[i][j-1]!=0 && getState[i+3][j-4]!=0 && getState[i+4][j-5]==0) {
							System.out.println("kk");
							if(!loss_creator.contains(i+4) && getState[i+1][j-1]==opp_color)
								loss_creator.add(i+4);
							else if(!bad_setup.contains(i+4) && getState[i+1][j-1]==AI_color)
								bad_setup.add(i+4);
						}	
					}
					
					if(i<3 && j>3 && getState[i][j]==0 && getState[i+1][j-1]!=0 && getState[i+1][j-1]==getState[i+3][j-3] 
					   && getState[i+2][j-2]==0 && getState[i+4][j-4]==0) {  //lower diagonal middle open
						
						if(getState[i][j-1]!=0 && getState[i+2][j-3]!=0 && (j==4 || getState[i+4][j-5]!=0)) {
							System.out.println("ll");
							if(!loss_avoider.contains(i+2))
								loss_avoider.add(i+2);
						}
						else if(getState[i][j-1]==0 && getState[i][j-2]!=0 && getState[i+2][j-3]!=0 && (j==4 || getState[i+4][j-5]!=0)) {
							System.out.println("mm");
							if(!loss_creator.contains(i) && getState[i+1][j-1]==opp_color)
								loss_creator.add(i);
							else if(!bad_setup.contains(i) && getState[i+1][j-1]==AI_color)
								bad_setup.add(i);
						}
						else if(getState[i][j-1]!=0 && getState[i+2][j-3]==0 && getState[i+2][j-4]!=0 && (j==4 || getState[i+4][j-5]!=0)) {
							System.out.println("nn");
							if(!loss_creator.contains(i+2) && getState[i+1][j-1]==opp_color)
								loss_creator.add(i+2);
							else if(!bad_setup.contains(i+2) && getState[i+1][j-1]==AI_color)
								bad_setup.add(i+2);
						}
						else if(j>4 &getState[i][j-1]!=0 && getState[i+2][j-3]!=0 && getState[i+4][j-5]==0) {
							System.out.println("oo");
							if(!loss_creator.contains(i+4) && getState[i+1][j-1]==opp_color)
								loss_creator.add(i+4);
							else if(!bad_setup.contains(i+4) && getState[i+1][j-1]==AI_color)
								bad_setup.add(i+4);
						}
					}				
				}
			}  
		}
		
		/**
	     * This method looks to see if a move will create a column with 2 winning moves stacked on top of one another
	     * @param column: column of move to be simulated
	     * @param row: row of simulated move
	     * @param opp_turn: boolean, true if simulating Ai's opponent's turn, false otherwise
	     * @return void
	    */
		public void winningColumnCreator(int column, int row, boolean opp_turn) {
			
			HashMap<Integer, Integer> opp_winning_moves = new HashMap<>();
			HashMap<Integer, Integer> ai_winning_moves = new HashMap<>();
			four_map = fourMap();
			boolean opp_can_win = false;
			int empty_squares;

			for(int i=0; i<g.getNumCols(); i++) {
				
				empty_squares=0;
				
				for(int j=0; j<g.getNumRows(); j++) {
					
					if(getState[i][j]==0)
						empty_squares++;
					
					if(!not_winning_column.contains(i)) {
					
						if(j<g.getNumRows()-1 && four_map[i][j]==AI_color && (four_map[i][j]==four_map[i][j+1] || four_map[i][j+1]==2)) {
						
							for(int num=0; num<j; num++) {
								if(four_map[i][num]==opp_color || four_map[i][num]==2)
									opp_can_win = true;			
							}
							if(!AI_winning_column.contains(column) && !opp_can_win) 
								AI_winnable_columns.put(column, empty_squares);
						}
						else if(j<g.getNumRows()-1 && four_map[i][j]==opp_color && (four_map[i][j]==four_map[i][j+1] || four_map[i][j+1]==2)) {
							
							for(int num=0; num<j; num++) {
								if(four_map[i][j]==opp_color || four_map[i][num]==2)
									opp_can_win = true;			
							}
							if(!opp_winning_column.contains(column) && !opp_can_win) 
								opp_winnable_columns.put(column, empty_squares);	
						}
						if(four_map[i][j]==AI_color && (j==0 || getState[i][j-1]!=0)) {
							
							if(!ai_winning_moves.containsKey(column))
								ai_winning_moves.put(column, 1);
							else {
								int count = ai_winning_moves.get(column);
								ai_winning_moves.put(column, count+1);
							}
						}
						else if(four_map[i][j]==opp_color && (j==0 || getState[i][j-1]!=0)) {
							
							if(!opp_winning_moves.containsKey(column))
								opp_winning_moves.put(column, 1);
							else {
								int count = opp_winning_moves.get(column);
								opp_winning_moves.put(column, count+1);
							}
						}	
					}
				}					    
			}
			
			for(int num=0; num<g.getNumCols(); num++){
				if(!AI_winning_column.contains(column) && (ai_winning_moves.containsKey(num) && ai_winning_moves.get(num)>1)){ 
					System.out.println("Ai winning column " +column );
					AI_winning_column.add(column);
				}
				if(!opp_winning_column.contains(column) && (opp_winning_moves.containsKey(num) && opp_winning_moves.get(num)>1)) {
					System.out.println("opp winning column " +column );
					opp_winning_column.add(column);
				}
			}
		}
		
		/**
	     * This method looks to see if a move will allow opponent to set up a winning move for themselves
	     * @param column: column of move to be simulated
	     * @param row: row of simulated move
	     * @return void
	    */
		public void losingBoardCreator(int column, int row){
			
			HashMap<Integer, Integer> opp_winning_creator = new HashMap<>();
			int[][] four_map;

			for(int c=0; c<g.getNumCols(); c++) {
				
				int empty_squares = 0;
				boolean AI_can_win = false;
				
				for(int r=0; r<g.getNumRows(); r++) {
					
					if(getState[c][r]==0) {
						empty_squares++;
					}
					
					if(getState[c][r]==0 && (r==0||getState[c][r-1]!=0)) {
						opp_winning_creator = new HashMap<>();
						
						getState[c][r] = opp_color;
						four_map = fourMap();
						
						for(int i=0; i<g.getNumCols(); i++) {
							for(int j=0; j<g.getNumRows(); j++) {
								
								if(j<g.getNumRows()-1 && four_map[i][j]==opp_color && (four_map[i][j]==four_map[i][j+1] || four_map[i][j+1]==2) 
								   && !loss_creator.contains(column)) {
									
									for(int num=0; num<j; num++) {
										if(four_map[i][num]==AI_color)
											AI_can_win = true;
									}
									
									if(!AI_can_win) {
										if(!AI_winnable_columns.isEmpty()) {
											for(int index: winning_index) {
												if(empty_squares<AI_winnable_columns.get(index)) 
													loss_creator.add(column);
											}
										}
										else {
											loss_creator.add(column);
										}
									}
								}
								
								if(four_map[i][j]==opp_color && (j==0 || getState[i][j-1]!=0)) {
									
									if(!opp_winning_creator.containsKey(column))
										opp_winning_creator.put(column, 1);
									else {
										int count = opp_winning_creator.get(column);
										opp_winning_creator.put(column, count+1);
									}
								}
							}
						}
						getState[c][r] = 0;

						for(int col_num=0; col_num<g.getNumCols(); col_num++){
							if(!opp_winning_creator.isEmpty() && opp_winning_creator.containsKey(col_num) && opp_winning_creator.get(col_num)>1) { 
								if(!loss_creator.contains(col_num)) 
									loss_creator.add(col_num);
							}
						}
					}
				}
			}
		}
		
		/**
	     * This method looks to see if a move will make a column with two winning moves on top of one another not be a winning column
	     * @param column: column of move to be simulated
	     * @param opp_turn: boolean, true if simulating Ai's opponent's turn, false otherwise
	     * @return void
	    */
		public void destroyWinningColumn(int column, boolean opp_turn) {
			
			four_map = fourMap();
			boolean opp_can_win = false;
			HashMap<Integer, Integer> winnable_columns;
			HashMap<Integer, Integer> temp_winning_map;
			ArrayList<Integer> winning_column;
			int good_color;
			int bad_color;
			
			if(opp_turn) {
				winnable_columns = AI_winnable_columns;
				winning_column= AI_winning_column;
				good_color = AI_color;
				bad_color = opp_color;
			}
			else {
				winnable_columns = opp_winnable_columns;
				winning_column= opp_winning_column;
				good_color = opp_color;
				bad_color = AI_color;
			}
			
			temp_winning_map = new HashMap(winnable_columns);
			
			for(int i: temp_winning_map.keySet()) {    //looks at places on board where four in a row can be made and if two of same color on top of each other, indicates winning column

				int still_good_column = -1;
				opp_can_win = false;

				for(int j=0;j<g.getNumRows(); j++) {
					
					if(j<g.getNumRows()-1 && (four_map[i][j]==good_color || four_map[i][j]==2) && (four_map[i][j+1]== good_color || four_map[i][j+1]==2)) {
						
						for(int num=0; num<j+1; num++) {
							if(four_map[i][num]==bad_color || four_map[i][num]==2) {
								opp_can_win = true;
								break;
							}
						}
							
						if(!opp_can_win) 
							still_good_column = i;
					}
				}
				
				if(still_good_column!=i) {
					winnable_columns.remove(i);
					not_winning_column.add(i);
				}	
			}
		
			if(winnable_columns.isEmpty()) {
				already_winning_column = false;
			}
			else {
				HashMap<Integer, Integer> temp_map = new HashMap<>(winnable_columns);
				
				while(winnable_columns.keySet().size()>1) {
					
					for(int key: temp_map.keySet()) {
						if(winnable_columns.keySet().size()<2)
							break;
						
						int num = temp_map.get(key);
						num--;
						if(num<0) 
							winnable_columns.remove(key);
						else 
							temp_map.put(key, num);
					}
				}

				if(!winning_column.contains((int)temp_map.keySet().toArray()[0])) 
					winning_column.add((int)temp_map.keySet().toArray()[0]);
			}
		}
		
		/**
	     * This method checks the columns that are considered winning columns and tests whether they are actually winning
	     * @param column: column of move to be simulated
	     * @param opp_turn: boolean, true if simulating Ai's opponent's turn, false otherwise
	     * @return void
	    */
		public void testWinningColumn(int column, boolean opp_turn) {

			boolean opp_can_win = false;
			HashMap<Integer, Integer> winnable_columns;
			HashMap<Integer, Integer> temp_map;
			
			ArrayList<Integer> winning_column;
			int good_color;
			int bad_color;
			int row_of_move = -1;
			
			if(opp_turn) {
				winnable_columns = AI_winnable_columns;
				winning_column= AI_winning_column;
				good_color = AI_color;
				bad_color = opp_color;
			}
			else {
				winnable_columns = opp_winnable_columns;
				winning_column= opp_winning_column;
				good_color = opp_color;
				bad_color = AI_color;
			}
			
			temp_map = new HashMap(winnable_columns);			
			
			for(int col: temp_map.keySet()) {    //looks at places on board where four in a row can be made and if two of same color on top of each other, indicates winning column

				for(int r=0; r<g.getNumRows(); r++) {  //finds row that potential winning move would be played on
					if(getState[col][r]==0) {
						getState[col][r] = good_color;
						row_of_move = r;
						break;
					}
				}
				
				int still_good_column = -1;
				opp_can_win = false;
				
				for(int i=0;i<g.getNumCols(); i++) {  //looking at all possible moves with current game board and seeing if the would make winning move not winning anymore
					for(int j=0;j<g.getNumRows(); j++) {
						
						if(getState[i][j]==0) { // simulating move to see whether move offsets predetermined winning move
							
							getState[i][j] = bad_color;
							four_map = fourMap();

							if(j<g.getNumRows()-1 && (four_map[i][j]==good_color || four_map[i][j]==2) && (four_map[i][j+1]== good_color || four_map[i][j+1]==2)) {
								
								for(int num=0; num<j+1; num++) {
									if(four_map[i][num]==bad_color || four_map[i][num]==2) {
										opp_can_win = true;
										break;
									}
								}
									
								if(!opp_can_win) 
									still_good_column = col;
							}
							getState[i][j] = 0;
						}
					}
				}
				
				if(still_good_column!=col)  //removing thought to be winning move if its not actually winning
					winnable_columns.remove(col);
				
				
				getState[col][row_of_move] = 0;  //resetting original simulated move
			}

			for(int index: winnable_columns.keySet()) {  //adding actual winning columns to ArrayList
				if(!winning_column.contains(index))
					winning_column.add(index);
			}
			
		}

		/**
	     * This method checks for moves that allow opponent to make three in a row
	     * @param none
	     * @return void 
	    */
		public void avoidAllowingThree() {
			
			for(int i=0; i<g.getNumCols(); i++) {
				for(int j=1;j<g.getNumRows(); j++) {
					if(!three_preventer.contains(i) && three_map[i][j]!=0 && ((j>1 && getState[i][j-1]==0 && getState[i][j-2] != 0) || (j==1 && getState[i][j-1]==0))) {
					  	three_preventer.add(i);
					}
					if(!bad_position.contains(i) && i<g.getNumCols()-3 && (three_map[i][j]==opp_color || three_map[i][j]==2)
					   && (three_map[i+3][j]==opp_color || three_map[i+3][j]==2)
					   && ((j>1 && getState[i][j-1]==0 && getState[i][j-2] != 0) || (j==1 && getState[i][j-1]==0)) 
			           && getState[i+1][j]==opp_color && getState[i+2][j]==opp_color && getState[i+3][j-1]==0) {
				  
						bad_position.add(i);
					}
					if(!bad_position.contains(i+3) && i<g.getNumCols()-3 && (three_map[i][j]==opp_color || three_map[i][j]==2)
					   && (three_map[i+3][j]==opp_color || three_map[i+3][j]==2)
					   && ((j>1 && getState[i+3][j-1]==0 && getState[i+3][j-2] != 0) || (j==1 && getState[i+3][j-1]==0)) 
					   && getState[i+1][j]==opp_color && getState[i+2][j]==opp_color && getState[i][j-1]==0) {
						  
						bad_position.add(i+3);
					}
				}
			}
		}
	}
	
	
	
	boolean sim_mode;  // if true, moves will be simulated by ai in order to see if initial move will cause winning/losing game under its own logic
	
	/**
     * This constructor initializes of sim_mode to whatever value is passed in
     * @param sim_mode: boolean, if true, moves will be simulated by ai in order to see if initial move will cause winning/losing game under its own logic
    */
	public JackMaloonAI(boolean sim_mode) {
		this.sim_mode = sim_mode;
	}
	
	/**
     * This method simulates several moves in advance (if sim_mode true) and will return a column to be played based on logic in  moveFinder
     * @param column: column of move to be simulated
     * @return int: column to be played
    */
	public int nextMove(CFGame g) {
		moveFinder m = new moveFinder(g);						//instance of moveFinder class	
		ArrayList<Integer> moves_played = new ArrayList<>();	//columns of moves simulated
		ArrayList<Integer> good_columns = new ArrayList<>();	//will hold columns of moves that causes ai (going first) to win against itself
		ArrayList<Integer> bad_columns = new ArrayList<>();		//will hold columns of moves that causes ai (going first) to lose against itself
		int ai_num = -1;
		int opp_first_move = -1;
		HashMap<Integer, Integer> good_first_moves = new HashMap<>();
		int index;
		
		int win_finder = m.findWinningColumn();

		if(win_finder!=-1) {
			return(win_finder);
		}
		else {
			for(int col=0; col<g.getNumCols(); col++) { 
				if(sim_mode && !g.fullColumn(col)) {  //simulating game moves
					g.play(col);
					if(!g.isRedTurn())
						ai_num = 1;
				
					if(!g.isGameOver()) {
						for(int sim_move=0; sim_move<10; sim_move++) {
							index = columnQuality(g, new moveFinder(g), false, new ArrayList<Integer>(), new ArrayList<Integer>());
							g.play(index);
							moves_played.add(index);
							
							if(sim_move==0)
								opp_first_move = index;
							
							if(g.isGameOver()) {
								if(g.isWinner()) {

									if(g.isRedTurn() && ai_num==-1 || !g.isRedTurn() && ai_num==1) {
										good_columns.add(col);
										good_first_moves.put(col, opp_first_move);
									}
									if(g.isRedTurn() && ai_num==1 || !g.isRedTurn() && ai_num==-1) 
										bad_columns.add(col);
								}
								break;
							}
						}
					}
					
					for(int num:moves_played) {
						g.unplay(num);
					}
					
					moves_played.clear();
					g.unplay(col);
				}	
			}

			ArrayList<Integer> temp_good = new ArrayList<Integer>(good_columns);
			ArrayList<Integer> test_bad_columns = new ArrayList<>();
			
			if(!temp_good.isEmpty()) {
				for(int col_index: temp_good) {
					
					if(sim_mode && !g.fullColumn(col_index)) {
						g.play(col_index);
												
						for(int col=0; col<g.getNumCols(); col++) {
							if(!g.fullColumn(col)) {
								
								g.play(col);

								if(g.isRedTurn())
									ai_num = -1;
								else
									ai_num = 1;
							
								if(!g.isGameOver()) {
									for(int sim_move=0; sim_move<10; sim_move++) {
										index = columnQuality(g, new moveFinder(g), false, new ArrayList<Integer>(), new ArrayList<Integer>());
										g.play(index);
										moves_played.add(index);
										
										if(g.isGameOver()) {
											if(g.isWinner()) {
												if(g.isRedTurn() && ai_num==1 || !g.isRedTurn() && ai_num==-1) 
													test_bad_columns.add(col);
											}
											
											break;
										}
									}
								}
								
								for(int num:moves_played) {
									g.unplay(num);
								}
								
								moves_played.clear();
								g.unplay(col);
							}
						}
						g.unplay(col_index);
					}
 
					if(test_bad_columns.contains(good_first_moves.get(col_index))) 
						good_columns.remove((Object) col_index);
				}
			}

			return(columnQuality(g, new moveFinder(g), true, good_columns, bad_columns));
		}
	}
	
	/**
     * This method creates instance of moveFinder inner class to gather logic based on differnt potential moves
     * @param g: instance of CFGame class that holds logic behind game
     * @param m: instance of moveFinder inner class that holds logic behind each potential move
     * @param print: boolean that will print values of ArrayLists from moveFinder if true
     * @param good_sim_columns: ArrayList of moves in which AI beat itself when going first
     * @param bad_sim_columns: ArrayList of moves in which AI lost to itself when going first
     * @return int: column to be played
    */
	public int columnQuality(CFGame g, moveFinder m, boolean print, ArrayList<Integer> good_sim_columns, ArrayList<Integer> bad_sim_columns) {
		int index = m.findWinningColumn();   //function that finds winning moves, moves to avoid losing, and also columns that are winnable

		int[] quality_array = {0,1,3,7,3,1,0}; 		//Assigning starting values to quality array that skew towards the center. This array will  determine which col to play

		if(index!=-1)      //Looks to see if there is a column that can be played that guarantees win
			return(index);
		
		else {
			for(int col=0; col<g.getNumCols(); col++) {   //plays winning move if possible
				m.pretendPlay(col, false);	
			}
			
			for(int col=0; col<g.getNumCols(); col++) {  //blocks opponent winning move if possible
				m.pretendPlay(col, true);

				quality_array[col] += m.num_touching;
			}

			if(print) {  //printing values in various ArrayLists
				System.out.println("good sim columns " + good_sim_columns);
				System.out.println("bad sim columns " + bad_sim_columns);
				System.out.println("losing moves " + m.losing_moves);
				System.out.println("unwise moves " + m.unwise_moves);
				System.out.println("sacrifice moves " + m.sacrifice_moves);
				System.out.println("illegal moves " + m.illegal_moves);
				System.out.println("three_preventable " + m.three_preventer);
				System.out.println("bad position " + m.bad_position);
				System.out.println("best ai  unblockable " + m.best_AI_unblockable);
				System.out.println("best opp unblockable " + m.best_opp_unblockable);
				System.out.println("ok ai  unblockable " + m.ok_AI_unblockable);
				System.out.println("ok opp unblockable " + m.ok_opp_unblockable);
				System.out.println("ai blockable " + m.AI_blockable);
				System.out.println("opp blockable " + m.opp_blockable);
				System.out.println("AI winning column " +m.AI_winning_column);
				System.out.println("opp winning column " +m.opp_winning_column);
				System.out.println("loss avoider " + m.loss_avoider);
				System.out.println("loss creator " + m.loss_creator);
			
			}
		
			return max_element_array(quality_array, good_sim_columns, bad_sim_columns, m.illegal_moves, m.losing_moves, m.unwise_moves, m.sacrifice_moves, 
					m.bad_setup, m.loss_avoider, m.loss_creator, m.best_AI_unblockable, m.best_opp_unblockable, m.ok_AI_unblockable, m.ok_opp_unblockable, 
					m.AI_blockable, m.opp_blockable, m.AI_winning_column, m.opp_winning_column, m.three_preventer, m.bad_position, m.four_map);
		}
	}
	
	/**
     * This method creates instance of moveFinder inner class to gather logic based on differnt potential moves
     * @params: ArrayLists from movefinder with column numbers as values
     * @return: int: column to be played
    */
	public int max_element_array(int[] quality_arr, ArrayList<Integer> good_sim_columns, ArrayList<Integer> bad_sim_columns, ArrayList<Integer> illegal_moves,
			ArrayList<Integer> losing_moves, ArrayList<Integer> unwise_moves, ArrayList<Integer> sacrifice_moves, ArrayList<Integer> bad_setup, 
			ArrayList<Integer> loss_avoider, ArrayList<Integer> loss_creator, ArrayList<Integer> best_AI_unblockable, ArrayList<Integer> best_opp_unblockable, 
			ArrayList<Integer> ok_AI_unblockable, ArrayList<Integer> ok_opp_unblockable, ArrayList<Integer> ai_blockable, ArrayList<Integer> opp_blockable, 
			ArrayList<Integer> AI_winning_column, ArrayList<Integer> opp_winning_column, ArrayList<Integer> three_preventable, ArrayList<Integer> bad_position, int[][] four_potential_map) {
		
		// values assigned to columns from different arraylists. Negative means bad (the more negative the worse) and positive is good (the more positive the better)
		int max = -1000000;
		int max_element = 0;
		int illegal = -10000;
		int losing = -1000;
		int unwise = -150;
		int loss_finder = 500;
		int loss_maker = -500;
		int block_opponent_three = -7;
		int ai_three_best = 30;
		int opp_three_best  = 20;
		int ai_three_ok = 24;
		int opp_three_ok  = 16;
		int three_grouped_blockable = 7;
		int opponent_three_blockable  = 5;
		int good_column = 50;
		int great_column = 400;
		int bad_place = -50;
		int sacrifice = 20;
		int poor_setup = -50;
		
		
		
		ArrayList<Integer> duplicate_max = new ArrayList<>();
		
		for(int index:good_sim_columns) {			//Adding positive value to a column in which AI going first beat itself
			quality_arr[index] += good_column;
		}
		for(int index:bad_sim_columns) {			//Adding negative value to a column in which AI going first lost to itself
			quality_arr[index] += unwise;
		}
		for(int index:unwise_moves) {				//Adding negative value to a move that allows opponent to block aAI winning move
			quality_arr[index] += unwise;
		}
		for(int index:sacrifice_moves) {			//Adding positive value to a column that allows opponent to block AI's winning move to create better winning move
			quality_arr[index] += sacrifice;
		}
		for(int index:bad_setup) {					//Adding negative value to a column that creates a good board setup for opponent
			quality_arr[index] += poor_setup;
		}
		for(int index:loss_avoider) {				//Adding positive value to a column move that avoids loss for AI
			quality_arr[index] += loss_finder;
		}
		for(int index:loss_creator) {				//Adding negative value to a column that allows opponent to make create a board that they can immediately win on
			quality_arr[index] += loss_maker;
		}
		for(int index:best_AI_unblockable) {		//Adding positive value to making three in a row that can't be blocked with potential winning move on good row
			quality_arr[index] += ai_three_best;
		}
		for(int index:best_opp_unblockable) {		//Adding positive value to blocking opponent three in a row that can't be blocked with potential winning move on good row
			quality_arr[index] += opp_three_best;
		}
		for(int index:ok_AI_unblockable) {			//Adding positive value to making three in a row that can't be blocked with potential winning move on bad row
			quality_arr[index] += ai_three_ok;
		}
		for(int index:ok_opp_unblockable) {			//Adding positive value to blocking opponent three in a row that can't be blocked with potential winning move on bad row
			quality_arr[index] += opp_three_ok;
		}
		for(int index:ai_blockable) {				//Adding positive value to making three in a row that can be blocked
			quality_arr[index] += three_grouped_blockable;
		}
		for(int index:opp_blockable) {				//Adding positive value to blocking opponent three in a row that can be blocked
			quality_arr[index] += opponent_three_blockable;
		}
		for(int index:three_preventable) {			//Adding negative value to a column that allows opponent to make three in a row
			quality_arr[index] += block_opponent_three;
		}
		for(int index:bad_position) {				//Adding negative value to move that sets up opponent in good position
			quality_arr[index] += bad_place;
		}
		for(int index:AI_winning_column) {			//Adding value to a column that creates two winning moves on top of one another
			quality_arr[index] += great_column;
		}
		for(int index:opp_winning_column) {			//Adding value to a column that blocks opponent from two winning moves on top of one another
			quality_arr[index] += great_column;
		}
		for(int index:losing_moves) {				//Setting values of columns that allow opponent to win to large negative number
			quality_arr[index] = losing;
		}
		for(int index:illegal_moves) {				//Setting values of columns that canoot be played to very large negative value if move is illegal
			quality_arr[index] = illegal;
		}

		for(int i=0; i<quality_arr.length; i++) {    //iterates through array with values assigned to each column, find biggest value (best column)
			if(quality_arr[i] > max) {
				max = quality_arr[i];
				max_element = i;
				if(duplicate_max.size() > 0)
					duplicate_max.clear();
			}
			else if(quality_arr[i] == max) {

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
	
	/**
     * This method returns name of this AI.
     * @param none
     * @return String: name of AI
    */
	public String getName() {
		if(sim_mode)
			return "Jack Maloon better AI";
		else
			return "Worse AI";
	}
}

/*
System.out.println("fourmap");
for(int ii=5; ii>=0; ii--) {
	for(int jj = 0;jj<g.getNumCols();jj++) {
		if(four_map[jj][ii] == -1)
			System.out.print(" " + four_map[jj][ii]);
		else
			System.out.print( "  " + four_map[jj][ii]);
	}
	System.out.println("");
}
System.out.println("");	
*/
