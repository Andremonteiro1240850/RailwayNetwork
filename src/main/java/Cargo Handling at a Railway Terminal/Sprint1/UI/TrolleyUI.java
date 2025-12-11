package Sprint1.UI;

import Sprint1.Controller.TrolleyController;
import Sprint1.Domain.PickingItems;
import Sprint1.Domain.PickingPlan;
import Sprint1.Domain.Trolley;
import Sprint1.Domain.ENUM.PackingHeuristic;

import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;

/**
 * The type Trolley ui.
 */
public class TrolleyUI implements Runnable {
    private final TrolleyController trolleyController;
    private final Scanner sc = new Scanner(System.in);

    /**
     * Instantiates a new Trolley ui.
     *
     * @param trolleyController the trolley controller
     */
    public TrolleyUI(TrolleyController trolleyController) {
        this.trolleyController = trolleyController;
    }

    @Override
    public void run() {
        boolean running = true;

        while (running) {
            showMenu();
            System.out.print("Choose an option: ");
            int opcao = getIntInput();

            switch (opcao) {
                case 1:
                    listTrolleys();
                    break;
                case 2:
                    createPickingPlan();
                    break;
                case 0:
                    System.out.println("App closed. Goodbye!");
                    running = false;
                    break;
                default:
                    System.out.println("Invalid option! Try again.");
            }
        }
    }

    private void showMenu() {
        System.out.println("\n====== MENU TROLLEY ======");
        System.out.println("1. List existing trolleys");
        System.out.println("2. Create Picking Plan ");
        System.out.println("0. Exit");
    }
    private void listTrolleys() {
        List<Trolley> lista = trolleyController.getTrolleyList();
        if (lista.isEmpty()) {
            System.out.println("No trolley created.");
            return;
        }

        System.out.println("\n--- Trolleys List ---");
        for (Trolley t : lista) {
            System.out.println(t);
        }
    }

    private void createPickingPlan() {
        System.out.println("\n=== PICKING PLAN CREATION ===");

        // Get trolley capacity
        System.out.print("Enter trolley capacity (kg): ");
        int capacity = getIntInput();

        // Get heuristic choice
        System.out.println("\nSelect packing heuristic:");
        System.out.println("1. First Fit (FF)");
        System.out.println("2. First Fit Decreasing (FFD)");
        System.out.println("3. Best Fit Decreasing (BFD)");
        System.out.print("Enter choice (1-3): ");
        int heuristicChoice = getIntInput();

        PackingHeuristic heuristic;
        switch (heuristicChoice) {
            case 1:
                heuristic = PackingHeuristic.FF;
                break;
            case 2:
                heuristic = PackingHeuristic.FFD;
                break;
            case 3:
                heuristic = PackingHeuristic.BFD;
                break;
            default:
                System.out.println("Invalid choice, defaulting to First Fit");
                heuristic = PackingHeuristic.FF;
                break;
        }

        // Get splitting preference
        System.out.print("Allow splitting items that don't fit? (y/n): ");
        boolean allowSplitting = sc.next().toLowerCase().startsWith("y");

        try {
            // Create picking plan
            PickingPlan plan = trolleyController.createPickingPlan(capacity, heuristic, allowSplitting);

            // Display results
            displayPickingPlan(plan);

        } catch (Exception e) {
            System.out.println("Error creating picking plan: " + e.getMessage());
        }
    }

    private void displayPickingPlan(PickingPlan plan) {
        System.out.println("\n" + generateLine(60));
        System.out.println("PICKING PLAN RESULTS");
        System.out.println(generateLine(60));
        System.out.println("Heuristic: " + plan.getHeuristic());
        System.out.println("Total Trolleys: " + plan.getTotalTrolleys());
        System.out.printf("Average Utilization: %.2f%%\n", plan.getAverageUtilization());
        System.out.println(generateLine(60));

        List<Trolley> trolleys = plan.getTrolleys();
        for (int i = 0; i < trolleys.size(); i++) {
            Trolley trolley = trolleys.get(i);
            displayTrolleyDetails(trolley, i + 1);
        }
    }

    private void displayTrolleyDetails(Trolley trolley, int trolleyNumber) {
        double utilization = (trolley.getCurrentLoad() / trolley.getMaxCapacity()) * 100;

        System.out.println("\n--- TROLLEY " + trolleyNumber + " ---");
        System.out.printf("Capacity: %d kg | Used: %.2f kg | Utilization: %.1f%%\n",
                trolley.getMaxCapacity(), trolley.getCurrentLoad(), utilization);

        List<PickingItems> items = trolley.getPickingItems();
        if (items.isEmpty()) {
            System.out.println("No items assigned to this trolley.");
        } else {
            System.out.println("Picking Plan:");
            System.out.println("Order ID | Line | Aisle | Bay | Box ID | SKU | Qty");
            System.out.println(generateLine(50));

            for (PickingItems item : items) {
                System.out.printf("%-8s | %-4d | %-5d | %-3d | %-6s | %-3s | %-3d\n",
                        item.getOrderId(),
                        item.getLineNo(),
                        item.getAisleId(),
                        item.getBayId(),
                        item.getBoxId(),
                        item.getSku(),
                        item.getQty()
                );
            }
        }
    }


    private String generateLine(int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append("=");
        }
        return sb.toString();
    }


    private String generateLine(int length, char character) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(character);
        }
        return sb.toString();
    }

    private int getIntInput() {
        while (true) {
            try {
                return sc.nextInt();
            } catch (InputMismatchException e) {
                System.out.print("Entrada inválida! Digite um número inteiro: ");
                sc.next(); // limpar buffer
            }
        }
    }

    private double getDoubleInput() {
        while (true) {
            try {
                return sc.nextDouble();
            } catch (InputMismatchException e) {
                System.out.print("Entrada inválida! Digite um número: ");
                sc.next(); // limpar buffer
            }
        }
    }
}