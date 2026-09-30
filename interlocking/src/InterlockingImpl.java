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

  private static final Map<Integer, List<Integer>> SOUTH = graph(SOUTH_EDGES);
  private static final Map<Integer, List<Integer>> NORTH = graph(NORTH_EDGES);

  /** A train and its fixed route through the corridor. */
  private static final class Train {
    final String name;
    final List<Integer> path;
    final boolean south;
    final boolean passenger;
    int index = 0;

    Train(String name, List<Integer> path, boolean south) {
      this.name = name;
      this.path = path;
      this.south = south;
      this.passenger = !FREIGHT.contains(path.get(0));
    }

    int current() {
      return path.get(index);
    }

    boolean atDestination() {
      return index == path.size() - 1;
    }
  }

  private final PetriNet net = new PetriNet();
  private final Map<String, Train> active = new HashMap<>();
  private final Set<String> known = new HashSet<>();
  private final String[] occupant = new String[SECTIONS + 1];

  /** Creates an empty corridor. */
  public InterlockingImpl() {
    for (int s = 1; s <= SECTIONS; s++) {
      net.addPlace(free(s), 1);
      net.addPlace(occ(true, s), 0);
      net.addPlace(occ(false, s), 0);
    }
    net.addPlace("J1_PASS", 0);
    net.addPlace("J1_FRT", 0);
  }

  private static String free(int s) {
    return "FREE:" + s;
  }

  private static String occ(boolean south, int s) {
    return (south ? "OCCS:" : "OCCN:") + s;
  }

  private static Map<Integer, List<Integer>> graph(int[][] edges) {
    Map<Integer, List<Integer>> g = new HashMap<>();
    for (int[] e : edges) {
      g.computeIfAbsent(e[0], k -> new ArrayList<>()).add(e[1]);
    }
    return g;
  }

  /** Depth-first search; the corridor is acyclic so this terminates. Null if no path. */
  private static List<Integer> findPath(Map<Integer, List<Integer>> g, int from, int to) {
    if (from == to) {
      return new ArrayList<>(List.of(from));
    }
    for (int next : g.getOrDefault(from, List.of())) {
      List<Integer> rest = findPath(g, next, to);
      if (rest != null) {
        rest.add(0, from);
        return rest;
      }
    }
    return null;
  }

  private static Train build(String name, int entry, int destination) {
    if (ENTRY_SOUTH.contains(entry) && EXIT_SOUTH.contains(destination)) {
      List<Integer> p = findPath(SOUTH, entry, destination);
      if (p != null) {
        return new Train(name, p, true);
      }
    }
    if (ENTRY_NORTH.contains(entry) && EXIT_NORTH.contains(destination)) {
      List<Integer> p = findPath(NORTH, entry, destination);
      if (p != null) {
        return new Train(name, p, false);
      }
    }
    return null;
  }

  /** Moves that pass through junction J1, where freight crosses the passenger tracks. */
  private static boolean crossesJunction(int from, int to) {
    return (from == 1 && to == 5)
        || (from == 6 && to == 2)
        || (from == 3 && to == 4)
        || (from == 4 && to == 3);
  }

  @Override
  public synchronized void addTrain(String trainName, int entryTrackSection,
      int destinationTrackSection) throws IllegalArgumentException, IllegalStateException {
    if (trainName == null || trainName.isEmpty() || active.containsKey(trainName)) {
      throw new IllegalArgumentException("Train name missing or already in use: " + trainName);
    }
    Train t = build(trainName, entryTrackSection, destinationTrackSection);
    if (t == null) {
      throw new IllegalArgumentException(
          "No valid path from " + entryTrackSection + " to " + destinationTrackSection);
    }
    if (!net.fire(PetriNet.arcs(free(entryTrackSection)),
        PetriNet.arcs(occ(t.south, entryTrackSection)))) {
      throw new IllegalStateException("Entry section is occupied: " + entryTrackSection);
    }
    occupant[entryTrackSection] = trainName;
    active.put(trainName, t);
    known.add(trainName);
  }

  @Override
  public synchronized int moveTrains(String[] trainNames) throws IllegalArgumentException {
    if (trainNames == null) {
      throw new IllegalArgumentException("Train names must not be null");
    }
    // Validate everything first so an invalid name moves nothing.
    Set<String> unique = new LinkedHashSet<>();
    for (String name : trainNames) {
      if (name == null || !active.containsKey(name)) {
        throw new IllegalArgumentException("Train not in the corridor: " + name);
      }
      unique.add(name);
    }
    List<Train> passenger = new ArrayList<>();
    List<Train> freight = new ArrayList<>();
    for (String name : unique) {
      Train t = active.get(name);
      (t.passenger ? passenger : freight).add(t);
    }
    // Passenger trains go first so they win the junction. The two groups share no sections.
    int moved = runGroup(passenger) + runGroup(freight);
    drain("J1_PASS");
    drain("J1_FRT");
    return moved;
  }

}
