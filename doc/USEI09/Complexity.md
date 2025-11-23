# USEI09 - Complexity Analysis

## Overview

This document provides a rigorous time complexity analysis for Nearest Neighbor Search and Radius Search operations on the KD-tree, following academic principles of asymptotic analysis.

**Notation:**
- **N** = total number of stations in the tree
- **k** = parameter n (number of nearest neighbors desired) OR number of results (for radius search)
- **h** = tree height (O(log N) for balanced tree, O(N) for degenerate tree)
- **b** = maximum bucket size (stations with identical coordinates)
- **m** = number of nodes visited during tree traversal

**Algorithm Classification:**
- **Non-deterministic algorithms:** `nearestN` and `radiusSearch` are non-deterministic because their execution paths depend on spatial pruning and branch-and-bound decisions that vary with input data distribution. These require best case, average case, and worst case analysis.
- **Deterministic algorithms:** `haversineDistance` is deterministic because it always executes the same sequence of operations for any input. It requires only a single complexity analysis.

**Assumptions:**
- **Balanced KD-tree:** Tree height is O(log N) in typical cases (bulk-build with median selection ensures balance)
- **Bounded bucket sizes:** In typical cases, bucket sizes are bounded by a constant or O(log N). In pathological cases, all N stations may share coordinates (b = N)
- **Bounded string lengths:** Station names have bounded length, making string comparisons O(1) in asymptotic analysis
- **Haversine distance:** Calculation is O(1) constant time (constant number of arithmetic and trigonometric operations)
- **Final sorting:** Results are sorted by distance as required by the algorithm specification

---

## Method 1: `nearestN(double targetLat, double targetLon, int n)`

### Description

Finds the n nearest stations to a target point using a branch-and-bound algorithm with spatial pruning. Uses a max-heap priority queue to maintain the n closest stations during tree traversal.

### Algorithm Structure

1. **Initialization:** Create max-heap priority queue (size ≤ n)
2. **Recursive traversal:** Visit nodes using branch-and-bound pruning
3. **For each node visited:**
   - Process all stations in the bucket
   - For each station: calculate Haversine distance O(1)
   - Update priority queue: O(log n) per insertion/deletion
4. **Final sorting:** Convert queue to list and sort by distance: O(k log k)

### Time Complexity — Best Case

**O(log N + n log n + k log k)**

**Component Breakdown:**

| Component | Best Case Complexity | Justification |
|-----------|---------------------|---------------|
| **Node visitation** | O(log N) | Best case: query point is positioned such that branch-and-bound pruning eliminates all but one path from root to leaf. Only O(log N) nodes (tree height) are visited |
| **Stations processed** | O(n) | In best case, only n stations need to be examined (the n nearest are found quickly along the optimal path) |
| **Distance calculations** | O(n) | O(1) per station × O(n) stations = O(n) |
| **Priority queue operations** | O(n log n) | All n stations are inserted into the queue: n × O(log n) = O(n log n) |
| **Final sorting** | O(k log k) | Sort k results, where k ≤ n |

**Detailed Justification:**

1. **Optimal pruning:** The best case occurs when the query point is positioned such that:
   - The nearest n stations are all located along a single path from root to leaf
   - Branch-and-bound pruning eliminates all other subtrees immediately
   - Only O(log N) nodes (the tree height) need to be visited

2. **Minimal station processing:** Only n stations need to be examined and added to the priority queue, as the optimal path contains exactly the n nearest neighbors.

3. **Priority queue:** All n stations are inserted once, requiring O(n log n) total operations.

**Note:** This best case is theoretical and rarely occurs in practice. It requires perfect spatial alignment between query point and data distribution.

### Time Complexity — Average Case

**O(√N log n + k log k)**

**Component Breakdown:**

| Component | Average Case Complexity | Justification |
|-----------|------------------------|---------------|
| **Node visitation** | O(√N) | Average case: with balanced tree and typical spatial distribution, approximately O(√N) nodes are visited. This is the classical result for 2D KD-tree nearest neighbor search |
| **Stations processed** | O(√N) | Each visited node processes stations in its bucket. With typical bucket sizes O(1), total stations processed is O(√N) |
| **Distance calculations** | O(√N) | O(1) per station × O(√N) stations = O(√N) |
| **Priority queue operations** | O(√N log n) | Each of the O(√N) stations processed may require queue operations: O(√N) × O(log n) = O(√N log n) |
| **Final sorting** | O(k log k) | Sort k results, where k ≤ n |

**Detailed Justification:**

1. **Expected node visitation:** With balanced tree and typical spatial distribution, the expected number of nodes visited in 2D nearest neighbor search is O(√N). This comes from the geometric properties of KD-trees in 2D space.

2. **Station processing:** Each visited node processes stations in its bucket. With typical bucket sizes bounded by a constant, the total number of stations processed is O(√N).

3. **Priority queue operations:** For each station processed, priority queue operations (offer/poll) cost O(log n). Total: O(√N log n).

4. **Final sorting:** Results are sorted, requiring O(k log k) where k ≤ n.

**Note:** The √N factor is the expected number of nodes that must be examined in 2D nearest neighbor search with balanced KD-tree and typical data distribution.

### Time Complexity — Worst Case

**O(N log n + k log k)**

**Component Breakdown:**

| Component | Worst Case Complexity | Justification |
|-----------|----------------------|---------------|
| **Node visitation** | O(N) | Worst case: all nodes must be visited when pruning is ineffective (e.g., all stations very close to target, or query point near dataset center with poor spatial distribution) |
| **Stations processed** | O(N) | In worst case, all N stations are examined (each station's distance is calculated) |
| **Distance calculations** | O(N) | O(1) per station × O(N) stations = O(N) |
| **Priority queue operations** | O(N log n) | Each of the N stations processed may require: `offer()` O(log n) when queue size < n, or `poll()` + `offer()` O(log n) when replacing farthest element. In worst case, all N stations trigger queue updates |
| **Final sorting** | O(k log k) | Convert priority queue to list O(k) + sort k results O(k log k). Note: k ≤ n, but k may be less than n if fewer than n stations exist |

**Detailed Justification:**

1. **Tree traversal:** In the worst case, the branch-and-bound pruning fails to eliminate subtrees effectively. This occurs when:
   - All stations are very close to the target point
   - The query point is positioned such that the distance to split planes is always less than the current maximum distance in the queue
   - The KD-tree is degenerate (height O(N)) and pruning cannot skip branches

2. **Priority queue operations:** For each of the N stations processed:
   - If queue size < n: `offer()` operation costs O(log n)
   - If queue size = n and new station is closer: `poll()` O(log n) + `offer()` O(log n) = O(log n)
   - Total: O(N log n)

3. **Final sorting:** After traversal, the priority queue contains at most k = min(n, number of stations) elements. Converting to list is O(k), sorting is O(k log k). Since k ≤ n ≤ N, this term is dominated by O(N log n) when n is large, but must be included for completeness.

**Note:** Worst-case analysis requires O(N) to provide an upper bound, even though average case performance is much better with O(√N).

---

## Method 2: `radiusSearch(double targetLat, double targetLon, double radiusKm)`

### Description

Finds all stations within a specified radius of a target point using spatial pruning. Uses recursive tree traversal with distance-based pruning to avoid exploring subtrees that cannot contain stations within the radius.

### Algorithm Structure

1. **Initialization:** Create empty result list
2. **Recursive traversal:** Visit nodes using spatial pruning
3. **For each node visited:**
   - Process all stations in the bucket
   - For each station: calculate Haversine distance O(1)
   - If distance ≤ radius: add to results O(1) amortized
4. **Final sorting:** Sort results by distance: O(k log k) where k = number of results

### Time Complexity — Best Case

**O(log N + k log k)**

**Component Breakdown:**

| Component | Best Case Complexity | Justification |
|-----------|---------------------|---------------|
| **Node visitation** | O(log N) | Best case: radius is very small and query point is positioned such that spatial pruning eliminates all but one path from root to leaf. Only O(log N) nodes (tree height) are visited |
| **Stations processed** | O(k) | In best case, only k stations (those within radius) are examined along the optimal path |
| **Distance calculations** | O(k) | O(1) per station × O(k) stations = O(k) |
| **Result collection** | O(k) | Adding k results to list: O(1) amortized per insertion = O(k) |
| **Final sorting** | O(k log k) | Sort k results by distance using TimSort: O(k log k) |

**Detailed Justification:**

1. **Optimal pruning:** The best case occurs when:
   - The search radius is very small
   - All stations within the radius are located along a single path from root to leaf
   - Spatial pruning eliminates all other subtrees immediately
   - Only O(log N) nodes (the tree height) need to be visited

2. **Minimal station processing:** Only k stations (those within radius) need to be examined, as they are all located along the optimal path.

3. **Result collection and sorting:** k results are collected and sorted, requiring O(k log k).

**Note:** This best case is theoretical and requires perfect spatial alignment. In practice, even small radii typically require visiting more than O(log N) nodes.

### Time Complexity — Average Case

**O(√N + k log k)**

**Component Breakdown:**

| Component | Average Case Complexity | Justification |
|-----------|------------------------|---------------|
| **Node visitation** | O(√N) | Average case: with balanced tree and reasonable radius, approximately O(√N) nodes are visited. This is the classical result for 2D KD-tree radius search |
| **Stations processed** | O(√N) | Each visited node processes stations in its bucket. With typical bucket sizes O(1), total stations processed is O(√N) |
| **Distance calculations** | O(√N) | O(1) per station × O(√N) stations = O(√N) |
| **Result collection** | O(k) | Adding k results to list: O(1) amortized per insertion = O(k) |
| **Final sorting** | O(k log k) | Sort k results by distance using TimSort: O(k log k) |

**Detailed Justification:**

1. **Expected node visitation:** With balanced tree and typical radius size, the expected number of nodes visited in 2D radius search is O(√N). This comes from the geometric properties of KD-trees in 2D space.

2. **Station processing:** Each visited node processes stations in its bucket. With typical bucket sizes bounded by a constant, the total number of stations processed is O(√N).

3. **Result collection and sorting:** k results are collected and sorted, requiring O(k log k).

**Note:** The √N factor is the expected number of nodes that must be examined in 2D radius search with balanced KD-tree and typical data distribution.

### Time Complexity — Worst Case

**O(N + k log k)**

**Component Breakdown:**

| Component | Worst Case Complexity | Justification |
|-----------|----------------------|---------------|
| **Node visitation** | O(N) | Worst case: radius is very large (covers entire dataset) or spatial pruning is ineffective, requiring all N nodes to be visited |
| **Stations processed** | O(N) | In worst case, all N stations are examined (distance calculated for each) |
| **Distance calculations** | O(N) | O(1) per station × O(N) stations = O(N) |
| **Result collection** | O(k) | Adding k results to list: O(1) amortized per insertion = O(k) |
| **Final sorting** | O(k log k) | Sort k results by distance using TimSort: O(k log k) |

**Detailed Justification:**

1. **Tree traversal:** In worst case, spatial pruning fails to eliminate subtrees. This occurs when:
   - The search radius is very large (radius ≥ maximum distance in dataset), causing all nodes to be within or intersect the search region
   - The KD-tree is degenerate (height O(N)) and pruning cannot effectively skip branches
   - Query point and radius are positioned such that distance to split planes is always ≤ radius

2. **Station processing:** All N stations are examined, each requiring:
   - Haversine distance calculation: O(1)
   - Distance comparison: O(1)
   - Conditional list insertion: O(1) amortized
   - Total: O(N)

3. **Final sorting:** Results are sorted by distance. When k = N (all stations in radius), sorting dominates: O(N log N). However, the general form O(N + k log k) is more precise:
   - When k ≪ N: O(N) dominates
   - When k = N: O(N + N log N) = O(N log N)
   - When k = O(1): O(N) dominates

**Note:** Worst-case analysis requires O(N) to provide an upper bound, even though average case performance is much better with O(√N).

---

## Method 3: `haversineDistance(double lat1, double lon1, double lat2, double lon2)`

### Description

Calculates the great-circle distance between two points on Earth using the Haversine formula.

### Algorithm Classification

**Deterministic algorithm:** This method always executes the same sequence of operations for any input. No conditional branches affect the execution path in a data-dependent manner.

### Time Complexity

**O(1)**

**Justification:**

The Haversine formula involves a fixed sequence of operations regardless of input values:
- Constant number of arithmetic operations (addition, subtraction, multiplication, division)
- Constant number of trigonometric function calls (sin, cos, atan2, toRadians)
- No loops or recursive calls
- All operations are O(1)

The algorithm always performs the same number of operations for any valid input coordinates, making it deterministic with constant time complexity.

---

## Summary Tables

### Time Complexity

| Operation | Best Case | Average Case | Worst Case | Notes |
|-----------|-----------|--------------|------------|-------|
| **nearestN** | O(log N + n log n + k log k) | O(√N log n + k log k) | **O(N log n + k log k)** | k ≤ n ≤ N. Best case: optimal path. Worst case: all nodes visited |
| **radiusSearch** | O(log N + k log k) | O(√N + k log k) | **O(N + k log k)** | k ≤ N. Best case: optimal path. Worst case: large radius (k = N) → O(N log N) |
| **haversineDistance** | **O(1)** | - | - | Deterministic algorithm |

**Where:**
- N = total number of stations in the tree
- n = number of nearest neighbors requested (for `nearestN`)
- k = number of results returned (k ≤ n for `nearestN`, k ≤ N for `radiusSearch`)

---

## Comparison with Naive Approach

### Performance Comparison

| Approach | nearestN | radiusSearch | Notes |
|----------|----------|--------------|-------|
| **Naive (linear search)** | O(N) | O(N) | No spatial structure |
| **Naive with sorting** | O(N log N) | O(N log N) | Linear search + sort |
| **KD-tree (best case)** | O(log N + n log n + k log k) | O(log N + k log k) | Optimal pruning |
| **KD-tree (average)** | O(√N log n + k log k) | O(√N + k log k) | Typical performance |
| **KD-tree (worst case)** | O(N log n + k log k) | O(N + k log k) | Pruning ineffective |

### Advantages of KD-tree

- **For large datasets (N >> n, N >> k):** Significantly faster than linear search in average case
- **Spatial pruning:** Skips large portions of dataset guaranteed to be outside search region
- **Logarithmic overhead:** Priority queue and recursion overhead (O(log n), O(log N)) is negligible compared to time savings from pruning

### When KD-tree is Less Efficient

- **Very small datasets (N < 100):** Tree traversal overhead may outweigh benefits
- **When n ≈ N or k ≈ N:** Algorithm approaches O(N log N), similar to naive approach with sorting
- **Degenerate trees:** When tree height is O(N), traversal may approach O(N) even with pruning
- **Worst-case scenarios:** When pruning is ineffective, performance degrades to O(N log n) or O(N + k log k)

---

## Notes on Pruning Efficiency

The efficiency of both algorithms depends critically on spatial pruning quality:

1. **Balanced tree:** O(log N) height enables effective pruning. Degenerate trees (height O(N)) reduce pruning effectiveness
2. **Radius size (radiusSearch):** Smaller radii lead to better pruning. Very large radii (covering entire dataset) eliminate pruning benefits
3. **Query location:** Queries near dataset center or in dense regions may explore more nodes
4. **Station distribution:** Clustered distributions or pathological cases (all stations at same point) reduce pruning effectiveness
5. **Bucket sizes:** Large buckets (many stations with identical coordinates) increase processing time per node but do not affect asymptotic complexity if bucket sizes are bounded

---

## Notes on Filtering

When filters are applied to results (e.g., timezone filters, country filters):

| Component | Complexity | Notes |
|-----------|------------|-------|
| **Filtering step** | O(k) | Linear pass through k results, O(1) filter check per result |
| **Overall complexity** | Same as without filters | O(k) filtering is dominated by search operations O(N log n) or O(N) |

**Explanation:** Filtering adds O(k) which is already dominated by O(N log n) or O(N) terms in the search phase. The final sorting cost may be reduced from O(k log k) to O(m log m) where m ≤ k is the number of filtered results, but this optimization is implementation-dependent.

---

## Rigorous Analysis Notes

### Best-Case Scenarios

**For `nearestN`:**
- **Optimal path:** Query point positioned such that n nearest neighbors lie along a single root-to-leaf path
- **Perfect pruning:** Branch-and-bound eliminates all other subtrees immediately
- **Result:** Only O(log N) nodes visited

**For `radiusSearch`:**
- **Small radius, optimal path:** Very small radius with all matching stations along a single root-to-leaf path
- **Perfect pruning:** Spatial pruning eliminates all other subtrees immediately
- **Result:** Only O(log N) nodes visited

### Average-Case Assumptions

- **Balanced tree:** Height O(log N) achieved through median selection in bulk-build
- **Reasonable spatial distribution:** Stations distributed across geographic region, not all clustered
- **Typical query parameters:** n ≪ N for `nearestN`, radius small relative to dataset extent for `radiusSearch`
- **Bounded bucket sizes:** Most buckets contain O(1) stations, pathological case (all stations at one point) is rare

### Worst-Case Scenarios

**For `nearestN`:**
- **All stations very close to target:** Branch-and-bound cannot prune effectively, all N stations processed
- **Query point at dataset center with poor distribution:** Distance to split planes always less than current maximum distance
- **Degenerate tree:** Height O(N) prevents effective pruning

**For `radiusSearch`:**
- **Very large radius (radius ≥ max distance):** All nodes intersect search region, all N stations processed (k = N)
- **Degenerate tree:** Height O(N) reduces pruning effectiveness
- **Query point and radius covering entire dataset:** Spatial pruning cannot eliminate any branches

### Big-O Notation Justification

All complexity bounds use Big-O notation to provide upper bounds. For non-deterministic algorithms, best case provides a lower bound (best possible performance), average case provides expected performance, and worst case provides an upper bound (guaranteed performance). For deterministic algorithms, a single analysis suffices as all cases are identical.
