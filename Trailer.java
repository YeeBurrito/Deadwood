import java.util.Arrays;

public class Trailer extends Room {
    private int[] area;

    // Constructor
    public Trailer(String name, String[] neighbors, int[] area) {
        super(name, neighbors, area);
    }

    // special printSet method as the Trailer is different from a normal set
    public void printSet() {
        System.out.println("The trailer is the starting place for all players!");
    }
}
