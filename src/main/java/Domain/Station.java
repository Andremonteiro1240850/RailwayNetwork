package Domain;

import java.util.Objects;

/**
 * The type Station.
 */
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

    /**
     * Instantiates a new Station.
     *
     * @param name          the name
     * @param latitude      the latitude
     * @param longitude     the longitude
     * @param country       the country
     * @param timeZone      the time zone
     * @param timeZoneGroup the time zone group
     * @param isCity        the is city
     * @param isMainStation the is main station
     * @param isAirport     the is airport
     */
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

    /**
     * Gets name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets name.
     *
     * @param name the name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets longitude.
     *
     * @return the longitude
     */
    public double getLongitude() {
        return longitude;
    }

    /**
     * Sets longitude.
     *
     * @param longitude the longitude
     */
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    /**
     * Gets latitude.
     *
     * @return the latitude
     */
    public double getLatitude() {
        return latitude;
    }

    /**
     * Sets latitude.
     *
     * @param latitude the latitude
     */
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    /**
     * Gets time zone.
     *
     * @return the time zone
     */
    public String getTimeZone() {
        return timeZone;
    }

    /**
     * Sets time zone.
     *
     * @param timeZone the time zone
     */
    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    /**
     * Gets country.
     *
     * @return the country
     */
    public String getCountry() {
        return country;
    }

    /**
     * Sets country.
     *
     * @param country the country
     */
    public void setCountry(String country) {
        this.country = country;
    }

    /**
     * Gets time zone group.
     *
     * @return the time zone group
     */
    public String getTimeZoneGroup() {
        return timeZoneGroup;
    }

    /**
     * Sets time zone group.
     *
     * @param timeZoneGroup the time zone group
     */
    public void setTimeZoneGroup(String timeZoneGroup) {
        this.timeZoneGroup = timeZoneGroup;
    }

    /**
     * Is city boolean.
     *
     * @return the boolean
     */
    public boolean isCity() {
        return isCity;
    }

    /**
     * Sets city.
     *
     * @param city the city
     */
    public void setCity(boolean city) {
        isCity = city;
    }

    /**
     * Is main station boolean.
     *
     * @return the boolean
     */
    public boolean isMainStation() {
        return isMainStation;
    }

    /**
     * Sets main station.
     *
     * @param mainStation the main station
     */
    public void setMainStation(boolean mainStation) {
        isMainStation = mainStation;
    }

    /**
     * Is airport boolean.
     *
     * @return the boolean
     */
    public boolean isAirport() {
        return isAirport;
    }

    /**
     * Sets airport.
     *
     * @param airport the airport
     */
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
