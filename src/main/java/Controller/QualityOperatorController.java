package Controller;

import Domain.Return;
import Domain.QuarantineInspection;
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
    
    public QualityOperatorController() {
        this.inspectionController = new InspectionController();
        this.scanner = new Scanner(System.in);
    }

    public QualityOperatorController(Domain.WarehouseManager warehouseManager) {
        this.inspectionController = new InspectionController(warehouseManager);
        this.scanner = new Scanner(System.in);
    }
    
    /**
     * Show login screen
     */
    public boolean showLogin() {
        System.out.println("\nQUALITY OPERATOR LOGIN");
        System.out.println("Available operators:");
        System.out.println("  - qop001");
        System.out.println("  - qop002");
        System.out.println("  - qop003");
        
        System.out.print("\nEnter User ID: ");
        String userId = scanner.nextLine().trim();
        
        System.out.print("Enter Password: ");
        String password = scanner.nextLine().trim();
        
        // Simple authentication
        if (authenticateOperator(userId, password)) {
            System.out.println("Login successful! Welcome, " + getOperatorName(userId));
            return true;
        } else {
            System.out.println("Invalid credentials!");
            return false;
        }
    }
    
    /**
     * Show main menu
     */
    public void showMainMenu() {
        while (true) {
            System.out.println("\n=== QUALITY OPERATOR MENU ===");
            System.out.println("1. View Quarantine Queue");
            System.out.println("2. Add Item to Quarantine");
            System.out.println("3. Process All Inspections");
            System.out.println("4. View Processed Inspections");
            System.out.println("5. View Audit Log");
            System.out.println("6. Logout");
            
            System.out.print("\nSelect option: ");
            String choice = scanner.nextLine().trim();
            
            switch (choice) {
                case "1":
                    viewQuarantineQueue();
                    break;
                case "2":
                    addItemToQuarantine();
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
                    System.out.println("Logged out successfully!");
                    return;
                default:
                    System.out.println("Invalid option!");
            }
        }
    }
    
    /**
     * Add item to quarantine queue
     */
    private void addItemToQuarantine() {
        System.out.println("\n=== ADD ITEM TO QUARANTINE ===");
        
        System.out.print("Enter Return ID: ");
        String returnId = scanner.nextLine().trim();
        
        System.out.print("Enter SKU: ");
        String sku = scanner.nextLine().trim();
        
        System.out.print("Enter Quantity: ");
        int quantity = Integer.parseInt(scanner.nextLine().trim());
        
        System.out.print("Enter Reason (REMORSE/DAMAGED/EXPIRED/CYCLE_COUNT): ");
        String reason = scanner.nextLine().trim();
        
        System.out.print("Enter Expiry Date (yyyy-mm-dd) or press Enter for none: ");
        String expiryStr = scanner.nextLine().trim();
        LocalDate expiryDate = expiryStr.isEmpty() ? null : LocalDate.parse(expiryStr);
        
        Return returnItem = new Return(returnId, sku, quantity, reason, LocalDateTime.now(), expiryDate);
        quarantineItems.add(returnItem);
        sortQuarantineQueue();
        
        System.out.println("Item added to quarantine queue.");
    }
    
    /**
     * View quarantine queue (sorted by timestamp DESC, returnId ASC)
     */
    private void viewQuarantineQueue() {
        System.out.println("\n=== QUARANTINE QUEUE ===");
        
        if (quarantineItems.isEmpty()) {
            System.out.println("No items in quarantine queue.");
            return;
        }
        
        System.out.printf("%-15s %-10s %-8s %-15s %-20s %-12s%n", 
                "Return ID", "SKU", "Qty", "Reason", "Timestamp", "Expiry Date");
        System.out.println("==========================================================================================");
        
        for (Return item : quarantineItems) {
            System.out.printf("%-15s %-10s %-8d %-15s %-20s %-12s%n",
                    item.getReturnId(),
                    item.getSku(),
                    item.getQty(),
                    item.getReason(),
                    item.getTimestamp().format(dateFormatter),
                    item.getExpiryDate() != null ? item.getExpiryDate().toString() : "N/A"
            );
        }
        
        System.out.println("\nTotal items in queue: " + quarantineItems.size());
    }
    
    /**
     * Process all inspections currently in the queue
     */
    private void processNextInspection() {
        System.out.println("\n=== PROCESS INSPECTIONS ===");
        
        if (quarantineItems.isEmpty()) {
            System.out.println("No items in quarantine queue.");
            return;
        }
        
        int totalToProcess = quarantineItems.size();
        System.out.println("Items to process: " + totalToProcess);
        
        while (!quarantineItems.isEmpty()) {
            Return item = quarantineItems.get(0);
            System.out.println("\nProcessing Return ID: " + item.getReturnId());
            System.out.println("SKU: " + item.getSku());
            System.out.println("Quantity: " + item.getQty());
            System.out.println("Reason: " + item.getReason());
            System.out.println("Timestamp: " + item.getTimestamp().format(dateFormatter));
            System.out.println("Expiry Date: " + (item.getExpiryDate() != null ? item.getExpiryDate().toString() : "N/A"));
            
            // If expired, force EXPIRED to ensure DISCARD path in controller
            String reasonToUse = (item.getExpiryDate() != null && item.getExpiryDate().isBefore(LocalDate.now()))
                    ? "EXPIRED"
                    : item.getReason();
            
            if ("EXPIRED".equals(reasonToUse)) {
                System.out.println("\n*** PRODUCT EXPIRED - AUTO-DISCARDING ***");
                System.out.println("Product expired on " + item.getExpiryDate() + " (auto-discarded)");
            }
            
            String result = inspectionController.processQuarantine(
                    item.getSku(),
                    item.getQty(),
                    reasonToUse,
                    item.getExpiryDate()
            );
            System.out.println(result);
            
            // Remove processed item
            quarantineItems.remove(0);
        }
        
        System.out.println("\nAll inspections processed. Quarantine queue is now empty.");
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
    
    /**
     * Simple authentication check
     */
    private boolean authenticateOperator(String userId, String password) {
        return (userId.equals("qop001") && password.equals("quality123")) ||
               (userId.equals("qop002") && password.equals("quality456")) ||
               (userId.equals("qop003") && password.equals("quality789"));
    }
    
    /**
     * Get operator name
     */
    private String getOperatorName(String userId) {
        switch (userId) {
            case "qop001": return "qop001";
            case "qop002": return "qop002";
            case "qop003": return "qop003";
            default: return "Unknown";
        }
    }
}
