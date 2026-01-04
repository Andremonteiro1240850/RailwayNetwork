# Railway Terminal Management System

**G044 Team Repository** - Semester 3 Project (2025-2026)

A comprehensive Java application for managing train stations, geographical searches, warehouse operations, freight management, and railway network optimization.

---

## Table of Contents

- [Overview](#overview)
- [Project Structure](#project-structure)
- [Technologies](#technologies)
- [Getting Started](#getting-started)
- [Documentation](#documentation)
- [User Stories](#user-stories)
- [Testing](#testing)
- [Datasets](#datasets)
- [Quick Links](#quick-links)

---

## Overview

This project implements a Railway Terminal Management System with the following main features:

- **Geographical Search**: KD-tree based spatial data structures for efficient geographical queries
- **Proximity Search**: Nearest neighbor and radius search algorithms with branch-and-bound optimization
- **Warehouse Management**: Inventory management, picking plans, and quality control
- **Freight Management**: Train configuration, dispatch operations, route planning, and scheduling
- **Railway Network Optimization**: Graph algorithms for shortest paths, maximum flow, and upgrade planning
- **Database Integration**: SQL database with comprehensive schema and SQL scripts
- **Assembly Code**: Low-level assembly implementations for performance-critical operations
- **Modern UI**: JavaFX-based user interface with professional styling

---

## Project Structure

```
├── doc/                           # Documentation
│   ├── Sprint1/                    # Sprint 1 user stories (USEI01-USEI05)
│   ├── Sprint2/                    # Sprint 2 user stories (USEI06-USEI10, USLP07)
│   ├── Sprint3/                    # Sprint 3 user stories (USEI11-USEI15, USLP08-USLP10)
│   │   ├── USEI11/                 # Directed Line Upgrade Plan
│   │   ├── USEI12/                 # Graph Visualization
│   │   ├── USEI13/                 # Maximum Flow
│   │   ├── USEI14/                 # Backbone Network
│   │   ├── USEI15/                 # Risk-Aware Shortest Paths
│   │   └── Complexity_Analysis/    # Overall complexity analysis
│   ├── global-artifacts/           # Domain models, conceptual models
│   │   ├── Conceptual Model/       # Conceptual model (VPP, SVG)
│   │   ├── Domain Model/           # Domain model (PlantUML, SVG)
│   │   ├── Relational Model/       # Physical database model
│   │   └── Glossary/               # Project glossary
│   └── auth/                       # Authentication documentation
├── src/
│   ├── main/
│   │   ├── java/                    # Java source code
│   │   │   ├── Cargo Handling at a Railway Terminal/  # Warehouse management
│   │   │   ├── Railway Network Optimization/         # Graph algorithms
│   │   │   ├── Spatial & indexed queries/            # Geographical search
│   │   │   ├── Lapr/                                 # Freight management
│   │   │   └── pt/ipp/isep/dei/                      # Main application entry point
│   │   ├── resources/               # Configuration files, FXML, CSS
│   │   │   ├── layout.fxml          # Main UI layout
│   │   │   └── styles.css           # Modern UI styling
│   │   └── sql-scripts/             # Database scripts
│   │       ├── InitialSchema/       # Schema and initial data
│   │       ├── Sprint1/              # Sprint 1 database scripts
│   │       ├── Sprint2/              # Sprint 2 database scripts
│   │       └── Sprint3/              # Sprint 3 database scripts
│   └── test/                        # Test code
│       └── java/                    # JUnit 5 test classes
├── Arqcp/                           # Assembly code (RISC-V)
│   ├── Sprint2/                     # Sprint 2 assembly implementations
│   └── Sprint3/                      # Sprint 3 embedded systems code
├── train_station_dataset/           # Sample datasets (CSV files)
│   └── Sprint3/                     # Sprint 3 specific datasets
├── pom.xml                          # Maven configuration
└── README.md                        # This file
```

---

## Technologies

- **Java**: 11+
- **JavaFX**: 17.0.2 (for modern UI)
- **JUnit**: 5.10.0 (for testing)
- **Maven**: Build and dependency management
- **Oracle Database**: Database backend
- **PlantUML**: Documentation diagrams
- **RISC-V Assembly**: Low-level optimizations

---

## Getting Started

### Prerequisites

- Java 11 or higher
- Maven 3.6+
- Oracle Database (for database features)
- Git

### Building the Project

```bash
# Clone the repository
git clone <repository-url>
cd sem3-pi-25-26-g044-repo

# Build the project
mvn clean install

# Run tests
mvn test

# Run the application
mvn javafx:run
```

### Database Setup

1. Connect to Oracle Database:
   - Host: `vsgate-s1.dei.isep.ipp.pt`
   - Port: `10715`
   - SID: `XE`
   - Username: `System`
   - Password: `G0442526BD`

2. Run SQL scripts in order:
   ```bash
   # 1. Create schema
   sqlplus System@vsgate-s1.dei.isep.ipp.pt:10715/XE @src/main/sql-scripts/InitialSchema/schema.sql
   
   # 2. Load initial data
   sqlplus System@vsgate-s1.dei.isep.ipp.pt:10715/XE @src/main/sql-scripts/InitialSchema/initial_data.sql
   ```

See [`src/main/sql-scripts/Readme.md`](src/main/sql-scripts/Readme.md) for detailed database setup instructions.

---

## Documentation

### Main Documentation

- **Global Artifacts**: [`doc/global-artifacts/`](doc/global-artifacts/)
  - Conceptual Model (domain perspective)
  - Domain Model (UML diagrams)
  - Physical Database Model
  - Glossary (BDAD and Project terms)

### User Story Documentation

Each user story (USEI/USLP) has its own documentation folder in `doc/`:

#### Sprint 1 (USEI01-USEI05)
- Warehouse management features
- Class diagrams

#### Sprint 2 (USEI06-USEI10, USLP07)
| User Story | Description | Documentation |
|------------|-------------|---------------|
| **USEI06** | AVL tree data structures | Class diagram, Complexity analysis |
| **USEI07** | KD-tree construction and range queries | Class diagram, Complexity analysis |
| **USEI08** | Geographical area search | Class diagram, Complexity analysis |
| **USEI09** | Proximity search (nearest neighbor) | Class diagram, Complexity analysis |
| **USEI10** | Radius search with density summary | Class diagram, Complexity analysis |
| **USLP07** | Train Dispatch Scheduler | Class diagram |

#### Sprint 3 (USEI11-USEI15, USLP08-USLP10)
| User Story | Description | Documentation |
|------------|-------------|---------------|
| **USEI11** | Directed Line Upgrade Plan | Class diagram, Complexity analysis |
| **USEI12** | Graph Visualization (DOT export) | Class diagram, Complexity analysis |
| **USEI13** | Maximum Flow Algorithm | Class diagram, Complexity analysis |
| **USEI14** | Backbone Network Identification | Class diagram, Complexity analysis |
| **USEI15** | Risk-Aware Shortest Paths (Bellman-Ford) | Class diagram, Complexity analysis |
| **USLP08** | Route Planner | Class diagram |
| **USLP09** | Train Assembly and Assignment | Class diagram |
| **USLP10** | Train Dispatch Scheduler (Enhanced) | Class diagram |

Each USEI folder contains:
- **Class Diagrams**: PlantUML format with SVG exports
- **Complexity Analysis**: Detailed time complexity analysis (best/average/worst case)
- **Implementation Details**: Algorithm descriptions and design decisions

### Additional Documentation

- **SQL Scripts**: [`src/main/sql-scripts/Readme.md`](src/main/sql-scripts/Readme.md)
- **Assembly Code**: [`Arqcp/README.md`](Arqcp/README.md)
- **Test Documentation**: [`src/test/Readme.md`](src/test/Readme.md)
- **Dataset Schema**: [`train_station_dataset/README.md`](train_station_dataset/README.md)
- **Java Source**: [`src/main/java/Readme.md`](src/main/java/Readme.md)
- **Resources**: [`src/main/resources/Readme.md`](src/main/resources/Readme.md)

---

## User Stories

### Warehouse Management (USEI01-USEI05)
- Item management and catalog
- Warehouse organization (bays, aisles)
- Picking plan generation
- Quality control operations
- Trolley management

### Data Structures (USEI06-USEI07)
- **USEI06**: AVL tree implementation for efficient station management
  - Multiple sorting criteria (latitude, longitude, country/timezone)
  - Automatic tree balancing
  - O(log n) operations
  
- **USEI07**: KD-tree construction for geographical data
  - Balanced 2D-tree using bulk-build algorithm
  - Range queries for rectangular areas
  - O(n log n) construction, O(√n) average query time

### Geographical Search (USEI08-USEI10)
- **USEI08**: Range queries and geographical area search
  - Filtering by country, city status, main station
  - Optimized sorting (filter before sort)
  
- **USEI09**: Nearest neighbor search with filters
  - Branch-and-bound algorithm
  - Priority queue for maintaining N closest stations
  - Haversine distance calculation
  
- **USEI10**: Radius search with aggregation
  - Density summary by country and city status
  - AVL tree for sorted result storage

### Railway Network Optimization (USEI11-USEI15)
- **USEI11**: Directed Line Upgrade Plan
  - Topological sort for upgrade dependencies
  - Cycle detection in directed graphs
  - O(V+E) complexity
  
- **USEI12**: Graph Visualization
  - DOT format export
  - Graphviz rendering support
  - Visual representation of railway networks
  
- **USEI13**: Maximum Flow Algorithm
  - Ford-Fulkerson algorithm implementation
  - Capacity optimization
  - Network flow analysis
  
- **USEI14**: Backbone Network Identification
  - Critical path identification
  - Network resilience analysis
  
- **USEI15**: Risk-Aware Shortest Paths
  - Bellman-Ford algorithm
  - Negative cycle detection
  - Cost optimization with risk factors

### Freight Management (USLP07-USLP10)
- **USLP07**: Train Dispatch Scheduler
  - Physics-based scheduling
  - Conflict detection and resolution
  - Travel time calculations
  
- **USLP08**: Route Planner
  - Simple and complex route definition
  - Route assignment to trains
  
- **USLP09**: Train Assembly and Assignment
  - Locomotive and wagon selection
  - Train assembly
  - Route assignment
  
- **USLP10**: Train Dispatch Scheduler (Enhanced)
  - Enhanced scheduling with pre-assembled trains
  - Advanced conflict resolution
  - Real-time dispatch management

### Assembly Code (USAC01-USAC09)
- **USAC01**: Data encryption
- **USAC02**: Data decryption
- **USAC03**: Data extraction
- **USAC04**: Command formatting
- **USAC05**: Queue enqueue operation
- **USAC06**: Queue dequeue operation
- **USAC07**: Move n elements to array
- **USAC08**: Array sorting
- **USAC09**: Median calculation

### Database Scripts (USBD08-USBD30)
- **USBD08**: List locomotives by type
- **USBD13**: List locomotives by type, gauge, and operator
- **USBD15**: Count lines departing from endpoint
- **USBD19**: Count segments by owner and track type
- **USBD24, USBD25, USBD28, USBD30**: Additional database queries

---

## Testing

The project uses **JUnit 5** for comprehensive testing:

```bash
# Run all tests
mvn test

# Run tests with coverage
mvn test jacoco:report
```

### Test Coverage

- **RailwayGraph**: 100% method and line coverage
- **UpgradePlanController**: Comprehensive test suite with real data
- **TopologicalSort**: Full algorithm coverage
- **CycleDetection**: Complete cycle detection tests
- **BellmanFord**: Risk-aware shortest path tests

See [`src/test/Readme.md`](src/test/Readme.md) for detailed test documentation.

---

## Datasets

Sample datasets are provided in `train_station_dataset/`:

### Station Data
- `train_stations_europe.csv`: European train stations with coordinates
- `Sprint3/stations.csv`: Sprint 3 station data
- `Sprint3/lines.csv`: Sprint 3 line/edge data

### Warehouse Data
- `bays.csv`: Warehouse bay information
- `wagons.csv`: Wagon inventory
- `items.csv`: Item catalog
- `orders.csv`, `order_lines.csv`: Order management
- `returns.csv`: Return information

### Infrastructure Data
- `facility.csv`: Facility information
- `line.csv`: Railway line information
- `locomotives.csv`: Locomotive data

See [`train_station_dataset/README.md`](train_station_dataset/README.md) for complete schema details.

---

## Quick Links

### Documentation
- [Project Documentation](doc/)
- [Global Artifacts](doc/global-artifacts/)
- [Database Scripts](src/main/sql-scripts/Readme.md)
- [Assembly Code](Arqcp/README.md)
- [Test Documentation](src/test/Readme.md)
- [Dataset Schema](train_station_dataset/README.md)

### Key Components
- [KD-Tree Implementation](src/main/java/Spatial%20&%20indexed%20queries/Domain/KDTree.java)
- [AVL Tree Implementation](src/main/java/Spatial%20&%20indexed%20queries/Domain/AVL.java)
- [RailwayGraph](src/main/java/Railway%20Network%20Optimization/Domain/RailwayGraph.java)
- [TopologicalSort](src/main/java/Railway%20Network%20Optimization/Domain/TopologicalSort.java)
- [BellmanFord](src/main/java/Railway%20Network%20Optimization/Domain/BellmanFord.java)
- [Main Application](src/main/java/pt/ipp/isep/dei/MainApp.java)

### User Stories
- [Sprint 1 - Warehouse Management](doc/Sprint1/)
- [Sprint 2 - Data Structures & Search](doc/Sprint2/)
- [Sprint 3 - Network Optimization](doc/Sprint3/)

---

## Team

**G044 Team** - Semester 3, Academic Year 2025-2026

---

## Architecture

The project follows a **layered architecture**:

- **UI Layer**: JavaFX user interfaces for interaction
- **Controller Layer**: Business logic and coordination
- **Domain Layer**: Core business models and algorithms
- **Repository Layer**: Data access and persistence
- **Database Layer**: Oracle SQL database

### Design Patterns

- **MVC (Model-View-Controller)**: Separation of concerns
- **Repository Pattern**: Data access abstraction
- **Strategy Pattern**: Algorithm selection
- **Factory Pattern**: Object creation

---

## Performance

### Complexity Highlights

- **KD-Tree Construction**: O(n log n) deterministic
- **Nearest Neighbor Search**: 
  - Best case: O(log N + n log n)
  - Average case: O(√N log n)
  - Worst case: O(N log n)
- **Radius Search**:
  - Average case: O(√N + k log k)
  - Worst case: O(N + k log k)
- **AVL Operations**: O(log n) for all operations
- **Topological Sort**: O(V+E) using Kahn's algorithm or DFS
- **Bellman-Ford**: O(V×E) for shortest paths with negative weights
- **Maximum Flow (Ford-Fulkerson)**: O(E×f) where f is maximum flow

See individual USEI complexity documentation for detailed analysis.

---

## License

This project is part of an academic course and is intended for educational purposes.

---

## Contributing

This is a team project for academic purposes. For questions or issues, please contact the team.
