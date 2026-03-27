package algorithms;

import java.util.*;

/*
  his module detects mutual exchange opportunities between students using graph cycle detection.
 * ExchangeGraph.java
 *
 * Directed graph where:
 *   Vertex(Node) = Student
 *   Edge (A → B) = A wants resource owned by B
 *
 * A cycle in this graph means a mutual exchange is possible.
 *
 * Example of a valid 2-way exchange:
 *   Alice wants Bob's Drafter  →  edge: Alice → Bob
 *   Bob wants Alice's Notes    →  edge: Bob → Alice
 *   Cycle detected             →  swap is possible
 *
 * Example of a valid 3-way exchange:
 *   Alice → Bob → Carol → Alice  →  3-way cycle = all three can swap
 *
 * ─── ALGORITHM ───────────────────────────────────────────────────────────────
 *
 * Cycle detection in a Directed Graph using DFS + Recursion Stack.
 *
 *   visited[]       → tracks nodes already fully processed
 *   recursionStack[]→ tracks nodes in the current DFS path
 *
 *   If we encounter a node already in the recursion stack → cycle found
 *
 * Time Complexity: O(V + E)
 *   V = number of students
 *   E = number of want-edges
 *
 * ─────────────────────────────────────────────────────────────────────────────
 */

public class ExchangeGraph {

    // ─── Fields ───────────────────────────────────────────────────────────────

    // Adjacency list: studentName → list of students they want resources from
    private Map<String, List<String>> adjList = new HashMap<>();

    // Tracks which cycle was found (for display/notification purposes)
    private List<String> detectedCycle = new ArrayList<>();

    // ─── Graph Construction ───────────────────────────────────────────────────

    public void addStudent(String student) {
        adjList.putIfAbsent(student, new ArrayList<>());
    }

    /*
     * addEdge(from, to)
     * Adds directed edge: 'from' wants resource owned by 'to'
     * Automatically adds both nodes if not present.
     */
    public void addEdge(String from, String to) {
        adjList.putIfAbsent(from, new ArrayList<>());
        adjList.putIfAbsent(to,   new ArrayList<>());
        adjList.get(from).add(to);
    }

    // ─── Cycle Detection ──────────────────────────────────────────────────────

    /*
     * hasCycle()
     *
     * Entry point for cycle detection.
     * Handles disconnected graph components by iterating all vertices.
     *
     * Returns true if any exchange cycle exists.
     */
    public boolean hasCycle() {

        Set<String> visited        = new HashSet<>();
        Set<String> recursionStack = new HashSet<>();
        detectedCycle.clear();

        for (String student : adjList.keySet()) {
            if (!visited.contains(student)) {
                List<String> path = new ArrayList<>();
                if (dfs(student, visited, recursionStack, path)) {
                    return true;
                }
            }
        }

        return false;
    }

    /*
     * dfs(current, visited, recursionStack, path)
     *
     * Recursive DFS with path tracking for cycle identification.
     *
     * @param current        Node currently being visited
     * @param visited        Set of fully processed nodes
     * @param recursionStack Nodes in current DFS call stack
     * @param path           Current traversal path (for cycle reporting)
     */
    private boolean dfs(String current,
                         Set<String> visited,
                         Set<String> recursionStack,
                         List<String> path) {

        visited.add(current);
        recursionStack.add(current);
        path.add(current);

        List<String> neighbors = adjList.getOrDefault(current, new ArrayList<>());

        for (String neighbor : neighbors) {

            if (!visited.contains(neighbor)) {
                if (dfs(neighbor, visited, recursionStack, path))
                    return true;

            } else if (recursionStack.contains(neighbor)) {
                // Cycle found — extract the cycle path for reporting
                int cycleStart = path.indexOf(neighbor);
                detectedCycle  = new ArrayList<>(path.subList(cycleStart, path.size()));
                detectedCycle.add(neighbor); // close the cycle
                return true;
            }
        }

        recursionStack.remove(current);
        path.remove(path.size() - 1);
        return false;
    }

    // ─── Getters ──────────────────────────────────────────────────────────────

    /*
     * getDetectedCycle()
     * Returns the list of students forming the exchange cycle.
     * Empty if no cycle was found.
     * Call only after hasCycle() returns true.
     */
    public List<String> getDetectedCycle() {
        return Collections.unmodifiableList(detectedCycle);
    }

    /*
     * getAdjList()
     * Returns the full adjacency list (for visualization or debugging).
     */
    public Map<String, List<String>> getAdjList() {
        return Collections.unmodifiableMap(adjList);
    }

    /*
     * getEdgeCount()
     * Total number of directed edges in the graph.
     */
    public int getEdgeCount() {
        return adjList.values().stream().mapToInt(List::size).sum();
    }

    /*
     * getVertexCount()
     */
    public int getVertexCount() {
        return adjList.size();
    }
}
