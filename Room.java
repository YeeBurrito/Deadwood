abstract class Room {
    private String name;
    private String[] neighbors;
    private int[] area;
    // Each row has 3 vals, first one represents if space is empty or num of player on space, then the next two are the x and y coords for the offset
    private int[][] offSetPoints;

    // Constructor
    public Room(String name, String[] neighbors, int[] area) {
        this.name = name;
        this.neighbors = neighbors;
        this.area = area;
        this.offSetPoints = createOffSetPoints();
    }

    // Creates a structure that stores where players are on the offsetPoints
    public int[][] createOffSetPoints() {
        int[][] offSetPoints = new int[8][3];
        int xIncrementor = 0;
        int yIncrementor = 0;
        for(int i = 0; i < 8; i++){
            offSetPoints[i][0] = -1;
            offSetPoints[i][1] = xIncrementor * 40;
            offSetPoints[i][2] = yIncrementor * 40;
            xIncrementor++;
            if(i == 4){
                xIncrementor = 0;
                yIncrementor = 1;
            }
        }
        return offSetPoints;
    }

    // Gets the coordinates with which to place the current player, and updates the structure to match
    public int[] getOffSetPoints(int pSignature) {
        for(int i = 0; i < offSetPoints.length; i++){
            if(offSetPoints[i][0] == -1){
                offSetPoints[i][0] = pSignature;
                return offSetPoints[i];
            }
        }
        return null;
    }

    // Returns the current offset that a player is on
    public int[] getCurOffSetPoints(int pSignature) {
        for(int i = 0; i < offSetPoints.length; i++){
            if(offSetPoints[i][0] == pSignature){
                return offSetPoints[i];
            }
        }
        return null;
    }

    // When a player leaves the waiting area of a room, remove them from that rooms offsetpoints index
    public void removePlayerFromWaiting(int pSignature){
        for(int i = 0; i < offSetPoints.length; i++){
            if(offSetPoints[i][0] == pSignature){
                offSetPoints[i][0] = -1;
                break;
            }
        }
    }    

    // resets all the offsetpoints for a room
    public void resetOffsetPoints() {
        for(int i = 0; i < offSetPoints.length; i++){
            offSetPoints[i][0] = -1;
        }
    }

    // Returns the area of the room
    public int[] getArea(){
        return this.area;
    }

    // Returns the name of the room
    public String getName() {
        return this.name;
    }

    // Returns a string[] containing the neighbors of the room
    public String[] getNeighbors() {
        return this.neighbors;
    }
}
