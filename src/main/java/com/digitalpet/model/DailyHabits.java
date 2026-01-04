package com.digitalpet.model;

import javafx.beans.property.*;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * Model representing daily habit tracking data with JavaFX properties for reactive UI binding.
 * Manages six habit categories with validation and range constraints.
 * 
 * Requirements addressed:
 * - 3.1: Six habit categories with specified ranges
 * - 3.4: Input validation with range checking and error messaging
 */
public class DailyHabits {
    
    // Habit tracking properties with validation ranges
    private final IntegerProperty studyHours;      // Range: 0-16
    private final DoubleProperty waterIntake;      // Range: 0-5.0 liters
    private final IntegerProperty stepsTaken;      // Range: 0-50000
    private final DoubleProperty sleepHours;       // Range: 0-24.0
    private final DoubleProperty moneySpent;       // Range: 0-1000.0
    private final IntegerProperty goalsCompleted;  // Range: 0-10
    
    // Date tracking
    private final ObjectProperty<LocalDate> date;
    
    // Validation ranges (min, max)
    public static final Map<String, double[]> VALIDATION_RANGES = Map.of(
        "studyHours", new double[]{0, 16},
        "waterIntake", new double[]{0, 5.0},
        "stepsTaken", new double[]{0, 50000},
        "sleepHours", new double[]{0, 24.0},
        "moneySpent", new double[]{0, 1000.0},
        "goalsCompleted", new double[]{0, 10}
    );
    
    // Validation error messages
    private final Map<String, String> validationErrors;
    
    /**
     * Creates a new DailyHabits instance for today's date
     */
    public DailyHabits() {
        this(LocalDate.now());
    }
    
    /**
     * Creates a new DailyHabits instance for the specified date
     * @param date The date for these habits
     */
    public DailyHabits(LocalDate date) {
        this.studyHours = new SimpleIntegerProperty(this, "studyHours", 0);
        this.waterIntake = new SimpleDoubleProperty(this, "waterIntake", 0.0);
        this.stepsTaken = new SimpleIntegerProperty(this, "stepsTaken", 0);
        this.sleepHours = new SimpleDoubleProperty(this, "sleepHours", 0.0);
        this.moneySpent = new SimpleDoubleProperty(this, "moneySpent", 0.0);
        this.goalsCompleted = new SimpleIntegerProperty(this, "goalsCompleted", 0);
        this.date = new SimpleObjectProperty<>(this, "date", date != null ? date : LocalDate.now());
        
        this.validationErrors = new HashMap<>();
        
        // Set up validation listeners
        setupValidation();
    }
    
    /**
     * Sets up property validation listeners for all habit categories
     */
    private void setupValidation() {
        // Study hours validation (0-16)
        studyHours.addListener((observable, oldValue, newValue) -> {
            validateIntegerRange("studyHours", newValue.intValue(), 0, 16);
        });
        
        // Water intake validation (0-5.0)
        waterIntake.addListener((observable, oldValue, newValue) -> {
            validateDoubleRange("waterIntake", newValue.doubleValue(), 0.0, 5.0);
        });
        
        // Steps taken validation (0-50000)
        stepsTaken.addListener((observable, oldValue, newValue) -> {
            validateIntegerRange("stepsTaken", newValue.intValue(), 0, 50000);
        });
        
        // Sleep hours validation (0-24.0)
        sleepHours.addListener((observable, oldValue, newValue) -> {
            validateDoubleRange("sleepHours", newValue.doubleValue(), 0.0, 24.0);
        });
        
        // Money spent validation (0-1000.0)
        moneySpent.addListener((observable, oldValue, newValue) -> {
            validateDoubleRange("moneySpent", newValue.doubleValue(), 0.0, 1000.0);
        });
        
        // Goals completed validation (0-10)
        goalsCompleted.addListener((observable, oldValue, newValue) -> {
            validateIntegerRange("goalsCompleted", newValue.intValue(), 0, 10);
        });
    }
    
    /**
     * Validates integer values within specified range
     */
    private void validateIntegerRange(String propertyName, int value, int min, int max) {
        if (value < min) {
            setPropertyValue(propertyName, min);
            validationErrors.put(propertyName, 
                String.format("%s cannot be less than %d", formatPropertyName(propertyName), min));
        } else if (value > max) {
            setPropertyValue(propertyName, max);
            validationErrors.put(propertyName, 
                String.format("%s cannot be greater than %d", formatPropertyName(propertyName), max));
        } else {
            validationErrors.remove(propertyName);
        }
    }
    
    /**
     * Validates double values within specified range
     */
    private void validateDoubleRange(String propertyName, double value, double min, double max) {
        if (value < min) {
            setPropertyValue(propertyName, min);
            validationErrors.put(propertyName, 
                String.format("%s cannot be less than %.1f", formatPropertyName(propertyName), min));
        } else if (value > max) {
            setPropertyValue(propertyName, max);
            validationErrors.put(propertyName, 
                String.format("%s cannot be greater than %.1f", formatPropertyName(propertyName), max));
        } else {
            validationErrors.remove(propertyName);
        }
    }
    
    /**
     * Sets property value without triggering validation listeners
     */
    private void setPropertyValue(String propertyName, Number value) {
        switch (propertyName) {
            case "studyHours" -> studyHours.set(value.intValue());
            case "stepsTaken" -> stepsTaken.set(value.intValue());
            case "goalsCompleted" -> goalsCompleted.set(value.intValue());
            case "waterIntake" -> waterIntake.set(value.doubleValue());
            case "sleepHours" -> sleepHours.set(value.doubleValue());
            case "moneySpent" -> moneySpent.set(value.doubleValue());
        }
    }
    
    /**
     * Formats property names for user-friendly error messages
     */
    private String formatPropertyName(String propertyName) {
        return switch (propertyName) {
            case "studyHours" -> "Study hours";
            case "waterIntake" -> "Water intake";
            case "stepsTaken" -> "Steps taken";
            case "sleepHours" -> "Sleep hours";
            case "moneySpent" -> "Money spent";
            case "goalsCompleted" -> "Goals completed";
            default -> propertyName;
        };
    }
    
    // Property getters for JavaFX binding
    
    public IntegerProperty studyHoursProperty() {
        return studyHours;
    }
    
    public DoubleProperty waterIntakeProperty() {
        return waterIntake;
    }
    
    public IntegerProperty stepsTakenProperty() {
        return stepsTaken;
    }
    
    public DoubleProperty sleepHoursProperty() {
        return sleepHours;
    }
    
    public DoubleProperty moneySpentProperty() {
        return moneySpent;
    }
    
    public IntegerProperty goalsCompletedProperty() {
        return goalsCompleted;
    }
    
    public ObjectProperty<LocalDate> dateProperty() {
        return date;
    }
    
    // Convenience getters
    
    public int getStudyHours() {
        return studyHours.get();
    }
    
    public double getWaterIntake() {
        return waterIntake.get();
    }
    
    public int getStepsTaken() {
        return stepsTaken.get();
    }
    
    public double getSleepHours() {
        return sleepHours.get();
    }
    
    public double getMoneySpent() {
        return moneySpent.get();
    }
    
    public int getGoalsCompleted() {
        return goalsCompleted.get();
    }
    
    public LocalDate getDate() {
        return date.get();
    }
    
    // Convenience setters with validation
    
    public void setStudyHours(int hours) {
        this.studyHours.set(hours);
    }
    
    public void setWaterIntake(double liters) {
        this.waterIntake.set(liters);
    }
    
    public void setStepsTaken(int steps) {
        this.stepsTaken.set(steps);
    }
    
    public void setSleepHours(double hours) {
        this.sleepHours.set(hours);
    }
    
    public void setMoneySpent(double amount) {
        this.moneySpent.set(amount);
    }
    
    public void setGoalsCompleted(int goals) {
        this.goalsCompleted.set(goals);
    }
    
    public void setDate(LocalDate date) {
        if (date != null) {
            this.date.set(date);
        }
    }
    
    // Validation methods
    
    /**
     * Checks if all habit values are within valid ranges
     * @return true if all values are valid
     */
    public boolean isValid() {
        return validationErrors.isEmpty();
    }
    
    /**
     * Gets validation error messages
     * @return Map of property names to error messages
     */
    public Map<String, String> getValidationErrors() {
        return new HashMap<>(validationErrors);
    }
    
    /**
     * Gets validation error for specific property
     * @param propertyName The property to check
     * @return Error message or null if valid
     */
    public String getValidationError(String propertyName) {
        return validationErrors.get(propertyName);
    }
    
    /**
     * Validates a specific habit value against its range
     * @param habitName The habit category name
     * @param value The value to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidHabitValue(String habitName, double value) {
        double[] range = VALIDATION_RANGES.get(habitName);
        if (range == null) {
            return false;
        }
        return value >= range[0] && value <= range[1];
    }
    
    // Utility methods
    
    /**
     * Calculates completion percentage based on reasonable daily targets
     * @return Completion percentage (0.0 to 1.0)
     */
    public double getCompletionPercentage() {
        // Define reasonable daily targets for percentage calculation
        double studyTarget = 8.0;      // 8 hours study
        double waterTarget = 2.5;      // 2.5 liters water
        double stepsTarget = 10000;    // 10k steps
        double sleepTarget = 8.0;      // 8 hours sleep
        double spendingTarget = 50.0;  // $50 spending (lower is better, so invert)
        double goalsTarget = 5.0;      // 5 goals
        
        double studyScore = Math.min(1.0, getStudyHours() / studyTarget);
        double waterScore = Math.min(1.0, getWaterIntake() / waterTarget);
        double stepsScore = Math.min(1.0, getStepsTaken() / stepsTarget);
        double sleepScore = Math.min(1.0, getSleepHours() / sleepTarget);
        double spendingScore = getMoneySpent() <= spendingTarget ? 1.0 : Math.max(0.0, 1.0 - (getMoneySpent() - spendingTarget) / spendingTarget);
        double goalsScore = Math.min(1.0, getGoalsCompleted() / goalsTarget);
        
        return (studyScore + waterScore + stepsScore + sleepScore + spendingScore + goalsScore) / 6.0;
    }
    
    /**
     * Gets habit values as a map for easy iteration
     * @return Map of habit names to values
     */
    public Map<String, Number> getHabitValues() {
        Map<String, Number> values = new HashMap<>();
        values.put("studyHours", getStudyHours());
        values.put("waterIntake", getWaterIntake());
        values.put("stepsTaken", getStepsTaken());
        values.put("sleepHours", getSleepHours());
        values.put("moneySpent", getMoneySpent());
        values.put("goalsCompleted", getGoalsCompleted());
        return values;
    }
    
    /**
     * Resets all habit values to zero
     */
    public void reset() {
        setStudyHours(0);
        setWaterIntake(0.0);
        setStepsTaken(0);
        setSleepHours(0.0);
        setMoneySpent(0.0);
        setGoalsCompleted(0);
        validationErrors.clear();
    }
    
    @Override
    public String toString() {
        return String.format("DailyHabits{date=%s, study=%dh, water=%.1fL, steps=%d, sleep=%.1fh, spent=$%.2f, goals=%d}", 
                getDate(), getStudyHours(), getWaterIntake(), getStepsTaken(), 
                getSleepHours(), getMoneySpent(), getGoalsCompleted());
    }
}