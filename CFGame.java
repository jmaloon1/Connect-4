

public class CFGame {
	
  //state[i][j]= 0 means the i,j slot is empty
  //state[i][j]= 1 means the i,j slot has red
  //state[i][j]=-1 means the i,j slot has black
	
  private final int[][] state;
  private int[] moves_played;
  private boolean isRedTurn;		
  boolean played;			
  private int numRows = 6;
  private int numCols = 7;
  private int move_num = 0;
  
  {
    state = new int[numCols][numRows];
    moves_played = new int[numCols*numRows];
    
    
    for (int i=0; i<numCols; i++) {
    	for (int j=0; j<numRows; j++) {
    		state[i][j] = 0;
    		moves_played[(j+ 6*i)] = -1;
    	}
    }
    isRedTurn = true; //red goes first
  }
    
  public int[][] getState() {						//returns state of board
    int[][] ret_arr = new int[numCols][numRows];
    for (int i=0; i<numCols; i++)
      for (int j=0; j<numRows; j++)
        ret_arr[i][j] = state[i][j];
    return ret_arr;
  }
  
  public boolean isRedTurn() {		//returns whether its reds turn
    return isRedTurn;
  }
  
  public boolean play(int column) {		//checks if it certain column is playable, and plays it if it is
	  
	  played = false;
	  
	  
	  if(column<0 || column>=numCols || !notFullColumn(column)) 
		  return false;
	  
		  for(int row=0; row<numRows; row++) {
				  if(state[column][row] == 0) {	
					  if(isRedTurn) { 
						  state[column][row] = 1;
						  gameMoves(column, false);
						  played = true;
						  isRedTurn = false;
					  }
					  else if(!isRedTurn && !played) {
						  state[column][row] = -1;
						  gameMoves(column, false);
						  isRedTurn = true;
						  played = true;
					  }
					  return true;
				  }  
		  }
		  return true;
  }
  
  public boolean unplay(int column) {		//undoes a move
	  
	  played = false;
	  
	  if(column<0 || column>=numCols ||state[column][0]==0) 
		  return false;
	  else {
		  for(int r=getNumRows()-1; r>=0; r--) {
			  if(state[column][r]!=0) {
				  state[column][r] = 0;
				  if(isRedTurn()) {
					  //System.out.println("made false");
					  isRedTurn = false;
				  }
				  else {
					  //System.out.println("made true");
					  isRedTurn = true;
				  }
				  
				  //System.out.println(isRedTurn());
				  return true;
			  }
		  }
		  
		  return true;
	  }
  }
  
  public void gameMoves(int column, boolean undo) {
	  
	  if(!undo) {
		  //System.out.println(move_num);
		  //moves_played[move_num] = column;
		  move_num++;
	  }
	  else {
		  move_num--;
		 // moves_played[move_num] = 0;		  
	  }
  }
  
  public void printGameMoves() {
	  
	  System.out.println("moves_played");
	  for(int move:moves_played) {
		  System.out.print(move + ", ");
	  }
	  System.out.println("");
  }
  
  public int winner() {			//returns who the winner is
	  
	  if(isGameOver()) {
		  
		  if(isWinner() && isRedTurn())
			  return 1;
		  else if(isWinner() & !isRedTurn())
			  return -1;  
	  }  
	  return 0;
  }
  
  public boolean isWinner() {		//checks whether there is a winner
	  
	  for(int i=0;i<numCols;i++) {
		  for(int j = 0;j<numRows;j++) {
			  if(state[i][j] != 0 && (j+3<numRows && state[i][j] == state[i][j+1] && state[i][j] == state[i][j+2] && state[i][j] == state[i][j+3] 
					  || i+3 < numCols && state[i][j] == state[i+1][j] && state[i][j] == state[i+2][j] && state[i][j] == state[i+3][j]
					  || j+3<numRows &&  i+3 < numCols && state[i][j] == state[i+1][j+1] && state[i][j] == state[i+2][j+2] && state[i][j] == state[i+3][j+3]
					  ||  i+3 < numCols && j-3 >= 0 && state[i][j] == state[i+1][j-1] && state[i][j] == state[i+2][j-2] && state[i][j] == state[i+3][j-3])) 
				  	  
				  return true;
		  }  
	  }
	  return false;
  }
  
  public boolean isGameOver() {		//checks to see if the game is over
	  
	  if(isWinner())
		  return true;
	  
	  for(int k = 0;k<numCols;k++) {
		  if(state[k][numRows-1]==0)
			  return false;
	  }
      return true;
  }
  
  public int getNumRows(){   	//returns number of rows
	  return numRows;
  }

  public int getNumCols(){		//returns number of columns
   	 return numCols;
  }
 
  public boolean notFullColumn(int col) {		//checks to see whether the top column is full for play method
	 
	 if(state[col][numRows-1] != 0)
		 return false;
	 
	 return true;
  }
}
