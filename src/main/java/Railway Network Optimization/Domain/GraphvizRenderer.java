package Domain;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;
import java.io.File;

/**
 * Utility class for rendering DOT files to SVG using Graphviz's neato engine.
 * 
 * Executes the external command: neato -Tsvg input.dot -o output.svg
 * Handles I/O operations and error propagation gracefully.
 * 
 * This class follows Clean Architecture principles:
 * - Encapsulates external tool execution
 * - Provides clean error handling
 *
 * @author Development Team
 * @version 1.0
 */
public class GraphvizRenderer {
    
    private static final String NEATO_COMMAND = "neato";
    private static final int TIMEOUT_SECONDS = 30;
    
    // Common Graphviz installation paths on Windows
    private static final String[] WINDOWS_GRAPHVIZ_PATHS = {
        "C:\\Program Files\\Graphviz\\bin\\neato.exe",
        "C:\\Program Files (x86)\\Graphviz\\bin\\neato.exe",
        "C:\\Graphviz\\bin\\neato.exe"
    };
    
    /**
     * Finds the neato executable path.
     * First tries to use 'neato' from PATH, then checks common Windows installation paths.
     * 
     * @return the path to neato executable, or "neato" if found in PATH
     */
    private static String findNeatoPath() {
        // First, try to use 'neato' from PATH
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            // On Windows, check common installation paths
            for (String path : WINDOWS_GRAPHVIZ_PATHS) {
                if (new File(path).exists()) {
                    return path;
                }
            }
        }
        // Fallback to PATH
        return NEATO_COMMAND;
    }
    
    /**
     * Renders a DOT file to SVG using Graphviz's neato engine.
     * 
     * Executes: neato -Tsvg input.dot -o output.svg
     *
     * @param dotFilePath  the path to the input DOT file
     * @param svgFilePath  the path where the SVG output should be written
     * @throws IOException if there's an error reading the DOT file or writing the SVG file
     * @throws GraphvizException if neato execution fails or produces invalid output
     */
    public void renderSvg(String dotFilePath, String svgFilePath) throws IOException, GraphvizException {
        if (dotFilePath == null || dotFilePath.trim().isEmpty()) {
            throw new IllegalArgumentException("DOT file path cannot be null or empty");
        }
        if (svgFilePath == null || svgFilePath.trim().isEmpty()) {
            throw new IllegalArgumentException("SVG file path cannot be null or empty");
        }
        
        Path dotPath = Paths.get(dotFilePath);
        Path svgPath = Paths.get(svgFilePath);
        
        // Validate DOT file exists
        if (!Files.exists(dotPath)) {
            throw new IOException("DOT file not found: " + dotFilePath);
        }
        
        // Validate DOT file is readable
        if (!Files.isReadable(dotPath)) {
            throw new IOException("DOT file is not readable: " + dotFilePath);
        }
        
        // Create parent directory for SVG if it doesn't exist
        Path svgParent = svgPath.getParent();
        if (svgParent != null && !Files.exists(svgParent)) {
            Files.createDirectories(svgParent);
        }
        
        // Find neato executable path
        String neatoPath = findNeatoPath();
        
        // Execute neato command
        ProcessBuilder processBuilder = new ProcessBuilder(
                neatoPath,
                "-Tsvg",
                dotPath.toString(),
                "-o",
                svgPath.toString()
        );
        
        processBuilder.redirectErrorStream(true);
        
        try {
            Process process = processBuilder.start();
            
            // Wait for process to complete with timeout
            boolean finished = process.waitFor(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            
            if (!finished) {
                process.destroyForcibly();
                throw new GraphvizException("neato command timed out after " + TIMEOUT_SECONDS + " seconds");
            }
            
            int exitCode = process.exitValue();
            
            if (exitCode != 0) {
                // Read error output
                String errorOutput = readProcessOutput(process);
                throw new GraphvizException("neato command failed with exit code " + exitCode + 
                        (errorOutput.isEmpty() ? "" : ": " + errorOutput));
            }
            
            // Validate SVG file was created
            if (!Files.exists(svgPath)) {
                throw new GraphvizException("SVG file was not created: " + svgFilePath);
            }
            
            // Validate SVG file is not empty
            if (Files.size(svgPath) == 0) {
                throw new GraphvizException("SVG file is empty: " + svgFilePath);
            }
            
            // Basic validation: check if file starts with SVG marker
            String firstLine = Files.readAllLines(svgPath, StandardCharsets.UTF_8).get(0);
            if (!firstLine.trim().startsWith("<?xml") && !firstLine.trim().startsWith("<svg")) {
                throw new GraphvizException("Generated file does not appear to be valid SVG: " + svgFilePath);
            }
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new GraphvizException("neato command was interrupted", e);
        } catch (IOException e) {
            throw new GraphvizException("Error executing neato command: " + e.getMessage(), e);
        }
    }
    
    /**
     * Reads the output from a process (stdout and stderr combined).
     *
     * @param process the process to read from
     * @return the process output as a string
     */
    private String readProcessOutput(Process process) {
        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (output.length() > 0) {
                    output.append("\n");
                }
                output.append(line);
            }
        } catch (IOException e) {
            // If we can't read output, return empty string
            return "";
        }
        return output.toString();
    }
    
    /**
     * Exception thrown when Graphviz (neato) execution fails.
     */
    public static class GraphvizException extends Exception {
        public GraphvizException(String message) {
            super(message);
        }
        
        public GraphvizException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}

