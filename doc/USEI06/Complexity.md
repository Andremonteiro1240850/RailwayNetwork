# USEI06 - Complexity

 ```java 
     public void populateTrees(List<Station> stations) {
        Comparator<Station> latitudeComparator = Comparator.comparingDouble(Station::getLatitude);
        Comparator<Station> longitudeComparator = Comparator.comparingDouble(Station::getLongitude);
        Comparator<Station> groupCountryComparator =Comparator
                .comparing(Station::getTimeZoneGroup)
                .thenComparing(Station::getCountry)
                .thenComparing(Station::getName);

        latitudeTree = new AVL<>(latitudeComparator);
        longitudeTree = new AVL<>(longitudeComparator);
        groupCountryTree = new AVL<>(groupCountryComparator);

        for (Station station : stations) {
            if(StationValidator.isValidStation(station)) {
                latitudeTree.insert(station);
                longitudeTree.insert(station);
                groupCountryTree.insert(station);
            }
        }
    }
```    
**Time Complexity Analysis:**
```text
    Comparator<Station> latitudeComparator = Comparator.comparingDouble(Station::getLatitude);
    Comparator<Station> longitudeComparator = Comparator.comparingDouble(Station::getLongitude);
    Comparator<Station> groupCountryComparator =Comparator
            .comparing(Station::getTimeZoneGroup)
            .thenComparing(Station::getCountry)
            .thenComparing(Station::getName);
```
- Complexity: O(1)
- Explanation: Creating a comparator is a constant time operation.

```text
    latitudeTree = new AVL<>(latitudeComparator);
    longitudeTree = new AVL<>(longitudeComparator);
    groupCountryTree = new AVL<>(groupCountryComparator);
```
- Complexity: O(1)
- Explanation: Initializing an empty AVL tree is a constant time operation.

```text
    for (Station station : stations) {
        if(StationValidator.isValidStation(station)) {
            latitudeTree.insert(station);
            longitudeTree.insert(station);
            groupCountryTree.insert(station);
        }
    }
```

- Complexity: O(n log n)
- Explanation: The loop iterates over each of the n stations. For each valid station, it performs three insertions into AVL trees, each taking O(log n) time. Therefore, the overall complexity for this part is O(n log n).
 

```java
    private void collectByTimeZone(BST.Node<Station> node, String timeZone, List<Station> result) {
        if (node == null) return;
    
        String nodeTimeZone = node.getElement().getTimeZoneGroup();
        int cmp = nodeTimeZone.compareTo(timeZone);
    
        // 1. Ir para a esquerda
        if (cmp >= 0) {
            collectByTimeZone(node.getLeft(), timeZone, result);
        }
    
        // 2. Adicionar nó
        if (cmp == 0) {
            result.add(node.getElement());
        }
    
        // 3. Ir para a direita
        if (cmp <= 0) {
            collectByTimeZone(node.getRight(), timeZone, result);
        }
    }
```
**Time Complexity Analysis:**
```text
    if (node == null) return;
```
- Complexity: O(1)
- Explanation: This is a base case check that takes constant time.

```text
    String nodeTimeZone = node.getElement().getTimeZoneGroup();
```
- Complexity: O(1)
- Explanation: Accessing the element and retrieving its time zone group is a constant time operation.

```text
    int cmp = nodeTimeZone.compareTo(timeZone);
```
- Complexity: O(m)
- Explanation: Comparing two strings takes O(m) time, where m is the length of the shorter string.

```text
    if (cmp >= 0) {
        collectByTimeZone(node.getLeft(), timeZone, result);
    }
```
- Complexity: O(n * m)
- Explanation: In the worst case, we may traverse all the n stations of the tree. Each comparison takes O(m) time, leading to a total of O(n * m).

```text
    if (cmp == 0) {
        result.add(node.getElement());
    }
```
- Complexity: O(1)
- Explanation: Adding an element to a list is a constant time operation.

```text
    if (cmp <= 0) {
        collectByTimeZone(node.getRight(), timeZone, result);
    }
```
- Complexity: O(n * m)
- Explanation: Similar to the left subtree traversal, in the worst case, we may traverse all the n stations of the tree, with each comparison taking O(m) time, leading to a total of O(h * m).

**Overall Time Complexity: O(n * m)**
**Explanation: The overall time complexity is dominated by the tree traversal, which in the worst case can go to the n stations of the tree, with each comparison taking O(m) time. Thus, the total complexity is O(n * m).**


```java 
    public List<Station> getStationsByCountry(String country) {
        List<Station> result = new ArrayList<>();
        collectByCountry(groupCountryTree.getRoot(), country, result);
        return result;
    }
   ```
**Time Complexity Analysis:**

```text
    List<Station> result = new ArrayList<>();
```
- Complexity: O(1)
- Explanation: Initializing an empty list is a constant time operation.

```text
    collectByCountry(groupCountryTree.getRoot(), country, result);
```
- Complexity: O(n * m)
- Explanation: In the worst case, we may traverse all the n stations of the tree. Each comparison takes O(k) time, where m is the length of the country string, leading to a total of O(n * m).

**Overall Time Complexity: O(n * m)**
**Explanation: The overall time complexity is dominated by the tree traversal, which in the worst case can go to the n stations of the tree, with each comparison taking O(m) time. Thus, the total complexity is O(n * m).**

```java
    public  List<Station> getStationsByCountry(String country) {
    List<Station> stationsByCountry = new ArrayList<>();
    for(Station station : groupCountryTree.inOrder()) {
        if(station.getCountry().equals(country)) {
            stationsByCountry.add(station);
        }
    }
    return stationsByCountry;
}
```
**Time Complexity Analysis:**
```text
    List<Station> stationsByCountry = new ArrayList<>();
```
- Complexity: O(1)
- Explanation: Initializing an empty list is a constant time operation.

```text
    for(Station station : groupCountryTree.inOrder()) {
        if(station.getCountry().equals(country)) {
            stationsByCountry.add(station);
        }
    }
```
- Complexity: O(n * m)
- Explanation: The in-order traversal visits each of the n stations in the tree. For each station, we perform a string comparison which takes O(m) time, where m is the length of the country string. Therefore, the overall complexity for this part is O(n * m).

**Overall Time Complexity: O(n * m)**
**Explanation: The overall time complexity is dominated by the in-order traversal of the tree, which visits each of the n stations, with each string comparison taking O(m) time. Thus, the total complexity is O(n * m).**

```java
    private void collectByLatitude(BST.Node<Station> node, double min, double max, List<Station> result) {
    if (node == null) return;

    if (node.getElement().getLatitude() > min)
        collectByLatitude(node.getLeft(), min, max, result);

    double lat = node.getElement().getLatitude();
    if (lat >= min && lat <= max)
        result.add(node.getElement());

    if (node.getElement().getLatitude() < max)
        collectByLatitude(node.getRight(), min, max, result);
}
```
**Time Complexity Analysis:**

```text
    if (node == null) return;
```
- Complexity: O(1)
- Explanation: This is a base case check that takes constant time.

```text
    if (node.getElement().getLatitude() > min)
        collectByLatitude(node.getLeft(), min, max, result);
```
- Complexity: O(n)
- Explanation: In the worst case, we may traverse all the n stations of the tree.

```text
    double lat = node.getElement().getLatitude();
```
- Complexity: O(1)
- Explanation: Accessing the element and retrieving its latitude is a constant time operation.

```text
    if (lat >= min && lat <= max)
        result.add(node.getElement());
```
- Complexity: O(1)
- Explanation: Checking the latitude range and adding an element to a list are constant time operations.

```text
    if (node.getElement().getLatitude() < max)
        collectByLatitude(node.getRight(), min, max, result);
```
- Complexity: O(n)
- Explanation: Similar to the left subtree traversal, in the worst case, we may traverse all the n stations of the tree.

**Overall Time Complexity: O(n)**
**Explanation: The overall time complexity is dominated by the tree traversal, which in the worst case can go to the n stations of the tree. Thus, the total complexity is O(n).**

```java 
    public List<Station> getStationsByLatitude(double min, double max) {
        List<Station> result = new ArrayList<>();
        collectByLatitude(latitudeTree.getRoot(), min, max, result);
        return result;
    }
```
**Time Complexity Analysis:**

```text
    List<Station> result = new ArrayList<>();
```
- Complexity: O(1)
- Explanation: Initializing an empty list is a constant time operation.

```text
    collectByLatitude(latitudeTree.getRoot(), min, max, result);
```
- Complexity: O(n)
- Explanation: In the worst case, we may traverse all the n stations of the tree.

**Overall Time Complexity: O(n)**
**Explanation: The overall time complexity is dominated by the tree traversal, which in the worst case can go to the n stations of the tree. Thus, the total complexity is O(n).**

```java
    private void collectByLongitude(BST.Node<Station> node, double min, double max, List<Station> result) {
        if (node == null) return;

        double lon = node.getElement().getLongitude();

        if (lon > min)
            collectByLongitude(node.getLeft(), min, max, result);

        if (lon >= min && lon <= max)
            result.add(node.getElement());

        if (lon < max)
            collectByLongitude(node.getRight(), min, max, result);
    }
``` 
**Time Complexity Analysis:**

```text
    if (node == null) return;
```
- Complexity: O(1)
- Explanation: This is a base case check that takes constant time.

```text
    double lon = node.getElement().getLongitude();
```
- Complexity: O(1)
- Explanation: Accessing the element and retrieving its longitude is a constant time operation.

```text
    if (lon > min)
        collectByLongitude(node.getLeft(), min, max, result);
```
- Complexity: O(n)
- Explanation: In the worst case, we may traverse all the n stations of the tree.

```text
    if (lon >= min && lon <= max)
        result.add(node.getElement());
```
- Complexity: O(1)
- Explanation: Checking the longitude range and adding an element to a list are constant time operations.

```text
    if (lon < max)
        collectByLongitude(node.getRight(), min, max, result);
```
- Complexity: O(n)
- Explanation: Similar to the left subtree traversal, in the worst case, we may traverse all the n stations of the tree.

**Overall Time Complexity: O(n)**
**Explanation: The overall time complexity is dominated by the tree traversal, which in the worst case can go to the n stations of the tree. Thus, the total complexity is O(n).**

```java 
    public List<Station> getStationsByLongitude(double min, double max) {
        List<Station> result = new ArrayList<>();
        collectByLongitude(longitudeTree.getRoot(), min, max, result);
        return result;
    }
```
**Time Complexity Analysis:**

```text
    List<Station> result = new ArrayList<>();
```
- Complexity: O(1)
- Explanation: Initializing an empty list is a constant time operation.

```text
    collectByLongitude(longitudeTree.getRoot(), min, max, result);
```
- Complexity: O(n)
  - Explanation: In the worst case, we may traverse all the n stations of the tree.

**Overall Time Complexity: O(n)**
**Explanation: The overall time complexity is dominated by the tree traversal, which in the worst case can go to the n stations of the tree. Thus, the total complexity is O(n).**


