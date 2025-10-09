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
}
