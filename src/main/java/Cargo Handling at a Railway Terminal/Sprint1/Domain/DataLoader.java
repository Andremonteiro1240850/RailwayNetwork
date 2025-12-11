package Sprint1.Domain;

import Sprint1.Repository.BoxRepository;
import Sprint2.Domain.ENUM.Country;
import Sprint2.Domain.LineSegment;

import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The type Data loader.
 */
public class DataLoader {
    /**
     * The Items.
     */
    public Map<String, Item> items = new HashMap<>();
    /**
     * The Stock.
     */
    public Map<String, List<Box>> stock = new HashMap<>(); // Keep for backward compatibility
    /**
     * The Orders.
     */
    public Map<String, Order> orders = new HashMap<>();
    /**
     * The Bays.
     */
    public List<Bay> bays = new ArrayList<>();
    /**
     * The Returns.
     */
    public List<Return> returns = new ArrayList<>();
    /**
     * The Box repository.
     */
    public BoxRepository boxRepository = new BoxRepository(); // New repository for boxes
    
    /**
     * The Facilities with geographical data (USEI08).
     */
    public List<Facility> facilities = new ArrayList<>();
    
    /**
     * The Line segments (railway lines).
     */
    public List<LineSegment> lineSegments = new ArrayList<>();
    
    /**
     * Map for quick station lookup by ID.
     */
    private final Map<String, Facility> stationMap = new HashMap<>();
    
    // Contadores não necessários quando ficheiro já fornece lineNo

    // Indexes auxiliares para construir Bays
    private final Map<String, Warehouse> warehouseById = new HashMap<>();
    private final Map<String, Map<Integer, Aisle>> aislesByWarehouse = new HashMap<>();

    // Helpers de parsing seguros
    private static final DateTimeFormatter[] SUPPORTED_DATE_FORMATS = new DateTimeFormatter[] {
            DateTimeFormatter.ISO_LOCAL_DATE,
            DateTimeFormatter.ofPattern("yyyy-MM-dd")
    };

    private static final DateTimeFormatter[] SUPPORTED_DATETIME_FORMATS = new DateTimeFormatter[] {
        DateTimeFormatter.ISO_LOCAL_DATE_TIME,
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
    };

    private static String safeGet(String[] parts, int idx) {
        if (idx < 0 || idx >= parts.length) return null;
        String v = parts[idx];
        return v == null ? null : v.trim();
    }

    private static Integer parseInteger(String raw) {
        if (raw == null || raw.isEmpty()) return null;
        try { return Integer.parseInt(raw.trim()); } catch (Exception ignored) { return null; }
    }

    private static Double parseDoubleValue(String raw) {
        if (raw == null || raw.isEmpty()) return null;
        try { return Double.parseDouble(raw.trim()); } catch (Exception ignored) { return null; }
    }

    private static LocalDate parseLocalDateFlexible(String raw) {
        if (raw == null || raw.isEmpty()) return null;
        String value = raw.trim();
        for (DateTimeFormatter f : SUPPORTED_DATE_FORMATS) {
            try { return LocalDate.parse(value, f); } catch (Exception ignored) { }
        }
        return null;
    }

    private static LocalDateTime parseLocalDateTimeFlexible(String raw) {
        if (raw == null || raw.isEmpty()) return null;
        String value = raw.trim();
        for (DateTimeFormatter f : SUPPORTED_DATETIME_FORMATS) {
            try { return LocalDateTime.parse(value, f); } catch (Exception ignored) { }
        }
        return null;
    }

    /**
     * Load items.
     *
     * @param filePath the file path
     */
// ---------- ITEMS ----------
    public void loadItems(String filePath) {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line = br.readLine(); // header
            while ((line = br.readLine()) != null) {
                String[] p = line.split(",");
                if (p.length < 6) continue;

                String sku = safeGet(p, 0);
                String name = safeGet(p, 1);
                String category = safeGet(p, 2);
                String unit=safeGet(p, 3);
                Double volume = parseDoubleValue(safeGet(p, 4));
                Double weight = parseDoubleValue(safeGet(p, 5));

                if (sku == null || sku.isEmpty()) {
                    System.out.println("WARNING: Linha items com SKU inválido (ignorado)");
                    continue;
                }
                if (volume == null || weight == null) {
                    System.out.println("WARNING: Item " + sku + " com volume/peso inválidos (ignorado)");
                    continue;
                }

                items.put(sku, new Item(sku, name, category,unit, volume, weight));
            }
        } catch (Exception e) {
            System.out.println("Erro a ler items: " + e.getMessage());
        }
    }

    /**
     * Load wagons.
     *
     * @param filePath the file path
     */
// ---------- WAGONS ----------
    public void loadWagons(String filePath) {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            br.readLine(); // header
            String line;
            while ((line = br.readLine()) != null) {
                String[] p = line.split(",");
                if (p.length < 6) continue;

                String wagonId = safeGet(p, 0);
                String boxId = safeGet(p, 1);
                String sku = safeGet(p, 2);
                Integer qty = parseInteger(safeGet(p, 3));
                LocalDate expiry = parseLocalDateFlexible(safeGet(p, 4));
                LocalDateTime receivedAt = parseLocalDateTimeFlexible(safeGet(p, 5));

                if (!items.containsKey(sku)) {
                    System.out.println("WARNING: SKU " + sku + " não existe no catálogo (ignorado)");
                    continue;
                }
                if (boxId == null || boxId.isEmpty()) {
                    System.out.println("WARNING: BoxId inválido (ignorado)");
                    continue;
                }
                if (qty == null || qty <= 0) {
                    System.out.println("WARNING: Quantidade inválida para box " + boxId + " (ignorado)");
                    continue;
                }
                if (receivedAt == null) {
                    System.out.println("WARNING: Data/hora de receção inválida para box " + boxId + " (ignorado)");
                    continue;
                }

                // Create box without location initially
                Box box = new Box(boxId, sku, qty, expiry, receivedAt);
                
                // Add to both old structure (for backward compatibility) and new repository
                stock.computeIfAbsent(sku, k -> new ArrayList<>()).add(box);
                boxRepository.addUnassignedBox(box);
            }
        } catch (Exception e) {
            System.out.println("Erro a ler wagons: " + e.getMessage());
        }
    }

    /**
     * Load orders.
     *
     * @param ordersFile the orders file
     * @param linesFile  the lines file
     */
// ---------- ORDERS ----------
    public void loadOrders(String ordersFile, String linesFile) {
        // Ler orders
        try (BufferedReader br = new BufferedReader(new FileReader(ordersFile))) {
            br.readLine();
            String line;
            while ((line = br.readLine()) != null) {
                String[] p = line.split(",");
                if (p.length < 3) continue;

                String id = safeGet(p, 0);
                LocalDateTime dueDate = parseLocalDateTimeFlexible(safeGet(p, 1));
                Integer priority = parseInteger(safeGet(p, 2));

                if (id == null || id.isEmpty()) {
                    System.out.println("WARNING: Order com ID inválido (ignorado)");
                    continue;
                }
                if (dueDate == null) {
                    System.out.println("WARNING: Order " + id + " com dueDate inválido (ignorado)");
                    continue;
                }
                if (priority == null) {
                    System.out.println("WARNING: Order " + id + " com prioridade inválida (ignorado)");
                    continue;
                }

                orders.put(id, new Order(id, dueDate, priority));
            }
        } catch (Exception e) {
            System.out.println("Erro a ler orders: " + e.getMessage());
        }

        // Ler order_lines
        try (BufferedReader br = new BufferedReader(new FileReader(linesFile))) {
            br.readLine();
            String line;
            while ((line = br.readLine()) != null) {
                String[] p = line.split(",");
                if (p.length < 4) continue;

                String orderId = safeGet(p, 0);
                Integer lineNo = parseInteger(safeGet(p, 1));
                String sku = safeGet(p, 2);
                Integer qty = parseInteger(safeGet(p, 3));

                if (!items.containsKey(sku)) {
                    System.out.println("WARNING: SKU " + sku + " não existe no catálogo (ignorado)");
                    continue;
                }
                if (orderId == null || orderId.isEmpty()) {
                    System.out.println("WARNING: Linha de encomenda com orderId inválido (ignorado)");
                    continue;
                }
                if (lineNo == null || lineNo <= 0) {
                    System.out.println("WARNING: Linha de encomenda " + orderId + " com lineNo inválido (ignorado)");
                    continue;
                }
                if (qty == null || qty <= 0) {
                    System.out.println("WARNING: Linha de encomenda " + orderId + " com quantidade inválida (ignorado)");
                    continue;
                }

                Order order = orders.get(orderId);
                if (order != null) {
                    order.addLine(new OrderLine(lineNo, sku, qty));
                }
            }
        } catch (Exception e) {
            System.out.println("Erro a ler order_lines: " + e.getMessage());
        }
    }

    /**
     * Load bays.
     *
     * @param filePath the file path
     */
// ---------- BAYS ----------
    public void loadBays(String filePath) {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            br.readLine(); // header
            String line;
            while ((line = br.readLine()) != null) {
                // ficheiro separado por ';'
                String[] p = line.split(";");
                if (p.length < 4) continue;

                String warehouseId = safeGet(p, 0);
                Integer aisleNo = parseInteger(safeGet(p, 1));
                Integer bayId = parseInteger(safeGet(p, 2));
                Integer capacity = parseInteger(safeGet(p, 3));

                if (warehouseId == null || warehouseId.isEmpty() || aisleNo == null || bayId == null || capacity == null) {
                    System.out.println("WARNING: Bay com campos inválidos (ignorado)");
                    continue;
                }
                if (capacity <= 0) {
                    System.out.println("WARNING: Bay " + aisleNo + "-" + bayId + " tem capacidade inválida: " + capacity);
                    continue;
                }

                BayId bayIdObj = new BayId(warehouseId, aisleNo, bayId);
                Bay b = new Bay(bayIdObj, capacity);
                bays.add(b);
            }
        } catch (Exception e) {
            System.out.println("Erro a ler bays: " + e.getMessage());
        }
    }

    /**
     * Load returns.
     *
     * @param filePath the file path
     */
// ---------- RETURNS ----------
    public void loadReturns(String filePath) {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            br.readLine(); // header
            String line;
            while ((line = br.readLine()) != null) {
                String[] p = line.split(",");
                if (p.length < 6) continue;

                String returnId = safeGet(p, 0);
                String sku = safeGet(p, 1);
                Integer qty = parseInteger(safeGet(p, 2));
                String reason = safeGet(p, 3);
                LocalDateTime timestamp = parseLocalDateTimeFlexible(safeGet(p, 4));
                LocalDate expiry = parseLocalDateFlexible(safeGet(p, 5));

                // validações
                if (!items.containsKey(sku)) {
                    System.out.println("WARNING: SKU " + sku + " não existe no catálogo (ignorado)");
                    continue;
                }
                if (returnId == null || returnId.isEmpty()) {
                    System.out.println("WARNING: Devolução com ID inválido (ignorado)");
                    continue;
                }
                if (qty == null || qty <= 0) {
                    System.out.println("WARNING: Devolução " + returnId + " tem quantidade inválida: " + qty);
                    continue;
                }
                if (timestamp == null) {
                    System.out.println("WARNING: Devolução " + returnId + " com timestamp inválido (ignorado)");
                    continue;
                }

                Return r = new Return(returnId, sku, qty, reason, timestamp, expiry);
                returns.add(r);
            }
        } catch (Exception e) {
            System.out.println("Erro a ler returns: " + e.getMessage());
        }
    }

    /**
     * Gets bays.
     *
     * @return the bays
     */
    public List<Bay> getBays() {
        return bays;
    }

    /**
     * Gets box repository.
     *
     * @return the box repository
     */
    public BoxRepository getBoxRepository() {
        return boxRepository;
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
     */
    public void loadStationsWithGeographicalData(String filePath) {
        Path path = Paths.get(filePath);
        
        if (!Files.exists(path)) {
            System.out.println("WARNING: File not found: " + filePath);
            return;
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
                        String id = safeGet(parts, 0);
                        String name = safeGet(parts, 1);
                        String latStr = safeGet(parts, 2);
                        String lonStr = safeGet(parts, 3);
                        String countryStr = safeGet(parts, 4);
                        String timeZone = safeGet(parts, 5);
                        String timeZoneGroup = safeGet(parts, 6);
                        String isCityStr = safeGet(parts, 7);
                        String isMainStationStr = safeGet(parts, 8);
                        
                        // Validate required fields
                        if (id == null || id.isEmpty() || name == null || name.isEmpty() || 
                            countryStr == null || countryStr.isEmpty() || 
                            timeZoneGroup == null || timeZoneGroup.isEmpty()) {
                            System.out.println("WARNING: Line " + lineNumber + " rejected: Missing required fields");
                            rejectedCount++;
                            continue;
                        }
                        
                        // Parse and validate coordinates
                        Double latitude = null;
                        Double longitude = null;
                        
                        if (latStr != null && !latStr.isEmpty()) {
                            latitude = parseDoubleValue(latStr);
                            if (latitude == null || latitude < -90 || latitude > 90) {
                                System.out.println("WARNING: Line " + lineNumber + " rejected: Invalid latitude " + latStr);
                                rejectedCount++;
                                continue;
                            }
                        }
                        
                        if (lonStr != null && !lonStr.isEmpty()) {
                            longitude = parseDoubleValue(lonStr);
                            if (longitude == null || longitude < -180 || longitude > 180) {
                                System.out.println("WARNING: Line " + lineNumber + " rejected: Invalid longitude " + lonStr);
                                rejectedCount++;
                                continue;
                            }
                        }
                        
                        // Parse country
                        Country country = Country.fromString(countryStr);
                        if (country == null && !countryStr.equalsIgnoreCase("all") && !countryStr.isEmpty()) {
                            System.out.println("WARNING: Line " + lineNumber + " rejected: Invalid country " + countryStr);
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
                        
                    } catch (IllegalArgumentException e) {
                        System.out.println("WARNING: Line " + lineNumber + " rejected: " + e.getMessage());
                        rejectedCount++;
                    } catch (Exception e) {
                        System.out.println("WARNING: Line " + lineNumber + " rejected: " + e.getMessage());
                        rejectedCount++;
                    }
                } else {
                    System.out.println("WARNING: Line " + lineNumber + " rejected: Insufficient columns (expected 9, got " + parts.length + ")");
                    rejectedCount++;
                }
            }
            
            System.out.println(" Total stations loaded: " + loadedCount);
            if (rejectedCount > 0) {
                System.out.println("WARNING: Total stations rejected: " + rejectedCount);
            }
        } catch (Exception e) {
            System.out.println("Erro a ler stations com dados geográficos: " + e.getMessage());
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
     * Gets facilities with geographical data.
     *
     * @return list of facilities
     */
    public List<Facility> getFacilities() {
        return new ArrayList<>(facilities);
    }
    
    // ---------- RAILWAY DATA LOADING (from RailwayDataLoader) ----------
    
    /**
     * Loads all railway data from CSV files.
     *
     * @param facilityFilePath path to facility CSV file
     * @param lineFilePath     path to line CSV file
     */
    public void loadAllRailwayData(String facilityFilePath, String lineFilePath) {
        loadStations(facilityFilePath);
        loadLineSegments(lineFilePath);
    }
    
    /**
     * Loads stations from the facility CSV file.
     *
     * @param filePath the path to the facility CSV file
     */
    public void loadStations(String filePath) {
        Path path = Paths.get(filePath);
        
        if (!Files.exists(path)) {
            System.out.println("WARNING: File not found: " + filePath);
            return;
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
                    String id = safeGet(parts, 0);
                    String name = safeGet(parts, 1);
                    
                    if (id != null && !id.isEmpty() && name != null && !name.isEmpty()) {
                        try {
                            Facility facility = new Facility(id, name);
                            facilities.add(facility);
                            stationMap.put(id, facility);
                            loadedCount++;
                        } catch (Exception e) {
                            System.out.println("WARNING: Error creating station from line " + lineNumber + ": " + e.getMessage());
                        }
                    }
                }
            }
            
            System.out.println(" Total stations loaded: " + loadedCount);
        } catch (Exception e) {
            System.out.println("Erro a ler stations: " + e.getMessage());
        }
    }
    
    /**
     * Loads line segments from the line CSV file.
     * The CSV file should contain columns: ID;Name;Owner;Start ID;Start;End ID;End;Gauge;Length_m
     *
     * @param filePath the path to the line CSV file
     */
    public void loadLineSegments(String filePath) {
        Path path = Paths.get(filePath);
        
        if (!Files.exists(path)) {
            System.out.println("WARNING: File not found: " + filePath);
            return;
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
                    continue; // Skip empty lines
                }
                
                String[] parts = line.split(";");
                
                if (parts.length >= 9) {
                    String id = safeGet(parts, 0);
                    String name = safeGet(parts, 1);
                    String startId = safeGet(parts, 3);
                    String endId = safeGet(parts, 5);
                    String gaugeStr = safeGet(parts, 7);
                    String lengthStr = safeGet(parts, 8);
                    
                    if (id != null && !id.isEmpty() && name != null && !name.isEmpty() && 
                        startId != null && !startId.isEmpty() && endId != null && !endId.isEmpty() && 
                        gaugeStr != null && !gaugeStr.isEmpty() && lengthStr != null && !lengthStr.isEmpty()) {
                        
                        try {
                            double gauge = Double.parseDouble(gaugeStr);
                            double lengthMeters = Double.parseDouble(lengthStr);
                            double lengthKm = lengthMeters / 1000.0; // Convert meters to kilometers
                            
                            Facility startFacility = stationMap.get(startId);
                            Facility endFacility = stationMap.get(endId);
                            
                            if (startFacility != null && endFacility != null) {
                                // Assume electrified for main lines
                                boolean electrified = isMainLine(name);
                                
                                LineSegment segment = new LineSegment(id, name, startFacility, endFacility,
                                                                    lengthKm, gauge, electrified);
                                lineSegments.add(segment);
                                loadedCount++;
                            } else {
                                System.out.println("WARNING: Station not found - Start: " + startFacility + ", End: " + endFacility);
                            }
                        } catch (NumberFormatException e) {
                            System.out.println("WARNING: Invalid numeric value in line " + lineNumber + ": " + e.getMessage());
                        } catch (Exception e) {
                            System.out.println("WARNING: Error creating line segment from line " + lineNumber + ": " + e.getMessage());
                        }
                    }
                }
            }
            
            System.out.println(" Total line segments loaded: " + loadedCount);
        } catch (Exception e) {
            System.out.println("Erro a ler line segments: " + e.getMessage());
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
        return lineName != null && lineName.toLowerCase().contains("ramal") && 
               (lineName.toLowerCase().contains("porto") || 
                lineName.toLowerCase().contains("campanhã"));
    }
    
    /**
     * Gets all loaded stations (alias for getFacilities for compatibility).
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
    
}


