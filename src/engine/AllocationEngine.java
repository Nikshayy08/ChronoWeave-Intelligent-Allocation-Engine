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
 * Central controller for the Smart Campus Resource Allocation System.
 *
 * Responsibilities:
 *  1. Manage students, resources, and requests
 *  2. Enforce the karma credit access gate before accepting requests
 *  3. Coordinate with PriorityScheduler for allocation
 *  4. Coordinate with ExchangeGraph for exchange cycle detection
 *  5. Maintain leaderboard (sorted by karma credits)
 *  6. Handle resource rating and karma bonus distribution
 *
 * This class is the bridge between data models and algorithm layer.
 * UI/Main should only interact with AllocationEngine — never directly
 * with PriorityScheduler or ExchangeGraph.
 *
 * Separation of Concerns:
 *   AllocationEngine  → orchestration + business rules
 *   PriorityScheduler → scheduling algorithm
 *   ExchangeGraph     → graph + cycle detection
 */

public class AllocationEngine {

    // ─── Data Stores ──────────────────────────────────────────────────────────

    private Map<String, Student>  students     = new LinkedHashMap<>();
    private Map<String, Resource> resourcePool = new LinkedHashMap<>();
    private List<Request>         requests     = new ArrayList<>();

    // Last allocation result — cached for exchange detection and display
    private List<Request> lastAllocationResult = new ArrayList<>();

    // ─── Student Management ───────────────────────────────────────────────────

    public void addStudent(Student student) {
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
     * Adds a resource to the pool and credits the owning student.
     * This is where karma credits are earned for contributions.
     *
     * Credit rules (defined in Student.java constants):
     *   DIGITAL  → +2 credits
     *   PHYSICAL → +3 credits
     */
    public boolean addResource(Resource resource) {

        // Duplicate digital resource detection via file hash
        if (resource.getType() == Resource.ResourceType.DIGITAL
            && resource.getFileHash() != null) {

            for (Resource existing : resourcePool.values()) {
                if (resource.getFileHash().equals(existing.getFileHash())) {
                    System.out.println("Duplicate resource detected. Upload rejected.");
                    return false;
                }
            }
        }

        resourcePool.put(resource.getResourceId(), resource);

        // Credit the contributing student
        Student owner = students.get(resource.getOwnedBy());
        if (owner != null) {
            if (resource.getType() == Resource.ResourceType.DIGITAL) {
                owner.contributeDigital();
            } else {
                owner.contributePhysical();
            }
        }

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
     * Access gate enforced here:
     *   Student must have karmaCredits > 0 to submit a request.
     *   Students with zero credits can browse but cannot request.
     *
     * Returns:
     *   true  → request accepted
     *   false → rejected (no credits or student not found)
     */
    public boolean addRequest(Request request) {

        Student student = students.get(request.getStudentName());

        if (student == null) {
            System.out.println("Student not found: " + request.getStudentName());
            return false;
        }

        // ── ACCESS GATE ──────────────────────────────────────────────────────
        if (!student.canRequest()) {
            System.out.println(request.getStudentName()
                + " has 0 karma credits. Contribute a resource first to unlock requests.");
            return false;
        }

        requests.add(request);
        student.incrementRequests();
        return true;
    }

    public List<Request> getAllRequests() {
        return Collections.unmodifiableList(requests);
    }

    // ─── Core Allocation ──────────────────────────────────────────────────────

    /*
     * runPriorityAllocation()
     *
     * Builds karma map → passes to PriorityScheduler → charges credits
     * for all successfully allocated requests.
     *
     * Returns full result list (ALLOCATED + WAITLISTED).
     */
    public List<Request> runPriorityAllocation() {

        // Build karma map for scheduler's composite score computation
        Map<String, Integer> karmaMap = new HashMap<>();
        for (Student s : students.values()) {
            karmaMap.put(s.getName(), s.getKarmaCredits());
        }

        lastAllocationResult = PriorityScheduler.allocate(
            new ArrayList<>(requests), karmaMap
        );

        // Charge -1 credit for each successfully allocated request
        for (Request r : lastAllocationResult) {
            if (r.getStatus() == Request.Status.ALLOCATED) {
                Student s = students.get(r.getStudentName());
                if (s != null) s.chargeForRequest();
            }
        }

        return lastAllocationResult;
    }

    /*
     * suggestNextAvailableSlot(resourceName)
     *
     * When a physical request is rejected, suggest the next free slot.
     * Delegates to PriorityScheduler.suggestNextSlot().
     */
    public int suggestNextAvailableSlot(String resourceName) {
        return PriorityScheduler.suggestNextSlot(resourceName, lastAllocationResult);
    }

    // ─── Exchange Cycle Detection ─────────────────────────────────────────────

    /*
     * checkExchangeCycle()
     *
     * Builds exchange graph from actual want/have relationships:
     *   Edge (A → B) = Student A wants a resource owned by Student B
     *
     * A cycle means mutual swap is possible — no extra resources needed.
     *
     * Returns true if any exchange cycle exists.
     */
    public boolean checkExchangeCycle() {

        ExchangeGraph graph = new ExchangeGraph();

        for (Request requester : requests) {
            String wanter        = requester.getStudentName();
            String resourceWanted = requester.getResourceName();

            // Find who owns the wanted resource
            for (Resource res : resourcePool.values()) {
                if (res.getResourceName().equals(resourceWanted)) {
                    String owner = res.getOwnedBy();

                    // No self-loops — student can't exchange with themselves
                    if (!owner.equals(wanter)) {
                        graph.addEdge(wanter, owner);
                    }
                }
            }
        }

        return graph.hasCycle();
    }

    /*
     * getExchangeGraph()
     *
     * Returns the fully built ExchangeGraph for display or further analysis.
     * Useful for JavaFX graph visualization.
     */
    public ExchangeGraph getExchangeGraph() {

        ExchangeGraph graph = new ExchangeGraph();

        for (Request requester : requests) {
            String wanter         = requester.getStudentName();
            String resourceWanted = requester.getResourceName();

            for (Resource res : resourcePool.values()) {
                if (res.getResourceName().equals(resourceWanted)) {
                    String owner = res.getOwnedBy();
                    if (!owner.equals(wanter)) {
                        graph.addEdge(wanter, owner);
                    }
                }
            }
        }

        return graph;
    }

    // ─── Rating System ────────────────────────────────────────────────────────

    /*
     * rateResource(resourceId, stars)
     *
     * Student rates a resource after use.
     * If rating >= 4.0 → owner receives +1 karma bonus.
     *
     * This rewards quality contributions, not just quantity.
     */
    public void rateResource(String resourceId, double stars) {

        Resource resource = resourcePool.get(resourceId);
        if (resource == null) return;

        resource.addRating(stars);

        // Bonus karma for high-quality resource
        if (stars >= 4.0) {
            Student owner = students.get(resource.getOwnedBy());
            if (owner != null) owner.receiveRatingBonus();
        }
    }

    // ─── No-Show Penalty ──────────────────────────────────────────────────────

    /*
     * applyNoShowPenalty(studentName)
     *
     * Called when a student confirms allocation but doesn't use the resource.
     * Deducts 2 credits — heavier than the normal -1 request cost.
     */
    public void applyNoShowPenalty(String studentName) {
        Student student = students.get(studentName);
        if (student != null) student.applyNoShowPenalty();
    }

    // ─── Leaderboard ──────────────────────────────────────────────────────────

    /*
     * getLeaderboard()
     *
     * Returns students sorted by karma credits (descending).
     * Credits = real currency in this system, so leaderboard = actual influence.
     */
    public List<Student> getLeaderboard() {
        List<Student> sorted = new ArrayList<>(students.values());
        sorted.sort((a, b) -> b.getKarmaCredits() - a.getKarmaCredits());
        return sorted;
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
            System.out.println("No resources in pool.");
            return;
        }
        resourcePool.values().forEach(System.out::println);
    }
}
