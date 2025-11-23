# Análise de Complexidade - USEI10

---

## 1. KDTree.radiusSearch(double targetLat, double targetLon, double radiusKm)

### Tipo de Algoritmo
**Não-determinístico**: O tempo de execução depende da distribuição espacial das estações, do tamanho do raio de busca e do número de estações encontradas (k).

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

### Time Complexity

**Análise detalhada:**

1. **Validação de parâmetros**: O(1)
   - Verificação de raio negativo e árvore vazia

2. **Traversal recursivo da KD-tree** (`radiusSearchRecursive`):
   - **Melhor caso**: O(log n)
     - Ocorre quando o raio é muito pequeno e nenhuma estação é encontrada (k = 0)
     - Apenas um caminho da raiz até uma folha é explorado
     - Pruning espacial elimina todos os outros ramos imediatamente
   - **Caso médio**: O(√n + k)
     - Assumindo árvore balanceada e raio moderado
     - √n representa a altura esperada da árvore (log₂ n para árvore balanceada, mas em KD-trees 2D o fator é tipicamente √n)
     - k é o número de estações encontradas dentro do raio
     - O pruning espacial permite evitar explorar muitos ramos
   - **Pior caso**: O(n)
     - Ocorre quando o raio de busca é suficientemente grande para visitar todos os nós da árvore
     - Em KD-trees 2D balanceadas, o pior caso teórico é O(n) quando o raio cobre toda a área
     - Cada nó visitado pode conter múltiplas estações no bucket (estações com coordenadas idênticas)

3. **Cálculo de distância Haversine por estação**:
   - **Melhor caso**: O(0) = O(1)
     - Nenhuma estação encontrada (k = 0), nenhum cálculo necessário
   - **Caso médio**: O(k)
     - Apenas k estações dentro do raio requerem cálculo de distância
   - **Pior caso**: O(n)
     - Se todas as n estações estiverem dentro do raio, calculamos n distâncias
   - Cada cálculo Haversine é O(1) (operações matemáticas constantes: sin, cos, atan2)

4. **Ordenação da lista de resultados**:
   - **Melhor caso**: O(1)
     - k = 0 ou k = 1, nenhuma ordenação necessária ou trivial
   - **Caso médio**: O(k log k)
     - k elementos ordenados usando `Comparator.comparing()` que compara doubles
     - Comparação de doubles: O(1)
     - Algoritmo de ordenação (Timsort): O(k log k) no caso médio
   - **Pior caso**: O(k log k)
     - k elementos ordenados usando `Comparator.comparing()` que compara doubles
     - Comparação de doubles: O(1)
     - Algoritmo de ordenação (Timsort): O(k log k) no pior caso

**Complexidade temporal total (Melhor Caso)**: **O(log n)**
- Dominado por: traversal O(log n) quando k = 0
- Nenhuma ordenação necessária quando k = 0

**Complexidade temporal total (Caso Médio)**: **O(√n + k log k)**
- Dominado por: traversal O(√n + k) + ordenação O(k log k)

**Complexidade temporal total (Pior Caso)**: **O(n + k log k)**
- Dominado por: traversal O(n) + ordenação O(k log k)
- Quando k ≈ n (raio muito grande), simplifica para O(n log n)

---

## 2. KDTree.radiusSearchRecursive(KDNode, double, double, double, int, List)

### Tipo de Algoritmo
**Não-determinístico**: O tempo de execução depende da distribuição espacial das estações, do tamanho do raio de busca, da estrutura da árvore e do número de estações encontradas.

### Código

```java
private void radiusSearchRecursive(KDNode node, double targetLat, double targetLon, 
                                   double radiusKm, int depth, List<StationDistance> results) {
    if (node == null) {
        return;
    }
    
    // Check all stations in the bucket
    for (Station station : node.getBucket()) {
        double distance = haversineDistance(targetLat, targetLon, 
                                           station.getLatitude(), station.getLongitude());
        if (distance <= radiusKm) {
            results.add(new StationDistance(station, distance));
        }
    }
    
    int axis = node.getAxis();
    double splitValue = node.getSplitValue();
    double targetCoord = (axis == 0) ? targetLat : targetLon;
    
    // Calculate distance from target to split plane
    double distanceToSplit = Math.abs(targetCoord - splitValue);
    
    // Convert distance along axis to approximate distance in km
    double axisDistanceKm;
    if (axis == 0) {
        axisDistanceKm = distanceToSplit * 111.0; // Latitude
    } else {
        axisDistanceKm = distanceToSplit * 111.0 * Math.cos(Math.toRadians(targetLat)); // Longitude
    }
    
    // Decide which subtrees to explore
    if (targetCoord <= splitValue) {
        radiusSearchRecursive(node.getLeft(), targetLat, targetLon, radiusKm, depth + 1, results);
        if (axisDistanceKm <= radiusKm) {
            radiusSearchRecursive(node.getRight(), targetLat, targetLon, radiusKm, depth + 1, results);
        }
    } else {
        radiusSearchRecursive(node.getRight(), targetLat, targetLon, radiusKm, depth + 1, results);
        if (axisDistanceKm <= radiusKm) {
            radiusSearchRecursive(node.getLeft(), targetLat, targetLon, radiusKm, depth + 1, results);
        }
    }
}
```

### Time Complexity

**Análise detalhada:**

1. **Verificação de nó nulo**: O(1)

2. **Iteração sobre bucket do nó**:
   - **Melhor caso**: O(1)
     - Bucket vazio ou com tamanho constante pequeno
     - Nenhuma estação dentro do raio
   - **Caso médio**: O(b)
     - b = tamanho médio do bucket (número de estações com coordenadas idênticas)
     - Tipicamente b << n
     - Para cada estação no bucket:
       - Cálculo Haversine: O(1)
       - Comparação: O(1)
       - Criação de `StationDistance`: O(1)
       - Adição à lista: O(1) amortizado
   - **Pior caso**: O(b)
     - b = tamanho do bucket (número de estações com coordenadas idênticas)
     - No pior caso, um bucket pode conter todas as estações (b = n), mas tipicamente b << n
     - Para cada estação no bucket:
       - Cálculo Haversine: O(1)
       - Comparação: O(1)
       - Criação de `StationDistance`: O(1)
       - Adição à lista: O(1) amortizado

3. **Cálculo de distância ao plano de split**:
   - Operações matemáticas: O(1)
   - `Math.abs()`, `Math.cos()`, `Math.toRadians()`: O(1)

4. **Decisão de exploração de subárvores**:
   - **Melhor caso**: Apenas um ramo é explorado
     - Pruning espacial elimina o outro ramo completamente
     - Ocorre quando `axisDistanceKm > radiusKm` para o ramo oposto
   - **Caso médio**: Apenas um ramo é explorado (pruning eficiente)
     - Na maioria dos casos, apenas o ramo mais próximo é explorado
   - **Pior caso**: Ambos os ramos são explorados
     - Ocorre quando `axisDistanceKm <= radiusKm` para ambos os lados
     - Isso acontece quando o raio é grande o suficiente

5. **Chamadas recursivas**:
   - **Melhor caso**: O(log n) nós visitados
     - Apenas um caminho da raiz até uma folha é explorado
     - Pruning espacial elimina todos os outros ramos imediatamente
   - **Caso médio**: O(√n + k) nós visitados
     - Pruning espacial reduz significativamente o número de nós visitados
   - **Pior caso**: O(n) nós visitados
     - Quando o raio cobre toda a área, todos os nós são visitados

**Complexidade temporal total (Melhor Caso)**: **O(log n)**
- Assumindo que cada nó tem bucket de tamanho constante O(1)
- Apenas um caminho da raiz até uma folha é explorado
- Pruning espacial elimina todos os outros ramos

**Complexidade temporal total (Caso Médio)**: **O(√n + k)**
- Pruning eficiente reduz o número de nós visitados
- Assumindo buckets de tamanho médio constante

**Complexidade temporal total (Pior Caso)**: **O(n)**
- Assumindo que cada nó tem bucket de tamanho constante O(1), mas no pior caso todos os nós são visitados
- Se considerarmos buckets grandes, pode ser O(n + Σbᵢ) onde bᵢ é o tamanho do bucket do nó i

---

## 3. StationDistanceComparator.compare(StationDistance, StationDistance)

### Tipo de Algoritmo
**Não-determinístico**: O tempo de execução depende dos valores de distância e dos nomes das estações. Se as distâncias forem diferentes, retorna imediatamente. Se forem iguais, precisa comparar strings.

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

### Time Complexity

**Análise detalhada:**

1. **Comparação de distâncias** (`Double.compare()`):
   - O(1)
   - Comparação de dois valores `double` é operação constante

2. **Comparação de nomes de estações** (`String.compareTo()`):
   - **Melhor caso**: O(1)
     - Distâncias são diferentes, retorna imediatamente após `Double.compare()`
     - Nenhuma comparação de strings é necessária
   - **Caso médio**: O(1)
     - Na maioria dos casos, as distâncias são diferentes e retorna após a primeira comparação
     - Quando distâncias são iguais, tipicamente encontra diferença nos primeiros caracteres
   - **Pior caso**: O(m)
     - m = comprimento máximo entre os dois nomes
     - Ocorre quando as distâncias são iguais E os nomes são idênticos ou muito similares
     - `String.compareTo()` compara caracteres até encontrar diferença ou chegar ao fim
     - No pior caso, compara todos os caracteres: min(len(name1), len(name2))

**Complexidade temporal total (Melhor Caso)**: **O(1)**
- Distâncias são diferentes, retorna imediatamente após comparação de doubles
- Nenhuma comparação de strings é necessária

**Complexidade temporal total (Caso Médio)**: **O(1)**
- Na maioria dos casos, as distâncias são diferentes e retorna após a primeira comparação
- Quando distâncias são iguais, tipicamente encontra diferença nos primeiros caracteres dos nomes

**Complexidade temporal total (Pior Caso)**: **O(m)**
- Onde m é o comprimento máximo do nome da estação
- Ocorre quando as distâncias são iguais e os nomes são idênticos ou muito similares
- Dominado pela comparação de strings que precisa comparar todos os caracteres

---

## 4. RadiusSearchController.searchRadiusWithSummary(double, double, double)

### Tipo de Algoritmo
**Não-determinístico**: O tempo de execução depende da distribuição espacial das estações, do tamanho do raio de busca, do número de estações encontradas (k), da distribuição de distâncias e da distribuição de países.

### Código

```java
public RadiusSearchResult searchRadiusWithSummary(double targetLat, double targetLon, double radiusKm) {
    // Validações: O(1)
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

### Time Complexity

**Análise detalhada:**

1. **Validações de parâmetros**: O(1)
   - Três comparações de doubles

2. **Busca por raio na KD-tree** (`kdTree.radiusSearch()`):
   - **Melhor caso**: O(log n)
     - Raio muito pequeno, nenhuma estação encontrada (k = 0)
     - Conforme análise anterior
   - **Caso médio**: O(√n + k log k)
     - Raio moderado, k estações encontradas
   - **Pior caso**: O(n + k log k)
     - Raio muito grande, todas as estações encontradas (k ≈ n)
     - Conforme análise anterior

3. **Construção da árvore AVL**:
   - **Melhor caso**: O(1)
     - k = 0 ou k = 1, nenhuma ou uma inserção trivial
   - **Caso médio**: O(k log k)
     - k inserções, cada uma custa O(log k) em AVL balanceada
     - Cada inserção chama `StationDistanceComparator.compare()`:
       - **Caso médio**: O(1) quando distâncias diferentes (maioria dos casos)
     - **Total caso médio**: O(k log k)
       - Assumindo que comparações são principalmente O(1)
   - **Pior caso**: O(k log k · m)
     - k inserções, cada uma custa O(log k) em AVL balanceada
     - Cada inserção chama `StationDistanceComparator.compare()`:
       - **Pior caso**: O(m) quando distâncias iguais
     - **Total pior caso**: O(k log k · m)
       - Se muitas estações tiverem distâncias iguais, cada comparação pode ser O(m)

4. **Cálculo do resumo** (`calculateSummary()`):
   - **Melhor caso**: O(1)
     - k = 0, nenhuma estação para processar
   - **Caso médio**: O(k)
     - k estações processadas
     - Operações HashMap: O(1) amortizado
     - HashMap bem dimensionado com boa função hash
   - **Pior caso**: O(k²)
     - k estações processadas
     - Ocorre quando todas as k estações são de países diferentes E há muitas colisões no HashMap
     - HashMap com função hash ruim pode degenerar para lista ligada
     - Na prática, com HashMap bem implementado: **O(k)**

**Complexidade temporal total (Melhor Caso)**: **O(log n)**
- Dominado por: busca KD-tree O(log n) quando k = 0
- Nenhuma construção AVL ou cálculo de resumo necessário

**Complexidade temporal total (Caso Médio)**: **O(√n + k log k)**
- Dominado por: busca KD-tree O(√n + k log k) + construção AVL O(k log k) + resumo O(k)
- Assumindo comparações O(1) e HashMap O(1) amortizado

**Complexidade temporal total (Pior Caso)**: **O(n + k log k · m)**
- Dominado por: busca KD-tree O(n + k log k) + construção AVL O(k log k · m) + resumo O(k)
- Quando k ≈ n e muitas distâncias iguais: **O(n log n · m)**

---

## 5. RadiusSearchController.calculateSummary(List<StationDistance>)

### Tipo de Algoritmo
**Não-determinístico**: O tempo de execução depende do número de estações (k) e da distribuição de países. O comportamento do HashMap varia com o número de colisões.

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

### Time Complexity

**Análise detalhada:**

1. **Inicialização de estruturas**: O(1)
   - Criação de HashMap vazio
   - Inicialização de contadores

2. **Loop sobre k estações**:
   - **Melhor caso**: k = 0, nenhuma iteração
   - **Iteração**: k iterações (caso médio e pior caso)
   - Por iteração:
     - Acesso a estação: O(1)
     - Obtenção de país (`getCountry()`): O(1)
     - **Operação HashMap** (`getOrDefault()` + `put()`):
       - **Melhor caso**: O(1)
         - Todas as estações são do mesmo país
         - HashMap tem apenas uma entrada, operações são O(1)
       - **Caso médio/amortizado**: O(1)
         - HashMap bem dimensionado com boa função hash
         - Número moderado de países distintos
       - **Pior caso**: O(k)
         - Ocorre quando há muitas colisões no HashMap
         - HashMap com função hash ruim pode degenerar para lista ligada
         - Todas as k estações são de países diferentes E há muitas colisões
     - Verificação `isCity()`: O(1)
     - Incremento de contador: O(1)

3. **Criação de RadiusSearchSummary**:
   - **Melhor caso**: O(1)
     - Apenas um país, HashMap tem uma entrada
   - **Caso médio**: O(c)
     - c = número de países distintos (geralmente c << k)
   - **Pior caso**: O(k)
     - k países distintos, HashMap tem k entradas

**Complexidade temporal total (Melhor Caso)**: **O(1)**
- k = 0 ou todas as estações são do mesmo país
- HashMap tem apenas uma entrada, todas as operações são O(1)

**Complexidade temporal total (Caso Médio)**: **O(k)**
- HashMap com O(1) amortizado por operação
- k iterações × O(1) = O(k)
- Número moderado de países distintos (c << k)

**Complexidade temporal total (Pior Caso)**: **O(k²)**
- Ocorre quando todas as k estações são de países diferentes E há muitas colisões no HashMap
- HashMap com função hash ruim pode degenerar para lista ligada
- k iterações × O(k) operações HashMap = O(k²)
- Na prática, com HashMap bem implementado (Java's HashMap): **O(k)**

---

## 6. Resumo - Complexidade End-to-End

### Operação Principal: `searchRadiusWithSummary()`

**Tipo de Algoritmo**: Não-determinístico

| Componente | Time Complexity (Melhor Caso) | Time Complexity (Caso Médio) | Time Complexity (Pior Caso) |
|------------|-------------------------------|-----------------------------|----------------------------|
| Validações | O(1) | O(1) | O(1) |
| KD-tree radius search | O(log n) | O(√n + k log k) | O(n + k log k) |
| Construção AVL tree | O(1) | O(k log k) | O(k log k · m) |
| Cálculo resumo | O(1) | O(k) | O(k²) |
| **TOTAL** | **O(log n)** | **O(√n + k log k)** | **O(n + k log k · m + k²)** |

**Simplificações práticas:**

- **Melhor caso**: O(log n)
  - Raio muito pequeno, nenhuma estação encontrada (k = 0)
  - Apenas validações e busca mínima na KD-tree

- **Caso médio**: O(√n + k log k)
  - Dominado por busca KD-tree e construção AVL
  - Assumindo HashMap bem implementado (O(k) em vez de O(k²))

- **Pior caso realista**: O(n + k log k · m)
  - Assumindo HashMap bem implementado (O(k) em vez de O(k²))
  - Quando k ≈ n: **O(n log n · m)**

**Notações:**
- n = número total de estações na KD-tree
- k = número de estações encontradas dentro do raio
- m = comprimento máximo do nome de uma estação
- c = número de países distintos (geralmente c << k)

---





