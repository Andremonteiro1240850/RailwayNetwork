# Documentation

This directory contains all project documentation including user story documentation, models, and analysis.

---

## Structure

### User Story Documentation

Each user story (USEI) has its own folder with documentation:

- **USEI01-USEI05**: Warehouse management features
  - Class diagrams (PlantUML)
  
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
- Algorithm descriptions

---

## Accessing Documentation

### User Stories

Navigate to the specific USEI folder:
- `doc/USEI01-USEI05/` - Warehouse management
- `doc/USEI06/` - AVL trees
- `doc/USEI07/` - KD-tree construction
- `doc/USEI08/` - Geographical search
- `doc/USEI09/` - Proximity search
- `doc/USEI10/` - Radius search
- `doc/USLP07/` - Train Dispatcher

### Models

- Conceptual Model: `doc/global-artifacts/Conceptual Model/`
- Domain Model: `doc/global-artifacts/Domain Model/`
- Relational Model: `doc/global-artifacts/Phisical Model/`

### Glossary

- Project Glossary: `doc/global-artifacts/Glossary/GLOSSARYPROJ.md`
- BDAD Glossary: `doc/global-artifacts/Glossary/GLOSSARYBDAD.md`

---

## Related Documentation

For additional project documentation, see:
- Main README: [`../README.md`](../README.md)
- SQL Scripts: [`../src/main/sql-scripts/Readme.md`](../src/main/sql-scripts/Readme.md)
- Assembly Code: [`../src/main/Arqcp/README.md`](../src/main/Arqcp/README.md)
- Test Documentation: [`../src/test/Readme.md`](../src/test/Readme.md)
- Dataset Schema: [`../train_station_dataset/README.md`](../train_station_dataset/README.md)

