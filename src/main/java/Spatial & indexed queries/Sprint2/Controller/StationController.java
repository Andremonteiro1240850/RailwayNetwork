package Sprint2.Controller;

import Sprint2.Domain.Station;
import Sprint2.Repository.StationRepository;

import java.util.List;

/**
 * The type Station controller.
 */
public class StationController {
    private final StationRepository stationRepository;

    /**
     * Instantiates a new Station controller.
     *
     * @param stationRepository the station repository
     */
    public StationController(StationRepository stationRepository) {
        this.stationRepository = stationRepository;
    }

    /**
     * Gets station by time zone.
     *
     * @param timeZone the time zone
     * @return the station by time zone
     */
    public Iterable<Station> getStationByTimeZone(String timeZone) {
        if(!isValidTimeZone(timeZone)) {
            throw new IllegalArgumentException("Invalid Time Zone Group: " + timeZone);
        }
        return stationRepository.getStationsByTimeZone(timeZone);
    }

    /**
     * Gets station by country.
     *
     * @param country the country
     * @return the station by country
     */
    public  Iterable<Station> getStationByCountry(String country) {
        if(!isValidCountry(country)) {
            throw new IllegalArgumentException("Invalid Country: " + country);
        }
        return stationRepository.getStationsByCountry(country);
    }

    /**
     * Gets station by latitude.
     *
     * @param latitudeMin the latitude min
     * @param latitudeMax the latitude max
     * @return the station by latitude
     */
    public Iterable<Station> getStationByLatitude(double latitudeMin,double latitudeMax ) {
        if(!isValidLatitude(latitudeMin, latitudeMax)) {
            throw new IllegalArgumentException("Invalid Latitude Range: " + latitudeMin + " to " + latitudeMax);
        }
        return stationRepository.getStationsByLatitude(latitudeMin,latitudeMax);
    }

    /**
     * Gets station by longitude.
     *
     * @param longitudeMin the longitude min
     * @param longitudeMax the longitude max
     * @return the station by longitude
     */
    public Iterable<Station> getStationByLongitude(double longitudeMin,double longitudeMax ) {
        if(!isValidLongitude(longitudeMin, longitudeMax)) {
            throw new IllegalArgumentException("Invalid Longitude Range: " + longitudeMin + " to " + longitudeMax);
        }
        return stationRepository.getStationsByLongitude(longitudeMin, longitudeMax);
    }

    /**
     * Is valid time zone boolean.
     *
     * @param timeZone the time zone
     * @return the boolean
     */
    public boolean isValidTimeZone(String timeZone) {
        List<Station> stations = stationRepository.getStationsByTimeZone(timeZone);
        for (Station station : stations) {
            if(station.getTimeZoneGroup().equals(timeZone)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Is valid country boolean.
     *
     * @param country the country
     * @return the boolean
     */
    public boolean isValidCountry(String country) {
        List<Station> stations = stationRepository.getStationsByCountry(country);
        for (Station station : stations) {
            if(station.getCountry().equals(country)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Is valid latitude boolean.
     *
     * @param latitudeMin the latitude min
     * @param latitudeMax the latitude max
     * @return the boolean
     */
    public  boolean isValidLatitude(double latitudeMin,double latitudeMax) {
        return latitudeMin >= -90 && latitudeMax <= 90 && latitudeMin <= latitudeMax;
    }

    /**
     * Is valid longitude boolean.
     *
     * @param longitudeMin the longitude min
     * @param longitudeMax the longitude max
     * @return the boolean
     */
    public  boolean isValidLongitude(double longitudeMin,double longitudeMax) {
        return longitudeMin >= -180 && longitudeMax <= 180 && longitudeMin <= longitudeMax;
    }



}
