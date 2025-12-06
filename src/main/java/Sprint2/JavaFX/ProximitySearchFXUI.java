package Sprint2.JavaFX;

import Sprint1.Domain.Station;
import Sprint2.Controller.ProximitySearchController;
import Sprint2.Domain.KDTree;
import Sprint2.Domain.SearchFilters;
import Sprint2.Domain.ENUM.Country;
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

public class ProximitySearchFXUI {
    private final ProximitySearchController controller;
    private Stage stage;
    private TableView<KDTree.StationDistance> resultsTable;

    public ProximitySearchFXUI(ProximitySearchController controller) {
        this.controller = controller;
    }

    public void show() {
        stage = new Stage();
        stage.setTitle("Proximity Search");

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #1E293B, #0F172A);");
        root.setPadding(new Insets(25));

        // Top: Title
        VBox topBox = new VBox(8);
        topBox.setAlignment(Pos.CENTER);
        topBox.setPadding(new Insets(0, 0, 25, 0));
        
        Label titleLabel = new Label("Proximity Search (Nearest-N)");
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

        Label nLabel = new Label("Number of Nearest Stations:");
        nLabel.setStyle("-fx-text-fill: #E2E8F0; -fx-font-size: 14px;");
        TextField nField = new TextField();
        nField.setPromptText("Enter N (e.g., 10)");
        nField.setPrefWidth(200);
        nField.setStyle("-fx-background-color: #0F172A; -fx-text-fill: #CBD5E1;");

        // Filters
        Label filtersLabel = new Label("Optional Filters:");
        filtersLabel.setStyle("-fx-text-fill: #E2E8F0; -fx-font-size: 14px; -fx-font-weight: 600;");
        
        Label countryLabel = new Label("Country:");
        countryLabel.setStyle("-fx-text-fill: #E2E8F0; -fx-font-size: 13px;");
        ComboBox<String> countryCombo = new ComboBox<>(FXCollections.observableArrayList("All", "PT", "ES", "FR", "DE", "IT", "GB"));
        countryCombo.setValue("All");
        countryCombo.setPrefWidth(150);
        countryCombo.setStyle("-fx-background-color: #0F172A; -fx-text-fill: #CBD5E1;");

        Label cityLabel = new Label("Is City:");
        cityLabel.setStyle("-fx-text-fill: #E2E8F0; -fx-font-size: 13px;");
        ComboBox<String> cityCombo = new ComboBox<>(FXCollections.observableArrayList("All", "Yes", "No"));
        cityCombo.setValue("All");
        cityCombo.setPrefWidth(150);
        cityCombo.setStyle("-fx-background-color: #0F172A; -fx-text-fill: #CBD5E1;");

        Label mainLabel = new Label("Main Station:");
        mainLabel.setStyle("-fx-text-fill: #E2E8F0; -fx-font-size: 13px;");
        ComboBox<String> mainCombo = new ComboBox<>(FXCollections.observableArrayList("All", "Yes", "No"));
        mainCombo.setValue("All");
        mainCombo.setPrefWidth(150);
        mainCombo.setStyle("-fx-background-color: #0F172A; -fx-text-fill: #CBD5E1;");

        Button searchButton = new Button("Find Nearest Stations");
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
                int n = Integer.parseInt(nField.getText());
                
                SearchFilters filters = createFilters(countryCombo.getValue(), cityCombo.getValue(), mainCombo.getValue());
                performSearch(targetLat, targetLon, n, filters);
            } catch (NumberFormatException ex) {
                showAlert(Alert.AlertType.ERROR, "Error", "Invalid input values");
            }
        });

        formGrid.add(targetLatLabel, 0, 0);
        formGrid.add(targetLatField, 1, 0);
        formGrid.add(targetLonLabel, 0, 1);
        formGrid.add(targetLonField, 1, 1);
        formGrid.add(nLabel, 0, 2);
        formGrid.add(nField, 1, 2);
        formGrid.add(filtersLabel, 0, 3);
        formGrid.add(countryLabel, 0, 4);
        formGrid.add(countryCombo, 1, 4);
        formGrid.add(cityLabel, 0, 5);
        formGrid.add(cityCombo, 1, 5);
        formGrid.add(mainLabel, 0, 6);
        formGrid.add(mainCombo, 1, 6);
        formGrid.add(searchButton, 0, 7, 2, 1);

        // Results table
        VBox tableBox = new VBox(10);
        tableBox.setPadding(new Insets(20, 0, 0, 0));
        
        Label resultsLabel = new Label("Nearest Stations");
        resultsLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: 600; -fx-text-fill: #E2E8F0;");

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

        TableColumn<KDTree.StationDistance, Double> latCol = new TableColumn<>("Latitude");
        latCol.setCellValueFactory(cellData -> {
            Station s = cellData.getValue().getStation();
            return new javafx.beans.property.ReadOnlyObjectWrapper<>(s.getLatitude());
        });
        latCol.setPrefWidth(100);

        TableColumn<KDTree.StationDistance, Double> lonCol = new TableColumn<>("Longitude");
        lonCol.setCellValueFactory(cellData -> {
            Station s = cellData.getValue().getStation();
            return new javafx.beans.property.ReadOnlyObjectWrapper<>(s.getLongitude());
        });
        lonCol.setPrefWidth(100);

        TableColumn<KDTree.StationDistance, String> countryCol = new TableColumn<>("Country");
        countryCol.setCellValueFactory(cellData -> {
            Station s = cellData.getValue().getStation();
            return new javafx.beans.property.ReadOnlyObjectWrapper<>(s.getCountry());
        });
        countryCol.setPrefWidth(100);

        resultsTable.getColumns().addAll(nameCol, distanceCol, latCol, lonCol, countryCol);

        tableBox.getChildren().addAll(resultsLabel, resultsTable);
        
        centerBox.getChildren().addAll(formGrid, tableBox);
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

    private SearchFilters createFilters(String country, String city, String main) {
        Country countryFilter = null;
        if (!country.equals("All")) {
            countryFilter = Country.fromString(country);
        }

        Boolean cityFilter = null;
        if (city.equals("Yes")) {
            cityFilter = true;
        } else if (city.equals("No")) {
            cityFilter = false;
        }

        Boolean mainFilter = null;
        if (main.equals("Yes")) {
            mainFilter = true;
        } else if (main.equals("No")) {
            mainFilter = false;
        }

        return new SearchFilters(cityFilter, mainFilter, countryFilter);
    }

    private void performSearch(double targetLat, double targetLon, int n, SearchFilters filters) {
        try {
            List<KDTree.StationDistance> results = controller.findNearestStations(targetLat, targetLon, n, filters);
            ObservableList<KDTree.StationDistance> observableList = FXCollections.observableArrayList(results);
            resultsTable.setItems(observableList);
            
            showAlert(Alert.AlertType.INFORMATION, "Search Complete", 
                    "Found " + results.size() + " nearest station(s).");
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

