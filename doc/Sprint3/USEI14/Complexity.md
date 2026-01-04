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

