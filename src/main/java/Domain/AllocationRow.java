package Domain;

public class AllocationRow {
    private String orderId;
    private int lineNo;
    private String sku;
    private int qty;
    private String boxId;
    private int aisleID;
    private int bayID;

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

    // Getters
    public String getOrderId() { return orderId; }
    public int getLineNo() { return lineNo; }
    public String getSku() { return sku; }
    public int getQty() { return qty; }
    public String getBoxId() { return boxId; }
    public int getAisle() { return aisleID; }
    public int getBay() { return bayID; }
}