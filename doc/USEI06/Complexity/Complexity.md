# Análise de Complexidade

---

## 1. populateTrees(List<Station> stations)

### Código

```java
   public void populateTrees(List<Station> stations) {
       Comparator<Station> latitudeComparator = Comparator.comparingDouble(Station::getLatitude);
       Comparator<Station> longitudeComparator = Comparator.comparingDouble(Station::getLongitude);
       Comparator<Station> groupCountryComparator = Comparator
       .comparing(Station::getTimeZoneGroup)
       .thenComparing(Station::getCountry)
       .thenComparing(Station::getName);

       latitudeTree = new AVL<>(latitudeComparator);
       longitudeTree = new AVL<>(longitudeComparator);
       groupCountryTree = new AVL<>(groupCountryComparator);

       for (Station station : stations) {
           if (StationValidator.isValidStation(station)) {
               latitudeTree.insert(station);
               longitudeTree.insert(station);
               groupCountryTree.insert(station);
           }
       }
    }
```

### Time Complexity (Pior Caso)

- **Criação dos comparadores**: O(1)
- **Inicialização das árvores AVL**: O(1)
- **Ciclo de inserção**:
  - Cada inserção em AVL custa O(log n)
  - Três inserções por estação válida
  - No pior caso, todas as estações são válidas

**Complexidade total**: O(n log n)

### Space Complexity (Pior Caso)

- Três árvores AVL contendo até n elementos cada: O(n)
- Espaço auxiliar é constante

**Complexidade espacial total**: O(n)

---

## 2. collectByTimeZone(...)

### Código

```java
   private void collectByTimeZone(BST.Node<Station> node, String timeZone, List<Station> result) {
       if (node == null) return;
    
       String nodeTimeZone = node.getElement().getTimeZoneGroup();
       int cmp = nodeTimeZone.compareTo(timeZone);
    
       if (cmp >= 0)
       collectByTimeZone(node.getLeft(), timeZone, result);
    
       if (cmp == 0)
       result.add(node.getElement());
    
       if (cmp <= 0)
       collectByTimeZone(node.getRight(), timeZone, result);
   }
```

### Time Complexity (Pior Caso)

- A lógica permite explorar ambos os ramos da árvore
- No pior caso, todos os nós são visitados: O(n)
- Cada comparação de strings custa O(m) (m = tamanho da string)

**Complexidade total**: O(n · m)

### Space Complexity (Pior Caso)

- Profundidade de recursão: até O(log n) por causa da árvore balanceada
- A lista resultante pode armazenar até n elementos

**Complexidade espacial**: O(n)

---

## 3. getStationsByTimeZone(...)

### Código

```java
   public List<Station> getStationsByTimeZone(String timeZone) {
       List<Station> result = new ArrayList<>();
       collectByTimeZone(groupCountryTree.getRoot(), timeZone, result);
       return result;
   }
```

### Time Complexity

**O(n · m)**

### Space Complexity

**O(n)**

---

## 4. getStationsByCountry(...)

### Código

```java
   public List<Station> getStationsByCountry(String country) {
       List<Station> stationsByCountry = new ArrayList<>();
       for (Station station : groupCountryTree.inOrder()) {
           if (station.getCountry().equals(country)) {
               stationsByCountry.add(station);
           }
       }
       return stationsByCountry;
   }
```

### Time Complexity

- In-order traversal: O(n)
- Comparação de strings: O(m)

**Complexidade total**: O(n · m)

### Space Complexity

- inOrder() gera lista com n elementos → O(n)
- Lista resultante também pode ter até n elementos

**Complexidade espacial**: O(n)

---

## 5. collectByLatitude(...) e collectByLongitude(...)

Estas duas funções são estruturalmente equivalentes.

### Código (Exemplo com collectByLatitude)

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

### Time Complexity (Pior Caso)

- Range search pode visitar toda a árvore
- Comparações de doubles têm custo constante

**Complexidade total**: O(n)

### Space Complexity (Pior Caso)

- Recursão pode atingir O(log n) pois a árvore é balanceada
- Lista resultante pode acumular até n elementos

**Complexidade espacial**: O(n) dominado pela lista resultante

---

## 6. getStationsByLatitude(...) e getStationsByLongitude(...)

Ambos seguem o mesmo padrão.

### Time Complexity

**O(n)**

### Space Complexity

**O(n)**
