package JavaFX;

import Controller.ShortestPathController;
import Domain.BellmanFord;
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
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * JavaFX UI for USEI15 - Risk-Aware Shortest Paths.
 * 
 * Provides a user interface to:
 * - Select a source station
 * - Select a target station
 * - Calculate shortest path using costs (may be negative)
 * - Detect and display negative cycles
 * - Display the path with accumulated costs
 * 
 * The UI follows MVC principles and delegates all computation to the controller and domain layers.
 * 
 * @author Development Team
 * @version 1.0
 */
public class ShortestPathFXUI {
    private ShortestPathController controller;
    private Stage stage;
    private TextField sourceStationField;
    private TextField targetStationField;
    private ListView<String> sourceSuggestionsList;
    private ListView<String> targetSuggestionsList;
    private VBox sourceContainer;
    private VBox targetContainer;
    private Button calculateButton;
    private Label resultLabel;
    private Label resultExplanationLabel;
    private Label validationErrorLabel;
    private ProgressIndicator progressIndicator;
    private Label statusLabel;
    
    // Organized result sections
    private Label pathTitleLabel;
    private TextArea pathArea;
    private Label edgeCostsTitleLabel;
    private TextArea edgeCostsArea;
    private Label totalCostTitleLabel;
    private Label totalCostLabel;
    
    private List<Station> allStations;
    private ObservableList<String> sourceStationOptions;
    private ObservableList<String> targetStationOptions;
    private FilteredList<String> filteredSourceStations;
    private FilteredList<String> filteredTargetStations;
    
    private String selectedSourceId;
    private String selectedTargetId;
    
    // Default file paths
    private static final String DEFAULT_STATIONS_PATH = "train_station_dataset/Sprint3/stations.csv";
    private static final String DEFAULT_LINES_PATH = "train_station_dataset/Sprint3/lines.csv";
    
    /**
     * Constructs a new ShortestPathFXUI.
     * The controller will be created when data is loaded.
     */
    public ShortestPathFXUI() {
        this.controller = null;
    }
    
    /**
     * Shows the shortest path UI window.
     */
    public void show() {
        stage = new Stage();
        stage.setTitle("Risk-Aware Shortest Paths (USEI15)");
        
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #1B2533;");
        root.setPadding(new Insets(20, 20, 10, 20));
        
        // Top: Title
        VBox topBox = new VBox(15);
        topBox.setAlignment(Pos.CENTER);
        topBox.setPadding(new Insets(0, 0, 20, 0));
        
        Label titleLabel = new Label("Risk-Aware Shortest Paths");
        titleLabel.setStyle("-fx-font-family: 'Segoe UI', 'Roboto', sans-serif; " +
                           "-fx-font-size: 28px; " +
                           "-fx-font-weight: 700; " +
                           "-fx-text-fill: #E2E8F0; " +
                           "-fx-letter-spacing: 0.5px;");
        
        Label descriptionLabel = new Label(
            "Calculate the shortest path between two stations using costs that may be negative. Detects negative cycles."
        );
        descriptionLabel.setStyle("-fx-font-family: 'Segoe UI', 'Roboto', sans-serif; " +
                                  "-fx-font-size: 13px; " +
                                  "-fx-text-fill: #94A3B8;");
        descriptionLabel.setWrapText(true);
        
        topBox.getChildren().addAll(titleLabel, descriptionLabel);
        root.setTop(topBox);
        
        // Center: Input and result
        VBox centerBox = new VBox(15);
        centerBox.setPadding(new Insets(15));
        centerBox.setAlignment(Pos.CENTER);
        centerBox.setClip(null);
        
        // Station selection
        VBox selectionBox = new VBox(15);
        selectionBox.setAlignment(Pos.CENTER);
        selectionBox.setPadding(new Insets(20));
        selectionBox.setStyle("-fx-background-color: #2D3748; " +
                             "-fx-background-radius: 12px;");
        selectionBox.setClip(null);
        
        Label sourceLabel = new Label("Source Station:");
        sourceLabel.setStyle("-fx-font-family: 'Segoe UI', 'Roboto', sans-serif; " +
                            "-fx-font-size: 14px; " +
                            "-fx-font-weight: 600; " +
                            "-fx-text-fill: #E2E8F0;");
        
        // Source station autocomplete
        sourceContainer = createAutocompleteField("Type station name or ID...");
        sourceStationField = (TextField) sourceContainer.getChildren().get(0);
        sourceSuggestionsList = (ListView<String>) sourceContainer.getChildren().get(1);
        
        Label targetLabel = new Label("Target Station:");
        targetLabel.setStyle("-fx-font-family: 'Segoe UI', 'Roboto', sans-serif; " +
                          "-fx-font-size: 14px; " +
                          "-fx-font-weight: 600; " +
                          "-fx-text-fill: #E2E8F0;");
        
        // Target station autocomplete
        targetContainer = createAutocompleteField("Type station name or ID...");
        targetStationField = (TextField) targetContainer.getChildren().get(0);
        targetSuggestionsList = (ListView<String>) targetContainer.getChildren().get(1);
        
        calculateButton = new Button("Calculate Shortest Path");
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
        
        selectionBox.getChildren().addAll(sourceLabel, sourceContainer, targetLabel, targetContainer, calculateButton);
        
        // Result display
        VBox resultBox = new VBox(8);
        resultBox.setAlignment(Pos.CENTER);
        resultBox.setPadding(new Insets(15));
        resultBox.setStyle("-fx-background-color: #2D3748; " +
                          "-fx-background-radius: 12px;");
        resultBox.setMinHeight(450);
        
        // Main result label
        resultLabel = new Label("Select stations and click 'Calculate Shortest Path'");
        resultLabel.setStyle("-fx-font-family: 'Segoe UI', 'Roboto', sans-serif; " +
                            "-fx-font-size: 24px; " +
                            "-fx-font-weight: 700; " +
                            "-fx-text-fill: #94A3B8; " +
                            "-fx-text-alignment: center;");
        resultLabel.setWrapText(true);
        
        // Explanatory label below main result
        resultExplanationLabel = new Label();
        resultExplanationLabel.setStyle("-fx-font-family: 'Segoe UI', 'Roboto', sans-serif; " +
                                        "-fx-font-size: 12px; " +
                                        "-fx-font-weight: 400; " +
                                        "-fx-text-fill: #94A3B8; " +
                                        "-fx-text-alignment: center;");
        resultExplanationLabel.setWrapText(true);
        resultExplanationLabel.setMaxWidth(650);
        resultExplanationLabel.setVisible(false);
        
        // Validation error label
        validationErrorLabel = new Label();
        validationErrorLabel.setStyle("-fx-font-family: 'Segoe UI', 'Roboto', sans-serif; " +
                                     "-fx-font-size: 13px; " +
                                     "-fx-font-weight: 500; " +
                                     "-fx-text-fill: #EF4444; " +
                                     "-fx-text-alignment: center;");
        validationErrorLabel.setWrapText(true);
        validationErrorLabel.setVisible(false);
        
        // Path section
        pathTitleLabel = new Label("Path:");
        pathTitleLabel.setStyle("-fx-font-family: 'Segoe UI', 'Roboto', sans-serif; " +
                               "-fx-font-size: 15px; " +
                               "-fx-font-weight: 600; " +
                               "-fx-text-fill: #E2E8F0; " +
                               "-fx-text-alignment: left;");
        pathTitleLabel.setPadding(new Insets(10, 0, 5, 0));
        pathTitleLabel.setVisible(false);
        
        pathArea = new TextArea();
        pathArea.setEditable(false);
        pathArea.setPrefRowCount(3);
        pathArea.setMinHeight(60);
        pathArea.setPrefHeight(80);
        pathArea.setMaxHeight(100);
        pathArea.setPrefWidth(600);
        pathArea.setWrapText(true);
        pathArea.setStyle("-fx-font-family: 'Consolas', 'Monaco', monospace; " +
                         "-fx-font-size: 13px; " +
                         "-fx-background-color: #FFFFFF; " +
                         "-fx-text-fill: #1E293B; " +
                         "-fx-border-color: #6366F1; " +
                         "-fx-border-width: 1; " +
                         "-fx-border-radius: 8px; " +
                         "-fx-padding: 10px;");
        pathArea.setVisible(false);
        
        // Edge costs section
        edgeCostsTitleLabel = new Label("Costs Between Stations:");
        edgeCostsTitleLabel.setStyle("-fx-font-family: 'Segoe UI', 'Roboto', sans-serif; " +
                                    "-fx-font-size: 15px; " +
                                    "-fx-font-weight: 600; " +
                                    "-fx-text-fill: #E2E8F0; " +
                                    "-fx-text-alignment: left;");
        edgeCostsTitleLabel.setPadding(new Insets(10, 0, 5, 0));
        edgeCostsTitleLabel.setVisible(false);
        
        edgeCostsArea = new TextArea();
        edgeCostsArea.setEditable(false);
        edgeCostsArea.setPrefRowCount(5);
        edgeCostsArea.setMinHeight(100);
        edgeCostsArea.setPrefHeight(120);
        edgeCostsArea.setMaxHeight(150);
        edgeCostsArea.setPrefWidth(600);
        edgeCostsArea.setWrapText(true);
        edgeCostsArea.setStyle("-fx-font-family: 'Consolas', 'Monaco', monospace; " +
                              "-fx-font-size: 12px; " +
                              "-fx-background-color: #FFFFFF; " +
                              "-fx-text-fill: #1E293B; " +
                              "-fx-border-color: #6366F1; " +
                              "-fx-border-width: 1; " +
                              "-fx-border-radius: 8px; " +
                              "-fx-padding: 10px;");
        edgeCostsArea.setVisible(false);
        
        // Total cost section
        totalCostTitleLabel = new Label("Total Cost:");
        totalCostTitleLabel.setStyle("-fx-font-family: 'Segoe UI', 'Roboto', sans-serif; " +
                                    "-fx-font-size: 15px; " +
                                    "-fx-font-weight: 600; " +
                                    "-fx-text-fill: #E2E8F0; " +
                                    "-fx-text-alignment: left;");
        totalCostTitleLabel.setPadding(new Insets(10, 0, 5, 0));
        totalCostTitleLabel.setVisible(false);
        
        totalCostLabel = new Label();
        totalCostLabel.setStyle("-fx-font-family: 'Consolas', 'Monaco', monospace; " +
                               "-fx-font-size: 20px; " +
                               "-fx-font-weight: 700; " +
                               "-fx-text-fill: #10B981; " +
                               "-fx-text-alignment: left; " +
                               "-fx-background-color: #FFFFFF; " +
                               "-fx-background-radius: 8px; " +
                               "-fx-padding: 10px 15px; " +
                               "-fx-border-color: #6366F1; " +
                               "-fx-border-width: 1; " +
                               "-fx-border-radius: 8px;");
        totalCostLabel.setPadding(new Insets(5, 0, 10, 10));
        totalCostLabel.setVisible(false);
        
        resultBox.getChildren().addAll(
            resultLabel, 
            resultExplanationLabel, 
            validationErrorLabel,
            pathTitleLabel,
            pathArea,
            edgeCostsTitleLabel,
            edgeCostsArea,
            totalCostTitleLabel,
            totalCostLabel
        );
        
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
        bottomContainer.setPadding(new Insets(0, 0, 10, 0));
        bottomContainer.getChildren().addAll(bottomBox, closeButton);
        
        root.setBottom(bottomContainer);
        
        Scene scene = new Scene(root, 700, 900);
        stage.setScene(scene);
        stage.setResizable(true);
        stage.setMinWidth(700);
        stage.setMinHeight(900);
        
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
                        // USEI15: Reuses graph from USEI11 if available
                        RailwayGraphRepository graphRepo = new RailwayGraphRepository();
                        graphRepo.loadData(DEFAULT_STATIONS_PATH, DEFAULT_LINES_PATH);
                        
                        // Create controller with RailwayGraphRepository
                        controller = new ShortestPathController(graphRepo);
                        controller.buildGraph();
                        
                        // Debug: Print graph statistics to console
                        System.out.println("\n=== USEI15 GRAPH STATISTICS ===");
                        System.out.println(controller.getGraphStatistics());
                        System.out.println("Total stations loaded: " + controller.getAllStations().size());
                        
                        // Get all stations
                        List<Station> stations = new ArrayList<>(controller.getAllStations());
                        stations.sort(Comparator.comparing(Station::getName));
                        
                        Platform.runLater(() -> {
                            // Store stations for autocomplete
                            allStations = stations;
                            
                            // Create display strings for autocomplete
                            List<String> stationDisplayList = stations.stream()
                                    .map(s -> String.format("%s (%s)", s.getName(), s.getId()))
                                    .collect(Collectors.toList());
                            
                            // Initialize autocomplete lists
                            sourceStationOptions = FXCollections.observableArrayList(stationDisplayList);
                            targetStationOptions = FXCollections.observableArrayList(stationDisplayList);
                            
                            filteredSourceStations = new FilteredList<>(sourceStationOptions);
                            filteredTargetStations = new FilteredList<>(targetStationOptions);
                            
                            sourceSuggestionsList.setItems(filteredSourceStations);
                            targetSuggestionsList.setItems(filteredTargetStations);
                            
                            // Setup autocomplete behavior
                            setupAutocomplete(sourceStationField, sourceSuggestionsList, filteredSourceStations, true);
                            setupAutocomplete(targetStationField, targetSuggestionsList, filteredTargetStations, false);
                            
                            statusLabel.setText("Ready - Type station names to search");
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
     * Creates an autocomplete field with TextField and ListView for suggestions.
     */
    private VBox createAutocompleteField(String promptText) {
        VBox container = new VBox(0);
        container.setPrefWidth(400);
        container.setMaxWidth(400);
        container.setSpacing(0);
        container.setFillWidth(true);
        container.setStyle("-fx-background-color: transparent;");
        container.setClip(null); // Don't clip children
        
        TextField textField = new TextField();
        textField.setPromptText(promptText);
        textField.setPrefWidth(400);
        textField.setMaxWidth(400);
        textField.setStyle("-fx-font-family: 'Segoe UI', 'Roboto', sans-serif; " +
                          "-fx-font-size: 13px; " +
                          "-fx-background-color: #475569; " +
                          "-fx-text-fill: #E2E8F0; " +
                          "-fx-prompt-text-fill: #94A3B8; " +
                          "-fx-background-radius: 8px 8px 0 0;");
        
        ListView<String> suggestionsList = new ListView<>();
        suggestionsList.setPrefWidth(400);
        suggestionsList.setMaxWidth(400);
        suggestionsList.setPrefHeight(200);
        suggestionsList.setMaxHeight(200);
        suggestionsList.setMinHeight(0);
        suggestionsList.setVisible(false);
        suggestionsList.setManaged(false); // Don't take up space when hidden
        suggestionsList.setMouseTransparent(false); // Allow mouse interaction
        suggestionsList.setFocusTraversable(true);
        suggestionsList.setOpacity(1.0); // Ensure fully opaque when visible
        suggestionsList.setStyle("-fx-background-color: #2D3748; " +
                                 "-fx-border-color: #6366F1; " +
                                 "-fx-border-width: 2; " +
                                 "-fx-border-radius: 0 0 8px 8px; " +
                                 "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.5), 10, 0, 0, 2); " +
                                 "-fx-background-insets: 0; " +
                                 "-fx-padding: 0;");
        
        // Style list cells with better visibility
        suggestionsList.setCellFactory(lv -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("-fx-background-color: transparent;");
                } else {
                    setText(item);
                    // Default style
                    setStyle("-fx-font-family: 'Segoe UI', 'Roboto', sans-serif; " +
                            "-fx-font-size: 13px; " +
                            "-fx-text-fill: #E2E8F0; " +
                            "-fx-background-color: #2D3748; " +
                            "-fx-padding: 10px 15px; " +
                            "-fx-border-color: transparent transparent #475569 transparent; " +
                            "-fx-border-width: 0 0 1 0;");
                    
                    // Hover effect
                    setOnMouseEntered(e -> {
                        if (!isEmpty()) {
                            setStyle("-fx-font-family: 'Segoe UI', 'Roboto', sans-serif; " +
                                    "-fx-font-size: 13px; " +
                                    "-fx-text-fill: #FFFFFF; " +
                                    "-fx-background-color: #6366F1; " +
                                    "-fx-padding: 10px 15px; " +
                                    "-fx-border-color: transparent transparent #475569 transparent; " +
                                    "-fx-border-width: 0 0 1 0;");
                        }
                    });
                    
                    setOnMouseExited(e -> {
                        if (!isEmpty() && !isSelected()) {
                            setStyle("-fx-font-family: 'Segoe UI', 'Roboto', sans-serif; " +
                                    "-fx-font-size: 13px; " +
                                    "-fx-text-fill: #E2E8F0; " +
                                    "-fx-background-color: #2D3748; " +
                                    "-fx-padding: 10px 15px; " +
                                    "-fx-border-color: transparent transparent #475569 transparent; " +
                                    "-fx-border-width: 0 0 1 0;");
                        }
                    });
                }
            }
        });
        
        // Style selected cells
        suggestionsList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                suggestionsList.refresh();
            }
        });
        
        // Add text field and suggestions list to container
        // Suggestions list will appear directly below text field
        container.getChildren().addAll(textField, suggestionsList);
        
        // Ensure suggestions list appears on top when visible
        suggestionsList.setViewOrder(-1);
        
        return container;
    }
    
    /**
     * Sets up autocomplete behavior for a TextField and ListView.
     */
    private void setupAutocomplete(TextField textField, ListView<String> suggestionsList, FilteredList<String> filteredList, boolean isSource) {
        
        // Filter suggestions as user types
        textField.textProperty().addListener((obs, oldVal, newVal) -> {
            Platform.runLater(() -> validationErrorLabel.setVisible(false));
            
            if (newVal == null || newVal.isEmpty()) {
                Platform.runLater(() -> {
                    suggestionsList.setVisible(false);
                    suggestionsList.setManaged(false);
                    suggestionsList.setPrefHeight(0);
                });
                if (isSource) {
                    selectedSourceId = null;
                } else {
                    selectedTargetId = null;
                }
            } else {
                String searchText = newVal.toLowerCase();
                filteredList.setPredicate(station -> 
                    station.toLowerCase().contains(searchText)
                );
                
                Platform.runLater(() -> {
                    if (filteredList.size() > 0) {
                        // Calculate dynamic height based on number of items (max 200px, min 150px)
                        int itemCount = filteredList.size();
                        double calculatedHeight = Math.min(200, Math.max(150, itemCount * 35));
                        
                        // Make sure list is visible and managed FIRST
                        suggestionsList.setManaged(true);
                        suggestionsList.setPrefHeight(calculatedHeight);
                        suggestionsList.setMinHeight(150);
                        suggestionsList.setMaxHeight(200);
                        suggestionsList.setVisible(true);
                        suggestionsList.setOpacity(1.0);
                        
                        // Force layout update
                        suggestionsList.requestLayout();
                        
                        // Auto-select first item
                        if (!suggestionsList.getItems().isEmpty()) {
                            suggestionsList.getSelectionModel().select(0);
                        }
                        // Scroll to top
                        suggestionsList.scrollTo(0);
                    } else {
                        suggestionsList.setVisible(false);
                        suggestionsList.setManaged(false);
                        suggestionsList.setPrefHeight(0);
                    }
                });
            }
            
            updateCalculateButtonState();
        });
        
        // Handle selection from list
        suggestionsList.setOnMouseClicked(e -> {
            String selected = suggestionsList.getSelectionModel().getSelectedItem();
            if (selected != null) {
                // Extract station ID from "Name (ID)" format
                String stationId = extractStationId(selected);
                if (stationId != null) {
                    textField.setText(selected);
                    if (isSource) {
                        selectedSourceId = stationId;
                    } else {
                        selectedTargetId = stationId;
                    }
                    suggestionsList.setVisible(false);
                    suggestionsList.setManaged(false);
                    textField.requestFocus();
                    updateCalculateButtonState();
                }
            }
        });
        
        // Handle Enter key
        textField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                if (suggestionsList.isVisible() && !suggestionsList.getSelectionModel().isEmpty()) {
                    String selected = suggestionsList.getSelectionModel().getSelectedItem();
                    if (selected != null) {
                        String stationId = extractStationId(selected);
                        if (stationId != null) {
                            textField.setText(selected);
                            if (isSource) {
                                selectedSourceId = stationId;
                            } else {
                                selectedTargetId = stationId;
                            }
                            suggestionsList.setVisible(false);
                            suggestionsList.setManaged(false);
                            updateCalculateButtonState();
                        }
                    }
                } else {
                    // Try to find matching station
                    String input = textField.getText().trim();
                    if (!input.isEmpty()) {
                        String stationId = findStationId(input);
                        if (stationId != null) {
                            if (isSource) {
                                selectedSourceId = stationId;
                            } else {
                                selectedTargetId = stationId;
                            }
                            updateCalculateButtonState();
                        }
                    }
                }
            } else if (e.getCode() == KeyCode.ESCAPE) {
                suggestionsList.setVisible(false);
                suggestionsList.setManaged(false);
            } else if (e.getCode() == KeyCode.DOWN && suggestionsList.isVisible()) {
                suggestionsList.requestFocus();
                if (!suggestionsList.getSelectionModel().isEmpty()) {
                    suggestionsList.getSelectionModel().select(0);
                }
            }
        });
        
        // Handle arrow keys in suggestions list
        suggestionsList.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                String selected = suggestionsList.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    String stationId = extractStationId(selected);
                    if (stationId != null) {
                        textField.setText(selected);
                        if (isSource) {
                            selectedSourceId = stationId;
                        } else {
                            selectedTargetId = stationId;
                        }
                        suggestionsList.setVisible(false);
                        suggestionsList.setManaged(false);
                        textField.requestFocus();
                        updateCalculateButtonState();
                    }
                }
            } else if (e.getCode() == KeyCode.ESCAPE) {
                suggestionsList.setVisible(false);
                suggestionsList.setManaged(false);
                textField.requestFocus();
            }
        });
    }
    
    /**
     * Extracts station ID from "Name (ID)" format.
     */
    private String extractStationId(String displayString) {
        if (displayString == null) return null;
        int lastParen = displayString.lastIndexOf('(');
        int lastParenClose = displayString.lastIndexOf(')');
        if (lastParen > 0 && lastParenClose > lastParen) {
            return displayString.substring(lastParen + 1, lastParenClose).trim();
        }
        return null;
    }
    
    /**
     * Finds station ID from input (could be ID or name).
     */
    private String findStationId(String input) {
        if (allStations == null) return null;
        
        // Try exact ID match first
        for (Station station : allStations) {
            if (station.getId().equalsIgnoreCase(input)) {
                return station.getId();
            }
        }
        
        // Try name match
        for (Station station : allStations) {
            if (station.getName().equalsIgnoreCase(input)) {
                return station.getId();
            }
        }
        
        // Try partial name match
        for (Station station : allStations) {
            if (station.getName().toLowerCase().contains(input.toLowerCase())) {
                return station.getId();
            }
        }
        
        return null;
    }
    
    /**
     * Updates the calculate button state based on input validity.
     */
    private void updateCalculateButtonState() {
        boolean canCalculate = selectedSourceId != null && 
                              selectedTargetId != null && 
                              !selectedSourceId.equals(selectedTargetId) &&
                              controller != null;
        calculateButton.setDisable(!canCalculate);
    }
    
    /**
     * Handles the calculate button click.
     */
    private void handleCalculate() {
        if (selectedSourceId == null || selectedTargetId == null) {
            showValidationError("Please select both source and target stations.");
            return;
        }
        
        if (selectedSourceId.equals(selectedTargetId)) {
            showValidationError("Source and target stations must be different.");
            return;
        }
        
       
        // Clear previous results
        validationErrorLabel.setVisible(false);
        resultExplanationLabel.setVisible(false);
        pathTitleLabel.setVisible(false);
        pathArea.setVisible(false);
        pathArea.clear();
        edgeCostsTitleLabel.setVisible(false);
        edgeCostsArea.setVisible(false);
        edgeCostsArea.clear();
        totalCostTitleLabel.setVisible(false);
        totalCostLabel.setVisible(false);
        
        // Show loading state
        resultLabel.setText("Calculating shortest path...");
        resultLabel.setStyle("-fx-font-family: 'Segoe UI', 'Roboto', sans-serif; " +
                            "-fx-font-size: 24px; " +
                            "-fx-font-weight: 700; " +
                            "-fx-text-fill: #94A3B8; " +
                            "-fx-text-alignment: center;");
        calculateButton.setDisable(true);
        statusLabel.setText("Calculating...");
        progressIndicator.setVisible(true);
        
        // Calculate in background
        Service<ShortestPathController.ShortestPathResult> service = new Service<ShortestPathController.ShortestPathResult>() {
            @Override
            protected Task<ShortestPathController.ShortestPathResult> createTask() {
                return new Task<ShortestPathController.ShortestPathResult>() {
                    @Override
                    protected ShortestPathController.ShortestPathResult call() throws Exception {
                        return controller.calculateShortestPath(selectedSourceId, selectedTargetId);
                    }
                };
            }
        };
        
        service.setOnSucceeded(e -> {
            ShortestPathController.ShortestPathResult result = service.getValue();
            displayResult(result);
            progressIndicator.setVisible(false);
            calculateButton.setDisable(false);
            statusLabel.setText("Ready");
        });
        
        service.setOnFailed(e -> {
            Platform.runLater(() -> {
                progressIndicator.setVisible(false);
                calculateButton.setDisable(false);
                statusLabel.setText("Error occurred");
                
                resultLabel.setText("Error calculating path");
                resultLabel.setStyle("-fx-font-family: 'Segoe UI', 'Roboto', sans-serif; " +
                                    "-fx-font-size: 24px; " +
                                    "-fx-font-weight: 700; " +
                                    "-fx-text-fill: #EF4444; " +
                                    "-fx-text-alignment: center;");
                
                String errorMsg = service.getException() != null ? 
                    service.getException().getMessage() : "Unknown error";
                showValidationError("Error: " + errorMsg);
            });
        });
        
        service.start();
    }
    
    /**
     * Displays the calculation result.
     */
    private void displayResult(ShortestPathController.ShortestPathResult result) {
        if (result.hasNegativeCycle()) {
            // Display negative cycle information
            resultLabel.setText("⚠ Negative Cycle Detected!");
            resultLabel.setStyle("-fx-font-family: 'Segoe UI', 'Roboto', sans-serif; " +
                                "-fx-font-size: 24px; " +
                                "-fx-font-weight: 700; " +
                                "-fx-text-fill: #F59E0B; " +
                                "-fx-text-alignment: center;");
            
            resultExplanationLabel.setText(
                "A negative cycle was detected in the graph. The shortest path cannot be calculated " +
                "because the cost can be infinitely reduced by traversing the cycle."
            );
            resultExplanationLabel.setVisible(true);
            
            // Display cycle details in path area
            pathTitleLabel.setText("Negative Cycle Details");
            pathTitleLabel.setVisible(true);
            
            StringBuilder cycleDetails = new StringBuilder();
            
            List<String> cycleStations = result.getCycleStations();
            List<BellmanFord.CycleEdge> cycleEdges = result.getCycleEdges();
            
            if (cycleStations != null && !cycleStations.isEmpty()) {
                cycleDetails.append("Stations involved: ");
                for (int i = 0; i < cycleStations.size(); i++) {
                    if (i > 0) cycleDetails.append(" → ");
                    cycleDetails.append(cycleStations.get(i));
                }
                cycleDetails.append("\n\n");
            }
            
            if (cycleEdges != null && !cycleEdges.isEmpty()) {
                cycleDetails.append("Edges with costs:\n");
                for (BellmanFord.CycleEdge edge : cycleEdges) {
                    cycleDetails.append("  ").append(edge.toString()).append("\n");
                }
            }
            
            pathArea.setText(cycleDetails.toString());
            pathArea.setVisible(true);
            
            // Show total cycle cost
            double cycleCost = result.getCycleCost();
            if (!Double.isNaN(cycleCost)) {
                totalCostTitleLabel.setVisible(true);
                totalCostLabel.setText(String.format("%.2f", cycleCost));
                totalCostLabel.setStyle("-fx-font-family: 'Consolas', 'Monaco', monospace; " +
                                       "-fx-font-size: 20px; " +
                                       "-fx-font-weight: 700; " +
                                       "-fx-text-fill: #F59E0B; " +
                                       "-fx-text-alignment: left; " +
                                       "-fx-background-color: #FFFFFF; " +
                                       "-fx-background-radius: 8px; " +
                                       "-fx-padding: 10px 15px; " +
                                       "-fx-border-color: #6366F1; " +
                                       "-fx-border-width: 1; " +
                                       "-fx-border-radius: 8px;");
                totalCostLabel.setVisible(true);
            }
            
        } else if (result.hasPath()) {
            // Display path
            resultLabel.setText("✓ Path Found");
            resultLabel.setStyle("-fx-font-family: 'Segoe UI', 'Roboto', sans-serif; " +
                                "-fx-font-size: 24px; " +
                                "-fx-font-weight: 700; " +
                                "-fx-text-fill: #10B981; " +
                                "-fx-text-alignment: center;");
            
            resultExplanationLabel.setText("Shortest path calculated successfully.");
            resultExplanationLabel.setVisible(true);
            
            // Get path from result
            List<BellmanFord.PathStep> path = result.getPath();
            if (path != null && !path.isEmpty()) {
                // Display path (sequence of stations)
                pathTitleLabel.setVisible(true);
                StringBuilder pathBuilder = new StringBuilder();
                for (int i = 0; i < path.size(); i++) {
                    if (i > 0) pathBuilder.append(" → ");
                    pathBuilder.append(path.get(i).getStationId());
                }
                pathArea.setText(pathBuilder.toString());
                pathArea.setVisible(true);
                
                // Display costs between stations
                edgeCostsTitleLabel.setVisible(true);
                StringBuilder costsBuilder = new StringBuilder();
                for (int i = 0; i < path.size() - 1; i++) {
                    String from = path.get(i).getStationId();
                    String to = path.get(i + 1).getStationId();
                    double costFrom = path.get(i).getAccumulatedCost();
                    double costTo = path.get(i + 1).getAccumulatedCost();
                    double edgeCost = costTo - costFrom;
                    
                    costsBuilder.append(String.format("%s → %s: %.2f\n", from, to, edgeCost));
                }
                edgeCostsArea.setText(costsBuilder.toString());
                edgeCostsArea.setVisible(true);
                
                // Display total cost
                double totalCost = result.getTotalCost();
                if (!Double.isNaN(totalCost)) {
                    totalCostTitleLabel.setVisible(true);
                    totalCostLabel.setText(String.format("%.2f", totalCost));
                    totalCostLabel.setStyle("-fx-font-family: 'Consolas', 'Monaco', monospace; " +
                                           "-fx-font-size: 20px; " +
                                           "-fx-font-weight: 700; " +
                                           "-fx-text-fill: #10B981; " +
                                           "-fx-text-alignment: left; " +
                                           "-fx-background-color: #FFFFFF; " +
                                           "-fx-background-radius: 8px; " +
                                           "-fx-padding: 10px 15px; " +
                                           "-fx-border-color: #6366F1; " +
                                           "-fx-border-width: 1; " +
                                           "-fx-border-radius: 8px;");
                    totalCostLabel.setVisible(true);
                }
            }
            
        } else {
            // No path exists
            resultLabel.setText("✗ No Path Exists");
            resultLabel.setStyle("-fx-font-family: 'Segoe UI', 'Roboto', sans-serif; " +
                                "-fx-font-size: 24px; " +
                                "-fx-font-weight: 700; " +
                                "-fx-text-fill: #EF4444; " +
                                "-fx-text-alignment: center;");
            
            resultExplanationLabel.setText(
                "There is no path from the source station to the target station in the graph."
            );
            resultExplanationLabel.setVisible(true);
        }
    }
    
    /**
     * Shows a validation error message.
     */
    private void showValidationError(String message) {
        validationErrorLabel.setText(message);
        validationErrorLabel.setVisible(true);
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

