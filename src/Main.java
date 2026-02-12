import java.util.List;
import java.util.ArrayList;

import model.Request;
import scheduler.GreedyScheduler;
import scheduler.PriorityScheduler;
import scheduler.ExchangeGraph;

public class Main {

    public static void main(String[] args) {

        // -------------------------
        // DAY 1 & DAY 2: Scheduling
        // -------------------------

        List<Request> requests = new ArrayList<>();

        requests.add(new Request("Aman", 1, 3, 1));
        requests.add(new Request("Riya", 2, 5, 5));
        requests.add(new Request("Karan", 4, 7, 2));
        requests.add(new Request("Sneha", 6, 9, 8));
        requests.add(new Request("Arjun", 8, 10, 1));

        System.out.println("All Requests:");
        for (Request r : requests) {
            System.out.println(r);
        }

        // Using Priority Scheduler (Day 2 Engine)
        List<Request> allocated = PriorityScheduler.allocate(requests);

        System.out.println("\nAllocated Requests:");
        for (Request r : allocated) {
            System.out.println(r);
        }

        // -------------------------
        // DAY 3: Exchange Graph
        // -------------------------

        System.out.println("\n--- Exchange Graph Simulation ---");

        ExchangeGraph graph = new ExchangeGraph();

        graph.addStudent("A");
        graph.addStudent("B");
        graph.addStudent("C");

        graph.addEdge("A", "B");
        graph.addEdge("B", "C");
        graph.addEdge("C", "A");

        if (graph.hasCycle()) {
            System.out.println("Exchange cycle detected! Swap possible.");
        } else {
            System.out.println("No exchange cycle found.");
        }
    }
}
