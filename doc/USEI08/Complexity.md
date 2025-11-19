# USEI08 - Complexity Analysis (Worst Case)

## Constructor Analysis

```java
public GeographicalSearchController(List<Station> stations) {
    this.allStations = new ArrayList<>(stations);
    this.kdTree = new KDTree(stations);
}
```

**Time Complexity Analysis (Worst Case):**

```text
this.allStations = new ArrayList<>(stations);
```
- Complexity: O(n)
- Explanation: Creating a new ArrayList and copying all n stations from the input list takes O(n) time.

```text
this.kdTree = new KDTree(stations);
```
- Complexity: O(n log n)
- Explanation: Building a KD-tree from n stations requires:
  - Sorting stations by latitude: O(n log n)
  - Sorting stations by longitude: O(n log n)
  - Bulk-build algorithm: T(n) = 2T(n/2) + O(n) = O(n log n)
  The overall complexity is O(n log n).

**Overall Time Complexity: O(n log n)**
- Explanation: The overall time complexity is dominated by the KD-tree construction, which takes O(n log n) time. The ArrayList copy takes O(n) time, which is dominated by O(n log n).

---

## Constructor with Existing KDTree

```java
public GeographicalSearchController(KDTree kdTree, List<Station> stations) {
    this.kdTree = kdTree;
    this.allStations = new ArrayList<>(stations);
}
```

**Time Complexity Analysis (Worst Case):**

```text
this.kdTree = kdTree;
```
- Complexity: O(1)
- Explanation: Assigning a reference to a field is a constant time operation.

```text
this.allStations = new ArrayList<>(stations);
```
- Complexity: O(n)
- Explanation: Creating a new ArrayList and copying all n stations from the input list takes O(n) time.

**Overall Time Complexity: O(n)**
- Explanation: The overall time complexity is dominated by the ArrayList copy operation, which takes O(n) time. The reference assignment takes O(1) time, which is dominated by O(n).

---

## searchByGeographicalArea (with filters)

```java
public List<Station> searchByGeographicalArea(double latMin, double latMax, 
                                               double lonMin, double lonMax,
                                               SearchFilters filters) {
    // Validation checks...
    List<Station> stationsInRange = kdTree.rangeQuery(latMin, latMax, lonMin, lonMax);
    
    if (filters != null && filters.hasFilters()) {
        List<Station> filteredStations = new ArrayList<>();
        for (Station station : stationsInRange) {
            if (filters.matches(station)) {
                filteredStations.add(station);
            }
        }
        return filteredStations;
    }
    
    return stationsInRange;
}
```

**Time Complexity Analysis (Worst Case):**

```text
Validation checks
```
- Complexity: O(1)
- Explanation: Comparing numeric values and creating exception messages are constant time operations.

```text
kdTree.rangeQuery(latMin, latMax, lonMin, lonMax)
```
- Complexity: O(n + k log k)
- Explanation: 
  - Tree traversal: In the worst case (when the query range covers most of the tree), the range query visits O(n) nodes.
  - Result sorting: The method sorts k results by name, which takes O(k log k) time.
  - Total: O(n + k log k), where n is total stations and k is number of results.

```text
Filtering loop
```
- Complexity: O(k)
- Explanation: The loop iterates through all k stations in the range query results. For each station, filters.matches() performs constant time operations (comparisons and getter calls). Therefore, the overall complexity for filtering is O(k).

**Overall Time Complexity: O(n + k log k)**
- Explanation: The overall time complexity is dominated by the rangeQuery operation. The filtering step takes O(k) time, which is dominated by the range query complexity. The sorting step O(k log k) is included in the rangeQuery complexity.

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
- Complexity: O(n + k log k)
- Explanation: This method is a convenience wrapper that calls the main searchByGeographicalArea method with null filters. The complexity is the same as the main method.

**Overall Time Complexity: O(n + k log k)**

---

## findNearestStations (with filters)

```java
public List<KDTree.StationDistance> findNearestStations(double targetLat, double targetLon, 
                                                           int n, SearchFilters filters) {
    // Validation checks...
    List<KDTree.StationDistance> nearestStations = kdTree.nearestN(targetLat, targetLon, n);
    
    if (filters != null && filters.hasFilters()) {
        return nearestStations.stream()
            .filter(sd -> filters.matches(sd.getStation()))
            .collect(Collectors.toList());
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
- Complexity: O(n log n) where n is the parameter (number of nearest stations)
- Explanation:
  - Tree traversal: In the worst case, the algorithm may need to visit all nodes in the tree. However, with branch-and-bound pruning, the actual worst case depends on the tree structure and query point.
  - Priority queue operations: For each node visited, we check stations in buckets and perform priority queue operations (offer/poll). With n nodes visited and assuming we maintain a priority queue of size n (the parameter), each operation is O(log n).
  - Result sorting: Sorting n results by distance takes O(n log n) time, where n is the parameter (number of nearest stations requested).
  - Total: O(n log n) where n is the parameter (number of nearest stations), assuming the tree traversal visits O(n) nodes in the worst case.

```text
Filtering with stream
```
- Complexity: O(n)
- Explanation: The stream filter iterates through all n results (where n is the parameter, number of nearest stations). For each result, filters.matches() performs constant time operations.

**Overall Time Complexity: O(n log n) where n is the parameter (number of nearest stations)**
- Explanation: The complexity is dominated by the nearestN operation and the sorting step. The filtering takes O(n) time which is dominated by O(n log n).

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
- Complexity: O(n log n) where n is the parameter (number of nearest stations)
- Explanation: This method delegates to the main findNearestStations method, so it has the same complexity.

**Overall Time Complexity: O(n log n) where n is the parameter**

---

## findStationsInRadius (with filters)

```java
public List<KDTree.StationDistance> findStationsInRadius(double targetLat, double targetLon, 
                                                              double radiusKm, SearchFilters filters) {
    // Validation checks...
    List<KDTree.StationDistance> stationsInRadius = kdTree.radiusSearch(targetLat, targetLon, radiusKm);
    
    if (filters != null && filters.hasFilters()) {
        return stationsInRadius.stream()
            .filter(sd -> filters.matches(sd.getStation()))
            .collect(Collectors.toList());
    }
    
    return stationsInRadius;
}
```

**Time Complexity Analysis (Worst Case):**

```text
Validation checks
```
- Complexity: O(1)
- Explanation: Comparing numeric values and creating exception messages are constant time operations.

```text
kdTree.radiusSearch(targetLat, targetLon, radiusKm)
```
- Complexity: O(n + k log k)
- Explanation:
  - Tree traversal: In the worst case (when the radius is large enough to cover most of the tree), the radius search visits O(n) nodes.
  - Distance calculations: For each node visited, calculates distances for stations in buckets. With k results, this is O(k) work.
  - Result sorting: Sorting k results by distance takes O(k log k) time.
  - Total: O(n + k log k), where n is total stations and k is number of results within radius.

```text
Filtering with stream
```
- Complexity: O(k)
- Explanation: The stream filter iterates through all k results. For each result, filters.matches() performs constant time operations.

**Overall Time Complexity: O(n + k log k)**
- Explanation: The complexity is dominated by the radiusSearch operation. The filtering takes O(k) time which is dominated by the radius search complexity.

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
- Complexity: O(n + k log k)
- Explanation: This method delegates to the main findStationsInRadius method, so it has the same complexity.

**Overall Time Complexity: O(n + k log k)**

---

## getTreeStatistics

```java
public String getTreeStatistics() {
    int treeSize = kdTree.size();
    int treeHeight = kdTree.height();
    return String.format("Size: %d nodes, Height: %d levels", treeSize, treeHeight);
}
```

**Time Complexity Analysis (Worst Case):**

```text
kdTree.size()
```
- Complexity: O(1)
- Explanation: The size() method returns a pre-calculated value stored in the KDTree object, so it takes constant time.

```text
kdTree.height()
```
- Complexity: O(n)
- Explanation: The height() method recursively traverses the entire tree to calculate its height. In the worst case, it visits all n nodes in the tree.

```text
String.format(...)
```
- Complexity: O(1)
- Explanation: Formatting a string with two integer values is a constant time operation.

**Overall Time Complexity: O(n)**
- Explanation: The overall time complexity is dominated by the height() method, which recursively traverses the tree and takes O(n) time. The size() and string formatting operations take O(1) time, which is dominated by O(n).

---

## getKDTree

```java
public KDTree getKDTree() {
    return kdTree;
}
```

**Time Complexity Analysis (Worst Case):**

```text
return kdTree;
```
- Complexity: O(1)
- Explanation: Returning a reference is a constant time operation.

**Overall Time Complexity: O(1)**

---

## getAllStations

```java
public List<Station> getAllStations() {
    return new ArrayList<>(allStations);
}
```

**Time Complexity Analysis (Worst Case):**

```text
return new ArrayList<>(allStations);
```
- Complexity: O(n)
- Explanation: Creating a new ArrayList and copying all n stations from the allStations list takes O(n) time.

**Overall Time Complexity: O(n)**

---

## getStationsCount

```java
public int getStationsCount() {
    return allStations.size();
}
```

**Time Complexity Analysis (Worst Case):**

```text
return allStations.size();
```
- Complexity: O(1)
- Explanation: Getting the size of an ArrayList is a constant time operation, as it is stored as a field in the ArrayList object.

**Overall Time Complexity: O(1)**
