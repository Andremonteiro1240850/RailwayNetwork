# Análise de Complexidade da USEI11 - Directed Line Upgrade Plan

## Definição de Variáveis de Complexidade
- $N$: O número de estações/vértices no grafo, $|V|$.
- $M$: O número de arestas direcionadas no grafo, $|E|$.
- $T(\cdot)$: Complexidade temporal.

---

## 1. Análise da Função `TopologicalSort.order` (Algoritmo de Kahn)
Esta função implementa o **Algoritmo de Kahn** para ordenação topológica, que calcula uma ordenação linear dos vértices respeitando as dependências direcionais das arestas.

### Complexidade 

| Código                                                                                         | Complexidade Worst Case | Complexidade Average Case | Complexidade Better Case | Explicação                                                                                                                                                           |
|------------------------------------------------------------------------------------------------|-------------------------|---------------------------|--------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `if (graph == null) { ... }`                                                                   | O(1)                    | O(1)                      | O(1)                     | Validação de entrada. Operação de tempo constante.                                                                                                                  |
| `Map<String, Integer> inDegree = new HashMap<>();`                                            | O(1)                    | O(1)                      | O(1)                     | Inicialização de um HashMap. É uma operação de tempo constante.                                                                                                      |
| `for (Station node : graph.getAllNodes()) { ... inDegree.put(node.getId(), 0); }`             | O(N²)                   | O(N)                      | O(N)                     | O ciclo itera N vezes. Operação put em HashMap. O(1) (médio), O(N) (pior). Complexidade acumulada: O(N²) (pior), O(N) (médio).                                        |
| `for (Line edge : graph.getAllEdges()) { ... inDegree.put(toId, inDegree.get(toId) + 1); }`   | O(M×N)                  | O(M)                      | O(M)                     | O ciclo itera M vezes. Duas operações get/put em HashMap. O(1) (médio), O(N) (pior). Complexidade acumulada: O(M×N) (pior), O(M) (médio).                            |
| `Queue<Station> queue = new LinkedList<>();`                                                    | O(1)                    | O(1)                      | O(1)                     | Inicialização de uma LinkedList. Tempo constante.                                                                                                                     |
| `for (Station node : graph.getAllNodes()) { if (inDegree.get(node.getId()) == 0) { ... } }`    | O(N²)                   | O(N)                      | O(N)                     | O ciclo itera N vezes. Operação get em HashMap. O(1) (médio), O(N) (pior). Complexidade acumulada: O(N²) (pior), O(N) (médio).                                       |
| `List<Station> result = new ArrayList<>();`                                                    | O(1)                    | O(1)                      | O(1)                     | Inicialização de um ArrayList. Tempo constante.                                                                                                                      |
| `while (!queue.isEmpty()) { ... }`                                                              | O(N+M)                  | O(N+M)                    | O(N+M)                   | O loop executa no máximo N vezes (uma por vértice) e processa todas as M arestas no total.                                                                          |
| `Station current = queue.poll();`                                                               | O(N)                    | O(1)                      | O(1)                     | Operação poll em LinkedList. O(1) (médio), O(N) (pior caso teórico). N chamadas acumulam O(N²) (pior), O(N) (médio).                                                  |
| `result.add(current);`                                                                          | O(N)                    | O(1)                      | O(1)                     | Operação add em ArrayList. O(1) amortizado (médio), O(N) quando precisa redimensionar (pior). N chamadas acumulam O(N²) (pior), O(N) (médio).                        |
| `for (Line edge : graph.getOutgoingEdges(current.getId())) { ... }`                             | O(M) (total)            | O(M) (total)              | O(M) (total)             | O número total de iterações do ciclo de arestas, em todas as execuções do while, é M.                                                                               |
| `String neighborId = edge.getArrivalStationId();`                                               | O(1)                    | O(1)                      | O(1)                     | Acesso a propriedade. Tempo constante.                                                                                                                               |
| `int newInDegree = inDegree.get(neighborId) - 1;`                                               | O(M×N)                  | O(M)                      | O(M)                     | Operação get em HashMap. O(1) (médio), O(N) (pior). M chamadas acumulam O(M×N) (pior), O(M) (médio).                                                                 |
| `inDegree.put(neighborId, newInDegree);`                                                        | O(M×N)                  | O(M)                      | O(M)                     | Operação put em HashMap. O(1) (médio), O(N) (pior). M chamadas acumulam O(M×N) (pior), O(M) (médio).                                                                 |
| `if (newInDegree == 0) { queue.offer(graph.getNode(neighborId)); }`                             | O(M×N)                  | O(M)                      | O(M)                     | Operação getNode e offer. O(1) (médio), O(N) (pior). M chamadas acumulam O(M×N) (pior), O(M) (médio).                                                                |
| `if (result.size() != graph.getNodeCount()) { ... }`                                            | O(1)                    | O(1)                      | O(1)                     | Comparação de tamanhos. Tempo constante.                                                                                                                              |

### Complexidade Total
- **Pior Caso (Worst Case):** $\mathbf{O(N^2 + M \cdot N)}$
- **Caso Médio (Average Case):** $\mathbf{O(N+M)}$

---

## 2. Análise da Função `TopologicalSort.orderDFS` (Algoritmo DFS)
Esta função implementa ordenação topológica usando **Depth-First Search (DFS)** com detecção de ciclos através de coloração de vértices.

### Complexidade 

| Código                                                                                         | Complexidade Worst Case | Complexidade Average Case | Complexidade Better Case | Explicação                                                                                                                                                           |
|------------------------------------------------------------------------------------------------|-------------------------|---------------------------|--------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `if (graph == null) { ... }`                                                                   | O(1)                    | O(1)                      | O(1)                     | Validação de entrada. Operação de tempo constante.                                                                                                                  |
| `Map<String, Color> color = new HashMap<>();`                                                  | O(1)                    | O(1)                      | O(1)                     | Inicialização de um HashMap. É uma operação de tempo constante.                                                                                                      |
| `List<Station> result = new ArrayList<>();`                                                    | O(1)                    | O(1)                      | O(1)                     | Inicialização de um ArrayList. Tempo constante.                                                                                                                      |
| `for (Station node : graph.getAllNodes()) { color.put(node.getId(), Color.WHITE); }`           | O(N²)                   | O(N)                      | O(N)                     | O ciclo itera N vezes. Operação put em HashMap. O(1) (médio), O(N) (pior). Complexidade acumulada: O(N²) (pior), O(N) (médio).                                        |
| `for (Station node : graph.getAllNodes()) { if (color.get(node.getId()) == Color.WHITE) { ... } }` | O(N²)                   | O(N)                      | O(N)                     | O ciclo itera N vezes. Operação get em HashMap. O(1) (médio), O(N) (pior). Complexidade acumulada: O(N²) (pior), O(N) (médio).                                        |
| `dfsVisit(graph, node, color, result);`                                                         | O(N+M)                  | O(N+M)                    | O(N+M)                   | DFS visita cada vértice uma vez e cada aresta uma vez. Complexidade total: O(N+M).                                                                                  |
| `Collections.reverse(result);`                                                                 | O(N)                    | O(N)                      | O(N)                     | Reversão de lista com N elementos. Tempo linear.                                                                                                                     |
| `if (result.size() != graph.getNodeCount()) { ... }`                                            | O(1)                    | O(1)                      | O(1)                     | Comparação de tamanhos. Tempo constante.                                                                                                                              |

### Análise da Função `dfsVisit` (Helper)

| Código                                                                                         | Complexidade Worst Case | Complexidade Average Case | Complexidade Better Case | Explicação                                                                                                                                                           |
|------------------------------------------------------------------------------------------------|-------------------------|---------------------------|--------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `color.put(current.getId(), Color.GRAY);`                                                      | O(N)                    | O(1)                      | O(1)                     | Operação put em HashMap. O(1) (médio), O(N) (pior). N chamadas acumulam O(N²) (pior), O(N) (médio).                                                                  |
| `for (Line edge : graph.getOutgoingEdges(current.getId())) { ... }`                             | O(M) (total)            | O(M) (total)              | O(M) (total)             | O número total de iterações sobre todas as arestas é M.                                                                                                             |
| `String neighborId = edge.getArrivalStationId();`                                               | O(1)                    | O(1)                      | O(1)                     | Acesso a propriedade. Tempo constante.                                                                                                                               |
| `Station neighbor = graph.getNode(neighborId);`                                                 | O(M×N)                  | O(M)                      | O(M)                     | Operação getNode. O(1) (médio), O(N) (pior). M chamadas acumulam O(M×N) (pior), O(M) (médio).                                                                       |
| `Color neighborColor = color.get(neighborId);`                                                  | O(M×N)                  | O(M)                      | O(M)                     | Operação get em HashMap. O(1) (médio), O(N) (pior). M chamadas acumulam O(M×N) (pior), O(M) (médio).                                                                 |
| `if (neighborColor == Color.GRAY) { throw new CycleDetectedException(...); }`                  | O(1)                    | O(1)                      | O(1)                     | Comparação e lançamento de exceção. Tempo constante (no pior caso, pode ocorrer uma vez).                                                                           |
| `if (neighborColor == Color.WHITE) { dfsVisit(...); }`                                         | O(N+M)                  | O(N+M)                    | O(N+M)                   | Chamada recursiva. Cada vértice é visitado uma vez e cada aresta é processada uma vez.                                                                              |
| `color.put(current.getId(), Color.BLACK);`                                                     | O(N)                    | O(1)                      | O(1)                     | Operação put em HashMap. O(1) (médio), O(N) (pior). N chamadas acumulam O(N²) (pior), O(N) (médio).                                                                  |
| `result.add(current);`                                                                          | O(N)                    | O(1)                      | O(1)                     | Operação add em ArrayList. O(1) amortizado (médio), O(N) quando precisa redimensionar (pior). N chamadas acumulam O(N²) (pior), O(N) (médio).                        |

### Complexidade Total
- **Pior Caso (Worst Case):** $\mathbf{O(N^2 + M \cdot N)}$
- **Caso Médio (Average Case):** $\mathbf{O(N+M)}$

---

## 3. Análise da Função `CycleDetection.hasCycles`
Esta função detecta se o grafo contém ciclos usando **DFS com coloração de vértices** (WHITE/GRAY/BLACK).

### Complexidade 

| Código                                                                                         | Complexidade Worst Case | Complexidade Average Case | Complexidade Better Case | Explicação                                                                                                                                                           |
|------------------------------------------------------------------------------------------------|-------------------------|---------------------------|--------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `if (graph == null) { ... }`                                                                   | O(1)                    | O(1)                      | O(1)                     | Validação de entrada. Operação de tempo constante.                                                                                                                  |
| `Map<String, Color> color = new HashMap<>();`                                                  | O(1)                    | O(1)                      | O(1)                     | Inicialização de um HashMap. É uma operação de tempo constante.                                                                                                      |
| `for (Station node : graph.getAllNodes()) { color.put(node.getId(), Color.WHITE); }`           | O(N²)                   | O(N)                      | O(N)                     | O ciclo itera N vezes. Operação put em HashMap. O(1) (médio), O(N) (pior). Complexidade acumulada: O(N²) (pior), O(N) (médio).                                        |
| `for (Station node : graph.getAllNodes()) { if (color.get(node.getId()) == Color.WHITE) { ... } }` | O(N²)                   | O(N)                      | O(N)                     | O ciclo itera N vezes. Operação get em HashMap. O(1) (médio), O(N) (pior). Complexidade acumulada: O(N²) (pior), O(N) (médio).                                        |
| `if (dfsHasCycle(graph, node, color)) { return true; }`                                        | O(N+M)                  | O(N+M)                    | O(N+M)                   | DFS visita cada vértice uma vez e cada aresta uma vez. Complexidade total: O(N+M).                                                                                  |

### Análise da Função `dfsHasCycle` (Helper)

| Código                                                                                         | Complexidade Worst Case | Complexidade Average Case | Complexidade Better Case | Explicação                                                                                                                                                           |
|------------------------------------------------------------------------------------------------|-------------------------|---------------------------|--------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `color.put(nodeId, Color.GRAY);`                                                                | O(N)                    | O(1)                      | O(1)                     | Operação put em HashMap. O(1) (médio), O(N) (pior). N chamadas acumulam O(N²) (pior), O(N) (médio).                                                                  |
| `for (Line edge : graph.getOutgoingEdges(nodeId)) { ... }`                                     | O(M) (total)            | O(M) (total)              | O(M) (total)             | O número total de iterações sobre todas as arestas é M.                                                                                                             |
| `String neighborId = edge.getArrivalStationId();`                                               | O(1)                    | O(1)                      | O(1)                     | Acesso a propriedade. Tempo constante.                                                                                                                               |
| `Color neighborColor = color.get(neighborId);`                                                  | O(M×N)                  | O(M)                      | O(M)                     | Operação get em HashMap. O(1) (médio), O(N) (pior). M chamadas acumulam O(M×N) (pior), O(M) (médio).                                                                 |
| `if (neighborColor == Color.GRAY) { return true; }`                                            | O(1)                    | O(1)                      | O(1)                     | Comparação e retorno. Tempo constante (no pior caso, pode ocorrer uma vez).                                                                                          |
| `if (neighborColor == Color.WHITE) { ... dfsHasCycle(...); }`                                 | O(N+M)                  | O(N+M)                    | O(N+M)                   | Chamada recursiva. Cada vértice é visitado uma vez e cada aresta é processada uma vez.                                                                              |
| `color.put(nodeId, Color.BLACK);`                                                              | O(N)                    | O(1)                      | O(1)                     | Operação put em HashMap. O(1) (médio), O(N) (pior). N chamadas acumulam O(N²) (pior), O(N) (médio).                                                                  |

### Complexidade Total
- **Pior Caso (Worst Case):** $\mathbf{O(N^2 + M \cdot N)}$
- **Caso Médio (Average Case):** $\mathbf{O(N+M)}$

---

## 4. Análise da Função `CycleDetection.findCycles`
Esta função encontra todos os ciclos no grafo e reconstrói os caminhos dos ciclos usando **DFS com coloração e reconstrução de caminhos**.

### Complexidade 

| Código                                                                                         | Complexidade Worst Case | Complexidade Average Case | Complexidade Better Case | Explicação                                                                                                                                                           |
|------------------------------------------------------------------------------------------------|-------------------------|---------------------------|--------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `if (graph == null) { ... }`                                                                   | O(1)                    | O(1)                      | O(1)                     | Validação de entrada. Operação de tempo constante.                                                                                                                  |
| `Set<String> cycleNodes = new HashSet<>();`                                                    | O(1)                    | O(1)                      | O(1)                     | Inicialização de um HashSet. Tempo constante.                                                                                                                        |
| `Set<Line> cycleEdges = new HashSet<>();`                                                      | O(1)                    | O(1)                      | O(1)                     | Inicialização de um HashSet. Tempo constante.                                                                                                                        |
| `Map<String, Color> color = new HashMap<>();`                                                  | O(1)                    | O(1)                      | O(1)                     | Inicialização de um HashMap. É uma operação de tempo constante.                                                                                                      |
| `Map<String, Station> parent = new HashMap<>();`                                               | O(1)                    | O(1)                      | O(1)                     | Inicialização de um HashMap. Tempo constante.                                                                                                                        |
| `List<List<Station>> cycles = new ArrayList<>();`                                               | O(1)                    | O(1)                      | O(1)                     | Inicialização de um ArrayList. Tempo constante.                                                                                                                      |
| `for (Station node : graph.getAllNodes()) { color.put(node.getId(), Color.WHITE); }`           | O(N²)                   | O(N)                      | O(N)                     | O ciclo itera N vezes. Operação put em HashMap. O(1) (médio), O(N) (pior). Complexidade acumulada: O(N²) (pior), O(N) (médio).                                        |
| `for (Station node : graph.getAllNodes()) { if (color.get(node.getId()) == Color.WHITE) { ... } }` | O(N²)                   | O(N)                      | O(N)                     | O ciclo itera N vezes. Operação get em HashMap. O(1) (médio), O(N) (pior). Complexidade acumulada: O(N²) (pior), O(N) (médio).                                        |
| `dfsFindCycles(graph, node, color, parent, cycleNodes, cycleEdges, cycles);`                   | O(N+M+C)                | O(N+M+C)                  | O(N+M+C)                 | DFS visita cada vértice uma vez, cada aresta uma vez, e reconstrói C ciclos. C pode ser até O(N) no pior caso.                                                      |
| `return new CycleInfo(cycleNodes, cycleEdges, cycles);`                                         | O(N+M+C)                | O(N+M+C)                  | O(N+M+C)                 | Criação de cópias dos conjuntos e listas. O(N) para cycleNodes, O(M) para cycleEdges, O(C×L) para cycles, onde L é o comprimento médio dos ciclos.                  |

### Análise da Função `dfsFindCycles` (Helper)

| Código                                                                                         | Complexidade Worst Case | Complexidade Average Case | Complexidade Better Case | Explicação                                                                                                                                                           |
|------------------------------------------------------------------------------------------------|-------------------------|---------------------------|--------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `color.put(nodeId, Color.GRAY);`                                                               | O(N)                    | O(1)                      | O(1)                     | Operação put em HashMap. O(1) (médio), O(N) (pior). N chamadas acumulam O(N²) (pior), O(N) (médio).                                                                  |
| `for (Line edge : graph.getOutgoingEdges(nodeId)) { ... }`                                     | O(M) (total)            | O(M) (total)              | O(M) (total)             | O número total de iterações sobre todas as arestas é M.                                                                                                             |
| `String neighborId = edge.getArrivalStationId();`                                               | O(1)                    | O(1)                      | O(1)                     | Acesso a propriedade. Tempo constante.                                                                                                                               |
| `Color neighborColor = color.get(neighborId);`                                                  | O(M×N)                  | O(M)                      | O(M)                     | Operação get em HashMap. O(1) (médio), O(N) (pior). M chamadas acumulam O(M×N) (pior), O(M) (médio).                                                                 |
| `if (neighborColor == Color.GRAY) { ... reconstructCycle(...); ... }`                          | O(N+C×L)                | O(C×L)                    | O(C×L)                   | Reconstrução de ciclo. L é o comprimento médio do ciclo. C é o número de ciclos encontrados. No pior caso, L pode ser O(N).                                         |
| `cycleEdges.add(edge);`                                                                         | O(M)                    | O(1)                      | O(1)                     | Operação add em HashSet. O(1) (médio), O(M) (pior). M chamadas acumulam O(M²) (pior), O(M) (médio).                                                                  |
| `cycleNodes.add(nodeId); cycleNodes.add(neighborId);`                                          | O(M)                    | O(1)                      | O(1)                     | Operação add em HashSet. O(1) (médio), O(M) (pior). M chamadas acumulam O(M²) (pior), O(M) (médio).                                                                 |
| `if (neighborColor == Color.WHITE) { parent.put(neighborId, node); dfsFindCycles(...); }`     | O(N+M)                  | O(N+M)                    | O(N+M)                   | Chamada recursiva. Cada vértice é visitado uma vez e cada aresta é processada uma vez.                                                                              |
| `color.put(nodeId, Color.BLACK);`                                                              | O(N)                    | O(1)                      | O(1)                     | Operação put em HashMap. O(1) (médio), O(N) (pior). N chamadas acumulam O(N²) (pior), O(N) (médio).                                                                  |

### Análise da Função `reconstructCycle` (Helper)

| Código                                                                                         | Complexidade Worst Case | Complexidade Average Case | Complexidade Better Case | Explicação                                                                                                                                                           |
|------------------------------------------------------------------------------------------------|-------------------------|---------------------------|--------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `List<Station> cycle = new ArrayList<>();`                                                     | O(1)                    | O(1)                      | O(1)                     | Inicialização de um ArrayList. Tempo constante.                                                                                                                      |
| `if (start.equals(end)) { ... }`                                                               | O(1)                    | O(1)                      | O(1)                     | Comparação e adição. Tempo constante.                                                                                                                                |
| `while (current != null && !current.equals(end) && !visited.contains(current.getId())) { ... }` | O(N)                    | O(L)                      | O(L)                     | Loop reconstrói o ciclo percorrendo o caminho de parent. L é o comprimento do ciclo. No pior caso, L pode ser O(N).                                                |
| `cycle.add(current);`                                                                           | O(N)                    | O(1)                      | O(1)                     | Operação add em ArrayList. O(1) amortizado (médio), O(N) quando precisa redimensionar (pior).                                                                      |
| `visited.add(current.getId());`                                                                | O(N)                    | O(1)                      | O(1)                     | Operação add em HashSet. O(1) (médio), O(N) (pior).                                                                                                                |
| `current = parent.get(current.getId());`                                                       | O(N)                    | O(1)                      | O(1)                     | Operação get em HashMap. O(1) (médio), O(N) (pior).                                                                                                                |

### Complexidade Total
- **Pior Caso (Worst Case):** $\mathbf{O(N^2 + M \cdot N + C \cdot N)}$ onde $C$ é o número de ciclos e pode ser até $O(N)$
- **Caso Médio (Average Case):** $\mathbf{O(N+M+C \cdot L)}$ onde $C$ é o número de ciclos e $L$ é o comprimento médio dos ciclos

---

## 5. Análise da Função `UpgradePlanController.calculateUpgradePlan`
Esta função orquestra a detecção de ciclos e a ordenação topológica.

### Complexidade 

| Código                                                                                         | Complexidade Worst Case | Complexidade Average Case | Complexidade Better Case | Explicação                                                                                                                                                           |
|------------------------------------------------------------------------------------------------|-------------------------|---------------------------|--------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `if (graph == null) { ... }`                                                                   | O(1)                    | O(1)                      | O(1)                     | Validação de entrada. Operação de tempo constante.                                                                                                                  |
| `CycleDetection.CycleInfo cycleInfo = CycleDetection.findCycles(graph);`                        | O(N²+M×N+C×N)          | O(N+M+C×L)                | O(N+M+C×L)               | Chamada à função findCycles. Complexidade conforme análise anterior.                                                                                                  |
| `if (cycleInfo.hasCycles()) { ... }`                                                            | O(1)                    | O(1)                      | O(1)                     | Verificação de conjunto vazio. Tempo constante.                                                                                                                     |
| `List<Station> topologicalOrder = TopologicalSort.order(graph);`                                | O(N²+M×N)              | O(N+M)                    | O(N+M)                   | Chamada à função order (Algoritmo de Kahn). Complexidade conforme análise anterior.                                                                                  |
| `return new UpgradePlanResult(...);`                                                             | O(N)                    | O(N)                      | O(N)                     | Criação do objeto resultado. O(N) para copiar a lista de ordenação topológica (se existir).                                                                        |

### Complexidade Total
- **Pior Caso (Worst Case):** $\mathbf{O(N^2 + M \cdot N + C \cdot N)}$ onde $C$ é o número de ciclos
- **Caso Médio (Average Case):** $\mathbf{O(N+M+C \cdot L)}$ onde $C$ é o número de ciclos e $L$ é o comprimento médio dos ciclos

**Nota:** A complexidade é dominada pela detecção de ciclos quando existem ciclos, ou pela ordenação topológica quando não existem ciclos. No caso médio, ambas as operações têm complexidade $\mathbf{O(N+M)}$.

---

## Resumo da Complexidade Temporal da USEI11

| Função                                                                 | Pior Caso                    | Caso Médio           |
|------------------------------------------------------------------------|------------------------------|----------------------|
| `TopologicalSort.order` (Kahn)                                        | $\mathbf{O(N^2 + M \cdot N)}$ | $\mathbf{O(N+M)}$    |
| `TopologicalSort.orderDFS` (DFS)                                      | $\mathbf{O(N^2 + M \cdot N)}$ | $\mathbf{O(N+M)}$    |
| `CycleDetection.hasCycles`                                            | $\mathbf{O(N^2 + M \cdot N)}$ | $\mathbf{O(N+M)}$    |
| `CycleDetection.findCycles`                                           | $\mathbf{O(N^2 + M \cdot N + C \cdot N)}$ | $\mathbf{O(N+M+C \cdot L)}$ |
| `UpgradePlanController.calculateUpgradePlan`                          | $\mathbf{O(N^2 + M \cdot N + C \cdot N)}$ | $\mathbf{O(N+M+C \cdot L)}$ |

Onde:
- $N$ = número de vértices (estações)
- $M$ = número de arestas (linhas direcionadas)
- $C$ = número de ciclos encontrados
- $L$ = comprimento médio dos ciclos

**Observação:** No caso médio, as operações de HashMap e HashSet têm complexidade O(1), resultando em complexidade linear $\mathbf{O(N+M)}$ para os algoritmos principais. No pior caso, devido a colisões de hash, essas operações podem degradar para O(N), resultando em complexidade quadrática.

