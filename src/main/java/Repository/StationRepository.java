package Repository;

import Domain.AVL;
import Domain.BST;
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
        List<Station> result = new ArrayList<>();
        collectByTimeZone(groupCountryTree.getRoot(), timeZone, result);
        return result;
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
    public List<Station> getStationsByLatitude(double latitudeMin,double latitudeMax) {
        List<Station> result = new ArrayList<>();
        collectByLatitude(latitudeTree.getRoot(), latitudeMin, latitudeMax, result);
        return result;
    }

    public List<Station> getStationsByLongitude(double longitudeMin, double longitudeMax) {
        List<Station> result = new ArrayList<>();
        collectByLongitude(longitudeTree.getRoot(), longitudeMin, longitudeMax, result);
        return result;
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
    private void collectByLatitude(BST.Node<Station> node, double min, double max, List<Station> result) {
        if (node == null) return;

        if (node.getElement().getLatitude() > min)
            collectByLatitude(node.getLeft(), min, max, result);

        double lat = node.getElement().getLatitude();
        if (lat >= min && lat <= max)
            result.add(node.getElement());

        if (node.getElement().getLatitude() < max)
            collectByLatitude(node.getRight(), min, max, result);
    }
    private void collectByLongitude(BST.Node<Station> node, double min, double max, List<Station> result) {
        if (node == null) return;

        double lon = node.getElement().getLongitude();

        if (lon > min)
            collectByLongitude(node.getLeft(), min, max, result);

        if (lon >= min && lon <= max)
            result.add(node.getElement());

        if (lon < max)
            collectByLongitude(node.getRight(), min, max, result);
    }
    private void collectByTimeZone(BST.Node<Station> node, String timeZone, List<Station> result) {
        if (node == null) return;

        int cmp = node.getElement().getTimeZoneGroup().compareTo(timeZone);

        if (cmp > 0) return; // todos os restantes são maiores → parar recursão

        if (cmp < 0)
            collectByTimeZone(node.getRight(), timeZone, result);
        else {
            // cmp == 0 → nó do fuso horário correto
            collectByTimeZone(node.getLeft(), timeZone, result);
            result.add(node.getElement());
            collectByTimeZone(node.getRight(), timeZone, result);
        }
    }


}
