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
            System.out.println("3. Process Next Inspection");
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
     * Process next inspection
     */
    private void processNextInspection() {
        System.out.println("\n=== PROCESS INSPECTION ===");
        
        if (quarantineItems.isEmpty()) {
            System.out.println("No items in quarantine queue.");
            return;
        }
        
        Return nextItem = quarantineItems.get(0);
        System.out.println("Next item to inspect:");
        System.out.println("Return ID: " + nextItem.getReturnId());
        System.out.println("SKU: " + nextItem.getSku());
        System.out.println("Quantity: " + nextItem.getQty());
        System.out.println("Reason: " + nextItem.getReason());
        System.out.println("Timestamp: " + nextItem.getTimestamp().format(dateFormatter));
        System.out.println("Expiry Date: " + (nextItem.getExpiryDate() != null ? nextItem.getExpiryDate().toString() : "N/A"));
        
        // Check if product is expired - if so, auto-discard
        if (nextItem.getExpiryDate() != null && nextItem.getExpiryDate().isBefore(LocalDate.now())) {
            System.out.println("\n*** PRODUCT EXPIRED - AUTO-DISCARDING ***");
            System.out.println("Product expired on " + nextItem.getExpiryDate() + " (auto-discarded)");
            
            // Process the inspection automatically
            String result = inspectionController.processQuarantine(
                    nextItem.getSku(),
                    nextItem.getQty(),
                    nextItem.getReason(),
                    nextItem.getExpiryDate()
            );
            
            System.out.println("\n" + result);
        } else {
            // Product not expired - ask user for action
            System.out.println("\nWhat action should be taken?");
            System.out.println("1. RESTOCK");
            System.out.println("2. DISCARD");
            
            System.out.print("Select action (1-2): ");
            scanner.nextLine().trim();
            
            // Process the inspection
            String result = inspectionController.processQuarantine(
                    nextItem.getSku(),
                    nextItem.getQty(),
                    nextItem.getReason(),
                    nextItem.getExpiryDate()
            );
            
            System.out.println("\n" + result);
        }
        
        // Remove from quarantine queue
        quarantineItems.remove(0);
        
        System.out.println("Inspection processed and item removed from quarantine queue.");
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
