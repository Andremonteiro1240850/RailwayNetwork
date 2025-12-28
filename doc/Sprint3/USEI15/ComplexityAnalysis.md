# Análise de Complexidade Temporal - USEI15 (Risk-Aware Shortest Paths)

## Método Analisado: `BellmanFord.findShortestPath()`

**Localização**: `src/main/java/Railway Network Optimization/Domain/BellmanFord.java`

**Parâmetros**:
- `graph`: RailwayGraph (grafo direcionado)
- `sourceId`: String (ID da estação origem)
- `targetId`: String (ID da estação destino)

**Notação**:
- |V| = número de vértices (estações) = `graph.getNodeCount()`
- |E| = número de arestas (linhas) = `graph.getEdgeCount()`

---

## Estruturas de Dados Utilizadas

### 1. `Map<String, Double> distances`
- **Tipo**: HashMap
- **Tamanho**: |V| (uma entrada por vértice)
- **Operações dominantes**:
  - `put(key, value)`: O(1) amortizado
  - `get(key)`: O(1) amortizado
  - `containsKey(key)`: O(1) amortizado

### 2. `Map<String, String> predecessors`
- **Tipo**: HashMap
- **Tamanho**: |V| (uma entrada por vértice)
- **Operações dominantes**:
  - `put(key, value)`: O(1) amortizado
  - `get(key)`: O(1) amortizado

### 3. `List<Line> allEdges` (do RailwayGraph)
- **Tipo**: ArrayList
- **Tamanho**: |E|
- **Operações dominantes**:
  - Iteração: O(|E|)
  - Acesso por índice: O(1)

### 4. `List<String> cycleStations` (em detectNegativeCycle)
- **Tipo**: ArrayList
- **Tamanho**: máximo |V| (no pior caso, ciclo contém todos os vértices)
- **Operações dominantes**:
  - `add(element)`: O(1) amortizado
  - `Collections.reverse()`: O(k) onde k = tamanho da lista

### 5. `Set<String> visited` e `Set<String> cycleSet`
- **Tipo**: HashSet
- **Tamanho**: máximo |V|
- **Operações dominantes**:
  - `add(element)`: O(1) amortizado
  - `contains(element)`: O(1) amortizado

---

## Análise Fase por Fase

### FASE 1: Validações e Casos Especiais

**Linhas 180-206 do código**

```java
// Validações de entrada (linhas 180-199)
if (graph == null) { ... }                    // O(1)
if (sourceId == null || ...) { ... }         // O(1)
if (targetId == null || ...) { ... }         // O(1)
sourceId = sourceId.trim();                  // O(1)
targetId = targetId.trim();                  // O(1)
if (!graph.containsNode(sourceId)) { ... }   // O(1) - HashMap.containsKey
if (!graph.containsNode(targetId)) { ... }   // O(1) - HashMap.containsKey

// Caso especial: source == target (linhas 202-206)
if (sourceId.equals(targetId)) {             // O(1)
    List<PathStep> path = new ArrayList<>();  // O(1)
    path.add(new PathStep(...));             // O(1)
    return new ShortestPathResult(...);      // O(1)
}
```

**Complexidade da Fase 1**: **O(1)**
- Todas as operações são constantes
- Não há loops
- Acesso a HashMap é O(1)

---

### FASE 2: Inicialização das Estruturas

**Linhas 208-219 do código**

```java
// Criação dos Maps (linhas 209-210)
Map<String, Double> distances = new HashMap<>();      // O(1)
Map<String, String> predecessors = new HashMap<>();    // O(1)

// Loop de inicialização (linhas 213-216)
for (Station station : graph.getAllNodes()) {         // Itera |V| vezes
    distances.put(station.getId(), Double.MAX_VALUE); // O(1) por iteração
    predecessors.put(station.getId(), null);          // O(1) por iteração
}

// Inicialização da origem (linha 219)
distances.put(sourceId, 0.0);                         // O(1)
```

**Análise detalhada**:
- `graph.getAllNodes()`: retorna `Collections.unmodifiableCollection(nodes.values())`
  - Acesso ao HashMap.values(): O(1) para obter a view
  - Iteração sobre a Collection: O(|V|)
- Cada iteração do loop:
  - `station.getId()`: O(1)
  - `distances.put()`: O(1) amortizado (HashMap)
  - `predecessors.put()`: O(1) amortizado (HashMap)

**Complexidade da Fase 2**: **O(|V|)**
- Loop executa exatamente |V| iterações
- Cada iteração tem custo O(1)
- Total: |V| × O(1) = O(|V|)

---

### FASE 3: Relaxação de Arestas (V-1 iterações)

**Linhas 221-244 do código**

```java
int vertexCount = graph.getNodeCount();              // O(1)
for (int i = 0; i < vertexCount - 1; i++) {         // Loop externo: (|V| - 1) iterações
    boolean relaxed = false;                          // O(1) por iteração
    
    for (Line edge : graph.getAllEdges()) {          // Loop interno: |E| iterações
        String u = edge.getDepartureStationId();     // O(1)
        String v = edge.getArrivalStationId();       // O(1)
        double cost = edge.getCost();                // O(1)
        
        // Verificação de relaxação (linhas 232-237)
        if (distances.get(u) != Double.MAX_VALUE &&   // O(1) - HashMap.get
            distances.get(u) + cost < distances.get(v)) {  // O(1) - HashMap.get + comparação
            distances.put(v, distances.get(u) + cost);    // O(1) - HashMap.put
            predecessors.put(v, u);                       // O(1) - HashMap.put
            relaxed = true;                                // O(1)
        }
    }
    
    // Early termination (linhas 241-243)
    if (!relaxed) {                                   // O(1)
        break;                                        // O(1)
    }
}
```

**Análise detalhada**:

1. **Loop externo**: executa no máximo (|V| - 1) vezes
   - No pior caso: todas as |V| - 1 iterações são executadas
   - Com early termination: pode terminar antes se nenhuma relaxação ocorrer

2. **Loop interno**: em cada iteração do loop externo, executa |E| vezes
   - `graph.getAllEdges()`: retorna `Collections.unmodifiableList(allEdges)`
   - Iteração sobre a lista: O(|E|)

3. **Operações dentro do loop interno** (por aresta):
   - Acesso a propriedades da `Line`: O(1)
   - `distances.get(u)`: O(1) - HashMap lookup
   - `distances.get(v)`: O(1) - HashMap lookup
   - Comparação e operação aritmética: O(1)
   - `distances.put(v, ...)`: O(1) amortizado - HashMap put
   - `predecessors.put(v, u)`: O(1) amortizado - HashMap put

4. **Early termination**:
   - Se nenhuma relaxação ocorrer numa iteração, o algoritmo termina
   - No melhor caso: O(|E|) se terminar na primeira iteração
   - No pior caso: todas as |V| - 1 iterações são necessárias

**Complexidade da Fase 3**:
- **Pior caso**: (|V| - 1) × |E| × O(1) = **O(|V| × |E|)**
- **Melhor caso** (com early termination): O(|E|)
- **Caso médio**: depende da estrutura do grafo, mas tipicamente O(|V| × |E|)

**Justificação**:
- O loop externo itera (|V| - 1) vezes no pior caso
- O loop interno itera |E| vezes em cada iteração do externo
- Cada operação dentro do loop interno é O(1)
- Total: (|V| - 1) × |E| × O(1) = O(|V| × |E|)

---

### FASE 4: Detecção de Ciclos Negativos

**Linhas 246-258 do código**

```java
// Verificação de ciclos negativos (linhas 247-258)
for (Line edge : graph.getAllEdges()) {              // Loop: |E| iterações
    String u = edge.getDepartureStationId();         // O(1)
    String v = edge.getArrivalStationId();           // O(1)
    double cost = edge.getCost();                    // O(1)
    
    if (distances.get(u) != Double.MAX_VALUE &&      // O(1) - HashMap.get
        distances.get(u) + cost < distances.get(v)) { // O(1) - HashMap.get + comparação
        // Ciclo negativo detectado!
        NegativeCycleInfo cycleInfo = detectNegativeCycle(graph, v, predecessors);  // Ver abaixo
        return new ShortestPathResult(cycleInfo);     // O(1)
    }
}
```

**Análise detalhada**:
- Loop executa |E| iterações (uma por aresta)
- Cada iteração:
  - Acesso a propriedades: O(1)
  - Lookups em HashMap: O(1)
  - Comparação: O(1)
- Se ciclo negativo for detectado, chama `detectNegativeCycle()`

**Complexidade da Fase 4 (sem detecção de ciclo)**: **O(|E|)**
- Loop executa |E| vezes
- Cada iteração: O(1)
- Total: |E| × O(1) = O(|E|)

**Complexidade da Fase 4 (com detecção de ciclo)**: **O(|E| + |V|)**
- Loop executa até |E| vezes (pode terminar antes)
- Se detectar ciclo: chama `detectNegativeCycle()` que tem complexidade O(|V|) - ver abaixo

---

### FASE 5: Método `detectNegativeCycle()`

**Linhas 278-332 do código**

```java
private static NegativeCycleInfo detectNegativeCycle(RailwayGraph graph, String startVertex, 
                                                     Map<String, String> predecessors) {
    // Fase 5.1: Encontrar vértice no ciclo (linhas 281-293)
    Set<String> visited = new HashSet<>();            // O(1)
    String current = startVertex;                     // O(1)
    
    while (current != null && !visited.contains(current)) {  // Loop: máximo |V| iterações
        visited.add(current);                         // O(1) por iteração
        current = predecessors.get(current);          // O(1) por iteração - HashMap.get
    }
    
    if (current == null) {
        current = startVertex;                        // O(1)
    }
    
    // Fase 5.2: Extrair o ciclo (linhas 295-325)
    List<String> cycleStations = new ArrayList<>();   // O(1)
    List<CycleEdge> cycleEdges = new ArrayList<>();   // O(1)
    Set<String> cycleSet = new HashSet<>();          // O(1)
    
    String cycleStart = current;                      // O(1)
    String node = cycleStart;                         // O(1)
    double totalCycleCost = 0.0;                      // O(1)
    
    do {                                              // Loop: máximo |V| iterações
        if (cycleSet.contains(node)) {                // O(1) - HashSet.contains
            break;                                    // O(1)
        }
        cycleSet.add(node);                           // O(1) - HashSet.add
        cycleStations.add(node);                      // O(1) - ArrayList.add
        
        String prev = predecessors.get(node);         // O(1) - HashMap.get
        if (prev != null) {
            Line edge = findEdge(graph, prev, node);  // Ver análise de findEdge abaixo
            if (edge != null) {
                cycleEdges.add(new CycleEdge(...));   // O(1)
                totalCycleCost += edge.getCost();     // O(1)
            }
            node = prev;                              // O(1)
        } else {
            break;                                    // O(1)
        }
    } while (node != null && !node.equals(cycleStart));
    
    // Fase 5.3: Reverter listas (linhas 327-329)
    Collections.reverse(cycleStations);               // O(k) onde k = tamanho do ciclo
    Collections.reverse(cycleEdges);                  // O(k) onde k = tamanho do ciclo
}
```

**Análise detalhada**:

**Fase 5.1 - Encontrar vértice no ciclo**:
- Loop `while`: no máximo |V| iterações (se percorrer todos os predecessores)
- Cada iteração:
  - `visited.contains()`: O(1) - HashSet lookup
  - `visited.add()`: O(1) - HashSet add
  - `predecessors.get()`: O(1) - HashMap lookup
- **Complexidade**: O(|V|)

**Fase 5.2 - Extrair o ciclo**:
- Loop `do-while`: no máximo |V| iterações (ciclo pode conter todos os vértices)
- Cada iteração:
  - Operações em HashSet: O(1)
  - Operações em ArrayList: O(1)
  - `predecessors.get()`: O(1)
  - `findEdge(graph, prev, node)`: **O(grau_saída(prev))**
    - Ver análise de `findEdge()` abaixo
    - No pior caso, um vértice pode ter grau de saída O(|V|)
    - No caso médio, grau de saída é O(|E|/|V|)
- **Complexidade**: O(|V| × grau_máximo_saída)
  - No pior caso: O(|V|²) se houver vértice com grau O(|V|)
  - No caso médio: O(|V| × |E|/|V|) = O(|E|)

**Fase 5.3 - Reverter listas**:
- `Collections.reverse()`: O(k) onde k = tamanho do ciclo
- k ≤ |V| (ciclo não pode ter mais vértices que o grafo)
- **Complexidade**: O(|V|)

**Método `findEdge()` (linhas 378-385)**:
```java
private static Line findEdge(RailwayGraph graph, String fromId, String toId) {
    for (Line edge : graph.getOutgoingEdges(fromId)) {  // Loop: grau_saída(fromId) iterações
        if (edge.getArrivalStationId().equals(toId)) {  // O(1) por iteração
            return edge;                                 // O(1)
        }
    }
    return null;                                         // O(1)
}
```
- Loop itera sobre todas as arestas de saída do vértice `fromId`
- Número de iterações = grau de saída de `fromId`
- No pior caso: O(|V|) se o vértice tiver arestas para todos os outros vértices
- No caso médio: O(|E|/|V|)

**Complexidade total de `detectNegativeCycle()`**:
- **Pior caso**: O(|V|) + O(|V|²) + O(|V|) = **O(|V|²)**
- **Caso médio**: O(|V|) + O(|E|) + O(|V|) = **O(|V| + |E|)**

---

### FASE 6: Verificação de Caminho e Reconstrução

**Linhas 260-267 do código**

```java
// Verificação de alcançabilidade (linhas 261-263)
if (distances.get(targetId) == Double.MAX_VALUE) {   // O(1) - HashMap.get
    return new ShortestPathResult();                  // O(1)
}

// Reconstrução do caminho (linha 266)
List<PathStep> path = reconstructPath(sourceId, targetId, predecessors, distances);
return new ShortestPathResult(path, distances.get(targetId));  // O(1)
```

**Método `reconstructPath()` (linhas 343-368)**:

```java
private static List<PathStep> reconstructPath(String sourceId, String targetId, 
                                               Map<String, String> predecessors,
                                               Map<String, Double> distances) {
    List<PathStep> path = new ArrayList<>();          // O(1)
    List<String> stationIds = new ArrayList<>();      // O(1)
    
    // Construir caminho de trás para frente (linhas 350-359)
    String current = targetId;                        // O(1)
    while (current != null) {                         // Loop: máximo |V| iterações
        stationIds.add(0, current);                   // O(k) onde k = tamanho atual da lista
        current = predecessors.get(current);          // O(1) - HashMap.get
        
        // Safety check (linhas 356-358)
        if (stationIds.size() > 1000) {               // O(1)
            break;                                    // O(1)
        }
    }
    
    // Converter para PathStep (linhas 362-365)
    for (String stationId : stationIds) {             // Loop: comprimento do caminho iterações
        double cost = distances.get(stationId);      // O(1) - HashMap.get
        path.add(new PathStep(stationId, cost));     // O(1) - ArrayList.add
    }
    
    return path;                                      // O(1)
}
```

**Análise detalhada**:

1. **Loop de reconstrução reversa** (linhas 350-359):
   - Executa no máximo |V| vezes (caminho não pode ter mais vértices que o grafo)
   - **Problema de complexidade**: `stationIds.add(0, current)`
     - Adicionar no início de ArrayList: **O(k)** onde k = tamanho atual da lista
     - Na primeira iteração: O(1)
     - Na segunda iteração: O(2)
     - Na terceira iteração: O(3)
     - ...
     - Na k-ésima iteração: O(k)
     - **Total**: 1 + 2 + 3 + ... + |V| = |V| × (|V| + 1) / 2 = **O(|V|²)**
   - `predecessors.get(current)`: O(1) por iteração

2. **Loop de conversão para PathStep** (linhas 362-365):
   - Executa p vezes, onde p = comprimento do caminho
   - p ≤ |V| (caminho não pode ter mais vértices que o grafo)
   - Cada iteração: O(1)
   - **Complexidade**: O(|V|)

**Complexidade total de `reconstructPath()`**:
- **Pior caso**: O(|V|²) + O(|V|) = **O(|V|²)**
- **Caso médio**: O(|V|²) (domina devido ao `add(0, ...)`)

**Nota sobre otimização**:
- A implementação atual usa `add(0, element)` que é O(k) por inserção
- Uma otimização seria usar `add(element)` e depois `Collections.reverse()`, resultando em O(|V|)
- No entanto, a análise deve ser baseada no código atual

**Complexidade da Fase 6**: **O(|V|²)**

---

## Complexidade Total do Algoritmo

### Análise por Fase:

1. **Validações e casos especiais**: O(1)
2. **Inicialização**: O(|V|)
3. **Relaxação de arestas**: O(|V| × |E|)
4. **Detecção de ciclos negativos** (sem ciclo): O(|E|)
5. **Detecção de ciclos negativos** (com ciclo): O(|E| + |V|²) no pior caso, O(|E| + |V|) no caso médio
6. **Reconstrução do caminho**: O(|V|²)

### Cenários:

#### Cenário 1: Caminho encontrado (sem ciclo negativo)
- Fases executadas: 1, 2, 3, 4 (sem detecção), 6
- Complexidade: O(1) + O(|V|) + O(|V| × |E|) + O(|E|) + O(|V|²)
- **Termo dominante**: O(|V| × |E|) ou O(|V|²), dependendo de qual é maior
- **Complexidade final**: **O(|V| × |E| + |V|²)**

#### Cenário 2: Ciclo negativo detectado
- Fases executadas: 1, 2, 3, 4 (com detecção), 5
- Complexidade: O(1) + O(|V|) + O(|V| × |E|) + O(|E|) + O(|V|²)
- **Termo dominante**: O(|V| × |E|) ou O(|V|²)
- **Complexidade final**: **O(|V| × |E| + |V|²)**

#### Cenário 3: Sem caminho
- Fases executadas: 1, 2, 3, 4 (sem detecção)
- Complexidade: O(1) + O(|V|) + O(|V| × |E|) + O(|E|)
- **Termo dominante**: O(|V| × |E|)
- **Complexidade final**: **O(|V| × |E|)**

### Análise Assintótica Final:

Para grafos típicos:
- Se |E| = O(|V|) (grafo esparso): O(|V| × |E|) = O(|V|²), então O(|V|²) domina
- Se |E| = O(|V|²) (grafo denso): O(|V| × |E|) = O(|V|³), então O(|V| × |E|) domina

**Complexidade assintótica (Big-O)**: **O(|V| × |E| + |V|²)**

**Simplificação**:
- Para grafos densos (|E| = O(|V|²)): O(|V| × |E|) domina → **O(|V| × |E|)**
- Para grafos esparsos (|E| = O(|V|)): O(|V|²) domina → **O(|V|²)**

**Complexidade final (Big-O)**: **O(|V| × |E|)**

*Nota: A complexidade O(|V|²) da reconstrução do caminho é tipicamente menor que O(|V| × |E|) para grafos não-triviais, mas está incluída na análise completa.*

---

## Influência das Escolhas de Implementação

### 1. Uso de HashMap para `distances` e `predecessors`
- **Impacto**: Operações de lookup e update são O(1) amortizado
- **Alternativa**: Array indexado por ID → O(1) garantido, mas requer IDs numéricos consecutivos
- **Escolha atual**: Flexível (aceita qualquer String como ID), mantém O(1) na prática

### 2. Uso de `List<Line> allEdges` para iteração
- **Impacto**: Iteração sobre todas as arestas é O(|E|)
- **Alternativa**: Iterar sobre vértices e suas arestas de saída → mesma complexidade O(|E|)
- **Escolha atual**: Simples e direta, facilita early termination

### 3. Early termination na relaxação
- **Impacto**: Pode terminar antes de (|V| - 1) iterações
- **Melhor caso**: O(|E|) se terminar na primeira iteração
- **Pior caso**: Ainda O(|V| × |E|)
- **Escolha atual**: Otimização útil para grafos onde o caminho converge rapidamente

### 4. Uso de `add(0, element)` em `reconstructPath()`
- **Impacto**: O(|V|²) devido a deslocamento de elementos
- **Alternativa**: Usar `add(element)` + `Collections.reverse()` → O(|V|)
- **Escolha atual**: Mais simples, mas menos eficiente
- **Impacto na complexidade total**: O(|V|²) é tipicamente dominado por O(|V| × |E|) em grafos densos

### 5. Detecção de ciclo negativo com `findEdge()`
- **Impacto**: O(grau_saída) por chamada, pode ser O(|V|) no pior caso
- **Alternativa**: Manter Map<Pair<String, String>, Line> → O(1) lookup, mas O(|E|) espaço extra
- **Escolha atual**: Trade-off entre espaço e tempo, aceitável para grafos típicos

---

## Resumo Executivo

| Fase | Complexidade | Justificação |
|------|--------------|--------------|
| Validações | O(1) | Operações constantes, sem loops |
| Inicialização | O(\|V\|) | Loop sobre todos os vértices |
| Relaxação | O(\|V\| × \|E\|) | Loop externo (\|V\|-1) × loop interno (\|E\|) |
| Detecção ciclo | O(\|E\|) ou O(\|E\| + \|V\|²) | Loop sobre arestas + extração do ciclo |
| Reconstrução | O(\|V\|²) | `add(0, ...)` em ArrayList causa O(k) por inserção |

**Complexidade Assintótica Final**: **O(|V| × |E|)**

Onde:
- |V| = número de vértices (estações)
- |E| = número de arestas (linhas)

Esta é a complexidade clássica do algoritmo Bellman-Ford, confirmada pela análise detalhada do código implementado.

