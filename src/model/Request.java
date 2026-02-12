package model;

// Request class represents one student's booking request
public class Request {

    // Name of the student requesting resource
    public String studentName;

    // Start time of request
    public int startTime;

    // End time of request
    public int endTime;

    // Higher value = higher priority
    public int priority;

    // Constructor to initialize request object
    public Request(String studentName, int startTime, int endTime, int priority) {
        this.studentName = studentName;
        this.startTime = startTime;
        this.endTime = endTime;
        this.priority = priority;
    }

    // Defines how the object will be printed
    @Override
    public String toString() {
        return studentName + " [" + startTime + " - " + endTime + "] Priority: " + priority;
    }
}
