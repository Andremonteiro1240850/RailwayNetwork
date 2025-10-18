package Domain;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Return {

    private String returnId;
    private String sku;
    private int qty;
    private String reason;
    private LocalDateTime timestamp;
    private LocalDate expiryDate;

    public Return(String returnId, String sku, int qty, String reason, LocalDateTime timestamp, LocalDate expiryDate) {
        this.returnId = returnId;
        this.sku = sku;
        this.qty = qty;
        this.reason = reason;
        this.timestamp = timestamp;
        this.expiryDate = expiryDate;
    }

    // --- Getters ---
    public String getReturnId() {
        return returnId;
    }

    public String getSku() {
        return sku;
    }

    public int getQty() {
        return qty;
    }

    public String getReason() {
        return reason;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    // --- Setters (caso precises de alterar algo) ---
    public void setQty(int qty) {
        this.qty = qty;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    // --- Métodos auxiliares ---
    @Override
    public String toString() {
        return "Return{" +
                "returnId='" + returnId + '\'' +
                ", sku='" + sku + '\'' +
                ", qty=" + qty +
                ", reason='" + reason + '\'' +
                ", timestamp=" + timestamp +
                ", expiryDate=" + expiryDate +
                '}';
    }
}
