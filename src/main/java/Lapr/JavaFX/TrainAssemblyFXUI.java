package Lapr.JavaFX;

import Lapr.Controller.TrainAssemblyController;
import Lapr.DataBaseAssociation.TrainAOD;
import Lapr.Domain.RoutePlannerRoute;
import Sprint1.Domain.Locomotive;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * JavaFX UI for USLP09 - Assemble and Assign Train to Route.
 * 
 * Allows Traffic Manager to:
 * - Select a route
 * - View locomotives and wagons with their status (in transit or parked)
 * - Select vehicles to assemble a train
 * - Assign the train to the selected route
 * 
 * @author Development Team
 * @version 1.0
 */
public class TrainAssemblyFXUI {
    private TrainAssemblyController controller;
    private Stage stage;
    
    // Route selection
    private ComboBox<RoutePlannerRoute> routeComboBox;
    private Label routeInfoLabel;
    
    // Locomotives
    private ListView<TrainAOD.LocomotiveWithStatus> locomotivesList;
    private Label locomotivesTitleLabel;
    private ObservableList<TrainAOD.LocomotiveWithStatus> locomotivesData;
    
    // Wagons (from selected route)
    private ListView<TrainAOD.WagonWithStatus> wagonsList;
    private Label wagonsTitleLabel;
    private ObservableList<TrainAOD.WagonWithStatus> wagonsData;
    
    // Selected items
    private ListView<Locomotive> selectedLocomotivesList;
    private ListView<RoutePlannerRoute.RouteWagon> selectedWagonsList;
    
    // Buttons
    private Button addLocomotiveButton;
    private Button removeLocomotiveButton;
    private Button addWagonButton;
    private Button removeWagonButton;
    private Button assignButton;
    private Button closeButton;
    private Button refreshRoutesButton;
    
    // Status
    private Label statusLabel;
    private ProgressIndicator progressIndicator;
    
    // Data
    private List<RoutePlannerRoute> availableRoutes;
    private List<Locomotive> availableLocomotives;
    
    public TrainAssemblyFXUI() {
        this.controller = new TrainAssemblyController();
    }
    
    /**
     * Shows the train assembly UI window.
     */
    public void show() {
        stage = new Stage();
        stage.setTitle("Assemble and Assign Train to Route (USLP09)");
        
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #1B2533;");
        root.setPadding(new Insets(20));
        
        // Top: Title
        VBox topBox = new VBox(15);
        topBox.setAlignment(Pos.CENTER);
        topBox.setPadding(new Insets(0, 0, 20, 0));
        
        Label titleLabel = new Label("Assemble and Assign Train to Route");
        titleLabel.setStyle("-fx-font-family: 'Segoe UI', 'Roboto', sans-serif; " +
                           "-fx-font-size: 28px; " +
                           "-fx-font-weight: 700; " +
                           "-fx-text-fill: #E2E8F0; " +
                           "-fx-letter-spacing: 0.5px;");
        
        Label descriptionLabel = new Label(
            "Select a route created in US08 and choose locomotives to assemble a train. " +
            "The wagons from the selected route will be automatically included."
        );
        descriptionLabel.setStyle("-fx-font-family: 'Segoe UI', 'Roboto', sans-serif; " +
                                  "-fx-font-size: 13px; " +
                                  "-fx-text-fill: #94A3B8;");
        descriptionLabel.setWrapText(true);
        
        topBox.getChildren().addAll(titleLabel, descriptionLabel);
        root.setTop(topBox);
        
        // Center: Main content
        HBox centerBox = new HBox(20);
        centerBox.setPadding(new Insets(15));
        
        // Left: Route selection and vehicle lists
        VBox leftColumn = new VBox(15);
        leftColumn.setPrefWidth(500);
        
        // Route selection
        VBox routeBox = new VBox(10);
        routeBox.setStyle("-fx-background-color: #2D3748; " +
                         "-fx-background-radius: 12px; " +
                         "-fx-padding: 15px;");
        
        HBox routeHeaderBox = new HBox(10);
        routeHeaderBox.setAlignment(Pos.CENTER_LEFT);
        
        Label routeLabel = new Label("Select Route (from US08):");
        routeLabel.setStyle("-fx-font-family: 'Segoe UI', 'Roboto', sans-serif; " +
                           "-fx-font-size: 14px; " +
                           "-fx-font-weight: 600; " +
                           "-fx-text-fill: #E2E8F0;");
        
        refreshRoutesButton = new Button("Refresh");
        refreshRoutesButton.setStyle("-fx-font-family: 'Segoe UI', 'Roboto', sans-serif; " +
                                    "-fx-font-size: 12px; " +
                                    "-fx-background-color: #6366F1; " +
                                    "-fx-text-fill: #FFFFFF; " +
                                    "-fx-background-radius: 6px; " +
                                    "-fx-cursor: hand;");
        refreshRoutesButton.setOnAction(e -> loadRoutes());
        
        HBox.setHgrow(routeLabel, Priority.ALWAYS);
        routeHeaderBox.getChildren().addAll(routeLabel, refreshRoutesButton);
        
        routeComboBox = new ComboBox<>();
        routeComboBox.setPrefWidth(450);
        routeComboBox.setStyle("-fx-font-family: 'Segoe UI', 'Roboto', sans-serif; " +
                              "-fx-font-size: 13px; " +
                              "-fx-background-color: #475569; " +
                              "-fx-text-fill: #E2E8F0;");
        routeComboBox.setOnAction(e -> {
            onRouteSelected();
            updateAssignButtonState();
        });
        
        // Set converter to display route text
        routeComboBox.setConverter(new StringConverter<RoutePlannerRoute>() {
            @Override
            public String toString(RoutePlannerRoute route) {
                if (route == null) {
                    return "Select a route...";
                }
                return route.getRouteName() + " (ID: " + route.getRouteId() + ")";
            }
            
            @Override
            public RoutePlannerRoute fromString(String string) {
                return null;
            }
        });
        
        routeInfoLabel = new Label("No route selected");
        routeInfoLabel.setStyle("-fx-font-family: 'Segoe UI', 'Roboto', sans-serif; " +
                                "-fx-font-size: 12px; " +
                                "-fx-text-fill: #94A3B8;");
        routeInfoLabel.setWrapText(true);
        
        routeBox.getChildren().addAll(routeHeaderBox, routeComboBox, routeInfoLabel);
        
        // Locomotives section
        VBox locomotivesBox = new VBox(10);
        locomotivesBox.setStyle("-fx-background-color: #2D3748; " +
                               "-fx-background-radius: 12px; " +
                               "-fx-padding: 15px;");
        
        locomotivesTitleLabel = new Label("Locomotives:");
        locomotivesTitleLabel.setStyle("-fx-font-family: 'Segoe UI', 'Roboto', sans-serif; " +
                                      "-fx-font-size: 14px; " +
                                      "-fx-font-weight: 600; " +
                                      "-fx-text-fill: #E2E8F0;");
        
        locomotivesList = new ListView<>();
        locomotivesList.setPrefHeight(200);
        locomotivesList.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        locomotivesList.setStyle("-fx-font-family: 'Consolas', 'Monaco', monospace; " +
                                "-fx-font-size: 12px; " +
                                "-fx-background-color: #1E293B; " +
                                "-fx-text-fill: #E2E8F0;");
        
        locomotivesData = FXCollections.observableArrayList();
        locomotivesList.setItems(locomotivesData);
        
        // Custom cell factory for locomotives with status
        locomotivesList.setCellFactory(lv -> new ListCell<TrainAOD.LocomotiveWithStatus>() {
            @Override
            protected void updateItem(TrainAOD.LocomotiveWithStatus item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    Locomotive loco = item.getLocomotive();
                    String statusText = item.getStatus().equals("IN_TRANSIT") 
                        ? String.format("In Transit → %s", item.getLocationInfo())
                        : String.format("Parked at %s (%.1f km from start)", 
                                       item.getLocationInfo(), 
                                       item.getDistanceFromStart() != null ? item.getDistanceFromStart() : 0.0);
                    setText(String.format("ID: %s | Type: %s | Power: %.1f kW | %s", 
                                         loco.getId(), loco.getType(), loco.getPowerKw(), statusText));
                }
            }
        });
        
        locomotivesBox.getChildren().addAll(locomotivesTitleLabel, locomotivesList);
        
        // Wagons section
        VBox wagonsBox = new VBox(10);
        wagonsBox.setStyle("-fx-background-color: #2D3748; " +
                          "-fx-background-radius: 12px; " +
                          "-fx-padding: 15px;");
        
        wagonsTitleLabel = new Label("Wagons:");
        wagonsTitleLabel.setStyle("-fx-font-family: 'Segoe UI', 'Roboto', sans-serif; " +
                                 "-fx-font-size: 14px; " +
                                 "-fx-font-weight: 600; " +
                                 "-fx-text-fill: #E2E8F0;");
        
        wagonsList = new ListView<>();
        wagonsList.setPrefHeight(200);
        wagonsList.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        wagonsList.setStyle("-fx-font-family: 'Consolas', 'Monaco', monospace; " +
                          "-fx-font-size: 12px; " +
                          "-fx-background-color: #1E293B; " +
                          "-fx-text-fill: #E2E8F0;");
        
        wagonsData = FXCollections.observableArrayList();
        wagonsList.setItems(wagonsData);
        
        // Custom cell factory for wagons with status
        wagonsList.setCellFactory(lv -> new ListCell<TrainAOD.WagonWithStatus>() {
            @Override
            protected void updateItem(TrainAOD.WagonWithStatus item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    String statusText = item.getStatus().equals("IN_TRANSIT") 
                        ? String.format("In Transit → %s", item.getLocationInfo())
                        : String.format("Parked at %s (%.1f km from start)", 
                                       item.getLocationInfo(), 
                                       item.getDistanceFromStart() != null ? item.getDistanceFromStart() : 0.0);
                    setText(String.format("Wagon ID: %d | Cargo: %s | %s", 
                                         item.getWagonId(), item.getCargoType(), statusText));
                }
            }
        });
        
        wagonsBox.getChildren().addAll(wagonsTitleLabel, wagonsList);
        
        leftColumn.getChildren().addAll(routeBox, locomotivesBox, wagonsBox);
        
        // Right: Selected items and actions
        VBox rightColumn = new VBox(15);
        rightColumn.setPrefWidth(400);
        
        // Selected locomotives
        VBox selectedLocomotivesBox = new VBox(10);
        selectedLocomotivesBox.setStyle("-fx-background-color: #2D3748; " +
                                       "-fx-background-radius: 12px; " +
                                       "-fx-padding: 15px;");
        
        Label selectedLocomotivesLabel = new Label("Selected Locomotives:");
        selectedLocomotivesLabel.setStyle("-fx-font-family: 'Segoe UI', 'Roboto', sans-serif; " +
                                         "-fx-font-size: 14px; " +
                                         "-fx-font-weight: 600; " +
                                         "-fx-text-fill: #E2E8F0;");
        
        selectedLocomotivesList = new ListView<>();
        selectedLocomotivesList.setPrefHeight(150);
        selectedLocomotivesList.setStyle("-fx-font-family: 'Consolas', 'Monaco', monospace; " +
                                        "-fx-font-size: 12px; " +
                                        "-fx-background-color: #1E293B; " +
                                        "-fx-text-fill: #E2E8F0;");
        selectedLocomotivesList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> 
            updateAssignButtonState());
        
        // Custom cell factory for selected locomotives
        selectedLocomotivesList.setCellFactory(lv -> new ListCell<Locomotive>() {
            @Override
            protected void updateItem(Locomotive item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("ID: %s | Type: %s | Power: %.1f kW", 
                                         item.getId(), item.getType(), item.getPowerKw()));
                }
            }
        });
        
        addLocomotiveButton = new Button("Add Locomotive");
        addLocomotiveButton.setPrefWidth(180);
        addLocomotiveButton.setStyle("-fx-font-family: 'Segoe UI', 'Roboto', sans-serif; " +
                                    "-fx-font-size: 13px; " +
                                    "-fx-background-color: #6366F1; " +
                                    "-fx-text-fill: #FFFFFF; " +
                                    "-fx-background-radius: 8px; " +
                                    "-fx-cursor: hand;");
        addLocomotiveButton.setOnAction(e -> addSelectedLocomotive());
        addLocomotiveButton.setDisable(true);
        
        removeLocomotiveButton = new Button("Remove Locomotive");
        removeLocomotiveButton.setPrefWidth(180);
        removeLocomotiveButton.setStyle("-fx-font-family: 'Segoe UI', 'Roboto', sans-serif; " +
                                       "-fx-font-size: 13px; " +
                                       "-fx-background-color: #EF4444; " +
                                       "-fx-text-fill: #FFFFFF; " +
                                       "-fx-background-radius: 8px; " +
                                       "-fx-cursor: hand;");
        removeLocomotiveButton.setOnAction(e -> removeSelectedLocomotive());
        removeLocomotiveButton.setDisable(true);
        
        HBox locomotiveButtonsBox = new HBox(10);
        locomotiveButtonsBox.getChildren().addAll(addLocomotiveButton, removeLocomotiveButton);
        
        selectedLocomotivesBox.getChildren().addAll(selectedLocomotivesLabel, selectedLocomotivesList, locomotiveButtonsBox);
        
        // Selected wagons
        VBox selectedWagonsBox = new VBox(10);
        selectedWagonsBox.setStyle("-fx-background-color: #2D3748; " +
                                  "-fx-background-radius: 12px; " +
                                  "-fx-padding: 15px;");
        
        Label selectedWagonsLabel = new Label("Selected Wagons:");
        selectedWagonsLabel.setStyle("-fx-font-family: 'Segoe UI', 'Roboto', sans-serif; " +
                                    "-fx-font-size: 14px; " +
                                    "-fx-font-weight: 600; " +
                                    "-fx-text-fill: #E2E8F0;");
        
        selectedWagonsList = new ListView<>();
        selectedWagonsList.setPrefHeight(150);
        selectedWagonsList.setStyle("-fx-font-family: 'Consolas', 'Monaco', monospace; " +
                                   "-fx-font-size: 12px; " +
                                   "-fx-background-color: #1E293B; " +
                                   "-fx-text-fill: #E2E8F0;");
        selectedWagonsList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> 
            updateAssignButtonState());
        
        // Custom cell factory for selected wagons
        selectedWagonsList.setCellFactory(lv -> new ListCell<RoutePlannerRoute.RouteWagon>() {
            @Override
            protected void updateItem(RoutePlannerRoute.RouteWagon item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("Wagon ID: %d | Cargo: %s", 
                                         item.getWagonId(), item.getCargoType()));
                }
            }
        });
        
        addWagonButton = new Button("Add Wagon");
        addWagonButton.setPrefWidth(180);
        addWagonButton.setStyle("-fx-font-family: 'Segoe UI', 'Roboto', sans-serif; " +
                               "-fx-font-size: 13px; " +
                               "-fx-background-color: #6366F1; " +
                               "-fx-text-fill: #FFFFFF; " +
                               "-fx-background-radius: 8px; " +
                               "-fx-cursor: hand;");
        addWagonButton.setOnAction(e -> addSelectedWagon());
        addWagonButton.setDisable(true);
        
        removeWagonButton = new Button("Remove Wagon");
        removeWagonButton.setPrefWidth(180);
        removeWagonButton.setStyle("-fx-font-family: 'Segoe UI', 'Roboto', sans-serif; " +
                                  "-fx-font-size: 13px; " +
                                  "-fx-background-color: #EF4444; " +
                                  "-fx-text-fill: #FFFFFF; " +
                                  "-fx-background-radius: 8px; " +
                                  "-fx-cursor: hand;");
        removeWagonButton.setOnAction(e -> removeSelectedWagon());
        removeWagonButton.setDisable(true);
        
        HBox wagonButtonsBox = new HBox(10);
        wagonButtonsBox.getChildren().addAll(addWagonButton, removeWagonButton);
        
        selectedWagonsBox.getChildren().addAll(selectedWagonsLabel, selectedWagonsList, wagonButtonsBox);
        
        // Assign button
        assignButton = new Button("Assign Train to Route");
        assignButton.setPrefWidth(380);
        assignButton.setPrefHeight(45);
        assignButton.setStyle("-fx-font-family: 'Segoe UI', 'Roboto', sans-serif; " +
                             "-fx-font-size: 15px; " +
                             "-fx-font-weight: 600; " +
                             "-fx-background-color: #10B981; " +
                             "-fx-text-fill: #FFFFFF; " +
                             "-fx-background-radius: 12px; " +
                             "-fx-cursor: hand; " +
                             "-fx-effect: dropshadow(gaussian, rgba(16, 185, 129, 0.4), 8, 0, 0, 2);");
        assignButton.setOnAction(e -> assignTrainToRoute());
        assignButton.setDisable(true);
        
        rightColumn.getChildren().addAll(selectedLocomotivesBox, selectedWagonsBox, assignButton);
        
        centerBox.getChildren().addAll(leftColumn, rightColumn);
        root.setCenter(centerBox);
        
        // Bottom: Status and close
        HBox bottomBox = new HBox(15);
        bottomBox.setAlignment(Pos.CENTER_LEFT);
        bottomBox.setPadding(new Insets(15, 0, 0, 0));
        
        progressIndicator = new ProgressIndicator();
        progressIndicator.setVisible(false);
        progressIndicator.setPrefSize(20, 20);
        
        statusLabel = new Label("Ready");
        statusLabel.setStyle("-fx-font-family: 'Segoe UI', 'Roboto', sans-serif; " +
                            "-fx-font-size: 13px; " +
                            "-fx-text-fill: #94A3B8;");
        
        closeButton = new Button("Close");
        closeButton.setPrefWidth(150);
        closeButton.setPrefHeight(40);
        closeButton.setStyle("-fx-font-family: 'Segoe UI', 'Roboto', sans-serif; " +
                            "-fx-font-size: 14px; " +
                            "-fx-font-weight: 500; " +
                            "-fx-background-color: #475569; " +
                            "-fx-text-fill: #E2E8F0; " +
                            "-fx-background-radius: 8px; " +
                            "-fx-cursor: hand;");
        closeButton.setOnAction(e -> stage.close());
        
        bottomBox.getChildren().addAll(progressIndicator, statusLabel);
        HBox.setHgrow(statusLabel, Priority.ALWAYS);
        
        HBox bottomContainer = new HBox(20);
        bottomContainer.setAlignment(Pos.CENTER);
        bottomContainer.getChildren().addAll(bottomBox, closeButton);
        
        root.setBottom(bottomContainer);
        
        Scene scene = new Scene(root, 950, 800);
        stage.setScene(scene);
        stage.setResizable(true);
        stage.setMinWidth(950);
        stage.setMinHeight(800);
        
        // Load data
        loadData();
        
        stage.show();
    }
    
    /**
     * Loads routes from US08 and locomotives, and initializes the UI.
     */
    private void loadData() {
        loadRoutes();
        loadLocomotives();
    }
    
    /**
     * Loads routes from US08.
     */
    private void loadRoutes() {
        statusLabel.setText("Loading routes from US08...");
        progressIndicator.setVisible(true);
        if (refreshRoutesButton != null) {
            refreshRoutesButton.setDisable(true);
        }
        
        Service<List<RoutePlannerRoute>> service = new Service<List<RoutePlannerRoute>>() {
            @Override
            protected Task<List<RoutePlannerRoute>> createTask() {
                return new Task<List<RoutePlannerRoute>>() {
                    @Override
                    protected List<RoutePlannerRoute> call() throws Exception {
                        return controller.getAvailableRoutesFromUS08();
                    }
                };
            }
        };
        
        service.setOnSucceeded(e -> {
            availableRoutes = service.getValue();
            if (availableRoutes != null && !availableRoutes.isEmpty()) {
                System.out.println("Found " + availableRoutes.size() + " routes from US08");
                routeComboBox.setItems(FXCollections.observableArrayList(availableRoutes));
                routeComboBox.setCellFactory(lv -> new ListCell<RoutePlannerRoute>() {
                    @Override
                    protected void updateItem(RoutePlannerRoute item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                            setStyle("");
                        } else {
                            setText(item.getRouteName() + " (ID: " + item.getRouteId() + ")");
                            setStyle("-fx-text-fill: #E2E8F0;");
                        }
                    }
                });
                statusLabel.setText("Ready - Select a route to begin (" + availableRoutes.size() + " routes available)");
            } else {
                System.out.println("No routes found from US08");
                statusLabel.setText("No routes available - Please create routes first in US08");
                showError("No Routes", "No routes are available. Please create routes first using US08.");
            }
            
            progressIndicator.setVisible(false);
            if (refreshRoutesButton != null) {
                refreshRoutesButton.setDisable(false);
            }
        });
        
        service.setOnFailed(e -> {
            Platform.runLater(() -> {
                statusLabel.setText("Error loading routes: " + service.getException().getMessage());
                progressIndicator.setVisible(false);
                if (refreshRoutesButton != null) {
                    refreshRoutesButton.setDisable(false);
                }
                showError("Error", "Failed to load routes: " + service.getException().getMessage());
            });
        });
        
        service.start();
    }
    
    /**
     * Loads available locomotives (without status - will be loaded when route is selected).
     */
    private void loadLocomotives() {
        Service<List<Locomotive>> service = new Service<List<Locomotive>>() {
            @Override
            protected Task<List<Locomotive>> createTask() {
                return new Task<List<Locomotive>>() {
                    @Override
                    protected List<Locomotive> call() throws Exception {
                        return controller.getAvailableLocomotives();
                    }
                };
            }
        };
        
        service.setOnSucceeded(e -> {
            availableLocomotives = service.getValue();
            // Locomotives with status will be loaded when a route is selected
        });
        
        service.setOnFailed(e -> {
            Platform.runLater(() -> {
                System.err.println("Error loading locomotives: " + service.getException().getMessage());
                showError("Error", "Failed to load locomotives: " + service.getException().getMessage());
            });
        });
        
        service.start();
    }
    
    /**
     * Handles route selection.
     */
    private void onRouteSelected() {
        RoutePlannerRoute selectedRoute = routeComboBox.getSelectionModel().getSelectedItem();
        if (selectedRoute == null) {
            return;
        }
        
        // Update route info
        StringBuilder routeInfo = new StringBuilder();
        routeInfo.append("Route: ").append(selectedRoute.getRouteName())
                 .append(" (ID: ").append(selectedRoute.getRouteId()).append(")\n");
        routeInfo.append("Type: ").append(selectedRoute.isSimple() ? "Simple" : "Complex").append("\n");
        routeInfo.append("Stations: ");
        List<RoutePlannerRoute.RouteStation> stations = selectedRoute.getStations();
        for (int i = 0; i < stations.size(); i++) {
            if (i > 0) routeInfo.append(" → ");
            routeInfo.append(stations.get(i).getFacility().getName());
        }
        routeInfoLabel.setText(routeInfo.toString());
        
        // Load locomotives and wagons with status for this route
        loadLocomotivesWithStatus(selectedRoute);
        loadWagonsWithStatus(selectedRoute);
    }
    
    /**
     * Loads locomotives with status information for the selected route.
     */
    private void loadLocomotivesWithStatus(RoutePlannerRoute route) {
        if (route == null || route.getStations().isEmpty()) {
            return;
        }
        
        statusLabel.setText("Loading locomotives with status...");
        progressIndicator.setVisible(true);
        
        Service<List<TrainAOD.LocomotiveWithStatus>> service = new Service<List<TrainAOD.LocomotiveWithStatus>>() {
            @Override
            protected Task<List<TrainAOD.LocomotiveWithStatus>> createTask() {
                return new Task<List<TrainAOD.LocomotiveWithStatus>>() {
                    @Override
                    protected List<TrainAOD.LocomotiveWithStatus> call() throws Exception {
                        int startFacilityId = Integer.parseInt(route.getStations().get(0).getFacility().getId());
                        int endFacilityId = Integer.parseInt(route.getStations().get(route.getStations().size() - 1).getFacility().getId());
                        return controller.getLocomotivesWithStatus(startFacilityId, endFacilityId);
                    }
                };
            }
        };
        
        service.setOnSucceeded(e -> {
            List<TrainAOD.LocomotiveWithStatus> locomotivesWithStatus = service.getValue();
            locomotivesData.clear();
            if (locomotivesWithStatus != null) {
                locomotivesData.addAll(locomotivesWithStatus);
                locomotivesTitleLabel.setText("Locomotives (" + locomotivesWithStatus.size() + " available)");
                addLocomotiveButton.setDisable(false);
            }
            progressIndicator.setVisible(false);
            statusLabel.setText("Ready");
        });
        
        service.setOnFailed(e -> {
            Platform.runLater(() -> {
                progressIndicator.setVisible(false);
                statusLabel.setText("Error loading locomotives");
                showError("Error", "Failed to load locomotives: " + service.getException().getMessage());
            });
        });
        
        service.start();
    }
    
    /**
     * Loads wagons with status information for the selected route.
     */
    private void loadWagonsWithStatus(RoutePlannerRoute route) {
        if (route == null || route.getStations().isEmpty()) {
            wagonsData.clear();
            wagonsTitleLabel.setText("Wagons from Route (0 wagons)");
            addWagonButton.setDisable(true);
            return;
        }
        
        List<Integer> wagonIds = new ArrayList<>();
        if (route.getWagons() != null && !route.getWagons().isEmpty()) {
            for (RoutePlannerRoute.RouteWagon wagon : route.getWagons()) {
                wagonIds.add(wagon.getWagonId());
            }
            System.out.println("Found " + wagonIds.size() + " wagons in route " + route.getRouteId());
        } else {
            System.out.println("Route " + route.getRouteId() + " has no wagons (getWagons() returned null or empty)");
            wagonsData.clear();
            wagonsTitleLabel.setText("Wagons from Route (0 wagons)");
            addWagonButton.setDisable(true);
            return;
        }
        
        if (wagonIds.isEmpty()) {
            System.out.println("Wagon IDs list is empty for route " + route.getRouteId());
            wagonsData.clear();
            wagonsTitleLabel.setText("Wagons from Route (0 wagons)");
            addWagonButton.setDisable(true);
            return;
        }
        
        Service<List<TrainAOD.WagonWithStatus>> service = new Service<List<TrainAOD.WagonWithStatus>>() {
            @Override
            protected Task<List<TrainAOD.WagonWithStatus>> createTask() {
                return new Task<List<TrainAOD.WagonWithStatus>>() {
                    @Override
                    protected List<TrainAOD.WagonWithStatus> call() throws Exception {
                        int startFacilityId = Integer.parseInt(route.getStations().get(0).getFacility().getId());
                        int endFacilityId = Integer.parseInt(route.getStations().get(route.getStations().size() - 1).getFacility().getId());
                        return controller.getWagonsWithStatus(startFacilityId, endFacilityId, wagonIds);
                    }
                };
            }
        };
        
        service.setOnSucceeded(e -> {
            List<TrainAOD.WagonWithStatus> wagonsWithStatus = service.getValue();
            wagonsData.clear();
            if (wagonsWithStatus != null && !wagonsWithStatus.isEmpty()) {
                wagonsData.addAll(wagonsWithStatus);
                wagonsTitleLabel.setText("Wagons from Route (" + wagonsWithStatus.size() + " wagons)");
                addWagonButton.setDisable(false);
                System.out.println("Loaded " + wagonsWithStatus.size() + " wagons with status for route " + route.getRouteId());
            } else {
                System.out.println("No wagons with status returned for route " + route.getRouteId());
                wagonsTitleLabel.setText("Wagons from Route (0 wagons)");
                addWagonButton.setDisable(true);
            }
        });
        
        service.setOnFailed(e -> {
            Platform.runLater(() -> {
                Throwable ex = service.getException();
                System.err.println("Error loading wagons: " + (ex != null ? ex.getMessage() : "Unknown error"));
                if (ex != null) {
                    ex.printStackTrace();
                }
                showError("Error", "Failed to load wagons: " + (ex != null ? ex.getMessage() : "Unknown error"));
            });
        });
        
        service.start();
    }
    
    /**
     * Adds selected locomotive to the selected list.
     */
    private void addSelectedLocomotive() {
        TrainAOD.LocomotiveWithStatus selected = locomotivesList.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }
        
        Locomotive loco = selected.getLocomotive();
        ObservableList<Locomotive> selectedList = selectedLocomotivesList.getItems();
        // Check if already added (by ID)
        boolean alreadyAdded = selectedList.stream()
            .anyMatch(l -> l.getId().equals(loco.getId()));
        
        if (!alreadyAdded) {
            selectedList.add(loco);
            updateAssignButtonState();
        } else {
            showError("Error", "Locomotive already selected.");
        }
    }
    
    /**
     * Removes selected locomotive from the selected list.
     */
    private void removeSelectedLocomotive() {
        Locomotive selected = selectedLocomotivesList.getSelectionModel().getSelectedItem();
        if (selected != null) {
            selectedLocomotivesList.getItems().remove(selected);
            updateAssignButtonState();
        }
    }
    
    /**
     * Adds selected wagon to the selected list.
     */
    private void addSelectedWagon() {
        TrainAOD.WagonWithStatus selected = wagonsList.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }
        
        // Convert to RouteWagon for compatibility
        RoutePlannerRoute.RouteWagon wagon = new RoutePlannerRoute.RouteWagon(
            selected.getWagonId(), selected.getCargoType());
        
        ObservableList<RoutePlannerRoute.RouteWagon> selectedList = selectedWagonsList.getItems();
        // Check if already added (by wagon ID)
        boolean alreadyAdded = selectedList.stream()
            .anyMatch(w -> w.getWagonId() == selected.getWagonId());
        
        if (!alreadyAdded) {
            selectedList.add(wagon);
            updateAssignButtonState();
        } else {
            showError("Error", "Wagon already selected.");
        }
    }
    
    /**
     * Removes selected wagon from the selected list.
     */
    private void removeSelectedWagon() {
        RoutePlannerRoute.RouteWagon selected = selectedWagonsList.getSelectionModel().getSelectedItem();
        if (selected != null) {
            selectedWagonsList.getItems().remove(selected);
            updateAssignButtonState();
        }
    }
    
    /**
     * Updates the assign button state based on selections.
     */
    private void updateAssignButtonState() {
        boolean hasRoute = routeComboBox.getSelectionModel().getSelectedItem() != null;
        boolean hasLocomotives = !selectedLocomotivesList.getItems().isEmpty();
        
        assignButton.setDisable(!hasRoute || !hasLocomotives);
        removeLocomotiveButton.setDisable(selectedLocomotivesList.getSelectionModel().getSelectedItem() == null);
        removeWagonButton.setDisable(selectedWagonsList.getSelectionModel().getSelectedItem() == null);
    }
    
    /**
     * Creates a train and assigns it to the selected route.
     */
    private void assignTrainToRoute() {
        RoutePlannerRoute selectedRoute = routeComboBox.getSelectionModel().getSelectedItem();
        if (selectedRoute == null) {
            showError("Error", "Please select a route first.");
            return;
        }
        
        List<Locomotive> selectedLocomotives = new ArrayList<>(selectedLocomotivesList.getItems());
        
        if (selectedLocomotives.isEmpty()) {
            showError("Error", "At least one locomotive must be selected.");
            return;
        }
        
        // Get locomotive IDs
        List<Integer> locomotiveIds = selectedLocomotives.stream()
            .map(l -> Integer.parseInt(l.getId()))
            .collect(Collectors.toList());
        
        statusLabel.setText("Creating train and assigning to route...");
        progressIndicator.setVisible(true);
        assignButton.setDisable(true);
        
        Service<Integer> service = new Service<Integer>() {
            @Override
            protected Task<Integer> createTask() {
                return new Task<Integer>() {
                    @Override
                    protected Integer call() throws Exception {
                        String operatorId = "Medway"; // Default operator - could be made configurable
                        double maxLength = 500.0; // Default max length - could be made configurable
                        
                        return controller.createTrainAndAssignToRoute(
                            operatorId, maxLength, locomotiveIds, selectedRoute);
                    }
                };
            }
        };
        
        service.setOnSucceeded(e -> {
            int trainId = service.getValue();
            progressIndicator.setVisible(false);
            assignButton.setDisable(false);
            
            if (trainId > 0) {
                statusLabel.setText("Train created and assigned successfully!");
                showSuccess("Success", 
                    String.format("Train ID %d has been created and assigned to route '%s' successfully.", 
                                trainId, selectedRoute.getRouteName()));
                
                // Clear selections
                selectedLocomotivesList.getItems().clear();
                selectedWagonsList.getItems().clear();
                routeComboBox.getSelectionModel().clearSelection();
                updateAssignButtonState();
                
                // Reload routes (the assigned route should no longer appear)
                loadRoutes();
            } else {
                statusLabel.setText("Failed to create train");
                showError("Error", "Failed to create train. Please try again.");
            }
        });
        
        service.setOnFailed(e -> {
            Platform.runLater(() -> {
                progressIndicator.setVisible(false);
                assignButton.setDisable(false);
                statusLabel.setText("Error occurred");
                showError("Error", "Failed to create train: " + service.getException().getMessage());
            });
        });
        
        service.start();
    }
    
    /**
     * Shows an error dialog.
     */
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Shows a success dialog.
     */
    private void showSuccess(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

