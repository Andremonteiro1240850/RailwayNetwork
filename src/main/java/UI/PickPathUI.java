package UI;

import Controller.PickPathController;
import Domain.PickPath;
import Domain.PickStop;
import Domain.ENUM.PathStrategy;

import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * UI para Pick Path Sequencing (USEI04)
 * Interface simples para interagir com o sistema de sequenciação
 */
public class PickPathUI {
    private final PickPathController controller;
    private final Scanner scanner;
    
    public PickPathUI(PickPathController controller) {
        this.controller = controller;
        this.scanner = new Scanner(System.in);
    }
    
    /**
     * Menu principal da USEI04
     */
    public void showMainMenu() {
        while (true) {
            System.out.println("\n=== PICK PATH SEQUENCING (USEI04) ===");
            System.out.println("1. Processar Sequenciação (dados simulados)");
            System.out.println("2. Processar Sequenciação (dados customizados)");
            System.out.println("3. Voltar ao Menu Principal");
            
            System.out.print("Escolha uma opção: ");
            String choice = scanner.nextLine().trim();
            
            switch (choice) {
                case "1":
                    processWithSimulatedData();
                    break;
                case "2":
                    processWithCustomData();
                    break;
                case "3":
                    return;
                default:
                    System.out.println("❌ Opção inválida! Tente novamente.");
            }
        }
    }
    
    /**
     * Processa com dados simulados (como estava no Main)
     */
    private void processWithSimulatedData() {
        System.out.println("\n=== PROCESSAMENTO COM DADOS SIMULADOS ===");
        
        // Simular dados de entrada (quando USEI03 estiver pronta, virá de trolley packing)
        List<PickStop> inputStops = simulateTrolleyPackingOutput();
        
        System.out.println("Stops de entrada (antes da consolidação):");
        displayStops(inputStops);
        
        try {
            // Processar usando o controller
            Map<PathStrategy, PickPath> results = controller.processPickPathSequencing(inputStops);
            
            // Mostrar resultados
            displayPathResults(results);
            
        } catch (Exception e) {
            System.out.println("❌ Erro ao processar: " + e.getMessage());
        }
        
        System.out.println("\nPressione Enter para continuar...");
        scanner.nextLine();
    }
    
    /**
     * Processa com dados customizados pelo utilizador
     */
    private void processWithCustomData() {
        System.out.println("\n=== PROCESSAMENTO COM DADOS CUSTOMIZADOS ===");
        
        try {
            List<PickStop> inputStops = readCustomStops();
            
            if (inputStops.isEmpty()) {
                System.out.println("❌ Nenhum stop foi inserido.");
                return;
            }
            
            System.out.println("\nStops inseridos:");
            displayStops(inputStops);
            
            // Processar usando o controller
            Map<PathStrategy, PickPath> results = controller.processPickPathSequencing(inputStops);
            
            // Mostrar resultados
            displayPathResults(results);
            
        } catch (Exception e) {
            System.out.println("❌ Erro ao processar: " + e.getMessage());
        }
        
        System.out.println("\nPressione Enter para continuar...");
        scanner.nextLine();
    }
    
    /**
     * Lê stops customizados do utilizador
     */
    private List<PickStop> readCustomStops() {
        System.out.println("Insira os stops (formato: aisle,bay,quantidade)");
        System.out.println("Exemplo: 1,8,5");
        System.out.println("Digite 'fim' para terminar");
        
        List<PickStop> stops = new java.util.ArrayList<>();
        int seq = 1;
        
        while (true) {
            System.out.print("Stop " + seq + ": ");
            String input = scanner.nextLine().trim();
            
            if (input.equalsIgnoreCase("fim")) {
                break;
            }
            
            try {
                String[] parts = input.split(",");
                if (parts.length != 3) {
                    System.out.println("❌ Formato inválido. Use: aisle,bay,quantidade");
                    continue;
                }
                
                int aisle = Integer.parseInt(parts[0].trim());
                int bay = Integer.parseInt(parts[1].trim());
                int qty = Integer.parseInt(parts[2].trim());
                
                if (aisle < 0 || bay < 0 || qty <= 0) {
                    System.out.println("❌ Valores inválidos. Aisle e bay >= 0, quantidade > 0");
                    continue;
                }
                
                stops.add(new PickStop(seq++, aisle, bay, qty));
                System.out.println("✓ Stop adicionado: (" + aisle + "," + bay + ") qty=" + qty);
                
            } catch (NumberFormatException e) {
                System.out.println("❌ Erro de formato numérico. Tente novamente.");
            }
        }
        
        return stops;
    }
    
    /**
     * Simula dados de trolley packing (como estava no Main)
     */
    private List<PickStop> simulateTrolleyPackingOutput() {
        return List.of(
                new PickStop(1, 1, 8, 5),   // Trolley 1
                new PickStop(2, 2, 2, 3),   // Trolley 1
                new PickStop(3, 2, 2, 2),   // Trolley 2 - DUPLICADO! Mesmo aisle/bay
                new PickStop(4, 3, 4, 7),   // Trolley 2
                new PickStop(5, 1, 1, 4),   // Trolley 3
                new PickStop(6, 2, 2, 1)    // Trolley 3 - OUTRO DUPLICADO!
        );
    }
    
    /**
     * Exibe uma lista de stops
     */
    private void displayStops(List<PickStop> stops) {
        for (int i = 0; i < stops.size(); i++) {
            PickStop stop = stops.get(i);
            System.out.println("  " + (i + 1) + ". " + stop);
        }
    }
    
    /**
     * Exibe os resultados das estratégias de sequenciação
     */
    private void displayPathResults(Map<PathStrategy, PickPath> results) {
        System.out.println("\n--- ESTRATÉGIA A - ASC SWEEP ---");
        PickPath ascSweep = results.get(PathStrategy.ASC_SWEEP);
        System.out.println("Distância Total: " + ascSweep.getTotalDistance());
        System.out.println("Caminho:");
        displayStops(ascSweep.getStops());
        
        System.out.println("\n--- ESTRATÉGIA B - NEAREST NEIGHBOUR ---");
        PickPath nearestNeighbour = results.get(PathStrategy.NEAREST_NEIGHBOUR);
        System.out.println("Distância Total: " + nearestNeighbour.getTotalDistance());
        System.out.println("Caminho:");
        displayStops(nearestNeighbour.getStops());
        
        // Comparação das estratégias
        System.out.println("\n--- COMPARAÇÃO ---");
        if (ascSweep.getTotalDistance() < nearestNeighbour.getTotalDistance()) {
            System.out.println("✓ ASC_SWEEP é mais eficiente (" + ascSweep.getTotalDistance() + " vs " + nearestNeighbour.getTotalDistance() + ")");
        } else if (nearestNeighbour.getTotalDistance() < ascSweep.getTotalDistance()) {
            System.out.println("✓ NEAREST_NEIGHBOUR é mais eficiente (" + nearestNeighbour.getTotalDistance() + " vs " + ascSweep.getTotalDistance() + ")");
        } else {
            System.out.println("= Ambas as estratégias têm a mesma distância total");
        }
    }
}
