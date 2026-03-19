import engine.AllocationEngine;
import models.Resource;
import models.Student;

/*
 * DataSeeder.java
 *
 * Preloads the system with initial students and resources on startup.
 *
 * Purpose:
 *  Solves the cold start problem — system is never empty on first launch.
 *  New users can immediately request resources without waiting for
 *  other students to contribute first.
 *
 * Admin Account:
 *  A system-level "Admin" student owns all seeded resources.
 *  Admin starts with high karma so they never get blocked.
 *
 * Seeded Resources:
 *  Digital  → PYQs, Notes, Lab Manual (shareable, no conflict)
 *  Physical → Drafter, Scientific Calculator (one user at a time)
 *
 * Location: src/DataSeeder.java (same level as Main.java)
 */

public class DataSeeder {

    public static void seed(AllocationEngine engine) {

        System.out.println("⚙ Seeding system with initial data...");

        // ── Create Admin Student ───────────────────────────────────────────
        Student admin = new Student("ADMIN", "Admin");

        // Give admin enough credits to never get blocked
        // Done by contributing resources below — credits are auto-earned
        engine.addStudent(admin);

        // ── Seed Digital Resources ─────────────────────────────────────────
        // Digital = infinitely shareable, no conflict detection applied

        Resource pyq1 = new Resource("R001", "CSE PYQ 2023",
                Resource.ResourceType.DIGITAL, "Admin");
        pyq1.addTag("CSE");
        pyq1.addTag("PYQ");
        pyq1.addTag("2023");
        engine.addResource(pyq1);

        Resource pyq2 = new Resource("R002", "Physics PYQ 2023",
                Resource.ResourceType.DIGITAL, "Admin");
        pyq2.addTag("Physics");
        pyq2.addTag("PYQ");
        pyq2.addTag("2023");
        engine.addResource(pyq2);

        Resource notes1 = new Resource("R003", "Data Structures Notes",
                Resource.ResourceType.DIGITAL, "Admin");
        notes1.addTag("CSE");
        notes1.addTag("DSA");
        notes1.addTag("Notes");
        engine.addResource(notes1);

        Resource notes2 = new Resource("R004", "Mathematics Notes",
                Resource.ResourceType.DIGITAL, "Admin");
        notes2.addTag("Maths");
        notes2.addTag("Notes");
        engine.addResource(notes2);

        Resource labManual = new Resource("R005", "Chemistry Lab Manual",
                Resource.ResourceType.DIGITAL, "Admin");
        labManual.addTag("Chemistry");
        labManual.addTag("Lab");
        labManual.addTag("Manual");
        engine.addResource(labManual);

        // ── Seed Physical Resources ────────────────────────────────────────
        // Physical = one user at a time, interval conflict detection applied

        Resource drafter = new Resource("R006", "Drafter",
                Resource.ResourceType.PHYSICAL, "Admin");
        drafter.addTag("Drawing");
        drafter.addTag("Engineering");
        engine.addResource(drafter);

        Resource calculator = new Resource("R007", "Scientific Calculator",
                Resource.ResourceType.PHYSICAL, "Admin");
        calculator.addTag("Maths");
        calculator.addTag("Physics");
        engine.addResource(calculator);

        Resource labKit = new Resource("R008", "Electronics Lab Kit",
                Resource.ResourceType.PHYSICAL, "Admin");
        labKit.addTag("ECE");
        labKit.addTag("Lab");
        engine.addResource(labKit);

        System.out.println("✓ Seeded 8 resources (5 Digital + 3 Physical).");
        System.out.println("✓ System ready. New users get 2 free requests.\n");
    }
}