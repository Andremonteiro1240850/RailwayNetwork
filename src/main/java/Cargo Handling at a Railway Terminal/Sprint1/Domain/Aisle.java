package Sprint1.Domain;

/**
 * The type Aisle.
 */
public class Aisle {
    /**
     * The Id.
     */
    int id;
    /**
     * The Warehouse.
     */
    Warehouse warehouse;

    /**
     * Instantiates a new Aisle.
     *
     * @param id        the id
     * @param warehouse the warehouse
     */
    public Aisle(int id,  Warehouse warehouse) {
        this.id = id;
        this.warehouse = warehouse;
    }

    /**
     * Get id int.
     *
     * @return the int
     */
    public int getId(){
        return id;
    }

    /**
     * Set id.
     *
     * @param id the id
     */
    public void setId(int id){
        this.id = id;
    }

    /**
     * Get warehouse warehouse.
     *
     * @return the warehouse
     */
    public Warehouse getWarehouse(){return warehouse;}
}
