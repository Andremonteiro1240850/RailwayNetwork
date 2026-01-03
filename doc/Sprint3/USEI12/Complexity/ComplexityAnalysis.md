# Análise de Complexidade Temporal - USEI12 (Minimal Backbone Network)

## Método Analisado: `MinimalBackboneService.computeMinimalBackbone()`

**Localização**: `src/main/java/Railway Network Optimization/Domain/MinimalBackboneService.java`

**Contexto da USEI12**: Este método calcula a rede mínima de ligações ferroviárias (Minimum Spanning Tree) que conecta todas as estações da Bélgica com o menor comprimento total de linha, usando o algoritmo de Kruskal.

**Parâmetros**:
- `graph`: Graph (grafo não direcionado representando a rede ferroviária belga)

**Notação**:
- |V| = número de vértices (estações ferroviárias) = `graph.getStationCount()`
- |E| = número de arestas (conexões entre estações) = `graph.getAllUniqueConnections().size()`

**Exemplo prático para a rede belga**: Suponhamos ~500 estações (|V| = 500) e ~2000 conexões (|E| = 2000)

---

## Estruturas de Dados Utilizadas na USEI12

### 1. `Map<String, String> parent` (Union-Find)
- **Tipo**: HashMap (tabela de hash do Java)
- **Tamanho**: |V| (uma entrada por estação)
- **Propósito na USEI12**: Guarda, para cada estação, qual é a estação "pai" no conjunto conectado. Usado para verificar rapidamente se duas estações já estão conectadas.
- **Operações dominantes**:
  - `put(key, value)`: O(1) amortizado - adicionar/atualizar uma entrada
  - `get(key)`: O(1) amortizado - obter o pai de uma estação
  - `containsKey(key)`: O(1) amortizado - verificar se uma estação existe

### 2. `Map<String, Integer> rank` (Union-Find)
- **Tipo**: HashMap
- **Tamanho**: |V| (uma entrada por estação)
- **Propósito na USEI12**: Guarda a "profundidade" de cada conjunto de estações conectadas. Usado para otimizar a união de conjuntos (union by rank).
- **Operações dominantes**:
  - `put(key, value)`: O(1) amortizado
  - `get(key)`: O(1) amortizado
  - `getOrDefault(key, defaultValue)`: O(1) amortizado

### 3. `List<Connection> sortedEdges`
- **Tipo**: ArrayList (lista dinâmica do Java)
- **Tamanho**: |E| (todas as conexões da rede)
- **Propósito na USEI12**: Lista ordenada de todas as conexões entre estações, ordenadas por distância (da mais curta para a mais longa). Esta ordenação é necessária para o algoritmo de Kruskal escolher sempre a conexão mais curta disponível.
- **Operações dominantes**:
  - `sort(comparator)`: O(|E| log |E|) - ordenação usando Timsort (algoritmo de ordenação do Java)
  - Iteração: O(|E|) - percorrer todas as conexões
  - Acesso por índice: O(1) - aceder a uma conexão específica

### 4. `Set<Connection> allConnections` (do Graph)
- **Tipo**: HashSet (conjunto de hash do Java)
- **Tamanho**: |E|
- **Propósito na USEI12**: Conjunto de todas as conexões únicas da rede ferroviária (sem duplicatas). Obtido diretamente do grafo que representa a rede belga.
- **Operações dominantes**:
  - Iteração: O(|E|) - percorrer todas as conexões
  - Conversão para ArrayList: O(|E|) - copiar para lista ordenável

### 5. `List<Station> stations` (do Graph)
- **Tipo**: ArrayList
- **Tamanho**: |V|
- **Propósito na USEI12**: Lista de todas as estações da rede ferroviária belga (ex: Bruxelas, Antuérpia, Gante, etc.).
- **Operações dominantes**:
  - Iteração: O(|V|) - percorrer todas as estações
  - Acesso por índice: O(1) - aceder a uma estação específica

### 6. `Graph mstGraph` (resultado)
- **Tipo**: Graph (contém List<Station> e Map<String, List<Connection>>)
- **Tamanho**: |V| estações, |V| - 1 conexões (MST sempre tem V-1 arestas)
- **Propósito na USEI12**: O grafo resultante que representa a rede mínima de ligações. Contém todas as estações mas apenas as conexões necessárias para conectá-las com o menor comprimento total.
- **Operações dominantes**:
  - `addStation(station)`: O(1) amortizado - adicionar uma estação
  - `addConnection(connection)`: O(1) amortizado - adicionar uma conexão

---

## Análise Fase por Fase - USEI12

### FASE 1: Validações e Casos Especiais

**Linhas 137-143 do código**

```java
if (graph == null) {
    throw new IllegalArgumentException("Graph cannot be null");
}

if (graph.getStationCount() == 0) {
    throw new IllegalArgumentException("Graph cannot be empty");
}
```

**O que faz na USEI12**: Verifica se o grafo da rede ferroviária existe e não está vazio antes de começar o cálculo.

**Análise detalhada**:
- `graph == null`: Verificação de referência nula - O(1) (constante)
- `graph.getStationCount() == 0`: Acesso a um contador (não itera sobre estações) - O(1) (constante)
- Não há loops, todas as operações são verificações simples

**Complexidade da Fase 1**: **O(1)**
- **Porquê**: Todas as operações são constantes, não dependem do número de estações ou conexões
- **Na prática**: Para a rede belga (500 estações, 2000 conexões), estas validações demoram menos de 0.001ms

---

### FASE 2: Criação do Grafo MST e Adição de Estações

**Linhas 145-151 do código**

```java
// Create new graph for MST
Graph mstGraph = new Graph();

// Add all stations to MST graph
for (Station station : graph.getStations()) {
    mstGraph.addStation(station);
}
```

**O que faz na USEI12**: Cria um novo grafo vazio e adiciona todas as estações da rede belga (ex: Bruxelas, Antuérpia, Gante, etc.) a este novo grafo. Ainda não adiciona conexões - isso acontece depois.

**Análise detalhada**:

1. **`Graph mstGraph = new Graph()`**:
   - Cria um objeto vazio - O(1) (constante)
   - Não depende do tamanho da rede

2. **`graph.getStations()`**:
   - Retorna uma vista imutável da lista de estações - O(1) (não copia, apenas cria uma vista)
   - A iteração sobre esta vista é O(|V|)

3. **Loop `for (Station station : graph.getStations())`**:
   - **Executa |V| vezes** (uma vez por cada estação da rede belga)
   - Para ~500 estações, executa 500 iterações

4. **`mstGraph.addStation(station)` por iteração**:
   - Verifica se a estação já existe: O(1) - `HashMap.containsKey()` (acesso direto por chave)
   - Adiciona à lista de estações: O(1) amortizado - `ArrayList.add()` (adiciona no fim)
   - Adiciona ao HashMap interno: O(1) amortizado - `HashMap.put()` (inserção em tabela de hash)
   - Inicializa lista de adjacência vazia: O(1) - criação de lista vazia
   - **Total por iteração**: O(1) amortizado

**Complexidade da Fase 2**: **O(|V|)**
- **Porquê**: O loop executa exatamente |V| iterações, e cada iteração tem custo O(1)
- **Fórmula**: |V| × O(1) = O(|V|)
- **Na prática**: Para ~500 estações, fazemos 500 operações simples, demora aproximadamente 0.5ms

---

### FASE 3: Obtenção e Ordenação das Arestas

**Linhas 153-162 do código**

```java
// Get all unique connections (edges)
Set<Connection> allConnections = graph.getAllUniqueConnections();

if (allConnections.isEmpty()) {
    return mstGraph; // No connections, return graph with only stations
}

// Sort edges deterministically: distance (asc), then originId (asc), then destinationId (asc)
List<Connection> sortedEdges = new ArrayList<>(allConnections);
sortedEdges.sort(new EdgeComparator());
```

**O que faz na USEI12**: Obtém todas as conexões entre estações (ex: Bruxelas-Antuérpia: 45km, Antuérpia-Gante: 58km, etc.) e ordena-as por distância, da mais curta para a mais longa. Esta ordenação é crucial para o algoritmo de Kruskal escolher sempre a conexão mais curta disponível.

**Análise detalhada**:

1. **`graph.getAllUniqueConnections()`**:
   - **O que faz**: Itera sobre todas as conexões da rede e remove duplicatas (como a rede é não direcionada, Bruxelas-Antuérpia e Antuérpia-Bruxelas são a mesma conexão)
   - **Complexidade**: O(|E|) - precisa de percorrer todas as conexões
   - **Na prática**: Para ~2000 conexões, percorre 2000 elementos

2. **`new ArrayList<>(allConnections)`**:
   - **O que faz**: Converte o Set (conjunto) de conexões para uma ArrayList (lista) para poder ordenar
   - **Complexidade**: O(|E|) - copia todos os elementos do Set para a lista
   - **Na prática**: Para ~2000 conexões, copia 2000 elementos

3. **`sortedEdges.sort(new EdgeComparator())`** - **ESTA É A OPERAÇÃO MAIS LENTA**:
   - **O que faz**: Ordena todas as conexões usando o algoritmo Timsort (algoritmo de ordenação do Java, uma variante otimizada do Merge Sort)
   - **Como funciona o EdgeComparator**:
     - Primeiro compara por distância (km): `Double.compare(c1.getDistance(), c2.getDistance())` - O(1)
     - Se as distâncias forem iguais, compara por ID da estação de origem: `c1.getOriginId().compareTo(c2.getOriginId())` - O(1) (assumindo IDs com comprimento limitado)
     - Se ainda forem iguais, compara por ID da estação de destino: `c1.getDestinationId().compareTo(c2.getDestinationId())` - O(1)
   - **Complexidade**: O(|E| log |E|) - o Timsort precisa de comparar cada conexão com outras várias vezes
   - **Porquê O(|E| log |E|)**: 
     - Para ordenar |E| elementos, o Timsort divide o problema em partes menores, ordena cada parte, e depois combina (merge)
     - O número de comparações cresce como |E| × log(|E|)
     - Para ~2000 conexões: 2000 × log(2000) ≈ 2000 × 11 ≈ 22,000 comparações
   - **Na prática**: Para ~2000 conexões, demora aproximadamente 5ms (a parte mais lenta do algoritmo)

**Complexidade da Fase 3**: **O(|E| log |E|)**
- **Porquê**: A ordenação O(|E| log |E|) domina as outras operações O(|E|)
- **Na prática**: Esta é a fase que demora mais tempo. Para ~2000 conexões, a ordenação é responsável por ~60% do tempo total do algoritmo

---

### FASE 4: Inicialização do Union-Find

**Linhas 164-169 do código**

```java
// Initialize Union-Find for cycle detection
List<String> stationIds = new ArrayList<>();
for (Station station : graph.getStations()) {
    stationIds.add(station.getId());
}
UnionFind uf = new UnionFind(stationIds);
```

**O que faz na USEI12**: Prepara a estrutura de dados Union-Find que será usada para verificar rapidamente se adicionar uma conexão criaria um ciclo. Inicialmente, cada estação está no seu próprio conjunto (nenhuma está conectada).

**Análise detalhada**:

1. **Loop de coleta de IDs** (linhas 165-168):
   - **O que faz**: Percorre todas as estações e guarda os seus IDs numa lista
   - **Executa |V| vezes** (uma vez por cada estação)
   - Por iteração:
     - `station.getId()`: O(1) - obter o ID da estação (ex: "BRU", "ANT", etc.)
     - `stationIds.add()`: O(1) amortizado - adicionar à lista (adiciona no fim)
   - **Complexidade**: O(|V|)
   - **Na prática**: Para ~500 estações, fazemos 500 operações simples

2. **Construtor `UnionFind(List<String> stationIds)`** (linhas 35-43):
   ```java
   public UnionFind(List<String> stationIds) {
       parent = new HashMap<>();               // O(1) - criar HashMap vazio
       rank = new HashMap<>();                 // O(1) - criar HashMap vazio
       
       for (String id : stationIds) {         // Itera |V| vezes
           parent.put(id, id);                // O(1) - cada estação é pai de si mesma
           rank.put(id, 0);                   // O(1) - rank inicial é 0
       }
   }
   ```
   - **O que faz**: Para cada estação, inicializa:
     - `parent`: cada estação é inicialmente pai de si mesma (ainda não há conexões)
     - `rank`: profundidade inicial é 0 (cada estação está sozinha)
   - **Executa |V| vezes** (uma vez por cada estação)
   - Por iteração:
     - `parent.put(id, id)`: O(1) amortizado - inserção em HashMap
     - `rank.put(id, 0)`: O(1) amortizado - inserção em HashMap
   - **Complexidade**: O(|V|)
   - **Na prática**: Para ~500 estações, fazemos 1000 operações (2 por estação), demora aproximadamente 0.5ms

**Complexidade da Fase 4**: **O(|V|)**
- **Porquê**: Coleta de IDs O(|V|) + Inicialização Union-Find O(|V|) = O(|V|)
- **Na prática**: Para ~500 estações, demora aproximadamente 0.5ms

---

### FASE 5: Algoritmo de Kruskal - Loop Principal

**Linhas 171-190 do código**

```java
// Kruskal's algorithm: add edges in sorted order if they don't create cycles
int edgesAdded = 0;
int targetEdges = graph.getStationCount() - 1; // MST has V-1 edges

for (Connection edge : sortedEdges) {
    if (edgesAdded >= targetEdges) {
        break; // MST is complete
    }
    
    String originId = edge.getOriginId();
    String destinationId = edge.getDestinationId();
    
    // Check if adding this edge would create a cycle
    if (uf.union(originId, destinationId)) {
        // No cycle, add edge to MST
        mstGraph.addConnection(edge);
        edgesAdded++;
    }
    // If union returns false, the edge would create a cycle, so skip it
}
```

**O que faz na USEI12**: Percorre as conexões ordenadas (da mais curta para a mais longa) e adiciona cada uma à rede mínima se não criar um ciclo. Para quando a rede mínima está completa (tem |V| - 1 conexões, o mínimo necessário para conectar todas as estações).

**Análise detalhada**:

1. **Inicialização** (linhas 172-173):
   - `edgesAdded = 0`: O(1) - constante
   - `targetEdges = graph.getStationCount() - 1`: O(1) - acesso a contador
   - **Porquê |V| - 1**: Uma MST sempre tem exatamente |V| - 1 arestas (o mínimo para conectar |V| vértices sem ciclos)

2. **Loop principal** (linha 175):
   - **Itera sobre `sortedEdges`**, que tem tamanho |E|
   - **No pior caso**: Executa todas as |E| iterações (se as conexões mais curtas estiverem no fim da lista ordenada)
   - **No melhor caso**: Termina após |V| - 1 iterações (se as |V| - 1 conexões mais curtas forem as primeiras e não criarem ciclos)
   - **Caso médio**: Executa aproximadamente |E| iterações (depende da ordem das arestas na lista ordenada)
   - **Na prática**: Para ~2000 conexões e ~500 estações, normalmente executa entre 500 e 2000 iterações

3. **Operações por iteração**:

   a. **`edgesAdded >= targetEdges`**: O(1) - comparação de inteiros
   
   b. **`edge.getOriginId()` e `edge.getDestinationId()`**: O(1) - acesso a atributos do objeto
   
   c. **`uf.union(originId, destinationId)`** - **ESTA É A OPERAÇÃO MAIS COMPLEXA POR ITERAÇÃO**:
      - **O que faz**: Verifica se as duas estações já estão no mesmo conjunto conectado. Se não estiverem, une os conjuntos. Se já estiverem, retorna `false` (adicionar esta conexão criaria um ciclo).
      - **Complexidade**: O(α(|V|)) onde α é a função inversa de Ackermann
      - **O que é α(|V|)**: Uma função que cresce extremamente lentamente. Para todos os propósitos práticos, α(|V|) ≤ 4 para qualquer valor realista de |V| (mesmo para milhões de estações!)
      - **Porquê é tão rápido**: O Union-Find usa duas otimizações:
        - **Path compression**: Quando encontra a raiz de um conjunto, "achata" o caminho para tornar futuras buscas mais rápidas
        - **Union by rank**: Sempre une o conjunto menor ao maior, mantendo a árvore balanceada
      - **Na prática**: Para ~500 estações, cada chamada `union()` demora menos de 0.001ms (praticamente instantâneo)
      - **Análise do método `union()`** (linhas 73-95):
        ```java
        public boolean union(String stationId1, String stationId2) {
            String root1 = find(stationId1);        // O(α(|V|))
            String root2 = find(stationId2);        // O(α(|V|))
            
            if (root1.equals(root2)) {             // O(1) - já estão conectadas
                return false;                       // O(1) - criaria ciclo
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
        - **Complexidade total**: O(α(|V|)) ≈ O(1) na prática
      
      d. **`mstGraph.addConnection(edge)`**: O(1) amortizado
         - Adiciona à lista de adjacência: O(1) amortizado
         - Adiciona conexão reversa (grafo não direcionado): O(1) amortizado
      
      e. **`edgesAdded++`**: O(1) - incremento de inteiro

4. **Análise do método `find()`** (linhas 52-63):
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
   - **O que faz**: Encontra a raiz do conjunto que contém a estação (a estação "pai" final)
   - **Path compression**: Durante a busca, "achata" o caminho ligando diretamente a estação à raiz, tornando futuras buscas mais rápidas
   - **Complexidade**: O(α(|V|)) com path compression e union by rank
   - **Na prática**: Para ~500 estações, cada chamada `find()` demora menos de 0.001ms

**Complexidade da Fase 5**:
- **Pior caso**: |E| iterações × O(α(|V|)) = **O(|E| × α(|V|))**
- **Melhor caso**: (|V| - 1) iterações × O(α(|V|)) = **O(|V| × α(|V|))**
- **Caso médio**: aproximadamente O(|E| × α(|V|))

Como α(|V|) é praticamente constante (≤ 4), podemos simplificar:
- **Complexidade prática**: **O(|E|)** no pior caso, **O(|V|)** no melhor caso
- **Na prática**: Para ~2000 conexões e ~500 estações, demora aproximadamente 2ms (muito mais rápido que a ordenação)

---

## Complexidade Total do Algoritmo - USEI12

### Análise por Fase:

1. **Validações**: O(1) - instantâneo
2. **Criação do grafo MST e adição de estações**: O(|V|) - proporcional ao número de estações
3. **Obtenção e ordenação das arestas**: O(|E| log |E|) - **DOMINANTE**
4. **Inicialização do Union-Find**: O(|V|) - proporcional ao número de estações
5. **Algoritmo de Kruskal**: O(|E| × α(|V|)) ≈ O(|E|) - proporcional ao número de conexões, mas muito rápido por iteração

### Termo Dominante:

- **O(|E| log |E|)** domina todas as outras fases
- **Porquê**:
  - O(|E| × α(|V|)) é dominado por O(|E| log |E|) porque:
    - α(|V|) é praticamente constante (≤ 4 para valores realistas)
    - log |E| cresce mais rápido que qualquer constante
    - Exemplo: para |E| = 2000, log(2000) ≈ 11, que é muito maior que α(|V|) ≈ 4
  - O(|V|) é dominado por O(|E| log |E|) porque:
    - Em grafos conectados (como a rede ferroviária), |E| ≥ |V| - 1
    - Para grafos não triviais (como a rede belga), |E| log |E| ≥ |V|
    - Exemplo: para |V| = 500 e |E| = 2000, temos 2000 × 11 = 22,000 que é muito maior que 500

### Complexidade Assintótica Final:

**Complexidade Temporal (Big-O)**: **O(|E| log |E|)**

Onde:
- |V| = número de vértices (estações) na rede ferroviária belga
- |E| = número de arestas (conexões) na rede ferroviária belga

### Justificação Específica para USEI12:

1. **Ordenação das arestas** (Fase 3) é o gargalo: O(|E| log |E|)
   - Para ~2000 conexões: 2000 × log(2000) ≈ 22,000 comparações
   - Demora aproximadamente 5ms (60% do tempo total)

2. **Algoritmo de Kruskal** (Fase 5) é O(|E| × α(|V|)) ≈ O(|E|), que é dominado por O(|E| log |E|)
   - Para ~2000 conexões: 2000 × α(500) ≈ 2000 × 4 ≈ 8,000 operações
   - Mas cada operação é muito rápida (Union-Find otimizado)
   - Demora aproximadamente 2ms (25% do tempo total)

3. **Outras fases** são O(|V|) ou O(|E|), todas dominadas por O(|E| log |E|)
   - Adicionar estações: ~500 operações, ~0.5ms
   - Inicializar Union-Find: ~1000 operações, ~0.5ms
   - Total das outras fases: ~1ms (15% do tempo total)

**Tempo total estimado para a rede belga**: ~8ms, sendo que a ordenação é responsável por ~60% deste tempo

---

## Influência das Escolhas de Implementação na USEI12

### 1. Uso de Union-Find com Path Compression e Union by Rank
- **Impacto na USEI12**: Operações `find()` e `union()` são O(α(|V|)) ≈ O(1) na prática
- **Alternativa**: DFS (Depth-First Search) para detecção de ciclos → O(|V|) por verificação, resultando em O(|E| × |V|) total
- **Escolha atual**: Muito mais eficiente
  - **Com Union-Find**: ~2000 conexões × ~0.001ms = ~2ms
  - **Com DFS**: ~2000 conexões × ~0.5ms = ~1000ms (500 vezes mais lento!)
- **Porquê é importante**: Para a rede belga, o Union-Find torna o algoritmo viável em tempo real

### 2. Ordenação das Arestas com Timsort
- **Impacto na USEI12**: O(|E| log |E|) - complexidade ótima para ordenação baseada em comparação
- **Alternativa**: Heap (priority queue) → mesma complexidade O(|E| log |E|), mas overhead maior (mais operações de memória)
- **Escolha atual**: Simples e eficiente, aproveita otimizações do Java
- **Porquê é importante**: O Timsort é altamente otimizado pelo Java e funciona muito bem com dados parcialmente ordenados

### 3. Comparador Determinístico (EdgeComparator)
- **Impacto na USEI12**: O(1) por comparação, não afeta a complexidade assintótica
- **Benefício**: Garante saída determinística (mesma ordem de arestas para mesmo input)
- **Porquê é importante**: Para a rede belga, queremos sempre o mesmo resultado para os mesmos dados, facilitando testes e validação

### 4. Uso de HashMap para Union-Find
- **Impacto na USEI12**: Operações são O(1) amortizado
- **Alternativa**: Array indexado por ID → O(1) garantido, mas requer IDs numéricos consecutivos (ex: 0, 1, 2, ...)
- **Escolha atual**: Flexível (aceita qualquer String como ID, como "BRU", "ANT", etc.), mantém O(1) na prática
- **Porquê é importante**: As estações belgas têm IDs alfanuméricos (não numéricos), então HashMap é necessário

### 5. Early Termination no Loop de Kruskal
- **Impacto na USEI12**: Pode terminar após |V| - 1 iterações se as arestas estiverem bem ordenadas
- **Melhor caso**: O(|V| × α(|V|)) ≈ O(|V|) para o loop (mas ordenação ainda domina)
- **Pior caso**: Ainda O(|E| × α(|V|)) ≈ O(|E|)
- **Escolha atual**: Otimização útil, mas não muda a complexidade assintótica total (ordenação domina)
- **Porquê é importante**: Para a rede belga, se as conexões mais curtas forem as primeiras, o loop pode terminar mais cedo, economizando algumas iterações

---

## Resumo Executivo - USEI12

| Fase | Complexidade | O que faz na USEI12 | Tempo estimado (rede belga) |
|------|--------------|---------------------|----------------------------|
| Validações | O(1) | Verifica se o grafo existe | < 0.001ms |
| Criação do grafo MST | O(\|V\|) | Adiciona todas as estações ao novo grafo | ~0.5ms |
| Ordenação das arestas | O(\|E\| log \|E\|) | Ordena conexões por distância (da mais curta para a mais longa) | **~5ms (60%)** |
| Inicialização Union-Find | O(\|V\|) | Prepara estrutura para detecção de ciclos | ~0.5ms |
| Algoritmo de Kruskal | O(\|E\| × α(\|V\|)) | Adiciona conexões mais curtas que não criam ciclos | ~2ms (25%) |

**Complexidade Assintótica Final**: **O(|E| log |E|)**

Onde:
- |V| = número de vértices (estações) na rede ferroviária belga
- |E| = número de arestas (conexões) na rede ferroviária belga
- α(|V|) = função inversa de Ackermann (praticamente constante ≤ 4)

**Tempo total estimado para a rede belga** (~500 estações, ~2000 conexões): **~8ms**

Esta é a complexidade clássica do algoritmo de Kruskal com Union-Find otimizado, confirmada pela análise detalhada do código implementado na USEI12.

**Nota**: A complexidade O(|E| log |E|) é dominada pela ordenação das arestas. O algoritmo de Kruskal em si (com Union-Find otimizado) tem complexidade O(|E| × α(|V|)) ≈ O(|E|), mas a ordenação é necessária e não pode ser evitada. Para a rede ferroviária belga, a ordenação é responsável por aproximadamente 60% do tempo total de execução.
