import java.util.*;

// This class contains the core Greedy algorithm
public class GreedyScheduler {

    // Static method to allocate maximum non-overlapping requests
    public static List<Request> allocate(List<Request> requests) {

        // Step 1: Sort requests based on end time (ascending order)
        // Comparator compares endTime of each request
        requests.sort(Comparator.comparingInt(r -> r.endTime));

        // List to store selected (optimal) requests
        List<Request> selected = new ArrayList<>();

        // This variable keeps track of end time of last selected interval
        int lastEndTime = -1;

        // Step 2: Traverse through sorted requests
        for (Request request : requests) {

            // If current request does not overlap
            // (starts after or exactly when last one ended)
            if (request.startTime >= lastEndTime) {

                // Select the request
                selected.add(request);

                // Update lastEndTime to current request's endTime
                lastEndTime = request.endTime;
            }
        }

        // Return optimal set of requests
        return selected;
    }
}
