# USEI08 - Complexity

```java
public GeographicalSearchController(List<Station> stations) {
    this.allStations = new ArrayList<>(stations);
    this.kdTree = new KDTree(stations);
}
```
**Time Complexity Analysis:**

```text
this.allStations = new ArrayList<>(stations);
```
- Complexity: O(n)
- Explanation: Creating a new ArrayList and copying all n stations from the input list takes O(n) time.

```text
this.kdTree = new KDTree(stations);
```
- Complexity: O(n log n)
- Explanation: Building a KD-tree from n stations requires sorting the stations by latitude and longitude (O(n log n) each), and then building the tree using a bulk-build algorithm which also takes O(n log n) time in the worst case. The overall complexity is O(n log n).

**Overall Time Complexity: O(n log n)**
**Explanation: The overall time complexity is dominated by the KD-tree construction, which takes O(n log n) time. The ArrayList copy takes O(n) time, which is dominated by O(n log n).**

---

```java
public GeographicalSearchController(KDTree kdTree, List<Station> stations) {
    this.kdTree = kdTree;
    this.allStations = new ArrayList<>(stations);
}
```
**Time Complexity Analysis:**

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
**Explanation: The overall time complexity is dominated by the ArrayList copy operation, which takes O(n) time. The reference assignment takes O(1) time, which is dominated by O(n).**

---

```java
public List<Station> searchByGeographicalArea(double latMin, double latMax, 
                                               double lonMin, double lonMax,
                                               SearchFilters filters) {
    if (latMin < -90 || latMax > 90 || latMin > latMax) {
        throw new IllegalArgumentException(
            "Invalid latitude range: [" + latMin + ", " + latMax + "]. " +
            "Latitude must be between -90 and 90, and minimum must be <= maximum.");
    }
    
    if (lonMin < -180 || lonMax > 180 || lonMin > lonMax) {
        throw new IllegalArgumentException(
            "Invalid longitude range: [" + lonMin + ", " + lonMax + "]. " +
            "Longitude must be between -180 and 180, and minimum must be <= maximum.");
    }
    
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
**Time Complexity Analysis:**

```text
if (latMin < -90 || latMax > 90 || latMin > latMax) {
    throw new IllegalArgumentException(...);
}
```
- Complexity: O(1)
- Explanation: Comparing numeric values and creating an exception message are constant time operations.

```text
if (lonMin < -180 || lonMax > 180 || lonMin > lonMax) {
    throw new IllegalArgumentException(...);
}
```
- Complexity: O(1)
- Explanation: Comparing numeric values and creating an exception message are constant time operations.

```text
List<Station> stationsInRange = kdTree.rangeQuery(latMin, latMax, lonMin, lonMax);
```
- Complexity: O(√n + k) average case, O(n) worst case
- Explanation: The rangeQuery method performs a recursive search on the KD-tree. In the average case with a balanced tree, it visits O(√n) nodes and finds k results. The method also sorts the results by name, which takes O(k log k) time where k is the number of results. However, in the worst case (when the query range covers most of the tree), it may need to visit O(n) nodes. The sorting step takes O(k log k) time, but since k ≤ n, this is dominated by the tree traversal in the worst case.

```text
if (filters != null && filters.hasFilters()) {
    List<Station> filteredStations = new ArrayList<>();
    for (Station station : stationsInRange) {
        if (filters.matches(station)) {
            filteredStations.add(station);
        }
    }
    return filteredStations;
}
```
- Complexity: O(k)
- Explanation: The loop iterates through all k stations in the range query results. For each station, it calls filters.matches() which performs constant time operations (comparisons and getter calls). Therefore, the overall complexity for filtering is O(k), where k is the number of stations in the range.

```text
return stationsInRange;
```
- Complexity: O(1)
- Explanation: Returning a reference is a constant time operation.

**Overall Time Complexity: O(√n + k) average case, O(n) worst case**
**Explanation: The overall time complexity is dominated by the rangeQuery operation. In the average case with a balanced KD-tree, the range query takes O(√n + k) time, where n is the total number of stations and k is the number of results. The filtering step takes O(k) time, which is dominated by the range query. In the worst case, when the query range covers most of the tree, the range query may take O(n) time. The sorting of results (O(k log k)) is also included in the rangeQuery complexity.**

---

```java
public List<Station> searchByGeographicalArea(double latMin, double latMax, 
                                               double lonMin, double lonMax) {
    return searchByGeographicalArea(latMin, latMax, lonMin, lonMax, null);
}
```
**Time Complexity Analysis:**

```text
return searchByGeographicalArea(latMin, latMax, lonMin, lonMax, null);
```
- Complexity: O(√n + k) average case, O(n) worst case
- Explanation: This method is a convenience wrapper that calls the main searchByGeographicalArea method with null filters. The complexity is the same as the main method: O(√n + k) in the average case and O(n) in the worst case, where n is the total number of stations and k is the number of results.

**Overall Time Complexity: O(√n + k) average case, O(n) worst case**
**Explanation: The method delegates to the main searchByGeographicalArea method, so it has the same complexity.**

---

```java
public String getTreeStatistics() {
    int treeSize = kdTree.size();
    int treeHeight = kdTree.height();
    return String.format("Size: %d nodes, Height: %d levels", treeSize, treeHeight);
}
```
**Time Complexity Analysis:**

```text
int treeSize = kdTree.size();
```
- Complexity: O(1)
- Explanation: The size() method returns a pre-calculated value stored in the KDTree object, so it takes constant time.

```text
int treeHeight = kdTree.height();
```
- Complexity: O(n)
- Explanation: The height() method recursively traverses the tree to calculate its height. In the worst case, it visits all n nodes in the tree.

```text
return String.format("Size: %d nodes, Height: %d levels", treeSize, treeHeight);
```
- Complexity: O(1)
- Explanation: Formatting a string with two integer values is a constant time operation.

**Overall Time Complexity: O(n)**
**Explanation: The overall time complexity is dominated by the height() method, which recursively traverses the tree and takes O(n) time in the worst case. The size() and string formatting operations take O(1) time, which is dominated by O(n).**

---

```java
public KDTree getKDTree() {
    return kdTree;
}
```
**Time Complexity Analysis:**

```text
return kdTree;
```
- Complexity: O(1)
- Explanation: Returning a reference is a constant time operation.

**Overall Time Complexity: O(1)**
**Explanation: The method only returns a reference to the KD-tree, which takes constant time.**

---

```java
public List<Station> getAllStations() {
    return new ArrayList<>(allStations);
}
```
**Time Complexity Analysis:**

```text
return new ArrayList<>(allStations);
```
- Complexity: O(n)
- Explanation: Creating a new ArrayList and copying all n stations from the allStations list takes O(n) time.

**Overall Time Complexity: O(n)**
**Explanation: The method creates a defensive copy of the allStations list, which requires copying all n stations, resulting in O(n) time complexity.**

---

```java
public int getStationsCount() {
    return allStations.size();
}
```
**Time Complexity Analysis:**

```text
return allStations.size();
```
- Complexity: O(1)
- Explanation: Getting the size of an ArrayList is a constant time operation, as it is stored as a field in the ArrayList object.

**Overall Time Complexity: O(1)**
**Explanation: The method returns the size of the allStations list, which is a constant time operation.**

