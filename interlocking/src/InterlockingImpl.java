import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Interlocking for the Islington corridor, built on a {@link PetriNet}.
 *
 * <p>Each section has a FREE place and one occupancy place per direction. Moving a train fires
 * a transition from the current section to the next. Inhibitor arcs give two safety rules:
 * a train does not step onto single track that an opposing train occupies further along its
 * route (deadlock avoidance), and freight may not cross junction J1 in a step where a
 * passenger train has crossed (passenger priority).
 */
public class InterlockingImpl implements Interlocking {
  private static final int SECTIONS = 11;

  // Topology (ASSUMPTION - verify against the diagram).
  private static final int[][] SOUTH_EDGES = {{1, 5}, {5, 8}, {5, 9}, {3, 4}, {3, 7}, {7, 11}};
  private static final int[][] NORTH_EDGES = {{9, 6}, {10, 6}, {6, 2}, {4, 3}, {11, 7}, {7, 3}};
  private static final Set<Integer> ENTRY_SOUTH = Set.of(1, 3);
  private static final Set<Integer> EXIT_SOUTH = Set.of(4, 8, 9, 11);
  private static final Set<Integer> ENTRY_NORTH = Set.of(4, 9, 10, 11);
  private static final Set<Integer> EXIT_NORTH = Set.of(2, 3);
  private static final Set<Integer> FREIGHT = Set.of(3, 4, 7, 11);
  private static final Set<Integer> BIDIRECTIONAL = Set.of(3, 4, 7, 9, 11);

}
