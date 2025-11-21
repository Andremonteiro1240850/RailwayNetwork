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

- **Recursive traversal (`nearestNRecursive`)**: O(√n + n log k) average case, O(n log k) worst case
  - Explanation: 
    - The algorithm uses branch-and-bound pruning to avoid exploring unnecessary subtrees.
    - In the average case, with a balanced KD-tree, the algorithm explores approximately O(√n) nodes.
    - For each node visited, it processes all stations in the bucket. For each station, it:
      - Calculates the Haversine distance: O(1)
      - Performs priority queue operations (offer/poll): O(log k) where k ≤ n is the maximum size of the priority queue
    - The total number of stations processed across all visited nodes is at most n (each station appears in exactly one bucket).
    - Therefore, the total cost of priority queue operations during traversal is O(n log k).
    - In the worst case (unbalanced tree or all stations very close), it may need to visit O(n) nodes, but the total stations processed remains O(n).

- **Final sorting**: O(k log k) where k ≤ n
  - Explanation: Sorting the final list of k nearest stations by distance takes O(k log k) time.

**Overall Time Complexity for `nearestN`**: O(√n + n log k + k log k) average case, O(n log k + k log k) worst case
- **Explanation**: The dominant operations are:
  - Tree traversal: O(√n) nodes visited in average case, O(n) in worst case
  - Priority queue operations during traversal: O(n log k) (processing n stations, each with O(log k) operations)
  - Final sorting: O(k log k)
- **Simplified**: Since k ≤ n and typically k << n, the complexity is dominated by O(n log k) in practice. In the worst case where k = n, this becomes O(n log n).

**Space Complexity Analysis:**

- **Priority queue**: O(k) where k ≤ n
  - Explanation: The priority queue stores at most k nearest stations.

- **Recursion stack**: O(log n)
  - Explanation: The maximum depth of recursion is proportional to the height of the tree, which is O(log n) for a balanced tree.

- **Result list**: O(k)
  - Explanation: The final sorted list contains k stations.

**Overall Space Complexity for `nearestN`**: O(k + log n)
- **Explanation**: The space is dominated by the priority queue and recursion stack.

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

- **Recursive traversal (`radiusSearchRecursive`)**: O(√n + n) average case, O(n) worst case
  - Explanation:
    - The algorithm uses spatial pruning to avoid exploring subtrees that cannot contain stations within the radius.
    - In the average case, with a balanced KD-tree and reasonable radius, the algorithm explores approximately O(√n) nodes.
    - For each node visited, it processes all stations in the bucket. For each station, it:
      - Calculates the Haversine distance: O(1)
      - Conditionally adds to results list: O(1) amortized
    - The total number of stations processed across all visited nodes is at most n (each station appears in exactly one bucket).
    - Therefore, the total cost of processing stations during traversal is O(n).
    - The number of results k depends on the radius and station density, but k ≤ n.
    - In the worst case (very large radius or all stations within radius), it may need to visit O(n) nodes, but the total stations processed remains O(n).

- **Final sorting**: O(k log k) where k is the number of results
  - Explanation: Sorting the final list of k stations by distance takes O(k log k) time.

**Overall Time Complexity for `radiusSearch`**: O(√n + n + k log k) average case, O(n + k log k) worst case
- **Explanation**: The dominant operations are:
  - Tree traversal: O(√n) nodes visited in average case, O(n) in worst case
  - Station processing during traversal: O(n) (processing n stations, each with O(1) operations)
  - Final sorting: O(k log k)
- **Simplified**: Since the tree traversal and station processing are both O(n) in the worst case, and k ≤ n, the complexity is O(n + k log k). In the worst case where k = n, this becomes O(n log n), dominated by the sorting operation. This is scientifically precise.

**Space Complexity Analysis:**

- **Results list**: O(k) where k is the number of stations within radius
  - Explanation: The list stores all stations found within the radius.

- **Recursion stack**: O(log n)
  - Explanation: The maximum depth of recursion is proportional to the height of the tree, which is O(log n) for a balanced tree.

**Overall Space Complexity for `radiusSearch`**: O(k + log n)
- **Explanation**: The space is dominated by the results list and recursion stack.

## `KDTree.haversineDistance(double lat1, double lon1, double lat2, double lon2)`

This static method calculates the great-circle distance between two points on Earth using the Haversine formula.

**Time Complexity**: O(1)
- **Explanation**: The calculation involves a constant number of arithmetic operations (sine, cosine, square root, etc.).

**Space Complexity**: O(1)
- **Explanation**: Only a constant amount of memory is used for intermediate calculations.

## Comparison with Naive Approach

**Naive Approach (linear search):**
- **Time Complexity**: O(n) for both nearest neighbor and radius search
- **Space Complexity**: O(k) for results

**KD-tree Approach:**
- **Time Complexity**: 
  - `nearestN`: O(√n + n log k + k log k) average case, O(n log k + k log k) worst case
  - `radiusSearch`: O(√n + n + k log k) average case, O(n + k log k) worst case
- **Space Complexity**: O(k + log n)

**Advantages:**
- For large datasets (n >> k), the KD-tree approach is significantly faster than linear search.
- Spatial pruning allows skipping large portions of the dataset that are guaranteed to be outside the search region.
- The logarithmic space overhead is negligible compared to the time savings.

**When KD-tree is less efficient:**
- For very small datasets (n < 100), the overhead of tree traversal may outweigh the benefits.
- When k ≈ n (e.g., finding all nearest neighbors or very large radius), the algorithm approaches O(n log n) complexity, dominated by the sorting operation, which is similar to the naive approach with sorting.

## Notes on Pruning Efficiency

The efficiency of both algorithms depends on the quality of spatial pruning:

1. **Balanced tree**: A balanced KD-tree (as built in USEI07) ensures O(log n) height, enabling effective pruning.

2. **Radius size**: For `radiusSearch`, smaller radii lead to better pruning (fewer nodes explored).

3. **Query point location**: Queries near the center of the dataset distribution may explore more nodes than queries near the edges.

4. **Station distribution**: Clustered distributions may reduce pruning effectiveness compared to uniform distributions.

