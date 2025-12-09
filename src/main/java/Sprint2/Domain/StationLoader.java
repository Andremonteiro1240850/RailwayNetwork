package Sprint2.Domain;
import Sprint1.Domain.Station;

import java.io.*;
import java.util.*;

/**
 * The type Station loader.
 */
public class StationLoader {
    /**
     * Load from csv list.
     *
     * @param filePath the file path
     * @return the list
     */
    public List<Station> loadFromCSV(String filePath) {
        List<Station> stations = new ArrayList<>();
        int lineNumber = 0;
        int validStations = 0;
        int totalLines = 0;

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line = br.readLine();
            lineNumber++;

            if (line == null) {
                System.err.println("[WARN] Ficheiro vazio: " + filePath);
                return stations;
            }

            while ((line = br.readLine()) != null) {
                lineNumber++;
                totalLines++;
                Station s = parseLine(line, lineNumber);
                if (s != null && StationValidator.isValidStation(s)) {
                    stations.add(s);
                    validStations++;
                }
            }

            System.err.println("[INFO] Carregadas " + validStations + " estações válidas de " + totalLines + " linhas");

        } catch (IOException e) {
            System.err.println("[ERRO] Falha ao ler ficheiro: " + e.getMessage());
        }

        return stations;
    }

    private Station parseLine(String line, int lineNumber) {
        try {
            String[] parts = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);

            if (parts.length < 9) {
                System.err.println("[WARN] Linha " + lineNumber + ": Número incorreto de colunas: " + line);
                return null;
            }

            // Validar campos obrigatórios ANTES de criar Station
            if (!isValidLine(parts)) {
                System.err.println("[WARN] Linha " + lineNumber + ": Campos obrigatórios em falta");
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
                System.err.println("[WARN] Linha " + lineNumber + ": Coordenadas inválidas (latitude ou longitude nulas)");
                return null;
            }
            
            // Validar range de latitude: latitude ∈ [-90, 90]
            if (latitude < -90.0 || latitude > 90.0) {
                System.err.println("[WARN] Linha " + lineNumber + ": Latitude fora do range válido [-90, 90]: " + latitude);
                return null;
            }
            
            // Validar range de longitude: longitude ∈ [-180, 180]
            if (longitude < -180.0 || longitude > 180.0) {
                System.err.println("[WARN] Linha " + lineNumber + ": Longitude fora do range válido [-180, 180]: " + longitude);
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
            System.err.println("[WARN] Linha " + lineNumber + ": Erro no parsing - " + e.getMessage());
            return null;
        }
    }

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