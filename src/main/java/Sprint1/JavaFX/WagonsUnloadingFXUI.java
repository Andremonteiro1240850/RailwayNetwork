package Sprint1.JavaFX;

import Sprint1.Controller.WagonsUnloadingController;
import Sprint1.Domain.Bay;
import Sprint1.Domain.Box;
import Sprint1.Domain.BayId;
import Sprint1.Repository.BoxRepository;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.collections.FXCollections;

import java.util.*;
import java.util.stream.Collectors;

public class WagonsUnloadingFXUI {
    private final WagonsUnloadingController controller;
    private Stage stage;
    private TextArea outputArea;

    public WagonsUnloadingFXUI(WagonsUnloadingController controller) {
        this.controller = controller;
    }

    public void show() {
        stage = new Stage();
        stage.setTitle("Wagons Unloading System");

        // Main container with gradient background
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #1E293B, #0F172A);");
        root.setPadding(new Insets(25));

        // Top: Title with elegant styling
        VBox topBox = new VBox(8);
        topBox.setAlignment(Pos.CENTER);
        topBox.setPadding(new Insets(0, 0, 25, 0));
        
        Label titleLabel = new Label("Wagons Unloading System");
        titleLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: 600; " +
                           "-fx-text-fill: linear-gradient(to right, #60A5FA, #3B82F6, #60A5FA); " +
                           "-fx-letter-spacing: 0.5px;");
        
        Separator separator = new Separator();
        separator.setPrefWidth(500);
        separator.setStyle("-fx-background-color: linear-gradient(to right, transparent, #3B82F6, transparent); " +
                          "-fx-pref-height: 2;");
        
        topBox.getChildren().addAll(titleLabel, separator);
        root.setTop(topBox);

        // Center: Buttons with professional styling
        VBox buttonBox = new VBox(15);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(20));

        Button unloadButton = new Button("Unload Wagons (FEFO/FIFO)");
        unloadButton.setPrefWidth(400);
        unloadButton.setPrefHeight(65);
        unloadButton.setStyle("-fx-font-size: 15px; -fx-font-weight: 600; " +
                             "-fx-background-color: #2563EB; " +
                             "-fx-text-fill: #FFFFFF; " +
                             "-fx-background-radius: 8; " +
                             "-fx-cursor: hand;");
        unloadButton.setOnAction(e -> unloadWagons());

        Button dispatchButton = new Button("Dispatch Stock");
        dispatchButton.setPrefWidth(400);
        dispatchButton.setPrefHeight(65);
        dispatchButton.setStyle("-fx-font-size: 15px; -fx-font-weight: 500; " +
                              "-fx-background-color: linear-gradient(to right, #1E293B, #334155); " +
                              "-fx-text-fill: #CBD5E1; " +
                              "-fx-background-radius: 8; " +
                              "-fx-cursor: hand; " +
                              "-fx-border-color: #475569; -fx-border-width: 1; -fx-border-radius: 8;");
        dispatchButton.setOnAction(e -> dispatchStock());

        Button relocateButton = new Button("Relocate Box");
        relocateButton.setPrefWidth(400);
        relocateButton.setPrefHeight(65);
        relocateButton.setStyle("-fx-font-size: 15px; -fx-font-weight: 500; " +
                               "-fx-background-color: linear-gradient(to right, #1E293B, #334155); " +
                               "-fx-text-fill: #CBD5E1; " +
                               "-fx-background-radius: 8; " +
                               "-fx-cursor: hand; " +
                               "-fx-border-color: #475569; -fx-border-width: 1; -fx-border-radius: 8;");
        relocateButton.setOnAction(e -> relocateBox());

        Button closeButton = new Button("Close");
        closeButton.setPrefWidth(400);
        closeButton.setPrefHeight(65);
        closeButton.setStyle("-fx-font-size: 15px; -fx-font-weight: 500; " +
                            "-fx-background-color: linear-gradient(to right, #475569, #64748B); " +
                            "-fx-text-fill: #E2E8F0; " +
                            "-fx-background-radius: 8; " +
                            "-fx-cursor: hand; " +
                            "-fx-border-color: #64748B; -fx-border-width: 1; -fx-border-radius: 8;");
        closeButton.setOnAction(e -> stage.close());

        buttonBox.getChildren().addAll(unloadButton, dispatchButton, relocateButton, closeButton);
        root.setCenter(buttonBox);

        // Bottom: Output area with elegant styling
        VBox bottomBox = new VBox(10);
        bottomBox.setPadding(new Insets(15, 0, 0, 0));
        
        Label outputLabel = new Label("Output Log");
        outputLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: #E2E8F0;");
        
        outputArea = new TextArea();
        outputArea.setEditable(false);
        outputArea.setPrefHeight(280);
        outputArea.setStyle("-fx-font-family: 'Consolas', 'Courier New', monospace; " +
                           "-fx-font-size: 12px; " +
                           "-fx-background-color: #FFFFFF; " +
                           "-fx-text-fill: #000000; " +
                           "-fx-border-color: #334155; " +
                           "-fx-border-radius: 8; " +
                           "-fx-background-radius: 8; " +
                           "-fx-padding: 10;");
        
        bottomBox.getChildren().addAll(outputLabel, outputArea);
        root.setBottom(bottomBox);

        Scene scene = new Scene(root, 900, 750);
        stage.setScene(scene);
        stage.setResizable(true);
        stage.show();

        appendOutput("System ready. Please select an option.");
    }

    private void appendOutput(String text) {
        Platform.runLater(() -> {
            outputArea.appendText(text + "\n");
        });
    }

    private void clearOutput() {
        Platform.runLater(() -> {
            outputArea.clear();
        });
    }

    private void unloadWagons() {
        clearOutput();
        appendOutput("=== UNLOADING WAGONS ===\n");

        if (!controller.isDataLoaded()) {
            appendOutput("Data not loaded. Please load data first.");
            showAlert(Alert.AlertType.ERROR, "Error", "Data not loaded");
            return;
        }

        // Show status before unloading
        BoxRepository boxRepo = controller.getBoxRepository();
        BoxRepository.RepositoryStats statsBefore = boxRepo.getStats();
        appendOutput("Current repository status:");
        appendOutput("  Unassigned boxes: " + statsBefore.totalUnassigned + " (SKUs: " + statsBefore.unassignedSkus + ")");
        appendOutput("  Assigned boxes: " + statsBefore.totalAssigned + " (SKUs: " + statsBefore.assignedSkus + ")\n");

        // Get unassigned boxes
        Map<String, List<Box>> unassignedBoxes = boxRepo.getAllUnassignedBoxes();

        if (unassignedBoxes.isEmpty()) {
            appendOutput("No unassigned boxes to process.");
            showAlert(Alert.AlertType.INFORMATION, "Info", "No unassigned boxes to process");
            return;
        }

        appendOutput("Starting wagon unloading process...");

        int totalBoxes = unassignedBoxes.values().stream().mapToInt(List::size).sum();
        appendOutput("Processing " + totalBoxes + " unassigned boxes across " + unassignedBoxes.size() + " SKU(s)...\n");

        for (Map.Entry<String, List<Box>> entry : unassignedBoxes.entrySet()) {
            String sku = entry.getKey();
            List<Box> boxes = entry.getValue();
            appendOutput("Processing " + boxes.size() + " unassigned boxes for SKU: " + sku);
        }

        // Execute unloading
        controller.unloadWagons();

        // Show results
        BoxRepository.RepositoryStats statsAfter = boxRepo.getStats();
        int assignedCount = statsBefore.totalUnassigned - statsAfter.totalUnassigned;

        appendOutput("\n=== UNLOADING COMPLETED ===");
        appendOutput("Boxes assigned: " + assignedCount);
        appendOutput("Remaining unassigned: " + statsAfter.totalUnassigned);

        if (statsAfter.totalUnassigned > 0) {
            appendOutput("\nWarning: Some boxes could not be assigned (no available bays).");
        }

        // Show detailed assignments
        if (statsAfter.totalAssigned > 0) {
            appendOutput("\n=== DETAILED BOX ASSIGNMENTS ===");
            showDetailedAssignments(boxRepo);
        }

        showAlert(Alert.AlertType.INFORMATION, "Success",
                "Unloading completed!\nBoxes assigned: " + assignedCount);
    }

    private void dispatchStock() {
        clearOutput();
        appendOutput("=== DISPATCH STOCK ===\n");

        if (!controller.isDataLoaded()) {
            appendOutput("Data not loaded.");
            showAlert(Alert.AlertType.ERROR, "Error", "Data not loaded");
            return;
        }

        List<String> skus = controller.getAvailableSkus();
        if (skus.isEmpty()) {
            appendOutput("No SKUs available.");
            showAlert(Alert.AlertType.INFORMATION, "Info", "No SKUs available");
            return;
        }

        // Create dialog with professional styling
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Dispatch Stock");
        dialog.setHeaderText("Select SKU and enter quantity");
        dialog.getDialogPane().setStyle("-fx-background-color: #1E293B;");

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new Insets(25));
        grid.setStyle("-fx-background-color: #1E293B;");

        Label skuLabel = new Label("SKU:");
        skuLabel.setStyle("-fx-text-fill: #E2E8F0; -fx-font-weight: 500; -fx-font-size: 13px;");
        
        ComboBox<String> skuCombo = new ComboBox<>(FXCollections.observableArrayList(skus));
        skuCombo.setPromptText("Select SKU");
        skuCombo.setPrefWidth(300);
        skuCombo.setStyle("-fx-background-color: #0F172A; -fx-text-fill: #CBD5E1; " +
                         "-fx-border-color: #475569; -fx-border-radius: 6; " +
                         "-fx-background-radius: 6; -fx-padding: 8;");

        Label quantityLabel = new Label("Quantity:");
        quantityLabel.setStyle("-fx-text-fill: #E2E8F0; -fx-font-weight: 500; -fx-font-size: 13px;");

        TextField quantityField = new TextField();
        quantityField.setPromptText("Enter quantity");
        quantityField.setPrefWidth(300);
        quantityField.setStyle("-fx-background-color: #0F172A; -fx-text-fill: #CBD5E1; " +
                              "-fx-border-color: #475569; -fx-border-radius: 6; " +
                              "-fx-background-radius: 6; -fx-padding: 8;");

        grid.add(skuLabel, 0, 0);
        grid.add(skuCombo, 1, 0);
        grid.add(quantityLabel, 0, 1);
        grid.add(quantityField, 1, 1);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        // Style dialog buttons
        Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.setStyle("-fx-background-color: linear-gradient(to right, #3B82F6, #2563EB); " +
                         "-fx-text-fill: white; -fx-background-radius: 6; -fx-padding: 8 20; " +
                         "-fx-font-weight: 500; -fx-cursor: hand;");
        
        Button cancelButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.CANCEL);
        cancelButton.setStyle("-fx-background-color: #475569; -fx-text-fill: #E2E8F0; " +
                            "-fx-background-radius: 6; -fx-padding: 8 20; " +
                            "-fx-font-weight: 500; -fx-cursor: hand;");

        Optional<ButtonType> result = dialog.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            String sku = skuCombo.getValue();
            String quantityStr = quantityField.getText().trim();

            if (sku == null || sku.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Error", "Please select a SKU");
                return;
            }

            try {
                int quantity = Integer.parseInt(quantityStr);

                if (quantity <= 0) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Quantity must be positive");
                    return;
                }

                appendOutput("Dispatching " + quantity + " units of SKU " + sku + "...\n");
                List<Box> dispatchedBoxes = controller.dispatchStock(sku, quantity);

                if (dispatchedBoxes.isEmpty()) {
                    appendOutput("No boxes were dispatched. Insufficient stock available.");
                    showAlert(Alert.AlertType.WARNING, "Warning", "Insufficient stock available");
                } else {
                    int totalDispatched = dispatchedBoxes.stream().mapToInt(Box::getQty).sum();
                    appendOutput("Successfully dispatched " + totalDispatched + " units in " +
                            dispatchedBoxes.size() + " box(es):\n");

                    for (Box box : dispatchedBoxes) {
                        String location = box.getLocation() != null ? " from " + box.getLocation() : "";
                        String type = box.getBoxId().contains("_PARTIAL") ? " (partial)" : " (full box)";
                        appendOutput("  - " + box.getBoxId() + type + " - qty: " + box.getQty() + location);
                    }

                    if (totalDispatched < quantity) {
                        appendOutput("\nWarning: Requested " + quantity + " units, but only " +
                                totalDispatched + " units were available.");
                    }

                    showAlert(Alert.AlertType.INFORMATION, "Success",
                            "Dispatched " + totalDispatched + " units in " +
                                    dispatchedBoxes.size() + " box(es)");
                }

            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Invalid quantity format");
            }
        }
    }

    private void relocateBox() {
        clearOutput();
        appendOutput("=== RELOCATE BOX ===\n");

        if (!controller.isDataLoaded()) {
            appendOutput("Data not loaded.");
            showAlert(Alert.AlertType.ERROR, "Error", "Data not loaded");
            return;
        }

        // Create dialog with professional styling
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Relocate Box");
        dialog.setHeaderText("Enter box ID and target location");
        dialog.getDialogPane().setStyle("-fx-background-color: #1E293B;");

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new Insets(25));
        grid.setStyle("-fx-background-color: #1E293B;");

        Label boxIdLabel = new Label("Box ID:");
        boxIdLabel.setStyle("-fx-text-fill: #E2E8F0; -fx-font-weight: 500; -fx-font-size: 13px;");
        
        TextField boxIdField = new TextField();
        boxIdField.setPromptText("Box ID");
        boxIdField.setPrefWidth(300);
        boxIdField.setStyle("-fx-background-color: #0F172A; -fx-text-fill: #CBD5E1; " +
                           "-fx-border-color: #475569; -fx-border-radius: 6; " +
                           "-fx-background-radius: 6; -fx-padding: 8;");

        Label warehouseLabel = new Label("Target Warehouse:");
        warehouseLabel.setStyle("-fx-text-fill: #E2E8F0; -fx-font-weight: 500; -fx-font-size: 13px;");
        
        TextField warehouseField = new TextField();
        warehouseField.setPromptText("Warehouse ID (e.g., W1)");
        warehouseField.setPrefWidth(300);
        warehouseField.setStyle("-fx-background-color: #0F172A; -fx-text-fill: #CBD5E1; " +
                               "-fx-border-color: #475569; -fx-border-radius: 6; " +
                               "-fx-background-radius: 6; -fx-padding: 8;");

        Label aisleLabel = new Label("Target Aisle:");
        aisleLabel.setStyle("-fx-text-fill: #E2E8F0; -fx-font-weight: 500; -fx-font-size: 13px;");
        
        TextField aisleField = new TextField();
        aisleField.setPromptText("Aisle number");
        aisleField.setPrefWidth(300);
        aisleField.setStyle("-fx-background-color: #0F172A; -fx-text-fill: #CBD5E1; " +
                           "-fx-border-color: #475569; -fx-border-radius: 6; " +
                           "-fx-background-radius: 6; -fx-padding: 8;");

        Label bayLabel = new Label("Target Bay:");
        bayLabel.setStyle("-fx-text-fill: #E2E8F0; -fx-font-weight: 500; -fx-font-size: 13px;");
        
        TextField bayField = new TextField();
        bayField.setPromptText("Bay number");
        bayField.setPrefWidth(300);
        bayField.setStyle("-fx-background-color: #0F172A; -fx-text-fill: #CBD5E1; " +
                         "-fx-border-color: #475569; -fx-border-radius: 6; " +
                         "-fx-background-radius: 6; -fx-padding: 8;");

        grid.add(boxIdLabel, 0, 0);
        grid.add(boxIdField, 1, 0);
        grid.add(warehouseLabel, 0, 1);
        grid.add(warehouseField, 1, 1);
        grid.add(aisleLabel, 0, 2);
        grid.add(aisleField, 1, 2);
        grid.add(bayLabel, 0, 3);
        grid.add(bayField, 1, 3);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        // Style dialog buttons
        Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.setStyle("-fx-background-color: linear-gradient(to right, #3B82F6, #2563EB); " +
                         "-fx-text-fill: white; -fx-background-radius: 6; -fx-padding: 8 20; " +
                         "-fx-font-weight: 500; -fx-cursor: hand;");
        
        Button cancelButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.CANCEL);
        cancelButton.setStyle("-fx-background-color: #475569; -fx-text-fill: #E2E8F0; " +
                             "-fx-background-radius: 6; -fx-padding: 8 20; " +
                             "-fx-font-weight: 500; -fx-cursor: hand;");

        Optional<ButtonType> result = dialog.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            String boxId = boxIdField.getText().trim();
            String warehouseId = warehouseField.getText().trim();
            String aisleStr = aisleField.getText().trim();
            String bayStr = bayField.getText().trim();

            if (boxId.isEmpty() || warehouseId.isEmpty() || aisleStr.isEmpty() || bayStr.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Error", "All fields are required");
                return;
            }

            try {
                // Check if box exists
                BoxRepository boxRepo = controller.getBoxRepository();
                Box box = boxRepo.getBoxById(boxId);
                if (box == null) {
                    appendOutput("Box " + boxId + " not found.");
                    showAlert(Alert.AlertType.ERROR, "Error", "Box not found");
                    return;
                }

                // Show current location
                BayId currentLocation = box.getLocation();
                if (currentLocation == null) {
                    appendOutput("Box " + boxId + " is not assigned to any bay.");
                    showAlert(Alert.AlertType.ERROR, "Error", "Box not assigned to any bay");
                    return;
                }

                appendOutput("Current location: " + currentLocation);
                appendOutput("Box SKU: " + box.getSku() + " | Quantity: " + box.getQty() + "\n");

                int aisle = Integer.parseInt(aisleStr);
                int bay = Integer.parseInt(bayStr);

                BayId targetBayId = new BayId(warehouseId, aisle, bay);

                // Check if target bay exists
                Bay targetBay = controller.getBayMap().get(targetBayId);
                if (targetBay == null) {
                    appendOutput("Target bay " + targetBayId + " not found.");
                    showAlert(Alert.AlertType.ERROR, "Error", "Target bay not found");
                    return;
                }

                // Check if target bay is full
                if (targetBay.isFull()) {
                    appendOutput("Target bay " + targetBayId + " is full (capacity: " +
                            targetBay.getCapacityBoxes() + ").");
                    showAlert(Alert.AlertType.ERROR, "Error", "Target bay is full");
                    return;
                }

                // Check if target bay can accept this SKU
                if (!targetBay.canAcceptSku(box.getSku())) {
                    appendOutput("Target bay " + targetBayId + " is dedicated to SKU " +
                            targetBay.getDedicatedSku() + ", cannot accept SKU " + box.getSku() + ".");
                    showAlert(Alert.AlertType.ERROR, "Error", "Target bay cannot accept this SKU");
                    return;
                }

                appendOutput("Relocating box " + boxId + " to " + targetBayId + "...");
                boolean success = controller.relocateBox(boxId, warehouseId, aisle, bay);

                if (success) {
                    appendOutput("Box " + boxId + " successfully relocated from " +
                            currentLocation + " to " + targetBayId + "!");
                    showAlert(Alert.AlertType.INFORMATION, "Success",
                            "Box successfully relocated to " + targetBayId);
                } else {
                    appendOutput("Failed to relocate box.");
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to relocate box");
                }

            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Invalid aisle or bay number");
            }
        }
    }

    private void showDetailedAssignments(BoxRepository boxRepo) {
        Map<String, List<Box>> assignedBoxes = boxRepo.getAllAssignedBoxes();

        if (assignedBoxes.isEmpty()) {
            appendOutput("No boxes assigned yet.");
            return;
        }

        // Group boxes by location
        Map<String, List<Box>> boxesByLocation = new HashMap<>();

        for (Map.Entry<String, List<Box>> entry : assignedBoxes.entrySet()) {
            List<Box> boxes = entry.getValue();

            for (Box box : boxes) {
                if (box.getLocation() != null) {
                    String location = box.getLocation().toString();
                    boxesByLocation.computeIfAbsent(location, k -> new ArrayList<>()).add(box);
                }
            }
        }

        // Sort locations
        List<String> sortedLocations = new ArrayList<>(boxesByLocation.keySet());
        sortedLocations.sort((loc1, loc2) -> {
            String[] parts1 = loc1.split("/");
            String[] parts2 = loc2.split("/");

            if (parts1.length != 3 || parts2.length != 3) {
                return loc1.compareTo(loc2);
            }

            int warehouseCompare = parts1[0].compareTo(parts2[0]);
            if (warehouseCompare != 0) return warehouseCompare;

            int aisleCompare = Integer.compare(
                    Integer.parseInt(parts1[1].substring(1)),
                    Integer.parseInt(parts2[1].substring(1))
            );
            if (aisleCompare != 0) return aisleCompare;

            return Integer.compare(
                    Integer.parseInt(parts1[2].substring(1)),
                    Integer.parseInt(parts2[2].substring(1))
            );
        });

        for (String location : sortedLocations) {
            List<Box> boxes = boxesByLocation.get(location);
            appendOutput("\n" + location + ":");

            Map<String, List<Box>> boxesBySku = boxes.stream()
                    .collect(Collectors.groupingBy(Box::getSku));

            for (Map.Entry<String, List<Box>> skuEntry : boxesBySku.entrySet()) {
                String sku = skuEntry.getKey();
                List<Box> skuBoxes = skuEntry.getValue();

                appendOutput("  SKU " + sku + " (" + skuBoxes.size() + " boxes):");
                for (Box box : skuBoxes) {
                    String expiryInfo = box.getExpiryDate() != null ?
                            " (expires: " + box.getExpiryDate() + ")" : " (non-perishable)";
                    appendOutput("    - " + box.getBoxId() + " (qty: " + box.getQty() + ")" + expiryInfo);
                }
            }
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(content);
            
            // Style the alert dialog
            alert.getDialogPane().setStyle("-fx-background-color: #1E293B;");
            
            // Style labels
            alert.getDialogPane().lookupAll(".label").forEach(node -> {
                if (node instanceof Label) {
                    ((Label) node).setStyle("-fx-text-fill: #E2E8F0; -fx-font-size: 13px;");
                }
            });
            
            // Style buttons
            Button okButton = (Button) alert.getDialogPane().lookupButton(ButtonType.OK);
            if (okButton != null) {
                okButton.setStyle("-fx-background-color: linear-gradient(to right, #3B82F6, #2563EB); " +
                                 "-fx-text-fill: white; -fx-background-radius: 6; -fx-padding: 8 20; " +
                                 "-fx-font-weight: 500; -fx-cursor: hand;");
            }
            
            alert.showAndWait();
        });
    }
}