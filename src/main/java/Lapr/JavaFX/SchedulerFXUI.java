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
    
    // Train selection tab - Left column (60%)
    private VBox trainSelectionTab;
    private ComboBox<Train> availableTrainsCombo; // Comboios já montados na USLP09
    private DatePicker departureDatePicker;
    private TextField departureTimeField;
    private CheckBox specifyIntermediateStationsCheckBox;
    private VBox intermediateStationsBox;
    private ListView<Facility> intermediateStationsList;
    private ComboBox<Facility> intermediateStationCombo;
    private Button addIntermediateStationButton;
    private Button removeIntermediateStationButton;
    private Button addTrainToDispatchButton; // Adiciona comboio à fila de dispatch
    private ListView<Train> trainsList; // Lista de comboios para dispatch
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
    private ObservableList<Train> availableTrains; // Comboios já montados na USLP09
    private ObservableList<Train> trains; // Comboios na fila de dispatch
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
     * USLP10: Loads trains that were already assembled in USLP09.
     */
    private void initializeData() {
        facilities = FXCollections.observableArrayList(controller.getStations());

        // Load trains that were already assembled and assigned to routes in USLP09
        try {
            List<Train> trainsFromDB = controller.getAvailableTrains();
            availableTrains = FXCollections.observableArrayList(trainsFromDB);
            
            if (availableTrains.isEmpty()) {
                System.err.println("WARNING: No trains available for dispatch. Please create trains in USLP09 first.");
            } else {
                System.out.println("Loaded " + availableTrains.size() + " trains from USLP09 for dispatch");
            }
        } catch (Exception e) {
            System.err.println("Error loading trains from database: " + e.getMessage());
            e.printStackTrace();
            availableTrains = FXCollections.observableArrayList();
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
        
        VBox bottomBox = createBottomBox();
        root.setBottom(bottomBox);

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
     * Creates the bottom box with status information.
     */
    private VBox createBottomBox() {
        VBox box = new VBox(5);
        box.setPadding(new Insets(10, 20, 10, 20));
        box.setStyle("-fx-background-color: white;");
        box.setPrefHeight(60);
        box.setMinHeight(60);
        box.setMaxHeight(60);
        
        Label statusInfo = new Label("Ready to dispatch trains");
        statusInfo.setStyle("-fx-text-fill: #0D1117; -fx-font-size: 13px;");
        
        box.getChildren().add(statusInfo);
        
        return box;
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
        VBox dispatchQueueBox = createDispatchQueueSection();

        leftColumn.getChildren().addAll(
                trainConfigBox,
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
     * USLP10: Only allows selection of trains already assembled in USLP09.
     */
    private VBox createCompactTrainConfigSection() {
        VBox box = new VBox(12);
        box.setPadding(new Insets(20));
        box.setStyle("-fx-background-color: #161B22; -fx-background-radius: 12; " +
                    "-fx-border-color: rgba(99, 102, 241, 0.15); -fx-border-radius: 12; -fx-border-width: 1;");
        
        Label sectionLabel = new Label("Train Selection & Dispatch Configuration");
        sectionLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: 600; -fx-text-fill: white;");
        
        VBox formBox = new VBox(12);
        formBox.setPadding(new Insets(15, 0, 0, 0));
        
        // Select Train (already assembled in USLP09)
        VBox trainBox = new VBox(5);
        Label trainLabel = new Label("Select Train:");
        trainLabel.setStyle("-fx-text-fill: white; -fx-font-size: 13px;");
        availableTrainsCombo = new ComboBox<>(availableTrains);
        availableTrainsCombo.setPrefWidth(250);
        availableTrainsCombo.setStyle("-fx-background-color: #0D1117; -fx-text-fill: white; " +
                                      "-fx-control-inner-background: #0D1117;");
        availableTrainsCombo.setCellFactory(list -> new ListCell<Train>() {
            @Override
            protected void updateItem(Train item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    List<Facility> route = item.getRoute();
                    String routeStr = route.size() >= 2 ? 
                        route.get(0).getId() + " -> " + route.get(route.size() - 1).getId() : 
                        "Route";
                    setText("Train " + item.getTrainId() + " (" + routeStr + ")");
                    setStyle("-fx-text-fill: white; -fx-background-color: #0D1117;");
                }
            }
        });
        trainBox.getChildren().addAll(trainLabel, availableTrainsCombo);
        
        // Departure Time
        VBox timeBox = new VBox(5);
        Label departureTimeLabel = new Label("Departure Time:");
        departureTimeLabel.setStyle("-fx-text-fill: white; -fx-font-size: 13px;");
        departureTimeField = new TextField();
        departureTimeField.setPrefWidth(250);
        departureTimeField.setPromptText("HH:mm");
        departureTimeField.setStyle("-fx-background-color: #0D1117; -fx-text-fill: white; -fx-border-color: #27272A; -fx-border-radius: 6;");
        timeBox.getChildren().addAll(departureTimeLabel, departureTimeField);
        
        // Departure Date
        VBox dateBox = new VBox(5);
        Label departureDateLabel = new Label("Departure Date:");
        departureDateLabel.setStyle("-fx-text-fill: white; -fx-font-size: 13px;");
        departureDatePicker = new DatePicker();
        departureDatePicker.setPrefWidth(250);
        departureDatePicker.setStyle("-fx-background-color: #0D1117; -fx-text-fill: white;");
        dateBox.getChildren().addAll(departureDateLabel, departureDatePicker);
        
        // Option to specify intermediate stations (path manual)
        specifyIntermediateStationsCheckBox = new CheckBox("Choose path manually (intermediate stations)");
        specifyIntermediateStationsCheckBox.setStyle("-fx-text-fill: white; -fx-font-size: 13px;");
        
        formBox.getChildren().addAll(trainBox, timeBox, dateBox, specifyIntermediateStationsCheckBox);
        
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
        
        box.getChildren().addAll(sectionLabel, formBox, intermediateStationsBox);
        
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
        Label instructionsLabel = new Label("Add intermediate stations in order:");
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
        
        // Validate that the station is not the start or end station of the selected train
        Train selectedTrain = availableTrainsCombo.getSelectionModel().getSelectedItem();
        if (selectedTrain != null && selectedTrain.getRoute().size() >= 2) {
            List<Facility> route = selectedTrain.getRoute();
            Facility startStation = route.get(0);
            Facility endStation = route.get(route.size() - 1);
            
            if (selected.equals(startStation)) {
                showAlert(Alert.AlertType.WARNING, "Warning", "This station is already the start station of the selected train.");
                return;
            }
            
            if (selected.equals(endStation)) {
                showAlert(Alert.AlertType.WARNING, "Warning", "This station is already the end station of the selected train.");
                return;
            }
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
     * USLP10: This section is no longer needed - trains are already assembled in USLP09.
     * Method kept for compatibility but returns empty VBox.
     */
    private VBox createResourcesSection() {
        // USLP10: Resources section removed - trains come pre-assembled from USLP09
        VBox box = new VBox(15);
        return box;
    }
    
    /**
     * Creates the locomotives section with professional formatting.
     * USLP10: This method is no longer needed - trains come pre-assembled from USLP09.
     */
    private VBox createLocomotivesSection() {
        // USLP10: Method disabled - trains are already assembled in USLP09
        return new VBox();
    }
    
    /**
     * Creates a modern TableView for locomotives with dark theme styling.
     * USLP10: This method is no longer needed.
     */
    private TableView<Locomotive> createLocomotiveTable() {
        // USLP10: Method disabled - trains are already assembled in USLP09
        return new TableView<>();
    }
    
    
    /**
     * Creates the wagons section with professional formatting.
     * USLP10: This method is no longer needed - trains come pre-assembled from USLP09.
     */
    /* USLP10: Method disabled - trains are already assembled
    private VBox createWagonsSection() {
        return new VBox(); // Placeholder - method disabled
    }
    
    private TableView<Wagon> createWagonTable() {
        return new TableView<>(); // Placeholder - method disabled
    }
    */
    
    // USLP10: Original implementation removed - trains are pre-assembled in USLP09
    // Removed code that referenced: availableWagonsTable, selectedWagonsTable, 
    // addWagonButton, removeWagonButton, wagons, etc.
    
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
        
        addTrainToDispatchButton = new Button("Add to Dispatch Queue");
        addTrainToDispatchButton.setStyle("-fx-background-color: #6366F1; -fx-text-fill: white; -fx-font-size: 13px; " +
                              "-fx-padding: 8 16; -fx-background-radius: 6; -fx-cursor: hand;");
        addTrainToDispatchButton.setOnAction(e -> {
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
        headerBox.getChildren().addAll(sectionLabel, addTrainToDispatchButton, removeTrainButton);
        
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
    /**
     * Updates the summary panel with information about selected train and dispatch queue.
     * USLP10: Shows information about trains in dispatch queue.
     */
    private void updateSummary() {
        Platform.runLater(() -> {
            summaryContent.getChildren().clear();
            
            // Show dispatch queue summary
            if (trains.isEmpty()) {
                Label emptyLabel = new Label("No trains in dispatch queue.\nSelect a train and add it to the queue.");
                emptyLabel.setStyle("-fx-text-fill: white; -fx-font-size: 13px; -fx-font-style: italic;");
                summaryContent.getChildren().add(emptyLabel);
                return;
            }
            
            // Show selected train info
            Train selectedTrain = availableTrainsCombo != null ? 
                availableTrainsCombo.getSelectionModel().getSelectedItem() : null;
            
            if (selectedTrain != null) {
                addSummaryItem("Selected Train:", selectedTrain.getTrainId());
                
                // Route
                List<Facility> route = selectedTrain.getRoute();
                if (route.size() >= 2) {
                    addSummaryItem("Route:", route.get(0).getId() + " → " + route.get(route.size() - 1).getId());
                }
                
                // Locomotives count
                addSummaryItem("Locomotives:", String.valueOf(selectedTrain.getLocomotives().size()));
                
                // Wagons count
                addSummaryItem("Wagons:", String.valueOf(selectedTrain.getWagons().size()));
                
                // Total weight
                addSummaryItem("Total Weight:", String.format("%.1f t", selectedTrain.getTotalWeight() / 1000.0));
            }
            
            // Departure info
            if (departureDatePicker != null && departureDatePicker.getValue() != null && 
                departureTimeField != null && !departureTimeField.getText().trim().isEmpty()) {
                String departure = departureDatePicker.getValue().toString() + " " + departureTimeField.getText().trim();
                addSummaryItem("Departure:", departure);
            }
            
            // Dispatch queue summary
            Label queueLabel = new Label("Dispatch Queue:");
            queueLabel.setStyle("-fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: 600; -fx-padding: 10 0 5 0;");
            summaryContent.getChildren().add(queueLabel);
            
            for (Train train : trains) {
                Label trainLabel = new Label(String.format("• Train %s", train.getTrainId()));
                trainLabel.setStyle("-fx-text-fill: white; -fx-font-size: 12px;");
                summaryContent.getChildren().add(trainLabel);
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
     * USLP10: This method is no longer needed - trains come pre-assembled from USLP09.
     */
    /* USLP10: Method disabled
    private void addLocomotive() {
        // Method removed - trains are already assembled
    }
    
    private void removeLocomotive() {
        // Method removed - trains are already assembled
    }
    
    private void addWagon() {
        // Method removed - trains are already assembled
    }
    
    private void removeWagon() {
        // Method removed - trains are already assembled
    }
    */
    
    /**
     * Adds a train to the dispatch queue.
     * USLP10: Uses train already assembled in USLP09, only sets path and departure time.
     */
    private void addTrain() {
        System.out.println("Add Train to Dispatch button clicked");
        
        // Validate inputs
        if (availableTrainsCombo == null || availableTrainsCombo.getSelectionModel().getSelectedItem() == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "Please select a train from the list.");
            System.out.println("Validation failed: No train selected");
            return;
        }
        
        if (departureDatePicker == null || departureDatePicker.getValue() == null || 
            departureTimeField == null || departureTimeField.getText() == null || 
            departureTimeField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Departure date and time are required.");
            System.out.println("Validation failed: Departure date/time not set");
            return;
        }
        
        System.out.println("All validations passed, adding train to dispatch queue...");
        
        try {
            // Get selected train (already assembled in USLP09)
            Train selectedTrain = availableTrainsCombo.getSelectionModel().getSelectedItem();
            
            // Parse departure time
            String timeText = departureTimeField.getText().trim();
            System.out.println("Parsing departure time: " + timeText);
            LocalDateTime departureTime = LocalDateTime.of(
                departureDatePicker.getValue(),
                java.time.LocalTime.parse(timeText, DateTimeFormatter.ofPattern("HH:mm"))
            );
            System.out.println("Departure time parsed: " + departureTime);
            
            // Get route from selected train
            List<Facility> route = selectedTrain.getRoute();
            List<Facility> selectedStations;
            
            if (specifyIntermediateStationsCheckBox.isSelected() && 
                intermediateStationsList != null && 
                !intermediateStationsList.getItems().isEmpty()) {
                // User specified intermediate stations - use them in order
                List<Facility> intermediateStations = new ArrayList<>(intermediateStationsList.getItems());
                List<Facility> newRoute = new ArrayList<>();
                newRoute.add(route.get(0)); // Start station
                newRoute.addAll(intermediateStations);
                newRoute.add(route.get(route.size() - 1)); // End station
                
                // Selected stations include start, intermediates, and end
                selectedStations = new ArrayList<>(newRoute);
                route = newRoute;
            } else {
                // Automatic calculation - use start and end from train route
                selectedStations = Arrays.asList(route.get(0), route.get(route.size() - 1));
            }
            
            // Create new Train with updated departure time and path
            // USLP10: Do not create new Train, Route, Locomotive or Wagon - reuse from USLP09
            Train trainForDispatch = new Train(
                selectedTrain.getTrainId(),
                route,                    // Full route (with or without intermediates)
                selectedStations,        // User-selected stations
                selectedTrain.getLocomotives(),  // Reuse locomotives from USLP09
                selectedTrain.getWagons(),        // Reuse wagons from USLP09
                departureTime,          // New departure time set by user
                selectedTrain.getTotalWeight()   // Reuse weight from USLP09
            );
            
            System.out.println("Adding train to dispatch queue: " + trainForDispatch.getTrainId());
            trains.add(trainForDispatch);
            System.out.println("Train added. Total trains in queue: " + trains.size());
            
            // Update summary
            updateSummary();
            
            // Clear form (but keep train selected)
            departureDatePicker.setValue(null);
            departureTimeField.clear();
            specifyIntermediateStationsCheckBox.setSelected(false);
            if (intermediateStationsList != null) {
                intermediateStationsList.getItems().clear();
            }
            if (intermediateStationCombo != null) {
                intermediateStationCombo.getSelectionModel().clearSelection();
            }
            
            // Show success message
            showAlert(Alert.AlertType.INFORMATION, "Success", 
                "Train " + trainForDispatch.getTrainId() + " added successfully to dispatch queue.\n\n" +
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
                "Failed to add train to dispatch queue: " + e.getMessage());
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
                        try {
                            updateMessage("Preparing to dispatch trains...");
                            System.out.println("Dispatching " + trains.size() + " train(s)...");
                            
                            // Validate trains before processing
                            for (Train train : trains) {
                                if (train == null) {
                                    throw new IllegalArgumentException("Train list contains null train");
                                }
                                if (train.getRoute() == null || train.getRoute().isEmpty()) {
                                    throw new IllegalArgumentException("Train " + train.getTrainId() + " has empty route");
                                }
                                System.out.println("  - Train " + train.getTrainId() + ": " + 
                                    train.getRoute().get(0).getId() + " -> " + 
                                    train.getRoute().get(train.getRoute().size() - 1).getId());
                            }
                            
                            updateMessage("Calculating schedules...");
                            Platform.runLater(() -> statusLabel.setText("Calculating schedules for " + trains.size() + " train(s)..."));
                            System.out.println("Calling controller.scheduleTrains()...");
                            
                            long startTime = System.currentTimeMillis();
                            DispatchReport report = controller.scheduleTrains(new ArrayList<>(trains));
                            long endTime = System.currentTimeMillis();
                            
                            System.out.println("Schedule calculation took " + (endTime - startTime) + " ms");
                            
                            System.out.println("Schedule calculation completed. Trains scheduled: " + 
                                (report != null && report.getSummary() != null ? 
                                    report.getSummary().getTotalTrainsDispatched() : 0));
                            
                            return report;
                        } catch (Exception ex) {
                            // Log full exception details
                            System.err.println("ERROR in dispatchTrains task:");
                            System.err.println("Exception type: " + ex.getClass().getName());
                            System.err.println("Exception message: " + ex.getMessage());
                            System.err.println("Stack trace:");
                            ex.printStackTrace();
                            
                            // Re-throw to be caught by onFailed handler
                            throw ex;
                        }
                    }
                };
            }
        };
        
        service.setOnSucceeded(e -> {
            try {
                DispatchReport report = service.getValue();
                if (report == null) {
                    throw new IllegalStateException("Dispatch report is null");
                }
                
                currentReport = report;
                updateResultsTab();
                dispatchButton.setDisable(false);
                progressIndicator.setVisible(false);
                
                // Professional success message
                DispatchReport.Summary summary = currentReport.getSummary();
                
                if (summary == null) {
                    throw new IllegalStateException("Dispatch summary is null");
                }
                
                // Check if any trains were actually scheduled
                if (summary.getTotalTrainsDispatched() == 0) {
                    statusLabel.setText("⚠ No trains scheduled - check console for errors");
                    statusLabel.setStyle("-fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: 500;");
                    showAlert(Alert.AlertType.WARNING, "Warning", 
                        "No trains were successfully scheduled. Check the console for error messages.\n\n" +
                        "Common issues:\n" +
                        "- Stations may not be directly connected\n" +
                        "- Missing segment data in database\n" +
                        "- Invalid train configuration\n" +
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
            } catch (Exception ex) {
                // Handle errors in success handler
                System.err.println("ERROR in onSucceeded handler: " + ex.getMessage());
                ex.printStackTrace();
                
                dispatchButton.setDisable(false);
                progressIndicator.setVisible(false);
                statusLabel.setText("✗ Error displaying results");
                statusLabel.setStyle("-fx-text-fill: #EF4444; -fx-font-size: 13px; -fx-font-weight: 500;");
                showAlert(Alert.AlertType.ERROR, "Error", 
                    "Failed to process dispatch results: " + ex.getMessage());
            }
        });
        
        service.setOnFailed(e -> {
            Platform.runLater(() -> {
                try {
                    Throwable exception = service.getException();
                    String errorMsg = "Unknown error occurred";
                    
                    if (exception != null) {
                        errorMsg = exception.getMessage();
                        if (errorMsg == null || errorMsg.isEmpty()) {
                            errorMsg = exception.getClass().getSimpleName();
                        }
                        
                        // Log full exception details
                        System.err.println("ERROR in dispatchTrains:");
                        System.err.println("Exception type: " + exception.getClass().getName());
                        System.err.println("Exception message: " + errorMsg);
                        System.err.println("Stack trace:");
                        exception.printStackTrace();
                    }
                    
                    dispatchButton.setDisable(false);
                    progressIndicator.setVisible(false);
                    statusLabel.setText("✗ Dispatch failed!");
                    statusLabel.setStyle("-fx-text-fill: #EF4444; -fx-font-size: 13px; -fx-font-weight: 500;");
                    
                    // Show detailed error message
                    StringBuilder alertMsg = new StringBuilder();
                    alertMsg.append("Failed to dispatch trains:\n\n");
                    alertMsg.append(errorMsg);
                    alertMsg.append("\n\nPlease check the console for more details.");
                    
                    showAlert(Alert.AlertType.ERROR, "Dispatch Error", alertMsg.toString());
                } catch (Exception ex) {
                    System.err.println("ERROR in onFailed handler: " + ex.getMessage());
                    ex.printStackTrace();
                }
            });
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
