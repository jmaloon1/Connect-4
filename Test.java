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
    } 
    else if (gameMode==2) {		//starts a console game with ai vs. ai, checks to see is personal ai wins and returns win probability with monte carlo simulation
    
      CFPlayer ai1 = new JackMaloonAI();
      CFPlayer ai2 = new RandomAI();
      int n = 100000;
      int ai1winCount = 0;
      int ai2winCount = 0;
      
      for (int i=0; i<n; i++) {
        ConsoleCF game = new ConsoleCF(ai1, ai2);
        game.playOut();

        if(game.getWinner() == ai1.getName())
          ai1winCount++;
        if(game.getWinner() == ai2.getName())
            ai2winCount++;
      }
      

      System.out.println(((double) ai1winCount)/n);
      System.out.println(((double) ai2winCount)/n);
      System.out.println(((double) (n-ai1winCount-ai2winCount)/n));
    } 
    else {													//starts a human vs ai console game
      System.out.println("Press the numbers 1-7 to play a move in the associated column (leftmost is 1, rightmost is 7)");	
      ConsoleCF game = new ConsoleCF(new JackMaloonAI());
      game.playOut();
      System.out.println(game.getWinner() + " has won.");
    } 
  }
}
