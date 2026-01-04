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
