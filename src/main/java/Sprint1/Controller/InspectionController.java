package Sprint1.Controller;

import Sprint1.Domain.QuarantineInspection;
import Sprint1.Domain.AuditLog;
import Sprint1.Domain.ENUM.QAAction;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Controller for handling quarantine inspections (USEI05)
 * 
 * Manages the inspection process for returned goods:
 * - Creates inspection records
 * - Determines action (RESTOCK or DISCARD) based on return reason
 * - Processes restock (creates box and inserts into inventory following FEFO/FIFO)
 * - Processes discard
 * - Maintains audit log of all actions
 * 
 * Architecture Note:
 * This controller follows clean architecture principles - it handles business logic only
 * and does NOT contain any print statements or direct output. All user-facing output
 * is handled by the UI layer (QualityOperatorUI). This controller returns data structures
 * and formatted strings that the UI layer displays to users.
 */
public class InspectionController {

    private List<QuarantineInspection> quarantineInspections = new ArrayList<>();
    private WagonsUnloadingController wagonsUnloadingController; // for FEFO/FIFO insertion

    private static final DateTimeFormatter AUDIT_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    /**
     * Instantiates a new Inspection controller.
     */
    public InspectionController() {
    }

    /**
     * Instantiates a new Inspection controller with wagons unloading controller.
     *
     * @param wagonsUnloadingController the wagons unloading controller for inventory operations
     */
    public InspectionController(WagonsUnloadingController wagonsUnloadingController) {
        this.wagonsUnloadingController = wagonsUnloadingController;
    }

    /**
     * Process inspection for a returned item (USEI05)
     * 
     * @param sku          the SKU of the returned item
     * @param quantity     the quantity returned
     * @param returnReason the reason for return (REMORSE, DAMAGED, EXPIRED, CYCLE_COUNT)
     * @param expiryDate   the expiry date (can be null)
     * @return result message with inspection details
     */
    public String processQuarantine(String sku, int quantity, String returnReason, LocalDate expiryDate) {
        return processQuarantine(sku, quantity, returnReason, expiryDate, null, null);
    }

    /**
     * Process inspection with partial restock support (USEI05)
     * 
     * Allows splitting a return into partial restock and discard quantities.
     * 
     * @param sku          the SKU of the returned item
     * @param quantity     the total quantity returned
     * @param returnReason the reason for return (REMORSE, DAMAGED, EXPIRED, CYCLE_COUNT)
     * @param expiryDate   the expiry date (can be null)
     * @param qtyRestocked the quantity to restock (if null, uses full quantity for restock)
     * @param qtyDiscarded the quantity to discard (if null, uses full quantity for discard)
     * @return result message with inspection details
     */
    public String processQuarantine(String sku, int quantity, String returnReason, LocalDate expiryDate,
                                    Integer qtyRestocked, Integer qtyDiscarded) {
        try {
            // 1. Create inspection record
            QuarantineInspection inspection = createQuarantineInspection();

            // 2. Determine action based on return reason
            QAAction action = determineAction(returnReason);
            inspection.setAction(action);

            // 3. Process according to the action
            if (action == QAAction.RESTOCK) {
                int restockQty = (qtyRestocked != null) ? qtyRestocked : quantity;
                processRestock(inspection, sku, restockQty, expiryDate);
            } else {
                int discardQty = (qtyDiscarded != null) ? qtyDiscarded : quantity;
                processDiscard(inspection, sku, discardQty);
            }

            // 4. Add to inspection list (most recent first)
            quarantineInspections.add(0, inspection);

            // 5. Create audit log entry
            createAuditLog(inspection, sku, quantity, action, returnReason, qtyRestocked, qtyDiscarded);

            // Note: Formatting moved to UI layer - return null to indicate success
            // UI will format and display using System.out.print
            return null;

        } catch (Exception e) {
            // Note: Error message formatting moved to UI layer
            // Return error indicator - UI will format and display
            return "ERROR: " + e.getMessage();
        }
    }

    /**
     * Get all processed inspections
     * 
     * Note: The sku parameter is currently not used but kept for API compatibility.
     * All inspections are returned regardless of SKU.
     *
     * @param sku the SKU filter (currently ignored)
     * @return list of all processed quarantine inspections
     */
    public List<QuarantineInspection> getInspectionsBySku(String sku) {
        return new ArrayList<>(quarantineInspections);
    }

    /**
     * Get all audit log entries
     *
     * @return list of audit log entries as strings
     */
    public List<String> getAuditLogs() {
        return AuditLog.readAll();
    }

    // ==================== Private Helper Methods ====================

    /**
     * Creates a new inspection record with generated IDs
     */
    private QuarantineInspection createQuarantineInspection() {
        QuarantineInspection inspection = new QuarantineInspection();
        inspection.setInspectionId(generateId("INSP"));
        inspection.setReturnId(generateId("R"));
        inspection.setInspectedAt(LocalDateTime.now());
        return inspection;
    }

    /**
     * Determines action (RESTOCK or DISCARD) based on return reason
     * 
     * Logic according to USEI05:
     * - REMORSE, CYCLE_COUNT -> RESTOCK
     * - DAMAGED, EXPIRED -> DISCARD
     * - Unknown -> DISCARD (safety default)
     */
    private QAAction determineAction(String returnReason) {
        switch (returnReason.toUpperCase()) {
            case "REMORSE":
            case "CYCLE_COUNT":
                return QAAction.RESTOCK;
            case "DAMAGED":
            case "EXPIRED":
                return QAAction.DISCARD;
            default:
                return QAAction.DISCARD; // Safety default
        }
    }

    /**
     * Process restock action
     * 
     * Creates a new box with ID format "RET-{returnId}" and inserts it into inventory
     * following FEFO/FIFO placement rules (USEI01).
     * 
     * @param inspection the inspection record
     * @param sku        the SKU to restock
     * @param quantity   the quantity to restock
     * @param expiryDate the expiry date (can be null)
     */
    private void processRestock(QuarantineInspection inspection, String sku, int quantity, LocalDate expiryDate) {
        // BoxId format: "RET-" + returnId (as per USEI05 requirements)
        String boxId = String.format("RET-%s", inspection.getReturnId());
        
        // Insert the box into inventory following FEFO/FIFO placement rules
        if (wagonsUnloadingController != null) {
            wagonsUnloadingController.insertReturnedBox(boxId, sku, quantity, expiryDate, LocalDateTime.now());
        }
    }

    /**
     * Process discard action
     * 
     * Marks the item as discarded (no further processing needed).
     * 
     * @param inspection the inspection record
     * @param sku        the SKU being discarded
     * @param quantity   the quantity being discarded
     */
    private void processDiscard(QuarantineInspection inspection, String sku, int quantity) {
        // Discard action - item is flagged as no longer usable
        // No additional processing required
    }

    /**
     * Create audit log entry
     * 
     * Format: timestamp | ReturnId=... | SKU=... | Action=[Restocked|Discarded] | Quantity=...
     * 
     * For partial restocks, also includes Quantity Restocked and Quantity Discarded.
     * 
     * @param inspection    the inspection record
     * @param sku           the SKU
     * @param quantity      the total quantity
     * @param action        the action taken
     * @param returnReason  the return reason
     * @param qtyRestocked  partial restock quantity (if applicable)
     * @param qtyDiscarded  partial discard quantity (if applicable)
     */
    private void createAuditLog(QuarantineInspection inspection, String sku, int quantity,
                                QAAction action, String returnReason, Integer qtyRestocked, Integer qtyDiscarded) {
        LocalDateTime timestamp = LocalDateTime.now();
        String actionLabel = (action == QAAction.RESTOCK) ? "Restocked" : "Discarded";

        StringBuilder logLine = new StringBuilder();
        logLine.append(AUDIT_FORMATTER.format(timestamp))
                .append(" | ReturnId=").append(inspection.getReturnId())
                .append(" | SKU=").append(sku)
                .append(" | Action=").append(actionLabel)
                .append(" | Quantity=").append(quantity);

        // Add partial quantities if provided
        if (qtyRestocked != null) {
            logLine.append(" | Quantity Restocked=").append(qtyRestocked);
        }
        if (qtyDiscarded != null) {
            logLine.append(" | Quantity Discarded=").append(qtyDiscarded);
        }

        // Write to audit log file
        AuditLog.appendLine(logLine.toString());
    }

    /**
     * Generates a unique ID with the given prefix
     * 
     * @param prefix the prefix for the ID
     * @return formatted ID: "{prefix}-{8-char-UUID}"
     */
    private String generateId(String prefix) {
        return String.format("%s-%s", prefix, UUID.randomUUID().toString().substring(0, 8).toUpperCase());
    }

}
