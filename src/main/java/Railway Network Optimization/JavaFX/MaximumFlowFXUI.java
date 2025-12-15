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
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.util.StringConverter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

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
    private TextField sourceStationField;
    private TextField sinkStationField;
    private ListView<String> sourceSuggestionsList;
    private ListView<String> sinkSuggestionsList;
    private VBox sourceContainer;
    private VBox sinkContainer;
    private Button calculateButton;
    private Label resultLabel;
    private Label resultExplanationLabel;
    private Label detailsTitleLabel;
    private TextArea detailsArea;
    private Label validationErrorLabel;
    private ProgressIndicator progressIndicator;
    private Label statusLabel;
    
    private List<Station> allStations;
    private ObservableList<String> sourceStationOptions;
    private ObservableList<String> sinkStationOptions;
    private FilteredList<String> filteredSourceStations;
    private FilteredList<String> filteredSinkStations;
    
    private String selectedSourceId;
    private String selectedSinkId;
    
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
        VBox centerBox = new VBox(15); // Reduced spacing from 20 to 15
        centerBox.setPadding(new Insets(15)); // Reduced padding from 20 to 15
        centerBox.setAlignment(Pos.CENTER);
        centerBox.setClip(null); // Don't clip autocomplete suggestions
        
        // Station selection
        VBox selectionBox = new VBox(15);
        selectionBox.setAlignment(Pos.CENTER);
        selectionBox.setPadding(new Insets(20));
        selectionBox.setStyle("-fx-background-color: #2D3748; " +
                             "-fx-background-radius: 12px;");
        selectionBox.setClip(null); // Don't clip autocomplete suggestions
        
        Label sourceLabel = new Label("Source Station:");
        sourceLabel.setStyle("-fx-font-family: 'Segoe UI', 'Roboto', sans-serif; " +
                            "-fx-font-size: 14px; " +
                            "-fx-font-weight: 600; " +
                            "-fx-text-fill: #E2E8F0;");
        
        // Source station autocomplete
        sourceContainer = createAutocompleteField("Type station name or ID...");
        sourceStationField = (TextField) sourceContainer.getChildren().get(0);
        sourceSuggestionsList = (ListView<String>) sourceContainer.getChildren().get(1);
        
        Label sinkLabel = new Label("Destination Station (Sink):");
        sinkLabel.setStyle("-fx-font-family: 'Segoe UI', 'Roboto', sans-serif; " +
                          "-fx-font-size: 14px; " +
                          "-fx-font-weight: 600; " +
                          "-fx-text-fill: #E2E8F0;");
        
        // Sink station autocomplete
        sinkContainer = createAutocompleteField("Type station name or ID...");
        sinkStationField = (TextField) sinkContainer.getChildren().get(0);
        sinkSuggestionsList = (ListView<String>) sinkContainer.getChildren().get(1);
        
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
        
        selectionBox.getChildren().addAll(sourceLabel, sourceContainer, sinkLabel, sinkContainer, calculateButton);
        
        // Result display
        VBox resultBox = new VBox(8); // Reduced spacing from 15 to 8
        resultBox.setAlignment(Pos.CENTER);
        resultBox.setPadding(new Insets(15)); // Reduced padding from 20 to 15
        resultBox.setStyle("-fx-background-color: #2D3748; " +
                          "-fx-background-radius: 12px;");
        resultBox.setMinHeight(400); // Ensure enough space for details
        
        // Main result label
        resultLabel = new Label("Select stations and click 'Calculate Maximum Flow'");
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
        resultExplanationLabel.setMaxWidth(650); // Limit width for better wrapping
        resultExplanationLabel.setVisible(false);
        
        // Details title label - positioned closer to details area
        detailsTitleLabel = new Label("Maximum Flow Calculation Summary");
        detailsTitleLabel.setStyle("-fx-font-family: 'Segoe UI', 'Roboto', sans-serif; " +
                                  "-fx-font-size: 16px; " +
                                  "-fx-font-weight: 600; " +
                                  "-fx-text-fill: #E2E8F0; " +
                                  "-fx-text-alignment: left;");
        detailsTitleLabel.setPadding(new Insets(2, 0, 2, 0)); // Minimal padding to bring it closer to details area
        detailsTitleLabel.setVisible(false);
        
        // Details text area
        detailsArea = new TextArea();
        detailsArea.setEditable(false);
        detailsArea.setPrefRowCount(12); // Increased from 8 to 12
        detailsArea.setMinHeight(250); // Minimum height
        detailsArea.setPrefHeight(300); // Preferred height
        detailsArea.setPrefWidth(600);
        detailsArea.setWrapText(true); // Enable text wrapping
        detailsArea.setStyle("-fx-font-family: 'Consolas', 'Monaco', monospace; " +
                            "-fx-font-size: 12px; " +
                            "-fx-background-color: #FFFFFF; " + // White background
                            "-fx-text-fill: #000000; " + // Black text
                            "-fx-border-color: #475569; " +
                            "-fx-border-width: 1; " +
                            "-fx-border-radius: 8px; " +
                            "-fx-padding: 10px;");
        detailsArea.setVisible(false);
        
        // Validation error label
        validationErrorLabel = new Label();
        validationErrorLabel.setStyle("-fx-font-family: 'Segoe UI', 'Roboto', sans-serif; " +
                                     "-fx-font-size: 13px; " +
                                     "-fx-font-weight: 500; " +
                                     "-fx-text-fill: #EF4444; " +
                                     "-fx-text-alignment: center;");
        validationErrorLabel.setWrapText(true);
        validationErrorLabel.setVisible(false);
        
        resultBox.getChildren().addAll(resultLabel, resultExplanationLabel, validationErrorLabel, detailsTitleLabel, detailsArea);
        
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
        
        Scene scene = new Scene(root, 700, 750); // Increased height to accommodate details
        stage.setScene(scene);
        stage.setResizable(true); // Allow resizing to see all content
        stage.setMinWidth(700);
        stage.setMinHeight(750);
        
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
                            // Store stations for autocomplete
                            allStations = stations;
                            
                            // Create display strings for autocomplete
                            List<String> stationDisplayList = stations.stream()
                                    .map(s -> String.format("%s (%s)", s.getName(), s.getId()))
                                    .collect(Collectors.toList());
                            
                            // Initialize autocomplete lists
                            sourceStationOptions = FXCollections.observableArrayList(stationDisplayList);
                            sinkStationOptions = FXCollections.observableArrayList(stationDisplayList);
                            
                            filteredSourceStations = new FilteredList<>(sourceStationOptions);
                            filteredSinkStations = new FilteredList<>(sinkStationOptions);
                            
                            sourceSuggestionsList.setItems(filteredSourceStations);
                            sinkSuggestionsList.setItems(filteredSinkStations);
                            
                            // Setup autocomplete behavior
                            setupAutocomplete(sourceStationField, sourceSuggestionsList, filteredSourceStations, true);
                            setupAutocomplete(sinkStationField, sinkSuggestionsList, filteredSinkStations, false);
                            
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
            // Clear validation error when user types
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
                    selectedSinkId = null;
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
                        
                        // Force parent container to update layout
                        VBox parent = (VBox) suggestionsList.getParent();
                        if (parent != null) {
                            parent.requestLayout();
                        }
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
                textField.setText(selected);
                suggestionsList.setVisible(false);
                
                // Extract station ID
                String stationId = extractStationId(selected);
                if (isSource) {
                    selectedSourceId = stationId;
                } else {
                    selectedSinkId = stationId;
                }
                updateCalculateButtonState();
            }
        });
        
        // Handle keyboard navigation
        textField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.DOWN && suggestionsList.isVisible()) {
                suggestionsList.requestFocus();
                if (!suggestionsList.getItems().isEmpty()) {
                    suggestionsList.getSelectionModel().select(0);
                }
                e.consume();
            } else if (e.getCode() == KeyCode.ENTER && suggestionsList.isVisible() && 
                      !suggestionsList.getSelectionModel().isEmpty()) {
                String selected = suggestionsList.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    textField.setText(selected);
                    suggestionsList.setVisible(false);
                    String stationId = extractStationId(selected);
                    if (isSource) {
                        selectedSourceId = stationId;
                    } else {
                        selectedSinkId = stationId;
                    }
                    // Clear validation error when selection changes
                    validationErrorLabel.setVisible(false);
                    updateCalculateButtonState();
                }
                e.consume();
            } else if (e.getCode() == KeyCode.ESCAPE) {
                suggestionsList.setVisible(false);
                e.consume();
            }
        });
        
        // Hide suggestions when clicking outside
        textField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (!isNowFocused && !suggestionsList.isFocused()) {
                Platform.runLater(() -> {
                    if (!suggestionsList.isFocused()) {
                        suggestionsList.setVisible(false);
                        suggestionsList.setManaged(false);
                    }
                });
            } else if (isNowFocused && !textField.getText().isEmpty()) {
                // Show suggestions when field gains focus and has text
                Platform.runLater(() -> {
                    if (filteredList.size() > 0) {
                        suggestionsList.setVisible(true);
                        suggestionsList.setManaged(true);
                        suggestionsList.toFront();
                    }
                });
            }
        });
        
        suggestionsList.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (!isNowFocused && !textField.isFocused()) {
                Platform.runLater(() -> {
                    suggestionsList.setVisible(false);
                    suggestionsList.setManaged(false);
                });
            }
        });
        
        // Keep suggestions visible when hovering over list
        suggestionsList.setOnMouseEntered(e -> {
            if (!suggestionsList.isVisible() && filteredList.size() > 0) {
                suggestionsList.setVisible(true);
                suggestionsList.setManaged(true);
            }
        });
    }
    
    /**
     * Extracts station ID from display text.
     */
    private String extractStationId(String displayText) {
        if (displayText == null) return "";
        // Format: "Station Name (ID)"
        int start = displayText.lastIndexOf('(');
        int end = displayText.lastIndexOf(')');
        if (start >= 0 && end > start) {
            return displayText.substring(start + 1, end);
        }
        return displayText;
    }
    
    /**
     * Updates the calculate button state based on station selection.
     */
    private void updateCalculateButtonState() {
        boolean canCalculate = selectedSourceId != null && 
                               selectedSinkId != null &&
                               !selectedSourceId.isEmpty() &&
                               !selectedSinkId.isEmpty() &&
                               !selectedSourceId.equals(selectedSinkId);
        
        // Show/hide validation error
        if (selectedSourceId != null && selectedSinkId != null && 
            !selectedSourceId.isEmpty() && !selectedSinkId.isEmpty() &&
            selectedSourceId.equals(selectedSinkId)) {
            validationErrorLabel.setText("Source and sink stations must be different.");
            validationErrorLabel.setVisible(true);
        } else {
            validationErrorLabel.setVisible(false);
        }
        
        // Only enable if stations are different
        // Re-enable button if computation is not running (progress indicator not visible)
        if (!progressIndicator.isVisible()) {
            calculateButton.setDisable(!canCalculate);
        }
    }
    
    /**
     * Handles the calculate button click.
     */
    private void handleCalculate() {
        // Input validation
        if (selectedSourceId == null || selectedSinkId == null || 
            selectedSourceId.isEmpty() || selectedSinkId.isEmpty()) {
            validationErrorLabel.setText("Please select both source and sink stations.");
            validationErrorLabel.setVisible(true);
            return;
        }
        
        if (selectedSourceId.equals(selectedSinkId)) {
            validationErrorLabel.setText("Source and sink stations must be different.");
            validationErrorLabel.setVisible(true);
            return;
        }
        
        // Clear validation error
        validationErrorLabel.setVisible(false);
        
        String sourceId = selectedSourceId;
        String sinkId = selectedSinkId;
        
        // Disable button during computation
        calculateButton.setDisable(true);
        progressIndicator.setVisible(true);
        statusLabel.setText("Calculating maximum flow...");
        resultLabel.setText("Calculating...");
        resultExplanationLabel.setVisible(false);
        detailsTitleLabel.setVisible(false);
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
            // Re-enable button after successful computation
            calculateButton.setDisable(false);
            progressIndicator.setVisible(false);
            statusLabel.setText("Maximum flow computation completed successfully.");
        });
        
        service.setOnFailed(e -> {
            Platform.runLater(() -> {
                String errorMsg = service.getException() != null ? 
                    service.getException().getMessage() : "Unknown error occurred";
                showError("Calculation Error", errorMsg);
                resultLabel.setText("Error calculating maximum flow");
                resultLabel.setStyle("-fx-font-family: 'Segoe UI', 'Roboto', sans-serif; " +
                                    "-fx-font-size: 24px; " +
                                    "-fx-font-weight: 700; " +
                                    "-fx-text-fill: #EF4444; " +
                                    "-fx-text-alignment: center;");
                resultExplanationLabel.setVisible(false);
                detailsTitleLabel.setVisible(false);
                detailsArea.setVisible(false);
                // Re-enable button after error
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
        
        // Update main result label with academic wording
        resultLabel.setText(String.format("Maximum Flow: %d trains per day", maxFlow));
        resultLabel.setStyle("-fx-font-family: 'Segoe UI', 'Roboto', sans-serif; " +
                            "-fx-font-size: 24px; " +
                            "-fx-font-weight: 700; " +
                            "-fx-text-fill: #10B981; " +
                            "-fx-text-alignment: center;");
        
        // Update explanatory label
        resultExplanationLabel.setText(
            "This value represents the maximum number of trains that can be routed daily " +
            "from the source to the sink without exceeding line capacities."
        );
        resultExplanationLabel.setVisible(true);
        
        // Show details title
        detailsTitleLabel.setVisible(true);
        
        // Update details area with structured information
        StringBuilder details = new StringBuilder();
        details.append("COMPUTATION DETAILS\n");
        details.append("===================\n\n");
        details.append(String.format("Source Station:     %s (%s)\n", source.getName(), source.getId()));
        details.append(String.format("Sink Station:       %s (%s)\n\n", sink.getName(), sink.getId()));
        details.append("Algorithm Used:     Ford-Fulkerson (Edmonds-Karp variant)\n\n");
        details.append(String.format("Maximum Flow Value: %d trains per day\n\n", maxFlow));
        details.append("NOTES:\n");
        details.append("- The algorithm computes the maximum flow by finding augmenting paths\n");
        details.append("  using breadth-first search (BFS) in the residual graph.\n");
        details.append("- Flow conservation is maintained: for every intermediate station,\n");
        details.append("  total incoming flow equals total outgoing flow.\n");
        details.append("- Edge capacities are strictly respected throughout the computation.");
        
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

