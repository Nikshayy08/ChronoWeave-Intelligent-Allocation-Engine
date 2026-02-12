package algorithms;

import java.util.*;
import models.Request;

/*
 * PriorityScheduler
 *
 * Implements Priority-Based Interval Scheduling.
 *
 * Strategy:
 * 1. Use Max Heap (PriorityQueue) based on priority.
 * 2. Higher priority requests are processed first.
 * 3. If priorities are equal → earlier finishing time preferred.
 * 4. Check time conflicts before allocation.
 *
 * Time Complexity:
 *  - Heap insertion: O(n log n)
 *  - Conflict checking: O(n²) worst case
 */

public class PriorityScheduler {

    /*
     * Allocates non-conflicting requests
     * based on highest priority first.
     */
    public static List<Request> allocate(List<Request> requests) {

        // Max Heap based on priority
        PriorityQueue<Request> pq = new PriorityQueue<>(
            (a, b) -> {
                // Higher priority first
                if (b.priority != a.priority)
                    return b.priority - a.priority;

                // If same priority → earlier end time first
                return a.endTime - b.endTime;
            }
        );

        // Insert all requests into heap
        pq.addAll(requests);

        List<Request> selected = new ArrayList<>();

        // Process requests in priority order
        while (!pq.isEmpty()) {

            Request current = pq.poll();
            boolean conflict = false;

            // Check overlap with already selected requests
            for (Request allocated : selected) {

                // If intervals overlap → conflict
                if (!(current.endTime <= allocated.startTime ||
                      current.startTime >= allocated.endTime)) {

                    conflict = true;
                    break;
                }
            }

            // If no conflict → select request
            if (!conflict) {
                selected.add(current);
            }
        }

        return selected;
    }
}
