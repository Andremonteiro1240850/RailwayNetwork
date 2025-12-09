package Sprint1.JavaFX;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Window that wraps console-based UIs in a JavaFX window.
 * Captures System.out and displays it in a TextArea.
 */
public class ConsoleUIWindow {
    private final String title;
    private final Runnable uiRunnable;
    private Stage stage;
    private TextArea outputArea;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private PrintStream originalOut;
    private ByteArrayOutputStream outputStream;
    private PrintStream printStream;

    public ConsoleUIWindow(String title, Runnable uiRunnable) {
        this.title = title;
        this.uiRunnable = uiRunnable;
    }

    public void show() {
        stage = new Stage();
        stage.setTitle(title);

        // Main container
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #1E293B, #0F172A);");
        root.setPadding(new Insets(25));

        // Top: Title
        VBox topBox = new VBox(8);
        topBox.setAlignment(Pos.CENTER);
        topBox.setPadding(new Insets(0, 0, 25, 0));
        
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: 600; " +
                           "-fx-text-fill: linear-gradient(to right, #60A5FA, #3B82F6, #60A5FA); " +
                           "-fx-letter-spacing: 0.5px;");
        
        Separator separator = new Separator();
        separator.setPrefWidth(500);
        separator.setStyle("-fx-background-color: linear-gradient(to right, transparent, #3B82F6, transparent); " +
                          "-fx-pref-height: 2;");
        
        topBox.getChildren().addAll(titleLabel, separator);
        root.setTop(topBox);

        // Center: Output area
        VBox centerBox = new VBox(10);
        centerBox.setPadding(new Insets(15, 0, 0, 0));
        
        Label outputLabel = new Label("Output");
        outputLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: #E2E8F0;");
        
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
        
        ScrollPane scrollPane = new ScrollPane(outputArea);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setStyle("-fx-background-color: transparent;");
        
        centerBox.getChildren().addAll(outputLabel, scrollPane);
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
                            "-fx-cursor: hand; " +
                            "-fx-border-color: #64748B; -fx-border-width: 1; -fx-border-radius: 8;");
        closeButton.setOnAction(e -> {
            executorService.shutdown();
            restoreSystemOut();
            stage.close();
        });
        
        bottomBox.getChildren().add(closeButton);
        root.setBottom(bottomBox);

        Scene scene = new Scene(root, 1000, 800);
        stage.setScene(scene);
        stage.setResizable(true);
        stage.setOnCloseRequest(e -> {
            executorService.shutdown();
            restoreSystemOut();
        });
        stage.show();

        // Redirect System.out to capture output
        redirectSystemOut();
        
        // Run UI in background thread
        executorService.execute(() -> {
            try {
                appendOutput("=== " + title + " ===\n");
                appendOutput("Starting...\n\n");
                uiRunnable.run();
                appendOutput("\n\n=== Operation completed ===");
            } catch (Exception e) {
                appendOutput("\n\nError: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    private void redirectSystemOut() {
        originalOut = System.out;
        outputStream = new ByteArrayOutputStream();
        printStream = new PrintStream(outputStream) {
            @Override
            public void println(String x) {
                super.println(x);
                appendOutput(x + "\n");
            }
            
            @Override
            public void print(String x) {
                super.print(x);
                appendOutput(x);
            }
            
            @Override
            public void println() {
                super.println();
                appendOutput("\n");
            }
        };
        System.setOut(printStream);
    }

    private void restoreSystemOut() {
        if (originalOut != null) {
            System.setOut(originalOut);
        }
    }

    private void appendOutput(String text) {
        Platform.runLater(() -> {
            outputArea.appendText(text);
            outputArea.setScrollTop(Double.MAX_VALUE); // Auto-scroll to bottom
        });
    }
}

