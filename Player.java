import java.io.IOException;
import java.util.ArrayList;

class Player {
    private String name;
    private boolean hasMoved;
    private boolean hasTakenAction;
    private boolean hasRole;
    private int rank;
    private int location;
    private String role;
    private int balance;
    private int credits;
    private int rehearsalTokens;
    private boolean isTurn;
    private char color;
    // Offset refers to where their stats should appear on the statsPanel
    private int statOffset;
    private int lastRollResult;
    // Represents their unique number when they were instantiated, important for offset indexing
    private int signature;

    // Constructor
    public Player(int num, int i) {
        // Get the name of the player
        try {
            this.name = Controller.getName(i);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // Set all attributes to default values, some depending on number of players.
        this.hasMoved = false;
        this.hasTakenAction = false;
        this.hasRole = false;
        if (num > 6) {
            this.rank = 2;
        } else {
            this.rank = 1;
        }
        this.location = 0;
        this.role = null;
        this.balance = 0;
        if (num == 5) {
            this.credits = 2;
        } else if (num == 6) {
            this.credits = 4;
        } else {
            this.credits = 0;
        }
        this.rehearsalTokens = 0;
        this.isTurn = false;
        this.color = Controller.getColor(i);
        this.statOffset = (i - 1) * 232;
        this.lastRollResult = 0;
        this.signature = i;
    }

    // resets the taken action of a player
    public void setTakenAction() {
        this.hasTakenAction = false;
    }

    // End the player's turn
    public void endTurn() {
        this.hasMoved = false;
        this.hasTakenAction = false;
        this.isTurn = false;
    }

    // resets the players turn logic
    public void startTurn() {
        this.isTurn = true;
    }

    // Sets a flag for the players' turn
    public boolean isTurn() {
        return this.isTurn;
    }

    // Move the player
    public void move(int location) throws IOException {
        Board b = Board.getBoard();
        this.location = location;
        Room pRoom = b.getRoom(location);
        // display the player in the new room
        View v = View.getView();
        v.placePlayerInRoom(this.name, pRoom.getName());
        // Print the set where the player is
        System.out.println("\nYou are now in: " + pRoom.getName());
        // If they are in a set, flip it and print its information
        if (pRoom instanceof Set) {
            Set pSet = ((Set) pRoom);
            // If the scene card for the set isn't flipped yet, flip it
            if (!pSet.isFlipped()) {
                pSet.flip();
            }
            ((Set) pRoom).printSet();
        }
        // Update moved flag for turn logic
        this.hasMoved = true;
        System.out.print(name + " has moved!");
    }

    // Take an available role
    public void takeExtraRole(String role) {
        Board b = Board.getBoard();
        this.role = role.toLowerCase();
        // Update the set
        ((Set) b.getRoom(location)).updateRole(role, b.getPlayerIndex(this));
        // Update flags for turn logic
        hasRole = true;
        hasTakenAction = true;
        // Update view
        View v = View.getView();
        v.takeExtraRole(this.name, b.getRoom(location).getName(), this.role);
        endTurn();
    }

    // take a star role on a scenecard
    public void takeStarRole(String role) {
        Board b = Board.getBoard();
        this.role = role.toLowerCase();
        // Update the set
        ((Set) b.getRoom(location)).getScene().updateRole(role, b.getPlayerIndex(this));
        // update flags for turn logic
        hasRole = true;
        hasTakenAction = true;
        // Update view
        View v = View.getView();
        v.takeStarRole(name, b.getRoom(location).getName(), this.role);
        endTurn();
    }

    // Returns the name of the players's role
    public String getRole() {
        return this.role;
    }

    // Act in a given role
    public void act() throws IOException {
        Board b = Board.getBoard();
        hasTakenAction = true;
        int rollResult = Dice.actRoll(rehearsalTokens);
        this.lastRollResult = rollResult;
        Role curRole = ((Set) b.getRoom(location)).getRole(role);
        // For success
        if (rollResult >= ((Set) b.getRoom(location)).getSceneBudget()) {
            View v = View.getView();
            System.out.println("Your roll plus practice chips resulted in a: " + rollResult + ", Success!");
            // Offcard bonuses
            if (curRole.getRoleType().equals("Extra")) {
                System.out.println("You have have been paid $1 and 1 credit.");
                this.balance++;
                this.credits++;
                ((Set) b.getRoom(location)).decrementShotCounters();
                v.updatePlayerStats(this);
                // Oncard bonuses
            } else {
                System.out.println("You have have been paid $2.");
                this.balance += 2;
                ((Set) b.getRoom(location)).decrementShotCounters();
                v.updatePlayerStats(this);
            }
            // Update shotCounter
            v.drawShotCounter(b.getRoom(location).getName(), ((Set)b.getRoom(location)).getShotsLeft(), ((Set)b.getRoom(location)).getMaxShots());
            // Failure
        } else {
            System.out.println("Your roll plus practice chips resulted in a: " + rollResult + ", Fail!");
            if (curRole.getRoleType().equals("Extra")) {
                View v = View.getView();
                this.balance++;
                v.updatePlayerStats(this);
            }
        }
        // End turn once acting is over
        this.endTurn();
        return;
    }

    // Rehearse for a role
    public void rehearse() {
        this.rehearsalTokens++;
        System.out.println("You now have " + rehearsalTokens + " rehearsal tokens.");
        View v = View.getView();
        // v.reDisplayPlayerStats(this);
        v.updatePlayerStats(this);
        hasTakenAction = true;
        this.endTurn();
        return;
    }

    // Upgrade a player's rank
    public void upgrade(int desiredRank, int dollars, int creds) throws IOException {
        System.out.println("Congratulations, you are now rank " + desiredRank + "!");
        this.rank = desiredRank;
        this.balance -= dollars;
        this.credits -= creds;
        View v = View.getView();
        v.upgradePImage(this);
        // v.reDisplayPlayerStats(this);
        v.updatePlayerStats(this);
        return;
    }

    // Reset the player's role
    public void resetRole() {
        this.rehearsalTokens = 0;
        this.hasRole = false;
        this.role = null;
        View v = View.getView();
        v.displayPlayerStats(this);
        // v.updatePlayerStats(this);
    }

    // Update players's currency based on input
    public void pay(int dollars, int creds) {
        this.balance += dollars;
        this.credits += creds;
        View v = View.getView();
        //v.reDisplayPlayerStats(this);
        v.updatePlayerStats(this);
    }

    // Returns the name of the player
    public String getName() {
        return this.name;
    }

    // Moves the player back to the trailer
    public void resetLocation() {
        this.location = 0;
    }

    public int getSignature() {
        return this.signature;
    }

    // returns the players location as an int for the index of the room in the
    // room[] for board
    public int getLocation() {
        return this.location;
    }

    // Returns the rank of the player as an int
    public int getRank() {
        return this.rank;
    }

    // Returns the number of rehearsal tokens the player has
    public int getRehearalTokens() {
        return this.rehearsalTokens;
    }

    public int getStatOffset() {
        return this.statOffset;
    }

    public int getLastRollResult(){
        return this.lastRollResult;
    }

    // Gets all the actions currently available to a player and returns it as an
    // arraylist of strings
    public ArrayList<String> getAvailableActions() {
        Board b = Board.getBoard();
        ArrayList<String> actions = new ArrayList<String>();
        if (!this.hasMoved && !this.hasRole) {
            actions.add("MOVE");
        }
        if (this.location > 1 && ((Set) b.getRoom(location)).getScene() != null && !this.hasRole
                && !((((Set) b.getRoom(this.location)).getExtraRoles(this.rank).length == 0)
                        && (((Set) b.getRoom(this.location)).getScene().getStarRoles(this.rank).length == 0))) {
            actions.add("TAKE ROLE");
        }
        if (this.hasRole && !hasTakenAction) {
            actions.add("ACT");
            if (this.rehearsalTokens < b.getMaxRehearsalTokens(this.location)) {
                actions.add("REHEARSE");
            }
        }
        if (this.location == 1 && rank < 6 && ((Office) b.getRoom(1)).canUpgrade(rank, balance, credits)) {
            actions.add("UPGRADE");
        }
        actions.add("Controller SET");
        actions.add("Controller PLAYER");
        actions.add("END TURN");
        actions.add("EXIT");
        return actions;
    }

    // Prints all the information about a player
    public void printPlayer() {
        Board b = Board.getBoard();
        System.out.println(this.getName().toUpperCase() + " is at " + b.getLocation(this.location) + ".");
        System.out.println("They are rank " + this.rank + ".");
        System.out.println("They have " + this.balance + " dollars, " + this.credits + " credits, and "
                + this.rehearsalTokens + " rehearsal tokens.");
        if (this.hasRole) {
            System.out.println("They are currently playing in role " + this.role.toUpperCase() + ".");
        } else {
            System.out.println("They currently are not playing in any roles.");
        }
    }

    // returns the balance (money) of current player
    public int getBalance() {
        return this.balance;
    }

    // returns the credits of current player
    public int getCredits() {
        return this.credits;
    }

    public char getColor() {
        return this.color;
    }
}
