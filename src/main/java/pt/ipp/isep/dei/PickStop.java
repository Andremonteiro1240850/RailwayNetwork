package pt.ipp.isep.dei;

public class PickStop {
    private int seq;
    private int aisle;
    private int bay;
    private int totalQty;

    public PickStop(int seq, int aisle, int bay, int totalQty) {
        this.seq = seq;
        this.aisle = aisle;
        this.bay = bay;
        this.totalQty = totalQty;
    }

    public int getSeq() {
        return seq;
    }

    public void setSeq(int seq) {
        this.seq = seq;
    }

    public int getAisle() {
        return aisle;
    }

    public void setAisle(int aisle) {
        this.aisle = aisle;
    }

    public int getBay() {
        return bay;
    }

    public void setBay(int bay) {
        this.bay = bay;
    }

    public int getTotalQty() {
        return totalQty;
    }

    public void setTotalQty(int totalQty) {
        this.totalQty = totalQty;
    }

    // Para merge de duplicados - equals e hashCode baseados em aisle e bay
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        PickStop other = (PickStop) obj;
        return aisle == other.aisle && bay == other.bay;
    }

    @Override
    public int hashCode() {
        return 31 * aisle + bay;
    }

    @Override
    public String toString() {
        return "(" + aisle + "," + bay + ") Qty:" + totalQty;
    }
}
