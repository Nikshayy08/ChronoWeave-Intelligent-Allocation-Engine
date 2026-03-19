package algorithms;

import java.util.*;
import models.Request;

/*
 * PriorityScheduler.java
 *
 * Implements Priority-Based Interval Scheduling with composite scoring.
 *
 * ─── SCHEDULING STRATEGY ─────────────────────────────────────────────────────
 *
 * Uses a Max Heap (PriorityQueue) ordered by composite priority score:
 *
 *   Score = (karmaCredits × 0.5) + (deadlineUrgency × 0.3) + (waitTime × 0.2)
 *
 * Where:
 *   karmaCredits   → students who contribute more get higher access priority
 *   deadlineUrgency → earlier deadlines float to top (EDF principle)
 *   waitTime        → aging: longer wait = higher score (prevents starvation)
 *
 * Tiebreaker order (if scores are equal):
 *   1. Earlier deadline first (EDF)
 *   2. Earlier end time first (frees slot sooner)
 *
 * ─── PHYSICAL vs DIGITAL BIFURCATION ─────────────────────────────────────────
 *
 * PHYSICAL resources:
 *   → Interval conflict detection applied
 *   → Only one student can hold the same resource in overlapping time
 *   → Rejected requests go to WAITLISTED status
 *
 * DIGITAL resources:
 *   → No conflict detection — PDFs/notes are infinitely shareable
 *   → All valid digital requests are ALLOCATED directly
 *   → This is the correct model; treating digital as scarce is a design flaw
 *
 * ─── TIME COMPLEXITY ─────────────────────────────────────────────────────────
 *
 *   Heap insertion (all n requests):  O(n log n)
 *   Conflict check per request:       O(k)   where k = already allocated
 *   Overall worst case:               O(n² ) — physical resources with n conflicts
 *   Digital resources:                O(n log n) — no conflict scan needed
 *
 * ─────────────────────────────────────────────────────────────────────────────
 */

public class PriorityScheduler {

    /*
     * allocate(requests, studentKarmaMap)
     *
     * @param requests       List of all pending requests
     * @param studentKarmaMap  Map of studentName → karmaCredits (for score computation)
     * @return               List of allocated requests (status updated in place)
     */
    public static List<Request> allocate(List<Request> requests,
                                         Map<String, Integer> studentKarmaMap) {

        // ── Build Max Heap ordered by composite priority score ──────────────
        PriorityQueue<Request> pq = new PriorityQueue<>(
            (a, b) -> {

                int karmaA = studentKarmaMap.getOrDefault(a.getStudentName(), 0);
                int karmaB = studentKarmaMap.getOrDefault(b.getStudentName(), 0);

                double scoreA = a.computePriorityScore(karmaA);
                double scoreB = b.computePriorityScore(karmaB);

                // Higher score → higher priority (max heap)
                if (Double.compare(scoreB, scoreA) != 0)
                    return Double.compare(scoreB, scoreA);

                // Tiebreaker 1: Earlier deadline first (EDF)
                if (a.getDeadline() != b.getDeadline())
                    return a.getDeadline() - b.getDeadline();

                // Tiebreaker 2: Earlier end time first
                return a.getEndTime() - b.getEndTime();
            }
        );

        pq.addAll(requests);

        List<Request> allocated   = new ArrayList<>();
        List<Request> waitlisted  = new ArrayList<>();

        // ── Process requests in priority order ─────────────────────────────
        while (!pq.isEmpty()) {

            Request current = pq.poll();

            // ── DIGITAL: No conflict — allocate immediately ─────────────────
            if (current.getResourceType() == Request.ResourceType.DIGITAL) {
                current.setStatus(Request.Status.ALLOCATED);
                allocated.add(current);
                continue;
            }

            // ── PHYSICAL: Check interval conflict ───────────────────────────
            boolean conflict = false;

            for (Request done : allocated) {

                // Only conflict if same resource name AND overlapping time
                if (done.getResourceType() == Request.ResourceType.PHYSICAL
                    && done.getResourceName().equals(current.getResourceName())) {

                    // Overlap condition: NOT (current ends before done starts
                    //                        OR current starts after done ends)
                    boolean overlaps = !(current.getEndTime()   <= done.getStartTime()
                                      || current.getStartTime() >= done.getEndTime());

                    if (overlaps) {
                        conflict = true;
                        break;
                    }
                }
            }

            if (!conflict) {
                current.setStatus(Request.Status.ALLOCATED);
                allocated.add(current);
            } else {
                current.setStatus(Request.Status.WAITLISTED);
                waitlisted.add(current);
            }
        }

        // ── Return allocated + waitlisted together for full picture ─────────
        List<Request> result = new ArrayList<>(allocated);
        result.addAll(waitlisted);
        return result;
    }

    /*
     * suggestNextSlot(resourceName, allocated)
     *
     * When a request is rejected due to conflict, suggest the
     * nearest free time slot for the same resource.
     *
     * Strategy: Find all allocated slots for the resource,
     *            sort by end time, return the first gap or
     *            the time after the last slot ends.
     *
     * Time Complexity: O(k log k) where k = allocated slots for resource
     */
    public static int suggestNextSlot(String resourceName, List<Request> allocated) {

        List<int[]> slots = new ArrayList<>();

        for (Request r : allocated) {
            if (r.getResourceType() == Request.ResourceType.PHYSICAL
                && r.getResourceName().equals(resourceName)
                && r.getStatus() == Request.Status.ALLOCATED) {

                slots.add(new int[]{ r.getStartTime(), r.getEndTime() });
            }
        }

        if (slots.isEmpty()) return 0;  // Resource is free now

        // Sort by end time
        slots.sort((a, b) -> a[1] - b[1]);

        // Return the end time of the last slot = earliest free moment
        return slots.get(slots.size() - 1)[1];
    }
}
