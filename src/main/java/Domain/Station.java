package Domain;

import java.util.Objects;

public class Station implements Comparable<Station> {
    @Override
    public int compareTo(Station o) {
        int cmp = this.name.compareTo(o.name);
        if (cmp != 0) return cmp;
        return this.country.compareTo(o.country);
    }

    private String name;
    private double latitude;
    private double longitude;
    private String country;
    private String timeZone;
    private String timeZoneGroup;
    private boolean isCity;
    private boolean isMainStation;
    private boolean isAirport;

    public Station(String name, double latitude, double longitude, String country, String timeZone, String timeZoneGroup, boolean isCity, boolean isMainStation, boolean isAirport) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.country = country;
        this.timeZone = timeZone;
        this.timeZoneGroup = timeZoneGroup;
        this.isCity = isCity;
        this.isMainStation = isMainStation;
        this.isAirport = isAirport;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getTimeZoneGroup() {
        return timeZoneGroup;
    }

    public void setTimeZoneGroup(String timeZoneGroup) {
        this.timeZoneGroup = timeZoneGroup;
    }

    public boolean isCity() {
        return isCity;
    }

    public void setCity(boolean city) {
        isCity = city;
    }

    public boolean isMainStation() {
        return isMainStation;
    }

    public void setMainStation(boolean mainStation) {
        isMainStation = mainStation;
    }

    public boolean isAirport() {
        return isAirport;
    }

    public void setAirport(boolean airport) {
        isAirport = airport;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Station station = (Station) o;
        return Double.compare(latitude, station.latitude) == 0 && Double.compare(longitude, station.longitude) == 0 && Objects.equals(name, station.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, latitude, longitude);
    }

    @Override
    public String toString() {
        return "Station{" +
                "name='" + name + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", country='" + country + '\'' +
                ", timeZone='" + timeZone + '\'' +
                ", timeZoneGroup='" + timeZoneGroup + '\'' +
                ", isCity=" + isCity +
                ", isMainStation=" + isMainStation +
                ", isAirport=" + isAirport +
                '}';
    }
}
