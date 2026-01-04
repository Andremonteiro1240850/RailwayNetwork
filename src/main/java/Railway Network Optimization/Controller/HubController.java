package Controller;

import Domain.RailwayGraph;
import Domain.Station;
import Repository.HubRepository;
import Repository.LineRepository;
import Repository.StationRepository;

import java.util.*;
import java.util.function.BinaryOperator;

public class HubController {
    private final HubRepository hubRepository;

    public HubController(HubRepository hubRepository) {
        this.hubRepository = hubRepository;
    }

    public Map<Station, Double> calculateBetweennessCentrality(Map<Station, Map<Station, Double>> undirectGraph) {
        Map<Station, Double> centralidades = new HashMap<>();

        for (Station s : undirectGraph.keySet()) {
            centralidades.put(s, 0.0);
        }

        for (Station origem : undirectGraph.keySet()) {
            Stack<Station> stack = new Stack<>();
            Map<Station, List<Station>> predecessores = new HashMap<>();
            Map<Station, Integer> sigma = new HashMap<>();
            Map<Station, Double> distancias = new HashMap<>();

            for (Station s : undirectGraph.keySet()) {
                predecessores.put(s, new ArrayList<>());
                sigma.put(s, 0);
                distancias.put(s, Double.MAX_VALUE);
            }

            sigma.put(origem, 1);
            distancias.put(origem, 0.0);

            // PriorityQueue com array [Station, distancia]
            PriorityQueue<Object[]> fila = new PriorityQueue<>(
                    Comparator.comparingDouble(arr -> (Double) arr[1])
            );
            fila.add(new Object[]{origem, 0.0});

            Set<Station> visitados = new HashSet<>();

            while (!fila.isEmpty()) {
                Object[] current = fila.poll();
                Station v = (Station) current[0];

                // Pular se já foi visitado
                if (visitados.contains(v)) continue;
                visitados.add(v);
                stack.push(v);

                if (!undirectGraph.containsKey(v)) continue;

                for (Map.Entry<Station, Double> entry : undirectGraph.get(v).entrySet()) {
                    Station w = entry.getKey();
                    double peso = entry.getValue();
                    double novaDistancia = distancias.get(v) + peso;

                    // Caminho mais curto encontrado
                    if (novaDistancia < distancias.get(w)) {
                        distancias.put(w, novaDistancia);
                        sigma.put(w, sigma.get(v));
                        predecessores.get(w).clear();
                        predecessores.get(w).add(v);
                        fila.add(new Object[]{w, novaDistancia});
                    }
                    // Caminho alternativo com mesma distância
                    else if (Math.abs(novaDistancia - distancias.get(w)) < 1e-9) {
                        sigma.put(w, sigma.get(w) + sigma.get(v));
                        predecessores.get(w).add(v);
                    }
                }
            }

            // Acumulação das dependências
            Map<Station, Double> delta = new HashMap<>();
            for (Station s : undirectGraph.keySet()) {
                delta.put(s, 0.0);
            }

            while (!stack.isEmpty()) {
                Station w = stack.pop();
                for (Station v : predecessores.get(w)) {
                    double contribuicao = (sigma.get(v) / (double) sigma.get(w)) * (1.0 + delta.get(w));
                    delta.put(v, delta.get(v) + contribuicao);
                }
                if (!w.equals(origem)) {
                    centralidades.put(w, centralidades.get(w) + delta.get(w));
                }
            }
        }

        // Normalização para grafos não-direcionados
        for (Station s : centralidades.keySet()) {
            centralidades.put(s, centralidades.get(s) / 2.0);
        }

        return centralidades;
    }

    public Map<Station, Double> djikstra(Station sOrig, Map<Station, Map<Station, Double>> undirectGraph, Set<Station> allStations) {
        Map<Station, Double> caminhosCurtos = new HashMap<>();

        // Inicialização
        for(Station s : allStations){
            caminhosCurtos.put(s, Double.MAX_VALUE);
        }
        caminhosCurtos.put(sOrig, 0.0);

        // PriorityQueue com par (distância, estação)
        PriorityQueue<Map.Entry<Double, Station>> fila = new PriorityQueue<>(
                Map.Entry.comparingByKey()
        );
        fila.add(Map.entry(0.0, sOrig));

        Set<Station> visitadas = new HashSet<>();

        while (!fila.isEmpty()) {
            Map.Entry<Double, Station> entry = fila.poll();
            double dist = entry.getKey();
            Station atual = entry.getValue();

            // Se já visitamos, ignorar
            if (visitadas.contains(atual)) {
                continue;
            }
            visitadas.add(atual);

            Map<Station, Double> vizinhos = undirectGraph.getOrDefault(atual, Collections.emptyMap());

            for(Map.Entry<Station, Double> vizinho : vizinhos.entrySet()){
                Station destino = vizinho.getKey();

                if (visitadas.contains(destino)) {
                    continue;
                }

                double peso = vizinho.getValue();
                double novaDistancia = dist + peso;

                if(novaDistancia < caminhosCurtos.get(destino)){
                    caminhosCurtos.put(destino, novaDistancia);
                    fila.add(Map.entry(novaDistancia, destino));
                }
            }
        }
        return caminhosCurtos;
    }

    public Map<Station, Double> calculateHarmonicCloseness (Map<Station, Map<Station, Double>> undirectGraph) {
        Set<Station> allStations = undirectGraph.keySet();
        Map<Station, Double> harmonicCloseness = new HashMap<>();
        for (Station s : allStations) {
            Map<Station, Double> caminhosCurtos = djikstra(s, undirectGraph, allStations);
            double hc = 0.0;
            for(Station s2 : allStations){
                if(s.equals(s2)){
                    continue;
                }
                Double novaDistancia = caminhosCurtos.get(s2);
                if(novaDistancia < Double.MAX_VALUE && novaDistancia > 0.0){
                    hc += 1.0 / novaDistancia;
                }
            }
            harmonicCloseness.put(s, hc);
        }
        return harmonicCloseness;
    }
    public Map<Station, Double> calculateStrengthNorm(Map<Station, Map<Station, Double>> undirectGraph) {
        Map<Station, Double> strength = new HashMap<>();

        // Obter todas as estações
        Set<Station> allStations = new HashSet<>(undirectGraph.keySet());
        for(Map<Station, Double> destinos : undirectGraph.values()) {
            allStations.addAll(destinos.keySet());
        }

        // Inicializar com 0
        for(Station s : allStations) {
            strength.put(s, 0.0);
        }

        // Calcular strength - adicionar peso apenas à origem
        // Como o grafo é não-direcionado, cada aresta aparece nas duas direções
        for(Map.Entry<Station, Map<Station, Double>> outerEntry : undirectGraph.entrySet()) {
            Station origem = outerEntry.getKey();

            for(Double peso : outerEntry.getValue().values()) {
                strength.merge(origem, peso, Double::sum);
            }
        }

        // Normalizar
        double maxStrength = strength.values().stream()
                .max(Double::compareTo)
                .orElse(0.0);

        Map<Station, Double> normalizedStrength = new HashMap<>();

        if(maxStrength > 0.0) {
            for(Map.Entry<Station, Double> entry : strength.entrySet()) {
                normalizedStrength.put(entry.getKey(), entry.getValue() / maxStrength);
            }
        } else {
            for(Station s : allStations) {
                normalizedStrength.put(s, 0.0);
            }
        }

        return normalizedStrength;
    }
    public Map<Station, Double> normalizeHarmonicCloseness(Map<Station, Double> harmonicCloseness) {
        Map<Station, Double> normalized = new HashMap<>();

        double maxValue = harmonicCloseness.values().stream()
                .max(Double::compareTo)
                .orElse(0.0);

        if(maxValue > 0.0) {
            for(Map.Entry<Station, Double> entry : harmonicCloseness.entrySet()) {
                normalized.put(entry.getKey(), entry.getValue() / maxValue);
            }
        } else {
            for(Station s : harmonicCloseness.keySet()) {
                normalized.put(s, 0.0);
            }
        }

        return normalized;
    }

    public Map<Station, Double> normalizeBetweenness(Map<Station, Double> betweenness) {
        Map<Station, Double> normalized = new HashMap<>();

        double maxValue = betweenness.values().stream()
                .max(Double::compareTo)
                .orElse(0.0);

        if(maxValue > 0.0) {
            for(Map.Entry<Station, Double> entry : betweenness.entrySet()) {
                normalized.put(entry.getKey(), entry.getValue() / maxValue);
            }
        } else {
            for(Station s : betweenness.keySet()) {
                normalized.put(s, 0.0);
            }
        }

        return normalized;
    }

    public Map<Station, Double> calculateHubScore(
            Map<Station, Double> força,
            Map<Station, Double> harmonicClosenessNorm,
            Map<Station, Double> betweenessNorm,
            Set<Station> allStations) {

        Map<Station, Double> hubScores = new HashMap<>();

        for(Station s : allStations) {
            if(!força.containsKey(s) || !harmonicClosenessNorm.containsKey(s) || !betweenessNorm.containsKey(s)) {
                throw new IllegalStateException("Estação " + s + " não encontrada em todos os mapas de métricas");
            }

            double hubScore = (0.35 * betweenessNorm.get(s)) +
                    (0.35 * harmonicClosenessNorm.get(s)) +
                    (0.30 * força.get(s));

            hubScores.put(s, hubScore);
        }

        return hubScores;
    }
    public Map<Station, Integer> calculateDegree(Map<Station, Map<Station, Double>> undirectGraph) {
        Map<Station, Integer> degree = new HashMap<>();

        // Obter todas as estações do grafo
        Set<Station> allStations = new HashSet<>(undirectGraph.keySet());
        for(Map<Station, Double> destinos : undirectGraph.values()) {
            allStations.addAll(destinos.keySet());
        }

        // Inicializar com 0
        for(Station s : allStations) {
            degree.put(s, 0);
        }

        // Calcular o grau de cada estação
        // Como o grafo é não-direcionado, contamos apenas as arestas que saem de cada nó
        for(Map.Entry<Station, Map<Station, Double>> entry : undirectGraph.entrySet()) {
            Station origem = entry.getKey();
            int numConexoes = entry.getValue().size();
            degree.put(origem, numConexoes);
        }

        return degree;
    }


    public HubRepository getHubRepository() {
        return hubRepository;
    }
}
