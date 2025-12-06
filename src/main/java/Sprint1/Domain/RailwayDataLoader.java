package Sprint1.Domain;

import Sprint2.Domain.LineSegment;
import Sprint2.Domain.ENUM.Country;

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
    
    private List<Facility> facilities;
    private List<LineSegment> lineSegments;
    private Map<String, Facility> stationMap;

    /**
     * Constructs a new RailwayDataLoader.
     */
    public RailwayDataLoader() {
        this.facilities = new ArrayList<>();
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
                            Facility facility = new Facility(id, name);
                            facilities.add(facility);
                            stationMap.put(id, facility);
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
                            
                            Facility startFacility = stationMap.get(startId);
                            Facility endFacility = stationMap.get(endId);
                            
                            if (startFacility != null && endFacility != null) {
                                // Use actual length from CSV instead of estimation
                                
                                // Assume electrified for main lines
                                boolean electrified = isMainLine(name);
                                
                                LineSegment segment = new LineSegment(id, name, startFacility, endFacility,
                                                                    lengthKm, gauge, electrified);
                                lineSegments.add(segment);
                                loadedCount++;
                            } else {
                                System.err.println("Station not found - Start: " + startFacility + ", End: " + endFacility);
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
    public List<Facility> getStations() {
        return new ArrayList<>(facilities);
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
    public Facility getStationById(String id) {
        return stationMap.get(id);
    }

    /**
     * Gets the number of loaded stations.
     *
     * @return number of stations
     */
    public int getStationCount() {
        return facilities.size();
    }

    /**
     * Gets the number of loaded line segments.
     *
     * @return number of line segments
     */
    public int getLineSegmentCount() {
        return lineSegments.size();
    }
    
    /**
     * Loads stations with geographical data from CSV file (USEI08).
     * Expected CSV format: ID;Name;Latitude;Longitude;Country;TimeZone;TimeZoneGroup;IsCity;IsMainStation
     * 
     * Validates: non-empty name, country, time zone group; 
     *            latitude ∈ [-90,90], longitude ∈ [-180,180]
     * Rejects invalid rows.
     *
     * @param filePath the path to the CSV file with geographical data
     * @throws IOException if there's an error reading the file
     */
    public void loadStationsWithGeographicalData(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        
        if (!Files.exists(path)) {
            throw new IOException("File not found: " + filePath);
        }
        
        System.out.println("Loading stations with geographical data from: " + filePath);
        
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
                
                String[] parts = line.split(";");
                
                // Expected format: ID;Name;Latitude;Longitude;Country;TimeZone;TimeZoneGroup;IsCity;IsMainStation
                if (parts.length >= 9) {
                    try {
                        String id = parts[0].trim();
                        String name = parts[1].trim();
                        String latStr = parts[2].trim();
                        String lonStr = parts[3].trim();
                        String countryStr = parts[4].trim();
                        String timeZone = parts[5].trim();
                        String timeZoneGroup = parts[6].trim();
                        String isCityStr = parts[7].trim();
                        String isMainStationStr = parts[8].trim();
                        
                        // Validate required fields
                        if (id.isEmpty() || name.isEmpty() || countryStr.isEmpty() || timeZoneGroup.isEmpty()) {
                            System.err.println("Line " + lineNumber + " rejected: Missing required fields");
                            rejectedCount++;
                            continue;
                        }
                        
                        // Parse and validate coordinates
                        Double latitude = null;
                        Double longitude = null;
                        
                        if (!latStr.isEmpty()) {
                            latitude = Double.parseDouble(latStr);
                            if (latitude < -90 || latitude > 90) {
                                System.err.println("Line " + lineNumber + " rejected: Invalid latitude " + latitude);
                                rejectedCount++;
                                continue;
                            }
                        }
                        
                        if (!lonStr.isEmpty()) {
                            longitude = Double.parseDouble(lonStr);
                            if (longitude < -180 || longitude > 180) {
                                System.err.println("Line " + lineNumber + " rejected: Invalid longitude " + longitude);
                                rejectedCount++;
                                continue;
                            }
                        }
                        
                        // Parse country
                        Country country = Country.fromString(countryStr);
                        if (country == null && !countryStr.equalsIgnoreCase("all") && !countryStr.isEmpty()) {
                            System.err.println("Line " + lineNumber + " rejected: Invalid country " + countryStr);
                            rejectedCount++;
                            continue;
                        }
                        
                        // Parse boolean flags
                        Boolean isCity = parseBoolean(isCityStr);
                        Boolean isMainStation = parseBoolean(isMainStationStr);
                        
                        // Create facility with geographical data
                        Facility facility = new Facility(id, name, "", latitude, longitude, country,
                                                         isCity, isMainStation, timeZone, timeZoneGroup);
                        
                        // Update existing facility or add new one
                        Facility existing = stationMap.get(id);
                        if (existing != null) {
                            // Update existing facility with geographical data
                            facilities.remove(existing);
                        }
                        
                        facilities.add(facility);
                        stationMap.put(id, facility);
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
                    System.err.println("Line " + lineNumber + " rejected: Insufficient columns (expected 9, got " + parts.length + ")");
                    rejectedCount++;
                }
            }
            
            System.out.println("Total stations loaded: " + loadedCount);
            System.out.println("Total stations rejected: " + rejectedCount);
        }
    }
    
    /**
     * Parses a boolean string value.
     *
     * @param value the string value (true/false/1/0/yes/no)
     * @return Boolean value, or null if empty/invalid
     */
    private Boolean parseBoolean(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        
        String lower = value.trim().toLowerCase();
        if (lower.equals("true") || lower.equals("1") || lower.equals("yes")) {
            return true;
        } else if (lower.equals("false") || lower.equals("0") || lower.equals("no")) {
            return false;
        }
        
        return null;
    }
    
    /**
     * Loads locomotives from a CSV file.
     * Expected CSV format: locomotiveId,type,maxSpeedKmH,gauge,powerKW
     * 
     * @param filePath the path to the CSV file
     * @return list of loaded locomotives
     * @throws IOException if there's an error reading the file
     */
    public List<Locomotive> loadLocomotivesFromCSV(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        
        if (!Files.exists(path)) {
            throw new IOException("File not found: " + filePath);
        }
        
        List<Locomotive> locomotives = new ArrayList<>();
        
        try (BufferedReader br = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String line;
            boolean firstLine = true;
            int lineNumber = 0;
            int loadedCount = 0;
            
            while ((line = br.readLine()) != null) {
                lineNumber++;
                
                if (firstLine) {
                    firstLine = false;
                    continue; // Skip header
                }
                
                if (line.trim().isEmpty()) {
                    continue; // Skip empty lines
                }
                
                String[] parts = line.split(",");
                
                if (parts.length >= 4) {
                    try {
                        String id = parts[0].trim();
                        String type = parts[1].trim();
                        String maxSpeedStr = parts[2].trim();
                        String gaugeStr = parts[3].trim();
                        
                        if (!id.isEmpty() && !type.isEmpty() && !maxSpeedStr.isEmpty() && !gaugeStr.isEmpty()) {
                            double maxSpeed = Double.parseDouble(maxSpeedStr);
                            double gauge = Double.parseDouble(gaugeStr);
                            
                            Locomotive locomotive = new Locomotive(id, type, maxSpeed, gauge);
                            locomotives.add(locomotive);
                            loadedCount++;
                        }
                    } catch (NumberFormatException e) {
                        System.err.println("Line " + lineNumber + " rejected: Invalid number format - " + e.getMessage());
                    } catch (IllegalArgumentException e) {
                        System.err.println("Line " + lineNumber + " rejected: " + e.getMessage());
                    } catch (Exception e) {
                        System.err.println("Line " + lineNumber + " rejected: " + e.getMessage());
                    }
                }
            }
            
            System.out.println("Total locomotives loaded: " + loadedCount);
        }
        
        return locomotives;
    }
    
    /**
     * Loads wagons from a CSV file.
     * Expected CSV format: wagonId,boxId,sku,qty,expiryDate,receivedAt,emptyWeightKg,maxLoadWeightKg
     * Note: Only wagonId, emptyWeightKg, and maxLoadWeightKg are used. Type defaults to "freight".
     * 
     * @param filePath the path to the CSV file
     * @return list of loaded wagons
     * @throws IOException if there's an error reading the file
     */
    public List<Wagon> loadWagonsFromCSV(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        
        if (!Files.exists(path)) {
            throw new IOException("File not found: " + filePath);
        }
        
        List<Wagon> wagons = new ArrayList<>();
        
        try (BufferedReader br = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String line;
            boolean firstLine = true;
            int lineNumber = 0;
            int loadedCount = 0;
            
            while ((line = br.readLine()) != null) {
                lineNumber++;
                
                if (firstLine) {
                    firstLine = false;
                    continue; // Skip header
                }
                
                if (line.trim().isEmpty()) {
                    continue; // Skip empty lines
                }
                
                String[] parts = line.split(",");
                
                if (parts.length >= 8) {
                    try {
                        String wagonId = parts[0].trim();
                        // parts[1] = boxId (not used)
                        // parts[2] = sku (not used)
                        // parts[3] = qty (not used)
                        // parts[4] = expiryDate (not used)
                        // parts[5] = receivedAt (not used)
                        String emptyWeightStr = parts[6].trim();
                        String maxLoadWeightStr = parts[7].trim();
                        
                        if (!wagonId.isEmpty() && !emptyWeightStr.isEmpty() && !maxLoadWeightStr.isEmpty()) {
                            double emptyWeight = Double.parseDouble(emptyWeightStr);
                            double maxLoadWeight = Double.parseDouble(maxLoadWeightStr);
                            
                            // Use "freight" as default type since it's not in the CSV
                            Wagon wagon = new Wagon(wagonId, "freight", emptyWeight, maxLoadWeight);
                            wagons.add(wagon);
                            loadedCount++;
                        }
                    } catch (NumberFormatException e) {
                        System.err.println("Line " + lineNumber + " rejected: Invalid number format - " + e.getMessage());
                    } catch (IllegalArgumentException e) {
                        System.err.println("Line " + lineNumber + " rejected: " + e.getMessage());
                    } catch (Exception e) {
                        System.err.println("Line " + lineNumber + " rejected: " + e.getMessage());
                    }
                }
            }
            
            System.out.println("Total wagons loaded: " + loadedCount);
        }
        
        return wagons;
    }
}
