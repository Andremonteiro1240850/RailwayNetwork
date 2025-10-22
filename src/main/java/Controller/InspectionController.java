package Controller;

import Domain.QuarantineInspection;
import Domain.ENUM.QAAction;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * The type Inspection controller.
 */
public class InspectionController {

    private List<QuarantineInspection> quarantineInspections = new ArrayList<>();
    private Domain.WarehouseManager warehouseManager; // for FEFO/FIFO insertion

    /**
     * Instantiates a new Inspection controller.
     */
    public InspectionController() {
    }

    /**
     * Instantiates a new Inspection controller.
     *
     * @param warehouseManager the warehouse manager
     */
    public InspectionController(Domain.WarehouseManager warehouseManager) {
        this.warehouseManager = warehouseManager;
    }

    /**
     * Sets warehouse manager.
     *
     * @param warehouseManager the warehouse manager
     */
    public void setWarehouseManager(Domain.WarehouseManager warehouseManager) {
        this.warehouseManager = warehouseManager;
    }
    private final Path auditLogPath = Paths.get("logs", "audit.log");
    private final DateTimeFormatter auditFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    /**
     * US05 - Process inspection
     *
     * @param sku          the sku
     * @param quantity     the quantity
     * @param returnReason the return reason
     * @param expiryDate   the expiry date
     * @return the string
     */
    public String processQuarantine(String sku, int quantity, String returnReason, LocalDate expiryDate) {
        return processQuarantine(sku, quantity, returnReason, expiryDate, null, null);
    }

    /**
     * US05 - Process inspection with partial restock support
     *
     * @param sku          the sku
     * @param quantity     the quantity
     * @param returnReason the return reason
     * @param expiryDate   the expiry date
     * @param qtyRestocked the qty restocked
     * @param qtyDiscarded the qty discarded
     * @return the string
     */
    public String processQuarantine(String sku, int quantity, String returnReason, LocalDate expiryDate,
                                    Integer qtyRestocked, Integer qtyDiscarded) {
        try {
            // 1. Created inspection
            QuarantineInspection inspection = createQuarantineInspection();

            // 2. Determines action based on motive
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

            // 4. Add to inspection list (reverse order - most recent first)
            quarantineInspections.add(0, inspection);

            // 5. Creates audit log
            createAuditLog(inspection, sku, quantity, action, returnReason, qtyRestocked, qtyDiscarded);

            return String.format(
                    "Inspection Processed\nInspection ID: %s\nSKU: %s\nQuantity: %d\nAction: %s\nMotive: %s\nDate: %s",
                    inspection.getInspectionId(), sku, quantity, action, returnReason, 
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
            );

        } catch (Exception e) {
            return String.format("RROR: Inspection error. %s", e.getMessage());
        }
    }

    /**
     * Creates new inspection
     */
    private QuarantineInspection createQuarantineInspection() {
        QuarantineInspection inspection = new QuarantineInspection();
        inspection.setInspectionId(generateId("INSP"));
        inspection.setReturnId(generateId("R"));
        inspection.setInspectedAt(LocalDateTime.now());
        return inspection;
    }

    /**
     * Determines action based on reason
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
                return QAAction.DISCARD;
        }
    }

    /**
     * Process restock - create new box for inventory
     */
    private void processRestock(QuarantineInspection inspection, String sku, int quantity, LocalDate expiryDate) {
        // BoxId conforme imagem: "RET-" + returnId
        String boxId = String.format("RET-%s", inspection.getReturnId());
        System.out.println(String.format("RESTOCK: Creating box %s for SKU %s, quantity: %d", boxId, sku, quantity));
        // Insert the box into inventory following FEFO/FIFO placement rules
        if (warehouseManager != null) {
            warehouseManager.insertReturnedBox(boxId, sku, quantity, expiryDate, LocalDateTime.now());
        }
    }

    /**
     * Process discard
     */
    private void processDiscard(QuarantineInspection inspection, String sku, int quantity) {
        System.out.println(String.format("DISCARD: Discarding SKU %s, quantity: %d", sku, quantity));
    }

    /**
     * Create audit log
     */
    private void createAuditLog(QuarantineInspection inspection, String sku, int quantity,
                                QAAction action, String returnReason, Integer qtyRestocked, Integer qtyDiscarded) {
        LocalDateTime ts = LocalDateTime.now();
        String actionLabel = (action == QAAction.RESTOCK) ? "Restocked" : "Discarded";

        StringBuilder line = new StringBuilder();
        line.append(auditFormatter.format(ts))
                .append(" | ReturnId=").append(inspection.getReturnId())
                .append(" | SKU=").append(sku)
                .append(" | Action=").append(actionLabel)
                .append(" | Quantity=").append(quantity);

        // Add partial quantities if provided
        if (qtyRestocked != null) {
            line.append(" | Quantity Restocked=").append(qtyRestocked);
        }
        if (qtyDiscarded != null) {
            line.append(" | Quantity Discarded=").append(qtyDiscarded);
        }

        appendAuditLine(line.append(System.lineSeparator()).toString());
    }

    /**
     * Generates unique ID
     */
    private String generateId(String prefix) {
        return String.format("%s-%s", prefix, UUID.randomUUID().toString().substring(0, 8).toUpperCase());
    }

    /**
     * Get inspections by SKU
     *
     * @param sku the sku
     * @return the inspections by sku
     */
    public List<QuarantineInspection> getInspectionsBySku(String sku) {
        return new ArrayList<>(quarantineInspections);
    }

    /**
     * Get audit log
     *
     * @return the audit logs
     */
    public List<String> getAuditLogs() {
        return Domain.AuditLog.readAll();
    }

    private void appendAuditLine(String content) {
        ensureAuditFile();
        try {
            Files.write(auditLogPath, content.getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
        } catch (Exception e) {
            throw new RuntimeException("Failed to write audit log entry", e);
        }
    }

    private void ensureAuditFile() {
        try {
            if (!Files.exists(auditLogPath)) {
                Path parent = auditLogPath.toAbsolutePath().getParent();
                if (parent != null) Files.createDirectories(parent);
                Files.write(auditLogPath, new byte[0], StandardOpenOption.CREATE);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize audit log file", e);
        }
    }
}