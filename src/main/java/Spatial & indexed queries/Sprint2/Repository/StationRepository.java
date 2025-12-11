package Sprint2.Repository;

import Sprint2.Domain.AVL;
import Sprint2.Domain.BST;
import Sprint2.Domain.Station;
import Sprint2.Domain.StationValidator;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * The type Station repository.
 */
public class StationRepository {
    private AVL<Station> latitudeTree;
    private AVL<Station> longitudeTree;
    private AVL<Station> groupCountryTree;
    // Armazenamento direto de todas as estações (além das AVL trees)
    private final List<Station> allStations;

    /**
     * Instantiates a new Station repository.
     *
     * @param stations the stations
     */
    public StationRepository(List<Station> stations) {
        this.allStations = new ArrayList<>();
        populateTrees(stations);
    }

    /**
     * Populate trees.
     *
     * @param stations the stations
     */
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
                // Guardar nas AVL trees
                latitudeTree.insert(station);
                longitudeTree.insert(station);
                groupCountryTree.insert(station);
                // Guardar também na lista direta
                allStations.add(station);
            }
        }
    }

    /**
     * Gets stations by time zone.
     *
     * @param timeZone the time zone
     * @return the stations by time zone
     */
    public List<Station> getStationsByTimeZone(String timeZone) {
        List<Station> result = new ArrayList<>();
        collectByTimeZone(groupCountryTree.getRoot(), timeZone, result);
        return result;
    }

    /**
     * Gets stations by country.
     *
     * @param country the country
     * @return the stations by country
     */
    public  List<Station> getStationsByCountry(String country) {
        List<Station> stationsByCountry = new ArrayList<>();
        for(Station station : groupCountryTree.inOrder()) {
            if(station.getCountry().equals(country)) {
                stationsByCountry.add(station);
            }
        }
        return stationsByCountry;
    }

    /**
     * Gets stations by latitude.
     *
     * @param latitudeMin the latitude min
     * @param latitudeMax the latitude max
     * @return the stations by latitude
     */
    public List<Station> getStationsByLatitude(double latitudeMin,double latitudeMax) {
        List<Station> result = new ArrayList<>();
        collectByLatitude(latitudeTree.getRoot(), latitudeMin, latitudeMax, result);
        return result;
    }

    /**
     * Gets stations by longitude.
     *
     * @param longitudeMin the longitude min
     * @param longitudeMax the longitude max
     * @return the stations by longitude
     */
    public List<Station> getStationsByLongitude(double longitudeMin, double longitudeMax) {
        List<Station> result = new ArrayList<>();
        collectByLongitude(longitudeTree.getRoot(), longitudeMin, longitudeMax, result);
        return result;
    }

    /**
     * Total stations int.
     *
     * @return the int
     */
    public int totalStations() {
        return latitudeTree.size(); // todas têm o mesmo número
    }

    /**
     * Gets height latitude tree.
     *
     * @return the height latitude tree
     */
    public int getHeightLatitudeTree() {
        return latitudeTree.height();
    }

    /**
     * Gets height time zone tree.
     *
     * @return the height time zone tree
     */
    public int getHeightTimeZoneTree() {
        return groupCountryTree.height();
    }

    /**
     * Gets all stations ordered by latitude.
     *
     * @return the all stations ordered by latitude
     */
    public Iterable<Station> getAllStationsOrderedByLatitude() {
        return latitudeTree.inOrder();
    }

    /**
     * Gets all stations ordered by longitude.
     *
     * @return the all stations ordered by longitude
     */
    public Iterable<Station> getAllStationsOrderedByLongitude() {
        return longitudeTree.inOrder();
    }

    /**
     * Gets all stations as a List.
     * Useful for building KD-trees and other operations that need all stations.
     * Returns the directly stored list (O(1) access) instead of traversing trees.
     *
     * @return list of all stations in the repository
     */
    public List<Station> getAllStations() {
        return new ArrayList<>(allStations); // Retorna cópia para evitar modificações
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

        String nodeTimeZone = node.getElement().getTimeZoneGroup();
        int cmp = nodeTimeZone.compareTo(timeZone);

        // 1. Ir para a esquerda
        if (cmp >= 0) {
            collectByTimeZone(node.getLeft(), timeZone, result);
        }

        // 2. Adicionar nó
        if (cmp == 0) {
            result.add(node.getElement());
        }

        // 3. Ir para a direita
        if (cmp <= 0) {
            collectByTimeZone(node.getRight(), timeZone, result);
        }
    }



}
