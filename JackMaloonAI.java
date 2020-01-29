package hw4;
//make it so bad move is involved with touches
import java.util.*;

public class JackMaloonAI implements CFPlayer{
	
	private class moveFinder{		//inner class that doesn't actually play moves, but simulates moves to see if there is a winning one or one to prevent the opponent from winning
		
		CFGame g;
		private int [][] getState;
		private int opponent_color = 1;
		private int AI_color = -1;
		private boolean winning_move;
		private ArrayList<Integer> losing_moves = new ArrayList<>();		//losing move is considered move that will allow opponent to win on following turn
		private ArrayList<Integer> unwise_moves = new ArrayList<>();		//unwise move is considered move that will allow opponent to block AI's winning move on following turn
		private ArrayList<Integer> illegal_moves = new ArrayList<>();    //move that is illegal
		private ArrayList<Integer> AI_three_unblockable = new ArrayList<>();
		private ArrayList<Integer> opposing_three_unblockable = new ArrayList<>();
		private ArrayList<Integer> AI_three_blockable = new ArrayList<>();
		private ArrayList<Integer> opposing_three_blockable = new ArrayList<>();
		private ArrayList<Integer> loss_avoider = new ArrayList<>();
		private ArrayList<Integer> loss_creator = new ArrayList<>();
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
				opponent_color = -1;
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
				    
				    if(!winning_move && (row==0 || (row>0 && getState[column][row-1]!=0))) {
				    	if(!already_winning_column) {
				    		winningColumnCreator(column, row);
				    	if(!opp_turn && !winning_column.contains(column)) {
				    		losingBoardCreator(column, row);
				    		
				    	}
				    	}
					}
					
					getState[column][row] = 0;
				} 
			}  
			  
			avoidLoss();   //sees is a row can be won horizontally in two moves
			avoidAllowingThree();  //checks to see is a certain move would alow three out of four to be created
				   
			return true;
		}
		
		public void findWinningColumn() {
			
			four_potential_map = fourMap();
			boolean opponent_can_win = false;
			int num_squares = g.getNumCols();
			
			for(int i=0; i<g.getNumCols(); i++) {    //looks at places on board where four in a row can be made and if two of same color on top of each other, indicates winning column
				for(int j=0;j<g.getNumRows(); j++) {
					
					if(j<g.getNumRows()-1 && four_potential_map[i][j]!=0 && four_potential_map[i][j+1]!=0 
					   && ((four_potential_map[i][j]==four_potential_map[i][j+1] || four_potential_map[i][j+1]==2))) {
						
						for(int num=0; num<j; num++) {
							if(four_potential_map[i][num]==opponent_color)
								opponent_can_win = true;
						}
						
						
						if(!opponent_can_win) {
							System.out.println("winning column is " + i);
							already_winning_column = true;
							if(!winning_column.contains(i) && (four_potential_map[i][j]==AI_color || four_potential_map[i][j+1]==AI_color) && j<num_squares) {
								winning_column.add(i);
								num_squares = j;
							}
						}
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
					
					if(getState[i+2][j]!=0 && (getState[i][j]==0 && getState[i+1][j]==0 && getState[i+2][j]==getState[i+3][j] && getState[i+4][j]==0)) {  //checking to see if row can be won/lost or can be set up to win/lose
						
						if(j==0 || (j>0 && (getState[i][j-1]!=0 && getState[i+1][j-1]!=0 && getState[i+4][j-1]!=0))) {
							System.out.println('a');
							if(!loss_avoider.contains(i+1))
								loss_avoider.add(i+1);
						}
						else if(j>0 && getState[i][j-1]!=0 && getState[i+1][j-1]!=0 && getState[i+4][j-1]==0 && (j==1 || j>1 && getState[i+4][j-2]!=0)) {
							System.out.println('b');
							if(!loss_creator.contains(i+4) && getState[i+2][j]==opponent_color)
								loss_creator.add(i+4);
							else if(!unwise_moves.contains(i+4) && getState[i+2][j]==AI_color)
								unwise_moves.add(i+4);
						}
						else if(j>0 && getState[i][j-1]!=0 && getState[i+1][j-1]==0 && getState[i+4][j-1]!=0 && (j==1 || j>1 && getState[i+1][j-2]!=0)) {
							System.out.println('c');
							if(!loss_creator.contains(i+1) && getState[i+2][j]==opponent_color)
								loss_creator.add(i+1);
							else if(!unwise_moves.contains(i+1) && getState[i+2][j]==AI_color)
								unwise_moves.add(i+1);
						}
						else if(j>0 && getState[i][j-1]==0 && getState[i+1][j-1]!=0 && getState[i+4][j-1]!=0 && (j==1 || j>1 && getState[i][j-2]!=0)) {
							System.out.println('d');
							if(!loss_creator.contains(i) && getState[i+2][j]==opponent_color)
								loss_creator.add(i);
							else if(!unwise_moves.contains(i) && getState[i+2][j]==AI_color)
								unwise_moves.add(i);
						}
					}
					
					if(getState[i+2][j]!=0 && (getState[i][j]==0 && getState[i+1][j]==getState[i+2][j] && getState[i+3][j]==0 && getState[i+4][j]==0)) {  //checking to see if row can be won/lost or can be set up to win/lose		
					
						if(j==0|| (j>0 && (getState[i][j-1]!=0 && getState[i+3][j-1]!=0 && getState[i+4][j-1]!=0))) {
							System.out.println('e');	
							if(!loss_avoider.contains(i+3))
								loss_avoider.add(i+3);
						}
						else if(j>0 && getState[i][j-1]!=0 && getState[i+3][j-1]!=0 && getState[i+4][j-1]==0 && (j==1 || j>1 && getState[i+4][j-2]!=0)) {
							System.out.println('f');
							if(!loss_creator.contains(i+4) && getState[i+2][j]==opponent_color)
								loss_creator.add(i+4);
							else if(!unwise_moves.contains(i+4) && getState[i+2][j]==AI_color)
								unwise_moves.add(i+4);
						}
						else if(j>0 && getState[i][j-1]!=0 && getState[i+3][j-1]==0 && getState[i+4][j-1]!=0 && (j==1 || j>1 && getState[i+3][j-2]!=0)) {
							System.out.println('g');
							if(!loss_creator.contains(i+3) && getState[i+2][j]==opponent_color)
								loss_creator.add(i+3);
							else if(!unwise_moves.contains(i+3) && getState[i+2][j]==AI_color)
								unwise_moves.add(i+3);
						}
						else if(j>0 && getState[i][j-1]==0 && getState[i+3][j-1]!=0 && getState[i+4][j-1]!=0 && (j==1 || j>1 && getState[i][j-2]!=0)) {
							System.out.println('h');
							if(!loss_creator.contains(i) && getState[i+2][j]==opponent_color)
								loss_creator.add(i);
							else if(!unwise_moves.contains(i) && getState[i+2][j]==AI_color)
								unwise_moves.add(i);
						}
					}
					
					if(i>0 && i<g.getNumCols()-3 && getState[i][j]!=0 && getState[i+1][j]==0 && getState[i-1][j]==0 && getState[i+3][j]==0 && getState[i][j]==getState[i+2][j]) { //checking to see if row can be won/lost or can be set up to win/lose	
						
						if(j==0|| (j>0 && (getState[i-1][j-1]!=0 && getState[i+1][j-1]!=0 && getState[i+3][j-1]!=0))) {
							System.out.println('i');
							if(!loss_avoider.contains(i+1))
								loss_avoider.add(i+1);
						}
						else if(j>0 && getState[i-1][j-1]!=0 && getState[i+1][j-1]!=0 && getState[i+3][j-1]==0 && (j==1 || j>1 && getState[i+3][j-2]!=0)) {
							System.out.println('j');
							if(!loss_creator.contains(i+3) && getState[i][j]==opponent_color)
								loss_creator.add(i+3);
							else if(!unwise_moves.contains(i+3) && getState[i][j]==AI_color)
								unwise_moves.add(i+3);
						}
						else if(j>0 && getState[i-1][j-1]!=0 && getState[i+1][j-1]==0 && getState[i+3][j-1]!=0 && (j==1 || j>1 && getState[i+1][j-2]!=0)) {
							System.out.println('k');
							if(!loss_creator.contains(i+1) && getState[i][j]==opponent_color)
								loss_creator.add(i+1);
							else if(!unwise_moves.contains(i+1) && getState[i][j]==AI_color)
								unwise_moves.add(i+1);
						}
						else if(j>0 && getState[i-1][j-1]==0 && getState[i+1][j-1]!=0 && getState[i+3][j-1]!=0 && (j==1 || j>1 && getState[i-1][j-2]!=0)) {
							System.out.println('l');
							if(!loss_creator.contains(i-1) && getState[i][j]==opponent_color)
								loss_creator.add(i-1);
							else if(!unwise_moves.contains(i-1) && getState[i][j]==AI_color)
								unwise_moves.add(i-1);
						}
					}
					
					if((i==0 || i==1) && getState[i][j]!=0 && getState[i+1][j]==0 && (getState[i+2][j]==getState[i][j] && getState[i+3][j]==0 || (getState[i+3][j]==getState[i][j] && getState[i+2][j]==0))  //checking to see if row can be won/lost or can be set up to win/lose	
						&& getState[i+4][j]==0 && getState[i+5][j]==getState[i][j]) {
						
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
							if(!loss_creator.contains(i+1) && getState[i][j]==opponent_color)
								loss_creator.add(i+1);
							else if(!unwise_moves.contains(i+1) && getState[i][j]==AI_color)
								unwise_moves.add(i+1);
						}
						else if(j>0 && getState[i+1][j-1]!=0 && getState[i+2][j]==0 && getState[i+2][j-1]==0 && getState[i+4][j-1]!=0 && (j==1 || j>1 && getState[i+2][j-2]!=0)){
							System.out.println('o');
							if(!loss_creator.contains(i+2) && getState[i][j]==opponent_color)
								loss_creator.add(i+2);
							else if(!unwise_moves.contains(i+2) && getState[i][j]==AI_color)
								unwise_moves.add(i+2);
						}
						else if(j>0 && getState[i+1][j-1]!=0 && getState[i+3][j]==0 && getState[i+3][j-1]==0 && getState[i+4][j-1]!=0 && (j==1 || j>1 && getState[i+3][j-2]!=0)){
							System.out.println('p');
							if(!loss_creator.contains(i+3) && getState[i][j]==opponent_color)
								loss_creator.add(i+3);
							else if(!unwise_moves.contains(i+3) && getState[i][j]==AI_color)
								unwise_moves.add(i+3);
						}
						else if(j>0 && getState[i+1][j-1]!=0 && (getState[i+2][j]==0 && getState[i+2][j-1]!=0 || getState[i+3][j]==0 && getState[i+3][j-1]!=0) 
							    && getState[i+4][j-1]==0 && (j==1 || j>1 && getState[i+4][j-2]!=0)){
							System.out.println('q');
							if(!loss_creator.contains(i+4) && getState[i][j]==opponent_color)
								loss_creator.add(i+4);
							else if(!unwise_moves.contains(i+4) && getState[i][j]==AI_color)
								unwise_moves.add(i+4);
						}
					}

					if((i<g.getNumCols()-3 && j<g.getNumRows()-3 && getState[i][j]==0 && getState[i+1][j+1]!=0 && getState[i+1][j+1]==getState[i+2][j+2] 
						&& getState[i+3][j+3]==0)) {   //two open on left upper diagonal
						
						if(i>0 && j>0 && getState[i-1][j-1]==0 && (j==1 || j>1 && getState[i-1][j-2]!=0) && getState[i][j-1]!=0 && getState[i+3][j+2]!=0) {    
							System.out.println('r');
							if(!loss_avoider.contains(i))
								loss_avoider.add(i);
						}
						else if(i>0 && j>1 && getState[i-1][j-2]==0 && (j==2 || getState[i-1][j-3]!=0) && getState[i][j-1]!=0 && getState[i+3][j+2]!=0) {
							System.out.println('s');
							if(!loss_creator.contains(i-1) && getState[i+1][j+1]==opponent_color)
								loss_creator.add(i-1);
							else if(!unwise_moves.contains(i-1) && getState[i+1][j+1]==AI_color)
								unwise_moves.add(i-1);
						}
						else if(i>0 && (j==1 || j>1 && getState[i-1][j-2]!=0 && getState[i][j-2]!=0) && getState[i][j-1]==0 && getState[i+3][j+2]!=0) {
							System.out.println('t');
							if(!loss_creator.contains(i) && getState[i+1][j+1]==opponent_color)
								loss_creator.add(i);
							else if(!unwise_moves.contains(i) && getState[i+1][j+1]==AI_color)
								unwise_moves.add(i);
						}
						else if(i>0 && (j==1 || j>1 && getState[i-1][j-2]!=0) && getState[i][j-1]!=0 && getState[i+3][j+2]==0 && getState[i+3][j+1]!=0) {
							System.out.println('u');
							if(!loss_creator.contains(i+3) && getState[i+1][j+1]==opponent_color)
								loss_creator.add(i+3);
							else if(!unwise_moves.contains(i+3) && getState[i+1][j+1]==AI_color)
								unwise_moves.add(i+3);
						}
						
						if((j==0 || getState[i][j-1]!=0) && getState[i+3][j+2]!=0 && getState[i+4][j+3]!=0) {    //two open on right upper diagonal
							System.out.println('v');
							if(!loss_avoider.contains(i))
								loss_avoider.add(i);
						}
						else if(j>0 && getState[i][j-1]==0 && (j==1 || getState[i][j-2]!=0) && getState[i+3][j+2]!=0 && getState[i+4][j+3]!=0) {
							System.out.println('w');
							if(!loss_creator.contains(i) && getState[i+1][j+1]==opponent_color)
								loss_creator.add(i);
							else if(!unwise_moves.contains(i) && getState[i+1][j+1]==AI_color)
								unwise_moves.add(i);
						}
						else if((j==0 || getState[i][j-1]!=0) && getState[i+3][j+2]==0 && getState[i+3][j+1]!=0 && getState[i+4][j+3]!=0) {
							System.out.println('x');
							if(!loss_creator.contains(i+3) && getState[i+1][j+1]==opponent_color)
								loss_creator.add(i+3);
							else if(!unwise_moves.contains(i+3) && getState[i+1][j+1]==AI_color)
								unwise_moves.add(i+3);
						}
						else if((j==0 || getState[i][j-1]!=0) && getState[i+3][j+2]!=0 && getState[i+4][j+3]==0 && getState[i+4][j+2]!=0) {
							System.out.println('y');
							if(!loss_creator.contains(i+4) && getState[i+1][j+1]==opponent_color)
								loss_creator.add(i+4);
							else if(!unwise_moves.contains(i+4) && getState[i+1][j+1]==AI_color)
								unwise_moves.add(i+4);
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
							if(!loss_creator.contains(i) && getState[i+1][j+1]==opponent_color)
								loss_creator.add(i);
							else if(!unwise_moves.contains(i) && getState[i+1][j+1]==AI_color)
								unwise_moves.add(i);
						}
						else if((j==0 || getState[i][j-1]!=0) && getState[i+2][j+1]==0 && getState[i+2][j]!=0 && getState[i+4][j+3]!=0) {
							System.out.println("bb");
							if(!loss_creator.contains(i+2) && getState[i+1][j+1]==opponent_color)
								loss_creator.add(i+2);
							else if(!unwise_moves.contains(i+2) && getState[i+1][j+1]==AI_color)
								unwise_moves.add(i+2);
						}
						else if((j==0 || getState[i][j-1]!=0) && getState[i+2][j+1]!=0 && getState[i+4][j+3]==0 && getState[i+4][j+2]!=0) {
							System.out.println("cc");
							if(!loss_creator.contains(i+4) && getState[i+1][j+1]==opponent_color)
								loss_creator.add(i+4);
							else if(!unwise_moves.contains(i+4) && getState[i+1][j+1]==AI_color)
								unwise_moves.add(i+4);
						}		
						
					}
					
					if(j>2 && getState[i][j]==0 && getState[i+1][j-1]!=0 && getState[i+1][j-1]==getState[i+2][j-2] && getState[i+3][j-3]==0) {
						
						if(i>0 && (j==3 || getState[i+3][j-4]!=0) && getState[i-1][j]!=0 && getState[i][j-1]!=0) {  //lower diagonal two open left
							System.out.println("dd");
							if(!loss_avoider.contains(i))
								loss_avoider.add(i);
						}
						else if(i>0 && getState[i-1][j]==0 && getState[i-1][j-1]!=0 && getState[i-1][j-1]!=0 && getState[i][j-1]!=0 && (j==3 || getState[i+3][j-4]!=0)) {
							System.out.println("ee");
							if(!loss_creator.contains(i-1) && getState[i+1][j-1]==opponent_color)
								loss_creator.add(i-1);
							else if(!unwise_moves.contains(i-1) && getState[i+1][j-1]==AI_color)
								unwise_moves.add(i-1);
						}
						else if(i>0 && getState[i-1][j]!=0 && getState[i][j-1]==0 && getState[i][j-2]!=0 && (j==3 || getState[i+3][j-4]!=0)) {
							System.out.println("ff");
							if(!loss_creator.contains(i) && getState[i+1][j-1]==opponent_color)
								loss_creator.add(i);
							else if(!unwise_moves.contains(i) && getState[i+1][j-1]==AI_color)
								unwise_moves.add(i);
						}
						else if(i>0 && j>3 && getState[i-1][j]!=0 && getState[i][j-1]!=0 && getState[i+3][j-4]==0 && (j==4 || getState[i+3][j-5]!=0)) {
							System.out.println("gg");
							if(!loss_creator.contains(i+3) && getState[i+1][j-1]==opponent_color)
								loss_creator.add(i+3);
							else if(!unwise_moves.contains(i+3) && getState[i+1][j-1]==AI_color)
								unwise_moves.add(i+3);
						}
						
						if(j>3 && getState[i][j-1]!=0 && getState[i+3][j-4]!=0 && (j==4 || getState[i+4][j-5]!=0)) {   //lower diagonal two open right
							System.out.println("hh");
							if(!loss_avoider.contains(i+3))
								loss_avoider.add(i+3);
						}
						else if(j>3 && getState[i][j-1]==0 && getState[i][j-2]!=0 && getState[i+3][j-4]!=0 && (j==4 || getState[i+4][j-5]!=0)) {
							System.out.println("ii");
							if(!loss_creator.contains(i) && getState[i+1][j-1]==opponent_color)
								loss_creator.add(i);
							else if(!unwise_moves.contains(i) && getState[i+1][j-1]==AI_color)
								unwise_moves.add(i);
						}
						else if(j>3 && getState[i][j-1]!=0 && getState[i+3][j-4]==0 && (j==4 || getState[i+3][j-5]!=0 && getState[i+4][j-5]!=0)) {
							System.out.println("jj");
							if(!loss_creator.contains(i+3) && getState[i+1][j-1]==opponent_color)
								loss_creator.add(i+3);
							else if(!unwise_moves.contains(i+3) && getState[i+1][j-1]==AI_color)
								unwise_moves.add(i+3);
						}
						else if(j>4 && getState[i][j-1]!=0 && getState[i+3][j-4]!=0 && getState[i+4][j-5]==0) {
							System.out.println("kk");
							if(!loss_creator.contains(i+4) && getState[i+1][j-1]==opponent_color)
								loss_creator.add(i+4);
							else if(!unwise_moves.contains(i+4) && getState[i+1][j-1]==AI_color)
								unwise_moves.add(i+4);
						}	
					}
					
					if(i<3 && j>3 && getState[i][j]==0 && getState[i+1][j-1]!=0 && getState[i+1][j-1]==getState[i+3][j-3] && getState[i+4][j-4]==0) {  //lower diagonal middle open
						
						if(getState[i][j-1]!=0 && getState[i+2][j-3]!=0 && (j==4 || getState[i+4][j-5]!=0)) {
							System.out.println("ll");
							if(!loss_avoider.contains(i+2))
								loss_avoider.add(i+2);
						}
						else if(getState[i][j-1]==0 && getState[i][j-2]!=0 && getState[i+2][j-3]!=0 && (j==4 || getState[i+4][j-5]!=0)) {
							System.out.println("mm");
							if(!loss_creator.contains(i) && getState[i+1][j-1]==opponent_color)
								loss_creator.add(i);
							else if(!unwise_moves.contains(i) && getState[i+1][j-1]==AI_color)
								unwise_moves.add(i);
						}
						else if(getState[i][j-1]!=0 && getState[i+2][j-3]==0 && getState[i+2][j-4]!=0 && (j==4 || getState[i+4][j-5]!=0)) {
							System.out.println("nn");
							if(!loss_creator.contains(i+2) && getState[i+1][j-1]==opponent_color)
								loss_creator.add(i+2);
							else if(!unwise_moves.contains(i+2) && getState[i+1][j-1]==AI_color)
								unwise_moves.add(i+2);
						}
						else if(j>4 &getState[i][j-1]!=0 && getState[i+2][j-3]!=0 && getState[i+4][j-5]==0) {
							System.out.println("oo");
							if(!loss_creator.contains(i+4) && getState[i+1][j-1]==opponent_color)
								loss_creator.add(i+4);
							else if(!unwise_moves.contains(i+4) && getState[i+1][j-1]==AI_color)
								unwise_moves.add(i+4);
						}
					}				
				}
			}  
		}
		
		public void winningColumnCreator(int column, int row) {
			
			future_winning_move = -1;
			
			HashMap<Integer, Integer> opp_winning_moves = new HashMap<>();
			HashMap<Integer, Integer> ai_winning_moves = new HashMap<>();
			
			int[][] four_map = fourMap();

			for(int i=0; i<g.getNumCols(); i++) {
				for(int j=0; j<g.getNumRows()-1; j++) {
					if(four_map[i][j]!=0 && four_map[i][j+1]!=0 && (four_map[i][j]==four_map[i][j+1] ||  four_map[i][j+1]==2)) {
						
						if(!winning_column.contains(column))
							winning_column.add(column);
					}	
					if(four_map[i][j]==AI_color && (j==0 || getState[i][j-1]!=0)) {
						//System.out.println("");
						//System.out.println("aicol " + column);
						//System.out.println("airow " + row);
						
						if(!ai_winning_moves.containsKey(column))
							ai_winning_moves.put(column, 1);
						else {
							int count = ai_winning_moves.get(column);
							ai_winning_moves.put(column, count+1);
						}
					}
					else if(four_map[i][j]==opponent_color && (j==0 || getState[i][j-1]!=0)) {
						//System.out.println("");
						//System.out.println("oppcol " + column);
						//System.out.println("opprow " + row);
						
						if(!opp_winning_moves.containsKey(column))
							opp_winning_moves.put(column, 1);
						else {
							int count = opp_winning_moves.get(column);
							opp_winning_moves.put(column, count+1);
						}
					}			
				}					    
			}
			
			for(int num=0; num<g.getNumCols(); num++){
				if(!winning_column.contains(column) && (ai_winning_moves.containsKey(num) && ai_winning_moves.get(num)>1) || (opp_winning_moves.containsKey(num) && opp_winning_moves.get(num)>1)) { 
					winning_column.add(num);
					System.out.println("sdefihedfhweifhiefh");
				}
			}			
		}
		
		public void losingBoardCreator(int column, int row){
			
			future_winning_move = -1;
			HashMap<Integer, Integer> opp_winning_creator = new HashMap<>();
			HashMap<Integer, Integer> ai_winning_creator = new HashMap<>();
			int[][] four_map;
			int[] arr = {-1,1};

			for(int c=0; c<g.getNumCols(); c++) {
				for(int r=0; r<g.getNumRows()-1; r++) {
					if(getState[c][r]==0 && (r==0||getState[c][r-1]!=0) && !winning_column.contains(c)) {
						opp_winning_creator = new HashMap<>();
						ai_winning_creator = new HashMap<>();
						
						getState[c][r] = opponent_color;
						four_map = fourMap();
						
						
						if(c==6 && r==2) {
							System.out.println("fourmap");
							System.out.println(opp_winning_creator);
							for(int cc=5; cc>=0; cc--) {
								for(int rr=0; rr<g.getNumCols(); rr++) {
									System.out.print(four_map[rr][cc]);
								}
								System.out.println("");
							}
							System.out.println("");
						}
						
						
						
						
						for(int i=0; i<g.getNumCols(); i++) {
							for(int j=0; j<g.getNumRows()-1; j++) {
								if(four_map[i][j]!=0 && four_map[i][j+1]!=0 && (four_map[i][j]==four_map[i][j+1] ||  four_map[i][j+1]==2)
								   && !loss_creator.contains(column) && (four_map[i][j]==opponent_color || four_map[i][j+1]==opponent_color))
										loss_creator.add(column);
								
								/*
								
								if(four_map[i][j]==AI_color && (j==0 || getState[i][j-1]!=0)) {
									//System.out.println("");
									//System.out.println("aicol " + column);
									//System.out.println("airow " + row);
									
									if(!ai_winning_creator.containsKey(column))
										ai_winning_creator.put(column, 1);
									else {
										int count = ai_winning_creator.get(column);
										ai_winning_creator.put(column, count+1);
									}
								}
								*/
								if(four_map[i][j]==opponent_color && (j==0 || getState[i][j-1]!=0)) {
									//System.out.println("");
									//System.out.println("oppcol " + column);
									//System.out.println("opprow " + row);
									
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
							if((!ai_winning_creator.isEmpty() && ai_winning_creator.containsKey(col_num) && ai_winning_creator.get(col_num)>1) 
							    || (!opp_winning_creator.isEmpty() && opp_winning_creator.containsKey(col_num) && opp_winning_creator.get(col_num)>1)) { 
								
								System.out.println("fourmap");
								System.out.println(opp_winning_creator);
								for(int cc=5; cc>=0; cc--) {
									for(int rr=0; rr<g.getNumCols(); rr++) {
										System.out.print(four_map[rr][cc]);
									}
									System.out.println("");
								}
								System.out.println("");
								if(!loss_creator.contains(col_num)) {
									loss_creator.add(col_num);
									System.out.println("column " + column);
									System.out.println("row " + row);
									System.out.println("c " + c);
									System.out.println("r " + r);
									System.out.println("num " + opponent_color);
								}
							}
						}
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
						
						if(j>0 && i>0 && i<g.getNumCols()-3 && (getState[i-1][j-1]==0 || getState[i+3][j-1]==0)) {	
							
							if(getState[i-1][j-1]==0)
								three_unblockable.add(column);
							if(getState[i+3][j-1]==0) 		
								three_unblockable.add(column);
						}
						else 
							three_blockable.add(column);
						
						if(three_potential_map[column][row]==0)
							three_potential_map[column][row] = getState[i][j];
						else if(three_potential_map[column][row]!=getState[i][j])
							three_potential_map[column][row] = 2;
				    }
					
					if(getState[i][j]!=0 && i<g.getNumCols()-3 && initial_four_map[i+1][j]!=getState[i][j] && initial_four_map[i+1][j]!=2 && (i==column || i+2==column || i+3==column) &&  j==row && getState[i][j]==getState[i+2][j]   //looks for three out of four horizontally with middle left empty
					   && getState[i][j]==getState[i+3][j] && getState[i+1][j]==0) {		
						
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
					   && getState[i][j]==getState[i+3][j] && getState[i+2][j]==0) {    //looks for three out of four horizontally with middle right empty
						
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
				
				quality_array[col] += m.num_touching;
			}
		}
		
		return max_element_array(quality_array, m.illegal_moves, m.losing_moves, m.unwise_moves, m.loss_avoider, m.loss_creator, m.AI_three_unblockable, 
				        		 m.opposing_three_unblockable, m.AI_three_blockable, m.opposing_three_blockable, m.winning_column, m.three_preventer, m.four_potential_map);
	}
	
	public int max_element_array(int[] quality_arr, ArrayList<Integer> illegal_moves, ArrayList<Integer> losing_moves, ArrayList<Integer> unwise_moves, ArrayList<Integer> loss_avoider, 
			ArrayList<Integer> loss_creator, ArrayList<Integer> AI_three_unblockable, ArrayList<Integer> opposing_three_unblockable, ArrayList<Integer> AI_three_blockable, ArrayList<Integer> opposing_three_blockable, 
			ArrayList<Integer> winning_column, ArrayList<Integer> three_preventable, int[][] four_potential_map) {
		
		int max = -1000000;
		int max_element = 0;
		/*
		System.out.println("losing moves " + losing_moves);
		System.out.println("unwise moves " + unwise_moves);
		System.out.println("illegal moves " + illegal_moves);
		System.out.println("three_preventable " + three_preventable);
		System.out.println("ai  unblockable " + AI_three_unblockable);
		System.out.println("opp unblockable " + opposing_three_unblockable);
		System.out.println("ai blockable " + AI_three_blockable);
		System.out.println("opp blockable " + opposing_three_blockable);
		System.out.println("winning column " + winning_column);
		System.out.println("loss avoider " + loss_avoider);
		
		*/
		System.out.println("loss creator " + loss_creator);
		int illegal = -10000;
		int losing = -1000;
		int unwise = -150;
		int loss_finder = 500;
		int loss_maker = -500;
		int block_opponent_three = -7;
		int three_grouped_unblockable = 30;
		int opponent_three_unblockable  = 20;
		int three_grouped_blockable = 7;
		int opponent_three_blockable  = 5;
		int great_column = 400;
		
		//System.out.println("before");
		for(int x=0; x<quality_arr.length; x++) { 
			//System.out.print(quality_arr[x] + " ");
		}
		//System.out.println("");
		
		ArrayList<Integer> duplicate_max = new ArrayList<>();
		
		for(int index:losing_moves) {		//Setting values in arr to negative value if move is losing
			quality_arr[index] += losing;
		}
		for(int index:unwise_moves) {		//Setting values in arr to negative value if move is unwise. Still greater than losing since this move is better
			quality_arr[index] += unwise;
		}
		for(int index:loss_avoider) {		//If a row can be "stopped", a lot of value is added to that column that "stops" it
			quality_arr[index] += loss_finder;
		}
		for(int index:loss_creator) {		//If a row can be "stopped", a lot of value is added to that column that "stops" it
			quality_arr[index] += loss_maker;
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
		for(int index:illegal_moves) {		//Setting values in arr to arbitrary negative value if move is illegal
			quality_arr[index] = illegal;
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
			System.out.print(quality_arr[i] + " ");
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
		System.out.println("");
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