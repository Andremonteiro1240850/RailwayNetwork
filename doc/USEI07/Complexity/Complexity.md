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
| 2 | Filter valid stations | O(n) | Iterate over n stations, O(1) validation per station |
| 3 | Create list copies | O(n) | Two copies of references (not objects), O(n) |
| 4 | Sort by latitude | O(n log n) | TimSort with multi-key comparator: `comparingDouble(Station::getLatitude).thenComparingDouble(Station::getLongitude).thenComparing(Station::getName)`. Each comparison is O(1) (double + double + string with bounded length), but total number of comparisons is O(n log n) |
| 5 | Sort by longitude | O(n log n) | TimSort with multi-key comparator: `comparingDouble(Station::getLongitude).thenComparingDouble(Station::getLatitude).thenComparing(Station::getName)`. Identical analysis to step 4 |
| 6 | Tree construction (bulkBuild) | O(n log n) | Divide-and-conquer algorithm (see detailed analysis below) |
| 7 | Calculate metrics | O(n) | Single tree traversal to count nodes and stations |

#### Overall Time Complexity

**O(n log n)**

**Justification:** The complexity is dominated by the two sorting operations (O(n log n) each) and the bulk-build algorithm (O(n log n)). Even in pathological cases where all stations share the same coordinates, the bulk-build has cost O(n log n) (all stations go into a single bucket at the root, which is sorted in O(n log n), and recursion terminates immediately).

**Note on Multi-Key Comparators:** Although each individual comparison involves multiple keys (latitude, longitude, name), the cost per comparison remains O(1) because:
- `double` comparisons are O(1)
- `String` comparisons are O(min(s₁, s₂)) where s is length, but we assume bounded string lengths, resulting in O(1) per comparison
- The total number of comparisons in TimSort is O(n log n), maintaining global complexity

---

### Method: `buildTree()` (no parameters)

**Algorithm Type:** Deterministic

This method uses AVL trees from `StationRepository` and always executes the same sequence of operations.

#### Step-by-Step Time Complexity Analysis

| Step | Operation | Complexity | Justification |
|------|-----------|------------|---------------|
| 1 | Iterate over AVL trees | O(n) | Two iterations, each O(n) |
| 2 | Create HashSet for intersection | O(n) | HashSet with n elements |
| 3 | Set intersection (retainAll) | O(n) | O(n) operation on HashSet |
| 4 | Rebuild final lists | O(n) | Two iterations over AVL trees with O(1) checks |
| 5 | Sort and bulkBuild | O(n log n) | Identical to `buildTree(List)` method |

#### Overall Time Complexity

**O(n log n)** — dominated by sorting and bulkBuild operations

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

- **Median selection:** O(1) — index access
- **Partition `sortedByAxis`:** O(n) — linear loop
- **Create HashSet for `bucketCoords`:** O(b) where b is bucket size
- **Sort bucket by name:** O(b log b) where b ≤ n (assuming bounded string lengths, each comparison is O(1))
- **Partition `sortedByOther`:** O(n) — linear loop with HashSet check (O(1) amortized)
- **Total per level:** O(n) + O(b log b) = O(n) when b << n, or O(n log n) when b = n

In the pathological case where b = n (all stations in the same bucket):
- Bucket sorting: O(n log n)
- Recursion terminates immediately (empty partitions)
- **Total cost:** O(n log n)

#### Overall Time Complexity

**O(n log n)** — regardless of data distribution

---

## Non-Deterministic Algorithms

### Method: `rangeQuery(double latMin, double latMax, double lonMin, double lonMax)`

**Algorithm Type:** Non-deterministic

This method uses spatial pruning: some tree nodes may not be visited depending on whether the query rectangle intersects the node's spatial region.

#### Best Case Time Complexity

**O(log n + k log k)**

**Conditions:**
- Very small query rectangle
- Optimal spatial pruning (most branches pruned)
- Small number of results (k << n)
- Balanced tree (height O(log n))

**Analysis:**
- Tree traversal: O(log n) — visits only nodes along a single path from root to leaf
- Bucket checks: O(1) per visited node (assuming bounded bucket sizes)
- Final sorting: O(k log k) — sorts k results by name (necessary for output specification)
- **Total:** O(log n + k log k)

#### Average Case Time Complexity

**O(√n + k log k)**

**Conditions:**
- Balanced tree (height O(log n))
- Rectangular query that intersects approximately O(√n) spatial cells
- Effective spatial pruning
- Moderate number of results (k = O(√n))
- Bounded bucket sizes

**Analysis:**
- Tree traversal: O(√n) — spatial pruning eliminates most branches
- Bucket checks: O(√n) — checks buckets in visited nodes (assuming bounded bucket sizes)
- Final sorting: O(k log k) — sorts k results by name (necessary for output specification)
- **Total:** O(√n + k log k)

**Justification for O(√n):**
For a balanced 2D KD-tree, a rectangular query typically intersects O(√n) nodes when:
- The query rectangle is reasonably sized
- Spatial pruning effectively eliminates subtrees that cannot contain results
- The tree is well-balanced

#### Worst Case Time Complexity

**O(n + k log k)**

**Conditions:**
- Query rectangle covers the entire dataset, OR
- Tree is degenerate (height O(n)), OR
- Spatial pruning is ineffective (query intersects all nodes)

**Analysis:**
- Tree traversal: O(n) — all n nodes are visited
- Bucket checks: O(n) — checks all buckets in all nodes
- Final sorting: O(k log k) — sorts k results by name (necessary for output specification)
- **Total:** O(n + k log k)

**Degenerate Tree Case:**
When the tree degenerates to a linked list (height O(n)), spatial pruning becomes ineffective, and all n nodes must be visited, resulting in O(n) traversal cost.

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
- Tree traversal: O(m) — visits m stations (distributed across visited nodes)
- Distance calculations: O(m) — calculates Haversine distance (O(1) each) for m stations
- Priority queue operations: O(m log N) — m operations (insert/extract) on a max heap of size ≤ N
- Final sorting: O(N log N) — sorts N results by distance (necessary for output specification)

**Overall Complexity:** O(m + m log N + N log N) = **O(m log N + N log N)**

#### Variation of m (Number of Stations Visited)

The value of **m** varies depending on the effectiveness of branch-and-bound pruning:

| Case | m (Stations Visited) | Conditions |
|------|----------------------|------------|
| **Best Case** | m = O(log n) | Optimal branch-and-bound pruning, target point very close to N stations, most branches pruned early, balanced tree |
| **Average Case** | m = O(√n) | Balanced tree, moderate spatial distribution, effective branch-and-bound pruning |
| **Worst Case** | m = O(n) | All stations must be visited: tree is degenerate (height O(n)), OR branch-and-bound pruning is ineffective, OR target point requires exploring all nodes |

#### Best Case Time Complexity

**O(log n · log N + N log N)**

**Conditions:**
- Optimal branch-and-bound pruning (most branches pruned early)
- Target point is very close to N stations
- N nearest stations found quickly along a single path
- Balanced tree (height O(log n))
- Bounded bucket sizes

**Analysis:**
- Stations visited: m = O(log n)
- Tree traversal: O(log n)
- Distance calculations: O(log n) — Haversine O(1) each
- Priority queue operations: O(log n · log N)
- Final sorting: O(N log N) — necessary for output specification
- **Total:** O(log n · log N + N log N)

**Note:** When N << n, this simplifies to O(log n · log N + N log N). When N is constant, this becomes O(log n + N log N).

#### Average Case Time Complexity

**O(√n · log N + N log N)**

**Conditions:**
- Balanced tree (height O(log n))
- Moderate spatial distribution
- Effective branch-and-bound pruning
- N nearest stations distributed across multiple subtrees
- Bounded bucket sizes

**Analysis:**
- Stations visited: m = O(√n)
- Tree traversal: O(√n)
- Distance calculations: O(√n) — Haversine O(1) each
- Priority queue operations: O(√n · log N)
- Final sorting: O(N log N) — necessary for output specification
- **Total:** O(√n · log N + N log N)

**Justification for O(√n):**
For a balanced 2D KD-tree, branch-and-bound pruning typically results in visiting O(√n) stations when:
- The tree is well-balanced
- Spatial pruning effectively eliminates subtrees that cannot contain closer stations
- The N-th nearest distance is established reasonably quickly

#### Worst Case Time Complexity

**O(n log N + N log N)**

**Conditions:**
- Tree is degenerate (height O(n)), OR
- Branch-and-bound pruning is ineffective (all stations must be visited), OR
- Target point requires visiting all nodes

**Analysis:**
- Stations visited: m = O(n)
- Tree traversal: O(n)
- Distance calculations: O(n) — Haversine O(1) each
- Priority queue operations: O(n log N)
- Final sorting: O(N log N) — necessary for output specification
- **Total:** O(n log N + N log N)

**Degenerate Tree Case:**
When the tree degenerates to a linked list (height O(n)), branch-and-bound pruning becomes ineffective, and all n stations must be visited, resulting in m = n.

**Note:** When N = O(n), complexity becomes O(n log n), dominated by sorting.

---

### Method: `radiusSearch(double targetLat, double targetLon, double radiusKm)`

**Algorithm Type:** Non-deterministic

This method uses spatial pruning: some tree nodes may not be visited depending on whether they can contain stations within the search radius.

#### Best Case Time Complexity

**O(log n + k log k)**

**Conditions:**
- Very small search radius
- Optimal spatial pruning (most branches pruned)
- Small number of results (k << n)
- Balanced tree (height O(log n))
- Bounded bucket sizes

**Analysis:**
- Tree traversal: O(log n) — visits only nodes along a single path
- Distance calculations: O(k) — calculates Haversine distance (O(1) each) for k stations within radius
- Final sorting: O(k log k) — sorts k results by distance (necessary for output specification)
- **Total:** O(log n + k log k)

#### Average Case Time Complexity

**O(√n + k log k)**

**Conditions:**
- Balanced tree (height O(log n))
- Moderate search radius
- Effective spatial pruning
- Moderate number of results (k = O(√n))
- Bounded bucket sizes

**Analysis:**
- Tree traversal: O(√n) — spatial pruning eliminates most branches
- Distance calculations: O(k) — calculates Haversine distance (O(1) each) for k stations within radius
- Final sorting: O(k log k) — sorts k results by distance (necessary for output specification)
- **Total:** O(√n + k log k)

**Justification for O(√n):**
For a balanced 2D KD-tree with moderate radius, spatial pruning typically results in visiting O(√n) nodes when:
- The search radius is reasonably sized
- Spatial pruning effectively eliminates subtrees outside the radius
- The tree is well-balanced

#### Worst Case Time Complexity

**O(n + k log k)**

**Conditions:**
- Search radius is very large (covers entire dataset), OR
- Tree is degenerate (height O(n)), OR
- Spatial pruning is ineffective

**Analysis:**
- Tree traversal: O(n) — all n nodes are visited
- Distance calculations: O(n) — calculates Haversine distance (O(1) each) for all stations
- Final sorting: O(k log k) — sorts k results by distance (necessary for output specification)
- **Total:** O(n + k log k)

**Degenerate Tree Case:**
When the tree degenerates to a linked list (height O(n)), spatial pruning becomes ineffective, and all n nodes must be visited, resulting in O(n) traversal cost.

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
