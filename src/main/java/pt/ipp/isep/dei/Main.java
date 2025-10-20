package pt.ipp.isep.dei;

import Controller.*;
import Domain.Warehouse;
import Repository.WarehouseRepository;
import UI.ReturnsUI;
import UI.WagonsUnloadingUI;
import Domain.DataLoader;
import Domain.ENUM.ReturnReason;
import Domain.PickPath;
import Domain.PickStop;
import Domain.ENUM.PathStrategy;
import UI.orderEligibilityAllocationUI;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;
import java.util.Scanner;

public class Main {
    
    private static Scanner scanner = new Scanner(System.in);
    private static DataLoader dataLoader;
    private static boolean dataLoaded = false;
    
    /**
     * Load all data from CSV files once at startup
     */
    private static void loadAllData() {
        System.out.println("Loading data from CSV files...");
        dataLoader = new DataLoader();
        
        try {
            // Load all required data
            dataLoader.loadItems("train_station_dataset/items.csv");
            dataLoader.loadBays("train_station_dataset/bays.csv");
            dataLoader.loadWagons("train_station_dataset/wagons.csv");
            dataLoader.loadOrders("train_station_dataset/orders.csv", "train_station_dataset/order_lines.csv");
            dataLoader.loadReturns("train_station_dataset/returns.csv");
            
            dataLoaded = true;
            System.out.println("✓ All data loaded successfully!");
        } catch (Exception e) {
            System.out.println("Error loading data: " + e.getMessage());
            dataLoaded = false;
        }
    }
    
    public static void main(String[] args) {
        // Load data at startup
        loadAllData();
        
        while (true) {
            System.out.println("\n=== WAREHOUSE MANAGEMENT SYSTEM ===");
            System.out.println("1. Wagons Unloading");
            System.out.println("2. Order Eligibility & Allocation");
            System.out.println("3. Picking Plans");
            System.out.println("4. Pick Path Sequencing");
            System.out.println("5. Returns & Quarantine");
            System.out.println("6. Exit");
            
            System.out.print("Select option: ");
            String choice = scanner.nextLine().trim();
            
            switch (choice) {
                case "1":
                    wagonsUnloading();
                    break;
                case "2":
                    orderEligibilityAllocation();
                    break;
                case "3":
                    pickingPlans();
                    break;
                case "4":
                    pickPathSequencing();
                    break;
                case "5":
                    returnsAndQuarantine();
                    break;
                case "6":
                    System.out.println("Goodbye!");
                    return;
                default:
                    System.out.println("Invalid option!");
            }
        }
    }


    private static ReturnReason mapReason(String csvReason) {
        String r = csvReason.toLowerCase();
        switch (r) {
            case "customer-remorse":
                return ReturnReason.REMORSE;
            case "expired":
                return ReturnReason.EXPIRED;
            case "damaged":
                return ReturnReason.DAMAGED;
            case "package-opened":
            case "wrong-item":
                // treat as remorse for restock per typical flows
                return ReturnReason.REMORSE;
            default:
                return ReturnReason.REMORSE;
        }
    }

    /**
     * Wagons Unloading
     */
    private static void wagonsUnloading() {
        if (!dataLoaded) {
            System.out.println("❌ Data not loaded. Cannot proceed with wagons unloading.");
            System.out.println("Press Enter to return to main menu...");
            scanner.nextLine();
            return;
        }
        
        WagonsUnloadingController controller = new WagonsUnloadingController(dataLoader);
        WagonsUnloadingUI ui = new WagonsUnloadingUI(controller);
        ui.showMainMenu();
    }
    
    /**
     * Order Eligibility & Allocation
     */
    private static void orderEligibilityAllocation() {
        Warehouse warehouse = new Warehouse();
        warehouse.loadItems("train_station_dataset/items.csv");
        warehouse.loadWagons("train_station_dataset\\wagons.csv");
        warehouse.loadOrders("train_station_dataset\\orders.csv", "train_station_dataset\\order_lines.csv");
        WarehouseRepository warehouseRepository = new WarehouseRepository();
        WarehouseController controller = new WarehouseController(warehouse, warehouseRepository);
        orderEligibilityAllocationUI ui = new orderEligibilityAllocationUI(controller);
        System.out.println("\n=== ORDER ELIGIBILITY & ALLOCATION ===");
        ui.run();
        scanner.nextLine();
    }
    
    /**
     * Picking Plans
     */
    private static void pickingPlans() {
        System.out.println("\n=== PICKING PLANS ===");
        System.out.println("This feature is not yet implemented.");
        System.out.println("Press Enter to return to main menu...");
        scanner.nextLine();
    }
    
    /**
     * Pick Path Sequencing (US04)
     */
    private static void pickPathSequencing() {
        System.out.println("\n=== PICK PATH SEQUENCING (US04) ===");
        
        // Dados de exemplo (simulando output da USEI03)
        // NOTA: Estes dados JÁ DEVEM ESTAR CONSOLIDADOS (sem duplicados)
        List<PickStop> consolidatedStops = Arrays.asList(
                new PickStop(1, 1, 8, 5),
                new PickStop(2, 2, 2, 5),  // Já consolidado: (2,2) tem qty 5
                new PickStop(3, 3, 4, 7)
        );

        System.out.println("Stops consolidados: " + consolidatedStops);

        // Gerar ambos os paths usando o teu PickPath
        Map<PathStrategy, PickPath> results = new HashMap<>();

        // Estratégia A - ASC_SWEEP
        PickPath pathA = new PickPath(PathStrategy.ASC_SWEEP, consolidatedStops);
        results.put(PathStrategy.ASC_SWEEP, pathA);

        // Estratégia B - NEAREST_NEIGHBOUR
        PickPath pathB = new PickPath(PathStrategy.NEAREST_NEIGHBOUR, consolidatedStops);
        results.put(PathStrategy.NEAREST_NEIGHBOUR, pathB);

        // Mostrar resultados
        System.out.println("\n--- ESTRATÉGIA A - ASC SWEEP ---");
        System.out.println("Distância Total: " + pathA.getTotalDistance());
        System.out.println("Caminho: " + pathA.getStops());

        System.out.println("\n--- ESTRATÉGIA B - NEAREST NEIGHBOUR ---");
        System.out.println("Distância Total: " + pathB.getTotalDistance());
        System.out.println("Caminho: " + pathB.getStops());

        System.out.println("\nPress Enter to return to main menu...");
        scanner.nextLine();
    }
    
    /**
     * Returns & Quarantine
     */
    private static void returnsAndQuarantine() {
        while (true) {
            System.out.println("\n=== RETURNS & QUARANTINE ===");
            System.out.println("1. Process Returns (CSV)");
            System.out.println("2. Quality Operator Login");
            System.out.println("3. Back to Main Menu");
            
            System.out.print("Select option: ");
            String choice = scanner.nextLine().trim();
            
            switch (choice) {
                case "1":
                    processReturnsFromCSV();
                    break;
                case "2":
                    qualityOperatorLogin();
                    break;
                case "3":
                    return;
                default:
                    System.out.println("Invalid option!");
            }
        }
    }
    
    /**
     * Process returns from CSV file
     */
    private static void processReturnsFromCSV() {
        InspectionController inspectionController = new InspectionController();
        ItemController itemController = new ItemController(inspectionController);

        Path csvPath = Paths.get("train_station_dataset", "returns.csv");
        List<String> results = new ArrayList<>();

        try (BufferedReader br = Files.newBufferedReader(csvPath, StandardCharsets.UTF_8)) {
            String line;
            boolean first = true;
            while ((line = br.readLine()) != null) {
                if (first) { // skip header
                    first = false;
                    continue;
                }
                if (line.trim().isEmpty()) continue;

                String[] parts = line.split(",");
                if (parts.length < 5) continue;

                // returnId from CSV not used in this flow
                String sku = parts[1].trim();
                int qty = Integer.parseInt(parts[2].trim());
                String reasonRaw = parts[3].trim();
                // timestamp from CSV not used in this flow
                String expiryRaw = parts.length > 5 ? parts[5].trim() : "";

                ReturnReason mappedReason = mapReason(reasonRaw);
                LocalDate expiryDate = expiryRaw.isEmpty() ? null : LocalDate.parse(expiryRaw);

                String result = itemController.processReturn(sku, qty, mappedReason.name(), expiryDate);
                results.add(result);
            }
        } catch (IOException e) {
            System.out.println(String.format("Failed to read CSV: %s", e.getMessage()));
        }

        // Pretty UI output with tables
        ReturnsUI ui = new ReturnsUI(inspectionController);
        ui.showDashboard();
        
        System.out.println("\nPress Enter to return to main menu...");
        scanner.nextLine();
    }
    
    /**
     * Quality Operator Login System
     */
    private static void qualityOperatorLogin() {
        // Build a WarehouseManager from the already loaded data so inspections can restock
        Domain.WarehouseManager wm = new Domain.WarehouseManager(
                dataLoader.items,
                dataLoader.stock,
                dataLoader.bays
        );

        QualityOperatorController controller = new QualityOperatorController(wm);
        if (controller.showLogin()) {
            controller.showMainMenu();
        }
    }
}