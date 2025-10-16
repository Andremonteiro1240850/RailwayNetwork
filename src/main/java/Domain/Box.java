package Domain;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Box {
    String boxId, sku;
    int qty;
    LocalDate expiryDate;
    LocalDateTime receivedAt;
    int aisle;
    int bay;

        Box(String boxId, String sku, int qty, LocalDate expiryDate, LocalDateTime receivedAt) {
        this.boxId = boxId; this.sku = sku; this.qty = qty;
        this.expiryDate = expiryDate; this.receivedAt = receivedAt;
        this.aisle = -1;
        this.bay = -1;
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
    public void setBoxId(String boxId) { this.boxId = boxId; }
    public void setSku(String sku) { this.sku = sku; }
    public void setQty(int qty) { this.qty = qty; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }
    public void setReceivedAt(LocalDateTime receivedAt) { this.receivedAt = receivedAt; }

    @Override
    public String toString() {
        return "Domain.Box{" +
                "Domain.Box Id: " + boxId + '\'' +
                ", SKU: " + sku + '\'' +
                ", Quantity: " + qty +
                ", Expiry Date: " + expiryDate +
                ", Received At: " + receivedAt +
                '}';
    }
}
