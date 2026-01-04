# Documentation

This directory contains all project documentation including user story documentation, models, and analysis.

---

## Structure

### User Story Documentation

Each user story (USEI/USLP) has its own folder with documentation organized by Sprint:

#### Sprint 1 (USEI01-USEI05)
- **USEI01-USEI05**: Warehouse management features
  - Class diagrams (PlantUML)
  - Implementation details

#### Sprint 2 (USEI06-USEI10, USLP07)
- **USEI06**: AVL tree data structures
  - Class diagram
  - Complexity analysis
  
- **USEI07**: KD-tree construction and range queries
  - Class diagram
  - Complexity analysis (time complexity)
  
- **USEI08**: Geographical area search
  - Class diagram
  - Complexity analysis
  
- **USEI09**: Proximity search (nearest neighbor)
  - Class diagram
  - Complexity analysis 
  
- **USEI10**: Radius search with density summary
  - Class diagram
  - Complexity analysis

- **USLP07**: Train Dispatch Scheduler
  - Class diagram

#### Sprint 3 (USEI11-USEI15, USLP08-USLP10)
- **USEI11**: Directed Line Upgrade Plan
  - Class diagram
  - Complexity analysis
  - Topological sort and cycle detection algorithms
  
- **USEI12**: Graph Visualization (DOT export)
  - Class diagram
  - Complexity analysis
  
- **USEI13**: Maximum Flow Algorithm
  - Class diagram
  - Complexity analysis
  - Ford-Fulkerson implementation
  
- **USEI14**: Backbone Network Identification
  - Class diagram
  - Complexity analysis
  
- **USEI15**: Risk-Aware Shortest Paths (Bellman-Ford)
  - Class diagram
  - Complexity analysis
  - Negative cycle detection

- **USLP08**: Route Planner
  - Class diagram

- **USLP09**: Train Assembly and Assignment
  - Class diagram

- **USLP10**: Train Dispatch Scheduler (Enhanced)
  - Class diagram

### Global Artifacts

The `global-artifacts/` folder contains:

- **Conceptual Model**: Domain perspective models (VPP, SVG)
- **Domain Model**: UML domain models (PlantUML, SVG)
- **Relational Model**: Database physical models (VPP, SVG)
- **Glossary**: Project and BDAD glossaries

---

## Documentation Types

### Class Diagrams

All class diagrams are in PlantUML format (`.puml`) with SVG exports for visualization.

### Complexity Analysis

Complexity documentation includes:
- Time complexity analysis (best case, average case, worst case)
- Space complexity analysis
- Algorithm descriptions
- Performance considerations

---

## Accessing Documentation

### User Stories by Sprint

#### Sprint 1
- `doc/Sprint1/USEI01/` - Wagon unloading
- `doc/Sprint1/USEI02/` - Picking plan
- `doc/Sprint1/USEI03/` - Quality control
- `doc/Sprint1/USEI04/` - Item management
- `doc/Sprint1/USEI05/` - Warehouse organization

#### Sprint 2
- `doc/Sprint2/USEI06/` - AVL trees
- `doc/Sprint2/USEI07/` - KD-tree construction
- `doc/Sprint2/USEI08/` - Geographical search
- `doc/Sprint2/USEI09/` - Proximity search
- `doc/Sprint2/USEI10/` - Radius search
- `doc/Sprint2/USLP07/` - Train Dispatcher

#### Sprint 3
- `doc/Sprint3/USEI11/` - Upgrade Plan
- `doc/Sprint3/USEI12/` - Graph Visualization
- `doc/Sprint3/USEI13/` - Maximum Flow
- `doc/Sprint3/USEI14/` - Backbone Network
- `doc/Sprint3/USEI15/` - Shortest Paths
- `doc/Sprint3/Complexity_Analysis/` - Overall complexity

### Models

- Conceptual Model: `doc/global-artifacts/Conceptual Model/`
- Domain Model: `doc/global-artifacts/Domain Model/`
- Relational Model: `doc/global-artifacts/Relational Model/`

### Glossary

- Project Glossary: `doc/global-artifacts/Glossary/GLOSSARYPROJ.md`
- BDAD Glossary: `doc/global-artifacts/Glossary/GLOSSARYBDAD.md`

---

## Related Documentation

For additional project documentation, see:
- Main README: [`../README.md`](../README.md)
- SQL Scripts: [`../src/main/sql-scripts/Readme.md`](../src/main/sql-scripts/Readme.md)
- Assembly Code: [`../Arqcp/README.md`](../Arqcp/README.md)
- Test Documentation: [`../src/test/Readme.md`](../src/test/Readme.md)
- Dataset Schema: [`../train_station_dataset/README.md`](../train_station_dataset/README.md)
- Java Source: [`../src/main/java/Readme.md`](../src/main/java/Readme.md)
- Resources: [`../src/main/resources/Readme.md`](../src/main/resources/Readme.md)

---

## Documentation Standards

- All class diagrams use PlantUML syntax
- Complexity analysis follows standard Big-O notation
- Documentation is kept up-to-date with code changes
- Each user story includes implementation details and design decisions
