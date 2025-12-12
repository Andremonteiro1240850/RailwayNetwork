package Repository;

import Domain.DataLoader;
import Domain.Station;

import java.util.*;

public class HubRepository {
    private final DataLoader loader;

    public HubRepository(DataLoader loader) {
        this.loader = loader;
    }

    public Map<Station, Map<Station, Double>> getUndirectedGraph() {
        return loader.buildGraph();
    }

    public Map<Station, List<Station>> getConectedStations() {
        Map<Station, List<Station>> estacoesLigadas = new HashMap<>();

        for (Station s : loader.buildGraph().keySet()) {
            List<Station> verticesLigados = new ArrayList<>(); //renova a lista de estações sempre pois cria nova

            for (Station s2 : loader.buildGraph().get(s).keySet()) {
                verticesLigados.add(s2);
            }

            estacoesLigadas.put(s, verticesLigados);
        }
        return estacoesLigadas;
    }
}
