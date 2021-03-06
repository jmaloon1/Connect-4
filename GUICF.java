package connect4;

import javax.swing.*;
import java.awt.*;
import java.util.Random;
import java.awt.event.*;

/**
 * This class holds the logic for a game to be played with a GUI. There are several options for a GUI game including:
 * 		human vs. human game
 * 		human vs. AI game
 * 		AI vs. AI game
*/
public class GUICF extends CFGame{
	
	/**
	 * This inner class creates a ButtonListener that will play a column if possible based on the button a human clicks.
	*/
	private class ButtonListener extends CFGame implements ActionListener{
		
		private int column;
		
		/**
		 * This constructor takes in an integer who's column is to be played.
		 * @param i: int of column to be played
		*/
		public ButtonListener(int i) {	
			
			column = i;
		}
		
		/**
	     * This method plays a column.
	     * @param e: ActionEvent that allows the button to have internal properties
	     * @return void
	    */
		public void actionPerformed(ActionEvent e){	 
			
			first_turn = false;
			ai_played = false;
			playGUI(column);
		}
	}
	
	/**
	 * This inner class creates a ButtonListener that will play a column determined by an AI when a button is pushed.
	*/
	private class aiButtonListener extends CFGame implements ActionListener{
		
		/**
	     * This method plays a column.
	     * @param e: ActionEvent that allows the button to have internal properties
	     * @return void
	    */
		public void actionPerformed(ActionEvent e){
			if(ai1_turn) {
				int m1 = ai1.nextMove(this);
				playGUI(m1);
				play(m1);
				ai1_turn = false;
			}
			else {
				int m2 = ai2.nextMove(this);
				playGUI(m2);
				play(m2);
				ai1_turn = true;
			}				
		}
	}
	
	/**
	 * This inner class creates a ButtonListener that will start a new game if pressed.
	*/
	private class newGameButtonListener extends CFGame implements ActionListener{
		
		/**
	     * This method starts a new GUI game when clicked.
	     * @param e: ActionEvent that allows the button to have internal properties
	     * @return void
	    */
		public void actionPerformed(ActionEvent e){		
			if(human_vs_AI)
				new GUICF(ai1);
			else if(human_vs_human)
				new GUICF();
			else
				new GUICF(ai1, ai2);
					
		}
	}
	
	/**
	 * This inner class holds creates a ButtonListener to close the GUI and terminate the program.
	*/
	private class CloseListener implements ActionListener{		

		/**
	     * This method closes the terminal when the button is clicked.
	     * @param e: ActionEvent that allows the button to have internal properties
	     * @return void
	    */
	    public void actionPerformed(ActionEvent e) {
	        System.exit(0);
	    }
	}
	
	private GameBoard this_board;				//current GUI game board
	private boolean human_vs_human = false;		//true if game is an human vs human game
	private boolean human_vs_AI = false;		//true if game is an AI vs AI game
	private boolean AI_vs_AI = false;			//true if game is an AI vs AI game
	private boolean ai_played;					//true if AI has just played a move
	private boolean ai1_turn = false;			//true if it is ai1's turn
	private boolean move_worked = true;			//true if a move works
	private boolean move_played;				//true if move has been played
	private boolean first_turn = false;			//true if it is the first turn of a game
	private int getState[][]; 					//current game board	
	CFPlayer ai1;								//instance of a certain AI interface
	CFPlayer ai2;								//instance of a certain AI interface
	
	JPanel button_panel = new JPanel();			//panels for the GUI
	JButton[] buttons;							//array of buttons for the GUI
	JButton game_button;							//gameButton that is used for AI vs. AI game
	JButton game_over;							//button that shows game is over and who the winner is
	JButton replay;								//button that will start a new game if clicked
	
	
	/**
	 * This constructor sets up a human vs. human GUI game.
	*/
	public GUICF () {

		human_vs_human = true;
		this_board = new GameBoard();
		
		button_panel = new JPanel();
		this_board.pane.add(button_panel, BorderLayout.NORTH);
		button_panel.setLayout(new GridLayout(1, getNumCols()));
		buttons = new JButton[getNumCols()]; 		//sets up buttons for the user to use when playing
	
		for(int i=0; i<getNumCols(); i++) {
			buttons[i] = new JButton("\u2193");
			buttons[i].addActionListener(new ButtonListener(i));		//gives buttons functionality when playing
			button_panel.add(buttons[i]);	 
		}
		
	}
	
	/**
	 * This constructor sets up a human vs. AI GUI game.
	 * @param ai: instance of certain ai class for human to play against
	*/
	public GUICF (CFPlayer ai) {	

		ai1 = ai;
		human_vs_AI = true;
		this_board = new GameBoard();
		Random rand = new Random();
		
		button_panel = new JPanel();
		this_board.pane.add(button_panel, BorderLayout.NORTH);
		button_panel.setLayout(new GridLayout(1, getNumCols()));
		buttons = new JButton[getNumCols()]; 		//sets up buttons for the user to use when playing
	
		for(int i=0; i<getNumCols(); i++) {
			
			buttons[i] = new JButton("\u2193");
			buttons[i].addActionListener(new ButtonListener(i));		//gives buttons functionality when playing
			button_panel.add(buttons[i]);	 
		}
		
		if(rand.nextInt(2)==0) {		//plays move if ai is determined to go first via random number generation		
			first_turn = true;
			playGUI(this.ai1.nextMove(this));	
		}
	}
	
	/**
	 * This constructor takes in an integer who's column is to be played.
	 * @param ai1: instance of certain ai class
	 * @param ai2: instance of certain ai class
	*/
	public GUICF(CFPlayer ai1, CFPlayer ai2) {
		
		this.ai1 = ai1;
		this.ai2 = ai2;
		AI_vs_AI = true;
		this_board = new GameBoard();
		Random rand = new Random();
		
		button_panel = new JPanel();
		this_board.pane.add(button_panel, BorderLayout.NORTH);
		button_panel.setLayout(new GridLayout(1,1));
		game_button = new JButton("Play"); 
		game_button.addActionListener(new aiButtonListener());		//'play' button created with functionality to make a move in the game
		button_panel.add(game_button);
	
		if(rand.nextInt(2)==0) 		//determines which ai goes first randomly
			ai1_turn = true;
	}
	
	/**
     * This method plays a move and colors GUI game board appropriately. If a human vs. AI game is being played, 
     * AI move will immediately be played after human move.
     * @param col: column to be played
     * @return boolean: true if move was played, false otherwise
    */
	private boolean playGUI (int col) {

		getState = getState();	
		if(col<0 || col>=getNumCols() || fullColumn(col)) 	//if column is full, no move is played
			return false;
		
		move_played = false;
		play(col);		//plays move so logic behind game is updated
		
		for(int row=0; row<getNumRows(); row++) {		//paints square that is most recently played either red or black depending on who's turn it is
			  if(getState[col][row]==0) {	
				  if(!isRedTurn()) { 
					  this_board.paint(col,row,1);
					  move_played = true;  
				  }
				  else if(isRedTurn() && !move_played) 
					  this_board.paint(col,row,-1);	
				  
				  if(isGameOver() && isWinner()) 					
						winnerButton();						

				  if(isGameOver() && !isWinner()) 		
					  drawButton();						
					
				  if(human_vs_AI && !first_turn) {		//if a human is playing, plays an ai move
					  if(!isGameOver() && move_worked && !ai_played) {		//plays ai move
					      ai_played = true;
						  playGUI(ai1.nextMove(this));
					  }	
				  }
				  
				  return true;
			  }
		}
		return true;
	}
	
	/**
     * This method makes a button that will display the winner and allow player to exit game or play new game 
     * @param none
     * @return void
    */
	private void winnerButton() {
		
		if(human_vs_AI || human_vs_human) {
			for(int i = 0; i<getNumCols(); i++) {
				buttons[i].setVisible(false); 
			}
		}
		else
			game_button.setVisible(false);
		
		button_panel= new JPanel();			
		this_board.pane.add(button_panel, BorderLayout.NORTH);
		button_panel.setLayout(new GridLayout(1,2));
		winnerName();										//creates a button with winner's name on it
		game_over.addActionListener(new CloseListener());		//adds functionality to close window when 'game over' button is pressed
		button_panel.add(game_over);
		button_panel.add(replayButton());
	}
	
	/**
     * This method makes a button that will show a draw occurred 
     * @param none
     * @return void
    */
	private void drawButton() {
		
		for(int i = 0; i<getNumCols(); i++) {
			
			buttons[i].setVisible(false); 
		}
		button_panel= new JPanel();
		this_board.pane.add(button_panel, BorderLayout.NORTH);
		button_panel.setLayout(new GridLayout(1,2));
		buttons[0] = new JButton("Game is Over! Draw");
		buttons[0].addActionListener(new CloseListener());
		button_panel.add(buttons[0]);
		button_panel.add(replayButton());
	}
	
	/**
     * This method makes a button that will allow user to start new game 
     * @param none
     * @return void
    */
	private JButton replayButton() {		//makes a button to display who the winner is
		
		replay = new JButton("Click to play a new Game");
		replay.addActionListener(new newGameButtonListener());
		
		return replay;
	}
	
	/**
     * This method creates text that will show in the winner Button if there is a winner
     * @param none
     * @return void
    */
	void winnerName() {
		
		if(human_vs_AI) {
			if(ai_played) 
				game_over = new JButton("Game is Over! " + ai1.getName() + " Wins. Click Here to Exit.");
				
			else
				game_over = new JButton("Game is Over! You Win. Click Here to Exit.");
		}
		else if(human_vs_human) {
			if(!isRedTurn()) 
				game_over = new JButton("Game is Over! Red Wins. Click Here to Exit.");
				
			else
				game_over = new JButton("Game is Over! Black Wins. Click Here to Exit.");
		}
		else if(AI_vs_AI){
			if(ai1_turn) 
				game_over = new JButton("Game is Over! " + ai1.getName() + " Wins. Click Here to Exit.");
				
			else
				game_over = new JButton("Game is Over! " + ai2.getName() + " Wins. Click Here to Exit.");
		}
		else {
			game_over = new JButton("Error");
		}
	}	
	
	/**
     * Inner class that sets up GUI board and contains logic behind certain squares being certain colors 
     * @param none
     * @return void
    */
	private class GameBoard extends javax.swing.JPanel {
		
		JFrame frame = new JFrame("Connect 4");								//creates a frame for the GUI
		Container pane = frame.getContentPane();							//creates pane for the GUI
		JPanel board = new JPanel();										//created a JPanel for the GUI
		JPanel panel_arr[][] = new JPanel[getNumCols()][getNumRows()];		//creates array of panels to represent the game squares
		
		/**
	     * This constructor sets up the GUI game board
	     * @param none
	    */
		private GameBoard () {
			
			frame.setSize(1000, 700);
			frame.setVisible(true);
			frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE );
			
			pane.add(board, BorderLayout.CENTER);
			board.setLayout(new GridLayout(6,7));
		
			for(int i = getNumRows()-1; i>=0; i--) {		//Format panels to make the game board look good
				for(int j = 0;j<getNumCols(); j++){
				
					panel_arr[j][i] = new JPanel();
					panel_arr[j][i].setBorder(BorderFactory.createLineBorder(Color.black));
					panel_arr[j][i].add(new JLabel());
					panel_arr[j][i].setBackground(Color.WHITE);
					board.add(panel_arr[j][i]);
				}
			}
		}
		
		/**
	     * This method paints squares red or black depending on who's turn it is and qhat column is played 
	     * @param none
	     * @return void
	    */
		private void paint (int x, int y, int color) {	

			if(color==1) 				
				panel_arr[x][y].setBackground(Color.RED);
			
			else 
				panel_arr[x][y].setBackground(Color.BLACK);
	
		}
	}
}