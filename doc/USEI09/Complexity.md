# USEI09 - Complexity

This document provides a detailed complexity analysis for the Nearest Neighbor Search and Radius Search operations implemented on the KD-tree.

## `KDTree.nearestN(double targetLat, double targetLon, int n)`

This method finds the N nearest stations to a target point using a branch-and-bound algorithm with spatial pruning.

```java
public List<StationDistance> nearestN(double targetLat, double targetLon, int n) {
    // ... (validation) ...
    
    PriorityQueue<StationDistance> nearestQueue = new PriorityQueue<>(...);
    nearestNRecursive(root, targetLat, targetLon, n, 0, nearestQueue);
    
    // ... (sort and return) ...
}
```

**Time Complexity Analysis:**

- **Initialization**: O(1)
  - Explanation: Creating the priority queue and validating parameters takes constant time.

- **Recursive traversal (`nearestNRecursive`)**: O(√N + N log k) average case, O(N log k) worst case
  - Explanation: 
    - Let N = total number of stations in the tree, k = parameter n (number of nearest neighbors desired)
    - The algorithm uses branch-and-bound pruning to avoid exploring unnecessary subtrees.
    - In the average case, with a balanced KD-tree, the algorithm explores approximately O(√N) nodes.
    - For each node visited, it processes all stations in the bucket. For each station, it:
      - Calculates the Haversine distance: O(1)
      - Performs priority queue operations (offer/poll): O(log k) where k is the maximum size of the priority queue (the parameter n)
    - The total number of stations processed across all visited nodes is at most N (each station appears in exactly one bucket).
    - Therefore, the total cost of priority queue operations during traversal is O(N log k), where each of the N stations may require O(log k) operations on the priority queue.
    - In the worst case (unbalanced tree or all stations very close), it may need to visit O(N) nodes, but the total stations processed remains O(N).

- **Final sorting**: O(k log k) where k is the parameter n (number of nearest neighbors)
  - Explanation: Sorting the final list of k nearest stations by distance takes O(k log k) time.

**Overall Time Complexity for `nearestN`**: O(√N + N log k + k log k) average case, O(N log k + k log k) worst case
- **Explanation**: Let N = total number of stations in the tree, k = parameter n (number of nearest neighbors desired)
  - The dominant operations are:
    - Tree traversal: O(√N) nodes visited in average case, O(N) in worst case
    - Priority queue operations during traversal: O(N log k) (processing N stations, each with O(log k) operations on a priority queue of size at most k)
    - Final sorting: O(k log k)
  - In the average case, the O(N log k) term dominates since N >> k typically.
  - In the worst case, the complexity is O(N log k + k log k). Since k ≤ N, this simplifies to O(N log k) when k << N, or O(N log N) when k = N.

**Space Complexity Analysis:**

- **Priority queue**: O(k) where k is the parameter n (number of nearest neighbors)
  - Explanation: The priority queue stores at most k nearest stations.

- **Recursion stack**: O(log N) where N is the total number of stations
  - Explanation: The maximum depth of recursion is proportional to the height of the tree, which is O(log N) for a balanced tree.

- **Result list**: O(k)
  - Explanation: The final sorted list contains k stations.

**Overall Space Complexity for `nearestN`**: O(k + log N)
- **Explanation**: The space is dominated by the priority queue (O(k)) and recursion stack (O(log N)), where N is the total number of stations and k is the parameter (number of nearest neighbors desired).

## `KDTree.radiusSearch(double targetLat, double targetLon, double radiusKm)`

This method finds all stations within a specified radius of a target point using spatial pruning.

```java
public List<StationDistance> radiusSearch(double targetLat, double targetLon, double radiusKm) {
    // ... (validation) ...
    
    List<StationDistance> results = new ArrayList<>();
    radiusSearchRecursive(root, targetLat, targetLon, radiusKm, 0, results);
    
    // ... (sort and return) ...
}
```

**Time Complexity Analysis:**

- **Initialization**: O(1)
  - Explanation: Creating the results list and validating parameters takes constant time.

- **Recursive traversal (`radiusSearchRecursive`)**: O(√N + N) average case, O(N) worst case
  - Explanation:
    - Let N = total number of stations in the tree, k = number of stations within radius (results)
    - The algorithm uses spatial pruning to avoid exploring subtrees that cannot contain stations within the radius.
    - In the average case, with a balanced KD-tree and reasonable radius, the algorithm explores approximately O(√N) nodes.
    - For each node visited, it processes all stations in the bucket. For each station, it:
      - Calculates the Haversine distance: O(1)
      - Conditionally adds to results list: O(1) amortized
    - The total number of stations processed across all visited nodes is at most N (each station appears in exactly one bucket).
    - Therefore, the total cost of processing stations during traversal is O(N).
    - The number of results k depends on the radius and station density, but k ≤ N.
    - In the worst case (very large radius or all stations within radius), it may need to visit O(N) nodes, but the total stations processed remains O(N).

- **Final sorting**: O(k log k) where k is the number of results
  - Explanation: Sorting the final list of k stations by distance takes O(k log k) time.

**Overall Time Complexity for `radiusSearch`**: O(√N + N + k log k) average case, O(N + k log k) worst case
- **Explanation**: Let N = total number of stations in the tree, k = number of stations within radius (results)
  - The dominant operations are:
    - Tree traversal: O(√N) nodes visited in average case, O(N) in worst case
    - Station processing during traversal: O(N) (processing N stations, each with O(1) operations)
    - Final sorting: O(k log k)
  - In the average case, the O(N) term dominates the tree traversal cost.
  - In the worst case, the complexity is O(N + k log k). Since k ≤ N, this simplifies to O(N) when k << N, or O(N log N) when k = N (dominated by the sorting operation).

**Space Complexity Analysis:**

- **Results list**: O(k) where k is the number of stations within radius
  - Explanation: The list stores all stations found within the radius.

- **Recursion stack**: O(log N) where N is the total number of stations
  - Explanation: The maximum depth of recursion is proportional to the height of the tree, which is O(log N) for a balanced tree.

**Overall Space Complexity for `radiusSearch`**: O(k + log N)
- **Explanation**: The space is dominated by the results list (O(k)) and recursion stack (O(log N)), where N is the total number of stations and k is the number of results.

## `KDTree.haversineDistance(double lat1, double lon1, double lat2, double lon2)`

This static method calculates the great-circle distance between two points on Earth using the Haversine formula.

**Time Complexity**: O(1)
- **Explanation**: The calculation involves a constant number of arithmetic operations (sine, cosine, square root, etc.).

**Space Complexity**: O(1)
- **Explanation**: Only a constant amount of memory is used for intermediate calculations.

## Comparison with Naive Approach

**Naive Approach (linear search):**
- **Time Complexity**: O(N) for both nearest neighbor and radius search, where N is the total number of stations
- **Space Complexity**: O(k) for results, where k is the number of results

**KD-tree Approach:**
- **Time Complexity**: 
  - `nearestN`: O(√N + N log k + k log k) average case, O(N log k + k log k) worst case
    - Where N = total number of stations, k = parameter n (number of nearest neighbors desired)
  - `radiusSearch`: O(√N + N + k log k) average case, O(N + k log k) worst case
    - Where N = total number of stations, k = number of stations within radius (results)
- **Space Complexity**: O(k + log N)
  - Where N = total number of stations, k = number of results

**Advantages:**
- For large datasets (N >> k), the KD-tree approach is significantly faster than linear search.
- Spatial pruning allows skipping large portions of the dataset that are guaranteed to be outside the search region.
- The logarithmic space overhead is negligible compared to the time savings.

**When KD-tree is less efficient:**
- For very small datasets (N < 100), the overhead of tree traversal may outweigh the benefits.
- When k ≈ N (e.g., finding all nearest neighbors or very large radius), the algorithm approaches O(N log N) complexity, dominated by the sorting operation, which is similar to the naive approach with sorting.

## Notes on Pruning Efficiency

The efficiency of both algorithms depends on the quality of spatial pruning:

1. **Balanced tree**: A balanced KD-tree (as built in USEI07) ensures O(log n) height, enabling effective pruning.

2. **Radius size**: For `radiusSearch`, smaller radii lead to better pruning (fewer nodes explored).

3. **Query point location**: Queries near the center of the dataset distribution may explore more nodes than queries near the edges.

4. **Station distribution**: Clustered distributions may reduce pruning effectiveness compared to uniform distributions.

## Notes on Filtering

When filters are applied to the results (e.g., timezone filters), the complexity analysis remains the same:

- **Filtering step**: O(k) where k is the number of results from the KD-tree search
  - Explanation: Filtering is a linear pass through the k results, checking each station against the filter criteria.
  - This adds O(k) to the overall complexity, which is already dominated by the O(N log k) or O(N) terms in the search phase.

- **Overall complexity with filters**: The same as without filters, since O(k) is dominated by the search operations.

