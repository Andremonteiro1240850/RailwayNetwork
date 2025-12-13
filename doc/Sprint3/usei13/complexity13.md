# Análise de Complexidade da USEI13 - Rail Hub Centrality Analysis


## Definição de Variáveis de Complexidade
- $N$: O número de estações/vértices no grafo, $|V|$.
- $M$: O número de arestas no grafo não-direcionado, $|E|$.
- $T(\cdot)$: Complexidade temporal.

---

## 1. Análise da Função `calculateBetweennessCentrality`
Esta função implementa o **Algoritmo de Brandes**, que calcula a Betweenness Centrality executando um algoritmo de **Single-Source Shortest Path (SSSP)**, neste caso, um Dijkstra modificado, a partir de cada um dos $N$ vértices.

### Complexidade 

| Código                                                                                         | Complexidade Worst Case | Complexidade Average Case | Complexidade Better Case | Explicação                                                                                                                                                           |
|------------------------------------------------------------------------------------------------|-------------------------|---------------------------|--------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `Map<Station, Double> centralidades = new HashMap<>();`                                        | O(1)                    | O(1)                      | O(1)                     | Inicialização de um HashMap. É uma operação de tempo constante.                                                                                                      |
| `for (Station station : undirectGraph.keySet()) {`                                             | O(N)                    | O(N)                      | O(N)                     | O ciclo itera N vezes, onde N é o número de estações.                                                                                                                |
| `centralidades.put(station, 0.0);`                                                             | O(N²)                   | O(N)                      | O(N)                     | Operação put em HashMap. O(1) no caso médio e melhor caso. No pior caso (colisões), é O(N). Complexidade acumulada: N×O(N)=O(N²) (pior), N×O(1)=O(N) (médio/melhor). |
| `for (Station origem : undirectGraph.keySet()) {`                                              | O(N)                    | O(N)                      | O(N)                     | Ciclo principal (Origem): repete N vezes. O corpo deste ciclo domina a complexidade total: TTotal = O(N × Tcorpo).                                                   |
| `Map<Station, Double> distancias = new HashMap<>(); ... Stack<Station> stack = new Stack<>();` | O(1)                    | O(1)                      | O(1)                     | Inicialização das estruturas de dados. Tempo constante.                                                                                                              |
| `for (Station s : undirectGraph.keySet()) {`                                                   | O(N)                    | O(N)                      | O(N)                     | Ciclo de inicialização. Repete N vezes.                                                                                                                              |
| `distancias.put(s, Double.MAX_VALUE); ... predecessores.put(s, new ArrayList<>());`            | O(N²)                   | O(N)                      | O(N)                     | Quatro operações put em HashMap dentro de um ciclo O(N). Complexidade acumulada: O(N²) (pior), O(N) (médio).                                                         |
| `PriorityQueue<Station> fila = new PriorityQueue<>(...);`                                      | O(1)                    | O(1)                      | O(1)                     | Inicialização da PriorityQueue. Tempo constante.                                                                                                                     |
| `fila.add(origem);`                                                                            | O(logN)                 | O(logN)                   | O(1)                     | Inserção numa Min-Heap. O(logN) no pior e caso médio, O(1) no melhor caso.                                                                                           |
| `while (!fila.isEmpty()) {`                                                                    | O(N+M)                  | O(N+M)                    | O(N+M)                   | O loop executa N extrações (poll) e M iterações nos vizinhos (total).                                                                                                |
| `Station atual = fila.poll();`                                                                 | O(logN)                 | O(logN)                   | O(1)                     | Extração do elemento de menor prioridade. O(logN) (pior/médio). N chamadas acumulam O(NlogN).                                                                        |
| `for (Map.Entry<Station, Double> vizinho : undirectGraph.get(atual).entrySet()) {`             | O(M) (total)            | O(M) (total)              | O(M) (total)             | O número total de iterações do ciclo de vizinhos, em todas as execuções do while, é M.                                                                               |
| `double novaDistancia = distancias.get(atual) + peso;`                                         | O(N)                    | O(1)                      | O(1)                     | Operação get em HashMap. O(1) (médio), O(N) (pior). M chamadas acumulam O(M×N) (pior), O(M) (médio).                                                                 |
| `fila.remove(proxima);`                                                                        | O(N)                    | O(N)                      | O(logN)                  | PriorityQueue.remove(Object) requer O(N) para pesquisa + O(logN) para reequilíbrio. M chamadas acumulam O(M×N) (pior/médio).                                         |
| `fila.add(proxima);`                                                                           | O(logN)                 | O(logN)                   | O(1)                     | Inserção/atualização. O(logN) (pior/médio). M chamadas acumulam O(MlogN).                                                                                            |
| `while (!stack.isEmpty()) {`                                                                   | O(N+M)                  | O(N+M)                    | O(N+M)                   | Fase de acumulação: o loop executa N vezes, e o loop interno executa um total de O(M) vezes (retrocesso das arestas).                                                |
| `for (Station predecessor : predecessores.get(atual)) {`                                       | O(M) (total)            | O(M) (total)              | O(M) (total)             | O número total de iterações sobre os predecessores é M.                                                                                                              |
| `double contribuicao = (...);`                                                                 | O(N)                    | O(1)                      | O(1)                     | Quatro operações get em HashMap. O(1) (médio), O(N) (pior). M chamadas acumulam O(M×N) (pior), O(M) (médio).                                                         |
| `centralidades.put(atual, centralidades.get(atual) + dependencias.get(atual));`                | O(N²)                   | O(N)                      | O(N)                     | Três operações get/put em HashMap. N chamadas acumulam O(N²) (pior), O(N) (médio).                                                                                   |
| `for (Station s : centralidades.keySet()) { ... }`                                             | O(N²)                   | O(N)                      | O(N)                     | Normalização: itera N vezes. Operação put em HashMap. O(N²) (pior), O(N) (médio).                                                                                    |

### Complexidade Total
- **Pior Caso (Worst Case):** $\mathbf{O(N^2 \cdot M)}$
- **Caso Médio (Average Case):** $\mathbf{O(N^2 \cdot M)}$

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
