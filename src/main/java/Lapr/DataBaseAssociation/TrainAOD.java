package Lapr.DataBaseAssociation;

import Lapr.Domain.Train;
import Sprint1.Domain.Facility;
import Sprint1.Domain.Locomotive;
import Sprint1.Domain.Wagon;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Access Object Data for Train.
 * Retrieves train data from Oracle database using PL/SQL functions.
 * 
 * NOTE: This class is currently not fully implemented as Train is an immutable class
 * that requires all parameters in its constructor. To properly load trains from database,
 * you would need to also load routes, locomotives, and wagons from their respective tables.
 * 
 * @author Development Team
 * @version 1.0
 */
public class TrainAOD {
    private static final String USERNAME = "System";
    private static final String PASSWORD = "G0442526BD";
    private static final String DATABASE = "jdbc:oracle:thin:@vsgate-s1.dei.isep.ipp.pt:10715:XE";

    /**
     * Gets all trains from database.
     * 
     * NOTE: This method is not fully implemented because Train is immutable and requires
     * route, locomotives, and wagons to be loaded from other tables.
     * This is a placeholder that would need to be completed with proper data loading.
     * 
     * @return list of trains (currently returns empty list - needs implementation)
     */
    public List<Train> getAllTrains() {
        List<Train> trains = new ArrayList<>();
        
        // TODO: Implement proper train loading from database
        // This requires:
        // 1. Loading route from TRAIN_ROUTE table
        // 2. Loading locomotives from LOCOMOTIVE_TRAIN table
        // 3. Loading wagons from WAGON_IN_FREIGHT or similar table
        // 4. Loading departure time from TRAIN table
        // 5. Calculating total weight from wagons
        
        // For now, return empty list as Train constructor requires all parameters
        // and we cannot create a Train without complete data
        
        return trains;
    }
}