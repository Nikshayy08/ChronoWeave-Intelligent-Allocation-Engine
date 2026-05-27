import engine.AllocationEngine;
import models.Resource;
import models.Student;
import models.Request;
import database.DatabaseManager;

/*
 * DataSeeder.java
 *
 * Clean refined demo dataset for DAA project.
 * Covers:
 *  - Buy Matching
 *  - Rent Scheduling
 *  - Conflict Detection
 *  - DFS Exchange Cycle
 *  - Digital Resource Sharing
 */

public class DataSeeder {

    public static void seed(AllocationEngine engine, DatabaseManager db) {

        // Prevent duplicate seeding
        if (!engine.getAllStudents().isEmpty())
            return;

        System.out.println("Seeding demo data...");

        // ─────────────────────────────────────────────────────────────
        // STUDENTS
        // ─────────────────────────────────────────────────────────────

        Student s1 = new Student("S001", "Nikshay Joshi", "7983990531", "7983990531");
        Student s2 = new Student("S002", "Dhruv Pathak", "8217637318", "9823789318");
        Student s3 = new Student("S003", "Shagun Sharma", "9876543210", "9876543210");
        Student s4 = new Student("S004", "Kailash Singh", "9988776655", "9988776655");
        Student s5 = new Student("S005", "Aarav Mehta", "9876500005", "9876500005");
        Student s6 = new Student("S006", "Priyansh Verma", "9876500006", "9876500006");
        Student s7 = new Student("S007", "Harsh Rana", "9876500007", "9876500007");
        Student s8 = new Student("S008", "Yash Thakur", "9876500008", "9876500008");
        Student s9 = new Student("S009", "Aryan Rawat", "9876500009", "9876500009");
        Student s10 = new Student("S010", "Kabir Singh", "9876500010", "9876500010");

        engine.addStudent(s1);
        engine.addStudent(s2);
        engine.addStudent(s3);
        engine.addStudent(s4);
        engine.addStudent(s5);
        engine.addStudent(s6);
        engine.addStudent(s7);
        engine.addStudent(s8);
        engine.addStudent(s9);
        engine.addStudent(s10);

        // ─────────────────────────────────────────────────────────────
        // SELL RESOURCES
        // ─────────────────────────────────────────────────────────────

        Resource r1 = new Resource(
                "R001",
                "Drafter Set",
                Resource.ResourceType.PHYSICAL,
                Resource.ListingType.SELL,
                "Nikshay Joshi");
        r1.setAskingPrice(250);

        Resource r2 = new Resource(
                "R002",
                "Arduino Kit",
                Resource.ResourceType.PHYSICAL,
                Resource.ListingType.SELL,
                "Shagun Sharma");
        r2.setAskingPrice(1500);

        Resource r3 = new Resource(
                "R003",
                "Java Programming Book",
                Resource.ResourceType.PHYSICAL,
                Resource.ListingType.SELL,
                "Aarav Mehta");
        r3.setAskingPrice(350);

        // ─────────────────────────────────────────────────────────────
        // RENT RESOURCES
        // ─────────────────────────────────────────────────────────────

        Resource r4 = new Resource(
                "R004",
                "Scientific Calculator",
                Resource.ResourceType.PHYSICAL,
                Resource.ListingType.RENT,
                "Dhruv Pathak");
        r4.setAskingPrice(10);

        Resource r5 = new Resource(
                "R005",
                "DSLR Camera",
                Resource.ResourceType.PHYSICAL,
                Resource.ListingType.RENT,
                "Harsh Rana");
        r5.setAskingPrice(200);

        // ─────────────────────────────────────────────────────────────
        // DIGITAL RESOURCES
        // ─────────────────────────────────────────────────────────────

        Resource r6 = new Resource(
                "R006",
                "DSA Notes",
                Resource.ResourceType.DIGITAL,
                Resource.ListingType.DIGITAL,
                "Nikshay Joshi");
        r6.setDownloadLink("https://drive.google.com/file/d/dsa_notes_demo");

        Resource r7 = new Resource(
                "R007",
                "DBMS PYQs",
                Resource.ResourceType.DIGITAL,
                Resource.ListingType.DIGITAL,
                "Shagun Sharma");
        r7.setDownloadLink("https://drive.google.com/file/d/dbms_pyq_demo");

        Resource r8 = new Resource(
                "R008",
                "Operating System Notes",
                Resource.ResourceType.DIGITAL,
                Resource.ListingType.DIGITAL,
                "Kailash Singh");
        r8.setDownloadLink("https://drive.google.com/file/d/os_notes_demo");

        Resource r9 = new Resource(
                "R009",
                "Computer Networks Notes",
                Resource.ResourceType.DIGITAL,
                Resource.ListingType.DIGITAL,
                "Priyansh Verma");
        r9.setDownloadLink("https://drive.google.com/file/d/cn_notes_demo");

        // ─────────────────────────────────────────────────────────────
        // EXCHANGE CYCLE RESOURCES
        // ─────────────────────────────────────────────────────────────

        Resource r10 = new Resource(
                "R010",
                "OS Book",
                Resource.ResourceType.PHYSICAL,
                Resource.ListingType.SELL,
                "Aarav Mehta");

        Resource r11 = new Resource(
                "R011",
                "DBMS Book",
                Resource.ResourceType.PHYSICAL,
                Resource.ListingType.SELL,
                "Priyansh Verma");

        Resource r12 = new Resource(
                "R012",
                "Java Book",
                Resource.ResourceType.PHYSICAL,
                Resource.ListingType.SELL,
                "Yash Thakur");

        engine.addResource(r1);
        engine.addResource(r2);
        engine.addResource(r3);
        engine.addResource(r4);
        engine.addResource(r5);
        engine.addResource(r6);
        engine.addResource(r7);
        engine.addResource(r8);
        engine.addResource(r9);
        engine.addResource(r10);
        engine.addResource(r11);
        engine.addResource(r12);

        // ─────────────────────────────────────────────────────────────
        // BUY REQUESTS
        // ─────────────────────────────────────────────────────────────

        // Successful Match
        engine.addRequest(new Request(
                "Aryan Rawat",
                "Drafter Set",
                300));

        // Budget Too Low
        engine.addRequest(new Request(
                "Kabir Singh",
                "Arduino Kit",
                1000));

        // No Listing Found
        engine.addRequest(new Request(
                "Kailash Singh",
                "3D Printer",
                5000));

        // ─────────────────────────────────────────────────────────────
        // RENT REQUESTS
        // ─────────────────────────────────────────────────────────────

        // Overlapping conflict demo

        engine.addRequest(new Request(
                "Aarav Mehta",
                "Scientific Calculator",
                Request.RequestType.NEED_TO_RENT,
                200,
                2,
                5));

        engine.addRequest(new Request(
                "Priyansh Verma",
                "Scientific Calculator",
                Request.RequestType.NEED_TO_RENT,
                500,
                3,
                6));

        // Non-overlapping successful allocation

        engine.addRequest(new Request(
                "Yash Thakur",
                "Scientific Calculator",
                Request.RequestType.NEED_TO_RENT,
                250,
                6,
                8));

        // DSLR conflict demo

        engine.addRequest(new Request(
                "Nikshay Joshi",
                "DSLR Camera",
                Request.RequestType.NEED_TO_RENT,
                400,
                1,
                4));

        engine.addRequest(new Request(
                "Shagun Sharma",
                "DSLR Camera",
                Request.RequestType.NEED_TO_RENT,
                600,
                2,
                5));

        // ─────────────────────────────────────────────────────────────
        // EXCHANGE CYCLE REQUESTS
        // ─────────────────────────────────────────────────────────────

        engine.addRequest(new Request(
                "Aarav Mehta",
                "DBMS Book",
                400));

        engine.addRequest(new Request(
                "Priyansh Verma",
                "Java Book",
                400));

        engine.addRequest(new Request(
                "Yash Thakur",
                "OS Book",
                400));

        System.out.println(
                "Demo data seeded successfully.");
    }
}