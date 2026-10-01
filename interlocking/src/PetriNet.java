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

}
