package Sprint2.JavaFX;

import Sprint1.Domain.Station;
import Sprint2.Controller.RadiusSearchController;
import Sprint2.Domain.AVL;
import Sprint2.Domain.KDTree;
import Sprint2.Domain.RadiusSearchSummary;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RadiusSearchFXUI {
    private final RadiusSearchController controller;
    private Stage stage;
    private TableView<KDTree.StationDistance> resultsTable;
    private TextArea summaryArea;

    public RadiusSearchFXUI(RadiusSearchController controller) {
        this.controller = controller;
    }

    public void show() {
        stage = new Stage();
        stage.setTitle("Radius Search");

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #1E293B, #0F172A);");
        root.setPadding(new Insets(25));

        // Top: Title
        VBox topBox = new VBox(8);
        topBox.setAlignment(Pos.CENTER);
        topBox.setPadding(new Insets(0, 0, 25, 0));
        
        Label titleLabel = new Label("Radius Search & Density Summary");
        titleLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: 600; " +
                           "-fx-text-fill: linear-gradient(to right, #8B5CF6, #7C3AED, #8B5CF6);");
        
        Separator separator = new Separator();
        separator.setPrefWidth(500);
        separator.setStyle("-fx-background-color: linear-gradient(to right, transparent, #8B5CF6, transparent);");
        
        topBox.getChildren().addAll(titleLabel, separator);
        root.setTop(topBox);

        // Center: Search form and results
        VBox centerBox = new VBox(20);
        centerBox.setPadding(new Insets(20));

        // Search form
        GridPane formGrid = new GridPane();
        formGrid.setHgap(15);
        formGrid.setVgap(15);
        formGrid.setPadding(new Insets(20));
        formGrid.setStyle("-fx-background-color: #1E293B; -fx-background-radius: 8;");

        Label targetLatLabel = new Label("Target Latitude:");
        targetLatLabel.setStyle("-fx-text-fill: #E2E8F0; -fx-font-size: 14px;");
        TextField targetLatField = new TextField();
        targetLatField.setPromptText("Latitude (-90 to 90)");
        targetLatField.setPrefWidth(200);
        targetLatField.setStyle("-fx-background-color: #0F172A; -fx-text-fill: #CBD5E1;");

        Label targetLonLabel = new Label("Target Longitude:");
        targetLonLabel.setStyle("-fx-text-fill: #E2E8F0; -fx-font-size: 14px;");
        TextField targetLonField = new TextField();
        targetLonField.setPromptText("Longitude (-180 to 180)");
        targetLonField.setPrefWidth(200);
        targetLonField.setStyle("-fx-background-color: #0F172A; -fx-text-fill: #CBD5E1;");

        Label radiusLabel = new Label("Search Radius (km):");
        radiusLabel.setStyle("-fx-text-fill: #E2E8F0; -fx-font-size: 14px;");
        TextField radiusField = new TextField();
        radiusField.setPromptText("Radius in kilometers");
        radiusField.setPrefWidth(200);
        radiusField.setStyle("-fx-background-color: #0F172A; -fx-text-fill: #CBD5E1;");

        Button searchButton = new Button("Search Within Radius");
        searchButton.setPrefWidth(250);
        searchButton.setPrefHeight(50);
        searchButton.setStyle("-fx-font-size: 16px; -fx-font-weight: 600; " +
                             "-fx-background-color: #8B5CF6; " +
                             "-fx-text-fill: #FFFFFF; " +
                             "-fx-background-radius: 8; " +
                             "-fx-cursor: hand;");
        searchButton.setOnAction(e -> {
            try {
                double targetLat = Double.parseDouble(targetLatField.getText());
                double targetLon = Double.parseDouble(targetLonField.getText());
                double radius = Double.parseDouble(radiusField.getText());
                performSearch(targetLat, targetLon, radius);
            } catch (NumberFormatException ex) {
                showAlert(Alert.AlertType.ERROR, "Error", "Invalid input values");
            }
        });

        formGrid.add(targetLatLabel, 0, 0);
        formGrid.add(targetLatField, 1, 0);
        formGrid.add(targetLonLabel, 0, 1);
        formGrid.add(targetLonField, 1, 1);
        formGrid.add(radiusLabel, 0, 2);
        formGrid.add(radiusField, 1, 2);
        formGrid.add(searchButton, 0, 3, 2, 1);

        // Results area with tabs
        TabPane resultsTabPane = new TabPane();
        resultsTabPane.setPrefHeight(450);
        resultsTabPane.setStyle("-fx-background-color: #0F172A;");

        // Summary Tab
        Tab summaryTab = new Tab("Density Summary");
        summaryTab.setClosable(false);
        summaryTab.setContent(createSummaryView());
        resultsTabPane.getTabs().add(summaryTab);

        // Stations Tab
        Tab stationsTab = new Tab("Stations");
        stationsTab.setClosable(false);
        stationsTab.setContent(createStationsTableView());
        resultsTabPane.getTabs().add(stationsTab);

        centerBox.getChildren().addAll(formGrid, resultsTabPane);
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

        Scene scene = new Scene(root, 1000, 800);
        stage.setScene(scene);
        stage.setResizable(true);
        stage.show();
    }

    private VBox createSummaryView() {
        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(20));
        vbox.setStyle("-fx-background-color: #0F172A;");

        summaryArea = new TextArea();
        summaryArea.setEditable(false);
        summaryArea.setPrefHeight(400);
        summaryArea.setStyle("-fx-font-family: 'Consolas', 'Courier New', monospace; " +
                            "-fx-font-size: 12px; " +
                            "-fx-background-color: #FFFFFF; " +
                            "-fx-text-fill: #000000; " +
                            "-fx-border-color: #334155; " +
                            "-fx-border-radius: 8; " +
                            "-fx-background-radius: 8; " +
                            "-fx-padding: 10;");

        vbox.getChildren().add(summaryArea);
        return vbox;
    }

    private VBox createStationsTableView() {
        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(20));
        vbox.setStyle("-fx-background-color: #0F172A;");

        resultsTable = new TableView<>();
        resultsTable.setPrefHeight(400);
        resultsTable.setStyle("-fx-background-color: #FFFFFF;");

        TableColumn<KDTree.StationDistance, String> nameCol = new TableColumn<>("Station Name");
        nameCol.setCellValueFactory(cellData -> {
            Station s = cellData.getValue().getStation();
            return new javafx.beans.property.ReadOnlyObjectWrapper<>(s.getName());
        });
        nameCol.setPrefWidth(200);

        TableColumn<KDTree.StationDistance, Double> distanceCol = new TableColumn<>("Distance (km)");
        distanceCol.setCellValueFactory(cellData -> {
            double dist = cellData.getValue().getDistance();
            return new javafx.beans.property.ReadOnlyObjectWrapper<>(dist);
        });
        distanceCol.setPrefWidth(120);

        TableColumn<KDTree.StationDistance, String> countryCol = new TableColumn<>("Country");
        countryCol.setCellValueFactory(cellData -> {
            Station s = cellData.getValue().getStation();
            return new javafx.beans.property.ReadOnlyObjectWrapper<>(s.getCountry());
        });
        countryCol.setPrefWidth(100);

        TableColumn<KDTree.StationDistance, Boolean> cityCol = new TableColumn<>("Is City");
        cityCol.setCellValueFactory(cellData -> {
            Station s = cellData.getValue().getStation();
            return new javafx.beans.property.ReadOnlyObjectWrapper<>(s.isCity());
        });
        cityCol.setPrefWidth(80);

        resultsTable.getColumns().addAll(nameCol, distanceCol, countryCol, cityCol);

        vbox.getChildren().add(resultsTable);
        return vbox;
    }

    private void performSearch(double targetLat, double targetLon, double radiusKm) {
        try {
            RadiusSearchController.RadiusSearchResult result = controller.searchRadiusWithSummary(targetLat, targetLon, radiusKm);
            
            RadiusSearchSummary summary = result.getSummary();
            AVL<KDTree.StationDistance> resultTree = result.getResultTree();
            
            // Update summary
            StringBuilder summaryText = new StringBuilder();
            summaryText.append("=== DENSITY SUMMARY ===\n\n");
            summaryText.append("Total Stations: ").append(summary.getTotalStations()).append("\n\n");
            summaryText.append("Distribution by City Status:\n");
            summaryText.append("  Cities:     ").append(summary.getStationsByCity()).append("\n");
            summaryText.append("  Non-Cities: ").append(summary.getStationsByNonCity()).append("\n\n");
            
            Map<String, Integer> byCountry = summary.getStationsByCountry();
            if (!byCountry.isEmpty()) {
                summaryText.append("Distribution by Country:\n");
                byCountry.entrySet().stream()
                    .sorted((a, b) -> {
                        int cmp = b.getValue().compareTo(a.getValue());
                        return cmp != 0 ? cmp : a.getKey().compareTo(b.getKey());
                    })
                    .forEach(entry -> summaryText.append("  ").append(entry.getKey())
                            .append(": ").append(entry.getValue()).append(" station(s)\n"));
            }
            
            summaryArea.setText(summaryText.toString());
            
            // Update stations table
            List<KDTree.StationDistance> stations = new ArrayList<>();
            for (KDTree.StationDistance sd : resultTree.inOrder()) {
                stations.add(sd);
            }
            ObservableList<KDTree.StationDistance> observableList = FXCollections.observableArrayList(stations);
            resultsTable.setItems(observableList);
            
            showAlert(Alert.AlertType.INFORMATION, "Search Complete", 
                    "Found " + summary.getTotalStations() + " station(s) within " + radiusKm + " km.");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
        }
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

