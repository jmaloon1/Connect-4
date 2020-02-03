package hw4;

/**
 * This interface contains two methods that will be used by other classes:
 * nextMove: move that will be played if legal
 * getName(): name of given participant
*/
public interface CFPlayer {
	
	int nextMove(CFGame g);
    String getName();
}