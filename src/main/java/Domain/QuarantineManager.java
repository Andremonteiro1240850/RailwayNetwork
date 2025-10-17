package Domain;

public class QuarantineManager {
    Warehouse warehouse;
    QuarantineInspection quarantineInspection;
    int id;

    public QuarantineManager(Warehouse warehouse, QuarantineInspection quarantineInspection, int id) {
        this.warehouse = warehouse;
        this.quarantineInspection = quarantineInspection;
        this.id = id;
    }

    public Warehouse getWarehouseId() {return warehouse;}
    public QuarantineInspection getQuarantineInspectionId() {return quarantineInspection;}
    public int getId() {return id;}

    public void setWarehouse(Warehouse warehouse) {this.warehouse = warehouse;}
    public void setQuarantineInspection(QuarantineInspection quarantineInspection) {this.quarantineInspection = quarantineInspection;}
    public void setId(int id) {this.id = id;}
}
