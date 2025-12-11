package Repository;

import java.util.*;

/**
 * Repository for managing railway stations.
 * Provides efficient storage and retrieval of station data for graph algorithms.
 * 
 * Uses HashMap for O(1) lookup by ID, which is essential for graph construction.
 *
 * @author Development Team
 * @version 1.0
 */
public class StationRepository {
    
    private final List<Station> stations;
    private final Map<String, Station> stationById;
    
    /**
     * Constructs an empty StationRepository
     */
    public StationRepository() {
        this.stations = new ArrayList<>();
        this.stationById = new HashMap<>();
    }
    
    /**
     * Constructs a StationRepository with initial stations.
     *
     * @param stations the initial list of stations
     */
    public StationRepository(List<Station> stations) {
        this();
        addAll(stations);
    }
    
    /**
     * Adds a station to the repository.
     *
     * @param station the station to add
     * @throws IllegalArgumentException if station is null or ID already exists
     */
    public void add(Station station) {
        if (station == null) {
            throw new IllegalArgumentException("Station cannot be null");
        }
        
        String id = station.getId();
        if (stationById.containsKey(id)) {
            throw new IllegalArgumentException("Station with ID '" + id + "' already exists");
        }
        
        stations.add(station);
        stationById.put(id, station);
    }
    
    /**
     * Adds all stations from a collection to the repository.
     *
     * @param stations the stations to add
     */
    public void addAll(Collection<Station> stations) {
        if (stations == null) {
            throw new IllegalArgumentException("Stations collection cannot be null");
        }
        
        for (Station station : stations) {
            add(station);
        }
    }
    
    /**
     * Gets a station by its ID.
     * Time complexity: O(1)
     *
     * @param id the station ID
     * @return the station, or null if not found
     */
    public Station getById(String id) {
        if (id == null) {
            return null;
        }
        return stationById.get(id.trim());
    }
    
    /**
     * Checks if a station exists with the given ID.
     * Time complexity: O(1)
     *
     * @param id the station ID
     * @return true if the station exists, false otherwise
     */
    public boolean exists(String id) {
        return id != null && stationById.containsKey(id.trim());
    }
    
    /**
     * Gets all stations.
     * Returns a defensive copy to prevent external modification.
     *
     * @return a new list containing all stations
     */
    public List<Station> getAll() {
        return new ArrayList<>(stations);
    }
    
    /**
     * Gets the number of stations in the repository.
     *
     * @return the number of stations
     */
    public int size() {
        return stations.size();
    }
    
    /**
     * Checks if the repository is empty.
     *
     * @return true if empty, false otherwise
     */
    public boolean isEmpty() {
        return stations.isEmpty();
    }
    
    /**
     * Clears all stations from the repository.
     */
    public void clear() {
        stations.clear();
        stationById.clear();
    }
    
    /**
     * Gets an iterator over all stations.
     *
     * @return iterator over stations
     */
    public Iterator<Station> iterator() {
        return stations.iterator();
    }
}

