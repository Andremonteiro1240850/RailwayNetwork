package Lapr.JavaFX;

import Lapr.Controller.DispatchController;
import Lapr.Domain.DispatchReport;
import Lapr.Domain.Train;
import Sprint1.Domain.Facility;
import Sprint1.Domain.Locomotive;
import Sprint1.Domain.Wagon;
import Sprint1.Domain.TravelCalculationService;
import Sprint1.Domain.IncompatibleGaugeException;
import Lapr.DataBaseAssociation.LocomotiveAOD;
import Lapr.DataBaseAssociation.WagonAOD;

import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.util.StringConverter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Professional JavaFX UI for Train Dispatch Scheduler (USLP07/USLP10).
 * Enterprise-style interface with real-time train summary and professional reporting.
 * 
 * @author Development Team
 * @version 2.0
 */
public class SchedulerFXUI {
    private DispatchController controller;
    private Stage stage;
    
    // Main components
    private TabPane mainTabPane;
    private Button dispatchButton;
    private ProgressIndicator progressIndicator;
    private Label statusLabel;
    
    // Train creation tab - Left column (60%)
    private VBox trainCreationTab;
    private TextField trainIdField;
    private ComboBox<Facility> startStationCombo;
    private ComboBox<Facility> endStationCombo;
    private DatePicker departureDatePicker;
    private TextField departureTimeField;
    private CheckBox wagonsLoadedCheckBox;
    private CheckBox specifyIntermediateStationsCheckBox;
    private VBox intermediateStationsBox;
    private ListView<Facility> intermediateStationsList;
    private ComboBox<Facility> intermediateStationCombo;
    private Button addIntermediateStationButton;
    private Button removeIntermediateStationButton;
    private TableView<Locomotive> availableLocomotivesTable;
    private TableView<Locomotive> selectedLocomotivesTable;
    private TableView<Wagon> availableWagonsTable;
    private TableView<Wagon> selectedWagonsTable;
    private Button addLocomotiveButton;
    private Button removeLocomotiveButton;
    private Button addWagonButton;
    private Button removeWagonButton;
    private Button addTrainButton;
    private ListView<Train> trainsList;
    private Button removeTrainButton;
    
    // Train creation tab - Right column (40%) - Real-time summary
    private VBox summaryPanel;
    private Label summaryTitle;
    private VBox summaryContent;
    
    // Results tab
    private VBox resultsTab;
    private ScrollPane resultsScrollPane;
    private VBox resultsContent;
    
    // Data
    private ObservableList<Facility> facilities;
    private ObservableList<Locomotive> locomotives;
    private ObservableList<Wagon> wagons;
    private ObservableList<Train> trains;
    private DispatchReport currentReport;
    
    /**
     * Constructs a new SchedulerFXUI with default controller.
     * USLP10: Loads all data from database, not CSV files.
     */
    public SchedulerFXUI() {
        this.controller = DispatchController.createFromDatabase();
        initializeData();
    }
    
    /**
     * Constructs a new SchedulerFXUI with custom controller.
     *
     * @param controller the dispatch controller
     */
    public SchedulerFXUI(DispatchController controller) {
        this.controller = controller;
        initializeData();
    }
    
    /**
     * Initializes data from controller and database.
     * USLP10: All data must come from database, not CSV files.
     */
    private void initializeData() {
        facilities = FXCollections.observableArrayList(controller.getStations());

        // Load locomotives and wagons from database (USLP10 requirement)
        LocomotiveAOD locomotiveAOD = new LocomotiveAOD();
        WagonAOD wagonAOD = new WagonAOD();

        try {
            locomotives = FXCollections.observableArrayList(locomotiveAOD.getAllLocomotives());
            wagons = FXCollections.observableArrayList(wagonAOD.getAllWagons());

            if (locomotives.isEmpty()) {
                System.err.println("WARNING: No locomotives loaded from database");
            }
            if (wagons.isEmpty()) {
                System.err.println("WARNING: No wagons loaded from database");
            }
        } catch (Exception e) {
            System.err.println("Error loading data from database: " + e.getMessage());
            e.printStackTrace();
            locomotives = FXCollections.observableArrayList();
            wagons = FXCollections.observableArrayList();
        }

        trains = FXCollections.observableArrayList();
        trains.addListener((javafx.collections.ListChangeListener.Change<? extends Train> c) -> updateSummary());
    }
    
    /**
     * Shows the scheduler UI window.
     */
    public void show() {
        stage = new Stage();
        stage.setTitle("Train Dispatch Scheduler - USLP07/USLP10");

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #0D1117;");
        root.setPadding(new Insets(15)); // menos padding

        HBox topBox = createTopBox();
        root.setTop(topBox);

        mainTabPane = createMainTabPane();
        root.setCenter(mainTabPane);

        BorderPane.setMargin(mainTabPane, Insets.EMPTY);

        Scene scene = new Scene(root, 1400, 900);
        
        // Add global CSS to ensure all text is white
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        String globalStyle = 
            ".combo-box .list-cell { -fx-text-fill: white; } " +
            ".combo-box-popup .list-view .list-cell { -fx-text-fill: white; -fx-background-color: #0D1117; } " +
            ".combo-box-popup .list-view .list-cell:hover { -fx-background-color: #27272A; } " +
            ".combo-box-popup .list-view .list-cell:selected { -fx-background-color: #6366F1; } " +
            ".date-picker .text-field { -fx-text-fill: white; } " +
            ".list-view .list-cell { -fx-text-fill: white; -fx-background-color: #0D1117; } " +
            ".list-view .list-cell:selected { -fx-background-color: #6366F1; } " +
            ".list-view .list-cell:hover { -fx-background-color: #27272A; }";
        root.setStyle(root.getStyle() + globalStyle);
        
        stage.setScene(scene);
        stage.setMinWidth(1200);
        stage.setMinHeight(800);
        stage.show();
    }


    /**
     * Creates the top box with title and dispatch button.
     */
    private HBox createTopBox() {
        HBox topBox = new HBox(20);
        topBox.setAlignment(Pos.CENTER_LEFT);
        topBox.setPadding(new Insets(0, 0, 20, 0));
        
        Label titleLabel = new Label("Train Dispatch Scheduler");
        titleLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: white;");
        
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        
        dispatchButton = new Button("Dispatch Trains");
        dispatchButton.setStyle("-fx-background-color: #10B981; -fx-text-fill: white; -fx-font-size: 15px; " +
                               "-fx-font-weight: 600; -fx-padding: 12 24; -fx-background-radius: 8; " +
                               "-fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(16, 185, 129, 0.4), 8, 0, 0, 2);");
        dispatchButton.setOnAction(e -> dispatchTrains());
        
        progressIndicator = new ProgressIndicator();
        progressIndicator.setVisible(false);
        progressIndicator.setPrefSize(24, 24);
        
        statusLabel = new Label();
        statusLabel.setStyle("-fx-text-fill: white; -fx-font-size: 13px;");
        
        buttonBox.getChildren().addAll(dispatchButton, progressIndicator, statusLabel);
        
        HBox.setHgrow(titleLabel, Priority.ALWAYS);
        topBox.getChildren().addAll(titleLabel, buttonBox);
        
        return topBox;
    }
    
    /**
     * Creates the main tab pane.
     */
    private TabPane createMainTabPane() {
        TabPane tabPane = new TabPane();
        tabPane.setStyle("-fx-background-color: #161B22;");

        Tab trainSetupTab = new Tab("Train Setup");
        trainSetupTab.setClosable(false);
        trainSetupTab.setContent(createTrainSetupTab());

        Tab resultsTab = new Tab("Dispatch Results");
        resultsTab.setClosable(false);
        resultsTab.setContent(createResultsTab());

        tabPane.getTabs().addAll(trainSetupTab, resultsTab);

        VBox.setVgrow(tabPane, Priority.ALWAYS);

        return tabPane;
    }


    /**
     * Creates the train setup tab with two-column layout.
     */
    private ScrollPane createTrainSetupTab() {

        HBox mainBox = new HBox(20);
        mainBox.setPadding(new Insets(20));
        mainBox.setStyle("-fx-background-color: #0D1117;");

        // Left column
        VBox leftColumn = new VBox(15);
        leftColumn.setFillWidth(true);
        HBox.setHgrow(leftColumn, Priority.ALWAYS);

        VBox trainConfigBox = createCompactTrainConfigSection();
        VBox resourcesBox = createResourcesSection();
        VBox dispatchQueueBox = createDispatchQueueSection();

        leftColumn.getChildren().addAll(
                trainConfigBox,
                resourcesBox,
                dispatchQueueBox
        );

        // Right column (summary)
        summaryPanel = createSummaryPanel();
        summaryPanel.setPrefWidth(480);
        summaryPanel.setMinWidth(380);
        summaryPanel.setMaxWidth(Region.USE_PREF_SIZE);

        mainBox.getChildren().addAll(leftColumn, summaryPanel);

        ScrollPane scrollPane = new ScrollPane(mainBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setPannable(true);

        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        return scrollPane;
    }



    /**
     * Creates compact train configuration section with 2-column form.
     */
    private VBox createCompactTrainConfigSection() {
        VBox box = new VBox(12);
        box.setPadding(new Insets(20));
        box.setStyle("-fx-background-color: #161B22; -fx-background-radius: 12; " +
                    "-fx-border-color: rgba(99, 102, 241, 0.15); -fx-border-radius: 12; -fx-border-width: 1;");
        
        Label sectionLabel = new Label("Train Configuration");
        sectionLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: 600; -fx-text-fill: white;");
        
        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(12);
        grid.setPadding(new Insets(15, 0, 0, 0));
        
        // Column 1
        // Train ID
        Label trainIdLabel = new Label("Train ID:");
        trainIdLabel.setStyle("-fx-text-fill: white; -fx-font-size: 13px;");
        trainIdField = new TextField();
        trainIdField.setPrefWidth(250);
        trainIdField.setStyle("-fx-background-color: #0D1117; -fx-text-fill: white; -fx-border-color: #27272A; -fx-border-radius: 6;");
        grid.add(trainIdLabel, 0, 0);
        grid.add(trainIdField, 1, 0);
        
        // End Station
        Label endStationLabel = new Label("End Station:");
        endStationLabel.setStyle("-fx-text-fill: white; -fx-font-size: 13px;");
        endStationCombo = new ComboBox<>(facilities);
        endStationCombo.setConverter(new FacilityStringConverter());
        endStationCombo.setPrefWidth(250);
        endStationCombo.setStyle("-fx-background-color: #0D1117; -fx-text-fill: white; " +
                                 "-fx-control-inner-background: #0D1117;");
        endStationCombo.setCellFactory(list -> new ListCell<Facility>() {
            @Override
            protected void updateItem(Facility item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getId() + " - " + item.getName());
                    setStyle("-fx-text-fill: white; -fx-background-color: #0D1117;");
                }
            }
        });
        grid.add(endStationLabel, 0, 1);
        grid.add(endStationCombo, 1, 1);
        
        // Departure Time
        Label departureTimeLabel = new Label("Departure Time:");
        departureTimeLabel.setStyle("-fx-text-fill: white; -fx-font-size: 13px;");
        departureTimeField = new TextField();
        departureTimeField.setPrefWidth(250);
        departureTimeField.setPromptText("HH:mm");
        departureTimeField.setStyle("-fx-background-color: #0D1117; -fx-text-fill: white; -fx-border-color: #27272A; -fx-border-radius: 6;");
        grid.add(departureTimeLabel, 0, 2);
        grid.add(departureTimeField, 1, 2);
        
        // Column 2
        // Start Station
        Label startStationLabel = new Label("Start Station:");
        startStationLabel.setStyle("-fx-text-fill: white; -fx-font-size: 13px;");
        startStationCombo = new ComboBox<>(facilities);
        startStationCombo.setConverter(new FacilityStringConverter());
        startStationCombo.setPrefWidth(250);
        startStationCombo.setStyle("-fx-background-color: #0D1117; -fx-text-fill: white; " +
                                   "-fx-control-inner-background: #0D1117;");
        startStationCombo.setCellFactory(list -> new ListCell<Facility>() {
            @Override
            protected void updateItem(Facility item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getId() + " - " + item.getName());
                    setStyle("-fx-text-fill: white; -fx-background-color: #0D1117;");
                }
            }
        });
        grid.add(startStationLabel, 2, 0);
        grid.add(startStationCombo, 3, 0);
        
        // Departure Date
        Label departureDateLabel = new Label("Departure Date:");
        departureDateLabel.setStyle("-fx-text-fill: white; -fx-font-size: 13px;");
        departureDatePicker = new DatePicker();
        departureDatePicker.setPrefWidth(250);
        departureDatePicker.setStyle("-fx-background-color: #0D1117; -fx-text-fill: white;");
        grid.add(departureDateLabel, 2, 1);
        grid.add(departureDatePicker, 3, 1);
        
        // Cargo status
        wagonsLoadedCheckBox = new CheckBox("Cargo status: Loaded");
        wagonsLoadedCheckBox.setStyle("-fx-text-fill: white; -fx-font-size: 13px;");
        grid.add(wagonsLoadedCheckBox, 2, 2);
        
        // Option to specify intermediate stations
        specifyIntermediateStationsCheckBox = new CheckBox("Choose path manually (intermediate stations)");
        specifyIntermediateStationsCheckBox.setStyle("-fx-text-fill: white; -fx-font-size: 13px;");
        grid.add(specifyIntermediateStationsCheckBox, 0, 3, 2, 1);
        
        // Intermediate stations section (initially hidden)
        intermediateStationsBox = createIntermediateStationsSection();
        intermediateStationsBox.setVisible(false);
        intermediateStationsBox.setManaged(false);
        
        // Show/hide intermediate stations section based on checkbox
        specifyIntermediateStationsCheckBox.setOnAction(e -> {
            boolean show = specifyIntermediateStationsCheckBox.isSelected();
            intermediateStationsBox.setVisible(show);
            intermediateStationsBox.setManaged(show);
        });
        
        box.getChildren().addAll(sectionLabel, grid, intermediateStationsBox);
        
        return box;
    }
    
    /**
     * Creates the intermediate stations section for manual route specification.
     */
    private VBox createIntermediateStationsSection() {
        VBox box = new VBox(10);
        box.setPadding(new Insets(15, 0, 0, 0));
        box.setStyle("-fx-background-color: #0D1117; -fx-background-radius: 8; " +
                    "-fx-border-color: rgba(99, 102, 241, 0.2); -fx-border-radius: 8; -fx-border-width: 1; " +
                    "-fx-padding: 15;");
        
        Label sectionLabel = new Label("Intermediate Stations");
        sectionLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: 500;");
        
        // Instructions
        Label instructionsLabel = new Label("Add stations in order between start and end stations:");
        instructionsLabel.setStyle("-fx-text-fill: #94A3B8; -fx-font-size: 12px;");
        
        // Combo box for selecting intermediate station
        HBox addStationBox = new HBox(10);
        intermediateStationCombo = new ComboBox<>(facilities);
        intermediateStationCombo.setConverter(new FacilityStringConverter());
        intermediateStationCombo.setPrefWidth(300);
        intermediateStationCombo.setStyle("-fx-background-color: #161B22; -fx-text-fill: white;");
        intermediateStationCombo.setPromptText("Select station");
        
        addIntermediateStationButton = new Button("Add");
        addIntermediateStationButton.setStyle("-fx-background-color: #6366F1; -fx-text-fill: white; -fx-font-size: 12px; " +
                                           "-fx-padding: 6 12; -fx-background-radius: 6; -fx-cursor: hand;");
        addIntermediateStationButton.setOnAction(e -> addIntermediateStation());
        
        addStationBox.getChildren().addAll(intermediateStationCombo, addIntermediateStationButton);
        
        // List of intermediate stations
        intermediateStationsList = new ListView<>(FXCollections.observableArrayList());
        intermediateStationsList.setPrefHeight(120);
        intermediateStationsList.setStyle("-fx-background-color: #161B22; -fx-control-inner-background: #161B22;");
        intermediateStationsList.setCellFactory(list -> new ListCell<Facility>() {
            @Override
            protected void updateItem(Facility item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                    setGraphic(null);
                } else {
                    setText(item.getName() + " (ID: " + item.getId() + ")");
                    setStyle("-fx-text-fill: white; -fx-font-size: 12px; " +
                            "-fx-background-color: #161B22; " +
                            "-fx-padding: 8 10; " +
                            "-fx-alignment: CENTER-LEFT;");
                }
            }
        });
        
        // Remove button
        removeIntermediateStationButton = new Button("Remove Selected");
        removeIntermediateStationButton.setStyle("-fx-background-color: #EF4444; -fx-text-fill: white; -fx-font-size: 12px; " +
                                               "-fx-padding: 6 12; -fx-background-radius: 6; -fx-cursor: hand;");
        removeIntermediateStationButton.setOnAction(e -> removeIntermediateStation());
        
        box.getChildren().addAll(sectionLabel, instructionsLabel, addStationBox, intermediateStationsList, removeIntermediateStationButton);
        
        return box;
    }
    
    /**
     * Adds an intermediate station to the route.
     */
    private void addIntermediateStation() {
        Facility selected = intermediateStationCombo.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Warning", "Please select a station to add.");
            return;
        }
        
        // Validate that the station is not the start or end station
        Facility startStation = startStationCombo.getSelectionModel().getSelectedItem();
        Facility endStation = endStationCombo.getSelectionModel().getSelectedItem();
        
        if (startStation != null && selected.equals(startStation)) {
            showAlert(Alert.AlertType.WARNING, "Warning", "This station is already selected as the start station.");
            return;
        }
        
        if (endStation != null && selected.equals(endStation)) {
            showAlert(Alert.AlertType.WARNING, "Warning", "This station is already selected as the end station.");
            return;
        }
        
        ObservableList<Facility> intermediateList = intermediateStationsList.getItems();
        
        // Check if station is already in list (by ID comparison, not object reference)
        boolean alreadyExists = intermediateList.stream()
            .anyMatch(station -> station.getId().equals(selected.getId()));
        
        if (!alreadyExists) {
            intermediateList.add(selected);
            intermediateStationCombo.getSelectionModel().clearSelection();
            System.out.println("Added intermediate station: " + selected.getName() + " (ID: " + selected.getId() + ")");
            System.out.println("Total intermediate stations: " + intermediateList.size());
            // Force refresh of the list view
            intermediateStationsList.refresh();
        } else {
            showAlert(Alert.AlertType.WARNING, "Warning", "This station is already in the intermediate stations list.");
        }
    }
    
    /**
     * Removes the selected intermediate station from the route.
     */
    private void removeIntermediateStation() {
        Facility selected = intermediateStationsList.getSelectionModel().getSelectedItem();
        if (selected != null) {
            intermediateStationsList.getItems().remove(selected);
            System.out.println("Removed intermediate station: " + selected.getName() + " (ID: " + selected.getId() + ")");
        } else {
            showAlert(Alert.AlertType.WARNING, "Warning", "Please select a station to remove.");
        }
    }
    
    /**
     * Creates the resources section (locomotives and wagons).
     */
    private VBox createResourcesSection() {
        VBox box = new VBox(15);
        
        // Locomotives section
        VBox locomotivesBox = createLocomotivesSection();
        
        // Wagons section
        VBox wagonsBox = createWagonsSection();
        
        box.getChildren().addAll(locomotivesBox, wagonsBox);
        VBox.setVgrow(locomotivesBox, Priority.ALWAYS);
        VBox.setVgrow(wagonsBox, Priority.ALWAYS);
        
        return box;
    }
    
    /**
     * Creates the locomotives section with professional formatting.
     */
    private VBox createLocomotivesSection() {
        VBox box = new VBox(15);
        box.setPadding(new Insets(20));
        box.setStyle("-fx-background-color: #161B22; -fx-background-radius: 12; " +
                    "-fx-border-color: rgba(99, 102, 241, 0.15); -fx-border-radius: 12; -fx-border-width: 1;");
        
        Label sectionLabel = new Label("Locomotives");
        sectionLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: 700; -fx-text-fill: #FFFFFF; " +
                             "-fx-font-family: 'Segoe UI', 'Roboto', sans-serif;");
        
        Separator separator = new Separator();
        separator.setStyle("-fx-background-color: rgba(99, 102, 241, 0.3); -fx-pref-height: 1;");
        
        HBox tablesBox = new HBox(15);
        
        // Available locomotives table
        VBox availableBox = new VBox(8);
        Label availableLabel = new Label("Available");
        availableLabel.setStyle("-fx-text-fill: #E2E8F0; -fx-font-size: 13px; -fx-font-weight: 600; " +
                               "-fx-font-family: 'Segoe UI', 'Roboto', sans-serif;");
        availableLocomotivesTable = createLocomotiveTable();
        availableLocomotivesTable.setItems(locomotives);
        availableLocomotivesTable.setPrefHeight(200);
        availableLocomotivesTable.setPrefWidth(900);
        availableBox.getChildren().addAll(availableLabel, availableLocomotivesTable);
        VBox.setVgrow(availableLocomotivesTable, Priority.ALWAYS);
        
        // Buttons
        VBox buttonsBox = new VBox(12);
        buttonsBox.setAlignment(Pos.CENTER);
        buttonsBox.setPadding(new Insets(0, 5, 0, 5));
        addLocomotiveButton = new Button("→");
        addLocomotiveButton.setStyle("-fx-background-color: #10B981; -fx-text-fill: white; " +
                                   "-fx-font-size: 16px; -fx-font-weight: bold; " +
                                   "-fx-min-width: 45; -fx-pref-height: 45; " +
                                   "-fx-background-radius: 8; -fx-cursor: hand; " +
                                   "-fx-effect: dropshadow(gaussian, rgba(16, 185, 129, 0.4), 6, 0, 0, 2);");
        addLocomotiveButton.setOnAction(e -> addLocomotive());
        addLocomotiveButton.setOnMouseEntered(e -> addLocomotiveButton.setStyle(
            "-fx-background-color: #059669; -fx-text-fill: white; " +
            "-fx-font-size: 16px; -fx-font-weight: bold; " +
            "-fx-min-width: 45; -fx-pref-height: 45; " +
            "-fx-background-radius: 8; -fx-cursor: hand; " +
            "-fx-effect: dropshadow(gaussian, rgba(16, 185, 129, 0.6), 8, 0, 0, 3);"));
        addLocomotiveButton.setOnMouseExited(e -> addLocomotiveButton.setStyle(
            "-fx-background-color: #10B981; -fx-text-fill: white; " +
            "-fx-font-size: 16px; -fx-font-weight: bold; " +
            "-fx-min-width: 45; -fx-pref-height: 45; " +
            "-fx-background-radius: 8; -fx-cursor: hand; " +
            "-fx-effect: dropshadow(gaussian, rgba(16, 185, 129, 0.4), 6, 0, 0, 2);"));
        removeLocomotiveButton = new Button("←");
        removeLocomotiveButton.setStyle("-fx-background-color: #EF4444; -fx-text-fill: white; " +
                                      "-fx-font-size: 16px; -fx-font-weight: bold; " +
                                      "-fx-min-width: 45; -fx-pref-height: 45; " +
                                      "-fx-background-radius: 8; -fx-cursor: hand; " +
                                      "-fx-effect: dropshadow(gaussian, rgba(239, 68, 68, 0.4), 6, 0, 0, 2);");
        removeLocomotiveButton.setOnAction(e -> removeLocomotive());
        removeLocomotiveButton.setOnMouseEntered(e -> removeLocomotiveButton.setStyle(
            "-fx-background-color: #DC2626; -fx-text-fill: white; " +
            "-fx-font-size: 16px; -fx-font-weight: bold; " +
            "-fx-min-width: 45; -fx-pref-height: 45; " +
            "-fx-background-radius: 8; -fx-cursor: hand; " +
            "-fx-effect: dropshadow(gaussian, rgba(239, 68, 68, 0.6), 8, 0, 0, 3);"));
        removeLocomotiveButton.setOnMouseExited(e -> removeLocomotiveButton.setStyle(
            "-fx-background-color: #EF4444; -fx-text-fill: white; " +
            "-fx-font-size: 16px; -fx-font-weight: bold; " +
            "-fx-min-width: 45; -fx-pref-height: 45; " +
            "-fx-background-radius: 8; -fx-cursor: hand; " +
            "-fx-effect: dropshadow(gaussian, rgba(239, 68, 68, 0.4), 6, 0, 0, 2);"));
        buttonsBox.getChildren().addAll(addLocomotiveButton, removeLocomotiveButton);
        
        // Selected locomotives table
        VBox selectedBox = new VBox(8);
        Label selectedLabel = new Label("Selected");
        selectedLabel.setStyle("-fx-text-fill: #E2E8F0; -fx-font-size: 8px; -fx-font-weight: 600; " +
                             "-fx-font-family: 'Segoe UI', 'Roboto', sans-serif;");
        selectedLocomotivesTable = createLocomotiveTable();
        selectedLocomotivesTable.getItems().addListener((javafx.collections.ListChangeListener.Change<? extends Locomotive> c) -> updateSummary());
        selectedLocomotivesTable.setPrefHeight(150);
        selectedLocomotivesTable.setPrefWidth(900);
        selectedBox.getChildren().addAll(selectedLabel, selectedLocomotivesTable);
        VBox.setVgrow(selectedLocomotivesTable, Priority.ALWAYS);
        
        tablesBox.getChildren().addAll(availableBox, buttonsBox, selectedBox);
        HBox.setHgrow(availableBox, Priority.ALWAYS);
        HBox.setHgrow(selectedBox, Priority.ALWAYS);
        
        box.getChildren().addAll(sectionLabel, separator, tablesBox);
        
        return box;
    }
    
    /**
     * Creates a modern TableView for locomotives with dark theme styling.
     */
    private TableView<Locomotive> createLocomotiveTable() {
        TableView<Locomotive> table = new TableView<>();
        table.setStyle("-fx-background-color: #1E293B; " +
                      "-fx-background-radius: 8; " +
                      "-fx-border-color: rgba(99, 102, 241, 0.2); " +
                      "-fx-border-radius: 8; " +
                      "-fx-border-width: 1; " +
                      "-fx-table-header-border-color: transparent; " +
                      "-fx-table-cell-border-color: transparent;");
        // Ajuste automático de colunas - usar FLEX_LAST_COLUMN para melhor distribuição
        table.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        table.setFixedCellSize(35); // Altura menor para ver mais linhas
        
        // Aplicar estilos aos headers após a tabela ser renderizada
        table.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                Platform.runLater(() -> {
                    applyTableHeaderStyles(table);
                });
            }
        });
        
        // Também aplicar imediatamente se já estiver na cena
        if (table.getScene() != null) {
            Platform.runLater(() -> {
                applyTableHeaderStyles(table);
            });
        }
        
        // ID Column
        TableColumn<Locomotive, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(loco -> new javafx.beans.property.SimpleStringProperty(loco.getValue().getId()));
        idCol.setMinWidth(70);
        idCol.setPrefWidth(80);
        idCol.setMaxWidth(100);
        idCol.setSortable(true);
        idCol.setResizable(true);
        idCol.setStyle("-fx-font-family: 'Segoe UI', 'Roboto', sans-serif; " +
                      "-fx-font-size: 11px; " +
                      "-fx-font-weight: 700; " +
                      "-fx-text-fill: #FFFFFF; " +
                      "-fx-background-color: #2D3748; " +
                      "-fx-padding: 8 10; " +
                      "-fx-alignment: CENTER;");
        idCol.setCellFactory(col -> new TableCell<Locomotive, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    setStyle("-fx-font-family: 'Segoe UI', 'Roboto', sans-serif; " +
                            "-fx-font-size: 11px; " +
                            "-fx-font-weight: 700; " +
                            "-fx-text-fill: #60A5FA; " +
                            "-fx-padding: 8 10; " +
                            "-fx-alignment: CENTER;");
                }
            }
        });
        
        // Type Column - texto completo (Diesel/Electric)
        TableColumn<Locomotive, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(loco -> {
            String type = loco.getValue().getType();
            // Garantir texto completo
            if (type != null) {
                type = type.substring(0, 1).toUpperCase() + type.substring(1).toLowerCase();
            }
            return new javafx.beans.property.SimpleStringProperty(type);
        });
        typeCol.setMinWidth(100);
        typeCol.setPrefWidth(120);
        typeCol.setMaxWidth(150);
        typeCol.setSortable(true);
        typeCol.setResizable(true);
        typeCol.setStyle("-fx-font-family: 'Segoe UI', 'Roboto', sans-serif; " +
                        "-fx-font-size: 11px; " +
                        "-fx-font-weight: 700; " +
                        "-fx-text-fill: #FFFFFF; " +
                        "-fx-background-color: #2D3748; " +
                        "-fx-padding: 8 10; " +
                        "-fx-alignment: CENTER;");
        typeCol.setCellFactory(col -> new TableCell<Locomotive, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item); // Texto completo sempre visível
                    String color = "Electric".equalsIgnoreCase(item) ? "#A78BFA" : "#F59E0B";
                    setStyle("-fx-font-family: 'Segoe UI', 'Roboto', sans-serif; " +
                            "-fx-font-size: 11px; " +
                            "-fx-font-weight: 600; " +
                            "-fx-text-fill: " + color + "; " +
                            "-fx-padding: 8 10; " +
                            "-fx-alignment: CENTER;");
                }
            }
        });
        
        // Power Column
        TableColumn<Locomotive, Double> powerCol = new TableColumn<>("Power (kW)");
        powerCol.setCellValueFactory(loco -> new javafx.beans.property.SimpleDoubleProperty(loco.getValue().getPowerKw()).asObject());
        powerCol.setMinWidth(110);
        powerCol.setPrefWidth(130);
        powerCol.setMaxWidth(150);
        powerCol.setSortable(true);
        powerCol.setResizable(true);
        powerCol.setStyle("-fx-font-family: 'Segoe UI', 'Roboto', sans-serif; " +
                         "-fx-font-size: 11px; " +
                         "-fx-font-weight: 700; " +
                         "-fx-text-fill: #FFFFFF; " +
                         "-fx-background-color: #2D3748; " +
                         "-fx-padding: 8 10; " +
                         "-fx-alignment: CENTER;");
        powerCol.setCellFactory(col -> new TableCell<Locomotive, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(String.format("%.0f kW", item)); // Texto completo com unidade
                    setStyle("-fx-font-family: 'Segoe UI', 'Roboto', sans-serif; " +
                            "-fx-font-size: 11px; " +
                            "-fx-text-fill: #E2E8F0; " +
                            "-fx-padding: 8 10; " +
                            "-fx-alignment: CENTER;");
                }
            }
        });
        
        // Max Speed Column
        TableColumn<Locomotive, Double> speedCol = new TableColumn<>("Max Speed (km/h)");
        speedCol.setCellValueFactory(loco -> new javafx.beans.property.SimpleDoubleProperty(loco.getValue().getMaxSpeedKmH()).asObject());
        speedCol.setMinWidth(140);
        speedCol.setPrefWidth(160);
        speedCol.setMaxWidth(180);
        speedCol.setSortable(true);
        speedCol.setResizable(true);
        speedCol.setStyle("-fx-font-family: 'Segoe UI', 'Roboto', sans-serif; " +
                         "-fx-font-size: 11px; " +
                         "-fx-font-weight: 700; " +
                         "-fx-text-fill: #FFFFFF; " +
                         "-fx-background-color: #2D3748; " +
                         "-fx-padding: 8 10; " +
                         "-fx-alignment: CENTER;");
        speedCol.setCellFactory(col -> new TableCell<Locomotive, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(String.format("%.0f km/h", item)); // Texto completo com unidade
                    setStyle("-fx-font-family: 'Segoe UI', 'Roboto', sans-serif; " +
                            "-fx-font-size: 11px; " +
                            "-fx-text-fill: #E2E8F0; " +
                            "-fx-padding: 8 10; " +
                            "-fx-alignment: CENTER;");
                }
            }
        });
        
        table.getColumns().add(idCol);
        table.getColumns().add(typeCol);
        table.getColumns().add(powerCol);
        table.getColumns().add(speedCol);
        
        // Ajustar larguras das colunas automaticamente após carregar dados
        Platform.runLater(() -> {
            table.getItems().addListener((javafx.collections.ListChangeListener.Change<? extends Locomotive> c) -> {
                Platform.runLater(() -> {
                    idCol.setPrefWidth(Math.max(70, idCol.getPrefWidth()));
                    typeCol.setPrefWidth(Math.max(90, typeCol.getPrefWidth()));
                    powerCol.setPrefWidth(Math.max(100, powerCol.getPrefWidth()));
                    speedCol.setPrefWidth(Math.max(120, speedCol.getPrefWidth()));
                });
            });
        });
        
        // Row styling with hover effects
        table.setRowFactory(tv -> {
            TableRow<Locomotive> row = new TableRow<Locomotive>() {
                @Override
                protected void updateItem(Locomotive item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setStyle("");
                    } else {
                        int index = getIndex();
                        String bgColor = (index % 2 == 0) ? "#1E293B" : "#243447";
                        setStyle("-fx-background-color: " + bgColor + "; " +
                                "-fx-border-color: transparent transparent rgba(99, 102, 241, 0.1) transparent; " +
                                "-fx-border-width: 0 0 1 0;");
                    }
                }
            };
            
            row.setOnMouseEntered(e -> {
                if (!row.isEmpty()) {
                    row.setStyle("-fx-background-color: #334155; " +
                                "-fx-border-color: transparent transparent rgba(99, 102, 241, 0.3) transparent; " +
                                "-fx-border-width: 0 0 1 0; " +
                                "-fx-cursor: hand;");
                }
            });
            
            row.setOnMouseExited(e -> {
                if (!row.isEmpty()) {
                    int index = row.getIndex();
                    String bgColor = (index % 2 == 0) ? "#1E293B" : "#243447";
                    row.setStyle("-fx-background-color: " + bgColor + "; " +
                                "-fx-border-color: transparent transparent rgba(99, 102, 241, 0.1) transparent; " +
                                "-fx-border-width: 0 0 1 0;");
                }
            });
            
            return row;
        });
        
        return table;
    }
    
    /**
     * Creates the wagons section with professional formatting.
     */
    private VBox createWagonsSection() {
        VBox box = new VBox(15);
        box.setPadding(new Insets(20));
        box.setStyle("-fx-background-color: #161B22; -fx-background-radius: 12; " +
                    "-fx-border-color: rgba(99, 102, 241, 0.15); -fx-border-radius: 12; -fx-border-width: 1;");
        
        Label sectionLabel = new Label("Wagons");
        sectionLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: 700; -fx-text-fill: #FFFFFF; " +
                            "-fx-font-family: 'Segoe UI', 'Roboto', sans-serif;");
        
        Separator separator = new Separator();
        separator.setStyle("-fx-background-color: rgba(99, 102, 241, 0.3); -fx-pref-height: 1;");
        
        HBox tablesBox = new HBox(15);
        
        // Available wagons table
        VBox availableBox = new VBox(8);
        Label availableLabel = new Label("Available");
        availableLabel.setStyle("-fx-text-fill: #E2E8F0; -fx-font-size: 13px; -fx-font-weight: 600; " +
                              "-fx-font-family: 'Segoe UI', 'Roboto', sans-serif;");
        availableWagonsTable = createWagonTable();
        availableWagonsTable.setItems(wagons);
        availableWagonsTable.setPrefHeight(200);
        availableWagonsTable.setPrefWidth(900);
        availableBox.getChildren().addAll(availableLabel, availableWagonsTable);
        VBox.setVgrow(availableWagonsTable, Priority.ALWAYS);
        
        // Buttons
        VBox buttonsBox = new VBox(12);
        buttonsBox.setAlignment(Pos.CENTER);
        buttonsBox.setPadding(new Insets(0, 5, 0, 5));
        addWagonButton = new Button("→");
        addWagonButton.setStyle("-fx-background-color: #10B981; -fx-text-fill: white; " +
                              "-fx-font-size: 16px; -fx-font-weight: bold; " +
                              "-fx-min-width: 45; -fx-pref-height: 45; " +
                              "-fx-background-radius: 8; -fx-cursor: hand; " +
                              "-fx-effect: dropshadow(gaussian, rgba(16, 185, 129, 0.4), 6, 0, 0, 2);");
        addWagonButton.setOnAction(e -> addWagon());
        addWagonButton.setOnMouseEntered(e -> addWagonButton.setStyle(
            "-fx-background-color: #059669; -fx-text-fill: white; " +
            "-fx-font-size: 16px; -fx-font-weight: bold; " +
            "-fx-min-width: 45; -fx-pref-height: 45; " +
            "-fx-background-radius: 8; -fx-cursor: hand; " +
            "-fx-effect: dropshadow(gaussian, rgba(16, 185, 129, 0.6), 8, 0, 0, 3);"));
        addWagonButton.setOnMouseExited(e -> addWagonButton.setStyle(
            "-fx-background-color: #10B981; -fx-text-fill: white; " +
            "-fx-font-size: 16px; -fx-font-weight: bold; " +
            "-fx-min-width: 45; -fx-pref-height: 45; " +
            "-fx-background-radius: 8; -fx-cursor: hand; " +
            "-fx-effect: dropshadow(gaussian, rgba(16, 185, 129, 0.4), 6, 0, 0, 2);"));
        removeWagonButton = new Button("←");
        removeWagonButton.setStyle("-fx-background-color: #EF4444; -fx-text-fill: white; " +
                                 "-fx-font-size: 16px; -fx-font-weight: bold; " +
                                 "-fx-min-width: 45; -fx-pref-height: 45; " +
                                 "-fx-background-radius: 8; -fx-cursor: hand; " +
                                 "-fx-effect: dropshadow(gaussian, rgba(239, 68, 68, 0.4), 6, 0, 0, 2);");
        removeWagonButton.setOnAction(e -> removeWagon());
        removeWagonButton.setOnMouseEntered(e -> removeWagonButton.setStyle(
            "-fx-background-color: #DC2626; -fx-text-fill: white; " +
            "-fx-font-size: 16px; -fx-font-weight: bold; " +
            "-fx-min-width: 45; -fx-pref-height: 45; " +
            "-fx-background-radius: 8; -fx-cursor: hand; " +
            "-fx-effect: dropshadow(gaussian, rgba(239, 68, 68, 0.6), 8, 0, 0, 3);"));
        removeWagonButton.setOnMouseExited(e -> removeWagonButton.setStyle(
            "-fx-background-color: #EF4444; -fx-text-fill: white; " +
            "-fx-font-size: 16px; -fx-font-weight: bold; " +
            "-fx-min-width: 45; -fx-pref-height: 45; " +
            "-fx-background-radius: 8; -fx-cursor: hand; " +
            "-fx-effect: dropshadow(gaussian, rgba(239, 68, 68, 0.4), 6, 0, 0, 2);"));
        buttonsBox.getChildren().addAll(addWagonButton, removeWagonButton);
        
        // Selected wagons table
        VBox selectedBox = new VBox(8);
        Label selectedLabel = new Label("Selected");
        selectedLabel.setStyle("-fx-text-fill: #E2E8F0; -fx-font-size: 13px; -fx-font-weight: 600; " +
                             "-fx-font-family: 'Segoe UI', 'Roboto', sans-serif;");
        selectedWagonsTable = createWagonTable();
        selectedWagonsTable.getItems().addListener((javafx.collections.ListChangeListener.Change<? extends Wagon> c) -> updateSummary());
        selectedWagonsTable.setPrefHeight(150);
        selectedWagonsTable.setPrefWidth(900);
        selectedBox.getChildren().addAll(selectedLabel, selectedWagonsTable);
        VBox.setVgrow(selectedWagonsTable, Priority.ALWAYS);
        
        tablesBox.getChildren().addAll(availableBox, buttonsBox, selectedBox);
        HBox.setHgrow(availableBox, Priority.ALWAYS);
        HBox.setHgrow(selectedBox, Priority.ALWAYS);
        
        box.getChildren().addAll(sectionLabel, separator, tablesBox);
        
        return box;
    }
    
    /**
     * Creates a modern TableView for wagons with dark theme styling.
     */
    private TableView<Wagon> createWagonTable() {
        TableView<Wagon> table = new TableView<>();
        table.setStyle("-fx-background-color: #1E293B; " +
                      "-fx-background-radius: 8; " +
                      "-fx-border-color: rgba(99, 102, 241, 0.2); " +
                      "-fx-border-radius: 8; " +
                      "-fx-border-width: 1; " +
                      "-fx-table-header-border-color: transparent; " +
                      "-fx-table-cell-border-color: transparent;");
        // Ajuste automático de colunas - usar FLEX_LAST_COLUMN para melhor distribuição
        table.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        table.setFixedCellSize(35); // Altura menor para ver mais linhas
        
        // Aplicar estilos aos headers após a tabela ser renderizada
        table.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                Platform.runLater(() -> {
                    applyTableHeaderStyles(table);
                });
            }
        });
        
        // Também aplicar imediatamente se já estiver na cena
        if (table.getScene() != null) {
            Platform.runLater(() -> {
                applyTableHeaderStyles(table);
            });
        }
        
        // ID Column
        TableColumn<Wagon, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(wagon -> new javafx.beans.property.SimpleStringProperty(wagon.getValue().getWagonId()));
        idCol.setMinWidth(70);
        idCol.setPrefWidth(80);
        idCol.setMaxWidth(100);
        idCol.setSortable(true);
        idCol.setResizable(true);
        idCol.setStyle("-fx-font-family: 'Segoe UI', 'Roboto', sans-serif; " +
                      "-fx-font-size: 11px; " +
                      "-fx-font-weight: 700; " +
                      "-fx-text-fill: #FFFFFF; " +
                      "-fx-background-color: #2D3748; " +
                      "-fx-padding: 8 10; " +
                      "-fx-alignment: CENTER;");
        idCol.setCellFactory(col -> new TableCell<Wagon, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    setStyle("-fx-font-family: 'Segoe UI', 'Roboto', sans-serif; " +
                            "-fx-font-size: 11px; " +
                            "-fx-font-weight: 700; " +
                            "-fx-text-fill: #60A5FA; " +
                            "-fx-padding: 8 10; " +
                            "-fx-alignment: CENTER;");
                }
            }
        });
        
        // Type Column - texto completo sempre visível
        TableColumn<Wagon, String> typeCol = new TableColumn<>("Cargo Type");
        typeCol.setCellValueFactory(wagon -> new javafx.beans.property.SimpleStringProperty(wagon.getValue().getType()));
        typeCol.setMinWidth(140);
        typeCol.setPrefWidth(180);
        typeCol.setMaxWidth(220);
        typeCol.setSortable(true);
        typeCol.setResizable(true);
        typeCol.setStyle("-fx-font-family: 'Segoe UI', 'Roboto', sans-serif; " +
                        "-fx-font-size: 11px; " +
                        "-fx-font-weight: 700; " +
                        "-fx-text-fill: #FFFFFF; " +
                        "-fx-background-color: #2D3748; " +
                        "-fx-padding: 8 10; " +
                        "-fx-alignment: CENTER;");
        typeCol.setCellFactory(col -> new TableCell<Wagon, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item); // Texto completo sempre visível
                    setStyle("-fx-font-family: 'Segoe UI', 'Roboto', sans-serif; " +
                            "-fx-font-size: 11px; " +
                            "-fx-font-weight: 500; " +
                            "-fx-text-fill: #E2E8F0; " +
                            "-fx-padding: 8 10; " +
                            "-fx-alignment: CENTER;");
                }
            }
        });
        
        // Empty Weight Column
        TableColumn<Wagon, Double> emptyWeightCol = new TableColumn<>("Empty (kg)");
        emptyWeightCol.setCellValueFactory(wagon -> new javafx.beans.property.SimpleDoubleProperty(wagon.getValue().getEmptyWeightKg()).asObject());
        emptyWeightCol.setMinWidth(110);
        emptyWeightCol.setPrefWidth(130);
        emptyWeightCol.setMaxWidth(150);
        emptyWeightCol.setSortable(true);
        emptyWeightCol.setResizable(true);
        emptyWeightCol.setStyle("-fx-font-family: 'Segoe UI', 'Roboto', sans-serif; " +
                               "-fx-font-size: 11px; " +
                               "-fx-font-weight: 700; " +
                               "-fx-text-fill: #FFFFFF; " +
                               "-fx-background-color: #2D3748; " +
                               "-fx-padding: 8 10; " +
                               "-fx-alignment: CENTER;");
        emptyWeightCol.setCellFactory(col -> new TableCell<Wagon, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(String.format("%.0f kg", item)); // Texto completo com unidade
                    setStyle("-fx-font-family: 'Segoe UI', 'Roboto', sans-serif; " +
                            "-fx-font-size: 11px; " +
                            "-fx-text-fill: #E2E8F0; " +
                            "-fx-padding: 8 10; " +
                            "-fx-alignment: CENTER;");
                }
            }
        });
        
        // Loaded Weight Column
        TableColumn<Wagon, Double> loadedWeightCol = new TableColumn<>("Loaded (kg)");
        loadedWeightCol.setCellValueFactory(wagon -> new javafx.beans.property.SimpleDoubleProperty(wagon.getValue().getFullyLoadedWeightKg()).asObject());
        loadedWeightCol.setMinWidth(110);
        loadedWeightCol.setPrefWidth(130);
        loadedWeightCol.setMaxWidth(150);
        loadedWeightCol.setSortable(true);
        loadedWeightCol.setResizable(true);
        loadedWeightCol.setStyle("-fx-font-family: 'Segoe UI', 'Roboto', sans-serif; " +
                                 "-fx-font-size: 11px; " +
                                 "-fx-font-weight: 700; " +
                                 "-fx-text-fill: #FFFFFF; " +
                                 "-fx-background-color: #2D3748; " +
                                 "-fx-padding: 8 10; " +
                                 "-fx-alignment: CENTER;");
        loadedWeightCol.setCellFactory(col -> new TableCell<Wagon, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(String.format("%.0f kg", item)); // Texto completo com unidade
                    setStyle("-fx-font-family: 'Segoe UI', 'Roboto', sans-serif; " +
                            "-fx-font-size: 11px; " +
                            "-fx-text-fill: #E2E8F0; " +
                            "-fx-padding: 8 10; " +
                            "-fx-alignment: CENTER;");
                }
            }
        });
        
        table.getColumns().add(idCol);
        table.getColumns().add(typeCol);
        table.getColumns().add(emptyWeightCol);
        table.getColumns().add(loadedWeightCol);
        
        // Ajustar larguras das colunas automaticamente após carregar dados
        Platform.runLater(() -> {
            table.getItems().addListener((javafx.collections.ListChangeListener.Change<? extends Wagon> c) -> {
                Platform.runLater(() -> {
                    idCol.setPrefWidth(Math.max(70, idCol.getPrefWidth()));
                    typeCol.setPrefWidth(Math.max(150, typeCol.getPrefWidth()));
                    emptyWeightCol.setPrefWidth(Math.max(100, emptyWeightCol.getPrefWidth()));
                    loadedWeightCol.setPrefWidth(Math.max(100, loadedWeightCol.getPrefWidth()));
                });
            });
        });
        
        // Row styling with hover effects
        table.setRowFactory(tv -> {
            TableRow<Wagon> row = new TableRow<Wagon>() {
                @Override
                protected void updateItem(Wagon item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setStyle("");
                    } else {
                        int index = getIndex();
                        String bgColor = (index % 2 == 0) ? "#1E293B" : "#243447";
                        setStyle("-fx-background-color: " + bgColor + "; " +
                                "-fx-border-color: transparent transparent rgba(99, 102, 241, 0.1) transparent; " +
                                "-fx-border-width: 0 0 1 0;");
                    }
                }
            };
            
            row.setOnMouseEntered(e -> {
                if (!row.isEmpty()) {
                    row.setStyle("-fx-background-color: #334155; " +
                                "-fx-border-color: transparent transparent rgba(99, 102, 241, 0.3) transparent; " +
                                "-fx-border-width: 0 0 1 0; " +
                                "-fx-cursor: hand;");
                }
            });
            
            row.setOnMouseExited(e -> {
                if (!row.isEmpty()) {
                    int index = row.getIndex();
                    String bgColor = (index % 2 == 0) ? "#1E293B" : "#243447";
                    row.setStyle("-fx-background-color: " + bgColor + "; " +
                                "-fx-border-color: transparent transparent rgba(99, 102, 241, 0.1) transparent; " +
                                "-fx-border-width: 0 0 1 0;");
                }
            });
            
            return row;
        });
        
        return table;
    }
    
    /**
     * Creates the dispatch queue section.
     */
    private VBox createDispatchQueueSection() {
        VBox box = new VBox(10);
        box.setPadding(new Insets(20));
        box.setStyle("-fx-background-color: #161B22; -fx-background-radius: 12; " +
                    "-fx-border-color: rgba(99, 102, 241, 0.15); -fx-border-radius: 12; -fx-border-width: 1;");
        
        HBox headerBox = new HBox(10);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        
        Label sectionLabel = new Label("Dispatch Queue");
        sectionLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: 600; -fx-text-fill: white;");
        
        addTrainButton = new Button("Add Train");
        addTrainButton.setStyle("-fx-background-color: #6366F1; -fx-text-fill: white; -fx-font-size: 13px; " +
                              "-fx-padding: 8 16; -fx-background-radius: 6; -fx-cursor: hand;");
        addTrainButton.setOnAction(e -> {
            System.out.println("Add Train button action triggered");
            try {
                addTrain();
            } catch (Exception ex) {
                System.err.println("Error in addTrain button handler: " + ex.getMessage());
                ex.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Error", 
                    "An unexpected error occurred: " + ex.getMessage());
            }
        });
        
        removeTrainButton = new Button("Remove Selected");
        removeTrainButton.setStyle("-fx-background-color: #EF4444; -fx-text-fill: white; -fx-font-size: 13px; " +
                                 "-fx-padding: 8 16; -fx-background-radius: 6; -fx-cursor: hand;");
        removeTrainButton.setOnAction(e -> removeTrain());
        
        HBox.setHgrow(sectionLabel, Priority.ALWAYS);
        headerBox.getChildren().addAll(sectionLabel, addTrainButton, removeTrainButton);
        
        trainsList = new ListView<>(trains);
        trainsList.setCellFactory(list -> new TrainListCell());
        trainsList.setPrefHeight(150);
        trainsList.setStyle("-fx-background-color: #0D1117;");
        
        box.getChildren().addAll(headerBox, trainsList);
        
        return box;
    }
    
    /**
     * Creates the real-time summary panel (right column).
     */
    private VBox createSummaryPanel() {
        VBox box = new VBox(15);
        box.setPadding(new Insets(20));
        box.setStyle("-fx-background-color: #161B22; -fx-background-radius: 12; " +
                    "-fx-border-color: rgba(16, 185, 129, 0.2); -fx-border-radius: 12; -fx-border-width: 1;");
        
        summaryTitle = new Label("Train Summary");
        summaryTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: 600; -fx-text-fill: white;");
        
        Separator separator = new Separator();
        separator.setStyle("-fx-background-color: rgba(16, 185, 129, 0.3);");
        
        summaryContent = new VBox(12);
        summaryContent.setPadding(new Insets(10, 0, 0, 0));
        
        updateSummary();
        
        box.getChildren().addAll(summaryTitle, separator, summaryContent);
        
        return box;
    }
    
    /**
     * Updates the real-time train summary panel.
     */
    private void updateSummary() {
        Platform.runLater(() -> {
            summaryContent.getChildren().clear();
            
            if (trainIdField.getText().trim().isEmpty() && 
                selectedLocomotivesTable.getItems().isEmpty() && 
                selectedWagonsTable.getItems().isEmpty()) {
                Label emptyLabel = new Label("Configure a train to see summary");
                emptyLabel.setStyle("-fx-text-fill: white; -fx-font-size: 13px; -fx-font-style: italic;");
                summaryContent.getChildren().add(emptyLabel);
                return;
            }
            
            // Train ID
            String trainId = trainIdField.getText().trim();
            if (!trainId.isEmpty()) {
                addSummaryItem("Train ID:", trainId);
            }
            
            // Route
            Facility start = startStationCombo.getSelectionModel().getSelectedItem();
            Facility end = endStationCombo.getSelectionModel().getSelectedItem();
            if (start != null && end != null) {
                addSummaryItem("Route:", start.getId() + " → " + end.getId());
            }
            
            // Departure
            if (departureDatePicker.getValue() != null && !departureTimeField.getText().trim().isEmpty()) {
                String departure = departureDatePicker.getValue().toString() + " " + departureTimeField.getText().trim();
                addSummaryItem("Departure:", departure);
            }
            
            // Locomotives
            ObservableList<Locomotive> selectedLocos = selectedLocomotivesTable.getItems();
            if (!selectedLocos.isEmpty()) {
                VBox locosBox = new VBox(5);
                Label locosLabel = new Label("Locomotives:");
                locosLabel.setStyle("-fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: 500;");
                locosBox.getChildren().add(locosLabel);
                
                double totalPower = 0;
                for (Locomotive loco : selectedLocos) {
                    double power = loco.getPowerKw();
                    totalPower += power;
                    Label locoLabel = new Label(String.format("• %s — %s\n  Power: %.0f kW | Max speed: %.0f km/h", 
                        loco.getId(), loco.getType(), power, loco.getMaxSpeedKmH()));
                    locoLabel.setStyle("-fx-text-fill: white; -fx-font-size: 12px;");
                    locosBox.getChildren().add(locoLabel);
                }
                Label totalPowerLabel = new Label(String.format("Total power: %.0f kW", totalPower));
                totalPowerLabel.setStyle("-fx-text-fill: white; -fx-font-size: 12px; -fx-font-weight: 500;");
                locosBox.getChildren().add(totalPowerLabel);
                summaryContent.getChildren().add(locosBox);
            }
            
            // Wagons
            ObservableList<Wagon> selectedWags = selectedWagonsTable.getItems();
            if (!selectedWags.isEmpty()) {
                VBox wagonsBox = new VBox(5);
                Label wagonsLabel = new Label("Wagons:");
                wagonsLabel.setStyle("-fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: 500;");
                wagonsBox.getChildren().add(wagonsLabel);
                
                double totalMass = 0;
                boolean loaded = wagonsLoadedCheckBox.isSelected();
                for (Wagon wagon : selectedWags) {
                    double mass = loaded ? wagon.getFullyLoadedWeightKg() : wagon.getEmptyWeightKg();
                    totalMass += mass;
                }
                
                Label countLabel = new Label(String.format("%d wagons (%s)", 
                    selectedWags.size(), loaded ? "loaded" : "empty"));
                countLabel.setStyle("-fx-text-fill: white; -fx-font-size: 12px;");
                wagonsBox.getChildren().add(countLabel);
                
                Label totalMassLabel = new Label(String.format("Total mass: %.1f t", totalMass / 1000.0));
                totalMassLabel.setStyle("-fx-text-fill: white; -fx-font-size: 12px; -fx-font-weight: 500;");
                wagonsBox.getChildren().add(totalMassLabel);
                summaryContent.getChildren().add(wagonsBox);
            }
        });
    }
    
    /**
     * Adds a summary item to the summary panel.
     */
    private void addSummaryItem(String label, String value) {
        HBox item = new HBox(10);
        Label labelLabel = new Label(label);
        labelLabel.setStyle("-fx-text-fill: white; -fx-font-size: 13px; -fx-min-width: 90;");
        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-text-fill: white; -fx-font-size: 13px;");
        item.getChildren().addAll(labelLabel, valueLabel);
        summaryContent.getChildren().add(item);
    }
    
    /**
     * Creates the results tab with professional report format.
     */
    private VBox createResultsTab() {
        VBox box = new VBox(20);
        box.setPadding(new Insets(20));
        box.setStyle("-fx-background-color: #0D1117;");
        
        resultsScrollPane = new ScrollPane();
        resultsScrollPane.setFitToWidth(true);
        resultsScrollPane.setStyle("-fx-background-color: #0D1117; -fx-border-color: transparent;");
        
        resultsContent = new VBox(20);
        resultsContent.setPadding(new Insets(10));
        resultsContent.setStyle("-fx-background-color: #0D1117;");
        
        Label emptyLabel = new Label("No dispatch report available. Dispatch trains to see results.");
        emptyLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-style: italic;");
        resultsContent.getChildren().add(emptyLabel);
        
        resultsScrollPane.setContent(resultsContent);
        VBox.setVgrow(resultsScrollPane, Priority.ALWAYS);
        
        box.getChildren().add(resultsScrollPane);
        
        return box;
    }
    
    /**
     * Adds a locomotive to the selected list.
     */
    private void addLocomotive() {
        Locomotive selected = availableLocomotivesTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            ObservableList<Locomotive> selectedList = selectedLocomotivesTable.getItems();
            if (!selectedList.contains(selected)) {
                selectedList.add(selected);
            }
        }
    }
    
    /**
     * Removes a locomotive from the selected list.
     */
    private void removeLocomotive() {
        Locomotive selected = selectedLocomotivesTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            selectedLocomotivesTable.getItems().remove(selected);
        }
    }
    
    /**
     * Adds a wagon to the selected list.
     */
    private void addWagon() {
        Wagon selected = availableWagonsTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            ObservableList<Wagon> selectedList = selectedWagonsTable.getItems();
            if (!selectedList.contains(selected)) {
                selectedList.add(selected);
            }
        }
    }
    
    /**
     * Removes a wagon from the selected list.
     */
    private void removeWagon() {
        Wagon selected = selectedWagonsTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            selectedWagonsTable.getItems().remove(selected);
        }
    }
    
    /**
     * Adds a train to the trains list.
     */
    private void addTrain() {
        System.out.println("Add Train button clicked");
        
        // Validate inputs
        if (trainIdField == null || trainIdField.getText() == null || trainIdField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Train ID is required.");
            System.out.println("Validation failed: Train ID is empty");
            return;
        }
        
        if (startStationCombo == null || endStationCombo == null ||
            startStationCombo.getSelectionModel().getSelectedItem() == null ||
            endStationCombo.getSelectionModel().getSelectedItem() == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "Start and end stations are required.");
            System.out.println("Validation failed: Stations not selected");
            return;
        }
        
        if (selectedLocomotivesTable == null || selectedLocomotivesTable.getItems() == null ||
            selectedLocomotivesTable.getItems().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "At least one locomotive is required.");
            System.out.println("Validation failed: No locomotives selected");
            return;
        }
        
        if (departureDatePicker == null || departureDatePicker.getValue() == null || 
            departureTimeField == null || departureTimeField.getText() == null || 
            departureTimeField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Departure date and time are required.");
            System.out.println("Validation failed: Departure date/time not set");
            return;
        }
        
        System.out.println("All validations passed, creating train...");
        
        try {
            // Parse departure time
            String timeText = departureTimeField.getText().trim();
            System.out.println("Parsing departure time: " + timeText);
            LocalDateTime departureTime = LocalDateTime.of(
                departureDatePicker.getValue(),
                java.time.LocalTime.parse(timeText, DateTimeFormatter.ofPattern("HH:mm"))
            );
            System.out.println("Departure time parsed: " + departureTime);
            
            // Create route based on user selection
            Facility startStation = startStationCombo.getSelectionModel().getSelectedItem();
            Facility endStation = endStationCombo.getSelectionModel().getSelectedItem();
            
            List<Facility> route;
            List<Facility> selectedStations;
            
            if (specifyIntermediateStationsCheckBox.isSelected() && 
                intermediateStationsList != null && 
                !intermediateStationsList.getItems().isEmpty()) {
                // User specified intermediate stations - use them in order
                List<Facility> intermediateStations = new ArrayList<>(intermediateStationsList.getItems());
                route = new ArrayList<>();
                route.add(startStation);
                route.addAll(intermediateStations);
                route.add(endStation);
                
                // Selected stations include start, intermediates, and end
                selectedStations = new ArrayList<>(route);
            } else {
                // Automatic calculation - only start and end
                // The SchedulerService will automatically find the fastest path if stations are not directly connected
                route = Arrays.asList(startStation, endStation);
                selectedStations = Arrays.asList(startStation, endStation); // User-selected stations
            }
            
            // Calculate total weight
            double totalWeight = selectedWagonsTable.getItems().stream()
                .mapToDouble(wagon -> wagonsLoadedCheckBox.isSelected() 
                    ? wagon.getFullyLoadedWeightKg() 
                    : wagon.getEmptyWeightKg())
                .sum();
            
            // Create train
            // Note: route will be expanded automatically by SchedulerService if stations are not directly connected
            Train train = new Train(
                trainIdField.getText().trim(),
                route,                    // Full route (will be expanded if needed)
                selectedStations,         // User-selected stations (start and end only)
                new ArrayList<>(selectedLocomotivesTable.getItems()),
                new ArrayList<>(selectedWagonsTable.getItems()),
                departureTime,
                totalWeight
            );
            
            System.out.println("Adding train to list: " + train.getTrainId());
            trains.add(train);
            System.out.println("Train added. Total trains: " + trains.size());
            
            // Update summary
            updateSummary();
            
            // Clear form
            trainIdField.clear();
            startStationCombo.getSelectionModel().clearSelection();
            endStationCombo.getSelectionModel().clearSelection();
            departureDatePicker.setValue(null);
            departureTimeField.clear();
            selectedLocomotivesTable.getItems().clear();
            selectedWagonsTable.getItems().clear();
            wagonsLoadedCheckBox.setSelected(false);
            specifyIntermediateStationsCheckBox.setSelected(false);
            if (intermediateStationsList != null) {
                intermediateStationsList.getItems().clear();
            }
            if (intermediateStationCombo != null) {
                intermediateStationCombo.getSelectionModel().clearSelection();
            }
            
            // Show success message
            showAlert(Alert.AlertType.INFORMATION, "Success", 
                "Train " + train.getTrainId() + " added successfully to dispatch queue.\n\n" +
                "Total trains in queue: " + trains.size());
            
        } catch (java.time.format.DateTimeParseException e) {
            showAlert(Alert.AlertType.ERROR, "Error", 
                "Invalid time format. Please use HH:mm format (e.g., 14:30).");
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            showAlert(Alert.AlertType.ERROR, "Error", 
                "Invalid input: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", 
                "Failed to create train: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Removes the selected train from the list.
     */
    private void removeTrain() {
        Train selected = trainsList.getSelectionModel().getSelectedItem();
        if (selected != null) {
            trains.remove(selected);
        }
    }
    
    /**
     * Dispatches trains and calculates schedules.
     */
    private void dispatchTrains() {
        if (trains.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Warning", "No trains to dispatch. Please add trains first.");
            return;
        }
        
        dispatchButton.setDisable(true);
        progressIndicator.setVisible(true);
        statusLabel.setText("Dispatching trains...");
        statusLabel.setStyle("-fx-text-fill: white; -fx-font-size: 13px;");
        
        Service<DispatchReport> service = new Service<DispatchReport>() {
            @Override
            protected Task<DispatchReport> createTask() {
                return new Task<DispatchReport>() {
                    @Override
                    protected DispatchReport call() throws Exception {
                        System.out.println("Dispatching " + trains.size() + " train(s)...");
                        for (Train train : trains) {
                            System.out.println("  - Train " + train.getTrainId() + ": " + 
                                train.getRoute().get(0).getId() + " -> " + 
                                train.getRoute().get(train.getRoute().size() - 1).getId());
                        }
                        return controller.scheduleTrains(new ArrayList<>(trains));
                    }
                };
            }
        };
        
        service.setOnSucceeded(e -> {
            currentReport = service.getValue();
            updateResultsTab();
            dispatchButton.setDisable(false);
            progressIndicator.setVisible(false);
            
            // Professional success message
            DispatchReport.Summary summary = currentReport.getSummary();
            
            // Check if any trains were actually scheduled
            if (summary.getTotalTrainsDispatched() == 0) {
                statusLabel.setText("⚠ No trains scheduled - check console for errors");
                statusLabel.setStyle("-fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: 500;");
                showAlert(Alert.AlertType.WARNING, "Warning", 
                    "No trains were successfully scheduled. Check the console for error messages.\n\n" +
                    "Common issues:\n" +
                    "- Stations may not be directly connected\n" +
                    "- Missing segment data in database\n" +
                    "Total trains attempted: " + trains.size());
            } else {
                // Clear the dispatch queue after successful dispatch
                trains.clear();
                updateSummary(); // Update summary to reflect empty queue
                
                StringBuilder successMsg = new StringBuilder();
                successMsg.append("✓ Dispatch completed successfully\n");
                successMsg.append(String.format("%d train%s scheduled\n", 
                    summary.getTotalTrainsDispatched(),
                    summary.getTotalTrainsDispatched() != 1 ? "s" : ""));
                successMsg.append(String.format("%d conflict%s resolved automatically",
                    summary.getNumberOfResolvableConflicts(),
                    summary.getNumberOfResolvableConflicts() != 1 ? "s" : ""));
                
                statusLabel.setText(successMsg.toString());
                statusLabel.setStyle("-fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: 500;");
                mainTabPane.getSelectionModel().select(1); // Switch to results tab
            }
        });
        
        service.setOnFailed(e -> {
            dispatchButton.setDisable(false);
            progressIndicator.setVisible(false);
            statusLabel.setText("✗ Dispatch failed!");
            statusLabel.setStyle("-fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: 500;");
            showAlert(Alert.AlertType.ERROR, "Error", 
                "Failed to dispatch trains: " + service.getException().getMessage());
        });
        
        service.start();
    }
    
    /**
     * Updates the results tab with professional report format.
     */
    private void updateResultsTab() {
        if (currentReport == null) return;
        
        Platform.runLater(() -> {
            resultsContent.getChildren().clear();
            
            DispatchReport.Summary summary = currentReport.getSummary();
            
            // Summary header
            VBox summaryBox = new VBox(10);
            summaryBox.setPadding(new Insets(15));
            summaryBox.setStyle("-fx-background-color: #161B22; -fx-background-radius: 12; " +
                              "-fx-border-color: rgba(16, 185, 129, 0.2); -fx-border-radius: 12; -fx-border-width: 1;");
            
            Label summaryTitle = new Label("Dispatch Summary");
            summaryTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: 600; -fx-text-fill: white;");
            
            HBox summaryStats = new HBox(30);
            addStatItem(summaryStats, "Total Trains", String.valueOf(summary.getTotalTrainsDispatched()));
            addStatItem(summaryStats, "Conflicts", String.valueOf(summary.getNumberOfConflicts()));
            addStatItem(summaryStats, "Resolved", String.valueOf(summary.getNumberOfResolvableConflicts()));
            addStatItem(summaryStats, "Unsolvable", String.valueOf(summary.getNumberOfUnsolvableConflicts()));
            
            VBox summaryContent = new VBox(10);
            summaryContent.getChildren().add(summaryStats);
            
            // Add double track passages information
            if (summary.getNumberOfDoubleTrackPassages() > 0) {
                java.util.List<String> doubleTrackSegmentIds = summary.getDoubleTrackSegmentIds();
                VBox doubleTrackBox = new VBox(5);
                doubleTrackBox.setPadding(new Insets(10, 0, 0, 0));
                
                for (String segmentId : doubleTrackSegmentIds) {
                    Label doubleTrackLabel = new Label("Since segment " + segmentId + " is a double-track segment, both trains can pass it simultaneously without conflict.");
                    doubleTrackLabel.setStyle("-fx-text-fill: #10B981; -fx-font-size: 12px; -fx-wrap-text: true;");
                    doubleTrackBox.getChildren().add(doubleTrackLabel);
                }
                
                summaryContent.getChildren().add(doubleTrackBox);
            }
            
            summaryBox.getChildren().addAll(summaryTitle, summaryContent);
            resultsContent.getChildren().add(summaryBox);
            
            // Train schedules - professional report format
            for (DispatchReport.TrainSchedule schedule : currentReport.getTrainSchedules()) {
                VBox trainBox = createTrainScheduleBox(schedule);
                resultsContent.getChildren().add(trainBox);
            }
            
            // Conflicts section
            if (!currentReport.getConflicts().isEmpty()) {
                VBox conflictsBox = new VBox(15);
                conflictsBox.setPadding(new Insets(15));
                conflictsBox.setStyle("-fx-background-color: #161B22; -fx-background-radius: 12; " +
                                    "-fx-border-color: rgba(239, 68, 68, 0.2); -fx-border-radius: 12; -fx-border-width: 1;");
                
                Label conflictsTitle = new Label("Conflicts");
                conflictsTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: 600; -fx-text-fill: white;");
                conflictsBox.getChildren().add(conflictsTitle);
                
                for (DispatchReport.Conflict conflict : currentReport.getConflicts()) {
                    VBox conflictBox = createConflictBox(conflict);
                    conflictsBox.getChildren().add(conflictBox);
                }
                
                resultsContent.getChildren().add(conflictsBox);
            }
        });
    }
    
    /**
     * Creates a professional train schedule box.
     */
    private VBox createTrainScheduleBox(DispatchReport.TrainSchedule schedule) {
        VBox box = new VBox(12);
        box.setPadding(new Insets(20));
        box.setStyle("-fx-background-color: #161B22; -fx-background-radius: 12; " +
                    "-fx-border-color: rgba(99, 102, 241, 0.15); -fx-border-radius: 12; -fx-border-width: 1;");
        
        // Header
        Label trainHeader = new Label("Train " + schedule.getTrainId());
        trainHeader.setStyle("-fx-font-size: 16px; -fx-font-weight: 600; -fx-text-fill: white;");
        
        Separator separator = new Separator();
        separator.setStyle("-fx-background-color: rgba(99, 102, 241, 0.3);");
        
        // Route - show only selected stations by user (not all intermediate stations)
        StringBuilder routeStr = new StringBuilder();
        List<Facility> selectedStations = schedule.getSelectedStations();
        
        for (Facility station : selectedStations) {
            if (routeStr.length() > 0) routeStr.append(" → ");
            routeStr.append(station.getName() + " (ID: " + station.getId() + ")");
        }
        
        // If route is long, show simplified version: start -> ... -> end
        String routeDisplay = routeStr.toString();
        if (selectedStations.size() > 4) {
            Facility firstStation = selectedStations.get(0);
            Facility lastStation = selectedStations.get(selectedStations.size() - 1);
            routeDisplay = firstStation.getName() + " (ID: " + firstStation.getId() + ") → ... (" + 
                          (selectedStations.size() - 2) + " stations) → " + 
                          lastStation.getName() + " (ID: " + lastStation.getId() + ")";
        }
        
        Label routeLabel = new Label("Route: " + routeDisplay);
        routeLabel.setStyle("-fx-text-fill: white; -fx-font-size: 13px;");
        
        // Departure and arrival
        String departure = "N/A";
        String arrival = "N/A";
        String totalTime = "N/A";
        
        if (!schedule.getRouteWithETAs().isEmpty()) {
            DispatchReport.TrainSchedule.StationETA first = schedule.getRouteWithETAs().get(0);
            DispatchReport.TrainSchedule.StationETA last = schedule.getRouteWithETAs().get(schedule.getRouteWithETAs().size() - 1);
            
            departure = first.getEta().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            arrival = last.getEta().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            totalTime = TravelCalculationService.formatTravelTime(schedule.getTotalTravelTimeHours());
        }
        
        Label departureLabel = new Label("Departure: " + departure);
        departureLabel.setStyle("-fx-text-fill: white; -fx-font-size: 13px;");
        
        Label arrivalLabel = new Label("Arrival: " + arrival);
        arrivalLabel.setStyle("-fx-text-fill: white; -fx-font-size: 13px;");
        
        Label timeLabel = new Label("Total time: " + totalTime);
        timeLabel.setStyle("-fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: 500;");
        
        // Stations list - show only selected stations by user, not all intermediate stations
        VBox stationsBox = new VBox(5);
        Label stationsLabel = new Label("Stations:");
        stationsLabel.setStyle("-fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: 500;");
        stationsBox.getChildren().add(stationsLabel);
        
        // Create a map of station ID to ETA for quick lookup
        java.util.Map<String, DispatchReport.TrainSchedule.StationETA> etaMap = new java.util.HashMap<>();
        for (DispatchReport.TrainSchedule.StationETA routeETA : schedule.getRouteWithETAs()) {
            etaMap.put(routeETA.getStation().getId(), routeETA);
        }
        
        // Display only selected stations (user-specified stations, not all intermediates)
        // selectedStations is already defined above, so we reuse it
        for (Facility station : selectedStations) {
            DispatchReport.TrainSchedule.StationETA routeETA = etaMap.get(station.getId());
            if (routeETA != null) {
                String stationTime = routeETA.getEta().format(DateTimeFormatter.ofPattern("HH:mm"));
                String stationDisplay = station.getName() + " (ID: " + station.getId() + ")";
                Label stationLabel = new Label("• " + stationDisplay + " " + stationTime);
                stationLabel.setStyle("-fx-text-fill: white; -fx-font-size: 12px;");
                stationsBox.getChildren().add(stationLabel);
            }
        }
        
        box.getChildren().addAll(trainHeader, separator, routeLabel, departureLabel, arrivalLabel, timeLabel, stationsBox);
        
        return box;
    }
    
    /**
     * Creates a professional conflict box.
     */
    private VBox createConflictBox(DispatchReport.Conflict conflict) {
        VBox box = new VBox(8);
        box.setPadding(new Insets(12));
        box.setStyle("-fx-background-color: #0D1117; -fx-background-radius: 8; " +
                    "-fx-border-color: rgba(239, 68, 68, 0.3); -fx-border-radius: 8; -fx-border-width: 1;");
        
        Label warningLabel = new Label("⚠ Conflict detected");
        warningLabel.setStyle("-fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: 600;");
        
        Label segmentLabel = new Label("Segment: " + conflict.getSegmentId());
        segmentLabel.setStyle("-fx-text-fill: white; -fx-font-size: 12px;");
        
        Label trainsLabel = new Label("Trains: " + conflict.getTrain1Id() + " / " + conflict.getTrain2Id());
        trainsLabel.setStyle("-fx-text-fill: white; -fx-font-size: 12px;");
        
        Label resolutionLabel = new Label("Resolution: " + conflict.getDescription());
        resolutionLabel.setStyle("-fx-text-fill: white; -fx-font-size: 12px;");
        
        box.getChildren().addAll(warningLabel, segmentLabel, trainsLabel, resolutionLabel);
        
        return box;
    }
    
    /**
     * Adds a stat item to the summary stats box.
     */
    private void addStatItem(HBox box, String label, String value) {
        VBox statBox = new VBox(3);
        Label labelLabel = new Label(label);
        labelLabel.setStyle("-fx-text-fill: white; -fx-font-size: 12px;");
        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: 600;");
        statBox.getChildren().addAll(labelLabel, valueLabel);
        box.getChildren().add(statBox);
    }
    
    /**
     * Shows an alert dialog.
     */
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private static class TrainListCell extends ListCell<Train> {
        @Override
        protected void updateItem(Train item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
                setGraphic(null);
            } else {
                // Build route string using selected stations
                StringBuilder routeStr = new StringBuilder();
                List<Facility> selectedStations = item.getSelectedStations();
                for (Facility station : selectedStations) {
                    if (routeStr.length() > 0) routeStr.append(" → ");
                    routeStr.append(station.getName() + " (ID: " + station.getId() + ")");
                }
                
                // Build locomotive IDs string
                StringBuilder locoIds = new StringBuilder();
                for (Locomotive loco : item.getLocomotives()) {
                    if (locoIds.length() > 0) locoIds.append(", ");
                    locoIds.append(loco.getId());
                }
                
                // Build wagon IDs string
                StringBuilder wagonIds = new StringBuilder();
                for (Wagon wagon : item.getWagons()) {
                    if (wagonIds.length() > 0) wagonIds.append(", ");
                    wagonIds.append(wagon.getWagonId());
                }
                
                // Build professional display text
                StringBuilder displayText = new StringBuilder();
                displayText.append("Train ").append(item.getTrainId()).append("\n");
                displayText.append("Route: ").append(routeStr.toString()).append("\n");
                
                if (item.getLocomotives().size() == 1) {
                    displayText.append("Locomotive: ").append(locoIds.toString());
                } else {
                    displayText.append("Locomotives: ").append(locoIds.toString());
                }
                
                if (!item.getWagons().isEmpty()) {
                    displayText.append("\n");
                    if (item.getWagons().size() == 1) {
                        displayText.append("Wagon: ").append(wagonIds.toString());
                    } else {
                        displayText.append("Wagons: ").append(wagonIds.toString());
                    }
                }
                
                displayText.append("\nDeparture: ").append(
                    item.getDeparturePlannedDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
                
                setText(displayText.toString());
                setStyle("-fx-text-fill: #E2E8F0; -fx-font-size: 12px; -fx-font-family: 'Segoe UI', 'Roboto', sans-serif; " +
                        "-fx-padding: 10 15; -fx-background-color: #1E293B; -fx-background-radius: 6;");
            }
        }
    }
    
    // String converter for Facility ComboBox
    private static class FacilityStringConverter extends StringConverter<Facility> {
        @Override
        public String toString(Facility facility) {
            return facility == null ? null : facility.getId() + " - " + facility.getName();
        }
        
        @Override
        public Facility fromString(String string) {
            return null; // Not needed for ComboBox
        }
    }
    
    /**
     * Helper method to apply styles to table headers.
     */
    private void applyTableHeaderStyles(TableView<?> table) {
        javafx.scene.Node header = table.lookup(".column-header-background");
        if (header != null) {
            header.setStyle("-fx-background-color: #2D3748;");
        }
        // Remover quadrado branco (filler)
        javafx.scene.Node filler = table.lookup(".filler");
        if (filler != null) {
            filler.setStyle("-fx-background-color: transparent;");
        }
        // Aplicar texto branco aos labels dos headers
        for (javafx.scene.Node node : table.lookupAll(".column-header .label")) {
            node.setStyle("-fx-text-fill: white;");
        }
    }
}
