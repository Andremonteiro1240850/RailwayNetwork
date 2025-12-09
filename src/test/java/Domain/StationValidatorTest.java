package Domain;

import Sprint1.Domain.Station;
import Sprint2.Domain.StationValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Station Validator Test Suite")
class StationValidatorTest {

    // ==================== TESTES DE ESTAÇÕES VÁLIDAS ====================

    @Test
    @DisplayName("Should return true when station is valid with all fields correct")
    void testValidStation_AllFieldsCorrect() {
        // Arrange
        Station station = new Station(
                "Lisboa Santa Apolónia",
                38.7167,
                -9.1333,
                "Portugal",
                "Europe/Lisbon",
                "Europe",
                false,
                true,
                false
        );

        // Act
        boolean result = StationValidator.isValidStation(station);

        // Assert
        assertTrue(result, "Station with all valid fields should be valid");
    }

    @Test
    @DisplayName("Should return true when station has boundary latitude values")
    void testValidStation_BoundaryLatitudeValues() {
        // Test North Pole
        Station northPole = new Station(
                "North Pole Station",
                90.0,
                0.0,
                "Arctic",
                "UTC",
                "UTC",
                false,
                false,
                false
        );

        assertTrue(StationValidator.isValidStation(northPole),
                "Station at North Pole (90°) should be valid");

        // Test South Pole
        Station southPole = new Station(
                "South Pole Station",
                -90.0,
                0.0,
                "Antarctica",
                "UTC",
                "UTC",
                false,
                false,
                false
        );

        assertTrue(StationValidator.isValidStation(southPole),
                "Station at South Pole (-90°) should be valid");
    }

    @Test
    @DisplayName("Should return true when station has boundary longitude values")
    void testValidStation_BoundaryLongitudeValues() {
        // Test International Date Line East
        Station dateLineEast = new Station(
                "Date Line East",
                0.0,
                180.0,
                "Pacific",
                "Pacific/Fiji",
                "Pacific",
                false,
                false,
                false
        );

        assertTrue(StationValidator.isValidStation(dateLineEast),
                "Station at 180° longitude should be valid");

        // Test International Date Line West
        Station dateLineWest = new Station(
                "Date Line West",
                0.0,
                -180.0,
                "Pacific",
                "Pacific/Samoa",
                "Pacific",
                false,
                false,
                false
        );

        assertTrue(StationValidator.isValidStation(dateLineWest),
                "Station at -180° longitude should be valid");
    }

    @Test
    @DisplayName("Should return true when station name has only whitespace around valid text")
    void testValidStation_NameWithWhitespace() {
        // Arrange
        Station station = new Station(
                "  Porto Campanhã  ",
                41.1496,
                -8.5850,
                "Portugal",
                "Europe/Lisbon",
                "Europe",
                false,
                true,
                false
        );

        // Act
        boolean result = StationValidator.isValidStation(station);

        // Assert
        assertTrue(result, "Station with whitespace around name should be valid");
    }

    @Test
    @DisplayName("Should return true for station at equator and prime meridian")
    void testValidStation_EquatorAndPrimeMeridian() {
        // Arrange
        Station station = new Station(
                "Null Island",
                0.0,
                0.0,
                "Atlantic Ocean",
                "UTC",
                "UTC",
                false,
                false,
                false
        );

        // Act
        boolean result = StationValidator.isValidStation(station);

        // Assert
        assertTrue(result, "Station at 0,0 coordinates should be valid");
    }

    @Test
    @DisplayName("Should return true for station with special characters in name")
    void testValidStation_SpecialCharactersInName() {
        // Arrange
        Station station = new Station(
                "São João-da-Madeira (Estação #1)",
                40.9000,
                -8.4900,
                "Portugal",
                "Europe/Lisbon",
                "Europe",
                true,
                false,
                false
        );

        // Act
        boolean result = StationValidator.isValidStation(station);

        // Assert
        assertTrue(result, "Station with special characters should be valid");
    }

    @Test
    @DisplayName("Should return true for airport station")
    void testValidStation_Airport() {
        // Arrange
        Station station = new Station(
                "Aeroporto de Lisboa",
                38.7813,
                -9.1359,
                "Portugal",
                "Europe/Lisbon",
                "Europe",
                false,
                false,
                true
        );

        // Act
        boolean result = StationValidator.isValidStation(station);

        // Assert
        assertTrue(result, "Airport station should be valid");
    }

    @Test
    @DisplayName("Should return true for city station that is main station")
    void testValidStation_CityMainStation() {
        // Arrange
        Station station = new Station(
                "Porto",
                41.1579,
                -8.6291,
                "Portugal",
                "Europe/Lisbon",
                "Europe",
                true,
                true,
                false
        );

        // Act
        boolean result = StationValidator.isValidStation(station);

        // Assert
        assertTrue(result, "City main station should be valid");
    }

    // ==================== TESTES DE ESTAÇÕES INVÁLIDAS - NULL ====================

    @Test
    @DisplayName("Should return false when station is null")
    void testInvalidStation_NullStation() {
        // Act
        boolean result = StationValidator.isValidStation(null);

        // Assert
        assertFalse(result, "Null station should be invalid");
    }

    @Test
    @DisplayName("Should return false when station name is null")
    void testInvalidStation_NullName() {
        // Arrange
        Station station = new Station(
                null,
                38.7167,
                -9.1333,
                "Portugal",
                "Europe/Lisbon",
                "Europe",
                false,
                true,
                false
        );

        // Act
        boolean result = StationValidator.isValidStation(station);

        // Assert
        assertFalse(result, "Station with null name should be invalid");
    }

    @Test
    @DisplayName("Should return false when country is null")
    void testInvalidStation_NullCountry() {
        // Arrange
        Station station = new Station(
                "Braga Station",
                41.5454,
                -8.4265,
                null,
                "Europe/Lisbon",
                "Europe",
                false,
                false,
                false
        );

        // Act
        boolean result = StationValidator.isValidStation(station);

        // Assert
        assertFalse(result, "Station with null country should be invalid");
    }

    @Test
    @DisplayName("Should return false when timezone is null")
    void testInvalidStation_NullTimeZone() {
        // Arrange
        Station station = new Station(
                "Coimbra Station",
                40.2033,
                -8.4103,
                "Portugal",
                null,
                "Europe",
                false,
                false,
                false
        );

        // Act
        boolean result = StationValidator.isValidStation(station);

        // Assert
        assertFalse(result, "Station with null timezone should be invalid");
    }

    // ==================== TESTES DE ESTAÇÕES INVÁLIDAS - EMPTY/BLANK ====================

    @Test
    @DisplayName("Should return false when station name is empty")
    void testInvalidStation_EmptyName() {
        // Arrange
        Station station = new Station(
                "",
                38.7167,
                -9.1333,
                "Portugal",
                "Europe/Lisbon",
                "Europe",
                false,
                true,
                false
        );

        // Act
        boolean result = StationValidator.isValidStation(station);

        // Assert
        assertFalse(result, "Station with empty name should be invalid");
    }

    @Test
    @DisplayName("Should return false when station name contains only whitespace")
    void testInvalidStation_BlankName() {
        // Arrange
        Station station = new Station(
                "   ",
                38.7167,
                -9.1333,
                "Portugal",
                "Europe/Lisbon",
                "Europe",
                false,
                true,
                false
        );

        // Act
        boolean result = StationValidator.isValidStation(station);

        // Assert
        assertFalse(result, "Station with blank name should be invalid");
    }

    @Test
    @DisplayName("Should return false when country is empty")
    void testInvalidStation_EmptyCountry() {
        // Arrange
        Station station = new Station(
                "Faro Station",
                37.0194,
                -7.9322,
                "",
                "Europe/Lisbon",
                "Europe",
                false,
                false,
                false
        );

        // Act
        boolean result = StationValidator.isValidStation(station);

        // Assert
        assertFalse(result, "Station with empty country should be invalid");
    }

    @Test
    @DisplayName("Should return false when country contains only whitespace")
    void testInvalidStation_BlankCountry() {
        // Arrange
        Station station = new Station(
                "Aveiro Station",
                40.6443,
                -8.6455,
                "  \t  ",
                "Europe/Lisbon",
                "Europe",
                false,
                false,
                false
        );

        // Act
        boolean result = StationValidator.isValidStation(station);

        // Assert
        assertFalse(result, "Station with blank country should be invalid");
    }

    @Test
    @DisplayName("Should return false when timezone is empty")
    void testInvalidStation_EmptyTimeZone() {
        // Arrange
        Station station = new Station(
                "Évora Station",
                38.5667,
                -7.9083,
                "Portugal",
                "",
                "Europe",
                false,
                false,
                false
        );

        // Act
        boolean result = StationValidator.isValidStation(station);

        // Assert
        assertFalse(result, "Station with empty timezone should be invalid");
    }

    @Test
    @DisplayName("Should return false when timezone contains only whitespace")
    void testInvalidStation_BlankTimeZone() {
        // Arrange
        Station station = new Station(
                "Setúbal Station",
                38.5244,
                -8.8882,
                "Portugal",
                "  \n  ",
                "Europe",
                false,
                false,
                false
        );

        // Act
        boolean result = StationValidator.isValidStation(station);

        // Assert
        assertFalse(result, "Station with blank timezone should be invalid");
    }

    // ==================== TESTES DE COORDENADAS INVÁLIDAS ====================

    @Test
    @DisplayName("Should return false when latitude is below minimum (-90)")
    void testInvalidStation_LatitudeBelowMinimum() {
        // Arrange
        Station station = new Station(
                "Invalid South Station",
                -90.1,
                0.0,
                "Antarctica",
                "UTC",
                "UTC",
                false,
                false,
                false
        );

        // Act
        boolean result = StationValidator.isValidStation(station);

        // Assert
        assertFalse(result, "Station with latitude < -90 should be invalid");
    }

    @Test
    @DisplayName("Should return false when latitude is above maximum (90)")
    void testInvalidStation_LatitudeAboveMaximum() {
        // Arrange
        Station station = new Station(
                "Invalid North Station",
                90.1,
                0.0,
                "Arctic",
                "UTC",
                "UTC",
                false,
                false,
                false
        );

        // Act
        boolean result = StationValidator.isValidStation(station);

        // Assert
        assertFalse(result, "Station with latitude > 90 should be invalid");
    }

    @Test
    @DisplayName("Should return false when longitude is below minimum (-180)")
    void testInvalidStation_LongitudeBelowMinimum() {
        // Arrange
        Station station = new Station(
                "Invalid West Station",
                0.0,
                -180.1,
                "Pacific",
                "UTC",
                "UTC",
                false,
                false,
                false
        );

        // Act
        boolean result = StationValidator.isValidStation(station);

        // Assert
        assertFalse(result, "Station with longitude < -180 should be invalid");
    }

    @Test
    @DisplayName("Should return false when longitude is above maximum (180)")
    void testInvalidStation_LongitudeAboveMaximum() {
        // Arrange
        Station station = new Station(
                "Invalid East Station",
                0.0,
                180.1,
                "Pacific",
                "UTC",
                "UTC",
                false,
                false,
                false
        );

        // Act
        boolean result = StationValidator.isValidStation(station);

        // Assert
        assertFalse(result, "Station with longitude > 180 should be invalid");
    }

    @Test
    @DisplayName("Should return false when both coordinates are out of bounds")
    void testInvalidStation_BothCoordinatesInvalid() {
        // Arrange
        Station station = new Station(
                "Completely Invalid Station",
                100.0,
                200.0,
                "Nowhere",
                "Invalid/Zone",
                "Invalid",
                false,
                false,
                false
        );

        // Act
        boolean result = StationValidator.isValidStation(station);

        // Assert
        assertFalse(result, "Station with both invalid coordinates should be invalid");
    }

    @Test
    @DisplayName("Should return false when latitude is slightly below minimum")
    void testInvalidStation_LatitudeJustBelowMinimum() {
        // Arrange
        Station station = new Station(
                "Edge Case South",
                -90.0001,
                0.0,
                "Antarctica",
                "UTC",
                "UTC",
                false,
                false,
                false
        );

        // Act
        boolean result = StationValidator.isValidStation(station);

        // Assert
        assertFalse(result, "Station with latitude slightly < -90 should be invalid");
    }

    @Test
    @DisplayName("Should return false when longitude is slightly above maximum")
    void testInvalidStation_LongitudeJustAboveMaximum() {
        // Arrange
        Station station = new Station(
                "Edge Case East",
                0.0,
                180.0001,
                "Pacific",
                "UTC",
                "UTC",
                false,
                false,
                false
        );

        // Act
        boolean result = StationValidator.isValidStation(station);

        // Assert
        assertFalse(result, "Station with longitude slightly > 180 should be invalid");
    }

    // ==================== TESTES DE CASOS EXTREMOS ====================

    @Test
    @DisplayName("Should return false when multiple fields are invalid")
    void testInvalidStation_MultipleInvalidFields() {
        // Arrange
        Station station = new Station(
                "",
                100.0,
                -200.0,
                null,
                "   ",
                "Invalid",
                false,
                false,
                false
        );

        // Act
        boolean result = StationValidator.isValidStation(station);

        // Assert
        assertFalse(result, "Station with multiple invalid fields should be invalid");
    }

    @Test
    @DisplayName("Should return true for valid station with all boolean flags false")
    void testValidStation_AllBooleansFalse() {
        // Arrange
        Station station = new Station(
                "Small Station",
                40.0,
                -8.0,
                "Portugal",
                "Europe/Lisbon",
                "Europe",
                false,
                false,
                false
        );

        // Act
        boolean result = StationValidator.isValidStation(station);

        // Assert
        assertTrue(result, "Station with all boolean flags false should be valid");
    }

    @Test
    @DisplayName("Should return true for valid station with all boolean flags true")
    void testValidStation_AllBooleansTrue() {
        // Arrange
        Station station = new Station(
                "Major Hub",
                41.0,
                -8.5,
                "Portugal",
                "Europe/Lisbon",
                "Europe",
                true,
                true,
                true
        );

        // Act
        boolean result = StationValidator.isValidStation(station);

        // Assert
        assertTrue(result, "Station with all boolean flags true should be valid");
    }

    @Test
    @DisplayName("Should return true for station with very long name")
    void testValidStation_VeryLongName() {
        // Arrange
        String longName = "Estação Ferroviária Internacional de Longa Distância " +
                "com Múltiplas Plataformas e Conexões Internacionais";
        Station station = new Station(
                longName,
                38.7167,
                -9.1333,
                "Portugal",
                "Europe/Lisbon",
                "Europe",
                false,
                true,
                false
        );

        // Act
        boolean result = StationValidator.isValidStation(station);

        // Assert
        assertTrue(result, "Station with very long name should be valid");
    }

    @Test
    @DisplayName("Should return true for station with single character name")
    void testValidStation_SingleCharacterName() {
        // Arrange
        Station station = new Station(
                "A",
                45.0,
                10.0,
                "Italy",
                "Europe/Rome",
                "Europe",
                false,
                false,
                false
        );

        // Act
        boolean result = StationValidator.isValidStation(station);

        // Assert
        assertTrue(result, "Station with single character name should be valid");
    }
}