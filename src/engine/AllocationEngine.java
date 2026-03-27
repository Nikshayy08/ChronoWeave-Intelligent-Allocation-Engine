package engine;

import java.util.*;
import models.Request;
import models.Resource;
import models.Student;
import algorithms.PriorityScheduler;
import algorithms.ExchangeGraph;

/*
 * AllocationEngine.java
 *
 * Central controller for Smart Campus Resource Exchange System.
 *
 * Responsibilities:
 *  1. Manage students, resources, requests
 *  2. Match RENT requests using PriorityScheduler (conflict-free)
 *  3. Match BUY requests using ExchangeGraph (cycle detection)
 *  4. Reveal contact details only when a match is made
 *  5. Handle digital resource listings (free access)
 *
 * No karma credits. No payments.
 * System only connects people — deals made in person.
 *
 * Separation of Concerns:
 *   AllocationEngine  → orchestration + matching logic
 *   PriorityScheduler → rent conflict detection + scheduling
 *   ExchangeGraph     → buy/sell cycle detection
 */

public class AllocationEngine {

    // ─── Data Stores ──────────────────────────────────────────────────────────

    private Map<String, Student>  students     = new LinkedHashMap<>();   //Stores all students
    private Map<String, Resource> resourcePool = new LinkedHashMap<>();   //All listings (SELL / RENT / DIGITAL)
    private List<Request>         requests     = new ArrayList<>();      // All buy + rent requests
    private List<Request>         lastAllocationResult = new ArrayList<>();

    // ─── Student Management ───────────────────────────────────────────────────

    public void addStudent(Student student) {    // Stores student in system
        students.put(student.getName(), student);
    }

    public Student getStudent(String name) {
        return students.get(name);
    }

    public Map<String, Student> getAllStudents() {
        return Collections.unmodifiableMap(students);
    }

    // ─── Resource Management ──────────────────────────────────────────────────

    /*
     * addResource(resource)
     *
     * Adds a resource listing to the pool.
     * Owner's listing count is incremented.
     *
     * SELL    → listed for permanent sale
     * RENT    → listed for short term use
     * DIGITAL → Google Drive link listed for free access
     */
    public boolean addResource(Resource resource) {      // Links resource ownership with student data
        resourcePool.put(resource.getResourceId(), resource);

        Student owner = students.get(resource.getOwnedBy());
        if (owner != null) owner.incrementListings();

        return true;
    }

    public Resource getResource(String resourceId) {
        return resourcePool.get(resourceId);
    }

    public Map<String, Resource> getAllResources() {
        return Collections.unmodifiableMap(resourcePool);
    }

    // ─── Request Management ───────────────────────────────────────────────────

    /*
     * addRequest(request)
     *
     * Accepts any request — no credit gate.
     * BUY requests → matched via exchange graph
     * RENT requests → matched via priority scheduler
     */
    public boolean addRequest(Request request) {

        Student student = students.get(request.getStudentName());

        if (student == null) {
            System.out.println("Student not found: " + request.getStudentName());
            return false;
        }

        requests.add(request);
        student.incrementRequests();
        return true;
    }

    public List<Request> getAllRequests() {
        return Collections.unmodifiableList(requests);
    }

    // ─── Rent Allocation (Priority Scheduler) ────────────────────────────────

    /*
     * runRentAllocation()
     *
     * Processes all RENT requests through PriorityScheduler.
     * Conflict-free scheduling — no two students get same
     * physical resource at overlapping times.
     *
     * On match → reveals owner contact to requester.
     */
    public List<Request> runRentAllocation() {

        // Filter only RENT requests
        List<Request> rentRequests = new ArrayList<>();
        for (Request r : requests) {
            if (r.getRequestType() == Request.RequestType.NEED_TO_RENT) {
                rentRequests.add(r);
            }
        }

        lastAllocationResult = PriorityScheduler.allocate(rentRequests);

        // Reveal contact for matched requests
        for (Request r : lastAllocationResult) {
            if (r.getStatus() == Request.Status.MATCHED) {
                revealContact(r);
            }
        }

        return lastAllocationResult;
    }

    /*
     * revealContact(request)
     *
     * Finds the owner of the requested resource
     * and prints their contact details to the requester.
     *
     * In JavaFX this will show a popup instead of console print.
     */
    private void revealContact(Request request) {
        for (Resource res : resourcePool.values()) {
            if (res.getResourceName().equals(request.getResourceName())) {
                Student owner = students.get(res.getOwnedBy());
                if (owner != null) {
                    System.out.println("\n  MATCH FOUND for " + request.getStudentName());
                    System.out.println("  Contact owner: " + owner.getName());
                    System.out.println("  Phone    : " + owner.getContactNumber());
                    System.out.println("  WhatsApp : " + owner.getWhatsappNumber());
                    System.out.println("  Meet in person to complete the deal.");
                }
                break;
            }
        }
    }

    /*
     * suggestNextAvailableSlot(resourceName)
     *
     * When a RENT request is rejected due to conflict,
     * suggest the next free time slot for that resource.
     */
    public int suggestNextAvailableSlot(String resourceName) {
        return PriorityScheduler.suggestNextSlot(resourceName, lastAllocationResult);
    }

    // ─── Buy Matching (Exchange Graph) ───────────────────────────────────────

    /*
     * checkExchangeCycle()
     *
     * Detects mutual swap possibility for BUY requests.
     *
     * Edge (A -> B) = Student A wants resource owned by Student B
     * Cycle = both can swap directly, no extra cost
     *
     * On cycle detected -> reveals both parties' contacts to each other.
     */
    public boolean checkExchangeCycle() {

        ExchangeGraph graph = buildExchangeGraph();
        boolean cycleFound  = graph.hasCycle();

        if (cycleFound) {
            List<String> cycle = graph.getDetectedCycle();
            System.out.println("\n  EXCHANGE CYCLE DETECTED: " + String.join(" -> ", cycle));
            System.out.println("  These students can swap directly. Revealing contacts:\n");

            for (String studentName : cycle) {
                Student s = students.get(studentName);
                if (s != null) {
                    System.out.println("  " + s.getName()
                        + " | Phone: " + s.getContactNumber()
                        + " | WhatsApp: " + s.getWhatsappNumber());
                }
            }
        }

        return cycleFound;
    }

    private ExchangeGraph buildExchangeGraph() {

        ExchangeGraph graph = new ExchangeGraph();

        for (Request requester : requests) {
            if (requester.getRequestType() != Request.RequestType.NEED_TO_BUY) continue;

            String wanter        = requester.getStudentName();
            String resourceWanted = requester.getResourceName();

            for (Resource res : resourcePool.values()) {
                if (res.getResourceName().equals(resourceWanted)
                    && res.getListingType() == Resource.ListingType.SELL) {

                    String owner = res.getOwnedBy();
                    if (!owner.equals(wanter)) {
                        graph.addEdge(wanter, owner);
                    }
                }
            }
        }

        return graph;
    }

    public ExchangeGraph getExchangeGraph() {
        return buildExchangeGraph();
    }

    // ─── Digital Resources ────────────────────────────────────────────────────

    /*
     * getDigitalResources()
     *
     * Returns all digital listings with their download links.
     * No request needed — freely accessible.
     */
    public List<Resource> getDigitalResources() {
        List<Resource> digital = new ArrayList<>();
        for (Resource r : resourcePool.values()) {
            if (r.getListingType() == Resource.ListingType.DIGITAL) {
                digital.add(r);
            }
        }
        return digital;
    }

    // ─── Display ──────────────────────────────────────────────────────────────

    public void displayAllRequests() {
        if (requests.isEmpty()) {
            System.out.println("No requests in system.");
            return;
        }
        requests.forEach(System.out::println);
    }

    public void displayAllResources() {
        if (resourcePool.isEmpty()) {
            System.out.println("No resources listed.");
            return;
        }
        resourcePool.values().forEach(System.out::println);
    }

    public void displayDigitalResources() {
        List<Resource> digital = getDigitalResources();
        if (digital.isEmpty()) {
            System.out.println("No digital resources available.");
            return;
        }
        digital.forEach(System.out::println);
    }
}