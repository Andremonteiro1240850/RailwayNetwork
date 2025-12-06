package Sprint1.JavaFX;

import Sprint1.Controller.QualityOperatorController;
import Sprint1.Domain.QuarantineInspection;
import Sprint1.Domain.Return;
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

import java.time.format.DateTimeFormatter;
import java.util.List;

public class ReturnsQuarantineFXUI {
    private final QualityOperatorController controller;
    private Stage stage;
    private TextArea outputArea;
    private TableView<Return> quarantineTable;
    private TableView<QuarantineInspection> inspectionsTable;
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public ReturnsQuarantineFXUI(QualityOperatorController controller) {
        this.controller = controller;
    }

    public void show() {
        stage = new Stage();
        stage.setTitle("Returns & Quarantine System");

        // Main container
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #1E293B, #0F172A);");
        root.setPadding(new Insets(25));

        // Top: Title
        VBox topBox = new VBox(8);
        topBox.setAlignment(Pos.CENTER);
        topBox.setPadding(new Insets(0, 0, 25, 0));
        
        Label titleLabel = new Label("Returns & Quarantine System");
        titleLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: 600; " +
                           "-fx-text-fill: linear-gradient(to right, #60A5FA, #3B82F6, #60A5FA); " +
                           "-fx-letter-spacing: 0.5px;");
        
        Separator separator = new Separator();
        separator.setPrefWidth(500);
        separator.setStyle("-fx-background-color: linear-gradient(to right, transparent, #3B82F6, transparent); " +
                          "-fx-pref-height: 2;");
        
        topBox.getChildren().addAll(titleLabel, separator);
        root.setTop(topBox);

        // Center: TabPane with different views
        TabPane tabPane = new TabPane();
        tabPane.setStyle("-fx-background-color: #0F172A;");
        
        // Tab 1: Quarantine Queue
        Tab queueTab = new Tab("Quarantine Queue");
        queueTab.setClosable(false);
        queueTab.setContent(createQuarantineQueueView());
        
        // Tab 2: Process Inspections
        Tab processTab = new Tab("Process Inspections");
        processTab.setClosable(false);
        processTab.setContent(createProcessInspectionsView());
        
        // Tab 3: Processed Inspections
        Tab inspectionsTab = new Tab("Processed Inspections");
        inspectionsTab.setClosable(false);
        inspectionsTab.setContent(createProcessedInspectionsView());
        
        // Tab 4: Audit Log
        Tab auditTab = new Tab("Audit Log");
        auditTab.setClosable(false);
        auditTab.setContent(createAuditLogView());
        
        tabPane.getTabs().addAll(queueTab, processTab, inspectionsTab, auditTab);
        root.setCenter(tabPane);

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
                            "-fx-cursor: hand; " +
                            "-fx-border-color: #64748B; -fx-border-width: 1; -fx-border-radius: 8;");
        closeButton.setOnAction(e -> stage.close());
        
        bottomBox.getChildren().add(closeButton);
        root.setBottom(bottomBox);

        Scene scene = new Scene(root, 1200, 800);
        stage.setScene(scene);
        stage.setResizable(true);
        stage.show();

        // Auto-load returns on startup
        loadReturnsFromCSV();
        refreshQuarantineQueue();
    }

    private VBox createQuarantineQueueView() {
        VBox vbox = new VBox(15);
        vbox.setPadding(new Insets(20));
        vbox.setStyle("-fx-background-color: #0F172A;");

        // Header
        Label headerLabel = new Label("Quarantine Queue");
        headerLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: 600; -fx-text-fill: #E2E8F0;");

        // Buttons
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_LEFT);
        
        Button refreshButton = createStyledButton("Refresh", "#2563EB", () -> refreshQuarantineQueue());
        Button reloadButton = createStyledButton("Reload from CSV", "#8B5CF6", () -> loadReturnsFromCSV());
        
        buttonBox.getChildren().addAll(refreshButton, reloadButton);

        // Table
        quarantineTable = new TableView<>();
        quarantineTable.setPrefHeight(500);
        quarantineTable.setStyle("-fx-background-color: #FFFFFF;");

        TableColumn<Return, String> returnIdCol = new TableColumn<>("Return ID");
        returnIdCol.setCellValueFactory(new PropertyValueFactory<>("returnId"));
        returnIdCol.setPrefWidth(120);

        TableColumn<Return, String> skuCol = new TableColumn<>("SKU");
        skuCol.setCellValueFactory(new PropertyValueFactory<>("sku"));
        skuCol.setPrefWidth(100);

        TableColumn<Return, Integer> qtyCol = new TableColumn<>("Quantity");
        qtyCol.setCellValueFactory(new PropertyValueFactory<>("qty"));
        qtyCol.setPrefWidth(80);

        TableColumn<Return, String> reasonCol = new TableColumn<>("Reason");
        reasonCol.setCellValueFactory(new PropertyValueFactory<>("reason"));
        reasonCol.setPrefWidth(150);

        TableColumn<Return, String> timestampCol = new TableColumn<>("Timestamp");
        timestampCol.setCellValueFactory(cellData -> {
            Return r = cellData.getValue();
            return new javafx.beans.property.ReadOnlyObjectWrapper<>(
                r.getTimestamp().format(dateFormatter)
            );
        });
        timestampCol.setPrefWidth(180);

        TableColumn<Return, String> expiryCol = new TableColumn<>("Expiry Date");
        expiryCol.setCellValueFactory(cellData -> {
            Return r = cellData.getValue();
            String expiry = r.getExpiryDate() != null ? r.getExpiryDate().toString() : "N/A";
            return new javafx.beans.property.ReadOnlyObjectWrapper<>(expiry);
        });
        expiryCol.setPrefWidth(120);

        quarantineTable.getColumns().addAll(returnIdCol, skuCol, qtyCol, reasonCol, timestampCol, expiryCol);

        vbox.getChildren().addAll(headerLabel, buttonBox, quarantineTable);
        return vbox;
    }

    private VBox createProcessInspectionsView() {
        VBox vbox = new VBox(15);
        vbox.setPadding(new Insets(20));
        vbox.setStyle("-fx-background-color: #0F172A;");

        Label headerLabel = new Label("Process Inspections");
        headerLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: 600; -fx-text-fill: #E2E8F0;");

        // Buttons
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        
        Button processOneButton = createStyledButton("Process Next Inspection", "#22C55E", () -> processSingleInspection());
        Button processAllButton = createStyledButton("Process All Inspections", "#F59E0B", () -> processAllInspections());
        
        buttonBox.getChildren().addAll(processOneButton, processAllButton);

        // Output area
        outputArea = new TextArea();
        outputArea.setEditable(false);
        outputArea.setPrefHeight(600);
        outputArea.setStyle("-fx-font-family: 'Consolas', 'Courier New', monospace; " +
                           "-fx-font-size: 12px; " +
                           "-fx-background-color: #FFFFFF; " +
                           "-fx-text-fill: #000000; " +
                           "-fx-border-color: #334155; " +
                           "-fx-border-radius: 8; " +
                           "-fx-background-radius: 8; " +
                           "-fx-padding: 10;");

        vbox.getChildren().addAll(headerLabel, buttonBox, outputArea);
        return vbox;
    }

    private VBox createProcessedInspectionsView() {
        VBox vbox = new VBox(15);
        vbox.setPadding(new Insets(20));
        vbox.setStyle("-fx-background-color: #0F172A;");

        Label headerLabel = new Label("Processed Inspections");
        headerLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: 600; -fx-text-fill: #E2E8F0;");

        Button refreshButton = createStyledButton("Refresh", "#2563EB", () -> refreshProcessedInspections());

        inspectionsTable = new TableView<>();
        inspectionsTable.setPrefHeight(600);
        inspectionsTable.setStyle("-fx-background-color: #FFFFFF;");

        TableColumn<QuarantineInspection, String> inspectionIdCol = new TableColumn<>("Inspection ID");
        inspectionIdCol.setCellValueFactory(new PropertyValueFactory<>("inspectionId"));
        inspectionIdCol.setPrefWidth(150);

        TableColumn<QuarantineInspection, String> returnIdCol = new TableColumn<>("Return ID");
        returnIdCol.setCellValueFactory(new PropertyValueFactory<>("returnId"));
        returnIdCol.setPrefWidth(120);

        TableColumn<QuarantineInspection, String> actionCol = new TableColumn<>("Action");
        actionCol.setCellValueFactory(cellData -> {
            QuarantineInspection i = cellData.getValue();
            String action = i.getAction() != null ? i.getAction().name() : "-";
            return new javafx.beans.property.ReadOnlyObjectWrapper<>(action);
        });
        actionCol.setPrefWidth(100);

        TableColumn<QuarantineInspection, String> inspectedAtCol = new TableColumn<>("Inspected At");
        inspectedAtCol.setCellValueFactory(cellData -> {
            QuarantineInspection i = cellData.getValue();
            String date = i.getInspectedAt() != null ? i.getInspectedAt().format(dateFormatter) : "-";
            return new javafx.beans.property.ReadOnlyObjectWrapper<>(date);
        });
        inspectedAtCol.setPrefWidth(180);

        inspectionsTable.getColumns().addAll(inspectionIdCol, returnIdCol, actionCol, inspectedAtCol);

        vbox.getChildren().addAll(headerLabel, refreshButton, inspectionsTable);
        return vbox;
    }

    private TextArea auditArea;
    
    private VBox createAuditLogView() {
        VBox vbox = new VBox(15);
        vbox.setPadding(new Insets(20));
        vbox.setStyle("-fx-background-color: #0F172A;");

        Label headerLabel = new Label("Audit Log");
        headerLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: 600; -fx-text-fill: #E2E8F0;");

        Button refreshButton = createStyledButton("Refresh", "#2563EB", () -> refreshAuditLog());

        auditArea = new TextArea();
        auditArea.setEditable(false);
        auditArea.setPrefHeight(600);
        auditArea.setStyle("-fx-font-family: 'Consolas', 'Courier New', monospace; " +
                          "-fx-font-size: 12px; " +
                          "-fx-background-color: #FFFFFF; " +
                          "-fx-text-fill: #000000; " +
                          "-fx-border-color: #334155; " +
                          "-fx-border-radius: 8; " +
                          "-fx-background-radius: 8; " +
                          "-fx-padding: 10;");

        vbox.getChildren().addAll(headerLabel, refreshButton, auditArea);
        
        refreshAuditLog();
        return vbox;
    }

    private Button createStyledButton(String text, String color, Runnable action) {
        Button button = new Button(text);
        button.setPrefHeight(40);
        button.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; " +
                       "-fx-background-color: " + color + "; " +
                       "-fx-text-fill: #FFFFFF; " +
                       "-fx-background-radius: 8; " +
                       "-fx-cursor: hand;");
        button.setOnAction(e -> action.run());
        return button;
    }

    private void loadReturnsFromCSV() {
        QualityOperatorController.LoadResult result = controller.loadReturnsFromCSV();
        if (result.isSuccess()) {
            appendOutput("Returns loaded from CSV: " + result.getLoadedCount() + " items");
            refreshQuarantineQueue();
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", result.getErrorMessage());
        }
    }

    private void refreshQuarantineQueue() {
        List<Return> items = controller.getQuarantineItems();
        ObservableList<Return> observableList = FXCollections.observableArrayList(items);
        quarantineTable.setItems(observableList);
    }

    private void processSingleInspection() {
        clearOutput();
        appendOutput("=== PROCESS NEXT INSPECTION ===");
        
        List<Return> items = controller.getQuarantineItems();
        if (items.isEmpty()) {
            appendOutput("No items in quarantine queue.");
            return;
        }

        Return item = items.get(0);
        appendOutput("Processing item: " + item.getReturnId());
        appendOutput("SKU: " + item.getSku() + ", Quantity: " + item.getQty());
        
        QualityOperatorController.ProcessingResult result = controller.processSingleInspection();
        
        if (result != null) {
            appendOutput("\nAction: " + result.getAction());
            appendOutput("Reason: " + result.getReason());
            appendOutput("\nInspection completed successfully!");
            refreshQuarantineQueue();
            refreshProcessedInspections();
        }
    }

    private void processAllInspections() {
        clearOutput();
        appendOutput("=== PROCESS ALL INSPECTIONS ===");
        
        List<Return> items = controller.getQuarantineItems();
        if (items.isEmpty()) {
            appendOutput("No items in quarantine queue.");
            return;
        }

        appendOutput("Processing " + items.size() + " items...");
        
        QualityOperatorController.ProcessingSummary summary = controller.processAllInspections();
        
        appendOutput("\n=== SUMMARY ===");
        appendOutput("Total Processed: " + summary.getProcessedCount());
        appendOutput("Discarded: " + summary.getDiscardedCount());
        appendOutput("Restocked: " + summary.getRestockedCount());
        appendOutput("\nAll inspections completed!");
        
        refreshQuarantineQueue();
        refreshProcessedInspections();
    }

    private void refreshProcessedInspections() {
        List<QuarantineInspection> inspections = controller.getProcessedInspections();
        ObservableList<QuarantineInspection> observableList = FXCollections.observableArrayList(inspections);
        inspectionsTable.setItems(observableList);
    }

    private void refreshAuditLog() {
        Platform.runLater(() -> {
            if (auditArea != null) {
                List<String> logs = controller.getAuditLogs();
                StringBuilder sb = new StringBuilder();
                for (String log : logs) {
                    sb.append(log).append("\n");
                }
                auditArea.setText(sb.toString());
                auditArea.setScrollTop(Double.MAX_VALUE);
            }
        });
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

