package Domain;

import Controller.InspectionController;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Quality Operator - Handles quarantine queue management,
 * inspection processing, and audit logging.
 * 
 * NOTE: This is legacy code used only for testing purposes.
 * Production code should use QualityOperatorController + QualityOperatorUI instead.
 * 
 * @deprecated Use QualityOperatorController and QualityOperatorUI for production code
 */
@Deprecated
public class QualityOperator {

    // Quarantine management
    private List<Return> quarantineItems;
    private InspectionController inspectionController;

    // UI
    private Scanner scanner;
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    /**
     * Creates a new QualityOperator instance
     */
    public QualityOperator() {
        this.quarantineItems = new ArrayList<>();
        this.inspectionController = new InspectionController();
        this.scanner = new Scanner(System.in);
    }

    /**
     * Start the Quality Operator interface directly (no authentication)
     */
    public void start() {
        showMainMenu();
    }

    /**
     * Show main menu
     */
    private void showMainMenu() {
        while (true) {
            System.out.println("\n=== QUALITY OPERATOR MENU ===");
            System.out.println("1. View Quarantine Queue");
            System.out.println("2. Process All Inspections");
            System.out.println("3. View Processed Inspections");
            System.out.println("4. View Audit Log");
            System.out.println("5. Logout");

            System.out.print("\nSelect option: ");
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    viewQuarantineQueue();
                    break;
                case "2":
                    processNextInspection();
                    break;
                case "3":
                    viewProcessedInspections();
                    break;
                case "4":
                    viewAuditLog();
                    break;
                case "5":
                    logout();
                    return;
                default:
                    System.out.println("Invalid option!");
            }
        }
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
            System.out.printf(
                    "%-15s %-10s %-8d %-15s %-20s %-12s%n",
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
     * Sort quarantine queue:
     * - Newest first (timestamp DESC)
     * - Ties broken by returnId ASC
     */
    private void sortQuarantineQueue() {
        quarantineItems.sort(Comparator
                .comparing(Return::getTimestamp)
                .reversed()
                .thenComparing(Return::getReturnId)
        );
    }

    /**
     * Logout
     */
    private void logout() {
        System.out.println("Logged out successfully!");
    }
}
