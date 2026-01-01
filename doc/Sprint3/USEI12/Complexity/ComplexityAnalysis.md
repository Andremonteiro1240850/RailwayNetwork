# Análise de Complexidade Temporal - USEI12 (Minimal Backbone Network)

## Método Analisado: `MinimalBackboneService.computeMinimalBackbone()`

**Localização**: `src/main/java/Railway Network Optimization/Domain/MinimalBackboneService.java`

**Parâmetros**:
- `graph`: Graph (grafo não direcionado)

**Notação**:
- |V| = número de vértices (estações) = `graph.getStationCount()`
- |E| = número de arestas (conexões) = `graph.getAllUniqueConnections().size()`

---

## Estruturas de Dados Utilizadas

### 1. `Map<String, String> parent` (Union-Find)
- **Tipo**: HashMap
- **Tamanho**: |V| (uma entrada por vértice)
- **Operações dominantes**:
  - `put(key, value)`: O(1) amortizado
  - `get(key)`: O(1) amortizado
  - `containsKey(key)`: O(1) amortizado

### 2. `Map<String, Integer> rank` (Union-Find)
- **Tipo**: HashMap
- **Tamanho**: |V| (uma entrada por vértice)
- **Operações dominantes**:
  - `put(key, value)`: O(1) amortizado
  - `get(key)`: O(1) amortizado
  - `getOrDefault(key, defaultValue)`: O(1) amortizado

### 3. `List<Connection> sortedEdges`
- **Tipo**: ArrayList
- **Tamanho**: |E|
- **Operações dominantes**:
  - `sort(comparator)`: O(|E| log |E|) - Timsort
  - Iteração: O(|E|)
  - Acesso por índice: O(1)

### 4. `Set<Connection> allConnections` (do Graph)
- **Tipo**: HashSet (retornado por `getAllUniqueConnections()`)
- **Tamanho**: |E|
- **Operações dominantes**:
  - Iteração: O(|E|)
  - Conversão para ArrayList: O(|E|)

### 5. `List<Station> stations` (do Graph)
- **Tipo**: ArrayList
- **Tamanho**: |V|
- **Operações dominantes**:
  - Iteração: O(|V|)
  - Acesso por índice: O(1)

### 6. `Graph mstGraph` (resultado)
- **Tipo**: Graph (contém List<Station> e Map<String, List<Connection>>)
- **Tamanho**: |V| estações, |V| - 1 conexões (MST)
- **Operações dominantes**:
  - `addStation(station)`: O(1) amortizado
  - `addConnection(connection)`: O(1) amortizado

---

## Análise Fase por Fase

### FASE 1: Validações e Casos Especiais

**Linhas 137-143 do código**

```java
// Validações de entrada (linhas 137-143)
if (graph == null) { ... }                    // O(1)
if (graph.getStationCount() == 0) { ... }      // O(1) - acesso a contador
```

**Complexidade da Fase 1**: **O(1)**
- Todas as operações são constantes
- Não há loops
- Acesso a contador é O(1)

---

### FASE 2: Criação do Grafo MST e Adição de Estações

**Linhas 145-151 do código**

```java
// Criação do grafo MST (linha 146)
Graph mstGraph = new Graph();                  // O(1)

// Adição de todas as estações ao MST (linhas 149-151)
for (Station station : graph.getStations()) { // Itera |V| vezes
    mstGraph.addStation(station);              // O(1) por iteração
}
```

**Análise detalhada**:
- `graph.getStations()`: retorna `Collections.unmodifiableList(stations)`
  - Acesso à lista: O(1) para obter a view
  - Iteração sobre a lista: O(|V|)
- Cada iteração do loop:
  - `station`: O(1) - acesso por referência
  - `mstGraph.addStation(station)`: O(1) amortizado
    - Verifica se já existe: O(1) - HashMap.containsKey
    - Adiciona à lista: O(1) amortizado
    - Adiciona ao HashMap: O(1) amortizado
    - Inicializa lista de adjacência: O(1) amortizado

**Complexidade da Fase 2**: **O(|V|)**
- Loop executa exatamente |V| iterações
- Cada iteração tem custo O(1)
- Total: |V| × O(1) = O(|V|)

---

### FASE 3: Obtenção e Ordenação das Arestas

**Linhas 153-162 do código**

```java
// Obter todas as conexões únicas (linha 154)
Set<Connection> allConnections = graph.getAllUniqueConnections();  // O(|E|)

if (allConnections.isEmpty()) {              // O(1)
    return mstGraph;                         // O(1)
}

// Converter para lista e ordenar (linhas 161-162)
List<Connection> sortedEdges = new ArrayList<>(allConnections);    // O(|E|)
sortedEdges.sort(new EdgeComparator());                            // O(|E| log |E|)
```

**Análise detalhada**:

1. **`graph.getAllUniqueConnections()`**:
   - Retorna um `Set<Connection>` contendo todas as conexões únicas
   - A implementação itera sobre todas as conexões e remove duplicatas
   - **Complexidade**: O(|E|)

2. **`new ArrayList<>(allConnections)`**:
   - Construtor que copia todos os elementos do Set para ArrayList
   - **Complexidade**: O(|E|)

3. **`sortedEdges.sort(new EdgeComparator())`**:
   - Usa Timsort (algoritmo de ordenação do Java)
   - **Complexidade**: O(|E| log |E|)
   - O comparador `EdgeComparator` compara:
     - Primeiro por distância: O(1) - `Double.compare()`
     - Depois por originId: O(1) - `String.compareTo()` (assumindo comprimento limitado)
     - Por fim por destinationId: O(1) - `String.compareTo()` (assumindo comprimento limitado)
   - Cada comparação é O(1), então a ordenação é O(|E| log |E|)

**Complexidade da Fase 3**: **O(|E| log |E|)**
- Termo dominante: ordenação O(|E| log |E|)
- Outras operações são O(|E|), que é dominado por O(|E| log |E|)

---

### FASE 4: Inicialização do Union-Find

**Linhas 164-169 do código**

```java
// Coletar IDs das estações (linhas 165-168)
List<String> stationIds = new ArrayList<>();    // O(1)
for (Station station : graph.getStations()) {  // Itera |V| vezes
    stationIds.add(station.getId());           // O(1) por iteração
}

// Inicializar Union-Find (linha 169)
UnionFind uf = new UnionFind(stationIds);     // Ver análise abaixo
```

**Análise detalhada**:

1. **Loop de coleta de IDs**:
   - Executa |V| iterações
   - Cada iteração: `station.getId()` é O(1), `stationIds.add()` é O(1) amortizado
   - **Complexidade**: O(|V|)

2. **Construtor `UnionFind(List<String> stationIds)`** (linhas 35-43):
   ```java
   public UnionFind(List<String> stationIds) {
       parent = new HashMap<>();               // O(1)
       rank = new HashMap<>();                 // O(1)
       
       for (String id : stationIds) {         // Itera |V| vezes
           parent.put(id, id);                // O(1) por iteração
           rank.put(id, 0);                   // O(1) por iteração
       }
   }
   ```
   - Loop executa |V| iterações
   - Cada iteração: duas operações `put()` em HashMap, cada uma O(1) amortizado
   - **Complexidade**: O(|V|)

**Complexidade da Fase 4**: **O(|V|)**
- Coleta de IDs: O(|V|)
- Inicialização do Union-Find: O(|V|)
- Total: O(|V|) + O(|V|) = O(|V|)

---

### FASE 5: Algoritmo de Kruskal - Loop Principal

**Linhas 171-190 do código**

```java
// Inicialização (linhas 172-173)
int edgesAdded = 0;                            // O(1)
int targetEdges = graph.getStationCount() - 1; // O(1) - MST tem |V| - 1 arestas

// Loop principal de Kruskal (linhas 175-190)
for (Connection edge : sortedEdges) {          // Itera até |E| vezes
    if (edgesAdded >= targetEdges) {          // O(1) por iteração
        break;                                 // O(1) - pode terminar antes
    }
    
    String originId = edge.getOriginId();      // O(1)
    String destinationId = edge.getDestinationId(); // O(1)
    
    // Verificar se adicionar esta aresta criaria ciclo (linha 184)
    if (uf.union(originId, destinationId)) {  // Ver análise de union() abaixo
        mstGraph.addConnection(edge);         // O(1) amortizado
        edgesAdded++;                          // O(1)
    }
}
```

**Análise detalhada**:

1. **Loop principal**:
   - Itera sobre `sortedEdges`, que tem tamanho |E|
   - **No pior caso**: executa todas as |E| iterações
   - **No melhor caso**: termina após |V| - 1 iterações (quando MST está completo)
   - **Caso médio**: executa aproximadamente |E| iterações (depende da ordem das arestas)

2. **Operações por iteração**:
   - `edgesAdded >= targetEdges`: O(1)
   - `edge.getOriginId()`: O(1)
   - `edge.getDestinationId()`: O(1)
   - `uf.union(originId, destinationId)`: **O(α(|V|))** onde α é a função inversa de Ackermann (praticamente constante)
     - Ver análise detalhada de `union()` abaixo
   - `mstGraph.addConnection(edge)`: O(1) amortizado
     - Adiciona à lista de adjacência: O(1) amortizado
     - Adiciona conexão reversa (grafo não direcionado): O(1) amortizado

3. **Método `union()`** (linhas 73-95):
   ```java
   public boolean union(String stationId1, String stationId2) {
       String root1 = find(stationId1);        // O(α(|V|))
       String root2 = find(stationId2);        // O(α(|V|))
       
       if (root1.equals(root2)) {             // O(1)
           return false;                       // O(1)
       }
       
       // Union by rank (linhas 82-92)
       int rank1 = rank.getOrDefault(root1, 0);  // O(1)
       int rank2 = rank.getOrDefault(root2, 0);  // O(1)
       
       if (rank1 < rank2) {                   // O(1)
           parent.put(root1, root2);           // O(1)
       } else if (rank1 > rank2) {            // O(1)
           parent.put(root2, root1);          // O(1)
       } else {                               // O(1)
           parent.put(root2, root1);          // O(1)
           rank.put(root1, rank1 + 1);        // O(1)
       }
       
       return true;                           // O(1)
   }
   ```
   - Chama `find()` duas vezes: 2 × O(α(|V|)) = O(α(|V|))
   - Operações restantes: O(1)
   - **Complexidade total**: O(α(|V|))

4. **Método `find()`** (linhas 52-63):
   ```java
   public String find(String stationId) {
       if (!parent.containsKey(stationId)) {  // O(1)
           return stationId;                   // O(1)
       }
       
       // Path compression (linhas 58-60)
       if (!parent.get(stationId).equals(stationId)) {  // O(1)
           parent.put(stationId, find(parent.get(stationId)));  // Recursão
       }
       
       return parent.get(stationId);          // O(1)
   }
   ```
   - Com path compression e union by rank, a complexidade é **O(α(|V|))**
   - α(|V|) é a função inversa de Ackermann, que cresce extremamente lentamente
   - Para todos os propósitos práticos, α(|V|) ≤ 4 para qualquer valor realista de |V|
   - Portanto, **O(α(|V|)) ≈ O(1)** na prática, mas mantemos a notação correta

**Complexidade da Fase 5**:
- **Pior caso**: |E| iterações × O(α(|V|)) = **O(|E| × α(|V|))**
- **Melhor caso**: (|V| - 1) iterações × O(α(|V|)) = **O(|V| × α(|V|))**
- **Caso médio**: aproximadamente O(|E| × α(|V|))

Como α(|V|) é praticamente constante, podemos simplificar:
- **Complexidade prática**: **O(|E|)** no pior caso, **O(|V|)** no melhor caso

---

## Complexidade Total do Algoritmo

### Análise por Fase:

1. **Validações**: O(1)
2. **Criação do grafo MST e adição de estações**: O(|V|)
3. **Obtenção e ordenação das arestas**: O(|E| log |E|)
4. **Inicialização do Union-Find**: O(|V|)
5. **Algoritmo de Kruskal**: O(|E| × α(|V|))

### Termo Dominante:

- **O(|E| log |E|)** domina todas as outras fases
- O(|E| × α(|V|)) é dominado por O(|E| log |E|) porque:
  - α(|V|) é praticamente constante (≤ 4 para valores realistas)
  - log |E| cresce mais rápido que qualquer constante
- O(|V|) é dominado por O(|E| log |E|) porque:
  - Em grafos conectados, |E| ≥ |V| - 1
  - Para grafos não triviais, |E| log |E| ≥ |V|

### Complexidade Assintótica Final:

**Complexidade Temporal (Big-O)**: **O(|E| log |E|)**

Onde:
- |V| = número de vértices (estações)
- |E| = número de arestas (conexões)

### Justificação:

1. **Ordenação das arestas** (Fase 3) é o gargalo: O(|E| log |E|)
2. **Algoritmo de Kruskal** (Fase 5) é O(|E| × α(|V|)) ≈ O(|E|), que é dominado por O(|E| log |E|)
3. **Outras fases** são O(|V|) ou O(|E|), todas dominadas por O(|E| log |E|)

---

## Influência das Escolhas de Implementação

### 1. Uso de Union-Find com Path Compression e Union by Rank
- **Impacto**: Operações `find()` e `union()` são O(α(|V|)) ≈ O(1) na prática
- **Alternativa**: DFS para detecção de ciclos → O(|V|) por verificação, resultando em O(|E| × |V|) total
- **Escolha atual**: Muito mais eficiente, reduz complexidade de O(|E| × |V|) para O(|E| × α(|V|))

### 2. Ordenação das Arestas com Timsort
- **Impacto**: O(|E| log |E|) - complexidade ótima para ordenação baseada em comparação
- **Alternativa**: Heap (priority queue) → mesma complexidade O(|E| log |E|), mas overhead maior
- **Escolha atual**: Simples e eficiente, aproveita otimizações do Java

### 3. Comparador Determinístico (EdgeComparator)
- **Impacto**: O(1) por comparação, não afeta a complexidade assintótica
- **Benefício**: Garante saída determinística (mesma ordem de arestas para mesmo input)
- **Escolha atual**: Mantém complexidade O(|E| log |E|) enquanto garante determinismo

### 4. Uso de HashMap para Union-Find
- **Impacto**: Operações são O(1) amortizado
- **Alternativa**: Array indexado por ID → O(1) garantido, mas requer IDs numéricos consecutivos
- **Escolha atual**: Flexível (aceita qualquer String como ID), mantém O(1) na prática

### 5. Early Termination no Loop de Kruskal
- **Impacto**: Pode terminar após |V| - 1 iterações se as arestas estiverem bem ordenadas
- **Melhor caso**: O(|V| × α(|V|)) ≈ O(|V|) para o loop (mas ordenação ainda domina)
- **Pior caso**: Ainda O(|E| × α(|V|)) ≈ O(|E|)
- **Escolha atual**: Otimização útil, mas não muda a complexidade assintótica total (ordenação domina)

---

## Resumo Executivo

| Fase | Complexidade | Justificação |
|------|--------------|--------------|
| Validações | O(1) | Operações constantes, sem loops |
| Criação do grafo MST | O(\|V\|) | Loop sobre todas as estações |
| Ordenação das arestas | O(\|E\| log \|E\|) | Timsort sobre |E| elementos |
| Inicialização Union-Find | O(\|V\|) | Loop sobre todas as estações |
| Algoritmo de Kruskal | O(\|E\| × α(\|V\|)) | Loop sobre arestas, union O(α(\|V\|)) |

**Complexidade Assintótica Final**: **O(|E| log |E|)**

Onde:
- |V| = número de vértices (estações)
- |E| = número de arestas (conexões)
- α(|V|) = função inversa de Ackermann (praticamente constante)

Esta é a complexidade clássica do algoritmo de Kruskal com Union-Find otimizado, confirmada pela análise detalhada do código implementado.

**Nota**: A complexidade O(|E| log |E|) é dominada pela ordenação das arestas. O algoritmo de Kruskal em si (com Union-Find otimizado) tem complexidade O(|E| × α(|V|)) ≈ O(|E|), mas a ordenação é necessária e não pode ser evitada.

