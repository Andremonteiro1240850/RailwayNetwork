package Controller;

import Domain.Station;
import Repository.StationRepository;

import java.util.List;

public class StationController {
    private final StationRepository stationRepository;

    public StationController(StationRepository stationRepository) {
        this.stationRepository = stationRepository;
    }

    public Iterable<Station> getStationByTimeZone(String timeZone) {
        if(!isValidTimeZone(timeZone)) {
            throw new IllegalArgumentException("Invalid Time Zone Group: " + timeZone);
        }
        return stationRepository.getStationsByTimeZone(timeZone);
    }
    public  Iterable<Station> getStationByCountry(String country) {
        if(!isValidCountry(country)) {
            throw new IllegalArgumentException("Invalid Country: " + country);
        }
        return stationRepository.getStationsByCountry(country);
    }
    public Iterable<Station> getStationByLatitude(double latitudeMin,double latitudeMax ) {
        if(!isValidLatitude(latitudeMin, latitudeMax)) {
            throw new IllegalArgumentException("Invalid Latitude Range: " + latitudeMin + " to " + latitudeMax);
        }
        return stationRepository.getStationsByLatitude(latitudeMin,latitudeMax);
    }
    public Iterable<Station> getStationByLongitude(double longitudeMin,double longitudeMax ) {
        if(!isValidLongitude(longitudeMin, longitudeMax)) {
            throw new IllegalArgumentException("Invalid Longitude Range: " + longitudeMin + " to " + longitudeMax);
        }
        return stationRepository.getStationsByLongitude(longitudeMin, longitudeMax);
    }
    public boolean isValidTimeZone(String timeZone) {
        List<Station> stations = stationRepository.getStationsByTimeZone(timeZone);
        for (Station station : stations) {
            if(station.getTimeZoneGroup().equals(timeZone)) {
                return true;
            }
        }
        return false;
    }
    public boolean isValidCountry(String country) {
        List<Station> stations = stationRepository.getStationsByCountry(country);
        for (Station station : stations) {
            if(station.getCountry().equals(country)) {
                return true;
            }
        }
        return false;
    }
    public  boolean isValidLatitude(double latitudeMin,double latitudeMax) {
        return latitudeMin >= -90 && latitudeMax <= 90 && latitudeMin <= latitudeMax;
    }
    public  boolean isValidLongitude(double longitudeMin,double longitudeMax) {
        return longitudeMin >= -180 && longitudeMax <= 180 && longitudeMin <= longitudeMax;
    }



}
