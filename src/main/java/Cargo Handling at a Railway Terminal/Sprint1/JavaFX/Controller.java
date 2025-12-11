package Sprint1.JavaFX;

import Sprint1.Controller.*;
import Sprint1.Domain.DataLoader;
import Sprint1.Domain.Warehouse;
import Sprint1.Repository.BoxRepository;
import Sprint1.Repository.TrolleyRepository;
import Sprint1.Repository.WarehouseRepository;
import Sprint2.Controller.KDTreeController;
import Sprint2.Controller.ProximitySearchController;
import Sprint2.Controller.RadiusSearchController;
import Sprint2.Controller.StationController;
import Sprint2.Domain.StationLoader;
import Sprint2.JavaFX.*;
import Sprint2.Repository.KDTreeRepository;
import Sprint2.Repository.StationRepository;
import JavaFX.UpgradePlanFXUI;
import javafx.fxml.FXML;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.animation.ScaleTransition;
import javafx.util.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import Sprint2.Controller.GeographicalSearchController;

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

    /**
     * Initialize station data if not already loaded
     */
    private void ensureStationDataLoaded() {
        if (stationRepository == null) {
            try {
                System.out.println("Loading stations from CSV...");
                stationRepository = new StationRepository(
                    stationLoader.loadFromCSV("train_station_dataset/train_stations_europe.csv")
                );
                System.out.println("Stations loaded successfully! Total: " + stationRepository.totalStations());
            } catch (Exception e) {
                System.out.println("Error loading stations: " + e.getMessage());
                e.printStackTrace();
                showError("Station Loading Error", "Failed to load stations: " + e.getMessage());
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
        executorService.execute(() -> {
            ensureDataLoaded();
            try {
                Warehouse warehouse = new Warehouse(warehouseRepository);
                warehouse.loadItems("train_station_dataset/items.csv");
                warehouse.loadWagons("train_station_dataset/wagons.csv");
                warehouse.loadOrders("train_station_dataset/orders.csv", "train_station_dataset/order_lines.csv");
                warehouseRepository.saveStock(boxRepository.getAllAssignedBoxes());
                
                WarehouseController controller = new WarehouseController(warehouse, warehouseRepository);
                
                Platform.runLater(() -> {
                    OrderEligibilityFXUI ui = new OrderEligibilityFXUI(controller, boxRepository);
                    ui.show();
                });
            } catch (Exception e) {
                Platform.runLater(() -> showError("Error", "Failed to start Order Eligibility: " + e.getMessage()));
            }
        });
    }

    @FXML
    private void handlePickingPlansButton() {
        executorService.execute(() -> {
            ensureDataLoaded();
            try {
                PickingPlanController controller = new PickingPlanController(warehouseRepository);
                
                Platform.runLater(() -> {
                    PickingPlanFXUI ui = new PickingPlanFXUI(controller);
                    ui.show();
                });
            } catch (Exception e) {
                Platform.runLater(() -> showError("Error", "Failed to start Picking Plans: " + e.getMessage()));
            }
        });
    }

    @FXML
    private void handlePickPathButton() {
        executorService.execute(() -> {
            ensureDataLoaded();
            try {
                PickPathController controller = new PickPathController(trolleyRepository);
                
                Platform.runLater(() -> {
                    PickPathFXUI ui = new PickPathFXUI(controller);
                    ui.show();
                });
            } catch (Exception e) {
                Platform.runLater(() -> showError("Error", "Failed to start Pick Path Sequencing: " + e.getMessage()));
            }
        });
    }

    @FXML
    private void handleReturnsQuarantineButton() {
        executorService.execute(() -> {
            ensureDataLoaded();
            try {
                WagonsUnloadingController wagonsController = new WagonsUnloadingController(dataLoader);
                QualityOperatorController controller = new QualityOperatorController(wagonsController);
                
                Platform.runLater(() -> {
                    ReturnsQuarantineFXUI ui = new ReturnsQuarantineFXUI(controller);
                    ui.show();
                });
            } catch (Exception e) {
                Platform.runLater(() -> showError("Error", "Failed to start Returns & Quarantine: " + e.getMessage()));
            }
        });
    }

    @FXML
    private void handleStationLookupButton() {
        executorService.execute(() -> {
            ensureStationDataLoaded();
            if (stationRepository == null) {
                Platform.runLater(() -> showError("Error", "Failed to load stations"));
                return;
            }
            try {
                StationController stationController = new StationController(stationRepository);
                
                Platform.runLater(() -> {
                    StationLookupFXUI ui = new StationLookupFXUI(stationController);
                    ui.show();
                });
            } catch (Exception e) {
                Platform.runLater(() -> showError("Error", "Failed to start Station Lookup: " + e.getMessage()));
            }
        });
    }

    @FXML
    private void handleKDTreeButton() {
        executorService.execute(() -> {
            ensureStationDataLoaded();
            if (stationRepository == null) {
                Platform.runLater(() -> showError("Error", "Failed to load stations"));
                return;
            }
            try {
                if (kdTreeController == null) {
                    kdTreeController = new KDTreeController(stationRepository);
                }
                
                Platform.runLater(() -> {
                    KDTreeFXUI ui = new KDTreeFXUI(kdTreeController);
                    ui.show();
                });
            } catch (Exception e) {
                Platform.runLater(() -> showError("Error", "Failed to start KD-Tree Building: " + e.getMessage()));
            }
        });
    }

    @FXML
    private void handleGeographicalSearchButton() {
        executorService.execute(() -> {
            ensureStationDataLoaded();
            if (stationRepository == null) {
                Platform.runLater(() -> showError("Error", "Failed to load stations"));
                return;
            }
            try {
                if (kdTreeController == null) {
                    kdTreeController = new KDTreeController(stationRepository);
                }
                if (!kdTreeController.isTreeBuilt()) {
                    Platform.runLater(() -> showError("Error", "KD-tree not built yet. Please build the KD-tree first."));
                    return;
                }
                KDTreeRepository kdTreeRepository = kdTreeController.getRepository();
                GeographicalSearchController searchController = new GeographicalSearchController(kdTreeRepository, stationRepository);
                
                Platform.runLater(() -> {
                    GeographicalSearchFXUI ui = new GeographicalSearchFXUI(searchController);
                    ui.show();
                });
            } catch (Exception e) {
                Platform.runLater(() -> showError("Error", "Failed to start Geographical Search: " + e.getMessage()));
            }
        });
    }

    @FXML
    private void handleProximitySearchButton() {
        executorService.execute(() -> {
            ensureStationDataLoaded();
            if (stationRepository == null) {
                Platform.runLater(() -> showError("Error", "Failed to load stations"));
                return;
            }
            try {
                if (kdTreeController == null) {
                    kdTreeController = new KDTreeController(stationRepository);
                }
                if (!kdTreeController.isTreeBuilt()) {
                    Platform.runLater(() -> showError("Error", "KD-tree not built yet. Please build the KD-tree first."));
                    return;
                }
                ProximitySearchController searchController = new ProximitySearchController(kdTreeController, stationRepository);
                
                Platform.runLater(() -> {
                    ProximitySearchFXUI ui = new ProximitySearchFXUI(searchController);
                    ui.show();
                });
            } catch (Exception e) {
                Platform.runLater(() -> showError("Error", "Failed to start Proximity Search: " + e.getMessage()));
            }
        });
    }

    @FXML
    private void handleRadiusSearchButton() {
        executorService.execute(() -> {
            ensureStationDataLoaded();
            if (stationRepository == null) {
                Platform.runLater(() -> showError("Error", "Failed to load stations"));
                return;
            }
            try {
                if (kdTreeController == null) {
                    kdTreeController = new KDTreeController(stationRepository);
                }
                if (!kdTreeController.isTreeBuilt()) {
                    Platform.runLater(() -> showError("Error", "KD-tree not built yet. Please build the KD-tree first."));
                    return;
                }
                KDTreeRepository kdTreeRepository = kdTreeController.getKDTreeRepository();
                RadiusSearchController searchController = new RadiusSearchController(kdTreeRepository, stationRepository);
                
                Platform.runLater(() -> {
                    RadiusSearchFXUI ui = new RadiusSearchFXUI(searchController);
                    ui.show();
                });
            } catch (Exception e) {
                Platform.runLater(() -> showError("Error", "Failed to start Radius Search: " + e.getMessage()));
            }
        });
    }

    @FXML
    private void handleUpgradePlanButton() {
        executorService.execute(() -> {
            try {
                Platform.runLater(() -> {
                    UpgradePlanFXUI ui = new UpgradePlanFXUI();
                    ui.show();
                });
            } catch (Exception e) {
                Platform.runLater(() -> showError("Error", "Failed to start Upgrade Plan: " + e.getMessage()));
            }
        });
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

    @FXML
    private void handleDevelopmentTeamButton() {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Development Team");
            alert.setHeaderText("Team G044 - 2025/26");
            StringBuilder content = new StringBuilder();
            content.append("Team Members:\n\n");
            content.append("• Hugo Azevedo (Nº 1240835)\n");
            content.append("• Francisco Sampaio (Nº 1240830)\n");
            content.append("• André Monteiro (Nº 1240850)\n");
            content.append("• João Silva (Nº 1240868)\n\n");
            content.append("Semester 3 - Project Integration\n");
            content.append("Academic Year 2024-2025\n\n");
            content.append("Railway Terminal Management System\n");
            content.append("Version 2.0");
            alert.setContentText(content.toString());
            alert.showAndWait();
        });
    }

    @FXML
    private void handleApplicationInfoButton() {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Application Information");
            alert.setHeaderText("System Details");
            StringBuilder content = new StringBuilder();
            content.append("Version: 2.0\n\n");
            content.append("Framework: JavaFX 17\n");
            content.append("Build Tool: Maven\n");
            content.append("Java Version: 17+\n\n");
            content.append("Platform: JavaFX Desktop Application\n");
            content.append("Architecture: MVC Pattern");
            alert.setContentText(content.toString());
            alert.showAndWait();
        });
    }

    @FXML
    private void handleFeaturesOverviewButton() {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Features Overview");
            alert.setHeaderText("Available Functionalities");
            StringBuilder content = new StringBuilder();
            content.append("Cargo Handling:\n");
            content.append("• Wagons Unloading (FEFO/FIFO)\n");
            content.append("• Order Eligibility & Allocation\n");
            content.append("• Picking Plans Generation\n");
            content.append("• Pick Path Sequencing\n");
            content.append("• Returns & Quarantine Management\n\n");
            content.append("Spatial Queries:\n");
            content.append("• Time-Zone Queries\n");
            content.append("• KD-Tree Construction\n");
            content.append("• Geographical Area Search\n");
            content.append("• Proximity Search (Nearest-N)\n");
            content.append("• Radius Search with Density Summary");
            alert.setContentText(content.toString());
            alert.showAndWait();
        });
    }

    @FXML
    private void handleTechnicalDetailsButton() {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Technical Details");
            alert.setHeaderText("Architecture & Implementation");
            StringBuilder content = new StringBuilder();
            content.append("Data Structures:\n");
            content.append("• KD-Tree for spatial indexing\n");
            content.append("• AVL Tree for balanced operations\n");
            content.append("• Repository pattern for data management\n\n");
            content.append("Architecture:\n");
            content.append("• MVC (Model-View-Controller) pattern\n");
            content.append("• Separation of concerns\n");
            content.append("• Modular design with Sprint-based organization\n\n");
            content.append("Design Patterns:\n");
            content.append("• Repository Pattern\n");
            content.append("• Controller Pattern\n");
            content.append("• Factory Pattern");
            alert.setContentText(content.toString());
            alert.showAndWait();
        });
    }

    /**
     * Adds hover effects to a button with scale and color animations
     */
    private void addHoverEffects(Button button) {
        ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(200), button);
        scaleTransition.setFromX(1.0);
        scaleTransition.setFromY(1.0);
        scaleTransition.setToX(1.03);
        scaleTransition.setToY(1.03);
        
        ScaleTransition scaleOut = new ScaleTransition(Duration.millis(200), button);
        scaleOut.setFromX(1.03);
        scaleOut.setFromY(1.03);
        scaleOut.setToX(1.0);
        scaleOut.setToY(1.0);
        
        button.setOnMouseEntered(e -> {
            scaleTransition.play();
            String currentStyle = button.getStyle();
            currentStyle = currentStyle.replace("#6366F1", "#818CF8");
            currentStyle = currentStyle.replace("rgba(99, 102, 241, 0.4)", "rgba(129, 140, 248, 0.6)");
            button.setStyle(currentStyle);
        });
        
        button.setOnMouseExited(e -> {
            scaleOut.play();
            String currentStyle = button.getStyle();
            currentStyle = currentStyle.replace("#818CF8", "#6366F1");
            currentStyle = currentStyle.replace("rgba(129, 140, 248, 0.6)", "rgba(99, 102, 241, 0.4)");
            button.setStyle(currentStyle);
        });
        
        button.setOnMousePressed(e -> {
            String currentStyle = button.getStyle();
            currentStyle = currentStyle.replace("#818CF8", "#4F46E5");
            button.setStyle(currentStyle);
        });
        
        button.setOnMouseReleased(e -> {
            String currentStyle = button.getStyle();
            currentStyle = currentStyle.replace("#4F46E5", "#818CF8");
            button.setStyle(currentStyle);
        });
    }
}