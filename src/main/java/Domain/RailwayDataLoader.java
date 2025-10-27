package Domain;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Data loader for railway stations and line segments from CSV files.
 * This class loads real railway data from facility.csv and line.csv files.
 *
 * @author Development Team
 * @version 1.0
 */
public class RailwayDataLoader {
    
    private List<Station> stations;
    private List<LineSegment> lineSegments;
    private Map<String, Station> stationMap;

    /**
     * Constructs a new RailwayDataLoader.
     */
    public RailwayDataLoader() {
        this.stations = new ArrayList<>();
        this.lineSegments = new ArrayList<>();
        this.stationMap = new HashMap<>();
    }

    /**
     * Loads all railway data from CSV files.
     *
     * @throws IOException if there's an error reading the files
     */
    public void loadAllData() throws IOException {
        loadStations("train_station_dataset/facility.csv");
        loadLineSegments("train_station_dataset/line.csv");
    }

    /**
     * Loads stations from the facility CSV file.
     *
     * @param filePath the path to the facility CSV file
     * @throws IOException if there's an error reading the file
     */
    public void loadStations(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        
        if (!Files.exists(path)) {
            throw new IOException("File not found: " + filePath);
        }
        
        System.out.println("Loading stations from: " + filePath);
        
        try (BufferedReader br = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String line;
            boolean firstLine = true;
            int lineNumber = 0;
            int loadedCount = 0;
            
            while ((line = br.readLine()) != null) {
                lineNumber++;
                
                if (firstLine) {
                    firstLine = false;
                    System.out.println("Header: " + line);
                    continue; // Skip header
                }
                
                if (line.trim().isEmpty()) {
                    continue; // Skip empty lines
                }
                
                String[] parts = line.split(";");
                
                if (parts.length >= 2) {
                    String id = parts[0].trim();
                    String name = parts[1].trim();
                    
                    if (!id.isEmpty() && !name.isEmpty()) {
                        try {
                            Station station = new Station(id, name);
                            stations.add(station);
                            stationMap.put(id, station);
                            loadedCount++;
                        } catch (Exception e) {
                            System.err.println("Error creating station from line " + lineNumber + ": " + e.getMessage());
                        }
                    }
                }
            }
            
            System.out.println("Total stations loaded: " + loadedCount);
        }
    }

    /**
     * Loads line segments from the line CSV file.
     * The CSV file should contain columns: ID, Name, Owner, Start ID, Start, End ID, End, Gauge, Length_m
     *
     * @param filePath the path to the line CSV file
     * @throws IOException if there's an error reading the file
     */
    public void loadLineSegments(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        
        if (!Files.exists(path)) {
            throw new IOException("File not found: " + filePath);
        }
        
        System.out.println("Loading line segments from: " + filePath);
        
        try (BufferedReader br = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String line;
            boolean firstLine = true;
            int lineNumber = 0;
            int loadedCount = 0;
            
            while ((line = br.readLine()) != null) {
                lineNumber++;
                
                if (firstLine) {
                    firstLine = false;
                    System.out.println("Header: " + line);
                    continue; // Skip header
                }
                
                if (line.trim().isEmpty()) {
                    System.out.println("Skipping empty line " + lineNumber);
                    continue; // Skip empty lines
                }
                
                String[] parts = line.split(";");
                
                if (parts.length >= 9) {
                    String id = parts[0].trim();
                    String name = parts[1].trim();
                    String startId = parts[3].trim();
                    String endId = parts[5].trim();
                    String gaugeStr = parts[7].trim();
                    String lengthStr = parts[8].trim();
                    
                    if (!id.isEmpty() && !name.isEmpty() && !startId.isEmpty() && 
                        !endId.isEmpty() && !gaugeStr.isEmpty() && !lengthStr.isEmpty()) {
                        
                        try {
                            double gauge = Double.parseDouble(gaugeStr);
                            double lengthMeters = Double.parseDouble(lengthStr);
                            double lengthKm = lengthMeters / 1000.0; // Convert meters to kilometers
                            
                            Station startStation = stationMap.get(startId);
                            Station endStation = stationMap.get(endId);
                            
                            if (startStation != null && endStation != null) {
                                // Use actual length from CSV instead of estimation
                                
                                // Assume electrified for main lines
                                boolean electrified = isMainLine(name);
                                
                                LineSegment segment = new LineSegment(id, name, startStation, endStation, 
                                                                    lengthKm, gauge, electrified);
                                lineSegments.add(segment);
                                loadedCount++;
                            } else {
                                System.err.println("Station not found - Start: " + startStation + ", End: " + endStation);
                            }
                        } catch (NumberFormatException e) {
                            System.err.println("Invalid numeric value in line " + lineNumber + ": " + e.getMessage());
                        } catch (Exception e) {
                            System.err.println("Error creating line segment from line " + lineNumber + ": " + e.getMessage());
                        }
                    }
                }
            }
            
            System.out.println("Total line segments loaded: " + loadedCount);
        }
    }
    
    
    /**
     * Determines if a line is a main electrified line based on its name.
     * 
     * @param lineName the name of the line
     * @return true if it's a main line, false otherwise
     */
    private boolean isMainLine(String lineName) {
        // Main lines in Portugal are typically electrified
        // This is a simplified approach - in a real system, you'd have electrification data
        return lineName.toLowerCase().contains("ramal") && 
               (lineName.toLowerCase().contains("porto") || 
                lineName.toLowerCase().contains("campanhã"));
    }

    /**
     * Gets all loaded stations.
     *
     * @return list of stations
     */
    public List<Station> getStations() {
        return new ArrayList<>(stations);
    }

    /**
     * Gets all loaded line segments.
     *
     * @return list of line segments
     */
    public List<LineSegment> getLineSegments() {
        return new ArrayList<>(lineSegments);
    }

    /**
     * Gets a station by its ID.
     *
     * @param id the station ID
     * @return the station or null if not found
     */
    public Station getStationById(String id) {
        return stationMap.get(id);
    }

    /**
     * Gets the number of loaded stations.
     *
     * @return number of stations
     */
    public int getStationCount() {
        return stations.size();
    }

    /**
     * Gets the number of loaded line segments.
     *
     * @return number of line segments
     */
    public int getLineSegmentCount() {
        return lineSegments.size();
    }
}
