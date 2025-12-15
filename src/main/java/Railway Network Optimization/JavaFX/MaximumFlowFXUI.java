package JavaFX;

import Controller.MaximumFlowController;
import Domain.Station;
import Repository.RailwayGraphRepository;
import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * JavaFX UI for USEI14 - Maximum Flow Between Two Stations.
 * 
 * Provides a user interface to:
 * - Select a source station
 * - Select a destination (sink) station
 * - Calculate maximum flow between them
 * - Display the result clearly
 * 
 * The UI follows MVC principles and delegates all computation to the controller and domain layers.
 * 
 * @author Development Team
 * @version 1.0
 */
public class MaximumFlowFXUI {
    private MaximumFlowController controller;
    private Stage stage;
    private ComboBox<String> sourceStationComboBox;
    private ComboBox<String> sinkStationComboBox;
    private Button calculateButton;
    private Label resultLabel;
    private TextArea detailsArea;
    private ProgressIndicator progressIndicator;
    private Label statusLabel;
    
    // Default file paths
    private static final String DEFAULT_STATIONS_PATH = "train_station_dataset/Sprint3/stations.csv";
    private static final String DEFAULT_LINES_PATH = "train_station_dataset/Sprint3/lines.csv";
    
    /**
     * Constructs a new MaximumFlowFXUI.
     * The controller will be created when data is loaded.
     */
    public MaximumFlowFXUI() {
        this.controller = null;
    }
    
    /**
     * Shows the maximum flow UI window.
     */
    public void show() {
        stage = new Stage();
        stage.setTitle("Maximum Flow Between Two Stations (USEI14)");
        
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #1B2533;");
        root.setPadding(new Insets(20));
        
        // Top: Title
        VBox topBox = new VBox(15);
        topBox.setAlignment(Pos.CENTER);
        topBox.setPadding(new Insets(0, 0, 20, 0));
        
        Label titleLabel = new Label("Maximum Flow Between Two Stations");
        titleLabel.setStyle("-fx-font-family: 'Segoe UI', 'Roboto', sans-serif; " +
                           "-fx-font-size: 28px; " +
                           "-fx-font-weight: 700; " +
                           "-fx-text-fill: #E2E8F0; " +
                           "-fx-letter-spacing: 0.5px;");
        
        Label descriptionLabel = new Label(
            "Calculate the maximum number of trains that can flow from a source station to a destination station per day"
        );
        descriptionLabel.setStyle("-fx-font-family: 'Segoe UI', 'Roboto', sans-serif; " +
                                  "-fx-font-size: 13px; " +
                                  "-fx-text-fill: #94A3B8;");
        descriptionLabel.setWrapText(true);
        
        topBox.getChildren().addAll(titleLabel, descriptionLabel);
        root.setTop(topBox);
        
        // Center: Input and result
        VBox centerBox = new VBox(20);
        centerBox.setPadding(new Insets(20));
        centerBox.setAlignment(Pos.CENTER);
        
        // Station selection
        VBox selectionBox = new VBox(15);
        selectionBox.setAlignment(Pos.CENTER);
        selectionBox.setPadding(new Insets(20));
        selectionBox.setStyle("-fx-background-color: #2D3748; " +
                             "-fx-background-radius: 12px;");
        
        Label sourceLabel = new Label("Source Station:");
        sourceLabel.setStyle("-fx-font-family: 'Segoe UI', 'Roboto', sans-serif; " +
                            "-fx-font-size: 14px; " +
                            "-fx-font-weight: 600; " +
                            "-fx-text-fill: #E2E8F0;");
        
        sourceStationComboBox = new ComboBox<>();
        sourceStationComboBox.setPrefWidth(400);
        sourceStationComboBox.setStyle("-fx-font-family: 'Segoe UI', 'Roboto', sans-serif; " +
                                     "-fx-font-size: 13px; " +
                                     "-fx-background-color: #475569; " +
                                     "-fx-text-fill: #E2E8F0;");
        
        Label sinkLabel = new Label("Destination Station (Sink):");
        sinkLabel.setStyle("-fx-font-family: 'Segoe UI', 'Roboto', sans-serif; " +
                          "-fx-font-size: 14px; " +
                          "-fx-font-weight: 600; " +
                          "-fx-text-fill: #E2E8F0;");
        
        sinkStationComboBox = new ComboBox<>();
        sinkStationComboBox.setPrefWidth(400);
        sinkStationComboBox.setStyle("-fx-font-family: 'Segoe UI', 'Roboto', sans-serif; " +
                                    "-fx-font-size: 13px; " +
                                    "-fx-background-color: #475569; " +
                                    "-fx-text-fill: #E2E8F0;");
        
        calculateButton = new Button("Calculate Maximum Flow");
        calculateButton.setPrefWidth(300);
        calculateButton.setPrefHeight(45);
        calculateButton.setStyle("-fx-font-family: 'Segoe UI', 'Roboto', sans-serif; " +
                                "-fx-font-size: 15px; " +
                                "-fx-font-weight: 600; " +
                                "-fx-background-color: #6366F1; " +
                                "-fx-text-fill: #FFFFFF; " +
                                "-fx-background-radius: 12px; " +
                                "-fx-cursor: hand; " +
                                "-fx-effect: dropshadow(gaussian, rgba(99, 102, 241, 0.4), 8, 0, 0, 2);");
        calculateButton.setOnAction(e -> handleCalculate());
        calculateButton.setDisable(true);
        
        selectionBox.getChildren().addAll(sourceLabel, sourceStationComboBox, sinkLabel, sinkStationComboBox, calculateButton);
        
        // Result display
        VBox resultBox = new VBox(10);
        resultBox.setAlignment(Pos.CENTER);
        resultBox.setPadding(new Insets(20));
        resultBox.setStyle("-fx-background-color: #2D3748; " +
                          "-fx-background-radius: 12px;");
        
        resultLabel = new Label("Select stations and click 'Calculate Maximum Flow'");
        resultLabel.setStyle("-fx-font-family: 'Segoe UI', 'Roboto', sans-serif; " +
                            "-fx-font-size: 18px; " +
                            "-fx-font-weight: 600; " +
                            "-fx-text-fill: #94A3B8; " +
                            "-fx-text-alignment: center;");
        resultLabel.setWrapText(true);
        
        detailsArea = new TextArea();
        detailsArea.setEditable(false);
        detailsArea.setPrefRowCount(8);
        detailsArea.setPrefWidth(600);
        detailsArea.setStyle("-fx-font-family: 'Consolas', 'Monaco', monospace; " +
                            "-fx-font-size: 12px; " +
                            "-fx-background-color: #1B2533; " +
                            "-fx-text-fill: #E2E8F0; " +
                            "-fx-border-color: #475569; " +
                            "-fx-border-radius: 8px;");
        detailsArea.setVisible(false);
        
        resultBox.getChildren().addAll(resultLabel, detailsArea);
        
        centerBox.getChildren().addAll(selectionBox, resultBox);
        root.setCenter(centerBox);
        
        // Bottom: Status and close
        HBox bottomBox = new HBox(15);
        bottomBox.setAlignment(Pos.CENTER_LEFT);
        bottomBox.setPadding(new Insets(15, 0, 0, 0));
        
        progressIndicator = new ProgressIndicator();
        progressIndicator.setVisible(false);
        progressIndicator.setPrefSize(20, 20);
        
        statusLabel = new Label("Ready");
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
        bottomContainer.getChildren().addAll(bottomBox, closeButton);
        
        root.setBottom(bottomContainer);
        
        Scene scene = new Scene(root, 700, 600);
        stage.setScene(scene);
        stage.setResizable(false);
        
        // Load data and populate stations
        loadDataAndPopulateStations();
        
        stage.show();
    }
    
    /**
     * Loads data and populates station combo boxes.
     */
    private void loadDataAndPopulateStations() {
        statusLabel.setText("Loading data...");
        progressIndicator.setVisible(true);
        calculateButton.setDisable(true);
        
        Service<Void> service = new Service<Void>() {
            @Override
            protected Task<Void> createTask() {
                return new Task<Void>() {
                    @Override
                    protected Void call() throws Exception {
                        Platform.runLater(() -> statusLabel.setText("Loading stations..."));
                        
                        // Load data and build graph using RailwayGraphRepository
                        // This reuses the same directed graph structure from USEI11
                        RailwayGraphRepository graphRepo = new RailwayGraphRepository();
                        graphRepo.loadData(DEFAULT_STATIONS_PATH, DEFAULT_LINES_PATH);
                        
                        // Create controller with RailwayGraphRepository
                        controller = new MaximumFlowController(graphRepo);
                        controller.buildGraph();
                        
                        // Get all stations
                        List<Station> stations = new ArrayList<>(controller.getAllStations());
                        stations.sort(Comparator.comparing(Station::getName));
                        
                        Platform.runLater(() -> {
                            // Populate combo boxes
                            for (Station station : stations) {
                                String displayText = String.format("%s (%s)", station.getName(), station.getId());
                                sourceStationComboBox.getItems().add(displayText);
                                sinkStationComboBox.getItems().add(displayText);
                            }
                            
                            // Enable calculate button when both stations are selected
                            sourceStationComboBox.setOnAction(e -> updateCalculateButtonState());
                            sinkStationComboBox.setOnAction(e -> updateCalculateButtonState());
                            
                            statusLabel.setText("Ready - Select stations to calculate maximum flow");
                            progressIndicator.setVisible(false);
                            updateCalculateButtonState();
                        });
                        
                        return null;
                    }
                };
            }
        };
        
        service.setOnFailed(e -> {
            Platform.runLater(() -> {
                statusLabel.setText("Error loading data: " + service.getException().getMessage());
                progressIndicator.setVisible(false);
                showError("Error", "Failed to load data: " + service.getException().getMessage());
            });
        });
        
        service.start();
    }
    
    /**
     * Updates the calculate button state based on station selection.
     */
    private void updateCalculateButtonState() {
        boolean canCalculate = sourceStationComboBox.getValue() != null &&
                               sinkStationComboBox.getValue() != null &&
                               !getStationIdFromComboBox(sourceStationComboBox).equals(getStationIdFromComboBox(sinkStationComboBox));
        calculateButton.setDisable(!canCalculate);
    }
    
    /**
     * Extracts station ID from combo box value.
     */
    private String getStationIdFromComboBox(ComboBox<String> comboBox) {
        String value = comboBox.getValue();
        if (value == null) return "";
        // Format: "Station Name (ID)"
        int start = value.lastIndexOf('(');
        int end = value.lastIndexOf(')');
        if (start >= 0 && end > start) {
            return value.substring(start + 1, end);
        }
        return value;
    }
    
    /**
     * Handles the calculate button click.
     */
    private void handleCalculate() {
        String sourceId = getStationIdFromComboBox(sourceStationComboBox);
        String sinkId = getStationIdFromComboBox(sinkStationComboBox);
        
        if (sourceId.isEmpty() || sinkId.isEmpty() || sourceId.equals(sinkId)) {
            showError("Invalid Selection", "Please select different source and destination stations.");
            return;
        }
        
        calculateButton.setDisable(true);
        progressIndicator.setVisible(true);
        statusLabel.setText("Calculating maximum flow...");
        resultLabel.setText("Calculating...");
        detailsArea.setVisible(false);
        
        Service<MaximumFlowController.MaximumFlowResult> service = new Service<MaximumFlowController.MaximumFlowResult>() {
            @Override
            protected Task<MaximumFlowController.MaximumFlowResult> createTask() {
                return new Task<MaximumFlowController.MaximumFlowResult>() {
                    @Override
                    protected MaximumFlowController.MaximumFlowResult call() throws Exception {
                        return controller.computeMaximumFlow(sourceId, sinkId);
                    }
                };
            }
        };
        
        service.setOnSucceeded(e -> {
            MaximumFlowController.MaximumFlowResult result = service.getValue();
            displayResult(result);
            calculateButton.setDisable(false);
            progressIndicator.setVisible(false);
            statusLabel.setText("Calculation completed");
        });
        
        service.setOnFailed(e -> {
            Platform.runLater(() -> {
                String errorMsg = service.getException() != null ? 
                    service.getException().getMessage() : "Unknown error occurred";
                showError("Calculation Error", errorMsg);
                resultLabel.setText("Error calculating maximum flow");
                resultLabel.setStyle("-fx-text-fill: #EF4444;");
                calculateButton.setDisable(false);
                progressIndicator.setVisible(false);
                statusLabel.setText("Error: " + errorMsg);
            });
        });
        
        service.start();
    }
    
    /**
     * Displays the maximum flow result.
     */
    private void displayResult(MaximumFlowController.MaximumFlowResult result) {
        int maxFlow = result.getMaxFlowValue();
        Station source = result.getSourceStation();
        Station sink = result.getSinkStation();
        
        // Update result label
        resultLabel.setText(String.format("Maximum Flow: %d trains/day", maxFlow));
        resultLabel.setStyle("-fx-font-family: 'Segoe UI', 'Roboto', sans-serif; " +
                            "-fx-font-size: 24px; " +
                            "-fx-font-weight: 700; " +
                            "-fx-text-fill: #10B981; " +
                            "-fx-text-alignment: center;");
        
        // Update details area
        StringBuilder details = new StringBuilder();
        details.append("Maximum Flow Calculation Result\n");
        details.append("================================\n\n");
        details.append(String.format("Source Station: %s (%s)\n", source.getName(), source.getId()));
        details.append(String.format("Sink Station:   %s (%s)\n\n", sink.getName(), sink.getId()));
        details.append(String.format("Maximum Flow:   %d trains per day\n\n", maxFlow));
        details.append("This represents the maximum number of trains that can\n");
        details.append("flow from the source to the sink station per day,\n");
        details.append("respecting edge capacities and flow conservation constraints.");
        
        detailsArea.setText(details.toString());
        detailsArea.setVisible(true);
    }
    
    /**
     * Shows an error dialog.
     */
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

