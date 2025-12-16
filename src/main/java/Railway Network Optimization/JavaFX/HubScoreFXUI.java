package JavaFX;

import Controller.HubController;
import Domain.Station;
import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.collections.ObservableList;
import javafx.collections.FXCollections;
import javafx.beans.property.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * JavaFX UI for Rail Hub Centrality Analysis.
 * Displays hub scores and centrality metrics in a graphical table.
 *
 * @author Development Team
 * @version 1.0
 */
public class HubScoreFXUI {
    private HubController controller;
    private Stage stage;
    private Button calculateButton;
    private TableView<StationMetrics> metricsTable;
    private ProgressIndicator progressIndicator;
    private Label statusLabel;
    private Label summaryLabel;
    private ComboBox<String> sortByComboBox;
    private TextField searchField;

    /**
     * Data model for table rows
     */
    public static class StationMetrics {
        private final StringProperty stationId;
        private final StringProperty stationName;
        private final IntegerProperty degree;
        private final DoubleProperty strength;
        private final DoubleProperty betweenness;
        private final DoubleProperty harmonicCloseness;
        private final DoubleProperty hubScore;

        public StationMetrics(String id, String name, int degree, double strength,
                              double betweenness, double harmonic, double score) {
            this.stationId = new SimpleStringProperty(id);
            this.stationName = new SimpleStringProperty(name);
            this.degree = new SimpleIntegerProperty(degree);
            this.strength = new SimpleDoubleProperty(strength);
            this.betweenness = new SimpleDoubleProperty(betweenness);
            this.harmonicCloseness = new SimpleDoubleProperty(harmonic);
            this.hubScore = new SimpleDoubleProperty(score);
        }

        // Getters for properties
        public StringProperty stationIdProperty() { return stationId; }
        public StringProperty stationNameProperty() { return stationName; }
        public IntegerProperty degreeProperty() { return degree; }
        public DoubleProperty strengthProperty() { return strength; }
        public DoubleProperty betweennessProperty() { return betweenness; }
        public DoubleProperty harmonicClosenessProperty() { return harmonicCloseness; }
        public DoubleProperty hubScoreProperty() { return hubScore; }

        // Getters for values
        public String getStationId() { return stationId.get(); }
        public String getStationName() { return stationName.get(); }
        public int getDegree() { return degree.get(); }
        public double getStrength() { return strength.get(); }
        public double getBetweenness() { return betweenness.get(); }
        public double getHarmonicCloseness() { return harmonicCloseness.get(); }
        public double getHubScore() { return hubScore.get(); }
    }

    /**
     * Constructs a new HubScoreFXUI with the given controller.
     */
    public HubScoreFXUI(HubController controller) {
        this.controller = controller;
    }

    /**
     * Shows the hub score analysis window.
     */
    public void show() {
        stage = new Stage();
        stage.setTitle("Rail Hub Centrality Analysis");

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #1E293B, #0F172A);");
        root.setPadding(new Insets(25));

        // Top: Title and controls
        VBox topBox = createTopSection();
        root.setTop(topBox);

        // Center: Table
        VBox centerBox = createCenterSection();
        root.setCenter(centerBox);

        // Bottom: Status and buttons
        HBox bottomBox = createBottomSection();
        root.setBottom(bottomBox);

        Scene scene = new Scene(root, 1200, 700);
        stage.setScene(scene);
        stage.setResizable(true);
        stage.show();
    }

    private VBox createTopSection() {
        VBox topBox = new VBox(15);
        topBox.setAlignment(Pos.CENTER);
        topBox.setPadding(new Insets(0, 0, 20, 0));

        // Title
        Label titleLabel = new Label("Rail Hub Centrality Analysis");
        titleLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: 600; " +
                "-fx-text-fill: linear-gradient(to right, #60A5FA, #3B82F6, #60A5FA);");

        Separator separator = new Separator();
        separator.setPrefWidth(600);
        separator.setStyle("-fx-background-color: linear-gradient(to right, transparent, #3B82F6, transparent);");

        // Description
        Label descriptionLabel = new Label(
                "Analyze railway network hubs using multiple centrality metrics.\n" +
                        "Higher scores indicate more important stations in the network."
        );
        descriptionLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #94A3B8;");
        descriptionLabel.setWrapText(true);
        descriptionLabel.setAlignment(Pos.CENTER);

        // Control panel
        HBox controlPanel = createControlPanel();

        topBox.getChildren().addAll(titleLabel, separator, descriptionLabel, controlPanel);
        return topBox;
    }

    private HBox createControlPanel() {
        HBox controlPanel = new HBox(15);
        controlPanel.setAlignment(Pos.CENTER);
        controlPanel.setPadding(new Insets(15, 0, 0, 0));

        // Calculate button
        calculateButton = new Button("Calculate Hub Scores");
        calculateButton.setPrefWidth(200);
        calculateButton.setPrefHeight(45);
        calculateButton.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; " +
                "-fx-background-color: #3B82F6; " +
                "-fx-text-fill: #FFFFFF; " +
                "-fx-background-radius: 8; " +
                "-fx-cursor: hand;");
        calculateButton.setOnAction(e -> handleCalculate());

        // Sort combo box
        Label sortLabel = new Label("Sort by:");
        sortLabel.setStyle("-fx-text-fill: #E2E8F0; -fx-font-size: 12px;");

        sortByComboBox = new ComboBox<>();
        sortByComboBox.getItems().addAll(
                "Hub Score (High to Low)",
                "Hub Score (Low to High)",
                "Station Name",
                "Degree",
                "Betweenness",
                "Harmonic Closeness"
        );
        sortByComboBox.setValue("Hub Score (High to Low)");
        sortByComboBox.setPrefWidth(200);
        sortByComboBox.setStyle("-fx-background-color: #334155; -fx-text-fill: #E2E8F0;");
        sortByComboBox.setOnAction(e -> sortTable());
        sortByComboBox.setDisable(true);

        // Search field
        Label searchLabel = new Label("Search:");
        searchLabel.setStyle("-fx-text-fill: #E2E8F0; -fx-font-size: 12px;");

        searchField = new TextField();
        searchField.setPromptText("Station name or ID...");
        searchField.setPrefWidth(200);
        searchField.setStyle("-fx-background-color: #334155; -fx-text-fill: #E2E8F0; " +
                "-fx-prompt-text-fill: #64748B;");
        searchField.textProperty().addListener((obs, oldVal, newVal) -> filterTable());
        searchField.setDisable(true);

        controlPanel.getChildren().addAll(
                calculateButton,
                new Separator(),
                sortLabel, sortByComboBox,
                new Separator(),
                searchLabel, searchField
        );

        return controlPanel;
    }

    private VBox createCenterSection() {
        VBox centerBox = new VBox(15);
        centerBox.setPadding(new Insets(10, 0, 10, 0));
        centerBox.setAlignment(Pos.CENTER);

        // Summary label
        summaryLabel = new Label("");
        summaryLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #94A3B8;");
        summaryLabel.setVisible(false);

        // Create table
        metricsTable = createMetricsTable();

        centerBox.getChildren().addAll(summaryLabel, metricsTable);
        VBox.setVgrow(metricsTable, Priority.ALWAYS);

        return centerBox;
    }

    private TableView<StationMetrics> createMetricsTable() {
        TableView<StationMetrics> table = new TableView<>();
        table.setStyle("-fx-background-color: #FFFFFF; -fx-border-color: #334155; " +
                "-fx-border-radius: 8; -fx-background-radius: 8;");
        table.setPlaceholder(new Label("Click 'Calculate Hub Scores' to analyze the network"));

        // Station ID column
        TableColumn<StationMetrics, String> idColumn = new TableColumn<>("Station ID");
        idColumn.setCellValueFactory(new PropertyValueFactory<>("stationId"));
        idColumn.setPrefWidth(100);
        idColumn.setStyle("-fx-alignment: CENTER;");

        // Station Name column
        TableColumn<StationMetrics, String> nameColumn = new TableColumn<>("Station Name");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("stationName"));
        nameColumn.setPrefWidth(250);

        // Degree column
        TableColumn<StationMetrics, Integer> degreeColumn = new TableColumn<>("Degree");
        degreeColumn.setCellValueFactory(new PropertyValueFactory<>("degree"));
        degreeColumn.setPrefWidth(80);
        degreeColumn.setStyle("-fx-alignment: CENTER-RIGHT;");

        // Strength column (normalizado)
        TableColumn<StationMetrics, Double> strengthColumn = new TableColumn<>("Strength");
        strengthColumn.setCellValueFactory(new PropertyValueFactory<>("strength"));
        strengthColumn.setPrefWidth(100);
        strengthColumn.setStyle("-fx-alignment: CENTER-RIGHT;");
        strengthColumn.setCellFactory(col -> new TableCell<StationMetrics, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%.4f", item));
                }
            }
        });

        // Betweenness column (valores reais)
        TableColumn<StationMetrics, Double> betweennessColumn = new TableColumn<>("Betweenness");
        betweennessColumn.setCellValueFactory(new PropertyValueFactory<>("betweenness"));
        betweennessColumn.setPrefWidth(120);
        betweennessColumn.setStyle("-fx-alignment: CENTER-RIGHT;");
        betweennessColumn.setCellFactory(col -> new TableCell<StationMetrics, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f", item));
                }
            }
        });

        // Harmonic Closeness column (valores reais)
        TableColumn<StationMetrics, Double> harmonicColumn = new TableColumn<>("Harmonic Closeness");
        harmonicColumn.setCellValueFactory(new PropertyValueFactory<>("harmonicCloseness"));
        harmonicColumn.setPrefWidth(150);
        harmonicColumn.setStyle("-fx-alignment: CENTER-RIGHT;");
        harmonicColumn.setCellFactory(col -> new TableCell<StationMetrics, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f", item));
                }
            }
        });

        // Hub Score column - highlighted (normalizado)
        TableColumn<StationMetrics, Double> scoreColumn = new TableColumn<>("Hub Score");
        scoreColumn.setCellValueFactory(new PropertyValueFactory<>("hubScore"));
        scoreColumn.setPrefWidth(120);
        scoreColumn.setStyle("-fx-alignment: CENTER-RIGHT;");
        scoreColumn.setCellFactory(col -> new TableCell<StationMetrics, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(String.format("%.4f", item));
                    // Highlight high scores
                    if (item > 0.7) {
                        setStyle("-fx-background-color: #10B981; -fx-text-fill: white; -fx-font-weight: bold;");
                    } else if (item > 0.4) {
                        setStyle("-fx-background-color: #F59E0B; -fx-text-fill: white; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-font-weight: bold;");
                    }
                }
            }
        });

        table.getColumns().addAll(
                idColumn, nameColumn, degreeColumn, strengthColumn,
                betweennessColumn, harmonicColumn, scoreColumn
        );

        return table;
    }

    private HBox createBottomSection() {
        HBox bottomBox = new HBox(15);
        bottomBox.setAlignment(Pos.CENTER_LEFT);
        bottomBox.setPadding(new Insets(20, 0, 0, 0));

        // Progress indicator
        progressIndicator = new ProgressIndicator();
        progressIndicator.setVisible(false);
        progressIndicator.setPrefSize(20, 20);

        // Status label
        statusLabel = new Label("Ready to calculate");
        statusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #94A3B8;");

        // Close button
        Button closeButton = new Button("Close");
        closeButton.setPrefWidth(150);
        closeButton.setPrefHeight(40);
        closeButton.setStyle("-fx-font-size: 14px; -fx-font-weight: 500; " +
                "-fx-background-color: #475569; " +
                "-fx-text-fill: #E2E8F0; " +
                "-fx-background-radius: 8; " +
                "-fx-cursor: hand;");
        closeButton.setOnAction(e -> stage.close());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        bottomBox.getChildren().addAll(
                progressIndicator, statusLabel, spacer, closeButton
        );

        return bottomBox;
    }

    private ObservableList<StationMetrics> allMetrics = FXCollections.observableArrayList();

    /**
     * Handles the calculate button click event.
     */
    private void handleCalculate() {
        calculateButton.setDisable(true);
        sortByComboBox.setDisable(true);
        searchField.setDisable(true);
        progressIndicator.setVisible(true);
        statusLabel.setText("Calculating metrics...");
        metricsTable.getItems().clear();

        Service<List<StationMetrics>> service = new Service<>() {
            @Override
            protected Task<List<StationMetrics>> createTask() {
                return new Task<>() {
                    @Override
                    protected List<StationMetrics> call() throws Exception {
                        updateMessage("Building graph...");

                        // Calculate all metrics
                        Map<Station, Map<Station, Double>> uGraph =
                                controller.getHubRepository().getLoader().buildGraph();

                        updateMessage("Calculating degree...");
                        Map<Station, Integer> degrees = controller.calculateDegree(uGraph);

                        updateMessage("Calculating strength...");
                        Map<Station, Double> strengths = controller.calculateStrengthNorm(uGraph);

                        updateMessage("Calculating betweenness centrality...");
                        Map<Station, Double> betweennesses =
                                controller.calculateBetweennessCentrality(uGraph);

                        updateMessage("Calculating harmonic closeness...");
                        Map<Station, Double> harmonics =
                                controller.calculateHarmonicCloseness(uGraph);

                        updateMessage("Normalizing metrics...");
                        Map<Station, Double> betweennessNorm =
                                controller.normalizeBetweenness(betweennesses);
                        Map<Station, Double> harmonicsNorm =
                                controller.normalizeHarmonicCloseness(harmonics);

                        updateMessage("Calculating hub scores...");
                        Map<Station, Double> hubScores = controller.calculateHubScore(
                                strengths, harmonicsNorm, betweennessNorm,
                                controller.getHubRepository().getUndirectedStations()
                        );

                        updateMessage("Preparing results...");

                        // Create metrics list
                        List<StationMetrics> metricsList = new ArrayList<>();
                        for (Station s : controller.getHubRepository().getUndirectedStations()) {
                            metricsList.add(new StationMetrics(
                                    s.getId(),
                                    s.getName() != null ? s.getName() : "N/A",
                                    degrees.get(s),
                                    strengths.get(s),
                                    betweennesses.get(s),  // valores reais
                                    harmonics.get(s),      // valores reais
                                    hubScores.get(s)       // normalizado
                            ));
                        }

                        return metricsList;
                    }
                };
            }
        };

        service.messageProperty().addListener((obs, oldMsg, newMsg) ->
                Platform.runLater(() -> statusLabel.setText(newMsg))
        );

        service.setOnSucceeded(e -> {
            List<StationMetrics> metrics = service.getValue();
            allMetrics.clear();
            allMetrics.addAll(metrics);

            // Sort by hub score descending by default
            metrics.sort((a, b) -> Double.compare(b.getHubScore(), a.getHubScore()));
            metricsTable.setItems(FXCollections.observableArrayList(metrics));

            // Update summary
            double avgScore = metrics.stream()
                    .mapToDouble(StationMetrics::getHubScore)
                    .average()
                    .orElse(0.0);
            summaryLabel.setText(String.format(
                    "Analysis complete: %d stations analyzed | Average Hub Score: %.4f",
                    metrics.size(), avgScore
            ));
            summaryLabel.setVisible(true);

            calculateButton.setDisable(false);
            sortByComboBox.setDisable(false);
            searchField.setDisable(false);
            progressIndicator.setVisible(false);
            statusLabel.setText("Calculation completed successfully");
        });

        service.setOnFailed(e -> {
            Throwable exception = service.getException();
            calculateButton.setDisable(false);
            sortByComboBox.setDisable(false);
            searchField.setDisable(false);
            progressIndicator.setVisible(false);
            statusLabel.setText("Error during calculation");

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Calculation Error");
            alert.setHeaderText("Failed to calculate hub scores");
            alert.setContentText(exception.getMessage());
            alert.showAndWait();
        });

        service.start();
    }

    /**
     * Sorts the table based on selected criterion.
     */
    private void sortTable() {
        String sortBy = sortByComboBox.getValue();
        List<StationMetrics> sorted = new ArrayList<>(metricsTable.getItems());

        switch (sortBy) {
            case "Hub Score (High to Low)":
                sorted.sort((a, b) -> Double.compare(b.getHubScore(), a.getHubScore()));
                break;
            case "Hub Score (Low to High)":
                sorted.sort((a, b) -> Double.compare(a.getHubScore(), b.getHubScore()));
                break;
            case "Station Name":
                sorted.sort(Comparator.comparing(StationMetrics::getStationName));
                break;
            case "Degree":
                sorted.sort((a, b) -> Integer.compare(b.getDegree(), a.getDegree()));
                break;
            case "Betweenness":
                sorted.sort((a, b) -> Double.compare(b.getBetweenness(), a.getBetweenness()));
                break;
            case "Harmonic Closeness":
                sorted.sort((a, b) -> Double.compare(b.getHarmonicCloseness(), a.getHarmonicCloseness()));
                break;
        }

        metricsTable.setItems(FXCollections.observableArrayList(sorted));
    }

    /**
     * Filters the table based on search text.
     */
    private void filterTable() {
        String searchText = searchField.getText().toLowerCase().trim();

        if (searchText.isEmpty()) {
            metricsTable.setItems(FXCollections.observableArrayList(allMetrics));
            sortTable(); // Reapply sort
        } else {
            List<StationMetrics> filtered = allMetrics.stream()
                    .filter(m ->
                            m.getStationId().toLowerCase().contains(searchText) ||
                                    m.getStationName().toLowerCase().contains(searchText)
                    )
                    .collect(Collectors.toList());
            metricsTable.setItems(FXCollections.observableArrayList(filtered));
        }
    }
}