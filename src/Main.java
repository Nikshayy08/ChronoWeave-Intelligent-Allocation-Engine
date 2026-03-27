import java.util.*;
import models.Request;
import models.Resource;
import models.Student;
import engine.AllocationEngine;

/*
 * Main.java
 *
 * Terminal entry point for Smart Campus Resource Exchange System.
 * Will be replaced by JavaFX UI in next phase.
 *
 * Menu:
 *  1.  Add Student
 *  2.  List Resource (Sell / Rent / Digital)
 *  3.  Post Buy Request
 *  4.  Post Rent Request
 *  5.  View All Listings
 *  6.  View All Requests
 *  7.  View Digital Resources
 *  8.  Run Rent Matching
 *  9.  Check Buy Exchange Cycle
 *  10. Exit
 */

public class Main {

    public static void main(String[] args) {

        Scanner sc     = new Scanner(System.in);
        AllocationEngine engine = new AllocationEngine();

        while (true) {

            System.out.println("\n╔══════════════════════════════════════════╗");
            System.out.println("║   SMART CAMPUS RESOURCE EXCHANGE         ║");
            System.out.println("╚══════════════════════════════════════════╝");
            System.out.println(" 1.  Add Student");
            System.out.println(" 2.  List a Resource (Sell/Rent/Digital)");
            System.out.println(" 3.  Post Buy Request");
            System.out.println(" 4.  Post Rent Request");
            System.out.println(" 5.  View All Listings");
            System.out.println(" 6.  View All Requests");
            System.out.println(" 7.  View Digital Resources");
            System.out.println(" 8.  Run Rent Matching");
            System.out.println(" 9.  Check Buy Exchange Cycle");
            System.out.println(" 10. Exit");
            System.out.print("\nChoice: ");

            int choice;
            try { choice = Integer.parseInt(sc.nextLine().trim()); }
            catch (NumberFormatException e) { System.out.println("Invalid."); continue; }

            switch (choice) {

                // ── 1. Add Student ───────────────────────────────────────────
                case 1: {
                    System.out.print("Student ID      : ");
                    String sid = sc.nextLine().trim();
                    System.out.print("Student Name    : ");
                    String sname = sc.nextLine().trim();
                    System.out.print("Contact Number  : ");
                    String contact = sc.nextLine().trim();
                    System.out.print("WhatsApp Number : ");
                    String whatsapp = sc.nextLine().trim();
                    engine.addStudent(new Student(sid, sname, contact, whatsapp));
                    System.out.println("Student added.");
                    break;
                }

                // ── 2. List Resource ─────────────────────────────────────────
                case 2: {
                    System.out.print("Resource ID   : ");
                    String rid = sc.nextLine().trim();
                    System.out.print("Resource Name : ");
                    String rname = sc.nextLine().trim();
                    System.out.print("Listing Type (SELL / RENT / DIGITAL) : ");
                    String ltype = sc.nextLine().trim().toUpperCase();

                    Resource.ListingType listingType;
                    try { listingType = Resource.ListingType.valueOf(ltype); }
                    catch (IllegalArgumentException e) { System.out.println("Invalid type."); break; }

                    System.out.print("Owned By (student name) : ");
                    String owner = sc.nextLine().trim();

                    Resource.ResourceType rtype = (listingType == Resource.ListingType.DIGITAL)
                            ? Resource.ResourceType.DIGITAL
                            : Resource.ResourceType.PHYSICAL;

                    Resource resource = new Resource(rid, rname, rtype, listingType, owner);

                    if (listingType == Resource.ListingType.SELL
                        || listingType == Resource.ListingType.RENT) {
                        System.out.print("Asking Price (Rs.) : ");
                        try { resource.setAskingPrice(Double.parseDouble(sc.nextLine().trim())); }
                        catch (NumberFormatException e) { System.out.println("Invalid price."); break; }
                    }

                    if (listingType == Resource.ListingType.DIGITAL) {
                        System.out.print("Download Link (Google Drive URL) : ");
                        resource.setDownloadLink(sc.nextLine().trim());
                    }

                    engine.addResource(resource);
                    System.out.println("Resource listed successfully.");
                    break;
                }

                // ── 3. Post Buy Request ──────────────────────────────────────
                case 3: {
                    System.out.print("Your Name        : ");
                    String name = sc.nextLine().trim();
                    System.out.print("Resource Wanted  : ");
                    String resName = sc.nextLine().trim();
                    System.out.print("Your Budget (Rs.): ");
                    double budget;
                    try { budget = Double.parseDouble(sc.nextLine().trim()); }
                    catch (NumberFormatException e) { System.out.println("Invalid."); break; }

                    Request req = new Request(name, resName, budget);
                    boolean accepted = engine.addRequest(req);
                    if (accepted) System.out.println("Buy request posted.");
                    break;
                }

                // ── 4. Post Rent Request ─────────────────────────────────────
                case 4: {
                    System.out.print("Your Name        : ");
                    String name = sc.nextLine().trim();
                    System.out.print("Resource Wanted  : ");
                    String resName = sc.nextLine().trim();
                    System.out.print("Offering (Rs.)   : ");
                    double offering;
                    try { offering = Double.parseDouble(sc.nextLine().trim()); }
                    catch (NumberFormatException e) { System.out.println("Invalid."); break; }
                    System.out.print("Start Time (int) : ");
                    int start;
                    try { start = Integer.parseInt(sc.nextLine().trim()); }
                    catch (NumberFormatException e) { System.out.println("Invalid."); break; }
                    System.out.print("End Time   (int) : ");
                    int end;
                    try { end = Integer.parseInt(sc.nextLine().trim()); }
                    catch (NumberFormatException e) { System.out.println("Invalid."); break; }

                    Request req = new Request(name, resName,
                            Request.RequestType.NEED_TO_RENT, offering, start, end);
                    boolean accepted = engine.addRequest(req);
                    if (accepted) System.out.println("Rent request posted.");
                    break;
                }

                // ── 5. View All Listings ─────────────────────────────────────
                case 5: {
                    System.out.println("\n--- All Listings ---");
                    engine.displayAllResources();
                    break;
                }

                // ── 6. View All Requests ─────────────────────────────────────
                case 6: {
                    System.out.println("\n--- All Requests ---");
                    engine.displayAllRequests();
                    break;
                }

                // ── 7. View Digital Resources ────────────────────────────────
                case 7: {
                    System.out.println("\n--- Digital Resources (Free Access) ---");
                    engine.displayDigitalResources();
                    break;
                }

                // ── 8. Run Rent Matching ─────────────────────────────────────
                case 8: {
                    System.out.println("\n--- Rent Matching Result ---");
                    List<Request> result = engine.runRentAllocation();
                    if (result.isEmpty()) {
                        System.out.println("No rent requests to process.");
                    } else {
                        for (Request r : result) {
                            System.out.println(r);
                            if (r.getStatus() == Request.Status.WAITLISTED) {
                                int next = engine.suggestNextAvailableSlot(r.getResourceName());
                                System.out.println("  -> Next free slot: from time " + next);
                            }
                        }
                    }
                    break;
                }

                // ── 9. Check Exchange Cycle ──────────────────────────────────
                case 9: {
                    System.out.println("\n--- Buy Exchange Cycle Detection ---");
                    boolean cycle = engine.checkExchangeCycle();
                    if (!cycle) System.out.println("No exchange cycle found.");
                    break;
                }

                // ── 10. Exit ─────────────────────────────────────────────────
                case 10: {
                    System.out.println("Goodbye.");
                    sc.close();
                    return;
                }

                default:
                    System.out.println("Invalid choice. Enter 1-10.");
            }
        }
    }
}