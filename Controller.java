import java.io.*;
import java.util.*;

// Controller is to take and process user input as well as give them feedback such as invalid inputs
class Controller {
    static BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

    // Get the number of players that are going to play the game
    public static int getNumPlayers() throws InterruptedException {
        int numPlayers = 0;
        numPlayers = View.getNumPlayersInput();
        return numPlayers;
    }

    // Get the name of player i
    public static String getName(int i) throws InterruptedException {
        String name = "";
        name = View.getPlayerName(i);
        return name;
    }

    // Alerts a player that it is their turn and begins the turn
    public static void doTurn(Player curPlayer) throws IOException, InterruptedException {
        Controller.getAction(curPlayer);
    }

    // Prompts the player for their desired action from the set of actions they are
    // able to take, then upon valid input calls the respective method in the player
    // class
    public static void getAction(Player curPlayer) throws IOException, InterruptedException {
        while (curPlayer.isTurn()) {
            ArrayList<String> actions = curPlayer.getAvailableActions();
            System.out.println(actions);
            View.getPlayerAction(curPlayer, actions);
        }
        // Set the taken action flag back to false for when it's that players next turn
        curPlayer.setTakenAction();
    }

    public static char getColor(int i){
        char[] colors = new char[] {'b', 'c', 'g', 'o', 'p', 'r', 'v', 'w', 'y'};
        return colors[i];
        
    }
}