package UI;

import Controller.TrolleyController;
import Domain.PickingItems;
import Domain.PickingPlan;
import Domain.Trolley;
import Domain.ENUM.PackingHeuristic;

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
            System.out.print("Escolha uma opção: ");
            int opcao = getIntInput();

            switch (opcao) {
                case 1:
                    createTrolley();
                    break;
                case 2:
                    startPacking();
                    break;
                case 3:
                    listTrolleys();
                    break;
                case 4:
                    createPickingPlan();
                    break;
                case 0:
                    System.out.println("Encerrando aplicação. Até logo!");
                    running = false;
                    break;
                default:
                    System.out.println("Opção inválida! Tente novamente.");
            }
        }
    }

    private void showMenu() {
        System.out.println("\n====== MENU TROLLEY (USEI03) ======");
        System.out.println("1. Criar novo trolley");
        System.out.println("2. Executar packing (heurísticas FF, FFD ou BFD)");
        System.out.println("3. Listar trolleys existentes");
        System.out.println("4. USEI03 - Create Picking Plan (NEW)");
        System.out.println("0. Sair");
    }

    private void createTrolley() {
        System.out.print("Digite a capacidade máxima do carrinho: ");
        int capacidade = getIntInput();

        System.out.print("Digite a opção de heurística a usar (FF, FFD, BFD): ");
        String opcao = sc.next().toUpperCase();

        if (!opcao.equals("FF") && !opcao.equals("FFD") && !opcao.equals("BFD")) {
            System.out.println("Heurística inválida! Use FF, FFD ou BFD.");
            return;
        }

        Trolley t = trolleyController.createTrolley(capacidade, opcao);
        System.out.println("Carrinho criado com sucesso!");
        System.out.println(t);
    }

    private void startPacking() {
        if (trolleyController.getTrolley() == null) {
            System.out.println("Nenhum trolley criado. Crie um primeiro.");
            return;
        }

        try {
            System.out.println("Executando packing...");
            trolleyController.packing();
            System.out.println("Packing concluído com sucesso!");
        } catch (Exception e) {
            System.out.println("Erro ao executar packing: " + e.getMessage());
        }
    }

    private void listTrolleys() {
        List<Trolley> lista = trolleyController.getTrolleyList();
        if (lista.isEmpty()) {
            System.out.println("Nenhum trolley criado ainda.");
            return;
        }

        System.out.println("\n--- Lista de Trolleys ---");
        for (Trolley t : lista) {
            System.out.println(t);
        }
    }

    private void createPickingPlan() {
        System.out.println("\n=== USEI03 - PICKING PLAN CREATION ===");

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