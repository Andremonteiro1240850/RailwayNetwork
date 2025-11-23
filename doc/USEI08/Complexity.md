# USEI08 - Complexity Analysis (Worst Case)

## Notation and Terminology

To avoid ambiguity, we use the following notation throughout this document:
- **N**: Total number of stations in the KD-tree
- **k**: Number of results returned by a query (for rangeQuery/radiusSearch) or the parameter `n` (for nearestN)
- **m**: Number of filtered results (m ≤ k)
- **s**: Average length of station names (for string comparison analysis)
- **h**: Height of the KD-tree (h = O(log N) for balanced tree, h = O(N) worst case)

---

## Constructor Analysis

```java
public GeographicalSearchController(KDTreeRepository kdTreeRepository, StationRepository stationRepository)
```

### Time Complexity Analysis (Worst Case)

```text
KDTree tree = kdTreeRepository.getCachedTree();
```
- **Complexity**: O(1)
- **Explanation**: Getting cached tree from repository is a constant time operation (simple reference retrieval).

```text
this.allStations = stationRepository.getAllStations();
```
- **Complexity**: O(N)
- **Explanation**: Creating a new ArrayList and copying all N stations takes O(N) time. Each station object is copied by reference, but the list structure itself requires O(N) operations.

**Overall Time Complexity: O(N)**
- **Explanation**: The overall time complexity is dominated by copying the station list, which takes O(N) time. The tree retrieval is O(1) and does not affect the asymptotic complexity.

### Space Complexity Analysis (Worst Case)

```text
KDTree tree = kdTreeRepository.getCachedTree();
```
- **Complexity**: O(1)
- **Explanation**: Only stores a reference to the existing tree (no additional space allocated).

```text
this.allStations = stationRepository.getAllStations();
```
- **Complexity**: O(N)
- **Explanation**: Creates a new ArrayList containing references to all N stations. Each reference is O(1), so total space is O(N).

**Overall Space Complexity: O(N)**
- **Explanation**: The dominant space usage is the ArrayList storing references to all N stations. The tree reference is O(1) and does not affect asymptotic space complexity.

---

## searchByGeographicalArea (with filters)

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

### Time Complexity Analysis (Worst Case)

```text
Validation checks
```
- **Complexity**: O(1)
- **Explanation**: Comparing numeric values (latMin, latMax, lonMin, lonMax) and creating exception messages are constant time operations.

```text
kdTree.rangeQueryUnsorted(latMin, latMax, lonMin, lonMax)
```
- **Complexity**: O(N + k) worst case
- **Explanation**: 
  - **Tree traversal**: In the worst case, when the query rectangle covers the entire dataset or a large portion of it, the KD-tree range query may visit O(N) nodes. The O(√N) bound applies to average case with balanced tree and small queries, but worst case can degenerate to O(N) when spatial pruning is ineffective (e.g., query covers entire tree).
  - **Station processing**: For each visited node, all stations in the bucket are checked. In worst case, all N stations are processed (one per node in degenerate tree, or all stations in buckets of visited nodes).
  - **Range checking**: Each station requires O(1) coordinate comparisons to check if it's within the range.
  - **No sorting**: This method does NOT sort results.
  - **Total worst case**: O(N + k), where N is total stations and k is number of results in range.
  - **Note**: In average case with balanced tree and typical queries, traversal is O(√N), giving O(√N + k).

```text
Filtering loop
```
- **Complexity**: O(k)
- **Explanation**: 
  - Single pass through k results with pre-allocated ArrayList (reduces reallocations from O(k log k) to O(k) amortized).
  - Each `filters.matches(station)` call is O(1): it performs at most 3 constant-time comparisons (country enum comparison, boolean comparisons for isCity and isMainStation). The `Country.fromString()` operation is O(1) for short country codes (2 characters).
  - Pre-allocation reduces ArrayList reallocations but does not change asymptotic complexity.
  - **Total**: O(k) where k is number of results in range.

```text
filteredResults.sort(Comparator.comparing(Station::getName))
```
- **Complexity**: O(m log m · s) worst case
- **Explanation**: 
  - Sorts only the m filtered results (m ≤ k).
  - Standard comparison-based sort (Timsort in Java) takes O(m log m) comparisons.
  - **String comparison cost**: Each comparison of station names using `Comparator.comparing(Station::getName)` requires O(s) time where s is the average length of station names. In worst case, all comparisons may need to compare full strings.
  - **Total**: O(m log m · s) where m is number of filtered results and s is average name length.
  - **Note**: For practical purposes with short station names (s is bounded by a constant), this simplifies to O(m log m), but technically the string comparison factor should be acknowledged.

**Overall Time Complexity: O(N + k + m log m · s) worst case, where m ≤ k**
- **Explanation**: 
  - The range query operation collects results without sorting: O(N + k) worst case where k is number of results in range.
  - Filtering: O(k) to filter k results.
  - Sorting: O(m log m · s) where m is the number of filtered results (m ≤ k) and s is average name length.
  - By filtering before sorting, we sort only m results instead of k results, reducing from O(k log k · s) to O(m log m · s).
  - In worst case: O(N + k + m log m · s) where N is total stations, k is results in range, m is filtered results.
  - When m << k, this is significantly better than O(N + k log k · s) (the original approach).
  - **Note**: If we assume s is bounded by a constant (reasonable for station names), this simplifies to O(N + k + m log m).

### Space Complexity Analysis (Worst Case)

```text
List<Station> filteredResults = new ArrayList<>(estimatedCapacity);
```
- **Complexity**: O(m)
- **Explanation**: Stores references to m filtered stations. Each reference is O(1), so total is O(m).

```text
List<Station> stationsInRange = kdTree.rangeQueryUnsorted(...)
```
- **Complexity**: O(k)
- **Explanation**: The rangeQueryUnsorted method creates an ArrayList containing k station references.

```text
Recursion stack (rangeQueryRecursive)
```
- **Complexity**: O(h) = O(N) worst case
- **Explanation**: The recursive range query uses a call stack. In worst case (degenerate tree), height h = O(N), requiring O(N) stack frames. Each frame stores O(1) data (parameters and local variables).

```text
Sorting auxiliary space
```
- **Complexity**: O(m)
- **Explanation**: Timsort requires O(m) auxiliary space in worst case for sorting m elements.

**Overall Space Complexity: O(N + k + m) worst case**
- **Explanation**: 
  - Recursion stack: O(N) in worst case (degenerate tree).
  - Results list: O(k) for stationsInRange.
  - Filtered results: O(m) for filteredResults.
  - Sorting auxiliary: O(m) for Timsort.
  - **Total**: O(N + k + m). Since m ≤ k, this simplifies to O(N + k) in terms of input size.


---

## searchByGeographicalArea (without filters)

```java
public List<Station> searchByGeographicalArea(double latMin, double latMax, 
                                               double lonMin, double lonMax) {
    return searchByGeographicalArea(latMin, latMax, lonMin, lonMax, null);
}
```

### Time Complexity Analysis (Worst Case)

```text
return searchByGeographicalArea(latMin, latMax, lonMin, lonMax, null);
```
- **Complexity**: O(N + k log k · s) worst case
- **Explanation**: Direct range query without filtering. The KD-tree range query with spatial pruning takes O(N + k) worst case for traversal and collection, plus O(k log k · s) for sorting by name (where s is average name length).

**Overall Time Complexity: O(N + k log k · s) worst case**
- **Explanation**: 
  - Range query: O(N + k) worst case (traversal + collection).
  - Sorting: O(k log k · s) for sorting k results by name.
  - **Total**: O(N + k log k · s). If s is bounded by a constant, this simplifies to O(N + k log k).
  - **Note**: This is already optimal for unfiltered queries (all results must be sorted).

### Space Complexity Analysis (Worst Case)

- **Complexity**: O(N + k) worst case
- **Explanation**: 
  - Recursion stack: O(N) in worst case (degenerate tree).
  - Results list: O(k) for the sorted result list.
  - Sorting auxiliary: O(k) for Timsort.
  - **Total**: O(N + k).

---

## findNearestStations (with filters)

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

### Time Complexity Analysis (Worst Case)

```text
Validation checks
```
- **Complexity**: O(1)
- **Explanation**: Comparing numeric values and creating exception messages are constant time operations.

```text
kdTree.nearestN(targetLat, targetLon, n)
```
- **Complexity**: O(N log k + k log k) worst case, where N is total stations and k is the parameter n
- **Explanation**:
  - **Tree traversal**: With branch-and-bound pruning, in worst case (when pruning is ineffective, e.g., all stations are equidistant or tree is degenerate), the algorithm may visit O(N) nodes where N is total stations. The average case with balanced tree and good pruning is O(√N), but worst case can be O(N).
  - **Station processing**: For each visited node, all stations in the bucket are processed. In worst case, processes all N stations.
  - **Haversine distance calculation**: Each distance calculation is O(1) (constant-time floating-point operations).
  - **Priority queue operations**: For each station processed, performs heap operations (offer/poll) on a max-heap of size at most k. Each operation is O(log k). In worst case, processes N stations, each with O(log k) operations, giving O(N log k).
  - **Result sorting**: After traversal, sorting k results by distance takes O(k log k) time. Note: comparing doubles is O(1), so no string comparison factor.
  - **Total worst case**: O(N log k + k log k), where N is total stations and k is the parameter n.
  - **Note**: In average case with balanced tree and good pruning, traversal is O(√N), giving O(√N log k + k log k), but worst case is O(N log k + k log k).

```text
Filtering loop
```
- **Complexity**: O(m) where m = nearestStations.size() ≤ k
- **Explanation**:
  - Pre-allocated ArrayList with exact size (no reallocations).
  - Single pass through m results (where m ≤ k).
  - Each `filters.matches()` call is O(1) (constant-time comparisons).
  - Direct loop avoids stream pipeline overhead.
  - **Total**: O(m) where m ≤ k.

**Overall Time Complexity: O(N log k + k log k + m) worst case, where N is total stations, k is parameter n, m is filtered results**
- **Explanation**:
  - The nearestN operation is O(N log k + k log k) in worst case.
  - Filtering adds O(m) where m ≤ k, which is dominated by the O(k log k) sorting when m ≈ k.
  - In worst case: O(N log k + k log k) where N is total stations and k is the parameter n.
  - **Note**: The m term is O(k) at most, so it doesn't change the asymptotic complexity: O(N log k + k log k).

### Space Complexity Analysis (Worst Case)

```text
PriorityQueue<StationDistance> nearestQueue (inside nearestN)
```
- **Complexity**: O(k)
- **Explanation**: Max-heap storing at most k StationDistance objects. Each object contains a Station reference and a double, so O(k) space.

```text
List<KDTree.StationDistance> nearestStations
```
- **Complexity**: O(k)
- **Explanation**: List containing k StationDistance objects.

```text
List<KDTree.StationDistance> filteredResults
```
- **Complexity**: O(m) where m ≤ k
- **Explanation**: List containing m filtered StationDistance objects.

```text
Recursion stack (nearestNRecursive)
```
- **Complexity**: O(h) = O(N) worst case
- **Explanation**: The recursive nearest neighbor search uses a call stack. In worst case (degenerate tree), height h = O(N), requiring O(N) stack frames.

```text
Sorting auxiliary space
```
- **Complexity**: O(k)
- **Explanation**: Timsort requires O(k) auxiliary space for sorting k StationDistance objects.

**Overall Space Complexity: O(N + k) worst case**
- **Explanation**: 
  - Recursion stack: O(N) in worst case.
  - Priority queue: O(k).
  - Results lists: O(k) for nearestStations, O(m) ≤ O(k) for filteredResults.
  - Sorting auxiliary: O(k).
  - **Total**: O(N + k).


---

## findNearestStations (without filters)

```java
public List<KDTree.StationDistance> findNearestStations(double targetLat, double targetLon, int n) {
    return findNearestStations(targetLat, targetLon, n, null);
}
```

### Time Complexity Analysis (Worst Case)

```text
return findNearestStations(targetLat, targetLon, n, null);
```
- **Complexity**: O(N log k + k log k) worst case, where N is total stations and k is the parameter n
- **Explanation**: Direct nearest neighbor search without filtering. Same complexity as nearestN method: O(N log k) for traversal and priority queue operations, plus O(k log k) for sorting.

**Overall Time Complexity: O(N log k + k log k) worst case, where N is total stations and k is the parameter n**
- **Explanation**: No change from original - this is already optimal.

### Space Complexity Analysis (Worst Case)

- **Complexity**: O(N + k) worst case
- **Explanation**: Same as findNearestStations with filters: O(N) for recursion stack, O(k) for priority queue and results.

---

## findStationsInRadius (with filters)

```java
public List<KDTree.StationDistance> findStationsInRadius(double targetLat, double targetLon, 
                                                              double radiusKm, SearchFilters filters) {
    // Validation: O(1)
    if (filters != null && filters.hasFilters()) {
        // Get stations in radius WITHOUT sorting first
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

### Time Complexity Analysis (Worst Case)

```text
Validation checks
```
- **Complexity**: O(1)
- **Explanation**: Comparing numeric values and creating exception messages are constant time operations.

```text
kdTree.radiusSearchUnsorted(targetLat, targetLon, radiusKm)
```
- **Complexity**: O(N + k) worst case, where N is total stations and k is number of results
- **Explanation**:
  - **Tree traversal**: With spatial pruning, visits O(√N) nodes in average case, but O(N) nodes in worst case (when radius is very large, covering the entire tree, or when tree is degenerate). The worst case occurs when the search radius is large enough that spatial pruning cannot eliminate any branches.
  - **Station processing**: Processes all stations in visited nodes. In worst case (very large radius), processes all N stations, each with O(1) Haversine distance calculation.
  - **Result collection**: Collects k results within radius. Each StationDistance object creation is O(1).
  - **No sorting**: This method does NOT sort results.
  - **Total worst case**: O(N + k), where N is total stations and k is number of results within radius.
  - **Note**: In average case with balanced tree and reasonable radius, traversal is O(√N), giving O(√N + k).

```text
Filtering loop
```
- **Complexity**: O(k)
- **Explanation**:
  - Pre-allocated ArrayList with estimated capacity (reduces reallocations).
  - Single pass through k results.
  - Each `filters.matches()` call is O(1) (constant-time comparisons).
  - Direct loop avoids stream pipeline overhead.
  - **Total**: O(k) where k is number of results in radius.

```text
filteredResults.sort(Comparator.comparing(KDTree.StationDistance::getDistance))
```
- **Complexity**: O(m log m) worst case
- **Explanation**: 
  - Sorts only the m filtered results (m ≤ k).
  - Standard comparison-based sort (Timsort) takes O(m log m) comparisons.
  - **Double comparison cost**: Comparing distances (doubles) is O(1), so no additional factor needed.
  - **Total**: O(m log m) where m is number of filtered results.

**Overall Time Complexity: O(N + k + m log m) worst case, where N is total stations, k is results, m ≤ k is filtered results**
- **Explanation**:
  - The radius search operation collects results without sorting: O(N + k) in worst case (processes all N stations, collects k results).
  - Filtering: O(k) to filter k results.
  - Sorting: O(m log m) where m is the number of filtered results (m ≤ k).
  - By filtering before sorting, we sort only m results instead of k results, reducing from O(k log k) to O(m log m).
  - In worst case: O(N + k + m log m) where N is total stations, k is results in radius, m is filtered results.
  - When m << k, this is significantly better than O(N + k log k) (the original approach).
  - **Note**: The k term is necessary because we must process all k stations in radius to filter them.

### Space Complexity Analysis (Worst Case)

```text
List<KDTree.StationDistance> stationsInRadius
```
- **Complexity**: O(k)
- **Explanation**: List containing k StationDistance objects.

```text
List<KDTree.StationDistance> filteredResults
```
- **Complexity**: O(m) where m ≤ k
- **Explanation**: List containing m filtered StationDistance objects.

```text
Recursion stack (radiusSearchRecursive)
```
- **Complexity**: O(h) = O(N) worst case
- **Explanation**: The recursive radius search uses a call stack. In worst case (degenerate tree), height h = O(N), requiring O(N) stack frames.

```text
Sorting auxiliary space
```
- **Complexity**: O(m)
- **Explanation**: Timsort requires O(m) auxiliary space for sorting m StationDistance objects.

**Overall Space Complexity: O(N + k + m) worst case**
- **Explanation**: 
  - Recursion stack: O(N) in worst case.
  - Results lists: O(k) for stationsInRadius, O(m) for filteredResults.
  - Sorting auxiliary: O(m).
  - **Total**: O(N + k + m). Since m ≤ k, this simplifies to O(N + k).


---

## findStationsInRadius (without filters)

```java
public List<KDTree.StationDistance> findStationsInRadius(double targetLat, double targetLon, double radiusKm) {
    return findStationsInRadius(targetLat, targetLon, radiusKm, null);
}
```

### Time Complexity Analysis (Worst Case)

```text
return findStationsInRadius(targetLat, targetLon, radiusKm, null);
```
- **Complexity**: O(N + k log k) worst case, where N is total stations and k is number of results
- **Explanation**: Direct radius search without filtering. Same complexity as radiusSearch method: O(N + k) for traversal and collection, plus O(k log k) for sorting by distance.

**Overall Time Complexity: O(N + k log k) worst case, where N is total stations and k is number of results**
- **Explanation**: No change from original - this is already optimal.

### Space Complexity Analysis (Worst Case)

- **Complexity**: O(N + k) worst case
- **Explanation**: 
  - Recursion stack: O(N) in worst case.
  - Results list: O(k).
  - Sorting auxiliary: O(k).
  - **Total**: O(N + k).

---

## Complexity Summary Table

| Method | Time Complexity (Worst Case) | Space Complexity (Worst Case) | Notes |
|--------|----------------------------|------------------------------|-------|
| Constructor | O(N) | O(N) | N = total stations |
| `searchByGeographicalArea` (with filters) | O(N + k + m log m · s) | O(N + k) | N = total stations, k = results in range, m ≤ k = filtered results, s = avg name length |
| `searchByGeographicalArea` (without filters) | O(N + k log k · s) | O(N + k) | N = total stations, k = results in range, s = avg name length |
| `findNearestStations` (with filters) | O(N log k + k log k) | O(N + k) | N = total stations, k = parameter n, m ≤ k = filtered results |
| `findNearestStations` (without filters) | O(N log k + k log k) | O(N + k) | N = total stations, k = parameter n |
| `findStationsInRadius` (with filters) | O(N + k + m log m) | O(N + k) | N = total stations, k = results in radius, m ≤ k = filtered results |
| `findStationsInRadius` (without filters) | O(N + k log k) | O(N + k) | N = total stations, k = results in radius |

### Case Analysis: Best, Average, and Worst Case

#### KD-tree Range Query (`rangeQuery`, `rangeQueryUnsorted`)

| Case | Time Complexity | Conditions |
|------|----------------|------------|
| **Best Case** | O(log N + k) | Balanced tree, query covers very small region, strong spatial pruning |
| **Average Case** | O(√N + k) | Balanced tree, typical query size, moderate spatial pruning |
| **Worst Case** | O(N + k) | Degenerate tree (linear) OR query covers entire dataset, no effective pruning |

#### KD-tree Nearest Neighbor Search (`nearestN`)

| Case | Time Complexity | Conditions |
|------|----------------|------------|
| **Best Case** | O(log N · log k + k log k) | Balanced tree, strong branch-and-bound pruning |
| **Average Case** | O(√N log k + k log k) | Balanced tree, moderate pruning |
| **Worst Case** | O(N log k + k log k) | Degenerate tree OR all stations equidistant, no effective pruning |

#### KD-tree Radius Search (`radiusSearch`, `radiusSearchUnsorted`)

| Case | Time Complexity | Conditions |
|------|----------------|------------|
| **Best Case** | O(log N + k) | Balanced tree, small radius, strong spatial pruning |
| **Average Case** | O(√N + k) | Balanced tree, moderate radius, moderate pruning |
| **Worst Case** | O(N + k) | Degenerate tree OR very large radius covering entire dataset, no effective pruning |

### Important Notes:

1. **String Comparison Factor**: When sorting by station name, each comparison takes O(s) time where s is the average name length. For practical purposes with bounded name lengths, this is often treated as O(1), but technically should be acknowledged. In this analysis, we include the factor explicitly: O(m log m · s).

2. **Notation Clarification**: 
   - **N** = total number of stations in the tree
   - **k** = number of results returned (for rangeQuery/radiusSearch) or the parameter `n` (for nearestN)
   - **m** = number of filtered results (m ≤ k)
   - **s** = average length of station names

3. **Space Complexity Components**:
   - **Recursion stack**: O(h) where h is tree height. In worst case (degenerate tree), h = O(N).
   - **Data structures**: O(k) for result lists, O(k) for priority queues (in nearestN), O(m) for filtered results.
   - **Auxiliary space**: O(k) or O(m) for sorting operations.


5. **Worst Case Scenarios**:
   - **Degenerate KD-tree**: If the tree is built from sorted data without proper balancing, it can degenerate to a linear structure (height = N), causing worst-case traversal.
   - **Large queries**: Queries covering the entire dataset or very large regions cannot benefit from spatial pruning.
   - **Equidistant points**: In nearest neighbor search, if all points are equidistant, branch-and-bound pruning is ineffective.

