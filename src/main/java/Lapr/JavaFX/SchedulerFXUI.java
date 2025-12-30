package Lapr.JavaFX;

import Lapr.Controller.DispatchController;
import Lapr.Domain.DispatchReport;
import Lapr.Domain.Train;
import Sprint1.Domain.Facility;
import Sprint1.Domain.Locomotive;
import Sprint1.Domain.Wagon;
import Sprint1.Domain.RailwayDataLoader;
import Sprint1.Domain.TravelCalculationService;
import Sprint1.Domain.IncompatibleGaugeException;

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
    private ListView<Locomotive> availableLocomotivesList;
    private ListView<Locomotive> selectedLocomotivesList;
    private ListView<Wagon> availableWagonsList;
    private ListView<Wagon> selectedWagonsList;
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
     */
    public SchedulerFXUI() {
        this.controller = new DispatchController();
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
     * Initializes data from controller and CSV files.
     */
    private void initializeData() {
        facilities = FXCollections.observableArrayList(controller.getStations());
        
        RailwayDataLoader dataLoader = new RailwayDataLoader();
        try {
            locomotives = FXCollections.observableArrayList(
                dataLoader.loadLocomotivesFromCSV("train_station_dataset/locomotives.csv"));
            wagons = FXCollections.observableArrayList(
                dataLoader.loadWagonsFromCSV("train_station_dataset/wagons.csv"));
        } catch (Exception e) {
            System.err.println("Error loading data: " + e.getMessage());
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
        titleLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #E4E4E7;");
        
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
        statusLabel.setStyle("-fx-text-fill: #A1A1AA; -fx-font-size: 13px;");
        
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
        HBox resourcesBox = createResourcesSection();
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
        sectionLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: 600; -fx-text-fill: #E4E4E7;");
        
        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(12);
        grid.setPadding(new Insets(15, 0, 0, 0));
        
        // Column 1
        // Train ID
        Label trainIdLabel = new Label("Train ID:");
        trainIdLabel.setStyle("-fx-text-fill: #A1A1AA; -fx-font-size: 13px;");
        trainIdField = new TextField();
        trainIdField.setPrefWidth(250);
        trainIdField.setStyle("-fx-background-color: #0D1117; -fx-text-fill: #E4E4E7; -fx-border-color: #27272A; -fx-border-radius: 6;");
        grid.add(trainIdLabel, 0, 0);
        grid.add(trainIdField, 1, 0);
        
        // End Station
        Label endStationLabel = new Label("End Station:");
        endStationLabel.setStyle("-fx-text-fill: #E4E4E7; -fx-font-size: 13px;");
        endStationCombo = new ComboBox<>(facilities);
        endStationCombo.setConverter(new FacilityStringConverter());
        endStationCombo.setPrefWidth(250);
        endStationCombo.setStyle("-fx-background-color: #0D1117; -fx-text-fill: #E4E4E7;");
        grid.add(endStationLabel, 0, 1);
        grid.add(endStationCombo, 1, 1);
        
        // Departure Time
        Label departureTimeLabel = new Label("Departure Time:");
        departureTimeLabel.setStyle("-fx-text-fill: #A1A1AA; -fx-font-size: 13px;");
        departureTimeField = new TextField();
        departureTimeField.setPrefWidth(250);
        departureTimeField.setPromptText("HH:mm");
        departureTimeField.setStyle("-fx-background-color: #0D1117; -fx-text-fill: #E4E4E7; -fx-border-color: #27272A; -fx-border-radius: 6;");
        grid.add(departureTimeLabel, 0, 2);
        grid.add(departureTimeField, 1, 2);
        
        // Column 2
        // Start Station
        Label startStationLabel = new Label("Start Station:");
        startStationLabel.setStyle("-fx-text-fill: #A1A1AA; -fx-font-size: 13px;");
        startStationCombo = new ComboBox<>(facilities);
        startStationCombo.setConverter(new FacilityStringConverter());
        startStationCombo.setPrefWidth(250);
        startStationCombo.setStyle("-fx-background-color: #0D1117; -fx-text-fill: #E4E4E7;");
        grid.add(startStationLabel, 2, 0);
        grid.add(startStationCombo, 3, 0);
        
        // Departure Date
        Label departureDateLabel = new Label("Departure Date:");
        departureDateLabel.setStyle("-fx-text-fill: #A1A1AA; -fx-font-size: 13px;");
        departureDatePicker = new DatePicker();
        departureDatePicker.setPrefWidth(250);
        departureDatePicker.setStyle("-fx-background-color: #0D1117; -fx-text-fill: #E4E4E7;");
        grid.add(departureDateLabel, 2, 1);
        grid.add(departureDatePicker, 3, 1);
        
        // Cargo status
        wagonsLoadedCheckBox = new CheckBox("Cargo status: Loaded");
        wagonsLoadedCheckBox.setStyle("-fx-text-fill: #A1A1AA; -fx-font-size: 13px;");
        grid.add(wagonsLoadedCheckBox, 2, 2);
        
        box.getChildren().addAll(sectionLabel, grid);
        
        return box;
    }
    
    /**
     * Creates the resources section (locomotives and wagons).
     */
    private HBox createResourcesSection() {
        HBox box = new HBox(15);
        
        // Locomotives section
        VBox locomotivesBox = createLocomotivesSection();
        
        // Wagons section
        VBox wagonsBox = createWagonsSection();
        
        box.getChildren().addAll(locomotivesBox, wagonsBox);
        HBox.setHgrow(locomotivesBox, Priority.ALWAYS);
        HBox.setHgrow(wagonsBox, Priority.ALWAYS);
        
        return box;
    }
    
    /**
     * Creates the locomotives section with professional formatting.
     */
    private VBox createLocomotivesSection() {
        VBox box = new VBox(10);
        box.setPadding(new Insets(20));
        box.setStyle("-fx-background-color: #161B22; -fx-background-radius: 12; " +
                    "-fx-border-color: rgba(99, 102, 241, 0.15); -fx-border-radius: 12; -fx-border-width: 1;");
        
        Label sectionLabel = new Label("Locomotives");
        sectionLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: 600; -fx-text-fill: #E4E4E7;");
        
        HBox listsBox = new HBox(10);
        
        // Available locomotives
        VBox availableBox = new VBox(5);
        Label availableLabel = new Label("Available:");
        availableLabel.setStyle("-fx-text-fill: #A1A1AA; -fx-font-size: 12px;");
        availableLocomotivesList = new ListView<>();
        availableLocomotivesList.setItems(locomotives);
        availableLocomotivesList.setCellFactory(list -> new LocomotiveListCell());
        availableLocomotivesList.setPrefHeight(200);
        availableLocomotivesList.setStyle("-fx-background-color: #0D1117;");
        availableBox.getChildren().addAll(availableLabel, availableLocomotivesList);
        
        // Buttons
        VBox buttonsBox = new VBox(10);
        buttonsBox.setAlignment(Pos.CENTER);
        addLocomotiveButton = new Button("→");
        addLocomotiveButton.setStyle("-fx-background-color: #10B981; -fx-text-fill: white; -fx-min-width: 40; -fx-pref-height: 30;");
        addLocomotiveButton.setOnAction(e -> addLocomotive());
        removeLocomotiveButton = new Button("←");
        removeLocomotiveButton.setStyle("-fx-background-color: #EF4444; -fx-text-fill: white; -fx-min-width: 40; -fx-pref-height: 30;");
        removeLocomotiveButton.setOnAction(e -> removeLocomotive());
        buttonsBox.getChildren().addAll(addLocomotiveButton, removeLocomotiveButton);
        
        // Selected locomotives
        VBox selectedBox = new VBox(5);
        Label selectedLabel = new Label("Selected:");
        selectedLabel.setStyle("-fx-text-fill: #A1A1AA; -fx-font-size: 12px;");
        selectedLocomotivesList = new ListView<>();
        selectedLocomotivesList.setCellFactory(list -> new LocomotiveListCell());
        selectedLocomotivesList.setPrefHeight(200);
        selectedLocomotivesList.setStyle("-fx-background-color: #0D1117;");
        selectedLocomotivesList.getItems().addListener((javafx.collections.ListChangeListener.Change<? extends Locomotive> c) -> updateSummary());
        selectedBox.getChildren().addAll(selectedLabel, selectedLocomotivesList);
        
        listsBox.getChildren().addAll(availableBox, buttonsBox, selectedBox);
        HBox.setHgrow(availableBox, Priority.ALWAYS);
        HBox.setHgrow(selectedBox, Priority.ALWAYS);
        
        box.getChildren().addAll(sectionLabel, listsBox);
        
        return box;
    }
    
    /**
     * Creates the wagons section with professional formatting.
     */
    private VBox createWagonsSection() {
        VBox box = new VBox(10);
        box.setPadding(new Insets(20));
        box.setStyle("-fx-background-color: #161B22; -fx-background-radius: 12; " +
                    "-fx-border-color: rgba(99, 102, 241, 0.15); -fx-border-radius: 12; -fx-border-width: 1;");
        
        Label sectionLabel = new Label("Wagons");
        sectionLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: 600; -fx-text-fill: #E4E4E7;");
        
        HBox listsBox = new HBox(10);
        
        // Available wagons
        VBox availableBox = new VBox(5);
        Label availableLabel = new Label("Available:");
        availableLabel.setStyle("-fx-text-fill: #A1A1AA; -fx-font-size: 12px;");
        availableWagonsList = new ListView<>();
        availableWagonsList.setItems(wagons);
        availableWagonsList.setCellFactory(list -> new WagonListCell());
        availableWagonsList.setPrefHeight(200);
        availableWagonsList.setStyle("-fx-background-color: #0D1117;");
        availableBox.getChildren().addAll(availableLabel, availableWagonsList);
        
        // Buttons
        VBox buttonsBox = new VBox(10);
        buttonsBox.setAlignment(Pos.CENTER);
        addWagonButton = new Button("→");
        addWagonButton.setStyle("-fx-background-color: #10B981; -fx-text-fill: white; -fx-min-width: 40; -fx-pref-height: 30;");
        addWagonButton.setOnAction(e -> addWagon());
        removeWagonButton = new Button("←");
        removeWagonButton.setStyle("-fx-background-color: #EF4444; -fx-text-fill: white; -fx-min-width: 40; -fx-pref-height: 30;");
        removeWagonButton.setOnAction(e -> removeWagon());
        buttonsBox.getChildren().addAll(addWagonButton, removeWagonButton);
        
        // Selected wagons
        VBox selectedBox = new VBox(5);
        Label selectedLabel = new Label("Selected:");
        selectedLabel.setStyle("-fx-text-fill: #A1A1AA; -fx-font-size: 12px;");
        selectedWagonsList = new ListView<>();
        selectedWagonsList.setCellFactory(list -> new WagonListCell());
        selectedWagonsList.setPrefHeight(200);
        selectedWagonsList.setStyle("-fx-background-color: #0D1117;");
        selectedWagonsList.getItems().addListener((javafx.collections.ListChangeListener.Change<? extends Wagon> c) -> updateSummary());
        selectedBox.getChildren().addAll(selectedLabel, selectedWagonsList);
        
        listsBox.getChildren().addAll(availableBox, buttonsBox, selectedBox);
        HBox.setHgrow(availableBox, Priority.ALWAYS);
        HBox.setHgrow(selectedBox, Priority.ALWAYS);
        
        box.getChildren().addAll(sectionLabel, listsBox);
        
        return box;
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
        sectionLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: 600; -fx-text-fill: #E4E4E7;");
        
        addTrainButton = new Button("Add Train");
        addTrainButton.setStyle("-fx-background-color: #6366F1; -fx-text-fill: white; -fx-font-size: 13px; " +
                              "-fx-padding: 8 16; -fx-background-radius: 6; -fx-cursor: hand;");
        addTrainButton.setOnAction(e -> addTrain());
        
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
        summaryTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: 600; -fx-text-fill: #E4E4E7;");
        
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
                selectedLocomotivesList.getItems().isEmpty() && 
                selectedWagonsList.getItems().isEmpty()) {
                Label emptyLabel = new Label("Configure a train to see summary");
                emptyLabel.setStyle("-fx-text-fill: #71717A; -fx-font-size: 13px; -fx-font-style: italic;");
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
            ObservableList<Locomotive> selectedLocos = selectedLocomotivesList.getItems();
            if (!selectedLocos.isEmpty()) {
                VBox locosBox = new VBox(5);
                Label locosLabel = new Label("Locomotives:");
                locosLabel.setStyle("-fx-text-fill: #A1A1AA; -fx-font-size: 13px; -fx-font-weight: 500;");
                locosBox.getChildren().add(locosLabel);
                
                double totalPower = 0;
                for (Locomotive loco : selectedLocos) {
                    double power = loco.getPowerKw();
                    totalPower += power;
                    Label locoLabel = new Label(String.format("• %s — %s\n  Power: %.0f kW | Max speed: %.0f km/h", 
                        loco.getId(), loco.getType(), power, loco.getMaxSpeedKmH()));
                    locoLabel.setStyle("-fx-text-fill: #E4E4E7; -fx-font-size: 12px;");
                    locosBox.getChildren().add(locoLabel);
                }
                Label totalPowerLabel = new Label(String.format("Total power: %.0f kW", totalPower));
                totalPowerLabel.setStyle("-fx-text-fill: #10B981; -fx-font-size: 12px; -fx-font-weight: 500;");
                locosBox.getChildren().add(totalPowerLabel);
                summaryContent.getChildren().add(locosBox);
            }
            
            // Wagons
            ObservableList<Wagon> selectedWags = selectedWagonsList.getItems();
            if (!selectedWags.isEmpty()) {
                VBox wagonsBox = new VBox(5);
                Label wagonsLabel = new Label("Wagons:");
                wagonsLabel.setStyle("-fx-text-fill: #A1A1AA; -fx-font-size: 13px; -fx-font-weight: 500;");
                wagonsBox.getChildren().add(wagonsLabel);
                
                double totalMass = 0;
                boolean loaded = wagonsLoadedCheckBox.isSelected();
                for (Wagon wagon : selectedWags) {
                    double mass = loaded ? wagon.getFullyLoadedWeightKg() : wagon.getEmptyWeightKg();
                    totalMass += mass;
                }
                
                Label countLabel = new Label(String.format("%d wagons (%s)", 
                    selectedWags.size(), loaded ? "loaded" : "empty"));
                countLabel.setStyle("-fx-text-fill: #E4E4E7; -fx-font-size: 12px;");
                wagonsBox.getChildren().add(countLabel);
                
                Label totalMassLabel = new Label(String.format("Total mass: %.1f t", totalMass / 1000.0));
                totalMassLabel.setStyle("-fx-text-fill: #10B981; -fx-font-size: 12px; -fx-font-weight: 500;");
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
        labelLabel.setStyle("-fx-text-fill: #A1A1AA; -fx-font-size: 13px; -fx-min-width: 90;");
        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-text-fill: #E4E4E7; -fx-font-size: 13px;");
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
        resultsScrollPane.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
        
        resultsContent = new VBox(20);
        resultsContent.setPadding(new Insets(10));
        
        Label emptyLabel = new Label("No dispatch report available. Dispatch trains to see results.");
        emptyLabel.setStyle("-fx-text-fill: #71717A; -fx-font-size: 14px; -fx-font-style: italic;");
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
        Locomotive selected = availableLocomotivesList.getSelectionModel().getSelectedItem();
        if (selected != null) {
            ObservableList<Locomotive> selectedList = selectedLocomotivesList.getItems();
            if (!selectedList.contains(selected)) {
                selectedList.add(selected);
            }
        }
    }
    
    /**
     * Removes a locomotive from the selected list.
     */
    private void removeLocomotive() {
        Locomotive selected = selectedLocomotivesList.getSelectionModel().getSelectedItem();
        if (selected != null) {
            selectedLocomotivesList.getItems().remove(selected);
        }
    }
    
    /**
     * Adds a wagon to the selected list.
     */
    private void addWagon() {
        Wagon selected = availableWagonsList.getSelectionModel().getSelectedItem();
        if (selected != null) {
            ObservableList<Wagon> selectedList = selectedWagonsList.getItems();
            if (!selectedList.contains(selected)) {
                selectedList.add(selected);
            }
        }
    }
    
    /**
     * Removes a wagon from the selected list.
     */
    private void removeWagon() {
        Wagon selected = selectedWagonsList.getSelectionModel().getSelectedItem();
        if (selected != null) {
            selectedWagonsList.getItems().remove(selected);
        }
    }
    
    /**
     * Adds a train to the trains list.
     */
    private void addTrain() {
        // Validate inputs
        if (trainIdField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Train ID is required.");
            return;
        }
        
        if (startStationCombo.getSelectionModel().getSelectedItem() == null ||
            endStationCombo.getSelectionModel().getSelectedItem() == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "Start and end stations are required.");
            return;
        }
        
        if (selectedLocomotivesList.getItems().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "At least one locomotive is required.");
            return;
        }
        
        if (departureDatePicker.getValue() == null || departureTimeField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Departure date and time are required.");
            return;
        }
        
        try {
            // Parse departure time
            LocalDateTime departureTime = LocalDateTime.of(
                departureDatePicker.getValue(),
                java.time.LocalTime.parse(departureTimeField.getText(), DateTimeFormatter.ofPattern("HH:mm"))
            );
            
            // Create route (simple route with start and end)
            Facility startStation = startStationCombo.getSelectionModel().getSelectedItem();
            Facility endStation = endStationCombo.getSelectionModel().getSelectedItem();
            List<Facility> route = Arrays.asList(startStation, endStation);
            
            // Calculate total weight
            double totalWeight = selectedWagonsList.getItems().stream()
                .mapToDouble(wagon -> wagonsLoadedCheckBox.isSelected() 
                    ? wagon.getFullyLoadedWeightKg() 
                    : wagon.getEmptyWeightKg())
                .sum();
            
            // Create train
            Train train = new Train(
                trainIdField.getText().trim(),
                route,
                route,
                new ArrayList<>(selectedLocomotivesList.getItems()),
                new ArrayList<>(selectedWagonsList.getItems()),
                departureTime,
                totalWeight
            );
            
            trains.add(train);
            
            // Clear form
            trainIdField.clear();
            startStationCombo.getSelectionModel().clearSelection();
            endStationCombo.getSelectionModel().clearSelection();
            departureDatePicker.setValue(null);
            departureTimeField.clear();
            selectedLocomotivesList.getItems().clear();
            selectedWagonsList.getItems().clear();
            wagonsLoadedCheckBox.setSelected(false);
            
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to create train: " + e.getMessage());
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
        statusLabel.setStyle("-fx-text-fill: #A1A1AA; -fx-font-size: 13px;");
        
        Service<DispatchReport> service = new Service<DispatchReport>() {
            @Override
            protected Task<DispatchReport> createTask() {
                return new Task<DispatchReport>() {
                    @Override
                    protected DispatchReport call() throws Exception {
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
            String successMsg = String.format("✓ Dispatch completed successfully\n%d trains scheduled\n%d conflict%s resolved automatically",
                summary.getTotalTrainsDispatched(),
                summary.getNumberOfResolvableConflicts(),
                summary.getNumberOfResolvableConflicts() != 1 ? "s" : "");
            statusLabel.setText(successMsg);
            statusLabel.setStyle("-fx-text-fill: #10B981; -fx-font-size: 13px; -fx-font-weight: 500;");
            
            mainTabPane.getSelectionModel().select(1); // Switch to results tab
        });
        
        service.setOnFailed(e -> {
            dispatchButton.setDisable(false);
            progressIndicator.setVisible(false);
            statusLabel.setText("✗ Dispatch failed!");
            statusLabel.setStyle("-fx-text-fill: #EF4444; -fx-font-size: 13px; -fx-font-weight: 500;");
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
            summaryTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: 600; -fx-text-fill: #E4E4E7;");
            
            HBox summaryStats = new HBox(30);
            addStatItem(summaryStats, "Total Trains", String.valueOf(summary.getTotalTrainsDispatched()));
            addStatItem(summaryStats, "Conflicts", String.valueOf(summary.getNumberOfConflicts()));
            addStatItem(summaryStats, "Resolved", String.valueOf(summary.getNumberOfResolvableConflicts()));
            addStatItem(summaryStats, "Unsolvable", String.valueOf(summary.getNumberOfUnsolvableConflicts()));
            
            summaryBox.getChildren().addAll(summaryTitle, summaryStats);
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
                conflictsTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: 600; -fx-text-fill: #E4E4E7;");
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
        trainHeader.setStyle("-fx-font-size: 16px; -fx-font-weight: 600; -fx-text-fill: #E4E4E7;");
        
        Separator separator = new Separator();
        separator.setStyle("-fx-background-color: rgba(99, 102, 241, 0.3);");
        
        // Route
        StringBuilder routeStr = new StringBuilder();
        for (Facility station : schedule.getSelectedStations()) {
            if (routeStr.length() > 0) routeStr.append(" → ");
            routeStr.append(station.getId());
        }
        Label routeLabel = new Label("Route: " + routeStr.toString());
        routeLabel.setStyle("-fx-text-fill: #A1A1AA; -fx-font-size: 13px;");
        
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
        departureLabel.setStyle("-fx-text-fill: #A1A1AA; -fx-font-size: 13px;");
        
        Label arrivalLabel = new Label("Arrival: " + arrival);
        arrivalLabel.setStyle("-fx-text-fill: #A1A1AA; -fx-font-size: 13px;");
        
        Label timeLabel = new Label("Total time: " + totalTime);
        timeLabel.setStyle("-fx-text-fill: #10B981; -fx-font-size: 13px; -fx-font-weight: 500;");
        
        // Stations list
        VBox stationsBox = new VBox(5);
        Label stationsLabel = new Label("Stations:");
        stationsLabel.setStyle("-fx-text-fill: #A1A1AA; -fx-font-size: 13px; -fx-font-weight: 500;");
        stationsBox.getChildren().add(stationsLabel);
        
        for (DispatchReport.TrainSchedule.StationETA routeETA : schedule.getRouteWithETAs()) {
            String stationTime = routeETA.getEta().format(DateTimeFormatter.ofPattern("HH:mm"));
            Label stationLabel = new Label("• " + routeETA.getStation().getId() + " " + stationTime);
            stationLabel.setStyle("-fx-text-fill: #E4E4E7; -fx-font-size: 12px;");
            stationsBox.getChildren().add(stationLabel);
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
        warningLabel.setStyle("-fx-text-fill: #F59E0B; -fx-font-size: 13px; -fx-font-weight: 600;");
        
        Label segmentLabel = new Label("Segment: " + conflict.getSegmentId());
        segmentLabel.setStyle("-fx-text-fill: #A1A1AA; -fx-font-size: 12px;");
        
        Label trainsLabel = new Label("Trains: " + conflict.getTrain1Id() + " / " + conflict.getTrain2Id());
        trainsLabel.setStyle("-fx-text-fill: #A1A1AA; -fx-font-size: 12px;");
        
        Label resolutionLabel = new Label("Resolution: " + conflict.getDescription());
        resolutionLabel.setStyle("-fx-text-fill: #10B981; -fx-font-size: 12px;");
        
        box.getChildren().addAll(warningLabel, segmentLabel, trainsLabel, resolutionLabel);
        
        return box;
    }
    
    /**
     * Adds a stat item to the summary stats box.
     */
    private void addStatItem(HBox box, String label, String value) {
        VBox statBox = new VBox(3);
        Label labelLabel = new Label(label);
        labelLabel.setStyle("-fx-text-fill: #A1A1AA; -fx-font-size: 12px;");
        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-text-fill: #E4E4E7; -fx-font-size: 16px; -fx-font-weight: 600;");
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
    
    // Custom list cell renderers with professional formatting
    private static class LocomotiveListCell extends ListCell<Locomotive> {
        @Override
        protected void updateItem(Locomotive item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
            } else {
                setText(String.format("%s — %s\nPower: %.0f kW | Max speed: %.0f km/h", 
                    item.getId(), item.getType(), item.getPowerKw(), item.getMaxSpeedKmH()));
                setStyle("-fx-text-fill: #000000; -fx-font-size: 12px;");
            }
        }
    }
    
    private static class WagonListCell extends ListCell<Wagon> {
        @Override
        protected void updateItem(Wagon item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
            } else {
                setText(String.format("%s — %s\nEmpty: %.0f kg | Loaded: %.0f kg", 
                    item.getWagonId(), item.getType(), 
                    item.getEmptyWeightKg(), item.getFullyLoadedWeightKg()));
                setStyle("-fx-text-fill: #000000; -fx-font-size: 12px;");
            }
        }
    }
    
    private static class TrainListCell extends ListCell<Train> {
        @Override
        protected void updateItem(Train item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
            } else {
                StringBuilder routeStr = new StringBuilder();
                for (Facility station : item.getRoute()) {
                    if (routeStr.length() > 0) routeStr.append(" → ");
                    routeStr.append(station.getId());
                }
                setText(String.format("%s: %s\n%d locomotive%s, %d wagon%s | Departure: %s", 
                    item.getTrainId(), routeStr.toString(), 
                    item.getLocomotives().size(), item.getLocomotives().size() != 1 ? "s" : "",
                    item.getWagons().size(), item.getWagons().size() != 1 ? "s" : "",
                    item.getDeparturePlannedDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))));
                setStyle("-fx-text-fill: #E4E4E7; -fx-font-size: 12px;");
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
}
