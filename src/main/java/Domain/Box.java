package Domain;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Box {
    private String boxId, sku;
    private int qty;
    private LocalDate expiryDate;
    private LocalDateTime receivedAt;

    //Pombinha tens de definir quais as aisle e o bay das box por FEFO
    private Aisle aisle;
    private Bay bay;

    public Box(String boxId, String sku, int qty, LocalDate expiryDate, LocalDateTime receivedAt,Aisle aisle,Bay bay) {
        this.boxId = boxId;
        this.sku = sku;
        this.qty = qty;
        this.expiryDate = expiryDate;
        this.receivedAt = receivedAt;
        this.aisle = aisle;
        this.bay = bay;
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

    public void setBoxId(String boxId) {
        this.boxId = boxId;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public void setQty(int qty) {
        this.qty = qty;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    public void setReceivedAt(LocalDateTime receivedAt) {
        this.receivedAt = receivedAt;
    }

    public Aisle getAisle() {
        return aisle;
    }

    public Bay getBay() {
        return bay;
    }

    public void setAisle(Aisle aisle) {
        this.aisle = aisle;
    }

    public void setBay(Bay bay) {
        this.bay = bay;
    }


    public boolean equals(Object obj) {
        if(this == obj){return true;}
        if (obj==null||this.getClass() != obj.getClass()) {
            return false;
        }
        Box box = (Box) obj;
        return this.boxId.equals(box.getBoxId());
    }


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

