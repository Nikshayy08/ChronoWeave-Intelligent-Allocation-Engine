package scheduler;

import java.util.*;
import model.Request;

/*
 * PriorityScheduler
 *
 * Implements Priority-Based Interval Scheduling.
 * Uses a Max Heap (PriorityQueue) and conflict detection.
 */

public class PriorityScheduler {

    public static List<Request> allocate(List<Request> requests) {

        // Max Heap based on priority
        PriorityQueue<Request> pq = new PriorityQueue<>(
            (a, b) -> {
                // Higher priority first
                if (b.priority != a.priority)
                    return b.priority - a.priority;

                // If same priority, earlier end time first
                return a.endTime - b.endTime;
            }
        );

        // Add all requests into heap
        pq.addAll(requests);

        List<Request> selected = new ArrayList<>();

        while (!pq.isEmpty()) {

            Request current = pq.poll();
            boolean conflict = false;

            // Check overlap with already selected requests
            for (Request allocated : selected) {

                if (!(current.endTime <= allocated.startTime ||
                      current.startTime >= allocated.endTime)) {

                    conflict = true;
                    break;
                }
            }

            if (!conflict) {
                selected.add(current);
            }
        }

        return selected;
    }
}
