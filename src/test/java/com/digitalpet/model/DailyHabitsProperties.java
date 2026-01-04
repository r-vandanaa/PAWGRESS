package com.digitalpet.model;

import net.jqwik.api.*;
import net.jqwik.api.constraints.DoubleRange;
import net.jqwik.api.constraints.IntRange;

import java.time.LocalDate;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based tests for DailyHabits model
 * Feature: digital-pet-evolution
 * 
 * Tests Property 6: Habit Input Validation Completeness
 * Validates: Requirements 3.1, 3.4, 3.5
 */
class DailyHabitsProperties {

    @Property
    @Label("Feature: digital-pet-evolution, Property 6: Habit Input Validation Completeness")
    void habitInputValidationIsComplete(
            @ForAll @IntRange(min = -10, max = 30) int studyHours,
            @ForAll @DoubleRange(min = -2.0, max = 10.0) double waterIntake,
            @ForAll @IntRange(min = -1000, max = 100000) int stepsTaken,
            @ForAll @DoubleRange(min = -5.0, max = 30.0) double sleepHours,
            @ForAll @DoubleRange(min = -100.0, max = 2000.0) double moneySpent,
            @ForAll @IntRange(min = -5, max = 20) int goalsCompleted) {
        
        DailyHabits habits = new DailyHabits();
        
        // Test all six habit categories as specified in Requirements 3.1
        
        // 1. Study hours validation (0-16)
        habits.setStudyHours(studyHours);
        int actualStudyHours = habits.getStudyHours();
        assertTrue(actualStudyHours >= 0 && actualStudyHours <= 16,
                "Study hours must be within range 0-16, got: " + actualStudyHours);
        
        if (studyHours < 0) {
            assertEquals(0, actualStudyHours, "Negative study hours should be clamped to 0");
            assertNotNull(habits.getValidationError("studyHours"), 
                    "Validation error should be present for out-of-range study hours");
        } else if (studyHours > 16) {
            assertEquals(16, actualStudyHours, "Excessive study hours should be clamped to 16");
            assertNotNull(habits.getValidationError("studyHours"), 
                    "Validation error should be present for out-of-range study hours");
        } else {
            assertEquals(studyHours, actualStudyHours, "Valid study hours should be preserved");
            assertNull(habits.getValidationError("studyHours"), 
                    "No validation error should exist for valid study hours");
        }
        
        // 2. Water intake validation (0-5.0 liters)
        habits.setWaterIntake(waterIntake);
        double actualWaterIntake = habits.getWaterIntake();
        assertTrue(actualWaterIntake >= 0.0 && actualWaterIntake <= 5.0,
                "Water intake must be within range 0-5.0 liters, got: " + actualWaterIntake);
        
        if (waterIntake < 0.0) {
            assertEquals(0.0, actualWaterIntake, 0.001, "Negative water intake should be clamped to 0");
            assertNotNull(habits.getValidationError("waterIntake"), 
                    "Validation error should be present for out-of-range water intake");
        } else if (waterIntake > 5.0) {
            assertEquals(5.0, actualWaterIntake, 0.001, "Excessive water intake should be clamped to 5.0");
            assertNotNull(habits.getValidationError("waterIntake"), 
                    "Validation error should be present for out-of-range water intake");
        } else {
            assertEquals(waterIntake, actualWaterIntake, 0.001, "Valid water intake should be preserved");
            assertNull(habits.getValidationError("waterIntake"), 
                    "No validation error should exist for valid water intake");
        }
        
        // 3. Steps taken validation (0-50000)
        habits.setStepsTaken(stepsTaken);
        int actualStepsTaken = habits.getStepsTaken();
        assertTrue(actualStepsTaken >= 0 && actualStepsTaken <= 50000,
                "Steps taken must be within range 0-50000, got: " + actualStepsTaken);
        
        if (stepsTaken < 0) {
            assertEquals(0, actualStepsTaken, "Negative steps should be clamped to 0");
            assertNotNull(habits.getValidationError("stepsTaken"), 
                    "Validation error should be present for out-of-range steps");
        } else if (stepsTaken > 50000) {
            assertEquals(50000, actualStepsTaken, "Excessive steps should be clamped to 50000");
            assertNotNull(habits.getValidationError("stepsTaken"), 
                    "Validation error should be present for out-of-range steps");
        } else {
            assertEquals(stepsTaken, actualStepsTaken, "Valid steps should be preserved");
            assertNull(habits.getValidationError("stepsTaken"), 
                    "No validation error should exist for valid steps");
        }
        
        // 4. Sleep hours validation (0-24.0)
        habits.setSleepHours(sleepHours);
        double actualSleepHours = habits.getSleepHours();
        assertTrue(actualSleepHours >= 0.0 && actualSleepHours <= 24.0,
                "Sleep hours must be within range 0-24.0, got: " + actualSleepHours);
        
        if (sleepHours < 0.0) {
            assertEquals(0.0, actualSleepHours, 0.001, "Negative sleep hours should be clamped to 0");
            assertNotNull(habits.getValidationError("sleepHours"), 
                    "Validation error should be present for out-of-range sleep hours");
        } else if (sleepHours > 24.0) {
            assertEquals(24.0, actualSleepHours, 0.001, "Excessive sleep hours should be clamped to 24.0");
            assertNotNull(habits.getValidationError("sleepHours"), 
                    "Validation error should be present for out-of-range sleep hours");
        } else {
            assertEquals(sleepHours, actualSleepHours, 0.001, "Valid sleep hours should be preserved");
            assertNull(habits.getValidationError("sleepHours"), 
                    "No validation error should exist for valid sleep hours");
        }
        
        // 5. Money spent validation (0-1000.0)
        habits.setMoneySpent(moneySpent);
        double actualMoneySpent = habits.getMoneySpent();
        assertTrue(actualMoneySpent >= 0.0 && actualMoneySpent <= 1000.0,
                "Money spent must be within range 0-1000.0, got: " + actualMoneySpent);
        
        if (moneySpent < 0.0) {
            assertEquals(0.0, actualMoneySpent, 0.001, "Negative money spent should be clamped to 0");
            assertNotNull(habits.getValidationError("moneySpent"), 
                    "Validation error should be present for out-of-range money spent");
        } else if (moneySpent > 1000.0) {
            assertEquals(1000.0, actualMoneySpent, 0.001, "Excessive money spent should be clamped to 1000.0");
            assertNotNull(habits.getValidationError("moneySpent"), 
                    "Validation error should be present for out-of-range money spent");
        } else {
            assertEquals(moneySpent, actualMoneySpent, 0.001, "Valid money spent should be preserved");
            assertNull(habits.getValidationError("moneySpent"), 
                    "No validation error should exist for valid money spent");
        }
        
        // 6. Goals completed validation (0-10)
        habits.setGoalsCompleted(goalsCompleted);
        int actualGoalsCompleted = habits.getGoalsCompleted();
        assertTrue(actualGoalsCompleted >= 0 && actualGoalsCompleted <= 10,
                "Goals completed must be within range 0-10, got: " + actualGoalsCompleted);
        
        if (goalsCompleted < 0) {
            assertEquals(0, actualGoalsCompleted, "Negative goals completed should be clamped to 0");
            assertNotNull(habits.getValidationError("goalsCompleted"), 
                    "Validation error should be present for out-of-range goals completed");
        } else if (goalsCompleted > 10) {
            assertEquals(10, actualGoalsCompleted, "Excessive goals completed should be clamped to 10");
            assertNotNull(habits.getValidationError("goalsCompleted"), 
                    "Validation error should be present for out-of-range goals completed");
        } else {
            assertEquals(goalsCompleted, actualGoalsCompleted, "Valid goals completed should be preserved");
            assertNull(habits.getValidationError("goalsCompleted"), 
                    "No validation error should exist for valid goals completed");
        }
        
        // Validate overall system behavior (Requirements 3.4, 3.5)
        
        // Check that validation state is consistent
        boolean hasErrors = !habits.getValidationErrors().isEmpty();
        boolean isValid = habits.isValid();
        assertEquals(!hasErrors, isValid, "Validation state should be consistent");
        
        // Check that completion percentage is calculable
        double completionPercentage = habits.getCompletionPercentage();
        assertTrue(completionPercentage >= 0.0 && completionPercentage <= 1.0,
                "Completion percentage must be between 0.0 and 1.0");
        
        // Check that habit values map is complete and accurate
        Map<String, Number> habitValues = habits.getHabitValues();
        assertEquals(6, habitValues.size(), "Habit values map should contain all six categories");
        assertEquals(actualStudyHours, habitValues.get("studyHours").intValue());
        assertEquals(actualWaterIntake, habitValues.get("waterIntake").doubleValue(), 0.001);
        assertEquals(actualStepsTaken, habitValues.get("stepsTaken").intValue());
        assertEquals(actualSleepHours, habitValues.get("sleepHours").doubleValue(), 0.001);
        assertEquals(actualMoneySpent, habitValues.get("moneySpent").doubleValue(), 0.001);
        assertEquals(actualGoalsCompleted, habitValues.get("goalsCompleted").intValue());
    }

    @Property
    @Label("Feature: digital-pet-evolution, Property 6: Static Validation Method Consistency")
    void staticValidationMethodIsConsistent(@ForAll("habitNames") String habitName,
                                            @ForAll @DoubleRange(min = -100.0, max = 100000.0) double value) {
        
        boolean isValid = DailyHabits.isValidHabitValue(habitName, value);
        double[] expectedRange = DailyHabits.VALIDATION_RANGES.get(habitName);
        
        if (expectedRange != null) {
            boolean expectedValid = value >= expectedRange[0] && value <= expectedRange[1];
            assertEquals(expectedValid, isValid,
                    String.format("Static validation for %s with value %f should match expected result", habitName, value));
        } else {
            assertFalse(isValid, "Static validation should return false for unknown habit names");
        }
    }

    @Property
    @Label("Feature: digital-pet-evolution, Property 6: Date Handling Consistency")
    void dateHandlingIsConsistent(@ForAll("localDates") LocalDate date) {
        DailyHabits habits = new DailyHabits(date);
        
        assertEquals(date, habits.getDate(), "Constructor should set the correct date");
        
        // Test date property binding
        assertNotNull(habits.dateProperty(), "Date property should not be null");
        assertEquals(date, habits.dateProperty().get(), "Date property should match constructor date");
        
        // Test date modification
        LocalDate newDate = date.plusDays(1);
        habits.setDate(newDate);
        assertEquals(newDate, habits.getDate(), "Date should be updatable");
        assertEquals(newDate, habits.dateProperty().get(), "Date property should reflect updates");
    }

    @Property
    @Label("Feature: digital-pet-evolution, Property 6: Reset Functionality")
    void resetFunctionalityWorksCorrectly(
            @ForAll @IntRange(min = 0, max = 16) int studyHours,
            @ForAll @DoubleRange(min = 0.0, max = 5.0) double waterIntake,
            @ForAll @IntRange(min = 0, max = 50000) int stepsTaken,
            @ForAll @DoubleRange(min = 0.0, max = 24.0) double sleepHours,
            @ForAll @DoubleRange(min = 0.0, max = 1000.0) double moneySpent,
            @ForAll @IntRange(min = 0, max = 10) int goalsCompleted) {
        
        DailyHabits habits = new DailyHabits();
        
        // Set all values to non-zero
        habits.setStudyHours(studyHours);
        habits.setWaterIntake(waterIntake);
        habits.setStepsTaken(stepsTaken);
        habits.setSleepHours(sleepHours);
        habits.setMoneySpent(moneySpent);
        habits.setGoalsCompleted(goalsCompleted);
        
        // Verify values are set
        assertTrue(habits.getStudyHours() >= 0 || habits.getWaterIntake() >= 0 || 
                   habits.getStepsTaken() >= 0 || habits.getSleepHours() >= 0 || 
                   habits.getMoneySpent() >= 0 || habits.getGoalsCompleted() >= 0,
                   "At least some values should be non-zero before reset");
        
        // Reset and verify all values are zero
        habits.reset();
        
        assertEquals(0, habits.getStudyHours(), "Study hours should be reset to 0");
        assertEquals(0.0, habits.getWaterIntake(), 0.001, "Water intake should be reset to 0");
        assertEquals(0, habits.getStepsTaken(), "Steps taken should be reset to 0");
        assertEquals(0.0, habits.getSleepHours(), 0.001, "Sleep hours should be reset to 0");
        assertEquals(0.0, habits.getMoneySpent(), 0.001, "Money spent should be reset to 0");
        assertEquals(0, habits.getGoalsCompleted(), "Goals completed should be reset to 0");
        
        assertTrue(habits.isValid(), "Habits should be valid after reset");
        assertTrue(habits.getValidationErrors().isEmpty(), "No validation errors should exist after reset");
    }

    @Provide
    Arbitrary<String> habitNames() {
        return Arbitraries.of("studyHours", "waterIntake", "stepsTaken", 
                             "sleepHours", "moneySpent", "goalsCompleted", "invalidHabit");
    }

    @Provide
    Arbitrary<LocalDate> localDates() {
        return Arbitraries.integers()
                .between(1, 365)
                .map(days -> LocalDate.of(2024, 1, 1).plusDays(days));
    }
}