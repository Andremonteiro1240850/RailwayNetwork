# USEI08 - Análise de Complexidade Temporal (Pior Caso)

## Notação

- **N**: Total de estações na KD-tree
- **k**: Resultados retornados (rangeQuery/radiusSearch) ou parâmetro n (nearestN)
- **m**: Resultados filtrados (m ≤ k)
- **s**: Comprimento médio dos nomes das estações
- **h**: Altura da árvore (O(log N) balanceada, O(N) degenerada)

## Tipo de Algoritmos

Os algoritmos de busca implementados (range query, nearest neighbor, radius search) são **não determinísticos**, pois têm passos que podem não ser executados dependendo dos valores nas estruturas de dados (poda espacial e branch-and-bound).

Para algoritmos não determinísticos, a análise de complexidade considera:
- **Worst case (Pior caso)**: Upper bound - caso que causa o máximo número de operações (quando a poda não funciona efetivamente)
- **Best case (Melhor caso)**: Lower bound - caso que causa o mínimo número de operações (quando a poda funciona de forma ótima)
- **Average case (Caso médio)**: Tempo esperado considerando todos os possíveis inputs e caminhos de execução

Esta análise foca no **pior caso** para garantir limites superiores de performance.

---

## Constructor

```java
public GeographicalSearchController(KDTreeRepository kdTreeRepository, StationRepository stationRepository)
```

**Complexidade Temporal: O(N)**

**Análise da complexidade:**
1. `getCachedTree()`: O(1) - apenas retorna uma referência ao objeto já existente em memória, sem processamento adicional
2. `getAllStations()`: O(N) - cria um novo ArrayList e copia todas as N referências às estações. Embora os objetos Station não sejam duplicados (apenas referências), a criação da lista e cópia das referências requer O(N) operações
3. **Total**: O(1) + O(N) = O(N) - a operação dominante é a cópia da lista

---

## searchByGeographicalArea (com filtros)

```java
public List<Station> searchByGeographicalArea(double latMin, double latMax, 
                                               double lonMin, double lonMax,
                                               SearchFilters filters)
```

**Complexidade Temporal: O(N + k + m log m · s)**

**Análise da complexidade:**

1. **Validações**: O(1) - verificações de limites de latitude/longitude são comparações numéricas simples, operação constante

2. **rangeQueryUnsorted**: O(N + k)
   - **Traversal recursivo**: No pior caso, quando o retângulo de busca cobre todo o dataset ou a árvore é degenerada (altura O(N)), a poda espacial não consegue eliminar ramos. Isto força a visita de todos os N nós da árvore
   - **Verificação de estações**: Para cada nó visitado, são verificadas todas as estações no bucket para ver se estão dentro do range retangular. Cada verificação de coordenadas é O(1)
   - **Resultado**: São visitados N nós e são recolhidas k estações que estão no range → O(N + k)
   - **Nota**: No caso médio com árvore balanceada, o traversal é O(√N), resultando em O(√N + k)

3. **Filtragem**: O(k)
   - São percorridos os k resultados obtidos da query uma única vez
   - Cada chamada `filters.matches(station)` executa no máximo 3 comparações constantes (país, isCity, isMainStation), então é O(1)
   - **Total**: k iterações × O(1) = O(k)

4. **Ordenação**: O(m log m · s)
   - São ordenados apenas os m resultados filtrados (não todos os k), o que é uma otimização importante
   - É utilizado Timsort (algoritmo padrão do Java), que é baseado em comparações e requer O(m log m) comparações no pior caso
   - Cada comparação de nomes de estações (`String.compareTo()`) compara caracteres sequencialmente até encontrar diferença, levando O(s) tempo onde s é o comprimento médio dos nomes
   - **Total**: O(m log m) comparações × O(s) por comparação = O(m log m · s)

5. **Total**: O(N + k) + O(k) + O(m log m · s) = O(N + k + m log m · s)

---

## searchByGeographicalArea (sem filtros)

```java
public List<Station> searchByGeographicalArea(double latMin, double latMax, 
                                               double lonMin, double lonMax)
```

**Complexidade Temporal: O(N + k log k · s)**

**Análise da complexidade:**
1. **Range query**: O(N + k) - mesma análise do método com filtros: traversal O(N) no pior caso e recolha de k resultados
2. **Ordenação**: O(k log k · s)
   - Como não há filtros, são ordenados todos os k resultados obtidos da query
   - Timsort requer O(k log k) comparações
   - Cada comparação de strings leva O(s) tempo
   - **Total**: O(k log k) comparações × O(s) = O(k log k · s)
3. **Total**: O(N + k) + O(k log k · s) = O(N + k log k · s)

---

## findNearestStations (com filtros)

```java
public List<KDTree.StationDistance> findNearestStations(double targetLat, double targetLon, 
                                                           int n, SearchFilters filters)
```

**Complexidade Temporal: O(N log k + k log k)**

**Análise da complexidade:**

1. **Validações**: O(1) - verificações de coordenadas válidas e parâmetro n positivo são operações constantes

2. **nearestN**: O(N log k + k log k)
   - **Traversal recursivo**: O(N)
     - No pior caso, quando todas as estações são equidistantes do ponto alvo ou a árvore é degenerada, a poda branch-and-bound não consegue eliminar ramos
     - Isto força a visita de todos os N nós da árvore
     - Para cada nó, são processadas todas as estações no bucket
   - **Priority queue (max-heap)**: O(N log k)
     - É mantida uma max-heap de tamanho máximo k com as k estações mais próximas encontradas até ao momento
     - Para cada uma das N estações processadas, é calculada a distância Haversine (O(1)) e são feitas operações na heap
     - Se a heap tem menos de k elementos, é feito `offer()` (O(log k))
     - Se a heap está cheia e é encontrada uma estação mais próxima, é feito `poll()` seguido de `offer()` (ambos O(log k))
     - **Total**: N estações × O(log k) operações = O(N log k)
   - **Ordenação final**: O(k log k)
     - Após o traversal, são ordenados os k resultados finais por distância
     - Comparar doubles é O(1), então não há fator de string
     - Timsort requer O(k log k) comparações
   - **Total nearestN**: O(N) + O(N log k) + O(k log k) = O(N log k + k log k) (O(N) é dominado por O(N log k))

3. **Filtragem**: O(m)
   - São percorridos os m resultados retornados por nearestN uma vez
   - Cada filtro é O(1) como explicado anteriormente
   - **Total**: O(m)

4. **Total**: O(N log k + k log k) + O(m) = O(N log k + k log k) (como m ≤ k, O(m) é dominado por O(k log k))

---

## findNearestStations (sem filtros)

```java
public List<KDTree.StationDistance> findNearestStations(double targetLat, double targetLon, int n)
```

**Complexidade Temporal: O(N log k + k log k)**
- Mesma que nearestN

---

## findStationsInRadius (com filtros)

```java
public List<KDTree.StationDistance> findStationsInRadius(double targetLat, double targetLon, 
                                                              double radiusKm, SearchFilters filters)
```

**Complexidade Temporal: O(N + k + m log m)**

**Análise da complexidade:**

1. **Validações**: O(1) - verificações de coordenadas válidas e raio não-negativo são operações constantes

2. **radiusSearchUnsorted**: O(N + k)
   - **Traversal recursivo**: O(N)
     - No pior caso, quando o raio de busca é muito grande (cobrindo toda a árvore) ou a árvore é degenerada, a poda espacial não consegue eliminar ramos
     - Isto força a visita de todos os N nós da árvore
   - **Processamento de estações**: Para cada estação encontrada, é calculada a distância Haversine até ao ponto alvo
     - O cálculo Haversine envolve operações trigonométricas (sin, cos, atan2) que são O(1)
     - É verificada se a distância está dentro do raio (comparação O(1))
   - **Recolha de resultados**: São guardados k resultados dentro do raio como objetos StationDistance
     - Cada criação de objeto é O(1)
   - **Total**: O(N) para visitar todos os nós + O(k) para processar e guardar resultados = O(N + k)
   - **Nota**: No caso médio com árvore balanceada e raio razoável, o traversal é O(√N), resultando em O(√N + k)

3. **Filtragem**: O(k)
   - São percorridos os k resultados obtidos da busca uma vez
   - Cada filtro é O(1) como explicado anteriormente
   - **Total**: O(k)

4. **Ordenação**: O(m log m)
   - São ordenados apenas os m resultados filtrados por distância (não todos os k)
   - Timsort requer O(m log m) comparações
   - Comparar doubles (distâncias) é O(1), então não há fator adicional
   - **Total**: O(m log m)

5. **Total**: O(N + k) + O(k) + O(m log m) = O(N + k + m log m)

---

## findStationsInRadius (sem filtros)

```java
public List<KDTree.StationDistance> findStationsInRadius(double targetLat, double targetLon, double radiusKm)
```

**Complexidade Temporal: O(N + k log k)**

**Análise da complexidade:**
1. **Radius search**: O(N + k)
   - Mesma análise do método com filtros: traversal O(N) no pior caso, processamento de estações e recolha de k resultados
2. **Ordenação**: O(k log k)
   - Como não há filtros, são ordenados todos os k resultados obtidos da busca por distância
   - Timsort requer O(k log k) comparações
   - Comparar doubles é O(1), então não há fator adicional
   - **Total**: O(k log k)
3. **Total**: O(N + k) + O(k log k) = O(N + k log k)

---

## Resumo

**Constructor**
- Complexidade Temporal: O(N)

**searchByGeographicalArea (com filtros)**
- Complexidade Temporal: O(N + k + m log m · s)

**searchByGeographicalArea (sem filtros)**
- Complexidade Temporal: O(N + k log k · s)

**findNearestStations (com filtros)**
- Complexidade Temporal: O(N log k + k log k)

**findNearestStations (sem filtros)**
- Complexidade Temporal: O(N log k + k log k)

**findStationsInRadius (com filtros)**
- Complexidade Temporal: O(N + k + m log m)

**findStationsInRadius (sem filtros)**
- Complexidade Temporal: O(N + k log k)

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

2. **Pior caso ocorre quando**: árvore degenerada, queries grandes cobrindo todo dataset, ou pontos equidistantes (nearest neighbor).
