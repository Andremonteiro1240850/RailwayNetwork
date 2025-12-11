package Sprint2.JavaFX;

import Sprint2.Domain.Station;
import Sprint2.Controller.GeographicalSearchController;
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

public class GeographicalSearchFXUI {
    private final GeographicalSearchController controller;
    private Stage stage;
    private TableView<Station> resultsTable;

    public GeographicalSearchFXUI(GeographicalSearchController controller) {
        this.controller = controller;
    }

    public void show() {
        stage = new Stage();
        stage.setTitle("Geographical Area Search");

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #1E293B, #0F172A);");
        root.setPadding(new Insets(25));

        // Top: Title
        VBox topBox = new VBox(8);
        topBox.setAlignment(Pos.CENTER);
        topBox.setPadding(new Insets(0, 0, 25, 0));
        
        Label titleLabel = new Label("Geographical Area Search");
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

        // Latitude range
        Label latLabel = new Label("Latitude Range:");
        latLabel.setStyle("-fx-text-fill: #E2E8F0; -fx-font-size: 14px;");
        HBox latBox = new HBox(10);
        TextField latMinField = new TextField();
        latMinField.setPromptText("Min (-90 to 90)");
        latMinField.setPrefWidth(120);
        latMinField.setStyle("-fx-background-color: #0F172A; -fx-text-fill: #CBD5E1;");
        Label latToLabel = new Label("to");
        latToLabel.setStyle("-fx-text-fill: #E2E8F0;");
        TextField latMaxField = new TextField();
        latMaxField.setPromptText("Max (-90 to 90)");
        latMaxField.setPrefWidth(120);
        latMaxField.setStyle("-fx-background-color: #0F172A; -fx-text-fill: #CBD5E1;");
        latBox.getChildren().addAll(latMinField, latToLabel, latMaxField);

        // Longitude range
        Label lonLabel = new Label("Longitude Range:");
        lonLabel.setStyle("-fx-text-fill: #E2E8F0; -fx-font-size: 14px;");
        HBox lonBox = new HBox(10);
        TextField lonMinField = new TextField();
        lonMinField.setPromptText("Min (-180 to 180)");
        lonMinField.setPrefWidth(120);
        lonMinField.setStyle("-fx-background-color: #0F172A; -fx-text-fill: #CBD5E1;");
        Label lonToLabel = new Label("to");
        lonToLabel.setStyle("-fx-text-fill: #E2E8F0;");
        TextField lonMaxField = new TextField();
        lonMaxField.setPromptText("Max (-180 to 180)");
        lonMaxField.setPrefWidth(120);
        lonMaxField.setStyle("-fx-background-color: #0F172A; -fx-text-fill: #CBD5E1;");
        lonBox.getChildren().addAll(lonMinField, lonToLabel, lonMaxField);

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

        Button searchButton = new Button("Search");
        searchButton.setPrefWidth(200);
        searchButton.setPrefHeight(50);
        searchButton.setStyle("-fx-font-size: 16px; -fx-font-weight: 600; " +
                             "-fx-background-color: #8B5CF6; " +
                             "-fx-text-fill: #FFFFFF; " +
                             "-fx-background-radius: 8; " +
                             "-fx-cursor: hand;");
        searchButton.setOnAction(e -> {
            try {
                double latMin = Double.parseDouble(latMinField.getText());
                double latMax = Double.parseDouble(latMaxField.getText());
                double lonMin = Double.parseDouble(lonMinField.getText());
                double lonMax = Double.parseDouble(lonMaxField.getText());
                
                SearchFilters filters = createFilters(countryCombo.getValue(), cityCombo.getValue(), mainCombo.getValue());
                performSearch(latMin, latMax, lonMin, lonMax, filters);
            } catch (NumberFormatException ex) {
                showAlert(Alert.AlertType.ERROR, "Error", "Invalid coordinate values");
            }
        });

        formGrid.add(latLabel, 0, 0);
        formGrid.add(latBox, 1, 0);
        formGrid.add(lonLabel, 0, 1);
        formGrid.add(lonBox, 1, 1);
        formGrid.add(filtersLabel, 0, 2);
        formGrid.add(countryLabel, 0, 3);
        formGrid.add(countryCombo, 1, 3);
        formGrid.add(cityLabel, 0, 4);
        formGrid.add(cityCombo, 1, 4);
        formGrid.add(mainLabel, 0, 5);
        formGrid.add(mainCombo, 1, 5);
        formGrid.add(searchButton, 0, 6, 2, 1);

        // Results table
        VBox tableBox = new VBox(10);
        tableBox.setPadding(new Insets(20, 0, 0, 0));
        
        Label resultsLabel = new Label("Search Results");
        resultsLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: 600; -fx-text-fill: #E2E8F0;");

        resultsTable = new TableView<>();
        resultsTable.setPrefHeight(400);
        resultsTable.setStyle("-fx-background-color: #FFFFFF;");

        TableColumn<Station, String> nameCol = new TableColumn<>("Station Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setPrefWidth(200);

        TableColumn<Station, Double> latCol = new TableColumn<>("Latitude");
        latCol.setCellValueFactory(new PropertyValueFactory<>("latitude"));
        latCol.setPrefWidth(100);

        TableColumn<Station, Double> lonCol = new TableColumn<>("Longitude");
        lonCol.setCellValueFactory(new PropertyValueFactory<>("longitude"));
        lonCol.setPrefWidth(100);

        TableColumn<Station, String> countryCol = new TableColumn<>("Country");
        countryCol.setCellValueFactory(new PropertyValueFactory<>("country"));
        countryCol.setPrefWidth(100);

        TableColumn<Station, Boolean> cityCol = new TableColumn<>("Is City");
        cityCol.setCellValueFactory(new PropertyValueFactory<>("city"));
        cityCol.setPrefWidth(80);

        TableColumn<Station, Boolean> mainCol = new TableColumn<>("Main Station");
        mainCol.setCellValueFactory(new PropertyValueFactory<>("mainStation"));
        mainCol.setPrefWidth(100);

        resultsTable.getColumns().addAll(nameCol, latCol, lonCol, countryCol, cityCol, mainCol);

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

    private void performSearch(double latMin, double latMax, double lonMin, double lonMax, SearchFilters filters) {
        try {
            List<Station> results = controller.searchByGeographicalArea(latMin, latMax, lonMin, lonMax, filters);
            ObservableList<Station> observableList = FXCollections.observableArrayList(results);
            resultsTable.setItems(observableList);
            
            showAlert(Alert.AlertType.INFORMATION, "Search Complete", 
                    "Found " + results.size() + " station(s) in the specified area.");
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

