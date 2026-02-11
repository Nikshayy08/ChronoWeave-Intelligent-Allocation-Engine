// Request class represents one student's booking request
public class Request {

    // Name of the student requesting resource
    String studentName;

    // Start time of request
    int startTime;

    // End time of request
    int endTime;

    int priority; // Higher = more urgent

    // Constructor to initialize request object
    public Request(String studentName, int startTime, int endTime, int priority) {
        this.studentName = studentName;
        this.startTime = startTime;
        this.endTime = endTime;
        this.priority=priority;
    }

    // This method defines how object will be printed
    @Override
    public String toString() {
       return studentName + " [" + startTime + " - " + endTime + "]" + " Priority: " + priority;
    }
}
