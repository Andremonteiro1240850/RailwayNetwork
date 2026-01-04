package Lapr.JavaFX;

import Lapr.Controller.RouteController;
import Lapr.Domain.Freight;
import Lapr.Domain.Route;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.util.ArrayList;
import java.util.List;

/**
 * JavaFX UI for USLP08 - Route Planner.
 * 
 * Allows Freight Manager to:
 * - View pending freights
 * - Define/assign routes (simple or complex) to accomplish pending freights
 * - View route with sequence of stations and cargos to be loaded/unloaded
 * 
 * @author Development Team
 * @version 1.0
 */
public class RoutePlannerFXUI {
    private RouteController controller;
    private Stage stage;
    
    // Tabs
    private TabPane mainTabPane;
    private Tab pendingFreightsTab;
    private Tab createRouteTab;
    private Tab viewRouteTab;
    
    // Pending Freights Tab
    private TableView<Freight> pendingFreightsTable;
    private ObservableList<Freight> pendingFreightsData;
    private Label pendingFreightsCountLabel;
    private Button refreshPendingButton;
    
    // Create Route Tab
    private RadioButton simpleRouteRadio;
    private RadioButton complexRouteRadio;
    private ToggleGroup routeTypeGroup;
    private TextField trainIdField;
    private ListView<Integer> stationIdsList;
    private TextField stationIdField;
    private Button addStationButton;
    private Button removeStationButton;
    private Button clearStationsButton;
    private ListView<Integer> selectedFreightsList;
    private Button addFreightButton;
    private Button removeFreightButton;
    private Button createRouteButton;
    private Label routeTypeLabel;
    
    // View Route Tab
    private TextField viewTrainIdField;
    private Button viewRouteButton;
    private TextArea routeDetailsArea;
    
    // Status
    private Label statusLabel;
    private ProgressIndicator progressIndicator;
    
    public RoutePlannerFXUI() {
        this.controller = new RouteController();
    }
    
    /**
     * Shows the route planner UI window.
     */
    public void show() {
        stage = new Stage();
        stage.setTitle("Route Planner (USLP08)");
        
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #1B2533;");
        root.setPadding(new Insets(20));
        
        // Top: Title
        VBox topBox = new VBox(15);
        topBox.setAlignment(Pos.CENTER);
        topBox.setPadding(new Insets(0, 0, 20, 0));
        
        Label titleLabel = new Label("Route Planner");
        titleLabel.setStyle("-fx-font-family: 'Segoe UI', 'Roboto', sans-serif; " +
                           "-fx-font-size: 28px; " +
                           "-fx-font-weight: 700; " +
                           "-fx-text-fill: #E2E8F0; " +
                           "-fx-letter-spacing: 0.5px;");
        
        Label descriptionLabel = new Label(
            "Define and assign routes (simple or complex) to accomplish a list of pending freights. " +
            "A route includes a sequence of stations and the cargos to be loaded/unloaded."
        );
        descriptionLabel.setStyle("-fx-font-family: 'Segoe UI', 'Roboto', sans-serif; " +
                                  "-fx-font-size: 13px; " +
                                  "-fx-text-fill: #94A3B8;");
        descriptionLabel.setWrapText(true);
        
        topBox.getChildren().addAll(titleLabel, descriptionLabel);
        root.setTop(topBox);
        
        // Center: Tabs
        mainTabPane = new TabPane();
        mainTabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        mainTabPane.setStyle("-fx-background-color: #1B2533;");
        
        pendingFreightsTab = createPendingFreightsTab();
        createRouteTab = createCreateRouteTab();
        viewRouteTab = createViewRouteTab();
        
        mainTabPane.getTabs().addAll(pendingFreightsTab, createRouteTab, viewRouteTab);
        root.setCenter(mainTabPane);
        
        // Bottom: Status
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
        
        Scene scene = new Scene(root, 1000, 750);
        stage.setScene(scene);
        stage.setResizable(true);
        stage.setMinWidth(1000);
        stage.setMinHeight(750);
        
        // Load initial data
        loadPendingFreights();
        
        stage.show();
    }
    
    /**
     * Creates the Pending Freights tab.
     */
    private Tab createPendingFreightsTab() {
        Tab tab = new Tab("Pending Freights");
        tab.setStyle("-fx-background-color: #1B2533;");
        
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color: #1B2533;");
        
        // Header
        HBox headerBox = new HBox(15);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        
        Label titleLabel = new Label("Pending Freights");
        titleLabel.setStyle("-fx-font-family: 'Segoe UI', 'Roboto', sans-serif; " +
                           "-fx-font-size: 20px; " +
                           "-fx-font-weight: 600; " +
                           "-fx-text-fill: #E2E8F0;");
        
        pendingFreightsCountLabel = new Label("Loading...");
        pendingFreightsCountLabel.setStyle("-fx-font-family: 'Segoe UI', 'Roboto', sans-serif; " +
                                          "-fx-font-size: 13px; " +
                                          "-fx-text-fill: #94A3B8;");
        
        refreshPendingButton = new Button("Refresh");
        refreshPendingButton.setStyle("-fx-font-family: 'Segoe UI', 'Roboto', sans-serif; " +
                                     "-fx-font-size: 13px; " +
                                     "-fx-background-color: #6366F1; " +
                                     "-fx-text-fill: #FFFFFF; " +
                                     "-fx-background-radius: 8px; " +
                                     "-fx-cursor: hand;");
        refreshPendingButton.setOnAction(e -> loadPendingFreights());
        
        HBox.setHgrow(pendingFreightsCountLabel, Priority.ALWAYS);
        headerBox.getChildren().addAll(titleLabel, pendingFreightsCountLabel, refreshPendingButton);
        
        // Table
        pendingFreightsTable = new TableView<>();
        pendingFreightsTable.setStyle("-fx-background-color: #2D3748; " +
                                     "-fx-background-radius: 12px;");
        pendingFreightsTable.setPrefHeight(500);
        
        TableColumn<Freight, Integer> idColumn = new TableColumn<>("Freight ID");
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        idColumn.setPrefWidth(100);
        
        TableColumn<Freight, String> cargoColumn = new TableColumn<>("Cargo Type");
        cargoColumn.setCellValueFactory(new PropertyValueFactory<>("cargoType"));
        cargoColumn.setPrefWidth(200);
        
        TableColumn<Freight, Integer> trainColumn = new TableColumn<>("Train ID");
        trainColumn.setCellValueFactory(new PropertyValueFactory<>("trainID"));
        trainColumn.setPrefWidth(100);
        
        TableColumn<Freight, Integer> wagonColumn = new TableColumn<>("Wagon ID");
        wagonColumn.setCellValueFactory(new PropertyValueFactory<>("wagonID"));
        wagonColumn.setPrefWidth(100);
        
        TableColumn<Freight, Integer> locomotiveColumn = new TableColumn<>("Locomotive ID");
        locomotiveColumn.setCellValueFactory(new PropertyValueFactory<>("locomotiveID"));
        locomotiveColumn.setPrefWidth(150);
        
        pendingFreightsTable.getColumns().addAll(idColumn, cargoColumn, trainColumn, wagonColumn, locomotiveColumn);
        
        pendingFreightsData = FXCollections.observableArrayList();
        pendingFreightsTable.setItems(pendingFreightsData);
        
        content.getChildren().addAll(headerBox, pendingFreightsTable);
        tab.setContent(content);
        
        return tab;
    }
    
    /**
     * Creates the Create Route tab.
     */
    private Tab createCreateRouteTab() {
        Tab tab = new Tab("Create Route");
        tab.setStyle("-fx-background-color: #1B2533;");
        
        VBox content = new VBox(20);
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color: #1B2533;");
        
        // Route Type Selection
        VBox routeTypeBox = new VBox(10);
        routeTypeBox.setStyle("-fx-background-color: #2D3748; " +
                             "-fx-background-radius: 12px; " +
                             "-fx-padding: 15px;");
        
        routeTypeLabel = new Label("Route Type:");
        routeTypeLabel.setStyle("-fx-font-family: 'Segoe UI', 'Roboto', sans-serif; " +
                               "-fx-font-size: 14px; " +
                               "-fx-font-weight: 600; " +
                               "-fx-text-fill: #E2E8F0;");
        
        routeTypeGroup = new ToggleGroup();
        simpleRouteRadio = new RadioButton("Simple Route (single freight)");
        simpleRouteRadio.setToggleGroup(routeTypeGroup);
        simpleRouteRadio.setSelected(true);
        simpleRouteRadio.setStyle("-fx-text-fill: #E2E8F0;");
        simpleRouteRadio.setOnAction(e -> updateRouteTypeUI());
        
        complexRouteRadio = new RadioButton("Complex Route (multiple freights)");
        complexRouteRadio.setToggleGroup(routeTypeGroup);
        complexRouteRadio.setStyle("-fx-text-fill: #E2E8F0;");
        complexRouteRadio.setOnAction(e -> updateRouteTypeUI());
        
        routeTypeBox.getChildren().addAll(routeTypeLabel, simpleRouteRadio, complexRouteRadio);
        
        // Train ID
        VBox trainIdBox = new VBox(10);
        trainIdBox.setStyle("-fx-background-color: #2D3748; " +
                           "-fx-background-radius: 12px; " +
                           "-fx-padding: 15px;");
        
        Label trainIdLabel = new Label("Train ID:");
        trainIdLabel.setStyle("-fx-font-family: 'Segoe UI', 'Roboto', sans-serif; " +
                             "-fx-font-size: 14px; " +
                             "-fx-font-weight: 600; " +
                             "-fx-text-fill: #E2E8F0;");
        
        trainIdField = new TextField();
        trainIdField.setPromptText("Enter train ID");
        trainIdField.setStyle("-fx-font-family: 'Segoe UI', 'Roboto', sans-serif; " +
                             "-fx-font-size: 13px; " +
                             "-fx-background-color: #475569; " +
                             "-fx-text-fill: #E2E8F0; " +
                             "-fx-background-radius: 8px;");
        
        trainIdBox.getChildren().addAll(trainIdLabel, trainIdField);
        
        // Stations Section
        VBox stationsBox = new VBox(10);
        stationsBox.setStyle("-fx-background-color: #2D3748; " +
                            "-fx-background-radius: 12px; " +
                            "-fx-padding: 15px;");
        
        Label stationsLabel = new Label("Sequence of Stations:");
        stationsLabel.setStyle("-fx-font-family: 'Segoe UI', 'Roboto', sans-serif; " +
                              "-fx-font-size: 14px; " +
                              "-fx-font-weight: 600; " +
                              "-fx-text-fill: #E2E8F0;");
        
        HBox stationInputBox = new HBox(10);
        stationIdField = new TextField();
        stationIdField.setPromptText("Enter station ID");
        stationIdField.setStyle("-fx-font-family: 'Segoe UI', 'Roboto', sans-serif; " +
                               "-fx-font-size: 13px; " +
                               "-fx-background-color: #475569; " +
                               "-fx-text-fill: #E2E8F0; " +
                               "-fx-background-radius: 8px;");
        stationIdField.setPrefWidth(200);
        
        addStationButton = new Button("Add Station");
        addStationButton.setStyle("-fx-font-family: 'Segoe UI', 'Roboto', sans-serif; " +
                                 "-fx-font-size: 13px; " +
                                 "-fx-background-color: #6366F1; " +
                                 "-fx-text-fill: #FFFFFF; " +
                                 "-fx-background-radius: 8px; " +
                                 "-fx-cursor: hand;");
        addStationButton.setOnAction(e -> addStation());
        
        removeStationButton = new Button("Remove");
        removeStationButton.setStyle("-fx-font-family: 'Segoe UI', 'Roboto', sans-serif; " +
                                    "-fx-font-size: 13px; " +
                                    "-fx-background-color: #EF4444; " +
                                    "-fx-text-fill: #FFFFFF; " +
                                    "-fx-background-radius: 8px; " +
                                    "-fx-cursor: hand;");
        removeStationButton.setOnAction(e -> removeSelectedStation());
        
        clearStationsButton = new Button("Clear All");
        clearStationsButton.setStyle("-fx-font-family: 'Segoe UI', 'Roboto', sans-serif; " +
                                    "-fx-font-size: 13px; " +
                                    "-fx-background-color: #475569; " +
                                    "-fx-text-fill: #E2E8F0; " +
                                    "-fx-background-radius: 8px; " +
                                    "-fx-cursor: hand;");
        clearStationsButton.setOnAction(e -> clearStations());
        
        HBox.setHgrow(stationIdField, Priority.ALWAYS);
        stationInputBox.getChildren().addAll(stationIdField, addStationButton, removeStationButton, clearStationsButton);
        
        stationIdsList = new ListView<>();
        stationIdsList.setPrefHeight(150);
        stationIdsList.setStyle("-fx-font-family: 'Consolas', 'Monaco', monospace; " +
                               "-fx-font-size: 12px; " +
                               "-fx-background-color: #1E293B; " +
                               "-fx-text-fill: #E2E8F0;");
        
        stationsBox.getChildren().addAll(stationsLabel, stationInputBox, stationIdsList);
        
        // Freights Section
        VBox freightsBox = new VBox(10);
        freightsBox.setStyle("-fx-background-color: #2D3748; " +
                            "-fx-background-radius: 12px; " +
                            "-fx-padding: 15px;");
        
        Label freightsLabel = new Label("Selected Freights:");
        freightsLabel.setStyle("-fx-font-family: 'Segoe UI', 'Roboto', sans-serif; " +
                              "-fx-font-size: 14px; " +
                              "-fx-font-weight: 600; " +
                              "-fx-text-fill: #E2E8F0;");
        
        HBox freightInputBox = new HBox(10);
        TextField freightIdField = new TextField();
        freightIdField.setPromptText("Enter freight ID");
        freightIdField.setStyle("-fx-font-family: 'Segoe UI', 'Roboto', sans-serif; " +
                               "-fx-font-size: 13px; " +
                               "-fx-background-color: #475569; " +
                               "-fx-text-fill: #E2E8F0; " +
                               "-fx-background-radius: 8px;");
        freightIdField.setPrefWidth(200);
        
        addFreightButton = new Button("Add Freight");
        addFreightButton.setStyle("-fx-font-family: 'Segoe UI', 'Roboto', sans-serif; " +
                                 "-fx-font-size: 13px; " +
                                 "-fx-background-color: #6366F1; " +
                                 "-fx-text-fill: #FFFFFF; " +
                                 "-fx-background-radius: 8px; " +
                                 "-fx-cursor: hand;");
        addFreightButton.setOnAction(e -> addFreight(freightIdField));
        
        removeFreightButton = new Button("Remove");
        removeFreightButton.setStyle("-fx-font-family: 'Segoe UI', 'Roboto', sans-serif; " +
                                    "-fx-font-size: 13px; " +
                                    "-fx-background-color: #EF4444; " +
                                    "-fx-text-fill: #FFFFFF; " +
                                    "-fx-background-radius: 8px; " +
                                    "-fx-cursor: hand;");
        removeFreightButton.setOnAction(e -> removeSelectedFreight());
        
        HBox.setHgrow(freightIdField, Priority.ALWAYS);
        freightInputBox.getChildren().addAll(freightIdField, addFreightButton, removeFreightButton);
        
        selectedFreightsList = new ListView<>();
        selectedFreightsList.setPrefHeight(150);
        selectedFreightsList.setStyle("-fx-font-family: 'Consolas', 'Monaco', monospace; " +
                                     "-fx-font-size: 12px; " +
                                     "-fx-background-color: #1E293B; " +
                                     "-fx-text-fill: #E2E8F0;");
        
        freightsBox.getChildren().addAll(freightsLabel, freightInputBox, selectedFreightsList);
        
        // Create Button
        createRouteButton = new Button("Create Route");
        createRouteButton.setPrefWidth(400);
        createRouteButton.setPrefHeight(45);
        createRouteButton.setStyle("-fx-font-family: 'Segoe UI', 'Roboto', sans-serif; " +
                                  "-fx-font-size: 15px; " +
                                  "-fx-font-weight: 600; " +
                                  "-fx-background-color: #10B981; " +
                                  "-fx-text-fill: #FFFFFF; " +
                                  "-fx-background-radius: 12px; " +
                                  "-fx-cursor: hand; " +
                                  "-fx-effect: dropshadow(gaussian, rgba(16, 185, 129, 0.4), 8, 0, 0, 2);");
        createRouteButton.setOnAction(e -> createRoute());
        
        content.getChildren().addAll(routeTypeBox, trainIdBox, stationsBox, freightsBox, createRouteButton);
        tab.setContent(content);
        
        updateRouteTypeUI();
        
        return tab;
    }
    
    /**
     * Creates the View Route tab.
     */
    private Tab createViewRouteTab() {
        Tab tab = new Tab("View Route");
        tab.setStyle("-fx-background-color: #1B2533;");
        
        VBox content = new VBox(20);
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color: #1B2533;");
        
        // Train ID Input
        VBox inputBox = new VBox(10);
        inputBox.setStyle("-fx-background-color: #2D3748; " +
                         "-fx-background-radius: 12px; " +
                         "-fx-padding: 15px;");
        
        Label trainIdLabel = new Label("Train ID:");
        trainIdLabel.setStyle("-fx-font-family: 'Segoe UI', 'Roboto', sans-serif; " +
                             "-fx-font-size: 14px; " +
                             "-fx-font-weight: 600; " +
                             "-fx-text-fill: #E2E8F0;");
        
        HBox inputRow = new HBox(10);
        viewTrainIdField = new TextField();
        viewTrainIdField.setPromptText("Enter train ID");
        viewTrainIdField.setStyle("-fx-font-family: 'Segoe UI', 'Roboto', sans-serif; " +
                                 "-fx-font-size: 13px; " +
                                 "-fx-background-color: #475569; " +
                                 "-fx-text-fill: #E2E8F0; " +
                                 "-fx-background-radius: 8px;");
        
        viewRouteButton = new Button("View Route");
        viewRouteButton.setStyle("-fx-font-family: 'Segoe UI', 'Roboto', sans-serif; " +
                                "-fx-font-size: 13px; " +
                                "-fx-background-color: #6366F1; " +
                                "-fx-text-fill: #FFFFFF; " +
                                "-fx-background-radius: 8px; " +
                                "-fx-cursor: hand;");
        viewRouteButton.setOnAction(e -> viewRoute());
        
        HBox.setHgrow(viewTrainIdField, Priority.ALWAYS);
        inputRow.getChildren().addAll(viewTrainIdField, viewRouteButton);
        inputBox.getChildren().addAll(trainIdLabel, inputRow);
        
        // Route Details
        VBox detailsBox = new VBox(10);
        detailsBox.setStyle("-fx-background-color: #2D3748; " +
                           "-fx-background-radius: 12px; " +
                           "-fx-padding: 15px;");
        
        Label detailsLabel = new Label("Route Details:");
        detailsLabel.setStyle("-fx-font-family: 'Segoe UI', 'Roboto', sans-serif; " +
                             "-fx-font-size: 14px; " +
                             "-fx-font-weight: 600; " +
                             "-fx-text-fill: #E2E8F0;");
        
        routeDetailsArea = new TextArea();
        routeDetailsArea.setEditable(false);
        routeDetailsArea.setPrefHeight(500);
        routeDetailsArea.setStyle("-fx-font-family: 'Consolas', 'Monaco', monospace; " +
                                 "-fx-font-size: 12px; " +
                                 "-fx-background-color: #1E293B; " +
                                 "-fx-text-fill: #E2E8F0; " +
                                 "-fx-background-radius: 8px;");
        routeDetailsArea.setPromptText("Enter a train ID and click 'View Route' to see route details...");
        
        detailsBox.getChildren().addAll(detailsLabel, routeDetailsArea);
        
        content.getChildren().addAll(inputBox, detailsBox);
        tab.setContent(content);
        
        return tab;
    }
    
    /**
     * Updates the UI based on selected route type.
     */
    private void updateRouteTypeUI() {
        boolean isSimple = simpleRouteRadio.isSelected();
        // Both route types allow freight selection, but simple route should only have 1
        // The validation happens in createRoute()
    }
    
    /**
     * Loads pending freights from the database.
     */
    private void loadPendingFreights() {
        statusLabel.setText("Loading pending freights...");
        progressIndicator.setVisible(true);
        refreshPendingButton.setDisable(true);
        
        Service<List<Freight>> service = new Service<List<Freight>>() {
            @Override
            protected Task<List<Freight>> createTask() {
                return new Task<List<Freight>>() {
                    @Override
                    protected List<Freight> call() throws Exception {
                        return controller.getPendingFreights();
                    }
                };
            }
        };
        
        service.setOnSucceeded(e -> {
            List<Freight> freights = service.getValue();
            pendingFreightsData.clear();
            pendingFreightsData.addAll(freights);
            pendingFreightsCountLabel.setText("Total: " + freights.size() + " pending freights");
            progressIndicator.setVisible(false);
            refreshPendingButton.setDisable(false);
            statusLabel.setText("Ready");
        });
        
        service.setOnFailed(e -> {
            Platform.runLater(() -> {
                progressIndicator.setVisible(false);
                refreshPendingButton.setDisable(false);
                statusLabel.setText("Error loading freights");
                showError("Error", "Failed to load pending freights: " + service.getException().getMessage());
            });
        });
        
        service.start();
    }
    
    /**
     * Adds a station to the list.
     */
    private void addStation() {
        String text = stationIdField.getText().trim();
        if (text.isEmpty()) {
            showError("Error", "Please enter a station ID.");
            return;
        }
        
        try {
            int stationId = Integer.parseInt(text);
            ObservableList<Integer> stations = stationIdsList.getItems();
            if (!stations.contains(stationId)) {
                stations.add(stationId);
                stationIdField.clear();
            } else {
                showError("Error", "Station ID already in the list.");
            }
        } catch (NumberFormatException e) {
            showError("Error", "Invalid station ID. Please enter a number.");
        }
    }
    
    /**
     * Removes the selected station from the list.
     */
    private void removeSelectedStation() {
        Integer selected = stationIdsList.getSelectionModel().getSelectedItem();
        if (selected != null) {
            stationIdsList.getItems().remove(selected);
        }
    }
    
    /**
     * Clears all stations from the list.
     */
    private void clearStations() {
        stationIdsList.getItems().clear();
    }
    
    /**
     * Adds a freight to the list.
     */
    private void addFreight(TextField freightIdField) {
        String text = freightIdField.getText().trim();
        if (text.isEmpty()) {
            showError("Error", "Please enter a freight ID.");
            return;
        }
        
        try {
            int freightId = Integer.parseInt(text);
            
            // Check if freight exists in pending freights
            boolean exists = false;
            for (Freight f : pendingFreightsData) {
                if (f.getId() == freightId) {
                    exists = true;
                    break;
                }
            }
            
            if (!exists) {
                showError("Error", "Freight ID not found in pending freights.");
                return;
            }
            
            ObservableList<Integer> freights = selectedFreightsList.getItems();
            if (!freights.contains(freightId)) {
                freights.add(freightId);
                freightIdField.clear();
            } else {
                showError("Error", "Freight ID already in the list.");
            }
        } catch (NumberFormatException e) {
            showError("Error", "Invalid freight ID. Please enter a number.");
        }
    }
    
    /**
     * Removes the selected freight from the list.
     */
    private void removeSelectedFreight() {
        Integer selected = selectedFreightsList.getSelectionModel().getSelectedItem();
        if (selected != null) {
            selectedFreightsList.getItems().remove(selected);
        }
    }
    
    /**
     * Creates a route (simple or complex).
     */
    private void createRoute() {
        // Validate train ID
        String trainIdText = trainIdField.getText().trim();
        if (trainIdText.isEmpty()) {
            showError("Error", "Please enter a train ID.");
            return;
        }
        
        int trainId;
        try {
            trainId = Integer.parseInt(trainIdText);
        } catch (NumberFormatException e) {
            showError("Error", "Invalid train ID. Please enter a number.");
            return;
        }
        
        // Validate stations
        List<Integer> stationIds = new ArrayList<>(stationIdsList.getItems());
        if (stationIds.size() < 2) {
            showError("Error", "At least 2 stations are required for a route.");
            return;
        }
        
        // Validate freights
        List<Integer> freightIds = new ArrayList<>(selectedFreightsList.getItems());
        boolean isSimple = simpleRouteRadio.isSelected();
        
        if (isSimple) {
            if (freightIds.size() != 1) {
                showError("Error", "Simple route requires exactly 1 freight.");
                return;
            }
        } else {
            if (freightIds.isEmpty()) {
                showError("Error", "Complex route requires at least 1 freight.");
                return;
            }
        }
        
        statusLabel.setText("Creating route...");
        progressIndicator.setVisible(true);
        createRouteButton.setDisable(true);
        
        Service<Boolean> service = new Service<Boolean>() {
            @Override
            protected Task<Boolean> createTask() {
                return new Task<Boolean>() {
                    @Override
                    protected Boolean call() throws Exception {
                        if (isSimple) {
                            return controller.createSimpleRoute(trainId, stationIds, freightIds.get(0));
                        } else {
                            return controller.createComplexRoute(trainId, stationIds, freightIds);
                        }
                    }
                };
            }
        };
        
        service.setOnSucceeded(e -> {
            boolean success = service.getValue();
            progressIndicator.setVisible(false);
            createRouteButton.setDisable(false);
            
            if (success) {
                statusLabel.setText("Route created successfully!");
                showSuccess("Success", "Route has been created successfully.");
                
                // Clear form
                trainIdField.clear();
                stationIdsList.getItems().clear();
                selectedFreightsList.getItems().clear();
                
                // Refresh pending freights
                loadPendingFreights();
            } else {
                statusLabel.setText("Failed to create route");
                showError("Error", "Failed to create route. Please try again.");
            }
        });
        
        service.setOnFailed(e -> {
            Platform.runLater(() -> {
                progressIndicator.setVisible(false);
                createRouteButton.setDisable(false);
                statusLabel.setText("Error occurred");
                showError("Error", "Failed to create route: " + service.getException().getMessage());
            });
        });
        
        service.start();
    }
    
    /**
     * Views route details for a given train ID.
     */
    private void viewRoute() {
        String trainIdText = viewTrainIdField.getText().trim();
        if (trainIdText.isEmpty()) {
            showError("Error", "Please enter a train ID.");
            return;
        }
        
        int trainId;
        try {
            trainId = Integer.parseInt(trainIdText);
        } catch (NumberFormatException e) {
            showError("Error", "Invalid train ID. Please enter a number.");
            return;
        }
        
        statusLabel.setText("Loading route details...");
        progressIndicator.setVisible(true);
        viewRouteButton.setDisable(true);
        
        Service<String> service = new Service<String>() {
            @Override
            protected Task<String> createTask() {
                return new Task<String>() {
                    @Override
                    protected String call() throws Exception {
                        // Get routes for this train
                        List<Route> allRoutes = controller.getAllRoutes();
                        List<Route> trainRoutes = new ArrayList<>();
                        
                        for (Route route : allRoutes) {
                            if (route.getTrainID() == trainId) {
                                trainRoutes.add(route);
                            }
                        }
                        
                        if (trainRoutes.isEmpty()) {
                            return "No routes found for Train ID: " + trainId;
                        }
                        
                        // Sort by order
                        trainRoutes.sort((r1, r2) -> Integer.compare(r1.getOrder(), r2.getOrder()));
                        
                        // Get freights for this train
                        List<Freight> allFreights = controller.verifyFreighAssigned();
                        List<Freight> trainFreights = new ArrayList<>();
                        for (Freight f : allFreights) {
                            if (f.getTrainID() == trainId && f.getTrainID() != 0) {
                                trainFreights.add(f);
                            }
                        }
                        
                        // Build details string
                        StringBuilder sb = new StringBuilder();
                        sb.append("=== Route Summary for Train ID: ").append(trainId).append(" ===\n\n");
                        sb.append("Sequence of Stations:\n");
                        for (int i = 0; i < trainRoutes.size(); i++) {
                            Route route = trainRoutes.get(i);
                            sb.append("  ").append(i + 1).append(". Station ID: ")
                              .append(route.getFacilityID()).append(" (Order: ")
                              .append(route.getOrder()).append(")\n");
                        }
                        
                        sb.append("\nCargos to be Loaded/Unloaded:\n");
                        if (trainFreights.isEmpty()) {
                            sb.append("  No cargos assigned to this route.\n");
                        } else {
                            for (Freight f : trainFreights) {
                                sb.append("  Freight ID: ").append(f.getId())
                                  .append(" | Cargo Type: ").append(f.getCargoType())
                                  .append(" | Wagon ID: ").append(f.getWagonID()).append("\n");
                            }
                        }
                        
                        return sb.toString();
                    }
                };
            }
        };
        
        service.setOnSucceeded(e -> {
            String details = service.getValue();
            routeDetailsArea.setText(details);
            progressIndicator.setVisible(false);
            viewRouteButton.setDisable(false);
            statusLabel.setText("Ready");
        });
        
        service.setOnFailed(e -> {
            Platform.runLater(() -> {
                progressIndicator.setVisible(false);
                viewRouteButton.setDisable(false);
                statusLabel.setText("Error occurred");
                routeDetailsArea.setText("Error loading route details: " + service.getException().getMessage());
            });
        });
        
        service.start();
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
    
    /**
     * Shows a success dialog.
     */
    private void showSuccess(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

