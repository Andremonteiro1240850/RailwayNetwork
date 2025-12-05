package Domain;

import Domain.ENUM.AllocationStatus;

/**
 * The type Eligibility result.
 */
public class EligibilityResult {
    private String orderId;
    private int lineNo;
    private String sku;
    private int requestedQty;
    private int allocatedQty;
    private AllocationStatus status;

    /**
     * Instantiates a new Eligibility result.
     *
     * @param orderId      the order id
     * @param lineNo       the line no
     * @param sku          the sku
     * @param requestedQty the requested qty
     * @param allocatedQty the allocated qty
     * @param status       the status
     */
    public EligibilityResult(String orderId, int lineNo, String sku,
                             int requestedQty, int allocatedQty, AllocationStatus status) {
        this.orderId = orderId;
        this.lineNo = lineNo;
        this.sku = sku;
        this.requestedQty = requestedQty;
        this.allocatedQty = allocatedQty;
        this.status = status;
    }

    /**
     * Gets order id.
     *
     * @return the order id
     */
// Getters
    public String getOrderId() { return orderId; }

    /**
     * Gets line no.
     *
     * @return the line no
     */
    public int getLineNo() { return lineNo; }

    /**
     * Gets sku.
     *
     * @return the sku
     */
    public String getSku() { return sku; }

    /**
     * Gets requested qty.
     *
     * @return the requested qty
     */
    public int getRequestedQty() { return requestedQty; }

    /**
     * Gets allocated qty.
     *
     * @return the allocated qty
     */
    public int getAllocatedQty() { return allocatedQty; }

    /**
     * Gets status.
     *
     * @return the status
     */
    public AllocationStatus getStatus() { return status; }
}
