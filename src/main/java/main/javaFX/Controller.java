package main.javaFX;

import Controller.*;
import Domain.*;
import Repository.*;
import javafx.fxml.FXML;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Controller {
    private static DataLoader dataLoader;
    private static boolean dataLoaded = false;
    private static WarehouseRepository warehouseRepository;
    private static TrolleyRepository trolleyRepository;
    private static BoxRepository boxRepository;
    private static StationRepository stationRepository;
    private static StationLoader stationLoader = new StationLoader();
    private static KDTreeController kdTreeController;
    private static ExecutorService executorService = Executors.newCachedThreadPool();

    /**
     * Initialize data if not already loaded
     */
    private void ensureDataLoaded() {
        if (!dataLoaded) {
            try {
                System.out.println("Loading data from CSV files...");
                dataLoader = new DataLoader();
                dataLoader.loadItems("train_station_dataset/items.csv");
                dataLoader.loadBays("train_station_dataset/bays.csv");
                dataLoader.loadWagons("train_station_dataset/wagons.csv");
                dataLoader.loadOrders("train_station_dataset/orders.csv", "train_station_dataset/order_lines.csv");
                dataLoader.loadReturns("train_station_dataset/returns.csv");

                boxRepository = dataLoader.getBoxRepository();
                warehouseRepository = new WarehouseRepository();
                trolleyRepository = new TrolleyRepository();

                dataLoaded = true;
                System.out.println("All data loaded successfully!");
            } catch (Exception e) {
                System.out.println("Error loading data: " + e.getMessage());
                e.printStackTrace();
                showError("Data Loading Error", "Failed to load data: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleWagonsButton() {
        executorService.execute(() -> {
            ensureDataLoaded();
            System.out.println("\n=== WAGONS UNLOADING ===");
            WagonsUnloadingController wagonsController = new WagonsUnloadingController(dataLoader);
            Platform.runLater(() -> {
                WagonsUnloadingFXUI wagonsUI = new WagonsUnloadingFXUI(wagonsController);
                wagonsUI.show();
            });
        });
    }

    @FXML
    private void handleOrderEligibilityButton() {
        showComingSoon("Order Eligibility & Allocation");
    }

    @FXML
    private void handlePickingPlansButton() {
        showComingSoon("Picking Plans");
    }

    @FXML
    private void handlePickPathButton() {
        showComingSoon("Pick Path Sequencing");
    }

    @FXML
    private void handleReturnsQuarantineButton() {
        showComingSoon("Returns & Quarantine");
    }

    @FXML
    private void handleStationLookupButton() {
        showComingSoon("Station Lookup");
    }

    @FXML
    private void handleKDTreeButton() {
        showComingSoon("KD-Tree Building");
    }

    @FXML
    private void handleGeographicalSearchButton() {
        showComingSoon("Geographical Search");
    }

    @FXML
    private void handleProximitySearchButton() {
        showComingSoon("Proximity Search");
    }

    @FXML
    private void handleRadiusSearchButton() {
        showComingSoon("Radius Search");
    }

    private void showComingSoon(String featureName) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Coming Soon");
            alert.setHeaderText("🚧 Feature Under Development");
            alert.setContentText(featureName + " is currently being implemented.\n\n" +
                    "This feature will be available in a future release.\n\n" +
                    "Currently only 'Wagons Unloading' is fully functional.");
            alert.showAndWait();
        });
    }

    private void showError(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
}