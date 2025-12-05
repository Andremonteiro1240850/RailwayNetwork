# Train Station Management System

**G044 Team Repository** - Semester 3 Project (2025-2026)

A comprehensive Java application for managing train stations, geographical searches, warehouse operations, and freight management.

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

This project implements a train station management system with the following main features:

- **Geographical Search**: KD-tree based spatial data structures for efficient geographical queries
- **Proximity Search**: Nearest neighbor and radius search algorithms with branch-and-bound optimization
- **Warehouse Management**: Inventory management, picking plans, and quality control
- **Freight Management**: Train configuration, dispatch operations, and route planning
- **Database Integration**: SQL database with comprehensive schema and SQL scripts
- **Assembly Code**: Low-level assembly implementations for performance-critical operations

---

## Project Structure

```
├── doc/                           # Documentation
│   ├── USEI01-USEI10/             # User story documentation
│   │   ├── Class Diagram/         # PlantUML class diagrams
│   │   └── Complexity/            # Complexity analysis
│   ├── global-artifacts/          # Domain models, conceptual models
│   │   ├── Conceptual Model/      # Conceptual model (VPP, SVG)
│   │   ├── Domain Model/          # Domain model (PlantUML, SVG)
│   │   ├── Realtional Model/      # Physical database model
│   │   └── Glossary/              # Project glossary
│   └── auth/                      # Authentication documentation
├── src/
│   ├── main/
│   │   ├── java/                  # Java source code
│   │   │   ├── Controller/        # Controllers (MVC pattern)
│   │   │   ├── Domain/            # Domain models and business logic
│   │   │   ├── Repository/        # Data access layer
│   │   │   ├── UI/                # User interfaces
│   │   │   └── pt/ipp/isep/dei/   # Main application entry point
│   │   ├── Arqcp/                 # Assembly code (RISC-V)
│   │   │   ├── asm.h              # Assembly function declarations
│   │   │   └── USAC01-USAC09/     # User story assembly implementations
│   │   ├── sql-scripts/           # Database scripts
│   │   │   ├── InitialSchema/     # Schema and initial data
│   │   │   └── USBD08-USBD30/     # User story database scripts
│   │   └── resources/             # Configuration files
│   └── test/                      # Test code
│       └── java/                  # JUnit test classes
├── train_station_dataset/         # Sample datasets (CSV files)
├── pom.xml                        # Maven configuration
└── README.md                      # This file
```

---




## Documentation

### Main Documentation

- **Global Artifacts**: [`doc/global-artifacts/Readme.md`](doc/global-artifacts/Readme.md)
  - Conceptual Model (domain perspective)
  - Domain Model (UML diagrams)
  - Physical Database Model
  - Glossary (BDAD and Project terms)

### User Story Documentation

Each user story (USEI) has its own documentation folder in `doc/`:

| User Story | Description | Documentation |
|------------|-------------|---------------|
| **USEI01-USEI05** | Warehouse management features | Class diagrams |
| **USEI06** | AVL tree data structures | Class diagram, Complexity analysis |
| **USEI07** | KD-tree construction and range queries | Class diagram, Complexity analysis |
| **USEI08** | Geographical area search | Class diagram, Complexity analysis |
| **USEI09** | Proximity search (nearest neighbor) | Class diagram, Complexity analysis |
| **USEI10** | Radius search with density summary | Class diagram, Complexity analysis |

Each USEI folder contains:
- **Class Diagrams**: PlantUML format with SVG exports
- **Complexity Analysis**: Detailed time complexity analysis (best/average/worst case)
- **Implementation Details**: Algorithm descriptions and design decisions

### Additional Documentation

- **SQL Scripts**: [`src/main/sql-scripts/Readme.md`](src/main/sql-scripts/Readme.md)
- **Assembly Code**: [`src/main/Arqcp/README.md`](src/main/Arqcp/README.md)
- **Test Documentation**: [`src/test/Readme.md`](src/test/Readme.md)
- **Dataset Schema**: [`train_station_dataset/README.md`](train_station_dataset/README.md)

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

### Freight Management (USLP07)
- Train configuration
- Dispatch management
- Travel time calculations
- Wagon unloading operations

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

## Datasets

Sample datasets are provided in `train_station_dataset/`:

### Station Data
- `train_stations_europe.csv`: European train stations with coordinates

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
- [Global Artifacts](doc/global-artifacts/Readme.md)
- [Database Scripts](src/main/sql-scripts/Readme.md)
- [Assembly Code](src/main/Arqcp/README.md)
- [Test Documentation](src/test/Readme.md)
- [Dataset Schema](train_station_dataset/README.md)

### Key Components
- [KD-Tree Implementation](src/main/java/Domain/KDTree.java)
- [AVL Tree Implementation](src/main/java/Domain/AVL.java)
- [Main Application](src/main/java/pt/ipp/isep/dei/Main.java)

### User Stories
- [USEI06 - AVL Trees](doc/USEI06/)
- [USEI07 - KD-Tree Construction](doc/USEI07/)
- [USEI08 - Geographical Search](doc/USEI08/)
- [USEI09 - Proximity Search](doc/USEI09/)
- [USEI10 - Radius Search](doc/USEI10/)

---

## Team

**G044 Team** - Semester 3, Academic Year 2025-2026

---


## Architecture

The project follows a **layered architecture**:

- **UI Layer**: User interfaces for interaction
- **Controller Layer**: Business logic and coordination
- **Domain Layer**: Core business models and algorithms
- **Repository Layer**: Data access and persistence
- **Database Layer**: SQL database


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

See individual USEI complexity documentation for detailed analysis.


