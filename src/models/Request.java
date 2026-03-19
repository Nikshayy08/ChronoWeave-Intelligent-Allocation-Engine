package models;

/*
 * Request.java
 *
 * Represents a student's resource booking request.
 *
 * Fields:
 *  - studentName  : Name of the requesting student
 *  - resourceName : Name of the resource being requested
 *  - resourceType : PHYSICAL or DIGITAL
 *  - startTime    : Requested start time (integer slot)
 *  - endTime      : Requested end time (integer slot)
 *  - priority     : Base priority (1–10, set by student urgency)
 *  - deadline     : Hard deadline for the request (for EDF scheduling)
 *  - status       : Current lifecycle state of this request
 *
 * Priority Formula (composite, computed dynamically):
 *   Score = (karmaCredits * 0.5) + (deadlineUrgency * 0.3) + (waitTime * 0.2)
 *
 * DAA Concepts:
 *  - Used in Max Heap (PriorityScheduler) → O(log n) insertion
 *  - Interval conflict detection → O(n) per request
 */

public class Request {

    // ─── Enums ────────────────────────────────────────────────────────────────

    public enum ResourceType {
        PHYSICAL,   // Drafter, lab instrument — only one user at a time
        DIGITAL     // Notes, PDFs, PYQs — multiple users simultaneously OK
    }

    public enum Status {
        PENDING,        // Just submitted, not yet processed
        ALLOCATED,      // Successfully assigned
        REJECTED,       // Conflict detected, not allocated
        WAITLISTED      // In queue, waiting for resource to free up
    }

    // ─── Fields ───────────────────────────────────────────────────────────────

    private String studentName;
    private String resourceName;
    private ResourceType resourceType;
    private int startTime;
    private int endTime;
    private int priority;       // Base priority (1–10)
    private int deadline;       // Used in EDF tiebreaker
    private Status status;

    // Timestamp when request was created — used for waitlist aging
    private long createdAt;

    // ─── Constructor ──────────────────────────────────────────────────────────

    public Request(String studentName, String resourceName, ResourceType resourceType,
                   int startTime, int endTime, int priority, int deadline) {

        this.studentName  = studentName;
        this.resourceName = resourceName;
        this.resourceType = resourceType;
        this.startTime    = startTime;
        this.endTime      = endTime;
        this.priority     = priority;
        this.deadline     = deadline;
        this.status       = Status.PENDING;
        this.createdAt    = System.currentTimeMillis();
    }

    // ─── Composite Priority Score ─────────────────────────────────────────────

    /*
     * computePriorityScore(karmaCredits)
     *
     * Computes a weighted priority score at scheduling time.
     *
     * Formula:
     *   score = (karmaCredits × 0.5)
     *         + (deadlineUrgency × 0.3)    ← inverse of deadline (sooner = more urgent)
     *         + (waitingMinutes × 0.2)     ← aging: longer wait = higher score
     *
     * This directly maps to Greedy + EDF scheduling strategies.
     */
    public double computePriorityScore(int karmaCredits) {

        // How long this request has been waiting (in minutes)
        long waitingMinutes = (System.currentTimeMillis() - createdAt) / 60000;

        // Deadline urgency: smaller deadline value = more urgent = higher urgency score
        // Cap at 100 to keep scale consistent
        double deadlineUrgency = Math.max(0, 100 - deadline);

        return (karmaCredits * 0.5)
             + (deadlineUrgency * 0.3)
             + (waitingMinutes  * 0.2);
    }

    // ─── Getters ──────────────────────────────────────────────────────────────

    public String getStudentName()        { return studentName; }
    public String getResourceName()       { return resourceName; }
    public ResourceType getResourceType() { return resourceType; }
    public int getStartTime()             { return startTime; }
    public int getEndTime()               { return endTime; }
    public int getPriority()              { return priority; }
    public int getDeadline()              { return deadline; }
    public Status getStatus()             { return status; }
    public long getCreatedAt()            { return createdAt; }

    // ─── Setters ──────────────────────────────────────────────────────────────

    public void setStatus(Status status)  { this.status = status; }

    // ─── Display ──────────────────────────────────────────────────────────────

    @Override
    public String toString() {
        return String.format("[%s] %s → %s | Time: %d–%d | Priority: %d | Deadline: %d | Status: %s",
                resourceType, studentName, resourceName,
                startTime, endTime, priority, deadline, status);
    }
}
