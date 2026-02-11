import java.util.*;

/*
 * PriorityScheduler
 *
 * This class implements Priority-Based Interval Scheduling.
 *
 * It combines:
 * 1. Priority Queue (Max Heap) → To handle urgency
 * 2. Conflict checking logic → To avoid time overlap
 *
 * Higher priority requests are processed first.
 * If two requests have same priority, the one with earlier end time is preferred.
 */

public class PriorityScheduler {

    /*
     * allocate()
     *
     * This method selects a set of non-overlapping requests
     * while giving preference to higher priority requests.
     *
     * Parameter:
     * requests → List of all student requests
     *
     * Returns:
     * List of selected (allocated) requests
     */
    public static List<Request> allocate(List<Request> requests) {

        /*
         * Create a PriorityQueue (Max Heap).
         *
         * Comparator logic:
         * 1. Higher priority first
         * 2. If same priority → earlier endTime first
         *
         * Java's PriorityQueue is internally implemented using a Binary Heap.
         */
        PriorityQueue<Request> pq = new PriorityQueue<>(
            (a, b) -> {
                // Compare priority first (descending order)
                if (b.priority != a.priority)
                    return b.priority - a.priority;

                // If priority same, compare by end time (ascending)
                return a.endTime - b.endTime;
            }
        );

        // Insert all requests into the heap
        pq.addAll(requests);

        // This list will store the final selected (non-overlapping) requests
        List<Request> selected = new ArrayList<>();

        /*
         * Process requests one by one in priority order.
         * Each poll() operation removes the highest priority request.
         */
        while (!pq.isEmpty()) {

            // Extract highest priority request
            Request current = pq.poll();

            // Flag to check if current request overlaps with already selected ones
            boolean conflict = false;

            /*
             * Check for time conflict with all already selected requests.
             *
             * Two intervals DO NOT overlap if:
             * current.endTime <= allocated.startTime
             * OR
             * current.startTime >= allocated.endTime
             *
             * If neither condition is true → they overlap.
             */
            for (Request allocated : selected) {

                if (!(current.endTime <= allocated.startTime ||
                      current.startTime >= allocated.endTime)) {

                    // Overlap detected
                    conflict = true;
                    break;
                }
            }

            // If no conflict found, add request to selected list
            if (!conflict) {
                selected.add(current);
            }
        }

        // Return final allocated list
        return selected;
    }
}
