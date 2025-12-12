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

        // Inicializar centralidades com 0
        for (Station station : undirectGraph.keySet()) {
            centralidades.put(station, 0.0);
        }

        // Para cada vértice como origem
        for (Station origem : undirectGraph.keySet()) {
            Map<Station, Double> distancias = new HashMap<>();
            Map<Station, Integer> numeroCaminhosCurtos = new HashMap<>();
            Map<Station, List<Station>> predecessores = new HashMap<>();
            Stack<Station> stack = new Stack<>();

            // Inicializar estruturas
            for (Station s : undirectGraph.keySet()) {
                distancias.put(s, Double.MAX_VALUE);
                numeroCaminhosCurtos.put(s, 0);
                predecessores.put(s, new ArrayList<>());
            }

            distancias.put(origem, 0.0);
            numeroCaminhosCurtos.put(origem, 1);

            PriorityQueue<Station> fila = new PriorityQueue<>(
                    Comparator.comparingDouble(distancias::get)
            );
            fila.add(origem);

            // Dijkstra modificado para este caso
            while (!fila.isEmpty()) {
                Station atual = fila.poll();
                stack.push(atual);

                if (undirectGraph.containsKey(atual)) {
                    for (Map.Entry<Station, Double> vizinho : undirectGraph.get(atual).entrySet()) {
                        Station proxima = vizinho.getKey();
                        double peso = vizinho.getValue();
                        double novaDistancia = distancias.get(atual) + peso;

                        // Caminho mais curto encontrado
                        if (novaDistancia < distancias.get(proxima)) {
                            distancias.put(proxima, novaDistancia);
                            fila.remove(proxima); // Remove se já estava na fila
                            fila.add(proxima);
                            numeroCaminhosCurtos.put(proxima, numeroCaminhosCurtos.get(atual));
                            predecessores.get(proxima).clear();
                            predecessores.get(proxima).add(atual);
                        }
                        // Outros caminso mais curtos com mesma distância
                        else if (novaDistancia == distancias.get(proxima)) {
                            numeroCaminhosCurtos.put(proxima,
                                    numeroCaminhosCurtos.get(proxima) + numeroCaminhosCurtos.get(atual));
                            predecessores.get(proxima).add(atual);
                        }
                    }
                }
            }

            // Acumulação das dependências
            Map<Station, Double> dependencias = new HashMap<>();
            for (Station s : undirectGraph.keySet()) {
                dependencias.put(s, 0.0);
            }

            while (!stack.isEmpty()) {
                Station atual = stack.pop();

                for (Station predecessor : predecessores.get(atual)) {
                    double contribuicao = (numeroCaminhosCurtos.get(predecessor) /
                            (double) numeroCaminhosCurtos.get(atual)) *
                            (1.0 + dependencias.get(atual));
                    dependencias.put(predecessor, dependencias.get(predecessor) + contribuicao);
                }

                if (!atual.equals(origem)) {
                    centralidades.put(atual, centralidades.get(atual) + dependencias.get(atual));
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
}
