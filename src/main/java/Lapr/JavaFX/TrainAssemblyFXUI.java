package Lapr.JavaFX;

import Lapr.Controller.TrainAssemblyController;
import Lapr.DataBaseAssociation.VehicleAOD;
import Lapr.Domain.Route;
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
    private ComboBox<Route> routeComboBox;
    private Label routeInfoLabel;
    
    // Locomotives
    private ListView<VehicleAOD.VehicleInfo> locomotivesList;
    private Label locomotivesTitleLabel;
    private ObservableList<VehicleAOD.VehicleInfo> locomotivesData;
    
    // Wagons
    private ListView<VehicleAOD.VehicleInfo> wagonsList;
    private Label wagonsTitleLabel;
    private ObservableList<VehicleAOD.VehicleInfo> wagonsData;
    
    // Selected items
    private ListView<String> selectedLocomotivesList;
    private ListView<String> selectedWagonsList;
    
    // Buttons
    private Button addLocomotiveButton;
    private Button removeLocomotiveButton;
    private Button addWagonButton;
    private Button removeWagonButton;
    private Button assignButton;
    private Button closeButton;
    
    // Status
    private Label statusLabel;
    private ProgressIndicator progressIndicator;
    
    // Data
    private List<Route> availableRoutes;
    private String currentRouteStartStationId;
    
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
            "Select a route and choose locomotives and wagons to assemble a train. " +
            "Vehicles are distinguished by status: In Transit (shows final destination) or Parked (shows station)."
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
        
        Label routeLabel = new Label("Select Route:");
        routeLabel.setStyle("-fx-font-family: 'Segoe UI', 'Roboto', sans-serif; " +
                           "-fx-font-size: 14px; " +
                           "-fx-font-weight: 600; " +
                           "-fx-text-fill: #E2E8F0;");
        
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
        
        // Set converter to display route text in the combo box button
        routeComboBox.setConverter(new javafx.util.StringConverter<Route>() {
            @Override
            public String toString(Route route) {
                if (route == null) {
                    return "Select a route...";
                }
                return String.format("Train ID: %d | Facility ID: %d | Order: %d", 
                                   route.getTrainID(), 
                                   route.getFacilityID(), 
                                   route.getOrder());
            }
            
            @Override
            public Route fromString(String string) {
                return null; // Not needed for display-only combo box
            }
        });
        
        routeInfoLabel = new Label("No route selected");
        routeInfoLabel.setStyle("-fx-font-family: 'Segoe UI', 'Roboto', sans-serif; " +
                                "-fx-font-size: 12px; " +
                                "-fx-text-fill: #94A3B8;");
        routeInfoLabel.setWrapText(true);
        
        routeBox.getChildren().addAll(routeLabel, routeComboBox, routeInfoLabel);
        
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
        
        // Custom cell factory to show status
        locomotivesList.setCellFactory(lv -> new ListCell<VehicleAOD.VehicleInfo>() {
            @Override
            protected void updateItem(VehicleAOD.VehicleInfo item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    String statusText;
                    if (item.getStatus().equals("IN_TRANSIT")) {
                        statusText = String.format("ID: %s | IN TRANSIT → Destination: %s", 
                                                   item.getVehicleId(), 
                                                   item.getFinalDestination() != null ? item.getFinalDestination() : "N/A");
                        setStyle("-fx-text-fill: #F59E0B;"); // Orange for in transit
                    } else {
                        statusText = String.format("ID: %s | PARKED at Station: %s (Distance: %.2f km)", 
                                                   item.getVehicleId(), 
                                                   item.getParkedStation() != null ? item.getParkedStation() : "N/A",
                                                   item.getDistanceFromStart());
                        setStyle("-fx-text-fill: #10B981;"); // Green for parked
                    }
                    setText(statusText);
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
        
        // Custom cell factory to show status
        wagonsList.setCellFactory(lv -> new ListCell<VehicleAOD.VehicleInfo>() {
            @Override
            protected void updateItem(VehicleAOD.VehicleInfo item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    String statusText;
                    if (item.getStatus().equals("IN_TRANSIT")) {
                        statusText = String.format("ID: %s | IN TRANSIT → Destination: %s", 
                                                   item.getVehicleId(), 
                                                   item.getFinalDestination() != null ? item.getFinalDestination() : "N/A");
                        setStyle("-fx-text-fill: #F59E0B;"); // Orange for in transit
                    } else {
                        statusText = String.format("ID: %s | PARKED at Station: %s (Distance: %.2f km)", 
                                                   item.getVehicleId(), 
                                                   item.getParkedStation() != null ? item.getParkedStation() : "N/A",
                                                   item.getDistanceFromStart());
                        setStyle("-fx-text-fill: #10B981;"); // Green for parked
                    }
                    setText(statusText);
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
     * Loads routes and initializes the UI.
     */
    private void loadData() {
        statusLabel.setText("Loading routes...");
        progressIndicator.setVisible(true);
        
        Service<Void> service = new Service<Void>() {
            @Override
            protected Task<Void> createTask() {
                return new Task<Void>() {
                    @Override
                    protected Void call() throws Exception {
                        Platform.runLater(() -> statusLabel.setText("Loading routes from database..."));
                        
                        try {
                            availableRoutes = controller.getAvailableRoutes();
                            
                            Platform.runLater(() -> {
                                if (availableRoutes != null && !availableRoutes.isEmpty()) {
                                    System.out.println("Found " + availableRoutes.size() + " routes in database");
                                    routeComboBox.setItems(FXCollections.observableArrayList(availableRoutes));
                                    routeComboBox.setCellFactory(lv -> new ListCell<Route>() {
                                        @Override
                                        protected void updateItem(Route item, boolean empty) {
                                            super.updateItem(item, empty);
                                            if (empty || item == null) {
                                                setText(null);
                                                setStyle("");
                                            } else {
                                                setText(String.format("Train ID: %d | Facility ID: %d | Order: %d", 
                                                                     item.getTrainID(), 
                                                                     item.getFacilityID(), 
                                                                     item.getOrder()));
                                                setStyle("-fx-text-fill: #E2E8F0;");
                                            }
                                        }
                                    });
                                    statusLabel.setText("Ready - Select a route to begin (" + availableRoutes.size() + " routes available)");
                                } else {
                                    System.out.println("No routes found in database");
                                    statusLabel.setText("No routes available - Please create routes first");
                                    showError("No Routes", "No routes are available in the database. Please create routes first using USLP08.");
                                }
                                
                                progressIndicator.setVisible(false);
                            });
                        } catch (Exception e) {
                            Platform.runLater(() -> {
                                System.err.println("Error loading routes: " + e.getMessage());
                                e.printStackTrace();
                                statusLabel.setText("Error loading routes");
                                progressIndicator.setVisible(false);
                                showError("Error", "Failed to load routes from database: " + e.getMessage());
                            });
                        }
                        
                        return null;
                    }
                };
            }
        };
        
        service.setOnFailed(e -> {
            Platform.runLater(() -> {
                statusLabel.setText("Error loading routes: " + service.getException().getMessage());
                progressIndicator.setVisible(false);
                showError("Error", "Failed to load routes: " + service.getException().getMessage());
            });
        });
        
        service.start();
    }
    
    /**
     * Handles route selection.
     */
    private void onRouteSelected() {
        Route selectedRoute = routeComboBox.getSelectionModel().getSelectedItem();
        if (selectedRoute == null) {
            return;
        }
        
        currentRouteStartStationId = String.valueOf(selectedRoute.getFacilityID());
        routeInfoLabel.setText(String.format("Route: Train ID %d, Starting at Facility %d", 
                                            selectedRoute.getTrainID(), 
                                            selectedRoute.getFacilityID()));
        
        // Load vehicles for this route
        loadVehiclesForRoute(currentRouteStartStationId);
    }
    
    /**
     * Loads locomotives and wagons for the selected route.
     */
    private void loadVehiclesForRoute(String routeStartStationId) {
        statusLabel.setText("Loading vehicles...");
        progressIndicator.setVisible(true);
        addLocomotiveButton.setDisable(true);
        addWagonButton.setDisable(true);
        
        Service<Void> service = new Service<Void>() {
            @Override
            protected Task<Void> createTask() {
                return new Task<Void>() {
                    @Override
                    protected Void call() throws Exception {
                        Platform.runLater(() -> statusLabel.setText("Loading vehicles from database..."));
                        
                        List<VehicleAOD.VehicleInfo> locomotives = controller.getLocomotivesForRoute(routeStartStationId);
                        List<VehicleAOD.VehicleInfo> wagons = controller.getWagonsForRoute(routeStartStationId);
                        
                        Platform.runLater(() -> {
                            locomotivesData.clear();
                            locomotivesData.addAll(locomotives);
                            locomotivesTitleLabel.setText(String.format("Locomotives (%d available)", locomotives.size()));
                            
                            wagonsData.clear();
                            wagonsData.addAll(wagons);
                            wagonsTitleLabel.setText(String.format("Wagons (%d available)", wagons.size()));
                            
                            addLocomotiveButton.setDisable(false);
                            addWagonButton.setDisable(false);
                            
                            progressIndicator.setVisible(false);
                            statusLabel.setText("Ready - Select vehicles to assemble train");
                        });
                        
                        return null;
                    }
                };
            }
        };
        
        service.setOnFailed(e -> {
            Platform.runLater(() -> {
                statusLabel.setText("Error loading vehicles: " + service.getException().getMessage());
                progressIndicator.setVisible(false);
                showError("Error", "Failed to load vehicles: " + service.getException().getMessage());
            });
        });
        
        service.start();
    }
    
    /**
     * Adds selected locomotive to the selected list.
     */
    private void addSelectedLocomotive() {
        VehicleAOD.VehicleInfo selected = locomotivesList.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }
        
        ObservableList<String> selectedList = selectedLocomotivesList.getItems();
        String vehicleId = selected.getVehicleId();
        
        if (!selectedList.contains(vehicleId)) {
            selectedList.add(vehicleId);
            updateAssignButtonState();
        }
    }
    
    /**
     * Removes selected locomotive from the selected list.
     */
    private void removeSelectedLocomotive() {
        String selected = selectedLocomotivesList.getSelectionModel().getSelectedItem();
        if (selected != null) {
            selectedLocomotivesList.getItems().remove(selected);
            updateAssignButtonState();
        }
    }
    
    /**
     * Adds selected wagon to the selected list.
     */
    private void addSelectedWagon() {
        VehicleAOD.VehicleInfo selected = wagonsList.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }
        
        ObservableList<String> selectedList = selectedWagonsList.getItems();
        String vehicleId = selected.getVehicleId();
        
        if (!selectedList.contains(vehicleId)) {
            selectedList.add(vehicleId);
            updateAssignButtonState();
        }
    }
    
    /**
     * Removes selected wagon from the selected list.
     */
    private void removeSelectedWagon() {
        String selected = selectedWagonsList.getSelectionModel().getSelectedItem();
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
     * Assigns the train to the selected route.
     */
    private void assignTrainToRoute() {
        Route selectedRoute = routeComboBox.getSelectionModel().getSelectedItem();
        if (selectedRoute == null) {
            showError("Error", "Please select a route first.");
            return;
        }
        
        List<String> selectedLocomotives = new ArrayList<>(selectedLocomotivesList.getItems());
        List<String> selectedWagons = new ArrayList<>(selectedWagonsList.getItems());
        
        if (selectedLocomotives.isEmpty()) {
            showError("Error", "At least one locomotive must be selected.");
            return;
        }
        
        statusLabel.setText("Assigning train to route...");
        progressIndicator.setVisible(true);
        assignButton.setDisable(true);
        
        Service<Boolean> service = new Service<Boolean>() {
            @Override
            protected Task<Boolean> createTask() {
                return new Task<Boolean>() {
                    @Override
                    protected Boolean call() throws Exception {
                        return controller.assignTrainToRoute(
                            String.valueOf(selectedRoute.getTrainID()),
                            selectedLocomotives,
                            selectedWagons
                        );
                    }
                };
            }
        };
        
        service.setOnSucceeded(e -> {
            boolean success = service.getValue();
            progressIndicator.setVisible(false);
            assignButton.setDisable(false);
            
            if (success) {
                statusLabel.setText("Train assigned successfully!");
                showSuccess("Success", "Train has been assigned to the route successfully.");
                
                // Clear selections
                selectedLocomotivesList.getItems().clear();
                selectedWagonsList.getItems().clear();
                updateAssignButtonState();
            } else {
                statusLabel.setText("Failed to assign train");
                showError("Error", "Failed to assign train to route. Please try again.");
            }
        });
        
        service.setOnFailed(e -> {
            Platform.runLater(() -> {
                progressIndicator.setVisible(false);
                assignButton.setDisable(false);
                statusLabel.setText("Error occurred");
                showError("Error", "Failed to assign train: " + service.getException().getMessage());
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

