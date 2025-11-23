# USEI08 - Análise de Complexidade (Pior Caso)

## Notação

- **N**: Total de estações na KD-tree
- **k**: Resultados retornados (rangeQuery/radiusSearch) ou parâmetro n (nearestN)
- **m**: Resultados filtrados (m ≤ k)
- **s**: Comprimento médio dos nomes das estações
- **h**: Altura da árvore (O(log N) balanceada, O(N) degenerada)

---

## Constructor

```java
public GeographicalSearchController(KDTreeRepository kdTreeRepository, StationRepository stationRepository)
```

**Complexidade Temporal: O(N)**

**Como cheguei a este resultado:**
1. `getCachedTree()`: O(1) - apenas retorna referência, operação constante
2. `getAllStations()`: O(N) - preciso copiar N referências para o novo ArrayList
3. **Total**: O(1) + O(N) = O(N)

**Complexidade Espacial: O(N)**
- ArrayList com N referências a estações

---

## searchByGeographicalArea (com filtros)

```java
public List<Station> searchByGeographicalArea(double latMin, double latMax, 
                                               double lonMin, double lonMax,
                                               SearchFilters filters)
```

**Complexidade Temporal: O(N + k + m log m · s)**

**Como cheguei a este resultado:**

1. **Validações**: O(1) - apenas comparações numéricas simples
2. **rangeQueryUnsorted**: O(N + k)
   - No pior caso, visito todos os N nós da árvore (quando a query cobre tudo ou a árvore é degenerada)
   - Para cada nó visitado, verifico todas as estações no bucket
   - No total: visito N nós e verifico k estações que estão no range → O(N + k)
3. **Filtragem**: O(k) - percorro os k resultados uma vez, cada filtro é O(1) → O(k)
4. **Ordenação**: O(m log m · s)
   - Ordeno apenas os m resultados filtrados (não todos os k)
   - Timsort faz O(m log m) comparações
   - Cada comparação de strings leva O(s) tempo → O(m log m · s)
5. **Total**: O(N + k) + O(k) + O(m log m · s) = O(N + k + m log m · s)

**Complexidade Espacial: O(N + k)**
- Pilha recursão: O(N) no pior caso
- Listas: O(k) + O(m) onde m ≤ k
- Espaço auxiliar ordenação: O(m)

---

## searchByGeographicalArea (sem filtros)

```java
public List<Station> searchByGeographicalArea(double latMin, double latMax, 
                                               double lonMin, double lonMax)
```

**Complexidade Temporal: O(N + k log k · s)**

**Como cheguei a este resultado:**
1. **Range query**: O(N + k) - mesma análise do método com filtros
2. **Ordenação**: O(k log k · s) - ordeno todos os k resultados (não filtrados), cada comparação de strings é O(s)
3. **Total**: O(N + k) + O(k log k · s) = O(N + k log k · s)

**Complexidade Espacial: O(N + k)**
- Pilha recursão: O(N)
- Lista resultados: O(k)
- Espaço auxiliar: O(k)

---

## findNearestStations (com filtros)

```java
public List<KDTree.StationDistance> findNearestStations(double targetLat, double targetLon, 
                                                           int n, SearchFilters filters)
```

**Complexidade Temporal: O(N log k + k log k)**

**Como cheguei a este resultado:**

1. **Validações**: O(1) - comparações simples
2. **nearestN**: O(N log k + k log k)
   - **Traversal**: O(N) - no pior caso visito todos os N nós (estações equidistantes ou árvore degenerada)
   - **Priority queue**: O(N log k)
     - Para cada uma das N estações, faço operações na heap de tamanho máximo k
     - Cada operação (offer/poll) na heap é O(log k)
     - Total: N estações × O(log k) = O(N log k)
   - **Ordenação**: O(k log k) - ordeno os k resultados finais por distância (comparar doubles é O(1))
   - **Total nearestN**: O(N log k) + O(k log k) = O(N log k + k log k)
3. **Filtragem**: O(m) - percorro os m resultados uma vez, cada filtro é O(1)
4. **Total**: O(N log k + k log k) + O(m) = O(N log k + k log k) (m ≤ k, então O(m) é dominado)

**Complexidade Espacial: O(N + k)**
- Pilha recursão: O(N)
- Priority queue: O(k)
- Listas: O(k) + O(m)
- Espaço auxiliar: O(k)

---

## findNearestStations (sem filtros)

```java
public List<KDTree.StationDistance> findNearestStations(double targetLat, double targetLon, int n)
```

**Complexidade Temporal: O(N log k + k log k)**
- Mesma que nearestN

**Complexidade Espacial: O(N + k)**

---

## findStationsInRadius (com filtros)

```java
public List<KDTree.StationDistance> findStationsInRadius(double targetLat, double targetLon, 
                                                              double radiusKm, SearchFilters filters)
```

**Complexidade Temporal: O(N + k + m log m)**

**Como cheguei a este resultado:**

1. **Validações**: O(1) - comparações simples
2. **radiusSearchUnsorted**: O(N + k)
   - **Traversal**: O(N) - no pior caso visito todos os N nós (raio muito grande ou árvore degenerada)
   - **Processamento**: para cada estação, calculo distância Haversine que é O(1)
   - **Recolha**: guardo k resultados dentro do raio
   - **Total**: O(N) para visitar nós + O(k) para guardar resultados = O(N + k)
3. **Filtragem**: O(k) - percorro os k resultados uma vez, cada filtro é O(1)
4. **Ordenação**: O(m log m) - ordeno apenas os m resultados filtrados por distância (comparar doubles é O(1))
5. **Total**: O(N + k) + O(k) + O(m log m) = O(N + k + m log m)

**Complexidade Espacial: O(N + k)**
- Pilha recursão: O(N)
- Listas: O(k) + O(m)
- Espaço auxiliar: O(m)

---

## findStationsInRadius (sem filtros)

```java
public List<KDTree.StationDistance> findStationsInRadius(double targetLat, double targetLon, double radiusKm)
```

**Complexidade Temporal: O(N + k log k)**

**Como cheguei a este resultado:**
1. **Radius search**: O(N + k) - mesma análise do método com filtros
2. **Ordenação**: O(k log k) - ordeno todos os k resultados por distância (comparar doubles é O(1))
3. **Total**: O(N + k) + O(k log k) = O(N + k log k)

**Complexidade Espacial: O(N + k)**

---

## Resumo

**Constructor**
- Tempo: O(N) | Espaço: O(N)

**searchByGeographicalArea (com filtros)**
- Tempo: O(N + k + m log m · s) | Espaço: O(N + k)

**searchByGeographicalArea (sem filtros)**
- Tempo: O(N + k log k · s) | Espaço: O(N + k)

**findNearestStations (com filtros)**
- Tempo: O(N log k + k log k) | Espaço: O(N + k)

**findNearestStations (sem filtros)**
- Tempo: O(N log k + k log k) | Espaço: O(N + k)

**findStationsInRadius (com filtros)**
- Tempo: O(N + k + m log m) | Espaço: O(N + k)

**findStationsInRadius (sem filtros)**
- Tempo: O(N + k log k) | Espaço: O(N + k)

### Casos: Melhor, Médio, Pior

**Range Query**
- Melhor: O(log N + k) - árvore balanceada, query pequena
- Médio: O(√N + k) - árvore balanceada, query típica
- Pior: O(N + k) - árvore degenerada ou query cobre todo dataset

**Nearest Neighbor**
- Melhor: O(log N · log k + k log k) - árvore balanceada, poda forte
- Médio: O(√N log k + k log k) - árvore balanceada, poda moderada
- Pior: O(N log k + k log k) - árvore degenerada ou pontos equidistantes

**Radius Search**
- Melhor: O(log N + k) - árvore balanceada, raio pequeno
- Médio: O(√N + k) - árvore balanceada, raio moderado
- Pior: O(N + k) - árvore degenerada ou raio muito grande

### Notas

1. **Comparação de strings**: O(s) por comparação. Para nomes limitados, simplifica para O(m log m).

2. **Espaço recursão**: O(h) onde h = O(N) no pior caso (árvore degenerada).

3. **Pior caso ocorre quando**: árvore degenerada, queries grandes cobrindo todo dataset, ou pontos equidistantes (nearest neighbor).
