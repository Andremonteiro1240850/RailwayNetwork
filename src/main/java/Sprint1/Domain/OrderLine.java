package Sprint1.Domain;

/**
 * The type Order line.
 */
public class OrderLine {
    private final int lineNo;
    private final String sku;
    private final int requestedQty;
    private int allocatedQty;
    private String status; // "ELIGIBLE", "PARTIAL", "UNDISPATCHABLE"

    /**
     * Instantiates a new Order line.
     *
     * @param lineNo       the line no
     * @param sku          the sku
     * @param requestedQty the requested qty
     */
    public OrderLine(int lineNo, String sku, int requestedQty) {
        this.lineNo = lineNo;
        this.sku = sku;
        this.requestedQty = requestedQty;
        this.allocatedQty = 0;
        this.status = "ELIGIBLE";
    }

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
    public String getStatus() { return status; }

    /**
     * Sets allocated qty.
     *
     * @param allocatedQty the allocated qty
     */
    public void setAllocatedQty(int allocatedQty) { this.allocatedQty = allocatedQty; }

    /**
     * Sets status.
     *
     * @param status the status
     */
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
