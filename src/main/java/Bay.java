public class Bay {
    Warehouse warehouse;
    int id;
    int boxCapacity;
    Aisle aisle;
    public Bay(Warehouse warehouse, int id, int boxCapacity, Aisle aisle){
        this.warehouse = warehouse;
        this.id = id;
        this.boxCapacity = boxCapacity;
        this.aisle = aisle;
    }
    public Warehouse getWarehouse(){
        return warehouse;
    }
    public void setWarehouse(Warehouse warehouse){
        this.warehouse = warehouse;
    }
    public int getId(){
        return id;
    }
    public void setId(int id){
        this.id = id;
    }
    public int getBoxCapacity(){
        return boxCapacity;
    }
    public void setBoxCapacity(int boxCapacity){
        this.boxCapacity = boxCapacity;
    }
    public Aisle getAisle(){
        return aisle;
    }
    public void setAisle(Aisle aisle){
        this.aisle = aisle;
    }
}
