import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

// This will contain our system logic
class Deadwood {
    private static Deck d;
    private static int day;
    private static int maxDays;
    private static int numPlayers;
    private static int pTurn;

    public static void main(String[] args) throws ParserConfigurationException, IOException, InterruptedException {

        // Print welcome message
        System.out.println(
                "\n\n\n        :::       ::: :::::::::: :::        ::::::::   ::::::::    :::   :::   ::::::::::      ::::::::::: ::::::::                                                                     \n       :+:       :+: :+:        :+:       :+:    :+: :+:    :+:  :+:+: :+:+:  :+:                 :+:    :+:    :+:                                                                     \n      +:+       +:+ +:+        +:+       +:+        +:+    +:+ +:+ +:+:+ +:+ +:+                 +:+    +:+    +:+                                                                      \n     +#+  +:+  +#+ +#++:++#   +#+       +#+        +#+    +:+ +#+  +:+  +#+ +#++:++#            +#+    +#+    +:+                                                                       \n    +#+ +#+#+ +#+ +#+        +#+       +#+        +#+    +#+ +#+       +#+ +#+                 +#+    +#+    +#+                                                                        \n    #+#+# #+#+#  #+#        #+#       #+#    #+# #+#    #+# #+#       #+# #+#                 #+#    #+#    #+#                                                                         \n    ###   ###   ########## ########## ########   ########  ###       ### ##########          ###     ########                                                                           \n          :::::::::  ::::::::::     :::     :::::::::  :::       :::  ::::::::   ::::::::  :::::::::          :::::::: ::::::::::: :::    ::: ::::::::: ::::::::::: ::::::::   :::::::: \n         :+:    :+: :+:          :+: :+:   :+:    :+: :+:       :+: :+:    :+: :+:    :+: :+:    :+:        :+:    :+:    :+:     :+:    :+: :+:    :+:    :+:    :+:    :+: :+:    :+: \n        +:+    +:+ +:+         +:+   +:+  +:+    +:+ +:+       +:+ +:+    +:+ +:+    +:+ +:+    +:+        +:+           +:+     +:+    +:+ +:+    +:+    +:+    +:+    +:+ +:+         \n       +#+    +:+ +#++:++#   +#++:++#++: +#+    +:+ +#+  +:+  +#+ +#+    +:+ +#+    +:+ +#+    +:+        +#++:++#++    +#+     +#+    +:+ +#+    +:+    +#+    +#+    +:+ +#++:++#++   \n      +#+    +#+ +#+        +#+     +#+ +#+    +#+ +#+ +#+#+ +#+ +#+    +#+ +#+    +#+ +#+    +#+               +#+    +#+     +#+    +#+ +#+    +#+    +#+    +#+    +#+        +#+    \n     #+#    #+# #+#        #+#     #+# #+#    #+#  #+#+# #+#+#  #+#    #+# #+#    #+# #+#    #+#        #+#    #+#    #+#     #+#    #+# #+#    #+#    #+#    #+#    #+# #+#    #+#     \n    #########  ########## ###     ### #########    ###   ###    ########   ########  #########          ########     ###      ########  ######### ########### ########   ########       \n\n\n");
        // Begin the game
        setupGame();
    }

    // Setup the game
    public static void setupGame() throws ParserConfigurationException, IOException, InterruptedException {
        // Create the xml parser
        XMLParse xml = new XMLParse();
        // Create the board and the deck
        Board b = xml.parseBoard();
        d = xml.parseDeck();

        View v = View.getView();
        v.setupView();

        // Get the players
        numPlayers = Controller.getNumPlayers();

        // Create the players in the board
        b.setPlayers(numPlayers);

        // Set the max number of days
        if (numPlayers < 4) {
            maxDays = 3;
        } else {
            maxDays = 4;
        }

        // Randomize the order of players
        b.randomizePlayers();

        // Setup the board
        b.setBoard();
        // Start the first day
        day = 0;
        pTurn = 0;
        for (int i = 0; i < b.getPlayers().length; i++) {
            v.displayPlayerStats(b.getPlayer(i));
        }
        startDay(); 
        return;
    }

    // End the game
    public static void endGame() {
        Player p = calculateWinner();
        System.out.println("\nThe game has ended...");
        System.out.println("Congratulations to " + p.getName() + " for winning the game!");
        View v = View.getView();
        v.declareWinner(p.getName());
        System.exit(0);
        return;
    }

    // Start a day
    public static void startDay() throws IOException, InterruptedException {
        View v = View.getView();
        day = day + 1;
        if (day > maxDays) {
            endGame();
        } else {
            // If not last day, remove old sceneCard and deal new scenecards to each set
            d.dealCards();
            v.drawSceneCards();
        }
        // If not first day reset the player images, otherwise, initialize them
        if(day != 1){
            Board b = Board.getBoard();
            Player[] players = b.getPlayers();
             for(Player curPlayer: players){
                v.placePlayerInRoom(curPlayer.getName(), "Trailer");
            }
        } else  {  
            v.setPlayerImages();
        }
        System.out.println("It's a bright new day with endless possibilities!");
        // Game is ready to go, players can now take their turns.
        turnFlow();
        return;
    }

    // End a day
    public static void endDay() throws IOException, InterruptedException {
        Board b = Board.getBoard();
        // setBoard moves all the players to the trailer as well as resetting all of the
        // sets on the board
        System.out.println("The day is ending, the actors go back to the trailer to sleep.");
        b.setBoard();
        // start the next day
        startDay();
    }

    // Calculates and returns the winner of the game based off of their balance
    // (money), credits, and rank
    public static Player calculateWinner() {
        Board b = Board.getBoard();
        Player winner = b.getPlayer(0);
        Player[] players = b.getPlayers();
        int winBalance = (winner.getRank() * 5) + winner.getBalance() + winner.getCredits();
        for (int i = 1; i < players.length; i++) {
            if (((players[i].getRank() * 5) + players[i].getBalance() + players[i].getCredits()) > winBalance) {
                winner = players[i];
                winBalance = (players[i].getRank() * 5) + players[i].getBalance() + players[i].getCredits();
            }
        }
        return winner;
    }

    // controls the flow of turns, when not beginning/ending the day or game, the
    // program will be looping in here
    public static void turnFlow() throws IOException, InterruptedException {
        Board b = Board.getBoard();
        // While there are still more than 1 active sets, go through players' turns
        while (b.getActiveSets() > 1) {
            Player curPlayer = b.getPlayer(pTurn);
            curPlayer.startTurn();
            Controller.doTurn(curPlayer);
            pTurn = (pTurn + 1) % numPlayers;
        }
        // Once there is one active set left, end the day
        endDay();
    }
}
