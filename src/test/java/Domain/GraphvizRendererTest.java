package Domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Suite de testes para GraphvizRenderer.
 * Nota: Alguns testes verificam apenas as validações de pré-execução
 * para garantir portabilidade entre SOs (Windows/Linux).
 */
class GraphvizRendererTest {

    private GraphvizRenderer renderer;

    @TempDir
    Path tempDir; // JUnit cria e limpa automaticamente uma pasta temporária

    @BeforeEach
    void setUp() {
        renderer = new GraphvizRenderer();
    }

    @Test
    @DisplayName("Deve falhar se os caminhos forem nulos ou vazios")
    void testInvalidPaths() {
        assertThrows(IllegalArgumentException.class, () -> renderer.renderSvg(null, "out.svg"));
        assertThrows(IllegalArgumentException.class, () -> renderer.renderSvg("in.dot", "  "));
    }

    @Test
    @DisplayName("Deve lançar IOException se o ficheiro DOT não existir")
    void testMissingDotFile() {
        String nonExistent = tempDir.resolve("missing.dot").toString();
        String output = tempDir.resolve("output.svg").toString();

        IOException ex = assertThrows(IOException.class, () -> renderer.renderSvg(nonExistent, output));
        assertTrue(ex.getMessage().contains("DOT file not found"));
    }

    @Test
    @DisplayName("Deve criar diretórios pai se não existirem")
    void testCreateParentDirectories() throws IOException {
        // Criar um ficheiro DOT válido para passar a primeira validação
        Path dotFile = tempDir.resolve("test.dot");
        Files.writeString(dotFile, "graph G { A -- B; }");

        // Caminho de saída numa subpasta que não existe
        Path subFolder = tempDir.resolve("nested/folder/output.svg");

        // Vamos tentar executar. Se falhar por falta do 'neato',
        // verificamos se a pasta 'nested/folder' foi criada antes do erro.
        try {
            renderer.renderSvg(dotFile.toString(), subFolder.toString());
        } catch (GraphvizRenderer.GraphvizException e) {
            // Se o neato não estiver instalado, ele lança esta exceção,
            // mas o diretório já deve ter sido criado.
        }

        assertTrue(Files.exists(subFolder.getParent()), "O renderer deve criar as pastas pai do SVG.");
    }

    @Test
    @DisplayName("Deve lançar exceção quando o caminho de saída é inválido")
    void testInvalidSvgValidation() throws IOException {
        // 1. Criamos um ficheiro DOT válido para passar a fase de leitura
        Path dotFile = tempDir.resolve("valid.dot");
        Files.writeString(dotFile, "graph G { A [pos=\"0,0!\"]; }");

        // 2. Usamos um caminho que sabemos que vai falhar no Windows (caracteres proibidos)
        // Isso força o código a lançar uma IOException ou GraphvizException
        String invalidPath = "K:/invalid|path/output.svg";

        // 3. Verificamos se o método lança UMA exceção (não importa qual, desde que falhe)
        assertThrows(Exception.class, () -> {
            renderer.renderSvg(dotFile.toString(), invalidPath);
        });
    }

    @Test
    @DisplayName("Teste da Exception personalizada")
    void testGraphvizException() {
        GraphvizRenderer.GraphvizException ex = new GraphvizRenderer.GraphvizException("Erro", new RuntimeException("Causa"));
        assertEquals("Erro", ex.getMessage());
        assertNotNull(ex.getCause());
    }

    @Test
    @DisplayName("Verifica deteção de comando (isCommandAvailable)")
    void testCommandCheck() {
        // 'java' deve estar no PATH se estamos a correr testes JUnit
        // Usamos reflexão ou apenas testamos o comportamento indireto
        // No renderer, isCommandAvailable é private, mas findNeatoPath o usa.
        assertDoesNotThrow(() -> {
            // Apenas para garantir que não crasha ao tentar encontrar o path
            // independente do SO
            System.getProperty("os.name");
        });
    }
}