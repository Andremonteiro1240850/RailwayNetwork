package Sprint2.JavaFX;

import Sprint2.Domain.Station;
import Sprint2.Controller.StationController;
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

public class StationLookupFXUI {
    private final StationController controller;
    private Stage stage;
    private TableView<Station> stationTable;

    public StationLookupFXUI(StationController controller) {
        this.controller = controller;
    }

    public void show() {
        stage = new Stage();
        stage.setTitle("Time-Zone Queries (Station Lookup)");

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #1E293B, #0F172A);");
        root.setPadding(new Insets(25));

        // Top: Title
        VBox topBox = new VBox(8);
        topBox.setAlignment(Pos.CENTER);
        topBox.setPadding(new Insets(0, 0, 25, 0));
        
        Label titleLabel = new Label("Time-Zone Queries (Station Lookup)");
        titleLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: 600; " +
                           "-fx-text-fill: linear-gradient(to right, #8B5CF6, #7C3AED, #8B5CF6);");
        
        Separator separator = new Separator();
        separator.setPrefWidth(500);
        separator.setStyle("-fx-background-color: linear-gradient(to right, transparent, #8B5CF6, transparent);");
        
        topBox.getChildren().addAll(titleLabel, separator);
        root.setTop(topBox);

        // Center: Query options and results
        VBox centerBox = new VBox(20);
        centerBox.setPadding(new Insets(20));

        // Query options
        GridPane queryGrid = new GridPane();
        queryGrid.setHgap(15);
        queryGrid.setVgap(15);
        queryGrid.setPadding(new Insets(20));
        queryGrid.setStyle("-fx-background-color: #1E293B; -fx-background-radius: 8;");

        // Query by Country
        Label countryLabel = new Label("Query by Country:");
        countryLabel.setStyle("-fx-text-fill: #E2E8F0; -fx-font-size: 14px;");
        TextField countryField = new TextField();
        countryField.setPromptText("Enter country code (e.g., PT, ES)");
        countryField.setPrefWidth(200);
        countryField.setStyle("-fx-background-color: #0F172A; -fx-text-fill: #CBD5E1;");
        Button countryButton = createStyledButton("Search", "#8B5CF6", () -> searchByCountry(countryField.getText()));

        // Query by Time Zone
        Label timeZoneLabel = new Label("Query by Time Zone:");
        timeZoneLabel.setStyle("-fx-text-fill: #E2E8F0; -fx-font-size: 14px;");
        TextField timeZoneField = new TextField();
        timeZoneField.setPromptText("Enter time zone group");
        timeZoneField.setPrefWidth(200);
        timeZoneField.setStyle("-fx-background-color: #0F172A; -fx-text-fill: #CBD5E1;");
        Button timeZoneButton = createStyledButton("Search", "#8B5CF6", () -> searchByTimeZone(timeZoneField.getText()));

        // Query by Latitude Range
        Label latLabel = new Label("Query by Latitude Range:");
        latLabel.setStyle("-fx-text-fill: #E2E8F0; -fx-font-size: 14px;");
        HBox latBox = new HBox(10);
        TextField latMinField = new TextField();
        latMinField.setPromptText("Min (-90 to 90)");
        latMinField.setPrefWidth(100);
        latMinField.setStyle("-fx-background-color: #0F172A; -fx-text-fill: #CBD5E1;");
        Label latToLabel = new Label("to");
        latToLabel.setStyle("-fx-text-fill: #E2E8F0;");
        TextField latMaxField = new TextField();
        latMaxField.setPromptText("Max (-90 to 90)");
        latMaxField.setPrefWidth(100);
        latMaxField.setStyle("-fx-background-color: #0F172A; -fx-text-fill: #CBD5E1;");
        Button latButton = createStyledButton("Search", "#8B5CF6", () -> {
            try {
                double min = Double.parseDouble(latMinField.getText());
                double max = Double.parseDouble(latMaxField.getText());
                searchByLatitude(min, max);
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Invalid latitude values");
            }
        });
        latBox.getChildren().addAll(latMinField, latToLabel, latMaxField, latButton);

        // Query by Longitude Range
        Label lonLabel = new Label("Query by Longitude Range:");
        lonLabel.setStyle("-fx-text-fill: #E2E8F0; -fx-font-size: 14px;");
        HBox lonBox = new HBox(10);
        TextField lonMinField = new TextField();
        lonMinField.setPromptText("Min (-180 to 180)");
        lonMinField.setPrefWidth(100);
        lonMinField.setStyle("-fx-background-color: #0F172A; -fx-text-fill: #CBD5E1;");
        Label lonToLabel = new Label("to");
        lonToLabel.setStyle("-fx-text-fill: #E2E8F0;");
        TextField lonMaxField = new TextField();
        lonMaxField.setPromptText("Max (-180 to 180)");
        lonMaxField.setPrefWidth(100);
        lonMaxField.setStyle("-fx-background-color: #0F172A; -fx-text-fill: #CBD5E1;");
        Button lonButton = createStyledButton("Search", "#8B5CF6", () -> {
            try {
                double min = Double.parseDouble(lonMinField.getText());
                double max = Double.parseDouble(lonMaxField.getText());
                searchByLongitude(min, max);
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Invalid longitude values");
            }
        });
        lonBox.getChildren().addAll(lonMinField, lonToLabel, lonMaxField, lonButton);

        queryGrid.add(countryLabel, 0, 0);
        queryGrid.add(countryField, 1, 0);
        queryGrid.add(countryButton, 2, 0);
        queryGrid.add(timeZoneLabel, 0, 1);
        queryGrid.add(timeZoneField, 1, 1);
        queryGrid.add(timeZoneButton, 2, 1);
        queryGrid.add(latLabel, 0, 2);
        queryGrid.add(latBox, 1, 2, 2, 1);
        queryGrid.add(lonLabel, 0, 3);
        queryGrid.add(lonBox, 1, 3, 2, 1);

        // Results table
        VBox tableBox = new VBox(10);
        tableBox.setPadding(new Insets(20, 0, 0, 0));
        
        Label resultsLabel = new Label("Search Results");
        resultsLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: 600; -fx-text-fill: #E2E8F0;");

        stationTable = new TableView<>();
        stationTable.setPrefHeight(400);
        stationTable.setStyle("-fx-background-color: #FFFFFF;");

        TableColumn<Station, String> nameCol = new TableColumn<>("Station Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setPrefWidth(200);

        TableColumn<Station, String> countryCol = new TableColumn<>("Country");
        countryCol.setCellValueFactory(new PropertyValueFactory<>("country"));
        countryCol.setPrefWidth(100);

        TableColumn<Station, String> timeZoneCol = new TableColumn<>("Time Zone");
        timeZoneCol.setCellValueFactory(new PropertyValueFactory<>("timeZoneGroup"));
        timeZoneCol.setPrefWidth(120);

        TableColumn<Station, Double> latCol = new TableColumn<>("Latitude");
        latCol.setCellValueFactory(new PropertyValueFactory<>("latitude"));
        latCol.setPrefWidth(100);

        TableColumn<Station, Double> lonCol = new TableColumn<>("Longitude");
        lonCol.setCellValueFactory(new PropertyValueFactory<>("longitude"));
        lonCol.setPrefWidth(100);

        TableColumn<Station, Boolean> cityCol = new TableColumn<>("Is City");
        cityCol.setCellValueFactory(new PropertyValueFactory<>("city"));
        cityCol.setPrefWidth(80);

        TableColumn<Station, Boolean> mainCol = new TableColumn<>("Main Station");
        mainCol.setCellValueFactory(new PropertyValueFactory<>("mainStation"));
        mainCol.setPrefWidth(100);

        stationTable.getColumns().addAll(nameCol, countryCol, timeZoneCol, latCol, lonCol, cityCol, mainCol);

        tableBox.getChildren().addAll(resultsLabel, stationTable);
        
        centerBox.getChildren().addAll(queryGrid, tableBox);
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

    private Button createStyledButton(String text, String color, Runnable action) {
        Button button = new Button(text);
        button.setPrefHeight(35);
        button.setStyle("-fx-font-size: 13px; -fx-font-weight: 600; " +
                       "-fx-background-color: " + color + "; " +
                       "-fx-text-fill: #FFFFFF; " +
                       "-fx-background-radius: 6; " +
                       "-fx-cursor: hand;");
        button.setOnAction(e -> action.run());
        return button;
    }

    private void searchByCountry(String country) {
        try {
            List<Station> stations = new ArrayList<>();
            controller.getStationByCountry(country).forEach(stations::add);
            updateTable(stations);
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
        }
    }

    private void searchByTimeZone(String timeZone) {
        try {
            List<Station> stations = new ArrayList<>();
            controller.getStationByTimeZone(timeZone).forEach(stations::add);
            updateTable(stations);
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
        }
    }

    private void searchByLatitude(double min, double max) {
        try {
            List<Station> stations = new ArrayList<>();
            controller.getStationByLatitude(min, max).forEach(stations::add);
            updateTable(stations);
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
        }
    }

    private void searchByLongitude(double min, double max) {
        try {
            List<Station> stations = new ArrayList<>();
            controller.getStationByLongitude(min, max).forEach(stations::add);
            updateTable(stations);
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
        }
    }

    private void updateTable(List<Station> stations) {
        ObservableList<Station> observableList = FXCollections.observableArrayList(stations);
        stationTable.setItems(observableList);
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

