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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Controller for Quality Operator functionality (USEI05)
 * 
 * Handles business logic for:
 * - Loading returns from CSV file into quarantine queue
 * - Managing quarantine queue (sorted by timestamp DESC, returnId ASC)
 * - Processing inspections (single or batch)
 * - Determining processing actions based on return reasons and expiry dates
 * - Accessing processed inspections and audit logs
 */
public class QualityOperatorController {

    private InspectionController inspectionController;
    private List<Return> quarantineItems = new ArrayList<>();

    /**
     * Instantiates a new Quality operator controller.
     */
    public QualityOperatorController() {
        this.inspectionController = new InspectionController();
    }

    /**
     * Instantiates a new Quality operator controller with warehouse manager.
     *
     * @param warehouseManager the warehouse manager for inventory operations
     */
    public QualityOperatorController(Domain.WarehouseManager warehouseManager) {
        this.inspectionController = new InspectionController(warehouseManager);
    }

    /**
     * Result of processing a single inspection
     */
    public static class ProcessingResult {
        private final Return item;
        private final String reason;
        private final String action;
        private final String resultMessage;

        public ProcessingResult(Return item, String reason, String action, String resultMessage) {
            this.item = item;
            this.reason = reason;
            this.action = action;
            this.resultMessage = resultMessage;
        }

        public Return getItem() {
            return item;
        }

        public String getReason() {
            return reason;
        }

        public String getAction() {
            return action;
        }

        public String getResultMessage() {
            return resultMessage;
        }
    }

    /**
     * Summary of processing all inspections
     */
    public static class ProcessingSummary {
        private final int processedCount;
        private final int discardedCount;
        private final int restockedCount;
        private final List<ProcessingResult> results;

        public ProcessingSummary(int processedCount, int discardedCount, int restockedCount, 
                                List<ProcessingResult> results) {
            this.processedCount = processedCount;
            this.discardedCount = discardedCount;
            this.restockedCount = restockedCount;
            this.results = results;
        }

        public int getProcessedCount() {
            return processedCount;
        }

        public int getDiscardedCount() {
            return discardedCount;
        }

        public int getRestockedCount() {
            return restockedCount;
        }

        public List<ProcessingResult> getResults() {
            return results;
        }
    }

    /**
     * Result of loading returns from CSV
     */
    public static class LoadResult {
        private final boolean success;
        private final int loadedCount;
        private final String errorMessage;
        private final List<String> warnings;
        private final String csvPath;

        public LoadResult(boolean success, int loadedCount, String errorMessage) {
            this(success, loadedCount, errorMessage, new ArrayList<>(), null);
        }

        public LoadResult(boolean success, int loadedCount, String errorMessage, 
                         List<String> warnings, String csvPath) {
            this.success = success;
            this.loadedCount = loadedCount;
            this.errorMessage = errorMessage;
            this.warnings = warnings != null ? warnings : new ArrayList<>();
            this.csvPath = csvPath;
        }

        public boolean isSuccess() {
            return success;
        }

        public int getLoadedCount() {
            return loadedCount;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public List<String> getWarnings() {
            return warnings;
        }

        public String getCsvPath() {
            return csvPath;
        }
    }

    /**
     * Load returns from CSV file according to USEI05 requirements
     * 
     * Automatically sorts queue: timestamp DESC, returnId ASC (latest first)
     * 
     * @return LoadResult containing success status and loaded count
     */
    public LoadResult loadReturnsFromCSV() {
        return loadReturnsFromCSV(false);
    }

    /**
     * Load returns from CSV file according to USEI05 requirements
     * 
     * @param showDetails whether to include detailed warnings in result (for UI display)
     * @return LoadResult containing success status, loaded count, warnings, and error messages
     */
    public LoadResult loadReturnsFromCSV(boolean showDetails) {
        Path csvPath = Paths.get("train_station_dataset", "returns.csv");

        if (!Files.exists(csvPath)) {
            return new LoadResult(false, 0, "ERROR: Returns CSV file not found at: " + csvPath);
        }

        List<Return> loadedReturns = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

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

                // Preserve trailing empty columns (important for lines where expiry_date is empty)
                String[] parts = line.split(",", -1);
                if (parts.length < 6) {
                    warnings.add("WARNING: Line " + lineNumber + " has insufficient columns, skipping...");
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
                    java.time.LocalDateTime timestamp = java.time.LocalDateTime.parse(timestampStr);

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
                    warnings.add("WARNING: Error parsing line " + lineNumber + ": " + e.getMessage());
                }
            }

            // Clear existing quarantine items and add loaded returns
            quarantineItems.clear();
            quarantineItems.addAll(loadedReturns);

            // Sort according to USEI05 requirements: timestamp DESC, returnId ASC
            sortQuarantineQueue();

            return new LoadResult(true, loadedReturns.size(), null, warnings, csvPath.toString());

        } catch (IOException e) {
            return new LoadResult(false, 0, "ERROR: Error reading CSV file: " + e.getMessage());
        }
    }

    /**
     * Get the quarantine items list (defensive copy)
     * 
     * @return list of returns in quarantine queue
     */
    public List<Return> getQuarantineItems() {
        return new ArrayList<>(quarantineItems);
    }

    /**
     * Process single inspection from quarantine queue
     * 
     * Processes the first item in queue (latest arrival) according to USEI05:
     * - Checks if expired (overrides original reason)
     * - Determines action (RESTOCK/DISCARD) based on reason
     * - Processes through InspectionController
     * - Removes item from queue
     * 
     * @return ProcessingResult with processing details, or null if queue is empty
     */
    public ProcessingResult processSingleInspection() {
        if (quarantineItems.isEmpty()) {
            return null;
        }

        Return item = quarantineItems.get(0);

        // Determine action based on USEI05 logic
        String reasonToUse = determineProcessingReason(item);
        String action = determineActionFromReason(reasonToUse);

        // Process the inspection
        String resultMessage = inspectionController.processQuarantine(
                item.getSku(),
                item.getQty(),
                reasonToUse,
                item.getExpiryDate()
        );

        // Remove processed item
        quarantineItems.remove(0);

        return new ProcessingResult(item, reasonToUse, action, resultMessage);
    }

    /**
     * Process all inspections currently in the queue according to USEI05 requirements
     * 
     * Processes all items in queue (latest first) and returns summary statistics.
     * 
     * @return ProcessingSummary with counts of processed items
     */
    public ProcessingSummary processAllInspections() {
        int processedCount = 0;
        int discardedCount = 0;
        int restockedCount = 0;
        List<ProcessingResult> results = new ArrayList<>();

        while (!quarantineItems.isEmpty()) {
            Return item = quarantineItems.get(0);
            processedCount++;

            // Determine action based on USEI05 logic
            String reasonToUse = determineProcessingReason(item);
            String action = determineActionFromReason(reasonToUse);

            if ("DISCARD".equals(action)) {
                discardedCount++;
            } else {
                restockedCount++;
            }

            // Process the inspection
            String resultMessage = inspectionController.processQuarantine(
                    item.getSku(),
                    item.getQty(),
                    reasonToUse,
                    item.getExpiryDate()
            );

            results.add(new ProcessingResult(item, reasonToUse, action, resultMessage));

            // Remove processed item
            quarantineItems.remove(0);
        }

        return new ProcessingSummary(processedCount, discardedCount, restockedCount, results);
    }

    /**
     * Get processed inspections
     * 
     * @return list of all processed quarantine inspections
     */
    public List<QuarantineInspection> getProcessedInspections() {
        return inspectionController.getInspectionsBySku("");
    }

    /**
     * Get audit logs
     * 
     * @return list of audit log entries
     */
    public List<String> getAuditLogs() {
        return inspectionController.getAuditLogs();
    }

    // ==================== Private Helper Methods ====================

    /**
     * Map CSV return reasons to system reasons according to USEI05 logic
     * 
     * @param csvReason the reason from CSV file
     * @return mapped system reason (REMORSE, DAMAGED, EXPIRED, CYCLE_COUNT)
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
     * Determine the final processing reason for a return item
     * 
     * According to USEI05: if product is expired, override original reason with EXPIRED.
     * 
     * @param item the return item
     * @return final reason to use for processing
     */
    private String determineProcessingReason(Return item) {
        // Check if product is expired first (overrides original reason)
        if (item.getExpiryDate() != null && item.getExpiryDate().isBefore(LocalDate.now())) {
            return "EXPIRED";
        }

        // Use original reason if not expired
        return item.getReason();
    }

    /**
     * Determine action based on reason according to USEI05 logic
     * 
     * - REMORSE, CYCLE_COUNT -> RESTOCK
     * - DAMAGED, EXPIRED -> DISCARD
     * - Unknown -> DISCARD (safety default)
     * 
     * @param reason the processing reason
     * @return action string ("RESTOCK" or "DISCARD")
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
     * Sort quarantine queue according to USEI05 requirements:
     * - Latest first (timestamp DESC)
     * - Ties broken by returnId ASC
     * 
     * This ensures items are processed in reverse arrival order (last to arrive, first to process).
     */
    private void sortQuarantineQueue() {
        quarantineItems.sort(Comparator
                .comparing(Return::getTimestamp)
                .reversed()  // Latest first (DESC)
                .thenComparing(Return::getReturnId)  // Ties by returnId ASC
        );
    }

}
