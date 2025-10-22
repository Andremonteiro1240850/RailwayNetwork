package pt.ipp.isep.dei;

import Controller.*;
import Domain.Trolley;
import Domain.Warehouse;
import Domain.WarehouseManager;
import Repository.BoxRepository;
import Repository.TrolleyRepository;
import Repository.WarehouseRepository;
import UI.*;
import Domain.DataLoader;
import Domain.ENUM.ReturnReason;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * The type Main.
 */
public class Main {
    
    private static Scanner scanner = new Scanner(System.in);
    private static DataLoader dataLoader;
    private static boolean dataLoaded = false;
    private static WarehouseRepository warehouseRepository = new WarehouseRepository();
    private static TrolleyRepository trolleyRepository = new TrolleyRepository();
    private static BoxRepository boxRepository;
    
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

            boxRepository = dataLoader.getBoxRepository();
            
            dataLoaded = true;
            System.out.println("✓ All data loaded successfully!");
        } catch (Exception e) {
            System.out.println("Error loading data: " + e.getMessage());
            dataLoaded = false;
        }
    }

    /**
     * The entry point of application.
     *
     * @param args the input arguments
     */
    public static void main(String[] args) {
        // Load data at startup
        loadAllData();
        
        if (!showPreMenu()) {
            return;
        }

        while (true) {
            System.out.println("\n=== WAREHOUSE MANAGEMENT SYSTEM ===");
            System.out.println("1. Wagons Unloading (Terminal Operator)");
            System.out.println("2. Order Eligibility & Allocation (Warehouse Planner)");
            System.out.println("3. Picking Plans (Picker)");
            System.out.println("4. Pick Path Sequencing (Picker)");
            System.out.println("5. Quality Operator");
            System.out.println("6. Travel Time Calculation (Traffic Dispatcher)");
            System.out.println("7. Exit");
            
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
                    qualityOperatorMain();
                    break;
                case "6":
                    travelTimeCalculation();
                    break;
                case "7":
                    System.out.println("Goodbye!");
                    return;
                default:
                    System.out.println("Invalid option!");
            }
        }
    }


    private static void showRoleLoginMenu() {
        while (true) {
            System.out.println("\n=== LOGIN ===");
            System.out.println("1. Login as a Terminal Operator");
            System.out.println("2. Login as a Warehouse Planner");
            System.out.println("3. Login as a Planner");
            System.out.println("4. Login as a Picker");
            System.out.println("5. Login as a Quality Operator");
            System.out.println("6. Login as a Traffic Dispatcher");
            System.out.println("7. Back");

            System.out.print("Select option: ");
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    // Terminal Operator -> Wagons Unloading UI
                    wagonsUnloading();
                    break;
                case "2":
                    // Warehouse Planner -> Order Eligibility & Allocation
                    orderEligibilityAllocation();
                    break;
                case "3":
                    // Planner -> Picking Plans
                    pickingPlans();
                    break;
                case "4":
                    // Picker -> Pick Path Sequencing
                    pickPathSequencing();
                    break;
                case "5":
                    // Quality Operator -> show main menu directly
                    qualityOperatorMain();
                    break;
                case "6":
                    // Traffic Dispatcher -> Travel Time Calculation
                    travelTimeCalculation();
                    break;
                case "7":
                    return;
                default:
                    System.out.println("Invalid option!");
            }
        }
    }

    private static boolean showPreMenu() {
        while (true) {
            System.out.println("\n=== START ===");
            System.out.println("1. Know the development team");
            System.out.println("2. Login");
            System.out.println("3. Cancel");

            System.out.print("Select option: ");
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    DevelopmentTeamUI.show();
                    System.out.println("\nPress Enter to continue...");
                    scanner.nextLine();
                    break;
                case "2":
                    showRoleLoginMenu();
                    return true;
                case "3":
                    System.out.println("Goodbye!");
                    return false;
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
        Warehouse warehouse = new Warehouse(warehouseRepository);
        warehouse.loadItems("train_station_dataset/items.csv");
        warehouse.loadWagons("train_station_dataset/wagons.csv");
        warehouse.loadOrders("train_station_dataset/orders.csv", "train_station_dataset/order_lines.csv");

        // Criar WarehouseManager com boxRepository
        WarehouseManager wm = new WarehouseManager(dataLoader.items, dataLoader.stock, dataLoader.bays, boxRepository);

        // Atribuir boxes aos bays (isso vai preencher a location das boxes no boxRepository)
        wm.unloadWagons();


        warehouseRepository.saveStock(boxRepository.getAllAssignedBoxes());

        // Agora cria controller e UI
        WarehouseController controller = new WarehouseController(warehouse, warehouseRepository);
        orderEligibilityAllocationUI ui = new orderEligibilityAllocationUI(controller, boxRepository);
        System.out.println("\n=== ORDER ELIGIBILITY & ALLOCATION ===");
        ui.run();
    }
    
    /**
     * Picking Plans
     */
    private static void pickingPlans() {
        System.out.println("\n=== PICKING PLANS ===");
        TrolleyController controller = new TrolleyController(warehouseRepository, trolleyRepository);
        TrolleyUI ui = new TrolleyUI(controller);
        ui.run();
    }
    
    /**
     * Pick Path Sequencing (US04)
     */
    private static void pickPathSequencing() {
        System.out.println("\n=== PICK PATH SEQUENCING (US04) ===");
        
        //Criar controller e UI
        PickPathController controller = new PickPathController();
        PickPathUI ui = new PickPathUI(controller);
        
        //  menu da UI
        ui.showMainMenu();
    }
    
    /**
     * Returns & Quarantine
     */
    private static void returnsAndQuarantine() {
        while (true) {
            System.out.println("\n=== RETURNS & QUARANTINE ===");
            System.out.println("1. Process Returns (CSV)");
            System.out.println("2. Back to Main Menu");
            System.out.println("3. ");
            
            System.out.print("Select option: ");
            String choice = scanner.nextLine().trim();
            
            switch (choice) {
                case "1":
                    processReturnsFromCSV();
                    break;
                case "2":
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
    private static void qualityOperatorMain() {
        Domain.WarehouseManager wm = new Domain.WarehouseManager(
                dataLoader.items,
                dataLoader.stock,
                dataLoader.bays
        );

        QualityOperatorController controller = new QualityOperatorController(wm);
        controller.showMainMenu();
    }
    
    /**
     * Travel Time Calculation (USLP03)
     */
    private static void travelTimeCalculation() {
        System.out.println("\n=== TRAVEL TIME CALCULATION (USLP03) ===");
        
        TravelTimeController controller = new TravelTimeController();
        TravelTimeUI ui = new TravelTimeUI(controller.getService());
        ui.showMainMenu();
    }
}