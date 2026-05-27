package engine;

import java.util.*;
import models.Request;
import models.Resource;
import models.Student;
import algorithms.PriorityScheduler;
import algorithms.ExchangeGraph;
import database.DatabaseManager;

/*
 * AllocationEngine.java
 *
 * Central controller for  Campus Resource Exchange System.
 *
 * Now integrated with SQLite via DatabaseManager.
 * All add operations are persisted to DB automatically.
 * Data is loaded from DB on startup.
 */

public class AllocationEngine {

    private Map<String, Student> students = new LinkedHashMap<>();
    private Map<String, Resource> resourcePool = new LinkedHashMap<>();
    private List<Request> requests = new ArrayList<>();
    private List<Request> lastAllocationResult = new ArrayList<>();

    private DatabaseManager db;

    // ── Constructor ───────────────────────────────────────────────────────────

    public AllocationEngine() {
        this.db = null; // DB disabled by default, set via setDatabase()
    }

    public AllocationEngine(DatabaseManager db) {
        this.db = db;
    }

    public void setDatabase(DatabaseManager db) {
        this.db = db;
    }

    /*
     * loadFromDatabase()
     * Called on startup — loads all persisted data into memory.
     */
    public void loadFromDatabase() {
        if (db == null || !db.isConnected())
            return;

        System.out.println("Loading data from database...");

        for (Student s : db.loadAllStudents()) {
            students.put(s.getName(), s);
        }

        for (Resource r : db.loadAllResources()) {
            resourcePool.put(r.getResourceId(), r);
        }

        for (Request r : db.loadAllRequests()) {
            requests.add(r);
        }

        System.out.println("Loaded: " + students.size() + " students, "
                + resourcePool.size() + " resources, "
                + requests.size() + " requests.");
    }

    // ── Student Management ────────────────────────────────────────────────────

    public void addStudent(Student student) {
        students.put(student.getName(), student);
        if (db != null && db.isConnected()) {
            db.saveStudent(student);
        }
    }

    public Student getStudent(String name) {
        return students.get(name);
    }

    public Map<String, Student> getAllStudents() {
        return Collections.unmodifiableMap(students);
    }

    // ── Resource Management ───────────────────────────────────────────────────

    public void addResource(Resource resource) {
        resourcePool.put(resource.getResourceId(), resource);
        Student owner = students.get(resource.getOwnedBy());
        if (owner != null)
            owner.incrementListings();
        if (db != null && db.isConnected()) {
            db.saveResource(resource);
        }
    }

    public Resource getResource(String resourceId) {
        return resourcePool.get(resourceId);
    }

    public Map<String, Resource> getAllResources() {
        return Collections.unmodifiableMap(resourcePool);
    }

    // ── Request Management ────────────────────────────────────────────────────

    public boolean addRequest(Request request) {
        Student student = students.get(request.getStudentName());
        if (student == null) {
            System.out.println("Student not found: " + request.getStudentName());
            return false;
        }
        requests.add(request);
        student.incrementRequests();
        if (db != null && db.isConnected()) {
            db.saveRequest(request);
        }
        return true;
    }

    public List<Request> getAllRequests() {
        return Collections.unmodifiableList(requests);
    }

    // ── Rent Allocation ───────────────────────────────────────────────────────

    public List<Request> runRentAllocation() {
        List<Request> rentRequests = new ArrayList<>();
        for (Request r : requests) {
            if (r.getRequestType() == Request.RequestType.NEED_TO_RENT) {
                rentRequests.add(r);
            }
        }
        lastAllocationResult = PriorityScheduler.allocate(rentRequests);

        for (Request r : lastAllocationResult) {
            if (r.getStatus() == Request.Status.MATCHED) {
                if (db != null && db.isConnected()) {
                    db.updateRequestStatus(r.getStudentName(), r.getResourceName(), Request.Status.MATCHED);
                }
            }
        }
        return lastAllocationResult;
    }

    public int suggestNextAvailableSlot(String resourceName) {
        return PriorityScheduler.suggestNextSlot(resourceName, lastAllocationResult);
    }

    // ── Buy Matching ──────────────────────────────────────────────────────────

    public List<String> runBuyMatching() {
        List<String> results = new ArrayList<>();

        for (Request request : requests) {
            if (request.getRequestType() != Request.RequestType.NEED_TO_BUY)
                continue;
           if (request.getStatus() == Request.Status.MATCHED)
    continue;

            String wantedResource = request.getResourceName();
            String buyerName = request.getStudentName();
            double buyerBudget = request.getOfferingPrice();

            boolean foundListing = false;

            for (Resource res : resourcePool.values()) {
                if (!res.getResourceName().equalsIgnoreCase(wantedResource))
                    continue;
                if (res.getListingType() != Resource.ListingType.SELL)
                    continue;
                if (!res.isAvailable())
                    continue;

                foundListing = true;
                String sellerName = res.getOwnedBy();
                double sellerPrice = res.getAskingPrice();

                if (buyerBudget >= sellerPrice) {
                    request.setStatus(Request.Status.MATCHED);
                    res.setAvailable(false);

                    if (db != null && db.isConnected()) {
                        db.updateRequestStatus(buyerName, wantedResource, Request.Status.MATCHED);
                        db.updateResourceAvailability(res.getResourceId(), false);
                    }

                    Student buyer = students.get(buyerName);
                    Student seller = students.get(sellerName);

                    StringBuilder sb = new StringBuilder();
                    sb.append("====== BUY MATCH FOUND ======\n");
                    sb.append("Resource  : ").append(res.getResourceName()).append("\n");
                    sb.append("Buyer     : ").append(buyerName)
                            .append("  |  Budget: Rs.").append((int) buyerBudget).append("\n");
                    sb.append("Seller    : ").append(sellerName)
                            .append("  |  Asking: Rs.").append((int) sellerPrice).append("\n\n");
                    if (buyer != null) {
                        sb.append("Buyer Contact:\n");
                        sb.append("  Phone    : ").append(buyer.getContactNumber()).append("\n");
                        sb.append("  WhatsApp : ").append(buyer.getWhatsappNumber()).append("\n\n");
                    }
                    if (seller != null) {
                        sb.append("Seller Contact:\n");
                        sb.append("  Phone    : ").append(seller.getContactNumber()).append("\n");
                        sb.append("  WhatsApp : ").append(seller.getWhatsappNumber()).append("\n\n");
                    }
                    sb.append("Action: Meet in person to complete the deal.\n");
                    sb.append("=============================\n");
                    results.add(sb.toString());
                    break;

                } else {
                    results.add("BUDGET TOO LOW: " + buyerName + " wants " + wantedResource
                            + " | Budget Rs." + (int) buyerBudget
                            + " < Asking Rs." + (int) sellerPrice + "\n");
                }
            }

            // if (!foundListing && request.getStatus() == Request.Status.PENDING) {
            // results.add("NO LISTING FOUND: No one has listed '"
            // + wantedResource + "' for sale yet.\n");
            // }

            if (!foundListing && request.getStatus() == Request.Status.PENDING) {

                // request.setStatus(Request.Status.REJECTED);

                results.add("NO LISTING FOUND: No one has listed '"
                        + wantedResource + "' for sale yet.\n");
            }
        }

        if (results.isEmpty())
            results.add("No BUY requests to process.");
        return results;
    }

    // ── Exchange Cycle Detection ──────────────────────────────────────────────

    public boolean checkExchangeCycle() {
        return buildExchangeGraph().hasCycle();
    }

    private ExchangeGraph buildExchangeGraph() {
        ExchangeGraph graph = new ExchangeGraph();
        for (Request requester : requests) {
            if (requester.getRequestType() != Request.RequestType.NEED_TO_BUY)
                continue;
            String wanter = requester.getStudentName();
            String resourceWanted = requester.getResourceName();
            for (Resource res : resourcePool.values()) {
                if (res.getResourceName().equalsIgnoreCase(resourceWanted)
                        && res.getListingType() == Resource.ListingType.SELL) {
                    String owner = res.getOwnedBy();
                    if (!owner.equals(wanter))
                        graph.addEdge(wanter, owner);
                }
            }
        }
        return graph;
    }

    public ExchangeGraph getExchangeGraph() {
        return buildExchangeGraph();
    }

    // ── Digital Resources ─────────────────────────────────────────────────────

    public List<Resource> getDigitalResources() {
        List<Resource> digital = new ArrayList<>();
        for (Resource r : resourcePool.values()) {
            if (r.getListingType() == Resource.ListingType.DIGITAL)
                digital.add(r);
        }
        return digital;
    }

    // ── Display ───────────────────────────────────────────────────────────────

    public void displayAllRequests() {
        if (requests.isEmpty()) {
            System.out.println("No requests.");
            return;
        }
        requests.forEach(System.out::println);
    }

    public void displayAllResources() {
        if (resourcePool.isEmpty()) {
            System.out.println("No resources.");
            return;
        }
        resourcePool.values().forEach(System.out::println);
    }

    public void displayAllStudents() {
        if (students.isEmpty()) {
            System.out.println("No students.");
            return;
        }
        students.values().forEach(System.out::println);
    }

    public void displayDigitalResources() {
        List<Resource> digital = getDigitalResources();
        if (digital.isEmpty()) {
            System.out.println("No digital resources available.");
            return;
        }
        for (Resource r : digital) {
            System.out.println(r.getResourceName()
                    + " | By: " + r.getOwnedBy()
                    + " | Link: " + r.getDownloadLink());
        }
    }
}