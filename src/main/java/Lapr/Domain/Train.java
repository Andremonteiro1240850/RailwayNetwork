package Lapr.Domain;

import java.util.Objects;

public class Train {
    private int trainID;
    private int locomotiveID;
    private int wagonID;
    private String operatorID;
    private String date;
    private String time;

    public Train() {
    }
    public Train(int locomotiveID, int wagonID, String operatorID, String date, String time, int trainID) {
        this.trainID = trainID;
        this.locomotiveID = locomotiveID;
        this.wagonID = wagonID;
        this.operatorID = operatorID;
        this.date = date;
        this.time = time;
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

    public String getOperatorID() {
        return operatorID;
    }

    public void setOperatorID(String operatorID) {
        this.operatorID = operatorID;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    @Override
    public String toString() {
        return "Train{" +
                "trainID=" + trainID +
                ", locomotiveID=" + locomotiveID +
                ", wagonID=" + wagonID +
                ", operatorID='" + operatorID + '\'' +
                ", date='" + date + '\'' +
                ", time='" + time + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Train train = (Train) o;
        return trainID == train.trainID;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(trainID);
    }
}
