package hw4;
//make it so bad move is involved with touches
import java.util.*;

public class TrialAI implements CFPlayer{
	
	private class moveFinder{		//inner class that doesn't actually play moves, but simulates moves to see if there is a winning one or one to prevent the opponent from winning
		
		CFGame g;
		private int [][] getState;
		private int opponent_num = 1;
		private int AI_color = -1;
		private boolean winning_move;
		private ArrayList<Integer> losing_moves = new ArrayList<>();		//losing move is considered move that will allow opponent to win on following turn
		private ArrayList<Integer> unwise_moves = new ArrayList<>();		//unwise move is considered move that will allow opponent to block AI's winning move on following turn
		private ArrayList<Integer> illegal_moves = new ArrayList<>();    //move that is illegal
		private ArrayList<Integer> AI_three_unblockable = new ArrayList<>();
		private ArrayList<Integer> opposing_three_unblockable = new ArrayList<>();
		private ArrayList<Integer> AI_three_blockable = new ArrayList<>();
		private ArrayList<Integer> opposing_three_blockable = new ArrayList<>();
		private ArrayList<Integer> row_stopper = new ArrayList<>();
		private ArrayList<Integer> three_preventer = new ArrayList<>();
		private ArrayList<Integer> winning_column= new ArrayList<>();
		private int future_winning_move;
		private int[][] three_potential_map;
		private int[][] four_potential_map;
		private int[][] initial_four_map;
		private boolean already_winning_column = false;
		private int num_touching;
		
		public moveFinder(CFGame g) {
			
			this.g = g;
			getState = g.getState();
			three_potential_map = new int[g.getNumCols()][g.getNumRows()];
			four_potential_map = new int[g.getNumCols()][g.getNumRows()];
			
			if(g.isRedTurn()) {
				opponent_num = -1;
				AI_color = 1;
			}  
			
			for(int i=0; i<g.getNumCols(); i++) {
				  for(int j=0;j<g.getNumRows(); j++) {
					  three_potential_map[i][j] = 0;
					  four_potential_map[i][j] = 0;
				  } 
			}
		}
		
		public boolean pretendPlay(int column, boolean opp_turn) {				//simulates playing specific column. opp_turn true if opponents turn
			
			winning_move = false;   //true if a winning move is present
			  
			if(column<0 || column>=g.getNumCols() || !g.notFullColumn(column)) { 	//if move cannot be made
				if(!illegal_moves.contains(column))
					illegal_moves.add(column);
				return false;
			}
			
			initial_four_map = fourMap();
			
			for(int row=g.getNumRows()-1; row>=0; row--) { 		//simulates move and what number will be played on move(1 or -1)

				if(getState[column][row] == 0) {	
						  
				    if((g.isRedTurn() && !opp_turn) || (!g.isRedTurn() && opp_turn))
					    getState[column][row] = 1;
				    else if((!g.isRedTurn() && !opp_turn) || (g.isRedTurn() && opp_turn)) 
					    getState[column][row] = -1;
				    
				    isMoveWinning(column, row);		//checks to see if winning or opponent winning move is available
				    threeInARow(column, row);		//checks to see if three out of four can be created
				    numberTouching(column, row);    //checks to see how many filled squares each potential move creates
				    if(!already_winning_column && !winning_move && (row==0 || (row>0 && getState[column][row-1]!=0)))
				    	winningColumnCreator(column, row);	
					
					getState[column][row] = 0;
				} 
			}  
			  
			avoidLoss();   //sees is a row can be won horizontally in two moves
			avoidAllowingThree();  //checks to see is a certain move would alow three out of four to be created
				   
			return true;
		}
		
		public void findWinningColumn() {
			
			four_potential_map = fourMap();
			
			for(int i=0; i<g.getNumCols(); i++) {    //looks at places on board where four in a row can be made and if two of same color on top of each other, indicates winning column
				for(int j=0;j<g.getNumRows(); j++) {
					if(j<g.getNumRows()-1 && four_potential_map[i][j]!=0 && four_potential_map[i][j+1]!=0 
							&& ((four_potential_map[i][j]==four_potential_map[i][j+1]) || (four_potential_map[i][j]==2 || four_potential_map[i][j+1]==2))) {
						
						already_winning_column = true;
						if(!winning_column.contains(i) && (four_potential_map[i][j]==AI_color || four_potential_map[i][j+1]==AI_color))
							winning_column.add(i);
					}
				}
			}
		}
  
		public void isMoveWinning(int column, int row) {	//sees whether there is a winning move
			  
			for(int i=0; i<g.getNumCols(); i++) {
				for(int j=0;j<g.getNumRows(); j++) {
					if(getState[i][j]!=0 && (j+3<g.getNumRows() && getState[i][j]==getState[i][j+1] && getState[i][j]==getState[i][j+2] && getState[i][j]==getState[i][j+3] 
					   || i+3<g.getNumCols() && getState[i][j]==getState[i+1][j] && getState[i][j]==getState[i+2][j] && getState[i][j]==getState[i+3][j]
					   || j+3<g.getNumRows() &&  i+3<g.getNumCols() && getState[i][j]==getState[i+1][j+1] && getState[i][j]==getState[i+2][j+2] && getState[i][j]==getState[i+3][j+3]
					   || i+3<g.getNumCols() && j-3>=0 && getState[i][j]==getState[i+1][j-1] && getState[i][j]==getState[i+2][j-2] && getState[i][j]==getState[i+3][j-3])) {
						  
						if((row>0 && getState[column][row-1] != 0)||row==0){
							winning_move = true;
						}
						else if((row==1 && getState[column][row-1]==0) ||(row>1 && getState[column][row-1]==0 && getState[column][row-2] !=0)) {  //checks to see whether a move 2 moves from now can win, and avoids it
					    	if((getState[column][row]==1 && g.isRedTurn() || getState[column][row]==-1 && !g.isRedTurn())) 
					    		unwise_moves.add(column);
							else if(((getState[column][row]==-1 && g.isRedTurn() || getState[column][row]==1 && !g.isRedTurn()))) 
								losing_moves.add(column);						
						}
					}
				}  
			}
		}
		
		public int[][] fourMap() {	//sees whether there is a winning move
			
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
									if(getState[col][row]!=0 && (row+3<g.getNumRows() && getState[col][row]==getState[col][row+1] && getState[col][row]==getState[col][row+2] && getState[col][row]==getState[col][row+3] 
									   || col+3<g.getNumCols() && getState[col][row]==getState[col+1][row] && getState[col][row]==getState[col+2][row] && getState[col][row]==getState[col+3][row]
									   || row+3<g.getNumRows() &&  col+3<g.getNumCols() && getState[col][row]==getState[col+1][row+1] && getState[col][row]==getState[col+2][row+2] && getState[col][row]==getState[col+3][row+3]
									   || row-3>=0 && col+3<g.getNumCols() && getState[col][row]==getState[col+1][row-1] && getState[col][row]==getState[col+2][row-2] && getState[col][row]==getState[col+3][row-3])) {

										  if(temp_array[i][j]==move)
											  continue;
										  else if(temp_array[i][j]==0)
									    	  temp_array[i][j] = move;
										  else if(temp_array[i][j]!=move) 
											  temp_array[i][j] = 2;
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

		public void avoidLoss() {	//sees whether a row be guaranteed 4 in a row in 2 turns
			  
			for(int i=0; i<g.getNumCols()-4; i++) {
				for(int j=0;j<g.getNumRows(); j++) {
					if(getState[i+2][j]!=0 && ((j==0 && (getState[i][j]==0 && getState[i+1][j]==0 && getState[i+2][j]==getState[i+3][j] && getState[i+4][j]==0) 
					   || (j>0 && (getState[i][j-1]!=0 && getState[i+1][j-1]!=0 && getState[i+4][j-1]!=0 && getState[i][j]==0 && getState[i+1][j]==0 && getState[i+2][j]==getState[i+3][j] && getState[i+4][j]==0))))) {
						
						//System.out.println("a");
						if(!row_stopper.contains(i+1))
							row_stopper.add(i+1);
					}
					else if(getState[i+2][j]!=0 && ((j==0 && getState[i][j]==0 && getState[i+1][j]==getState[i+2][j] && getState[i+3][j]==0 && getState[i+4][j]==0)
						    || (j>0 && getState[i][j-1]!=0 && getState[i+3][j-1]!=0 && getState[i+4][j-1]!=0 && getState[i][j]==0 && getState[i+1][j]==getState[i+2][j] && getState[i+3][j]==0 && getState[i+4][j]==0))) {
						
						//System.out.println("b");
						if(!row_stopper.contains(i+3))
							row_stopper.add(i+3);
					}
					
					if((i<g.getNumCols()-3 && j<g.getNumRows()-3 && getState[i][j]==0 && (j==0 ||getState[i][j-1]!=0) && getState[i+1][j+1]!=0 && getState[i+1][j+1]==getState[i+2][j+2] 
						&& getState[i+3][j+3]==0 && getState[i+3][j+2]!=0 && ((j==0 && i<g.getNumCols()-4 && getState[i+4][j+4]==0 && getState[i+4][j+3]!=0) 
						|| (i>0 && j==1 && getState[i-1][j-1]==0) || (i>0 && j>1 && getState[i-1][j-1]==0 && getState[i-1][j-2]!=0)
						|| (i<g.getNumCols()-4 && j<g.getNumRows()-4 && getState[i+4][j+4]==0 && getState[i+4][j+3]!=0)))){
							
						if(!row_stopper.contains(i))
							row_stopper.add(i);
						else if(!row_stopper.contains(i+3))
							row_stopper.add(i+3);
						//System.out.println("c");
					}
								
					if(i>1 && i<g.getNumCols()-2 && j>1 && j<g.getNumRows()-2 && getState[i][j]==0 && getState[i][j-1]!=0 && getState[i-1][j-1]!=0 && getState[i-1][j-1]==getState[i+1][j+1] 
					   && getState[i+2][j+2]==0 && getState[i+2][j+1]!=0 && ((j==2 && getState[i-2][j-2]==0) || (j>2 && getState[i-2][j-2]==0 && getState[i-2][j-3]!=0))){
							
						if(!row_stopper.contains(i))
							row_stopper.add(i);
						//System.out.println("d");	
					}
					
					if((j>2 && i<g.getNumCols()-3 && getState[i][j]==0 && getState[i][j-1]!=0 && getState[i+1][j-1]!=0 && getState[i+1][j-1]==getState[i+2][j-2] && getState[i+3][j-3]==0 && (j==3 || j>3 && getState[i+3][j-4]!=0))
						&& ((i<g.getNumCols()-4 && (j==4 && getState[i+4][j-4]==0 || j>4 && getState[i+4][j-4]==0 && getState[i+4][j-5]!=0)) ||(i>0 && j<g.getNumRows()-1 && getState[i-1][j+1]==0 && getState[i-1][j]!=0))){
							
						if(!row_stopper.contains(i))
							row_stopper.add(i);
						else if(!row_stopper.contains(i+3))
							row_stopper.add(i+3);
							
						//System.out.println("e");
					}
					
					if(i>1 && i<g.getNumCols()-2 && j>1 && j<g.getNumRows()-2 && getState[i][j]==0 && getState[i][j-1]!=0 && getState[i-1][j+1]!=0 && getState[i-1][j+1]==getState[i+1][j-1]
					   && getState[i-2][j+2]==0 && getState[i-2][j+1]!=0 && ((j==2 && getState[i+2][j-2]==0) || j>2 && getState[i+2][j-2]==0 && getState[i+2][j-3]!=0)) {
						
						if(!row_stopper.contains(i))
							row_stopper.add(i);
						//System.out.println("f");
					}
								

				}
			}  
		}
		
		public void winningColumnCreator(int column, int row) {
			
			future_winning_move = -1;
			int[][] four_map = fourMap();

			for(int i=0; i<g.getNumCols(); i++) {
				for(int j=0; j<g.getNumRows()-1; j++) {
					if(four_map[i][j]!=0 && four_map[i][j+1]!=0 && (four_map[i][j]==four_map[i][j+1] ||  four_map[i][j+1]==2)) {

						if(!winning_column.contains(column))
							winning_column.add(column);
					}	
				}					    
			}						
		}
		
		public void threeInARow(int column, int row) {	      //sees whether there is an opportunity to make three in a row or block opponent from doing so
			  
			ArrayList<Integer> three_unblockable = new ArrayList<>();    ////arraylist thats hold columns that make three in a row that aren't blockable
			ArrayList<Integer> three_blockable = new ArrayList<>();    ////arraylist thats hold columns that make three in a row that are blockable
			
			for(int i=0; i<g.getNumCols(); i++) {
				for(int j=0;j<g.getNumRows(); j++) {
					if(getState[i][j]!=0 && j<g.getNumRows()-3 && i==column && (j+2==row) && getState[i][j]==getState[i][j+1]    //looks for three vertically
					   && getState[i][j]==getState[i][j+2] && getState[i][j+3]==0) {
						
						three_blockable.add(column);
						
						if(three_potential_map[column][row]==0)
							three_potential_map[column][row] = getState[i][j];
						else if(three_potential_map[column][row]!=getState[i][j])
							three_potential_map[column][row] = 2;
					}
					
					if(getState[i][j]!=0 && i<g.getNumCols()-2 && (i==column || i+1==column || i+2==column) && j==row && getState[i][j]==getState[i+1][j]   //looks for three connected horizontally
					   && getState[i][j]==getState[i+2][j] && (i<g.getNumCols()-3 && getState[i+3][j]==0 || i>0 && getState[i-1][j]==0)) {
						
						if(j>0 && i>0 && i<g.getNumCols()-3 && (getState[i-1][j-1]==0 || getState[i+3][j-1]==0)) 	
							three_unblockable.add(column);
						else 
							three_blockable.add(column);
						
						if(three_potential_map[column][row]==0)
							three_potential_map[column][row] = getState[i][j];
						else if(three_potential_map[column][row]!=getState[i][j])
							three_potential_map[column][row] = 2;
				    }
					
					if(getState[i][j]!=0 && i<g.getNumCols()-3 && initial_four_map[i+1][j]!=getState[i][j] && initial_four_map[i+1][j]!=2 && (i==column || i+2==column || i+3==column) &&  j==row && getState[i][j]==getState[i+2][j]   //looks for three out of four horizontally with middle left empty
					   && getState[i][j]==getState[i+3][j] && getState[i+1][j]==0 && (i==0 || i>2 || ((i==1 || i==2 ) && getState[i-1][j]==getState[i+3][j]))) {		
						
						if(j>0 && getState[i+1][j-1]==0)
							three_unblockable.add(column);
						else 
							three_blockable.add(column);						

						if(three_potential_map[column][row]==0)
							three_potential_map[column][row] = getState[i][j];
						else if(three_potential_map[column][row]!=getState[i][j])
							three_potential_map[column][row] = 2;
					}
					
					if(getState[i][j]!=0 && i<g.getNumCols()-3 && initial_four_map[i+2][j]!=getState[i][j] && initial_four_map[i+2][j]!=2 && (i==column || i+1==column || i+3==column) && j==row && getState[i][j]==getState[i+1][j] 
					   && getState[i][j]==getState[i+3][j] && getState[i+2][j]==0 && (i==0 || i>2 || ((i==1 || i==2 ) && getState[i][j]==getState[i+4][j]))) {    //looks for three out of four horizontally with middle right empty
						
						if(j>0 && getState[i+2][j-1]==0) 
							three_unblockable.add(column);
						else 
							three_blockable.add(column);
						
						if(three_potential_map[column][row]==0)
							three_potential_map[column][row] = getState[i][j];
						else if(three_potential_map[column][row]!=getState[i][j])
							three_potential_map[column][row] = 2;
					}
	
				    if(getState[i][j]!=0 && i<g.getNumCols()-2 && j<g.getNumRows()-2 && (i==column && j==row || i+1==column && j+1==row || i+2==column && j+2==row)  //looks for three in a row in upper right diagonal
				       && getState[i][j]==getState[i+1][j+1] && getState[i][j]==getState[i+2][j+2] && (j<g.getNumRows()-3 &&  i<g.getNumCols()-3 
				       && getState[i+3][j+3]==0 || j>1 && i>1 && getState[i-1][j-1]==0)) {   
				    	
				    	if((i<g.getNumCols()-3 && j<g.getNumRows()-3 && getState[i+3][j+2]==0) || (j==2 && i<g.getNumCols()-3 
				    		&& ((i>0 && getState[i-1][j-2]==0) || getState[i+3][j+2]==0)) || (i==g.getNumCols()-3 && j>1 && getState[i-1][j-2]==0) 
				    		|| (j==g.getNumRows()-3 && getState[i-1][j-2]==0)) 
				    		
				    		three_unblockable.add(column);
				    	else 
				    		three_blockable.add(column);
				    	
				    	if(three_potential_map[column][row]==0)
							three_potential_map[column][row] = getState[i][j];
						else if(three_potential_map[column][row]!=getState[i][j])
							three_potential_map[column][row] = 2;
				    }
				    
				    if(getState[i][j]!=0 && i<g.getNumCols()-3 && j<g.getNumRows()-3 && initial_four_map[i+1][j+1]!=getState[i][j] && initial_four_map[i+1][j+1]!=2   //looks for 3/4 diagonally middle left empty
				    		&& (i==column && j==row || i+2==column && j+2==row || i+3==column && j+3==row)&& getState[i][j]==getState[i+2][j+2] && getState[i][j]==getState[i+3][j+3] && getState[i+1][j+1]==0) {   
				    	
				    	if(getState[i+1][j]==0)
				    		three_unblockable.add(column);
				    	else 
				    		three_blockable.add(column);
				    	
				    	if(three_potential_map[column][row]==0)
							three_potential_map[column][row] = getState[i][j];
						else if(three_potential_map[column][row]!=getState[i][j])
							three_potential_map[column][row] = 2;
				    }
				    
				    if(getState[i][j]!=0 && i<g.getNumCols()-3 && j<g.getNumRows()-3 && initial_four_map[i+2][j+2]!=getState[i][j] && initial_four_map[i+2][j+2]!=2   //looks for 3/4 diagonally middle right empty
				       && (i==column && j==row || i+1==column && j+1==row || i+3==column && j+3==row) && getState[i][j]==getState[i+1][j+1] && getState[i][j]==getState[i+3][j+3] && getState[i+2][j+2]==0) {
				    	
				    	if(getState[i+2][j+1]==0) 
				    		three_unblockable.add(column);
				    	else 
				    		three_blockable.add(column);
				    	
				    	if(three_potential_map[column][row]==0)
							three_potential_map[column][row] = getState[i][j];
						else if(three_potential_map[column][row]!=getState[i][j])
							three_potential_map[column][row] = 2;
				    }
				    
				    if(getState[i][j]!=0 && i<g.getNumCols()-2 && j>=2 && (i==column && j==row || i+1==column && j-1==row || i+2==column && j-2==row) 
				       && getState[i][j]==getState[i+1][j-1] && getState[i][j]==getState[i+2][j-2] && (i>1 && j<g.getNumRows()-1 
				       && getState[i-1][j+1]==0 || i<g.getNumCols()-3 && j>2 && getState[i+3][j-3]==0)) {   //looks for three in a row in lower right diagonal
				    	
				    	if(((j==3 || j==2) && i>0 && getState[i-1][j]==0) || (i>0 && i<g.getNumCols()-3 && j==4 && (getState[i-1][j]==0 || getState[i+3][j-4]==0)) 
				    		|| (i==0 && j>3 && getState[i+3][j-4]==0) || (j==g.getNumCols()-1 && getState[i+3][j-4]==0)) 
				    		
				    		three_unblockable.add(column);
				    	else 
				    		three_blockable.add(column);
				    	
				    	if(three_potential_map[column][row]==0)
							three_potential_map[column][row] = getState[i][j];
						else if(three_potential_map[column][row]!=getState[i][j])
							three_potential_map[column][row] = 2;
				    }
				    
				    if(getState[i][j]!=0 && i<g.getNumCols()-3 && j>2 && initial_four_map[i+1][j-1]!=getState[i][j] && initial_four_map[i+1][j-1]!=2   //looks for 3/4 diagonally middle left empty
				       && (i==column && j==row || i+2==column && j-2==row || i+3==column && j-3==row) && getState[i][j]==getState[i+2][j-2] && getState[i][j]==getState[i+3][j-3] && getState[i+1][j-1]==0) {  
				    	
				    	if(getState[i+1][j-2]==0) 
				    		three_unblockable.add(column);
				    	else 
				    		three_blockable.add(column);
				    	
				    	if(three_potential_map[column][row]==0)
							three_potential_map[column][row] = getState[i][j];
						else if(three_potential_map[column][row]!=getState[i][j])
							three_potential_map[column][row] = 2;
				    }
				    
				    if(getState[i][j]!=0 && i<g.getNumCols()-3 && j>2 && initial_four_map[i+2][j-2]!=getState[i][j] && initial_four_map[i+2][j-2]!=2   //looks for 3/4 diagonally middle right empty
				       && (i==column && j==row || i+1==column && j-1==row || i+3==column && j-3==row) && getState[i][j]==getState[i+1][j-1] && getState[i][j]==getState[i+3][j-3] && getState[i+2][j-2]==0) {  
				    	
				    	if(getState[i+2][j-3]==0) 
				    		three_unblockable.add(column);
				    	else 
				    		three_blockable.add(column);
				    	
				    	if(three_potential_map[column][row]==0)
							three_potential_map[column][row] = getState[i][j];
						else if(three_potential_map[column][row]!=getState[i][j])
							three_potential_map[column][row] = 2;
				    }
			    }  
			}
		
			if((row>0 && getState[column][row-1] != 0)||row==0) {
		    	if((getState[column][row]==1 && g.isRedTurn() || getState[column][row]==-1 && !g.isRedTurn())) { 
		    		AI_three_unblockable.addAll(three_unblockable);
		    		AI_three_blockable.addAll(three_blockable);
		    	}
		    
		    	else if(((getState[column][row]==-1 && g.isRedTurn() || getState[column][row]==1 && !g.isRedTurn()))) { 
		    		opposing_three_unblockable.addAll(three_unblockable);
		    		opposing_three_blockable.addAll(three_blockable);
		    	}
		    }	
		}
		
		public void numberTouching(int column, int row) {	//counts number of filled squares are adjacent to simulated move
			num_touching = 0;
			
			if(row == 0) {
				if(column == 0) {
					if(getState[column+1][row+1]!=0) 
						num_touching++;
					
					if(getState[column+1][row]!=0) 
						num_touching++;
				}
				else if(column==g.getNumCols()-1){
					if(getState[column-1][row+1]!=0) 
						num_touching++;
					
					if(getState[column-1][row]!=0) 
						num_touching++;
				}	
				else {
					if(getState[column-1][row]!=0) 
						num_touching++;
					
					if(getState[column-1][row+1]!=0) 
						num_touching++;
					if(getState[column+1][row]!=0) 
						num_touching++;
					
					if(getState[column+1][row+1]!=0) 
						num_touching++;
				}
			}
			else if(row==g.getNumRows()-1) {
				num_touching++;    //if row>0, there will always be at least one filled square below simulated play
				
				if(column==0) {
					if(getState[column+1][row]!=0) 
						num_touching++;
					
					if(getState[column+1][row-1]!=0) 
						num_touching++;
				}
				else if(column==g.getNumCols()-1){
					if(getState[column-1][row]!=0) 
						num_touching++;
					
					if(getState[column-1][row-1]!=0) 
						num_touching++;
				}	
				else {
					if(getState[column-1][row]!=0) 
						num_touching++;
					
					if(getState[column-1][row-1]!=0) 
						num_touching++;
					
					if(getState[column+1][row]!=0) 
						num_touching++;
					
					if(getState[column+1][row-1]!=0) 
						num_touching++;
				}
			}
			else {
				num_touching++;    //if row>0, there will always be at least one filled square below simulated play
				
				if(column==0) {
					if(getState[column+1][row+1]!=0) 
						num_touching++;
					
					if(getState[column+1][row]!=0) 
						num_touching++;
					
					if(getState[column][row-1]!=0) 
						num_touching++;
				}
				else if(column==g.getNumCols()-1){
					if(getState[column-1][row+1]!=0) 
						num_touching++;
					
					if(getState[column-1][row]!=0) 
						num_touching++;
					
					if(getState[column-1][row-1]!=0) 
						num_touching++;
				}	
				else {
					if(getState[column-1][row+1]!=0) 
						num_touching++;
					
					if(getState[column-1][row]!=0) 
						num_touching++;
					
					if(getState[column-1][row-1]!=0) 
						num_touching++;
					
					if(getState[column+1][row+1]!=0) 
						num_touching++;
					
					if(getState[column+1][row]!=0) 
						num_touching++;
					
					if(getState[column+1][row-1]!=0) 
						num_touching++;
				}
			}
		}
	
		public void avoidAllowingThree() {
			
			for(int i=0; i<g.getNumCols(); i++) {
				for(int j=0;j<g.getNumRows(); j++) {
					  if(!three_preventer.contains(i) && three_potential_map[i][j]!=0 && ((j>1 && getState[i][j-1]==0 && getState[i][j-2] != 0) || (j==1 && getState[i][j-1]==0)))
						  	three_preventer.add(i);
				}
			}
		}
	}
	
	public int nextMove(CFGame g) {			//plays the next move with sound logic
		
		moveFinder m = new moveFinder(g);
		
		int[] quality_array = {0,1,2,3,2,1,0}; 		//Assigning starting values to quality array that skew towards the center. This array will  determine which col to play

		m.findWinningColumn();      //Looks to see if there is a column that can be played that guarantees win

		for(int col=0; col<g.getNumCols(); col++) {   //plays winning move if possible
			if(m.pretendPlay(col, false)) {
				if(m.winning_move) 
					return col;	
			}		
		}
		
		for(int col=0; col<g.getNumCols(); col++) {  //blocks opponent winning move if possible
			if(m.pretendPlay(col, true)) {
				if(m.winning_move) 
					return col;	
				//quality_array[col] += m.num_touching;
			}
		}
		
		return max_element_array(quality_array, m.illegal_moves, m.losing_moves, m.unwise_moves, m.row_stopper, m.AI_three_unblockable, 
				        m.opposing_three_unblockable, m.AI_three_blockable, m.opposing_three_blockable, m.winning_column, m.three_preventer, m.four_potential_map);
	}
	
	public int max_element_array(int[] quality_arr, ArrayList<Integer> illegal_moves, ArrayList<Integer> losing_moves, ArrayList<Integer> unwise_moves, ArrayList<Integer> row_stopper, 
			ArrayList<Integer> AI_three_unblockable, ArrayList<Integer> opposing_three_unblockable, ArrayList<Integer> AI_three_blockable, ArrayList<Integer> opposing_three_blockable, 
			ArrayList<Integer> winning_column, ArrayList<Integer> three_preventable, int[][] four_potential_map) {
		int max = -1000000;
		int max_element = 0;
		//System.out.println("losing moves " + losing_moves);
		//System.out.println("unwise moves " + unwise_moves);
		//System.out.println("illegal moves " + illegal_moves);
		//System.out.println("ai  unblockable " + AI_three_unblockable);
		//System.out.println("opp unblockable " + opposing_three_unblockable);
		//System.out.println("ai blockable " + AI_three_blockable);
		//System.out.println("opp blockable " + opposing_three_blockable);
		//System.out.println("winning column " + winning_column);
		//System.out.println("row stopper " + row_stopper);
		int illegal = -10000;
		int losing = -1000;
		int unwise = -150;
		int row_stop = 500;
		int block_opponent_three = -5;
		int three_grouped_unblockable = 25;
		int opponent_three_unblockable  = 18;
		int three_grouped_blockable = 5;
		int opponent_three_blockable  = 4;
		int great_column = 400;
		
		
		ArrayList<Integer> duplicate_max = new ArrayList<>();
		
		for(int index:illegal_moves) {		//Setting values in arr to arbitrary negative value if move is illegal
			quality_arr[index] = illegal;
		}
		for(int index:losing_moves) {		//Setting values in arr to negative value if move is losing
			quality_arr[index] = losing;
		}
		for(int index:unwise_moves) {		//Setting values in arr to negative value if move is unwise. Still greater than losing since this move is better
			quality_arr[index] = unwise;
		}
		for(int index:row_stopper) {		//If a row can be "stopped", a lot of value is added to that column that "stops" it
			quality_arr[index] += row_stop;
		}
		for(int index:AI_three_unblockable) {		//Adding value to making three in a row
			quality_arr[index] += three_grouped_unblockable;
		}
		for(int index:opposing_three_unblockable) {		//Adding value to blocking three in a row
			quality_arr[index] += opponent_three_unblockable;
		}
		for(int index:AI_three_blockable) {		//Adding value to making three in a row
			quality_arr[index] += three_grouped_blockable;
		}
		for(int index:opposing_three_blockable) {		//Adding value to blocking three in a row
			quality_arr[index] += opponent_three_blockable;
		}
		for(int index:three_preventable) {		//Adding value to blocking three in a row
			quality_arr[index] += block_opponent_three;
		}
		for(int index:winning_column) {		//Adding value to a winning column
			quality_arr[index] += great_column;
		}
		
		for(int c=5; c>=0; c--) {
			for(int r=0; r<7; r++) {
				
				//if(four_potential_map[r][c]==-1)
					//System.out.print(" " + four_potential_map[r][c]);
				//else
					//System.out.print( "  " + four_potential_map[r][c]);
			}
			//System.out.println("");
		}
		//System.out.println("");

		
		for(int i=0; i<quality_arr.length; i++) { 
			//System.out.print(quality_arr[i] + " ");
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
		//System.out.println("");
		//System.out.println("");
		
		if(duplicate_max.size()>0) {
			Random rand = new Random();
			int r = rand.nextInt(duplicate_max.size());
			
			return duplicate_max.get(r);
		}
		return max_element;
	}
	

	public String getName() {
		
		return "Trial AI";
	}
	
}