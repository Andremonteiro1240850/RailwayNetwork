# Train Station Dataset

This directory contains sample datasets used throughout the project for testing and development.

## Overview

The datasets support various user stories and features:
- **USEI01-USEI05**: Warehouse management
- **USEI06-USEI10**: Station management and geographical search
- **USLP07**: Freight management

---

## Files & Schemas

### Station Data

#### `train_stations_europe.csv`
European train stations dataset with geographical coordinates.

**Schema:**
- `name`: Station name
- `latitude`: Latitude coordinate (decimal degrees)
- `longitude`: Longitude coordinate (decimal degrees)
- `country`: Country code (e.g., PT, ES)
- `timeZone`: Timezone identifier
- `timeZoneGroup`: Timezone group
- `isCity`: Boolean flag (city status)
- `isMainStation`: Boolean flag (main station status)
- `isAirport`: Boolean flag (airport connection)

**Usage:**
- USEI06: AVL tree construction
- USEI07: KD-tree construction
- USEI08: Geographical area search
- USEI09: Proximity search
- USEI10: Radius search

---

### Warehouse Data

#### `bays.csv`
Warehouse bay information.

**Schema:**
- `warehouseId`: Warehouse identifier
- `aisle`: Aisle number
- `bay`: Bay number
- `capacityBoxes`: Maximum box capacity

**Usage:** USEI01-USEI05 (Warehouse management)

#### `items.csv`
Item catalog.

**Schema:**
- `sku`: Stock Keeping Unit identifier
- `name`: Item name
- `category`: Item category
- `unit`: Unit of measurement
- `volume`: Volume per unit
- `unitWeight`: Weight per unit

**Usage:** USEI01-USEI05 (Warehouse management)

#### `wagons.csv`
Wagon inventory data.

**Schema:**
- `wagonId`: Wagon identifier
- `boxId`: Box identifier
- `sku`: Stock Keeping Unit
- `qty`: Quantity
- `expiryDate`: Expiration date
- `receivedAt`: Receipt timestamp

**Usage:** USEI01-USEI05 (Warehouse management)

---

### Order Management

#### `orders.csv`
Order information.

**Schema:**
- `orderId`: Order identifier
- `dueDate`: Due date
- `priority`: Priority level

**Usage:** USEI01-USEI05 (Warehouse management)

#### `order_lines.csv`
Order line items.

**Schema:**
- `orderId`: Order identifier (foreign key)
- `lineNo`: Line number
- `sku`: Stock Keeping Unit
- `qty`: Quantity ordered

**Usage:** USEI01-USEI05 (Warehouse management)

#### `returns.csv`
Return information.

**Schema:**
- `returnId`: Return identifier
- `sku`: Stock Keeping Unit
- `qty`: Returned quantity
- `reason`: Return reason
- `timestamp`: Return timestamp
- `expiryDate`: Expiration date

**Usage:** USEI01-USEI05 (Warehouse management)

---

### Infrastructure Data

#### `facility.csv`
Facility information.

**Schema:**
- `facilityId`: Facility identifier
- `name`: Facility name

**Usage:** Infrastructure and route management

#### `line.csv`
Railway line information.

**Schema:**
- `lineId`: Line identifier
- `name`: Line name
- `owner`: Line owner
- `startFacilityID`: Starting facility ID
- `startFacilityName`: Starting facility name
- `endFacilityID`: Ending facility ID
- `endFacilityName`: Ending facility name
- `gauge`: Track gauge

**Usage:** Infrastructure and route management

#### `locomotives.csv`
Locomotive information.

**Schema:**
- (Schema details depend on implementation)

**Usage:** Freight management (USLP07)

---

## Data Format

- **File format**: CSV (Comma-Separated Values)
- **Encoding**: UTF-8
- **Header row**: First row contains column names
- **Delimiter**: Comma (`,`)

---

## Usage Notes

1. **Data Loading**: Datasets are loaded through repository classes
2. **Validation**: Data validation is performed during loading
3. **Coordinate Validation**: Station coordinates must be valid (latitude: -90 to 90, longitude: -180 to 180)
4. **Required Fields**: Station names must be non-empty

---

## Related Documentation

- **USEI01-USEI05**: Warehouse management documentation
- **USEI06-USEI10**: Station and geographical search documentation
- **Database Schema**: See `src/main/sql-scripts/schema.sql`

---

## Notes

- This is sample data for development and testing
- Real production data should be loaded through proper database procedures
- Some datasets may be optional depending on which features are used
