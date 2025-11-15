package pt.ipp.isep.dei;

import Controller.*;
import Domain.StationLoader;
import Domain.Warehouse;
import Domain.ENUM.ReturnReason;
import Repository.BoxRepository;
import Repository.StationRepository;
import Repository.TrolleyRepository;
import Repository.WarehouseRepository;
import UI.*;
import Domain.DataLoader;

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
    private StationRepository stationRepository;
    private static StationLoader stationLoader = new StationLoader();
    
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
        // Clear audit log at startup to only show inspections from this session
        Domain.AuditLog.clearAll();
        
        // Load data at startup
        loadAllData();
        
        if (!showPreMenu()) {
            return;
        }

        // After pre-menu, go directly to login menu
        while (true) {
            if (!showRoleLoginMenu()) {
                // If user selects "Back" from login menu, return to pre-menu
                if (!showPreMenu()) {
                    return;
                }
            }
        }
    }


    private static boolean showRoleLoginMenu() {
        while (true) {
            System.out.println("\n=== LOGIN ===");
            System.out.println("1. Cargo Handling & Terminal Operations");
            System.out.println("2. Spatial Indexing & Geographical Queries");
            System.out.println("3. Back");

            System.out.print("Select option: ");
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    // Cargo Handling & Terminal Operations
                    if (!showCargoHandlingMenu()) {
                        continue; // User selected back, return to main login menu
                    }
                    break;
                case "2":
                    // Spatial Indexing & Geographical Queries
                    if (!showSpatialIndexingMenu()) {
                        continue; // User selected back, return to main login menu
                    }
                    break;
                case "3":
                    return false; // Back selecionado
                default:
                    System.out.println("Invalid option!");
            }
        }
    }

    /**
     * Shows the Cargo Handling & Terminal Operations menu
     */
    private static boolean showCargoHandlingMenu() {
        while (true) {
            System.out.println("\n=== CARGO HANDLING & TERMINAL OPERATIONS ===");
            System.out.println("1. Login as a Quality Operator");
            System.out.println("2. Login as a Planner");
            System.out.println("3. Login as a Picker");
            System.out.println("4. Login as a Warehouse Planner");
            System.out.println("5. Login as a Terminal Operator");
            System.out.println("6. Login as a Traffic Dispatcher");
            System.out.println("7. Back");

            System.out.print("Select option: ");
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    // Quality Operator -> show main menu directly
                    qualityOperatorMain();
                    // After quality operator, return to menu (continue loop)
                    break;
                case "2":
                    // Planner -> Picking Plans
                    pickingPlans();
                    // After picking plans, return to menu (continue loop)
                    break;
                case "3":
                    // Picker -> Pick Path Sequencing
                    pickPathSequencing();
                    // After pick path sequencing, return to menu (continue loop)
                    break;
                case "4":
                    // Warehouse Planner -> Order Eligibility & Allocation
                    orderEligibilityAllocation();
                    // After order eligibility, return to menu (continue loop)
                    break;
                case "5":
                    // Terminal Operator -> Wagons Unloading UI
                    wagonsUnloading();
                    // After wagons unloading, return to menu (continue loop)
                    break;
                case "6":
                    // Traffic Dispatcher -> Travel Time Calculation
                    travelTimeCalculation();
                    // After travel time calculation, return to menu (continue loop)
                    break;
                case "7":
                    return false; // Back selecionado
                default:
                    System.out.println("Invalid option!");
            }
        }
    }

    /**
     * Shows the Spatial Indexing & Geographical Queries menu
     */
    private static boolean showSpatialIndexingMenu() {
        while (true) {
            System.out.println("\n=== SPATIAL INDEXING & GEOGRAPHICAL QUERIES ===");
            System.out.println("1. Login as a Planner to lookup for Stations");
            System.out.println("2. Login as a Freight Manager");
            System.out.println("3. Back");

            System.out.print("Select option: ");
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    // Planner -> Station Lookup
                    stationLookup();
                    // After station lookup, return to menu (continue loop)
                    break;
                case "2":
                    // Freight Manager -> Train Dispatch Scheduler
                    freightManager();
                    // After freight manager, return to menu (continue loop)
                    break;
                case "3":
                    return false; // Back selecionado
                default:
                    System.out.println("Invalid option!");
            }
        }
    }

    private static boolean showPreMenu() {
        while (true) {
            System.out.println("\n=== MAIN MENU ===");
            System.out.println("1. Know the development team");
            System.out.println("2. Login");
            System.out.println("3. Cancel");

            System.out.print("Select option: ");
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    DevelopmentTeamUI.show();
                    break;
                case "2":
                    if (showRoleLoginMenu()) {
                        return true;
                    }
                    // Se showRoleLoginMenu retornar false (Back foi selecionado), continua o loop
                    break;
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
            System.out.println("Data not loaded. Cannot proceed with wagons unloading.");
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

        // Note Boxes are already loaded and unassigned in DataLoader
        // They will be assigned to bays when the user explicitly chooses to unload wagons in the UI

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
        System.out.println("\n=== PICK PATH SEQUENCING ===");
        
        //Criar controller e UI com acesso aos trolleys da US03
        PickPathController controller = new PickPathController(trolleyRepository);
        PickPathUI ui = new PickPathUI(controller);
        
        //  menu da UI
        ui.showMainMenu();
    }
    
    
    /**
     * Quality Operator Login System (USEI05)
     */
    private static void qualityOperatorMain() {
        if (!dataLoaded) {
            System.out.println("Data not loaded. Cannot proceed with quality operator functions.");
            return;
        }
        
        System.out.println("\n=== QUALITY OPERATOR LOGIN ===");
        
        WagonsUnloadingController wagonsController = new WagonsUnloadingController(dataLoader);

        QualityOperatorController controller = new QualityOperatorController(wagonsController);
        QualityOperatorUI ui = new QualityOperatorUI(controller);
        
        // Show welcome message and go directly to menu
        ui.showLogin();
        ui.showMainMenu();
    }
    
    /**
     * Travel Time Calculation (USLP03)
     */
    private static void travelTimeCalculation() {
        System.out.println("\n=== TRAVEL TIME CALCULATION  ===");
        
        TravelTimeController controller = new TravelTimeController();
        TravelTimeUI ui = new TravelTimeUI(controller);
        ui.showMainMenu();
    }
    private static void stationLookup() {
        try {
            System.out.println("\n=== STATION LOOKUP ===");
            System.out.println("Loading stations...");

            StationController controller = new StationController(new StationRepository(stationLoader.loadFromCSV("train_station_dataset/train_stations_europe.csv")));

            System.out.println("Stations loaded successfully!");
            System.out.println("Starting Station UI...");


            StationUI ui = new StationUI(controller, scanner);
            ui.run();

            System.out.println("Returned from Station UI");

        } catch (Exception e) {
            System.err.println("ERROR in stationLookup: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Freight Manager - Train Dispatch Scheduler
     */
    private static void freightManager() {
        System.out.println("\n=== FREIGHT MANAGER - TRAIN DISPATCH SCHEDULER ===");
        
        DispatchController controller = new DispatchController();
        SchedulerUI ui = new SchedulerUI(controller);
        ui.showMainMenu();
    }
}