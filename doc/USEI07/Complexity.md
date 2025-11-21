# USEI07 - Complexity

```java
public KDNode buildKdTree(List<Station> stations) {
    if (stations == null || stations.isEmpty()) {
        this.root = null;
        this.numNodes = 0;
        this.numStations = 0;
        return null;
    }
    
    // Filter out invalid stations
    List<Station> validStations = new ArrayList<>();
    for (Station station : stations) {
        if (isValidCoordinate(station) && station.getName() != null && !station.getName().trim().isEmpty()) {
            validStations.add(station);
        }
    }
    
    if (validStations.isEmpty()) {
        this.root = null;
        this.numNodes = 0;
        this.numStations = 0;
        return null;
    }
    
    // Create sorted arrays by latitude and longitude
    List<Station> sortedByLat = new ArrayList<>(validStations);
    List<Station> sortedByLon = new ArrayList<>(validStations);
    
    sortedByLat.sort(Comparator.comparingDouble(Station::getLatitude)
            .thenComparingDouble(Station::getLongitude)
            .thenComparing(Station::getName));
    
    sortedByLon.sort(Comparator.comparingDouble(Station::getLongitude)
            .thenComparingDouble(Station::getLatitude)
            .thenComparing(Station::getName));
    
    // Build tree using bulk-build algorithm
    this.root = bulkBuild(sortedByLat, sortedByLon, 0);
    
    // Calculate metrics
    this.numNodes = countNodes(this.root);
    this.numStations = countStations(this.root);
    
    return this.root;
}
```

**Time Complexity Analysis:**

```text
    if (stations == null || stations.isEmpty()) {
        this.root = null;
        this.numNodes = 0;
        this.numStations = 0;
        return null;
    }
```
- Complexity: O(1)
- Explanation: Checking if the list is null or empty is a constant time operation.

```text
    List<Station> validStations = new ArrayList<>();
    for (Station station : stations) {
        if (isValidCoordinate(station) && station.getName() != null && !station.getName().trim().isEmpty()) {
            validStations.add(station);
        }
    }
```
- Complexity: O(n)
- Explanation: The loop iterates over each of the n stations. For each station, we perform validation checks which take constant time. Therefore, the overall complexity for this part is O(n).

```text
    List<Station> sortedByLat = new ArrayList<>(validStations);
    List<Station> sortedByLon = new ArrayList<>(validStations);
```
- Complexity: O(n)
- Explanation: Creating two copies of the list, each containing n stations, takes O(n) time.

```text
    sortedByLat.sort(Comparator.comparingDouble(Station::getLatitude)
            .thenComparingDouble(Station::getLongitude)
            .thenComparing(Station::getName));
```
- Complexity: O(n log n)
- Explanation: Sorting n stations using a stable sort algorithm (like TimSort in Java) takes O(n log n) time. The comparator performs constant time operations for each comparison.

```text
    sortedByLon.sort(Comparator.comparingDouble(Station::getLongitude)
            .thenComparingDouble(Station::getLatitude)
            .thenComparing(Station::getName));
```
- Complexity: O(n log n)
- Explanation: Similar to the previous sort, sorting n stations takes O(n log n) time.

```text
    this.root = bulkBuild(sortedByLat, sortedByLon, 0);
```
- Complexity: O(n log n) average case, O(n² log n) worst case (patological)
- Explanation: The bulk-build algorithm is a divide-and-conquer algorithm. In the average case with spatially distributed data (like railway stations), the complexity is O(n log n). However, in the worst pathological case where all stations share the same coordinates, the complexity degrades to O(n² log n) due to the bucket sorting operation. See detailed analysis in the bulkBuild section below.

```text
    this.numNodes = countNodes(this.root);
    this.numStations = countStations(this.root);
```
- Complexity: O(n)
- Explanation: Both countNodes and countStations traverse the entire tree, visiting each of the n nodes once, taking O(n) time.

**Overall Time Complexity: O(n log n) average case, O(n² log n) worst case (patological)**
**Explanation: The overall time complexity is dominated by the sorting operations (O(n log n)) and the bulk-build algorithm. In the average case with spatially distributed data, the bulk-build takes O(n log n) time, resulting in overall O(n log n). In the worst pathological case where all stations share the same coordinates, the bulk-build degrades to O(n² log n), dominating the overall complexity.**

**Space Complexity Analysis:**

```text
    List<Station> validStations = new ArrayList<>();
```
- Complexity: O(n)
- Explanation: The list stores at most n stations.

```text
    List<Station> sortedByLat = new ArrayList<>(validStations);
    List<Station> sortedByLon = new ArrayList<>(validStations);
```
- Complexity: O(n)
- Explanation: Both lists store n stations, requiring O(n) space.

```text
    this.root = bulkBuild(sortedByLat, sortedByLon, 0);
```
- Complexity: O(n)
- Explanation: The tree structure stores n nodes (one per unique coordinate pair). Each node contains a bucket with stations, and the total number of stations across all buckets is n. Additionally, during the recursive build, temporary arrays are created for partitioning, but the total space used is bounded by O(n).

**Overall Space Complexity: O(n)**
**Explanation: The overall space complexity is O(n) for storing the tree nodes, buckets, and temporary arrays during construction.**

```java
private KDNode bulkBuild(List<Station> sortedByLat, List<Station> sortedByLon, int depth) {
    if (sortedByLat.isEmpty() || sortedByLon.isEmpty()) {
        return null;
    }
    
    int axis = depth % 2; // 0 for latitude, 1 for longitude
    List<Station> sortedByAxis = (axis == 0) ? sortedByLat : sortedByLon;
    
    // Select median
    int medianIndex = sortedByAxis.size() / 2;
    Station medianStation = sortedByAxis.get(medianIndex);
    double medianValue = (axis == 0) ? medianStation.getLatitude() : medianStation.getLongitude();
    
    // Collect all stations with the exact same (lat, lon) as median
    List<Station> bucket = new ArrayList<>();
    List<Station> leftByAxis = new ArrayList<>();
    List<Station> rightByAxis = new ArrayList<>();
    
    double medianLat = medianStation.getLatitude();
    double medianLon = medianStation.getLongitude();
    
    for (Station station : sortedByAxis) {
        double coord = (axis == 0) ? station.getLatitude() : station.getLongitude();
        int cmp = Double.compare(coord, medianValue);
        
        // Check if exact same (lat, lon) pair
        boolean exactMatch = Double.compare(station.getLatitude(), medianLat) == 0 &&
                             Double.compare(station.getLongitude(), medianLon) == 0;
        
        if (exactMatch) {
            bucket.add(station);
        } else if (cmp < 0) {
            leftByAxis.add(station);
        } else {
            rightByAxis.add(station);
        }
    }
    
    // Sort bucket by name (ascending)
    bucket.sort(Comparator.comparing(Station::getName));
    
    // Partition the other array (excluding stations in bucket)
    List<Station> leftByOther = new ArrayList<>();
    List<Station> rightByOther = new ArrayList<>();
    List<Station> sortedByOther = (axis == 0) ? sortedByLon : sortedByLat;
    
    // Create a set of (lat, lon) pairs for bucket stations
    Set<String> bucketCoords = new HashSet<>();
    for (Station s : bucket) {
        bucketCoords.add(s.getLatitude() + "," + s.getLongitude());
    }
    
    for (Station station : sortedByOther) {
        String stationCoord = station.getLatitude() + "," + station.getLongitude();
        if (bucketCoords.contains(stationCoord)) {
            continue; // Skip stations in bucket (same coordinates)
        }
        
        double coord = (axis == 0) ? station.getLatitude() : station.getLongitude();
        int cmp = Double.compare(coord, medianValue);
        
        if (cmp < 0) {
            leftByOther.add(station);
        } else {
            rightByOther.add(station);
        }
    }
    
    // Create node
    KDNode node = new KDNode(axis, medianValue, bucket);
    
    // Recursively build left and right subtrees
    if (axis == 0) {
        node.setLeft(bulkBuild(leftByAxis, leftByOther, depth + 1));
        node.setRight(bulkBuild(rightByAxis, rightByOther, depth + 1));
    } else {
        node.setLeft(bulkBuild(leftByOther, leftByAxis, depth + 1));
        node.setRight(bulkBuild(rightByOther, rightByAxis, depth + 1));
    }
    
    return node;
}
```

**Time Complexity Analysis:**

```text
    if (sortedByLat.isEmpty() || sortedByLon.isEmpty()) {
        return null;
    }
```
- Complexity: O(1)
- Explanation: Checking if lists are empty is a constant time operation.

```text
    int axis = depth % 2;
    List<Station> sortedByAxis = (axis == 0) ? sortedByLat : sortedByLon;
    int medianIndex = sortedByAxis.size() / 2;
    Station medianStation = sortedByAxis.get(medianIndex);
    double medianValue = (axis == 0) ? medianStation.getLatitude() : medianStation.getLongitude();
```
- Complexity: O(1)
- Explanation: Selecting the median from a sorted array by index is a constant time operation. The code uses the median of an already sorted list, which is the classical method to guarantee that the KD-tree remains balanced during initial construction. This is scientifically sound, as choosing the middle element as the pivot ensures that the left and right subtrees have at most n/2 elements.

```text
    for (Station station : sortedByAxis) {
        // ... comparison and partitioning logic
    }
```
- Complexity: O(n)
- Explanation: The loop iterates over each of the n stations in the current partition. Each iteration performs constant time operations (comparisons and list additions). This is the "hidden cost" of linear operations within bulkBuild.

```text
    bucket.sort(Comparator.comparing(Station::getName));
```
- Complexity: O(b log b) where b is the bucket size
- Explanation: Sorting the bucket takes O(b log b) time. In the average/expected case with spatially distributed data (like railway stations), overlap is minimal (b is small), and this operation contributes O(1) or O(log n) to the overall complexity. However, in the worst pathological case where all n stations share the same coordinates, the bucket will have size n, making this operation O(n log n) at the root level. This breaks the recurrence relation, as it becomes T(n) = T(0) + T(0) + O(n log n) for that node, which is O(n log n) by itself.

```text
    Set<String> bucketCoords = new HashSet<>();
    for (Station s : bucket) {
        bucketCoords.add(s.getLatitude() + "," + s.getLongitude());
    }
```
- Complexity: O(b) where b is the bucket size
- Explanation: Creating the set and adding b coordinates takes O(b) time.

```text
    for (Station station : sortedByOther) {
        // ... partitioning logic
    }
```
- Complexity: O(n)
- Explanation: The loop iterates over each of the n stations in the other sorted array. Each iteration performs constant time operations. This is another linear operation contributing to the recurrence relation.

```text
    node.setLeft(bulkBuild(leftByAxis, leftByOther, depth + 1));
    node.setRight(bulkBuild(rightByAxis, rightByOther, depth + 1));
```
- Complexity: O(n log n) average case, O(n² log n) worst case (patological)
- Explanation: The recursive calls process the left and right partitions. The algorithm is a divide-and-conquer algorithm whose complexity critically depends on how the problem is divided.

**Scientific Analysis of bulkBuild:**

**Ideal Case (Perfectly Balanced Tree):**
- At each recursive level, the list of n elements is partitioned into two lists of ~n/2 elements.
- The work at each level (excluding recursive calls) is dominated by the loops that traverse sortedByAxis and sortedByOther, i.e., O(n).
- The height of a perfectly balanced tree is log₂(n).
- The recurrence relation is: **T(n) = 2T(n/2) + O(n)**
- By the Master Theorem (Case 2), this recurrence directly resolves to **O(n log n)**.

**Worst Pathological Case (All Stations Share Same Coordinates):**
- If all n stations share the same coordinates, the bucket at the root will have size n.
- The `bucket.sort(...)` operation becomes O(n log n) at the root level.
- After this, all partitions will be empty, so the process terminates.
- The total cost is dominated by the sort of the root bucket: **O(n² log n)**.

**Average/Expected Case (Spatially Distributed Data):**
- With spatially distributed data (like railway stations), overlap is minimal (b is small).
- The complexity maintains **O(n log n)**.

**Overall Time Complexity: O(n log n) average case, O(n² log n) worst case (patological)**
**Explanation: In the average case with spatially distributed data, the bulk-build algorithm has O(n log n) complexity following the recurrence relation T(n) = 2T(n/2) + O(n). In the worst pathological case where all stations share the same coordinates, the complexity degrades to O(n² log n) due to the bucket sorting operation at the root level.**

**Space Complexity Analysis:**

```text
    List<Station> bucket = new ArrayList<>();
    List<Station> leftByAxis = new ArrayList<>();
    List<Station> rightByAxis = new ArrayList<>();
    List<Station> leftByOther = new ArrayList<>();
    List<Station> rightByOther = new ArrayList<>();
```
- Complexity: O(n)
- Explanation: The total size of all these lists is at most n (the number of stations being partitioned at this level).

```text
    Set<String> bucketCoords = new HashSet<>();
```
- Complexity: O(b) where b is the bucket size
- Explanation: The set stores at most b coordinate pairs.

```text
    node.setLeft(bulkBuild(leftByAxis, leftByOther, depth + 1));
    node.setRight(bulkBuild(rightByAxis, rightByOther, depth + 1));
```
- Complexity: O(n) overall
- Explanation: The recursive calls create temporary arrays, but the total space used across all recursive levels is bounded by O(n), as each station is stored exactly once in the tree structure.

**Scientific Analysis of Space Complexity:**

**Persistent Data Structure:** The final KD-tree stores n nodes. Each node contains a constant amount of data (axis value, bucket of stations, pointers), plus the bucket. The sum of the sizes of all buckets is n. Therefore, the tree itself occupies Θ(n) space.

**Construction Cost:** During construction, the algorithm creates temporary lists (leftByAxis, rightByAxis, etc.). However, at any given level, the sum of the sizes of these lists is O(n). While the levels are recursive, as long as the lists are passed to recursive calls and then become eligible for garbage collection, the working memory space can be maintained at O(n). The worst-case memory usage during the process is, in fact, O(n).

**Overall Space Complexity: O(n)**
**Explanation: The overall space complexity is O(n) for storing the tree nodes, buckets, and temporary arrays during the recursive build process. The persistent data structure stores n nodes with buckets summing to n stations, occupying Θ(n) space. During construction, temporary lists are created, but the total space used across all recursive levels is bounded by O(n).**

```java
public List<Station> rangeQuery(double latMin, double latMax, double lonMin, double lonMax) {
    List<Station> results = new ArrayList<>();
    rangeQueryRecursive(this.root, latMin, latMax, lonMin, lonMax, 0, results);
    
    // Sort all results by name
    results.sort(Comparator.comparing(Station::getName));
    return results;
}
```

**Time Complexity Analysis:**

```text
    List<Station> results = new ArrayList<>();
```
- Complexity: O(1)
- Explanation: Initializing an empty list is a constant time operation.

```text
    rangeQueryRecursive(this.root, latMin, latMax, lonMin, lonMax, 0, results);
```
- Complexity: O(√n + k) where k is the number of results
- Explanation: In a balanced KD-tree, a range query visits O(√n) nodes in the worst case, and collects k results. This notation is typical for k-d trees with k=2 in two-dimensional range queries. It is correct for the tree traversal phase.

```text
    results.sort(Comparator.comparing(Station::getName));
```
- Complexity: O(k log k) where k is the number of results
- Explanation: Sorting k results takes O(k log k) time. In the worst case, k = n, giving O(n log n).

**Overall Time Complexity: O(√n + k log k)**
**Explanation: The overall time complexity is O(√n + k log k), where k is the number of results. The tree traversal phase takes O(√n + k) time, and the subsequent sorting operation takes O(k log k) time. In the worst case where the region covers all stations (k = n), the complexity becomes O(n log n), dominated by the sorting operation. This is scientifically precise.**

