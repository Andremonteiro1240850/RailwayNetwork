## USEI10 – Complexity Analysis

This document explains the temporal and spatial complexity of the primary operations involved in USEI10 (Radius Search and Density Summary).

### 1. KD-tree Radius Search (`KDTree.radiusSearch`)

- **Tree traversal with spatial pruning**: `O(√n + k)` average case, `O(n)` worst case  
  - `n` – total stations in the KD-tree  
  - `k` – stations found within the radius  
  - Average case assumes a balanced KD-tree and moderate radius, allowing pruning of most branches. Worst case occurs when the radius is large enough to visit every node.
- **Haversine distance calculations**: `O(k)`  
  - Each candidate station inside the radius requires a constant-time Haversine computation.
- **Final sorting of results**: `O(k log k)`  
  - The raw list is sorted by distance (ASC) to comply with USEI10 output requirements.
- **Space complexity**: `O(k + log n)`  
  - `O(k)` for the result list, `O(log n)` for recursion depth.
- **Overall**: `O(√n + k log k)` average, `O(n + k log k)` worst case.

### 2. AVL Result Tree Construction (`RadiusSearchController.searchRadiusWithSummary`)

- **AVL insertions**: `O(k log k)`  
  - The `k` stations returned by the KD-tree are inserted using `StationDistanceComparator` (distance ASC, name DESC). Each insertion is `O(log k)` because the AVL tree remains balanced.
- **Overall controller step**: dominated by KD-tree search (`O(√n + k log k)` average).

### 3. Density Summary (`RadiusSearchController.calculateSummary`)

- **Aggregation by country / isCity**: `O(k)`  
  - Single pass through the `k` stations; each update to the map or counters is `O(1)`.

### 4. End-to-end Complexity per Query

- **Average case**: `O(√n + k log k)`  
  - KD-tree traversal plus AVL insertions dominate.
- **Worst case**: `O(n + k log k)`  
  - Happens when the search radius covers almost all stations or the KD-tree is highly unbalanced.

These bounds assume the KD-tree is already built (via USEI07) and that the controller only performs query-time work. No additional CSV reads occur during USEI10.

