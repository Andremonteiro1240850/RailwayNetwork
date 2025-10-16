package Domain;

public class OrderLine {
    String sku;
    int qty;
    OrderLine(String sku, int qty) {
        this.sku = sku; this.qty = qty;
    }
    public String getSku() { return sku; }
    public int getQty() { return qty; }

}
