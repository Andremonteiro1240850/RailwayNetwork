package Sprint1.JavaFX;

import Sprint1.Controller.PickPathController;
import Sprint1.Domain.PickPath;
import Sprint1.Domain.PickStop;
import Sprint1.Domain.Trolley;
import Sprint1.Domain.ENUM.PathStrategy;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.List;
import java.util.Map;

public class PickPathFXUI {
    private final PickPathController controller;
    private Stage stage;
    private TextArea outputArea;
    private TableView<PickStop> ascSweepTable;
    private TableView<PickStop> nearestNeighbourTable;

    public PickPathFXUI(PickPathController controller) {
        this.controller = controller;
    }

    public void show() {
        stage = new Stage();
        stage.setTitle("Pick Path Sequencing");

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #1E293B, #0F172A);");
        root.setPadding(new Insets(25));

        // Top: Title
        VBox topBox = new VBox(8);
        topBox.setAlignment(Pos.CENTER);
        topBox.setPadding(new Insets(0, 0, 25, 0));
        
        Label titleLabel = new Label("Pick Path Sequencing");
        titleLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: 600; " +
                           "-fx-text-fill: linear-gradient(to right, #60A5FA, #3B82F6, #60A5FA);");
        
        Separator separator = new Separator();
        separator.setPrefWidth(500);
        separator.setStyle("-fx-background-color: linear-gradient(to right, transparent, #3B82F6, transparent);");
        
        topBox.getChildren().addAll(titleLabel, separator);
        root.setTop(topBox);

        // Center: Buttons and results
        VBox centerBox = new VBox(20);
        centerBox.setPadding(new Insets(20));

        // Buttons
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        
        Button processTrolleysButton = new Button("Process from Trolleys (US03)");
        processTrolleysButton.setPrefWidth(250);
        processTrolleysButton.setPrefHeight(50);
        processTrolleysButton.setStyle("-fx-font-size: 15px; -fx-font-weight: 600; " +
                                      "-fx-background-color: #2563EB; " +
                                      "-fx-text-fill: #FFFFFF; " +
                                      "-fx-background-radius: 8; " +
                                      "-fx-cursor: hand;");
        processTrolleysButton.setOnAction(e -> processFromTrolleys());
        
        buttonBox.getChildren().add(processTrolleysButton);

        // Results area with tabs
        TabPane resultsTabPane = new TabPane();
        resultsTabPane.setPrefHeight(500);
        resultsTabPane.setStyle("-fx-background-color: #0F172A;");

        Tab ascSweepTab = new Tab("ASC SWEEP Strategy");
        ascSweepTab.setClosable(false);
        ascSweepTab.setContent(createAscSweepTableView());
        resultsTabPane.getTabs().add(ascSweepTab);

        Tab nearestNeighbourTab = new Tab("NEAREST NEIGHBOUR Strategy");
        nearestNeighbourTab.setClosable(false);
        nearestNeighbourTab.setContent(createNearestNeighbourTableView());
        resultsTabPane.getTabs().add(nearestNeighbourTab);

        // Output area
        outputArea = new TextArea();
        outputArea.setEditable(false);
        outputArea.setPrefHeight(150);
        outputArea.setStyle("-fx-font-family: 'Consolas', 'Courier New', monospace; " +
                           "-fx-font-size: 12px; " +
                           "-fx-background-color: #FFFFFF; " +
                           "-fx-text-fill: #000000;");

        centerBox.getChildren().addAll(buttonBox, resultsTabPane, outputArea);
        root.setCenter(centerBox);

        // Bottom: Close button
        HBox bottomBox = new HBox();
        bottomBox.setAlignment(Pos.CENTER);
        bottomBox.setPadding(new Insets(20, 0, 0, 0));
        
        Button closeButton = new Button("Close");
        closeButton.setPrefWidth(200);
        closeButton.setPrefHeight(50);
        closeButton.setStyle("-fx-font-size: 15px; -fx-font-weight: 500; " +
                            "-fx-background-color: linear-gradient(to right, #475569, #64748B); " +
                            "-fx-text-fill: #E2E8F0; " +
                            "-fx-background-radius: 8; " +
                            "-fx-cursor: hand;");
        closeButton.setOnAction(e -> stage.close());
        
        bottomBox.getChildren().add(closeButton);
        root.setBottom(bottomBox);

        Scene scene = new Scene(root, 1000, 750);
        stage.setScene(scene);
        stage.setResizable(true);
        stage.show();
    }

    private VBox createAscSweepTableView() {
        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(20));
        vbox.setStyle("-fx-background-color: #0F172A;");

        ascSweepTable = new TableView<>();
        ascSweepTable.setPrefHeight(450);
        ascSweepTable.setStyle("-fx-background-color: #FFFFFF;");

        TableColumn<PickStop, Integer> seqCol = new TableColumn<>("Sequence");
        seqCol.setCellValueFactory(new PropertyValueFactory<>("sequence"));
        seqCol.setPrefWidth(100);

        TableColumn<PickStop, Integer> aisleCol = new TableColumn<>("Aisle");
        aisleCol.setCellValueFactory(new PropertyValueFactory<>("aisle"));
        aisleCol.setPrefWidth(100);

        TableColumn<PickStop, Integer> bayCol = new TableColumn<>("Bay");
        bayCol.setCellValueFactory(new PropertyValueFactory<>("bay"));
        bayCol.setPrefWidth(100);

        TableColumn<PickStop, Integer> qtyCol = new TableColumn<>("Quantity");
        qtyCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        qtyCol.setPrefWidth(100);

        ascSweepTable.getColumns().addAll(seqCol, aisleCol, bayCol, qtyCol);

        vbox.getChildren().add(ascSweepTable);
        return vbox;
    }

    private VBox createNearestNeighbourTableView() {
        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(20));
        vbox.setStyle("-fx-background-color: #0F172A;");

        nearestNeighbourTable = new TableView<>();
        nearestNeighbourTable.setPrefHeight(450);
        nearestNeighbourTable.setStyle("-fx-background-color: #FFFFFF;");

        TableColumn<PickStop, Integer> seqCol = new TableColumn<>("Sequence");
        seqCol.setCellValueFactory(new PropertyValueFactory<>("sequence"));
        seqCol.setPrefWidth(100);

        TableColumn<PickStop, Integer> aisleCol = new TableColumn<>("Aisle");
        aisleCol.setCellValueFactory(new PropertyValueFactory<>("aisle"));
        aisleCol.setPrefWidth(100);

        TableColumn<PickStop, Integer> bayCol = new TableColumn<>("Bay");
        bayCol.setCellValueFactory(new PropertyValueFactory<>("bay"));
        bayCol.setPrefWidth(100);

        TableColumn<PickStop, Integer> qtyCol = new TableColumn<>("Quantity");
        qtyCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        qtyCol.setPrefWidth(100);

        nearestNeighbourTable.getColumns().addAll(seqCol, aisleCol, bayCol, qtyCol);

        vbox.getChildren().add(nearestNeighbourTable);
        return vbox;
    }

    private void processFromTrolleys() {
        clearOutput();
        appendOutput("=== PROCESSING FROM TROLLEYS ===");
        
        try {
            List<Trolley> trolleys = controller.getTrolleysFromUS03();
            
            if (trolleys.isEmpty()) {
                appendOutput("No trolleys found. Please create picking plans first.");
                showAlert(Alert.AlertType.WARNING, "Warning", "No trolleys found. Please create picking plans first.");
                return;
            }
            
            appendOutput("Found " + trolleys.size() + " trolleys");
            
            // Convert all trolleys to stops
            List<PickStop> allStops = new java.util.ArrayList<>();
            for (Trolley trolley : trolleys) {
                for (Sprint1.Domain.PickingItems item : trolley.getPickingItems()) {
                    allStops.add(new PickStop(allStops.size() + 1, item.getAisleId(), item.getBayId(), item.getQty()));
                }
            }
            
            appendOutput("Processing " + allStops.size() + " stops...");
            
            Map<PathStrategy, PickPath> results = controller.processPickPathSequencing(allStops);
            
            PickPath ascSweep = results.get(PathStrategy.ASC_SWEEP);
            PickPath nearestNeighbour = results.get(PathStrategy.NEAREST_NEIGHBOUR);
            
            // Update tables
            ObservableList<PickStop> ascSweepList = FXCollections.observableArrayList(ascSweep.getStops());
            ascSweepTable.setItems(ascSweepList);
            
            ObservableList<PickStop> nearestNeighbourList = FXCollections.observableArrayList(nearestNeighbour.getStops());
            nearestNeighbourTable.setItems(nearestNeighbourList);
            
            appendOutput("\n=== RESULTS ===");
            appendOutput("ASC SWEEP - Total Distance: " + ascSweep.getTotalDistance());
            appendOutput("NEAREST NEIGHBOUR - Total Distance: " + nearestNeighbour.getTotalDistance());
            
            if (ascSweep.getTotalDistance() < nearestNeighbour.getTotalDistance()) {
                appendOutput("\nASC SWEEP is more efficient!");
            } else if (nearestNeighbour.getTotalDistance() < ascSweep.getTotalDistance()) {
                appendOutput("\nNEAREST NEIGHBOUR is more efficient!");
            } else {
                appendOutput("\nBoth strategies have the same distance!");
            }
            
        } catch (Exception e) {
            appendOutput("Error: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to process: " + e.getMessage());
        }
    }

    private void appendOutput(String text) {
        Platform.runLater(() -> {
            if (outputArea != null) {
                outputArea.appendText(text + "\n");
                outputArea.setScrollTop(Double.MAX_VALUE);
            }
        });
    }

    private void clearOutput() {
        Platform.runLater(() -> {
            if (outputArea != null) {
                outputArea.clear();
            }
        });
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(content);
            alert.showAndWait();
        });
    }
}

