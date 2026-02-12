package models;

/*
 * Request
 *
 * Represents a student's resource booking request.
 *
 * Attributes:
 *  - studentName : Name of the student
 *  - startTime   : Requested start time
 *  - endTime     : Requested end time
 *  - priority    : Priority level (higher value = higher importance)
 *
 * This class is a data model (POJO).
 */

public class Request {

    // Name of the student requesting the resource
    public String studentName;

    // Start time of request
    public int startTime;

    // End time of request
    public int endTime;

    // Priority value (higher means more important)
    public int priority;

    // Constructor to initialize request object
    public Request(String studentName, int startTime, int endTime, int priority) {
        this.studentName = studentName;
        this.startTime = startTime;
        this.endTime = endTime;
        this.priority = priority;
    }

    // String representation for printing request details
    @Override
    public String toString() {
        return studentName + " [" + startTime + " - " + endTime + "] Priority: " + priority;
    }
}
