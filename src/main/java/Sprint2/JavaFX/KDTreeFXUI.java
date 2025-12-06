package Sprint2.JavaFX;

import Sprint2.Controller.KDTreeController;
import Sprint2.Controller.KDTreeMetrics;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class KDTreeFXUI {
    private final KDTreeController controller;
    private Stage stage;
    private TextArea metricsArea;

    public KDTreeFXUI(KDTreeController controller) {
        this.controller = controller;
    }

    public void show() {
        stage = new Stage();
        stage.setTitle("Build 2D-Tree");

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #1E293B, #0F172A);");
        root.setPadding(new Insets(25));

        // Top: Title
        VBox topBox = new VBox(8);
        topBox.setAlignment(Pos.CENTER);
        topBox.setPadding(new Insets(0, 0, 25, 0));
        
        Label titleLabel = new Label("Build 2D-Tree (KD-Tree)");
        titleLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: 600; " +
                           "-fx-text-fill: linear-gradient(to right, #8B5CF6, #7C3AED, #8B5CF6);");
        
        Separator separator = new Separator();
        separator.setPrefWidth(500);
        separator.setStyle("-fx-background-color: linear-gradient(to right, transparent, #8B5CF6, transparent);");
        
        topBox.getChildren().addAll(titleLabel, separator);
        root.setTop(topBox);

        // Center: Build button and metrics
        VBox centerBox = new VBox(20);
        centerBox.setPadding(new Insets(20));
        centerBox.setAlignment(Pos.CENTER);

        Button buildButton = new Button("Build KD-Tree");
        buildButton.setPrefWidth(300);
        buildButton.setPrefHeight(60);
        buildButton.setStyle("-fx-font-size: 16px; -fx-font-weight: 600; " +
                            "-fx-background-color: #8B5CF6; " +
                            "-fx-text-fill: #FFFFFF; " +
                            "-fx-background-radius: 8; " +
                            "-fx-cursor: hand;");
        buildButton.setOnAction(e -> buildTree());

        Button viewMetricsButton = new Button("View Tree Metrics");
        viewMetricsButton.setPrefWidth(300);
        viewMetricsButton.setPrefHeight(60);
        viewMetricsButton.setStyle("-fx-font-size: 16px; -fx-font-weight: 600; " +
                                  "-fx-background-color: #7C3AED; " +
                                  "-fx-text-fill: #FFFFFF; " +
                                  "-fx-background-radius: 8; " +
                                  "-fx-cursor: hand;");
        viewMetricsButton.setOnAction(e -> viewMetrics());

        // Metrics area
        Label metricsLabel = new Label("Tree Metrics");
        metricsLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: 600; -fx-text-fill: #E2E8F0;");

        metricsArea = new TextArea();
        metricsArea.setEditable(false);
        metricsArea.setPrefHeight(400);
        metricsArea.setStyle("-fx-font-family: 'Consolas', 'Courier New', monospace; " +
                            "-fx-font-size: 12px; " +
                            "-fx-background-color: #FFFFFF; " +
                            "-fx-text-fill: #000000; " +
                            "-fx-border-color: #334155; " +
                            "-fx-border-radius: 8; " +
                            "-fx-background-radius: 8; " +
                            "-fx-padding: 10;");

        centerBox.getChildren().addAll(buildButton, viewMetricsButton, metricsLabel, metricsArea);
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

        Scene scene = new Scene(root, 800, 700);
        stage.setScene(scene);
        stage.setResizable(true);
        stage.show();
    }

    private void buildTree() {
        if (controller.isTreeBuilt()) {
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Rebuild Tree");
            confirmAlert.setHeaderText("Tree already built");
            confirmAlert.setContentText("Do you want to rebuild the KD-tree?");
            confirmAlert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    performBuild();
                }
            });
        } else {
            performBuild();
        }
    }

    private void performBuild() {
        long startTime = System.currentTimeMillis();
        boolean success = controller.buildTree();
        long endTime = System.currentTimeMillis();

        if (success) {
            appendMetrics("KD-tree built successfully in " + (endTime - startTime) + " ms.\n\n");
            
            KDTreeMetrics metrics = controller.getAllMetrics();
            if (metrics != null) {
                appendMetrics("=== KD-TREE BUILD SUMMARY ===\n");
                appendMetrics("Total Number of Stations: " + metrics.getNumStations() + "\n");
                appendMetrics("Total Number of Nodes: " + metrics.getNumNodes() + "\n");
                appendMetrics("Tree Height: " + metrics.getHeight() + "\n\n");
                appendMetrics("Bucket Size Distribution:\n");
                metrics.getBucketStats().entrySet().stream()
                    .sorted((a, b) -> {
                        int cmp = b.getValue().compareTo(a.getValue());
                        return cmp != 0 ? cmp : b.getKey().compareTo(a.getKey());
                    })
                    .forEach(entry -> appendMetrics("  Bucket size " + entry.getKey() + ": " + entry.getValue() + " node(s)\n"));
            }
            
            showAlert(Alert.AlertType.INFORMATION, "Success", "KD-tree built successfully!");
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to build KD-tree. Please check the station repository.");
        }
    }

    private void viewMetrics() {
        if (!controller.isTreeBuilt()) {
            showAlert(Alert.AlertType.WARNING, "Warning", "Tree not built yet. Please build the tree first.");
            return;
        }

        String metrics = controller.getMetricsString();
        clearMetrics();
        appendMetrics(metrics);
    }

    private void appendMetrics(String text) {
        Platform.runLater(() -> {
            if (metricsArea != null) {
                metricsArea.appendText(text);
                metricsArea.setScrollTop(Double.MAX_VALUE);
            }
        });
    }

    private void clearMetrics() {
        Platform.runLater(() -> {
            if (metricsArea != null) {
                metricsArea.clear();
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

