package scheduler;

import java.util.*;

/*
 * ExchangeGraph
 *
 * Represents a directed graph where:
 * Node  = Student
 * Edge  = "Student A wants resource that Student B has"
 *
 * Used to detect exchange cycles using DFS.
 */

public class ExchangeGraph {

    // Adjacency List Representation
    private Map<String, List<String>> adjList = new HashMap<>();

    // Add student node to graph
    public void addStudent(String student) {
        adjList.putIfAbsent(student, new ArrayList<>());
    }

    // Add directed edge: from → to
    public void addEdge(String from, String to) {
        adjList.putIfAbsent(from, new ArrayList<>());
        adjList.putIfAbsent(to, new ArrayList<>());
        adjList.get(from).add(to);
    }

    // Detect cycle using DFS + Recursion Stack
    public boolean hasCycle() {

        Set<String> visited = new HashSet<>();
        Set<String> recursionStack = new HashSet<>();

        for (String student : adjList.keySet()) {
            if (!visited.contains(student)) {
                if (dfs(student, visited, recursionStack)) {
                    return true;
                }
            }
        }

        return false;
    }

    // DFS helper
    private boolean dfs(String current,
                        Set<String> visited,
                        Set<String> recursionStack) {

        visited.add(current);
        recursionStack.add(current);

        for (String neighbor : adjList.get(current)) {

            if (!visited.contains(neighbor)) {
                if (dfs(neighbor, visited, recursionStack))
                    return true;
            } else if (recursionStack.contains(neighbor)) {
                return true;
            }
        }

        recursionStack.remove(current);
        return false;
    }
}
