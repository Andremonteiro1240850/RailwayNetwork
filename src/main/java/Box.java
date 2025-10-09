import java.time.LocalDate;
import java.time.LocalDateTime;

public class Box {
    String boxId, sku;
    int qty;
    LocalDate expiry;
    LocalDateTime receivedAt;
    Box(String boxId, String sku, int qty, LocalDate expiry, LocalDateTime receivedAt) {
        this.boxId = boxId; this.sku = sku; this.qty = qty;
        this.expiry = expiry; this.receivedAt = receivedAt;
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
    public LocalDate getExpiry() {
        return expiry;
    }
    public LocalDateTime getReceivedAt() {
        return receivedAt;
    }
    public void setBoxId(String boxId) { this.boxId = boxId; }
    public void setSku(String sku) { this.sku = sku; }
    public void setQty(int qty) { this.qty = qty; }
    public void setExpiry(LocalDate expiry) { this.expiry = expiry; }
    public void setReceivedAt(LocalDateTime receivedAt) { this.receivedAt = receivedAt; }

}
