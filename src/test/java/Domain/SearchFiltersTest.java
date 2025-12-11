package Domain;

import Sprint2.Domain.Station;
import Sprint2.Domain.ENUM.Country;
import Sprint2.Domain.SearchFilters;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for SearchFilters (USEI08).
 * Tests all methods and edge cases of the SearchFilters class.
 */
class SearchFiltersTest {

    @Test
    void testDefaultConstructor_NoFilters() {
        SearchFilters filters = new SearchFilters();
        
        assertNull(filters.getIsCity(), "isCity deve ser null");
        assertNull(filters.getIsMainStation(), "isMainStation deve ser null");
        assertNull(filters.getCountry(), "country deve ser null");
        assertFalse(filters.hasFilters(), "Não deve ter filtros ativos");
    }

    @Test
    void testConstructor_WithAllFilters() {
        SearchFilters filters = new SearchFilters(true, false, Country.PT);
        
        assertTrue(filters.getIsCity(), "isCity deve ser true");
        assertFalse(filters.getIsMainStation(), "isMainStation deve ser false");
        assertEquals(Country.PT, filters.getCountry(), "country deve ser PT");
        assertTrue(filters.hasFilters(), "Deve ter filtros ativos");
    }

    @Test
    void testConstructor_WithPartialFilters() {
        SearchFilters filters1 = new SearchFilters(true, null, null);
        assertTrue(filters1.hasFilters(), "Deve ter filtro isCity");
        
        SearchFilters filters2 = new SearchFilters(null, true, null);
        assertTrue(filters2.hasFilters(), "Deve ter filtro isMainStation");
        
        SearchFilters filters3 = new SearchFilters(null, null, Country.ES);
        assertTrue(filters3.hasFilters(), "Deve ter filtro country");
    }

    @Test
    void testHasFilters_NoFilters_ReturnsFalse() {
        SearchFilters filters = new SearchFilters();
        assertFalse(filters.hasFilters(), "Sem filtros deve retornar false");
    }

    @Test
    void testHasFilters_WithCityFilter_ReturnsTrue() {
        SearchFilters filters = new SearchFilters(true, null, null);
        assertTrue(filters.hasFilters(), "Com filtro isCity deve retornar true");
    }

    @Test
    void testHasFilters_WithMainStationFilter_ReturnsTrue() {
        SearchFilters filters = new SearchFilters(null, true, null);
        assertTrue(filters.hasFilters(), "Com filtro isMainStation deve retornar true");
    }

    @Test
    void testHasFilters_WithCountryFilter_ReturnsTrue() {
        SearchFilters filters = new SearchFilters(null, null, Country.PT);
        assertTrue(filters.hasFilters(), "Com filtro country deve retornar true");
    }

    @Test
    void testHasFilters_WithMultipleFilters_ReturnsTrue() {
        SearchFilters filters = new SearchFilters(true, false, Country.ES);
        assertTrue(filters.hasFilters(), "Com múltiplos filtros deve retornar true");
    }

    @Test
    void testMatches_NullStation_ReturnsFalse() {
        SearchFilters filters = new SearchFilters();
        assertFalse(filters.matches(null), "Station null deve retornar false");
    }

    @Test
    void testMatches_NoFilters_MatchesAllStations() {
        SearchFilters filters = new SearchFilters();
        
        Station station1 = new Station("Test1", 40.0, -8.0, "PT", "tz", "WET", true, true, false);
        Station station2 = new Station("Test2", 50.0, 2.0, "FR", "tz", "CET", false, false, false);
        
        assertTrue(filters.matches(station1), "Sem filtros deve aceitar todas as estações");
        assertTrue(filters.matches(station2), "Sem filtros deve aceitar todas as estações");
    }

    @Test
    void testMatches_CountryFilter_MatchesCorrectCountry() {
        SearchFilters filters = new SearchFilters(null, null, Country.PT);
        
        Station ptStation = new Station("Lisboa", 38.7, -9.1, "PT", "tz", "WET", true, true, false);
        Station esStation = new Station("Madrid", 40.4, -3.7, "ES", "tz", "CET", true, true, false);
        Station frStation = new Station("Paris", 48.8, 2.3, "FR", "tz", "CET", true, true, false);
        
        assertTrue(filters.matches(ptStation), "Deve aceitar estação portuguesa");
        assertFalse(filters.matches(esStation), "Não deve aceitar estação espanhola quando filtro é PT");
        assertFalse(filters.matches(frStation), "Não deve aceitar estação francesa quando filtro é PT");
    }

    @Test
    void testMatches_CountryFilterPT_OnlyMatchesPT() {
        SearchFilters filters = new SearchFilters(null, null, Country.PT);
        
        Station ptStation = new Station("Lisboa", 38.7, -9.1, "PT", "tz", "WET", true, true, false);
        Station esStation = new Station("Madrid", 40.4, -3.7, "ES", "tz", "CET", true, true, false);
        
        assertTrue(filters.matches(ptStation), "Deve aceitar estação portuguesa");
        
        // Verificar se ES não faz match (depende da implementação de Country.fromString)
        Country esCountry = Country.fromString("ES");
        if (esCountry != Country.PT) {
            assertFalse(filters.matches(esStation), "Não deve aceitar estação espanhola quando filtro é PT");
        }
    }

    @Test
    void testMatches_IsCityFilter_MatchesCorrectCityStatus() {
        SearchFilters filtersTrue = new SearchFilters(true, null, null);
        SearchFilters filtersFalse = new SearchFilters(false, null, null);
        
        Station cityStation = new Station("Lisboa", 38.7, -9.1, "PT", "tz", "WET", true, true, false);
        Station nonCityStation = new Station("SmallTown", 40.0, -8.0, "PT", "tz", "WET", false, false, false);
        
        assertTrue(filtersTrue.matches(cityStation), "Filtro isCity=true deve aceitar cidades");
        assertFalse(filtersTrue.matches(nonCityStation), "Filtro isCity=true não deve aceitar não-cidades");
        
        assertFalse(filtersFalse.matches(cityStation), "Filtro isCity=false não deve aceitar cidades");
        assertTrue(filtersFalse.matches(nonCityStation), "Filtro isCity=false deve aceitar não-cidades");
    }

    @Test
    void testMatches_IsMainStationFilter_MatchesCorrectMainStationStatus() {
        SearchFilters filtersTrue = new SearchFilters(null, true, null);
        SearchFilters filtersFalse = new SearchFilters(null, false, null);
        
        Station mainStation = new Station("Lisboa", 38.7, -9.1, "PT", "tz", "WET", true, true, false);
        Station nonMainStation = new Station("SmallTown", 40.0, -8.0, "PT", "tz", "WET", false, false, false);
        
        assertTrue(filtersTrue.matches(mainStation), "Filtro isMainStation=true deve aceitar estações principais");
        assertFalse(filtersTrue.matches(nonMainStation), "Filtro isMainStation=true não deve aceitar não-principais");
        
        assertFalse(filtersFalse.matches(mainStation), "Filtro isMainStation=false não deve aceitar estações principais");
        assertTrue(filtersFalse.matches(nonMainStation), "Filtro isMainStation=false deve aceitar não-principais");
    }

    @Test
    void testMatches_CombinedFilters_AllMustMatch() {
        // Filtro: cidades portuguesas que são estações principais
        SearchFilters filters = new SearchFilters(true, true, Country.PT);
        
        Station matchingStation = new Station("Lisboa", 38.7, -9.1, "PT", "tz", "WET", true, true, false);
        Station nonCityStation = new Station("SmallTown", 40.0, -8.0, "PT", "tz", "WET", false, true, false);
        Station nonMainStation = new Station("Porto", 41.1, -8.6, "PT", "tz", "WET", true, false, false);
        Station wrongCountry = new Station("Madrid", 40.4, -3.7, "ES", "tz", "CET", true, true, false);
        
        assertTrue(filters.matches(matchingStation), "Deve aceitar estação que corresponde a todos os filtros");
        assertFalse(filters.matches(nonCityStation), "Não deve aceitar se não for cidade");
        assertFalse(filters.matches(nonMainStation), "Não deve aceitar se não for estação principal");
        
        // Verificar país
        Country esCountry = Country.fromString("ES");
        if (esCountry != Country.PT) {
            assertFalse(filters.matches(wrongCountry), "Não deve aceitar se país não corresponder");
        }
    }

    @Test
    void testMatches_PartialFilters_OnlyActiveFiltersMatter() {
        // Apenas filtro de cidade, país e mainStation são null
        SearchFilters filters = new SearchFilters(true, null, null);
        
        Station ptCity = new Station("Lisboa", 38.7, -9.1, "PT", "tz", "WET", true, true, false);
        Station esCity = new Station("Madrid", 40.4, -3.7, "ES", "tz", "CET", true, true, false);
        Station ptNonCity = new Station("SmallTown", 40.0, -8.0, "PT", "tz", "WET", false, false, false);
        
        assertTrue(filters.matches(ptCity), "Deve aceitar cidade portuguesa (filtro país não ativo)");
        assertTrue(filters.matches(esCity), "Deve aceitar cidade espanhola (filtro país não ativo)");
        assertFalse(filters.matches(ptNonCity), "Não deve aceitar não-cidade");
    }

    @Test
    void testToString_NoFilters() {
        SearchFilters filters = new SearchFilters();
        String result = filters.toString();
        
        assertEquals("No filters applied", result, "toString sem filtros deve indicar 'No filters applied'");
    }

    @Test
    void testToString_WithCountryFilter() {
        SearchFilters filters = new SearchFilters(null, null, Country.PT);
        String result = filters.toString();
        
        assertTrue(result.contains("Active filters"), "Deve conter 'Active filters'");
        assertTrue(result.contains("Country=PT"), "Deve conter o país");
    }

    @Test
    void testToString_WithCityFilter() {
        SearchFilters filtersTrue = new SearchFilters(true, null, null);
        SearchFilters filtersFalse = new SearchFilters(false, null, null);
        
        String resultTrue = filtersTrue.toString();
        String resultFalse = filtersFalse.toString();
        
        assertTrue(resultTrue.contains("City=Yes"), "isCity=true deve mostrar 'City=Yes'");
        assertTrue(resultFalse.contains("City=No"), "isCity=false deve mostrar 'City=No'");
    }

    @Test
    void testToString_WithMainStationFilter() {
        SearchFilters filtersTrue = new SearchFilters(null, true, null);
        SearchFilters filtersFalse = new SearchFilters(null, false, null);
        
        String resultTrue = filtersTrue.toString();
        String resultFalse = filtersFalse.toString();
        
        assertTrue(resultTrue.contains("Main Station=Yes"), "isMainStation=true deve mostrar 'Main Station=Yes'");
        assertTrue(resultFalse.contains("Main Station=No"), "isMainStation=false deve mostrar 'Main Station=No'");
    }

    @Test
    void testToString_WithMultipleFilters() {
        SearchFilters filters = new SearchFilters(true, false, Country.ES);
        String result = filters.toString();
        
        assertTrue(result.contains("Active filters"), "Deve conter 'Active filters'");
        assertTrue(result.contains("Country=ES"), "Deve conter o país");
        assertTrue(result.contains("City=Yes"), "Deve conter isCity");
        assertTrue(result.contains("Main Station=No"), "Deve conter isMainStation");
        assertTrue(result.contains(","), "Deve ter vírgulas separando múltiplos filtros");
    }

    @Test
    void testGetters_ReturnCorrectValues() {
        SearchFilters filters = new SearchFilters(true, false, Country.PT);
        
        assertEquals(true, filters.getIsCity(), "getIsCity deve retornar true");
        assertEquals(false, filters.getIsMainStation(), "getIsMainStation deve retornar false");
        assertEquals(Country.PT, filters.getCountry(), "getCountry deve retornar PT");
    }

    @Test
    void testGetters_WithNullValues() {
        SearchFilters filters = new SearchFilters(null, null, null);
        
        assertNull(filters.getIsCity(), "getIsCity deve retornar null");
        assertNull(filters.getIsMainStation(), "getIsMainStation deve retornar null");
        assertNull(filters.getCountry(), "getCountry deve retornar null");
    }

    @Test
    void testMatches_CountryFilterWithUnknownCountry() {
        SearchFilters filters = new SearchFilters(null, null, Country.PT);
        
        // Estação com país que não está no enum Country (ex: "FR")
        Station frStation = new Station("Paris", 48.8, 2.3, "FR", "tz", "CET", true, true, false);
        
        // Country.fromString("FR") pode retornar null ou lançar exceção
        // Vamos testar o comportamento real
        Country frCountry = Country.fromString("FR");
        if (frCountry == null || frCountry != Country.PT) {
            assertFalse(filters.matches(frStation), "Não deve aceitar país que não corresponde ao filtro");
        }
    }
}

