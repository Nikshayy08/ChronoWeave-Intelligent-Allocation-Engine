import java.util.Scanner;
import java.util.List;

import models.Request;
import engine.AllocationEngine;

/*
 * Main Class
 *
 * Entry point of Smart Campus Resource Allocation System.
 *
 * Responsibilities:
 * 1. Provide menu-driven console interface
 * 2. Take user input
 * 3. Call AllocationEngine methods
 * 4. Display formatted results
 *
 * Note:
 * All algorithm logic is handled inside AllocationEngine.
 * Main only manages interaction (Separation of Concerns).
 */

public class Main {

    public static void main(String[] args) {

        // Scanner object for user input
        Scanner sc = new Scanner(System.in);

        // Core system engine (handles all allocation logic)
        AllocationEngine engine = new AllocationEngine();

        // Infinite loop for menu-driven system
        while (true) {

            // Display Menu
            System.out.println("\n=======================================");
            System.out.println(" SMART CAMPUS RESOURCE ALLOCATION ");
            System.out.println("=======================================");
            System.out.println("1. Add Request");
            System.out.println("2. View All Requests");
            System.out.println("3. Run Priority Allocation");
            System.out.println("4. Check Exchange Possibility");
            System.out.println("5. Exit");
            System.out.print("Enter your choice: ");

            int choice = sc.nextInt();
            sc.nextLine(); // consume leftover newline

            switch (choice) {

                case 1:
                    // Add a new resource request

                    System.out.print("Enter Student Name: ");
                    String name = sc.nextLine();

                    System.out.print("Enter Start Time: ");
                    int start = sc.nextInt();

                    System.out.print("Enter End Time: ");
                    int end = sc.nextInt();

                    System.out.print("Enter Priority (1-10): ");
                    int priority = sc.nextInt();

                    // Create new Request object
                    Request newRequest = new Request(name, start, end, priority);

                    // Add request to engine
                    engine.addRequest(newRequest);

                    System.out.println("Request added successfully!");
                    break;

                case 2:
                    // Display all stored requests

                    System.out.println("\n--- All Requests ---");
                    engine.displayAllRequests();
                    break;

                case 3:
                    // Run Priority-Based Allocation

                    System.out.println("\n--- Allocated Requests (Priority-Based) ---");

                    List<Request> allocated = engine.runPriorityAllocation();

                    if (allocated.isEmpty()) {
                        System.out.println("No requests allocated.");
                    } else {
                        for (Request r : allocated) {
                            System.out.println(r);
                        }
                    }
                    break;

                case 4:
                    // Check for exchange cycle using graph algorithm

                    System.out.println("\n--- Exchange Cycle Detection ---");

                    boolean cycleExists = engine.checkExchangeCycle();

                    if (cycleExists) {
                        System.out.println("Exchange cycle detected! Swap possible.");
                    } else {
                        System.out.println("No exchange cycle found.");
                    }
                    break;

                case 5:
                    // Exit program

                    System.out.println("Exiting system...");
                    sc.close();
                    return;

                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }
}
