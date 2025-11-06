package Controller;

import Domain.Return;
import Domain.QuarantineInspection;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;

/**
 * Controller for Quality Operator functionality
 */
public class QualityOperatorController {

    private InspectionController inspectionController;
    private Scanner scanner;
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private List<Return> quarantineItems = new ArrayList<>();

    /**
     * Instantiates a new Quality operator controller.
     */
    public QualityOperatorController() {
        this.inspectionController = new InspectionController();
        this.scanner = new Scanner(System.in);
    }

    /**
     * Instantiates a new Quality operator controller.
     *
     * @param warehouseManager the warehouse manager
     */
    public QualityOperatorController(Domain.WarehouseManager warehouseManager) {
        this.inspectionController = new InspectionController(warehouseManager);
        this.scanner = new Scanner(System.in);
    }

    /**
     * Show login screen - now just shows welcome message
     *
     * @return the boolean
     */
    public boolean showLogin() {
        System.out.println("\nQUALITY OPERATOR SYSTEM");
        System.out.println("Welcome to the Quality Operator interface");
        return true;
    }

    /**
     * Show main menu
     */
    public void showMainMenu() {
        // Automatically load returns from CSV when entering the menu
        loadReturnsFromCSV();

        while (true) {
            System.out.println("\n=== QUALITY OPERATOR MENU (USEI05) ===");
            System.out.println("1. View Quarantine Queue");
            System.out.println("2. Process Next Inspection");
            System.out.println("3. Process All Inspections");
            System.out.println("4. View Processed Inspections");
            System.out.println("5. View Audit Log");
            System.out.println("6. Reload Returns from CSV");
            System.out.println("7. Exit");

            System.out.print("\nSelect option: ");
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    viewQuarantineQueue();
                    break;
                case "2":
                    processSingleInspection();
                    break;
                case "3":
                    processNextInspection();
                    break;
                case "4":
                    viewProcessedInspections();
                    break;
                case "5":
                    viewAuditLog();
                    break;
                case "6":
                    loadReturnsFromCSV(true);
                    break;
                case "7":
                    System.out.println("Exiting Quality Operator system...");
                    return;
                default:
                    System.out.println("Invalid option!");
            }
        }
    }

    /**
     * Load returns from CSV file according to USEI05 requirements
     */
    private void loadReturnsFromCSV() {
        loadReturnsFromCSV(false);
    }

    /**
     * Load returns from CSV file according to USEI05 requirements
     * @param showDetails whether to show detailed output
     */
    private void loadReturnsFromCSV(boolean showDetails) {
        if (showDetails) {
            System.out.println("\n=== LOAD RETURNS FROM CSV ===");
        }

        Path csvPath = Paths.get("train_station_dataset", "returns.csv");

        if (!Files.exists(csvPath)) {
            if (showDetails) {
                System.out.println("ERROR: Returns CSV file not found at: " + csvPath);
                System.out.println("Please ensure the file exists and try again.");
            }
            return;
        }

        if (showDetails) {
            System.out.println("Loading returns from: " + csvPath);
        }

        List<Return> loadedReturns = new ArrayList<>();

        try (BufferedReader br = Files.newBufferedReader(csvPath, StandardCharsets.UTF_8)) {
            String line;
            boolean first = true;
            int lineNumber = 0;

            while ((line = br.readLine()) != null) {
                lineNumber++;

                if (first) { // skip header
                    first = false;
                    continue;
                }

                if (line.trim().isEmpty()) continue;

                // preserve trailing empty columns (important for lines where expiry_date is empty)
                String[] parts = line.split(",", -1);
                if (parts.length < 6) {
                    if (showDetails) {
                        System.out.println("WARNING: Line " + lineNumber + " has insufficient columns, skipping...");
                    }
                    continue;
                }

                try {
                    String returnId = parts[0].trim();
                    String sku = parts[1].trim();
                    int qty = Integer.parseInt(parts[2].trim());
                    String reason = parts[3].trim();
                    String timestampStr = parts[4].trim();
                    String expiryStr = parts.length > 5 ? parts[5].trim() : "";

                    // Parse timestamp
                    LocalDateTime timestamp = LocalDateTime.parse(timestampStr);

                    // Parse expiry date (optional)
                    LocalDate expiryDate = null;
                    if (!expiryStr.isEmpty()) {
                        expiryDate = LocalDate.parse(expiryStr);
                    }

                    // Map CSV reasons to system reasons
                    String mappedReason = mapReturnReason(reason);

                    Return returnItem = new Return(returnId, sku, qty, mappedReason, timestamp, expiryDate);
                    loadedReturns.add(returnItem);

                } catch (Exception e) {
                    if (showDetails) {
                        System.out.println("WARNING: Error parsing line " + lineNumber + ": " + e.getMessage());
                    }
                }
            }

            // Clear existing quarantine items and add loaded returns
            quarantineItems.clear();
            quarantineItems.addAll(loadedReturns);

            // Sort according to USEI05 requirements: timestamp DESC, returnId ASC
            sortQuarantineQueue();

            if (showDetails) {
                System.out.println("SUCCESS: Successfully loaded " + loadedReturns.size() + " returns from CSV");
                System.out.println("Returns added to quarantine queue and sorted by timestamp DESC, returnId ASC");

                if (!loadedReturns.isEmpty()) {
                    System.out.println("\nFirst few returns loaded:");
                    int displayCount = Math.min(5, loadedReturns.size());
                    for (int i = 0; i < displayCount; i++) {
                        Return r = loadedReturns.get(i);
                        System.out.printf("  %s - %s (Qty: %d, Reason: %s, Time: %s)%n",
                                r.getReturnId(), r.getSku(), r.getQty(), r.getReason(),
                                r.getTimestamp().format(dateFormatter));
                    }
                    if (loadedReturns.size() > 5) {
                        System.out.println("  ... and " + (loadedReturns.size() - 5) + " more");
                    }
                }

            } else {
                // Silent loading - just show a brief message
                System.out.println("Returns automatically loaded from CSV (" + loadedReturns.size() + " items)");
            }

        } catch (IOException e) {
            if (showDetails) {
                System.out.println("ERROR: Error reading CSV file: " + e.getMessage());
            }
        }
    }

    /**
     * Map CSV return reasons to system reasons according to USEI05 logic
     */
    private String mapReturnReason(String csvReason) {
        String reason = csvReason.toLowerCase().trim();

        switch (reason) {
            case "customer-remorse":
                return "REMORSE";
            case "cycle-count":
                return "CYCLE_COUNT";
            case "damaged":
                return "DAMAGED";
            case "expired":
                return "EXPIRED";
            case "package-opened":
            case "wrong-item":
                // These are typically customer remorse cases
                return "REMORSE";
            default:
                // Default to REMORSE for unknown reasons
                return "REMORSE";
        }
    }


    /**
     * View quarantine queue (sorted by timestamp DESC, returnId ASC) according to USEI05
     */
    private void viewQuarantineQueue() {
        System.out.println("\n=== QUARANTINE QUEUE ===");

        if (quarantineItems.isEmpty()) {
            System.out.println("No items in quarantine queue.");
            System.out.println("Returns are automatically loaded from CSV when you enter the menu.");
            System.out.println("Use option 5 to reload from CSV if needed.");
            return;
        }

        System.out.println("Queue sorted by: Timestamp DESC, ReturnId ASC (Last to arrive, first to process)");
        System.out.println("WARNING: Action (Restock/Discard) will be determined during inspection processing");
        System.out.println();

        System.out.printf("%-12s %-10s %-6s %-15s %-20s %-12s%n",
                "Return ID", "SKU", "Qty", "Reason", "Timestamp", "Expiry Date");
        System.out.println("=".repeat(85));

        for (Return item : quarantineItems) {
            System.out.printf("%-12s %-10s %-6d %-15s %-20s %-12s%n",
                    item.getReturnId(),
                    item.getSku(),
                    item.getQty(),
                    item.getReason(),
                    item.getTimestamp().format(dateFormatter),
                    item.getExpiryDate() != null ? item.getExpiryDate().toString() : "N/A"
            );
        }

        System.out.println("=".repeat(85));
        System.out.println("Total items in queue: " + quarantineItems.size());
        System.out.println("Use option 2 to process inspections and determine actions");

    }

    /**
     * Process single inspection from quarantine queue
     */
    private void processSingleInspection() {
        System.out.println("\n=== PROCESS NEXT INSPECTION (USEI05) ===");

        if (quarantineItems.isEmpty()) {
            System.out.println("No items in quarantine queue.");
            System.out.println("Returns are automatically loaded from CSV when you enter the menu.");
            System.out.println("If the queue is empty, try option 6 to reload from CSV.");
            return;
        }

        Return item = quarantineItems.get(0);
        int remainingItems = quarantineItems.size();

        System.out.println("Processing 1 item from quarantine queue");
        System.out.println("Remaining items in queue: " + (remainingItems - 1));
        System.out.println("Following USEI05 logic: Last to arrive, first to process");

        System.out.println("\n" + "=".repeat(60));
        System.out.println("PROCESSING SINGLE ITEM");
        System.out.println("=".repeat(60));
        System.out.println("Return ID: " + item.getReturnId());
        System.out.println("SKU: " + item.getSku());
        System.out.println("Quantity: " + item.getQty());
        System.out.println("Original Reason: " + item.getReason());
        System.out.println("Timestamp: " + item.getTimestamp().format(dateFormatter));
        System.out.println("Expiry Date: " + (item.getExpiryDate() != null ? item.getExpiryDate().toString() : "N/A"));

        // Determine action based on USEI05 logic
        String reasonToUse = determineProcessingReason(item);
        String action = determineActionFromReason(reasonToUse);

        System.out.println("\nUSEI05 Processing Logic:");
        System.out.println("  Final Reason: " + reasonToUse);
        System.out.println("  Action: " + action);

        if ("DISCARD".equals(action)) {
            System.out.println("\nDISCARDING PRODUCT");
            if ("EXPIRED".equals(reasonToUse)) {
                System.out.println("   Reason: Product expired on " + item.getExpiryDate());
            } else {
                System.out.println("   Reason: " + reasonToUse);
            }
        } else {
            System.out.println("\nRESTOCKING PRODUCT");
            System.out.println("   Reason: " + reasonToUse);
            System.out.println("   Following FEFO/FIFO placement rules");
        }

        // Process the inspection
        String result = inspectionController.processQuarantine(
                item.getSku(),
                item.getQty(),
                reasonToUse,
                item.getExpiryDate()
        );

        System.out.println("\nProcessing Result:");
        System.out.println(result);

        // Remove processed item
        quarantineItems.remove(0);

        System.out.println("\n" + "=".repeat(60));
        System.out.println("INSPECTION COMPLETED");
        System.out.println("=".repeat(60));
        System.out.println("Action: " + action);
        System.out.println("Remaining items in queue: " + quarantineItems.size());
        System.out.println("\nAudit log has been updated with this action.");

    }

    /**
     * Process all inspections currently in the queue according to USEI05 requirements
     */
    private void processNextInspection() {
        System.out.println("\n=== PROCESS INSPECTIONS (USEI05) ===");

        if (quarantineItems.isEmpty()) {
            System.out.println("No items in quarantine queue.");
            System.out.println("Returns are automatically loaded from CSV when you enter the menu.");
            System.out.println("If the queue is empty, try option 5 to reload from CSV.");
            return;
        }

        int totalToProcess = quarantineItems.size();
        System.out.println("Processing " + totalToProcess + " items from quarantine queue");
        System.out.println("Following USEI05 logic: Last to arrive, first to process");

        int processedCount = 0;
        int discardedCount = 0;
        int restockedCount = 0;

        while (!quarantineItems.isEmpty()) {
            Return item = quarantineItems.get(0);
            processedCount++;

            System.out.println("\n" + "=".repeat(60));
            System.out.println("Processing Item " + processedCount + "/" + totalToProcess);
            System.out.println("=".repeat(60));
            System.out.println("Return ID: " + item.getReturnId());
            System.out.println("SKU: " + item.getSku());
            System.out.println("Quantity: " + item.getQty());
            System.out.println("Original Reason: " + item.getReason());
            System.out.println("Timestamp: " + item.getTimestamp().format(dateFormatter));
            System.out.println("Expiry Date: " + (item.getExpiryDate() != null ? item.getExpiryDate().toString() : "N/A"));

            // Determine action based on USEI05 logic
            String reasonToUse = determineProcessingReason(item);
            String action = determineActionFromReason(reasonToUse);

            System.out.println("\nUSEI05 Processing Logic:");
            System.out.println("  Final Reason: " + reasonToUse);
            System.out.println("  Action: " + action);

            if ("DISCARD".equals(action)) {
                discardedCount++;
                System.out.println("\nDISCARDING PRODUCT");
                if ("EXPIRED".equals(reasonToUse)) {
                    System.out.println("   Reason: Product expired on " + item.getExpiryDate());
                } else {
                    System.out.println("   Reason: " + reasonToUse);
                }
            } else {
                restockedCount++;
                System.out.println("\nRESTOCKING PRODUCT");
                System.out.println("   Reason: " + reasonToUse);
                System.out.println("   Following FEFO/FIFO placement rules");
            }

            // Process the inspection
            String result = inspectionController.processQuarantine(
                    item.getSku(),
                    item.getQty(),
                    reasonToUse,
                    item.getExpiryDate()
            );

            System.out.println("\nProcessing Result:");
            System.out.println(result);

            // Remove processed item
            quarantineItems.remove(0);
        }

        System.out.println("\n" + "=".repeat(60));
        System.out.println("ALL INSPECTIONS COMPLETED");
        System.out.println("=".repeat(60));
        System.out.println("Total Processed: " + processedCount);
        System.out.println("Discarded: " + discardedCount);
        System.out.println("Restocked: " + restockedCount);
        System.out.println("Quarantine queue is now empty.");
        System.out.println("\nAudit log has been updated with all actions.");

    }

    /**
     * Determine the final processing reason for a return item
     */
    private String determineProcessingReason(Return item) {
        // Check if product is expired first
        if (item.getExpiryDate() != null && item.getExpiryDate().isBefore(LocalDate.now())) {
            return "EXPIRED";
        }

        // Use original reason if not expired
        return item.getReason();
    }

    /**
     * Determine action based on reason according to USEI05 logic
     */
    private String determineActionFromReason(String reason) {
        switch (reason.toUpperCase()) {
            case "REMORSE":
            case "CYCLE_COUNT":
                return "RESTOCK";
            case "DAMAGED":
            case "EXPIRED":
                return "DISCARD";
            default:
                return "DISCARD"; // Default to discard for safety
        }
    }

    /**
     * View processed inspections
     */
    private void viewProcessedInspections() {
        System.out.println("\n=== PROCESSED INSPECTIONS ===");

        List<QuarantineInspection> inspections = inspectionController.getInspectionsBySku("");

        if (inspections.isEmpty()) {
            System.out.println("No processed inspections yet.");
            return;
        }

        System.out.printf("%-15s %-15s %-20s %-10s%n",
                "Inspection ID", "Return ID", "Inspected At", "Action");
        System.out.println("======================================================================");

        for (QuarantineInspection inspection : inspections) {
            System.out.printf("%-15s %-15s %-20s %-10s%n",
                    inspection.getInspectionId(),
                    inspection.getReturnId(),
                    inspection.getInspectedAt().format(dateFormatter),
                    inspection.getAction()
            );
        }
    }

    /**
     * View audit log
     */
    private void viewAuditLog() {
        System.out.println("\n=== AUDIT LOG ===");

        List<String> auditLogs = inspectionController.getAuditLogs();

        if (auditLogs.isEmpty()) {
            System.out.println("No audit logs available.");
            return;
        }

        System.out.println("Recent audit entries:");
        System.out.println("====================================================================================================");

        for (String logEntry : auditLogs) {
            System.out.println(logEntry);
        }
    }

    /**
     * Sort quarantine queue according to user story requirements:
     * - Latest first (timestamp DESC)
     * - Ties broken by returnId ASC
     */
    private void sortQuarantineQueue() {
        quarantineItems.sort(Comparator
                .comparing(Return::getTimestamp)
                .reversed()  // Latest first (DESC)
                .thenComparing(Return::getReturnId)  // Ties by returnId ASC
        );
    }

}
