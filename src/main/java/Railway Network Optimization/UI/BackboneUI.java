package UI;

import Controller.BackboneController;
import Domain.Graph;
import Domain.GraphvizRenderer;

import java.util.Scanner;

/**
 * Console-based user interface for minimal backbone network operations.
 * 
 * Provides menu-based navigation for:
 * - Generating Minimal Spanning Tree (MST)
 * - Exporting to DOT format
 * - Exporting to SVG format
 * 
 * Handles invalid input gracefully and displays output paths for generated files.
 *
 * @author Development Team
 * @version 1.0
 */
public class BackboneUI {
    
    private final BackboneController controller;
    private final Scanner scanner;
    private Graph currentMST;
    
    /**
     * Constructs a BackboneUI with default controller.
     */
    public BackboneUI() {
        this.controller = new BackboneController();
        this.scanner = new Scanner(System.in);
    }
    
    /**
     * Constructs a BackboneUI with custom controller.
     * Useful for dependency injection and testing.
     *
     * @param controller the backbone controller
     */
    public BackboneUI(BackboneController controller) {
        if (controller == null) {
            throw new IllegalArgumentException("BackboneController cannot be null");
        }
        this.controller = controller;
        this.scanner = new Scanner(System.in);
    }
    
    /**
     * Shows the main menu and handles user interaction.
     */
    public void showMainMenu() {
        while (true) {
            System.out.println("\n" + "=".repeat(60));
            System.out.println("MINIMAL BACKBONE NETWORK (USEI12)");
            System.out.println("=".repeat(60));
            System.out.println("1. Generate Minimal Backbone (MST)");
            System.out.println("2. Export to DOT");
            System.out.println("3. Export to SVG");
            System.out.println("4. Generate and Export DOT");
            System.out.println("5. Generate and Export SVG");
            System.out.println("6. View Graph Statistics");
            System.out.println("7. View Complexity Analysis");
            System.out.println("0. Exit");
            
            System.out.print("\nSelect option: ");
            String choice = scanner.nextLine().trim();
            
            switch (choice) {
                case "1":
                    generateMST();
                    break;
                case "2":
                    exportDot();
                    break;
                case "3":
                    exportSvg();
                    break;
                case "4":
                    generateAndExportDot();
                    break;
                case "5":
                    generateAndExportSvg();
                    break;
                case "6":
                    showGraphStatistics();
                    break;
                case "7":
                    showComplexityAnalysis();
                    break;
                case "0":
                    System.out.println("\nExiting... Goodbye!");
                    return;
                default:
                    System.out.println("\nERROR: Invalid option! Please try again.");
            }
        }
    }
    
    /**
     * Generates the minimal backbone network (MST).
     */
    private void generateMST() {
        System.out.println("\n--- Generate Minimal Backbone Network ---");
        
        try {
            System.out.println("Loading graph and computing MST...");
            currentMST = controller.generateBackbone();
            
            System.out.println("\n✓ Minimal Backbone Network generated successfully!");
            System.out.println("  Stations: " + currentMST.getStationCount());
            System.out.println("  Connections: " + currentMST.getConnectionCount());
            
        } catch (Exception e) {
            System.out.println("\n✗ ERROR: Failed to generate MST");
            System.out.println("  " + e.getMessage());
            if (e.getCause() != null) {
                System.out.println("  Cause: " + e.getCause().getMessage());
            }
        }
        
        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
    }
    
    /**
     * Exports the current MST to DOT format.
     */
    private void exportDot() {
        System.out.println("\n--- Export to DOT Format ---");
        
        if (currentMST == null) {
            System.out.println("ERROR: No MST generated yet. Please generate MST first (option 1).");
            System.out.println("\nPress Enter to continue...");
            scanner.nextLine();
            return;
        }
        
        String outputPath = getOutputPath("DOT", "backbone.dot");
        if (outputPath == null) {
            return; // User cancelled
        }
        
        try {
            System.out.println("Exporting to DOT format...");
            String filePath = controller.exportDot(currentMST, outputPath);
            
            System.out.println("\n✓ DOT file exported successfully!");
            System.out.println("  File path: " + filePath);
            
        } catch (Exception e) {
            System.out.println("\n✗ ERROR: Failed to export DOT file");
            System.out.println("  " + e.getMessage());
            if (e.getCause() != null) {
                System.out.println("  Cause: " + e.getCause().getMessage());
            }
        }
        
        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
    }
    
    /**
     * Exports the current MST to SVG format.
     */
    private void exportSvg() {
        System.out.println("\n--- Export to SVG Format ---");
        
        if (currentMST == null) {
            System.out.println("ERROR: No MST generated yet. Please generate MST first (option 1).");
            System.out.println("\nPress Enter to continue...");
            scanner.nextLine();
            return;
        }
        
        String outputPath = getOutputPath("SVG", "backbone.svg");
        if (outputPath == null) {
            return; // User cancelled
        }
        
        try {
            System.out.println("Exporting to SVG format using Graphviz...");
            String filePath = controller.exportSvg(currentMST, outputPath);
            
            System.out.println("\n✓ SVG file exported successfully!");
            System.out.println("  File path: " + filePath);
            System.out.println("  Note: Make sure Graphviz (neato) is installed and in your PATH");
            
        } catch (GraphvizRenderer.GraphvizException e) {
            System.out.println("\n✗ ERROR: Graphviz execution failed");
            System.out.println("  " + e.getMessage());
            System.out.println("  Make sure Graphviz is installed: https://graphviz.org/download/");
        } catch (Exception e) {
            System.out.println("\n✗ ERROR: Failed to export SVG file");
            System.out.println("  " + e.getMessage());
            if (e.getCause() != null) {
                System.out.println("  Cause: " + e.getCause().getMessage());
            }
        }
        
        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
    }
    
    /**
     * Generates MST and exports to DOT in one step.
     */
    private void generateAndExportDot() {
        System.out.println("\n--- Generate and Export DOT ---");
        
        String outputPath = getOutputPath("DOT", "backbone.dot");
        if (outputPath == null) {
            return; // User cancelled
        }
        
        try {
            System.out.println("Generating MST and exporting to DOT...");
            String filePath = controller.generateAndExportDot(outputPath);
            
            currentMST = controller.generateBackbone(); // Update current MST
            
            System.out.println("\n✓ MST generated and DOT file exported successfully!");
            System.out.println("  File path: " + filePath);
            System.out.println("  Stations: " + currentMST.getStationCount());
            System.out.println("  Connections: " + currentMST.getConnectionCount());
            
        } catch (Exception e) {
            System.out.println("\n✗ ERROR: Failed to generate and export DOT");
            System.out.println("  " + e.getMessage());
            if (e.getCause() != null) {
                System.out.println("  Cause: " + e.getCause().getMessage());
            }
        }
        
        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
    }
    
    /**
     * Generates MST and exports to SVG in one step.
     */
    private void generateAndExportSvg() {
        System.out.println("\n--- Generate and Export SVG ---");
        
        String outputPath = getOutputPath("SVG", "backbone.svg");
        if (outputPath == null) {
            return; // User cancelled
        }
        
        try {
            System.out.println("Generating MST and exporting to SVG...");
            String filePath = controller.generateAndExportSvg(outputPath);
            
            currentMST = controller.generateBackbone(); // Update current MST
            
            System.out.println("\n✓ MST generated and SVG file exported successfully!");
            System.out.println("  File path: " + filePath);
            System.out.println("  Stations: " + currentMST.getStationCount());
            System.out.println("  Connections: " + currentMST.getConnectionCount());
            System.out.println("  Note: Make sure Graphviz (neato) is installed and in your PATH");
            
        } catch (GraphvizRenderer.GraphvizException e) {
            System.out.println("\n✗ ERROR: Graphviz execution failed");
            System.out.println("  " + e.getMessage());
            System.out.println("  Make sure Graphviz is installed: https://graphviz.org/download/");
        } catch (Exception e) {
            System.out.println("\n✗ ERROR: Failed to generate and export SVG");
            System.out.println("  " + e.getMessage());
            if (e.getCause() != null) {
                System.out.println("  Cause: " + e.getCause().getMessage());
            }
        }
        
        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
    }
    
    /**
     * Shows graph statistics.
     */
    private void showGraphStatistics() {
        System.out.println("\n--- Graph Statistics ---");
        
        try {
            String statistics = controller.getGraphStatistics();
            System.out.println(statistics);
            
        } catch (Exception e) {
            System.out.println("\n✗ ERROR: Failed to load graph statistics");
            System.out.println("  " + e.getMessage());
            if (e.getCause() != null) {
                System.out.println("  Cause: " + e.getCause().getMessage());
            }
        }
        
        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
    }
    
    /**
     * Shows the temporal complexity analysis.
     */
    private void showComplexityAnalysis() {
        System.out.println("\n--- Temporal Complexity Analysis ---");
        
        try {
            String analysis = controller.getComplexityAnalysis();
            System.out.println(analysis);
            
        } catch (Exception e) {
            System.out.println("\n✗ ERROR: Failed to load complexity analysis");
            System.out.println("  " + e.getMessage());
            if (e.getCause() != null) {
                System.out.println("  Cause: " + e.getCause().getMessage());
            }
        }
        
        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
    }
    
    /**
     * Gets output file path from user with validation.
     *
     * @param fileType the type of file (for display purposes)
     * @param defaultName the default filename
     * @return the output path, or null if user cancelled
     */
    private String getOutputPath(String fileType, String defaultName) {
        System.out.print("Enter output file path [" + defaultName + "]: ");
        String input = scanner.nextLine().trim();
        
        if (input.isEmpty()) {
            return defaultName;
        }
        
        // Basic validation
        if (input.contains("..") || input.contains("//") || input.contains("\\\\")) {
            System.out.println("ERROR: Invalid file path. Please enter a valid path.");
            return null;
        }
        
        return input;
    }
}

