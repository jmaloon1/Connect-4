package hw4;

import javax.swing.*;
import java.awt.*;
import java.util.Random;
import java.awt.event.*;

public class GUICF extends CFGame{
		
	private class ButtonListener extends CFGame implements ActionListener{
		
		private int column;
		
		public ButtonListener(int i) {		//looks for what button was clicked
			
			column = i;
		}
		
		public void actionPerformed(ActionEvent e){	 //plays the column associated with whatever button was clicked
			
					firstTurn = false;
					aiPlayed = false;
					playGUI(column);
		}
	}
	
	private class aiButtonListener extends CFGame implements ActionListener{
		
		public void actionPerformed(ActionEvent e){		//plays a move in the ai vs. ai game after 'play' button is pressed

			if(ai1turn) {
				int m1 = ai1.nextMove(this);
				playGUI(m1);
				play(m1);
				ai1turn = false;
			}
			else {
				int m2 = ai2.nextMove(this);
				playGUI(m2);
				play(m2);
				ai1turn = true;
			}				
		}
	}
	
	private class newGameButtonListener extends CFGame implements ActionListener{
		
		public void actionPerformed(ActionEvent e){		//plays a move in the ai vs. ai game after 'play' button is pressed
			if(humanvsAI)
				new GUICF(ai1);
			else if(humanvsHuman)
				new GUICF();
			else
				new GUICF(ai1, ai2);
					
		}
	}
	
	private class CloseListener implements ActionListener{		//closes GUI terminal

	    public void actionPerformed(ActionEvent e) {
	        System.exit(0);
	    }
	}
	
	private GameBoard this_board;
	private boolean humanvsAI = false;
	private boolean humanvsHuman = false;
	private boolean aiPlayed;
	private boolean ai1turn = false;
	private boolean moveWorked = true;
	private boolean movePlayed;
	private boolean firstTurn = false;
	private int x[][]; 
	CFPlayer ai1;
	CFPlayer ai2;
	
	JPanel buttonPanel = new JPanel();
	JButton[] buttons;
	JButton gameButton;
	JButton gameOver;
	JButton replay;
	
	public GUICF () {		//sets up human vs. ai GUI game

		humanvsHuman = true;
		this_board = new GameBoard();
		Random rand = new Random();
		
		buttonPanel = new JPanel();
		this_board.pane.add(buttonPanel, BorderLayout.NORTH);
		buttonPanel.setLayout(new GridLayout(1, getNumCols()));
		buttons = new JButton[getNumCols()]; 		//sets up buttons for the user to use when playing
	
		for(int i=0; i<getNumCols(); i++) {
			
			buttons[i] = new JButton("\u2193");
			buttons[i].addActionListener(new ButtonListener(i));		//gives buttons functionality when playing
			buttonPanel.add(buttons[i]);	 
		}
		
	}
	
	public GUICF (CFPlayer ai) {		//sets up human vs. ai GUI game

		ai1 = ai;
		humanvsAI = true;
		this_board = new GameBoard();
		Random rand = new Random();
		
		buttonPanel = new JPanel();
		this_board.pane.add(buttonPanel, BorderLayout.NORTH);
		buttonPanel.setLayout(new GridLayout(1, getNumCols()));
		buttons = new JButton[getNumCols()]; 		//sets up buttons for the user to use when playing
	
		for(int i=0; i<getNumCols(); i++) {
			
			buttons[i] = new JButton("\u2193");
			buttons[i].addActionListener(new ButtonListener(i));		//gives buttons functionality when playing
			buttonPanel.add(buttons[i]);	 
		}
		
		if(rand.nextInt(2) == 0) {		//plays move if ai is determined to go first via random number generation		
			firstTurn = true;
			playGUI(this.ai1.nextMove(this));	
		}
	}
		
	public GUICF(CFPlayer ai1, CFPlayer ai2) {		//sets up ai vs. ai game
		
		this.ai1 = ai1;
		this.ai2 = ai2;
		this_board = new GameBoard();
		Random rand = new Random();
		
		buttonPanel = new JPanel();
		this_board.pane.add(buttonPanel, BorderLayout.NORTH);
		buttonPanel.setLayout(new GridLayout(1,1));
		gameButton = new JButton("Play"); 
		gameButton.addActionListener(new aiButtonListener());		//'play' button created with functionality to make a move in the game
		buttonPanel.add(gameButton);
	
		if(rand.nextInt(2) == 0) 		//determines which ai goes first randomly
			ai1turn = true;
	}
	
	private boolean playGUI (int col) {
		//System.out.println("column " + col);
		x = getState();	
		if(col<0 || col>=getNumCols() || x[col][getNumRows()-1]!=0) 	//if column is full, no move is played
			  return false;
		
		movePlayed = false;
		play(col);		//plays move so logic behind game is updated
		for(int row=0; row<getNumRows(); row++) {		//paints square that is most recently played either red or black depending on who's turn it is
			  if(x[col][row] == 0) {	
				  if(!isRedTurn()) { 
					  this_board.paint(col,row,1);
					  movePlayed = true;  
				  }
				  else if(isRedTurn() && !movePlayed) 
					  this_board.paint(col,row,2);	
				  
				  if(isGameOver() && isWinner()) 			//checks if there is a winner			
						winnerButton();						//calls the button that replaces game button(s) to display the winner and close the game

					if(isGameOver() && !isWinner()) 		//checks to see if the game is a draw
						drawButton();						//calls the button that replaces game button(s) to display "draw" and close the game
					
					if(humanvsAI && !firstTurn) {		//if a human is playing, plays an ai move
						if(!isGameOver() && moveWorked && !aiPlayed) {		//plays ai move
							aiPlayed = true;
							playGUI(ai1.nextMove(this));
						}	
					}
					else if(humanvsHuman && !firstTurn) {		//if a human is playing, plays an ai move
						if(!isGameOver() && moveWorked && !aiPlayed) {		//plays ai move
							
						}	
					}
				  return true;
			  }
		}
		return true;
	}
	
	private class GameBoard extends javax.swing.JPanel {		//sets up empty game board
		
		JFrame frame = new JFrame("Connect 4");				//creates a frame for the GUI
		Container pane = frame.getContentPane();
		JPanel board = new JPanel();
		JPanel panelArr[][] = new JPanel[getNumCols()][getNumRows()];	//creates array of panels to represent the game squares
		
		private GameBoard () {
			
			frame.setSize(1000, 700);
			frame.setVisible(true);
			frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE );
			
			pane.add(board, BorderLayout.CENTER);
			board.setLayout(new GridLayout(6,7));
		
			for(int i = getNumRows()-1; i>=0; i--) {		//Format panels to make the game board look good
				for(int j = 0;j<getNumCols(); j++){
				
					panelArr[j][i] = new JPanel();
					panelArr[j][i].setBorder(BorderFactory.createLineBorder(Color.black));
					panelArr[j][i].add(new JLabel());
					panelArr[j][i].setBackground(Color.WHITE);
					board.add(panelArr[j][i]);
				}
			}
		}
		
		private void paint (int x, int y, int color) {		//paints a given square a certain color

			if(color == 1) 				
				panelArr[x][y].setBackground(Color.RED);
			
			else 
				panelArr[x][y].setBackground(Color.BLACK);
	
		}
	}
	
	void tempPrint() {					//prints the board to the console
		
		int x[][] = this.getState();
		
		for(int i=5; i >= 0; i--) {
			for(int j = 0;j<getNumCols();j++) {
				System.out.print(x[j][i] + "  ");
			}
			System.out.println("");
		}
		System.out.println("");
	}
	
	void winnerButton() {		//creates a button with the winner's name on it to replace game button(s)
		
		if(humanvsAI || humanvsHuman) {
			for(int i = 0; i<getNumCols(); i++) {
				buttons[i].setVisible(false); 
			}
		}
		else
			gameButton.setVisible(false);
		
		buttonPanel= new JPanel();			
		this_board.pane.add(buttonPanel, BorderLayout.NORTH);
		buttonPanel.setLayout(new GridLayout(1,2));
		winnerName();										//creates a button with winner's name on it
		gameOver.addActionListener(new CloseListener());		//adds functionality to close window when 'game over' button is pressed
		buttonPanel.add(gameOver);
		buttonPanel.add(replayButton());
	}
	
	void drawButton() {		//creates a button if the game is a draw to display this and close the window. Works similarly to 'winnerButton'
		
		for(int i = 0; i<getNumCols(); i++) {
			
			buttons[i].setVisible(false); 
		}
		buttonPanel= new JPanel();
		this_board.pane.add(buttonPanel, BorderLayout.NORTH);
		buttonPanel.setLayout(new GridLayout(1,2));
		buttons[0] = new JButton("Game is Over! Draw");
		buttons[0].addActionListener(new CloseListener());
		buttonPanel.add(buttons[0]);
		buttonPanel.add(replayButton());
	}
	
	void winnerName() {		//makes a button to display who the winner is
		
		if(humanvsAI) {
			if(aiPlayed) 
				gameOver = new JButton("Game is Over! " + ai1.getName() + " Wins. Click Here to Exit.");
				
			else
				gameOver = new JButton("Game is Over! You Win. Click Here to Exit.");
		}
		else if(humanvsHuman) {
			if(aiPlayed) 
				gameOver = new JButton("Game is Over! You Win. Click Here to Exit.");
				
			else
				gameOver = new JButton("Game is Over! You Win. Click Here to Exit.");
		}
		else {
			
			if(ai1turn) 
				gameOver = new JButton("Game is Over! " + ai1.getName() + " Wins. Click Here to Exit.");
				
			else
				gameOver = new JButton("Game is Over! " + ai2.getName() + " Wins. Click Here to Exit.");
		}		
	}	
	
	JButton replayButton() {		//makes a button to display who the winner is
		
		replay = new JButton("Click to play a new Game");
		replay.addActionListener(new newGameButtonListener());
		
		return replay;
	}
}