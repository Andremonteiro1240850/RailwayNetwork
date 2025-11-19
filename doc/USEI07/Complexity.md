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
- Complexity: O(n log n)
- Explanation: The bulk-build algorithm recursively processes the stations. At each level, it partitions O(n) elements, and there are O(log n) levels. Therefore, the overall complexity is O(n log n).

```text
    this.numNodes = countNodes(this.root);
    this.numStations = countStations(this.root);
```
- Complexity: O(n)
- Explanation: Both countNodes and countStations traverse the entire tree, visiting each of the n nodes once, taking O(n) time.

**Overall Time Complexity: O(n log n)**
**Explanation: The overall time complexity is dominated by the sorting operations and the bulk-build algorithm, both of which take O(n log n) time. Therefore, the total complexity is O(n log n).**

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
- Explanation: Selecting the median from a sorted array by index is a constant time operation.

```text
    for (Station station : sortedByAxis) {
        // ... comparison and partitioning logic
    }
```
- Complexity: O(n)
- Explanation: The loop iterates over each of the n stations in the current partition. Each iteration performs constant time operations (comparisons and list additions).

```text
    bucket.sort(Comparator.comparing(Station::getName));
```
- Complexity: O(b log b) where b is the bucket size
- Explanation: Sorting the bucket takes O(b log b) time. In the worst case, if all stations have the same coordinates, b = n, giving O(n log n). However, typically b is small, so this is usually O(1) or O(log n) in practice.

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
- Explanation: The loop iterates over each of the n stations in the other sorted array. Each iteration performs constant time operations.

```text
    node.setLeft(bulkBuild(leftByAxis, leftByOther, depth + 1));
    node.setRight(bulkBuild(rightByAxis, rightByOther, depth + 1));
```
- Complexity: O(n log n) overall
- Explanation: The recursive calls process the left and right partitions. At each level, we process O(n) elements, and there are O(log n) levels. Therefore, the overall complexity is O(n log n).

**Overall Time Complexity: O(n log n)**
**Explanation: The bulk-build algorithm processes O(n) elements at each of the O(log n) levels, resulting in O(n log n) time complexity.**

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

**Overall Space Complexity: O(n)**
**Explanation: The overall space complexity is O(n) for storing the tree nodes, buckets, and temporary arrays during the recursive build process.**

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
- Explanation: In a balanced KD-tree, a range query visits O(√n) nodes in the worst case, and collects k results. However, in the worst case where all stations are in the range, k = n, giving O(n).

```text
    results.sort(Comparator.comparing(Station::getName));
```
- Complexity: O(k log k) where k is the number of results
- Explanation: Sorting k results takes O(k log k) time. In the worst case, k = n, giving O(n log n).

**Overall Time Complexity: O(√n + k log k)**
**Explanation: The overall time complexity is O(√n + k log k), where k is the number of results. In the worst case where all stations are in the range, this becomes O(n log n).**

