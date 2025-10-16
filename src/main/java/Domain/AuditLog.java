package Domain;

public class AuditLog {
    int id;
    int returnId;
    int timeStamp;
    int sku;
    String action;
    int qtyRestocked;
    int qtyDiscarded;

    public AuditLog(int id, int returnId, int timeStamp, int sku, String action, int qtyRestocked, int qtyDiscarded) {
        this.id = id;
        this.returnId = returnId;
        this.timeStamp = timeStamp;
        this.sku = sku;
        this.action = action;
        this.qtyRestocked = qtyRestocked;
        this.qtyDiscarded = qtyDiscarded;
    }

    public int getId() {return id;}

    public void setId(int id) {this.id = id;}

    public int getReturnId() {return returnId;}

    public void setReturnId(int returnId) {this.returnId = returnId;}

    public int getTimeStamp() {return timeStamp;}

    public void setTimeStamp(int timeStamp) {this.timeStamp = timeStamp;}

    public int getSku() {return sku;}

    public void setSku(int sku) {this.sku = sku;}

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public int getQtyRestocked() {
        return qtyRestocked;
    }

    public void setQtyRestocked(int qtyRestocked) {
        this.qtyRestocked = qtyRestocked;
    }

    public int getQtyDiscarded() {
        return qtyDiscarded;
    }

    public void setQtyDiscarded(int qtyDiscarded) {
        this.qtyDiscarded = qtyDiscarded;
    }

    @Override
    public String toString() {
        return String.format(
                "Domain.AuditLog{id=%d, returnId=%d, timeStamp=%d, sku=%d, action='%s', qtyRestocked=%d, qtyDiscarded=%d}",
                id, returnId, timeStamp, sku, action, qtyRestocked, qtyDiscarded
        );
    }

    // Static helpers to read audit log file
    // Note: the file is written by InspectionController
    private static final java.nio.file.Path AUDIT_LOG_PATH = java.nio.file.Paths.get("logs", "audit.log");

    //Reads the file and returns all lines
    public static java.util.List<String> readAll() {
        ensureFile();
        try {
            return java.nio.file.Files.readAllLines(AUDIT_LOG_PATH, java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) {
            return new java.util.ArrayList<>();
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
}
