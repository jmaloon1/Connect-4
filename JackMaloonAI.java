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
		ArrayList<Integer> AI_three_unblockable = new ArrayList<>();
		ArrayList<Integer> opposing_three_unblockable = new ArrayList<>();
		ArrayList<Integer> AI_three_blockable = new ArrayList<>();
		ArrayList<Integer> opposing_three_blockable = new ArrayList<>();
		ArrayList<Integer> row_stopper = new ArrayList<>();
		int[][] three_potential_map = new int[7][6];

		private int adjacent_touches = 0;
		
		public moveFinder(CFGame g) {
			
			this.g = g;
			getState = g.getState();
			for(int i=0; i<g.getNumCols(); i++) {
				  for(int j=0;j<g.getNumRows(); j++) {
					  three_potential_map[i][j] = 0;
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
					getState[column][row] = 0;
				} 
			}
			  	
			  row_stopper = isRowWinnable();
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
						//System.out.println('a');
					}
					if(getState[i][j]!=0 && i<g.getNumCols()-2 && (i==col || i+1==col || i+2==col) && j==row   //looks for three connected horizontally
							&& getState[i][j]==getState[i+1][j] && getState[i][j]==getState[i+2][j] 
							&& (i<g.getNumCols()-3 && getState[i+3][j]==0 || i>0 && getState[i-1][j]==0)) {
						if(j>0 && i>0 && i<g.getNumCols()-3 && (getState[i-1][j-1]==0 || getState[i+3][j-1]==0)) {
							//System.out.println('b');
							three_unblockable.add(col);
							three_unblockable.add(col);
						}
						else {
							three_blockable.add(col);
							three_blockable.add(col);
							//System.out.println("rowj " + j);
							//System.out.println("coli " + i);
							//System.out.println("row " + row);
							//System.out.println("col " + col);
							//System.out.println(getState[i][j]);
							//System.out.println(getState[i+1][j]);
							//System.out.println(getState[i+2][j]);

							//System.out.println('c');
						}
				    }
					//System.out.println("row" + row);
					//System.out.println("col" + col);
					//System.out.println("i" + i);
					//System.out.println("j" + j);
					
					if(getState[i][j]!=0 && i<g.getNumCols()-3 && (i==col || i+2==col || i+3==col) &&  j==row && getState[i][j]==getState[i+2][j] 
							&& getState[i][j]==getState[i+3][j] && getState[i+1][j]==0) {		//looks for three out of four horizontally with middle left empty
						if(j>0 && getState[i+1][j-1]==0) {
							three_unblockable.add(col);
							three_unblockable.add(col);
						}
						else {
							three_blockable.add(col);
							three_blockable.add(col);
						}
					}
					
					if(getState[i][j]!=0 && i<g.getNumCols()-3 && (i==col || i+1==col || i+3==col) && j==row && getState[i][j]==getState[i+1][j] 
							&& getState[i][j]==getState[i+3][j] && getState[i+2][j]==0) {    //looks for three out of four horizontally with middle right empty
						if(j>0 && getState[i+2][j-1]==0) {
							three_unblockable.add(col);
							three_unblockable.add(col);
						}
						else {
							three_blockable.add(col);
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
				    		three_unblockable.add(col);
				    		three_unblockable.add(col);
				    		//System.out.println('d');
				    	}
				    	else {
				    		three_blockable.add(col);
				    		three_blockable.add(col);
				    		three_blockable.add(col);
				    		//System.out.println('e');
				    	}
				    }
				    
				    if(getState[i][j]!=0 && i<g.getNumCols()-3 && j<g.getNumRows()-3 && (i==col && j==row || i+2==col && j+2==row || i+3==col && j+3==row)
				    		&& getState[i][j]==getState[i+2][j+2] && getState[i][j]==getState[i+3][j+3] && getState[i+1][j+1]==0) {   //looks for 3/4 diagonally middle left empty
				    	if(getState[i+1][j]==0) {
				    		three_unblockable.add(col);
				    		three_unblockable.add(col);
				    		three_unblockable.add(col);
				    		//System.out.println('d');
				    	}
				    	else {
				    		three_blockable.add(col);
				    		three_blockable.add(col);
				    		three_blockable.add(col);
				    		//System.out.println('e');
				    	}
				    }
				    
				    if(getState[i][j]!=0 && i<g.getNumCols()-3 && j<g.getNumRows()-3 && (i==col && j==row || i+1==col && j+1==row || i+3==col && j+3==row)
				    		&& getState[i][j]==getState[i+1][j+1] && getState[i][j]==getState[i+3][j+3] && getState[i+2][j+2]==0) {   //looks for 3/4 diagonally middle right empty
				    	if(getState[i+2][j]==0) {
				    		three_unblockable.add(col);
				    		three_unblockable.add(col);
				    		three_unblockable.add(col);
				    		//System.out.println('d');
				    	}
				    	else {
				    		three_blockable.add(col);
				    		three_blockable.add(col);
				    		three_blockable.add(col);
				    		//System.out.println('e');
				    	}
				    }
				    
				    if(getState[i][j]!=0 && i<g.getNumCols()-2 && j>=2 && (i==col && j==row || i+1==col && j-1==row || i+2==col && j-2==row) 
				    		&& getState[i][j]==getState[i+1][j-1] && getState[i][j]==getState[i+2][j-2] && (i>1 && j<g.getNumRows()-1 
				    		&& getState[i-1][j+1]==0 || i<g.getNumCols()-3 && j>2 && getState[i+3][j-3]==0)) {   //looks for three in a row in lower right diagonal
				    	if(((j==3 || j==2) && i>0 && getState[i-1][j]==0) || (i>0 && i<g.getNumCols()-3 && j==4 && (getState[i-1][j]==0 || getState[i+3][j-4]==0)) 
				    			|| (i==0 && j>3 && getState[i+3][j-4]==0) || (j==g.getNumCols()-1 && getState[i+3][j-4]==0)) {
				    		three_unblockable.add(col);
				    		three_unblockable.add(col);
				    		three_unblockable.add(col);
				    		//System.out.println('f');
				    	}
				    	else {
				    		three_blockable.add(col);
				    		three_blockable.add(col);
				    		three_blockable.add(col);
				    		//System.out.println('g');
				    	}
				    }
				    
				    if(getState[i][j]!=0 && i<g.getNumCols()-3 && j>2 && (i==col && j==row || i+2==col && j-2==row || i+3==col && j-3==row)
				    		&& getState[i][j]==getState[i+2][j-2] && getState[i][j]==getState[i+3][j-3] && getState[i+1][j-1]==0) {  //looks for 3/4 diagonally middle left empty
				    	if(getState[i+1][j-2]==0) {
				    		three_unblockable.add(col);
				    		three_unblockable.add(col);
				    		three_unblockable.add(col);
				    		//System.out.println('d');
				    	}
				    	else {
				    		three_blockable.add(col);
				    		three_blockable.add(col);
				    		three_blockable.add(col);
				    		//System.out.println('e');
				    	}
				    }
				    
				    if(getState[i][j]!=0 && i<g.getNumCols()-3 && j>2 && (i==col && j==row || i+1==col && j-1==row || i+3==col && j-3==row)
				    		&& getState[i][j]==getState[i+1][j-1] && getState[i][j]==getState[i+3][j-3] && getState[i+2][j-2]==0) {  //looks for 3/4 diagonally middle right empty
				    	if(getState[i+2][j-3]==0) {
				    		three_unblockable.add(col);
				    		three_unblockable.add(col);
				    		three_unblockable.add(col);
				    		//System.out.println('d');
				    	}
				    	else {
				    		three_blockable.add(col);
				    		three_blockable.add(col);
				    		three_blockable.add(col);
				    		//System.out.println('e');
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
			//System.out.println("3blockable " + three_blockable);
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
						//System.out.println("a");
						index_holder[0] = col;
						index_holder[1] = row;
						if(arr[col][row] != 0)
							index_holder[2] = 2;
						else
							index_holder[2]= getState[col][row];
						//System.out.println(aaa[col][row]);
					}
					if(getState[i][j] != 0 && i < g.getNumCols()-2 && (i == col || i+1 == col || i+2 == col) && j == row && getState[i][j] == getState[i+1][j] && getState[i][j] == getState[i+2][j] && (i < g.getNumCols()-3 && getState[i+3][j] == 0 || i>0 && getState[i-1][j] == 0)) {
						index_holder[0] = col;
						index_holder[1] = row;
						if(arr[col][row] != 0)
							index_holder[2] = 2;
						else
							index_holder[2]= getState[col][row];
					    //System.out.println("b");
					    //System.out.println(aaa[col][row]);
				    }
				    if(getState[i][j] != 0 && i < g.getNumCols()-2 && j < g.getNumRows()-2 && (i == col && j == row || i+1 == col && j+1 == row || i+2 == col && j+2 == row) && getState[i][j] == getState[i+1][j+1] && getState[i][j] == getState[i+2][j+2] && (j < g.getNumRows()-3 &&  i < g.getNumCols()-3 && getState[i+3][j+3] == 0 || j>1 && i>1 && getState[i-1][j-1] == 0)) {
				    	index_holder[0] = col;
				    	index_holder[1] = row;
						if(arr[col][row] != 0)
							index_holder[2] = 2;
						else
							index_holder[2]= getState[col][row];
				    	//System.out.println("c");
				    	//System.out.println(aaa[col][row]);
				    }
				    if(getState[i][j] != 0 && i < g.getNumCols()-2 && j >= 2 && (i == col && j == row || i+1 == col && j-1 == row || i+2 == col && j-2 == row) && getState[i][j] == getState[i+1][j-1] && getState[i][j] == getState[i+2][j-2] && (i > 1 && j < g.getNumRows()-1 && getState[i-1][j+1] == 0 || i < g.getNumCols()-3 && j>2 && getState[i+3][j-3] == 0)) {
				    	index_holder[0] = col;
				    	index_holder[1] = row;
						if(arr[col][row] != 0)
							index_holder[2] = 2;
						else
							index_holder[2]= getState[col][row];
				    	//System.out.println("d");
				    	//System.out.println(aaa[col][row]);

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
			//System.out.println(col + " touching " + num_touching);
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
		
		
		int best_move = max_element_array(touching_array, m.illegal_moves, m.losing_moves, m.unwise_moves, m.row_stopper, m.AI_three_unblockable, m.opposing_three_unblockable, m.AI_three_blockable, m.opposing_three_blockable, m.three_potential_map);
		return best_move;
	}
	
	public int max_element_array(int[] arr, ArrayList<Integer> illegal_moves, ArrayList<Integer> losing_moves, ArrayList<Integer> unwise_moves, ArrayList<Integer> row_stopper, ArrayList<Integer> AI_three_unblockable, ArrayList<Integer> opposing_three_unblockable, ArrayList<Integer> AI_three_blockable, ArrayList<Integer> opposing_three_blockable, int[][] aaa) {
		int max = -1000000;
		int max_element = 0;
		//System.out.println(losing_moves);
		//System.out.println(unwise_moves);
		//System.out.println(illegal_moves);
		//System.out.println(AI_three_unblockable);
		//System.out.println(opposing_three_unblockable);
		//System.out.println(AI_three_blockable);
		//System.out.println(opposing_three_blockable);
		int illegal = -1000;
		int losing = -150;
		int unwise = -100;
		int row_stop = 50;
		int three_grouped_unblockable = 15;
		int opponent_three_unblockable  = 12;
		int three_grouped_blockable = 4;
		int opponent_three_blockable  = 3;
			
		for(int i=5; i>=0; i--){ 
			for(int j=0; j<7; j++) { 
				System.out.print(aaa[j][i] + " ");	
			}
			System.out.println(" ");
		}
		System.out.println(" ");

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