package Sprint2.Domain.ENUM;

/**
 * Enum representing countries for geographical search (USEI08).
 * Contains all countries found in train_stations_europe.csv.
 * 
 * @author Development Team
 * @version 2.0
 */
public enum Country {
    AD,
    AL,
    AT,
    BA,
    BE,
    BG,
    BY,
    CH,
    CY,
    CZ,
    DE,
    DK,
    EE,
    ES,
    FI,
    FR,
    GB,
    GR,
    HR,
    HU,
    IE,
    IT,
    LI,
    LT,
    LU,
    LV,
    MA,
    MD,
    ME,
    MK,
    MT,
    NL,
    NO,
    PL,
    PT,
    RO,
    RS,
    RU,
    SE,
    SI,
    SK,
    TR,
    UA;
    
    /**
     * Parses a country code string to Country enum.
     *
     * @param code the country code (e.g., PT, ES, FR, DE, etc., or "all")
     * @return the Country enum, or null if "all" or invalid
     */
    public static Country fromString(String code) {
        if (code == null || code.trim().isEmpty() || code.equalsIgnoreCase("all")) {
            return null;
        }
        
        try {
            return Country.valueOf(code.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}

