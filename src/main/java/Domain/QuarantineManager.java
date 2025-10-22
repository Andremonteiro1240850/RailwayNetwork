package Domain;

/**
 * The type Quarantine manager.
 */
public class QuarantineManager {
    /**
     * The Warehouse.
     */
    Warehouse warehouse;
    /**
     * The Quarantine inspection.
     */
    QuarantineInspection quarantineInspection;
    /**
     * The Id.
     */
    int id;

    /**
     * Instantiates a new Quarantine manager.
     *
     * @param warehouse            the warehouse
     * @param quarantineInspection the quarantine inspection
     * @param id                   the id
     */
    public QuarantineManager(Warehouse warehouse, QuarantineInspection quarantineInspection, int id) {
        this.warehouse = warehouse;
        this.quarantineInspection = quarantineInspection;
        this.id = id;
    }

    /**
     * Gets warehouse id.
     *
     * @return the warehouse id
     */
    public Warehouse getWarehouseId() {return warehouse;}

    /**
     * Gets quarantine inspection id.
     *
     * @return the quarantine inspection id
     */
    public QuarantineInspection getQuarantineInspectionId() {return quarantineInspection;}

    /**
     * Gets id.
     *
     * @return the id
     */
    public int getId() {return id;}

    /**
     * Sets warehouse.
     *
     * @param warehouse the warehouse
     */
    public void setWarehouse(Warehouse warehouse) {this.warehouse = warehouse;}

    /**
     * Sets quarantine inspection.
     *
     * @param quarantineInspection the quarantine inspection
     */
    public void setQuarantineInspection(QuarantineInspection quarantineInspection) {this.quarantineInspection = quarantineInspection;}

    /**
     * Sets id.
     *
     * @param id the id
     */
    public void setId(int id) {this.id = id;}
}
