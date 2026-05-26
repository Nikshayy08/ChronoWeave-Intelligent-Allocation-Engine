package models;

/*
 * Student.java
 *
 * Represents a student in the Smart Campus Resource Exchange System.
 *
 * Stores:
 *  - Basic identity (id, name)
 *  - Contact details revealed only when a deal is matched
 *  - Listing and request counters for activity tracking
 *
 *  Real money handled in person.
 */

public class Student {

    private String studentId;
    private String name;
    private String contactNumber;     // Revealed to matched party only
    private String whatsappNumber;    // Optional WhatsApp for easier contact
    private int totalListings;
    private int totalRequests;

    // Full constructor
    public Student(String studentId, String name,
                   String contactNumber, String whatsappNumber) {
        this.studentId      = studentId;
        this.name           = name;
        this.contactNumber  = contactNumber;
        this.whatsappNumber = whatsappNumber;
        this.totalListings  = 0;
        this.totalRequests  = 0;
    }

    // Convenience constructor — contact added later
    public Student(String studentId, String name) {
        this(studentId, name, "Not provided", "Not provided");
    }

    public void incrementListings() { this.totalListings++; }
    public void incrementRequests() { this.totalRequests++; }

    public String getStudentId()      { return studentId; }
    public String getName()           { return name; }
    public String getContactNumber()  { return contactNumber; }
    public String getWhatsappNumber() { return whatsappNumber; }
    public int getTotalListings()     { return totalListings; }
    public int getTotalRequests()     { return totalRequests; }

    public void setContactNumber(String c)  { this.contactNumber = c; }
    public void setWhatsappNumber(String w) { this.whatsappNumber = w; }

    @Override
    public String toString() {
        return String.format("%s (%s) | Listings: %d | Requests: %d",
                name, studentId, totalListings, totalRequests);
    }
}