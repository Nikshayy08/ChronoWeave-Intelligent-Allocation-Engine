import engine.AllocationEngine;
import models.Resource;
import models.Student;
import database.DatabaseManager;

/*
 * DataSeeder.java
 *
 * Pre-loads the system with realistic demo data on first run.
 *
 * Seeds:
 *  - 4 students (representing real campus scenario)
 *  - 6 resources (SELL + RENT + DIGITAL mix)
 *
 * Only seeds if database is empty — never duplicates data.
 *
 * Location: src/DataSeeder.java
 */

public class DataSeeder {

    public static void seed(AllocationEngine engine, DatabaseManager db) {

        // Only seed if no students exist yet
        if (!engine.getAllStudents().isEmpty()) return;

        System.out.println("Seeding demo data...");

        // ── Students ──────────────────────────────────────────────────────────

        Student s1 = new Student("S001", "Nikshay Joshi",    "7983990531", "7983990531");
        Student s2 = new Student("S002", "Dhruv Pathak",     "8217637318", "9823789318");
        Student s3 = new Student("S003", "Shagun Sharma",    "9876543210", "9876543210");
        Student s4 = new Student("S004", "Kailash Singh",    "9988776655", "9988776655");

        engine.addStudent(s1);
        engine.addStudent(s2);
        engine.addStudent(s3);
        engine.addStudent(s4);

        if (db.isConnected()) {
            db.saveStudent(s1);
            db.saveStudent(s2);
            db.saveStudent(s3);
            db.saveStudent(s4);
        }

        // ── Resources ─────────────────────────────────────────────────────────

        // SELL listings — permanent items after semester
        Resource r1 = new Resource("R001", "Drafter Set",
            Resource.ResourceType.PHYSICAL, Resource.ListingType.SELL, "Nikshay Joshi");
        r1.setAskingPrice(250);

        Resource r2 = new Resource("R002", "Engineering Drawing Kit",
            Resource.ResourceType.PHYSICAL, Resource.ListingType.SELL, "Shagun Sharma");
        r2.setAskingPrice(180);

        // RENT listings — short term borrowing
        Resource r3 = new Resource("R003", "Scientific Calculator",
            Resource.ResourceType.PHYSICAL, Resource.ListingType.RENT, "Dhruv Pathak");
        r3.setAskingPrice(10);

        Resource r4 = new Resource("R004", "Lab Coat",
            Resource.ResourceType.PHYSICAL, Resource.ListingType.RENT, "Kailash Singh");
        r4.setAskingPrice(15);

        // DIGITAL listings — free notes
        Resource r5 = new Resource("R005", "Data Structures PYQ 2023",
            Resource.ResourceType.DIGITAL, Resource.ListingType.DIGITAL, "Nikshay Joshi");
        r5.setDownloadLink("https://drive.google.com/file/d/pyq2023");

        Resource r6 = new Resource("R006", "OOPs in Java Notes",
            Resource.ResourceType.DIGITAL, Resource.ListingType.DIGITAL, "Shagun Sharma");
        r6.setDownloadLink("https://drive.google.com/file/d/oopsnotes");

        engine.addResource(r1);
        engine.addResource(r2);
        engine.addResource(r3);
        engine.addResource(r4);
        engine.addResource(r5);
        engine.addResource(r6);

        if (db.isConnected()) {
            db.saveResource(r1);
            db.saveResource(r2);
            db.saveResource(r3);
            db.saveResource(r4);
            db.saveResource(r5);
            db.saveResource(r6);
        }

        System.out.println("Demo data seeded: 4 students, 6 resources (2 SELL + 2 RENT + 2 DIGITAL)");
    }
}