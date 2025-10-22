package Domain;

import java.util.Objects;

/**
 * The type Bay id.
 */
public class BayId  implements Comparable<BayId> {

    private final String warehouseId;
    private final int aisle;
    private final int bay;


    /**
     * Instantiates a new Bay id.
     *
     * @param warehouseId the warehouse id
     * @param aisle       the aisle
     * @param bay         the bay
     */
    public BayId(String warehouseId, int aisle, int bay) {
        this.warehouseId = warehouseId;
        this.aisle = aisle;
        this.bay = bay;
    }

    /**
     * Gets warehouse id.
     *
     * @return the warehouse id
     */
    public String getWarehouseId() { return warehouseId; }

    /**
     * Gets aisle.
     *
     * @return the aisle
     */
    public int getAisle() { return aisle; }

    /**
     * Gets bay.
     *
     * @return the bay
     */
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