package Domain;

import java.util.Objects;

public class BayId  implements Comparable<BayId> {

    private final String warehouseId;
    private final int aisle;
    private final int bay;


    public BayId(String warehouseId, int aisle, int bay) {
        this.warehouseId = warehouseId;
        this.aisle = aisle;
        this.bay = bay;
    }

    public String getWarehouseId() { return warehouseId; }
    public int getAisle() { return aisle; }
    public int getBay() { return bay; }

    @Override
    public int compareTo(BayId o) {
        int cmp = this.warehouseId.compareTo(o.warehouseId);
        if (cmp != 0) return cmp;
        cmp = Integer.compare(this.aisle, o.aisle);
        if (cmp != 0) return cmp;
        return Integer.compare(this.bay, o.bay);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BayId)) return false;
        BayId other = (BayId) o;
        return aisle == other.aisle && bay == other.bay &&
                Objects.equals(warehouseId, other.warehouseId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(warehouseId, aisle, bay);
    }

    @Override
    public String toString() {
        return warehouseId + "/A" + aisle + "/B" + bay;
    }




}