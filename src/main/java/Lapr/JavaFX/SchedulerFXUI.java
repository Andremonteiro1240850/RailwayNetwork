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
        endStationCombo.setStyle("-fx-background-color: #0D1117; -fx-text-fill: white;");
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
        startStationCombo.setStyle("-fx-background-color: #0D1117; -fx-text-fill: white;");
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
        
        box.getChildren().addAll(sectionLabel, grid);
        
        return box;
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
                      "-fx-border-width: 1;");
        // Ajuste automático de colunas - usar CONSTRAINED para evitar coluna vazia
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setFixedCellSize(35); // Altura menor para ver mais linhas
        
        // ID Column
        TableColumn<Locomotive, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(loco -> new javafx.beans.property.SimpleStringProperty(loco.getValue().getId()));
        idCol.setMinWidth(60);
        idCol.setPrefWidth(70);
        idCol.setSortable(true);
        idCol.setStyle("-fx-font-family: 'Segoe UI', 'Roboto', sans-serif; " +
                      "-fx-font-size: 11px; " +
                      "-fx-font-weight: 700; " +
                      "-fx-text-fill: #FFFFFF; " +
                      "-fx-background-color: #2D3748; " +
                      "-fx-padding: 6 8;");
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
                            "-fx-padding: 6 8; " +
                            "-fx-alignment: CENTER-LEFT;");
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
        typeCol.setMinWidth(80);
        typeCol.setPrefWidth(90);
        typeCol.setSortable(true);
        typeCol.setStyle("-fx-font-family: 'Segoe UI', 'Roboto', sans-serif; " +
                        "-fx-font-size: 11px; " +
                        "-fx-font-weight: 700; " +
                        "-fx-text-fill: #FFFFFF; " +
                        "-fx-background-color: #2D3748; " +
                        "-fx-padding: 6 8;");
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
                            "-fx-padding: 6 8; " +
                            "-fx-alignment: CENTER-LEFT;");
                }
            }
        });
        
        // Power Column
        TableColumn<Locomotive, Double> powerCol = new TableColumn<>("Power (kW)");
        powerCol.setCellValueFactory(loco -> new javafx.beans.property.SimpleDoubleProperty(loco.getValue().getPowerKw()).asObject());
        powerCol.setMinWidth(90);
        powerCol.setPrefWidth(100);
        powerCol.setSortable(true);
        powerCol.setStyle("-fx-font-family: 'Segoe UI', 'Roboto', sans-serif; " +
                         "-fx-font-size: 11px; " +
                         "-fx-font-weight: 700; " +
                         "-fx-text-fill: #FFFFFF; " +
                         "-fx-background-color: #2D3748; " +
                         "-fx-padding: 6 8;");
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
                            "-fx-padding: 6 8; " +
                            "-fx-alignment: CENTER-RIGHT;");
                }
            }
        });
        
        // Max Speed Column
        TableColumn<Locomotive, Double> speedCol = new TableColumn<>("Max Speed (km/h)");
        speedCol.setCellValueFactory(loco -> new javafx.beans.property.SimpleDoubleProperty(loco.getValue().getMaxSpeedKmH()).asObject());
        speedCol.setMinWidth(110);
        speedCol.setPrefWidth(120);
        speedCol.setSortable(true);
        speedCol.setStyle("-fx-font-family: 'Segoe UI', 'Roboto', sans-serif; " +
                         "-fx-font-size: 11px; " +
                         "-fx-font-weight: 700; " +
                         "-fx-text-fill: #FFFFFF; " +
                         "-fx-background-color: #2D3748; " +
                         "-fx-padding: 6 8;");
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
                            "-fx-padding: 6 8; " +
                            "-fx-alignment: CENTER-RIGHT;");
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
                      "-fx-border-width: 1;");
        // Ajuste automático de colunas - usar CONSTRAINED para evitar coluna vazia
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setFixedCellSize(35); // Altura menor para ver mais linhas
        
        // ID Column
        TableColumn<Wagon, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(wagon -> new javafx.beans.property.SimpleStringProperty(wagon.getValue().getWagonId()));
        idCol.setMinWidth(60);
        idCol.setPrefWidth(70);
        idCol.setSortable(true);
        idCol.setStyle("-fx-font-family: 'Segoe UI', 'Roboto', sans-serif; " +
                      "-fx-font-size: 11px; " +
                      "-fx-font-weight: 700; " +
                      "-fx-text-fill: #FFFFFF; " +
                      "-fx-background-color: #2D3748; " +
                      "-fx-padding: 6 8;");
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
                            "-fx-padding: 6 8; " +
                            "-fx-alignment: CENTER-LEFT;");
                }
            }
        });
        
        // Type Column - texto completo sempre visível
        TableColumn<Wagon, String> typeCol = new TableColumn<>("Cargo Type");
        typeCol.setCellValueFactory(wagon -> new javafx.beans.property.SimpleStringProperty(wagon.getValue().getType()));
        typeCol.setMinWidth(120);
        typeCol.setPrefWidth(150);
        typeCol.setSortable(true);
        typeCol.setStyle("-fx-font-family: 'Segoe UI', 'Roboto', sans-serif; " +
                        "-fx-font-size: 11px; " +
                        "-fx-font-weight: 700; " +
                        "-fx-text-fill: #FFFFFF; " +
                        "-fx-background-color: #2D3748; " +
                        "-fx-padding: 6 8;");
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
                            "-fx-padding: 6 8; " +
                            "-fx-alignment: CENTER-LEFT;");
                }
            }
        });
        
        // Empty Weight Column
        TableColumn<Wagon, Double> emptyWeightCol = new TableColumn<>("Empty (kg)");
        emptyWeightCol.setCellValueFactory(wagon -> new javafx.beans.property.SimpleDoubleProperty(wagon.getValue().getEmptyWeightKg()).asObject());
        emptyWeightCol.setMinWidth(90);
        emptyWeightCol.setPrefWidth(100);
        emptyWeightCol.setSortable(true);
        emptyWeightCol.setStyle("-fx-font-family: 'Segoe UI', 'Roboto', sans-serif; " +
                               "-fx-font-size: 11px; " +
                               "-fx-font-weight: 700; " +
                               "-fx-text-fill: #FFFFFF; " +
                               "-fx-background-color: #2D3748; " +
                               "-fx-padding: 6 8;");
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
                            "-fx-padding: 6 8; " +
                            "-fx-alignment: CENTER-RIGHT;");
                }
            }
        });
        
        // Loaded Weight Column
        TableColumn<Wagon, Double> loadedWeightCol = new TableColumn<>("Loaded (kg)");
        loadedWeightCol.setCellValueFactory(wagon -> new javafx.beans.property.SimpleDoubleProperty(wagon.getValue().getFullyLoadedWeightKg()).asObject());
        loadedWeightCol.setMinWidth(90);
        loadedWeightCol.setPrefWidth(100);
        loadedWeightCol.setSortable(true);
        loadedWeightCol.setStyle("-fx-font-family: 'Segoe UI', 'Roboto', sans-serif; " +
                                 "-fx-font-size: 11px; " +
                                 "-fx-font-weight: 700; " +
                                 "-fx-text-fill: #FFFFFF; " +
                                 "-fx-background-color: #2D3748; " +
                                 "-fx-padding: 6 8;");
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
                            "-fx-padding: 6 8; " +
                            "-fx-alignment: CENTER-RIGHT;");
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
        resultsScrollPane.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
        
        resultsContent = new VBox(20);
        resultsContent.setPadding(new Insets(10));
        
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
        
        if (selectedLocomotivesTable.getItems().isEmpty()) {
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
            // The SchedulerService will automatically find the fastest path if stations are not directly connected
            Facility startStation = startStationCombo.getSelectionModel().getSelectedItem();
            Facility endStation = endStationCombo.getSelectionModel().getSelectedItem();
            List<Facility> route = Arrays.asList(startStation, endStation);
            List<Facility> selectedStations = Arrays.asList(startStation, endStation); // User-selected stations
            
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
            
            trains.add(train);
            
            // Clear form
            trainIdField.clear();
            startStationCombo.getSelectionModel().clearSelection();
            endStationCombo.getSelectionModel().clearSelection();
            departureDatePicker.setValue(null);
            departureTimeField.clear();
            selectedLocomotivesTable.getItems().clear();
            selectedWagonsTable.getItems().clear();
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
                String successMsg = String.format("✓ Dispatch completed successfully\n%d train%s scheduled\n%d conflict%s resolved automatically",
                    summary.getTotalTrainsDispatched(),
                    summary.getTotalTrainsDispatched() != 1 ? "s" : "",
                    summary.getNumberOfResolvableConflicts(),
                    summary.getNumberOfResolvableConflicts() != 1 ? "s" : "");
                statusLabel.setText(successMsg);
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
        
        // Route - show full route with all intermediate stations
        StringBuilder routeStr = new StringBuilder();
        List<Facility> stationsToShow = schedule.getRouteWithETAs().stream()
            .map(eta -> eta.getStation())
            .collect(java.util.stream.Collectors.toList());
        
        for (Facility station : stationsToShow) {
            if (routeStr.length() > 0) routeStr.append(" → ");
            routeStr.append(station.getId());
        }
        
        // If route is long, show simplified version: start -> ... -> end
        String routeDisplay = routeStr.toString();
        if (stationsToShow.size() > 4) {
            routeDisplay = stationsToShow.get(0).getId() + " → ... (" + 
                          (stationsToShow.size() - 2) + " stations) → " + 
                          stationsToShow.get(stationsToShow.size() - 1).getId();
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
        
        // Stations list
        VBox stationsBox = new VBox(5);
        Label stationsLabel = new Label("Stations:");
        stationsLabel.setStyle("-fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: 500;");
        stationsBox.getChildren().add(stationsLabel);
        
        for (DispatchReport.TrainSchedule.StationETA routeETA : schedule.getRouteWithETAs()) {
            String stationTime = routeETA.getEta().format(DateTimeFormatter.ofPattern("HH:mm"));
            Label stationLabel = new Label("• " + routeETA.getStation().getId() + " " + stationTime);
            stationLabel.setStyle("-fx-text-fill: white; -fx-font-size: 12px;");
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
                setStyle("-fx-text-fill: #000000; -fx-font-size: 12px;");
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
