package Repository;

import Domain.AVL;
import Domain.Station;
import Domain.StationValidator;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class StationRepository {
    private AVL<Station> latitudeTree;
    private AVL<Station> longitudeTree;
    private AVL<Station> groupCountryTree;

    public StationRepository(List<Station> stations) {
        populateTrees(stations);
    }
    public void populateTrees(List<Station> stations) {
        Comparator<Station> latitudeComparator = Comparator.comparingDouble(Station::getLatitude);
        Comparator<Station> longitudeComparator = Comparator.comparingDouble(Station::getLongitude);
        Comparator<Station> groupCountryComparator =Comparator
                .comparing(Station::getTimeZoneGroup)
                .thenComparing(Station::getCountry)
                .thenComparing(Station::getName);

        latitudeTree = new AVL<>(latitudeComparator);
        longitudeTree = new AVL<>(longitudeComparator);
        groupCountryTree = new AVL<>(groupCountryComparator);

        for (Station station : stations) {
            if(StationValidator.isValidStation(station)) {
                latitudeTree.insert(station);
                longitudeTree.insert(station);
                groupCountryTree.insert(station);
            }
        }
    }
    public List<Station> getStationsByTimeZone(String timeZone) {
        List<Station> stationsByTimeZone = new ArrayList<>();
        for(Station station : groupCountryTree.inOrder()) {
            if(station.getTimeZone().equals(timeZone)) {
                stationsByTimeZone.add(station);
            }
        }
        return stationsByTimeZone;
    }
    public  List<Station> getStationsByCountry(String country) {
        List<Station> stationsByCountry = new ArrayList<>();
        for(Station station : groupCountryTree.inOrder()) {
            if(station.getCountry().equals(country)) {
                stationsByCountry.add(station);
            }
        }
        return stationsByCountry;
    }
    public List<Station> getStationsByLatitude(double latitudeMin,double latitudeMax ) {
        List<Station> stationsByLat = new ArrayList<>();
        for(Station station : latitudeTree.inOrder()) {
            if(station.getLatitude() >= latitudeMin && station.getLatitude() <= latitudeMax) {
                stationsByLat.add(station);
            }
        }
        return stationsByLat;
    }
    public List<Station> getStationsByLongitude(double longitudeMin,double longitudeMax ) {
        List<Station> stationsByLon = new ArrayList<>();
        for(Station station : longitudeTree.inOrder()) {
            if(station.getLatitude() >= longitudeMin && station.getLatitude() <= longitudeMax) {
                stationsByLon.add(station);
            }
        }
        return stationsByLon;
    }
    public int totalStations() {
        return latitudeTree.size(); // todas têm o mesmo número
    }

    public int getHeightLatitudeTree() {
        return latitudeTree.height();
    }

    public int getHeightTimeZoneTree() {
        return groupCountryTree.height();
    }

    public Iterable<Station> getAllStationsOrderedByLatitude() {
        return latitudeTree.inOrder();
    }

    public Iterable<Station> getAllStationsOrderedByLongitude() {
        return longitudeTree.inOrder();
    }

}
