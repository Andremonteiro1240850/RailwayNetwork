package pt.ipp.isep.dei;

import Controller.InspectionController;
import Controller.ItemController;
import UI.ReturnsUI;
import Domain.ENUM.ReturnReason;
import Domain.PickPath;
import Domain.PickStop;
import Domain.ENUM.PathStrategy;


import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;

public class Main {
    public static void main(String[] args) {
        testUSEI04();
        InspectionController inspectionController = new InspectionController();
        ItemController itemController = new ItemController(inspectionController);

        Path csvPath = Paths.get("train_station_dataset", "returns.csv");
        List<String> results = new ArrayList<>();

        try (BufferedReader br = Files.newBufferedReader(csvPath, StandardCharsets.UTF_8)) {
            String line;
            boolean first = true;
            while ((line = br.readLine()) != null) {
                if (first) { // skip header
                    first = false;
                    continue;
                }
                if (line.trim().isEmpty()) continue;

                String[] parts = line.split(",");
                if (parts.length < 5) continue;

                // returnId from CSV not used in this flow
                String sku = parts[1].trim();
                int qty = Integer.parseInt(parts[2].trim());
                String reasonRaw = parts[3].trim();
                // timestamp from CSV not used in this flow
                String expiryRaw = parts.length > 5 ? parts[5].trim() : "";

                ReturnReason mappedReason = mapReason(reasonRaw);
                LocalDate expiryDate = expiryRaw.isEmpty() ? null : LocalDate.parse(expiryRaw);

                String result = itemController.processReturn(sku, qty, mappedReason.name(), expiryDate);
                results.add(result);
            }
        } catch (IOException e) {
            System.out.println(String.format("Failed to read CSV: %s", e.getMessage()));
        }

        // Pretty UI output with tables
        ReturnsUI ui = new ReturnsUI(inspectionController);
        ui.showDashboard();
    }


    private static ReturnReason mapReason(String csvReason) {
        String r = csvReason.toLowerCase();
        switch (r) {
            case "customer-remorse":
                return ReturnReason.REMORSE;
            case "expired":
                return ReturnReason.EXPIRED;
            case "damaged":
                return ReturnReason.DAMAGED;
            case "package-opened":
            case "wrong-item":
                // treat as remorse for restock per typical flows
                return ReturnReason.REMORSE;
            default:
                return ReturnReason.REMORSE;
        }
    }

    private static void testUSEI04() {
        System.out.println("=== TESTE USEI04 - Pick Path Sequencing ===");

        // Dados de exemplo (simulando output da USEI03)
        // NOTA: Estes dados JÁ DEVEM ESTAR CONSOLIDADOS (sem duplicados)
        List<PickStop> consolidatedStops = Arrays.asList(
                new PickStop(1, 1, 8, 5),
                new PickStop(2, 2, 2, 5),  // Já consolidado: (2,2) tem qty 5
                new PickStop(3, 3, 4, 7)
        );

        System.out.println("Stops consolidados: " + consolidatedStops);

        // Gerar ambos os paths usando o teu PickPath
        Map<PathStrategy, PickPath> results = new HashMap<>();

        // Estratégia A - ASC_SWEEP
        PickPath pathA = new PickPath(PathStrategy.ASC_SWEEP, consolidatedStops);
        results.put(PathStrategy.ASC_SWEEP, pathA);

        // Estratégia B - NEAREST_NEIGHBOUR
        PickPath pathB = new PickPath(PathStrategy.NEAREST_NEIGHBOUR, consolidatedStops);
        results.put(PathStrategy.NEAREST_NEIGHBOUR, pathB);

        // Mostrar resultados
        System.out.println("\n--- ESTRATÉGIA A - ASC SWEEP ---");
        System.out.println("Distância Total: " + pathA.getTotalDistance());
        System.out.println("Caminho: " + pathA.getStops());

        System.out.println("\n--- ESTRATÉGIA B - NEAREST NEIGHBOUR ---");
        System.out.println("Distância Total: " + pathB.getTotalDistance());
        System.out.println("Caminho: " + pathB.getStops());

        System.out.println("\n=== FIM TESTE USEI04 ===\n");
    }
}