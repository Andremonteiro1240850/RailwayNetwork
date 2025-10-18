package Domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.Objects;

public class Box {
    private final String boxId;            // identificador único
    private final String sku;              // produto
    private int qty;                       // quantidade de unidades
    private final LocalDate expiryDate;    // null se não perecível
    private final LocalDateTime receivedAt;// data de receção
    private BayId location;                // localização atual

    public Box(String boxId, String sku, int qty,
               LocalDate expiryDate, LocalDateTime receivedAt, BayId location) {
        this.boxId = boxId;
        this.sku = sku;
        this.qty = qty;
        this.expiryDate = expiryDate;
        this.receivedAt = receivedAt;
        this.location = location;
    }
    
    // Constructor without location (for initial creation)
    public Box(String boxId, String sku, int qty,
               LocalDate expiryDate, LocalDateTime receivedAt) {
        this.boxId = boxId;
        this.sku = sku;
        this.qty = qty;
        this.expiryDate = expiryDate;
        this.receivedAt = receivedAt;
        this.location = null;
    }

    public String getBoxId() { return boxId; }
    public String getSku() { return sku; }
    public int getQty() { return qty; }
    public LocalDate getExpiryDate() { return expiryDate; }
    public LocalDateTime getReceivedAt() { return receivedAt; }
    public BayId getLocation() { return location; }

    public void setLocation(BayId newLoc) { this.location = newLoc; }
    public void reduceQty(int amount) { this.qty -= amount; }
    public void setQty(int qty) { this.qty = qty; }
    
    // Methods needed for WarehouseController compatibility
    public Aisle getAisle() {
        // Return null for now - this is for compatibility with existing code
        return null;
    }
    
    public Bay getBay() {
        // Return null for now - this is for compatibility with existing code  
        return null;
    }

    // FEFO/FIFO comparator
    public static final Comparator<Box> FEFO_COMPARATOR = (a, b) -> {
        LocalDate ea = a.getExpiryDate();
        LocalDate eb = b.getExpiryDate();

        // 1. FEFO: mais cedo expira → vem primeiro
        if (ea == null && eb != null) return 1;
        if (ea != null && eb == null) return -1;
        if (ea != null && eb != null) {
            int cmp = ea.compareTo(eb);
            if (cmp != 0) return cmp;
        }

        // 2. FIFO: mais cedo recebido → vem primeiro
        int cmp = a.getReceivedAt().compareTo(b.getReceivedAt());
        if (cmp != 0) return cmp;

        // 3. desempate por boxId
        return a.getBoxId().compareTo(b.getBoxId());
    };

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Box)) return false;
        Box other = (Box) o;
        return Objects.equals(this.boxId, other.boxId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(boxId);
    }

    @Override
    public String toString() {
        return "Box{" + boxId + ", sku=" + sku + ", qty=" + qty + "}";
    }
}

