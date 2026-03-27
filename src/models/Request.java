package models;

// The Request class represents user demand in the system.
// It stores details like resource required, offering price, and time interval for rent requests.
// We also implemented a priority scoring function which is used in scheduling algorithms to ensure fair and optimized allocation.”
// This class represents a demand made by a student.
/*
 * Request.java
 *
 * Represents a student's request in the exchange system.
 *
 * Two kinds of requests:
 *
 *  NEED_TO_BUY   → Student needs a resource permanently (SELL match)
 *  NEED_TO_RENT  → Student needs a resource temporarily (RENT match)
 *
 * Digital resources have no request — freely accessible via link.
 *
 * Key fields:
 *  offeringPrice → what the requester is willing to pay (displayed to owners)
 *  startTime     → for RENT only — when they need it
 *  endTime       → for RENT only — when they'll return it
 *
 * DAA Relevance:
 *  RENT requests → used in PriorityScheduler for conflict detection
 *  BUY requests  → used in ExchangeGraph for mutual swap detection
 *
 * Priority Formula (for RENT scheduling):
 *  Higher offering price → higher priority in queue
 *  Earlier end time      → tiebreaker (frees resource sooner)
 */

public class Request {

    // ─── Enums ────────────────────────────────────────────────────────────────

    public enum RequestType {
        NEED_TO_BUY,    // Permanent — match with SELL listing
        NEED_TO_RENT    // Temporary — match with RENT listing, conflict checked
    }

    public enum Status {
        PENDING,        // Submitted, waiting for match
        MATCHED,        // Owner/seller found, contact revealed
        REJECTED,       // Conflict detected (RENT only)
        WAITLISTED      // In queue, waiting for resource to free up
    }

    // ─── Fields ───────────────────────────────────────────────────────────────

    private String studentName;
    private String resourceName;
    private RequestType requestType;
    private double offeringPrice;     // What requester is willing to pay
    private int startTime;            // RENT only
    private int endTime;              // RENT only
    private Status status;
    private long createdAt;           // For waitlist aging

    // ─── Constructor (RENT) ───────────────────────────────────────────────────

    public Request(String studentName, String resourceName,
                   RequestType requestType, double offeringPrice,
                   int startTime, int endTime) {
        this.studentName   = studentName;
        this.resourceName  = resourceName;
        this.requestType   = requestType;
        this.offeringPrice = offeringPrice;
        this.startTime     = startTime;
        this.endTime       = endTime;
        this.status        = Status.PENDING;
        this.createdAt     = System.currentTimeMillis();
    }

    // ─── Constructor (BUY — no time slot needed) ──────────────────────────────

    public Request(String studentName, String resourceName,
                   double offeringPrice) {
        this(studentName, resourceName, RequestType.NEED_TO_BUY,
             offeringPrice, 0, 0);
    }

    // ─── Priority Score (for RENT scheduling) ─────────────────────────────────

    /*
     * computePriorityScore()
     *
     * Higher offering price = higher priority (willing to pay more = more urgent)
     * Longer wait time = slight boost (prevents starvation)
     *
     * Formula:
     *   score = (offeringPrice * 0.7) + (waitingMinutes * 0.3)
     */
    public double computePriorityScore() {
        long waitingMinutes = (System.currentTimeMillis() - createdAt) / 60000;
        return (offeringPrice * 0.7) + (waitingMinutes * 0.3);
    }

    // ─── Getters ──────────────────────────────────────────────────────────────

    public String getStudentName()    { return studentName; }
    public String getResourceName()   { return resourceName; }
    public RequestType getRequestType() { return requestType; }
    public double getOfferingPrice()  { return offeringPrice; }
    public int getStartTime()         { return startTime; }
    public int getEndTime()           { return endTime; }
    public Status getStatus()         { return status; }
    public long getCreatedAt()        { return createdAt; }

    // ─── Setters ──────────────────────────────────────────────────────────────

    public void setStatus(Status status) { this.status = status; }    
        // Used when:  MATCHED or  WAITLISTED

    // ─── Display ──────────────────────────────────────────────────────────────

    // This method converts a Request object into a user-friendly string based on its type.”
    @Override
    public String toString() {
        if (requestType == RequestType.NEED_TO_RENT) {
            return String.format("[RENT REQUEST] %s needs %s | Time: %d-%d | Offering: Rs.%.0f | Status: %s",
                    studentName, resourceName, startTime, endTime, offeringPrice, status);
        } else {
            return String.format("[BUY REQUEST] %s wants to buy %s | Budget: Rs.%.0f | Status: %s",
                    studentName, resourceName, offeringPrice, status);
        }
    }
}