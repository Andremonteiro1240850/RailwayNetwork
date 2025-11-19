package Domain;

/**
 * The type Station validator.
 */
public class StationValidator {
    /**
     * Is valid station boolean.
     *
     * @param station the station
     * @return the boolean
     */
    public static boolean isValidStation(Station station) {
        if (station == null) {
            return false;
        }
        if (station.getName() == null || station.getName().trim().isEmpty()) {
            return false;
        }
        if (station.getLatitude() < -90 || station.getLatitude() > 90) {
            return false;
        }
        if (station.getLongitude() < -180 || station.getLongitude() > 180) {
            return false;
        }
        if (station.getCountry() == null || station.getCountry().trim().isEmpty()) {
            return false;
        }
        if (station.getTimeZone() == null || station.getTimeZone().trim().isEmpty()) {
            return false;
        }
        return true;
    }

}
