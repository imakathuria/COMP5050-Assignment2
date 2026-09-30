import java.util.ArrayList;
import java.util.List;

/** Manual driver: run with {@code java Driver}. */
public class Driver {
  public static void main(String[] args) {
    Interlocking il = new InterlockingImpl();
    String[] names = {"P", "F"};
    il.addTrain("P", 1, 8);
    il.addTrain("F", 3, 4);
    for (int step = 1; step <= 5; step++) {
      List<String> live = new ArrayList<>();
      for (String n : names) {
        if (il.getTrain(n) != -1) {
          live.add(n);
        }
      }
      int moved = il.moveTrains(live.toArray(new String[0]));
      System.out.println("step " + step + ": moved=" + moved
          + " P@" + il.getTrain("P") + " F@" + il.getTrain("F"));
    }
  }
}
