package pt.ipp.isep.dei;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

/**
 * Main JavaFX Application
 * Railway Terminal Management System
 */
public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // Tenta carregar o FXML do classpath
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/layout.fxml"));

            if (loader.getLocation() == null) {
                System.err.println("ERRO: Ficheiro layout.fxml não encontrado em src/main/resources/");
                System.err.println("Certifica-te que o ficheiro existe em: src/main/resources/layout.fxml");
                return;
            }

            Scene scene = new Scene(loader.load(), 1100, 750);

            // Adicionar estilos CSS
            scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());

            primaryStage.setTitle("Railway Terminal Management System");
            primaryStage.setScene(scene);
            primaryStage.setResizable(true);
            primaryStage.show();

            System.out.println("Aplicação iniciada com sucesso!");

        } catch (IOException e) {
            System.err.println("ERRO ao carregar FXML: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("ERRO inesperado: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}