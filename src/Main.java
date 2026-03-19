import java.util.*;
import models.Request;
import models.Resource;
import models.Student;
import engine.AllocationEngine;

/*
 * Main.java
 *
 * Console entry point. Will be replaced by JavaFX in next phase.
 * DataSeeder.seed(engine) preloads system with initial resources on startup.
 */

public class Main {

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);
        AllocationEngine engine = new AllocationEngine();

        // Preload system with admin resources on startup
        DataSeeder.seed(engine);

        while (true) {

            System.out.println("\n╔══════════════════════════════════════════╗");
            System.out.println("║   SMART CAMPUS RESOURCE ALLOCATION       ║");
            System.out.println("╚══════════════════════════════════════════╝");
            System.out.println(" 1.  Add Student");
            System.out.println(" 2.  Add Resource");
            System.out.println(" 3.  Add Request");
            System.out.println(" 4.  View All Requests");
            System.out.println(" 5.  View All Resources");
            System.out.println(" 6.  Run Priority Allocation");
            System.out.println(" 7.  Check Exchange Cycle");
            System.out.println(" 8.  Rate a Resource");
            System.out.println(" 9.  Apply No-Show Penalty");
            System.out.println(" 10. View Leaderboard");
            System.out.println(" 11. Exit");
            System.out.print("\nChoice: ");

            int choice;
            try {
                choice = Integer.parseInt(sc.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Invalid input.");
                continue;
            }

            switch (choice) {

                case 1: {
                    System.out.print("Student ID   : ");
                    String sid   = sc.nextLine().trim();
                    System.out.print("Student Name : ");
                    String sname = sc.nextLine().trim();
                    engine.addStudent(new Student(sid, sname));
                    System.out.println("Student added. 2 free trial credits granted.");
                    break;
                }

                case 2: {
                    System.out.print("Resource ID   : ");
                    String rid   = sc.nextLine().trim();
                    System.out.print("Resource Name : ");
                    String rname = sc.nextLine().trim();
                    System.out.print("Type (PHYSICAL / DIGITAL) : ");
                    String typeStr = sc.nextLine().trim().toUpperCase();
                    Resource.ResourceType rtype;
                    try {
                        rtype = Resource.ResourceType.valueOf(typeStr);
                    } catch (IllegalArgumentException e) {
                        System.out.println("Invalid type.");
                        break;
                    }
                    System.out.print("Owned By (student name) : ");
                    String owner = sc.nextLine().trim();
                    Resource resource = new Resource(rid, rname, rtype, owner);
                    boolean added = engine.addResource(resource);
                    if (added) {
                        int credits = rtype == Resource.ResourceType.DIGITAL
                                      ? Student.CREDIT_DIGITAL_UPLOAD
                                      : Student.CREDIT_PHYSICAL_LEND;
                        System.out.println("Resource added. " + owner
                            + " earned +" + credits + " credits.");
                    }
                    break;
                }

                case 3: {
                    System.out.print("Student Name    : ");
                    String name = sc.nextLine().trim();
                    System.out.print("Resource Wanted : ");
                    String resName = sc.nextLine().trim();
                    System.out.print("Type (PHYSICAL / DIGITAL) : ");
                    String typeStr = sc.nextLine().trim().toUpperCase();
                    Request.ResourceType rtype;
                    try {
                        rtype = Request.ResourceType.valueOf(typeStr);
                    } catch (IllegalArgumentException e) {
                        System.out.println("Invalid type.");
                        break;
                    }
                    System.out.print("Start Time (int): ");
                    int start;
                    try { start = Integer.parseInt(sc.nextLine().trim()); }
                    catch (NumberFormatException e) { System.out.println("Invalid."); break; }

                    System.out.print("End Time   (int): ");
                    int end;
                    try { end = Integer.parseInt(sc.nextLine().trim()); }
                    catch (NumberFormatException e) { System.out.println("Invalid."); break; }

                    System.out.print("Priority (1-10) : ");
                    int pri;
                    try { pri = Integer.parseInt(sc.nextLine().trim()); }
                    catch (NumberFormatException e) { System.out.println("Invalid."); break; }

                    System.out.print("Deadline  (int) : ");
                    int deadline;
                    try { deadline = Integer.parseInt(sc.nextLine().trim()); }
                    catch (NumberFormatException e) { System.out.println("Invalid."); break; }

                    Request req = new Request(name, resName, rtype, start, end, pri, deadline);
                    boolean accepted = engine.addRequest(req);
                    if (accepted) System.out.println("Request accepted.");
                    break;
                }

                case 4: {
                    System.out.println("\n--- All Requests ---");
                    engine.displayAllRequests();
                    break;
                }

                case 5: {
                    System.out.println("\n--- Resource Pool ---");
                    engine.displayAllResources();
                    break;
                }

                case 6: {
                    System.out.println("\n--- Allocation Result ---");
                    List<Request> result = engine.runPriorityAllocation();
                    if (result.isEmpty()) {
                        System.out.println("No requests to allocate.");
                    } else {
                        for (Request r : result) {
                            System.out.println(r);
                            if (r.getStatus() == Request.Status.WAITLISTED
                                && r.getResourceType() == Request.ResourceType.PHYSICAL) {
                                int nextSlot = engine.suggestNextAvailableSlot(r.getResourceName());
                                System.out.println("   -> Next free slot for "
                                    + r.getResourceName() + ": from time " + nextSlot);
                            }
                        }
                    }
                    break;
                }

                case 7: {
                    System.out.println("\n--- Exchange Cycle Detection ---");
                    boolean cycle = engine.checkExchangeCycle();
                    if (cycle) {
                        System.out.println("Exchange cycle detected! Mutual swap possible.");
                        List<String> nodes = engine.getExchangeGraph().getDetectedCycle();
                        if (!nodes.isEmpty())
                            System.out.println("Cycle: " + String.join(" -> ", nodes));
                    } else {
                        System.out.println("No exchange cycle found.");
                    }
                    break;
                }

                case 8: {
                    System.out.print("Resource ID : ");
                    String rid = sc.nextLine().trim();
                    System.out.print("Rating (1.0 - 5.0) : ");
                    double stars;
                    try { stars = Double.parseDouble(sc.nextLine().trim()); }
                    catch (NumberFormatException e) { System.out.println("Invalid."); break; }
                    engine.rateResource(rid, stars);
                    System.out.println("Rating submitted."
                        + (stars >= 4.0 ? " Owner received +1 karma bonus!" : ""));
                    break;
                }

                case 9: {
                    System.out.print("Student Name : ");
                    String sname = sc.nextLine().trim();
                    engine.applyNoShowPenalty(sname);
                    System.out.println("No-show penalty applied (-2 credits) to " + sname);
                    break;
                }

                case 10: {
                    System.out.println("\n--- Leaderboard ---");
                    List<Student> board = engine.getLeaderboard();
                    if (board.isEmpty()) {
                        System.out.println("No students yet.");
                    } else {
                        int rank = 1;
                        for (Student s : board) {
                            System.out.printf("#%d  %s%n", rank++, s);
                        }
                    }
                    break;
                }

                case 11: {
                    System.out.println("Exiting. Goodbye.");
                    sc.close();
                    return;
                }

                default:
                    System.out.println("Invalid choice. Enter 1-11.");
            }
        }
    }
}