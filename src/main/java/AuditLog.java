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
}
