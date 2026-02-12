package algorithms;

import java.util.*;

/*
 * ExchangeGraph
 *
 * Represents a Directed Graph where:
 *  - Vertex = Student
 *  - Edge (A → B) means Student A wants a resource from Student B
 *
 * Used to detect exchange cycles using:
 * DFS + Recursion Stack (Cycle Detection in Directed Graph)
 *
 * Time Complexity: O(V + E)
 */

public class ExchangeGraph {

    // Adjacency List Representation of Graph
    private Map<String, List<String>> adjList = new HashMap<>();

    // Add a student (vertex) to the graph
    public void addStudent(String student) {
        adjList.putIfAbsent(student, new ArrayList<>());
    }

    // Add a directed edge from → to
    public void addEdge(String from, String to) {
        adjList.putIfAbsent(from, new ArrayList<>());
        adjList.putIfAbsent(to, new ArrayList<>());
        adjList.get(from).add(to);
    }

    /*
     * Detect cycle in directed graph
     * Uses DFS traversal with recursion stack tracking
     */
    public boolean hasCycle() {

        // Stores fully processed nodes
        Set<String> visited = new HashSet<>();

        // Stores nodes currently in recursion stack
        Set<String> recursionStack = new HashSet<>();

        // Check each disconnected component
        for (String student : adjList.keySet()) {
            if (!visited.contains(student)) {
                if (dfs(student, visited, recursionStack)) {
                    return true;
                }
            }
        }

        return false;
    }

    /*
     * DFS Helper Function
     *
     * visited → nodes already explored
     * recursionStack → nodes in current DFS path
     */
    private boolean dfs(String current,
                        Set<String> visited,
                        Set<String> recursionStack) {

        // Mark current node as visited
        visited.add(current);

        // Add to recursion stack
        recursionStack.add(current);

        // Explore all neighbors
        for (String neighbor : adjList.get(current)) {

            // If neighbor not visited → continue DFS
            if (!visited.contains(neighbor)) {
                if (dfs(neighbor, visited, recursionStack))
                    return true;
            }
            // If neighbor already in recursion stack → cycle found
            else if (recursionStack.contains(neighbor)) {
                return true;
            }
        }

        // Remove current node from recursion stack
        recursionStack.remove(current);

        return false;
    }
}
