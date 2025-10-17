package Domain;

import Domain.ENUM.AllocationStatus;

public class EligibilityResult {
    private String orderId;
    private int lineNo;
    private String sku;
    private int requestedQty;
    private int allocatedQty;
    private AllocationStatus status;

    public EligibilityResult(String orderId, int lineNo, String sku,
                             int requestedQty, int allocatedQty, AllocationStatus status) {
        this.orderId = orderId;
        this.lineNo = lineNo;
        this.sku = sku;
        this.requestedQty = requestedQty;
        this.allocatedQty = allocatedQty;
        this.status = status;
    }

    // Getters
    public String getOrderId() { return orderId; }
    public int getLineNo() { return lineNo; }
    public String getSku() { return sku; }
    public int getRequestedQty() { return requestedQty; }
    public int getAllocatedQty() { return allocatedQty; }
    public AllocationStatus getStatus() { return status; }
}
