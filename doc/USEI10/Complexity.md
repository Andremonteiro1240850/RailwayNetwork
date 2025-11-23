# Análise de Complexidade - USEI10

---

## 1. KDTree.radiusSearch(double targetLat, double targetLon, double radiusKm)

**Tipo**: Não-determinístico - depende da distribuição espacial das estações, tamanho do raio e número de estações encontradas (k).

### Código

```java
public List<StationDistance> radiusSearch(double targetLat, double targetLon, double radiusKm) {
    if (radiusKm < 0) {
        throw new IllegalArgumentException("Radius must be non-negative");
    }
    if (root == null) {
        return new ArrayList<>();
    }
    
    List<StationDistance> results = new ArrayList<>();
    radiusSearchRecursive(root, targetLat, targetLon, radiusKm, 0, results);
    
    // Sort by distance (ascending)
    results.sort(Comparator.comparing(StationDistance::getDistance));
    
    return results;
}
```

### Complexidade Temporal

**Melhor caso**: O(log n)
- Ocorre quando o raio é muito pequeno e nenhuma estação é encontrada (k = 0)
- Apenas um caminho da raiz até uma folha é explorado
- Pruning espacial elimina todos os outros ramos imediatamente
- Nenhuma ordenação necessária

**Caso médio**: O(√n + k log k)
- Assumindo árvore balanceada e raio moderado
- √n representa a altura esperada da árvore em KD-trees 2D
- k é o número de estações encontradas dentro do raio
- Pruning espacial reduz significativamente o número de nós visitados
- Ordenação domina quando k é grande

**Pior caso**: O(n + k log k)
- Ocorre quando o raio cobre toda a área, visitando todos os nós da árvore
- Todas as n estações podem estar dentro do raio (k ≈ n)
- Quando k ≈ n, simplifica para O(n log n)

**Análise por componente**:
- Validação de parâmetros: O(1)
- Traversal recursivo da KD-tree: O(log n) melhor / O(√n + k) médio / O(n) pior
- Cálculo de distâncias Haversine: O(k) - cada cálculo é O(1)
- Ordenação (Timsort): O(k log k)

---

## 2. KDTree.radiusSearchRecursive(KDNode, double, double, double, int, List)

**Tipo**: Não-determinístico - depende da estrutura da árvore, tamanho do raio e distribuição espacial.

### Complexidade Temporal

**Melhor caso**: O(log n)
- Apenas um caminho da raiz até uma folha é explorado
- Pruning espacial elimina todos os outros ramos completamente
- Buckets de tamanho constante O(1)

**Caso médio**: O(√n + k)
- Pruning eficiente reduz o número de nós visitados
- Apenas um ramo é tipicamente explorado por nível
- Buckets de tamanho médio constante

**Pior caso**: O(n)
- Todos os nós são visitados quando o raio cobre toda a área
- Ambos os ramos podem ser explorados quando o raio é grande
- Se considerarmos buckets grandes, pode ser O(n + Σbᵢ) onde bᵢ é o tamanho do bucket do nó i

---

## 3. StationDistanceComparator.compare(StationDistance, StationDistance)

**Tipo**: Não-determinístico - depende dos valores de distância e dos nomes das estações.

### Código

```java
public int compare(KDTree.StationDistance sd1, KDTree.StationDistance sd2) {
    // First compare by distance (ascending)
    int distanceCompare = Double.compare(sd1.getDistance(), sd2.getDistance());
    if (distanceCompare != 0) {
        return distanceCompare;
    }
    
    // If distances are equal, compare by station name (descending)
    String name1 = sd1.getStation().getName();
    String name2 = sd2.getStation().getName();
    return name2.compareTo(name1); // Reverse order for descending
}
```

### Complexidade Temporal

**Melhor caso**: O(1)
- Distâncias são diferentes, retorna imediatamente após `Double.compare()`
- Nenhuma comparação de strings é necessária

**Caso médio**: O(1)
- Na maioria dos casos, as distâncias são diferentes
- Quando distâncias são iguais, tipicamente encontra diferença nos primeiros caracteres

**Pior caso**: O(m)
- Ocorre quando as distâncias são iguais E os nomes são idênticos ou muito similares
- `String.compareTo()` compara caracteres até encontrar diferença ou chegar ao fim
- m = comprimento máximo entre os dois nomes

---

## 4. RadiusSearchController.searchRadiusWithSummary(double, double, double)

**Tipo**: Não-determinístico - depende da distribuição espacial, tamanho do raio, número de estações encontradas (k), distribuição de distâncias e países.

### Código

```java
public RadiusSearchResult searchRadiusWithSummary(double targetLat, double targetLon, double radiusKm) {
    // Validações
    if (targetLat < -90 || targetLat > 90) {
        throw new IllegalArgumentException("Latitude must be between -90 and 90");
    }
    if (targetLon < -180 || targetLon > 180) {
        throw new IllegalArgumentException("Longitude must be between -180 and 180");
    }
    if (radiusKm < 0) {
        throw new IllegalArgumentException("Radius must be non-negative");
    }
    
    // Get stations in radius from KD-tree
    List<KDTree.StationDistance> stationsInRadius = kdTree.radiusSearch(targetLat, targetLon, radiusKm);
    
    // Create AVL tree sorted by distance (ASC) and station name (DESC)
    StationDistanceComparator comparator = new StationDistanceComparator();
    AVL<KDTree.StationDistance> resultTree = new AVL<>(comparator);
    
    for (KDTree.StationDistance sd : stationsInRadius) {
        resultTree.insert(sd);
    }
    
    // Calculate summary by country and isCity
    RadiusSearchSummary summary = calculateSummary(stationsInRadius);
    
    return new RadiusSearchResult(resultTree, summary);
}
```

### Complexidade Temporal

**Melhor caso**: O(log n)
- Raio muito pequeno, nenhuma estação encontrada (k = 0)
- Apenas busca mínima na KD-tree, nenhuma construção AVL ou cálculo de resumo

**Caso médio**: O(√n + k log k)
- Raio moderado, k estações encontradas
- Comparações do comparador são principalmente O(1)
- HashMap bem implementado com O(1) amortizado

**Pior caso**: O(n + k log k · m)
- Raio muito grande, todas as estações encontradas (k ≈ n)
- Muitas estações com distâncias iguais, cada comparação pode ser O(m)
- Quando k ≈ n e muitas distâncias iguais: O(n log n · m)

**Análise por componente**:
- Validações de parâmetros: O(1)
- Busca por raio na KD-tree: O(log n) melhor / O(√n + k log k) médio / O(n + k log k) pior
- Construção da árvore AVL: O(1) melhor / O(k log k) médio / O(k log k · m) pior
- Cálculo do resumo: O(1) melhor / O(k) médio / O(k²) pior (O(k) prático com HashMap Java)

---

## 5. RadiusSearchController.calculateSummary(List<StationDistance>)

**Tipo**: Não-determinístico - depende do número de estações (k) e da distribuição de países.

### Código

```java
private RadiusSearchSummary calculateSummary(List<KDTree.StationDistance> stationsInRadius) {
    Map<String, Integer> stationsByCountry = new HashMap<>();
    int stationsByCity = 0;
    int stationsByNonCity = 0;
    
    for (KDTree.StationDistance sd : stationsInRadius) {
        Station station = sd.getStation();
        
        // Count by country
        String country = station.getCountry();
        stationsByCountry.put(country, stationsByCountry.getOrDefault(country, 0) + 1);
        
        // Count by isCity
        if (station.isCity()) {
            stationsByCity++;
        } else {
            stationsByNonCity++;
        }
    }
    
    return new RadiusSearchSummary(
        stationsByCountry,
        stationsByCity,
        stationsByNonCity,
        stationsInRadius.size()
    );
}
```

### Complexidade Temporal

**Melhor caso**: O(1)
- k = 0 ou todas as estações são do mesmo país
- HashMap tem apenas uma entrada, todas as operações são O(1)

**Caso médio**: O(k)
- HashMap com O(1) amortizado por operação
- Número moderado de países distintos (c << k)
- k iterações × O(1) = O(k)

**Pior caso**: O(k²)
- Ocorre quando todas as k estações são de países diferentes E há muitas colisões no HashMap
- HashMap com função hash ruim pode degenerar para lista ligada
- Na prática, com HashMap bem implementado (Java's HashMap): **O(k)**

---

## 6. Resumo - Complexidade End-to-End

**Operação Principal**: `searchRadiusWithSummary()`  
**Tipo**: Não-determinístico

| Componente | Melhor Caso | Caso Médio | Pior Caso |
|------------|-------------|------------|-----------|
| Validações | O(1) | O(1) | O(1) |
| KD-tree radius search | O(log n) | O(√n + k log k) | O(n + k log k) |
| Construção AVL tree | O(1) | O(k log k) | O(k log k · m) |
| Cálculo resumo | O(1) | O(k) | O(k²) → O(k) prático |
| **TOTAL** | **O(log n)** | **O(√n + k log k)** | **O(n + k log k · m)** |

### Observações

- **Melhor caso**: Ocorre quando o raio é muito pequeno e nenhuma estação é encontrada (k = 0)
- **Caso médio**: Dominado por busca KD-tree e construção AVL, assumindo HashMap bem implementado
- **Pior caso realista**: O(n + k log k · m) - assumindo HashMap bem implementado (O(k) em vez de O(k²))
- Quando k ≈ n: **O(n log n · m)**

**Notações:**
- n = número total de estações na KD-tree
- k = número de estações encontradas dentro do raio
- m = comprimento máximo do nome de uma estação
- c = número de países distintos (geralmente c << k)

---





