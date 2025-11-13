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
        Scanner scanner = new Scanner(System.in);
        System.out.println("1. Show Stations by Country");
        System.out.println("2. Show Stations by Time Zone Group");
        System.out.println("3. Show Stations by Latitude Range");
        System.out.println("4. Show Stations by Longitude Range");
        System.out.println("5. Exit");
        int option = scanner.nextInt();
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
                System.out.println("Exiting...");
                return;
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
        System.out.println("Select the max Latitude : ");
        double latitudeMax = scanner.nextDouble();
        System.out.println("Select the min Latitude : ");
        double latitudeMin = scanner.nextDouble();
        stationController.getStationByLatitude(latitudeMin, latitudeMax).forEach(station -> {
            System.out.println(station.getName() + " - " + station.getLatitude());
        });
    }
    private void showStationsByLongitudeRange() {
        System.out.println("Select the max Longitude : ");
        double longitudeMax = scanner.nextDouble();
        System.out.println("Select the min Longitude : ");
        double longitudeMin = scanner.nextDouble();
        stationController.getStationByLongitude(longitudeMin, longitudeMax).forEach(station -> {
            System.out.println(station.getName() + " - " + station.getLongitude());
        });
    }
}

