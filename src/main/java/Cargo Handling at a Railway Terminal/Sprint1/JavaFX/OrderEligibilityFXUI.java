package Sprint1.JavaFX;

import Sprint1.Controller.WarehouseController;
import Sprint1.Domain.AllocationRow;
import Sprint1.Domain.AllocationResult;
import Sprint1.Domain.EligibilityResult;
import Sprint1.Domain.ENUM.AllocationMode;
import Sprint1.Repository.BoxRepository;
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

public class OrderEligibilityFXUI {
    private final WarehouseController controller;
    private final BoxRepository boxRepository;
    private Stage stage;
    private TableView<EligibilityResult> eligibilityTable;
    private TableView<AllocationRow> allocationTable;

    public OrderEligibilityFXUI(WarehouseController controller, BoxRepository boxRepository) {
        this.controller = controller;
        this.boxRepository = boxRepository;
    }

    public void show() {
        stage = new Stage();
        stage.setTitle("Order Eligibility & Allocation");

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #1E293B, #0F172A);");
        root.setPadding(new Insets(25));

        // Top: Title
        VBox topBox = new VBox(8);
        topBox.setAlignment(Pos.CENTER);
        topBox.setPadding(new Insets(0, 0, 25, 0));
        
        Label titleLabel = new Label("Order Eligibility & Allocation");
        titleLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: 600; " +
                           "-fx-text-fill: linear-gradient(to right, #60A5FA, #3B82F6, #60A5FA);");
        
        Separator separator = new Separator();
        separator.setPrefWidth(500);
        separator.setStyle("-fx-background-color: linear-gradient(to right, transparent, #3B82F6, transparent);");
        
        topBox.getChildren().addAll(titleLabel, separator);
        root.setTop(topBox);

        // Center: Process button and results
        VBox centerBox = new VBox(20);
        centerBox.setPadding(new Insets(20));
        centerBox.setAlignment(Pos.CENTER);

        Button processButton = new Button("Process Order Eligibility & Allocation");
        processButton.setPrefWidth(400);
        processButton.setPrefHeight(60);
        processButton.setStyle("-fx-font-size: 16px; -fx-font-weight: 600; " +
                              "-fx-background-color: #2563EB; " +
                              "-fx-text-fill: #FFFFFF; " +
                              "-fx-background-radius: 8; " +
                              "-fx-cursor: hand;");
        processButton.setOnAction(e -> processEligibilityAndAllocation());

        // Results area with tabs
        TabPane resultsTabPane = new TabPane();
        resultsTabPane.setPrefHeight(500);
        resultsTabPane.setStyle("-fx-background-color: #0F172A;");

        // Eligibility Results Tab
        Tab eligibilityTab = new Tab("Eligibility Results");
        eligibilityTab.setClosable(false);
        eligibilityTab.setContent(createEligibilityTableView());
        resultsTabPane.getTabs().add(eligibilityTab);

        // Allocation Rows Tab
        Tab allocationTab = new Tab("Allocation Rows");
        allocationTab.setClosable(false);
        allocationTab.setContent(createAllocationTableView());
        resultsTabPane.getTabs().add(allocationTab);

        centerBox.getChildren().addAll(processButton, resultsTabPane);
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

    private VBox createEligibilityTableView() {
        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(20));
        vbox.setStyle("-fx-background-color: #0F172A;");

        eligibilityTable = new TableView<>();
        eligibilityTable.setPrefHeight(450);
        eligibilityTable.setStyle("-fx-background-color: #FFFFFF;");

        TableColumn<EligibilityResult, String> orderIdCol = new TableColumn<>("Order ID");
        orderIdCol.setCellValueFactory(new PropertyValueFactory<>("orderId"));
        orderIdCol.setPrefWidth(120);

        TableColumn<EligibilityResult, Integer> lineNoCol = new TableColumn<>("Line No");
        lineNoCol.setCellValueFactory(new PropertyValueFactory<>("lineNo"));
        lineNoCol.setPrefWidth(80);

        TableColumn<EligibilityResult, String> skuCol = new TableColumn<>("SKU");
        skuCol.setCellValueFactory(new PropertyValueFactory<>("sku"));
        skuCol.setPrefWidth(100);

        TableColumn<EligibilityResult, Integer> requestedCol = new TableColumn<>("Requested");
        requestedCol.setCellValueFactory(new PropertyValueFactory<>("requestedQty"));
        requestedCol.setPrefWidth(100);

        TableColumn<EligibilityResult, Integer> allocatedCol = new TableColumn<>("Allocated");
        allocatedCol.setCellValueFactory(new PropertyValueFactory<>("allocatedQty"));
        allocatedCol.setPrefWidth(100);

        TableColumn<EligibilityResult, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusCol.setPrefWidth(150);

        eligibilityTable.getColumns().addAll(orderIdCol, lineNoCol, skuCol, requestedCol, allocatedCol, statusCol);

        vbox.getChildren().add(eligibilityTable);
        return vbox;
    }

    private VBox createAllocationTableView() {
        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(20));
        vbox.setStyle("-fx-background-color: #0F172A;");

        allocationTable = new TableView<>();
        allocationTable.setPrefHeight(450);
        allocationTable.setStyle("-fx-background-color: #FFFFFF;");

        TableColumn<AllocationRow, String> orderIdCol = new TableColumn<>("Order ID");
        orderIdCol.setCellValueFactory(new PropertyValueFactory<>("orderId"));
        orderIdCol.setPrefWidth(120);

        TableColumn<AllocationRow, Integer> lineNoCol = new TableColumn<>("Line No");
        lineNoCol.setCellValueFactory(new PropertyValueFactory<>("lineNo"));
        lineNoCol.setPrefWidth(80);

        TableColumn<AllocationRow, String> skuCol = new TableColumn<>("SKU");
        skuCol.setCellValueFactory(new PropertyValueFactory<>("sku"));
        skuCol.setPrefWidth(100);

        TableColumn<AllocationRow, Integer> qtyCol = new TableColumn<>("Quantity");
        qtyCol.setCellValueFactory(new PropertyValueFactory<>("qty"));
        qtyCol.setPrefWidth(100);

        TableColumn<AllocationRow, String> boxIdCol = new TableColumn<>("Box ID");
        boxIdCol.setCellValueFactory(new PropertyValueFactory<>("boxId"));
        boxIdCol.setPrefWidth(150);

        TableColumn<AllocationRow, Integer> aisleCol = new TableColumn<>("Aisle");
        aisleCol.setCellValueFactory(new PropertyValueFactory<>("aisle"));
        aisleCol.setPrefWidth(80);

        TableColumn<AllocationRow, Integer> bayCol = new TableColumn<>("Bay");
        bayCol.setCellValueFactory(new PropertyValueFactory<>("bay"));
        bayCol.setPrefWidth(80);

        allocationTable.getColumns().addAll(orderIdCol, lineNoCol, skuCol, qtyCol, boxIdCol, aisleCol, bayCol);

        vbox.getChildren().add(allocationTable);
        return vbox;
    }

    private void processEligibilityAndAllocation() {
        AllocationResult result = controller.processOrderEligibilityAndAllocation(boxRepository, AllocationMode.STRICT);
        
        // Update eligibility table
        ObservableList<EligibilityResult> eligibilityList = FXCollections.observableArrayList(result.getEligibilityResults());
        eligibilityTable.setItems(eligibilityList);
        
        // Update allocation table
        ObservableList<AllocationRow> allocationList = FXCollections.observableArrayList(result.getAllocationRows());
        allocationTable.setItems(allocationList);
        
        showAlert(Alert.AlertType.INFORMATION, "Success", 
                 "Processed " + result.getEligibilityResults().size() + " eligibility results and " +
                 result.getAllocationRows().size() + " allocation rows.");
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

