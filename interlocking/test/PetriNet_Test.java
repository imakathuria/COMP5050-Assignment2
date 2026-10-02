
import java.util.HashMap;
import java.util.Map;



public class PetriNet_Test {
  private PetriNet net;

  private static Map<String, Integer> m(String p, int n) {
    Map<String, Integer> map = new HashMap<>();
    map.put(p, n);
    return map;
  }
  @Before
  public void setUp() {
    net = new PetriNet();
    net.addPlace("A", 1);
    net.addPlace("B", 0);
  }
}
