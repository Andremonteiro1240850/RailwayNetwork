package Sprint2.Domain;

import java.io.*;
import java.util.*;

/**
 * The type Station loader.
 * Optimized version for fast CSV loading.
 */
public class StationLoader {
    
    private boolean verbose = false; // Control logging for performance
    
    /**
     * Load from csv list.
     *
     * @param filePath the file path
     * @return the list
     */
    public List<Station> loadFromCSV(String filePath) {
        return loadFromCSV(filePath, false);
    }
    
    /**
     * Load from csv list with optional verbose logging.
     *
     * @param filePath the file path
     * @param verbose  if true, enables detailed logging (slower)
     * @return the list
     */
    public List<Station> loadFromCSV(String filePath, boolean verbose) {
        this.verbose = verbose;
        
        // Pre-allocate with estimated size (reduces ArrayList resizing)
        List<Station> stations = new ArrayList<>(70000);
        int lineNumber = 0;
        int validStations = 0;
        int totalLines = 0;

        try (BufferedReader br = new BufferedReader(new FileReader(filePath), 8192 * 4)) { // 32KB buffer
            String line = br.readLine(); // Skip header
            lineNumber++;

            if (line == null) {
                if (verbose) {
                    System.err.println("[WARN] Ficheiro vazio: " + filePath);
                }
                return stations;
            }

            while ((line = br.readLine()) != null) {
                lineNumber++;
                totalLines++;
                Station s = parseLineFast(line, lineNumber);
                if (s != null && StationValidator.isValidStation(s)) {
                    stations.add(s);
                    validStations++;
                }
            }

            if (verbose) {
                System.err.println("[INFO] Carregadas " + validStations + " estações válidas de " + totalLines + " linhas");
            }

        } catch (IOException e) {
            System.err.println("[ERRO] Falha ao ler ficheiro: " + e.getMessage());
        }

        return stations;
    }

    /**
     * Fast parsing method - optimized for performance.
     * Removes verbose logging and uses simpler split when possible.
     */
    private Station parseLineFast(String line, int lineNumber) {
        try {
            // Use simple split first (much faster than regex)
            // Only use regex if we detect quoted fields
            String[] parts;
            if (line.indexOf('"') >= 0) {
                // Has quotes, need regex split
                parts = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
            } else {
                // No quotes, simple split is much faster
                parts = line.split(",");
            }

            if (parts.length < 9) {
                return null; // Skip invalid lines silently
            }

            // Fast validation - check empty strings without trim first
            if (parts[0].isEmpty() || parts[1].isEmpty() || 
                parts[2].isEmpty() || parts[3].isEmpty()) {
                return null;
            }

            // Trim only once per field
            String country = parts[0].trim();
            String timeZoneRaw = parseTimeZoneFast(parts[1].trim());
            String timeZoneGroup = parts[2].trim();
            String stationName = parts[3].trim();

            // Fast double parsing - avoid multiple trim calls
            Double latitude = parseDoubleFast(parts[4]);
            Double longitude = parseDoubleFast(parts[5]);

            // Fast validation - check nulls and ranges in one go
            if (latitude == null || longitude == null || 
                latitude < -90.0 || latitude > 90.0 ||
                longitude < -180.0 || longitude > 180.0) {
                return null;
            }

            // Fast boolean parsing
            boolean isCity = parseBooleanFast(parts[6]);
            boolean isMainStation = parseBooleanFast(parts[7]);
            boolean isAirport = parseBooleanFast(parts[8]);

            // Create Station
            return new Station(
                    stationName,
                    latitude,
                    longitude,
                    country,
                    timeZoneRaw,
                    timeZoneGroup,
                    isCity,
                    isMainStation,
                    isAirport
            );

        } catch (Exception e) {
            // Silent failure for performance - only log if verbose
            if (verbose) {
                System.err.println("[WARN] Linha " + lineNumber + ": Erro no parsing - " + e.getMessage());
            }
            return null;
        }
    }
    
    /**
     * Original parseLine method - kept for compatibility if needed.
     */
    private Station parseLine(String line, int lineNumber) {
        try {
            String[] parts = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);

            if (parts.length < 9) {
                if (verbose) {
                    System.err.println("[WARN] Linha " + lineNumber + ": Número incorreto de colunas: " + line);
                }
                return null;
            }

            // Validar campos obrigatórios ANTES de criar Station
            if (!isValidLine(parts)) {
                if (verbose) {
                    System.err.println("[WARN] Linha " + lineNumber + ": Campos obrigatórios em falta");
                }
                return null;
            }

            String country = parts[0].trim();
            String timeZoneRaw = parseTimeZone(parts[1].trim());
            String timeZoneGroup = parts[2].trim();
            String stationName = parts[3].trim();

            // Parse seguro de coordenadas
            Double latitude = parseDoubleSafe(parts[4]);
            Double longitude = parseDoubleSafe(parts[5]);

            // VALIDAÇÃO COMPLETA conforme requisitos:
            // - name, country, timeZoneGroup → não vazios (já validado em isValidLine)
            // - latitude ∈ [-90, 90]
            // - longitude ∈ [-180, 180]
            // Rejeitar linhas inválidas
            
            // Validar que as coordenadas não são nulas
            if (latitude == null || longitude == null) {
                if (verbose) {
                    System.err.println("[WARN] Linha " + lineNumber + ": Coordenadas inválidas (latitude ou longitude nulas)");
                }
                return null;
            }
            
            // Validar range de latitude: latitude ∈ [-90, 90]
            if (latitude < -90.0 || latitude > 90.0) {
                if (verbose) {
                    System.err.println("[WARN] Linha " + lineNumber + ": Latitude fora do range válido [-90, 90]: " + latitude);
                }
                return null;
            }
            
            // Validar range de longitude: longitude ∈ [-180, 180]
            if (longitude < -180.0 || longitude > 180.0) {
                if (verbose) {
                    System.err.println("[WARN] Linha " + lineNumber + ": Longitude fora do range válido [-180, 180]: " + longitude);
                }
                return null;
            }

            boolean isCity = Boolean.parseBoolean(parts[6].trim());
            boolean isMainStation = Boolean.parseBoolean(parts[7].trim());
            boolean isAirport = Boolean.parseBoolean(parts[8].trim());

            // Todas as validações passaram, criar Station
            return new Station(
                    stationName,
                    latitude,
                    longitude,
                    country,
                    timeZoneRaw,
                    timeZoneGroup,
                    isCity,
                    isMainStation,
                    isAirport
            );

        } catch (Exception e) {
            if (verbose) {
                System.err.println("[WARN] Linha " + lineNumber + ": Erro no parsing - " + e.getMessage());
            }
            return null;
        }
    }

    /**
     * Fast timezone parsing - optimized version.
     */
    private String parseTimeZoneFast(String timeZoneRaw) {
        if (timeZoneRaw == null || timeZoneRaw.isEmpty()) {
            return "";
        }
        
        // Fast path: if no quotes or parentheses, return as-is
        if (timeZoneRaw.indexOf('"') < 0 && timeZoneRaw.indexOf('(') < 0) {
            return timeZoneRaw;
        }
        
        // Need processing - use optimized string operations
        int len = timeZoneRaw.length();
        if (len >= 4 && timeZoneRaw.startsWith("('") && timeZoneRaw.endsWith("')")) {
            return timeZoneRaw.substring(2, len - 2);
        } else if (len >= 5 && timeZoneRaw.startsWith("('") && timeZoneRaw.endsWith("',)")) {
            return timeZoneRaw.substring(2, len - 3);
        } else if (len >= 2 && timeZoneRaw.startsWith("(") && timeZoneRaw.endsWith(")")) {
            String result = timeZoneRaw.substring(1, len - 1);
            return result.replace("'", "");
        }
        
        // Remove quotes if present
        return timeZoneRaw.replace("\"", "");
    }
    
    /**
     * Original parseTimeZone - kept for compatibility.
     */
    private String parseTimeZone(String timeZoneRaw) {
        String cleaned = timeZoneRaw.replace("\"", "").trim();

        // Lidar com múltiplos formatos de timezone
        if (cleaned.startsWith("('") && cleaned.endsWith("')")) {
            return cleaned.substring(2, cleaned.length() - 2);
        } else if (cleaned.startsWith("('") && cleaned.endsWith("',)")) {
            return cleaned.substring(2, cleaned.length() - 3);
        } else if (cleaned.startsWith("(") && cleaned.endsWith(")")) {
            return cleaned.substring(1, cleaned.length() - 1).replace("'", "");
        }

        return cleaned;
    }
    
    /**
     * Fast double parsing - optimized version.
     */
    private Double parseDoubleFast(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        
        // Fast path: no whitespace, parse directly
        if (value.indexOf(' ') < 0 && value.indexOf('\t') < 0) {
            try {
                return Double.parseDouble(value);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        
        // Has whitespace, need trim
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    /**
     * Fast boolean parsing - optimized version.
     */
    private boolean parseBooleanFast(String value) {
        if (value == null || value.isEmpty()) {
            return false;
        }
        
        // Fast path: check first character (most common case)
        char first = value.charAt(0);
        
        // Handle whitespace - check if needs trim
        if (first == ' ' || first == '\t') {
            String trimmed = value.trim();
            if (trimmed.isEmpty()) {
                return false;
            }
            first = trimmed.charAt(0);
        }
        
        // Fast character-based parsing
        if (first == 't' || first == 'T' || first == '1') {
            return true;
        } else if (first == 'f' || first == 'F' || first == '0') {
            return false;
        }
        
        // Fallback to standard parsing
        return Boolean.parseBoolean(value.trim());
    }

    /**
     * Original parseDoubleSafe - kept for compatibility.
     */
    private Double parseDoubleSafe(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Valida que os campos obrigatórios não estão vazios.
     * Valida: name, country, timeZoneGroup → não vazios
     * 
     * @param parts array de partes da linha CSV
     * @return true se todos os campos obrigatórios são não vazios
     */
    private boolean isValidLine(String[] parts) {
        // Validar campos obrigatórios não vazios conforme requisitos:
        // - name (station name) → não vazio
        // - country → não vazio
        // - timeZoneGroup → não vazio
        return !parts[0].trim().isEmpty() &&  // country
                !parts[1].trim().isEmpty() &&  // timezone (raw)
                !parts[2].trim().isEmpty() &&  // timezone group
                !parts[3].trim().isEmpty();    // station name
    }
}