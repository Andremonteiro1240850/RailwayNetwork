package Domain;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * The type Audit log.
 */
public class AuditLog {
    /**
     * The Id.
     */
    int id;
    /**
     * The Return id.
     */
    int returnId;
    /**
     * The Time stamp.
     */
    int timeStamp;
    /**
     * The Sku.
     */
    int sku;
    /**
     * The Action.
     */
    String action;
    /**
     * The Qty restocked.
     */
    int qtyRestocked;
    /**
     * The Qty discarded.
     */
    int qtyDiscarded;

    // Static helpers to read audit log file
    // Note: the file is written by InspectionController
    private static Path AUDIT_LOG_PATH = Paths.get("train_station_dataset", "audit.log");

    /**
     * Instantiates a new Audit log.
     *
     * @param id           the id
     * @param returnId     the return id
     * @param timeStamp    the time stamp
     * @param sku          the sku
     * @param action       the action
     * @param qtyRestocked the qty restocked
     * @param qtyDiscarded the qty discarded
     */
    public AuditLog(int id, int returnId, int timeStamp, int sku, String action, int qtyRestocked, int qtyDiscarded) {
        this.id = id;
        this.returnId = returnId;
        this.timeStamp = timeStamp;
        this.sku = sku;
        this.action = action;
        this.qtyRestocked = qtyRestocked;
        this.qtyDiscarded = qtyDiscarded;
    }

    /**
     * FOR TESTING ONLY - Set audit log path
     *
     * @param testPath the test path to use
     */
    public static void setAuditLogPath(Path testPath) {
        AUDIT_LOG_PATH = testPath;
    }

    /**
     * Gets id.
     *
     * @return the id
     */
    public int getId() {return id;}

    /**
     * Sets id.
     *
     * @param id the id
     */
    public void setId(int id) {this.id = id;}

    /**
     * Gets return id.
     *
     * @return the return id
     */
    public int getReturnId() {return returnId;}

    /**
     * Sets return id.
     *
     * @param returnId the return id
     */
    public void setReturnId(int returnId) {this.returnId = returnId;}

    /**
     * Gets time stamp.
     *
     * @return the time stamp
     */
    public int getTimeStamp() {return timeStamp;}

    /**
     * Sets time stamp.
     *
     * @param timeStamp the time stamp
     */
    public void setTimeStamp(int timeStamp) {this.timeStamp = timeStamp;}

    /**
     * Gets sku.
     *
     * @return the sku
     */
    public int getSku() {return sku;}

    /**
     * Sets sku.
     *
     * @param sku the sku
     */
    public void setSku(int sku) {this.sku = sku;}

    /**
     * Gets action.
     *
     * @return the action
     */
    public String getAction() {
        return action;
    }

    /**
     * Sets action.
     *
     * @param action the action
     */
    public void setAction(String action) {
        this.action = action;
    }

    /**
     * Gets qty restocked.
     *
     * @return the qty restocked
     */
    public int getQtyRestocked() {
        return qtyRestocked;
    }

    /**
     * Sets qty restocked.
     *
     * @param qtyRestocked the qty restocked
     */
    public void setQtyRestocked(int qtyRestocked) {
        this.qtyRestocked = qtyRestocked;
    }

    /**
     * Gets qty discarded.
     *
     * @return the qty discarded
     */
    public int getQtyDiscarded() {
        return qtyDiscarded;
    }

    /**
     * Sets qty discarded.
     *
     * @param qtyDiscarded the qty discarded
     */
    public void setQtyDiscarded(int qtyDiscarded) {
        this.qtyDiscarded = qtyDiscarded;
    }

    @Override
    public String toString() {
        return "AuditLog{" +
                "id=" + id +
                ", returnId=" + returnId +
                ", timeStamp=" + timeStamp +
                ", sku=" + sku +
                ", action='" + action + '\'' +
                ", qtyRestocked=" + qtyRestocked +
                ", qtyDiscarded=" + qtyDiscarded +
                '}';
    }

    /**
     * Read all java . util . list.
     *
     * @return the java . util . list
     */
//Reads the file and returns all lines
    public static List<String> readAll() {
        ensureFile();
        try {
            return java.nio.file.Files.readAllLines(AUDIT_LOG_PATH, java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    //Creates the file if it doesn't exist
    private static void ensureFile() {
        try {
            if (!java.nio.file.Files.exists(AUDIT_LOG_PATH)) {
                java.nio.file.Path parent = AUDIT_LOG_PATH.toAbsolutePath().getParent();
                if (parent != null) java.nio.file.Files.createDirectories(parent);
                java.nio.file.Files.write(AUDIT_LOG_PATH, new byte[0], java.nio.file.StandardOpenOption.CREATE);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize audit log file", e);
        }
    }

    /**
     * Clear all audit log entries
     * This method clears the audit log file, removing all previous entries
     */
    public static void clearAll() {
        try {
            ensureFile();
            java.nio.file.Files.write(AUDIT_LOG_PATH, new byte[0], java.nio.file.StandardOpenOption.TRUNCATE_EXISTING);
        } catch (Exception e) {
            throw new RuntimeException("Failed to clear audit log file", e);
        }
    }

    /**
     * Append a line to the audit log
     * This method appends a new line to the audit log file
     *
     * @param line the line
     */
    public static void appendLine(String line) {
        try {
            ensureFile();
            java.nio.file.Files.write(AUDIT_LOG_PATH, (line + System.lineSeparator()).getBytes(java.nio.charset.StandardCharsets.UTF_8),
                    java.nio.file.StandardOpenOption.APPEND);
        } catch (Exception e) {
            throw new RuntimeException("Failed to append audit log entry", e);
        }
    }
}