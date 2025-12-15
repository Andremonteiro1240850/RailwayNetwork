package JavaFX;

import Controller.BackboneController;
import Domain.Graph;
import Domain.GraphvizRenderer;
import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.stage.FileChooser;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * JavaFX UI for Minimal Backbone Network (USEI12).
 * Provides a graphical interface to generate MST, export DOT, and export SVG.
 *
 * @author Development Team
 * @version 1.0
 */
public class BackboneFXUI {
    private BackboneController controller;
    private Stage stage;
    private Button generateButton;
    private Button exportDotButton;
    private Button exportSvgButton;
    private Button generateAndExportDotButton;
    private Button generateAndExportSvgButton;
    private ProgressIndicator progressIndicator;
    private Label statusLabel;
    private TextArea infoArea;
    private Graph currentMST;
    
    /**
     * Constructs a new BackboneFXUI with default controller.
     */
    public BackboneFXUI() {
        this.controller = new BackboneController();
    }
    
    /**
     * Constructs a new BackboneFXUI with custom controller.
     *
     * @param controller the backbone controller
     */
    public BackboneFXUI(BackboneController controller) {
        this.controller = controller;
    }
    
    /**
     * Shows the minimal backbone network UI window.
     */
    public void show() {
        stage = new Stage();
        stage.setTitle("Minimal Backbone Network (USEI12)");
        
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #1E293B, #0F172A);");
        root.setPadding(new Insets(25));
        
        // Top: Title
        VBox topBox = new VBox(10);
        topBox.setAlignment(Pos.CENTER);
        Label titleLabel = new Label("Minimal Backbone Network");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #E2E8F0;");
        Label subtitleLabel = new Label("Minimum Spanning Tree (MST) for Railway Network");
        subtitleLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #94A3B8;");
        topBox.getChildren().addAll(titleLabel, subtitleLabel);
        root.setTop(topBox);
        
        // Center: Main content
        VBox centerBox = new VBox(20);
        centerBox.setPadding(new Insets(20));
        
        // Buttons
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);
        
        generateButton = createStyledButton("Generate MST", "#3B82F6");
        generateButton.setOnAction(e -> handleGenerateMST());
        
        exportDotButton = createStyledButton("Export DOT", "#10B981");
        exportDotButton.setOnAction(e -> handleExportDot());
        exportDotButton.setDisable(true);
        
        exportSvgButton = createStyledButton("Export SVG", "#F59E0B");
        exportSvgButton.setOnAction(e -> handleExportSvg());
        exportSvgButton.setDisable(true);
        
        generateAndExportDotButton = createStyledButton("Generate & Export DOT", "#8B5CF6");
        generateAndExportDotButton.setOnAction(e -> handleGenerateAndExportDot());
        
        generateAndExportSvgButton = createStyledButton("Generate & Export SVG", "#EC4899");
        generateAndExportSvgButton.setOnAction(e -> handleGenerateAndExportSvg());
        
        buttonBox.getChildren().addAll(
            generateButton, exportDotButton, exportSvgButton,
            generateAndExportDotButton, generateAndExportSvgButton
        );
        
        // Progress indicator
        progressIndicator = new ProgressIndicator();
        progressIndicator.setVisible(false);
        progressIndicator.setPrefSize(40, 40);
        
        // Status label
        statusLabel = new Label("Ready to generate minimal backbone network");
        statusLabel.setStyle("-fx-text-fill: #94A3B8; -fx-font-size: 14px;");
        
        // Info area
        infoArea = new TextArea();
        infoArea.setEditable(false);
        infoArea.setPrefRowCount(10);
        infoArea.setStyle("-fx-background-color: #1E293B; -fx-text-fill: #E2E8F0; -fx-font-family: 'Courier New';");
        
        centerBox.getChildren().addAll(buttonBox, progressIndicator, statusLabel, infoArea);
        root.setCenter(centerBox);
        
        // Bottom: Statistics button
        HBox bottomBox = new HBox(10);
        bottomBox.setAlignment(Pos.CENTER);
        bottomBox.setPadding(new Insets(10));
        
        Button statsButton = createStyledButton("View Graph Statistics", "#6B7280");
        statsButton.setOnAction(e -> handleViewStatistics());
        bottomBox.getChildren().add(statsButton);
        
        root.setBottom(bottomBox);
        
        Scene scene = new Scene(root, 900, 700);
        stage.setScene(scene);
        stage.setResizable(true);
        stage.show();
    }
    
    /**
     * Creates a styled button with consistent appearance.
     */
    private Button createStyledButton(String text, String color) {
        Button button = new Button(text);
        button.setStyle(String.format(
            "-fx-background-color: %s; -fx-text-fill: white; -fx-font-weight: bold; " +
            "-fx-padding: 10 20; -fx-background-radius: 5;",
            color
        ));
        button.setOnMouseEntered(e -> button.setStyle(String.format(
            "-fx-background-color: derive(%s, 20%%); -fx-text-fill: white; -fx-font-weight: bold; " +
            "-fx-padding: 10 20; -fx-background-radius: 5; -fx-cursor: hand;",
            color
        )));
        button.setOnMouseExited(e -> button.setStyle(String.format(
            "-fx-background-color: %s; -fx-text-fill: white; -fx-font-weight: bold; " +
            "-fx-padding: 10 20; -fx-background-radius: 5;",
            color
        )));
        return button;
    }
    
    /**
     * Handles MST generation.
     */
    private void handleGenerateMST() {
        Service<Graph> service = new Service<Graph>() {
            @Override
            protected Task<Graph> createTask() {
                return new Task<Graph>() {
                    @Override
                    protected Graph call() throws Exception {
                        updateMessage("Loading graph and computing MST...");
                        return controller.generateBackbone();
                    }
                };
            }
        };
        
        service.setOnSucceeded(e -> {
            currentMST = service.getValue();
            progressIndicator.setVisible(false);
            exportDotButton.setDisable(false);
            exportSvgButton.setDisable(false);
            
            String info = String.format(
                "✓ Minimal Backbone Network generated successfully!\n\n" +
                "Statistics:\n" +
                "  Stations: %d\n" +
                "  Connections: %d\n",
                currentMST.getStationCount(),
                currentMST.getConnectionCount()
            );
            infoArea.setText(info);
            statusLabel.setText("MST generated successfully!");
            statusLabel.setStyle("-fx-text-fill: #10B981; -fx-font-size: 14px;");
        });
        
        service.setOnFailed(e -> {
            progressIndicator.setVisible(false);
            Throwable ex = service.getException();
            String error = "✗ ERROR: Failed to generate MST\n" + ex.getMessage();
            if (ex.getCause() != null) {
                error += "\nCause: " + ex.getCause().getMessage();
            }
            infoArea.setText(error);
            statusLabel.setText("Error generating MST");
            statusLabel.setStyle("-fx-text-fill: #EF4444; -fx-font-size: 14px;");
            showError("Error", "Failed to generate MST", ex);
        });
        
        progressIndicator.setVisible(true);
        statusLabel.setText("Generating MST...");
        service.start();
    }
    
    /**
     * Handles DOT export.
     */
    private void handleExportDot() {
        if (currentMST == null) {
            showError("Error", "No MST generated", "Please generate MST first.");
            return;
        }
        
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save DOT File");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("DOT Files", "*.dot")
        );
        fileChooser.setInitialFileName("backbone.dot");
        
        File file = fileChooser.showSaveDialog(stage);
        if (file == null) {
            return;
        }
        
        Service<String> service = new Service<String>() {
            @Override
            protected Task<String> createTask() {
                return new Task<String>() {
                    @Override
                    protected String call() throws Exception {
                        updateMessage("Exporting to DOT format...");
                        return controller.exportDot(currentMST, file.getAbsolutePath());
                    }
                };
            }
        };
        
        service.setOnSucceeded(e -> {
            String filePath = service.getValue();
            infoArea.appendText("\n\n✓ DOT file exported successfully!\n");
            infoArea.appendText("File path: " + filePath + "\n");
            statusLabel.setText("DOT file exported successfully!");
            statusLabel.setStyle("-fx-text-fill: #10B981; -fx-font-size: 14px;");
        });
        
        service.setOnFailed(e -> {
            Throwable ex = service.getException();
            showError("Error", "Failed to export DOT", ex);
        });
        
        progressIndicator.setVisible(true);
        service.start();
    }
    
    /**
     * Handles SVG export.
     */
    private void handleExportSvg() {
        if (currentMST == null) {
            showError("Error", "No MST generated", "Please generate MST first.");
            return;
        }
        
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save SVG File");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("SVG Files", "*.svg")
        );
        fileChooser.setInitialFileName("backbone.svg");
        
        File file = fileChooser.showSaveDialog(stage);
        if (file == null) {
            return;
        }
        
        Service<String> service = new Service<String>() {
            @Override
            protected Task<String> createTask() {
                return new Task<String>() {
                    @Override
                    protected String call() throws Exception {
                        updateMessage("Exporting to SVG format using Graphviz...");
                        return controller.exportSvg(currentMST, file.getAbsolutePath());
                    }
                };
            }
        };
        
        service.setOnSucceeded(e -> {
            String filePath = service.getValue();
            infoArea.appendText("\n\n✓ SVG file exported successfully!\n");
            infoArea.appendText("File path: " + filePath + "\n");
            infoArea.appendText("Note: Make sure Graphviz (neato) is installed and in your PATH\n");
            statusLabel.setText("SVG file exported successfully!");
            statusLabel.setStyle("-fx-text-fill: #10B981; -fx-font-size: 14px;");
        });
        
        service.setOnFailed(e -> {
            Throwable ex = service.getException();
            if (ex instanceof GraphvizRenderer.GraphvizException) {
                showError("Graphviz Error", 
                    "Graphviz execution failed", 
                    ex.getMessage() + "\n\nMake sure Graphviz is installed: https://graphviz.org/download/");
            } else {
                showError("Error", "Failed to export SVG", ex);
            }
        });
        
        progressIndicator.setVisible(true);
        service.start();
    }
    
    /**
     * Handles generate and export DOT in one step.
     */
    private void handleGenerateAndExportDot() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save DOT File");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("DOT Files", "*.dot")
        );
        fileChooser.setInitialFileName("backbone.dot");
        
        File file = fileChooser.showSaveDialog(stage);
        if (file == null) {
            return;
        }
        
        Service<String> service = new Service<String>() {
            @Override
            protected Task<String> createTask() {
                return new Task<String>() {
                    @Override
                    protected String call() throws Exception {
                        updateMessage("Generating MST and exporting to DOT...");
                        String path = controller.generateAndExportDot(file.getAbsolutePath());
                        currentMST = controller.generateBackbone(); // Update current MST
                        return path;
                    }
                };
            }
        };
        
        service.setOnSucceeded(e -> {
            String filePath = service.getValue();
            progressIndicator.setVisible(false);
            exportDotButton.setDisable(false);
            exportSvgButton.setDisable(false);
            
            String info = String.format(
                "✓ MST generated and DOT file exported successfully!\n\n" +
                "Statistics:\n" +
                "  Stations: %d\n" +
                "  Connections: %d\n\n" +
                "File path: %s\n",
                currentMST.getStationCount(),
                currentMST.getConnectionCount(),
                filePath
            );
            infoArea.setText(info);
            statusLabel.setText("MST generated and DOT exported successfully!");
            statusLabel.setStyle("-fx-text-fill: #10B981; -fx-font-size: 14px;");
        });
        
        service.setOnFailed(e -> {
            progressIndicator.setVisible(false);
            Throwable ex = service.getException();
            showError("Error", "Failed to generate and export DOT", ex);
        });
        
        progressIndicator.setVisible(true);
        service.start();
    }
    
    /**
     * Handles generate and export SVG in one step.
     */
    private void handleGenerateAndExportSvg() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save SVG File");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("SVG Files", "*.svg")
        );
        fileChooser.setInitialFileName("backbone.svg");
        
        File file = fileChooser.showSaveDialog(stage);
        if (file == null) {
            return;
        }
        
        Service<String> service = new Service<String>() {
            @Override
            protected Task<String> createTask() {
                return new Task<String>() {
                    @Override
                    protected String call() throws Exception {
                        updateMessage("Generating MST and exporting to SVG...");
                        String path = controller.generateAndExportSvg(file.getAbsolutePath());
                        currentMST = controller.generateBackbone(); // Update current MST
                        return path;
                    }
                };
            }
        };
        
        service.setOnSucceeded(e -> {
            String filePath = service.getValue();
            progressIndicator.setVisible(false);
            exportDotButton.setDisable(false);
            exportSvgButton.setDisable(false);
            
            String info = String.format(
                "✓ MST generated and SVG file exported successfully!\n\n" +
                "Statistics:\n" +
                "  Stations: %d\n" +
                "  Connections: %d\n\n" +
                "File path: %s\n" +
                "Note: Make sure Graphviz (neato) is installed and in your PATH\n",
                currentMST.getStationCount(),
                currentMST.getConnectionCount(),
                filePath
            );
            infoArea.setText(info);
            statusLabel.setText("MST generated and SVG exported successfully!");
            statusLabel.setStyle("-fx-text-fill: #10B981; -fx-font-size: 14px;");
        });
        
        service.setOnFailed(e -> {
            progressIndicator.setVisible(false);
            Throwable ex = service.getException();
            if (ex instanceof GraphvizRenderer.GraphvizException) {
                showError("Graphviz Error", 
                    "Graphviz execution failed", 
                    ex.getMessage() + "\n\nMake sure Graphviz is installed: https://graphviz.org/download/");
            } else {
                showError("Error", "Failed to generate and export SVG", ex);
            }
        });
        
        progressIndicator.setVisible(true);
        service.start();
    }
    
    /**
     * Handles viewing graph statistics.
     */
    private void handleViewStatistics() {
        Service<String> service = new Service<String>() {
            @Override
            protected Task<String> createTask() {
                return new Task<String>() {
                    @Override
                    protected String call() throws Exception {
                        updateMessage("Loading graph statistics...");
                        return controller.getGraphStatistics();
                    }
                };
            }
        };
        
        service.setOnSucceeded(e -> {
            String statistics = service.getValue();
            infoArea.setText(statistics);
            statusLabel.setText("Graph statistics loaded");
            statusLabel.setStyle("-fx-text-fill: #10B981; -fx-font-size: 14px;");
        });
        
        service.setOnFailed(e -> {
            Throwable ex = service.getException();
            showError("Error", "Failed to load graph statistics", ex);
        });
        
        progressIndicator.setVisible(true);
        service.start();
    }
    
    /**
     * Shows an error alert dialog.
     */
    private void showError(String title, String header, Object content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(header);
            alert.setContentText(content.toString());
            alert.showAndWait();
        });
    }
}

