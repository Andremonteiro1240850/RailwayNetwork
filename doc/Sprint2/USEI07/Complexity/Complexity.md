# USEI07 - Time Complexity Analysis

## Overview

This document provides a detailed and rigorous time complexity analysis for building and querying a balanced 2D-tree (KD-tree) on latitude/longitude coordinates, following academic principles of asymptotic analysis.

**Notation:**
- **n** = total number of stations (points) in the input/tree
- **k** = number of results returned by a query
- **N** = number of nearest neighbors requested (for nearest-N search)
- **m** = number of stations (points) visited during tree traversal
- **b** = bucket size (stations with identical coordinates)
- **h** = tree height (O(log n) balanced, O(n) degenerate)

**Algorithm Classification:**
- **Deterministic algorithms:** Always execute the same steps for the same input
- **Non-deterministic algorithms:** Have steps that may not be executed depending on values in data structures (e.g., spatial pruning, branch-and-bound)

**Assumptions:**
- **Balanced tree:** The KD-tree is balanced (height O(log n)) unless otherwise stated. The bulk-build algorithm uses median selection, ensuring balance in typical cases.
- **Bounded bucket sizes:** Bucket sizes are bounded by a constant or O(log n) in typical cases. In pathological cases, buckets can be O(n) when all stations share coordinates.
- **Bounded string lengths:** Station names have bounded length, making string comparisons O(1) in asymptotic analysis.
- **Haversine distance:** Calculation is O(1) constant time (constant number of arithmetic and trigonometric operations).
- **Final sorting:** Results are sorted by distance/name as required by the algorithm specification. This sorting step is necessary and cannot be avoided.

---

## Deterministic Algorithms

### Method: `buildTree(List<Station> stations)`

**Algorithm Type:** Deterministic

This method always executes the same sequence of operations for a given input.

#### Code Structure

1. Input validation (null/empty check)
2. Filter valid stations
3. Create list copies
4. Sort by latitude and longitude (with multi-key comparators)
5. Build tree via recursive bulk-build algorithm
6. Calculate metrics (optional, does not affect asymptotic complexity)

#### Step-by-Step Time Complexity Analysis

| Step | Operation | Complexity | Justification |
|------|-----------|------------|---------------|
| 1 | Null/empty validation | O(1) | Constant time checks |
| 2 | Filter valid stations | O(n) | Iterate over n stations |
| 3 | Create list copies | O(n) | Two copies of references |
| 4 | Sort by latitude | O(n log n) | TimSort with multi-key comparator |
| 5 | Sort by longitude | O(n log n) | TimSort with multi-key comparator |
| 6 | Tree construction (bulkBuild) | O(n log n) | Divide-and-conquer algorithm |
| 7 | Calculate metrics | O(n) | Single tree traversal |

#### Overall Time Complexity

**O(n log n)**

**Justification:** Dominated by two sorting operations (O(n log n) each) and bulk-build (O(n log n)). Even when all stations share coordinates, bucket sorting costs O(n log n).

---

### Method: `buildTree()` (no parameters)

**Algorithm Type:** Deterministic

This method uses AVL trees from `StationRepository` and always executes the same sequence of operations.

#### Step-by-Step Time Complexity Analysis

| Step | Operation | Complexity | Justification |
|------|-----------|------------|---------------|
| 1 | Iterate over AVL trees | O(n) | Two iterations |
| 2 | Create HashSet for intersection | O(n) | HashSet with n elements |
| 3 | Set intersection (retainAll) | O(n) | O(n) operation on HashSet |
| 4 | Rebuild final lists | O(n) | Two iterations over AVL trees |
| 5 | Sort and bulkBuild | O(n log n) | Identical to `buildTree(List)` method |

#### Overall Time Complexity

**O(n log n)** — dominated by sorting and bulkBuild

---

### Method: `bulkBuild` (Detailed Analysis)

**Algorithm Type:** Deterministic

The `bulkBuild` method implements a divide-and-conquer algorithm that:
1. Selects the median element from the current partition
2. Partitions stations into bucket (same coordinates), left, and right
3. Sorts the bucket by name
4. Partitions the other axis list (excluding bucket stations)
5. Recursively builds left and right subtrees

#### Recurrence Relation Analysis

**Recurrence:** T(n) = 2T(n/2) + O(n)

**Solution (Master Theorem, Case 2):** T(n) = Θ(n log n) = **O(n log n)**

#### Work per Level Analysis

- **Median selection:** O(1)
- **Partition `sortedByAxis`:** O(n)
- **Create HashSet for `bucketCoords`:** O(b) where b is bucket size
- **Sort bucket by name:** O(b log b) where b ≤ n
- **Partition `sortedByOther`:** O(n)
- **Total per level:** O(n) + O(b log b) = O(n) when b << n, or O(n log n) when b = n

In pathological case (b = n): bucket sorting O(n log n), recursion terminates immediately.

#### Overall Time Complexity

**O(n log n)** — regardless of data distribution

---

## Non-Deterministic Algorithms

### Method: `rangeQuery(double latMin, double latMax, double lonMin, double lonMax)`

**Algorithm Type:** Non-deterministic

This method uses spatial pruning: some tree nodes may not be visited depending on whether the query rectangle intersects the node's spatial region.

#### Best Case Time Complexity

**O(log n + k log k)**

**Conditions:** Very small query rectangle, optimal spatial pruning, small number of results (k << n), balanced tree.

**Analysis:**
- Tree traversal: O(log n) — single path from root to leaf
- Bucket checks: O(1) per visited node
- Final sorting: O(k log k) — sorts k results by name
- **Total:** O(log n + k log k)

#### Average Case Time Complexity

**O(√n + k log k)**

**Conditions:** Balanced tree, rectangular query intersecting O(√n) spatial cells, effective spatial pruning.

**Analysis:**
- Tree traversal: O(√n) — spatial pruning eliminates most branches
- Bucket checks: O(√n) — checks buckets in visited nodes
- Final sorting: O(k log k) — sorts k results by name
- **Total:** O(√n + k log k)

**Justification:** For balanced 2D KD-tree, rectangular queries typically intersect O(√n) nodes.

#### Worst Case Time Complexity

**O(n + k log k)**

**Conditions:** Query rectangle covers entire dataset, OR tree is degenerate (height O(n)), OR spatial pruning is ineffective.

**Analysis:**
- Tree traversal: O(n) — all n nodes are visited
- Bucket checks: O(n) — checks all buckets in all nodes
- Final sorting: O(k log k) — sorts k results by name
- **Total:** O(n + k log k)

**Note:** When k = n, complexity becomes O(n log n), dominated by sorting.

---

### Method: `nearestN(double targetLat, double targetLon, int N)`

**Algorithm Type:** Non-deterministic

This method uses branch-and-bound pruning: some tree nodes may not be visited depending on whether they can contain stations closer than the current N-th nearest.

#### Time Complexity Formulation

The complexity is expressed in terms of:
- **m** = number of stations visited during tree traversal
- **N** = number of nearest neighbors requested

**Component Breakdown:**
- Tree traversal: O(m)
- Distance calculations: O(m) — Haversine O(1) each
- Priority queue operations: O(m log N) — m operations on max heap of size ≤ N
- Final sorting: O(N log N) — sorts N results by distance

**Overall Complexity:** O(m + m log N + N log N) = **O(m log N + N log N)**

#### Variation of m (Number of Stations Visited)

| Case | m (Stations Visited) | Conditions |
|------|----------------------|------------|
| **Best Case** | m = O(log n) | Optimal branch-and-bound pruning, balanced tree |
| **Average Case** | m = O(√n) | Balanced tree, effective branch-and-bound pruning |
| **Worst Case** | m = O(n) | Degenerate tree OR ineffective pruning |

#### Best Case Time Complexity

**O(log n · log N + N log N)**

**Conditions:** Optimal branch-and-bound pruning, target point very close to N stations, balanced tree.

**Analysis:**
- Stations visited: m = O(log n)
- Tree traversal: O(log n)
- Distance calculations: O(log n)
- Priority queue operations: O(log n · log N)
- Final sorting: O(N log N)
- **Total:** O(log n · log N + N log N)

#### Average Case Time Complexity

**O(√n · log N + N log N)**

**Conditions:** Balanced tree, moderate spatial distribution, effective branch-and-bound pruning.

**Analysis:**
- Stations visited: m = O(√n)
- Tree traversal: O(√n)
- Distance calculations: O(√n)
- Priority queue operations: O(√n · log N)
- Final sorting: O(N log N)
- **Total:** O(√n · log N + N log N)

**Justification:** For balanced 2D KD-tree, branch-and-bound typically visits O(√n) stations.

#### Worst Case Time Complexity

**O(n log N + N log N)**

**Conditions:** Degenerate tree (height O(n)) OR ineffective branch-and-bound pruning.

**Analysis:**
- Stations visited: m = O(n)
- Tree traversal: O(n)
- Distance calculations: O(n)
- Priority queue operations: O(n log N)
- Final sorting: O(N log N)
- **Total:** O(n log N + N log N)

**Note:** When N = O(n), complexity becomes O(n log n), dominated by sorting.

---

### Method: `radiusSearch(double targetLat, double targetLon, double radiusKm)`

**Algorithm Type:** Non-deterministic

This method uses spatial pruning: some tree nodes may not be visited depending on whether they can contain stations within the search radius.

#### Best Case Time Complexity

**O(log n + k log k)**

**Conditions:** Very small search radius, optimal spatial pruning, small number of results (k << n), balanced tree.

**Analysis:**
- Tree traversal: O(log n) — single path from root to leaf
- Distance calculations: O(k) — Haversine O(1) each
- Final sorting: O(k log k) — sorts k results by distance
- **Total:** O(log n + k log k)

#### Average Case Time Complexity

**O(√n + k log k)**

**Conditions:** Balanced tree, moderate search radius, effective spatial pruning.

**Analysis:**
- Tree traversal: O(√n) — spatial pruning eliminates most branches
- Distance calculations: O(k) — Haversine O(1) each
- Final sorting: O(k log k) — sorts k results by distance
- **Total:** O(√n + k log k)

**Justification:** For balanced 2D KD-tree, spatial pruning typically visits O(√n) nodes.

#### Worst Case Time Complexity

**O(n + k log k)**

**Conditions:** Very large search radius (covers entire dataset) OR degenerate tree (height O(n)) OR ineffective spatial pruning.

**Analysis:**
- Tree traversal: O(n) — all n nodes are visited
- Distance calculations: O(n) — Haversine O(1) each
- Final sorting: O(k log k) — sorts k results by distance
- **Total:** O(n + k log k)

**Note:** When k = n, complexity becomes O(n log n), dominated by sorting.

---

## Summary Table

| Operation | Algorithm Type | Time Complexity |
|-----------|----------------|-----------------|
| **buildTree(List)** | Deterministic | O(n log n) |
| **buildTree()** | Deterministic | O(n log n) |
| **bulkBuild** | Deterministic | O(n log n) |
| **rangeQuery** | Non-deterministic | Best: O(log n + k log k)<br>Average: O(√n + k log k)<br>Worst: O(n + k log k) |
| **nearestN** | Non-deterministic | Best: O(log n · log N + N log N)<br>Average: O(√n · log N + N log N)<br>Worst: O(n log N + N log N) |
| **radiusSearch** | Non-deterministic | Best: O(log n + k log k)<br>Average: O(√n + k log k)<br>Worst: O(n + k log k) |

**Where:**
- n = total number of stations (points) in the tree
- k = number of results returned
- N = number of nearest neighbors requested
- m = number of stations visited (varies: O(log n) best, O(√n) average, O(n) worst)

---

## Degenerate Cases

### Degenerate Tree Impact

When the KD-tree degenerates to a linked list (height O(n)):

| Operation | Normal Case (Balanced) | Degenerate Case |
|-----------|------------------------|-----------------|
| **rangeQuery** | O(√n + k log k) average | O(n + k log k) |
| **nearestN** | O(√n · log N + N log N) average | O(n log N + N log N) |
| **radiusSearch** | O(√n + k log k) average | O(n + k log k) |

**Causes of Degeneration:**
- Pathological input data (e.g., all stations on a line)
- Unbalanced construction (though median selection in bulk-build prevents this in typical cases)
- Insertion-based construction without rebalancing

**Impact:**
- Spatial pruning becomes ineffective
- Branch-and-bound pruning becomes ineffective
- All n nodes must be visited
- Complexity degrades from O(√n) to O(n) for tree traversal
- For `nearestN`, m increases from O(√n) to O(n), increasing cost from O(√n · log N + N log N) to O(n log N + N log N)

---

## Technical Notes

1. **Multi-Key Comparators:** Although comparators use multiple keys (latitude, longitude, name), each comparison is O(1) assuming bounded string lengths. The total number of comparisons remains O(n log n) for sorting or O(k log k) / O(N log N) for result sorting.

2. **HashSet for Bucket Coordinates:** Creating HashSet for bucket coordinates is O(b) where b ≤ n. In worst case (b = n), it is O(n), but dominated by O(n log n) sorting.

3. **Bucket Sorting:** Each bucket is sorted by name in O(b log b) where b ≤ n. In worst case (b = n), this is O(n log n), but occurs only once at the root when all stations share coordinates. We assume bounded string lengths, making each comparison O(1).

4. **Spatial Pruning Effectiveness:** The O(√n) average case for queries assumes effective spatial pruning and a balanced tree. When pruning is ineffective or the tree is degenerate, complexity degrades to O(n).

5. **Branch-and-Bound Pruning:** The effectiveness of branch-and-bound in `nearestN` depends on how quickly the N-th nearest distance is established, allowing early pruning of distant subtrees. This effectiveness is reduced in degenerate trees.

6. **Haversine Distance:** The Haversine formula involves a constant number of arithmetic and trigonometric operations, making it O(1) in asymptotic analysis.

7. **Final Sorting Necessity:** Results are sorted by distance (and name as tiebreaker) as required by the algorithm specification. This sorting step is necessary and cannot be avoided, contributing O(k log k) or O(N log N) to the complexity.

8. **Bounded Bucket Sizes:** In typical cases, bucket sizes are bounded (O(1) or O(log n)). In pathological cases where all stations share coordinates, buckets can be O(n), but this is handled during tree construction and does not affect query complexity in typical scenarios.

---

## References and Applied Principles

This analysis follows principles established in:
- [GeeksforGeeks - Time and Space Complexity](https://www.geeksforgeeks.org/dsa/time-complexity-and-space-complexity/)
- [GeeksforGeeks - Worst, Average and Best Case Analysis](https://www.geeksforgeeks.org/dsa/worst-average-and-best-case-analysis-of-algorithms/)

**Applied Principles:**
- Worst case analysis for deterministic algorithms to guarantee upper bounds
- Best, average, and worst case analysis for non-deterministic algorithms
- Consideration of all operations, including auxiliary structures
- Rigorous recursion analysis using Master Theorem
- Detailed justification of each complexity component
- Clear distinction between deterministic and non-deterministic algorithms
- Explicit formulation in terms of visited stations (m) and requested neighbors (N)
- Explicit statement of assumptions (balanced tree, bounded buckets, bounded strings, O(1) Haversine, necessary sorting)
- Explicit identification of degenerate cases and their impact
