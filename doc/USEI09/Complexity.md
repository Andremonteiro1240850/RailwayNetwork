# USEI09 - Complexity Analysis

## Overview

This document provides a detailed complexity analysis for Nearest Neighbor Search and Radius Search operations on the KD-tree.

**Notation:**
- **N** = total number of stations in the tree
- **k** = parameter n (number of nearest neighbors desired) OR number of results (for radius search)

---

## Method 1: `nearestN(double targetLat, double targetLon, int n)`

### Description

Finds the N nearest stations to a target point using a branch-and-bound algorithm with spatial pruning.

### Time Complexity

#### Algorithm Breakdown

| Component | Complexity | Explanation |
|-----------|------------|-------------|
| Node visitation | O(√N) average | Classical result for KD-trees in 2D |
| Distance calculation | O(1) per station | Haversine formula |
| Priority queue operations | O(log k) per station | Queue size ≤ k |
| Stations processed | O(√N) average, O(N) worst | Depends on spatial distribution |
| Final sorting | O(k log k) | Sort k nearest neighbors |

#### Overall Complexity

| Case | Time Complexity | Notes |
|------|-----------------|-------|
| **Average** | **O(√N · log k + k log k)** | k ≪ N typically |
| **Worst** | **O(N log k + k log k)** | All stations very close |

**Observations:**
- If k ≪ N (normal case), dominant cost is **O(√N log k)**
- Final term k log k comes from sorting the k neighbors

### Space Complexity

| Component | Complexity | Explanation |
|-----------|------------|-------------|
| Priority queue | O(k) | Stores at most k stations |
| Recursion stack | O(log N) | Tree depth |
| Result list | O(k) | Final sorted results |
| **Overall** | **O(k + log N)** | |

---

## Method 2: `radiusSearch(double targetLat, double targetLon, double radiusKm)`

### Description

Finds all stations within a specified radius of a target point using spatial pruning.

### Time Complexity

#### Algorithm Breakdown

| Component | Complexity | Explanation |
|-----------|------------|-------------|
| Node visitation | O(√N) average | Spatial pruning |
| Stations checked | O(√N) average | Distance calculation + comparison |
| Results sorting | O(k log k) | Where k = number of results |

#### Overall Complexity

| Case | Time Complexity | Notes |
|------|-----------------|-------|
| **Average** | **O(√N + k log k)** | Reasonable radius |
| **Worst** | **O(N log N)** | Very large radius (k = N) |

**Note:** Worst case simplifies to O(N log N) because sorting dominates when k = N.

### Space Complexity

| Component | Complexity | Explanation |
|-----------|------------|-------------|
| Results list | O(k) | Stations within radius |
| Recursion stack | O(log N) | Tree depth |
| **Overall** | **O(k + log N)** | |

---

## Method 3: `haversineDistance(double lat1, double lon1, double lat2, double lon2)`

### Description

Calculates the great-circle distance between two points on Earth using the Haversine formula.

### Complexity

| Type | Complexity | Explanation |
|------|------------|-------------|
| **Time** | **O(1)** | Constant number of arithmetic operations |
| **Space** | **O(1)** | Constant memory for intermediate calculations |

---

## Summary Table

### Time Complexity

| Operation | Average Case | Worst Case |
|-----------|--------------|------------|
| **nearestN** | O(√N log k + k log k) | O(N log k + k log k) |
| **radiusSearch** | O(√N + k log k) | O(N log N) |

### Space Complexity

| Operation | Space Complexity |
|-----------|------------------|
| **nearestN** | O(k + log N) |
| **radiusSearch** | O(k + log N) |

**Where:**
- N = total number of stations
- k = number of nearest neighbors (nearestN) or results (radiusSearch)

---

## Comparison with Naive Approach

### Performance Comparison

| Approach | nearestN | radiusSearch | Space |
|----------|----------|--------------|-------|
| **Naive (linear search)** | O(N) | O(N) | O(k) |
| **KD-tree** | O(√N log k + k log k) | O(√N + k log k) | O(k + log N) |

### Advantages of KD-tree

- **For large datasets (N >> k):** Significantly faster than linear search
- **Spatial pruning:** Skips large portions of dataset guaranteed to be outside search region
- **Logarithmic overhead:** Negligible compared to time savings

### When KD-tree is Less Efficient

- **Very small datasets (N < 100):** Tree traversal overhead may outweigh benefits
- **When k ≈ N:** Algorithm approaches O(N log N), similar to naive approach with sorting

---

## Notes on Pruning Efficiency

The efficiency of both algorithms depends on spatial pruning quality:

1. **Balanced tree:** O(log N) height enables effective pruning
2. **Radius size:** Smaller radii lead to better pruning
3. **Query location:** Queries near dataset center may explore more nodes
4. **Station distribution:** Clustered distributions reduce pruning effectiveness

---

## Notes on Filtering

When filters are applied to results (e.g., timezone filters):

| Component | Complexity | Notes |
|-----------|------------|-------|
| **Filtering step** | O(k) | Linear pass through k results |
| **Overall complexity** | Same as without filters | O(k) dominated by search operations |

**Explanation:** Filtering adds O(k) which is already dominated by O(√N log k) or O(√N) terms in the search phase.
