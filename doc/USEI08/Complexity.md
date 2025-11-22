# USEI08 - Complexity Analysis (Worst Case) - Optimized

## Constructor Analysis

```java
public GeographicalSearchController(KDTreeRepository kdTreeRepository, StationRepository stationRepository)
```

**Time Complexity Analysis (Worst Case):**

```text
KDTree tree = kdTreeRepository.getCachedTree();
```
- Complexity: O(1)
- Explanation: Getting cached tree from repository is a constant time operation.

```text
this.allStations = stationRepository.getAllStations();
```
- Complexity: O(n)
- Explanation: Creating a new ArrayList and copying all n stations takes O(n) time.

**Overall Time Complexity: O(n)**
- Explanation: The overall time complexity is dominated by copying the station list, which takes O(n) time.

---

## searchByGeographicalArea (with filters) - OPTIMIZED

```java
public List<Station> searchByGeographicalArea(double latMin, double latMax, 
                                               double lonMin, double lonMax,
                                               SearchFilters filters) {
    // Validation: O(1)
    if (filters != null && filters.hasFilters()) {
        List<Station> filteredResults = new ArrayList<>(estimatedCapacity);
        List<Station> stationsInRange = kdTree.rangeQueryUnsorted(latMin, latMax, lonMin, lonMax);
        for (Station station : stationsInRange) {
            if (filters.matches(station)) {
                filteredResults.add(station);
            }
        }
        filteredResults.sort(Comparator.comparing(Station::getName));
        return filteredResults;
    }
    return kdTree.rangeQuery(latMin, latMax, lonMin, lonMax);
}
```

**Time Complexity Analysis (Worst Case):**

```text
Validation checks
```
- Complexity: O(1)
- Explanation: Comparing numeric values and creating exception messages are constant time operations.

```text
kdTree.rangeQueryUnsorted(latMin, latMax, lonMin, lonMax)
```
- Complexity: O(√n + k)
- Explanation: 
  - Tree traversal: In the worst case, a KD-tree range query visits O(√n) nodes due to spatial pruning (where n is total stations).
  - Station processing: Processes all stations in visited nodes. In worst case, all k stations in range are processed.
  - No sorting: This method does NOT sort results (optimization for filter-before-sort).
  - Total: O(√n + k), where n is total stations and k is number of results in range.

```text
Filtering loop (optimized)
```
- Complexity: O(k)
- Explanation: 
  - Single pass through k results with pre-allocated ArrayList
  - Each filters.matches() call is O(1) (constant time comparisons)
  - Pre-allocation reduces ArrayList reallocations
  - Total: O(k) where k is number of results in range

```text
filteredResults.sort(Comparator.comparing(Station::getName))
```
- Complexity: O(m log m)
- Explanation: 
  - Sorts only the m filtered results (m ≤ k)
  - Standard comparison-based sort takes O(m log m) time
  - Total: O(m log m) where m is number of filtered results

**Overall Time Complexity: O(√n + k + m log m) where m ≤ k**
- Explanation: 
  - The range query operation collects results without sorting: O(√n + k) where k is number of results in range
  - Filtering: O(k) to filter k results
  - Sorting: O(m log m) where m is the number of filtered results (m ≤ k)
  - **OPTIMIZATION**: By filtering before sorting, we sort only m results instead of k results
  - In the worst case: O(√n + k + m log m) where n is total stations, k is results in range, m is filtered results
  - When m << k, this is significantly better than O(√n + k log k) (the original approach)
  - Note: The k term is necessary because we must process all k stations in range to filter them

**Optimizations Applied:**
1. Pre-allocated ArrayList with estimated capacity (reduces reallocations)
2. Single-pass filtering loop (avoids intermediate list creation)
3. Direct loop instead of stream (reduces overhead)
4. **Filter-before-sort optimization**: Use rangeQueryUnsorted to filter first, then sort only filtered results (m log m instead of k log k)

---

## searchByGeographicalArea (without filters)

```java
public List<Station> searchByGeographicalArea(double latMin, double latMax, 
                                               double lonMin, double lonMax) {
    return searchByGeographicalArea(latMin, latMax, lonMin, lonMax, null);
}
```

**Time Complexity Analysis (Worst Case):**

```text
return searchByGeographicalArea(latMin, latMax, lonMin, lonMax, null);
```
- Complexity: O(√n + k log k)
- Explanation: Direct range query without filtering. The KD-tree range query with spatial pruning takes O(√n + k log k) time.

**Overall Time Complexity: O(√n + k log k)**
- Explanation: No change from original - this is already optimal for unfiltered queries.

---

## findNearestStations (with filters) - OPTIMIZED

```java
public List<KDTree.StationDistance> findNearestStations(double targetLat, double targetLon, 
                                                           int n, SearchFilters filters) {
    // Validation: O(1)
    List<KDTree.StationDistance> nearestStations = kdTree.nearestN(targetLat, targetLon, n);
    
    if (filters != null && filters.hasFilters()) {
        List<KDTree.StationDistance> filteredResults = new ArrayList<>(nearestStations.size());
        for (KDTree.StationDistance sd : nearestStations) {
            if (filters.matches(sd.getStation())) {
                filteredResults.add(sd);
            }
        }
        return filteredResults;
    }
    return nearestStations;
}
```

**Time Complexity Analysis (Worst Case):**

```text
Validation checks
```
- Complexity: O(1)
- Explanation: Comparing numeric values and creating exception messages are constant time operations.

```text
kdTree.nearestN(targetLat, targetLon, n)
```
- Complexity: O(n log k + k log k) worst case, where n is total stations and k is the parameter n
- Explanation:
  - Tree traversal: With branch-and-bound pruning, visits O(n) nodes in worst case where n is total stations
  - Station processing: Processes all stations in visited nodes. In worst case, processes all n stations.
  - Priority queue operations: For each station processed, performs O(log k) operations where k is the parameter (max queue size). In worst case, processes n stations, each with O(log k) operations.
  - Result sorting: Sorting k results by distance takes O(k log k) time
  - Total worst case: O(n log k + k log k), where n is total stations and k is the parameter n
  - Note: In average case with balanced tree and good pruning, traversal is O(√n), giving O(√n + n log k + k log k), but worst case is O(n log k + k log k)

```text
Filtering loop (optimized)
```
- Complexity: O(m) where m = nearestStations.size() ≤ k
- Explanation:
  - Pre-allocated ArrayList with exact size (no reallocations)
  - Single pass through m results
  - Each filters.matches() call is O(1)
  - Direct loop avoids stream pipeline overhead
  - Total: O(m) where m ≤ k

**Overall Time Complexity: O(n log k + k log k + m) worst case, where n is total stations, k is parameter, m is filtered results**
- Explanation:
  - The nearestN operation is O(n log k + k log k) in worst case
  - Filtering adds O(m) where m ≤ k, which is dominated by the O(k log k) sorting when m ≈ k
  - In worst case: O(n log k + k log k) where n is total stations and k is the parameter n
  - Note: The m term is O(k) at most, so it doesn't change the asymptotic complexity

**Optimizations Applied:**
1. Pre-allocated ArrayList with exact size (eliminates reallocations)
2. Direct loop instead of stream (reduces overhead from stream pipeline)
3. Single-pass filtering (avoids intermediate operations)

---

## findNearestStations (without filters)

```java
public List<KDTree.StationDistance> findNearestStations(double targetLat, double targetLon, int n) {
    return findNearestStations(targetLat, targetLon, n, null);
}
```

**Time Complexity Analysis (Worst Case):**

```text
return findNearestStations(targetLat, targetLon, n, null);
```
- Complexity: O(n log k + k log k) worst case, where n is total stations and k is the parameter n
- Explanation: Direct nearest neighbor search without filtering. Same complexity as nearestN method.

**Overall Time Complexity: O(n log k + k log k) worst case, where n is total stations and k is the parameter n**
- Explanation: No change from original - this is already optimal.

---

## findStationsInRadius (with filters) - OPTIMIZED

```java
public List<KDTree.StationDistance> findStationsInRadius(double targetLat, double targetLon, 
                                                              double radiusKm, SearchFilters filters) {
    // Validation: O(1)
    if (filters != null && filters.hasFilters()) {
        // Get stations in radius WITHOUT sorting first (optimization)
        // Filter first, then sort only the filtered results
        List<KDTree.StationDistance> stationsInRadius = kdTree.radiusSearchUnsorted(targetLat, targetLon, radiusKm);
        
        List<KDTree.StationDistance> filteredResults = new ArrayList<>(estimatedCapacity);
        for (KDTree.StationDistance sd : stationsInRadius) {
            if (filters.matches(sd.getStation())) {
                filteredResults.add(sd);
            }
        }
        filteredResults.sort(Comparator.comparing(KDTree.StationDistance::getDistance));
        return filteredResults;
    }
    return kdTree.radiusSearch(targetLat, targetLon, radiusKm);
}
```

**Time Complexity Analysis (Worst Case):**

```text
Validation checks
```
- Complexity: O(1)
- Explanation: Comparing numeric values and creating exception messages are constant time operations.

```text
kdTree.radiusSearchUnsorted(targetLat, targetLon, radiusKm)
```
- Complexity: O(n + k) worst case, where n is total stations and k is number of results
- Explanation:
  - Tree traversal: With spatial pruning, visits O(√n) nodes in average case, but O(n) nodes in worst case (when radius is very large)
  - Station processing: Processes all stations in visited nodes. In worst case (very large radius), processes all n stations, each with O(1) distance calculation.
  - Result collection: Collects k results within radius
  - No sorting: This method does NOT sort results (optimization for filter-before-sort)
  - Total worst case: O(n + k), where n is total stations and k is number of results within radius
  - Note: In average case with balanced tree and reasonable radius, traversal is O(√n), giving O(√n + k)

```text
Filtering loop (optimized)
```
- Complexity: O(k)
- Explanation:
  - Pre-allocated ArrayList with estimated capacity (reduces reallocations)
  - Single pass through k results
  - Each filters.matches() call is O(1)
  - Direct loop avoids stream pipeline overhead
  - Total: O(k) where k is number of results in radius

**Overall Time Complexity: O(n + k + m log m) worst case, where n is total stations, k is results, m ≤ k is filtered results**
- Explanation:
  - The radius search operation collects results without sorting: O(n + k) in worst case (processes all n stations, collects k results)
  - Filtering: O(k) to filter k results
  - Sorting: O(m log m) where m is the number of filtered results (m ≤ k)
  - **OPTIMIZATION**: By filtering before sorting, we sort only m results instead of k results
  - In worst case: O(n + k + m log m) where n is total stations, k is results in radius, m is filtered results
  - When m << k, this is significantly better than O(n + k log k) (the original approach)
  - Note: The k term is necessary because we must process all k stations in radius to filter them

**Optimizations Applied:**
1. Pre-allocated ArrayList with estimated capacity (reduces reallocations)
2. Direct loop instead of stream (reduces overhead from stream pipeline)
3. Single-pass filtering (avoids intermediate operations)
4. **Filter-before-sort optimization**: Use radiusSearchUnsorted to filter first, then sort only filtered results (m log m instead of k log k)

---

## findStationsInRadius (without filters)

```java
public List<KDTree.StationDistance> findStationsInRadius(double targetLat, double targetLon, double radiusKm) {
    return findStationsInRadius(targetLat, targetLon, radiusKm, null);
}
```

**Time Complexity Analysis (Worst Case):**

```text
return findStationsInRadius(targetLat, targetLon, radiusKm, null);
```
- Complexity: O(n + k log k) worst case, where n is total stations and k is number of results
- Explanation: Direct radius search without filtering. Same complexity as radiusSearch method.

**Overall Time Complexity: O(n + k log k) worst case, where n is total stations and k is number of results**
- Explanation: No change from original - this is already optimal.

---

## Summary of Optimizations

### Key Improvements:

1. **Pre-allocation of ArrayLists**: 
   - Reduces reallocations from O(k log k) to O(k) in worst case
   - Uses estimated capacity based on typical result sizes

2. **Direct loops instead of streams**:
   - Eliminates stream pipeline overhead
   - Reduces memory allocations
   - Improves cache locality

3. **Single-pass filtering**:
   - Avoids creating intermediate lists
   - Reduces memory footprint
   - Note: Filtering complexity O(k) is dominated by sorting O(k log k), so overall complexity remains the same

### Complexity Improvements:

| Method | Worst Case Complexity | Notes |
|--------|----------------------|-------|
| `searchByGeographicalArea` (with filters) | O(√n + k + m log m) | n = total stations, k = results in range, m ≤ k = filtered results. Optimized: filter before sort. |
| `findNearestStations` (with filters) | O(n log k + k log k + m) | n = total stations, k = parameter n, m ≤ k = filtered results. Cannot optimize further. |
| `findStationsInRadius` (with filters) | O(n + k + m log m) | n = total stations, k = results in radius, m ≤ k = filtered results. Optimized: filter before sort. |

### Notes:

- The optimizations improve both **constant factors** and **asymptotic complexity** when filters are applied
- **Key optimization**: Filter-before-sort reduces sorting complexity from O(k log k) to O(m log m) where m ≤ k
- Pre-allocation reduces ArrayList reallocations but does not change asymptotic complexity
- Direct loops provide better performance than streams for simple filtering operations
- **Important**: When m << k (few results pass filters), the filter-before-sort optimization provides significant improvement
- **Notation**: 
  - n = total number of stations in the tree
  - k = number of results returned (for rangeQuery/radiusSearch) or parameter n (for nearestN)
  - m = number of filtered results (≤ k)
