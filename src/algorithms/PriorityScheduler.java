package algorithms;

import java.util.*;
import models.Request;

/*
    It decides who gets a resource first when multiple students request the same item for rent
    
 * PriorityScheduler.java
 *
 * Handles RENT request scheduling using Priority Queue + Interval Conflict Detection.
 *
 * Strategy:
 *  1. Build Max Heap ordered by priority score
 *     Score = (offeringPrice * 0.7) + (waitingMinutes * 0.3)
 *     Higher offering price = more urgent = processed first
 *     Longer wait = slight boost to prevent starvation
 *
 *  2. For each request, check interval conflict:
 *     If same resource already allocated in overlapping time -> WAITLISTED
 *     Else -> MATCHED
 *
 *  3. Tiebreaker: earlier end time first (frees resource sooner)
 *
 * Time Complexity:
 *  Heap insertion : O(n log n)
 *  Conflict check : O(n^2) worst case
 */

public class PriorityScheduler {

    public static List<Request> allocate(List<Request> requests) {

        // Max Heap by priority score
        PriorityQueue<Request> pq = new PriorityQueue<>(
            (a, b) -> {
                double scoreA = a.computePriorityScore();
                double scoreB = b.computePriorityScore();

                if (Double.compare(scoreB, scoreA) != 0)
                    return Double.compare(scoreB, scoreA);

                // Tiebreaker: earlier end time first
                return a.getEndTime() - b.getEndTime();
            }
        );

        pq.addAll(requests);

        List<Request> matched    = new ArrayList<>();
        List<Request> waitlisted = new ArrayList<>();

        while (!pq.isEmpty()) {

            Request current  = pq.poll();
            boolean conflict = false;

            // Check overlap with already matched requests for same resource
            for (Request done : matched) {
                if (done.getResourceName().equals(current.getResourceName())) {

                    boolean overlaps = !(current.getEndTime()   <= done.getStartTime()
                                      || current.getStartTime() >= done.getEndTime());
                    if (overlaps) {
                        conflict = true;
                        break;
                    }
                }
            }

            if (!conflict) {
                current.setStatus(Request.Status.MATCHED);
                matched.add(current);
            } else {
                current.setStatus(Request.Status.WAITLISTED);
                waitlisted.add(current);
            }
        }

        List<Request> result = new ArrayList<>(matched);
        result.addAll(waitlisted);
        return result;
    }

    /*
     * suggestNextSlot(resourceName, allocated)
     *
     * Finds the earliest free time slot for a resource
     * after all currently matched slots end.
     *
     * Time Complexity: O(k log k) where k = matched slots for resource
     */
    public static int suggestNextSlot(String resourceName, List<Request> allocated) {

        List<int[]> slots = new ArrayList<>();

        for (Request r : allocated) {
            if (r.getResourceName().equals(resourceName)
                && r.getStatus() == Request.Status.MATCHED) {
                slots.add(new int[]{ r.getStartTime(), r.getEndTime() });
            }
        }

        if (slots.isEmpty()) return 0;

        slots.sort((a, b) -> a[1] - b[1]);
        return slots.get(slots.size() - 1)[1];
    }
}