package hw4;

public class CFGame {
	
  //state[i][j]= 0 means the i,j slot is empty
  //state[i][j]= 1 means the i,j slot has red
  //state[i][j]=-1 means the i,j slot has black
  private final int[][] state;

  private boolean isRedTurn;		
  boolean played;			
  private int numRows = 6;
  private int numCols = 7;
  
  {
    state = new int[numCols][numRows];
    for (int i=0; i<numCols; i++)
      for (int j=0; j<numRows; j++)
        state[i][j] = 0;
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
	  
	  if(column < 1 || column > numCols || !notFullColumn(column)) 
		  return false;
	  
		  for(int i = 0;i<numRows;i++) {
				  if(state[column-1][i] == 0) {	
					  if(isRedTurn) { 
						  state[column-1][i] = 1;
						  played = true;
						  isRedTurn = false;
					  }
					  else if(!isRedTurn && !played) {
						  state[column-1][i] = -1;
						  isRedTurn = true;
						  played = true;
					  }
					  return true;
				  }  
		  }
		  return true;
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
		  if(state[k][numRows-1]== 0)
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
 
  public boolean notFullColumn(int c) {		//checks to see whether the top column is full for play method
	 
	 if(state[c-1][numRows-1] != 0)
		 return false;
	 return true;
  }
}
