import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.event.*;

class View {
    private static View view = null;
    private static JFrame frame;
    private static JLayeredPane boardLP;
    private static JPanel container;
    private static JPanel controlPanel;
    private static JScrollPane statsPanel;
    // Need to store the JLabels for each player somewhere, so that their images can
    // be updated dynamically, the String is their name and the JLabel is their
    // associated image
    private static HashMap<String, JLabel> playerRepresentation = new HashMap<>();
    private static HashMap<String, JLabel> sceneRepresentation = new HashMap<>();
    private static HashMap<String, JTable> playerStats = new HashMap<>();
    private static HashMap<String, JLabel> shotCounters = new HashMap<>();
    protected static Integer selection;
    protected static String name;
    protected static boolean gotAction;
    protected static boolean endTurn;

    // Sets up the view itself
    public void setupView() throws IOException {
        // Initialize our outermost frame
        frame = new JFrame("Deadwood");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(null);

        // Container panel
        Container c = frame.getContentPane();
        container = new JPanel();
        container.setLayout(null);
        container.setSize(1920, 1080);
        container.setVisible(true);
        // Initialize our 3 main panes
        boardLP = new JLayeredPane();
        controlPanel = new JPanel();
        statsPanel = new JScrollPane();
        boardLP.setLayout(null);
        controlPanel.setLayout(null);
        statsPanel.setLayout(null);

        // Setup the boardPanel
        // Get board picture
        boardLP.setBounds(0, 0, 1200, 900);
        BufferedImage boardPicture = ImageIO.read(new File("./images/board.jpg"));
        boardLP.setPreferredSize(new Dimension(1200, 900));
        JLabel boardPicLabel = new JLabel(new ImageIcon(boardPicture));
        // Add picture to lp
        boardPicLabel.setBounds(0, 0, 1200, 900);
        boardPicLabel.setSize(boardPicLabel.getPreferredSize());
        boardLP.add(boardPicLabel, 0);

        controlPanel.setBounds(1200, 0, 720, 900);
        statsPanel.setBounds(0, 900, 1920, 140);

        container.add(boardLP);
        container.add(controlPanel);
        container.add(statsPanel);

        c.add(container);

        frame.setSize(1920, 1080);
        frame.setVisible(true);
    }

    // Implementation of the singleton class, gets view if it exists, if not, constructs one.
    public static View getView() {
        if (view == null)
            view = new View();

        return view;
    }

    // Popup message declaring the winner
    public void declareWinner(String winnerName) {
        JOptionPane winnerMessage = new JOptionPane();
        JOptionPane.showMessageDialog(frame, "The game has ended. Congratulations to " + winnerName + " for winning the game!");
    }

    // Draws the scene cards
    public void drawSceneCards() throws IOException {
        Board b = Board.getBoard();
        Room[] rooms = b.getRooms();
        for (int i = 0; i < rooms.length; i++) {
            if (rooms[i] instanceof Set) {
                Room curSet = rooms[i];
                int[] curSetArea = curSet.getArea();
                JLabel setImage = new JLabel(new ImageIcon(((Set) curSet).getCardImage()));
                setImage.setBounds(curSetArea[0], curSetArea[1], curSetArea[2], curSetArea[3]);
                setImage.setLocation(curSetArea[0], curSetArea[1] - 90);
                setImage.setSize(curSetArea[2] + 90, curSetArea[3] + 90);
                sceneRepresentation.put(curSet.getName(), setImage);
                boardLP.add(setImage, new Integer(1));
                // Draw the shot counters for the set
                drawShotCounter(curSet.getName(), ((Set)curSet).getShotsLeft(), ((Set)curSet).getMaxShots());
                boardLP.revalidate();
                boardLP.repaint();
            }
        }
        // Placing sceneCards on the bottom of the board messes with playerStats so the entire container must be revalidated
        container.repaint();
        container.revalidate();
    }

    // Removes a sceneCard from the board view
    public void removeSceneCard(String setName) {
        Board b = Board.getBoard();
        JLabel setImage = sceneRepresentation.get(setName);
        Room curSet = b.getRoomFromName(setName);
        int[] curSetArea = curSet.getArea();
        setImage.setIcon(null);
        setImage.setBounds(curSetArea[0], curSetArea[1], curSetArea[2], curSetArea[3]);
        setImage.setLocation(curSetArea[0], curSetArea[1] - 90);
        setImage.setSize(curSetArea[2] + 90, curSetArea[3] + 90);
        sceneRepresentation.put(curSet.getName(), setImage);
        boardLP.add(setImage, new Integer(1));
        boardLP.revalidate();
        boardLP.repaint();
    }

    // Flips the sceneCard when player gets to scene for the first time
    public void flipScene(String sceneName, Image cardFront) {
        JLabel setImage = sceneRepresentation.get(sceneName);
        setImage.setIcon(new ImageIcon(cardFront));
        boardLP.remove(sceneRepresentation.get(sceneName));
        sceneRepresentation.put(sceneName, setImage);
        boardLP.add(setImage);
        boardLP.revalidate();
        boardLP.repaint();
    }

    // Draws the shotCounter for each scene, as well as updates upon acting
    public void drawShotCounter(String setName, int shotsLeft, int maxShots) {
        Board b = Board.getBoard();
        Set curSet = ((Set) b.getRoomFromName(setName));
        int[][] shotArea = curSet.getShotArea();
        JLabel shotCounter = new JLabel();
        int shotArrayIndex = maxShots - shotsLeft;
        // If the scene is wrapping
        if (shotArrayIndex == maxShots){
            boardLP.remove(shotCounters.get(setName));
        }
        // If the shotcounter has already been initialized
        else if (shotCounters.containsKey(setName)) {
            shotCounter = shotCounters.get(setName);
            shotCounter.setBounds(shotArea[shotArrayIndex][0], shotArea[shotArrayIndex][1], shotArea[shotArrayIndex][2], shotArea[shotArrayIndex][3]);
            shotCounter.setLocation(shotArea[shotArrayIndex][0], shotArea[shotArrayIndex][1]);
            boardLP.remove(shotCounters.get(setName));
            shotCounters.put(setName, shotCounter);
            boardLP.add(shotCounter, new Integer(1));
        } else {
            // If it hasn't been initialized, make a new one
            shotCounter = new JLabel(new ImageIcon("./images/shot.png"));
            shotCounter.setBounds(shotArea[shotArrayIndex][0], shotArea[shotArrayIndex][1], shotArea[shotArrayIndex][2], shotArea[shotArrayIndex][3]);
            shotCounter.setLocation(shotArea[shotArrayIndex][0], shotArea[shotArrayIndex][1]);
            // JPG file says it's 42x42
            shotCounter.setSize(42, 42);
            shotCounters.put(setName, shotCounter);
            boardLP.add(shotCounter, new Integer(1));
        }
        boardLP.revalidate();
        boardLP.repaint();
    }

    // Update curPlayer's die image when they upgrade
    public void upgradePImage(Player curPlayer) throws IOException {
        Board b = Board.getBoard();
        String playerName = curPlayer.getName();
        // Define where the Label should be
        int[] roomArea = b.getRoom(curPlayer.getLocation()).getArea();
        int[] offSetPoints = b.getRoom(curPlayer.getLocation()).getCurOffSetPoints(curPlayer.getSignature());
        JLabel playerImageJLabel = playerRepresentation.get(playerName);
        playerImageJLabel.setIcon(new ImageIcon(getPImage(curPlayer)));
        playerImageJLabel.setBounds(roomArea[0], roomArea[1], roomArea[2], roomArea[3]);
        playerImageJLabel.setLocation(roomArea[0] + offSetPoints[1], roomArea[1] + offSetPoints[2] + 120);
        // Dice images are 40x40 pixels
        playerImageJLabel.setSize(40, 40);
        boardLP.remove(playerRepresentation.get(playerName));
        playerRepresentation.put(playerName, playerImageJLabel);
        boardLP.add(playerImageJLabel, new Integer(2));
        boardLP.revalidate();
        boardLP.repaint();
    }

    // Get die image associated to specific player/their rank
    public static Image getPImage(Player curPlayer) throws IOException {
        String dieIndex = "./images/dice/dice/" + curPlayer.getColor() + Integer.toString(curPlayer.getRank()) + ".png";
        // System.out.println(dieIndex);
        BufferedImage pDie = ImageIO.read(new File(dieIndex));
        return pDie;

    }

    // Display the images of the players dice in the trailer
    public void setPlayerImages() throws IOException {
        Board b = Board.getBoard();
        Player[] p = b.getPlayers();
        for (int i = 0; i < p.length; i++) {
            int[] roomArea = b.getRoomFromName("Trailer").getArea();
            int[] offSetPoints = b.getRoomFromName("Trailer").getOffSetPoints(p[i].getSignature());
            JLabel playerImageJLabel = new JLabel(new ImageIcon(getPImage(p[i])));
            playerImageJLabel.setBounds(roomArea[0], roomArea[1], roomArea[2], roomArea[3]);
            playerImageJLabel.setLocation(roomArea[0] + offSetPoints[1], roomArea[1] + offSetPoints[2]);
            // Dice images are 40x40 pixels
            playerImageJLabel.setSize(40, 40);
            playerRepresentation.put(p[i].getName(), playerImageJLabel);
            boardLP.add(playerImageJLabel, new Integer(1));
            boardLP.revalidate();
            boardLP.repaint();
        }
    }

    // Display JTables showing the stats of each player
    public void displayPlayerStats(Player curPlayer) {
        String pName = curPlayer.getName();
        // If a players' stats have been initialized, instead update them
        if (playerStats.containsKey(pName)) {
            updatePlayerStats(curPlayer);
        } else {
            // Prep the stats to be added as they all need to be entered as strings
            String pRank = String.valueOf(curPlayer.getRank());
            String pMoney = String.valueOf(curPlayer.getBalance());
            String pCredits = String.valueOf(curPlayer.getCredits());
            String pRehearsalTokens = String.valueOf(curPlayer.getRehearalTokens());
            String color = Character.toString(curPlayer.getColor());
            String rollResult = String.valueOf(curPlayer.getLastRollResult());
            String[] primMetaData = { "Name", "Rank", "Money", "Credits", "Rehearsal Tokens", "Color", "Last Roll Result"};
            String[] primData = { pName, pRank, pMoney, pCredits, pRehearsalTokens, color, rollResult};
            String[][] pData = new String[7][2];
            // Populate the playerData 2D array with the correct strings to be passed into JTable constructor
            for (int i = 0; i < 7; i++) {
                pData[i][0] = primMetaData[i];
                pData[i][1] = primData[i];
            }
            String[] colNames = { "a", curPlayer.getName() };
            JTable curPStats = new JTable(pData, colNames);
            curPStats.setBounds(curPlayer.getStatOffset(), 1, 231, 112);
            playerStats.put(curPlayer.getName(), curPStats);
            // Make sure table cannot be edited
            curPStats.setEnabled(false);
            statsPanel.add(curPStats);
            statsPanel.revalidate();
            statsPanel.repaint();
        }
    }

    // Once a players stars have been initialized they will need to be updated when
    // various events occur
    public void updatePlayerStats(Player curPlayer) {
        String pRank = String.valueOf(curPlayer.getRank());
        String pMoney = String.valueOf(curPlayer.getBalance());
        String pCredits = String.valueOf(curPlayer.getCredits());
        String pRehearsalTokens = String.valueOf(curPlayer.getRehearalTokens());
        String rollResult = String.valueOf(curPlayer.getLastRollResult());
        JTable pStats = playerStats.get(curPlayer.getName());
        pStats.setValueAt(pRank, 1, 1);
        pStats.setValueAt(pMoney, 2, 1);
        pStats.setValueAt(pCredits, 3, 1);
        pStats.setValueAt(pRehearsalTokens, 4, 1);
        pStats.setValueAt(rollResult, 6, 1);
        pStats.repaint();
    }

    // Place a player on a scenecard role
    public void takeStarRole(String playerName, String roomName, String roleName) {
        Board b = Board.getBoard();
        int[] curSetArea = b.getRoomFromName(roomName).getArea();
        int[] roleArea = ((Set) b.getRoomFromName(roomName)).getScene().getRole(roleName).getArea();
        JLabel playerImageJLabel = playerRepresentation.get(playerName);
        playerImageJLabel.setBounds(roleArea[0], roleArea[1], roleArea[2], roleArea[3]);
        playerImageJLabel.setLocation(roleArea[0] + curSetArea[0] + 1, roleArea[1] + curSetArea[1] + 1);
        playerImageJLabel.setSize(40, 40);
        b.getRoomFromName(roomName).removePlayerFromWaiting(b.getPlayerSigFromName(playerName));
        boardLP.remove(playerRepresentation.get(playerName));
        playerRepresentation.put(playerName, playerImageJLabel);
        boardLP.add(playerImageJLabel, new Integer(3));
        boardLP.revalidate();
        boardLP.repaint();
    }

    // Place a player on their desired role on the set
    public void takeExtraRole(String playerName, String roomName, String roleName) {
        Board b = Board.getBoard();
        int[] roleArea = ((Set) b.getRoomFromName(roomName)).getRole(roleName).getArea();
        JLabel playerImageJLabel = playerRepresentation.get(playerName);
        playerImageJLabel.setBounds(roleArea[0], roleArea[1], roleArea[2], roleArea[3]);
        playerImageJLabel.setLocation(roleArea[0] + 3, roleArea[1] + 3);
        playerImageJLabel.setSize(40, 40);
        b.getRoomFromName(roomName).removePlayerFromWaiting(b.getPlayerSigFromName(playerName));
        boardLP.remove(playerRepresentation.get(playerName));
        playerRepresentation.put(playerName, playerImageJLabel);
        boardLP.add(playerImageJLabel, new Integer(2));
        boardLP.revalidate();
        boardLP.repaint();
    }

    // Place a player in a specified location, and remove their die from the old
    // location
    public void placePlayerInRoom(String playerName, String roomName) {
        Board b = Board.getBoard();
        int[] roomArea = b.getRoomFromName(roomName).getArea();
        int[] offSetPoints = b.getRoomFromName(roomName).getOffSetPoints(b.getPlayerSigFromName(playerName));
        JLabel playerImageJLabel = playerRepresentation.get(playerName);
        playerImageJLabel.setBounds(roomArea[0], roomArea[1], roomArea[2], roomArea[3]);
        playerImageJLabel.setLocation(roomArea[0] + offSetPoints[1], roomArea[1] + offSetPoints[2] + 120);
        // Dice images are 40x40 pixels
        playerImageJLabel.setSize(40, 40);
        // Every time a player is moved, need to remove the old placement from the JLP
        boardLP.remove(playerRepresentation.get(playerName));
        playerRepresentation.put(playerName, playerImageJLabel);
        boardLP.add(playerImageJLabel, new Integer(2));
        boardLP.revalidate();
        boardLP.repaint();
    }

    // Prompts the user for the number of players and receives input
    public static int getNumPlayersInput() throws InterruptedException {
        // Add all the ui elements
        JLabel askPlayers = new JLabel("How many players are playing?");
        askPlayers.setBounds(0, 0, 500, 100);
        askPlayers.setLocation(180, 100);
        controlPanel.add(askPlayers);
        Integer[] playerChoices = { 2, 3, 4, 5, 6, 7, 8 };
        JComboBox<Integer> jComboBox = new JComboBox<>(playerChoices);
        jComboBox.setBounds(80, 50, 140, 20);
        jComboBox.setLocation(180, 200);
        controlPanel.add(jComboBox);
        final JButton jButton = new JButton("Start!");
        jButton.setBounds(100, 100, 90, 20);
        jButton.setLocation(180, 250);
        controlPanel.add(jButton);
        controlPanel.revalidate();
        controlPanel.repaint();
        frame.setVisible(true);
        // create an action listener (weird)
        jButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent aActionEvent) {
                selection = jComboBox.getItemAt(jComboBox.getSelectedIndex());
            }
        });
        // wait for the player to select the number of players
        while (selection == null) {
            Thread.sleep(1);
        }
        controlPanel.remove(askPlayers);
        controlPanel.remove(jComboBox);
        controlPanel.remove(jButton);
        controlPanel.revalidate();
        controlPanel.repaint();
        frame.setVisible(true);
        return selection;
    }

    // Prompts each user for their name and receives input
    public static String getPlayerName(int i) throws InterruptedException {
        String localName;
        // Add all the ui elements
        JLabel askName = new JLabel("Player " + i + ", what is your name?");
        askName.setBounds(0, 0, 500, 100);
        askName.setLocation(180, 100);
        controlPanel.add(askName);
        JTextField textField = new JTextField();
        textField.setBounds(80, 50, 140, 20);
        textField.setLocation(180, 200);
        controlPanel.add(textField);
        final JButton jButton = new JButton("Submit");
        jButton.setBounds(100, 100, 90, 20);
        jButton.setLocation(180, 250);
        controlPanel.add(jButton);
        controlPanel.revalidate();
        controlPanel.repaint();
        frame.setVisible(true);
        // create an action listener (weird)
        jButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent aActionEvent) {
                name = textField.getText();
            }
        });
        // wait for the player to select the number of players
        while (name == null || name == "") {
            Thread.sleep(1);
        }
        controlPanel.remove(askName);
        controlPanel.remove(textField);
        controlPanel.remove(jButton);
        controlPanel.revalidate();
        controlPanel.repaint();
        frame.setVisible(true);
        localName = name;
        name = null;
        return localName;
    }

    // Prompts a player for an action out of prevalidated options and receives input
    public static void getPlayerAction(Player curPlayer, ArrayList<String> actions) throws InterruptedException {
        Board b = Board.getBoard();
        int pLocation = curPlayer.getLocation();
        int playerRank = curPlayer.getRank();
        // Add all the ui elements
        JLabel turnInfo = new JLabel(curPlayer.getName() + ", it is your turn! What would you like to do?");
        turnInfo.setBounds(0, 0, 500, 100);
        turnInfo.setLocation(180, 100);
        controlPanel.add(turnInfo);
        final JButton moveButton = new JButton("Move");
        moveButton.setBounds(100, 100, 90, 50);
        moveButton.setLocation(180, 250);
        moveButton.setEnabled(false);
        controlPanel.add(moveButton);
        JComboBox<String> moveOptions = new JComboBox<>(b.getRoom(pLocation).getNeighbors());
        moveOptions.setBounds(80, 50, 140, 20);
        moveOptions.setLocation(270, 250);
        controlPanel.add(moveOptions);
        // Move case
        if (actions.stream().anyMatch("move"::equalsIgnoreCase)) {
            moveButton.setEnabled(true);
            moveButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent aActionEvent) {
                    String moveChoice = moveOptions.getItemAt(moveOptions.getSelectedIndex());
                    if (moveChoice != null && moveChoice != "") {
                        try {
                            b.getRoom(pLocation).removePlayerFromWaiting(curPlayer.getSignature());
                            curPlayer.move(b.matchNameToIndex(moveChoice));
                            gotAction = true;
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }
        final JButton roleButton = new JButton("Take Role");
        roleButton.setBounds(100, 100, 90, 50);
        roleButton.setLocation(180, 300);
        roleButton.setEnabled(false);
        JComboBox<String> extraRoles = new JComboBox<>();
        JComboBox<String> starRoles = new JComboBox<>();
        extraRoles.addItem("");
        starRoles.addItem("");
        starRoles.setBounds(80, 50, 140, 20);
        starRoles.setLocation(270, 300);
        extraRoles.setBounds(80, 50, 140, 20);
        extraRoles.setLocation(410, 300);
        controlPanel.add(roleButton);
        controlPanel.add(extraRoles);
        controlPanel.add(starRoles);
        // Take Role case
        if (actions.stream().anyMatch("take role"::equalsIgnoreCase)) {
            if (b.getRoom(pLocation) instanceof Set) {
                ArrayList<String> extraList = new ArrayList<>(Arrays.asList(((Set) b.getRoom(pLocation)).getExtraRoles(playerRank)));
                ArrayList<String> starList = new ArrayList<>(Arrays.asList(((Set) b.getRoom(pLocation)).getScene().getStarRoles(playerRank)));
                for(int i = 0; i < extraList.size(); i++)
                {
                    extraRoles.addItem(extraList.get(i));
                }
                for(int i = 0; i < starList.size(); i++)
                {
                    starRoles.addItem(starList.get(i));
                }
            }
            roleButton.setEnabled(true);
            roleButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent aActionEvent) {
                    String extraChoice = extraRoles.getItemAt(extraRoles.getSelectedIndex());
                    String starChoice = starRoles.getItemAt(starRoles.getSelectedIndex());
                    if (starChoice != null && starChoice != "") {
                        curPlayer.takeStarRole(starChoice);
                        gotAction = true;
                    } else if (extraChoice != null && extraChoice != "") {
                        curPlayer.takeExtraRole(extraChoice);
                        gotAction = true;
                    }
                }
            });
        }
        final JButton actButton = new JButton("Act");
        actButton.setBounds(100, 100, 90, 50);
        actButton.setLocation(180, 350);
        actButton.setEnabled(false);
        controlPanel.add(actButton);
        // Act case
        if (actions.stream().anyMatch("act"::equalsIgnoreCase)) {
            actButton.setEnabled(true);
            actButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent aActionEvent) {
                    try {
                        curPlayer.act();
                        gotAction = true;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
        final JButton rehearseButton = new JButton("Rehearse");
        rehearseButton.setBounds(100, 100, 90, 50);
        rehearseButton.setLocation(270, 350);
        rehearseButton.setEnabled(false);
        controlPanel.add(rehearseButton);
        // Rehearse case
        if (actions.stream().anyMatch("rehearse"::equalsIgnoreCase)) {
            rehearseButton.setEnabled(true);
            rehearseButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent aActionEvent) {
                    curPlayer.rehearse();
                    gotAction = true;
                }
            });
        }
        final JButton endTurnButton = new JButton("End Turn");
        endTurnButton.setBounds(100, 100, 90, 50);
        endTurnButton.setLocation(180, 450);
        endTurnButton.setEnabled(true);
        controlPanel.add(endTurnButton);
        // End turn case
        endTurnButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent aActionEvent) {
                endTurn = true;
                gotAction = true;
            }
        });
        final JButton upgradeButton = new JButton("Upgrade");
        upgradeButton.setBounds(100, 100, 90, 50);
        upgradeButton.setLocation(180, 400);
        upgradeButton.setEnabled(false);
        Office o = ((Office) b.getRoom(1));
        ArrayList<String> availableUpgradesList = o.getAvailableUpgrades(curPlayer.getRank(), curPlayer.getBalance(),
                curPlayer.getCredits());
        String[] availableUpgrades = new String[availableUpgradesList.size()];
        availableUpgrades = availableUpgradesList.toArray(availableUpgrades);
        JComboBox<String> upgradeLevels = new JComboBox<>(availableUpgrades);
        String[] currencies = { "dollars", "credits" };
        JComboBox<String> upgradeCurrencies = new JComboBox<>(currencies);
        upgradeLevels.setBounds(80, 50, 140, 20);
        upgradeLevels.setLocation(270, 400);
        upgradeCurrencies.setBounds(80, 50, 140, 20);
        upgradeCurrencies.setLocation(410, 400);
        controlPanel.add(upgradeButton);
        controlPanel.add(upgradeLevels);
        controlPanel.add(upgradeCurrencies);
        // Upgrade case
        if (actions.stream().anyMatch("upgrade"::equalsIgnoreCase)) {
            upgradeButton.setEnabled(true);
            upgradeButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent aActionEvent) {
                    String upgradeChoice = upgradeLevels.getItemAt(upgradeLevels.getSelectedIndex());
                    String currencyChoice = upgradeCurrencies.getItemAt(upgradeCurrencies.getSelectedIndex());
                    if (upgradeChoice != null && upgradeChoice != "") {
                        ArrayList<String> upgradeTypes = o.getUpgradeTypes(Integer.parseInt(upgradeChoice),
                                curPlayer.getBalance(), curPlayer.getCredits());
                        if (currencyChoice != null && currencyChoice != ""
                                && upgradeTypes.stream().anyMatch(currencyChoice::equalsIgnoreCase)) {
                            int dollars, credits;
                            if (currencyChoice.toLowerCase().equals("credits")) {
                                credits = o.getCost(Integer.parseInt(upgradeChoice), true);
                                dollars = 0;
                            } else {
                                dollars = o.getCost(Integer.parseInt(upgradeChoice), false);
                                credits = 0;
                            }
                            try {
                                curPlayer.upgrade(Integer.parseInt(upgradeChoice), dollars, credits);
                                gotAction = true;
                            } catch (NumberFormatException | IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            });
        }
        controlPanel.revalidate();
        controlPanel.repaint();
        frame.setVisible(true);
        while (!gotAction) {
            Thread.sleep(1);
        }
        if (endTurn) {
            curPlayer.endTurn();
            endTurn = false;
        }
        // Remove all buttons at the end of an action so the next set of actions can be validated and displayed
        controlPanel.remove(turnInfo);
        controlPanel.remove(moveButton);
        controlPanel.remove(moveOptions);
        controlPanel.remove(roleButton);
        controlPanel.remove(extraRoles);
        controlPanel.remove(starRoles);
        controlPanel.remove(actButton);
        controlPanel.remove(rehearseButton);
        controlPanel.remove(endTurnButton);
        controlPanel.remove(upgradeButton);
        controlPanel.remove(upgradeLevels);
        controlPanel.remove(upgradeCurrencies);
        controlPanel.revalidate();
        controlPanel.repaint();
        frame.setVisible(true);
        gotAction = false;
    }
}