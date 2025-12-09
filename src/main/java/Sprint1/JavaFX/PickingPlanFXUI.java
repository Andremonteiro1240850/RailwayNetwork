package Sprint1.JavaFX;

import Sprint1.Controller.PickingPlanController;
import Sprint1.Domain.ENUM.PackingHeuristic;
import Sprint1.Domain.PickingItems;
import Sprint1.Domain.PickingPlan;
import Sprint1.Domain.Trolley;
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

public class PickingPlanFXUI {
    private final PickingPlanController controller;
    private Stage stage;
    private TextArea outputArea;
    private TableView<Trolley> trolleyTable;
    private TableView<PickingItems> itemsTable;

    public PickingPlanFXUI(PickingPlanController controller) {
        this.controller = controller;
    }

    public void show() {
        stage = new Stage();
        stage.setTitle("Picking Plans");

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #1E293B, #0F172A);");
        root.setPadding(new Insets(25));

        // Top: Title
        VBox topBox = new VBox(8);
        topBox.setAlignment(Pos.CENTER);
        topBox.setPadding(new Insets(0, 0, 25, 0));
        
        Label titleLabel = new Label("Picking Plans");
        titleLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: 600; " +
                           "-fx-text-fill: linear-gradient(to right, #60A5FA, #3B82F6, #60A5FA);");
        
        Separator separator = new Separator();
        separator.setPrefWidth(500);
        separator.setStyle("-fx-background-color: linear-gradient(to right, transparent, #3B82F6, transparent);");
        
        topBox.getChildren().addAll(titleLabel, separator);
        root.setTop(topBox);

        // Center: Input form and results
        VBox centerBox = new VBox(20);
        centerBox.setPadding(new Insets(20));

        // Input form
        GridPane formGrid = new GridPane();
        formGrid.setHgap(15);
        formGrid.setVgap(15);
        formGrid.setPadding(new Insets(20));
        formGrid.setStyle("-fx-background-color: #1E293B; -fx-background-radius: 8;");

        Label capacityLabel = new Label("Trolley Capacity (kg):");
        capacityLabel.setStyle("-fx-text-fill: #E2E8F0; -fx-font-size: 14px;");
        TextField capacityField = new TextField();
        capacityField.setPrefWidth(200);
        capacityField.setStyle("-fx-background-color: #0F172A; -fx-text-fill: #CBD5E1;");

        Label heuristicLabel = new Label("Packing Heuristic:");
        heuristicLabel.setStyle("-fx-text-fill: #E2E8F0; -fx-font-size: 14px;");
        ComboBox<PackingHeuristic> heuristicCombo = new ComboBox<>(
            FXCollections.observableArrayList(PackingHeuristic.FF, PackingHeuristic.FFD, PackingHeuristic.BFD)
        );
        heuristicCombo.setValue(PackingHeuristic.FF);
        heuristicCombo.setPrefWidth(200);
        heuristicCombo.setStyle("-fx-background-color: #0F172A; -fx-text-fill: #CBD5E1;");

        CheckBox allowSplittingCheck = new CheckBox("Allow splitting items that don't fit");
        allowSplittingCheck.setStyle("-fx-text-fill: #E2E8F0;");

        Button createButton = new Button("Create Picking Plan");
        createButton.setPrefWidth(250);
        createButton.setPrefHeight(50);
        createButton.setStyle("-fx-font-size: 16px; -fx-font-weight: 600; " +
                             "-fx-background-color: #2563EB; " +
                             "-fx-text-fill: #FFFFFF; " +
                             "-fx-background-radius: 8; " +
                             "-fx-cursor: hand;");
        createButton.setOnAction(e -> {
            try {
                int capacity = Integer.parseInt(capacityField.getText());
                PackingHeuristic heuristic = heuristicCombo.getValue();
                boolean allowSplitting = allowSplittingCheck.isSelected();
                createPickingPlan(capacity, heuristic, allowSplitting);
            } catch (NumberFormatException ex) {
                showAlert(Alert.AlertType.ERROR, "Error", "Invalid capacity value");
            }
        });

        formGrid.add(capacityLabel, 0, 0);
        formGrid.add(capacityField, 1, 0);
        formGrid.add(heuristicLabel, 0, 1);
        formGrid.add(heuristicCombo, 1, 1);
        formGrid.add(allowSplittingCheck, 0, 2, 2, 1);
        formGrid.add(createButton, 0, 3, 2, 1);

        // Results area
        TabPane resultsTabPane = new TabPane();
        resultsTabPane.setPrefHeight(400);
        resultsTabPane.setStyle("-fx-background-color: #0F172A;");

        Tab trolleysTab = new Tab("Trolleys");
        trolleysTab.setClosable(false);
        trolleysTab.setContent(createTrolleysTableView());
        resultsTabPane.getTabs().add(trolleysTab);

        Tab itemsTab = new Tab("Picking Items");
        itemsTab.setClosable(false);
        itemsTab.setContent(createItemsTableView());
        resultsTabPane.getTabs().add(itemsTab);

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

        Scene scene = new Scene(root, 1000, 750);
        stage.setScene(scene);
        stage.setResizable(true);
        stage.show();
    }

    private VBox createTrolleysTableView() {
        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(20));
        vbox.setStyle("-fx-background-color: #0F172A;");

        trolleyTable = new TableView<>();
        trolleyTable.setPrefHeight(350);
        trolleyTable.setStyle("-fx-background-color: #FFFFFF;");

        TableColumn<Trolley, String> idCol = new TableColumn<>("Trolley ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(120);

        TableColumn<Trolley, Integer> capacityCol = new TableColumn<>("Capacity");
        capacityCol.setCellValueFactory(new PropertyValueFactory<>("maxCapacity"));
        capacityCol.setPrefWidth(100);

        TableColumn<Trolley, Double> loadCol = new TableColumn<>("Current Load");
        loadCol.setCellValueFactory(new PropertyValueFactory<>("currentLoad"));
        loadCol.setPrefWidth(120);

        TableColumn<Trolley, Integer> itemsCountCol = new TableColumn<>("Items Count");
        itemsCountCol.setCellValueFactory(cellData -> {
            Trolley t = cellData.getValue();
            return new javafx.beans.property.ReadOnlyObjectWrapper<>(t.getPickingItems().size());
        });
        itemsCountCol.setPrefWidth(100);

        trolleyTable.getColumns().addAll(idCol, capacityCol, loadCol, itemsCountCol);

        vbox.getChildren().add(trolleyTable);
        return vbox;
    }

    private VBox createItemsTableView() {
        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(20));
        vbox.setStyle("-fx-background-color: #0F172A;");

        itemsTable = new TableView<>();
        itemsTable.setPrefHeight(350);
        itemsTable.setStyle("-fx-background-color: #FFFFFF;");

        TableColumn<PickingItems, String> orderIdCol = new TableColumn<>("Order ID");
        orderIdCol.setCellValueFactory(new PropertyValueFactory<>("orderId"));
        orderIdCol.setPrefWidth(120);

        TableColumn<PickingItems, Integer> lineNoCol = new TableColumn<>("Line No");
        lineNoCol.setCellValueFactory(new PropertyValueFactory<>("lineNo"));
        lineNoCol.setPrefWidth(80);

        TableColumn<PickingItems, Integer> aisleCol = new TableColumn<>("Aisle");
        aisleCol.setCellValueFactory(new PropertyValueFactory<>("aisleId"));
        aisleCol.setPrefWidth(80);

        TableColumn<PickingItems, Integer> bayCol = new TableColumn<>("Bay");
        bayCol.setCellValueFactory(new PropertyValueFactory<>("bayId"));
        bayCol.setPrefWidth(80);

        TableColumn<PickingItems, String> boxIdCol = new TableColumn<>("Box ID");
        boxIdCol.setCellValueFactory(new PropertyValueFactory<>("boxId"));
        boxIdCol.setPrefWidth(150);

        TableColumn<PickingItems, String> skuCol = new TableColumn<>("SKU");
        skuCol.setCellValueFactory(new PropertyValueFactory<>("sku"));
        skuCol.setPrefWidth(100);

        TableColumn<PickingItems, Integer> qtyCol = new TableColumn<>("Quantity");
        qtyCol.setCellValueFactory(new PropertyValueFactory<>("qty"));
        qtyCol.setPrefWidth(100);

        itemsTable.getColumns().addAll(orderIdCol, lineNoCol, aisleCol, bayCol, boxIdCol, skuCol, qtyCol);

        vbox.getChildren().add(itemsTable);
        return vbox;
    }

    private void createPickingPlan(int capacity, PackingHeuristic heuristic, boolean allowSplitting) {
        PickingPlan plan = controller.createPickingPlan(capacity, heuristic, allowSplitting);
        
        // Update trolleys table
        ObservableList<Trolley> trolleyList = FXCollections.observableArrayList(plan.getTrolleys());
        trolleyTable.setItems(trolleyList);
        
        // Collect all items from all trolleys
        List<PickingItems> allItems = plan.getTrolleys().stream()
            .flatMap(t -> t.getPickingItems().stream())
            .collect(java.util.stream.Collectors.toList());
        ObservableList<PickingItems> itemsList = FXCollections.observableArrayList(allItems);
        itemsTable.setItems(itemsList);
        
        showAlert(Alert.AlertType.INFORMATION, "Success", 
                 "Created picking plan with " + plan.getTotalTrolleys() + " trolleys.\n" +
                 "Average utilization: " + String.format("%.2f%%", plan.getAverageUtilization()));
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

