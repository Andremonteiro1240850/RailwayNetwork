package Domain;

import Repository.StationRepository;
import Repository.LineRepository;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Data loader for railway network data from CSV files.
 * Loads stations and lines from Sprint3 CSV files and populates repositories.
 * 
 * This class follows the Repository pattern, separating data loading from data storage.
 * The loaded data is stored in StationRepository and LineRepository for efficient access
 * by graph algorithms and optimization routines.
 *
 * @author Development Team
 * @version 2.0
 */
public class DataLoader {
    
    private StationRepository stationRepository;
    private LineRepository lineRepository;
    
    /**
     * Constructs a new DataLoader with empty repositories.
     */
    public DataLoader() {
        this.stationRepository = new StationRepository();
        this.lineRepository = new LineRepository();
    }
    
    /**
     * Loads all railway network data from CSV files and populates repositories.
     *
     * @param stationsFilePath the path to the stations CSV file
     * @param linesFilePath    the path to the lines CSV file
     * @throws IOException if there's an error reading the files
     */
    public void loadAllData(String stationsFilePath, String linesFilePath) throws IOException {
        // Clear existing data
        stationRepository.clear();
        lineRepository.clear();
        
        // Load stations first (required for line validation)
        loadStations(stationsFilePath);
        
        // Load lines (depends on stations being loaded)
        loadLines(linesFilePath);
        
        System.out.println("\n=== DATA LOADING COMPLETE ===");
        System.out.println("Stations loaded: " + stationRepository.size());
        System.out.println("Lines loaded: " + lineRepository.size());
    }
    
    /**
     * Loads stations from the CSV file and adds them to StationRepository.
     * Expected format: Station id,Station,Lat,Lon,CoordX,CoordY
     *
     * @param filePath the path to the stations CSV file
     * @throws IOException if there's an error reading the file
     */
    private void loadStations(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        
        if (!Files.exists(path)) {
            throw new IOException("File not found: " + filePath);
        }
        
        System.out.println("\nLoading stations from: " + filePath);
        
        try (BufferedReader br = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String line;
            boolean firstLine = true;
            int lineNumber = 0;
            int loadedCount = 0;
            int rejectedCount = 0;
            
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
                        
                        if (id.isEmpty() || name.isEmpty()) {
                            System.err.println("Line " + lineNumber + " rejected: Missing required fields (id or name)");
                            rejectedCount++;
                            continue;
                        }
                        
                        double latitude = Double.parseDouble(latStr);
                        double longitude = Double.parseDouble(lonStr);
                        double coordX = Double.parseDouble(coordXStr);
                        double coordY = Double.parseDouble(coordYStr);
                        
                        // Validate coordinates
                        if (latitude < -90 || latitude > 90) {
                            System.err.println("Line " + lineNumber + " rejected: Invalid latitude " + latitude);
                            rejectedCount++;
                            continue;
                        }
                        if (longitude < -180 || longitude > 180) {
                            System.err.println("Line " + lineNumber + " rejected: Invalid longitude " + longitude);
                            rejectedCount++;
                            continue;
                        }
                        
                        // Create Station object and add to repository
                        Station station = new Station(id, name, latitude, longitude, coordX, coordY);
                        stationRepository.add(station);
                        loadedCount++;
                        
                    } catch (NumberFormatException e) {
                        System.err.println("Line " + lineNumber + " rejected: Invalid number format - " + e.getMessage());
                        rejectedCount++;
                    } catch (IllegalArgumentException e) {
                        System.err.println("Line " + lineNumber + " rejected: " + e.getMessage());
                        rejectedCount++;
                    } catch (Exception e) {
                        System.err.println("Line " + lineNumber + " rejected: " + e.getMessage());
                        rejectedCount++;
                    }
                } else {
                    System.err.println("Line " + lineNumber + " rejected: Insufficient columns (expected 6, got " + parts.length + ")");
                    rejectedCount++;
                }
            }
            
            System.out.println("Total stations loaded: " + loadedCount);
            if (rejectedCount > 0) {
                System.out.println("Total stations rejected: " + rejectedCount);
            }
        }
    }
    
    /**
     * Loads lines from the CSV file and adds them to LineRepository.
     * Expected format: departure_stid,arrival_stid,dist,capacity,cost
     *
     * @param filePath the path to the lines CSV file
     * @throws IOException if there's an error reading the file
     */
    private void loadLines(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        
        if (!Files.exists(path)) {
            throw new IOException("File not found: " + filePath);
        }
        
        System.out.println("\nLoading lines from: " + filePath);
        
        try (BufferedReader br = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String line;
            boolean firstLine = true;
            int lineNumber = 0;
            int loadedCount = 0;
            int rejectedCount = 0;
            
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
                
                // CSV format uses comma as separator
                String[] parts = line.split(",");
                
                // Expected format: departure_stid,arrival_stid,dist,capacity,cost
                if (parts.length >= 5) {
                    try {
                        String departureId = parts[0].trim();
                        String arrivalId = parts[1].trim();
                        String distStr = parts[2].trim();
                        String capacityStr = parts[3].trim();
                        String costStr = parts[4].trim();
                        
                        if (departureId.isEmpty() || arrivalId.isEmpty()) {
                            System.err.println("Line " + lineNumber + " rejected: Missing station IDs");
                            rejectedCount++;
                            continue;
                        }
                        
                        double distance = Double.parseDouble(distStr);
                        int capacity = Integer.parseInt(capacityStr);
                        double cost = Double.parseDouble(costStr);
                        
                        // Validate that both stations exist in repository
                        if (!stationRepository.exists(departureId)) {
                            System.err.println("Line " + lineNumber + " rejected: Departure station not found: " + departureId);
                            rejectedCount++;
                            continue;
                        }
                        if (!stationRepository.exists(arrivalId)) {
                            System.err.println("Line " + lineNumber + " rejected: Arrival station not found: " + arrivalId);
                            rejectedCount++;
                            continue;
                        }
                        
                        // Create Line object and add to repository
                        Line lineObj = new Line(departureId, arrivalId, distance, capacity, cost);
                        lineRepository.add(lineObj);
                        loadedCount++;
                        
                    } catch (NumberFormatException e) {
                        System.err.println("Line " + lineNumber + " rejected: Invalid number format - " + e.getMessage());
                        rejectedCount++;
                    } catch (IllegalArgumentException e) {
                        System.err.println("Line " + lineNumber + " rejected: " + e.getMessage());
                        rejectedCount++;
                    } catch (Exception e) {
                        System.err.println("Line " + lineNumber + " rejected: " + e.getMessage());
                        rejectedCount++;
                    }
                } else {
                    System.err.println("Line " + lineNumber + " rejected: Insufficient columns (expected 5, got " + parts.length + ")");
                    rejectedCount++;
                }
            }
            
            System.out.println("Total lines loaded: " + loadedCount);
            if (rejectedCount > 0) {
                System.out.println("Total lines rejected: " + rejectedCount);
            }
        }
    }
    
    /**
     * Gets the StationRepository containing all loaded stations.
     *
     * @return the station repository
     */
    public StationRepository getStationRepository() {
        return stationRepository;
    }
    
    /**
     * Gets the LineRepository containing all loaded lines.
     *
     * @return the line repository
     */
    public LineRepository getLineRepository() {
        return lineRepository;
    }
    
    /**
     * Gets the number of loaded stations.
     *
     * @return number of stations
     */
    public int getStationCount() {
        return stationRepository.size();
    }
    
    /**
     * Gets the number of loaded lines.
     *
     * @return number of lines
     */
    public int getLineCount() {
        return lineRepository.size();
    }
}
