package Repository;

import Domain.Line;
import Domain.Station;

import java.util.*;

/**
 * Repository for managing railway lines (edges in the graph).
 * Provides efficient storage and retrieval of line data optimized for graph algorithms.
 * 
 * Maintains adjacency lists for efficient graph traversal:
 * - Lines by departure station (outgoing edges)
 * - Lines by arrival station (incoming edges)
 *
 * @author Development Team
 * @version 1.0
 */
public class LineRepository {
    
    private final List<Line> lines;
    private final Map<String, List<Line>> linesByDeparture; // Outgoing edges
    private final Map<String, List<Line>> linesByArrival;  // Incoming edges
    
    /**
     * Constructs an empty LineRepository.
     */
    public LineRepository() {
        this.lines = new ArrayList<>();
        this.linesByDeparture = new HashMap<>();
        this.linesByArrival = new HashMap<>();
    }
    
    /**
     * Constructs a LineRepository with initial lines.
     *
     * @param lines the initial list of lines
     */
    public LineRepository(List<Line> lines) {
        this();
        addAll(lines);
    }
    
    /**
     * Adds a line to the repository.
     *
     * @param line the line to add
     * @throws IllegalArgumentException if line is null
     */
    public void add(Line line) {
        if (line == null) {
            throw new IllegalArgumentException("Line cannot be null");
        }
        
        lines.add(line);
        
        // Update adjacency lists for efficient graph traversal
        String departureId = line.getDepartureStationId();
        String arrivalId = line.getArrivalStationId();
        
        // Add to outgoing edges (departure -> arrival)
        linesByDeparture.computeIfAbsent(departureId, k -> new ArrayList<>()).add(line);
        
        // Add to incoming edges (arrival <- departure)
        linesByArrival.computeIfAbsent(arrivalId, k -> new ArrayList<>()).add(line);
    }
    
    /**
     * Adds all lines from a collection to the repository.
     *
     * @param lines the lines to add
     */
    public void addAll(Collection<Line> lines) {
        if (lines == null) {
            throw new IllegalArgumentException("Lines collection cannot be null");
        }
        
        for (Line line : lines) {
            add(line);
        }
    }
    
    /**
     * Gets all lines.
     * Returns a defensive copy to prevent external modification.
     *
     * @return a new list containing all lines
     */
    public List<Line> getAll() {
        return new ArrayList<>(lines);
    }
    
    /**
     * Gets all lines departing from a given station (outgoing edges).
     * Time complexity: O(1) average case
     *
     * @param stationId the departure station ID
     * @return list of lines departing from the station, empty list if none
     */
    public List<Line> getByDeparture(String stationId) {
        if (stationId == null) {
            return new ArrayList<>();
        }
        List<Line> result = linesByDeparture.get(stationId.trim());
        return result != null ? new ArrayList<>(result) : new ArrayList<>();
    }
    
    /**
     * Gets all lines arriving at a given station (incoming edges).
     * Time complexity: O(1) average case
     *
     * @param stationId the arrival station ID
     * @return list of lines arriving at the station, empty list if none
     */
    public List<Line> getByArrival(String stationId) {
        if (stationId == null) {
            return new ArrayList<>();
        }
        List<Line> result = linesByArrival.get(stationId.trim());
        return result != null ? new ArrayList<>(result) : new ArrayList<>();
    }
    
    /**
     * Gets all lines connected to a station (both incoming and outgoing).
     * Useful for undirected graph algorithms.
     * Time complexity: O(1) average case
     *
     * @param stationId the station ID
     * @return list of all lines connected to the station
     */
    public List<Line> getConnected(String stationId) {
        Set<Line> connected = new HashSet<>();
        
        List<Line> outgoing = getByDeparture(stationId);
        List<Line> incoming = getByArrival(stationId);
        
        connected.addAll(outgoing);
        connected.addAll(incoming);
        
        return new ArrayList<>(connected);
    }
    
    /**
     * Gets all neighbors (stations directly connected) of a given station.
     * For undirected graphs, returns all stations connected by any line.
     *
     * @param stationId the station ID
     * @return set of neighbor station IDs
     */
    public Set<String> getNeighbors(String stationId) {
        Set<String> neighbors = new HashSet<>();
        
        // Outgoing edges
        for (Line line : getByDeparture(stationId)) {
            neighbors.add(line.getArrivalStationId());
        }
        
        // Incoming edges
        for (Line line : getByArrival(stationId)) {
            neighbors.add(line.getDepartureStationId());
        }
        
        return neighbors;
    }
    
    /**
     * Gets the number of lines in the repository.
     *
     * @return the number of lines
     */
    public int size() {
        return lines.size();
    }
    
    /**
     * Checks if the repository is empty.
     *
     * @return true if empty, false otherwise
     */
    public boolean isEmpty() {
        return lines.isEmpty();
    }
    
    /**
     * Clears all lines from the repository.
     */
    public void clear() {
        lines.clear();
        linesByDeparture.clear();
        linesByArrival.clear();
    }
    
    /**
     * Gets an iterator over all lines.
     *
     * @return iterator over lines
     */
    public Iterator<Line> iterator() {
        return lines.iterator();
    }
}

