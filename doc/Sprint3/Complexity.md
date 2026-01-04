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

---

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

---
# Análise de Complexidade da USEI13 - Rail Hub Centrality Analysis


## Definição de Variáveis de Complexidade
- $N$: O número de estações/vértices no grafo, $|V|$.
- $M$: O número de arestas no grafo não-direcionado, $|E|$.
- $T(\cdot)$: Complexidade temporal.

---

# Análise de Complexidade - Betweenness Centrality

## Análise da Função `calculateBetweennessCentrality`
Esta função implementa o **Algoritmo de Brandes**, que calcula a Betweenness Centrality executando um algoritmo de **Single-Source Shortest Path (SSSP)**, neste caso, um Dijkstra modificado, a partir de cada um dos $N$ vértices.

## Complexidade por Operação

| Código                                                                   | Complexidade Worst Case | Complexidade Average Case | Complexidade Best Case | Explicação                                                      |
|--------------------------------------------------------------------------|-------------------------|---------------------------|------------------------|-----------------------------------------------------------------|
| `Map<Station, Double> centralidades = new HashMap<>();`                  | O(1)                    | O(1)                      | O(1)                   | Inicialização de HashMap vazio                                  |
| `for (Station s : undirectGraph.keySet())` (inicialização centralidades) | O(N)                    | O(N)                      | O(N)                   | Itera N estações                                                |
| `centralidades.put(s, 0.0);`                                             | O(1)                    | O(1)                      | O(1)                   | Operação put em HashMap (análise amortizada padrão)             |
| `for (Station origem : undirectGraph.keySet())`                          | O(N)                    | O(N)                      | O(N)                   | Loop externo: N iterações (uma por vértice origem)              |
| Inicialização de estruturas (Stack, Maps)                                | O(1)                    | O(1)                      | O(1)                   | Tempo constante                                                 |
| `for (Station s : undirectGraph.keySet())` (loop inicialização)          | O(N)                    | O(N)                      | O(N)                   | Inicializa estruturas para N estações                           |
| `predecessores.put(s, new ArrayList<>());` etc.                          | O(1)                    | O(1)                      | O(1)                   | 4 operações put em HashMap (análise amortizada)                 |
| `sigma.put(origem, 1);` etc.                                             | O(1)                    | O(1)                      | O(1)                   | 2 operações put                                                 |
| `PriorityQueue<Object[]> fila = new PriorityQueue<>(...)`                | O(1)                    | O(1)                      | O(1)                   | Criação da heap vazia                                           |
| `fila.add(new Object[]{origem, 0.0});`                                   | O(log N)                | O(log N)                  | O(1)                   | Inserção em heap. Best case quando heap está vazia              |
| `Set<Station> visitados = new HashSet<>();`                              | O(1)                    | O(1)                      | O(1)                   | Criação do Set                                                  |
| `while (!fila.isEmpty())`                                                | O(N + M)                | O(N + M)                  | O(N + M)               | Processa cada vértice e cada aresta. Total de iterações fixo    |
| `Object[] current = fila.poll();`                                        | O(log N)                | O(log N)                  | O(log N)               | Extração da heap: até N extrações = O(N log N) total            |
| `if (visitados.contains(v)) continue;`                                   | O(1)                    | O(1)                      | O(1)                   | Verificação em HashSet (análise amortizada)                     |
| `visitados.add(v);`                                                      | O(1)                    | O(1)                      | O(1)                   | Inserção em HashSet (análise amortizada)                        |
| `stack.push(v);`                                                         | O(1)                    | O(1)                      | O(1)                   | Push em Stack                                                   |
| `for (Map.Entry<Station, Double> entry : ...)`                           | O(M)                    | O(M)                      | O(M)                   | Itera sobre todas as arestas (M total no somatório)             |
| `double novaDistancia = distancias.get(v) + peso;`                       | O(1)                    | O(1)                      | O(1)                   | Get em HashMap (análise amortizada)                             |
| `if (novaDistancia < distancias.get(w))`                                 | O(1)                    | O(1)                      | O(1)                   | Get em HashMap                                                  |
| `distancias.put(w, novaDistancia);`                                      | O(1)                    | O(1)                      | O(1)                   | Put em HashMap (análise amortizada)                             |
| `sigma.put(w, sigma.get(v));`                                            | O(1)                    | O(1)                      | O(1)                   | Get + Put em HashMap                                            |
| `predecessores.get(w).clear();`                                          | O(k)                    | O(k)                      | O(1)                   | k = número atual de predecessores. Best case quando lista vazia |
| `predecessores.get(w).add(v);`                                           | O(1)                    | O(1)                      | O(1)                   | Add em ArrayList (análise amortizada)                           |
| `fila.add(new Object[]{w, novaDistancia});`                              | O(log N)                | O(log N)                  | O(log N)               | Inserção em heap: até M inserções = O(M log N) total            |
| `Math.abs(novaDistancia - distancias.get(w)) < 1e-9`                     | O(1)                    | O(1)                      | O(1)                   | Get + comparação aritmética                                     |
| `sigma.put(w, sigma.get(w) + sigma.get(v));`                             | O(1)                    | O(1)                      | O(1)                   | 2 Gets + 1 Put (análise amortizada)                             |
| `while (!stack.isEmpty())` (acumulação)                                  | O(N + M)                | O(N + M)                  | O(N + M)               | N pops + M iterações sobre predecessores (total fixo)           |
| `Station w = stack.pop();`                                               | O(1)                    | O(1)                      | O(1)                   | Pop de Stack                                                    |
| `for (Station v : predecessores.get(w))`                                 | O(M)                    | O(M)                      | O(M)                   | Total de iterações = M (soma de todos os predecessores)         |
| `double contribuicao = (...)`                                            | O(1)                    | O(1)                      | O(1)                   | Gets em HashMap (análise amortizada)                            |
| `delta.put(v, delta.get(v) + contribuicao);`                             | O(1)                    | O(1)                      | O(1)                   | Get + Put (análise amortizada)                                  |
| `if (!w.equals(origem))`                                                 | O(1)                    | O(1)                      | O(1)                   | Comparação de referência                                        |
| `centralidades.put(w, centralidades.get(w) + delta.get(w));`             | O(1)                    | O(1)                      | O(1)                   | Gets + Put (análise amortizada)                                 |
| `for (Station s : centralidades.keySet())` (normalização)                | O(N)                    | O(N)                      | O(N)                   | Itera N estações                                                |
| `centralidades.put(s, centralidades.get(s) / 2.0);`                      | O(1)                    | O(1)                      | O(1)                   | Get + Put (análise amortizada)                                  |

## Complexidade Total do Algoritmo

Como executamos **N origens** (uma para cada vértice):

| Caso             | Complexidade             | Justificação                                                                                                                   |
|------------------|--------------------------|--------------------------------------------------------------------------------------------------------------------------------|
| **Worst Case**   | **O(N · (N + M) log N)** | Assume heap balanceada e operações HashMap O(1) amortizado. No pior caso teórico com adversário forçando colisões: O(N³ + N²M) |
| **Average Case** | **O(N · (N + M) log N)** | Análise esperada com funções hash uniformes. Comportamento típico em grafos reais                                              |

---

## 2. Análise da Função `djikstra`
Implementação do **algoritmo de Dijkstra** para SSSP.

| Código                                                                         | Complexidade Worst Case | Complexidade Average Case | Complexidade Better Case | Explicação                                                                                    |
|--------------------------------------------------------------------------------|-------------------------|---------------------------|--------------------------|-----------------------------------------------------------------------------------------------|
| `for(Station s : allStations){ ... caminhosCurtos.put(s, Double.MAX_VALUE); }` | O(N²)                   | O(N)                      | O(N)                     | Inicialização de N entradas no HashMap. O(1) (médio), O(N) (pior).                            |
| `fila.add(Map.entry(0.0, sOrig));`                                             | O(logN)                 | O(logN)                   | O(1)                     | Inserção numa Min-Heap. O(logN) (pior/médio), O(1) (melhor).                                  |
| `Set<Station> visitadas = new HashSet<>();`                                    | O(1)                    | O(1)                      | O(1)                     | Inicialização do HashSet.                                                                     |
| `while (!fila.isEmpty()) {`                                                    | O(N+M)                  | O(N+M)                    | O(N+M)                   | O loop executa N polls e M iterações nos vizinhos (total).                                    |
| `Map.Entry<Double, Station> entry = fila.poll();`                              | O(NlogN)                | O(NlogN)                  | O(N)                     | N operações poll, O(logN) cada. Total O(NlogN).                                               |
| `if (visitadas.contains(atual)) { continue; }`                                 | O(N²)                   | O(N)                      | O(N)                     | N operações contains em HashSet. O(1) (médio), O(N) (pior). Total O(N²) (pior), O(N) (médio). |
| `for(Map.Entry<Station, Double> vizinho : vizinhos.entrySet()){`               | O(M) (total)            | O(M) (total)              | O(M) (total)             | O número total de iterações do ciclo de vizinhos é M.                                         |
| `if (visitadas.contains(destino)) { continue; }`                               | O(M×N)                  | O(M)                      | O(M)                     | contains em HashSet. M chamadas. O(M×N) (pior), O(M) (médio).                                 |
| `if(novaDistancia < caminhosCurtos.get(destino)){`                             | O(M×N)                  | O(M)                      | O(M)                     | get em HashMap. M chamadas. O(M×N) (pior), O(M) (médio).                                      |
| `caminhosCurtos.put(destino, novaDistancia);`                                  | O(M×N)                  | O(M)                      | O(M)                     | put em HashMap. M chamadas. O(M×N) (pior), O(M) (médio).                                      |
| `fila.add(Map.entry(novaDistancia, destino));`                                 | O(MlogN)                | O(MlogN)                  | O(M)                     | O(M) operações add, O(logN) cada. Total O(MlogN).                                             |

### Complexidade Total
- **Pior Caso:** $\mathbf{O(N \cdot (N+M))}$
- **Caso Médio:** $\mathbf{O((N+M) \log N)}$

---

## 3. Análise da Função `calculateHarmonicCloseness`
Executa **Dijkstra N vezes** (APSP).

| Código                                                                           | Complexidade Worst Case | Complexidade Average Case | Complexidade Better Case | Explicação                                                                                                 |
|----------------------------------------------------------------------------------|-------------------------|---------------------------|--------------------------|------------------------------------------------------------------------------------------------------------|
| `for (Station s : allStations) {`                                                | O(N)                    | O(N)                      | O(N)                     | Ciclo Principal (APSP): itera N vezes, uma vez por estação de origem.                                      |
| `Map<Station, Double> caminhosCurtos = djikstra(s, undirectGraph, allStations);` | O(N⋅N⋅(N+M))            | O(N⋅(N+M)logN)            | O(N⋅(N+M))               | Chamada à função djikstra. A complexidade total é N × T(djikstra).                                         |
| `for(Station s2 : allStations){`                                                 | O(N)                    | O(N)                      | O(N)                     | Segundo ciclo aninhado, itera N vezes para a soma da Centralidade Harmónica.                               |
| `Double novaDistancia = caminhosCurtos.get(s2);`                                 | O(N²)                   | O(N)                      | O(N)                     | get em HashMap. O(1) (médio), O(N) (pior). Complexidade acumulada: N × O(N) = O(N²) (pior) e O(N) (médio). |
| `harmonicCloseness.put(s, hc);`                                                  | O(N)                    | O(1)                      | O(1)                     | put em HashMap. O(1) (médio), O(N) (pior).                                                                 |

### Complexidade Total
- **Pior Caso:** $\mathbf{O(N^2 \cdot (N+M))}$
- **Caso Médio:** $\mathbf{O(N \cdot (N+M) \log N)}$

---

## 4. Análise da Função `calculateStrengthNorm`
Calcula **Strength (grau ponderado)** e normaliza.

| Código                                                                                                       | Complexidade Worst Case | Complexidade Average Case | Complexidade Better Case | Explicação                                                                                                                |
|--------------------------------------------------------------------------------------------------------------|-------------------------|---------------------------|--------------------------|---------------------------------------------------------------------------------------------------------------------------|
| `Set<Station> allStations = new HashSet<>(undirectGraph.keySet());`                                          | O(N²)                   | O(N)                      | O(N)                     | N inserções no HashSet. O(1) (médio), O(N) (pior).                                                                        |
| `for(Map<Station, Double> destinos : undirectGraph.values()) { ... allStations.addAll(destinos.keySet()); }` | O(M⋅N)                  | O(N+M)                    | O(N+M)                   | O addAll totaliza O(M) inserções em HashSet. O(1) (médio), O(N) (pior) por inserção. Total O(M⋅N) (pior), O(N+M) (médio). |
| `for(Station s : allStations) { ... strength.put(s, 0.0); }`                                                 | O(N²)                   | O(N)                      | O(N)                     | Inicialização de N entradas no HashMap. O(N) (médio), O(N²) (pior).                                                       |
| `for(Map.Entry<Station, Map<Station, Double>> outerEntry : undirectGraph.entrySet()) {`                      | O(N)                    | O(N)                      | O(N)                     | Itera sobre os N vértices (origem).                                                                                       |
| `for(Double peso : outerEntry.getValue().values()) { ... strength.merge(origem, peso, Double::sum); }`       | O(M⋅N)                  | O(M)                      | O(M)                     | O loop interno tem um total de M iterações. merge é O(1) (médio), O(N) (pior). Total O(M⋅N) (pior), O(M) (médio).         |
| `double maxStrength = strength.values().stream().max(Double::compareTo).orElse(0.0);`                        | O(N²)                   | O(N)                      | O(N)                     | Percorre os N valores. O(N) (médio), O(N²) (pior).                                                                        |
| `for(Map.Entry<Station, Double> entry : strength.entrySet()) { ... }`                                        | O(N²)                   | O(N)                      | O(N)                     | Normalização: Itera N vezes. Operação put O(N²) (pior), O(N) (médio).                                                     |

### Complexidade Total
- **Pior Caso:** $\mathbf{O(N^2 + M \cdot N)}$
- **Caso Médio:** $\mathbf{O(N+M)}$

---

## 5. Análise da Função `calculateHubScore`
Calcula **Hub Score** através de combinação linear das métricas.

| Código                                              | Complexidade Worst Case | Complexidade Average Case | Complexidade Better Case | Explicação                                                                                                    |
|-----------------------------------------------------|-------------------------|---------------------------|--------------------------|---------------------------------------------------------------------------------------------------------------|
| `for(Station s : allStations) {`                    | O(N)                    | O(N)                      | O(N)                     | O ciclo itera N vezes.                                                                                        |
| `if(!força.containsKey(s),,... ) { ... }`           | O(N²)                   | O(N)                      | O(N)                     | Checagem e inserção em HashMap. O(1) (médio), O(N) (pior). Total acumulado: O(N²) (pior), O(N) (médio).       |
| `double hubScore = (0.35 * betweness.get(s)) + ...` | O(N²)                   | O(N)                      | O(N)                     | Três operações get em HashMap. O(1) (médio), O(N) (pior). Complexidade acumulada: O(N²) (pior), O(N) (médio). |
| `hubScores.put(s, hubScore);`                       | O(N²)                   | O(N)                      | O(N)                     | Operação put em HashMap. O(1) (médio), O(N) (pior). Complexidade acumulada: O(N²) (pior), O(N) (médio).       |

### Complexidade Total
- **Pior Caso:** $\mathbf{O(N^2)}$
- **Caso Médio:** $\mathbf{O(N)}$

---

## 6. Análise da Função `calculateDegree`
Calcula o **grau de cada vértice**.

| Código                                                                                                       | Complexidade Worst Case | Complexidade Average Case | Complexidade Better Case | Explicação                                                                                             |
|--------------------------------------------------------------------------------------------------------------|-------------------------|---------------------------|--------------------------|--------------------------------------------------------------------------------------------------------|
| `Set<Station> allStations = new HashSet<>(undirectGraph.keySet());`                                          | O(N²)                   | O(N)                      | O(N)                     | Inicialização do HashSet com N elementos. O(N) (médio), O(N²) (pior).                                  |
| `for(Map<Station, Double> destinos : undirectGraph.values()) { ... allStations.addAll(destinos.keySet()); }` | O(M⋅N)                  | O(N+M)                    | O(N+M)                   | addAll de O(M) elementos. O(1) (médio), O(N) (pior) por inserção. Total O(M⋅N) (pior), O(N+M) (médio). |
| `for(Station s : allStations) { ... degree.put(s, 0); }`                                                     | O(N²)                   | O(N)                      | O(N)                     | Inicialização de N entradas no HashMap. O(N) (médio), O(N²) (pior).                                    |
| `for(Map.Entry<Station, Map<Station, Double>> entry : undirectGraph.entrySet()) {`                           | O(N)                    | O(N)                      | O(N)                     | Itera sobre os N vértices (origem).                                                                    |
| `int numConexoes = entry.getValue().size();`                                                                 | O(N)                    | O(N)                      | O(N)                     | size() de HashMap é O(1). Total O(N).                                                                  |
| `degree.put(origem, numConexoes);`                                                                           | O(N²)                   | O(N)                      | O(N)                     | put em HashMap. O(1) (médio), O(N) (pior). Complexidade acumulada: O(N²) (pior), O(N) (médio).         |

### Complexidade Total
- **Pior Caso:** $\mathbf{O(N^2 + M \cdot N)}$
- **Caso Médio:** $\mathbf{O(N+M)}$

---
# Análise de Complexidade da USEI14 - Maximum Throughput Between Two Hubs

## Definição de Variáveis de Complexidade
- $N$: O número de estações/vértices no grafo, $|V|$.
- $M$: O número de arestas direcionadas no grafo, $|E|$.
- $F$: O valor do fluxo máximo encontrado.
- $C_{max}$: A capacidade máxima de uma aresta no grafo.
- $T(\cdot)$: Complexidade temporal.

---

## 1. Análise da Função `FordFulkersonAlgorithm.computeMaxFlow`
Esta função implementa o **Algoritmo de Ford-Fulkerson** usando **BFS (Edmonds-Karp)** para encontrar caminhos aumentantes, calculando o fluxo máximo entre uma estação origem e uma estação destino.

### Complexidade

| Código                                                                                         | Complexidade Worst Case | Complexidade Average Case | Complexidade Better Case | Explicação                                                                                                                                                           |
|------------------------------------------------------------------------------------------------|-------------------------|---------------------------|--------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `if (graph == null) { ... }`                                                                   | O(1)                    | O(1)                      | O(1)                     | Validação de entrada. Operação de tempo constante.                                                                                                                  |
| `if (sourceStationId == null || sinkStationId == null) { ... }`                                | O(1)                    | O(1)                      | O(1)                     | Validação de entrada. Operação de tempo constante.                                                                                                                  |
| `if (sourceStationId.equals(sinkStationId)) { ... }`                                            | O(1)                    | O(1)                      | O(1)                     | Comparação de strings. Tempo constante.                                                                                                                              |
| `if (!graph.containsNode(sourceStationId)) { ... }`                                            | O(N)                    | O(1)                      | O(1)                     | Verificação de existência de nó. O(1) (médio), O(N) (pior).                                                                                                         |
| `if (!graph.containsNode(sinkStationId)) { ... }`                                              | O(N)                    | O(1)                      | O(1)                     | Verificação de existência de nó. O(1) (médio), O(N) (pior).                                                                                                         |
| `Map<String, List<ResidualEdge>> residualGraph = buildResidualGraph(graph);`                  | O(N²+M×N)              | O(N+M)                    | O(N+M)                   | Construção do grafo residual. Complexidade conforme análise da função buildResidualGraph.                                                                          |
| `int maxFlow = 0;`                                                                              | O(1)                    | O(1)                      | O(1)                     | Inicialização de variável. Tempo constante.                                                                                                                         |
| `while (true) { ... }`                                                                          | O(N×M²)                 | O(N×M)                    | O(N×M)                   | Loop principal do Ford-Fulkerson. No pior caso, executa O(N×M) iterações (Edmonds-Karp). Cada iteração tem O(M) para BFS. Total: O(N×M²). No caso médio: O(N×M).    |
| `List<ResidualEdge> augmentingPath = findAugmentingPath(...);`                                 | O(N+M)                  | O(N+M)                    | O(N+M)                   | Busca de caminho aumentante usando BFS. Complexidade conforme análise da função findAugmentingPath.                                                                  |
| `if (augmentingPath == null || augmentingPath.isEmpty()) { break; }`                           | O(1)                    | O(1)                      | O(1)                     | Verificação de lista vazia. Tempo constante.                                                                                                                         |
| `int minCapacity = findMinResidualCapacity(augmentingPath);`                                   | O(N)                    | O(L)                      | O(L)                     | Encontrar capacidade mínima. L é o comprimento do caminho. No pior caso, L pode ser O(N).                                                                            |
| `if (minCapacity <= 0) { break; }`                                                             | O(1)                    | O(1)                      | O(1)                     | Comparação. Tempo constante.                                                                                                                                        |
| `maxFlow += minCapacity;`                                                                      | O(1)                    | O(1)                      | O(1)                     | Operação aritmética. Tempo constante.                                                                                                                                |
| `updateResidualGraph(augmentingPath, minCapacity);`                                            | O(N)                    | O(L)                      | O(L)                     | Atualização do grafo residual. L é o comprimento do caminho. No pior caso, L pode ser O(N).                                                                        |
| `return maxFlow;`                                                                               | O(1)                    | O(1)                      | O(1)                     | Retorno de valor. Tempo constante.                                                                                                                                  |

### Complexidade Total
- **Pior Caso (Worst Case):** $\mathbf{O(N \cdot M^2)}$ (Edmonds-Karp com BFS)
- **Caso Médio (Average Case):** $\mathbf{O(N \cdot M)}$

**Nota:** A complexidade do Ford-Fulkerson depende do número de iterações do loop principal. Com BFS (Edmonds-Karp), o número de iterações é limitado a O(N×M), e cada BFS tem complexidade O(N+M). No pior caso, isso resulta em O(N×M×(N+M)) = O(N×M²) quando M >> N, ou O(N²×M) quando N >> M.

---

## 2. Análise da Função `buildResidualGraph`
Esta função constrói o grafo residual completo a partir do grafo original, criando arestas forward e reverse para cada aresta original.

### Complexidade

| Código                                                                                         | Complexidade Worst Case | Complexidade Average Case | Complexidade Better Case | Explicação                                                                                                                                                           |
|------------------------------------------------------------------------------------------------|-------------------------|---------------------------|--------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `Map<String, List<ResidualEdge>> residualGraph = new HashMap<>();`                            | O(1)                    | O(1)                      | O(1)                     | Inicialização de um HashMap. É uma operação de tempo constante.                                                                                                      |
| `for (Station station : graph.getAllNodes()) { residualGraph.put(station.getId(), new ArrayList<>()); }` | O(N²)                   | O(N)                      | O(N)                     | O ciclo itera N vezes. Operação put em HashMap. O(1) (médio), O(N) (pior). Complexidade acumulada: O(N²) (pior), O(N) (médio).                                        |
| `for (Line line : graph.getAllEdges()) { ... }`                                                | O(M)                    | O(M)                      | O(M)                     | O ciclo itera M vezes, uma vez por aresta original.                                                                                                                   |
| `String from = line.getDepartureStationId();`                                                  | O(1)                    | O(1)                      | O(1)                     | Acesso a propriedade. Tempo constante.                                                                                                                               |
| `String to = line.getArrivalStationId();`                                                      | O(1)                    | O(1)                      | O(1)                     | Acesso a propriedade. Tempo constante.                                                                                                                               |
| `int capacity = line.getCapacity();`                                                           | O(1)                    | O(1)                      | O(1)                     | Acesso a propriedade. Tempo constante.                                                                                                                               |
| `ResidualEdge forwardEdge = new ResidualEdge(to, capacity, line);`                             | O(1)                    | O(1)                      | O(1)                     | Criação de objeto. Tempo constante.                                                                                                                                  |
| `ResidualEdge reverseEdge = new ResidualEdge(from, 0, null);`                                 | O(1)                    | O(1)                      | O(1)                     | Criação de objeto. Tempo constante.                                                                                                                                  |
| `forwardEdge.setReverseEdge(reverseEdge);`                                                      | O(1)                    | O(1)                      | O(1)                     | Atribuição de referência. Tempo constante.                                                                                                                          |
| `reverseEdge.setReverseEdge(forwardEdge);`                                                     | O(1)                    | O(1)                      | O(1)                     | Atribuição de referência. Tempo constante.                                                                                                                          |
| `residualGraph.get(from).add(forwardEdge);`                                                    | O(M×N)                  | O(M)                      | O(M)                     | Operação get em HashMap seguida de add em ArrayList. O(1) (médio), O(N) (pior) para get. M chamadas acumulam O(M×N) (pior), O(M) (médio).                            |
| `residualGraph.get(to).add(reverseEdge);`                                                      | O(M×N)                  | O(M)                      | O(M)                     | Operação get em HashMap seguida de add em ArrayList. O(1) (médio), O(N) (pior) para get. M chamadas acumulam O(M×N) (pior), O(M) (médio).                              |
| `return residualGraph;`                                                                         | O(1)                    | O(1)                      | O(1)                     | Retorno de referência. Tempo constante.                                                                                                                              |

### Complexidade Total
- **Pior Caso (Worst Case):** $\mathbf{O(N^2 + M \cdot N)}$
- **Caso Médio (Average Case):** $\mathbf{O(N+M)}$

---

## 3. Análise da Função `findAugmentingPath`
Esta função encontra um caminho aumentante do source ao sink usando **BFS (Breadth-First Search)** no grafo residual.

### Complexidade

| Código                                                                                         | Complexidade Worst Case | Complexidade Average Case | Complexidade Better Case | Explicação                                                                                                                                                           |
|------------------------------------------------------------------------------------------------|-------------------------|---------------------------|--------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `Queue<String> queue = new LinkedList<>();`                                                    | O(1)                    | O(1)                      | O(1)                     | Inicialização de uma LinkedList. Tempo constante.                                                                                                                     |
| `Map<String, PathEntry> pathInfo = new HashMap<>();`                                           | O(1)                    | O(1)                      | O(1)                     | Inicialização de um HashMap. Tempo constante.                                                                                                                        |
| `Set<String> visited = new HashSet<>();`                                                       | O(1)                    | O(1)                      | O(1)                     | Inicialização de um HashSet. Tempo constante.                                                                                                                        |
| `queue.offer(source);`                                                                          | O(1)                    | O(1)                      | O(1)                     | Operação offer em LinkedList. Tempo constante.                                                                                                                       |
| `visited.add(source);`                                                                          | O(N)                    | O(1)                      | O(1)                     | Operação add em HashSet. O(1) (médio), O(N) (pior).                                                                                                                  |
| `pathInfo.put(source, new PathEntry(null, null));`                                             | O(N)                    | O(1)                      | O(1)                     | Operação put em HashMap. O(1) (médio), O(N) (pior).                                                                                                                  |
| `while (!queue.isEmpty()) { ... }`                                                             | O(N+M)                  | O(N+M)                    | O(N+M)                   | BFS visita cada vértice no máximo uma vez e cada aresta no máximo uma vez. Complexidade total: O(N+M).                                                               |
| `String current = queue.poll();`                                                                | O(N)                    | O(1)                      | O(1)                     | Operação poll em LinkedList. O(1) (médio), O(N) (pior caso teórico). N chamadas acumulam O(N²) (pior), O(N) (médio).                                                  |
| `if (current.equals(sink)) { return reconstructPath(...); }`                                   | O(1)                    | O(1)                      | O(1)                     | Comparação de strings. Tempo constante.                                                                                                                              |
| `List<ResidualEdge> edges = residualGraph.get(current);`                                       | O(N²)                   | O(N+M)                    | O(N+M)                   | Operação get em HashMap. O(1) (médio), O(N) (pior). N chamadas acumulam O(N²) (pior), O(N) (médio). Mas o número total de arestas processadas é M.                   |
| `if (edges != null) { for (ResidualEdge edge : edges) { ... } }`                               | O(M) (total)            | O(M) (total)              | O(M) (total)             | O número total de iterações sobre todas as arestas do grafo residual é O(M) (considerando forward e reverse edges, total 2M arestas, mas processamos apenas as com capacidade > 0). |
| `if (edge.residualCapacity > 0 && !visited.contains(edge.to)) { ... }`                         | O(M×N)                  | O(M)                      | O(M)                     | Verificação de capacidade e contains em HashSet. O(1) (médio), O(N) (pior) para contains. M chamadas acumulam O(M×N) (pior), O(M) (médio).                           |
| `visited.add(edge.to);`                                                                          | O(M×N)                  | O(M)                      | O(M)                     | Operação add em HashSet. O(1) (médio), O(N) (pior). M chamadas acumulam O(M×N) (pior), O(M) (médio).                                                                 |
| `pathInfo.put(edge.to, new PathEntry(current, edge));`                                         | O(M×N)                  | O(M)                      | O(M)                     | Operação put em HashMap. O(1) (médio), O(N) (pior). M chamadas acumulam O(M×N) (pior), O(M) (médio).                                                                 |
| `queue.offer(edge.to);`                                                                          | O(M)                    | O(M)                      | O(M)                     | Operação offer em LinkedList. O(1) por chamada. M chamadas acumulam O(M).                                                                                            |
| `return null;`                                                                                  | O(1)                    | O(1)                      | O(1)                     | Retorno. Tempo constante.                                                                                                                                            |

### Complexidade Total
- **Pior Caso (Worst Case):** $\mathbf{O(N^2 + M \cdot N)}$
- **Caso Médio (Average Case):** $\mathbf{O(N+M)}$

**Nota:** A complexidade do BFS é O(N+M) no caso médio, mas pode degradar para O(N² + M×N) no pior caso devido a colisões de hash.

---

## 4. Análise da Função `reconstructPath`
Esta função reconstrói o caminho aumentante como uma sequência de arestas residuais, percorrendo do sink ao source.

### Complexidade

| Código                                                                                         | Complexidade Worst Case | Complexidade Average Case | Complexidade Better Case | Explicação                                                                                                                                                           |
|------------------------------------------------------------------------------------------------|-------------------------|---------------------------|--------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `List<ResidualEdge> path = new ArrayList<>();`                                                 | O(1)                    | O(1)                      | O(1)                     | Inicialização de um ArrayList. Tempo constante.                                                                                                                      |
| `String current = sink;`                                                                        | O(1)                    | O(1)                      | O(1)                     | Atribuição. Tempo constante.                                                                                                                                        |
| `while (current != null && !current.equals(source)) { ... }`                                  | O(N)                    | O(L)                      | O(L)                     | Loop reconstrói o caminho percorrendo do sink ao source. L é o comprimento do caminho. No pior caso, L pode ser O(N).                                                |
| `PathEntry entry = pathInfo.get(current);`                                                     | O(N²)                   | O(L)                      | O(L)                     | Operação get em HashMap. O(1) (médio), O(N) (pior). L chamadas acumulam O(L×N) (pior), O(L) (médio).                                                                |
| `if (entry == null || entry.edge == null) { break; }`                                          | O(1)                    | O(1)                      | O(1)                     | Verificação. Tempo constante.                                                                                                                                       |
| `path.add(0, entry.edge);`                                                                      | O(N²)                   | O(L²)                     | O(L²)                    | Inserção no início de ArrayList. O(L) por inserção. L inserções acumulam O(L²). No pior caso, L = O(N), então O(N²).                                                |
| `current = entry.from;`                                                                         | O(1)                    | O(1)                      | O(1)                     | Atribuição. Tempo constante.                                                                                                                                        |
| `return path;`                                                                                  | O(1)                    | O(1)                      | O(1)                     | Retorno de referência. Tempo constante.                                                                                                                              |

### Complexidade Total
- **Pior Caso (Worst Case):** $\mathbf{O(N^2)}$
- **Caso Médio (Average Case):** $\mathbf{O(L^2)}$ onde $L$ é o comprimento do caminho

**Nota:** A complexidade é dominada pela inserção no início do ArrayList, que requer deslocamento de elementos. No pior caso, quando o caminho tem comprimento O(N), a complexidade é O(N²).

---

## 5. Análise da Função `findMinResidualCapacity`
Esta função encontra a capacidade residual mínima (gargalo) ao longo de um caminho aumentante.

### Complexidade

| Código                                                                                         | Complexidade Worst Case | Complexidade Average Case | Complexidade Better Case | Explicação                                                                                                                                                           |
|------------------------------------------------------------------------------------------------|-------------------------|---------------------------|--------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `if (path == null || path.isEmpty()) { return 0; }`                                            | O(1)                    | O(1)                      | O(1)                     | Verificação. Tempo constante.                                                                                                                                       |
| `int minCapacity = Integer.MAX_VALUE;`                                                         | O(1)                    | O(1)                      | O(1)                     | Inicialização. Tempo constante.                                                                                                                                     |
| `for (ResidualEdge edge : path) { ... }`                                                      | O(N)                    | O(L)                      | O(L)                     | O ciclo itera sobre as arestas do caminho. L é o comprimento do caminho. No pior caso, L pode ser O(N).                                                             |
| `minCapacity = Math.min(minCapacity, edge.residualCapacity);`                                  | O(N)                    | O(L)                      | O(L)                     | Operação Math.min e acesso a propriedade. O(1) por iteração. L iterações acumulam O(L).                                                                             |
| `return minCapacity == Integer.MAX_VALUE ? 0 : minCapacity;`                                   | O(1)                    | O(1)                      | O(1)                     | Comparação e retorno. Tempo constante.                                                                                                                               |

### Complexidade Total
- **Pior Caso (Worst Case):** $\mathbf{O(N)}$
- **Caso Médio (Average Case):** $\mathbf{O(L)}$ onde $L$ é o comprimento do caminho

---

## 6. Análise da Função `updateResidualGraph`
Esta função atualiza o grafo residual após enviar fluxo ao longo de um caminho aumentante, atualizando as capacidades das arestas forward e reverse.

### Complexidade

| Código                                                                                         | Complexidade Worst Case | Complexidade Average Case | Complexidade Better Case | Explicação                                                                                                                                                           |
|------------------------------------------------------------------------------------------------|-------------------------|---------------------------|--------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `for (ResidualEdge edge : path) { ... }`                                                      | O(N)                    | O(L)                      | O(L)                     | O ciclo itera sobre as arestas do caminho. L é o comprimento do caminho. No pior caso, L pode ser O(N).                                                             |
| `edge.residualCapacity -= flow;`                                                               | O(N)                    | O(L)                      | O(L)                     | Operação aritmética e atribuição. O(1) por iteração. L iterações acumulam O(L).                                                                                     |
| `if (edge.reverseEdge != null) { edge.reverseEdge.residualCapacity += flow; }`                | O(N)                    | O(L)                      | O(L)                     | Verificação, acesso a propriedade e operação aritmética. O(1) por iteração. L iterações acumulam O(L).                                                               |

### Complexidade Total
- **Pior Caso (Worst Case):** $\mathbf{O(N)}$
- **Caso Médio (Average Case):** $\mathbf{O(L)}$ onde $L$ é o comprimento do caminho

---

## 7. Análise da Função `MaximumFlowController.computeMaximumFlow`
Esta função orquestra o cálculo do fluxo máximo, validando entradas e chamando o algoritmo de domínio.

### Complexidade

| Código                                                                                         | Complexidade Worst Case | Complexidade Average Case | Complexidade Better Case | Explicação                                                                                                                                                           |
|------------------------------------------------------------------------------------------------|-------------------------|---------------------------|--------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `if (graph == null) { ... }`                                                                   | O(1)                    | O(1)                      | O(1)                     | Validação. Tempo constante.                                                                                                                                         |
| `if (sourceStationId == null || sinkStationId == null) { ... }`                                | O(1)                    | O(1)                      | O(1)                     | Validação. Tempo constante.                                                                                                                                         |
| `Station sourceStation = graph.getNode(sourceStationId);`                                     | O(N)                    | O(1)                      | O(1)                     | Operação getNode. O(1) (médio), O(N) (pior).                                                                                                                         |
| `Station sinkStation = graph.getNode(sinkStationId);`                                          | O(N)                    | O(1)                      | O(1)                     | Operação getNode. O(1) (médio), O(N) (pior).                                                                                                                         |
| `if (sourceStation == null) { ... }`                                                           | O(1)                    | O(1)                      | O(1)                     | Verificação. Tempo constante.                                                                                                                                       |
| `if (sinkStation == null) { ... }`                                                             | O(1)                    | O(1)                      | O(1)                     | Verificação. Tempo constante.                                                                                                                                       |
| `int maxFlow = FordFulkersonAlgorithm.computeMaxFlow(...);`                                     | O(N×M²)                 | O(N×M)                    | O(N×M)                   | Chamada ao algoritmo Ford-Fulkerson. Complexidade conforme análise anterior.                                                                                         |
| `return new MaximumFlowResult(sourceStation, sinkStation, maxFlow);`                            | O(1)                    | O(1)                      | O(1)                     | Criação de objeto. Tempo constante.                                                                                                                                 |

### Complexidade Total
- **Pior Caso (Worst Case):** $\mathbf{O(N \cdot M^2)}$
- **Caso Médio (Average Case):** $\mathbf{O(N \cdot M)}$

---

## 8. Análise da Função `MaximumFlowFXUI.extractStationId`
Esta função extrai o ID da estação a partir de uma string no formato "Nome (ID)".

### Complexidade

| Código                                                                                         | Complexidade Worst Case | Complexidade Average Case | Complexidade Better Case | Explicação                                                                                                                                                           |
|------------------------------------------------------------------------------------------------|-------------------------|---------------------------|--------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `if (displayText == null) return "";`                                                         | O(1)                    | O(1)                      | O(1)                     | Verificação de null. Tempo constante.                                                                                                                                |
| `int start = displayText.lastIndexOf('(');`                                                    | O(S)                    | O(S)                      | O(S)                     | Busca do último '(' na string. S é o comprimento da string. No pior caso, percorre toda a string.                                                                 |
| `int end = displayText.lastIndexOf(')');`                                                      | O(S)                    | O(S)                      | O(S)                     | Busca do último ')' na string. S é o comprimento da string. No pior caso, percorre toda a string.                                                                 |
| `if (start >= 0 && end > start) { ... }`                                                       | O(1)                    | O(1)                      | O(1)                     | Comparações. Tempo constante.                                                                                                                                      |
| `return displayText.substring(start + 1, end).trim();`                                        | O(S)                    | O(S)                      | O(S)                     | Criação de substring e trim. O(S) onde S é o comprimento da substring.                                                                                             |
| `return displayText.trim();`                                                                    | O(S)                    | O(S)                      | O(S)                     | Operação trim. O(S) onde S é o comprimento da string.                                                                                                             |

### Complexidade Total
- **Pior Caso (Worst Case):** $\mathbf{O(S)}$ onde $S$ é o comprimento da string de entrada
- **Caso Médio (Average Case):** $\mathbf{O(S)}$
- **Melhor Caso (Better Case):** $\mathbf{O(S)}$

**Nota:** A complexidade é linear no comprimento da string de entrada, pois as operações `lastIndexOf`, `substring` e `trim` requerem percorrer a string.

---

## 9. Análise da Função `MaximumFlowFXUI.findStationId`
Esta função encontra o ID da estação a partir de uma entrada que pode ser um ID, nome, ou formato "Nome (ID)".

### Complexidade

| Código                                                                                         | Complexidade Worst Case | Complexidade Average Case | Complexidade Better Case | Explicação                                                                                                                                                           |
|------------------------------------------------------------------------------------------------|-------------------------|---------------------------|--------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `if (allStations == null || input == null || input.isEmpty()) return null;`                  | O(1)                    | O(1)                      | O(1)                     | Validações. Tempo constante.                                                                                                                                        |
| `String trimmedInput = input.trim();`                                                           | O(S)                    | O(S)                      | O(S)                     | Operação trim. O(S) onde S é o comprimento da string.                                                                                                             |
| `String extractedId = extractStationId(trimmedInput);`                                         | O(S)                    | O(S)                      | O(S)                     | Chamada a extractStationId. Complexidade conforme análise anterior.                                                                                                |
| `if (!extractedId.equals(trimmedInput)) { for (Station station : allStations) { ... } }`      | O(N)                    | O(N)                      | O(1)                     | Loop sobre todas as estações. No pior caso, percorre todas as N estações. No melhor caso, encontra na primeira iteração.                                           |
| `if (station.getId().equals(extractedId)) { return station.getId(); }`                         | O(1)                    | O(1)                      | O(1)                     | Comparação de strings. Tempo constante por iteração.                                                                                                               |
| `for (Station station : allStations) { if (station.getId().equalsIgnoreCase(...)) { ... } }` | O(N)                    | O(N)                      | O(1)                     | Loop sobre todas as estações para match exato de ID. No pior caso, percorre todas as N estações. No melhor caso, encontra na primeira.                              |
| `for (Station station : allStations) { if (station.getName().equalsIgnoreCase(...)) { ... } }` | O(N×S)                  | O(N×S)                    | O(S)                     | Loop sobre todas as estações para match exato de nome. Cada comparação equalsIgnoreCase é O(S) onde S é o comprimento médio do nome.                                |
| `for (Station station : allStations) { if (station.getName().toLowerCase().contains(...)) { ... } }` | O(N×S)                  | O(N×S)                    | O(S)                     | Loop sobre todas as estações para match parcial de nome. Cada toLowerCase() e contains() é O(S).                                                                  |

### Complexidade Total
- **Pior Caso (Worst Case):** $\mathbf{O(N \cdot S)}$ onde $N$ é o número de estações e $S$ é o comprimento médio dos nomes
- **Caso Médio (Average Case):** $\mathbf{O(N \cdot S)}$
- **Melhor Caso (Better Case):** $\mathbf{O(S)}$ quando o ID é extraído do formato "Nome (ID)" e encontrado imediatamente

**Nota:** A complexidade é dominada pela busca linear nas estações. No melhor caso, quando o ID é extraído do formato "Nome (ID)" e a estação é encontrada na primeira iteração, a complexidade é O(S). No pior caso, quando é necessário fazer match parcial de nomes, a complexidade é O(N×S).

---

## 10. Análise da Função `MaximumFlowFXUI.handleCalculate`
Esta função orquestra o cálculo do fluxo máximo na UI, validando entradas e executando o cálculo em background.

### Complexidade

| Código                                                                                         | Complexidade Worst Case | Complexidade Average Case | Complexidade Better Case | Explicação                                                                                                                                                           |
|------------------------------------------------------------------------------------------------|-------------------------|---------------------------|--------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `if (controller == null) { ... }`                                                              | O(1)                    | O(1)                      | O(1)                     | Verificação. Tempo constante.                                                                                                                                       |
| `if (selectedSourceId == null || selectedSourceId.isEmpty()) { ... }`                          | O(1)                    | O(1)                      | O(1)                     | Verificação. Tempo constante.                                                                                                                                       |
| `selectedSourceId = findStationId(sourceText);`                                                 | O(N×S)                  | O(N×S)                    | O(S)                     | Chamada a findStationId. Complexidade conforme análise anterior.                                                                                                   |
| `if (selectedSinkId == null || selectedSinkId.isEmpty()) { ... }`                             | O(1)                    | O(1)                      | O(1)                     | Verificação. Tempo constante.                                                                                                                                       |
| `selectedSinkId = findStationId(sinkText);`                                                    | O(N×S)                  | O(N×S)                    | O(S)                     | Chamada a findStationId. Complexidade conforme análise anterior.                                                                                                   |
| Validações de entrada                                                                          | O(1)                    | O(1)                      | O(1)                     | Comparações e verificações. Tempo constante.                                                                                                                       |
| `Service<MaximumFlowController.MaximumFlowResult> service = new Service<...>() { ... }`      | O(1)                    | O(1)                      | O(1)                     | Criação de objeto Service. Tempo constante.                                                                                                                        |
| `controller.computeMaximumFlow(sourceId, sinkId)` (executado em background)                   | O(N×M²)                 | O(N×M)                    | O(N×M)                   | Chamada ao cálculo do fluxo máximo. Complexidade conforme análise de MaximumFlowController.computeMaximumFlow.                                                     |
| Handlers de sucesso/erro                                                                       | O(1)                    | O(1)                      | O(1)                     | Operações de atualização da UI. Tempo constante.                                                                                                                   |

### Complexidade Total
- **Pior Caso (Worst Case):** $\mathbf{O(N \times S + N \cdot M^2)}$ onde $N$ é o número de estações, $S$ é o comprimento médio dos nomes, e $M$ é o número de arestas
- **Caso Médio (Average Case):** $\mathbf{O(N \times S + N \cdot M)}$
- **Melhor Caso (Better Case):** $\mathbf{O(S + N \cdot M)}$ quando os IDs já estão definidos

**Nota:** A complexidade é dominada pelo cálculo do fluxo máximo (O(N×M²) no pior caso). A extração de IDs das estações adiciona O(N×S) no pior caso, mas é geralmente dominada pelo cálculo do fluxo.

---

## 11. Análise da Função `MaximumFlowFXUI.updateCalculateButtonState`
Esta função atualiza o estado do botão de cálculo baseado na seleção de estações.

### Complexidade

| Código                                                                                         | Complexidade Worst Case | Complexidade Average Case | Complexidade Better Case | Explicação                                                                                                                                                           |
|------------------------------------------------------------------------------------------------|-------------------------|---------------------------|--------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Verificação e extração de ID do campo source                                                  | O(N×S)                  | O(N×S)                    | O(S)                     | Chamada a findStationId se necessário. Complexidade conforme análise anterior.                                                                                      |
| Verificação e extração de ID do campo sink                                                    | O(N×S)                  | O(N×S)                    | O(S)                     | Chamada a findStationId se necessário. Complexidade conforme análise anterior.                                                                                      |
| `boolean canCalculate = controller != null && ...`                                             | O(1)                    | O(1)                      | O(1)                     | Verificações booleanas. Tempo constante.                                                                                                                            |
| `calculateButton.setDisable(!canCalculate);`                                                   | O(1)                    | O(1)                      | O(1)                     | Atualização de propriedade da UI. Tempo constante.                                                                                                                 |

### Complexidade Total
- **Pior Caso (Worst Case):** $\mathbf{O(N \times S)}$ onde $N$ é o número de estações e $S$ é o comprimento médio dos nomes
- **Caso Médio (Average Case):** $\mathbf{O(N \times S)}$
- **Melhor Caso (Better Case):** $\mathbf{O(1)}$ quando os IDs já estão definidos e não é necessário extrair dos campos de texto

**Nota:** A complexidade é dominada pela busca de IDs nas estações quando necessário. No melhor caso, quando os IDs já estão definidos, a função é O(1).

---

## Resumo da Complexidade Temporal da USEI14

| Função                                                                 | Pior Caso                    | Caso Médio           |
|------------------------------------------------------------------------|------------------------------|----------------------|
| `FordFulkersonAlgorithm.computeMaxFlow`                               | $\mathbf{O(N \cdot M^2)}$    | $\mathbf{O(N \cdot M)}$ |
| `buildResidualGraph`                                                   | $\mathbf{O(N^2 + M \cdot N)}$ | $\mathbf{O(N+M)}$    |
| `findAugmentingPath` (BFS)                                            | $\mathbf{O(N^2 + M \cdot N)}$ | $\mathbf{O(N+M)}$    |
| `reconstructPath`                                                     | $\mathbf{O(N^2)}$            | $\mathbf{O(L^2)}$    |
| `findMinResidualCapacity`                                             | $\mathbf{O(N)}$              | $\mathbf{O(L)}$      |
| `updateResidualGraph`                                                  | $\mathbf{O(N)}$              | $\mathbf{O(L)}$      |
| `MaximumFlowController.computeMaximumFlow`                             | $\mathbf{O(N \cdot M^2)}$    | $\mathbf{O(N \cdot M)}$ |
| `MaximumFlowFXUI.extractStationId`                                     | $\mathbf{O(S)}$              | $\mathbf{O(S)}$      |
| `MaximumFlowFXUI.findStationId`                                        | $\mathbf{O(N \cdot S)}$      | $\mathbf{O(N \cdot S)}$ |
| `MaximumFlowFXUI.handleCalculate`                                      | $\mathbf{O(N \cdot S + N \cdot M^2)}$ | $\mathbf{O(N \cdot S + N \cdot M)}$ |
| `MaximumFlowFXUI.updateCalculateButtonState`                            | $\mathbf{O(N \cdot S)}$      | $\mathbf{O(N \cdot S)}$ |

Onde:
- $N$ = número de vértices (estações)
- $M$ = número de arestas (linhas direcionadas)
- $L$ = comprimento do caminho aumentante (geralmente $L \leq N$)
- $S$ = comprimento médio das strings (nomes de estações)
- $F$ = valor do fluxo máximo

**Observação:** A complexidade dominante é do algoritmo Ford-Fulkerson com BFS (Edmonds-Karp). No caso médio, o número de iterações é O(N×M) e cada BFS tem complexidade O(N+M), resultando em O(N×M×(N+M)) = O(N×M²) quando M >> N. No caso médio prático, a complexidade tende a ser melhor devido a caminhos mais curtos e menos iterações.

**Nota sobre complexidade do Ford-Fulkerson:** Com BFS (Edmonds-Karp), o número de iterações é limitado a O(N×M), e cada BFS tem complexidade O(N+M). A complexidade total é O(N×M×(N+M)). Quando M >> N, isso simplifica para O(N×M²). Quando N >> M, simplifica para O(N²×M). No caso médio, devido a caminhos mais curtos e menos iterações, a complexidade prática é frequentemente melhor que o pior caso teórico.

**Nota sobre complexidade da UI:** As funções da UI (`extractStationId`, `findStationId`, `updateCalculateButtonState`) têm complexidade O(N×S) no pior caso, onde N é o número de estações e S é o comprimento médio dos nomes. Esta complexidade é geralmente dominada pelo cálculo do fluxo máximo (O(N×M²)), mas pode ser relevante quando o número de estações é muito grande. No entanto, estas operações são executadas apenas quando o utilizador interage com a interface, não durante o cálculo do fluxo máximo em si.

---

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

