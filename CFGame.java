package hw4;

/**
 * This class holds the logic behind the game. Functions in this class play certain moves,
 * tell if the game is over, and tell who's turn it is.
	
 * state[i][j]= 0 means the i,j slot is empty
 * state[i][j]= 1 means the i,j slot has red
 * state[i][j]=-1 means the i,j slot has black
*/
public class CFGame {
	
	private final int[][] state;		//current game board
	private int[] moves_played;			//holds in order all columns played in a game
	private boolean is_red_turn;		//true if it is currently red's turn (or 1's turn)
	boolean played;						//true if move has been played in current iteration
	private int num_rows = 6;			//number of rows in game board
	private int num_cols = 7;			//number of columns in game board
	private int move_num = 0;           //number of moves played so far in game
	  
    {
    	state = new int[num_cols][num_rows];
    	moves_played = new int[num_cols*num_rows];
	
    	for (int i=0; i<num_cols; i++) {
    		for (int j=0; j<num_rows; j++) {
    			state[i][j] = 0;
    			moves_played[(j+ num_rows*i)] = -1;
    		}
    	}
    	is_red_turn = true;   //red goes first
    }
    
    /**
     * This method returns the current game in array form.
     * @param none
     * @return ret_arr: This is a 2D array with first array representing columns and second representing rows
    */
	public int[][] getState() {
		
		int[][] ret_arr = new int[num_cols][num_rows];
	  
		for (int i=0; i<num_cols; i++)
			for (int j=0; j<num_rows; j++)
				ret_arr[i][j] = state[i][j];
	  
		return ret_arr;
	}
	
	/**
     * This method returns whether it's red's turn.
     * @param none
     * @return is_red_turn: true if it is red's turn (or 1's turn)
    */
	public boolean isRedTurn() {
		
		return is_red_turn;
	}
	
	/**
     * This method plays a move if it move is legal.
     * @param column: column to be played
     * @return boolean: true if move was played, false if move can't be played
    */  
    public boolean play(int column) {
	  
    	played = false;
	  
    	if(column<0 || column>=num_cols || !notFullColumn(column)) 
    		return false;
	  
    	for(int row=0; row<num_rows; row++) {
    		if(state[column][row]==0) {	
    			if(is_red_turn) { 
    		        state[column][row] = 1;
				    gameMoves(column, false);
				    played = true;
				    is_red_turn = false;
    			}
				else if(!is_red_turn && !played) {
					state[column][row] = -1;
					gameMoves(column, false);
					is_red_turn = true;
					played = true;
				}
				return true;
			}  
    	}    
		return true;
    }
    
    /**
     * This method undoes a move if possible.
     * @param column: column to be unplayed
     * @return boolean: true if move was unplayed, false if move can't be unplayed
    */
    public boolean unplay(int column) {
	  
    	played = false;
	  
    	if(column<0 || column>=num_cols ||state[column][0]==0) 
		 	return false;
    	else {
    		for(int r=getNumRows()-1; r>=0; r--) {
    			if(state[column][r]!=0) {
    				state[column][r] = 0;
    				gameMoves(column, true);
    				if(isRedTurn()) {
    					is_red_turn = false;
    				}
    				else {
    					is_red_turn = true;
				    }
				    return true;
				}
			}
		}
    	return true;
	}
	 
    /**
     * This method updates the moves played within a game.
     * @param column: column to be added to current array with previous game moves
     * @param undo: true if a moe is being unplayed. If true, index of move will be made -1
     * @return void
    */
    public void gameMoves(int column, boolean undo) {
	  
	    if(!undo) {
	        moves_played[move_num] = column;
	        move_num++;
	    }
	    else {
	        move_num--;
	        moves_played[move_num] = 0;		  
	    }
    }
  
    /**
     * This method prints the moves (columns) played in a game. -1 will be printed for all moves past last move
     * @param none
     * @return void
    */
    public void printGameMoves() {
	  
	    for(int move:moves_played) {
	    	System.out.print(move + ", ");
	    }
	    System.out.println("");
    }
  
    /**
     * This method returns number of winner (1 for red, -1 for black, 0 for draw).
     * @param none
     * @return int: number of winner
    */
    public int winner() {
	  
    	if(isGameOver() && isWinner()) {
    		if(isRedTurn())
    			return 1;
    		else if(!isRedTurn())
    			return -1;  
    	}  
    	return 0;
    }
	  
    /**
     * This method finds whether there is a winner.
     * @param none
     * @return boolean: true if there is a winner, false otherwise
    */
    public boolean isWinner() {
	  
    	for(int i=0;i<num_cols;i++) {
    		for(int j = 0;j<num_rows;j++) {
    			if(state[i][j] != 0 && (j+3<num_rows && state[i][j] == state[i][j+1] && state[i][j] == state[i][j+2] && state[i][j] == state[i][j+3] 
				   || i+3 < num_cols && state[i][j] == state[i+1][j] && state[i][j] == state[i+2][j] && state[i][j] == state[i+3][j]
				   || j+3<num_rows &&  i+3 < num_cols && state[i][j] == state[i+1][j+1] && state[i][j] == state[i+2][j+2] && state[i][j] == state[i+3][j+3]
				   ||  i+3 < num_cols && j-3 >= 0 && state[i][j] == state[i+1][j-1] && state[i][j] == state[i+2][j-2] && state[i][j] == state[i+3][j-3])) 
				  	  
				  return true;
		    }  
	    }
	    return false;
    }
	  
    /**
     * This method determines whether a game is over.
     * @param none
     * @return boolean: true if game is over, false otherwise
    */
    public boolean isGameOver() {
	  
	    if(isWinner())
		    return true;
	  
	    for(int k = 0;k<num_cols;k++) {
		    if(state[k][num_rows-1]==0)
			    return false;
	    }
        return true;
    }
    
    /**
     * This method returns number of rows in game board.
     * @param none
     * @return num_rows: number of rows in game
    */  
    public int getNumRows(){
	    return num_rows;
    }

    /**
     * This method returns number of columns in game board.
     * @param none
     * @return num_cols: number of columns in game
    */ 
    public int getNumCols(){
     	 return num_cols;
    }
    
    /**
     * This method tells whether a column is full or not.
     * @param column: column number
     * @return boolean: true if column exists and is not full, false otherwise
    */ 
    public boolean notFullColumn(int column) {
	 
    	if(column<0 || column>=num_cols || state[column][num_rows-1]!=0) 
    		return false;
	   
    	return true;
    }
}