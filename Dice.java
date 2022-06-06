import java.util.Arrays;
import java.util.Random;

public class Dice {

    // Calculates roll for acting, dependent upon players' rehearsal counters as a
    // modifier
    public static int actRoll(int rehearsalCounters) {
        Random rand = new Random();
        // Generate random number between 1 and 6
        int n = rand.nextInt(6);
        n++;
        // Then add the rehearsal counters onto n
        n += rehearsalCounters;
        return n;
    }

    // When a scene wraps, a number of dice is rolled equal to the budget, and since
    // each individual roll must be preserved, they'll be stored in an array and
    // returned
    public static int[] payoutRoll(int sceneBudget) {
        int[] returnArray = new int[sceneBudget];
        Random rand = new Random();
        for (int i = 0; i < returnArray.length; i++) {
            // Random number from [0,6)
            int n = rand.nextInt(6);
            // Make it [1,6]
            n += 1;
            returnArray[i] = n;
        }
        Arrays.sort(returnArray);
        // reverse the array
        int i, t;
        for (i = 0; i < returnArray.length / 2; i++) {
            t = returnArray[i];
            returnArray[i] = returnArray[returnArray.length - i - 1];
            returnArray[returnArray.length - i - 1] = t;
        }
        return returnArray;
    }
}
