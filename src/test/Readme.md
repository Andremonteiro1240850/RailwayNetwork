# Test Package

This package provides comprehensive test coverage for the Railway Terminal Management System using **JUnit 5**.

---

## Features

- **JUnit 5**: Modern testing framework
- **Comprehensive Coverage**: Unit and integration tests
- **Real Data Testing**: Tests use actual CSV datasets
- **Clear Test Reports**: Maven Surefire generates detailed reports
- **Test Organization**: Tests mirror source code structure

---

## Running Tests

### Run All Tests

```bash
mvn test
```

### Run Specific Test Class

```bash
mvn test -Dtest=UpgradePlanControllerTest
```

### Run Tests with Coverage

```bash
mvn test jacoco:report
```

Coverage reports are generated in `target/site/jacoco/`

---

## Test Structure

Tests are organized to mirror the source code structure:

```
src/test/java/
├── Controller/
│   ├── UpgradePlanControllerTest.java
│   ├── ShortestPathControllerTest.java
│   └── HubControllerTest.java
├── Domain/
│   ├── RailwayGraphTest.java
│   ├── TopologicalSortTest.java
│   ├── CycleDetectionTest.java
│   ├── BellmanFordTest.java
│   └── GraphSimpleTest.java
└── ...
```

---

## Test Coverage

### High Coverage Classes

- **RailwayGraph**: 100% method and line coverage
- **UpgradePlanController**: Comprehensive test suite
- **TopologicalSort**: Full algorithm coverage
- **CycleDetection**: Complete cycle detection tests
- **BellmanFord**: Risk-aware shortest path tests

### Test Types

1. **Unit Tests**: Test individual methods and classes
2. **Integration Tests**: Test component interactions
3. **Algorithm Tests**: Verify algorithm correctness
4. **Edge Case Tests**: Test boundary conditions
5. **Error Handling Tests**: Test exception scenarios

---

## Test Data

Tests use real data from CSV files:
- `train_station_dataset/Sprint3/stations.csv`
- `train_station_dataset/Sprint3/lines.csv`

This ensures tests reflect real-world scenarios and data structures.

---

## Test Best Practices

1. **Test Isolation**: Each test is independent
2. **Real Data**: Use actual datasets when possible
3. **Clear Names**: Test method names describe what they test
4. **Comprehensive Coverage**: Test all branches and edge cases
5. **Assertions**: Use meaningful assertion messages

---

## Example Test

```java
@Test
void calculateUpgradePlan_WithRealData_ReturnsResult() {
    controller.buildGraph();
    UpgradePlanController.UpgradePlanResult result = 
        controller.calculateUpgradePlan();
    
    assertNotNull(result);
    assertTrue(result.hasTopologicalOrder() || result.hasCycles());
}
```

---

## Test Reports

After running tests, reports are available in:
- `target/surefire-reports/` - Test execution reports
- `target/site/jacoco/` - Code coverage reports (if Jacoco is configured)

---

## Related Documentation

- [Main README](../../README.md)
- [Java Source Code](../main/java/Readme.md)
- [Maven Configuration](../../pom.xml)

---

## Notes

- All test classes use JUnit 5 annotations (`@Test`, `@BeforeEach`, `@BeforeAll`)
- Tests are designed to be fast and reliable
- Real data loading is handled in `@BeforeAll` methods for efficiency
