package Sprint1.Domain;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * The type Return.
 */
public class Return {

    private String returnId;
    private String sku;
    private int qty;
    private String reason;
    private LocalDateTime timestamp;
    private LocalDate expiryDate;

    /**
     * Instantiates a new Return.
     *
     * @param returnId   the return id
     * @param sku        the sku
     * @param qty        the qty
     * @param reason     the reason
     * @param timestamp  the timestamp
     * @param expiryDate the expiry date
     */
    public Return(String returnId, String sku, int qty, String reason, LocalDateTime timestamp, LocalDate expiryDate) {
        this.returnId = returnId;
        this.sku = sku;
        this.qty = qty;
        this.reason = reason;
        this.timestamp = timestamp;
        this.expiryDate = expiryDate;
    }

    /**
     * Gets return id.
     *
     * @return the return id
     */
// --- Getters ---
    public String getReturnId() {
        return returnId;
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
     * Gets reason.
     *
     * @return the reason
     */
    public String getReason() {
        return reason;
    }

    /**
     * Gets timestamp.
     *
     * @return the timestamp
     */
    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    /**
     * Gets expiry date.
     *
     * @return the expiry date
     */
    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    /**
     * Sets qty.
     *
     * @param qty the qty
     */
// --- Setters (caso precises de alterar algo) ---
    public void setQty(int qty) {
        this.qty = qty;
    }

    /**
     * Sets reason.
     *
     * @param reason the reason
     */
    public void setReason(String reason) {
        this.reason = reason;
    }

    /**
     * Sets expiry date.
     *
     * @param expiryDate the expiry date
     */
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
