package Domain;
import java.io.*;
import java.util.*;

public class StationLoader {
    public List<Station> loadFromCSV(String filePath) {
        List<Station> stations = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line = br.readLine(); // lê cabeçalho e ignora
            if (line == null) return stations; // ficheiro vazio

            while ((line = br.readLine()) != null) {
                Station s = parseLine(line);
                if (s != null && StationValidator.isValidStation(s)) {
                    stations.add(s);
                } else {
                    System.err.println("[WARN] Linha inválida ignorada: " + line);
                }
            }
        } catch (IOException e) {
            System.err.println("[ERRO] Falha ao ler ficheiro: " + e.getMessage());
        }

        return stations;
    }
    private Station parseLine(String line) {
        try {

            String[] parts = line.split(",", -1);

            if (parts.length < 9) return null; // número errado de colunas

            String country = parts[0].trim();


            String timeZone = parts[1]
                    .replace("\"", "")
                    .replace("('", "")
                    .replace("',)", "")
                    .trim();

            String timeZoneGroup = parts[2].trim();
            String stationName = parts[3].trim();
            double latitude = Double.parseDouble(parts[4].trim());
            double longitude = Double.parseDouble(parts[5].trim());
            boolean isCity = Boolean.parseBoolean(parts[6].trim());
            boolean isMainStation = Boolean.parseBoolean(parts[7].trim());
            boolean isAirport = Boolean.parseBoolean(parts[8].trim());

            return new Station(stationName, latitude, longitude, country,
                    timeZone, timeZoneGroup, isCity, isMainStation, isAirport);

        } catch (Exception e) {

            System.err.println("[ERRO] Linha mal formatada: " + line);
            return null;
        }
    }
}
