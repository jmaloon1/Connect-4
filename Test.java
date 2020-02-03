package hw4;


import java.util.Scanner;
import hw4.CFPlayer;
import hw4.RandomAI;

import hw4.JackMaloonAI;
import hw4.ConsoleCF;
import hw4.GUICF;


public class Test {
  public static void main(String[] args) {
	System.out.println("Press '1' for a GUI game against the AI , '2' for a monte carlo simulation of my AI vs. random AI, or '3' for a console game against the AI: ");
    Scanner reader = new Scanner (System.in);
    int gameMode = reader.nextInt();
    
    
    
    if (gameMode==1) {			//starts a GUI game with human playing ai, can also be changed to make it an ai vs. ai game
      new GUICF(new JackMaloonAI());
      //new GUICF();
    } 
    else if (gameMode==2) {		//starts a console game with ai vs. ai, checks to see is personal ai wins and returns win probability with monte carlo simulation
      
      CFPlayer ai1 = new JackMaloonAI();
      CFPlayer ai2 = new TrialAI();
      int n = 1;
      int ai1winCount = 0;
      int ai2winCount = 0;
      long startTime = System.currentTimeMillis();
      for (int i=0; i<n; i++) {
        ConsoleCF game = new ConsoleCF(ai1, ai2);
        game.playOut();
        
        String winner = game.getWinner();
        
        if(winner == ai1.getName()) {
          ai1winCount++;
          //System.out.println("ai1 " + ai1.getName());
        }
        if(winner == ai2.getName()) {
        	
            ai2winCount++;
            game.printGameMoves();
            break;
            //System.out.println("ai2 " + ai2.getName());
        }
        
      }
      long endTime = System.currentTimeMillis();

     System.out.println("Game took " + (endTime - startTime) + " milliseconds");

      System.out.println(((double) ai1winCount)/n);
      System.out.println(((double) ai2winCount)/n);
      System.out.println(((double) (n-ai1winCount-ai2winCount)/n));
    } 
    else {													//starts a human vs ai console game
      
      ConsoleCF game = new ConsoleCF();
      game.playOut();
      System.out.println(game.getWinner() + " has won.");
    } 
  }
}
