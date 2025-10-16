package Controller;

import Domain.QuarantineInspection;
import java.time.LocalDate;
import java.util.*;


public class ItemController {

    InspectionController inspectionController;

    public ItemController(InspectionController inspectionController) {
        this.inspectionController = inspectionController;
    }

    /**
     * US05 - Process a return and forward it to quarantine
     */
    public String processReturn(String sku, int quantity, String returnReason, LocalDate expiryDate) {
        return processReturn(sku, quantity, returnReason, expiryDate, null, null);
    }

    /**
     * US05 - Process a return with partial restock support
     */
    public String processReturn(String sku, int quantity, String returnReason, LocalDate expiryDate, 
                               Integer qtyRestocked, Integer qtyDiscarded) {
        try {
            // Validation
            if (sku == null || sku.trim().isEmpty()) {
                return "ERROR: SKU can't be null or empty";
            }

            if (quantity <= 0) {
                return "ERROR: Quantity must be greater than 0";
            }

            if (returnReason == null || returnReason.trim().isEmpty()) {
                return "ERROR: Return reason can't be null or empty";
            }

            // Validates permitted motives
            List<String> validReasons = Arrays.asList("REMORSE", "DAMAGED", "EXPIRED", "CYCLE COUNT");
            if (!validReasons.contains(returnReason.toUpperCase())) {
                return "ERROR: Invalid Motive. Use: REMORSE, DAMAGED, EXPIRED ou CYCLE COUNT";
            }

            // Forwards to quarantine
            String result = inspectionController.processQuarantine(sku, quantity, returnReason, expiryDate, qtyRestocked, qtyDiscarded);

            return result;

        } catch (Exception e) {
            return String.format("ERROR processing return: %s", e.getMessage());
        }
    }

    /**
     * List returns by SKU
     */
    public List<QuarantineInspection> getReturnsBySku(String sku) {
        return inspectionController.getInspectionsBySku(sku);
    }
}