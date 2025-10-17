import java.time.LocalDate;
import java.time.LocalDateTime;

public class Box {
    String boxId, sku;
    int qty;
    LocalDate expiryDate;
    LocalDateTime receivedAt;
    Aisle aisle;
    Bay bay;
    Box(String boxId, String sku, int qty, LocalDate expiryDate, LocalDateTime receivedAt) {
        this.boxId = boxId; this.sku = sku; this.qty = qty;
        this.expiryDate = expiryDate; this.receivedAt = receivedAt;
        this.aisle = aisle; this.bay = bay;
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
    public LocalDate getExpiryDate() {
        return expiryDate;
    }
    public LocalDateTime getReceivedAt() {
        return receivedAt;
    }
    public int getAisleId() { return aisle.getId(); }
    public int getBayId() { return bay.getId(); }
    public void setBoxId(String boxId) { this.boxId = boxId; }
    public void setSku(String sku) { this.sku = sku; }
    public void setQty(int qty) { this.qty = qty; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }
    public void setReceivedAt(LocalDateTime receivedAt) { this.receivedAt = receivedAt; }
    public void reduceQty(int amount) {
        this.qty -= amount;
        if (this.qty < 0) this.qty = 0;
    }


}
