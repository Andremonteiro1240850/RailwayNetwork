package Sprint1.Domain;

/**
 * The type Allocation row.
 */
public class AllocationRow {
    private String orderId;
    private int lineNo;
    private String sku;
    private int qty;
    private String boxId;
    private int aisleID;
    private int bayID;

    /**
     * Instantiates a new Allocation row.
     *
     * @param orderId the order id
     * @param lineNo  the line no
     * @param sku     the sku
     * @param qty     the qty
     * @param boxId   the box id
     * @param aisleID the aisle id
     * @param bayID   the bay id
     */
    public AllocationRow(String orderId, int lineNo, String sku, int qty,
                         String boxId, int aisleID, int bayID) {
        this.orderId = orderId;
        this.lineNo = lineNo;
        this.sku = sku;
        this.qty = qty;
        this.boxId = boxId;
        this.aisleID = aisleID;
        this.bayID = bayID;
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
     * Gets qty.
     *
     * @return the qty
     */
    public int getQty() { return qty; }

    /**
     * Gets box id.
     *
     * @return the box id
     */
    public String getBoxId() { return boxId; }

    /**
     * Gets aisle.
     *
     * @return the aisle
     */
    public int getAisle() { return aisleID; }

    /**
     * Gets bay.
     *
     * @return the bay
     */
    public int getBay() { return bayID; }
}