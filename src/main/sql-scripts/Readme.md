# SQL Scripts

This folder contains all SQL scripts used to create, populate, and maintain the project's database.

---

## Core Scripts

### Accessing the Database

**Connection Details:**
- **Host**: `vsgate-s1.dei.isep.ipp.pt`
- **Username**: `System`
- **Password**: `G0442526BD`
- **Port**: `10715`
- **SID**: `XE`

**Connection String:**
```
vsgate-s1.dei.isep.ipp.pt:10715/XE
```

---

### InitialSchema Folder

The `InitialSchema/` folder contains the foundational database scripts:

#### `schema.sql`
Defines the complete database schema including:
- Tables and their relationships
- Primary keys and foreign keys
- Constraints and indexes
- Data types and column definitions
- Sequences and triggers

**Usage:** Run this script first to create the database structure.

**Key Tables:**
- `Facility`: Railway facilities (stations, warehouses, etc.)
- `Station`: Train stations
- `Line`: Railway lines
- `Segment`: Line segments
- `Locomotive`: Locomotive information
- `Wagon`: Wagon information
- `Train`: Train configurations
- `Route`: Train routes
- And many more...

#### `initial_data.sql`
Inserts initial seed data into the database for development and testing purposes.

**Usage:** Run after `schema.sql` to populate the database with sample data.

**Contains:**
- Facility types
- Facilities and stations
- Lines and segments
- Locomotives and wagons
- Initial train configurations

---

## User Story Scripts

Scripts are organized by Sprint and User Story:

### Sprint 1

#### USBD08
**Folder:** `Sprint1/USBD08/`

- **`USBD8.sql`** - List of all locomotives of a given type.

#### USBD13
**Folder:** `Sprint1/USBD13/`

- **`USBD13.sql`** - List of all locomotives of a given type and gauge that belong to a given operator.

### Sprint 2

#### USBD15
**Folder:** `Sprint2/USBD15/`

- **`USBD15.sql`** - How many lines depart from a given endpoint.

#### USBD19
**Folder:** `Sprint2/USBD19/`

- **`USBD19.sql`** - Number of segments of a given owner, by track type.

#### USBD24
**Folder:** `Sprint2/USBD24/`

- **`USBD24.sql`** - List of all endpoints of a planned train route.
- **`USBD24Test.sql`** - Test script for User Story 24.

#### USBD25
**Folder:** `Sprint2/USBD25/`

- **`USBD25.sql`** - List of all endpoints of a planned train route where the train does not stop.
- **`USBD25Test.sql`** - Test script for User Story 25.

#### USBD28
**Folder:** `Sprint2/USBD28/`

- **`USBD28.sql`** - List of all locomotives of a given operator that support multiple gauges.
- **`USBD28Test.sql`** - Test script for User Story 28.

#### USBD30
**Folder:** `Sprint2/USBD30/`

- **`USBD30.sql`** - List of all facilities that have grain silos, but no warehouses.
- **`USBD30Test.sql`** - Test script for User Story 30.

### Sprint 3

Additional database scripts for Sprint 3 features are located in `Sprint3/` folder.

---

## Folder Structure

```
sql-scripts/
├── InitialSchema/
│   ├── schema.sql              # Database schema definition
│   └── initial_data.sql        # Initial seed data
├── Sprint1/
│   ├── USBD08/
│   │   └── USBD8.sql
│   └── USBD13/
│       └── USBD13.sql
├── Sprint2/
│   ├── USBD15/
│   │   └── USBD15.sql
│   ├── USBD19/
│   │   └── USBD19.sql
│   ├── USBD24/
│   │   ├── USBD24.sql
│   │   └── USBD24Test.sql
│   ├── USBD25/
│   │   ├── USBD25.sql
│   │   └── USBD25Test.sql
│   ├── USBD28/
│   │   ├── USBD28.sql
│   │   └── USBD28Test.sql
│   └── USBD30/
│       ├── USBD30.sql
│       └── USBD30Test.sql
└── Sprint3/
    └── [Sprint 3 scripts]
```

---

## Running Scripts

### Using SQL*Plus

```bash
# Connect to database
sqlplus System@vsgate-s1.dei.isep.ipp.pt:10715/XE

# Run schema script
@src/main/sql-scripts/InitialSchema/schema.sql

# Run initial data script
@src/main/sql-scripts/InitialSchema/initial_data.sql

# Run user story script
@src/main/sql-scripts/Sprint2/USBD24/USBD24.sql
```

### Using SQL Developer

1. Create connection with provided credentials
2. Open script file
3. Execute script (F5)

### Using JDBC in Java

Scripts can be executed programmatically using JDBC connections.

---

## Script Execution Order

**Important:** Scripts must be executed in the correct order:

1. **First**: `InitialSchema/schema.sql` - Creates database structure
2. **Second**: `InitialSchema/initial_data.sql` - Populates initial data
3. **Then**: User story scripts can be run in any order

---

## Testing Scripts

Test scripts (e.g., `USBD24Test.sql`) are provided for validation:
- Test data setup
- Query execution
- Result verification
- Cleanup

---

## Related Documentation

- [Main README](../../README.md)
- [Database Schema Documentation](../../doc/global-artifacts/Relational%20Model/)
- [Java Source Code](../java/Readme.md)

---

## Notes

- All scripts use Oracle SQL syntax
- Scripts include proper error handling where applicable
- Test scripts should be run after main scripts
- Some scripts use substitution variables (e.g., `&p_type`)
