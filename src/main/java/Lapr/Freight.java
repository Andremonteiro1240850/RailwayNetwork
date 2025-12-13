package Lapr;

public class Freight {
    private int id;
    private String cargoType;
    private int trainID;
    private int locomotiveID;
    private int wagonID;

    public Freight(int id, String cargoType, int trainID, int locomotiveID, int wagonID) {
        this.id = id;
        this.cargoType = cargoType;
        this.trainID = trainID;
        this.locomotiveID = locomotiveID;
        this.wagonID = wagonID;
    }
    public Freight() {

    }
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public String getCargoType() {
        return cargoType;
    }
    public void setCargoType(String cargoType) {
        this.cargoType = cargoType;
    }
    public int getTrainID() {
        return trainID;
    }
    public void setTrainID(int trainID) {
        this.trainID = trainID;
    }
    public int getLocomotiveID() {
        return locomotiveID;
    }
    public void setLocomotiveID(int locomotiveID) {
        this.locomotiveID = locomotiveID;
    }
    public int getWagonID() {
        return wagonID;
    }
    public void setWagonID(int wagonID) {
        this.wagonID = wagonID;
    }
    @Override
    public String toString() {
        return "Freight{" +
                "id=" + id +
                ", cargoType='" + cargoType + '\'' +
                ", trainID=" + trainID +
                ", locomotiveID=" + locomotiveID +
                ", wagonID=" + wagonID +
                '}';
    }


}
