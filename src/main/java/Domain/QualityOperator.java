package Domain;

import Controller.InspectionController;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Quality Operator - Single class that handles everything:
 * - Authentication
 * - Quarantine queue management
 * - UI for inspections
 * - Audit logging
 */
public class QualityOperator {
    
    // Authentication
    private String userId;
    private String name;
    private String password;
    private boolean isAuthenticated;
    private LocalDateTime lastLogin;
    
    // Quarantine management
    private List<Return> quarantineItems;
    private InspectionController inspectionController;
    
    // UI
    private Scanner scanner;
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    
    // Static operators database
    private static final Map<String, QualityOperator> operators = new HashMap<>();
    
    static {
        // Initialize with default operators
        operators.put("qop001", new QualityOperator("qop001", "Quality Operator 1", "quality123"));
        operators.put("qop002", new QualityOperator("qop002", "Quality Operator 2", "quality456"));
        operators.put("qop003", new QualityOperator("qop003", "Quality Operator 3", "quality789"));
    }
    
    public QualityOperator(String userId, String name, String password) {
        this.userId = userId;
        this.name = name;
        this.password = password;
        this.isAuthenticated = false;
        this.quarantineItems = new ArrayList<>();
        this.inspectionController = new InspectionController();
        this.scanner = new Scanner(System.in);
    }
    
    
    /**
     * Start the quality operator interface
     */
    public void start() {
        if (!login()) {
            System.out.println("Authentication failed. Exiting...");
            return;
        }
        
        showMainMenu();
    }
    
    /**
     * Handle login process
     */
    private boolean login() {
        System.out.println("\n QUALITY OPERATOR LOGIN");
        System.out.println("Available operators:");
        System.out.println("  - qop001");
        System.out.println("  - qop002");
        System.out.println("  - qop003");
        
        System.out.print("\nEnter User ID: ");
        String userId = scanner.nextLine().trim();
        
        System.out.print("Enter Password: ");
        String password = scanner.nextLine().trim();
        
        QualityOperator operator = operators.get(userId);
        if (operator != null && operator.password.equals(password)) {
            // Copy operator data to current instance
            this.userId = operator.userId;
            this.name = operator.name;
            this.password = operator.password;
            this.isAuthenticated = true;
            this.lastLogin = LocalDateTime.now();
            System.out.println("Login successful! Welcome, " + this.name);
            return true;
        } else {
            System.out.println("Invalid credentials!");
            return false;
        }
    }
    
    /**
     * Show main menu
     */
    private void showMainMenu() {
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
                    logout();
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
            System.out.println("📦 No items in quarantine queue.");
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
            
            String result = inspectionController.processQuarantine(
                    item.getSku(),
                    item.getQty(),
                    item.getReason(),
                    item.getExpiryDate()
            );
            
            System.out.println(result);
            
            // Remove from quarantine queue
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
     * Logout
     */
    private void logout() {
        this.isAuthenticated = false;
        System.out.println("Logged out successfully!");
    }
    
    // Getters
    public String getUserId() { return userId; }
    public String getName() { return name; }
    public boolean isAuthenticated() { return isAuthenticated; }
    public LocalDateTime getLastLogin() { return lastLogin; }
}
