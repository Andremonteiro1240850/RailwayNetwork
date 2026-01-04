package Lapr.JavaFX;

import Lapr.DataBaseAssociation.AvailableWagonDAO;
import Lapr.Domain.RoutePlannerRoute;
import Lapr.Domain.RoutePlannerService;
import Sprint1.Domain.Facility;
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
import java.util.Map;

/**
 * JavaFX UI for USLP08 - Route Planner.
 * 
 * Allows Freight Manager to:
 * - View available wagons grouped by cargo type
 * - Define routes (simple or complex) with stations
 * - Assign load/unload operations for wagons at stations
 * - View route details with sequence of stations and cargos
 * 
 * @author Development Team
 * @version 2.0
 */
public class RoutePlannerFXUI {
    private RoutePlannerService service;
    private Stage stage;
    
    // Tabs
    private TabPane mainTabPane;
    private Tab availableWagonsTab;
    private Tab createRouteTab;
    private Tab viewRouteTab;
    
    // Available Wagons Tab
    private TableView<AvailableWagonDAO.AvailableWagon> wagonsTable;
    private ObservableList<AvailableWagonDAO.AvailableWagon> wagonsData;
    private ComboBox<String> cargoTypeFilter;
    private Label wagonsCountLabel;
    private Button refreshWagonsButton;
    
    // Create Route Tab
    private RadioButton simpleRouteRadio;
    private RadioButton complexRouteRadio;
    private ToggleGroup routeTypeGroup;
    private TextField routeNameField;
    private ComboBox<Facility> startStationCombo;
    private ComboBox<Facility> endStationCombo;
    private ListView<Facility> intermediateStationsList;
    private ComboBox<Facility> stationCombo;
    private Button addStationButton;
    private Button removeStationButton;
    private Button clearStationsButton;
    private ListView<AvailableWagonDAO.AvailableWagon> selectedWagonsList;
    private Button addWagonButton;
    private Button removeWagonButton;
    private Button createRouteButton;
    
    // View Route Tab
    private ComboBox<RoutePlannerRoute> routeCombo;
    private Button viewRouteButton;
    private TextArea routeDetailsArea;
    
    // Status
    private Label statusLabel;
    private ProgressIndicator progressIndicator;
    
    // Data
    private List<Facility> allFacilities;
    private Map<String, List<AvailableWagonDAO.AvailableWagon>> wagonsByCargoType;
    private ComboBox<AvailableWagonDAO.AvailableWagon> wagonCombo; // Reference to wagon combo in Create Route tab
    
    public RoutePlannerFXUI() {
        this.service = new RoutePlannerService();
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
            "As Freight Manager, define and assign routes to accomplish a list of pending freights. " +
            "A route can be simple or complex, with a sequence of stations and cargos to be loaded/unloaded."
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
        
        availableWagonsTab = createAvailableWagonsTab();
        createRouteTab = createCreateRouteTab();
        viewRouteTab = createViewRouteTab();
        
        mainTabPane.getTabs().addAll(availableWagonsTab, createRouteTab, viewRouteTab);
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
        
        Scene scene = new Scene(root, 1200, 800);
        stage.setScene(scene);
        stage.setResizable(true);
        stage.setMinWidth(1200);
        stage.setMinHeight(800);
        
        // Load initial data
        loadAvailableWagons();
        loadFacilities();
        loadRoutes();
        
        stage.show();
    }
    
    /**
     * Creates the Available Wagons tab.
     */
    private Tab createAvailableWagonsTab() {
        Tab tab = new Tab("Available Wagons");
        tab.setStyle("-fx-background-color: #1B2533;");
        
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color: #1B2533;");
        
        // Header
        HBox headerBox = new HBox(15);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        
        Label titleLabel = new Label("Available Wagons (Grouped by Cargo Type)");
        titleLabel.setStyle("-fx-font-family: 'Segoe UI', 'Roboto', sans-serif; " +
                           "-fx-font-size: 20px; " +
                           "-fx-font-weight: 600; " +
                           "-fx-text-fill: #E2E8F0;");
        
        wagonsCountLabel = new Label("Loading...");
        wagonsCountLabel.setStyle("-fx-font-family: 'Segoe UI', 'Roboto', sans-serif; " +
                                  "-fx-font-size: 13px; " +
                                  "-fx-text-fill: #94A3B8;");
        
        cargoTypeFilter = new ComboBox<>();
        cargoTypeFilter.setPromptText("Filter by cargo type");
        cargoTypeFilter.setStyle("-fx-font-family: 'Segoe UI', 'Roboto', sans-serif; " +
                                "-fx-font-size: 13px; " +
                                "-fx-background-color: #475569; " +
                                "-fx-text-fill: #E2E8F0;");
        cargoTypeFilter.setOnAction(e -> filterWagonsByCargoType());
        
        refreshWagonsButton = new Button("Refresh");
        refreshWagonsButton.setStyle("-fx-font-family: 'Segoe UI', 'Roboto', sans-serif; " +
                                    "-fx-font-size: 13px; " +
                                    "-fx-background-color: #6366F1; " +
                                    "-fx-text-fill: #FFFFFF; " +
                                    "-fx-background-radius: 8px; " +
                                    "-fx-cursor: hand;");
        refreshWagonsButton.setOnAction(e -> loadAvailableWagons());
        
        HBox.setHgrow(wagonsCountLabel, Priority.ALWAYS);
        headerBox.getChildren().addAll(titleLabel, wagonsCountLabel, cargoTypeFilter, refreshWagonsButton);
        
        // Table
        wagonsTable = new TableView<>();
        wagonsTable.setStyle("-fx-background-color: #2D3748; " +
                            "-fx-background-radius: 12px;");
        wagonsTable.setPrefHeight(550);
        
        TableColumn<AvailableWagonDAO.AvailableWagon, Integer> idColumn = 
            new TableColumn<>("Wagon ID");
        idColumn.setCellValueFactory(new PropertyValueFactory<>("wagonId"));
        idColumn.setPrefWidth(100);
        
        TableColumn<AvailableWagonDAO.AvailableWagon, String> cargoColumn = 
            new TableColumn<>("Cargo Type");
        cargoColumn.setCellValueFactory(new PropertyValueFactory<>("cargoType"));
        cargoColumn.setPrefWidth(150);
        
        TableColumn<AvailableWagonDAO.AvailableWagon, Integer> modelColumn = 
            new TableColumn<>("Model ID");
        modelColumn.setCellValueFactory(new PropertyValueFactory<>("modelId"));
        modelColumn.setPrefWidth(100);
        
        TableColumn<AvailableWagonDAO.AvailableWagon, String> modelNameColumn = 
            new TableColumn<>("Model Name");
        modelNameColumn.setCellValueFactory(new PropertyValueFactory<>("modelName"));
        modelNameColumn.setPrefWidth(200);
        
        TableColumn<AvailableWagonDAO.AvailableWagon, Double> emptyWeightColumn = 
            new TableColumn<>("Empty Weight (kg)");
        emptyWeightColumn.setCellValueFactory(new PropertyValueFactory<>("emptyWeightKg"));
        emptyWeightColumn.setPrefWidth(150);
        
        TableColumn<AvailableWagonDAO.AvailableWagon, Double> maxLoadColumn = 
            new TableColumn<>("Max Load (kg)");
        maxLoadColumn.setCellValueFactory(new PropertyValueFactory<>("maxLoadWeightKg"));
        maxLoadColumn.setPrefWidth(150);
        
        wagonsTable.getColumns().addAll(idColumn, cargoColumn, modelColumn, modelNameColumn, 
                                        emptyWeightColumn, maxLoadColumn);
        
        wagonsData = FXCollections.observableArrayList();
        wagonsTable.setItems(wagonsData);
        
        content.getChildren().addAll(headerBox, wagonsTable);
        tab.setContent(content);
        
        return tab;
    }
    
    /**
     * Creates the Create Route tab.
     */
    private Tab createCreateRouteTab() {
        Tab tab = new Tab("Create Route");
        tab.setStyle("-fx-background-color: #1B2533;");
        
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: #1B2533;");
        
        VBox content = new VBox(20);
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color: #1B2533;");
        
        // Route Type Selection
        VBox routeTypeBox = new VBox(10);
        routeTypeBox.setStyle("-fx-background-color: #2D3748; " +
                             "-fx-background-radius: 12px; " +
                             "-fx-padding: 15px;");
        
        Label routeTypeLabel = new Label("Route Type:");
        routeTypeLabel.setStyle("-fx-font-family: 'Segoe UI', 'Roboto', sans-serif; " +
                               "-fx-font-size: 14px; " +
                               "-fx-font-weight: 600; " +
                               "-fx-text-fill: #E2E8F0;");
        
        routeTypeGroup = new ToggleGroup();
        simpleRouteRadio = new RadioButton("Simple Route (single wagon)");
        simpleRouteRadio.setToggleGroup(routeTypeGroup);
        simpleRouteRadio.setSelected(true);
        simpleRouteRadio.setStyle("-fx-text-fill: #E2E8F0;");
        simpleRouteRadio.setOnAction(e -> updateRouteTypeUI());
        
        complexRouteRadio = new RadioButton("Complex Route (multiple wagons)");
        complexRouteRadio.setToggleGroup(routeTypeGroup);
        complexRouteRadio.setStyle("-fx-text-fill: #E2E8F0;");
        complexRouteRadio.setOnAction(e -> updateRouteTypeUI());
        
        routeTypeBox.getChildren().addAll(routeTypeLabel, simpleRouteRadio, complexRouteRadio);
        
        // Route Name
        VBox routeNameBox = new VBox(10);
        routeNameBox.setStyle("-fx-background-color: #2D3748; " +
                             "-fx-background-radius: 12px; " +
                             "-fx-padding: 15px;");
        
        Label routeNameLabel = new Label("Route Name:");
        routeNameLabel.setStyle("-fx-font-family: 'Segoe UI', 'Roboto', sans-serif; " +
                               "-fx-font-size: 14px; " +
                               "-fx-font-weight: 600; " +
                               "-fx-text-fill: #E2E8F0;");
        
        routeNameField = new TextField();
        routeNameField.setPromptText("Enter route name");
        routeNameField.setStyle("-fx-font-family: 'Segoe UI', 'Roboto', sans-serif; " +
                               "-fx-font-size: 13px; " +
                               "-fx-background-color: #475569; " +
                               "-fx-text-fill: #E2E8F0; " +
                               "-fx-background-radius: 8px;");
        
        routeNameBox.getChildren().addAll(routeNameLabel, routeNameField);
        
        // Stations Section
        VBox stationsBox = new VBox(10);
        stationsBox.setStyle("-fx-background-color: #2D3748; " +
                            "-fx-background-radius: 12px; " +
                            "-fx-padding: 15px;");
        
        Label stationsLabel = new Label("Route Stations:");
        stationsLabel.setStyle("-fx-font-family: 'Segoe UI', 'Roboto', sans-serif; " +
                              "-fx-font-size: 14px; " +
                              "-fx-font-weight: 600; " +
                              "-fx-text-fill: #E2E8F0;");
        
        // Start and End stations
        HBox startEndBox = new HBox(15);
        VBox startBox = new VBox(5);
        Label startLabel = new Label("Start Station:");
        startLabel.setStyle("-fx-text-fill: #E2E8F0;");
        startStationCombo = new ComboBox<>();
        startStationCombo.setConverter(new FacilityStringConverter());
        startStationCombo.setStyle("-fx-background-color: #475569; -fx-text-fill: #E2E8F0;");
        startBox.getChildren().addAll(startLabel, startStationCombo);
        
        VBox endBox = new VBox(5);
        Label endLabel = new Label("End Station:");
        endLabel.setStyle("-fx-text-fill: #E2E8F0;");
        endStationCombo = new ComboBox<>();
        endStationCombo.setConverter(new FacilityStringConverter());
        endStationCombo.setStyle("-fx-background-color: #475569; -fx-text-fill: #E2E8F0;");
        endBox.getChildren().addAll(endLabel, endStationCombo);
        
        HBox.setHgrow(startBox, Priority.ALWAYS);
        HBox.setHgrow(endBox, Priority.ALWAYS);
        startEndBox.getChildren().addAll(startBox, endBox);
        
        // Intermediate stations
        Label intermediateLabel = new Label("Intermediate Stations (optional):");
        intermediateLabel.setStyle("-fx-text-fill: #E2E8F0;");
        
        HBox stationInputBox = new HBox(10);
        stationCombo = new ComboBox<>();
        stationCombo.setConverter(new FacilityStringConverter());
        stationCombo.setStyle("-fx-background-color: #475569; -fx-text-fill: #E2E8F0;");
        stationCombo.setPrefWidth(300);
        
        addStationButton = new Button("Add Station");
        addStationButton.setStyle("-fx-font-family: 'Segoe UI', 'Roboto', sans-serif; " +
                                 "-fx-font-size: 13px; " +
                                 "-fx-background-color: #6366F1; " +
                                 "-fx-text-fill: #FFFFFF; " +
                                 "-fx-background-radius: 8px; " +
                                 "-fx-cursor: hand;");
        addStationButton.setOnAction(e -> addIntermediateStation());
        
        removeStationButton = new Button("Remove");
        removeStationButton.setStyle("-fx-font-family: 'Segoe UI', 'Roboto', sans-serif; " +
                                    "-fx-font-size: 13px; " +
                                    "-fx-background-color: #EF4444; " +
                                    "-fx-text-fill: #FFFFFF; " +
                                    "-fx-background-radius: 8px; " +
                                    "-fx-cursor: hand;");
        removeStationButton.setOnAction(e -> removeSelectedIntermediateStation());
        
        clearStationsButton = new Button("Clear All");
        clearStationsButton.setStyle("-fx-font-family: 'Segoe UI', 'Roboto', sans-serif; " +
                                    "-fx-font-size: 13px; " +
                                    "-fx-background-color: #475569; " +
                                    "-fx-text-fill: #E2E8F0; " +
                                    "-fx-background-radius: 8px; " +
                                    "-fx-cursor: hand;");
        clearStationsButton.setOnAction(e -> clearIntermediateStations());
        
        HBox.setHgrow(stationCombo, Priority.ALWAYS);
        stationInputBox.getChildren().addAll(stationCombo, addStationButton, removeStationButton, clearStationsButton);
        
        intermediateStationsList = new ListView<>();
        intermediateStationsList.setPrefHeight(120);
        intermediateStationsList.setStyle("-fx-background-color: #334155; " +
                                         "-fx-control-inner-background: #334155; " +
                                         "-fx-text-fill: #F1F5F9;");
        intermediateStationsList.setCellFactory(param -> new ListCell<Facility>() {
            @Override
            protected void updateItem(Facility item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("-fx-background-color: #334155; -fx-text-fill: #F1F5F9;");
                } else {
                    setText(item.getName() + " (ID: " + item.getId() + ")");
                    setStyle("-fx-background-color: #334155; " +
                            "-fx-text-fill: #F1F5F9; " +
                            "-fx-font-size: 13px; " +
                            "-fx-font-weight: 500; " +
                            "-fx-padding: 8px;");
                }
            }
            
            @Override
            public void updateSelected(boolean selected) {
                super.updateSelected(selected);
                if (selected && getItem() != null) {
                    setStyle("-fx-background-color: #475569; " +
                            "-fx-text-fill: #FFFFFF; " +
                            "-fx-font-size: 13px; " +
                            "-fx-font-weight: 600; " +
                            "-fx-padding: 8px;");
                }
            }
        });
        
        stationsBox.getChildren().addAll(stationsLabel, startEndBox, intermediateLabel, 
                                        stationInputBox, intermediateStationsList);
        
        // Wagons Section
        VBox wagonsBox = new VBox(10);
        wagonsBox.setStyle("-fx-background-color: #2D3748; " +
                          "-fx-background-radius: 12px; " +
                          "-fx-padding: 15px;");
        
        Label wagonsLabel = new Label("Selected Wagons:");
        wagonsLabel.setStyle("-fx-font-family: 'Segoe UI', 'Roboto', sans-serif; " +
                           "-fx-font-size: 14px; " +
                           "-fx-font-weight: 600; " +
                           "-fx-text-fill: #E2E8F0;");
        
        HBox wagonInputBox = new HBox(10);
        wagonCombo = new ComboBox<>();
        wagonCombo.setConverter(new WagonStringConverter());
        wagonCombo.setStyle("-fx-background-color: #475569; -fx-text-fill: #E2E8F0;");
        wagonCombo.setPrefWidth(300);
        // Populate wagon combo from available wagons (will be updated when wagons are loaded)
        wagonCombo.setItems(FXCollections.observableArrayList());
        
        addWagonButton = new Button("Add Wagon");
        addWagonButton.setStyle("-fx-font-family: 'Segoe UI', 'Roboto', sans-serif; " +
                               "-fx-font-size: 13px; " +
                               "-fx-background-color: #6366F1; " +
                               "-fx-text-fill: #FFFFFF; " +
                               "-fx-background-radius: 8px; " +
                               "-fx-cursor: hand;");
        addWagonButton.setOnAction(e -> addWagon(wagonCombo));
        
        removeWagonButton = new Button("Remove");
        removeWagonButton.setStyle("-fx-font-family: 'Segoe UI', 'Roboto', sans-serif; " +
                                  "-fx-font-size: 13px; " +
                                  "-fx-background-color: #EF4444; " +
                                  "-fx-text-fill: #FFFFFF; " +
                                  "-fx-background-radius: 8px; " +
                                  "-fx-cursor: hand;");
        removeWagonButton.setOnAction(e -> removeSelectedWagon());
        
        HBox.setHgrow(wagonCombo, Priority.ALWAYS);
        wagonInputBox.getChildren().addAll(wagonCombo, addWagonButton, removeWagonButton);
        
        selectedWagonsList = new ListView<>();
        selectedWagonsList.setPrefHeight(150);
        selectedWagonsList.setStyle("-fx-background-color: #334155; " +
                                   "-fx-control-inner-background: #334155; " +
                                   "-fx-text-fill: #F1F5F9;");
        selectedWagonsList.setCellFactory(param -> new ListCell<AvailableWagonDAO.AvailableWagon>() {
            @Override
            protected void updateItem(AvailableWagonDAO.AvailableWagon item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("-fx-background-color: #334155; -fx-text-fill: #F1F5F9;");
                } else {
                    setText("Wagon ID: " + item.getWagonId() + " | Cargo: " + item.getCargoType());
                    setStyle("-fx-background-color: #334155; " +
                            "-fx-text-fill: #F1F5F9; " +
                            "-fx-font-size: 13px; " +
                            "-fx-font-weight: 500; " +
                            "-fx-padding: 8px;");
                }
            }
            
            @Override
            public void updateSelected(boolean selected) {
                super.updateSelected(selected);
                if (selected && getItem() != null) {
                    setStyle("-fx-background-color: #475569; " +
                            "-fx-text-fill: #FFFFFF; " +
                            "-fx-font-size: 13px; " +
                            "-fx-font-weight: 600; " +
                            "-fx-padding: 8px;");
                }
            }
        });
        
        wagonsBox.getChildren().addAll(wagonsLabel, wagonInputBox, selectedWagonsList);
        
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
        
        content.getChildren().addAll(routeTypeBox, routeNameBox, stationsBox, wagonsBox, createRouteButton);
        scrollPane.setContent(content);
        tab.setContent(scrollPane);
        
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
        
        // Route Selection
        VBox inputBox = new VBox(10);
        inputBox.setStyle("-fx-background-color: #2D3748; " +
                         "-fx-background-radius: 12px; " +
                         "-fx-padding: 15px;");
        
        Label routeLabel = new Label("Select Route:");
        routeLabel.setStyle("-fx-font-family: 'Segoe UI', 'Roboto', sans-serif; " +
                           "-fx-font-size: 14px; " +
                           "-fx-font-weight: 600; " +
                           "-fx-text-fill: #E2E8F0;");
        
        HBox inputRow = new HBox(10);
        routeCombo = new ComboBox<>();
        routeCombo.setConverter(new RouteStringConverter());
        routeCombo.setStyle("-fx-background-color: #475569; -fx-text-fill: #E2E8F0;");
        
        viewRouteButton = new Button("View Route");
        viewRouteButton.setStyle("-fx-font-family: 'Segoe UI', 'Roboto', sans-serif; " +
                                "-fx-font-size: 13px; " +
                                "-fx-background-color: #6366F1; " +
                                "-fx-text-fill: #FFFFFF; " +
                                "-fx-background-radius: 8px; " +
                                "-fx-cursor: hand;");
        viewRouteButton.setOnAction(e -> viewRoute());
        
        HBox.setHgrow(routeCombo, Priority.ALWAYS);
        inputRow.getChildren().addAll(routeCombo, viewRouteButton);
        inputBox.getChildren().addAll(routeLabel, inputRow);
        
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
        routeDetailsArea.setPromptText("Select a route and click 'View Route' to see details...");
        
        detailsBox.getChildren().addAll(detailsLabel, routeDetailsArea);
        
        content.getChildren().addAll(inputBox, detailsBox);
        tab.setContent(content);
        
        return tab;
    }
    
    // Helper methods
    
    private void loadAvailableWagons() {
        statusLabel.setText("Loading available wagons...");
        progressIndicator.setVisible(true);
        if (refreshWagonsButton != null) {
            refreshWagonsButton.setDisable(true);
        }
        
        Service<Map<String, List<AvailableWagonDAO.AvailableWagon>>> service = 
            new Service<Map<String, List<AvailableWagonDAO.AvailableWagon>>>() {
            @Override
            protected Task<Map<String, List<AvailableWagonDAO.AvailableWagon>>> createTask() {
                return new Task<Map<String, List<AvailableWagonDAO.AvailableWagon>>>() {
                    @Override
                    protected Map<String, List<AvailableWagonDAO.AvailableWagon>> call() throws Exception {
                        return RoutePlannerFXUI.this.service.getAvailableWagonsByCargoType();
                    }
                };
            }
        };
        
        service.setOnSucceeded(e -> {
            wagonsByCargoType = service.getValue();
            wagonsData.clear();
            int total = 0;
            for (List<AvailableWagonDAO.AvailableWagon> wagonList : wagonsByCargoType.values()) {
                wagonsData.addAll(wagonList);
                total += wagonList.size();
            }
            wagonsCountLabel.setText("Total: " + total + " available wagons");
            
            // Update cargo type filter
            ObservableList<String> cargoTypes = FXCollections.observableArrayList();
            cargoTypes.add("All");
            cargoTypes.addAll(wagonsByCargoType.keySet());
            cargoTypeFilter.setItems(cargoTypes);
            cargoTypeFilter.getSelectionModel().select("All");
            
            // Update wagon combo in Create Route tab
            updateWagonCombo();
            
            progressIndicator.setVisible(false);
            if (refreshWagonsButton != null) {
                refreshWagonsButton.setDisable(false);
            }
            statusLabel.setText("Ready");
        });
        
        service.setOnFailed(e -> {
            Platform.runLater(() -> {
                progressIndicator.setVisible(false);
                if (refreshWagonsButton != null) {
                    refreshWagonsButton.setDisable(false);
                }
                statusLabel.setText("Error loading wagons");
                showError("Error", "Failed to load available wagons: " + service.getException().getMessage());
            });
        });
        
        service.start();
    }
    
    private void updateWagonCombo() {
        // Update the wagon combo in Create Route tab
        if (wagonsByCargoType != null && wagonCombo != null) {
            ObservableList<AvailableWagonDAO.AvailableWagon> allWagons = FXCollections.observableArrayList();
            for (List<AvailableWagonDAO.AvailableWagon> wagonList : wagonsByCargoType.values()) {
                allWagons.addAll(wagonList);
            }
            wagonCombo.setItems(allWagons);
        }
    }
    
    private void loadFacilities() {
        Service<List<Facility>> service = new Service<List<Facility>>() {
            @Override
            protected Task<List<Facility>> createTask() {
                return new Task<List<Facility>>() {
                    @Override
                    protected List<Facility> call() throws Exception {
                        return RoutePlannerFXUI.this.service.getAllFacilities();
                    }
                };
            }
        };
        
        service.setOnSucceeded(e -> {
            allFacilities = service.getValue();
            ObservableList<Facility> facilityList = FXCollections.observableArrayList(allFacilities);
            startStationCombo.setItems(facilityList);
            endStationCombo.setItems(facilityList);
            stationCombo.setItems(facilityList);
        });
        
        service.start();
    }
    
    private void loadRoutes() {
        Service<List<RoutePlannerRoute>> service = new Service<List<RoutePlannerRoute>>() {
            @Override
            protected Task<List<RoutePlannerRoute>> createTask() {
                return new Task<List<RoutePlannerRoute>>() {
                    @Override
                    protected List<RoutePlannerRoute> call() throws Exception {
                        return RoutePlannerFXUI.this.service.getAllRoutes();
                    }
                };
            }
        };
        
        service.setOnSucceeded(e -> {
            List<RoutePlannerRoute> routes = service.getValue();
            ObservableList<RoutePlannerRoute> routeList = FXCollections.observableArrayList(routes);
            routeCombo.setItems(routeList);
        });
        
        service.start();
    }
    
    private void filterWagonsByCargoType() {
        String selected = cargoTypeFilter.getSelectionModel().getSelectedItem();
        if (selected == null || "All".equals(selected)) {
            wagonsData.clear();
            for (List<AvailableWagonDAO.AvailableWagon> wagonList : wagonsByCargoType.values()) {
                wagonsData.addAll(wagonList);
            }
        } else {
            wagonsData.clear();
            List<AvailableWagonDAO.AvailableWagon> wagons = wagonsByCargoType.get(selected);
            if (wagons != null) {
                wagonsData.addAll(wagons);
            }
        }
    }
    
    private void updateRouteTypeUI() {
        // Simple route should only allow 1 wagon
        // Validation happens in createRoute()
    }
    
    private void addIntermediateStation() {
        Facility selected = stationCombo.getSelectionModel().getSelectedItem();
        if (selected != null) {
            ObservableList<Facility> stations = intermediateStationsList.getItems();
            if (!stations.contains(selected)) {
                stations.add(selected);
                stationCombo.getSelectionModel().clearSelection();
            } else {
                showError("Error", "Station already in the list.");
            }
        }
    }
    
    private void removeSelectedIntermediateStation() {
        Facility selected = intermediateStationsList.getSelectionModel().getSelectedItem();
        if (selected != null) {
            intermediateStationsList.getItems().remove(selected);
        }
    }
    
    private void clearIntermediateStations() {
        intermediateStationsList.getItems().clear();
    }
    
    private void addWagon(ComboBox<AvailableWagonDAO.AvailableWagon> wagonCombo) {
        AvailableWagonDAO.AvailableWagon selected = wagonCombo.getSelectionModel().getSelectedItem();
        if (selected != null) {
            ObservableList<AvailableWagonDAO.AvailableWagon> wagons = selectedWagonsList.getItems();
            if (!wagons.contains(selected)) {
                wagons.add(selected);
                wagonCombo.getSelectionModel().clearSelection();
            } else {
                showError("Error", "Wagon already in the list.");
            }
        }
    }
    
    private void removeSelectedWagon() {
        AvailableWagonDAO.AvailableWagon selected = selectedWagonsList.getSelectionModel().getSelectedItem();
        if (selected != null) {
            selectedWagonsList.getItems().remove(selected);
        }
    }
    
    private void createRoute() {
        // Validate route name
        String routeName = routeNameField.getText().trim();
        if (routeName.isEmpty()) {
            showError("Error", "Please enter a route name.");
            return;
        }
        
        // Validate stations
        Facility startStation = startStationCombo.getSelectionModel().getSelectedItem();
        Facility endStation = endStationCombo.getSelectionModel().getSelectedItem();
        if (startStation == null || endStation == null) {
            showError("Error", "Please select both start and end stations.");
            return;
        }
        if (startStation.equals(endStation)) {
            showError("Error", "Start and end stations must be different.");
            return;
        }
        
        // Build station list
        List<Integer> stationIds = new ArrayList<>();
        stationIds.add(Integer.parseInt(startStation.getId()));
        for (Facility f : intermediateStationsList.getItems()) {
            stationIds.add(Integer.parseInt(f.getId()));
        }
        stationIds.add(Integer.parseInt(endStation.getId()));
        
        // Validate wagons
        List<Integer> wagonIds = new ArrayList<>();
        for (AvailableWagonDAO.AvailableWagon w : selectedWagonsList.getItems()) {
            wagonIds.add(w.getWagonId());
        }
        
        if (simpleRouteRadio.isSelected() && wagonIds.size() != 1) {
            showError("Error", "Simple route requires exactly 1 wagon.");
            return;
        }
        if (wagonIds.isEmpty()) {
            showError("Error", "Please select at least 1 wagon.");
            return;
        }
        
        statusLabel.setText("Creating route...");
        progressIndicator.setVisible(true);
        createRouteButton.setDisable(true);
        
        // For now, create route without load/unload info (can be added later)
        boolean isSimpleRoute = simpleRouteRadio.isSelected();
        Service<RoutePlannerRoute> service = new Service<RoutePlannerRoute>() {
            @Override
            protected Task<RoutePlannerRoute> createTask() {
                return new Task<RoutePlannerRoute>() {
                    @Override
                    protected RoutePlannerRoute call() throws Exception {
                        return RoutePlannerFXUI.this.service.createRoute(
                            routeName, isSimpleRoute, stationIds, wagonIds, null);
                    }
                };
            }
        };
        
        service.setOnSucceeded(e -> {
            RoutePlannerRoute route = service.getValue();
            progressIndicator.setVisible(false);
            createRouteButton.setDisable(false);
            
            if (route != null) {
                statusLabel.setText("Route created successfully!");
                showSuccess("Success", "Route has been created successfully with ID: " + route.getRouteId());
                
                // Clear form
                routeNameField.clear();
                startStationCombo.getSelectionModel().clearSelection();
                endStationCombo.getSelectionModel().clearSelection();
                intermediateStationsList.getItems().clear();
                selectedWagonsList.getItems().clear();
                
                // Reload routes
                loadRoutes();
            } else {
                statusLabel.setText("Failed to create route");
                showError("Error", "Failed to create route. Please try again.");
            }
        });
        
        service.setOnFailed(e -> {
            Platform.runLater(() -> {
                progressIndicator.setVisible(false);
                createRouteButton.setDisable(false);
                statusLabel.setText("Failed to create route");
                
                Throwable exception = service.getException();
                StringBuilder errorMessage = new StringBuilder("Failed to create route.");
                
                if (exception != null) {
                    // Log full exception details to console
                    System.err.println("=== ERROR CREATING ROUTE ===");
                    System.err.println("Exception type: " + exception.getClass().getName());
                    System.err.println("Exception message: " + exception.getMessage());
                    exception.printStackTrace();
                    
                    errorMessage.append("\n\nError: ").append(exception.getMessage());
                    
                    // Check for SQLException
                    Throwable cause = exception;
                    while (cause != null) {
                        if (cause instanceof java.sql.SQLException) {
                            java.sql.SQLException sqlEx = (java.sql.SQLException) cause;
                            errorMessage.append("\n\nSQL Error Code: ").append(sqlEx.getErrorCode());
                            errorMessage.append("\nSQL State: ").append(sqlEx.getSQLState());
                            String sqlMessage = sqlEx.getMessage();
                            if (sqlMessage != null && !sqlMessage.isEmpty()) {
                                errorMessage.append("\nSQL Message: ").append(sqlMessage);
                            }
                            break;
                        }
                        cause = cause.getCause();
                    }
                    
                    if (exception.getCause() != null && !(exception.getCause() instanceof java.sql.SQLException)) {
                        errorMessage.append("\nCause: ").append(exception.getCause().getMessage());
                    }
                }
                
                showError("Error", errorMessage.toString());
            });
        });
        
        service.start();
    }
    
    private void viewRoute() {
        RoutePlannerRoute selected = routeCombo.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Error", "Please select a route.");
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
                        return RoutePlannerFXUI.this.service.formatRouteDetails(selected);
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
    
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void showSuccess(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    // String converters for ComboBoxes
    private static class FacilityStringConverter extends StringConverter<Facility> {
        @Override
        public String toString(Facility facility) {
            return facility == null ? null : facility.getName() + " (ID: " + facility.getId() + ")";
        }
        
        @Override
        public Facility fromString(String string) {
            return null; // Not needed for display only
        }
    }
    
    private static class WagonStringConverter extends StringConverter<AvailableWagonDAO.AvailableWagon> {
        @Override
        public String toString(AvailableWagonDAO.AvailableWagon wagon) {
            return wagon == null ? null : 
                "Wagon ID: " + wagon.getWagonId() + " | " + wagon.getCargoType();
        }
        
        @Override
        public AvailableWagonDAO.AvailableWagon fromString(String string) {
            return null;
        }
    }
    
    private static class RouteStringConverter extends StringConverter<RoutePlannerRoute> {
        @Override
        public String toString(RoutePlannerRoute route) {
            return route == null ? null : 
                route.getRouteName() + " (ID: " + route.getRouteId() + ")";
        }
        
        @Override
        public RoutePlannerRoute fromString(String string) {
            return null;
        }
    }
}
