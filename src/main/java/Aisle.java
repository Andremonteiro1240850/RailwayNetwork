public class Aisle {
    int id;
    Warehouse warehouse;

    public Aisle(int id,  Warehouse warehouse) {
        this.id = id;
        this.warehouse = warehouse;
    }
    public int getId(){
        return id;
    }
    public void setId(int id){
        this.id = id;
    }
    public Warehouse getWarehouse(){return warehouse;}
}
