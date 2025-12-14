package Lapr.Domain;

import java.util.Objects;

public class Route {
    private int trainID;
    private int order;
    private int facilityID;

    public  Route(int trainID, int order, int facilityID) {
        this.trainID = trainID;
        this.order = order;
        this.facilityID = facilityID;
    }
    public Route() {
    }

    public int getTrainID() {
        return trainID;
    }

    public void setTrainID(int trainID) {
        this.trainID = trainID;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public int getFacilityID() {
        return facilityID;
    }

    public void setFacilityID(int facilityID) {
        this.facilityID = facilityID;
    }

    @Override
    public String toString() {
        return "Route => " +
                "trainID |" + trainID +
                " order |" + order +
                " facilityID |" + facilityID;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Route route = (Route) o;
        return trainID == route.trainID && order == route.order && facilityID == route.facilityID;
    }

    @Override
    public int hashCode() {
        return Objects.hash(trainID, order, facilityID);
    }

}
