package Domain;

import java.util.*;

/**
 * Utility class for exporting Graph objects to DOT format for Graphviz visualization.
 * 
 * Converts a Graph to a valid DOT string with:
 * - Nodes (stations) with labels and positions
 * - Edges (connections) as undirected (using --)
 * - Deterministic ordering of nodes and edges
 * 
 * This class follows Clean Architecture principles:
 * - Pure domain utility with no external dependencies
 * - Deterministic output for same input
 *
 * @author Development Team
 * @version 1.0
 */
public class DotExporter {
    
    /**
     * Exports a Graph to DOT format string.
     * 
     * The output is deterministic:
     * - Nodes are sorted by stationId (ascending)
     * - Edges are sorted by originId, then destinationId (ascending)
     * 
     * Format:
     * - Nodes: stationId [label="name", pos="x,y!"];
     * - Edges: originId -- destinationId;
     *
     * @param graph the graph to export (cannot be null)
     * @return a valid DOT format string
     * @throws IllegalArgumentException if graph is null
     */
    public String exportToDot(Graph graph) {
        if (graph == null) {
            throw new IllegalArgumentException("Graph cannot be null");
        }
        
        StringBuilder dot = new StringBuilder();
        
        // DOT header for undirected graph
        dot.append("graph G {\n");
        dot.append("    layout=neato;\n");
        dot.append("    node [shape=circle];\n\n");
        
        // Export nodes (stations) - sorted deterministically
        List<Station> stations = new ArrayList<>(graph.getStations());
        stations.sort(Comparator.comparing(Station::getId));
        
        for (Station station : stations) {
            String nodeId = escapeNodeId(station.getId());
            String label = escapeLabel(station.getName());
            double x = station.getCoordX();
            double y = station.getCoordY();
            
            dot.append(String.format("    %s [label=\"%s\", pos=\"%.2f,%.2f!\"];\n", 
                    nodeId, label, x, y));
        }
        
        dot.append("\n");
        
        // Export edges (connections) - sorted deterministically
        Set<Connection> uniqueConnections = graph.getAllUniqueConnections();
        List<Connection> sortedConnections = new ArrayList<>(uniqueConnections);
        sortedConnections.sort((c1, c2) -> {
            int originCompare = c1.getOriginId().compareTo(c2.getOriginId());
            if (originCompare != 0) {
                return originCompare;
            }
            return c1.getDestinationId().compareTo(c2.getDestinationId());
        });
        
        for (Connection connection : sortedConnections) {
            String originId = escapeNodeId(connection.getOriginId());
            String destinationId = escapeNodeId(connection.getDestinationId());
            
            dot.append(String.format("    %s -- %s;\n", originId, destinationId));
        }
        
        dot.append("}\n");
        
        return dot.toString();
    }
    
    /**
     * Escapes a node ID for DOT format.
     * DOT node IDs must be valid identifiers. If the ID contains special characters,
     * it needs to be quoted or escaped.
     *
     * @param nodeId the node ID to escape
     * @return the escaped node ID
     */
    private String escapeNodeId(String nodeId) {
        if (nodeId == null || nodeId.isEmpty()) {
            return "\"\"";
        }
        
        // Check if nodeId is a valid DOT identifier (starts with letter/underscore, contains letters/digits/underscores)
        if (nodeId.matches("^[a-zA-Z_][a-zA-Z0-9_]*$")) {
            return nodeId;
        }
        
        // If not valid identifier, quote it
        return "\"" + nodeId.replace("\"", "\\\"") + "\"";
    }
    
    /**
     * Escapes a label string for DOT format.
     * Handles special characters that need escaping in DOT labels.
     *
     * @param label the label to escape
     * @return the escaped label
     */
    private String escapeLabel(String label) {
        if (label == null) {
            return "";
        }
        
        // Escape special characters in DOT labels
        return label.replace("\\", "\\\\")
                   .replace("\"", "\\\"")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r");
    }
}

