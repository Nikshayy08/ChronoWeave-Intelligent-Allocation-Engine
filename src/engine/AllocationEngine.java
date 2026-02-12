package engine;

import java.util.*;
import models.Request;
import algorithms.PriorityScheduler;
import algorithms.ExchangeGraph;

public class AllocationEngine {

    private List<Request> requests = new ArrayList<>();

    public void addRequest(Request request) {
        requests.add(request);
    }

    public List<Request> runPriorityAllocation() {
        return PriorityScheduler.allocate(new ArrayList<>(requests));
    }

    public boolean checkExchangeCycle() {

        ExchangeGraph graph = new ExchangeGraph();

        for (Request r : requests) {
            graph.addStudent(r.studentName);
        }

        for (int i = 0; i < requests.size() - 1; i++) {
            graph.addEdge(requests.get(i).studentName,
                          requests.get(i + 1).studentName);
        }

        if (requests.size() > 1) {
            graph.addEdge(requests.get(requests.size() - 1).studentName,
                          requests.get(0).studentName);
        }

        return graph.hasCycle();
    }

    public void displayAllRequests() {
        for (Request r : requests) {
            System.out.println(r);
        }
    }
}
