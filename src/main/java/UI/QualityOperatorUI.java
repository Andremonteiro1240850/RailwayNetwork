package UI;

import Controller.QualityOperatorController;
import Domain.Return;
import Domain.QuarantineInspection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;

/**
 * UI for Quality Operator functionality
 * Handles all user interface and presentation logic
 * Includes dashboard functionality from ReturnsUI
 * 
 * Architecture Note:
 * This UI class is responsible for ALL user-facing output and interaction.
 * The controllers (QualityOperatorController and InspectionController) handle
 * business logic only and return data structures/strings. This UI class:
 * - Calls controller methods to get data
 * - Formats and displays all messages using System.out.println()
 * - Handles all user input via Scanner
 * - Presents information in a user-friendly format
 * 
 * All print statements that were previously in the controllers have been moved here
 * to maintain proper separation of concerns between Controller and UI layers.
 */
public class QualityOperatorUI {

    private QualityOperatorController controller;
    private Scanner scanner;
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    /**
     * Instantiates a new Quality operator UI.
     *
     * @param controller the quality operator controller
     */
    public QualityOperatorUI(QualityOperatorController controller) {
        this.controller = controller;
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
        QualityOperatorController.LoadResult loadResult = controller.loadReturnsFromCSV();
        if (loadResult.isSuccess() && loadResult.getLoadedCount() > 0) {
            System.out.println("Returns automatically loaded from CSV (" + loadResult.getLoadedCount() + " items)");
        }

        while (true) {
            System.out.println("\n=== QUALITY OPERATOR MENU (USEI05) ===");
            System.out.println("1. View Quarantine Queue");
            System.out.println("2. Process Next Inspection");
            System.out.println("3. Process All Inspections");
            System.out.println("4. View Processed Inspections");
            System.out.println("5. View Audit Log");
            System.out.println("6. Show Dashboard");
            System.out.println("7. Reload Returns from CSV");
            System.out.println("8. Exit");

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
                    processAllInspections();
                    break;
                case "4":
                    viewProcessedInspections();
                    break;
                case "5":
                    viewAuditLog();
                    break;
                case "6":
                    showDashboard();
                    break;
                case "7":
                    reloadReturnsFromCSV();
                    break;
                case "8":
                    System.out.println("Exiting Quality Operator system...");
                    return;
                default:
                    System.out.println("Invalid option!");
            }
        }
    }

    /**
     * Show dashboard with inspections and audit log
     */
    public void showDashboard() {
        println();
        println(center("Returns Dashboard", 86));
        println();
        renderInspectionsTable();
        System.out.println();
        renderAuditLogTable();
    }

    /**
     * Render inspections table in formatted style
     */
    private void renderInspectionsTable() {
        List<QuarantineInspection> inspections = controller.getProcessedInspections();
        String[] cols = {"InspectionID", "ReturnID", "Action", "InspectedAt"};
        int[] widths = {16, 12, 10, 24};
        printTableHeader("Inspections", cols, widths);
        if (inspections.isEmpty()) {
            printEmptyRow(widths, "No inspections available");
            printTableFooter(widths);
            return;
        }
        for (QuarantineInspection i : inspections) {
            printRow(widths,
                    safe(i.getInspectionId()),
                    safe(i.getReturnId()),
                    i.getAction() == null ? "-" : i.getAction().name(),
                    i.getInspectedAt() == null ? "-" : i.getInspectedAt().format(dateFormatter));
        }
        printTableFooter(widths);
    }

    /**
     * Render audit log table in formatted style
     */
    private void renderAuditLogTable() {
        List<String> logs = controller.getAuditLogs();
        String[] cols = {"Timestamp", "ReturnId", "SKU", "Action", "Qty"};
        int[] widths = {17, 12, 10, 10, 6};
        printTableHeader("Audit Log", cols, widths);
        if (logs.isEmpty()) {
            printEmptyRow(widths, "No audit entries");
            printTableFooter(widths);
            return;
        }
        for (String l : logs) {
            String ts = "-", returnId = "-", sku = "-", action = "-", qty = "-";
            String[] parts = l.split("\\|");
            if (parts.length >= 1) {
                ts = parts[0].trim();
                for (int i = 1; i < parts.length; i++) {
                    String p = parts[i].trim();
                    int eq = p.indexOf('=');
                    if (eq > 0) {
                        String key = p.substring(0, eq).trim();
                        String val = p.substring(eq + 1).trim();
                        String k = key.toLowerCase();
                        if ("returnid".equals(k)) returnId = val;
                        else if ("sku".equals(k)) sku = val;
                        else if ("action".equals(k)) action = val;
                        else if ("qty".equals(k) || "quantity".equals(k)) qty = val;
                    }
                }
            }
            printRow(widths, ts, returnId, sku, action, qty);
        }
        printTableFooter(widths);
    }

    /**
     * View quarantine queue (sorted by timestamp DESC, returnId ASC) according to USEI05
     */
    private void viewQuarantineQueue() {
        System.out.println("\n=== QUARANTINE QUEUE ===");

        List<Return> quarantineItems = controller.getQuarantineItems();

        if (quarantineItems.isEmpty()) {
            System.out.println("No items in quarantine queue.");
            System.out.println("Returns are automatically loaded from CSV when you enter the menu.");
            System.out.println("Use option 7 to reload from CSV if needed.");
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
     * 
     * Note: This method displays all output related to processing a single inspection.
     * The controller (QualityOperatorController.processSingleInspection()) returns a
     * ProcessingResult object containing the data, and this UI method formats and
     * displays it to the user. All print statements are in this UI layer, not in the controller.
     */
    private void processSingleInspection() {
        System.out.println("\n=== PROCESS NEXT INSPECTION (USEI05) ===");

        List<Return> quarantineItems = controller.getQuarantineItems();

        if (quarantineItems.isEmpty()) {
            System.out.println("No items in quarantine queue.");
            System.out.println("Returns are automatically loaded from CSV when you enter the menu.");
            System.out.println("If the queue is empty, try option 7 to reload from CSV.");
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

        // Process the inspection through controller
        QualityOperatorController.ProcessingResult result = controller.processSingleInspection();

        if (result != null) {
            System.out.println("\nUSEI05 Processing Logic:");
            System.out.println("  Final Reason: " + result.getReason());
            System.out.println("  Action: " + result.getAction());

            if ("DISCARD".equals(result.getAction())) {
                System.out.println("\nDISCARDING PRODUCT");
                if ("EXPIRED".equals(result.getReason())) {
                    System.out.println("   Reason: Product expired on " + result.getItem().getExpiryDate());
                } else {
                    System.out.println("   Reason: " + result.getReason());
                }
            } else {
                System.out.println("\nRESTOCKING PRODUCT");
                System.out.println("   Reason: " + result.getReason());
                System.out.println("   Following FEFO/FIFO placement rules");
            }

            System.out.println("\nProcessing Result:");
            // Formatting moved from controller - display using System.out.print
            // Get the most recent inspection to access inspectionId and date
            List<QuarantineInspection> inspections = controller.getProcessedInspections();
            if (!inspections.isEmpty()) {
                QuarantineInspection latestInspection = inspections.get(0);
                System.out.print("Inspection Processed\n");
                System.out.print("Inspection ID: " + latestInspection.getInspectionId() + "\n");
                System.out.print("SKU: " + result.getItem().getSku() + "\n");
                System.out.print("Quantity: " + result.getItem().getQty() + "\n");
                System.out.print("Action: " + result.getAction() + "\n");
                System.out.print("Motive: " + result.getReason() + "\n");
                if (latestInspection.getInspectedAt() != null) {
                    System.out.print("Date: " + latestInspection.getInspectedAt().format(
                        java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "\n");
                }
            } else {
                // Fallback if inspection not found
                System.out.print("Inspection Processed\n");
                System.out.print("SKU: " + result.getItem().getSku() + "\n");
                System.out.print("Quantity: " + result.getItem().getQty() + "\n");
                System.out.print("Action: " + result.getAction() + "\n");
                System.out.print("Motive: " + result.getReason() + "\n");
            }
            // Check for error message
            if (result.getResultMessage() != null && result.getResultMessage().startsWith("ERROR:")) {
                System.out.print(result.getResultMessage() + "\n");
            }

            System.out.println("\n" + "=".repeat(60));
            System.out.println("INSPECTION COMPLETED");
            System.out.println("=".repeat(60));
            System.out.println("Action: " + result.getAction());
            System.out.println("Remaining items in queue: " + controller.getQuarantineItems().size());
            System.out.println("\nAudit log has been updated with this action.");
        }
    }

    /**
     * Process all inspections currently in the queue according to USEI05 requirements
     * 
     * Note: This method displays all output related to batch processing inspections.
     * The controller (QualityOperatorController.processAllInspections()) returns a
     * ProcessingSummary object containing the data, and this UI method formats and
     * displays it to the user. All print statements are in this UI layer, not in the controller.
     */
    private void processAllInspections() {
        System.out.println("\n=== PROCESS INSPECTIONS (USEI05) ===");

        List<Return> quarantineItems = controller.getQuarantineItems();

        if (quarantineItems.isEmpty()) {
            System.out.println("No items in quarantine queue.");
            System.out.println("Returns are automatically loaded from CSV when you enter the menu.");
            System.out.println("If the queue is empty, try option 7 to reload from CSV.");
            return;
        }

        int totalToProcess = quarantineItems.size();
        System.out.println("Processing " + totalToProcess + " items from quarantine queue");
        System.out.println("Following USEI05 logic: Last to arrive, first to process");

        QualityOperatorController.ProcessingSummary summary = controller.processAllInspections();

        // Display processing details for each item
        int itemNumber = 0;
        for (QualityOperatorController.ProcessingResult result : summary.getResults()) {
            itemNumber++;
            Return item = result.getItem();

            System.out.println("\n" + "=".repeat(60));
            System.out.println("Processing Item " + itemNumber + "/" + totalToProcess);
            System.out.println("=".repeat(60));
            System.out.println("Return ID: " + item.getReturnId());
            System.out.println("SKU: " + item.getSku());
            System.out.println("Quantity: " + item.getQty());
            System.out.println("Original Reason: " + item.getReason());
            System.out.println("Timestamp: " + item.getTimestamp().format(dateFormatter));
            System.out.println("Expiry Date: " + (item.getExpiryDate() != null ? item.getExpiryDate().toString() : "N/A"));

            System.out.println("\nUSEI05 Processing Logic:");
            System.out.println("  Final Reason: " + result.getReason());
            System.out.println("  Action: " + result.getAction());

            if ("DISCARD".equals(result.getAction())) {
                System.out.println("\nDISCARDING PRODUCT");
                if ("EXPIRED".equals(result.getReason())) {
                    System.out.println("   Reason: Product expired on " + item.getExpiryDate());
                } else {
                    System.out.println("   Reason: " + result.getReason());
                }
            } else {
                System.out.println("\nRESTOCKING PRODUCT");
                System.out.println("   Reason: " + result.getReason());
                System.out.println("   Following FEFO/FIFO placement rules");
            }

            System.out.println("\nProcessing Result:");
            // Formatting moved from controller - display using System.out.print
            // Get the inspection for this specific item
            // Inspections are added in processing order (most recent first), so we can use itemNumber-1 as index
            List<QuarantineInspection> allInspections = controller.getProcessedInspections();
            QuarantineInspection matchingInspection = null;
            if (!allInspections.isEmpty() && itemNumber <= allInspections.size()) {
                // Use the inspection at index (itemNumber-1) since inspections are in processing order
                // (first processed = index 0, second = index 1, etc.)
                matchingInspection = allInspections.get(itemNumber - 1);
            }
            if (matchingInspection != null) {
                System.out.print("Inspection Processed\n");
                System.out.print("Inspection ID: " + matchingInspection.getInspectionId() + "\n");
                System.out.print("SKU: " + item.getSku() + "\n");
                System.out.print("Quantity: " + item.getQty() + "\n");
                System.out.print("Action: " + result.getAction() + "\n");
                System.out.print("Motive: " + result.getReason() + "\n");
                if (matchingInspection.getInspectedAt() != null) {
                    System.out.print("Date: " + matchingInspection.getInspectedAt().format(
                        java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "\n");
                }
            } else {
                // Fallback if inspection not found
                System.out.print("Inspection Processed\n");
                System.out.print("SKU: " + item.getSku() + "\n");
                System.out.print("Quantity: " + item.getQty() + "\n");
                System.out.print("Action: " + result.getAction() + "\n");
                System.out.print("Motive: " + result.getReason() + "\n");
            }
            // Check for error message
            if (result.getResultMessage() != null && result.getResultMessage().startsWith("ERROR:")) {
                System.out.print(result.getResultMessage() + "\n");
            }
        }

        System.out.println("\n" + "=".repeat(60));
        System.out.println("ALL INSPECTIONS COMPLETED");
        System.out.println("=".repeat(60));
        System.out.println("Total Processed: " + summary.getProcessedCount());
        System.out.println("Discarded: " + summary.getDiscardedCount());
        System.out.println("Restocked: " + summary.getRestockedCount());
        System.out.println("Quarantine queue is now empty.");
        System.out.println("\nAudit log has been updated with all actions.");
    }

    /**
     * View processed inspections
     */
    private void viewProcessedInspections() {
        System.out.println("\n=== PROCESSED INSPECTIONS ===");

        List<QuarantineInspection> inspections = controller.getProcessedInspections();

        if (inspections.isEmpty()) {
            System.out.println("No processed inspections yet.");
            return;
        }

        // Use formatted table style
        String[] cols = {"InspectionID", "ReturnID", "Action", "InspectedAt"};
        int[] widths = {16, 12, 10, 24};
        printTableHeader("Processed Inspections", cols, widths);
        
        for (QuarantineInspection i : inspections) {
            printRow(widths,
                    safe(i.getInspectionId()),
                    safe(i.getReturnId()),
                    i.getAction() == null ? "-" : i.getAction().name(),
                    i.getInspectedAt() == null ? "-" : i.getInspectedAt().format(dateFormatter));
        }
        printTableFooter(widths);
    }

    /**
     * Reload returns from CSV with detailed feedback
     * 
     * Note: This method displays all output related to loading returns from CSV.
     * The controller (QualityOperatorController.loadReturnsFromCSV()) returns a
     * LoadResult object containing success status, counts, warnings, and errors.
     * This UI method formats and displays all that information. All print statements
     * are in this UI layer, not in the controller.
     */
    private void reloadReturnsFromCSV() {
        System.out.println("\n=== LOAD RETURNS FROM CSV ===");

        QualityOperatorController.LoadResult loadResult = controller.loadReturnsFromCSV(true);

        if (!loadResult.isSuccess()) {
            System.out.println(loadResult.getErrorMessage());
            if (loadResult.getCsvPath() != null) {
                System.out.println("Please ensure the file exists and try again.");
            }
            return;
        }

        if (loadResult.getCsvPath() != null) {
            System.out.println("Loading returns from: " + loadResult.getCsvPath());
        }

        // Display warnings if any
        for (String warning : loadResult.getWarnings()) {
            System.out.println(warning);
        }

        System.out.println("SUCCESS: Successfully loaded " + loadResult.getLoadedCount() + " returns from CSV");
        System.out.println("Returns added to quarantine queue and sorted by timestamp DESC, returnId ASC");

        List<Return> items = controller.getQuarantineItems();
        if (!items.isEmpty()) {
            System.out.println("\nFirst few returns loaded:");
            int displayCount = Math.min(5, items.size());
            for (int i = 0; i < displayCount; i++) {
                Return r = items.get(i);
                System.out.printf("  %s - %s (Qty: %d, Reason: %s, Time: %s)%n",
                        r.getReturnId(), r.getSku(), r.getQty(), r.getReason(),
                        r.getTimestamp().format(dateFormatter));
            }
            if (items.size() > 5) {
                System.out.println("  ... and " + (items.size() - 5) + " more");
            }
        }
    }

    /**
     * View audit log
     */
    private void viewAuditLog() {
        System.out.println("\n=== AUDIT LOG ===");

        List<String> auditLogs = controller.getAuditLogs();

        if (auditLogs.isEmpty()) {
            System.out.println("No audit logs available.");
            return;
        }

        // Use formatted table style
        String[] cols = {"Timestamp", "ReturnId", "SKU", "Action", "Qty"};
        int[] widths = {17, 12, 10, 10, 6};
        printTableHeader("Audit Log", cols, widths);
        
        for (String l : auditLogs) {
            String ts = "-", returnId = "-", sku = "-", action = "-", qty = "-";
            String[] parts = l.split("\\|");
            if (parts.length >= 1) {
                ts = parts[0].trim();
                for (int i = 1; i < parts.length; i++) {
                    String p = parts[i].trim();
                    int eq = p.indexOf('=');
                    if (eq > 0) {
                        String key = p.substring(0, eq).trim();
                        String val = p.substring(eq + 1).trim();
                        String k = key.toLowerCase();
                        if ("returnid".equals(k)) returnId = val;
                        else if ("sku".equals(k)) sku = val;
                        else if ("action".equals(k)) action = val;
                        else if ("qty".equals(k) || "quantity".equals(k)) qty = val;
                    }
                }
            }
            printRow(widths, ts, returnId, sku, action, qty);
        }
        printTableFooter(widths);
    }

    // -----------------------
    // Table formatting helpers (from ReturnsUI)
    // -----------------------

    private void printTableHeader(String title, String[] cols, int[] widths) {
        String border = borderTop(widths);
        println(border);
        println(titleRow(title, widths));
        println(borderSep(widths));
        printRow(widths, cols);
        println(borderSep(widths));
    }

    private void printTableFooter(int[] widths) {
        println(borderBottom(widths));
    }

    private void printRow(int[] widths, String... values) {
        StringBuilder sb = new StringBuilder();
        sb.append('│');
        for (int i = 0; i < widths.length; i++) {
            String v = i < values.length ? values[i] : "";
            sb.append(" ").append(padRight(v, widths[i])).append(" │");
        }
        println(sb.toString());
    }

    private void printEmptyRow(int[] widths, String message) {
        StringBuilder sb = new StringBuilder();
        sb.append('│');
        int total = 0;
        for (int w : widths) total += (w + 3);
        sb.append(" ").append(padRight(message, total - 3)).append("│");
        println(sb.toString());
    }

    private String borderTop(int[] widths) { 
        return border(widths, '┌', '┬', '┐'); 
    }
    
    private String borderSep(int[] widths) { 
        return border(widths, '├', '┼', '┤'); 
    }
    
    private String borderBottom(int[] widths) { 
        return border(widths, '└', '┴', '┘'); 
    }

    private String border(int[] widths, char left, char mid, char right) {
        StringBuilder sb = new StringBuilder();
        sb.append(left);
        for (int i = 0; i < widths.length; i++) {
            sb.append(repeat('─', widths[i] + 2));
            sb.append(i == widths.length - 1 ? right : mid);
        }
        return sb.toString();
    }

    private String titleRow(String title, int[] widths) {
        int total = 0; 
        for (int w : widths) total += (w + 3); 
        total += 1; // leading │
        String centered = center(title, total - 2);
        return new StringBuilder().append('│').append(centered).append('│').toString();
    }

    private String safe(String v) {
        return v == null ? "-" : v;
    }

    private String repeat(char c, int n) {
        StringBuilder sb = new StringBuilder(n);
        for (int i = 0; i < n; i++) sb.append(c);
        return sb.toString();
    }

    private String padRight(String s, int n) {
        if (s == null) s = "";
        if (s.length() > n) return s.substring(0, n);
        StringBuilder sb = new StringBuilder(n);
        sb.append(s);
        while (sb.length() < n) sb.append(' ');
        return sb.toString();
    }

    private String center(String s, int n) {
        if (s == null) s = "";
        if (s.length() >= n) return s.substring(0, n);
        int left = (n - s.length()) / 2;
        int right = n - s.length() - left;
        StringBuilder sb = new StringBuilder(n);
        sb.append(repeat(' ', left));
        sb.append(s);
        sb.append(repeat(' ', right));
        return sb.toString();
    }

    private void println() { 
        System.out.println(); 
    }
    
    private void println(String s) { 
        System.out.println(s); 
    }

}
