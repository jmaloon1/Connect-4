package hw4;
//make it so bad move is involved with touches
import java.util.*;

public class JackMaloonAI implements CFPlayer{
	
	private class moveFinder{		//inner class that doesn't actually play moves, but simulates moves to see if there is a winning one or one to prevent the opponent from winning
		
		CFGame g;
		private int [][] getState;
		private int opponent_num = 1;
		private int AI_color = -1;
		private boolean winningMove;
		private boolean played;
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
		private boolean already_winning_column = false;
		

		private int adjacent_touches = 0;
		
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
			
			four_potential_map = fourMap();
			
			for(int i=0; i<g.getNumCols(); i++) {    //looks at places on board where four in a row can be made and if two of same color on top of each other, indicates winning column
				for(int j=0;j<g.getNumRows(); j++) {
					if(j<g.getNumRows()-1 && four_potential_map[i][j]!=0 && four_potential_map[i][j+1]!=0 
							&& ((four_potential_map[i][j]==four_potential_map[i][j+1]) || (four_potential_map[i][j]==2 || four_potential_map[i][j+1]==2))) {
						
						for(int c=5; c>=0; c--) {
							for(int r=0; r<g.getNumCols(); r++) {
								//System.out.print(four_potential_map[r][c]);
							}
							//System.out.println("");
						}
						//System.out.println("");
						
						already_winning_column = true;
						if(!winning_column.contains(i) && (four_potential_map[i][j]==AI_color || four_potential_map[i][j+1]==AI_color))
							winning_column.add(i);
					}
					  
				}
			}
			  
			for(int row=g.getNumRows()-1; row>=0; row--) { 		//simulates move and what number will be played on move(1 or -1)
				played = false;			

				if(getState[column][row] == 0) {	
						  
				    if(g.isRedTurn() && !opp_turn) { 
					    getState[column][row] = 1;
					    played = true;
				    }
				    else if(!g.isRedTurn() && !opp_turn && !played) {
					    getState[column][row] = -1;
					    played = true;
				    }
				    else if(g.isRedTurn() && opp_turn && !played) { 
					    getState[column][row] = -1;
					    played = true;
				    }
				    else if(!g.isRedTurn() && opp_turn && !played) {
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
				    		int[] unblockable_temp = threeInARow(column, row).get("u");
				    		for(int num:unblockable_temp) {
				    			AI_three_unblockable.add(num);
				    		}
				    		
				    		int[] blockable_temp = threeInARow(column, row).get("b");
				    		for(int num:blockable_temp) {
				    			AI_three_blockable.add(num);
				    		}
				    	}
				    
				    	else if(((getState[column][row]==-1 && g.isRedTurn() || getState[column][row]==1 && !g.isRedTurn()))) { 
				    		int[] unblockable_temp = threeInARow(column, row).get("u");
				    		for(int num:unblockable_temp) {
				    			opposing_three_unblockable.add(num);
				    		}
				    		
				    		int[] blockable_temp = threeInARow(column, row).get("b");
				    		for(int num:blockable_temp) {
				    			opposing_three_blockable.add(num);
				    		}
				    	}
				    }
				    if(threeSquares(column, row, three_potential_map)[2] != 0)
				    	three_potential_map[threeSquares(column, row, three_potential_map)[0]][threeSquares(column, row, three_potential_map)[1]] = threeSquares(column, row, three_potential_map)[2];
				    
				  
					adjacent_touches = numberTouching(column, row);
					
	
					if(row==0 || (row>0 && getState[column][row-1]!=0)) {
					    if(!already_winning_column && wm(column)) {
					    	if(!winning_column.contains(column)) {
					    		//System.out.println("ttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttt" + future_winning_move); 	
					    		winning_column.add(column);
					    	}
					    }
					}
					
					
					getState[column][row] = 0;
				} 
			}  
			  
			row_stopper = isRowWinnable();
	
			for(int i=0; i<g.getNumCols(); i++) {
				for(int j=0;j<g.getNumRows(); j++) {
					  if((three_potential_map[i][j]==opponent_num || three_potential_map[i][j]==2) && ((j>1 && getState[i][j-1]==0 && getState[i][j-2] != 0) || (j==1 && getState[i][j-1]==0)))
						  	three_preventer.add(i);
				}
			}
				   
			  return true;
		}
			  
		
		public boolean isMoveWinning() {	//sees whether there is a winning move
			  
			for(int i=0; i<g.getNumCols(); i++) {
				for(int j=0;j<g.getNumRows(); j++) {
					if(getState[i][j]!=0 && (j+3<g.getNumRows() && getState[i][j]==getState[i][j+1] && getState[i][j]==getState[i][j+2] && getState[i][j]==getState[i][j+3] 
						 || i+3<g.getNumCols() && getState[i][j]==getState[i+1][j] && getState[i][j]==getState[i+2][j] && getState[i][j]==getState[i+3][j]
						 || j+3<g.getNumRows() &&  i+3<g.getNumCols() && getState[i][j]==getState[i+1][j+1] && getState[i][j]==getState[i+2][j+2] && getState[i][j]==getState[i+3][j+3]
						 || i+3<g.getNumCols() && j-3>=0 && getState[i][j]==getState[i+1][j-1] && getState[i][j]==getState[i+2][j-2] && getState[i][j]==getState[i+3][j-3])) {
						  
						  return true;
					}
				}  
			}
			  return false;
		}
		
		public int[] fourSquares(int col, int row, int[][] arr) {	//sees whether there is a winning move
			
			int[] index_holder = new int[3];
			index_holder[0]=0;
			index_holder[1]=0;
			index_holder[2]=0;
			
			for(int i=0; i<g.getNumCols(); i++) {
				  for(int j=0;j<g.getNumRows(); j++) {
					  if(getState[i][j]!=0 && (j+3<g.getNumRows() && getState[i][j]==getState[i][j+1] && getState[i][j]==getState[i][j+2] && getState[i][j]==getState[i][j+3] 
						 || i+3<g.getNumCols() && getState[i][j]==getState[i+1][j] && getState[i][j]==getState[i+2][j] && getState[i][j]==getState[i+3][j]
						 || j+3<g.getNumRows() &&  i+3<g.getNumCols() && getState[i][j]==getState[i+1][j+1] && getState[i][j]==getState[i+2][j+2] && getState[i][j]==getState[i+3][j+3]
						 || i+3<g.getNumCols() && j-3>=0 && getState[i][j]==getState[i+1][j-1] && getState[i][j]==getState[i+2][j-2] && getState[i][j]==getState[i+3][j-3])) {
						  
						  index_holder[0] = col;
						  index_holder[1] = row;
					      if(arr[col][row] != 0) {
					    	  if(arr[col][row]==getState[col][row])
					    		  index_holder[2] = getState[col][row];
					    	  else
					    		  index_holder[2] = 2;
					      }
							else
								index_holder[2]= getState[col][row];
					  }
				  }  
			  }
			  return index_holder;
		}
		
		public int[][] fourMap() {	//sees whether there is a winning move
			
	
			int[] fe = {-1,1};
			int[][] a = new int[g.getNumCols()][g.getNumRows()];
			
			for(int i=0; i<g.getNumCols(); i++) {
				  for(int j=0;j<g.getNumRows(); j++) {
					  a[i][j] = 0;
				  }
			}
			
			for(int i=0; i<g.getNumCols(); i++) {
				for(int j=0;j<g.getNumRows(); j++) {
					for(int move:fe) {
						if(getState[i][j]==0) {
			
							getState[i][j]=move;
							
							for(int col=0; col<g.getNumCols(); col++) {
								for(int row=0; row<g.getNumRows(); row++) {	
									
									  if(getState[col][row]!=0 && (row+3<g.getNumRows() && getState[col][row]==getState[col][row+1] && getState[col][row]==getState[col][row+2] && getState[col][row]==getState[col][row+3] 
										 || col+3<g.getNumCols() && getState[col][row]==getState[col+1][row] && getState[col][row]==getState[col+2][row] && getState[col][row]==getState[col+3][row]
										 || row+3<g.getNumRows() &&  col+3<g.getNumCols() && getState[col][row]==getState[col+1][row+1] && getState[col][row]==getState[col+2][row+2] && getState[col][row]==getState[col+3][row+3]
										 || row-3>=0 && col+3<g.getNumCols() && getState[col][row]==getState[col+1][row-1] && getState[col][row]==getState[col+2][row-2] && getState[col][row]==getState[col+3][row-3])) {
										  
									      if(a[i][j] != 0) {
									    	  a[i][j] = 2;
									      }
											else
												a[i][j] = move;
									  }
								}
							}
							    		
							getState[i][j] = 0;
						  }
					  }
				  }  
			  }
			  return a;
		}

		
		public ArrayList<Integer> isRowWinnable() {	//sees whether a row be guaranteed 4 in a row in 2 turns
			  
			ArrayList<Integer> rw = new ArrayList<>();    //arraylist thats hold columns that should be played

			
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
		
		public boolean wm(int column) {
			
			future_winning_move = -1;
			
			for(int i=0; i<g.getNumCols(); i++) {
				for(int j=0; j<g.getNumRows(); j++) {
					if(getState[i][j]==0 && (j==0||(j>1 && getState[i][j-1]!=0))){

					    int[][] temp = fourMap();		 

					    for(int col=0; col<g.getNumCols(); col++) {
							for(int row=0; row<g.getNumRows(); row++) {
								
								if(row<g.getNumRows()-1 && temp[col][row]!=0 && temp[col][row+1]!=0 
										&& ((temp[col][row]==temp[col][row+1]) || (temp[col][row]==2 || temp[col][row+1]==2))) {

				    				future_winning_move = column;
				    				return true;
								}	
							}
					    }
					    
					    
					}						
				}
			}
			return false;
		}
		
		public HashMap<String, int[]> threeInARow(int col, int row) {	      //sees whether there is an opportunity to make three in a row or block opponent from doing so
			  
			ArrayList<Integer> three_unblockable = new ArrayList<>();    ////arraylist thats hold columns that make three in a row that aren't blockable
			ArrayList<Integer> three_blockable = new ArrayList<>();    ////arraylist thats hold columns that make three in a row that are blockable
			int[] unblockable_arr;
			int[] blockable_arr;
			HashMap<String, int[]> hm = new HashMap<>();

			
			for(int i=0; i<g.getNumCols(); i++) {
				for(int j=0;j<g.getNumRows(); j++) {
					if(getState[i][j]!=0 && j<g.getNumRows()-3 && i==col && (j+2==row) && getState[i][j]==getState[i][j+1]    //looks for three vertically
							&& getState[i][j]==getState[i][j+2] && getState[i][j+3]==0) {
						three_blockable.add(col);
					}
					if(getState[i][j]!=0 && i<g.getNumCols()-2 && (i==col || i+1==col || i+2==col) && j==row   //looks for three connected horizontally
							&& getState[i][j]==getState[i+1][j] && getState[i][j]==getState[i+2][j] 
							&& (i<g.getNumCols()-3 && getState[i+3][j]==0 || i>0 && getState[i-1][j]==0)) {
						if(j>0 && i>0 && i<g.getNumCols()-3 && (getState[i-1][j-1]==0 || getState[i+3][j-1]==0)) {
							three_unblockable.add(col);
						}
						else {
							three_blockable.add(col);
						}
				    }
					
					if(getState[i][j]!=0 && i<g.getNumCols()-3 && (i==col || i+2==col || i+3==col) &&  j==row && getState[i][j]==getState[i+2][j] 
							&& getState[i][j]==getState[i+3][j] && getState[i+1][j]==0) {		//looks for three out of four horizontally with middle left empty
						if(j>0 && getState[i+1][j-1]==0) {
							three_unblockable.add(col);
						}
						else {
							three_blockable.add(col);
						}
					}
					
					if(getState[i][j]!=0 && i<g.getNumCols()-3 && (i==col || i+1==col || i+3==col) && j==row && getState[i][j]==getState[i+1][j] 
							&& getState[i][j]==getState[i+3][j] && getState[i+2][j]==0) {    //looks for three out of four horizontally with middle right empty
						if(j>0 && getState[i+2][j-1]==0) {
							three_unblockable.add(col);
						}
						else {
							three_blockable.add(col);
						}
					}
	
				    if(getState[i][j]!=0 && i<g.getNumCols()-2 && j<g.getNumRows()-2 && (i==col && j==row || i+1==col && j+1==row || i+2==col && j+2==row)
				    		&& getState[i][j]==getState[i+1][j+1] && getState[i][j]==getState[i+2][j+2] && (j<g.getNumRows()-3 &&  i<g.getNumCols()-3 
				    		&& getState[i+3][j+3]==0 || j>1 && i>1 && getState[i-1][j-1]==0)) {   //looks for three in a row in upper right diagonal
				    	if((i<g.getNumCols()-3 && j<g.getNumRows()-3 && getState[i+3][j+2]==0) || (j==2 && i<g.getNumCols()-3 
				    			&& ((i>0 && getState[i-1][j-2]==0) || getState[i+3][j+2]==0)) || (i==g.getNumCols()-3 && j>1 && getState[i-1][j-2]==0) 
				    			|| (j==g.getNumRows()-3 && getState[i-1][j-2]==0)) {
				    		three_unblockable.add(col);
				    	}
				    	else {
				    		three_blockable.add(col);
				    	}
				    }
				    
				    if(getState[i][j]!=0 && i<g.getNumCols()-3 && j<g.getNumRows()-3 && (i==col && j==row || i+2==col && j+2==row || i+3==col && j+3==row)
				    		&& getState[i][j]==getState[i+2][j+2] && getState[i][j]==getState[i+3][j+3] && getState[i+1][j+1]==0) {   //looks for 3/4 diagonally middle left empty
				    	if(getState[i+1][j]==0) {
				    		three_unblockable.add(col);
				    	}
				    	else {
				    		three_blockable.add(col);
				    	}
				    }
				    
				    if(getState[i][j]!=0 && i<g.getNumCols()-3 && j<g.getNumRows()-3 && (i==col && j==row || i+1==col && j+1==row || i+3==col && j+3==row)
				    		&& getState[i][j]==getState[i+1][j+1] && getState[i][j]==getState[i+3][j+3] && getState[i+2][j+2]==0) {   //looks for 3/4 diagonally middle right empty
				    	if(getState[i+2][j]==0) {
				    		three_unblockable.add(col);
				    	}
				    	else {
				    		three_blockable.add(col);
				    	}
				    }
				    
				    if(getState[i][j]!=0 && i<g.getNumCols()-2 && j>=2 && (i==col && j==row || i+1==col && j-1==row || i+2==col && j-2==row) 
				    		&& getState[i][j]==getState[i+1][j-1] && getState[i][j]==getState[i+2][j-2] && (i>1 && j<g.getNumRows()-1 
				    		&& getState[i-1][j+1]==0 || i<g.getNumCols()-3 && j>2 && getState[i+3][j-3]==0)) {   //looks for three in a row in lower right diagonal
				    	if(((j==3 || j==2) && i>0 && getState[i-1][j]==0) || (i>0 && i<g.getNumCols()-3 && j==4 && (getState[i-1][j]==0 || getState[i+3][j-4]==0)) 
				    			|| (i==0 && j>3 && getState[i+3][j-4]==0) || (j==g.getNumCols()-1 && getState[i+3][j-4]==0)) {
				    		three_unblockable.add(col);
				    	}
				    	else {
				    		three_blockable.add(col);
				    	}
				    }
				    
				    if(getState[i][j]!=0 && i<g.getNumCols()-3 && j>2 && (i==col && j==row || i+2==col && j-2==row || i+3==col && j-3==row)
				    		&& getState[i][j]==getState[i+2][j-2] && getState[i][j]==getState[i+3][j-3] && getState[i+1][j-1]==0) {  //looks for 3/4 diagonally middle left empty
				    	if(getState[i+1][j-2]==0) {
				    		three_unblockable.add(col);
				    	}
				    	else {
				    		three_blockable.add(col);
				    	}
				    }
				    
				    if(getState[i][j]!=0 && i<g.getNumCols()-3 && j>2 && (i==col && j==row || i+1==col && j-1==row || i+3==col && j-3==row)
				    		&& getState[i][j]==getState[i+1][j-1] && getState[i][j]==getState[i+3][j-3] && getState[i+2][j-2]==0) {  //looks for 3/4 diagonally middle right empty
				    	if(getState[i+2][j-3]==0) {
				    		three_unblockable.add(col);
				    	}
				    	else {
				    		three_blockable.add(col);
				    	}
				    }
			    }  
			}
			unblockable_arr = new int[three_unblockable.size()];
			int unblockable_index = 0;
			for(int num:three_unblockable) {
				unblockable_arr[unblockable_index]=num;
				unblockable_index++;
			}
			blockable_arr = new int[three_blockable.size()];
			int blockable_index = 0;
			for(int num:three_blockable) {
				blockable_arr[blockable_index]=num;
				blockable_index++;
			}

			hm.put("u", unblockable_arr);
			hm.put("b", blockable_arr);
			
			return hm;
		}
		
		public int[] threeSquares(int col, int row, int[][] arr) {	      //sees whether there is an opportunity to make three in a row or block opponent from doing so
			  
			int[] index_holder = new int[3];
			index_holder[0]=0;
			index_holder[1]=0;
			index_holder[2]=0;
			
			
			
			for(int i=0; i<g.getNumCols(); i++) {
				for(int j=0;j<g.getNumRows(); j++) {
					if(getState[i][j] != 0 && j < g.getNumRows()-3 && i == col && (j+2 == row) && getState[i][j] == getState[i][j+1] && getState[i][j] == getState[i][j+2] && getState[i][j+3] == 0) {
						index_holder[0] = col;
						index_holder[1] = row;
						if(arr[col][row] != 0)
							index_holder[2] = 2;
						else
							index_holder[2]= getState[col][row];
					}
					if(getState[i][j] != 0 && i < g.getNumCols()-2 && (i == col || i+1 == col || i+2 == col) && j == row && getState[i][j] == getState[i+1][j] && getState[i][j] == getState[i+2][j] && (i < g.getNumCols()-3 && getState[i+3][j] == 0 || i>0 && getState[i-1][j] == 0)) {
						index_holder[0] = col;
						index_holder[1] = row;
						if(arr[col][row] != 0)
							index_holder[2] = 2;
						else
							index_holder[2]= getState[col][row];
				    }
				    if(getState[i][j] != 0 && i < g.getNumCols()-2 && j < g.getNumRows()-2 && (i == col && j == row || i+1 == col && j+1 == row || i+2 == col && j+2 == row) && getState[i][j] == getState[i+1][j+1] && getState[i][j] == getState[i+2][j+2] && (j < g.getNumRows()-3 &&  i < g.getNumCols()-3 && getState[i+3][j+3] == 0 || j>1 && i>1 && getState[i-1][j-1] == 0)) {
				    	index_holder[0] = col;
				    	index_holder[1] = row;
						if(arr[col][row] != 0)
							index_holder[2] = 2;
						else
							index_holder[2]= getState[col][row];
				    }
				    if(getState[i][j] != 0 && i < g.getNumCols()-2 && j >= 2 && (i == col && j == row || i+1 == col && j-1 == row || i+2 == col && j-2 == row) && getState[i][j] == getState[i+1][j-1] && getState[i][j] == getState[i+2][j-2] && (i > 1 && j < g.getNumRows()-1 && getState[i-1][j+1] == 0 || i < g.getNumCols()-3 && j>2 && getState[i+3][j-3] == 0)) {
				    	index_holder[0] = col;
				    	index_holder[1] = row;
						if(arr[col][row] != 0)
							index_holder[2] = 2;
						else
							index_holder[2]= getState[col][row];
				    }
			    }  
			}
			
			  return index_holder;
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
		
		int[] quality_array = {0,1,2,3,2,1,0}; 		//Assigning starting values to quality array that skew towards the center. This array will  determine which col to play



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
				quality_array[col] = m.adjacent_touches;
				
			}
		}
		
		
		int best_move = max_element_array(quality_array, m.illegal_moves, m.losing_moves, m.unwise_moves, m.row_stopper, m.AI_three_unblockable, 
				m.opposing_three_unblockable, m.AI_three_blockable, m.opposing_three_blockable, m.winning_column, m.three_preventer);
		return best_move;
	}
	
	public int max_element_array(int[] arr, ArrayList<Integer> illegal_moves, ArrayList<Integer> losing_moves, ArrayList<Integer> unwise_moves, ArrayList<Integer> row_stopper, 
			ArrayList<Integer> AI_three_unblockable, ArrayList<Integer> opposing_three_unblockable, ArrayList<Integer> AI_three_blockable, ArrayList<Integer> opposing_three_blockable, 
			ArrayList<Integer> winning_column, ArrayList<Integer> three_preventable) {
		int max = -1000000;
		int max_element = 0;
		//System.out.println(losing_moves);
		//System.out.println(unwise_moves);
		//System.out.println(illegal_moves);
		//System.out.println(AI_three_unblockable);
		//System.out.println(opposing_three_unblockable);
		//System.out.println(AI_three_blockable);
		//System.out.println(winning_column);
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
		
			
		for(int i=5; i>=0; i--){ 
			for(int j=0; j<7; j++) { 
				//System.out.print(aaa[j][i] + " ");	
			}
			//System.out.println(" ");
		}
		//System.out.println(" ");

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
		for(int index:AI_three_unblockable) {		//Adding value to making three in a row
			arr[index] += three_grouped_unblockable;
		}
		for(int index:opposing_three_unblockable) {		//Adding value to blocking three in a row
			arr[index] += opponent_three_unblockable;
		}
		for(int index:AI_three_blockable) {		//Adding value to making three in a row
			arr[index] += three_grouped_blockable;
		}
		for(int index:opposing_three_blockable) {		//Adding value to blocking three in a row
			arr[index] += opponent_three_blockable;
		}
		for(int index:three_preventable) {		//Adding value to blocking three in a row
			arr[index] += block_opponent_three;
		}
		for(int index:winning_column) {		//Adding value to a winning column
			arr[index] += great_column;
		}

		
		for(int i=0; i<arr.length; i++) { 
			//System.out.print(arr[i] + " ");
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