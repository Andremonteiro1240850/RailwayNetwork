package UI;

import Controller.StationController;
import java.util.Scanner;

public class StationUI implements Runnable {
    private final StationController stationController;
    private final Scanner scanner;

    public StationUI(StationController stationController, Scanner scanner) {
        this.stationController = stationController;
        this.scanner = scanner;
    }

    @Override
    public void run() {
        boolean running = true;

        while (running) {
            System.out.println("\n=== STATION LOOKUP ===");
            System.out.println("1. Show Stations by Country");
            System.out.println("2. Show Stations by Time Zone Group");
            System.out.println("3. Show Stations by Latitude Range");
            System.out.println("4. Show Stations by Longitude Range");
            System.out.println("5. Exit");

            System.out.print("Select an option: ");
            String line = scanner.nextLine();
            int option = Integer.parseInt(line);

            switch (option) {
                case 1:
                    showStationsByCountry();
                    break;
                case 2:
                    showStationsByTimeZoneGroup();
                    break;
                case 3:
                    showStationsByLatitudeRange();
                    break;
                case 4:
                    showStationsByLongitudeRange();
                    break;
                case 5:
                    System.out.println("Exiting Station Lookup...");
                    running = false;
                    break;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }

    private void showStationsByCountry() {
        System.out.println("Select a Country: ");
        String country = scanner.nextLine();
        stationController.getStationByCountry(country).forEach(station -> {
            System.out.println(station.getName() + " - " + station.getCountry());
        });
    }

    private void showStationsByTimeZoneGroup() {
        System.out.println("Select a Time Zone Group: ");
        String timeZoneGroup = scanner.nextLine();
        stationController.getStationByTimeZone(timeZoneGroup).forEach(station -> {
            System.out.println(station.getName() + " - " + station.getTimeZoneGroup());
        });
    }

    private void showStationsByLatitudeRange() {
        System.out.println("Select the min Latitude: ");
        double latitudeMin = scanner.nextDouble();
        System.out.println("Select the max Latitude: ");
        double latitudeMax = scanner.nextDouble();
        scanner.nextLine(); // Consumir newline

        stationController.getStationByLatitude(latitudeMin, latitudeMax).forEach(station -> {
            System.out.println(station.getName() + " - " + station.getLatitude());
        });
    }

    private void showStationsByLongitudeRange() {
        System.out.println("Select the min Longitude: ");
        double longitudeMin = scanner.nextDouble();
        System.out.println("Select the max Longitude: ");
        double longitudeMax = scanner.nextDouble();
        scanner.nextLine(); // Consumir newline

        stationController.getStationByLongitude(longitudeMin, longitudeMax).forEach(station -> {
            System.out.println(station.getName() + " - " + station.getLongitude());
        });
    }
}