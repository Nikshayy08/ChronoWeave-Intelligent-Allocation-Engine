import java.util.*;

/*
 * Main Class
 *
 * This class acts as a simulation layer.
 * It creates sample requests and calls the PriorityScheduler.
 *
 * It does NOT contain algorithm logic.
 * It only:
 * 1. Prepares data
 * 2. Calls allocation engine
 * 3. Displays results
 */

public class Main {

    public static void main(String[] args) {

        // Create a list to store all student requests
        List<Request> requests = new ArrayList<>();

        /*
         * Adding sample test data
         *
         * Format:
         * new Request(studentName, startTime, endTime, priority)
         *
         * Higher priority value means higher urgency.
         */

        requests.add(new Request("Aman", 1, 3, 1));   // Normal
        requests.add(new Request("Riya", 2, 5, 5));   // Urgent
        requests.add(new Request("Karan", 4, 7, 2));  // Medium
        requests.add(new Request("Sneha", 6, 9, 8));  // Very urgent
        requests.add(new Request("Arjun", 8, 10, 1)); // Normal

        // Display all input requests
        System.out.println("All Requests:");
        for (Request r : requests) {
            System.out.println(r);
        }

        /*
         * Call the Priority-based Allocation Engine
         *
         * This method:
         * - Uses PriorityQueue (Max Heap)
         * - Handles urgency first
         * - Avoids overlapping intervals
         */
        List<Request> allocated = PriorityScheduler.allocate(requests);  // GreedyScheduler ko wo isi file m accordingly call krdega

        // Display final allocated requests
        System.out.println("\nAllocated Requests (Priority-Based Allocation):");
        for (Request r : allocated) {
            System.out.println(r);
        }

        /*
         * Program End
         *
         * This demonstrates:
         * - Heap usage
         * - Multi-criteria scheduling
         * - Conflict detection
         */
    }
}
