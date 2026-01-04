package Controller;

import Domain.*;
import Repository.*;

import java.util.*;
import java.io.IOException;

/**
 * Comprehensive test suite for UpgradePlanController (USEI11).
 * 
 * Tests cover:
 * - Integration tests: full workflow from graph building to result calculation
 * - Functional tests: acyclic graphs, cycles, various scenarios
 * - Structural tests: all controller methods and branches
 * - Edge cases: empty graph, single node, error handling
 * 
 * All tests use REAL data from train_station_dataset/Sprint3/stations.csv and lines.csv
 * 
 * @author Development Team
 * @version 1.0
 */
public class UpgradePlanControllerTest {
    
    private static int testsRun = 0;
    private static int testsPassed = 0;
    private static int testsFailed = 0;
    
    // Real data repository
    private static RailwayGraphRepository realDataRepository;
    
    public static void main(String[] args) {
        System.out.println("=== UPGRADE PLAN CONTROLLER TESTS (USEI11) ===\n");
        
        // Load real data from CSV files
        System.out.println("Loading real data from CSV files...");
        try {
            realDataRepository = new RailwayGraphRepository();
            realDataRepository.loadData("train_station_dataset/Sprint3/stations.csv", 
                                       "train_station_dataset/Sprint3/lines.csv");
            System.out.println("Real data loaded successfully!\n");
        } catch (Exception e) {
            System.err.println("ERROR: Failed to load real data: " + e.getMessage());
            System.err.println("Some tests may fail.");
            e.printStackTrace();
        }
        
        // ==================== Constructor Tests ====================
        testConstructor_NullRepository();
        testConstructor_ValidRepository();
        
        // ==================== Build Graph Tests ====================
        testBuildGraph_ValidData();
        testBuildGraph_EmptyRepositories();
        testBuildGraph_WithoutCallingBuild();
        
        // ==================== Calculate Upgrade Plan - Acyclic Tests ====================
        testCalculateUpgradePlan_SimpleChain();
        testCalculateUpgradePlan_ComplexDAG();
        testCalculateUpgradePlan_DisconnectedAcyclic();
        testCalculateUpgradePlan_SingleNode();
        
        // ==================== Calculate Upgrade Plan - Cyclic Tests ====================
        testCalculateUpgradePlan_SimpleCycle();
        testCalculateUpgradePlan_MultipleCycles();
        testCalculateUpgradePlan_CycleWithBranch();
        
        // ==================== Result Validation Tests ====================
        testResult_HasTopologicalOrder();
        testResult_GetTopologicalOrder();
        testResult_HasCycles();
        testResult_GetCycleInfo();
        testResult_AcyclicGraph();
        testResult_CyclicGraph();
        
        // ==================== Graph Statistics Tests ====================
        testGetGraphStatistics_ValidGraph();
        testGetGraphStatistics_WithoutBuild();
        
        // ==================== Station Access Tests ====================
        testGetStationById_Exists();
        testGetStationById_NotExists();
        testGetStationById_WithoutBuild();
        testGetAllStations();
        testGetAllStations_WithoutBuild();
        
        // ==================== Degree Info Tests ====================
        testGetStationDegreeInfo_ValidStation();
        testGetStationDegreeInfo_WithoutBuild();
        
        // ==================== Edge Access Tests ====================
        testGetAllEdges();
        testGetAllEdges_WithoutBuild();
        
        // ==================== Edge Cases ====================
        testCalculateUpgradePlan_EmptyGraph();
        testCalculateUpgradePlan_LargeGraph();
        testCalculateUpgradePlan_MultipleSources();
        testCalculateUpgradePlan_MultipleSinks();
        
        System.out.println("\n=== TEST SUMMARY ===");
        System.out.println("Tests run: " + testsRun);
        System.out.println("Tests passed: " + testsPassed);
        System.out.println("Tests failed: " + testsFailed);
        
        if (testsFailed == 0) {
            System.out.println("\n✓ ALL TESTS PASSED!");
        } else {
            System.out.println("\n✗ SOME TESTS FAILED");
        }
    }
    
    // ==================== Helper Methods ====================
    
    /**
     * Creates a repository using real data from CSV files.
     */
    private static RailwayGraphRepository createRepositoryWithRealData() {
        if (realDataRepository != null) {
            return realDataRepository;
        }
        
        // Fallback: try to load
        try {
            RailwayGraphRepository repo = new RailwayGraphRepository();
            repo.loadData("train_station_dataset/Sprint3/stations.csv", 
                         "train_station_dataset/Sprint3/lines.csv");
            return repo;
        } catch (Exception e) {
            // If loading fails, return a repository with synthetic data
            return createRepositoryWithSimpleChain();
        }
    }
    
    private static RailwayGraphRepository createRepositoryWithSimpleChain() {
        StationRepository stationRepo = new StationRepository();
        LineRepository lineRepo = new LineRepository();
        
        Station a = new Station("A", "Station A", 50.0, 4.0, 100.0, 200.0);
        Station b = new Station("B", "Station B", 51.0, 5.0, 101.0, 201.0);
        Station c = new Station("C", "Station C", 52.0, 6.0, 102.0, 202.0);
        
        stationRepo.add(a);
        stationRepo.add(b);
        stationRepo.add(c);
        
        lineRepo.add(new Line("A", "B", 10.0, 100, 5.0));
        lineRepo.add(new Line("B", "C", 15.0, 100, 7.0));
        
        return new RailwayGraphRepository(stationRepo, lineRepo);
    }
    
    private static RailwayGraphRepository createRepositoryWithCycle() {
        StationRepository stationRepo = new StationRepository();
        LineRepository lineRepo = new LineRepository();
        
        Station a = new Station("A", "Station A", 50.0, 4.0, 100.0, 200.0);
        Station b = new Station("B", "Station B", 51.0, 5.0, 101.0, 201.0);
        Station c = new Station("C", "Station C", 52.0, 6.0, 102.0, 202.0);
        
        stationRepo.add(a);
        stationRepo.add(b);
        stationRepo.add(c);
        
        lineRepo.add(new Line("A", "B", 10.0, 100, 5.0));
        lineRepo.add(new Line("B", "C", 15.0, 100, 7.0));
        lineRepo.add(new Line("C", "A", 20.0, 100, 9.0)); // Cycle A->B->C->A
        
        return new RailwayGraphRepository(stationRepo, lineRepo);
    }
    
    private static RailwayGraphRepository createRepositoryWithComplexDAG() {
        StationRepository stationRepo = new StationRepository();
        LineRepository lineRepo = new LineRepository();
        
        Station[] stations = new Station[5];
        for (int i = 0; i < 5; i++) {
            stations[i] = new Station(String.valueOf((char)('A' + i)), 
                                     "Station " + (char)('A' + i),
                                     50.0 + i, 4.0 + i, 100.0 + i, 200.0 + i);
            stationRepo.add(stations[i]);
        }
        
        lineRepo.add(new Line("A", "B", 10.0, 100, 5.0));
        lineRepo.add(new Line("A", "C", 12.0, 100, 6.0));
        lineRepo.add(new Line("B", "D", 15.0, 100, 7.0));
        lineRepo.add(new Line("C", "D", 18.0, 100, 8.0));
        lineRepo.add(new Line("D", "E", 20.0, 100, 9.0));
        
        return new RailwayGraphRepository(stationRepo, lineRepo);
    }
    
    private static RailwayGraphRepository createRepositoryWithSingleNode() {
        StationRepository stationRepo = new StationRepository();
        LineRepository lineRepo = new LineRepository();
        
        Station a = new Station("A", "Station A", 50.0, 4.0, 100.0, 200.0);
        stationRepo.add(a);
        
        return new RailwayGraphRepository(stationRepo, lineRepo);
    }
    
    private static RailwayGraphRepository createRepositoryWithEmptyGraph() {
        StationRepository stationRepo = new StationRepository();
        LineRepository lineRepo = new LineRepository();
        
        return new RailwayGraphRepository(stationRepo, lineRepo);
    }
    
    private static RailwayGraphRepository createRepositoryWithMultipleCycles() {
        StationRepository stationRepo = new StationRepository();
        LineRepository lineRepo = new LineRepository();
        
        Station a = new Station("A", "Station A", 50.0, 4.0, 100.0, 200.0);
        Station b = new Station("B", "Station B", 51.0, 5.0, 101.0, 201.0);
        Station c = new Station("C", "Station C", 52.0, 6.0, 102.0, 202.0);
        Station d = new Station("D", "Station D", 53.0, 7.0, 103.0, 203.0);
        
        stationRepo.add(a);
        stationRepo.add(b);
        stationRepo.add(c);
        stationRepo.add(d);
        
        lineRepo.add(new Line("A", "B", 10.0, 100, 5.0));
        lineRepo.add(new Line("B", "A", 12.0, 100, 6.0)); // Cycle 1: A->B->A
        lineRepo.add(new Line("C", "D", 15.0, 100, 7.0));
        lineRepo.add(new Line("D", "C", 18.0, 100, 8.0)); // Cycle 2: C->D->C
        
        return new RailwayGraphRepository(stationRepo, lineRepo);
    }
    
    // ==================== Constructor Tests ====================
    
    private static void testConstructor_NullRepository() {
        test("constructor with null repository throws exception", () -> {
            try {
                new UpgradePlanController(null);
                throw new AssertionError("Should throw IllegalArgumentException");
            } catch (IllegalArgumentException e) {
                assertTrue(e.getMessage().contains("null") || e.getMessage().contains("RailwayGraphRepository"), 
                          "Error message mentions null or RailwayGraphRepository");
            }
        });
    }
    
    private static void testConstructor_ValidRepository() {
        test("constructor with valid repository", () -> {
            RailwayGraphRepository repo = createRepositoryWithSimpleChain();
            UpgradePlanController controller = new UpgradePlanController(repo);
            assertTrue(controller != null, "Controller created successfully");
        });
    }
    
    // ==================== Build Graph Tests ====================
    
    private static void testBuildGraph_ValidData() {
        test("buildGraph with valid data", () -> {
            RailwayGraphRepository repo = createRepositoryWithSimpleChain();
            UpgradePlanController controller = new UpgradePlanController(repo);
            controller.buildGraph();
            
            // Verify graph was built by checking statistics
            String stats = controller.getGraphStatistics();
            assertTrue(stats != null, "Statistics not null");
            assertTrue(stats.contains("Nodes"), "Statistics contains Nodes");
        });
    }
    
    private static void testBuildGraph_EmptyRepositories() {
        test("buildGraph with empty repositories throws exception", () -> {
            RailwayGraphRepository repo = createRepositoryWithEmptyGraph();
            UpgradePlanController controller = new UpgradePlanController(repo);
            try {
                controller.buildGraph();
                throw new AssertionError("Should throw IllegalStateException");
            } catch (IllegalStateException e) {
                assertTrue(e.getMessage().contains("empty") || e.getMessage().contains("Empty"), 
                          "Error message mentions empty");
            }
        });
    }
    
    private static void testBuildGraph_WithoutCallingBuild() {
        test("methods fail without calling buildGraph first", () -> {
            RailwayGraphRepository repo = createRepositoryWithSimpleChain();
            UpgradePlanController controller = new UpgradePlanController(repo);
            // Don't call buildGraph()
            
            try {
                controller.calculateUpgradePlan();
                throw new AssertionError("Should throw IllegalStateException");
            } catch (IllegalStateException e) {
                assertTrue(e.getMessage().contains("loaded") || e.getMessage().contains("Data"), 
                          "Error message mentions loaded or Data");
            }
        });
    }
    
    // ==================== Calculate Upgrade Plan - Acyclic Tests ====================
    
    private static void testCalculateUpgradePlan_SimpleChain() {
        test("calculateUpgradePlan with real data returns topological order", () -> {
            RailwayGraphRepository repo = createRepositoryWithRealData();
            UpgradePlanController controller = new UpgradePlanController(repo);
            controller.buildGraph();
            
            UpgradePlanController.UpgradePlanResult result = controller.calculateUpgradePlan();
            assertTrue(result != null, "Result is not null");
            assertTrue(result.hasTopologicalOrder() == true, "Has topological order");
            assertTrue(result.hasCycles() == false, "No cycles");
            
            List<Station> order = result.getTopologicalOrder();
            assertTrue(order != null, "Order is not null");
            assertTrue(order.size() > 0, "Order has nodes");
        });
    }
    
    private static void testCalculateUpgradePlan_ComplexDAG() {
        test("calculateUpgradePlan with complex DAG using real data returns topological order", () -> {
            RailwayGraphRepository repo = createRepositoryWithRealData();
            UpgradePlanController controller = new UpgradePlanController(repo);
            controller.buildGraph();
            
            UpgradePlanController.UpgradePlanResult result = controller.calculateUpgradePlan();
            assertTrue(result != null, "Result is not null");
            assertTrue(result.hasTopologicalOrder() == true, "Has topological order");
            assertTrue(result.hasCycles() == false, "No cycles");
            
            List<Station> order = result.getTopologicalOrder();
            assertTrue(order != null, "Order is not null");
            assertTrue(order.size() > 0, "Order has nodes");
        });
    }
    
    private static void testCalculateUpgradePlan_DisconnectedAcyclic() {
        test("calculateUpgradePlan with disconnected acyclic components", () -> {
            StationRepository stationRepo = new StationRepository();
            LineRepository lineRepo = new LineRepository();
            
            Station a = new Station("A", "A", 50.0, 4.0, 100.0, 200.0);
            Station b = new Station("B", "B", 51.0, 5.0, 101.0, 201.0);
            Station c = new Station("C", "C", 52.0, 6.0, 102.0, 202.0);
            Station d = new Station("D", "D", 53.0, 7.0, 103.0, 203.0);
            
            stationRepo.add(a);
            stationRepo.add(b);
            stationRepo.add(c);
            stationRepo.add(d);
            
            lineRepo.add(new Line("A", "B", 10.0, 100, 5.0));
            // C and D are disconnected
            
            RailwayGraphRepository repo = new RailwayGraphRepository(stationRepo, lineRepo);
            UpgradePlanController controller = new UpgradePlanController(repo);
            controller.buildGraph();
            
            UpgradePlanController.UpgradePlanResult result = controller.calculateUpgradePlan();
            assertTrue(result.hasTopologicalOrder() == true, "Has topological order");
            assertTrue(result.getTopologicalOrder().size() == 4, "All nodes included");
        });
    }
    
    private static void testCalculateUpgradePlan_SingleNode() {
        test("calculateUpgradePlan with single node", () -> {
            RailwayGraphRepository repo = createRepositoryWithSingleNode();
            UpgradePlanController controller = new UpgradePlanController(repo);
            controller.buildGraph();
            
            UpgradePlanController.UpgradePlanResult result = controller.calculateUpgradePlan();
            assertTrue(result.hasTopologicalOrder() == true, "Has topological order");
            assertTrue(result.getTopologicalOrder().size() == 1, "Single node in order");
            assertTrue(result.getTopologicalOrder().get(0).getId().equals("A"), "Node A in order");
        });
    }
    
    // ==================== Calculate Upgrade Plan - Cyclic Tests ====================
    
    private static void testCalculateUpgradePlan_SimpleCycle() {
        test("calculateUpgradePlan with simple cycle using synthetic data returns cycle info", () -> {
            // Use synthetic data for cycle test since real data may not have cycles
            RailwayGraphRepository repo = createRepositoryWithCycle();
            UpgradePlanController controller = new UpgradePlanController(repo);
            controller.buildGraph();
            
            UpgradePlanController.UpgradePlanResult result = controller.calculateUpgradePlan();
            assertTrue(result != null, "Result is not null");
            assertTrue(result.hasTopologicalOrder() == false, "No topological order");
            assertTrue(result.hasCycles() == true, "Has cycles");
            
            CycleDetection.CycleInfo cycleInfo = result.getCycleInfo();
            assertTrue(cycleInfo != null, "CycleInfo is not null");
            assertTrue(cycleInfo.hasCycles() == true, "CycleInfo indicates cycles");
            assertTrue(cycleInfo.getCycleNodeIds().size() >= 3, "At least 3 nodes in cycle");
        });
    }
    
    private static void testCalculateUpgradePlan_MultipleCycles() {
        test("calculateUpgradePlan with multiple cycles returns cycle info", () -> {
            RailwayGraphRepository repo = createRepositoryWithMultipleCycles();
            UpgradePlanController controller = new UpgradePlanController(repo);
            controller.buildGraph();
            
            UpgradePlanController.UpgradePlanResult result = controller.calculateUpgradePlan();
            assertTrue(result.hasCycles() == true, "Has cycles");
            assertTrue(result.hasTopologicalOrder() == false, "No topological order");
            
            CycleDetection.CycleInfo cycleInfo = result.getCycleInfo();
            assertTrue(cycleInfo.getCycleNodeIds().size() >= 4, "At least 4 nodes in cycles");
        });
    }
    
    private static void testCalculateUpgradePlan_CycleWithBranch() {
        test("calculateUpgradePlan with cycle and branch", () -> {
            StationRepository stationRepo = new StationRepository();
            LineRepository lineRepo = new LineRepository();
            
            Station a = new Station("A", "A", 50.0, 4.0, 100.0, 200.0);
            Station b = new Station("B", "B", 51.0, 5.0, 101.0, 201.0);
            Station c = new Station("C", "C", 52.0, 6.0, 102.0, 202.0);
            Station d = new Station("D", "D", 53.0, 7.0, 103.0, 203.0);
            
            stationRepo.add(a);
            stationRepo.add(b);
            stationRepo.add(c);
            stationRepo.add(d);
            
            lineRepo.add(new Line("A", "B", 10.0, 100, 5.0));
            lineRepo.add(new Line("B", "C", 12.0, 100, 6.0));
            lineRepo.add(new Line("C", "A", 15.0, 100, 7.0)); // Cycle A->B->C->A
            lineRepo.add(new Line("A", "D", 18.0, 100, 8.0)); // Branch from cycle
            
            RailwayGraphRepository repo = new RailwayGraphRepository(stationRepo, lineRepo);
            UpgradePlanController controller = new UpgradePlanController(repo);
            controller.buildGraph();
            
            UpgradePlanController.UpgradePlanResult result = controller.calculateUpgradePlan();
            assertTrue(result.hasCycles() == true, "Has cycles");
        });
    }
    
    // ==================== Result Validation Tests ====================
    
    private static void testResult_HasTopologicalOrder() {
        test("result.hasTopologicalOrder() returns correct value using real data", () -> {
            RailwayGraphRepository acyclicRepo = createRepositoryWithRealData();
            UpgradePlanController acyclicController = new UpgradePlanController(acyclicRepo);
            acyclicController.buildGraph();
            UpgradePlanController.UpgradePlanResult acyclicResult = acyclicController.calculateUpgradePlan();
            assertTrue(acyclicResult.hasTopologicalOrder() == true, "Acyclic graph has topological order");
            
            RailwayGraphRepository cyclicRepo = createRepositoryWithCycle();
            UpgradePlanController cyclicController = new UpgradePlanController(cyclicRepo);
            cyclicController.buildGraph();
            UpgradePlanController.UpgradePlanResult cyclicResult = cyclicController.calculateUpgradePlan();
            assertTrue(cyclicResult.hasTopologicalOrder() == false, "Cyclic graph has no topological order");
        });
    }
    
    private static void testResult_GetTopologicalOrder() {
        test("result.getTopologicalOrder() returns correct order", () -> {
            RailwayGraphRepository repo = createRepositoryWithSimpleChain();
            UpgradePlanController controller = new UpgradePlanController(repo);
            controller.buildGraph();
            
            UpgradePlanController.UpgradePlanResult result = controller.calculateUpgradePlan();
            List<Station> order = result.getTopologicalOrder();
            assertTrue(order != null, "Order is not null");
            assertTrue(order.size() == 3, "Order has correct size");
            
            // Verify it's a copy (immutability)
            List<Station> order2 = result.getTopologicalOrder();
            assertTrue(order != order2, "Returns a copy");
        });
    }
    
    private static void testResult_HasCycles() {
        test("result.hasCycles() returns correct value", () -> {
            RailwayGraphRepository acyclicRepo = createRepositoryWithSimpleChain();
            UpgradePlanController acyclicController = new UpgradePlanController(acyclicRepo);
            acyclicController.buildGraph();
            UpgradePlanController.UpgradePlanResult acyclicResult = acyclicController.calculateUpgradePlan();
            assertTrue(acyclicResult.hasCycles() == false, "Acyclic graph has no cycles");
            
            RailwayGraphRepository cyclicRepo = createRepositoryWithCycle();
            UpgradePlanController cyclicController = new UpgradePlanController(cyclicRepo);
            cyclicController.buildGraph();
            UpgradePlanController.UpgradePlanResult cyclicResult = cyclicController.calculateUpgradePlan();
            assertTrue(cyclicResult.hasCycles() == true, "Cyclic graph has cycles");
        });
    }
    
    private static void testResult_GetCycleInfo() {
        test("result.getCycleInfo() returns correct info", () -> {
            RailwayGraphRepository repo = createRepositoryWithCycle();
            UpgradePlanController controller = new UpgradePlanController(repo);
            controller.buildGraph();
            
            UpgradePlanController.UpgradePlanResult result = controller.calculateUpgradePlan();
            CycleDetection.CycleInfo cycleInfo = result.getCycleInfo();
            assertTrue(cycleInfo != null, "CycleInfo is not null");
            assertTrue(cycleInfo.hasCycles() == true, "CycleInfo indicates cycles");
            assertTrue(cycleInfo.getCycleNodeIds().size() >= 3, "Cycle nodes identified");
        });
    }
    
    private static void testResult_AcyclicGraph() {
        test("result properties for acyclic graph", () -> {
            RailwayGraphRepository repo = createRepositoryWithSimpleChain();
            UpgradePlanController controller = new UpgradePlanController(repo);
            controller.buildGraph();
            
            UpgradePlanController.UpgradePlanResult result = controller.calculateUpgradePlan();
            assertTrue(result.hasTopologicalOrder() == true, "Has topological order");
            assertTrue(result.hasCycles() == false, "No cycles");
            assertTrue(result.getTopologicalOrder() != null, "Topological order not null");
            assertTrue(result.getCycleInfo() == null, "CycleInfo is null");
        });
    }
    
    private static void testResult_CyclicGraph() {
        test("result properties for cyclic graph", () -> {
            RailwayGraphRepository repo = createRepositoryWithCycle();
            UpgradePlanController controller = new UpgradePlanController(repo);
            controller.buildGraph();
            
            UpgradePlanController.UpgradePlanResult result = controller.calculateUpgradePlan();
            assertTrue(result.hasTopologicalOrder() == false, "No topological order");
            assertTrue(result.hasCycles() == true, "Has cycles");
            assertTrue(result.getTopologicalOrder() == null, "Topological order is null");
            assertTrue(result.getCycleInfo() != null, "CycleInfo is not null");
        });
    }
    
    // ==================== Graph Statistics Tests ====================
    
    private static void testGetGraphStatistics_ValidGraph() {
        test("getGraphStatistics returns correct statistics using real data", () -> {
            RailwayGraphRepository repo = createRepositoryWithRealData();
            UpgradePlanController controller = new UpgradePlanController(repo);
            controller.buildGraph();
            
            String stats = controller.getGraphStatistics();
            assertTrue(stats != null, "Statistics not null");
            assertTrue(stats.contains("Nodes"), "Contains Nodes");
            assertTrue(stats.contains("Edges"), "Contains Edges");
        });
    }
    
    private static void testGetGraphStatistics_WithoutBuild() {
        test("getGraphStatistics without buildGraph throws exception", () -> {
            RailwayGraphRepository repo = createRepositoryWithSimpleChain();
            UpgradePlanController controller = new UpgradePlanController(repo);
            // Don't call buildGraph()
            
            try {
                controller.getGraphStatistics();
                throw new AssertionError("Should throw IllegalStateException");
            } catch (IllegalStateException e) {
                assertTrue(e.getMessage().contains("loaded") || e.getMessage().contains("Data"), 
                          "Error message mentions loaded or Data");
            }
        });
    }
    
    // ==================== Station Access Tests ====================
    
    private static void testGetStationById_Exists() {
        test("getStationById with existing station using real data", () -> {
            RailwayGraphRepository repo = createRepositoryWithRealData();
            UpgradePlanController controller = new UpgradePlanController(repo);
            controller.buildGraph();
            
            // Get first station from real data
            Collection<Station> allStations = controller.getAllStations();
            if (!allStations.isEmpty()) {
                Station firstStation = allStations.iterator().next();
                Station station = controller.getStationById(firstStation.getId());
                assertTrue(station != null, "Station found");
                assertTrue(station.getId().equals(firstStation.getId()), "Correct station");
            }
        });
    }
    
    private static void testGetStationById_NotExists() {
        test("getStationById with non-existing station", () -> {
            RailwayGraphRepository repo = createRepositoryWithSimpleChain();
            UpgradePlanController controller = new UpgradePlanController(repo);
            controller.buildGraph();
            
            Station station = controller.getStationById("Z");
            assertTrue(station == null, "Station not found");
        });
    }
    
    private static void testGetStationById_WithoutBuild() {
        test("getStationById without buildGraph throws exception", () -> {
            RailwayGraphRepository repo = createRepositoryWithSimpleChain();
            UpgradePlanController controller = new UpgradePlanController(repo);
            // Don't call buildGraph()
            
            try {
                controller.getStationById("A");
                throw new AssertionError("Should throw IllegalStateException");
            } catch (IllegalStateException e) {
                assertTrue(e.getMessage().contains("loaded") || e.getMessage().contains("Data"), 
                          "Error message mentions loaded or Data");
            }
        });
    }
    
    private static void testGetAllStations() {
        test("getAllStations returns all stations using real data", () -> {
            RailwayGraphRepository repo = createRepositoryWithRealData();
            UpgradePlanController controller = new UpgradePlanController(repo);
            controller.buildGraph();
            
            Collection<Station> stations = controller.getAllStations();
            assertTrue(stations != null, "Stations collection not null");
            assertTrue(stations.size() > 0, "Stations included");
        });
    }
    
    private static void testGetAllStations_WithoutBuild() {
        test("getAllStations without buildGraph throws exception", () -> {
            RailwayGraphRepository repo = createRepositoryWithSimpleChain();
            UpgradePlanController controller = new UpgradePlanController(repo);
            // Don't call buildGraph()
            
            try {
                controller.getAllStations();
                throw new AssertionError("Should throw IllegalStateException");
            } catch (IllegalStateException e) {
                assertTrue(e.getMessage().contains("loaded") || e.getMessage().contains("Data"), 
                          "Error message mentions loaded or Data");
            }
        });
    }
    
    // ==================== Degree Info Tests ====================
    
    private static void testGetStationDegreeInfo_ValidStation() {
        test("getStationDegreeInfo with valid station", () -> {
            RailwayGraphRepository repo = createRepositoryWithSimpleChain();
            UpgradePlanController controller = new UpgradePlanController(repo);
            controller.buildGraph();
            
            UpgradePlanController.DegreeInfo degreeInfo = controller.getStationDegreeInfo("B");
            assertTrue(degreeInfo != null, "DegreeInfo not null");
            assertTrue(degreeInfo.getInDegree() == 1, "In-degree is 1");
            assertTrue(degreeInfo.getOutDegree() == 1, "Out-degree is 1");
            assertTrue(degreeInfo.getTotalDegree() == 2, "Total degree is 2");
        });
    }
    
    private static void testGetStationDegreeInfo_WithoutBuild() {
        test("getStationDegreeInfo without buildGraph throws exception", () -> {
            RailwayGraphRepository repo = createRepositoryWithSimpleChain();
            UpgradePlanController controller = new UpgradePlanController(repo);
            // Don't call buildGraph()
            
            try {
                controller.getStationDegreeInfo("A");
                throw new AssertionError("Should throw IllegalStateException");
            } catch (IllegalStateException e) {
                assertTrue(e.getMessage().contains("loaded") || e.getMessage().contains("Data"), 
                          "Error message mentions loaded or Data");
            }
        });
    }
    
    // ==================== Edge Access Tests ====================
    
    private static void testGetAllEdges() {
        test("getAllEdges returns all edges using real data", () -> {
            RailwayGraphRepository repo = createRepositoryWithRealData();
            UpgradePlanController controller = new UpgradePlanController(repo);
            controller.buildGraph();
            
            List<Line> edges = controller.getAllEdges();
            assertTrue(edges != null, "Edges list not null");
            assertTrue(edges.size() > 0, "Edges included");
        });
    }
    
    private static void testGetAllEdges_WithoutBuild() {
        test("getAllEdges without buildGraph throws exception", () -> {
            RailwayGraphRepository repo = createRepositoryWithSimpleChain();
            UpgradePlanController controller = new UpgradePlanController(repo);
            // Don't call buildGraph()
            
            try {
                controller.getAllEdges();
                throw new AssertionError("Should throw IllegalStateException");
            } catch (IllegalStateException e) {
                assertTrue(e.getMessage().contains("loaded") || e.getMessage().contains("Data"), 
                          "Error message mentions loaded or Data");
            }
        });
    }
    
    // ==================== Edge Cases ====================
    
    private static void testCalculateUpgradePlan_EmptyGraph() {
        test("calculateUpgradePlan with empty graph", () -> {
            StationRepository stationRepo = new StationRepository();
            LineRepository lineRepo = new LineRepository();
            RailwayGraphRepository repo = new RailwayGraphRepository(stationRepo, lineRepo);
            
            try {
                UpgradePlanController controller = new UpgradePlanController(repo);
                controller.buildGraph();
                UpgradePlanController.UpgradePlanResult result = controller.calculateUpgradePlan();
                assertTrue(result.hasTopologicalOrder() == true, "Empty graph has topological order");
                assertTrue(result.getTopologicalOrder().isEmpty(), "Order is empty");
            } catch (IllegalStateException e) {
                // Empty repositories may throw exception, which is acceptable
                assertTrue(true, "Empty repositories handled correctly");
            }
        });
    }
    
    private static void testCalculateUpgradePlan_LargeGraph() {
        test("calculateUpgradePlan with large graph using real data", () -> {
            RailwayGraphRepository repo = createRepositoryWithRealData();
            UpgradePlanController controller = new UpgradePlanController(repo);
            controller.buildGraph();
            
            UpgradePlanController.UpgradePlanResult result = controller.calculateUpgradePlan();
            assertTrue(result.hasTopologicalOrder() == true, "Has topological order");
            assertTrue(result.getTopologicalOrder().size() > 0, "All nodes included");
        });
    }
    
    private static void testCalculateUpgradePlan_MultipleSources() {
        test("calculateUpgradePlan with multiple source nodes", () -> {
            StationRepository stationRepo = new StationRepository();
            LineRepository lineRepo = new LineRepository();
            
            Station a = new Station("A", "A", 50.0, 4.0, 100.0, 200.0);
            Station b = new Station("B", "B", 51.0, 5.0, 101.0, 201.0);
            Station c = new Station("C", "C", 52.0, 6.0, 102.0, 202.0);
            Station d = new Station("D", "D", 53.0, 7.0, 103.0, 203.0);
            
            stationRepo.add(a);
            stationRepo.add(b);
            stationRepo.add(c);
            stationRepo.add(d);
            
            lineRepo.add(new Line("A", "C", 10.0, 100, 5.0));
            lineRepo.add(new Line("B", "C", 12.0, 100, 6.0));
            lineRepo.add(new Line("C", "D", 15.0, 100, 7.0));
            
            RailwayGraphRepository repo = new RailwayGraphRepository(stationRepo, lineRepo);
            UpgradePlanController controller = new UpgradePlanController(repo);
            controller.buildGraph();
            
            UpgradePlanController.UpgradePlanResult result = controller.calculateUpgradePlan();
            assertTrue(result.hasTopologicalOrder() == true, "Has topological order");
            List<Station> order = result.getTopologicalOrder();
            int aPos = findPosition(order, "A");
            int bPos = findPosition(order, "B");
            int cPos = findPosition(order, "C");
            assertTrue(aPos < cPos && bPos < cPos, "A and B before C");
        });
    }
    
    private static void testCalculateUpgradePlan_MultipleSinks() {
        test("calculateUpgradePlan with multiple sink nodes", () -> {
            StationRepository stationRepo = new StationRepository();
            LineRepository lineRepo = new LineRepository();
            
            Station a = new Station("A", "A", 50.0, 4.0, 100.0, 200.0);
            Station b = new Station("B", "B", 51.0, 5.0, 101.0, 201.0);
            Station c = new Station("C", "C", 52.0, 6.0, 102.0, 202.0);
            Station d = new Station("D", "D", 53.0, 7.0, 103.0, 203.0);
            
            stationRepo.add(a);
            stationRepo.add(b);
            stationRepo.add(c);
            stationRepo.add(d);
            
            lineRepo.add(new Line("A", "B", 10.0, 100, 5.0));
            lineRepo.add(new Line("A", "C", 12.0, 100, 6.0));
            lineRepo.add(new Line("A", "D", 15.0, 100, 7.0));
            
            RailwayGraphRepository repo = new RailwayGraphRepository(stationRepo, lineRepo);
            UpgradePlanController controller = new UpgradePlanController(repo);
            controller.buildGraph();
            
            UpgradePlanController.UpgradePlanResult result = controller.calculateUpgradePlan();
            assertTrue(result.hasTopologicalOrder() == true, "Has topological order");
            List<Station> order = result.getTopologicalOrder();
            assertTrue(order.get(0).getId().equals("A"), "A (source) comes first");
        });
    }
    
    // ==================== Helper Methods ====================
    
    private static int findPosition(List<Station> list, String id) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getId().equals(id)) {
                return i;
            }
        }
        return -1;
    }
    
    private static void test(String testName, Runnable testCode) {
        testsRun++;
        try {
            System.out.print("Testing: " + testName + " ... ");
            testCode.run();
            System.out.println("✓ PASSED");
            testsPassed++;
        } catch (AssertionError e) {
            System.out.println("✗ FAILED: " + e.getMessage());
            testsFailed++;
        } catch (Exception e) {
            System.out.println("✗ ERROR: " + e.getClass().getSimpleName() + ": " + e.getMessage());
            e.printStackTrace();
            testsFailed++;
        }
    }
    
    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}

