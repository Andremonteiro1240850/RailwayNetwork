package Domain;

public class PickingItems {
    String orderId;
    int lineNo;
    int aisleId;
    int bayId;
    String boxId;
    String sku;
    int qty;

    public PickingItems(String orderId, int lineNo, int aisleId, int bayId, String boxId, String sku, int qty) {
        this.orderId = orderId;
        this.lineNo = lineNo;
        this.aisleId = aisleId;
        this.bayId = bayId;
        this.boxId = boxId;
        this.sku = sku;
        this.qty = qty;
    }
    public String getOrderId() {
        return orderId;
    }
    public int getLineNo() {
        return lineNo;
    }
    public int getAisleId() {
        return aisleId;
    }
    public int getBayId() {
        return bayId;
    }
    public String getBoxId() {
        return boxId;
    }
    public String getSku() {
        return sku;
    }
    public int getQty() {
        return qty;
    }
    public void  setOrderId(String orderId) {
        this.orderId = orderId;
    }
    public void setLineNo(int lineNo) {
        this.lineNo = lineNo;
    }
    public void setAisleId(int aisleId) {
        this.aisleId = aisleId;
    }
    public void setBayId(int bayId) {
        this.bayId = bayId;
    }
    public void setBoxId(String boxId) {
        this.boxId = boxId;
    }
    public void setSku(String sku) {
        this.sku = sku;
    }
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
