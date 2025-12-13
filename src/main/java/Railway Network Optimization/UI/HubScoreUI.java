package UI;

import Controller.HubController;
import Domain.Station;


import java.util.Map;
import java.util.Scanner;
import java.util.Set;

public class HubScoreUI implements Runnable {
    private final HubController hubController;
    private final Scanner scanner = new Scanner(System.in);

    public HubScoreUI(HubController hubController) {
        this.hubController = hubController;
    }

    @Override
    public void run() {
        System.out.println("\n=== Rail Hub Centrality Analysis ===\n");

        // Calcular todas as métricas uma única vez
        Map<Station, Map<Station, Double>> uGraph = hubController.getHubRepository().getLoader().buildGraph();
        Map<Station, Integer> degrees = hubController.calculateDegree(uGraph);
        Map<Station, Double> strengths = hubController.calculateStrengthNorm(uGraph);
        Map<Station, Double> betweennesses = hubController.calculateBetweennessCentrality(uGraph);
        Map<Station, Double> harmonics = hubController.calculateHarmonicCloseness(uGraph);
        Map<Station, Double> hubScores = hubController.calculateHubScore(strengths, harmonics, betweennesses,
                hubController.getHubRepository().getUndirectedStations());

        // Definir larguras das colunas
        int idWidth = 12;
        int nameWidth = 30;
        int degreeWidth = 8;
        int strengthWidth = 10;
        int betweennessWidth = 13;
        int harmonicWidth = 18;
        int scoreWidth = 11;

        // Imprimir cabeçalho
        printLine(idWidth, nameWidth, degreeWidth, strengthWidth, betweennessWidth, harmonicWidth, scoreWidth);
        System.out.printf("| %-" + idWidth + "s | %-" + nameWidth + "s | %-" + degreeWidth + "s | %-" + strengthWidth + "s | %-" + betweennessWidth + "s | %-" + harmonicWidth + "s | %-" + scoreWidth + "s |%n",
                "Station ID", "Station Name", "Degree", "Strength", "Betweenness", "Harmonic Closeness", "Hub Score");
        printLine(idWidth, nameWidth, degreeWidth, strengthWidth, betweennessWidth, harmonicWidth, scoreWidth);

        // Imprimir dados de cada estação
        for (Station s : hubController.getHubRepository().getUndirectedStations()) {
            String name = truncate(s.getName(), nameWidth);
            String id = truncate(s.getId(), idWidth);
            int degree = degrees.get(s);
            double strength = strengths.get(s);
            double betweenness = betweennesses.get(s);
            double harmonic = harmonics.get(s);
            double score = hubScores.get(s);

            System.out.printf("| %-" + idWidth + "s | %-" + nameWidth + "s | %" + degreeWidth + "d | %" + strengthWidth + ".4f | %" + betweennessWidth + ".4f | %" + harmonicWidth + ".4f | %" + scoreWidth + ".4f |%n",
                    id, name, degree, strength, betweenness, harmonic, score);
        }

        printLine(idWidth, nameWidth, degreeWidth, strengthWidth, betweennessWidth, harmonicWidth, scoreWidth);
        System.out.println();
    }

    private void printLine(int... widths) {
        System.out.print("+");
        for (int width : widths) {
            for (int i = 0; i < width + 2; i++) {
                System.out.print("-");
            }
            System.out.print("+");
        }
        System.out.println();
    }

    private String truncate(String text, int maxWidth) {
        if (text.length() <= maxWidth) {
            return text;
        }
        return text.substring(0, maxWidth - 3) + "...";
    }
}
