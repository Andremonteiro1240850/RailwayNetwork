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

            // Verificar se o controller pode ser carregado antes de carregar o FXML
            try {
                Class<?> controllerClass = Class.forName("Sprint1.JavaFX.Controller");
                System.out.println("Controller encontrado: " + controllerClass.getName());
            } catch (ClassNotFoundException e) {
                System.err.println("ERRO CRÍTICO: Controller não encontrado: Sprint1.JavaFX.Controller");
                System.err.println("Verifica se o ficheiro Controller.java existe e está no package correto.");
                System.err.println("Caminho esperado: src/main/java/Cargo Handling at a Railway Terminal/Sprint1/JavaFX/Controller.java");
                e.printStackTrace();
                return;
            }

            Scene scene = new Scene(loader.load(), 1100, 750);

            // Adicionar estilos CSS (opcional - pode não existir)
            try {
                scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
            } catch (Exception e) {
                System.out.println("Aviso: styles.css não encontrado, continuando sem estilos CSS");
            }

            primaryStage.setTitle("Railway Terminal Management System");
            primaryStage.setScene(scene);
            primaryStage.setResizable(true);
            primaryStage.show();

            System.out.println("Aplicação iniciada com sucesso!");

        } catch (javafx.fxml.LoadException e) {
            System.err.println("ERRO ao carregar FXML (LoadException): " + e.getMessage());
            if (e.getCause() != null) {
                System.err.println("Causa: " + e.getCause().getClass().getName() + ": " + e.getCause().getMessage());
                e.getCause().printStackTrace();
            }
            e.printStackTrace();
            // Mostrar alerta visual
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
            alert.setTitle("Erro ao Carregar Interface");
            alert.setHeaderText("Falha ao carregar o FXML");
            alert.setContentText("Erro: " + e.getMessage() + "\n\nVerifica o console para mais detalhes.");
            alert.showAndWait();
        } catch (IOException e) {
            System.err.println("ERRO ao carregar FXML (IOException): " + e.getMessage());
            e.printStackTrace();
            // Mostrar alerta visual
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
            alert.setTitle("Erro ao Carregar Interface");
            alert.setHeaderText("Falha ao carregar o FXML");
            alert.setContentText("Erro: " + e.getMessage() + "\n\nVerifica o console para mais detalhes.");
            alert.showAndWait();
        } catch (Exception e) {
            System.err.println("ERRO inesperado: " + e.getMessage());
            e.printStackTrace();
            // Mostrar alerta visual
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
            alert.setTitle("Erro Inesperado");
            alert.setHeaderText("Erro ao iniciar aplicação");
            alert.setContentText("Erro: " + e.getMessage() + "\n\nVerifica o console para mais detalhes.");
            alert.showAndWait();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}