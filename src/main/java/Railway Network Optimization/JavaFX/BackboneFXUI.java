package JavaFX;

import Controller.BackboneController;
import Domain.Graph;
import Domain.GraphvizRenderer;
import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.stage.FileChooser;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * JavaFX UI for Minimal Backbone Network (USEI12).
 * Provides a graphical interface to generate MST, export DOT, and export SVG.
 *
 * Following the presentation style of USEI11 (UpgradePlanFXUI).
 *
 * @author Development Team
 * @version 2.0
 */
public class BackboneFXUI {
    private BackboneController controller;
    private Stage stage;
    private Button calculateButton;
    private ProgressIndicator progressIndicator;
    private Label statusLabel;
    private TabPane resultTabPane;
    
    // Overview tab components
    private VBox overviewTab;
    private Label totalStationsLabel;
    private Label totalConnectionsLabel;
    private Label mstStationsLabel;
    private Label mstConnectionsLabel;
    private Label reductionLabel;
    private Label complexityLabel;
    
    // Export tab components
    private VBox exportTab;
    private Button exportDotButton;
    private Button exportSvgButton;
    private Label exportStatusLabel;
    private TextArea exportInfoArea;
    
    // Technical log tab
    private TextArea technicalLogArea;
    
    private Graph currentMST;
    private Graph fullGraph;
    private String rawTechnicalLog;
    private long computationTimeMs;
    
    /**
     * Constructs a new BackboneFXUI with default controller.
     */
    public BackboneFXUI() {
        this.controller = new BackboneController();
    }
    
    /**
     * Constructs a new BackboneFXUI with custom controller.
     *
     * @param controller the backbone controller
     */
    public BackboneFXUI(BackboneController controller) {
        this.controller = controller;
    }
    
    /**
     * Shows the minimal backbone network UI window.
     */
    public void show() {
        stage = new Stage();
        stage.setTitle("Minimal Backbone Network (USEI12)");
        
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #1B2533;");
        root.setPadding(new Insets(20, 20, 10, 20));
        
        // Top: Title and button
        VBox topBox = new VBox(15);
        topBox.setAlignment(Pos.CENTER);
        topBox.setPadding(new Insets(0, 0, 20, 0));
        
        Label titleLabel = new Label("Minimal Backbone Network");
        titleLabel.setStyle("-fx-font-family: 'Segoe UI', 'Roboto', sans-serif; " +
                           "-fx-font-size: 32px; " +
                           "-fx-font-weight: 700; " +
                           "-fx-text-fill: #E2E8F0; " +
                           "-fx-letter-spacing: 0.5px;");
        
        Label descriptionLabel = new Label(
            "Compute the minimum spanning tree (MST) to identify a minimal backbone network " +
            "that connects all stations with minimum total track length"
        );
        descriptionLabel.setStyle("-fx-font-family: 'Segoe UI', 'Roboto', sans-serif; " +
                                  "-fx-font-size: 13px; " +
                                  "-fx-text-fill: #94A3B8;");
        descriptionLabel.setWrapText(true);
        
        calculateButton = new Button("Calculate Minimal Backbone Network");
        calculateButton.setPrefWidth(400);
        calculateButton.setPrefHeight(50);
        calculateButton.setStyle("-fx-font-family: 'Segoe UI', 'Roboto', sans-serif; " +
                                "-fx-font-size: 15px; " +
                                "-fx-font-weight: 600; " +
                                "-fx-background-color: #6366F1; " +
                                "-fx-text-fill: #FFFFFF; " +
                                "-fx-background-radius: 12px; " +
                                "-fx-cursor: hand; " +
                                "-fx-effect: dropshadow(gaussian, rgba(99, 102, 241, 0.4), 8, 0, 0, 2);");
        calculateButton.setOnAction(e -> handleCalculate());
        
        calculateButton.setOnMouseEntered(e -> calculateButton.setStyle(
            "-fx-font-family: 'Segoe UI', 'Roboto', sans-serif; " +
            "-fx-font-size: 15px; " +
            "-fx-font-weight: 600; " +
            "-fx-background-color: #4F46E5; " +
            "-fx-text-fill: #FFFFFF; " +
            "-fx-background-radius: 12px; " +
            "-fx-cursor: hand; " +
            "-fx-effect: dropshadow(gaussian, rgba(99, 102, 241, 0.6), 10, 0, 0, 3);"
        ));
        calculateButton.setOnMouseExited(e -> calculateButton.setStyle(
            "-fx-font-family: 'Segoe UI', 'Roboto', sans-serif; " +
            "-fx-font-size: 15px; " +
            "-fx-font-weight: 600; " +
            "-fx-background-color: #6366F1; " +
            "-fx-text-fill: #FFFFFF; " +
            "-fx-background-radius: 12px; " +
            "-fx-cursor: hand; " +
            "-fx-effect: dropshadow(gaussian, rgba(99, 102, 241, 0.4), 8, 0, 0, 2);"
        ));
        
        topBox.getChildren().addAll(titleLabel, descriptionLabel, calculateButton);
        root.setTop(topBox);
        
        // Center: TabPane with results
        resultTabPane = new TabPane();
        resultTabPane.setStyle("-fx-background-color: #1B2533; " +
                              "-fx-tab-min-width: 120px; " +
                              "-fx-tab-min-height: 35px;");
        resultTabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        
        // Create tabs
        createOverviewTab();
        createExportTab();
        createTechnicalLogTab();
        
        root.setCenter(resultTabPane);
        
        // Bottom: Status and progress
        HBox bottomBox = new HBox(15);
        bottomBox.setAlignment(Pos.CENTER_LEFT);
        bottomBox.setPadding(new Insets(15, 0, 0, 0));
        
        progressIndicator = new ProgressIndicator();
        progressIndicator.setVisible(false);
        progressIndicator.setPrefSize(20, 20);
        
        statusLabel = new Label("Ready to calculate");
        statusLabel.setStyle("-fx-font-family: 'Segoe UI', 'Roboto', sans-serif; " +
                            "-fx-font-size: 13px; " +
                            "-fx-text-fill: #94A3B8;");
        
        Button closeButton = new Button("Close");
        closeButton.setPrefWidth(150);
        closeButton.setPrefHeight(40);
        closeButton.setStyle("-fx-font-family: 'Segoe UI', 'Roboto', sans-serif; " +
                            "-fx-font-size: 14px; " +
                            "-fx-font-weight: 500; " +
                            "-fx-background-color: #475569; " +
                            "-fx-text-fill: #E2E8F0; " +
                            "-fx-background-radius: 8px; " +
                            "-fx-cursor: hand;");
        closeButton.setOnAction(e -> stage.close());
        
        bottomBox.getChildren().addAll(progressIndicator, statusLabel);
        HBox.setHgrow(statusLabel, Priority.ALWAYS);
        
        HBox bottomContainer = new HBox(20);
        bottomContainer.setAlignment(Pos.CENTER);
        bottomContainer.setPadding(new Insets(0, 0, 10, 0));
        bottomContainer.getChildren().addAll(bottomBox, closeButton);
        
        root.setBottom(bottomContainer);
        
        Scene scene = new Scene(root, 1200, 800);
        stage.setScene(scene);
        stage.setResizable(true);
        stage.show();
    }
    
    /**
     * Creates the Overview tab with metric cards.
     */
    private void createOverviewTab() {
        overviewTab = new VBox(30);
        overviewTab.setPadding(new Insets(40, 40, 40, 40));
        overviewTab.setStyle("-fx-background-color: #1B2533;");
        
        // Header section
        VBox headerBox = new VBox(8);
        headerBox.setPadding(new Insets(0, 0, 20, 0));
        
        Label titleLabel = new Label("📊 Overview");
        titleLabel.setStyle("-fx-font-family: 'Segoe UI', 'Roboto', sans-serif; " +
                           "-fx-font-size: 28px; " +
                           "-fx-font-weight: 700; " +
                           "-fx-text-fill: #E2E8F0;");
        
        Label descriptionLabel = new Label("Key metrics and statistics from the minimal backbone network analysis");
        descriptionLabel.setStyle("-fx-font-family: 'Segoe UI', 'Roboto', sans-serif; " +
                                 "-fx-font-size: 14px; " +
                                 "-fx-text-fill: #94A3B8;");
        
        headerBox.getChildren().addAll(titleLabel, descriptionLabel);
        
        // Grid for metric cards
        GridPane cardsGrid = new GridPane();
        cardsGrid.setHgap(30);
        cardsGrid.setVgap(30);
        cardsGrid.setAlignment(Pos.CENTER);
        cardsGrid.setPadding(new Insets(20, 0, 20, 0));
        
        // Card 1: Total Stations
        VBox card1 = createMetricCard("Total Stations", "0", "#6366F1");
        totalStationsLabel = (Label) ((VBox) card1.getChildren().get(1)).getChildren().get(0);
        cardsGrid.add(card1, 0, 0);
        
        // Card 2: Total Connections
        VBox card2 = createMetricCard("Total Connections", "0", "#8B5CF6");
        totalConnectionsLabel = (Label) ((VBox) card2.getChildren().get(1)).getChildren().get(0);
        cardsGrid.add(card2, 1, 0);
        
        // Card 3: MST Stations
        VBox card3 = createMetricCard("MST Stations", "0", "#10B981");
        mstStationsLabel = (Label) ((VBox) card3.getChildren().get(1)).getChildren().get(0);
        cardsGrid.add(card3, 2, 0);
        
        // Card 4: MST Connections
        VBox card4 = createMetricCard("MST Connections", "0", "#F59E0B");
        mstConnectionsLabel = (Label) ((VBox) card4.getChildren().get(1)).getChildren().get(0);
        cardsGrid.add(card4, 0, 1);
        
        // Card 5: Reduction
        VBox card5 = createMetricCard("Connection Reduction", "0%", "#EC4899");
        reductionLabel = (Label) ((VBox) card5.getChildren().get(1)).getChildren().get(0);
        cardsGrid.add(card5, 1, 1);
        
        // Card 6: Complexity
        VBox card6 = createMetricCard("Time Complexity", "O(E log E)", "#EF4444");
        complexityLabel = (Label) ((VBox) card6.getChildren().get(1)).getChildren().get(0);
        cardsGrid.add(card6, 2, 1);
        
        overviewTab.getChildren().addAll(headerBox, cardsGrid);
        VBox.setVgrow(cardsGrid, Priority.ALWAYS);
        
        Tab tab = new Tab("Overview", overviewTab);
        tab.setStyle("-fx-font-family: 'Segoe UI', 'Roboto', sans-serif; " +
                    "-fx-font-size: 13px;");
        resultTabPane.getTabs().add(tab);
    }
    
    /**
     * Creates a metric card with rounded corners.
     */
    private VBox createMetricCard(String title, String value, String color) {
        VBox card = new VBox(15);
        card.setPadding(new Insets(30, 25, 30, 25));
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle("-fx-background-color: #243447; " +
                     "-fx-background-radius: 16px; " +
                     "-fx-border-color: rgba(99, 102, 241, 0.3); " +
                     "-fx-border-radius: 16px; " +
                     "-fx-border-width: 1.5px; " +
                     "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.25), 15, 0, 0, 4);");
        card.setPrefWidth(280);
        card.setPrefHeight(160);
        card.setMinWidth(250);
        card.setMinHeight(140);
        
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-family: 'Segoe UI', 'Roboto', sans-serif; " +
                           "-fx-font-size: 14px; " +
                           "-fx-text-fill: #94A3B8; " +
                           "-fx-font-weight: 500; " +
                           "-fx-letter-spacing: 0.3px;");
        
        VBox valueBox = new VBox(5);
        valueBox.setAlignment(Pos.CENTER_LEFT);
        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-font-family: 'Segoe UI', 'Roboto', sans-serif; " +
                           "-fx-font-size: 24px; " +
                           "-fx-font-weight: 700; " +
                           "-fx-text-fill: " + color + "; " +
                           "-fx-letter-spacing: -0.5px; " +
                           "-fx-wrap-text: true;");
        valueBox.getChildren().add(valueLabel);
        
        card.getChildren().addAll(titleLabel, valueBox);
        return card;
    }
    
    /**
     * Creates the Export tab.
     */
    private void createExportTab() {
        exportTab = new VBox(20);
        exportTab.setPadding(new Insets(30));
        exportTab.setStyle("-fx-background-color: #1B2533;");
        
        Label titleLabel = new Label("📁 Export");
        titleLabel.setStyle("-fx-font-family: 'Segoe UI', 'Roboto', sans-serif; " +
                           "-fx-font-size: 28px; " +
                           "-fx-font-weight: 700; " +
                           "-fx-text-fill: #E2E8F0;");
        
        Label descriptionLabel = new Label("Export the minimal backbone network to DOT or SVG format");
        descriptionLabel.setStyle("-fx-font-family: 'Segoe UI', 'Roboto', sans-serif; " +
                                 "-fx-font-size: 14px; " +
                                 "-fx-text-fill: #94A3B8;");
        
        HBox buttonBox = new HBox(20);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(20, 0, 20, 0));
        
        exportDotButton = createExportButton("Export DOT File", "#10B981");
        exportDotButton.setOnAction(e -> handleExportDot());
        exportDotButton.setDisable(true);
        
        exportSvgButton = createExportButton("Export SVG File", "#F59E0B");
        exportSvgButton.setOnAction(e -> handleExportSvg());
        exportSvgButton.setDisable(true);
        
        buttonBox.getChildren().addAll(exportDotButton, exportSvgButton);
        
        exportStatusLabel = new Label("Generate MST first to enable export options");
        exportStatusLabel.setStyle("-fx-font-family: 'Segoe UI', 'Roboto', sans-serif; " +
                                 "-fx-font-size: 13px; " +
                                 "-fx-text-fill: #94A3B8;");
        
        exportInfoArea = new TextArea();
        exportInfoArea.setEditable(false);
        exportInfoArea.setPrefRowCount(8);
        exportInfoArea.setWrapText(true);
        exportInfoArea.setStyle("-fx-font-family: 'Courier New', monospace; " +
                               "-fx-font-size: 12px; " +
                               "-fx-background-color: #FFFFFF; " +
                               "-fx-text-fill: #000000; " +
                               "-fx-border-color: rgba(99, 102, 241, 0.2); " +
                               "-fx-border-width: 1px; " +
                               "-fx-border-radius: 8px; " +
                               "-fx-background-radius: 8px;");
        exportInfoArea.setText("Export information will appear here after generating the MST.");
        
        exportTab.getChildren().addAll(titleLabel, descriptionLabel, buttonBox, exportStatusLabel, exportInfoArea);
        VBox.setVgrow(exportInfoArea, Priority.ALWAYS);
        
        Tab tab = new Tab("Export", exportTab);
        tab.setStyle("-fx-font-family: 'Segoe UI', 'Roboto', sans-serif; " +
                    "-fx-font-size: 13px;");
        resultTabPane.getTabs().add(tab);
    }
    
    /**
     * Creates an export button with consistent styling.
     */
    private Button createExportButton(String text, String color) {
        Button button = new Button(text);
        button.setPrefWidth(250);
        button.setPrefHeight(45);
        button.setStyle("-fx-font-family: 'Segoe UI', 'Roboto', sans-serif; " +
                       "-fx-font-size: 14px; " +
                       "-fx-font-weight: 600; " +
                       "-fx-background-color: " + color + "; " +
                       "-fx-text-fill: #FFFFFF; " +
                       "-fx-background-radius: 10px; " +
                       "-fx-cursor: hand; " +
                       "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.2), 6, 0, 0, 2);");
        button.setOnMouseEntered(e -> button.setStyle(
            "-fx-font-family: 'Segoe UI', 'Roboto', sans-serif; " +
            "-fx-font-size: 14px; " +
            "-fx-font-weight: 600; " +
            "-fx-background-color: derive(" + color + ", -10%); " +
            "-fx-text-fill: #FFFFFF; " +
            "-fx-background-radius: 10px; " +
            "-fx-cursor: hand; " +
            "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.3), 8, 0, 0, 3);"
        ));
        button.setOnMouseExited(e -> button.setStyle(
            "-fx-font-family: 'Segoe UI', 'Roboto', sans-serif; " +
            "-fx-font-size: 14px; " +
            "-fx-font-weight: 600; " +
            "-fx-background-color: " + color + "; " +
            "-fx-text-fill: #FFFFFF; " +
            "-fx-background-radius: 10px; " +
            "-fx-cursor: hand; " +
            "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.2), 6, 0, 0, 2);"
        ));
        return button;
    }
    
    /**
     * Creates the Technical Log tab.
     */
    private void createTechnicalLogTab() {
        VBox tabContent = new VBox(15);
        tabContent.setPadding(new Insets(20));
        tabContent.setStyle("-fx-background-color: #1B2533;");
        
        Label titleLabel = new Label("📋 Technical Log");
        titleLabel.setStyle("-fx-font-family: 'Segoe UI', 'Roboto', sans-serif; " +
                           "-fx-font-size: 28px; " +
                           "-fx-font-weight: 700; " +
                           "-fx-text-fill: #E2E8F0;");
        
        Label descriptionLabel = new Label("Detailed technical output and analysis information");
        descriptionLabel.setStyle("-fx-font-family: 'Segoe UI', 'Roboto', sans-serif; " +
                                 "-fx-font-size: 14px; " +
                                 "-fx-text-fill: #94A3B8;");
        
        technicalLogArea = new TextArea();
        technicalLogArea.setEditable(false);
        technicalLogArea.setWrapText(false);
        technicalLogArea.setStyle("-fx-font-family: 'Courier New', monospace; " +
                                 "-fx-font-size: 12px; " +
                                 "-fx-background-color: #FFFFFF; " +
                                 "-fx-text-fill: #000000; " +
                                 "-fx-border-color: rgba(99, 102, 241, 0.2); " +
                                 "-fx-border-width: 1px; " +
                                 "-fx-border-radius: 8px; " +
                                 "-fx-background-radius: 8px;");
        technicalLogArea.setText("Technical log will appear here after calculation.");
        
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));
        
        Button exportLogButton = new Button("Export Log");
        exportLogButton.setStyle("-fx-font-family: 'Segoe UI', 'Roboto', sans-serif; " +
                                "-fx-font-size: 13px; " +
                                "-fx-font-weight: 500; " +
                                "-fx-background-color: #475569; " +
                                "-fx-text-fill: #E2E8F0; " +
                                "-fx-background-radius: 6px; " +
                                "-fx-padding: 8 16; " +
                                "-fx-cursor: hand;");
        exportLogButton.setOnAction(e -> handleExportLog());
        
        buttonBox.getChildren().add(exportLogButton);
        
        tabContent.getChildren().addAll(titleLabel, descriptionLabel, technicalLogArea, buttonBox);
        VBox.setVgrow(technicalLogArea, Priority.ALWAYS);
        
        Tab tab = new Tab("Technical Log", tabContent);
        tab.setStyle("-fx-font-family: 'Segoe UI', 'Roboto', sans-serif; " +
                    "-fx-font-size: 13px;");
        resultTabPane.getTabs().add(tab);
    }
    
    /**
     * Handles the calculate button action.
     */
    private void handleCalculate() {
        calculateButton.setDisable(true);
        progressIndicator.setVisible(true);
        statusLabel.setText("Loading graph and computing MST...");
        
        Service<Graph> service = new Service<Graph>() {
            @Override
            protected Task<Graph> createTask() {
                return new Task<Graph>() {
                    @Override
                    protected Graph call() throws Exception {
                        long startTime = System.currentTimeMillis();
                        updateMessage("Loading graph...");
                        
                        // Get graph statistics first (this loads the full graph)
                        try {
                            controller.getGraphStatistics();
                        } catch (Exception e) {
                            // Ignore - will use MST values as fallback
                        }
                        
                        updateMessage("Computing minimum spanning tree...");
                        currentMST = controller.generateBackbone();
                        
                        computationTimeMs = System.currentTimeMillis() - startTime;
                        updateMessage("Calculation completed");
                        
                        return currentMST;
                    }
                };
            }
        };
        
        service.messageProperty().addListener((obs, oldMsg, newMsg) -> 
            Platform.runLater(() -> statusLabel.setText(newMsg))
        );
        
        service.setOnSucceeded(e -> {
            Graph result = service.getValue();
            currentMST = result;
            displayResult();
            calculateButton.setDisable(false);
            progressIndicator.setVisible(false);
            statusLabel.setText("Calculation completed");
            exportDotButton.setDisable(false);
            exportSvgButton.setDisable(false);
        });
        
        service.setOnFailed(e -> {
            calculateButton.setDisable(false);
            progressIndicator.setVisible(false);
            Throwable ex = service.getException();
            statusLabel.setText("Error: " + ex.getMessage());
            showError("Error", "Failed to calculate MST", ex);
        });
        
        service.start();
    }
    
    /**
     * Displays the calculation result in all tabs.
     */
    private void displayResult() {
        if (currentMST == null) {
            return;
        }
        
        // Get full graph statistics
        int fullStationsTemp = 0;
        int fullConnectionsTemp = 0;
        try {
            String stats = controller.getGraphStatistics();
            // Debug: print stats to see format
            System.out.println("Graph Statistics:\n" + stats);
            
            String[] lines = stats.split("\n");
            for (String line : lines) {
                line = line.trim(); // Remove leading/trailing whitespace
                if (line.contains("Stations:")) {
                    // Extract number after "Stations:"
                    String numberStr = line.substring(line.indexOf("Stations:") + "Stations:".length()).trim();
                    fullStationsTemp = Integer.parseInt(numberStr.split("\\s")[0]);
                } else if (line.contains("Connections:")) {
                    // Extract number after "Connections:"
                    String numberStr = line.substring(line.indexOf("Connections:") + "Connections:".length()).trim();
                    fullConnectionsTemp = Integer.parseInt(numberStr.split("\\s")[0]);
                }
            }
            
            // If still 0, try alternative parsing
            if (fullStationsTemp == 0 || fullConnectionsTemp == 0) {
                // Try parsing with regex
                java.util.regex.Pattern stationsPattern = java.util.regex.Pattern.compile("Stations:\\s*(\\d+)");
                java.util.regex.Pattern connectionsPattern = java.util.regex.Pattern.compile("Connections:\\s*(\\d+)");
                
                java.util.regex.Matcher stationsMatcher = stationsPattern.matcher(stats);
                if (stationsMatcher.find()) {
                    fullStationsTemp = Integer.parseInt(stationsMatcher.group(1));
                }
                
                java.util.regex.Matcher connectionsMatcher = connectionsPattern.matcher(stats);
                if (connectionsMatcher.find()) {
                    fullConnectionsTemp = Integer.parseInt(connectionsMatcher.group(1));
                }
            }
        } catch (Exception e) {
            System.err.println("Error parsing graph statistics: " + e.getMessage());
            e.printStackTrace();
            // Use MST values as fallback
            fullStationsTemp = currentMST.getStationCount();
            fullConnectionsTemp = currentMST.getConnectionCount();
        }
        
        // Make final for lambda
        final int fullStations = fullStationsTemp;
        final int fullConnections = fullConnectionsTemp;
        final int mstStations = currentMST.getStationCount();
        final int mstConnections = currentMST.getConnectionCount();
        final double reduction = fullConnections > 0 ? 
            (1.0 - (double) mstConnections / fullConnections) * 100.0 : 0.0;
        final long computationTime = computationTimeMs;
        
        // Update Overview cards
        Platform.runLater(() -> {
            if (totalStationsLabel != null) totalStationsLabel.setText(String.valueOf(fullStations));
            if (totalConnectionsLabel != null) totalConnectionsLabel.setText(String.valueOf(fullConnections));
            if (mstStationsLabel != null) mstStationsLabel.setText(String.valueOf(mstStations));
            if (mstConnectionsLabel != null) mstConnectionsLabel.setText(String.valueOf(mstConnections));
            if (reductionLabel != null) reductionLabel.setText(String.format("%.1f%%", reduction));
            if (complexityLabel != null) {
                complexityLabel.setText("O(E log E)\n(" + computationTime + " ms)");
            }
        });
        
        // Build technical log
        StringBuilder sb = new StringBuilder();
        sb.append("MINIMAL BACKBONE NETWORK ANALYSIS (USEI12)\n");
        sb.append("=".repeat(80)).append("\n\n");
        
        sb.append("GRAPH OVERVIEW\n");
        sb.append("-".repeat(80)).append("\n");
        sb.append(String.format("Total Stations: %d\n", fullStations));
        sb.append(String.format("Total Connections: %d\n\n", fullConnections));
        
        sb.append("MINIMAL SPANNING TREE (MST) RESULTS\n");
        sb.append("-".repeat(80)).append("\n");
        sb.append(String.format("MST Stations: %d\n", mstStations));
        sb.append(String.format("MST Connections: %d\n", mstConnections));
        sb.append(String.format("Connection Reduction: %.1f%%\n\n", reduction));
        
        sb.append("TEMPORAL ANALYSIS\n");
        sb.append("-".repeat(80)).append("\n");
        sb.append(String.format("Computation Time: %d ms\n", computationTime));
        sb.append("Algorithm: Kruskal's with Union-Find\n");
        sb.append("Time Complexity: O(E log E) where E is the number of edges\n");
        sb.append("Space Complexity: O(V + E) where V is the number of vertices\n\n");
        
        sb.append("EXPORT INFORMATION\n");
        sb.append("-".repeat(80)).append("\n");
        sb.append("• DOT file: Contains graph structure with XY coordinates for each station\n");
        sb.append("• SVG file: Visual representation generated using Graphviz 'neato' engine\n");
        sb.append("• Both formats preserve the minimal backbone network structure\n");
        
        rawTechnicalLog = sb.toString();
        if (technicalLogArea != null) {
            Platform.runLater(() -> {
                technicalLogArea.setText(rawTechnicalLog);
                technicalLogArea.setScrollTop(0);
            });
        }
        
        // Update export tab
        if (exportStatusLabel != null) {
            Platform.runLater(() -> {
                exportStatusLabel.setText("✓ MST generated successfully. Ready to export.");
                exportStatusLabel.setStyle("-fx-font-family: 'Segoe UI', 'Roboto', sans-serif; " +
                                         "-fx-font-size: 13px; " +
                                         "-fx-text-fill: #10B981;");
            });
        }
        
        if (exportInfoArea != null) {
            Platform.runLater(() -> {
                String exportInfo = String.format(
                    "Minimal Backbone Network Statistics:\n" +
                    "  • Stations: %d\n" +
                    "  • Connections: %d\n" +
                    "  • Reduction: %.1f%%\n\n" +
                    "Export Options:\n" +
                    "  • DOT: Graph structure with XY coordinates\n" +
                    "  • SVG: Visual graph (requires Graphviz)\n",
                    mstStations, mstConnections, reduction
                );
                exportInfoArea.setText(exportInfo);
            });
        }
    }
    
    /**
     * Handles DOT export.
     */
    private void handleExportDot() {
        if (currentMST == null) {
            showError("Error", "No MST generated", "Please calculate MST first.");
            return;
        }
        
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save DOT File");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("DOT Files", "*.dot")
        );
        fileChooser.setInitialFileName("backbone.dot");
        
        File file = fileChooser.showSaveDialog(stage);
        if (file == null) {
            return;
        }
        
        Service<String> service = new Service<String>() {
            @Override
            protected Task<String> createTask() {
                return new Task<String>() {
                    @Override
                    protected String call() throws Exception {
                        updateMessage("Exporting to DOT format...");
                        return controller.exportDot(currentMST, file.getAbsolutePath());
                    }
                };
            }
        };
        
        service.messageProperty().addListener((obs, oldMsg, newMsg) -> 
            Platform.runLater(() -> exportStatusLabel.setText(newMsg))
        );
        
        service.setOnSucceeded(e -> {
            String filePath = service.getValue();
            Platform.runLater(() -> {
                exportStatusLabel.setText("✓ DOT file exported successfully: " + filePath);
                exportStatusLabel.setStyle("-fx-font-family: 'Segoe UI', 'Roboto', sans-serif; " +
                                         "-fx-font-size: 13px; " +
                                         "-fx-text-fill: #10B981;");
                exportInfoArea.appendText("\n\n✓ DOT file exported:\n" + filePath);
            });
        });
        
        service.setOnFailed(e -> {
            Throwable ex = service.getException();
            Platform.runLater(() -> {
                exportStatusLabel.setText("✗ Error exporting DOT file");
                exportStatusLabel.setStyle("-fx-font-family: 'Segoe UI', 'Roboto', sans-serif; " +
                                         "-fx-font-size: 13px; " +
                                         "-fx-text-fill: #EF4444;");
            });
            showError("Error", "Failed to export DOT", ex);
        });
        
        service.start();
    }
    
    /**
     * Handles SVG export.
     */
    private void handleExportSvg() {
        if (currentMST == null) {
            showError("Error", "No MST generated", "Please calculate MST first.");
            return;
        }
        
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save SVG File");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("SVG Files", "*.svg")
        );
        fileChooser.setInitialFileName("backbone.svg");
        
        File file = fileChooser.showSaveDialog(stage);
        if (file == null) {
            return;
        }
        
        Service<String> service = new Service<String>() {
            @Override
            protected Task<String> createTask() {
                return new Task<String>() {
                    @Override
                    protected String call() throws Exception {
                        updateMessage("Exporting to SVG format using Graphviz...");
                        return controller.exportSvg(currentMST, file.getAbsolutePath());
                    }
                };
            }
        };
        
        service.messageProperty().addListener((obs, oldMsg, newMsg) -> 
            Platform.runLater(() -> exportStatusLabel.setText(newMsg))
        );
        
        service.setOnSucceeded(e -> {
            String filePath = service.getValue();
            Platform.runLater(() -> {
                exportStatusLabel.setText("✓ SVG file exported successfully: " + filePath);
                exportStatusLabel.setStyle("-fx-font-family: 'Segoe UI', 'Roboto', sans-serif; " +
                                         "-fx-font-size: 13px; " +
                                         "-fx-text-fill: #10B981;");
                exportInfoArea.appendText("\n\n✓ SVG file exported:\n" + filePath + 
                    "\nNote: Generated using Graphviz 'neato' engine");
            });
        });
        
        service.setOnFailed(e -> {
            Throwable ex = service.getException();
            if (ex instanceof GraphvizRenderer.GraphvizException) {
                Platform.runLater(() -> {
                    exportStatusLabel.setText("✗ Graphviz error - check installation");
                    exportStatusLabel.setStyle("-fx-font-family: 'Segoe UI', 'Roboto', sans-serif; " +
                                             "-fx-font-size: 13px; " +
                                             "-fx-text-fill: #EF4444;");
                });
                showError("Graphviz Error", 
                    "Graphviz execution failed", 
                    ex.getMessage() + "\n\nMake sure Graphviz is installed: https://graphviz.org/download/");
            } else {
                Platform.runLater(() -> {
                    exportStatusLabel.setText("✗ Error exporting SVG file");
                    exportStatusLabel.setStyle("-fx-font-family: 'Segoe UI', 'Roboto', sans-serif; " +
                                             "-fx-font-size: 13px; " +
                                             "-fx-text-fill: #EF4444;");
                });
                showError("Error", "Failed to export SVG", ex);
            }
        });
        
        service.start();
    }
    
    /**
     * Handles exporting the technical log to a file.
     */
    private void handleExportLog() {
        if (rawTechnicalLog == null || rawTechnicalLog.isEmpty()) {
            showError("Error", "No log available", "Please calculate MST first.");
            return;
        }
        
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Technical Log");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Text Files", "*.txt")
        );
        fileChooser.setInitialFileName("backbone_analysis.txt");
        
        File file = fileChooser.showSaveDialog(stage);
        if (file == null) {
            return;
        }
        
        try {
            Files.write(Paths.get(file.getAbsolutePath()), rawTechnicalLog.getBytes());
            showError("Success", "Log exported", "Technical log saved to:\n" + file.getAbsolutePath());
        } catch (Exception e) {
            showError("Error", "Failed to export log", e);
        }
    }
    
    /**
     * Shows an error alert dialog.
     */
    private void showError(String title, String header, Object content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(header);
            alert.setContentText(content.toString());
            alert.showAndWait();
        });
    }
}
