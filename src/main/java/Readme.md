# Java Source Code

This directory contains all Java source code for the Railway Terminal Management System.

---

## Structure

The Java source code is organized into several main packages:

### Cargo Handling at a Railway Terminal
**Package**: `Cargo Handling at a Railway Terminal/`

Warehouse and cargo management functionality (Sprint 1):
- Item management and catalog
- Warehouse organization (bays, aisles)
- Picking plan generation
- Quality control operations
- Wagon unloading

**Key Classes**:
- `DataLoader`: Loads data from CSV files
- `WagonsUnloadingController`: Manages wagon unloading operations
- `PickPathController`: Generates picking paths
- `QualityControlController`: Handles quality control

### Railway Network Optimization
**Package**: `Railway Network Optimization/`

Graph algorithms and network optimization (Sprint 3):
- Topological sort and cycle detection
- Maximum flow algorithms
- Shortest path algorithms
- Graph visualization
- Network analysis

**Key Classes**:
- `RailwayGraph`: Directed graph representation
- `TopologicalSort`: Topological sorting algorithms
- `CycleDetection`: Cycle detection in graphs
- `BellmanFord`: Risk-aware shortest paths
- `FordFulkersonAlgorithm`: Maximum flow calculation
- `UpgradePlanController`: Upgrade plan orchestration
- `ShortestPathController`: Shortest path calculation
- `MaximumFlowController`: Maximum flow calculation

### Spatial & Indexed Queries
**Package**: `Spatial & indexed queries/`

Geographical search and spatial data structures (Sprint 2):
- KD-tree implementation
- AVL tree implementation
- Proximity search
- Radius search
- Geographical area queries

**Key Classes**:
- `KDTree`: KD-tree data structure
- `AVL`: AVL tree implementation
- `ProximitySearchController`: Nearest neighbor search
- `RadiusSearchController`: Radius-based search
- `GeographicalSearchController`: Area-based search

### Lapr
**Package**: `Lapr/`

Freight management and train operations (Sprint 2 & 3):
- Train dispatch scheduling
- Route planning
- Train assembly
- Travel time calculations

**Key Classes**:
- `SchedulerService`: Train scheduling service
- `SchedulerFXUI`: JavaFX UI for train scheduling
- `DispatchController`: Train dispatch management
- `RoutePlannerFXUI`: Route planning interface
- `TrainAssemblyFXUI`: Train assembly interface

### Main Application
**Package**: `pt/ipp/isep/dei/`

Application entry points:
- `MainApp`: Main JavaFX application
- `Main`: Console application entry point

---

## Architecture

The codebase follows a **layered architecture**:

### Domain Layer
Core business logic and algorithms:
- Data structures (trees, graphs)
- Algorithms (sorting, searching, optimization)
- Business models (Station, Line, Train, etc.)

### Controller Layer
Business logic coordination:
- Controllers orchestrate domain operations
- Handle data transformation
- Manage business workflows

### Repository Layer
Data access:
- Load data from CSV files
- Database access (when applicable)
- Data caching and management

### UI Layer
User interfaces:
- JavaFX interfaces for modern UI
- Console interfaces for testing
- FXML layouts for UI design

---

## Key Algorithms

### Graph Algorithms
- **Topological Sort**: Kahn's algorithm and DFS-based
- **Cycle Detection**: DFS-based cycle detection
- **Shortest Paths**: Bellman-Ford algorithm
- **Maximum Flow**: Ford-Fulkerson algorithm

### Data Structures
- **KD-Tree**: 2D spatial indexing
- **AVL Tree**: Self-balancing binary search tree
- **RailwayGraph**: Directed graph with adjacency lists

### Search Algorithms
- **Nearest Neighbor**: Branch-and-bound with priority queue
- **Radius Search**: KD-tree traversal with distance filtering
- **Geographical Search**: Range queries with filtering

---

## Running the Application

### JavaFX Application

```bash
mvn javafx:run
```

The main JavaFX application provides a modern UI for:
- Train Dispatch Scheduler
- Route Planner
- Train Assembly
- Network Optimization Tools

### Console Application

```bash
mvn exec:java -Dexec.mainClass="pt.ipp.isep.dei.Main"
```

---

## Testing

All classes have corresponding test classes in `src/test/java/`:

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=UpgradePlanControllerTest
```

See [`../test/Readme.md`](../test/Readme.md) for test documentation.

---

## Dependencies

Key dependencies (see `pom.xml`):
- **JavaFX**: 17.0.2 - UI framework
- **JUnit**: 5.10.0 - Testing framework
- **Oracle JDBC**: Database connectivity

---

## Code Style

- Follows Java naming conventions
- Package structure reflects domain organization
- Controllers follow MVC pattern
- Domain classes are independent of frameworks

---

## Related Documentation

- [Main README](../../README.md)
- [Test Documentation](../test/Readme.md)
- [SQL Scripts](../sql-scripts/Readme.md)
- [Resources](../resources/Readme.md)
