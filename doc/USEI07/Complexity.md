# USEI07 - Complexity Analysis

## Overview

This document provides a detailed complexity analysis for building a balanced 2D-tree (KD-tree) on latitude/longitude coordinates.

**Notation:**
- **n** = number of stations

---

## Method: `buildKdTree(List<Station> stations)`

### Code Structure

```java
public KDNode buildKdTree(List<Station> stations) {
    // Validation
    if (stations == null || stations.isEmpty()) {
        return null;
    }
    
    // Filter valid stations
    List<Station> validStations = new ArrayList<>();
    for (Station station : stations) {
        if (isValidCoordinate(station) && station.getName() != null) {
            validStations.add(station);
        }
    }
    
    // Create and sort arrays
    List<Station> sortedByLat = new ArrayList<>(validStations);
    List<Station> sortedByLon = new ArrayList<>(validStations);
    sortedByLat.sort(...);
    sortedByLon.sort(...);
    
    // Build tree
    this.root = bulkBuild(sortedByLat, sortedByLon, 0);
    
    // Calculate metrics
    this.numNodes = countNodes(this.root);
    this.numStations = countStations(this.root);
    
    return this.root;
}
```

---

## Time Complexity Analysis

### Step-by-Step Breakdown

| Step | Operation | Complexity | Explanation |
|------|-----------|------------|-------------|
| 1 | Null/empty check | O(1) | Constant time validation |
| 2 | Filter valid stations | O(n) | Iterate over n stations, constant validation per station |
| 3 | Create list copies | O(n) | Two copies of n stations |
| 4 | Sort by latitude | O(n log n) | TimSort algorithm |
| 5 | Sort by longitude | O(n log n) | TimSort algorithm |
| 6 | Bulk-build tree | O(n log n) | Divide-and-conquer algorithm (see detailed analysis below) |
| 7 | Count nodes/stations | O(n) | Traverse tree once |

### Overall Time Complexity

**O(n log n)** - both average and worst case (pathological)

**Explanation:** The complexity is dominated by the two sorting operations (O(n log n) each) and the bulk-build algorithm (O(n log n)). Even in the worst pathological case where all stations share the same coordinates, the bulk-build takes O(n log n) time (all stations go into a single bucket at the root, which is sorted in O(n log n), then recursion terminates).

---

## Space Complexity Analysis

### Step-by-Step Breakdown

| Step | Operation | Complexity | Explanation |
|------|-----------|------------|-------------|
| 1 | Valid stations list | O(n) | Stores at most n stations |
| 2 | Sorted lists (lat/lon) | O(n) | Two lists of n stations each |
| 3 | Tree structure | O(n) | n nodes with buckets totaling n stations |
| 4 | Temporary arrays (construction) | O(n) | Geometric sum of active lists (see detailed analysis) |

### Overall Space Complexity

**O(n)**

**Explanation:** The persistent tree structure stores n nodes with buckets summing to n stations. During construction, temporary lists are created at each recursive level, but due to the geometric sum property (active lists at level d total at most n elements), the peak memory usage is O(n), not O(n log n).

---

## Detailed Analysis: `bulkBuild` Method

### Algorithm Overview

The `bulkBuild` method implements a divide-and-conquer algorithm that:
1. Selects the median element from the current partition
2. Partitions stations into bucket (same coordinates), left, and right
3. Recursively builds left and right subtrees

### Time Complexity: Recurrence Relation

**Ideal Case (Perfectly Balanced Tree):**

- At each level: partition n elements into ~n/2 elements
- Work per level: O(n) (two linear loops for partitioning)
- Tree height: log₂(n)
- **Recurrence:** T(n) = 2T(n/2) + O(n)
- **Solution (Master Theorem Case 2):** T(n) = Θ(n log n) = **O(n log n)**

**Worst Pathological Case (All Stations Share Same Coordinates):**

- All n stations go into root bucket
- Bucket sort: O(n log n)
- Recursion terminates immediately (empty partitions)
- **Total cost:** O(n log n)

**Average Case (Spatially Distributed Data):**

- Minimal coordinate overlap (small bucket sizes)
- **Complexity:** O(n log n)

### Space Complexity: Geometric Sum Analysis

During construction, temporary lists are created at each recursive level:

- **Level 0 (root):** Lists totaling n elements
- **Level 1:** Two sets of lists, each ≤ n/2, together ≤ n elements
- **Level 2:** Four sets of lists, each ≤ n/4, together ≤ n elements
- **Level d:** 2^d sets of lists, each ≤ n/2^d, together ≤ n elements

Since lists become eligible for garbage collection after recursive calls complete, the **peak memory usage is O(n)**, not O(n log n).

---

## Range Query Complexity

### Method: `rangeQuery(double latMin, double latMax, double lonMin, double lonMax)`

| Operation | Complexity | Explanation |
|-----------|------------|-------------|
| Tree traversal | O(√n + k) | Visits O(√n) nodes, collects k results |
| Final sorting | O(k log k) | Sort k results by name |
| **Overall** | **O(√n + k log k)** | Where k = number of results |

**Note:** In worst case where k = n, complexity becomes O(n log n), dominated by sorting.

---

## Summary Table

| Operation | Time Complexity | Space Complexity |
|-----------|----------------|------------------|
| **Build Tree** | O(n log n) | O(n) |
| **Range Query** | O(√n + k log k) | O(k + log n) |

**Where:**
- n = total number of stations
- k = number of results
