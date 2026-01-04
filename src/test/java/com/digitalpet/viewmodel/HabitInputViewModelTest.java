package com.digitalpet.viewmodel;

import com.digitalpet.model.DailyHabits;
import com.digitalpet.model.DigitalPet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for HabitInputViewModel validation logic.
 * Tests edge cases and boundary conditions for all six habit categories.
 * 
 * Requirements addressed:
 * - 3.4: Range validation with error messaging for invalid inputs
 */
class HabitInputViewModelTest {
    
    private HabitInputViewModel viewModel;
    private DigitalPet testPet;
    
    @BeforeEach
    void setUp() {
        testPet = new DigitalPet("Test Pet");
        viewModel = new HabitInputViewModel(testPet);
    }
    
    // Study Hours Validation Tests
    
    @Test
    @DisplayName("Study hours - valid range (0-16) should not produce errors")
    void studyHours_validRange_noErrors() {
        // Test boundary values and middle values
        int[] validValues = {0, 1, 8, 15, 16};
        
        for (int value : validValues) {
            viewModel.setStudyHours(value);
            assertTrue(viewModel.studyHoursErrorProperty().get().isEmpty(),
                    "Study hours " + value + " should be valid");
        }
    }
    
    @ParameterizedTest
    @ValueSource(doubles = {-1, -0.1, 16.1, 17, 25, 100})
    @DisplayName("Study hours - invalid values should produce error messages")
    void studyHours_invalidValues_producesErrors(double invalidValue) {
        viewModel.setStudyHours(invalidValue);
        assertFalse(viewModel.studyHoursErrorProperty().get().isEmpty(),
                "Study hours " + invalidValue + " should produce an error message");
        
        if (invalidValue < 0) {
            assertTrue(viewModel.studyHoursErrorProperty().get().contains("cannot be negative"),
                    "Negative study hours should mention 'cannot be negative'");
        } else {
            assertTrue(viewModel.studyHoursErrorProperty().get().contains("cannot exceed 16"),
                    "Excessive study hours should mention 'cannot exceed 16'");
        }
    }
    
    // Water Intake Validation Tests
    
    @Test
    @DisplayName("Water intake - valid range (0-5.0) should not produce errors")
    void waterIntake_validRange_noErrors() {
        double[] validValues = {0.0, 0.1, 2.5, 4.9, 5.0};
        
        for (double value : validValues) {
            viewModel.setWaterIntake(value);
            assertTrue(viewModel.waterIntakeErrorProperty().get().isEmpty(),
                    "Water intake " + value + " should be valid");
        }
    }
    
    @ParameterizedTest
    @ValueSource(doubles = {-1.0, -0.1, 5.1, 6.0, 10.0})
    @DisplayName("Water intake - invalid values should produce error messages")
    void waterIntake_invalidValues_producesErrors(double invalidValue) {
        viewModel.setWaterIntake(invalidValue);
        assertFalse(viewModel.waterIntakeErrorProperty().get().isEmpty(),
                "Water intake " + invalidValue + " should produce an error message");
        
        if (invalidValue < 0) {
            assertTrue(viewModel.waterIntakeErrorProperty().get().contains("cannot be negative"),
                    "Negative water intake should mention 'cannot be negative'");
        } else {
            assertTrue(viewModel.waterIntakeErrorProperty().get().contains("cannot exceed 5.0"),
                    "Excessive water intake should mention 'cannot exceed 5.0'");
        }
    }
    
    // Steps Taken Validation Tests
    
    @Test
    @DisplayName("Steps taken - valid range (0-50000) should not produce errors")
    void stepsTaken_validRange_noErrors() {
        double[] validValues = {0, 1, 10000, 49999, 50000};
        
        for (double value : validValues) {
            viewModel.setStepsTaken(value);
            assertTrue(viewModel.stepsTakenErrorProperty().get().isEmpty(),
                    "Steps taken " + value + " should be valid");
        }
    }
    
    @ParameterizedTest
    @ValueSource(doubles = {-1, -100, 50001, 60000, 100000})
    @DisplayName("Steps taken - invalid values should produce error messages")
    void stepsTaken_invalidValues_producesErrors(double invalidValue) {
        viewModel.setStepsTaken(invalidValue);
        assertFalse(viewModel.stepsTakenErrorProperty().get().isEmpty(),
                "Steps taken " + invalidValue + " should produce an error message");
        
        if (invalidValue < 0) {
            assertTrue(viewModel.stepsTakenErrorProperty().get().contains("cannot be negative"),
                    "Negative steps should mention 'cannot be negative'");
        } else {
            assertTrue(viewModel.stepsTakenErrorProperty().get().contains("cannot exceed 50,000"),
                    "Excessive steps should mention 'cannot exceed 50,000'");
        }
    }
    
    // Sleep Hours Validation Tests
    
    @Test
    @DisplayName("Sleep hours - valid range (0-24.0) should not produce errors")
    void sleepHours_validRange_noErrors() {
        double[] validValues = {0.0, 0.5, 8.0, 23.5, 24.0};
        
        for (double value : validValues) {
            viewModel.setSleepHours(value);
            assertTrue(viewModel.sleepHoursErrorProperty().get().isEmpty(),
                    "Sleep hours " + value + " should be valid");
        }
    }
    
    @ParameterizedTest
    @ValueSource(doubles = {-1.0, -0.5, 24.1, 25.0, 30.0})
    @DisplayName("Sleep hours - invalid values should produce error messages")
    void sleepHours_invalidValues_producesErrors(double invalidValue) {
        viewModel.setSleepHours(invalidValue);
        assertFalse(viewModel.sleepHoursErrorProperty().get().isEmpty(),
                "Sleep hours " + invalidValue + " should produce an error message");
        
        if (invalidValue < 0) {
            assertTrue(viewModel.sleepHoursErrorProperty().get().contains("cannot be negative"),
                    "Negative sleep hours should mention 'cannot be negative'");
        } else {
            assertTrue(viewModel.sleepHoursErrorProperty().get().contains("cannot exceed 24"),
                    "Excessive sleep hours should mention 'cannot exceed 24'");
        }
    }
    
    // Money Spent Validation Tests
    
    @Test
    @DisplayName("Money spent - valid range (0-1000.0) should not produce errors")
    void moneySpent_validRange_noErrors() {
        double[] validValues = {0.0, 0.01, 50.0, 999.99, 1000.0};
        
        for (double value : validValues) {
            viewModel.setMoneySpent(value);
            assertTrue(viewModel.moneySpentErrorProperty().get().isEmpty(),
                    "Money spent " + value + " should be valid");
        }
    }
    
    @ParameterizedTest
    @ValueSource(doubles = {-1.0, -0.01, 1000.01, 1500.0, 5000.0})
    @DisplayName("Money spent - invalid values should produce error messages")
    void moneySpent_invalidValues_producesErrors(double invalidValue) {
        viewModel.setMoneySpent(invalidValue);
        assertFalse(viewModel.moneySpentErrorProperty().get().isEmpty(),
                "Money spent " + invalidValue + " should produce an error message");
        
        if (invalidValue < 0) {
            assertTrue(viewModel.moneySpentErrorProperty().get().contains("cannot be negative"),
                    "Negative money spent should mention 'cannot be negative'");
        } else {
            assertTrue(viewModel.moneySpentErrorProperty().get().contains("cannot exceed $1,000"),
                    "Excessive money spent should mention 'cannot exceed $1,000'");
        }
    }
    
    // Goals Completed Validation Tests
    
    @Test
    @DisplayName("Goals completed - valid range (0-10) should not produce errors")
    void goalsCompleted_validRange_noErrors() {
        double[] validValues = {0, 1, 5, 9, 10};
        
        for (double value : validValues) {
            viewModel.setGoalsCompleted(value);
            assertTrue(viewModel.goalsCompletedErrorProperty().get().isEmpty(),
                    "Goals completed " + value + " should be valid");
        }
    }
    
    @ParameterizedTest
    @ValueSource(doubles = {-1, -5, 11, 15, 100})
    @DisplayName("Goals completed - invalid values should produce error messages")
    void goalsCompleted_invalidValues_producesErrors(double invalidValue) {
        viewModel.setGoalsCompleted(invalidValue);
        assertFalse(viewModel.goalsCompletedErrorProperty().get().isEmpty(),
                "Goals completed " + invalidValue + " should produce an error message");
        
        if (invalidValue < 0) {
            assertTrue(viewModel.goalsCompletedErrorProperty().get().contains("cannot be negative"),
                    "Negative goals should mention 'cannot be negative'");
        } else {
            assertTrue(viewModel.goalsCompletedErrorProperty().get().contains("cannot exceed 10"),
                    "Excessive goals should mention 'cannot exceed 10'");
        }
    }
    
    // Overall Validation State Tests
    
    @Test
    @DisplayName("Overall validation - all valid values should result in no validation errors")
    void overallValidation_allValidValues_noErrors() {
        // Set all values to valid ranges
        viewModel.setStudyHours(8);
        viewModel.setWaterIntake(2.5);
        viewModel.setStepsTaken(10000);
        viewModel.setSleepHours(8.0);
        viewModel.setMoneySpent(50.0);
        viewModel.setGoalsCompleted(5);
        
        assertFalse(viewModel.hasValidationErrors(),
                "All valid values should result in no validation errors");
    }
    
    @Test
    @DisplayName("Overall validation - any invalid value should result in validation errors")
    void overallValidation_anyInvalidValue_hasErrors() {
        // Set all values to valid ranges first
        viewModel.setStudyHours(8);
        viewModel.setWaterIntake(2.5);
        viewModel.setStepsTaken(10000);
        viewModel.setSleepHours(8.0);
        viewModel.setMoneySpent(50.0);
        viewModel.setGoalsCompleted(5);
        
        assertFalse(viewModel.hasValidationErrors(), "Should start with no errors");
        
        // Test each invalid value individually
        viewModel.setStudyHours(-1);
        assertTrue(viewModel.hasValidationErrors(), "Invalid study hours should cause validation error");
        
        viewModel.setStudyHours(8); // Reset to valid
        viewModel.setWaterIntake(-1);
        assertTrue(viewModel.hasValidationErrors(), "Invalid water intake should cause validation error");
        
        viewModel.setWaterIntake(2.5); // Reset to valid
        viewModel.setStepsTaken(-1);
        assertTrue(viewModel.hasValidationErrors(), "Invalid steps should cause validation error");
        
        viewModel.setStepsTaken(10000); // Reset to valid
        viewModel.setSleepHours(-1);
        assertTrue(viewModel.hasValidationErrors(), "Invalid sleep hours should cause validation error");
        
        viewModel.setSleepHours(8.0); // Reset to valid
        viewModel.setMoneySpent(-1);
        assertTrue(viewModel.hasValidationErrors(), "Invalid money spent should cause validation error");
        
        viewModel.setMoneySpent(50.0); // Reset to valid
        viewModel.setGoalsCompleted(-1);
        assertTrue(viewModel.hasValidationErrors(), "Invalid goals should cause validation error");
    }
    
    // Edge Case Tests
    
    @Test
    @DisplayName("Edge case - boundary values should be handled correctly")
    void edgeCase_boundaryValues_handledCorrectly() {
        // Test exact boundary values
        viewModel.setStudyHours(0);
        viewModel.setWaterIntake(0.0);
        viewModel.setStepsTaken(0);
        viewModel.setSleepHours(0.0);
        viewModel.setMoneySpent(0.0);
        viewModel.setGoalsCompleted(0);
        
        assertFalse(viewModel.hasValidationErrors(), "Minimum boundary values should be valid");
        
        // Test maximum boundary values
        viewModel.setStudyHours(16);
        viewModel.setWaterIntake(5.0);
        viewModel.setStepsTaken(50000);
        viewModel.setSleepHours(24.0);
        viewModel.setMoneySpent(1000.0);
        viewModel.setGoalsCompleted(10);
        
        assertFalse(viewModel.hasValidationErrors(), "Maximum boundary values should be valid");
    }
    
    @Test
    @DisplayName("Edge case - just outside boundary values should produce errors")
    void edgeCase_justOutsideBoundaries_producesErrors() {
        // Test just below minimum (negative values)
        viewModel.setStudyHours(-0.1);
        assertTrue(viewModel.hasValidationErrors(), "Just below minimum should produce error");
        
        viewModel.setStudyHours(0); // Reset
        
        // Test just above maximum
        viewModel.setStudyHours(16.1);
        assertTrue(viewModel.hasValidationErrors(), "Just above maximum should produce error");
    }
    
    // Submission State Tests
    
    @Test
    @DisplayName("Submission - should be disabled when validation errors exist")
    void submission_disabledWithValidationErrors() {
        // Set invalid value
        viewModel.setStudyHours(-1);
        
        assertTrue(viewModel.hasValidationErrors(), "Should have validation errors");
        
        // Attempt submission should not proceed (tested by checking state)
        assertFalse(viewModel.isSubmitting(), "Should not be in submitting state with validation errors");
    }
    
    @Test
    @DisplayName("Submission - should be enabled when all values are valid")
    void submission_enabledWithValidValues() {
        // Set all valid values
        viewModel.setStudyHours(8);
        viewModel.setWaterIntake(2.5);
        viewModel.setStepsTaken(10000);
        viewModel.setSleepHours(8.0);
        viewModel.setMoneySpent(50.0);
        viewModel.setGoalsCompleted(5);
        
        assertFalse(viewModel.hasValidationErrors(), "Should have no validation errors");
        assertFalse(viewModel.isSubmitting(), "Should not be in submitting state initially");
    }
    
    // Message Clearing Tests
    
    @Test
    @DisplayName("Message clearing - should clear all feedback messages")
    void messageCleaning_clearsAllFeedback() {
        // Manually set some messages (simulating post-submission state)
        viewModel.confirmationMessageProperty().set("Test confirmation");
        viewModel.showConfirmationProperty().set(true);
        viewModel.xpGainMessageProperty().set("Test XP gain");
        viewModel.showXpGainProperty().set(true);
        
        // Clear messages
        viewModel.clearMessages();
        
        // Verify all messages are cleared
        assertTrue(viewModel.confirmationMessageProperty().get().isEmpty(), "Confirmation message should be cleared");
        assertFalse(viewModel.showConfirmationProperty().get(), "Confirmation visibility should be false");
        assertTrue(viewModel.xpGainMessageProperty().get().isEmpty(), "XP gain message should be cleared");
        assertFalse(viewModel.showXpGainProperty().get(), "XP gain visibility should be false");
    }
    
    // Pet Integration Tests
    
    @Test
    @DisplayName("Pet integration - should maintain reference to current pet")
    void petIntegration_maintainsReference() {
        assertEquals(testPet, viewModel.getCurrentPet(), "Should maintain reference to current pet");
        
        // Test setting new pet
        DigitalPet newPet = new DigitalPet("New Pet");
        viewModel.setCurrentPet(newPet);
        
        assertEquals(newPet, viewModel.getCurrentPet(), "Should update to new pet reference");
    }
    
    // Habit History Tests
    
    @Test
    @DisplayName("Habit history - should manage habit history correctly")
    void habitHistory_managedCorrectly() {
        assertTrue(viewModel.getHabitHistory().isEmpty(), "Should start with empty habit history");
        
        // Add some habit history
        DailyHabits habits1 = new DailyHabits();
        habits1.setStudyHours(8);
        
        DailyHabits habits2 = new DailyHabits();
        habits2.setStudyHours(6);
        
        java.util.List<DailyHabits> history = java.util.List.of(habits1, habits2);
        viewModel.setHabitHistory(history);
        
        assertEquals(2, viewModel.getHabitHistory().size(), "Should contain added habit history");
        assertEquals(8, viewModel.getHabitHistory().get(0).getStudyHours(), "Should maintain habit data correctly");
    }
}