package Lapr.Domain;

import Sprint2.Domain.LineSegment;

/**
 * Service class for calculating train physics using Newton's 2nd Law.
 * Implements USLP10 requirements for dynamic speed calculation.
 * 
 * This service calculates:
 * - Acceleration based on forces (traction, rolling resistance, grade resistance, aerodynamic drag)
 * - Velocity using kinematic equations
 * - Travel time per segment
 * 
 * @author Development Team
 * @version 1.0
 */
public class CalculationService {
    
    // Physical constants
    private static final double GRAVITY_MS2 = 9.81; // m/s²
    private static final double AIR_DENSITY_KG_M3 = 1.225; // kg/m³ at sea level
    private static final double DRAG_COEFFICIENT = 0.8; // Typical for trains
    private static final double FRONTAL_AREA_M2 = 12.0; // Typical frontal area in m²
    private static final double ROLLING_RESISTANCE_COEFFICIENT = 0.0015; // Typical for steel wheels on steel rails
    
    /**
     * Calculates the travel time for a train on a segment using physics.
     * 
     * @param train the train with locomotives and weight
     * @param segment the line segment with length, inclination, and speed limit
     * @param initialVelocityMs initial velocity in m/s (from previous segment or 0 if starting)
     * @return SegmentCalculationResult containing final velocity, travel time, and distance
     */
    public SegmentCalculationResult calculateSegment(Train train, LineSegment segment, double initialVelocityMs) {
        if (train == null) {
            throw new IllegalArgumentException("Train cannot be null");
        }
        if (segment == null) {
            throw new IllegalArgumentException("Segment cannot be null");
        }
        if (initialVelocityMs < 0) {
            throw new IllegalArgumentException("Initial velocity cannot be negative");
        }
        
        // Get train properties
        double totalMassKg = train.getTotalMassKg(); // Mass in kg (includes locomotives + wagons)
        double totalPowerKw = calculateTotalPower(train);
        
        // Validate train properties
        if (totalMassKg <= 0) {
            throw new IllegalStateException(
                String.format("Train %s has invalid mass: %.2f kg. Mass must be greater than 0 for physics calculations.",
                            train.getTrainId(), totalMassKg));
        }
        if (totalPowerKw <= 0) {
            throw new IllegalStateException(
                String.format("Train %s has invalid power: %.2f kW. Power must be greater than 0 for physics calculations.",
                            train.getTrainId(), totalPowerKw));
        }
        
        // Get segment properties
        double segmentLengthM = segment.getLengthKm() * 1000.0; // Convert km to m
        double inclinationPercent = segment.getInclinationPercent();
        Double speedLimitMs = segment.hasSpeedLimit() ? 
            segment.getSpeedLimitKmH() / 3.6 : null; // Convert km/h to m/s
        
        // Debug logging
        System.out.println("    Physics calculation: mass=" + totalMassKg + " kg, power=" + totalPowerKw + " kW, " +
                         "segmentLength=" + segmentLengthM + " m, inclination=" + inclinationPercent + "%");
        
        // Calculate forces and acceleration
        double currentVelocityMs = initialVelocityMs;
        double distanceTraveledM = 0.0;
        double totalTimeS = 0.0;
        
        // Use small time steps for numerical integration
        double timeStepS = 0.1; // 0.1 second steps
        double minVelocityMs = 0.1; // Minimum velocity to avoid division by zero
        
        int iterationCount = 0;
        int maxIterations = (int)(3600.0 / timeStepS); // Maximum iterations for 1 hour
        
        while (distanceTraveledM < segmentLengthM && iterationCount < maxIterations) {
            iterationCount++;
            // Calculate forces at current velocity
            double tractionForceN = calculateTractionForce(totalPowerKw, currentVelocityMs);
            double rollingResistanceN = calculateRollingResistance(totalMassKg);
            double gradeResistanceN = calculateGradeResistance(totalMassKg, inclinationPercent);
            double aerodynamicDragN = calculateAerodynamicDrag(currentVelocityMs);
            
            // Net force
            double netForceN = tractionForceN - rollingResistanceN - gradeResistanceN - aerodynamicDragN;
            
            // Debug first iteration
            if (iterationCount == 1) {
                System.out.println("    First iteration forces: traction=" + tractionForceN + " N, " +
                                 "rolling=" + rollingResistanceN + " N, grade=" + gradeResistanceN + " N, " +
                                 "drag=" + aerodynamicDragN + " N, net=" + netForceN + " N");
            }
            
            // Acceleration (F = ma, so a = F/m)
            double accelerationMs2 = netForceN / totalMassKg;
            
            // If net force is negative or zero and we're at rest, train cannot move
            if (iterationCount == 1 && netForceN <= 0 && currentVelocityMs < minVelocityMs) {
                System.err.println("ERROR: Train cannot move - net force is " + netForceN + " N (must be positive to start)");
                System.err.println("  Traction: " + tractionForceN + " N, Resistances: " + 
                                 (rollingResistanceN + gradeResistanceN + aerodynamicDragN) + " N");
                throw new IllegalStateException(
                    String.format("Train %s cannot move on segment %s: net force is %.2f N (must be positive). " +
                                "Check train power (%.2f kW) and mass (%.2f kg).",
                                train.getTrainId(), segment.getId(), netForceN, totalPowerKw, totalMassKg));
            }
            
            // Calculate distance that would be traveled in this time step
            // s = v₀*t + 0.5*a*t²
            double distanceStepM = currentVelocityMs * timeStepS + 0.5 * accelerationMs2 * timeStepS * timeStepS;
            
            // Ensure distance step is positive (train can't go backwards)
            if (distanceStepM < 0) {
                distanceStepM = currentVelocityMs * timeStepS; // Use constant velocity if deceleration would cause negative distance
            }
            
            // Update velocity using v² = v₀² + 2as (where s is the distance step)
            double velocitySquared = currentVelocityMs * currentVelocityMs + 2 * accelerationMs2 * distanceStepM;
            double newVelocityMs = Math.sqrt(Math.max(0, velocitySquared));
            
            // Apply speed limit if present
            if (speedLimitMs != null && newVelocityMs > speedLimitMs) {
                newVelocityMs = speedLimitMs;
                accelerationMs2 = 0; // Constant speed
            }
            
            // Ensure minimum velocity
            if (newVelocityMs < minVelocityMs) {
                newVelocityMs = minVelocityMs;
            }
            
            // Check if we would exceed segment length
            if (distanceTraveledM + distanceStepM > segmentLengthM) {
                // Adjust time step to exactly reach segment end
                double remainingDistanceM = segmentLengthM - distanceTraveledM;
                // Solve: remainingDistance = v₀*t + 0.5*a*t²
                // Using quadratic formula or approximation
                double avgVelocityMs = (currentVelocityMs + newVelocityMs) / 2.0;
                if (avgVelocityMs > minVelocityMs) {
                    double finalTimeStepS = remainingDistanceM / avgVelocityMs;
                    totalTimeS += finalTimeStepS;
                } else {
                    // Very slow, use constant acceleration formula
                    if (Math.abs(accelerationMs2) > 0.001) {
                        // s = v₀*t + 0.5*a*t² -> solve for t
                        double discriminant = currentVelocityMs * currentVelocityMs + 2 * accelerationMs2 * remainingDistanceM;
                        if (discriminant >= 0) {
                            double finalTimeStepS = (-currentVelocityMs + Math.sqrt(discriminant)) / accelerationMs2;
                            totalTimeS += Math.max(0, finalTimeStepS);
                        } else {
                            totalTimeS += remainingDistanceM / minVelocityMs;
                        }
                    } else {
                        totalTimeS += remainingDistanceM / minVelocityMs;
                    }
                }
                distanceTraveledM = segmentLengthM;
                currentVelocityMs = newVelocityMs;
                break;
            }
            
            // Update state
            distanceTraveledM += distanceStepM;
            totalTimeS += timeStepS;
            currentVelocityMs = newVelocityMs;
            
            // Safety check to avoid infinite loops
            if (totalTimeS > 3600.0) { // More than 1 hour for a segment is unrealistic
                throw new IllegalStateException("Segment calculation exceeded maximum time limit");
            }
            
            // Debug: Log if stuck (not making progress)
            if (iterationCount % 1000 == 0) {
                System.out.println("    Iteration " + iterationCount + ": distance=" + distanceTraveledM + 
                                 " m, velocity=" + currentVelocityMs + " m/s, time=" + totalTimeS + " s");
            }
        }
        
        if (iterationCount >= maxIterations) {
            System.err.println("WARNING: Maximum iterations reached. distanceTraveled=" + distanceTraveledM + 
                             " m, segmentLength=" + segmentLengthM + " m, totalTime=" + totalTimeS + " s");
        }
        
        if (totalTimeS == 0.0 && distanceTraveledM < segmentLengthM) {
            System.err.println("ERROR: Travel time is 0 but distance not covered. " +
                             "distanceTraveled=" + distanceTraveledM + " m, segmentLength=" + segmentLengthM + " m");
            System.err.println("  This usually means the train cannot move (zero or negative net force)");
        }
        
        // Convert final velocity to km/h
        double finalVelocityKmH = currentVelocityMs * 3.6;
        
        // Convert time to hours
        double travelTimeHours = totalTimeS / 3600.0;
        
        System.out.println("    Result: travelTime=" + travelTimeHours + " hours (" + totalTimeS + " s), " +
                         "finalVelocity=" + finalVelocityKmH + " km/h, iterations=" + iterationCount);
        
        return new SegmentCalculationResult(finalVelocityKmH, travelTimeHours, segmentLengthM / 1000.0);
    }
    
    /**
     * Calculates the total power of all locomotives in the train.
     * 
     * @param train the train
     * @return total power in kilowatts
     */
    private double calculateTotalPower(Train train) {
        // Use the method from Train class that calculates total power
        return train.getTotalPowerKw();
    }
    
    /**
     * Calculates traction force from power and velocity.
     * F_traction = P / v (with limitation at low speeds)
     * 
     * @param powerKw total power in kilowatts
     * @param velocityMs velocity in m/s
     * @return traction force in Newtons
     */
    private double calculateTractionForce(double powerKw, double velocityMs) {
        if (velocityMs < 0.1) {
            // At very low speeds, traction force is limited by adhesion
            // Use a maximum force approximation: F_max ≈ 0.25 * m * g (typical for steel on steel)
            // For now, use a large but reasonable force to allow starting
            // In practice, this would be limited by wheel-rail adhesion coefficient
            double powerW = powerKw * 1000.0;
            return powerW / 0.1; // Use minimum velocity for calculation
        }
        
        // F = P / v (power = force * velocity)
        double powerW = powerKw * 1000.0; // Convert to watts
        return powerW / velocityMs;
    }
    
    /**
     * Calculates rolling resistance force.
     * F_rolling = c_r * m * g
     * 
     * @param massKg mass in kilograms
     * @return rolling resistance force in Newtons
     */
    private double calculateRollingResistance(double massKg) {
        return ROLLING_RESISTANCE_COEFFICIENT * massKg * GRAVITY_MS2;
    }
    
    /**
     * Calculates grade resistance force.
     * F_grade = m * g * sin(θ) ≈ m * g * (inclination / 1000)
     * For small angles: sin(θ) ≈ tan(θ) ≈ inclination_percent / 1000
     * 
     * @param massKg mass in kilograms
     * @param inclinationPercent inclination in percentage (e.g., 2.5 for 2.5%)
     * @return grade resistance force in Newtons (positive for uphill, negative for downhill)
     */
    private double calculateGradeResistance(double massKg, double inclinationPercent) {
        // Convert percentage to ratio per 1000 (2.5% = 0.025 = 25 per 1000)
        // For railway inclinations, we use the per mille (per 1000) convention
        // inclinationPercent is already in percentage, so we divide by 100 to get ratio
        double inclinationRatio = inclinationPercent / 100.0;
        return massKg * GRAVITY_MS2 * inclinationRatio;
    }
    
    /**
     * Calculates aerodynamic drag force.
     * F_drag = 0.5 * ρ * C_d * A * v²
     * 
     * @param velocityMs velocity in m/s
     * @return aerodynamic drag force in Newtons
     */
    private double calculateAerodynamicDrag(double velocityMs) {
        return 0.5 * AIR_DENSITY_KG_M3 * DRAG_COEFFICIENT * FRONTAL_AREA_M2 * velocityMs * velocityMs;
    }
    
    /**
     * Result of segment calculation.
     */
    public static class SegmentCalculationResult {
        private final double finalVelocityKmH;
        private final double travelTimeHours;
        private final double distanceKm;
        
        public SegmentCalculationResult(double finalVelocityKmH, double travelTimeHours, double distanceKm) {
            this.finalVelocityKmH = finalVelocityKmH;
            this.travelTimeHours = travelTimeHours;
            this.distanceKm = distanceKm;
        }
        
        public double getFinalVelocityKmH() {
            return finalVelocityKmH;
        }
        
        public double getTravelTimeHours() {
            return travelTimeHours;
        }
        
        public double getDistanceKm() {
            return distanceKm;
        }
    }
}

