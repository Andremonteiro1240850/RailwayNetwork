# Train Station Domain.Warehouse — Sample Dataset (Sprint 1)

This dataset supports USEI01–USEI05 and optional Reorder.

## Files & Schemas
- bays.csv: warehouseId, aisle, bay, capacityBoxes
- wagons.csv: wagonId, boxId, sku, qty, expiryDate, receivedAt
- items.csv: sku, name, category, unit, volume, unitWeight
- orders.csv: oorderId, dueDate, priority
- order_lines.csv: orderId, lineNo, sku, qty
- returns.csv: returnId, sku, qty, reason, timestamp, expiryDate
- facility.csv: facilityId, name
- line.csv; lineId, name, owner, startFacilityID, startFacilityName, endFacilityID, endFacilityName, gauge
