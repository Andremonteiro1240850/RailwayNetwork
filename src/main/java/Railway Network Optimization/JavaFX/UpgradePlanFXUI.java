package JavaFX;

import Controller.UpgradePlanController;
import Domain.CycleDetection;
import Domain.DataLoader;
import Domain.Line;
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
import javafx.collections.ObservableList;
import javafx.collections.FXCollections;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.scene.web.WebView;
import javafx.scene.web.WebEngine;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

import java.util.*;
import java.util.stream.Collectors;

/**
 * JavaFX UI for the Directed Line Upgrade Plan functionality (USEI11).
 * Provides a user interface to calculate and display upgrade order or cycle information.
 * 
 * @author Development Team
 * @version 1.0
 */
public class UpgradePlanFXUI {
    private UpgradePlanController controller;
    private Stage stage;
    private Button calculateButton;
    private TextArea technicalLogArea;
    private ProgressIndicator progressIndicator;
    private Label statusLabel;
    private TabPane resultTabPane;
    
    // Overview tab components
    private VBox overviewTab;
    private Label totalNodesLabel;
    private Label totalEdgesLabel;
    private Label cyclesDetectedLabel;
    private Label affectedStationsLabel;
    private Label largestCycleLabel;
    private Label mostProblematicStationLabel;
    
    // Tables
    private TableView<StationData> affectedStationsTable;
    private TableView<CycleData> cyclesTable;
    private TableView<EdgeData> criticalEdgesTable;
    
    // Graph visualization
    private WebView graphWebView;
    private WebEngine webEngine;
    private Button exportDOTButton;
    private Button exportSVGButton;
    private Button refreshGraphButton;
    
    private CycleDetection.CycleInfo currentCycleInfo;
    private Map<String, Integer> currentStationDegrees;
    private Map<String, List<Integer>> currentStationCycles;
    private List<List<Station>> currentCycles;
    private UpgradePlanController.UpgradePlanResult currentResult;
    private String rawTechnicalLog;
    
    // Default file paths
    private static final String DEFAULT_STATIONS_PATH = "train_station_dataset/Sprint3/stations.csv";
    private static final String DEFAULT_EDGES_PATH = "train_station_dataset/Sprint3/lines.csv";
    
    /**
     * Constructs a new UpgradePlanFXUI.
     * The controller will be created when data is loaded.
     */
    public UpgradePlanFXUI() {
        this.controller = null;
    }
    
    /**
     * Shows the upgrade plan UI window.
     */
    public void show() {
        stage = new Stage();
        stage.setTitle("Directed Line Upgrade Plan");
        
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #1B2533;");
        root.setPadding(new Insets(20));
        
        // Top: Title and button
        VBox topBox = new VBox(15);
        topBox.setAlignment(Pos.CENTER);
        topBox.setPadding(new Insets(0, 0, 20, 0));
        
        Label titleLabel = new Label("Directed Line Upgrade Plan");
        titleLabel.setStyle("-fx-font-family: 'Segoe UI', 'Roboto', sans-serif; " +
                           "-fx-font-size: 32px; " +
                           "-fx-font-weight: 700; " +
                           "-fx-text-fill: #E2E8F0; " +
                           "-fx-letter-spacing: 0.5px;");
        
        Label descriptionLabel = new Label(
            "Calculate the upgrade order for Belgian railway stations based on directed dependencies"
        );
        descriptionLabel.setStyle("-fx-font-family: 'Segoe UI', 'Roboto', sans-serif; " +
                                  "-fx-font-size: 13px; " +
                                  "-fx-text-fill: #94A3B8;");
        descriptionLabel.setWrapText(true);
        
        calculateButton = new Button("Calculate Upgrade Order");
        calculateButton.setPrefWidth(300);
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
        createAffectedStationsTab();
        createCyclesTab();
        createCriticalEdgesTab();
        createGraphVisualizationTab();
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
        bottomContainer.getChildren().addAll(bottomBox, closeButton);
        
        root.setBottom(bottomContainer);
        
        Scene scene = new Scene(root, 1200, 800);
        
        // Add CSS for table column headers
        String tableHeaderCSS = 
            ".table-view .column-header .label { " +
            "    -fx-text-fill: #FFFFFF !important; " +
            "    -fx-font-weight: 700 !important; " +
            "    -fx-font-size: 13px !important; " +
            "    -fx-font-family: 'Segoe UI', 'Roboto', sans-serif !important; " +
            "} " +
            ".table-view .column-header { " +
            "    -fx-background-color: #2D3748; " +
            "} " +
            ".table-view .column-header-background { " +
            "    -fx-background-color: #2D3748; " +
            "}";
        scene.getStylesheets().add("data:text/css;charset=utf-8," + tableHeaderCSS);
        
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
        
        // Header section with title and description
        VBox headerBox = new VBox(8);
        headerBox.setPadding(new Insets(0, 0, 20, 0));
        
        Label titleLabel = new Label("📊 Overview");
        titleLabel.setStyle("-fx-font-family: 'Segoe UI', 'Roboto', sans-serif; " +
                           "-fx-font-size: 28px; " +
                           "-fx-font-weight: 700; " +
                           "-fx-text-fill: #E2E8F0;");
        
        Label descriptionLabel = new Label("Key metrics and statistics from the network analysis");
        descriptionLabel.setStyle("-fx-font-family: 'Segoe UI', 'Roboto', sans-serif; " +
                                 "-fx-font-size: 14px; " +
                                 "-fx-text-fill: #94A3B8;");
        
        headerBox.getChildren().addAll(titleLabel, descriptionLabel);
        
        // Grid for metric cards with more spacing
        GridPane cardsGrid = new GridPane();
        cardsGrid.setHgap(30);
        cardsGrid.setVgap(30);
        cardsGrid.setAlignment(Pos.CENTER);
        cardsGrid.setPadding(new Insets(20, 0, 20, 0));
        
        // Card 1: Total Nodes
        VBox card1 = createMetricCard("Total Nodes", "0", "#6366F1");
        totalNodesLabel = (Label) ((VBox) card1.getChildren().get(1)).getChildren().get(0);
        cardsGrid.add(card1, 0, 0);
        
        // Card 2: Total Edges
        VBox card2 = createMetricCard("Total Edges", "0", "#8B5CF6");
        totalEdgesLabel = (Label) ((VBox) card2.getChildren().get(1)).getChildren().get(0);
        cardsGrid.add(card2, 1, 0);
        
        // Card 3: Cycles Detected
        VBox card3 = createMetricCard("Cycles Detected", "0", "#EC4899");
        cyclesDetectedLabel = (Label) ((VBox) card3.getChildren().get(1)).getChildren().get(0);
        cardsGrid.add(card3, 2, 0);
        
        // Card 4: Affected Stations
        VBox card4 = createMetricCard("Affected Stations", "0", "#F59E0B");
        affectedStationsLabel = (Label) ((VBox) card4.getChildren().get(1)).getChildren().get(0);
        cardsGrid.add(card4, 0, 1);
        
        // Card 5: Largest Cycle
        VBox card5 = createMetricCard("Largest Cycle", "N/A", "#10B981");
        largestCycleLabel = (Label) ((VBox) card5.getChildren().get(1)).getChildren().get(0);
        cardsGrid.add(card5, 1, 1);
        
        // Card 6: Most Problematic Station
        VBox card6 = createMetricCard("Most Problematic", "N/A", "#EF4444");
        mostProblematicStationLabel = (Label) ((VBox) card6.getChildren().get(1)).getChildren().get(0);
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
        return createMetricCard(title, value, color, 24);
    }
    
    /**
     * Creates a metric card with rounded corners and custom font size.
     */
    private VBox createMetricCard(String title, String value, String color, double fontSize) {
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
                           "-fx-font-size: " + fontSize + "px; " +
                           "-fx-font-weight: 700; " +
                           "-fx-text-fill: " + color + "; " +
                           "-fx-letter-spacing: -0.5px; " +
                           "-fx-wrap-text: true;");
        valueBox.getChildren().add(valueLabel);
        
        card.getChildren().addAll(titleLabel, valueBox);
        return card;
    }
    
    /**
     * Helper method to format table columns with consistent styling.
     */
    private <T, S> void formatTableColumn(TableColumn<T, S> column, String headerText, boolean rightAlign) {
        column.setText(headerText);
        column.setSortable(true);
        column.setResizable(true);
        
        // Header style - using bright white for maximum visibility
        column.setStyle("-fx-font-family: 'Segoe UI', 'Roboto', sans-serif; " +
                       "-fx-font-size: 13px; " +
                       "-fx-font-weight: 700; " +
                       "-fx-text-fill: #FFFFFF; " +
                       "-fx-background-color: #2D3748; " +
                       "-fx-padding: 10 12;");
        
        // Cell style
        column.setCellFactory(tc -> new javafx.scene.control.TableCell<T, S>() {
            @Override
            protected void updateItem(S item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item.toString());
                    setStyle("-fx-font-family: 'Segoe UI', 'Roboto', sans-serif; " +
                            "-fx-font-size: 13px; " +
                            "-fx-text-fill: #E2E8F0; " +
                            "-fx-padding: 8 12; " +
                            "-fx-alignment: " + (rightAlign ? "CENTER-RIGHT" : "CENTER-LEFT") + ";");
                }
            }
        });
    }
    
    /**
     * Creates the Affected Stations tab with table.
     */
    private void createAffectedStationsTab() {
        VBox tabContent = new VBox(15);
        tabContent.setPadding(new Insets(20));
        tabContent.setStyle("-fx-background-color: #1B2533;");
        
        Label titleLabel = new Label("Affected Stations");
        titleLabel.setStyle("-fx-font-family: 'Segoe UI', 'Roboto', sans-serif; " +
                           "-fx-font-size: 18px; " +
                           "-fx-font-weight: 600; " +
                           "-fx-text-fill: #E2E8F0;");
        
        affectedStationsTable = new TableView<>();
        affectedStationsTable.setStyle("-fx-background-color: #243447; " +
                                      "-fx-table-cell-border-color: rgba(99, 102, 241, 0.1); " +
                                      "-fx-border-color: rgba(99, 102, 241, 0.2); " +
                                      "-fx-border-width: 1px; " +
                                      "-fx-border-radius: 8px; " +
                                      "-fx-background-radius: 8px;");
        affectedStationsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        // Enable row striping
        affectedStationsTable.setRowFactory(tv -> new javafx.scene.control.TableRow<StationData>() {
            @Override
            protected void updateItem(StationData item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setStyle("");
                } else {
                    int index = getIndex();
                    if (index % 2 == 0) {
                        setStyle("-fx-background-color: #243447;");
                    } else {
                        setStyle("-fx-background-color: #1E293B;");
                    }
                }
            }
        });
        
        TableColumn<StationData, String> idCol = new TableColumn<>();
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        formatTableColumn(idCol, "ID", false);
        idCol.setMinWidth(80);
        idCol.setMaxWidth(100);
        
        TableColumn<StationData, String> nameCol = new TableColumn<>();
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        formatTableColumn(nameCol, "Name", false);
        nameCol.setMinWidth(150);
        
        TableColumn<StationData, Integer> inCol = new TableColumn<>();
        inCol.setCellValueFactory(new PropertyValueFactory<>("inDegree"));
        formatTableColumn(inCol, "In", true);
        inCol.setMinWidth(60);
        inCol.setMaxWidth(80);
        
        TableColumn<StationData, Integer> outCol = new TableColumn<>();
        outCol.setCellValueFactory(new PropertyValueFactory<>("outDegree"));
        formatTableColumn(outCol, "Out", true);
        outCol.setMinWidth(60);
        outCol.setMaxWidth(80);
        
        TableColumn<StationData, Integer> totalCol = new TableColumn<>();
        totalCol.setCellValueFactory(new PropertyValueFactory<>("totalDegree"));
        formatTableColumn(totalCol, "Total", true);
        totalCol.setMinWidth(70);
        totalCol.setMaxWidth(90);
        
        TableColumn<StationData, String> cyclesCol = new TableColumn<>();
        cyclesCol.setCellValueFactory(new PropertyValueFactory<>("cycles"));
        formatTableColumn(cyclesCol, "In Cycle #", false);
        cyclesCol.setMinWidth(150);
        
        affectedStationsTable.getColumns().addAll(idCol, nameCol, inCol, outCol, totalCol, cyclesCol);
        
        Label placeholder = new Label("No data available. Click 'Calculate Upgrade Order' to analyze.");
        placeholder.setStyle("-fx-font-family: 'Segoe UI', 'Roboto', sans-serif; " +
                            "-fx-font-size: 14px; " +
                            "-fx-text-fill: #94A3B8;");
        affectedStationsTable.setPlaceholder(placeholder);
        
        VBox.setVgrow(affectedStationsTable, Priority.ALWAYS);
        tabContent.getChildren().addAll(titleLabel, affectedStationsTable);
        
        Tab tab = new Tab("Affected Stations", tabContent);
        tab.setStyle("-fx-font-family: 'Segoe UI', 'Roboto', sans-serif; " +
                    "-fx-font-size: 13px;");
        resultTabPane.getTabs().add(tab);
    }
    
    /**
     * Creates the Cycles tab with table.
     */
    private void createCyclesTab() {
        VBox tabContent = new VBox(15);
        tabContent.setPadding(new Insets(20));
        tabContent.setStyle("-fx-background-color: #1B2533;");
        
        Label titleLabel = new Label("Cycles");
        titleLabel.setStyle("-fx-font-family: 'Segoe UI', 'Roboto', sans-serif; " +
                           "-fx-font-size: 18px; " +
                           "-fx-font-weight: 600; " +
                           "-fx-text-fill: #E2E8F0;");
        
        cyclesTable = new TableView<>();
        cyclesTable.setStyle("-fx-background-color: #243447; " +
                            "-fx-table-cell-border-color: rgba(99, 102, 241, 0.1); " +
                            "-fx-border-color: rgba(99, 102, 241, 0.2); " +
                            "-fx-border-width: 1px; " +
                            "-fx-border-radius: 8px; " +
                            "-fx-background-radius: 8px;");
        // Use UNCONSTRAINED_RESIZE_POLICY to allow columns to expand and show full content
        cyclesTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        
        // Enable row striping
        cyclesTable.setRowFactory(tv -> new javafx.scene.control.TableRow<CycleData>() {
            @Override
            protected void updateItem(CycleData item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setStyle("");
                } else {
                    int index = getIndex();
                    if (index % 2 == 0) {
                        setStyle("-fx-background-color: #243447;");
                    } else {
                        setStyle("-fx-background-color: #1E293B;");
                    }
                }
            }
        });
        
        TableColumn<CycleData, Integer> numCol = new TableColumn<>();
        numCol.setCellValueFactory(new PropertyValueFactory<>("cycleNumber"));
        formatTableColumn(numCol, "Cycle #", true);
        numCol.setMinWidth(80);
        numCol.setMaxWidth(100);
        
        TableColumn<CycleData, Integer> sizeCol = new TableColumn<>();
        sizeCol.setCellValueFactory(new PropertyValueFactory<>("size"));
        formatTableColumn(sizeCol, "Size", true);
        sizeCol.setMinWidth(70);
        sizeCol.setMaxWidth(90);
        
        TableColumn<CycleData, String> pathCol = new TableColumn<>();
        pathCol.setCellValueFactory(new PropertyValueFactory<>("path"));
        pathCol.setText("Path");
        pathCol.setSortable(true);
        pathCol.setResizable(true);
        pathCol.setStyle("-fx-font-family: 'Segoe UI', 'Roboto', sans-serif; " +
                        "-fx-font-size: 13px; " +
                        "-fx-font-weight: 600; " +
                        "-fx-text-fill: #FFFFFF; " +
                        "-fx-background-color: #2D3748; " +
                        "-fx-padding: 10 12;");
        // Custom cell factory to show full path without truncation
        pathCol.setCellFactory(tc -> new javafx.scene.control.TableCell<CycleData, String>() {
            private final Label label = new Label();
            
            {
                label.setStyle("-fx-font-family: 'Consolas', 'Monaco', 'Courier New', monospace; " +
                              "-fx-font-size: 12px; " +
                              "-fx-text-fill: #E2E8F0; " +
                              "-fx-padding: 0; " +
                              "-fx-alignment: CENTER-LEFT;");
                label.setWrapText(false);
                label.setMaxWidth(Double.MAX_VALUE);
                setGraphic(label);
            }
            
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    label.setText(null);
                    setGraphic(null);
                } else {
                    label.setText(item); // Show full path - no truncation
                    setGraphic(label);
                    setTooltip(new Tooltip(item)); // Show full path on hover
                }
            }
        });
        pathCol.setMinWidth(800);
        pathCol.setPrefWidth(1500);
        pathCol.setMaxWidth(Double.MAX_VALUE);
        
        TableColumn<CycleData, String> impactCol = new TableColumn<>();
        impactCol.setCellValueFactory(new PropertyValueFactory<>("impact"));
        // Custom cell factory for impact with color coding
        impactCol.setText("Impact");
        impactCol.setSortable(true);
        impactCol.setResizable(true);
        impactCol.setStyle("-fx-font-family: 'Segoe UI', 'Roboto', sans-serif; " +
                          "-fx-font-size: 13px; " +
                          "-fx-font-weight: 700; " +
                          "-fx-text-fill: #FFFFFF; " +
                          "-fx-background-color: #2D3748; " +
                          "-fx-padding: 10 12;");
        impactCol.setCellFactory(tc -> new javafx.scene.control.TableCell<CycleData, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    String color;
                    String upper = item.toUpperCase();
                    if ("HIGH".equals(upper)) {
                        color = "#EF4444";
                    } else if ("MEDIUM".equals(upper)) {
                        color = "#F59E0B";
                    } else if ("LOW".equals(upper)) {
                        color = "#10B981";
                    } else {
                        color = "#E2E8F0";
                    }
                    setStyle("-fx-font-family: 'Segoe UI', 'Roboto', sans-serif; " +
                            "-fx-font-size: 13px; " +
                            "-fx-font-weight: 600; " +
                            "-fx-text-fill: " + color + "; " +
                            "-fx-padding: 8 12; " +
                            "-fx-alignment: CENTER;");
                }
            }
        });
        impactCol.setMinWidth(100);
        impactCol.setMaxWidth(120);
        
        cyclesTable.getColumns().addAll(numCol, sizeCol, pathCol, impactCol);
        
        Label placeholder = new Label("No data available. Click 'Calculate Upgrade Order' to analyze.");
        placeholder.setStyle("-fx-font-family: 'Segoe UI', 'Roboto', sans-serif; " +
                            "-fx-font-size: 14px; " +
                            "-fx-text-fill: #94A3B8;");
        cyclesTable.setPlaceholder(placeholder);
        
        VBox.setVgrow(cyclesTable, Priority.ALWAYS);
        tabContent.getChildren().addAll(titleLabel, cyclesTable);
        
        Tab tab = new Tab("Cycles", tabContent);
        tab.setStyle("-fx-font-family: 'Segoe UI', 'Roboto', sans-serif; " +
                    "-fx-font-size: 13px;");
        resultTabPane.getTabs().add(tab);
    }
    
    /**
     * Creates the Critical Edges tab with table.
     */
    private void createCriticalEdgesTab() {
        VBox tabContent = new VBox(15);
        tabContent.setPadding(new Insets(20));
        tabContent.setStyle("-fx-background-color: #1B2533;");
        
        Label titleLabel = new Label("Critical Edges");
        titleLabel.setStyle("-fx-font-family: 'Segoe UI', 'Roboto', sans-serif; " +
                           "-fx-font-size: 18px; " +
                           "-fx-font-weight: 600; " +
                           "-fx-text-fill: #E2E8F0;");
        
        criticalEdgesTable = new TableView<>();
        criticalEdgesTable.setStyle("-fx-background-color: #243447; " +
                                    "-fx-table-cell-border-color: rgba(99, 102, 241, 0.1); " +
                                    "-fx-border-color: rgba(99, 102, 241, 0.2); " +
                                    "-fx-border-width: 1px; " +
                                    "-fx-border-radius: 8px; " +
                                    "-fx-background-radius: 8px;");
        criticalEdgesTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        // Enable row striping
        criticalEdgesTable.setRowFactory(tv -> new javafx.scene.control.TableRow<EdgeData>() {
            @Override
            protected void updateItem(EdgeData item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setStyle("");
                } else {
                    int index = getIndex();
                    if (index % 2 == 0) {
                        setStyle("-fx-background-color: #243447;");
                    } else {
                        setStyle("-fx-background-color: #1E293B;");
                    }
                }
            }
        });
        
        TableColumn<EdgeData, Integer> rankCol = new TableColumn<>();
        rankCol.setCellValueFactory(new PropertyValueFactory<>("rank"));
        formatTableColumn(rankCol, "Rank", true);
        rankCol.setMinWidth(70);
        rankCol.setMaxWidth(90);
        
        TableColumn<EdgeData, String> edgeCol = new TableColumn<>();
        edgeCol.setCellValueFactory(new PropertyValueFactory<>("edge"));
        formatTableColumn(edgeCol, "Edge", false);
        edgeCol.setMinWidth(120);
        edgeCol.setMaxWidth(150);
        
        TableColumn<EdgeData, String> distCol = new TableColumn<>();
        distCol.setCellValueFactory(new PropertyValueFactory<>("distance"));
        formatTableColumn(distCol, "Distance", true);
        distCol.setMinWidth(100);
        distCol.setMaxWidth(120);
        
        TableColumn<EdgeData, String> cyclesCol = new TableColumn<>();
        cyclesCol.setCellValueFactory(new PropertyValueFactory<>("cycles"));
        formatTableColumn(cyclesCol, "Cycles Involved", false);
        cyclesCol.setMinWidth(150);
        
        TableColumn<EdgeData, String> recCol = new TableColumn<>();
        recCol.setCellValueFactory(new PropertyValueFactory<>("recommendation"));
        // Custom cell factory for recommendation with color coding
        recCol.setText("Recommendation");
        recCol.setSortable(true);
        recCol.setResizable(true);
        recCol.setStyle("-fx-font-family: 'Segoe UI', 'Roboto', sans-serif; " +
                       "-fx-font-size: 13px; " +
                       "-fx-font-weight: 700; " +
                       "-fx-text-fill: #FFFFFF; " +
                       "-fx-background-color: #2D3748; " +
                       "-fx-padding: 10 12;");
        recCol.setCellFactory(tc -> new javafx.scene.control.TableCell<EdgeData, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    String color;
                    String upper = item.toUpperCase();
                    if ("HIGH PRIORITY".equals(upper) || "CRITICAL".equals(upper)) {
                        color = "#EF4444";
                    } else if ("MEDIUM PRIORITY".equals(upper)) {
                        color = "#F59E0B";
                    } else if ("LOW PRIORITY".equals(upper)) {
                        color = "#10B981";
                    } else {
                        color = "#E2E8F0";
                    }
                    setStyle("-fx-font-family: 'Segoe UI', 'Roboto', sans-serif; " +
                            "-fx-font-size: 13px; " +
                            "-fx-font-weight: 600; " +
                            "-fx-text-fill: " + color + "; " +
                            "-fx-padding: 8 12; " +
                            "-fx-alignment: CENTER;");
                }
            }
        });
        recCol.setMinWidth(150);
        recCol.setMaxWidth(180);
        
        criticalEdgesTable.getColumns().addAll(rankCol, edgeCol, distCol, cyclesCol, recCol);
        
        Label placeholder = new Label("No data available. Click 'Calculate Upgrade Order' to analyze.");
        placeholder.setStyle("-fx-font-family: 'Segoe UI', 'Roboto', sans-serif; " +
                            "-fx-font-size: 14px; " +
                            "-fx-text-fill: #94A3B8;");
        criticalEdgesTable.setPlaceholder(placeholder);
        
        VBox.setVgrow(criticalEdgesTable, Priority.ALWAYS);
        tabContent.getChildren().addAll(titleLabel, criticalEdgesTable);
        
        Tab tab = new Tab("Critical Edges", tabContent);
        tab.setStyle("-fx-font-family: 'Segoe UI', 'Roboto', sans-serif; " +
                    "-fx-font-size: 13px;");
        resultTabPane.getTabs().add(tab);
    }
    
    /**
     * Creates the Graph Visualization tab.
     */
    private void createGraphVisualizationTab() {
        VBox tabContent = new VBox(15);
        tabContent.setPadding(new Insets(20));
        tabContent.setStyle("-fx-background-color: #1B2533;");
        
        Label titleLabel = new Label("Graph Visualization");
        titleLabel.setStyle("-fx-font-family: 'Segoe UI', 'Roboto', sans-serif; " +
                           "-fx-font-size: 18px; " +
                           "-fx-font-weight: 600; " +
                           "-fx-text-fill: #E2E8F0;");
        
        // Buttons
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_LEFT);
        
        refreshGraphButton = new Button("Refresh Graph");
        refreshGraphButton.setStyle("-fx-font-family: 'Segoe UI', 'Roboto', sans-serif; " +
                                   "-fx-font-size: 13px; " +
                                   "-fx-font-weight: 500; " +
                                   "-fx-background-color: #6366F1; " +
                                   "-fx-text-fill: #FFFFFF; " +
                                   "-fx-background-radius: 8px; " +
                                   "-fx-cursor: hand; " +
                                   "-fx-padding: 8 16;");
        refreshGraphButton.setOnAction(e -> refreshGraphVisualization());
        
        exportDOTButton = new Button("Export DOT");
        exportDOTButton.setStyle("-fx-font-family: 'Segoe UI', 'Roboto', sans-serif; " +
                                "-fx-font-size: 13px; " +
                                "-fx-font-weight: 500; " +
                                "-fx-background-color: #475569; " +
                                "-fx-text-fill: #E2E8F0; " +
                                "-fx-background-radius: 8px; " +
                                "-fx-cursor: hand; " +
                                "-fx-padding: 8 16;");
        exportDOTButton.setOnAction(e -> exportDOTFile());
        
        exportSVGButton = new Button("Export SVG");
        exportSVGButton.setStyle("-fx-font-family: 'Segoe UI', 'Roboto', sans-serif; " +
                               "-fx-font-size: 13px; " +
                               "-fx-font-weight: 500; " +
                               "-fx-background-color: #475569; " +
                               "-fx-text-fill: #E2E8F0; " +
                               "-fx-background-radius: 8px; " +
                               "-fx-cursor: hand; " +
                               "-fx-padding: 8 16;");
        exportSVGButton.setOnAction(e -> exportSVGFile());
        
        // Zoom controls
        Button zoomInButton = new Button("🔍+");
        zoomInButton.setStyle("-fx-font-family: 'Segoe UI', 'Roboto', sans-serif; " +
                             "-fx-font-size: 13px; " +
                             "-fx-font-weight: 500; " +
                             "-fx-background-color: #475569; " +
                             "-fx-text-fill: #E2E8F0; " +
                             "-fx-background-radius: 8px; " +
                             "-fx-cursor: hand; " +
                             "-fx-padding: 8 12;");
        zoomInButton.setTooltip(new Tooltip("Zoom In"));
        
        Button zoomOutButton = new Button("🔍-");
        zoomOutButton.setStyle("-fx-font-family: 'Segoe UI', 'Roboto', sans-serif; " +
                              "-fx-font-size: 13px; " +
                              "-fx-font-weight: 500; " +
                              "-fx-background-color: #475569; " +
                              "-fx-text-fill: #E2E8F0; " +
                              "-fx-background-radius: 8px; " +
                              "-fx-cursor: hand; " +
                              "-fx-padding: 8 12;");
        zoomOutButton.setTooltip(new Tooltip("Zoom Out"));
        
        Button zoomResetButton = new Button("🔍↺");
        zoomResetButton.setStyle("-fx-font-family: 'Segoe UI', 'Roboto', sans-serif; " +
                               "-fx-font-size: 13px; " +
                               "-fx-font-weight: 500; " +
                               "-fx-background-color: #475569; " +
                               "-fx-text-fill: #E2E8F0; " +
                               "-fx-background-radius: 8px; " +
                               "-fx-cursor: hand; " +
                               "-fx-padding: 8 12;");
        zoomResetButton.setTooltip(new Tooltip("Reset Zoom"));
        
        buttonBox.getChildren().addAll(refreshGraphButton, exportDOTButton, exportSVGButton, 
                                       new Label("  "), zoomInButton, zoomOutButton, zoomResetButton);
        
        // WebView for SVG display
        graphWebView = new WebView();
        webEngine = graphWebView.getEngine();
        graphWebView.setPrefHeight(600);
        VBox.setVgrow(graphWebView, Priority.ALWAYS);
        
        // Zoom functionality - zoom only the container, not the body
        zoomInButton.setOnAction(e -> {
            if (webEngine != null) {
                webEngine.executeScript("var container = document.getElementById('graphContainer'); " +
                                       "if (!container) return; " +
                                       "if (window.currentZoom === undefined) window.currentZoom = 1.0; " +
                                       "window.currentZoom = Math.min(window.currentZoom * 1.2, 5.0); " +
                                       "container.style.transform = 'scale(' + window.currentZoom + ')'; " +
                                       "container.style.transformOrigin = 'top left';");
            }
        });
        
        zoomOutButton.setOnAction(e -> {
            if (webEngine != null) {
                webEngine.executeScript("var container = document.getElementById('graphContainer'); " +
                                       "if (!container) return; " +
                                       "if (window.currentZoom === undefined) window.currentZoom = 1.0; " +
                                       "window.currentZoom = Math.max(window.currentZoom / 1.2, 0.2); " +
                                       "container.style.transform = 'scale(' + window.currentZoom + ')'; " +
                                       "container.style.transformOrigin = 'top left';");
            }
        });
        
        zoomResetButton.setOnAction(e -> {
            if (webEngine != null) {
                webEngine.executeScript("var container = document.getElementById('graphContainer'); " +
                                       "if (!container) return; " +
                                       "window.currentZoom = 1.0; " +
                                       "container.style.transform = 'scale(1.0)'; " +
                                       "container.style.transformOrigin = 'top left';");
            }
        });
        
        Label placeholderLabel = new Label("Graph visualization will appear here after calculation");
        placeholderLabel.setStyle("-fx-font-family: 'Segoe UI', 'Roboto', sans-serif; " +
                                 "-fx-font-size: 14px; " +
                                 "-fx-text-fill: #94A3B8;");
        placeholderLabel.setAlignment(Pos.CENTER);
        VBox.setVgrow(placeholderLabel, Priority.ALWAYS);
        
        tabContent.getChildren().addAll(titleLabel, buttonBox, graphWebView);
        
        Tab tab = new Tab("Graph Visualization", tabContent);
        tab.setStyle("-fx-font-family: 'Segoe UI', 'Roboto', sans-serif; " +
                    "-fx-font-size: 13px;");
        resultTabPane.getTabs().add(tab);
    }
    
    /**
     * Creates the Technical Log tab with raw text.
     */
    private void createTechnicalLogTab() {
        VBox tabContent = new VBox(20);
        tabContent.setPadding(new Insets(25));
        tabContent.setStyle("-fx-background-color: #1B2533;");
        
        // Header section with title and description
        VBox headerBox = new VBox(8);
        Label titleLabel = new Label("📋 Technical Log");
        titleLabel.setStyle("-fx-font-family: 'Segoe UI', 'Roboto', sans-serif; " +
                           "-fx-font-size: 22px; " +
                           "-fx-font-weight: 700; " +
                           "-fx-text-fill: #E2E8F0;");
        
        Label descriptionLabel = new Label("Complete raw analysis output for debugging and technical review");
        descriptionLabel.setStyle("-fx-font-family: 'Segoe UI', 'Roboto', sans-serif; " +
                                 "-fx-font-size: 13px; " +
                                 "-fx-text-fill: #94A3B8;");
        headerBox.getChildren().addAll(titleLabel, descriptionLabel);
        
        // Action buttons
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_LEFT);
        
        Button copyButton = new Button("📋 Copy to Clipboard");
        copyButton.setStyle("-fx-font-family: 'Segoe UI', 'Roboto', sans-serif; " +
                          "-fx-font-size: 13px; " +
                          "-fx-font-weight: 500; " +
                          "-fx-background-color: #475569; " +
                          "-fx-text-fill: #E2E8F0; " +
                          "-fx-background-radius: 8px; " +
                          "-fx-cursor: hand; " +
                          "-fx-padding: 8 16;");
        copyButton.setOnAction(e -> {
            if (technicalLogArea != null && technicalLogArea.getText() != null) {
                Clipboard clipboard = Clipboard.getSystemClipboard();
                ClipboardContent content = new ClipboardContent();
                content.putString(technicalLogArea.getText());
                clipboard.setContent(content);
                
                // Show feedback
                copyButton.setText("✓ Copied!");
                copyButton.setStyle("-fx-font-family: 'Segoe UI', 'Roboto', sans-serif; " +
                                  "-fx-font-size: 13px; " +
                                  "-fx-font-weight: 500; " +
                                  "-fx-background-color: #10B981; " +
                                  "-fx-text-fill: #FFFFFF; " +
                                  "-fx-background-radius: 8px; " +
                                  "-fx-cursor: hand; " +
                                  "-fx-padding: 8 16;");
                new java.util.Timer().schedule(new java.util.TimerTask() {
                    @Override
                    public void run() {
                        Platform.runLater(() -> {
                            copyButton.setText("📋 Copy to Clipboard");
                            copyButton.setStyle("-fx-font-family: 'Segoe UI', 'Roboto', sans-serif; " +
                                              "-fx-font-size: 13px; " +
                                              "-fx-font-weight: 500; " +
                                              "-fx-background-color: #475569; " +
                                              "-fx-text-fill: #E2E8F0; " +
                                              "-fx-background-radius: 8px; " +
                                              "-fx-cursor: hand; " +
                                              "-fx-padding: 8 16;");
                        });
                    }
                }, 2000);
            }
        });
        
        Button exportButton = new Button("💾 Export to File");
        exportButton.setStyle("-fx-font-family: 'Segoe UI', 'Roboto', sans-serif; " +
                            "-fx-font-size: 13px; " +
                            "-fx-font-weight: 500; " +
                            "-fx-background-color: #475569; " +
                            "-fx-text-fill: #E2E8F0; " +
                            "-fx-background-radius: 8px; " +
                            "-fx-cursor: hand; " +
                            "-fx-padding: 8 16;");
        exportButton.setOnAction(e -> {
            if (technicalLogArea != null && technicalLogArea.getText() != null && !technicalLogArea.getText().isEmpty()) {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Export Technical Log");
                fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text files", "*.txt"));
                fileChooser.setInitialFileName("upgrade_plan_analysis.txt");
                File file = fileChooser.showSaveDialog(stage);
                
                if (file != null) {
                    try {
                        Files.write(Paths.get(file.getAbsolutePath()), technicalLogArea.getText().getBytes());
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("Export Successful");
                        alert.setHeaderText(null);
                        alert.setContentText("Technical log exported successfully!");
                        alert.showAndWait();
                    } catch (Exception ex) {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Export Error");
                        alert.setHeaderText(null);
                        alert.setContentText("Failed to export file: " + ex.getMessage());
                        alert.showAndWait();
                    }
                }
            }
        });
        
        Button clearButton = new Button("🗑️ Clear");
        clearButton.setStyle("-fx-font-family: 'Segoe UI', 'Roboto', sans-serif; " +
                           "-fx-font-size: 13px; " +
                           "-fx-font-weight: 500; " +
                           "-fx-background-color: #475569; " +
                           "-fx-text-fill: #E2E8F0; " +
                           "-fx-background-radius: 8px; " +
                           "-fx-cursor: hand; " +
                           "-fx-padding: 8 16;");
        clearButton.setOnAction(e -> {
            if (technicalLogArea != null) {
                technicalLogArea.clear();
            }
        });
        
        buttonBox.getChildren().addAll(copyButton, exportButton, clearButton);
        
        // TextArea with enhanced styling - black text on light background
        technicalLogArea = new TextArea();
        technicalLogArea.setEditable(false);
        technicalLogArea.setWrapText(false); // Disable wrapping to show more content horizontally
        technicalLogArea.setStyle("-fx-font-family: 'Consolas', 'Monaco', 'Courier New', monospace; " +
                                 "-fx-font-size: 11px; " +
                                 "-fx-line-spacing: 2px; " +
                                 "-fx-background-color: #FFFFFF; " +
                                 "-fx-text-fill: #000000; " +
                                 "-fx-border-color: rgba(99, 102, 241, 0.3); " +
                                 "-fx-border-width: 2px; " +
                                 "-fx-border-radius: 12px; " +
                                 "-fx-background-radius: 12px; " +
                                 "-fx-padding: 12; " +
                                 "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.1), 5, 0, 0, 1);");
        
        // Placeholder text
        Label placeholderLabel = new Label("No analysis data available.\nClick 'Calculate Upgrade Order' to generate the technical log.");
        placeholderLabel.setStyle("-fx-font-family: 'Segoe UI', 'Roboto', sans-serif; " +
                                "-fx-font-size: 14px; " +
                                "-fx-text-fill: #64748B; " +
                                "-fx-alignment: CENTER;");
        placeholderLabel.setWrapText(true);
        placeholderLabel.setMaxWidth(Double.MAX_VALUE);
        placeholderLabel.setAlignment(Pos.CENTER);
        
        // Container for TextArea with placeholder
        StackPane logContainer = new StackPane();
        logContainer.getChildren().addAll(technicalLogArea, placeholderLabel);
        VBox.setVgrow(logContainer, Priority.ALWAYS);
        
        // Make TextArea fill available space and auto-scroll to top when content changes
        technicalLogArea.textProperty().addListener((obs, oldText, newText) -> {
            if (newText != null && !newText.isEmpty()) {
                // Scroll to top when new content is set
                Platform.runLater(() -> {
                    technicalLogArea.setScrollTop(0);
                    technicalLogArea.setScrollLeft(0);
                });
            }
        });
        
        // Show/hide placeholder based on content
        technicalLogArea.textProperty().addListener((obs, oldText, newText) -> {
            placeholderLabel.setVisible(newText == null || newText.isEmpty());
        });
        
        tabContent.getChildren().addAll(headerBox, buttonBox, logContainer);
        
        Tab tab = new Tab("Technical Log", tabContent);
        tab.setStyle("-fx-font-family: 'Segoe UI', 'Roboto', sans-serif; " +
                    "-fx-font-size: 13px;");
        resultTabPane.getTabs().add(tab);
    }
    
    /**
     * Handles the calculate button click event.
     * Uses JavaFX Service to perform the calculation in a background thread.
     */
    private void handleCalculate() {
        calculateButton.setDisable(true);
        progressIndicator.setVisible(true);
        statusLabel.setText("Loading data and calculating...");
        if (technicalLogArea != null) technicalLogArea.clear();
        if (affectedStationsTable != null) affectedStationsTable.getItems().clear();
        if (cyclesTable != null) cyclesTable.getItems().clear();
        if (criticalEdgesTable != null) criticalEdgesTable.getItems().clear();
        
        // Create a service to perform the calculation in background
        Service<UpgradePlanController.UpgradePlanResult> service = new Service<>() {
            @Override
            protected Task<UpgradePlanController.UpgradePlanResult> createTask() {
                return new Task<>() {
                    @Override
                    protected UpgradePlanController.UpgradePlanResult call() throws Exception {
                        updateMessage("Loading data...");
                        
                        // STEP 1: Load data and build DIRECTED graph using RailwayGraphRepository
                        // RailwayGraphRepository ensures the graph is directed, preserving
                        // directional dependencies (A->B means A must be upgraded before B)
                        RailwayGraphRepository graphRepo = new RailwayGraphRepository();
                        graphRepo.loadData(DEFAULT_STATIONS_PATH, DEFAULT_EDGES_PATH);
                        
                        updateMessage("Building directed graph for USEI11...");
                        
                        // STEP 2: Create controller with RailwayGraphRepository
                        // This ensures the graph used for analysis is DIRECTED (required for USEI11)
                        controller = new UpgradePlanController(graphRepo);
                        controller.buildGraph();
                        
                        updateMessage("Calculating upgrade order...");
                        
                        // STEP 3: Calculate upgrade plan using the directed graph
                        return controller.calculateUpgradePlan();
                    }
                };
            }
        };
        
        // Update status during execution
        service.messageProperty().addListener((obs, oldMsg, newMsg) -> 
            Platform.runLater(() -> statusLabel.setText(newMsg))
        );
        
        // Handle completion
        service.setOnSucceeded(e -> {
            UpgradePlanController.UpgradePlanResult result = service.getValue();
            currentResult = result;
            displayResult(result);
            calculateButton.setDisable(false);
            progressIndicator.setVisible(false);
            statusLabel.setText("Calculation completed");
        });
        
        // Handle errors
        service.setOnFailed(e -> {
            Throwable exception = service.getException();
            displayError(exception);
            calculateButton.setDisable(false);
            progressIndicator.setVisible(false);
            statusLabel.setText("Calculation error");
        });
        
        service.start();
    }
    
    /**
     * Displays the calculation result in all tabs.
     * 
     * @param result the upgrade plan result
     */
    private void displayResult(UpgradePlanController.UpgradePlanResult result) {
        // Build technical log (raw text output)
        StringBuilder sb = new StringBuilder();
        sb.append("UPGRADE PLAN ANALYSIS \n");
        sb.append("=".repeat(80)).append("\n\n");
        
        // Get graph statistics
        final int[] nodes = {0};
        final int[] edges = {0};
        try {
            String stats = controller.getGraphStatistics();
            String[] lines = stats.split("\n");
            for (String line : lines) {
                if (line.contains("Nodes:")) {
                    nodes[0] = Integer.parseInt(line.replaceAll("[^0-9]", ""));
                } else if (line.contains("Edges:")) {
                    edges[0] = Integer.parseInt(line.replaceAll("[^0-9]", ""));
                }
            }
            sb.append("GRAPH OVERVIEW\n");
            sb.append("-".repeat(80)).append("\n");
            sb.append(String.format("Nodes (Stations): %d\n", nodes[0]));
            sb.append(String.format("Edges (Connections): %d\n\n", edges[0]));
        } catch (Exception e) {
            // Ignore
        }
        
        // Update Overview cards
        final int finalNodes = nodes[0];
        final int finalEdges = edges[0];
        Platform.runLater(() -> {
            if (totalNodesLabel != null) totalNodesLabel.setText(String.valueOf(finalNodes));
            if (totalEdgesLabel != null) totalEdgesLabel.setText(String.valueOf(finalEdges));
        });
        
        // Populate tables and update UI
        if (result.hasTopologicalOrder()) {
            populateTopologicalOrderResult(result, sb);
        } else if (result.hasCycles()) {
            populateCyclesResult(result, sb);
        } else {
            sb.append("RESULT: EMPTY OR INVALID\n");
            sb.append("No valid result available.\n");
        }
        
        // Save technical log
        final String finalLog = sb.toString();
        rawTechnicalLog = finalLog;
        if (technicalLogArea != null) {
            Platform.runLater(() -> {
                technicalLogArea.setText(finalLog);
                // Scroll to top after setting text
                technicalLogArea.setScrollTop(0);
                technicalLogArea.setScrollLeft(0);
            });
        }
    }
    
    /**
     * Populates UI for topological order result.
     */
    private void populateTopologicalOrderResult(UpgradePlanController.UpgradePlanResult result, StringBuilder sb) {
        List<Station> order = result.getTopologicalOrder();
        sb.append("RESULT: VALID ORDER FOUND\n");
        sb.append("-".repeat(80)).append("\n");
        sb.append("A valid topological order can be determined.\n\n");
        sb.append("STATION UPGRADE ORDER:\n");
        sb.append("=".repeat(80)).append("\n");
        for (int i = 0; i < order.size(); i++) {
            Station station = order.get(i);
            sb.append(String.format("%4d. [%s] %s\n", i + 1, station.getId(), 
                station.getName() != null ? station.getName() : "N/A"));
        }
        sb.append("\n").append("=".repeat(80)).append("\n");
        sb.append(String.format("Total: %d stations\n", order.size()));
        
        Platform.runLater(() -> {
            if (cyclesDetectedLabel != null) cyclesDetectedLabel.setText("0");
            if (affectedStationsLabel != null) affectedStationsLabel.setText("0");
            if (largestCycleLabel != null) largestCycleLabel.setText("N/A");
            if (mostProblematicStationLabel != null) mostProblematicStationLabel.setText("N/A");
        });
    }
    
    /**
     * Populates UI for cycles result.
     */
    private void populateCyclesResult(UpgradePlanController.UpgradePlanResult result, StringBuilder sb) {
        var cycleInfo = result.getCycleInfo();
        currentStationDegrees = calculateStationDegrees(cycleInfo);
        currentStationCycles = calculateStationCycleParticipation(cycleInfo);
        currentCycleInfo = cycleInfo;
        currentCycles = cycleInfo.getCycles();
        
        Set<String> cycleNodeIds = cycleInfo.getCycleNodeIds();
        List<List<Station>> cycles = currentCycles;
        
        // Update Overview cards
        final int[] totalStations = {0};
        try {
            String stats = controller.getGraphStatistics();
            String[] lines = stats.split("\n");
            for (String line : lines) {
                if (line.contains("Nodes:")) {
                    totalStations[0] = Integer.parseInt(line.replaceAll("[^0-9]", ""));
                }
            }
        } catch (Exception e) {
            // Ignore
        }
        
        final double percentage = totalStations[0] > 0 ? (cycleNodeIds.size() * 100.0 / totalStations[0]) : 0;
        
        // Find largest cycle
        final int maxCycleSize = cycles.stream().mapToInt(List::size).max().orElse(0);
        int largestCycleIndex = -1;
        for (int i = 0; i < cycles.size(); i++) {
            if (cycles.get(i).size() == maxCycleSize) {
                largestCycleIndex = i;
                break;
            }
        }
        final int finalLargestCycleIndex = largestCycleIndex;
        
        // Find most problematic station
        String mostProblematic = currentStationCycles.entrySet().stream()
            .max((a, b) -> Integer.compare(a.getValue().size(), b.getValue().size()))
            .map(Map.Entry::getKey)
            .orElse("N/A");
        final int maxCycles = mostProblematic.equals("N/A") ? 0 : currentStationCycles.get(mostProblematic).size();
        final String finalMostProblematic = mostProblematic;
        final int finalTotalStations = totalStations[0];
        final int finalCyclesSize = cycles.size();
        final int finalCycleNodeIdsSize = cycleNodeIds.size();
        
        Platform.runLater(() -> {
            if (cyclesDetectedLabel != null) cyclesDetectedLabel.setText(String.valueOf(finalCyclesSize));
            if (affectedStationsLabel != null) affectedStationsLabel.setText(String.valueOf(finalCycleNodeIdsSize) + 
                " (" + String.format("%.1f", percentage) + "%)");
            if (largestCycleLabel != null) {
                largestCycleLabel.setText(maxCycleSize > 0 ? maxCycleSize + " stations" : "N/A");
            }
            if (mostProblematicStationLabel != null) {
                mostProblematicStationLabel.setText(maxCycles > 0 ? finalMostProblematic + " (" + maxCycles + " cycles)" : "N/A");
            }
        });
        
        // Populate Affected Stations table
        ObservableList<StationData> stationDataList = FXCollections.observableArrayList();
        List<Map.Entry<String, Integer>> sortedStations = new ArrayList<>(currentStationDegrees.entrySet());
        sortedStations.sort((a, b) -> b.getValue().compareTo(a.getValue()));
        
        for (Map.Entry<String, Integer> entry : sortedStations) {
            String stationId = entry.getKey();
            Station station = findStationById(stationId);
            String name = station != null && station.getName() != null ? station.getName() : "N/A";
            List<Integer> cycleNums = currentStationCycles.getOrDefault(stationId, new ArrayList<>());
            String cyclesStr = cycleNums.stream().map(String::valueOf).collect(Collectors.joining(", "));
            
            try {
                UpgradePlanController.DegreeInfo degreeInfo = controller.getStationDegreeInfo(stationId);
                stationDataList.add(new StationData(stationId, name, degreeInfo.getInDegree(), 
                    degreeInfo.getOutDegree(), degreeInfo.getTotalDegree(), cyclesStr));
            } catch (Exception e) {
                stationDataList.add(new StationData(stationId, name, 0, 0, entry.getValue(), cyclesStr));
            }
        }
        
        Platform.runLater(() -> {
            if (affectedStationsTable != null) {
                affectedStationsTable.setItems(stationDataList);
            }
        });
        
        // Populate Cycles table
        ObservableList<CycleData> cycleDataList = FXCollections.observableArrayList();
        for (int i = 0; i < cycles.size(); i++) {
            List<Station> cycle = cycles.get(i);
            String path = cycle.stream().map(Station::getId).collect(Collectors.joining(" -> "));
            String impact = cycle.size() > 10 ? "HIGH" : cycle.size() >= 5 ? "MEDIUM" : "LOW";
            cycleDataList.add(new CycleData(i + 1, cycle.size(), path, impact));
        }
        
        Platform.runLater(() -> {
            if (cyclesTable != null) {
                cyclesTable.setItems(cycleDataList);
            }
        });
        
        // Populate Critical Edges table
        Map<Line, Integer> edgeCycleCount = calculateEdgeCycleCount(cycleInfo);
        List<Map.Entry<Line, Integer>> sortedEdges = new ArrayList<>(edgeCycleCount.entrySet());
        sortedEdges.sort((a, b) -> b.getValue().compareTo(a.getValue()));
        
        ObservableList<EdgeData> edgeDataList = FXCollections.observableArrayList();
        int rank = 1;
        for (Map.Entry<Line, Integer> entry : sortedEdges.stream().limit(20).collect(Collectors.toList())) {
            Line edge = entry.getKey();
            int cycleCount = entry.getValue();
            String recommendation = cycleCount >= 4 ? "HIGH PRIORITY" : cycleCount >= 2 ? "MEDIUM PRIORITY" : "LOW PRIORITY";
            List<Integer> edgeCycles = findEdgeCycles(edge, cycleInfo);
            String cyclesStr = edgeCycles.stream().map(String::valueOf).collect(Collectors.joining(", "));
            double dist = edge.getDistance();
            String distStr = dist > 0 ? String.format("%.2f km", dist) : "? km";
            
            edgeDataList.add(new EdgeData(rank++, edge.getDepartureStationId() + " -> " + edge.getArrivalStationId(), 
                distStr, cyclesStr, recommendation));
        }
        
        Platform.runLater(() -> {
            if (criticalEdgesTable != null) {
                criticalEdgesTable.setItems(edgeDataList);
            }
        });
        
        // Build technical log for cycles
        sb.append("RESULT: CYCLES DETECTED\n");
        sb.append("-".repeat(80)).append("\n");
        sb.append("A valid topological order cannot be determined due to circular dependencies.\n\n");
        sb.append(String.format("- %d of %d stations (%.1f%%) are involved in circular dependencies\n",
            finalCycleNodeIdsSize, finalTotalStations, percentage));
        sb.append(String.format("- %d distinct cycles identified in the network\n", finalCyclesSize));
        if (maxCycleSize > 0) {
            sb.append(String.format("- Largest cycle: %d stations (Cycle %d)\n", maxCycleSize, finalLargestCycleIndex + 1));
        }
        if (maxCycles > 0) {
            sb.append(String.format("- Most problematic station: ID %s (appears in %d cycles)\n", finalMostProblematic, maxCycles));
        }
        
        // Refresh graph visualization
        refreshGraphVisualization();
    }
    
    /**
     * Exports graph to DOT file.
     */
    private void exportDOTFile() {
        if (controller == null) return;
        
        try {
            String dotContent = generateDOTContent();
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Export DOT File");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("DOT files", "*.dot"));
            fileChooser.setInitialFileName("graph.dot");
            File file = fileChooser.showSaveDialog(stage);
            
            if (file != null) {
                Files.write(Paths.get(file.getAbsolutePath()), dotContent.getBytes());
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Export Successful");
                alert.setHeaderText(null);
                alert.setContentText("DOT file exported successfully!");
                alert.showAndWait();
            }
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Export Error");
            alert.setHeaderText(null);
            alert.setContentText("Failed to export DOT file: " + e.getMessage());
            alert.showAndWait();
        }
    }
    
    /**
     * Exports graph to SVG file using Graphviz.
     */
    private void exportSVGFile() {
        if (controller == null) return;
        
        File tempDot = null;
        try {
            String dotContent = generateDOTContent();
            tempDot = File.createTempFile("graph", ".dot");
            Files.write(Paths.get(tempDot.getAbsolutePath()), dotContent.getBytes("UTF-8"));
            
            // Try to convert using neato (Graphviz)
            String neatoPath = "C:\\Program Files\\Graphviz\\bin\\neato.exe";
            File neatoFile = new File(neatoPath);
            if (!neatoFile.exists()) {
                throw new Exception("neato.exe not found at: " + neatoPath);
            }
            
            // Use the absolute path
            String absolutePath = neatoFile.getAbsolutePath();
            ProcessBuilder pb = new ProcessBuilder(absolutePath, "-Tsvg", tempDot.getAbsolutePath());
            pb.redirectErrorStream(true);
            Process process = pb.start();
            
            // Read the SVG output from the process
            StringBuilder allOutput = new StringBuilder();
            StringBuilder errorContent = new StringBuilder();
            
            // Read from stdout (warnings and SVG are mixed)
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), "UTF-8"))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    allOutput.append(line).append("\n");
                }
            }
            
            // Also read from stderr (in case errors are not redirected)
            try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream(), "UTF-8"))) {
                String line;
                while ((line = errorReader.readLine()) != null) {
                    errorContent.append(line).append("\n");
                }
            }
            
            // Wait for the process to complete
            int exitCode = process.waitFor();
            
            // Check if we got any content
            String allOutputStr = allOutput.toString();
            String errorOutput = errorContent.toString();
            
            if (exitCode != 0) {
                throw new Exception("neato process exited with code: " + exitCode + 
                    (errorOutput.isEmpty() ? "" : "\nError output: " + errorOutput));
            }
            
            if (allOutputStr.trim().isEmpty()) {
                throw new Exception("neato produced empty output" + 
                    (errorOutput.isEmpty() ? "" : "\nError output: " + errorOutput));
            }
            
            // Extract SVG content - neato may output warnings before the actual SVG
            // Find where the SVG actually starts (<?xml or <svg)
            String svgOutput = null;
            int xmlIndex = allOutputStr.indexOf("<?xml");
            int svgIndex = allOutputStr.indexOf("<svg");
            
            if (xmlIndex >= 0) {
                // SVG starts with <?xml, extract from there
                svgOutput = allOutputStr.substring(xmlIndex);
            } else if (svgIndex >= 0) {
                // SVG starts with <svg, extract from there
                svgOutput = allOutputStr.substring(svgIndex);
            } else {
                // No valid SVG found
                String preview = allOutputStr.length() > 300 ? allOutputStr.substring(0, 300) + "..." : allOutputStr;
                throw new Exception("Could not find SVG content in neato output.\n" +
                    "Output preview: " + preview.replace("\n", "\\n") +
                    (errorOutput.isEmpty() ? "" : "\nError output: " + errorOutput));
            }
            
            // Clean up the SVG - remove leading/trailing whitespace but preserve structure
            String finalSvg = svgOutput.trim();
            
            if (finalSvg.isEmpty()) {
                throw new Exception("Extracted SVG content is empty");
            }
            
            // Verify it's actually SVG - check if it starts with <?xml or <svg (case-insensitive)
            String trimmedStart = finalSvg.trim();
            if (!trimmedStart.startsWith("<?xml") && !trimmedStart.startsWith("<svg") && 
                !trimmedStart.toLowerCase().startsWith("<?xml") && !trimmedStart.toLowerCase().startsWith("<svg")) {
                // Show more context in error
                String preview = trimmedStart.length() > 150 ? trimmedStart.substring(0, 150) : trimmedStart;
                throw new Exception("neato output does not appear to be valid SVG.\n" +
                    "Output starts with: " + preview.replace("\n", "\\n").replace("\r", "\\r") +
                    (errorOutput.isEmpty() ? "" : "\nError output: " + errorOutput));
            }
            
            // Now show the file chooser
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Export SVG File");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("SVG files", "*.svg"));
            fileChooser.setInitialFileName("graph.svg");
            File file = fileChooser.showSaveDialog(stage);
            
            if (file != null) {
                // Write the SVG content to the file with UTF-8 encoding
                Files.write(Paths.get(file.getAbsolutePath()), finalSvg.getBytes("UTF-8"));
                
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Export Successful");
                alert.setHeaderText(null);
                alert.setContentText("SVG file exported successfully!");
                alert.showAndWait();
            }
            
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Export Error");
            alert.setHeaderText(null);
            alert.setContentText("Failed to export SVG file.\n\n" + 
                "Make sure Graphviz (neato) is installed at:\n" +
                "C:\\Program Files\\Graphviz\\bin\\neato.exe\n\n" +
                "Error details: " + e.getMessage());
            alert.showAndWait();
        } finally {
            // Clean up temp file
            if (tempDot != null && tempDot.exists()) {
                try {
                    tempDot.delete();
                } catch (Exception e) {
                    // Ignore cleanup errors
                }
            }
        }
    }
    
    /**
     * Generates DOT content for the graph.
     */
    private String generateDOTContent() {
        if (controller == null) {
            return "digraph G {\n  // No graph data available\n}\n";
        }
        
        StringBuilder dot = new StringBuilder();
        dot.append("digraph G {\n");
        dot.append("  rankdir=LR;\n");
        dot.append("  nodesep=2.0;\n");  // Increased horizontal spacing between nodes
        dot.append("  ranksep=3.0;\n");  // Increased vertical spacing between ranks
        dot.append("  overlap=false;\n"); // Prevent node overlap
        dot.append("  splines=true;\n");  // Use curved edges for better layout
        dot.append("  node [shape=circle, style=filled, fillcolor=lightblue, fontname=\"Segoe UI\", width=0.8, height=0.8];\n");
        dot.append("  edge [fontname=\"Segoe UI\", fontsize=10];\n");
        
        Set<Line> cycleEdges = currentCycleInfo != null ? currentCycleInfo.getCycleEdges() : new HashSet<>();
        Set<String> cycleNodeIds = currentCycleInfo != null ? currentCycleInfo.getCycleNodeIds() : new HashSet<>();
        
        try {
            // Add all nodes - mark cycle nodes in red
            Collection<Station> allStations = controller.getAllStations();
            for (Station station : allStations) {
                String nodeId = station.getId().replaceAll("[^a-zA-Z0-9_]", "_");
                String label = station.getName() != null && !station.getName().isEmpty() 
                    ? station.getName() 
                    : station.getId();
                if (label.length() > 20) label = label.substring(0, 17) + "...";
                
                if (cycleNodeIds.contains(station.getId())) {
                    dot.append(String.format("  %s [label=\"%s\", fillcolor=lightcoral];\n", nodeId, label));
                } else {
                    dot.append(String.format("  %s [label=\"%s\"];\n", nodeId, label));
                }
            }
            
            // Add edges - mark cycle edges in red
            List<Line> allEdges = controller.getAllEdges();
            for (Line edge : allEdges) {
                String fromId = edge.getDepartureStationId().replaceAll("[^a-zA-Z0-9_]", "_");
                String toId = edge.getArrivalStationId().replaceAll("[^a-zA-Z0-9_]", "_");
                
                if (cycleEdges.contains(edge)) {
                    dot.append(String.format("  %s -> %s [color=red, penwidth=2];\n", fromId, toId));
                } else {
                    dot.append(String.format("  %s -> %s;\n", fromId, toId));
                }
            }
        } catch (Exception e) {
            dot.append("  // Error generating graph: ").append(e.getMessage()).append("\n");
        }
        
        dot.append("}\n");
        return dot.toString();
    }
    
    /**
     * Refreshes the graph visualization.
     */
    private void refreshGraphVisualization() {
        if (controller == null || currentCycleInfo == null) {
            Platform.runLater(() -> {
                if (webEngine != null) {
                    webEngine.loadContent("<html><body style='background:#1B2533;color:#E2E8F0;font-family:Segoe UI;padding:20px;'><h3>Graph Visualization</h3><p>No graph data available. Please calculate upgrade order first.</p></body></html>");
                }
            });
            return;
        }
        
        try {
            // Generate DOT and convert to SVG
            String dotContent = generateDOTContent();
            File tempDot = File.createTempFile("graph", ".dot");
            Files.write(Paths.get(tempDot.getAbsolutePath()), dotContent.getBytes());
            
            // Try to convert using neato
            String neatoPath = "C:\\Program Files\\Graphviz\\bin\\neato.exe";
            File neatoFile = new File(neatoPath);
            if (!neatoFile.exists()) {
                throw new Exception("neato.exe not found at: " + neatoPath);
            }
            
            // Use the absolute path and ensure proper quoting for Windows paths with spaces
            String absolutePath = neatoFile.getAbsolutePath();
            ProcessBuilder pb = new ProcessBuilder(absolutePath, "-Tsvg", tempDot.getAbsolutePath());
            pb.redirectErrorStream(true);
            Process process = pb.start();
            
            StringBuilder svgContent = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    svgContent.append(line).append("\n");
                }
            }
            
            process.waitFor();
            tempDot.delete();
            
            String finalSvg = svgContent.toString();
            // Wrap SVG in HTML with zoom support and white background that stays visible
            String htmlContent = "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "  <style>\n" +
                "    body {\n" +
                "      margin: 0;\n" +
                "      padding: 0;\n" +
                "      background: #1B2533;\n" +
                "      overflow: auto;\n" +
                "      min-height: 100vh;\n" +
                "    }\n" +
                "    #graphContainer {\n" +
                "      background: #FFFFFF;\n" +
                "      padding: 20px;\n" +
                "      display: inline-block;\n" +
                "      min-width: 100%;\n" +
                "      min-height: 100vh;\n" +
                "      transform-origin: top left;\n" +
                "    }\n" +
                "    svg {\n" +
                "      display: block;\n" +
                "      max-width: 100%;\n" +
                "      height: auto;\n" +
                "      background: #FFFFFF;\n" +
                "    }\n" +
                "  </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "  <div id=\"graphContainer\">\n" +
                finalSvg +
                "  </div>\n" +
                "<script>\n" +
                "  window.currentZoom = 1.0;\n" +
                "  var container = document.getElementById('graphContainer');\n" +
                "  // Enable mouse wheel zoom\n" +
                "  document.addEventListener('wheel', function(e) {\n" +
                "    if (e.ctrlKey || e.metaKey) {\n" +
                "      e.preventDefault();\n" +
                "      var delta = e.deltaY > 0 ? 0.9 : 1.1;\n" +
                "      window.currentZoom = Math.max(0.2, Math.min(5.0, window.currentZoom * delta));\n" +
                "      container.style.transform = 'scale(' + window.currentZoom + ')';\n" +
                "    }\n" +
                "  }, { passive: false });\n" +
                "</script>\n" +
                "</body>\n" +
                "</html>";
            
            Platform.runLater(() -> {
                if (webEngine != null) {
                    webEngine.loadContent(htmlContent);
                }
            });
        } catch (Exception e) {
            // If Graphviz is not available, show a message
            String errorMsg = e.getMessage();
            String neatoPath = "C:\\Program Files\\Graphviz\\bin\\neato.exe";
            Platform.runLater(() -> {
                if (webEngine != null) {
                    webEngine.loadContent("<html><body style='background:#1B2533;color:#E2E8F0;font-family:Segoe UI;padding:20px;'><h3>Graph Visualization</h3><p>Graphviz (neato) is required for graph visualization.</p><p>Expected path: " + neatoPath + "</p><p>Error: " + errorMsg + "</p></body></html>");
                }
            });
        }
    }
    
    /**
     * Shows a dialog with all affected stations.
     */
    private void showAllStationsDialog() {
        if (currentCycleInfo == null || currentStationDegrees == null || currentStationCycles == null) {
            return;
        }
        
        Stage dialog = new Stage();
        dialog.setTitle("All Affected Stations");
        dialog.initOwner(stage);
        
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #1E293B;");
        
        Label titleLabel = new Label("All Affected Stations (" + 
            currentCycleInfo.getCycleNodeIds().size() + " stations)");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: 600; -fx-text-fill: #E2E8F0;");
        
        // Create table
        TableView<Map.Entry<String, Integer>> table = new TableView<>();
        table.setPrefHeight(500);
        table.setStyle("-fx-background-color: #FFFFFF;");
        
        // ID column
        TableColumn<Map.Entry<String, Integer>, String> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(param -> 
            new javafx.beans.property.SimpleStringProperty(param.getValue().getKey()));
        idColumn.setPrefWidth(100);
        
        // Name column
        TableColumn<Map.Entry<String, Integer>, String> nameColumn = new TableColumn<>("Name");
        nameColumn.setCellValueFactory(param -> {
            Station station = findStationById(param.getValue().getKey());
            String name = station != null && station.getName() != null 
                ? station.getName() 
                : "N/A";
            return new javafx.beans.property.SimpleStringProperty(name);
        });
        nameColumn.setPrefWidth(200);
        
        // In-Degree column
        TableColumn<Map.Entry<String, Integer>, Integer> inDegreeColumn = new TableColumn<>("In");
        inDegreeColumn.setCellValueFactory(param -> {
            try {
                UpgradePlanController.DegreeInfo degreeInfo = controller.getStationDegreeInfo(param.getValue().getKey());
                return new javafx.beans.property.SimpleIntegerProperty(degreeInfo.getInDegree()).asObject();
            } catch (Exception e) {
                return new javafx.beans.property.SimpleIntegerProperty(0).asObject();
            }
        });
        inDegreeColumn.setPrefWidth(60);
        
        // Out-Degree column
        TableColumn<Map.Entry<String, Integer>, Integer> outDegreeColumn = new TableColumn<>("Out");
        outDegreeColumn.setCellValueFactory(param -> {
            try {
                UpgradePlanController.DegreeInfo degreeInfo = controller.getStationDegreeInfo(param.getValue().getKey());
                return new javafx.beans.property.SimpleIntegerProperty(degreeInfo.getOutDegree()).asObject();
            } catch (Exception e) {
                return new javafx.beans.property.SimpleIntegerProperty(0).asObject();
            }
        });
        outDegreeColumn.setPrefWidth(60);
        
        // Total Degree column
        TableColumn<Map.Entry<String, Integer>, Integer> totalDegreeColumn = new TableColumn<>("Total");
        totalDegreeColumn.setCellValueFactory(param -> 
            new javafx.beans.property.SimpleIntegerProperty(param.getValue().getValue()).asObject());
        totalDegreeColumn.setPrefWidth(60);
        
        // Cycles column
        TableColumn<Map.Entry<String, Integer>, String> cyclesColumn = new TableColumn<>("In Cycle #");
        cyclesColumn.setCellValueFactory(param -> {
            List<Integer> cycles = currentStationCycles.getOrDefault(
                param.getValue().getKey(), new ArrayList<>());
            String cyclesStr = cycles.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
            return new javafx.beans.property.SimpleStringProperty(cyclesStr);
        });
        cyclesColumn.setPrefWidth(200);
        
        table.getColumns().addAll(idColumn, nameColumn, inDegreeColumn, outDegreeColumn, totalDegreeColumn, cyclesColumn);
        
        // Populate table with sorted stations
        List<Map.Entry<String, Integer>> sortedStations = new ArrayList<>(
            currentStationDegrees.entrySet());
        sortedStations.sort((a, b) -> b.getValue().compareTo(a.getValue()));
        
        ObservableList<Map.Entry<String, Integer>> items = FXCollections.observableArrayList();
        items.addAll(sortedStations);
        table.setItems(items);
        
        Button closeButton = new Button("Close");
        closeButton.setStyle("-fx-font-size: 14px; -fx-font-weight: 500; " +
                            "-fx-background-color: #475569; " +
                            "-fx-text-fill: #E2E8F0; " +
                            "-fx-background-radius: 6; " +
                            "-fx-cursor: hand; " +
                            "-fx-padding: 8 24;");
        closeButton.setOnAction(e -> dialog.close());
        
        HBox buttonBox = new HBox();
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.getChildren().add(closeButton);
        
        root.getChildren().addAll(titleLabel, table, buttonBox);
        
        Scene scene = new Scene(root, 700, 600);
        dialog.setScene(scene);
        dialog.setResizable(true);
        dialog.show();
    }
    
    /**
     * Shows a dialog with all cycles.
     */
    private void showAllCyclesDialog() {
        if (currentCycles == null || currentCycles.isEmpty()) {
            return;
        }
        
        Stage dialog = new Stage();
        dialog.setTitle("All Cycles");
        dialog.initOwner(stage);
        
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #1E293B;");
        
        Label titleLabel = new Label("All Cycles (" + currentCycles.size() + " cycles detected)");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: 600; -fx-text-fill: #E2E8F0;");
        
        // Create text area with all cycles
        TextArea cyclesArea = new TextArea();
        cyclesArea.setEditable(false);
        cyclesArea.setWrapText(false);
        cyclesArea.setPrefHeight(500);
        cyclesArea.setStyle("-fx-font-family: 'Consolas', 'Monaco', monospace; " +
                           "-fx-font-size: 12px; " +
                           "-fx-background-color: #FFFFFF; " +
                           "-fx-text-fill: #000000;");
        
        // Categorize and format all cycles
        StringBuilder cyclesText = new StringBuilder();
        
        // HIGH IMPACT
        List<List<Station>> highImpact = new ArrayList<>();
        List<List<Station>> mediumImpact = new ArrayList<>();
        List<List<Station>> lowImpact = new ArrayList<>();
        
        for (List<Station> cycle : currentCycles) {
            int size = cycle.size();
            if (size > 10) {
                highImpact.add(cycle);
            } else if (size >= 5) {
                mediumImpact.add(cycle);
            } else {
                lowImpact.add(cycle);
            }
        }
        
        if (!highImpact.isEmpty()) {
            cyclesText.append("HIGH IMPACT - LARGE CYCLES (>10 stations):\n");
            cyclesText.append("=".repeat(80)).append("\n");
            for (int i = 0; i < currentCycles.size(); i++) {
                List<Station> cycle = currentCycles.get(i);
                if (cycle.size() > 10) {
                    cyclesText.append(formatCycleFull(cycle, i + 1));
                }
            }
            cyclesText.append("\n");
        }
        
        if (!mediumImpact.isEmpty()) {
            cyclesText.append("MEDIUM IMPACT (5-10 stations):\n");
            cyclesText.append("=".repeat(80)).append("\n");
            for (int i = 0; i < currentCycles.size(); i++) {
                List<Station> cycle = currentCycles.get(i);
                if (cycle.size() >= 5 && cycle.size() <= 10) {
                    cyclesText.append(formatCycleFull(cycle, i + 1));
                }
            }
            cyclesText.append("\n");
        }
        
        if (!lowImpact.isEmpty()) {
            cyclesText.append("LOW IMPACT - SMALL CYCLES (<5 stations):\n");
            cyclesText.append("=".repeat(80)).append("\n");
            for (int i = 0; i < currentCycles.size(); i++) {
                List<Station> cycle = currentCycles.get(i);
                if (cycle.size() < 5) {
                    cyclesText.append(formatCycleFull(cycle, i + 1));
                }
            }
            cyclesText.append("\n");
        }
        
        cyclesArea.setText(cyclesText.toString());
        
        Button closeButton = new Button("Close");
        closeButton.setStyle("-fx-font-size: 14px; -fx-font-weight: 500; " +
                            "-fx-background-color: #475569; " +
                            "-fx-text-fill: #E2E8F0; " +
                            "-fx-background-radius: 6; " +
                            "-fx-cursor: hand; " +
                            "-fx-padding: 8 24;");
        closeButton.setOnAction(e -> dialog.close());
        
        HBox buttonBox = new HBox();
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.getChildren().add(closeButton);
        
        root.getChildren().addAll(titleLabel, cyclesArea, buttonBox);
        
        Scene scene = new Scene(root, 900, 600);
        dialog.setScene(scene);
        dialog.setResizable(true);
        dialog.show();
    }
    
    // Helper methods
    private Map<String, Integer> calculateStationDegrees(CycleDetection.CycleInfo cycleInfo) {
        Map<String, Integer> degrees = new HashMap<>();
        Set<String> cycleNodeIds = cycleInfo.getCycleNodeIds();
        
        // Calculate actual total degree (in-degree + out-degree) for each station in cycles
        for (String stationId : cycleNodeIds) {
            try {
                UpgradePlanController.DegreeInfo degreeInfo = controller.getStationDegreeInfo(stationId);
                degrees.put(stationId, degreeInfo.getTotalDegree());
            } catch (Exception e) {
                // If we can't get degree, use 0
                degrees.put(stationId, 0);
            }
        }
        return degrees;
    }
    
    private Map<String, List<Integer>> calculateStationCycleParticipation(CycleDetection.CycleInfo cycleInfo) {
        Map<String, List<Integer>> participation = new HashMap<>();
        List<List<Station>> cycles = cycleInfo.getCycles();
        
        for (int i = 0; i < cycles.size(); i++) {
            for (Station station : cycles.get(i)) {
                participation.computeIfAbsent(station.getId(), k -> new ArrayList<>()).add(i + 1);
            }
        }
        return participation;
    }
    
    private String formatCycleList(List<Integer> cycles) {
        if (cycles.isEmpty()) return "N/A";
        if (cycles.size() <= 3) {
            return cycles.stream().map(String::valueOf).collect(java.util.stream.Collectors.joining(","));
        }
        return cycles.subList(0, 3).stream().map(String::valueOf)
            .collect(java.util.stream.Collectors.joining(",")) + "...";
    }
    
    /**
     * Formats a cycle for display in main result area.
     * Truncates large cycles with "..." to keep output readable.
     */
    private String formatCycle(List<Station> cycle, int cycleNum) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Cycle %d (%d stations): ", cycleNum, cycle.size()));
        
        if (cycle.size() <= 10) {
            // Show full cycle for small cycles
            for (int i = 0; i < cycle.size(); i++) {
                sb.append(cycle.get(i).getId());
                if (i < cycle.size() - 1) sb.append(" -> ");
            }
        } else {
            // Show first 3, then ..., then last for large cycles
            sb.append(cycle.get(0).getId()).append(" -> ");
            sb.append(cycle.get(1).getId()).append(" -> ");
            sb.append(cycle.get(2).getId()).append(" -> ... -> ");
            sb.append(cycle.get(cycle.size() - 1).getId());
        }
        sb.append("\n");
        return sb.toString();
    }
    
    /**
     * Formats a cycle showing all stations (used in dialog).
     * Always shows complete cycle without truncation.
     */
    private String formatCycleFull(List<Station> cycle, int cycleNum) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Cycle %d (%d stations): ", cycleNum, cycle.size()));
        
        // Show full cycle - all stations
        for (int i = 0; i < cycle.size(); i++) {
            sb.append(cycle.get(i).getId());
            if (i < cycle.size() - 1) sb.append(" -> ");
        }
        sb.append("\n");
        return sb.toString();
    }
    
    private Station findStationById(String stationId) {
        try {
            return controller.getStationById(stationId);
        } catch (Exception e) {
            return null;
        }
    }
    
    private Map<Line, Integer> calculateEdgeCycleCount(CycleDetection.CycleInfo cycleInfo) {
        Map<Line, Integer> edgeCount = new HashMap<>();
        List<List<Station>> cycles = cycleInfo.getCycles();
        Set<Line> cycleEdges = cycleInfo.getCycleEdges();
        
        for (Line edge : cycleEdges) {
            int count = 0;
            for (List<Station> cycle : cycles) {
                if (isEdgeInCycle(edge, cycle)) {
                    count++;
                }
            }
            edgeCount.put(edge, count);
        }
        return edgeCount;
    }
    
    private boolean isEdgeInCycle(Line edge, List<Station> cycle) {
        String from = edge.getDepartureStationId();
        String to = edge.getArrivalStationId();
        
        for (int i = 0; i < cycle.size(); i++) {
            String current = cycle.get(i).getId();
            String next = cycle.get((i + 1) % cycle.size()).getId();
            if (current.equals(from) && next.equals(to)) {
                return true;
            }
        }
        return false;
    }
    
    private List<Integer> findEdgeCycles(Line edge, CycleDetection.CycleInfo cycleInfo) {
        List<Integer> cycles = new ArrayList<>();
        List<List<Station>> allCycles = cycleInfo.getCycles();
        
        for (int i = 0; i < allCycles.size(); i++) {
            if (isEdgeInCycle(edge, allCycles.get(i))) {
                cycles.add(i + 1);
            }
        }
        return cycles;
    }
    
    /**
     * Displays an error message in the result area.
     * 
     * @param exception the exception that occurred
     */
    private void displayError(Throwable exception) {
        StringBuilder sb = new StringBuilder();
        sb.append("ERROR CALCULATING UPGRADE ORDER\n");
        sb.append("=".repeat(60)).append("\n\n");
        sb.append("Error type: ").append(exception.getClass().getSimpleName()).append("\n");
        sb.append("Message: ").append(exception.getMessage()).append("\n\n");
        
        if (exception.getCause() != null) {
            sb.append("Cause: ").append(exception.getCause().getMessage()).append("\n\n");
        }
        
        if (technicalLogArea != null) {
            Platform.runLater(() -> {
                technicalLogArea.setText(sb.toString());
                technicalLogArea.setStyle(
                    "-fx-font-family: 'Consolas', 'Monaco', monospace; " +
                    "-fx-font-size: 12px; " +
                    "-fx-background-color: #ffebee; " +
                    "-fx-text-fill: #c62828;"
                );
            });
        }
        
        // Show alert dialog
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Error calculating upgrade order");
            alert.setContentText(exception.getMessage());
            alert.showAndWait();
        });
    }
    
    // Data classes for tables
    public static class StationData {
        private final String id;
        private final String name;
        private final int inDegree;
        private final int outDegree;
        private final int totalDegree;
        private final String cycles;
        
        public StationData(String id, String name, int inDegree, int outDegree, int totalDegree, String cycles) {
            this.id = id;
            this.name = name;
            this.inDegree = inDegree;
            this.outDegree = outDegree;
            this.totalDegree = totalDegree;
            this.cycles = cycles;
        }
        
        public String getId() { return id; }
        public String getName() { return name; }
        public int getInDegree() { return inDegree; }
        public int getOutDegree() { return outDegree; }
        public int getTotalDegree() { return totalDegree; }
        public String getCycles() { return cycles; }
    }
    
    public static class CycleData {
        private final int cycleNumber;
        private final int size;
        private final String path;
        private final String impact;
        
        public CycleData(int cycleNumber, int size, String path, String impact) {
            this.cycleNumber = cycleNumber;
            this.size = size;
            this.path = path;
            this.impact = impact;
        }
        
        public int getCycleNumber() { return cycleNumber; }
        public int getSize() { return size; }
        public String getPath() { return path; }
        public String getImpact() { return impact; }
    }
    
    public static class EdgeData {
        private final int rank;
        private final String edge;
        private final String distance;
        private final String cycles;
        private final String recommendation;
        
        public EdgeData(int rank, String edge, String distance, String cycles, String recommendation) {
            this.rank = rank;
            this.edge = edge;
            this.distance = distance;
            this.cycles = cycles;
            this.recommendation = recommendation;
        }
        
        public int getRank() { return rank; }
        public String getEdge() { return edge; }
        public String getDistance() { return distance; }
        public String getCycles() { return cycles; }
        public String getRecommendation() { return recommendation; }
    }
}



