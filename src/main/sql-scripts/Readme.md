# SQL Scripts

This folder contains all SQL scripts used to create, populate, and maintain the project's database.

---

## Core Scripts

### InitialSchema Folder

The `InitialSchema/` folder contains the foundational database scripts:

- **`schema.sql`** - Defines the complete database schema including:
  - Tables and their relationships
  - Primary keys and foreign keys
  - Constraints and indexes
  - Data types and column definitions
  
  **Usage:** Run this script first to create the database structure.

- **`initial_data.sql`** - Inserts initial seed data into the database for development and testing purposes.
  
  **Usage:** Run after `schema.sql` to populate the database with sample data.

---

## User Story Scripts

### USBD08
**Folder:** `USBD08/`

- **`USBD8.sql`** - User Story 8: List of all locomotives of a given type.

### USBD13
**Folder:** `USBD13/`

- **`USBD13.sql`** - User Story 13: List of all locomotives of a given type and gauge that belong to a given operator.

### USBD15
**Folder:** `USBD15/`

- **`USBD15.sql`** - User Story 15: How many lines depart from a given endpoint.

### USBD19
**Folder:** `USBD19/`

- **`USBD19.sql`** - User Story 19: Number of segments of a given owner, by track type.

### USBD24
**Folder:** `USBD24/`

- **`USBD24.sql`** - User Story 24: Implementation script.
- **`USBD24Test.sql`** - Test script for User Story 24.

### USBD25
**Folder:** `USBD25/`

- **`USBD25.sql`** - User Story 25: Implementation script.
- **`USBD25Test.sql`** - Test script for User Story 25.

### USBD28
**Folder:** `USBD28/`

- **`USBD28.sql`** - User Story 28: Implementation script.

### USBD30
**Folder:** `USBD30/`

- **`USBD30.sql`** - User Story 30: Implementation script.
- **`USBD30Test.sql`** - Test script for User Story 30.

---


## Folder Structure

```
sql-scripts/
├── InitialSchema/
│   ├── schema.sql              # Database schema definition
│   └── initial_data.sql        # Initial seed data
├── USBD08/
│   └── USBD8.sql
├── USBD13/
│   └── USBD13.sql
├── USBD15/
│   └── USBD15.sql
├── USBD19/
│   └── USBD19.sql
├── USBD24/
│   ├── USBD24.sql
│   └── USBD24Test.sql
├── USBD25/
│   ├── USBD25.sql
│   └── USBD25Test.sql
├── USBD28/
│   └── USBD28.sql
└── USBD30/
    ├── USBD30.sql
    └── USBD30Test.sql
```
