package Domain;

public class OrderLine {
    private final int lineNo;
    private final String sku;
    private final int requestedQty;
    private int allocatedQty;
    private String status; // "ELIGIBLE", "PARTIAL", "UNDISPATCHABLE"

    public OrderLine(int lineNo, String sku, int requestedQty) {
        this.lineNo = lineNo;
        this.sku = sku;
        this.requestedQty = requestedQty;
        this.allocatedQty = 0;
        this.status = "ELIGIBLE";
    }

    public int getLineNo() { return lineNo; }
    public String getSku() { return sku; }
    public int getRequestedQty() { return requestedQty; }
    public int getAllocatedQty() { return allocatedQty; }
    public String getStatus() { return status; }

    public void setAllocatedQty(int allocatedQty) { this.allocatedQty = allocatedQty; }
    public void setStatus(String status) { this.status = status; }

    @Override
    public String toString() {
        return "OrderLine{" +
                "lineNo=" + lineNo +
                ", sku='" + sku + '\'' +
                ", requestedQty=" + requestedQty +
                ", allocatedQty=" + allocatedQty +
                ", status='" + status + '\'' +
                '}';
    }
}
