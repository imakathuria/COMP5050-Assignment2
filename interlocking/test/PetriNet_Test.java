
import java.util.HashMap;
import java.util.Map;



public class PetriNet_Test {
  private PetriNet net;

  private static Map<String, Integer> m(String p, int n) {
    Map<String, Integer> map = new HashMap<>();
    map.put(p, n);
    return map;
  }
}
