import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Minimal place/transition net with inhibitor arcs.
 *
 * <p>A transition is described by its input arcs, output arcs and inhibitor places. It fires
 * atomically: either every arc is applied, or nothing changes.
 */
public class PetriNet {
  private final Map<String, Integer> marking = new HashMap<>();

  /** Creates (or resets) a place with the given number of tokens. */
  public void addPlace(String place, int tokens) {
    marking.put(place, tokens);
  }

  /** Returns the tokens in a place (0 for unknown places). */
  public int tokens(String place) {
    return marking.getOrDefault(place, 0);
  }

  /** Builds an arc map; a place listed n times gets weight n. */
  public static Map<String, Integer> arcs(String... places) {
    Map<String, Integer> map = new HashMap<>();
    for (String p : places) {
      map.merge(p, 1, Integer::sum);
    }
    return map;
  }

  /** True if every input place holds enough tokens. */
  public boolean isEnabled(Map<String, Integer> in) {
    return isEnabled(in, Collections.emptyList());
  }

  /** True if inputs are satisfied and every inhibitor place is empty. */
  public boolean isEnabled(Map<String, Integer> in, Collection<String> inhibitors) {
    for (String p : inhibitors) {
      if (tokens(p) > 0) {
        return false;
      }
    }
    for (Map.Entry<String, Integer> e : in.entrySet()) {
      if (tokens(e.getKey()) < e.getValue()) {
        return false;
      }
    }
    return true;
  }
}
