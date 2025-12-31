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

            // Carregar o FXML - o controller será carregado automaticamente pelo FXMLLoader
            // O package do Controller é Sprint1.JavaFX (verificado no ficheiro Controller.java)
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
            String errorMessage = sanitizeErrorMessage(e.getMessage());
            String detailedMessage = errorMessage;
            
            // Verificar se é um ClassNotFoundException
            if (e.getCause() instanceof ClassNotFoundException) {
                ClassNotFoundException cnfe = (ClassNotFoundException) e.getCause();
                String className = cnfe.getMessage();
                detailedMessage = "Classe não encontrada: " + className + "\n\n";
                detailedMessage += "Possíveis soluções:\n";
                detailedMessage += "1. Recompilar o projeto: mvn clean compile\n";
                // O caminho correto considera que o diretório tem espaços
                if (className.equals("Sprint1.JavaFX.Controller")) {
                    detailedMessage += "2. Verificar se a classe existe em: src/main/java/Cargo Handling at a Railway Terminal/Sprint1/JavaFX/Controller.java\n";
                } else {
                    detailedMessage += "2. Verificar se a classe existe no diretório correto\n";
                }
                detailedMessage += "3. Verificar se o package está correto no ficheiro FXML\n";
                detailedMessage += "4. Limpar e reconstruir o projeto no IDE";
            }
            
            System.err.println("ERRO ao carregar FXML (LoadException): " + errorMessage);
            if (e.getCause() != null) {
                String causeMessage = sanitizeErrorMessage(e.getCause().getMessage());
                System.err.println("Causa: " + e.getCause().getClass().getName() + ": " + causeMessage);
                e.getCause().printStackTrace();
            }
            e.printStackTrace();
            
            // Mostrar alerta visual
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
            alert.setTitle("Erro ao Carregar Interface");
            alert.setHeaderText("Falha ao carregar o FXML");
            alert.setContentText(detailedMessage + "\n\nVerifica o console para mais detalhes.");
            alert.setResizable(true);
            alert.getDialogPane().setPrefWidth(600);
            alert.showAndWait();
        } catch (IOException e) {
            String errorMessage = sanitizeErrorMessage(e.getMessage());
            System.err.println("ERRO ao carregar FXML (IOException): " + errorMessage);
            e.printStackTrace();
            // Mostrar alerta visual
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
            alert.setTitle("Erro ao Carregar Interface");
            alert.setHeaderText("Falha ao carregar o FXML");
            alert.setContentText("Erro: " + errorMessage + "\n\nVerifica o console para mais detalhes.");
            alert.showAndWait();
        } catch (Exception e) {
            String errorMessage = sanitizeErrorMessage(e.getMessage());
            System.err.println("ERRO inesperado: " + errorMessage);
            e.printStackTrace();
            // Mostrar alerta visual
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
            alert.setTitle("Erro Inesperado");
            alert.setHeaderText("Erro ao iniciar aplicação");
            alert.setContentText("Erro: " + errorMessage + "\n\nVerifica o console para mais detalhes.");
            alert.showAndWait();
        }
    }
    
    /**
     * Remove caminhos absolutos das mensagens de erro para tornar o projeto portável.
     * Extrai e substitui caminhos absolutos por caminhos relativos corretos.
     */
    private String sanitizeErrorMessage(String message) {
        if (message == null) {
            return "Erro desconhecido";
        }
        
        // Normalizar separadores de caminho para /
        message = message.replace('\\', '/');
        
        // Extrair caminho relativo após o nome do projeto
        // Procura por "sem3-pi-25-26-g044-repo/" e mantém tudo depois
        int projectIndex = message.indexOf("sem3-pi-25-26-g044-repo/");
        if (projectIndex >= 0) {
            message = message.substring(projectIndex + "sem3-pi-25-26-g044-repo/".length());
        }
        
        // Se contiver target/classes, substituir por src/main/resources (equivalente em desenvolvimento)
        message = message.replaceAll("target/classes/", "src/main/resources/");
        
        // Remover qualquer caminho absoluto restante (Windows: C:/, Unix: /home/, /Users/)
        message = message.replaceAll("(?i)[A-Z]:/.*?sem3-pi-25-26-g044-repo/", "");
        message = message.replaceAll("/Users/[^/]+/.*?sem3-pi-25-26-g044-repo/", "");
        message = message.replaceAll("/home/[^/]+/.*?sem3-pi-25-26-g044-repo/", "");
        
        // Limpar múltiplas barras consecutivas
        message = message.replaceAll("/+", "/");
        
        // Se começar com / e não for um caminho de recurso válido, remover
        if (message.startsWith("/") && !message.startsWith("/src/") && !message.startsWith("/target/")) {
            message = message.substring(1);
        }
        
        return message;
    }

    public static void main(String[] args) {
        launch(args);
    }
}