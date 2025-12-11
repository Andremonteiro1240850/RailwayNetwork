package JavaFX;

import Controller.UpgradePlanController;
import Domain.CycleDetection;
import Domain.DataLoader;
import Domain.Line;
import Repository.StationRepository;
import Repository.LineRepository;
import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.collections.ObservableList;
import javafx.collections.FXCollections;

import java.util.*;
import java.util.stream.Collectors;

/**
 * JavaFX UI for the Directed Line Upgrade Plan functionality (USEI11).
 * Provides a user interface to calculate and display upgrade order or cycle information.
 * 
 * @author Development Team
 * @version 1.0
 */
public class UpgradePlanFXUI {
    private UpgradePlanController controller;
    private Stage stage;
    private Button calculateButton;
    private TextArea resultArea;
    private ProgressIndicator progressIndicator;
    private Label statusLabel;
    private Button viewAllStationsButton;
    private Button viewAllCyclesButton;
    private CycleDetection.CycleInfo currentCycleInfo;
    private Map<String, Integer> currentStationDegrees;
    private Map<String, List<Integer>> currentStationCycles;
    private List<List<Station>> currentCycles;
    
    // Default file paths
    private static final String DEFAULT_STATIONS_PATH = "train_station_dataset/Sprint3/stations.csv";
    private static final String DEFAULT_EDGES_PATH = "train_station_dataset/Sprint3/lines.csv";
    
    /**
     * Constructs a new UpgradePlanFXUI.
     * The controller will be created when data is loaded.
     */
    public UpgradePlanFXUI() {
        this.controller = null;
    }
    
    /**
     * Shows the upgrade plan UI window.
     */
    public void show() {
        stage = new Stage();
        stage.setTitle("Directed Line Upgrade Plan");
        
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #1E293B, #0F172A);");
        root.setPadding(new Insets(25));
        
        // Top: Title
        VBox topBox = new VBox(8);
        topBox.setAlignment(Pos.CENTER);
        topBox.setPadding(new Insets(0, 0, 25, 0));
        
        Label titleLabel = new Label("Directed Line Upgrade Plan");
        titleLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: 600; " +
                           "-fx-text-fill: linear-gradient(to right, #60A5FA, #3B82F6, #60A5FA);");
        
        Separator separator = new Separator();
        separator.setPrefWidth(500);
        separator.setStyle("-fx-background-color: linear-gradient(to right, transparent, #3B82F6, transparent);");
        
        Label descriptionLabel = new Label(
            "Calculate the upgrade order for Belgian railway stations based on directed dependencies.\n" +
            "If cycles are detected, they will be identified and reported."
        );
        descriptionLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #94A3B8;");
        descriptionLabel.setWrapText(true);
        
        topBox.getChildren().addAll(titleLabel, separator, descriptionLabel);
        root.setTop(topBox);
        
        // Center: Button and results
        VBox centerBox = new VBox(20);
        centerBox.setPadding(new Insets(20));
        centerBox.setAlignment(Pos.CENTER);
        
        calculateButton = new Button("Calculate Upgrade Order");
        calculateButton.setPrefWidth(350);
        calculateButton.setPrefHeight(60);
        calculateButton.setStyle("-fx-font-size: 16px; -fx-font-weight: 600; " +
                                "-fx-background-color: #3B82F6; " +
                                "-fx-text-fill: #FFFFFF; " +
                                "-fx-background-radius: 8; " +
                                "-fx-cursor: hand;");
        calculateButton.setOnAction(e -> handleCalculate());
        
        Label resultsLabel = new Label("Results:");
        resultsLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: 600; -fx-text-fill: #E2E8F0;");
        
        resultArea = new TextArea();
        resultArea.setEditable(false);
        resultArea.setWrapText(true);
        resultArea.setPrefHeight(400);
        resultArea.setStyle("-fx-font-family: 'Consolas', 'Monaco', monospace; " +
                           "-fx-font-size: 12px; " +
                           "-fx-background-color: #FFFFFF; " +
                           "-fx-text-fill: #000000; " +
                           "-fx-border-color: #334155; " +
                           "-fx-border-radius: 8; " +
                           "-fx-background-radius: 8; " +
                           "-fx-padding: 10;");
        
        // Button to view all stations (initially hidden)
        viewAllStationsButton = new Button();
        viewAllStationsButton.setVisible(false);
        viewAllStationsButton.setStyle("-fx-font-size: 13px; -fx-font-weight: 500; " +
                                      "-fx-background-color: #475569; " +
                                      "-fx-text-fill: #E2E8F0; " +
                                      "-fx-background-radius: 6; " +
                                      "-fx-cursor: hand; " +
                                      "-fx-padding: 8 16;");
        viewAllStationsButton.setOnAction(e -> showAllStationsDialog());
        
        // Button to view all cycles (initially hidden)
        viewAllCyclesButton = new Button();
        viewAllCyclesButton.setVisible(false);
        viewAllCyclesButton.setStyle("-fx-font-size: 13px; -fx-font-weight: 500; " +
                                    "-fx-background-color: #475569; " +
                                    "-fx-text-fill: #E2E8F0; " +
                                    "-fx-background-radius: 6; " +
                                    "-fx-cursor: hand; " +
                                    "-fx-padding: 8 16;");
        viewAllCyclesButton.setOnAction(e -> showAllCyclesDialog());
        
        VBox resultContainer = new VBox(10);
        resultContainer.getChildren().addAll(resultArea, viewAllStationsButton, viewAllCyclesButton);
        
        centerBox.getChildren().addAll(calculateButton, resultsLabel, resultContainer);
        root.setCenter(centerBox);
        
        // Bottom: Status and progress
        HBox bottomBox = new HBox(10);
        bottomBox.setAlignment(Pos.CENTER_LEFT);
        bottomBox.setPadding(new Insets(20, 0, 0, 0));
        
        progressIndicator = new ProgressIndicator();
        progressIndicator.setVisible(false);
        progressIndicator.setPrefSize(20, 20);
        
        statusLabel = new Label("Ready to calculate");
        statusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #94A3B8;");
        
        Button closeButton = new Button("Close");
        closeButton.setPrefWidth(200);
        closeButton.setPrefHeight(50);
        closeButton.setStyle("-fx-font-size: 15px; -fx-font-weight: 500; " +
                            "-fx-background-color: linear-gradient(to right, #475569, #64748B); " +
                            "-fx-text-fill: #E2E8F0; " +
                            "-fx-background-radius: 8; " +
                            "-fx-cursor: hand;");
        closeButton.setOnAction(e -> stage.close());
        
        bottomBox.getChildren().addAll(progressIndicator, statusLabel);
        HBox.setHgrow(statusLabel, Priority.ALWAYS);
        
        HBox bottomContainer = new HBox(20);
        bottomContainer.setAlignment(Pos.CENTER);
        bottomContainer.getChildren().addAll(bottomBox, closeButton);
        
        root.setBottom(bottomContainer);
        
        Scene scene = new Scene(root, 900, 700);
        stage.setScene(scene);
        stage.setResizable(true);
        stage.show();
    }
    
    /**
     * Handles the calculate button click event.
     * Uses JavaFX Service to perform the calculation in a background thread.
     */
    private void handleCalculate() {
        calculateButton.setDisable(true);
        progressIndicator.setVisible(true);
        statusLabel.setText("Loading data and calculating...");
        resultArea.clear();
        
        // Create a service to perform the calculation in background
        Service<UpgradePlanController.UpgradePlanResult> service = new Service<>() {
            @Override
            protected Task<UpgradePlanController.UpgradePlanResult> createTask() {
                return new Task<>() {
                    @Override
                    protected UpgradePlanController.UpgradePlanResult call() throws Exception {
                        updateMessage("Loading data...");
                        
                        // STEP 1: Repository layer - Load data from CSV files
                        DataLoader dataLoader = new DataLoader(false);
                        dataLoader.loadAllData(DEFAULT_STATIONS_PATH, DEFAULT_EDGES_PATH);
                        
                        // Get repositories with loaded data
                        StationRepository stationRepo = dataLoader.getStationRepository();
                        LineRepository lineRepo = dataLoader.getLineRepository();
                        
                        updateMessage("Building graph...");
                        
                        // STEP 2: Controller layer - Create controller with repositories
                        controller = new UpgradePlanController(stationRepo, lineRepo);
                        
                        // Build graph from repositories
                        controller.buildGraph();
                        
                        updateMessage("Calculating upgrade order...");
                        
                        // STEP 3: Calculate upgrade plan
                        return controller.calculateUpgradePlan();
                    }
                };
            }
        };
        
        // Update status during execution
        service.messageProperty().addListener((obs, oldMsg, newMsg) -> 
            Platform.runLater(() -> statusLabel.setText(newMsg))
        );
        
        // Handle completion
        service.setOnSucceeded(e -> {
            UpgradePlanController.UpgradePlanResult result = service.getValue();
            displayResult(result);
            calculateButton.setDisable(false);
            progressIndicator.setVisible(false);
            statusLabel.setText("Calculation completed");
        });
        
        // Handle errors
        service.setOnFailed(e -> {
            Throwable exception = service.getException();
            displayError(exception);
            calculateButton.setDisable(false);
            progressIndicator.setVisible(false);
            statusLabel.setText("Calculation error");
        });
        
        service.start();
    }
    
    /**
     * Displays the calculation result in the result area.
     * 
     * @param result the upgrade plan result
     */
    private void displayResult(UpgradePlanController.UpgradePlanResult result) {
        StringBuilder sb = new StringBuilder();
        
        // Title
        sb.append("UPGRADE PLAN ANALYSIS - USEI11\n");
        sb.append("=".repeat(80)).append("\n\n");
        
        // Graph Overview
        try {
            String stats = controller.getGraphStatistics();
            // Parse stats to extract numbers
            String[] lines = stats.split("\n");
            int nodes = 0, edges = 0;
            for (String line : lines) {
                if (line.contains("Nodes:")) {
                    nodes = Integer.parseInt(line.replaceAll("[^0-9]", ""));
                } else if (line.contains("Edges:")) {
                    edges = Integer.parseInt(line.replaceAll("[^0-9]", ""));
                }
            }
            sb.append("GRAPH OVERVIEW\n");
            sb.append("-".repeat(80)).append("\n");
            sb.append(String.format("Nodes (Stations): %d\n", nodes));
            sb.append(String.format("Edges (Connections): %d\n\n", edges));
        } catch (Exception e) {
            // Ignore if statistics can't be retrieved
        }
        
        if (result.hasTopologicalOrder()) {
            // Display topological order
            sb.append("RESULT: VALID ORDER FOUND\n");
            sb.append("-".repeat(80)).append("\n");
            sb.append("A valid topological order can be determined.\n\n");
            
            sb.append("STATION UPGRADE ORDER:\n");
            sb.append("=".repeat(80)).append("\n");
            
            List<Station> order = result.getTopologicalOrder();
            for (int i = 0; i < order.size(); i++) {
                Station station = order.get(i);
                sb.append(String.format("%4d. [%s] %s\n", 
                    i + 1, 
                    station.getId(), 
                    station.getName() != null ? station.getName() : "N/A"));
            }
            
            sb.append("\n").append("=".repeat(80)).append("\n");
            sb.append(String.format("Total: %d stations\n", order.size()));
            sb.append("\nThis order respects all directed dependencies.\n");
            sb.append("Stations must be upgraded in this sequence.\n");
            
        } else if (result.hasCycles()) {
            // Display cycle information in the format from images
            var cycleInfo = result.getCycleInfo();
            sb.append("RESULT: CYCLES DETECTED\n");
            sb.append("-".repeat(80)).append("\n");
            sb.append("A valid topological order cannot be determined due to circular dependencies.\n\n");
            
            // Calculate station statistics
            currentStationDegrees = calculateStationDegrees(cycleInfo);
            currentStationCycles = calculateStationCycleParticipation(cycleInfo);
            currentCycleInfo = cycleInfo;
            currentCycles = cycleInfo.getCycles();
            List<List<Station>> cycles = currentCycles;
            
            // Affected Stations Table
            Set<String> cycleNodeIds = cycleInfo.getCycleNodeIds();
            sb.append("AFFECTED STATIONS (").append(cycleNodeIds.size())
              .append(" stations in cycles)\n");
            sb.append("-".repeat(80)).append("\n");
            sb.append(String.format("%-8s %-30s %-8s %-8s %-8s %-20s\n", 
                "ID", "Name (if available)", "In", "Out", "Total", "In Cycle #"));
            sb.append("-".repeat(80)).append("\n");
            
            // Sort stations by total degree (descending) and show top 15
            List<Map.Entry<String, Integer>> sortedStations = new ArrayList<>(
                currentStationDegrees.entrySet());
            sortedStations.sort((a, b) -> b.getValue().compareTo(a.getValue()));
            
            int displayCount = Math.min(15, sortedStations.size());
            for (int i = 0; i < displayCount; i++) {
                String stationId = sortedStations.get(i).getKey();
                List<Integer> cycleNums = currentStationCycles.getOrDefault(stationId, new ArrayList<>());
                String cycleStr = formatCycleList(cycleNums);
                
                Station station = findStationById(stationId);
                String name = station != null && station.getName() != null 
                    ? station.getName() 
                    : "N/A";
                if (name.length() > 28) name = name.substring(0, 25) + "...";
                
                // Get correct degree information
                try {
                    UpgradePlanController.DegreeInfo degreeInfo = controller.getStationDegreeInfo(stationId);
                    int inDegree = degreeInfo.getInDegree();
                    int outDegree = degreeInfo.getOutDegree();
                    int totalDegree = degreeInfo.getTotalDegree();
                    
                    sb.append(String.format("%-8s %-30s %-8d %-8d %-8d %-20s\n", 
                        stationId, name, inDegree, outDegree, totalDegree, cycleStr));
                } catch (Exception e) {
                    // Fallback to stored degree if error
                    int degree = sortedStations.get(i).getValue();
                    sb.append(String.format("%-8s %-30s %-8s %-8s %-8d %-20s\n", 
                        stationId, name, "?", "?", degree, cycleStr));
                }
            }
            
            if (sortedStations.size() > 15) {
                sb.append("\n(top 10-15 most problematic)\n");
                // Show button to view all stations
                Platform.runLater(() -> {
                    viewAllStationsButton.setText("View all affected stations");
                    viewAllStationsButton.setVisible(true);
                });
            } else {
                Platform.runLater(() -> viewAllStationsButton.setVisible(false));
            }
            sb.append("\n");
            
            // Critical Findings
            sb.append("CRITICAL FINDINGS:\n");
            sb.append("-".repeat(80)).append("\n");
            int totalStations = 0;
            try {
                String stats = controller.getGraphStatistics();
                String[] lines = stats.split("\n");
                for (String line : lines) {
                    if (line.contains("Nodes:")) {
                        totalStations = Integer.parseInt(line.replaceAll("[^0-9]", ""));
                    }
                }
            } catch (Exception e) {
                // Ignore
            }
            
            double percentage = totalStations > 0 
                ? (cycleNodeIds.size() * 100.0 / totalStations) 
                : 0;
            sb.append(String.format("- %d of %d stations (%.1f%%) are involved in circular dependencies\n",
                cycleNodeIds.size(), totalStations, percentage));
            sb.append(String.format("- %d distinct cycles identified in the network\n", cycles.size()));
            
            // Find largest cycle
            int maxCycleSize = cycles.stream()
                .mapToInt(List::size)
                .max()
                .orElse(0);
            int largestCycleIndex = -1;
            for (int i = 0; i < cycles.size(); i++) {
                if (cycles.get(i).size() == maxCycleSize) {
                    largestCycleIndex = i;
                    break;
                }
            }
            if (largestCycleIndex >= 0) {
                sb.append(String.format("- Largest cycle: %d stations (Cycle %d)\n", 
                    maxCycleSize, largestCycleIndex + 1));
            }
            
            // Find most problematic station
            String mostProblematic = currentStationCycles.entrySet().stream()
                .max((a, b) -> Integer.compare(a.getValue().size(), b.getValue().size()))
                .map(Map.Entry::getKey)
                .orElse("N/A");
            int maxCycles = mostProblematic.equals("N/A") ? 0 
                : currentStationCycles.get(mostProblematic).size();
            if (maxCycles > 0) {
                sb.append(String.format("- Most problematic station: ID %s (appears in %d cycles)\n",
                    mostProblematic, maxCycles));
            }
            sb.append("\n");
            
            // Action Required
            sb.append("ACTION REQUIRED:\n");
            sb.append("-".repeat(80)).append("\n");
            sb.append("1. Break cycles before attempting upgrade planning\n");
            sb.append("2. Focus on stations with highest cycle participation\n");
            sb.append("3. Review edges marked as primary cycle contributors\n\n");
            
            // Cycles Analysis (sorted by impact)
            sb.append("CYCLES ANALYSIS (sorted by impact)\n");
            sb.append("=".repeat(80)).append("\n\n");
            
            // Categorize cycles
            List<List<Station>> highImpact = new ArrayList<>();
            List<List<Station>> mediumImpact = new ArrayList<>();
            List<List<Station>> lowImpact = new ArrayList<>();
            
            for (List<Station> cycle : cycles) {
                int size = cycle.size();
                if (size > 10) {
                    highImpact.add(cycle);
                } else if (size >= 5) {
                    mediumImpact.add(cycle);
                } else {
                    lowImpact.add(cycle);
                }
            }
            
            // HIGH IMPACT
            if (!highImpact.isEmpty()) {
                sb.append("HIGH IMPACT - LARGE CYCLES (>10 stations):\n");
                for (int i = 0; i < cycles.size(); i++) {
                    List<Station> cycle = cycles.get(i);
                    if (cycle.size() > 10) {
                        sb.append(formatCycle(cycle, i + 1));
                    }
                }
                sb.append("\n");
            }
            
            // MEDIUM IMPACT
            if (!mediumImpact.isEmpty()) {
                sb.append("MEDIUM IMPACT (5-10 stations):\n");
                for (int i = 0; i < cycles.size(); i++) {
                    List<Station> cycle = cycles.get(i);
                    if (cycle.size() >= 5 && cycle.size() <= 10) {
                        sb.append(formatCycle(cycle, i + 1));
                    }
                }
                sb.append("\n");
            }
            
            // LOW IMPACT
            if (!lowImpact.isEmpty()) {
                sb.append("LOW IMPACT - SMALL CYCLES (<5 stations):\n");
                for (int i = 0; i < cycles.size(); i++) {
                    List<Station> cycle = cycles.get(i);
                    if (cycle.size() < 5) {
                        sb.append(formatCycle(cycle, i + 1));
                    }
                }
                sb.append("\n");
            }
            
            // Show button to view all cycles
            if (cycles.size() > 0) {
                Platform.runLater(() -> {
                    viewAllCyclesButton.setText("View all cycles");
                    viewAllCyclesButton.setVisible(true);
                });
            } else {
                Platform.runLater(() -> viewAllCyclesButton.setVisible(false));
            }
            
            // Critical Edges Breaking Suggestions
            Map<Line, Integer> edgeCycleCount = calculateEdgeCycleCount(cycleInfo);
            List<Map.Entry<Line, Integer>> sortedEdges = new ArrayList<>(edgeCycleCount.entrySet());
            sortedEdges.sort((a, b) -> b.getValue().compareTo(a.getValue()));
            
            sb.append("CRITICAL EDGES BREAKING SUGGESTIONS\n");
            sb.append("-".repeat(80)).append("\n");
            sb.append("These edges appear in multiple cycles - consider removing/upgrading them first:\n\n");
            sb.append(String.format("%-6s %-15s %-12s %-25s %-20s\n",
                "Rank", "Edge", "Distance", "Cycles Involved", "Recommendation"));
            sb.append("-".repeat(80)).append("\n");
            
            int rank = 1;
            for (Map.Entry<Line, Integer> entry : sortedEdges.stream().limit(10).collect(Collectors.toList())) {
                Line edge = entry.getKey();
                int cycleCount = entry.getValue();
                String recommendation = cycleCount >= 4 ? "HIGH PRIORITY" 
                    : cycleCount >= 2 ? "MEDIUM PRIORITY" 
                    : "LOW PRIORITY";
                
                List<Integer> edgeCycles = findEdgeCycles(edge, cycleInfo);
                String cyclesStr = formatCycleList(edgeCycles);
                
                double dist = edge.getDistance();
                String distStr = dist > 0 ? String.format("%.2f km", dist) : "? km";
                
                sb.append(String.format("%-6d %-15s %-12s %-25s %-20s\n",
                    rank++,
                    edge.getDepartureStationId() + " -> " + edge.getArrivalStationId(),
                    distStr,
                    cyclesStr,
                    recommendation));
            }
            
        } else {
            sb.append("RESULT: EMPTY OR INVALID\n");
            sb.append("No valid result available.\n");
            Platform.runLater(() -> {
                viewAllStationsButton.setVisible(false);
                viewAllCyclesButton.setVisible(false);
            });
        }
        
        resultArea.setText(sb.toString());
    }
    
    /**
     * Shows a dialog with all affected stations.
     */
    private void showAllStationsDialog() {
        if (currentCycleInfo == null || currentStationDegrees == null || currentStationCycles == null) {
            return;
        }
        
        Stage dialog = new Stage();
        dialog.setTitle("All Affected Stations");
        dialog.initOwner(stage);
        
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #1E293B;");
        
        Label titleLabel = new Label("All Affected Stations (" + 
            currentCycleInfo.getCycleNodeIds().size() + " stations)");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: 600; -fx-text-fill: #E2E8F0;");
        
        // Create table
        TableView<Map.Entry<String, Integer>> table = new TableView<>();
        table.setPrefHeight(500);
        table.setStyle("-fx-background-color: #FFFFFF;");
        
        // ID column
        TableColumn<Map.Entry<String, Integer>, String> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(param -> 
            new javafx.beans.property.SimpleStringProperty(param.getValue().getKey()));
        idColumn.setPrefWidth(100);
        
        // Name column
        TableColumn<Map.Entry<String, Integer>, String> nameColumn = new TableColumn<>("Name");
        nameColumn.setCellValueFactory(param -> {
            Station station = findStationById(param.getValue().getKey());
            String name = station != null && station.getName() != null 
                ? station.getName() 
                : "N/A";
            return new javafx.beans.property.SimpleStringProperty(name);
        });
        nameColumn.setPrefWidth(200);
        
        // In-Degree column
        TableColumn<Map.Entry<String, Integer>, Integer> inDegreeColumn = new TableColumn<>("In");
        inDegreeColumn.setCellValueFactory(param -> {
            try {
                UpgradePlanController.DegreeInfo degreeInfo = controller.getStationDegreeInfo(param.getValue().getKey());
                return new javafx.beans.property.SimpleIntegerProperty(degreeInfo.getInDegree()).asObject();
            } catch (Exception e) {
                return new javafx.beans.property.SimpleIntegerProperty(0).asObject();
            }
        });
        inDegreeColumn.setPrefWidth(60);
        
        // Out-Degree column
        TableColumn<Map.Entry<String, Integer>, Integer> outDegreeColumn = new TableColumn<>("Out");
        outDegreeColumn.setCellValueFactory(param -> {
            try {
                UpgradePlanController.DegreeInfo degreeInfo = controller.getStationDegreeInfo(param.getValue().getKey());
                return new javafx.beans.property.SimpleIntegerProperty(degreeInfo.getOutDegree()).asObject();
            } catch (Exception e) {
                return new javafx.beans.property.SimpleIntegerProperty(0).asObject();
            }
        });
        outDegreeColumn.setPrefWidth(60);
        
        // Total Degree column
        TableColumn<Map.Entry<String, Integer>, Integer> totalDegreeColumn = new TableColumn<>("Total");
        totalDegreeColumn.setCellValueFactory(param -> 
            new javafx.beans.property.SimpleIntegerProperty(param.getValue().getValue()).asObject());
        totalDegreeColumn.setPrefWidth(60);
        
        // Cycles column
        TableColumn<Map.Entry<String, Integer>, String> cyclesColumn = new TableColumn<>("In Cycle #");
        cyclesColumn.setCellValueFactory(param -> {
            List<Integer> cycles = currentStationCycles.getOrDefault(
                param.getValue().getKey(), new ArrayList<>());
            String cyclesStr = cycles.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
            return new javafx.beans.property.SimpleStringProperty(cyclesStr);
        });
        cyclesColumn.setPrefWidth(200);
        
        table.getColumns().addAll(idColumn, nameColumn, inDegreeColumn, outDegreeColumn, totalDegreeColumn, cyclesColumn);
        
        // Populate table with sorted stations
        List<Map.Entry<String, Integer>> sortedStations = new ArrayList<>(
            currentStationDegrees.entrySet());
        sortedStations.sort((a, b) -> b.getValue().compareTo(a.getValue()));
        
        ObservableList<Map.Entry<String, Integer>> items = FXCollections.observableArrayList();
        items.addAll(sortedStations);
        table.setItems(items);
        
        Button closeButton = new Button("Close");
        closeButton.setStyle("-fx-font-size: 14px; -fx-font-weight: 500; " +
                            "-fx-background-color: #475569; " +
                            "-fx-text-fill: #E2E8F0; " +
                            "-fx-background-radius: 6; " +
                            "-fx-cursor: hand; " +
                            "-fx-padding: 8 24;");
        closeButton.setOnAction(e -> dialog.close());
        
        HBox buttonBox = new HBox();
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.getChildren().add(closeButton);
        
        root.getChildren().addAll(titleLabel, table, buttonBox);
        
        Scene scene = new Scene(root, 700, 600);
        dialog.setScene(scene);
        dialog.setResizable(true);
        dialog.show();
    }
    
    /**
     * Shows a dialog with all cycles.
     */
    private void showAllCyclesDialog() {
        if (currentCycles == null || currentCycles.isEmpty()) {
            return;
        }
        
        Stage dialog = new Stage();
        dialog.setTitle("All Cycles");
        dialog.initOwner(stage);
        
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #1E293B;");
        
        Label titleLabel = new Label("All Cycles (" + currentCycles.size() + " cycles detected)");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: 600; -fx-text-fill: #E2E8F0;");
        
        // Create text area with all cycles
        TextArea cyclesArea = new TextArea();
        cyclesArea.setEditable(false);
        cyclesArea.setWrapText(false);
        cyclesArea.setPrefHeight(500);
        cyclesArea.setStyle("-fx-font-family: 'Consolas', 'Monaco', monospace; " +
                           "-fx-font-size: 12px; " +
                           "-fx-background-color: #FFFFFF; " +
                           "-fx-text-fill: #000000;");
        
        // Categorize and format all cycles
        StringBuilder cyclesText = new StringBuilder();
        
        // HIGH IMPACT
        List<List<Station>> highImpact = new ArrayList<>();
        List<List<Station>> mediumImpact = new ArrayList<>();
        List<List<Station>> lowImpact = new ArrayList<>();
        
        for (List<Station> cycle : currentCycles) {
            int size = cycle.size();
            if (size > 10) {
                highImpact.add(cycle);
            } else if (size >= 5) {
                mediumImpact.add(cycle);
            } else {
                lowImpact.add(cycle);
            }
        }
        
        if (!highImpact.isEmpty()) {
            cyclesText.append("HIGH IMPACT - LARGE CYCLES (>10 stations):\n");
            cyclesText.append("=".repeat(80)).append("\n");
            for (int i = 0; i < currentCycles.size(); i++) {
                List<Station> cycle = currentCycles.get(i);
                if (cycle.size() > 10) {
                    cyclesText.append(formatCycleFull(cycle, i + 1));
                }
            }
            cyclesText.append("\n");
        }
        
        if (!mediumImpact.isEmpty()) {
            cyclesText.append("MEDIUM IMPACT (5-10 stations):\n");
            cyclesText.append("=".repeat(80)).append("\n");
            for (int i = 0; i < currentCycles.size(); i++) {
                List<Station> cycle = currentCycles.get(i);
                if (cycle.size() >= 5 && cycle.size() <= 10) {
                    cyclesText.append(formatCycleFull(cycle, i + 1));
                }
            }
            cyclesText.append("\n");
        }
        
        if (!lowImpact.isEmpty()) {
            cyclesText.append("LOW IMPACT - SMALL CYCLES (<5 stations):\n");
            cyclesText.append("=".repeat(80)).append("\n");
            for (int i = 0; i < currentCycles.size(); i++) {
                List<Station> cycle = currentCycles.get(i);
                if (cycle.size() < 5) {
                    cyclesText.append(formatCycleFull(cycle, i + 1));
                }
            }
            cyclesText.append("\n");
        }
        
        cyclesArea.setText(cyclesText.toString());
        
        Button closeButton = new Button("Close");
        closeButton.setStyle("-fx-font-size: 14px; -fx-font-weight: 500; " +
                            "-fx-background-color: #475569; " +
                            "-fx-text-fill: #E2E8F0; " +
                            "-fx-background-radius: 6; " +
                            "-fx-cursor: hand; " +
                            "-fx-padding: 8 24;");
        closeButton.setOnAction(e -> dialog.close());
        
        HBox buttonBox = new HBox();
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.getChildren().add(closeButton);
        
        root.getChildren().addAll(titleLabel, cyclesArea, buttonBox);
        
        Scene scene = new Scene(root, 900, 600);
        dialog.setScene(scene);
        dialog.setResizable(true);
        dialog.show();
    }
    
    // Helper methods
    private Map<String, Integer> calculateStationDegrees(CycleDetection.CycleInfo cycleInfo) {
        Map<String, Integer> degrees = new HashMap<>();
        Set<String> cycleNodeIds = cycleInfo.getCycleNodeIds();
        
        // Calculate actual total degree (in-degree + out-degree) for each station in cycles
        for (String stationId : cycleNodeIds) {
            try {
                UpgradePlanController.DegreeInfo degreeInfo = controller.getStationDegreeInfo(stationId);
                degrees.put(stationId, degreeInfo.getTotalDegree());
            } catch (Exception e) {
                // If we can't get degree, use 0
                degrees.put(stationId, 0);
            }
        }
        return degrees;
    }
    
    private Map<String, List<Integer>> calculateStationCycleParticipation(CycleDetection.CycleInfo cycleInfo) {
        Map<String, List<Integer>> participation = new HashMap<>();
        List<List<Station>> cycles = cycleInfo.getCycles();
        
        for (int i = 0; i < cycles.size(); i++) {
            for (Station station : cycles.get(i)) {
                participation.computeIfAbsent(station.getId(), k -> new ArrayList<>()).add(i + 1);
            }
        }
        return participation;
    }
    
    private String formatCycleList(List<Integer> cycles) {
        if (cycles.isEmpty()) return "N/A";
        if (cycles.size() <= 3) {
            return cycles.stream().map(String::valueOf).collect(java.util.stream.Collectors.joining(","));
        }
        return cycles.subList(0, 3).stream().map(String::valueOf)
            .collect(java.util.stream.Collectors.joining(",")) + "...";
    }
    
    /**
     * Formats a cycle for display in main result area.
     * Truncates large cycles with "..." to keep output readable.
     */
    private String formatCycle(List<Station> cycle, int cycleNum) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Cycle %d (%d stations): ", cycleNum, cycle.size()));
        
        if (cycle.size() <= 10) {
            // Show full cycle for small cycles
            for (int i = 0; i < cycle.size(); i++) {
                sb.append(cycle.get(i).getId());
                if (i < cycle.size() - 1) sb.append(" -> ");
            }
        } else {
            // Show first 3, then ..., then last for large cycles
            sb.append(cycle.get(0).getId()).append(" -> ");
            sb.append(cycle.get(1).getId()).append(" -> ");
            sb.append(cycle.get(2).getId()).append(" -> ... -> ");
            sb.append(cycle.get(cycle.size() - 1).getId());
        }
        sb.append("\n");
        return sb.toString();
    }
    
    /**
     * Formats a cycle showing all stations (used in dialog).
     * Always shows complete cycle without truncation.
     */
    private String formatCycleFull(List<Station> cycle, int cycleNum) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Cycle %d (%d stations): ", cycleNum, cycle.size()));
        
        // Show full cycle - all stations
        for (int i = 0; i < cycle.size(); i++) {
            sb.append(cycle.get(i).getId());
            if (i < cycle.size() - 1) sb.append(" -> ");
        }
        sb.append("\n");
        return sb.toString();
    }
    
    private Station findStationById(String stationId) {
        try {
            return controller.getStationById(stationId);
        } catch (Exception e) {
            return null;
        }
    }
    
    private Map<Line, Integer> calculateEdgeCycleCount(CycleDetection.CycleInfo cycleInfo) {
        Map<Line, Integer> edgeCount = new HashMap<>();
        List<List<Station>> cycles = cycleInfo.getCycles();
        Set<Line> cycleEdges = cycleInfo.getCycleEdges();
        
        for (Line edge : cycleEdges) {
            int count = 0;
            for (List<Station> cycle : cycles) {
                if (isEdgeInCycle(edge, cycle)) {
                    count++;
                }
            }
            edgeCount.put(edge, count);
        }
        return edgeCount;
    }
    
    private boolean isEdgeInCycle(Line edge, List<Station> cycle) {
        String from = edge.getDepartureStationId();
        String to = edge.getArrivalStationId();
        
        for (int i = 0; i < cycle.size(); i++) {
            String current = cycle.get(i).getId();
            String next = cycle.get((i + 1) % cycle.size()).getId();
            if (current.equals(from) && next.equals(to)) {
                return true;
            }
        }
        return false;
    }
    
    private List<Integer> findEdgeCycles(Line edge, CycleDetection.CycleInfo cycleInfo) {
        List<Integer> cycles = new ArrayList<>();
        List<List<Station>> allCycles = cycleInfo.getCycles();
        
        for (int i = 0; i < allCycles.size(); i++) {
            if (isEdgeInCycle(edge, allCycles.get(i))) {
                cycles.add(i + 1);
            }
        }
        return cycles;
    }
    
    /**
     * Displays an error message in the result area.
     * 
     * @param exception the exception that occurred
     */
    private void displayError(Throwable exception) {
        StringBuilder sb = new StringBuilder();
        sb.append("ERROR CALCULATING UPGRADE ORDER\n");
        sb.append("=".repeat(60)).append("\n\n");
        sb.append("Error type: ").append(exception.getClass().getSimpleName()).append("\n");
        sb.append("Message: ").append(exception.getMessage()).append("\n\n");
        
        if (exception.getCause() != null) {
            sb.append("Cause: ").append(exception.getCause().getMessage()).append("\n\n");
        }
        
        resultArea.setText(sb.toString());
        resultArea.setStyle(
            "-fx-font-family: 'Consolas', 'Monaco', monospace; " +
            "-fx-font-size: 12px; " +
            "-fx-background-color: #ffebee; " +
            "-fx-text-fill: #c62828;"
        );
        
        // Show alert dialog
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Error calculating upgrade order");
            alert.setContentText(exception.getMessage());
            alert.showAndWait();
        });
    }
}



