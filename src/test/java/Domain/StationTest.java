package Domain;

import Sprint1.Domain.Station;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StationTest {

    private Station station1;
    private Station station2;
    private Station station3;

    @BeforeEach
    void setUp() {
        station1 = new Station(
                "Lisboa Oriente",
                38.7681,
                -9.0947,
                "PT",
                "('Europe/Lisbon',)",
                "WET",
                true,
                true,
                false
        );

        station2 = new Station(
                "Porto Campanhã",
                41.1496,
                -8.5850,
                "PT",
                "('Europe/Lisbon',)",
                "WET",
                false,
                true,
                false
        );

        station3 = new Station(
                "Lisboa Oriente",
                38.7681,
                -9.0947,
                "PT",
                "('Europe/Lisbon',)",
                "WET",
                true,
                true,
                false
        );
    }

    @Test
    void compareTo_SameName_ReturnsZero() {
        Station s1 = new Station("Menton", 43.77, 7.50, "FR", "tz", "CET", true, false, false);
        Station s2 = new Station("Menton", 43.78, 7.51, "FR", "tz", "CET", false, true, false);

        assertEquals(0, s1.compareTo(s2),
                "Estações com mesmo nome devem retornar 0");
    }

    @Test
    void compareTo_DifferentName_ReturnsNonZero() {
        Station s1 = new Station("Aveiro", 40.64, -8.65, "PT", "tz", "WET", true, false, false);
        Station s2 = new Station("Braga", 41.55, -8.43, "PT", "tz", "WET", true, false, false);

        assertTrue(s1.compareTo(s2) < 0,
                "Aveiro deve vir antes de Braga alfabeticamente");
        assertTrue(s2.compareTo(s1) > 0,
                "Braga deve vir depois de Aveiro alfabeticamente");
    }

    @Test
    void compareTo_SameNameDifferentCountry_ComparesCountry() {
        Station s1 = new Station("Central", 40.0, -8.0, "FR", "tz", "CET", true, false, false);
        Station s2 = new Station("Central", 40.0, -8.0, "PT", "tz", "WET", true, false, false);

        assertTrue(s1.compareTo(s2) < 0,
                "FR deve vir antes de PT alfabeticamente");
        assertTrue(s2.compareTo(s1) > 0,
                "PT deve vir depois de FR alfabeticamente");
    }

    @Test
    void compareTo_Reflexive_ReturnsZero() {
        assertEquals(0, station1.compareTo(station1),
                "Estação comparada consigo mesma deve retornar 0");
    }

    @Test
    void compareTo_Symmetric() {
        int result1 = station1.compareTo(station2);
        int result2 = station2.compareTo(station1);

        assertTrue((result1 > 0 && result2 < 0) || (result1 < 0 && result2 > 0),
                "Comparação deve ser simétrica");
    }

    @Test
    void getName_ReturnsCorrectName() {
        assertEquals("Lisboa Oriente", station1.getName(),
                "Nome deve corresponder ao definido no construtor");
    }

    @Test
    void setName_UpdatesName() {
        station1.setName("Lisboa Santa Apolónia");
        assertEquals("Lisboa Santa Apolónia", station1.getName(),
                "Nome deve ser atualizado");
    }

    @Test
    void setName_Null_UpdatesToNull() {
        station1.setName(null);
        assertNull(station1.getName(), "Nome deve poder ser null");
    }

    @Test
    void getLongitude_ReturnsCorrectValue() {
        assertEquals(-9.0947, station1.getLongitude(), 0.0001,
                "Longitude deve corresponder ao valor definido");
    }

    @Test
    void setLongitude_UpdatesValue() {
        station1.setLongitude(-9.1000);
        assertEquals(-9.1000, station1.getLongitude(), 0.0001,
                "Longitude deve ser atualizada");
    }

    @Test
    void setLongitude_NegativeValue_Accepts() {
        station1.setLongitude(-180.0);
        assertEquals(-180.0, station1.getLongitude(), 0.0001,
                "Deve aceitar longitude negativa");
    }

    @Test
    void setLongitude_PositiveValue_Accepts() {
        station1.setLongitude(180.0);
        assertEquals(180.0, station1.getLongitude(), 0.0001,
                "Deve aceitar longitude positiva");
    }

    @Test
    void getLatitude_ReturnsCorrectValue() {
        assertEquals(38.7681, station1.getLatitude(), 0.0001,
                "Latitude deve corresponder ao valor definido");
    }

    @Test
    void setLatitude_UpdatesValue() {
        station1.setLatitude(38.7500);
        assertEquals(38.7500, station1.getLatitude(), 0.0001,
                "Latitude deve ser atualizada");
    }

    @Test
    void setLatitude_NegativeValue_Accepts() {
        station1.setLatitude(-90.0);
        assertEquals(-90.0, station1.getLatitude(), 0.0001,
                "Deve aceitar latitude negativa");
    }

    @Test
    void setLatitude_PositiveValue_Accepts() {
        station1.setLatitude(90.0);
        assertEquals(90.0, station1.getLatitude(), 0.0001,
                "Deve aceitar latitude positiva");
    }

    @Test
    void getTimeZone_ReturnsCorrectValue() {
        assertEquals("('Europe/Lisbon',)", station1.getTimeZone(),
                "TimeZone deve corresponder ao valor definido");
    }

    @Test
    void setTimeZone_UpdatesValue() {
        station1.setTimeZone("('Europe/Paris',)");
        assertEquals("('Europe/Paris',)", station1.getTimeZone(),
                "TimeZone deve ser atualizado");
    }

    @Test
    void getCountry_ReturnsCorrectValue() {
        assertEquals("PT", station1.getCountry(),
                "País deve corresponder ao valor definido");
    }

    @Test
    void setCountry_UpdatesValue() {
        station1.setCountry("ES");
        assertEquals("ES", station1.getCountry(),
                "País deve ser atualizado");
    }

    @Test
    void getTimeZoneGroup_ReturnsCorrectValue() {
        assertEquals("WET", station1.getTimeZoneGroup(),
                "TimeZoneGroup deve corresponder ao valor definido");
    }

    @Test
    void setTimeZoneGroup_UpdatesValue() {
        station1.setTimeZoneGroup("CET");
        assertEquals("CET", station1.getTimeZoneGroup(),
                "TimeZoneGroup deve ser atualizado");
    }

    @Test
    void isCity_ReturnsCorrectValue() {
        assertTrue(station1.isCity(), "Deve retornar true para cidade");
        assertFalse(station2.isCity(), "Deve retornar false quando não é cidade");
    }

    @Test
    void setCity_UpdatesValue() {
        station2.setCity(true);
        assertTrue(station2.isCity(), "isCity deve ser atualizado para true");

        station1.setCity(false);
        assertFalse(station1.isCity(), "isCity deve ser atualizado para false");
    }

    @Test
    void isMainStation_ReturnsCorrectValue() {
        assertTrue(station1.isMainStation(), "Deve retornar true para estação principal");
        assertTrue(station2.isMainStation(), "Deve retornar true para estação principal");
    }

    @Test
    void setMainStation_UpdatesValue() {
        station1.setMainStation(false);
        assertFalse(station1.isMainStation(),
                "isMainStation deve ser atualizado para false");

        station1.setMainStation(true);
        assertTrue(station1.isMainStation(),
                "isMainStation deve ser atualizado para true");
    }

    @Test
    void isAirport_ReturnsCorrectValue() {
        assertFalse(station1.isAirport(), "Deve retornar false quando não é aeroporto");
    }

    @Test
    void setAirport_UpdatesValue() {
        station1.setAirport(true);
        assertTrue(station1.isAirport(), "isAirport deve ser atualizado para true");

        station1.setAirport(false);
        assertFalse(station1.isAirport(), "isAirport deve ser atualizado para false");
    }

    @Test
    void testEquals_SameObject_ReturnsTrue() {
        assertTrue(station1.equals(station1),
                "Estação deve ser igual a si mesma");
    }

    @Test
    void testEquals_EqualObjects_ReturnsTrue() {
        assertTrue(station1.equals(station3),
                "Estações com mesmo nome, latitude e longitude devem ser iguais");
        assertTrue(station3.equals(station1),
                "Equals deve ser simétrico");
    }

    @Test
    void testEquals_DifferentName_ReturnsFalse() {
        assertFalse(station1.equals(station2),
                "Estações com nomes diferentes devem ser diferentes");
    }

    @Test
    void testEquals_DifferentLatitude_ReturnsFalse() {
        Station s = new Station("Lisboa Oriente", 38.7700, -9.0947, "PT", "tz", "WET",
                true, true, false);
        assertFalse(station1.equals(s),
                "Estações com latitudes diferentes devem ser diferentes");
    }

    @Test
    void testEquals_DifferentLongitude_ReturnsFalse() {
        Station s = new Station("Lisboa Oriente", 38.7681, -9.1000, "PT", "tz", "WET",
                true, true, false);
        assertFalse(station1.equals(s),
                "Estações com longitudes diferentes devem ser diferentes");
    }

    @Test
    void testEquals_Null_ReturnsFalse() {
        assertFalse(station1.equals(null),
                "Estação não deve ser igual a null");
    }

    @Test
    void testEquals_DifferentClass_ReturnsFalse() {
        assertFalse(station1.equals("Not a Station"),
                "Estação não deve ser igual a objeto de outra classe");
    }

    @Test
    void testEquals_DifferentBooleanFlags_StillEqual() {
        // equals() considera apenas name, latitude, longitude
        Station s = new Station("Lisboa Oriente", 38.7681, -9.0947, "PT", "tz", "WET",
                false, false, true);
        assertTrue(station1.equals(s),
                "Estações devem ser iguais independentemente dos flags booleanos");
    }

    @Test
    void testEquals_DifferentCountry_StillEqual() {
        // equals() considera apenas name, latitude, longitude
        Station s = new Station("Lisboa Oriente", 38.7681, -9.0947, "ES", "tz", "CET",
                true, true, false);
        assertTrue(station1.equals(s),
                "Estações devem ser iguais independentemente do país");
    }

    @Test
    void testHashCode_EqualObjects_SameHashCode() {
        assertEquals(station1.hashCode(), station3.hashCode(),
                "Objetos iguais devem ter o mesmo hashCode");
    }

    @Test
    void testHashCode_DifferentObjects_MayHaveDifferentHashCode() {
        assertNotEquals(station1.hashCode(), station2.hashCode(),
                "Objetos diferentes geralmente têm hashCodes diferentes");
    }

    @Test
    void testHashCode_Consistent() {
        int hash1 = station1.hashCode();
        int hash2 = station1.hashCode();

        assertEquals(hash1, hash2,
                "hashCode deve ser consistente em múltiplas chamadas");
    }

    @Test
    void testHashCode_BasedOnNameLatitudeLongitude() {
        Station s = new Station("Lisboa Oriente", 38.7681, -9.0947, "ES", "tz", "CET",
                false, false, true);
        assertEquals(station1.hashCode(), s.hashCode(),
                "hashCode deve considerar apenas name, latitude e longitude");
    }

    @Test
    void testToString_ContainsAllFields() {
        String result = station1.toString();

        assertTrue(result.contains("name='Lisboa Oriente'"),
                "toString deve conter o nome");
        assertTrue(result.contains("latitude=38.7681"),
                "toString deve conter a latitude");
        assertTrue(result.contains("longitude=-9.0947"),
                "toString deve conter a longitude");
        assertTrue(result.contains("country='PT'"),
                "toString deve conter o país");
        assertTrue(result.contains("timeZone='('Europe/Lisbon',)'"),
                "toString deve conter o timezone");
        assertTrue(result.contains("timeZoneGroup='WET'"),
                "toString deve conter o timezone group");
        assertTrue(result.contains("isCity=true"),
                "toString deve conter isCity");
        assertTrue(result.contains("isMainStation=true"),
                "toString deve conter isMainStation");
        assertTrue(result.contains("isAirport=false"),
                "toString deve conter isAirport");
    }

    @Test
    void testToString_StartsWithStation() {
        String result = station1.toString();
        assertTrue(result.startsWith("Station{"),
                "toString deve começar com 'Station{'");
    }

    @Test
    void testToString_NotNull() {
        assertNotNull(station1.toString(),
                "toString não deve retornar null");
    }

    @Test
    void testToString_WithEmptyStringFields() {
        // Testa toString com strings vazias (válido) ao invés de null (inválido)
        Station s = new Station("", 0.0, 0.0, "", "", "", false, false, false);
        String result = s.toString();

        assertNotNull(result, "toString deve funcionar com campos vazios");
        assertTrue(result.contains("name=''"),
                "toString deve representar campos vazios");
        assertTrue(result.contains("country=''"),
                "toString deve representar país vazio");
    }

    @Test
    void constructor_AllParameters_CreatesValidObject() {
        Station s = new Station("Test", 45.0, 7.5, "FR", "tz", "CET", true, false, true);

        assertEquals("Test", s.getName());
        assertEquals(45.0, s.getLatitude(), 0.0001);
        assertEquals(7.5, s.getLongitude(), 0.0001);
        assertEquals("FR", s.getCountry());
        assertEquals("tz", s.getTimeZone());
        assertEquals("CET", s.getTimeZoneGroup());
        assertTrue(s.isCity());
        assertFalse(s.isMainStation());
        assertTrue(s.isAirport());
    }

    @Test
    void equalsAndHashCode_Contract() {
        // Se dois objetos são iguais, devem ter o mesmo hashCode
        if (station1.equals(station3)) {
            assertEquals(station1.hashCode(), station3.hashCode(),
                    "Objetos iguais devem ter hashCode igual");
        }
    }

    @Test
    void compareTo_Transitive() {
        Station a = new Station("A", 40.0, -8.0, "PT", "tz", "WET", true, false, false);
        Station b = new Station("B", 41.0, -9.0, "PT", "tz", "WET", true, false, false);
        Station c = new Station("C", 42.0, -7.0, "PT", "tz", "WET", true, false, false);

        assertTrue(a.compareTo(b) < 0);
        assertTrue(b.compareTo(c) < 0);
        assertTrue(a.compareTo(c) < 0,
                "compareTo deve ser transitivo");
    }

    @Test
    void allGettersAndSetters_WorkCorrectly() {
        Station s = new Station("Initial", 0.0, 0.0, "XX", "tz1", "TZ1",
                false, false, false);

        // Testa todos os setters
        s.setName("Updated");
        s.setLatitude(45.5);
        s.setLongitude(-8.5);
        s.setCountry("YY");
        s.setTimeZone("tz2");
        s.setTimeZoneGroup("TZ2");
        s.setCity(true);
        s.setMainStation(true);
        s.setAirport(true);

        // Verifica todos os getters
        assertEquals("Updated", s.getName());
        assertEquals(45.5, s.getLatitude(), 0.0001);
        assertEquals(-8.5, s.getLongitude(), 0.0001);
        assertEquals("YY", s.getCountry());
        assertEquals("tz2", s.getTimeZone());
        assertEquals("TZ2", s.getTimeZoneGroup());
        assertTrue(s.isCity());
        assertTrue(s.isMainStation());
        assertTrue(s.isAirport());
    }
}