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
    private static final int TIMEOUT_SECONDS = 60;  // Increased timeout for large graphs
    
    // Common Graphviz installation paths on Windows
    private static final String[] WINDOWS_GRAPHVIZ_PATHS = {
        "C:\\Program Files\\Graphviz\\bin\\neato.exe",
        "C:\\Program Files (x86)\\Graphviz\\bin\\neato.exe",
        "C:\\Graphviz\\bin\\neato.exe",
        "C:\\Program Files\\Graphviz12\\bin\\neato.exe"
    };
    
    /**
     * Finds the neato executable path.
     * On Windows, checks common installation paths first for reliability.
     * Then tries to use 'neato' from PATH.
     * 
     * @return the path to neato executable, or "neato" if found in PATH
     */
    private static String findNeatoPath() {
        String os = System.getProperty("os.name").toLowerCase();
        
        // On Windows, check common installation paths FIRST (more reliable)
        if (os.contains("win")) {
            for (String path : WINDOWS_GRAPHVIZ_PATHS) {
                File neatoFile = new File(path);
                if (neatoFile.exists() && neatoFile.isFile()) {
                    System.out.println("[GraphvizRenderer] Found neato at: " + path);
                    return path;
                }
            }
        }
        
        // Try to verify if 'neato' is available in PATH
        if (isCommandAvailable(NEATO_COMMAND)) {
            System.out.println("[GraphvizRenderer] Using neato from PATH");
            return NEATO_COMMAND;
        }
        
        // Fallback to PATH (will fail with better error message if not found)
        System.out.println("[GraphvizRenderer] WARNING: neato not found, trying PATH anyway");
        return NEATO_COMMAND;
    }
    
    /**
     * Checks if a command is available in the system PATH.
     * 
     * @param command the command name to check
     * @return true if the command is available, false otherwise
     */
    private static boolean isCommandAvailable(String command) {
        try {
            ProcessBuilder pb = new ProcessBuilder(command, "-V");
            pb.redirectErrorStream(true);
            pb.redirectOutput(ProcessBuilder.Redirect.DISCARD);
            Process process = pb.start();
            boolean finished = process.waitFor(2, TimeUnit.SECONDS);
            if (finished) {
                return process.exitValue() == 0;
            }
            process.destroyForcibly();
            return false;
        } catch (Exception e) {
            return false;
        }
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
        
        // Execute neato command with -n flag to use fixed positions from pos attribute
        // The -n flag tells neato to use the pos attributes from the DOT file
        System.out.println("[GraphvizRenderer] Executing: " + neatoPath + " -n -Tsvg " + dotPath + " -o " + svgPath);
        ProcessBuilder processBuilder = new ProcessBuilder(
                neatoPath,
                "-n",
                "-Tsvg",
                dotPath.toString(),
                "-o",
                svgPath.toString()
        );
        
        processBuilder.redirectErrorStream(true);
        
        try {
            Process process = processBuilder.start();
            
            // Read output in a separate thread to prevent blocking
            StringBuilder outputBuilder = new StringBuilder();
            Thread outputReader = new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        outputBuilder.append(line).append("\n");
                        System.out.println("[neato] " + line);
                    }
                } catch (IOException e) {
                    // Ignore
                }
            });
            outputReader.start();
            
            // Wait for process to complete with timeout
            boolean finished = process.waitFor(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            
            // Wait for output reader to finish
            outputReader.join(1000);
            
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

