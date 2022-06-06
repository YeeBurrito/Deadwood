import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class SceneCard {
    // These 3 currently aren't being used but will be necessary for the GUI
    private String name;
    private int sceneNumber;
    private String sceneDescription;
    private int budget;
    private String image;
    private BufferedImage cardFront;
    // This hashMap stores all the roles on the sceneCard as the keys, and the value
    // is either null or the number representing the player on the role, however
    // this is public unlike the one on Set as Set will need to access this
    private HashMap<Role, Integer> onCardRoles;

    // Constructor
    public SceneCard(String name, int sceneNumber, String sceneDescription, int budget, String image, 
            HashMap<Role, Integer> onCardRoles) throws IOException {
        this.name = name;
        this.sceneNumber = sceneNumber;
        this.sceneDescription = sceneDescription;
        this.budget = budget;
        this.image = image;
        this.onCardRoles = onCardRoles;
        String cardIndex = "./images/cards/cards/" + image;
        this.cardFront = ImageIO.read(new File(cardIndex));
    }

    // Returns the budget of the sceneCard
    public int getBudget() {
        return this.budget;
    }

    // Gets the image associated with the sceneCard
    public BufferedImage getCardFront(){
        return this.cardFront;
    }

    // Print roles available to player trying to take a role
    public void printStarRoles(int playerRank) {
        System.out.println("\nAvailable Star Roles are: ");
        for (Role key : onCardRoles.keySet()) {
            if ((playerRank >= key.getRank()) && (onCardRoles.get(key) == -1)) {
                key.printRole();
                System.out.println("");
            }
        }
    }

    // Get roles available to player trying to take a role
    public String[] getStarRoles(int playerRank) {
        ArrayList<String> stars = new ArrayList<String>();
        for (Role key : onCardRoles.keySet()) {
            if ((playerRank >= key.getRank()) && (onCardRoles.get(key) == -1)) {
                stars.add(key.getRoleName());
            }
        }
        // Convert arraylist to array
        String[] starRoles = new String[stars.size()];
        for (int i = 0; i < starRoles.length; i++) {
            starRoles[i] = stars.get(i);
        }
        return starRoles;
    }

    // Returns a role from the hashmap based on its name
    public Role getRole(String roleName) {
        for (Role key : onCardRoles.keySet()) {
            if (roleName.equals(key.getRoleName().toLowerCase())) {
                return key;
            }
        }
        return null;
    }

    // Returns an array consisting of all the starring roles
    public HashMap<Role, Integer> getRoles() {
        return this.onCardRoles;
    }

    // Updates the value of a role with the number representing the player on it
    public void updateRole(String desiredRole, int playerNumber) {
        for (Role key : onCardRoles.keySet()) {
            if (key.getRoleName().toLowerCase().equals(desiredRole.toLowerCase())) {
                onCardRoles.put(key, playerNumber);
            }
        }
    }

    // Payout for players on the sceneCard. Returns boolean because if there are
    // players on card that get paid during sceneWrap then players off card can
    // receive bonuses as well. This boolean allows that to happen
    public boolean onCardpayout() {
        boolean isStars = false;
        // Get players on card
        Board b = Board.getBoard();
        // Get players from hash map
        ArrayList<Player> playersToPay = new ArrayList<Player>();
        ArrayList<int[]> orderedRoles = new ArrayList<int[]>();
        for (Role key : onCardRoles.keySet()) {
            int[] playerRanks = { onCardRoles.get(key), key.getRank() };
            orderedRoles.add(playerRanks);
        }
        Collections.sort(orderedRoles, Comparator.comparingInt(o -> o[1]));
        // reverse the array
        int[] t;
        for (int i = 0; i < orderedRoles.size() / 2; i++) {
            t = orderedRoles.get(i);
            orderedRoles.set(i, orderedRoles.get(orderedRoles.size() - i - 1));
            orderedRoles.set(orderedRoles.size() - i - 1, t);
        }
        for (int i = 0; i < orderedRoles.size(); i++) {
            if (orderedRoles.get(i)[0] == -1) {
                playersToPay.add(null);
            } else {
                playersToPay.add(b.getPlayer(orderedRoles.get(i)[0]));
            }
        }
        // calculate payout from payout roll
        int[] payouts = Dice.payoutRoll(budget);
        // pay them
        for (int i = 0; i < budget; i++) {
            Player toPay = playersToPay.get(i % onCardRoles.keySet().size());
            if (toPay != null) {
                int amount = payouts[i % onCardRoles.keySet().size()];
                toPay.pay(amount, 0);
                System.out.println(toPay.getName() + " has been paid $" + amount
                        + " as a bonus for the scene wrapping!");
            }
        }
        // reset the players roles and place them back in the room waiting area
        View v = View.getView();
        for (int i = 0; i < playersToPay.size(); i++) {
            Player toReset = playersToPay.get(i);
            if (toReset != null) {
                playersToPay.get(i).resetRole();
                v.placePlayerInRoom(playersToPay.get(i).getName(), b.getRoom(playersToPay.get(i).getLocation()).getName());
                isStars = true;
            }
        }
        return isStars;
    }
}
