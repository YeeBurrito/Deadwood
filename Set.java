import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import javax.imageio.ImageIO;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Set extends Room {
    private String name;
    private String[] neighbors;
    private int[] area;
    // Hold max shots for set for when resetting
    private int maxShots;
    // Tracks how many shots are left on the set
    private int shotsLeft;
    // This hashMap stores all the roles on the sceneCard as the keys, and the value
    // is either null or the number representing the player on the role
    private HashMap<Role, Integer> offCardRoles;
    // Every set has a scene card
    private SceneCard scene;
    private boolean isFlipped;
    private int[][] shotArea;

    // Constructor
    public Set(String name, String[] neighbors, int shotsLeft, int[] area, int[][] shotArea) {
        super(name, neighbors, area);
        this.shotsLeft = shotsLeft;
        this.maxShots = shotsLeft;
        this.shotArea = shotArea;
        offCardRoles = new HashMap<Role, Integer>();
    }

    // Returns an array consisting of all the extra roles
    public HashMap<Role, Integer> getRoles() {
        return this.offCardRoles;
    }

    // Returns the area values for the shot counters
    public int[][] getShotArea() {
        return this.shotArea;
    }

    // Returns number of shots left
    public int getShotsLeft() {
        return this.shotsLeft;
    }

    // Returns the max number of shots for the set
    public int getMaxShots() {
        return this.maxShots;
    }

    // Flips the sceneCard
    public void flip() throws IOException {
        isFlipped = !isFlipped;
        if(isFlipped() && (scene != null)){
            View v = View.getView();
            v.flipScene(this.getName(), this.getCardImage());
        }
    }

    // Returns a boolean on if the sceneCard is flipped or not
    public boolean isFlipped() {
        return isFlipped;
    }

    // uses the HashMap of players, calculates their payout and calls setter to
    // update player money/credits
    private void offCardPayout() {
        Board b = Board.getBoard();
        // Get players from hash map
        ArrayList<Player> playersToPay = new ArrayList<Player>();
        for (Role key : offCardRoles.keySet()) {
            if (offCardRoles.get(key) != -1) {
                playersToPay.add(b.getPlayer(offCardRoles.get(key)));
            }
        }
        // pay them and move them back to the room waiting area
        View v = View.getView();
        for (int i = 0; i < playersToPay.size(); i++) {
            int amount = ((Set) b.getRoom(playersToPay.get(i).getLocation())).getRole(playersToPay.get(i).getRole())
                    .getRank();
            playersToPay.get(i).pay(amount, 0);
            System.out.println(playersToPay.get(i).getName() + " has been paid $" + amount
                    + " as a bonus for the scene wrapping!");
            // reset the players roles and place them back in the room's waiting area
            playersToPay.get(i).resetRole();
            v.placePlayerInRoom(playersToPay.get(i).getName(), b.getRoom(playersToPay.get(i).getLocation()).getName());
        }
    }

    // Delete sceneCard once set wraps
    private void removeSceneCard() {
        this.scene = null;
        View v = View.getView();
        v.removeSceneCard(this.getName());
    }

    // Decrement shot counters upon successful act
    public void decrementShotCounters() throws IOException {
        this.shotsLeft--;
        if (shotsLeft > 1) {
            System.out.println("There are " + shotsLeft + " shots left!");
        }
        if (shotsLeft == 1) {
            System.out.println("There is " + shotsLeft + " shot remaining!");
        }
        if (shotsLeft == 0) {
            sceneWrap();
        }
    }

    // First do payouts
    // Make sure to have check for endDay
    private void sceneWrap() throws IOException {
        Board b = Board.getBoard();
        // offCardPayout
        // If there's players on card, then pay extras as well
        if (scene.onCardpayout()) {
            // onCardpayout
            offCardPayout();
        } else {
            // If no players on the sceneCard
            System.out.println("No stars on set, so extras do note receive a bonus!");
            // Make sure to still reset the roles of the extras
            View v = View.getView();
            for (Role key : offCardRoles.keySet()) {
                if (offCardRoles.get(key) != -1) {
                    // Reset the player
                    b.getPlayer(offCardRoles.get(key)).resetRole();
                    // move the players to the waiting area of the room
                    v.placePlayerInRoom(b.getPlayer(offCardRoles.get(key)).getName(), this.getName());
                }
            }

        }
        removeSceneCard();
        // reset the hashmap so that it doesn't think that there's players on it
        reset();
        b.decrementActiveSets();
    }

    // Print roles available to player trying to take a role
    public void printExtraRoles(int playerRank) {
        System.out.println("\nAvailable Extra Roles are: ");
        for (Role key : offCardRoles.keySet()) {
            if ((playerRank >= key.getRank()) && (offCardRoles.get(key) == -1)) {
                key.printRole();
                System.out.println("");
            }
        }
        scene.printStarRoles(playerRank);
    }

    // Get roles available to player trying to take a role
    public String[] getExtraRoles(int playerRank) {
        ArrayList<String> extras = new ArrayList<String>();
        for (Role key : offCardRoles.keySet()) {
            if ((playerRank >= key.getRank()) && (offCardRoles.get(key) == -1)) {
                extras.add(key.getRoleName());
            }
        }
        // Convert arraylist to array
        String[] extraRoles = new String[extras.size()];
        for (int i = 0; i < extraRoles.length; i++) {
            extraRoles[i] = extras.get(i);
        }
        return extraRoles;
    }

    // Returns a Role from the hashmap based on its name
    public Role getRole(String roleName) {
        for (Role key : offCardRoles.keySet()) {
            if (roleName.equals(key.getRoleName().toLowerCase())) {
                return key;
            }
        }
        return scene.getRole(roleName);
    }

    // adds a role to the hashmap
    public void addRole(Role r) {
        offCardRoles.put(r, -1);
        return;
    }

    // Updates the value of the desiredRole with the number representing the player
    // that took the role
    public void updateRole(String desiredRole, int playerNumber) {
        for (Role key : offCardRoles.keySet()) {
            if (key.getRoleName().toLowerCase().equals(desiredRole.toLowerCase())) {
                offCardRoles.put(key, playerNumber);
            }
        }
    }

    // Sets the sets sceneCard
    public void setSceneCard(SceneCard card) {
        this.scene = card;
    }

    // Returns the sets sceneCard
    public SceneCard getScene() {
        return this.scene;
    }

    public Boolean hasScene() {
        if(this.scene != null) {
            return true;
        }
        return false;
    }

    // Removes sceneCard from the board, and removes all players from set
    public void reset() {
        this.scene = null;
        this.isFlipped = false;
        // For each role on the set, set its value back to -1 as all players are moved
        // back to trailer
        for (Role key : offCardRoles.keySet()) {
            offCardRoles.put(key, -1);
        }
    }

    // Resets the shotCounters for the scene
    public void resetShots() {
        this.shotsLeft = maxShots;
    }

    // Returns an int representing the budget of the sets sceneCard
    public int getSceneBudget() {
        if (!this.isFlipped || this.scene == null) {
            return 0;
        } else {
            return this.scene.getBudget();
        }
    }

    // Print information about the set
    public void printSet() {
        if (this.scene == null) {
            System.out.println(this.getName() + " has " + shotsLeft + " shots remaining, so it is inactive.\n");
            return;
        }
        Board b = Board.getBoard();
        System.out.println(this.getName() + " has " + shotsLeft + " shots remaining.");
        if (isFlipped) {
            System.out.println("\nThe budget of this set is: " + this.getSceneBudget());
        }
        System.out.println("\nThere are " + offCardRoles.keySet().size() + " extra roles on this set:");
        // print each extra role and the player that is on each one
        for (Role key : offCardRoles.keySet()) {
            key.printRole();
            int ifP = offCardRoles.get(key);
            if (ifP == -1) {
                System.out.println("This role is empty.");
            } else {
                System.out.println(b.getPlayer(ifP).getName().toUpperCase() + " is on this role.");
            }
        }
        if (!isFlipped) {
            System.out.println(this.getName() + "'s card has not been revealed yet.");
        } else {
            HashMap<Role, Integer> sceneRoles = scene.getRoles();
            System.out.println("\nThere are " + sceneRoles.keySet().size() + " star roles on this set:");
            // print each star role and the player that is on each one
            for (Role key : sceneRoles.keySet()) {
                key.printRole();
                int ifP = sceneRoles.get(key);
                if (ifP == -1) {
                    System.out.println("This role is empty.");
                } else {
                    System.out.println(b.getPlayer(ifP).getName().toUpperCase() + " is on this role.");
                }
            }
        }
        System.out.println("\n");
    }

    // Get the image of the set's scenecard, either front or back depending if the card is flipped
    public BufferedImage getCardImage() throws IOException {
        if (isFlipped) {
            return scene.getCardFront();
        }
        return ImageIO.read(new File("./images/CardBack-small.jpg"));
    }
}