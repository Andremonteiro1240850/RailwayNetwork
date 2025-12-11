package Sprint1.Domain;

/**
 * The type Picking items.
 */
public class PickingItems {
    /**
     * The Order id.
     */
    String orderId;
    /**
     * The Line no.
     */
    int lineNo;
    /**
     * The Aisle id.
     */
    int aisleId;
    /**
     * The Bay id.
     */
    int bayId;
    /**
     * The Box id.
     */
    String boxId;
    /**
     * The Sku.
     */
    String sku;
    /**
     * The Qty.
     */
    int qty;

    /**
     * Instantiates a new Picking items.
     *
     * @param orderId the order id
     * @param lineNo  the line no
     * @param aisleId the aisle id
     * @param bayId   the bay id
     * @param boxId   the box id
     * @param sku     the sku
     * @param qty     the qty
     */
    public PickingItems(String orderId, int lineNo, int aisleId, int bayId, String boxId, String sku, int qty) {
        this.orderId = orderId;
        this.lineNo = lineNo;
        this.aisleId = aisleId;
        this.bayId = bayId;
        this.boxId = boxId;
        this.sku = sku;
        this.qty = qty;
    }

    /**
     * Gets order id.
     *
     * @return the order id
     */
    public String getOrderId() {
        return orderId;
    }

    /**
     * Gets line no.
     *
     * @return the line no
     */
    public int getLineNo() {
        return lineNo;
    }

    /**
     * Gets aisle id.
     *
     * @return the aisle id
     */
    public int getAisleId() {
        return aisleId;
    }

    /**
     * Gets bay id.
     *
     * @return the bay id
     */
    public int getBayId() {
        return bayId;
    }

    /**
     * Gets box id.
     *
     * @return the box id
     */
    public String getBoxId() {
        return boxId;
    }

    /**
     * Gets sku.
     *
     * @return the sku
     */
    public String getSku() {
        return sku;
    }

    /**
     * Gets qty.
     *
     * @return the qty
     */
    public int getQty() {
        return qty;
    }

    /**
     * Sets order id.
     *
     * @param orderId the order id
     */
    public void  setOrderId(String orderId) {
        this.orderId = orderId;
    }

    /**
     * Sets line no.
     *
     * @param lineNo the line no
     */
    public void setLineNo(int lineNo) {
        this.lineNo = lineNo;
    }

    /**
     * Sets aisle id.
     *
     * @param aisleId the aisle id
     */
    public void setAisleId(int aisleId) {
        this.aisleId = aisleId;
    }

    /**
     * Sets bay id.
     *
     * @param bayId the bay id
     */
    public void setBayId(int bayId) {
        this.bayId = bayId;
    }

    /**
     * Sets box id.
     *
     * @param boxId the box id
     */
    public void setBoxId(String boxId) {
        this.boxId = boxId;
    }

    /**
     * Sets sku.
     *
     * @param sku the sku
     */
    public void setSku(String sku) {
        this.sku = sku;
    }

    /**
     * Sets qty.
     *
     * @param qty the qty
     */
    public void setQty(int qty) {
        this.qty = qty;
    }

    @Override
    public String toString() {
        return "PickingItems: " +
                "orderId=" + orderId +
                ", lineNo=" + lineNo +
                ", aisleId=" + aisleId +
                ", bayId=" + bayId +
                ", boxId=" + boxId +
                ", sku='" + sku + '\'' +
                ", qty=" + qty +
                '.';
    }
}
