package Repository;

import Domain.Connection;
import Domain.Graph;
import Domain.Station;
import Interfaces.GraphRepository;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of GraphRepository for loading Belgian railway network data.
 * 
 * Loads data from CSV files:
 * - stations.csv: Station id,Station,Lat,Lon,CoordX,CoordY
 * - lines.csv: departure_stid,arrival_stid,dist,capacity,cost
 * 
 * This implementation follows Clean Architecture principles:
 * - Domain layer has no dependencies on infrastructure
 * - Invalid data is ignored deterministically
 * - Duplicate stations are preserved (first occurrence kept)
 * - Graph is constructed as undirected
 *
 * @author Development Team
 * @version 1.0
 */
public class BelgiumGraphRepository implements GraphRepository {
    
    private static final String DEFAULT_STATIONS_PATH = "train_station_dataset/Sprint3/stations.csv";
    private static final String DEFAULT_LINES_PATH = "train_station_dataset/Sprint3/lines.csv";
    
    private final String stationsFilePath;
    private final String linesFilePath;
    
    private int loadedStationCount = 0;
    private int loadedConnectionCount = 0;
    private int rejectedStationCount = 0;
    private int rejectedConnectionCount = 0;
    
    /**
     * Constructs a BelgiumGraphRepository with default file paths.
     */
    public BelgiumGraphRepository() {
        this(DEFAULT_STATIONS_PATH, DEFAULT_LINES_PATH);
    }
    
    /**
     * Constructs a BelgiumGraphRepository with custom file paths.
     *
     * @param stationsFilePath the path to the stations CSV file
     * @param linesFilePath    the path to the lines CSV file
     */
    public BelgiumGraphRepository(String stationsFilePath, String linesFilePath) {
        this.stationsFilePath = stationsFilePath;
        this.linesFilePath = linesFilePath;
    }
    
    @Override
    public Graph loadGraph() throws IOException {
        Graph graph = new Graph();
        
        // Reset counters
        loadedStationCount = 0;
        loadedConnectionCount = 0;
        rejectedStationCount = 0;
        rejectedConnectionCount = 0;
        
        // Load stations first (required for connection validation)
        loadStations(graph);
        
        // Load connections (depends on stations being loaded)
        loadConnections(graph);
        
        return graph;
    }
    
    /**
     * Loads stations from the CSV file and adds them to the graph.
     * Expected format: Station id,Station,Lat,Lon,CoordX,CoordY
     * 
     * Duplicate stations (same ID) are preserved (first occurrence kept).
     * Invalid data is ignored deterministically.
     *
     * @param graph the graph to add stations to
     * @throws IOException if there's an error reading the file
     */
    private void loadStations(Graph graph) throws IOException {
        Path path = Paths.get(stationsFilePath);
        
        if (!Files.exists(path)) {
            throw new IOException("Stations file not found: " + stationsFilePath);
        }
        
        try (BufferedReader br = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String line;
            boolean firstLine = true;
            int lineNumber = 0;
            
            while ((line = br.readLine()) != null) {
                lineNumber++;
                
                if (firstLine) {
                    firstLine = false;
                    continue; // Skip header
                }
                
                if (line.trim().isEmpty()) {
                    continue; // Skip empty lines
                }
                
                // CSV format uses comma as separator
                String[] parts = line.split(",");
                
                // Expected format: Station id,Station,Lat,Lon,CoordX,CoordY
                if (parts.length >= 6) {
                    try {
                        String id = parts[0].trim();
                        String name = parts[1].trim();
                        String latStr = parts[2].trim();
                        String lonStr = parts[3].trim();
                        String coordXStr = parts[4].trim();
                        String coordYStr = parts[5].trim();
                        
                        // Validate required fields
                        if (id.isEmpty() || name.isEmpty()) {
                            rejectedStationCount++;
                            continue; // Ignore invalid entry deterministically
                        }
                        
                        // Parse numeric values
                        double latitude = Double.parseDouble(latStr);
                        double longitude = Double.parseDouble(lonStr);
                        double coordX = Double.parseDouble(coordXStr);
                        double coordY = Double.parseDouble(coordYStr);
                        
                        // Validate coordinates
                        if (latitude < -90 || latitude > 90 || longitude < -180 || longitude > 180) {
                            rejectedStationCount++;
                            continue; // Ignore invalid coordinates deterministically
                        }
                        
                        // Create Station and add to graph
                        // Graph.addStation() preserves duplicates (first occurrence kept)
                        Station station = new Station(id, name, latitude, longitude, coordX, coordY);
                        graph.addStation(station);
                        loadedStationCount++;
                        
                    } catch (NumberFormatException e) {
                        rejectedStationCount++;
                        // Ignore invalid number format deterministically
                    } catch (IllegalArgumentException e) {
                        rejectedStationCount++;
                        // Ignore validation errors deterministically
                    }
                } else {
                    rejectedStationCount++;
                    // Ignore lines with insufficient columns deterministically
                }
            }
        }
    }
    
    /**
     * Loads connections from the CSV file and adds them to the graph.
     * Expected format: departure_stid,arrival_stid,dist,capacity,cost
     * 
     * Connections are added in both directions (undirected graph).
     * Invalid data is ignored deterministically.
     * Only distance is used for Connection objects (capacity and cost are available in CSV but not used for MST).
     *
     * @param graph the graph to add connections to
     * @throws IOException if there's an error reading the file
     */
    private void loadConnections(Graph graph) throws IOException {
        Path path = Paths.get(linesFilePath);
        
        if (!Files.exists(path)) {
            throw new IOException("Lines file not found: " + linesFilePath);
        }
        
        // Track which connections we've already added to avoid duplicates
        // Use a deterministic ordering: originId < destinationId
        Map<String, Boolean> processedConnections = new HashMap<>();
        
        try (BufferedReader br = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String line;
            boolean firstLine = true;
            int lineNumber = 0;
            
            while ((line = br.readLine()) != null) {
                lineNumber++;
                
                if (firstLine) {
                    firstLine = false;
                    continue; // Skip header
                }
                
                if (line.trim().isEmpty()) {
                    continue; // Skip empty lines
                }
                
                // CSV format uses comma as separator
                String[] parts = line.split(",");
                
                // Expected format: departure_stid,arrival_stid,dist,capacity,cost
                if (parts.length >= 3) {
                    try {
                        String departureId = parts[0].trim();
                        String arrivalId = parts[1].trim();
                        String distStr = parts[2].trim();
                        
                        // Validate required fields
                        if (departureId.isEmpty() || arrivalId.isEmpty()) {
                            rejectedConnectionCount++;
                            continue; // Ignore invalid entry deterministically
                        }
                        
                        // Skip self-loops
                        if (departureId.equals(arrivalId)) {
                            rejectedConnectionCount++;
                            continue; // Ignore self-loops deterministically
                        }
                        
                        // Parse distance
                        double distance = Double.parseDouble(distStr);
                        
                        // Validate distance
                        if (distance < 0) {
                            rejectedConnectionCount++;
                            continue; // Ignore negative distances deterministically
                        }
                        
                        // Check if stations exist in graph
                        if (!graph.containsStation(departureId)) {
                            rejectedConnectionCount++;
                            continue; // Ignore connections to non-existent stations deterministically
                        }
                        if (!graph.containsStation(arrivalId)) {
                            rejectedConnectionCount++;
                            continue; // Ignore connections to non-existent stations deterministically
                        }
                        
                        // Create deterministic key to avoid duplicate edges
                        // Use lexicographic ordering: smaller ID first
                        String key;
                        String originId;
                        String destinationId;
                        
                        if (departureId.compareTo(arrivalId) < 0) {
                            key = departureId + "->" + arrivalId;
                            originId = departureId;
                            destinationId = arrivalId;
                        } else {
                            key = arrivalId + "->" + departureId;
                            originId = arrivalId;
                            destinationId = departureId;
                        }
                        
                        // Skip if we've already processed this connection
                        if (processedConnections.containsKey(key)) {
                            continue; // Ignore duplicate connections deterministically
                        }
                        
                        // Create Connection and add to graph
                        // Graph.addConnection() adds in both directions (undirected)
                        Connection connection = new Connection(originId, destinationId, distance);
                        graph.addConnection(connection);
                        processedConnections.put(key, true);
                        loadedConnectionCount++;
                        
                    } catch (NumberFormatException e) {
                        rejectedConnectionCount++;
                        // Ignore invalid number format deterministically
                    } catch (IllegalArgumentException e) {
                        rejectedConnectionCount++;
                        // Ignore validation errors deterministically
                    }
                } else {
                    rejectedConnectionCount++;
                    // Ignore lines with insufficient columns deterministically
                }
            }
        }
    }
    
    @Override
    public int getLoadedStationCount() {
        return loadedStationCount;
    }
    
    @Override
    public int getLoadedConnectionCount() {
        return loadedConnectionCount;
    }
    
    @Override
    public int getRejectedStationCount() {
        return rejectedStationCount;
    }
    
    @Override
    public int getRejectedConnectionCount() {
        return rejectedConnectionCount;
    }
}

