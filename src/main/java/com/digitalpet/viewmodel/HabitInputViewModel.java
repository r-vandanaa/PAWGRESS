package com.digitalpet.viewmodel;

import com.digitalpet.model.DailyHabits;
import com.digitalpet.model.DigitalPet;
import com.digitalpet.service.XPSystem;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.*;
import javafx.concurrent.Task;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * ViewModel for the habit input interface.
 * Handles real-time validation, submission logic, and XP calculation integration.
 * 
 * Requirements addressed:
 * - 3.3: Real-time validation using JavaFX binding
 * - 3.5: Submission handling with XP calculation integration
 * - 3.5: Confirmation feedback and animation triggers
 */
public class HabitInputViewModel {
    
    // Habit input properties
    private final DoubleProperty studyHours;
    private final DoubleProperty waterIntake;
    private final DoubleProperty stepsTaken;
    private final DoubleProperty sleepHours;
    private final DoubleProperty moneySpent;
    private final DoubleProperty goalsCompleted;
    
    // Validation error properties
    private final StringProperty studyHoursError;
    private final StringProperty waterIntakeError;
    private final StringProperty stepsTakenError;
    private final StringProperty sleepHoursError;
    private final StringProperty moneySpentError;
    private final StringProperty goalsCompletedError;
    
    // Validation state properties
    private BooleanProperty hasValidationErrors;
    private final BooleanProperty isSubmitting;
    
    // Feedback message properties
    private final StringProperty confirmationMessage;
    private final BooleanProperty showConfirmation;
    private final StringProperty xpGainMessage;
    private final BooleanProperty showXpGain;
    
    // Services and models
    private final XPSystem xpSystem;
    private DigitalPet currentPet;
    private final List<DailyHabits> habitHistory;
    
    public HabitInputViewModel() {
        // Initialize properties
        this.studyHours = new SimpleDoubleProperty(this, "studyHours", 0.0);
        this.waterIntake = new SimpleDoubleProperty(this, "waterIntake", 0.0);
        this.stepsTaken = new SimpleDoubleProperty(this, "stepsTaken", 0.0);
        this.sleepHours = new SimpleDoubleProperty(this, "sleepHours", 0.0);
        this.moneySpent = new SimpleDoubleProperty(this, "moneySpent", 0.0);
        this.goalsCompleted = new SimpleDoubleProperty(this, "goalsCompleted", 0.0);
        
        // Initialize error properties
        this.studyHoursError = new SimpleStringProperty(this, "studyHoursError", "");
        this.waterIntakeError = new SimpleStringProperty(this, "waterIntakeError", "");
        this.stepsTakenError = new SimpleStringProperty(this, "stepsTakenError", "");
        this.sleepHoursError = new SimpleStringProperty(this, "sleepHoursError", "");
        this.moneySpentError = new SimpleStringProperty(this, "moneySpentError", "");
        this.goalsCompletedError = new SimpleStringProperty(this, "goalsCompletedError", "");
        
        // Initialize state properties
        this.isSubmitting = new SimpleBooleanProperty(this, "isSubmitting", false);
        
        // Initialize feedback properties
        this.confirmationMessage = new SimpleStringProperty(this, "confirmationMessage", "");
        this.showConfirmation = new SimpleBooleanProperty(this, "showConfirmation", false);
        this.xpGainMessage = new SimpleStringProperty(this, "xpGainMessage", "");
        this.showXpGain = new SimpleBooleanProperty(this, "showXpGain", false);
        
        // Initialize services
        this.xpSystem = new XPSystem();
        this.habitHistory = new ArrayList<>();
        
        // Set up validation bindings
        setupValidation();
        
        // Initialize with default pet for testing
        this.currentPet = new DigitalPet("Test Pet");
    }
    
    /**
     * Constructor with pet injection
     * @param pet The digital pet to track habits for
     */
    public HabitInputViewModel(DigitalPet pet) {
        this();
        this.currentPet = pet;
    }
    
    /**
     * Sets up real-time validation using JavaFX binding
     */
    private void setupValidation() {
        // Study hours validation (0-16)
        studyHours.addListener((observable, oldValue, newValue) -> {
            validateStudyHours(newValue.doubleValue());
        });
        
        // Water intake validation (0-5.0)
        waterIntake.addListener((observable, oldValue, newValue) -> {
            validateWaterIntake(newValue.doubleValue());
        });
        
        // Steps taken validation (0-50000)
        stepsTaken.addListener((observable, oldValue, newValue) -> {
            validateStepsTaken(newValue.doubleValue());
        });
        
        // Sleep hours validation (0-24.0)
        sleepHours.addListener((observable, oldValue, newValue) -> {
            validateSleepHours(newValue.doubleValue());
        });
        
        // Money spent validation (0-1000.0)
        moneySpent.addListener((observable, oldValue, newValue) -> {
            validateMoneySpent(newValue.doubleValue());
        });
        
        // Goals completed validation (0-10)
        goalsCompleted.addListener((observable, oldValue, newValue) -> {
            validateGoalsCompleted(newValue.doubleValue());
        });
        
        // Create binding for overall validation state
        BooleanBinding hasErrors = Bindings.createBooleanBinding(() -> {
            return !studyHoursError.get().isEmpty() ||
                   !waterIntakeError.get().isEmpty() ||
                   !stepsTakenError.get().isEmpty() ||
                   !sleepHoursError.get().isEmpty() ||
                   !moneySpentError.get().isEmpty() ||
                   !goalsCompletedError.get().isEmpty();
        }, studyHoursError, waterIntakeError, stepsTakenError, 
           sleepHoursError, moneySpentError, goalsCompletedError);
        
        this.hasValidationErrors = new SimpleBooleanProperty();
        hasValidationErrors.bind(hasErrors);
    }
    
    /**
     * Validates study hours input
     */
    private void validateStudyHours(double value) {
        if (value < 0) {
            studyHoursError.set("Study hours cannot be negative");
        } else if (value > 16) {
            studyHoursError.set("Study hours cannot exceed 16 hours per day");
        } else {
            studyHoursError.set("");
        }
    }
    
    /**
     * Validates water intake input
     */
    private void validateWaterIntake(double value) {
        if (value < 0) {
            waterIntakeError.set("Water intake cannot be negative");
        } else if (value > 5.0) {
            waterIntakeError.set("Water intake cannot exceed 5.0 liters per day");
        } else {
            waterIntakeError.set("");
        }
    }
    
    /**
     * Validates steps taken input
     */
    private void validateStepsTaken(double value) {
        if (value < 0) {
            stepsTakenError.set("Steps taken cannot be negative");
        } else if (value > 50000) {
            stepsTakenError.set("Steps taken cannot exceed 50,000 per day");
        } else {
            stepsTakenError.set("");
        }
    }
    
    /**
     * Validates sleep hours input
     */
    private void validateSleepHours(double value) {
        if (value < 0) {
            sleepHoursError.set("Sleep hours cannot be negative");
        } else if (value > 24.0) {
            sleepHoursError.set("Sleep hours cannot exceed 24 hours per day");
        } else {
            sleepHoursError.set("");
        }
    }
    
    /**
     * Validates money spent input
     */
    private void validateMoneySpent(double value) {
        if (value < 0) {
            moneySpentError.set("Money spent cannot be negative");
        } else if (value > 1000.0) {
            moneySpentError.set("Money spent cannot exceed $1,000 per day");
        } else {
            moneySpentError.set("");
        }
    }
    
    /**
     * Validates goals completed input
     */
    private void validateGoalsCompleted(double value) {
        if (value < 0) {
            goalsCompletedError.set("Goals completed cannot be negative");
        } else if (value > 10) {
            goalsCompletedError.set("Goals completed cannot exceed 10 per day");
        } else {
            goalsCompletedError.set("");
        }
    }
    
    /**
     * Submits the current habit data and integrates with XP calculation
     */
    public void submitHabits() {
        if (hasValidationErrors.get() || isSubmitting.get()) {
            return; // Don't submit if there are validation errors or already submitting
        }
        
        // Clear previous messages
        clearMessages();
        
        // Set submitting state
        isSubmitting.set(true);
        
        // Create task for background processing
        Task<Void> submitTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                // Create DailyHabits object from current values
                DailyHabits habits = createDailyHabitsFromInput();
                
                // Add to history for consistency calculation
                habitHistory.add(0, habits); // Add to beginning (most recent first)
                
                // Calculate consistency score based on recent history
                double consistencyScore = xpSystem.calculateConsistencyScore(habitHistory, 7);
                
                // Calculate and award XP
                int xpGained = xpSystem.awardXP(currentPet, habits, consistencyScore);
                
                // Simulate processing time for better UX
                Thread.sleep(500);
                
                // Update UI on JavaFX Application Thread
                javafx.application.Platform.runLater(() -> {
                    // Show confirmation message
                    confirmationMessage.set("✅ Habits submitted successfully!");
                    showConfirmation.set(true);
                    
                    // Show XP gain message
                    if (xpGained > 0) {
                        xpGainMessage.set(String.format("🌟 +%d XP earned! (Completion: %.0f%%)", 
                                xpGained, habits.getCompletionPercentage() * 100));
                        showXpGain.set(true);
                    }
                    
                    // Reset form after successful submission
                    resetForm();
                    
                    // Hide messages after delay
                    scheduleMessageHiding();
                });
                
                return null;
            }
            
            @Override
            protected void failed() {
                javafx.application.Platform.runLater(() -> {
                    confirmationMessage.set("❌ Failed to submit habits. Please try again.");
                    showConfirmation.set(true);
                    scheduleMessageHiding();
                });
            }
            
            @Override
            protected void succeeded() {
                // Task completed successfully
            }
            
            @Override
            protected void cancelled() {
                javafx.application.Platform.runLater(() -> {
                    confirmationMessage.set("⚠️ Submission cancelled.");
                    showConfirmation.set(true);
                    scheduleMessageHiding();
                });
            }
        };
        
        // Handle task completion
        submitTask.setOnSucceeded(e -> isSubmitting.set(false));
        submitTask.setOnFailed(e -> isSubmitting.set(false));
        submitTask.setOnCancelled(e -> isSubmitting.set(false));
        
        // Run task in background thread
        Thread submitThread = new Thread(submitTask);
        submitThread.setDaemon(true);
        submitThread.start();
    }
    
    /**
     * Creates a DailyHabits object from current input values
     */
    private DailyHabits createDailyHabitsFromInput() {
        DailyHabits habits = new DailyHabits(LocalDate.now());
        
        // Set values from input properties
        habits.setStudyHours((int) Math.round(studyHours.get()));
        habits.setWaterIntake(waterIntake.get());
        habits.setStepsTaken((int) Math.round(stepsTaken.get()));
        habits.setSleepHours(sleepHours.get());
        habits.setMoneySpent(moneySpent.get());
        habits.setGoalsCompleted((int) Math.round(goalsCompleted.get()));
        
        return habits;
    }
    
    /**
     * Resets the form to default values
     */
    private void resetForm() {
        studyHours.set(0.0);
        waterIntake.set(0.0);
        stepsTaken.set(0.0);
        sleepHours.set(0.0);
        moneySpent.set(0.0);
        goalsCompleted.set(0.0);
    }
    
    /**
     * Schedules hiding of feedback messages after a delay
     */
    private void scheduleMessageHiding() {
        Task<Void> hideTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                Thread.sleep(3000); // Show messages for 3 seconds
                return null;
            }
            
            @Override
            protected void succeeded() {
                javafx.application.Platform.runLater(() -> {
                    showConfirmation.set(false);
                    showXpGain.set(false);
                });
            }
        };
        
        Thread hideThread = new Thread(hideTask);
        hideThread.setDaemon(true);
        hideThread.start();
    }
    
    /**
     * Clears all feedback messages
     */
    public void clearMessages() {
        confirmationMessage.set("");
        showConfirmation.set(false);
        xpGainMessage.set("");
        showXpGain.set(false);
    }
    
    /**
     * Sets the current pet for habit tracking
     * @param pet The digital pet
     */
    public void setCurrentPet(DigitalPet pet) {
        this.currentPet = pet;
    }
    
    /**
     * Gets the current pet
     * @return The current digital pet
     */
    public DigitalPet getCurrentPet() {
        return currentPet;
    }
    
    /**
     * Adds historical habit data for consistency calculation
     * @param habits List of historical daily habits
     */
    public void setHabitHistory(List<DailyHabits> habits) {
        this.habitHistory.clear();
        if (habits != null) {
            this.habitHistory.addAll(habits);
        }
    }
    
    /**
     * Gets the current habit history
     * @return List of daily habits
     */
    public List<DailyHabits> getHabitHistory() {
        return new ArrayList<>(habitHistory);
    }
    
    // Property getters for JavaFX binding
    
    public DoubleProperty studyHoursProperty() {
        return studyHours;
    }
    
    public DoubleProperty waterIntakeProperty() {
        return waterIntake;
    }
    
    public DoubleProperty stepsTakenProperty() {
        return stepsTaken;
    }
    
    public DoubleProperty sleepHoursProperty() {
        return sleepHours;
    }
    
    public DoubleProperty moneySpentProperty() {
        return moneySpent;
    }
    
    public DoubleProperty goalsCompletedProperty() {
        return goalsCompleted;
    }
    
    // Error property getters
    
    public StringProperty studyHoursErrorProperty() {
        return studyHoursError;
    }
    
    public StringProperty waterIntakeErrorProperty() {
        return waterIntakeError;
    }
    
    public StringProperty stepsTakenErrorProperty() {
        return stepsTakenError;
    }
    
    public StringProperty sleepHoursErrorProperty() {
        return sleepHoursError;
    }
    
    public StringProperty moneySpentErrorProperty() {
        return moneySpentError;
    }
    
    public StringProperty goalsCompletedErrorProperty() {
        return goalsCompletedError;
    }
    
    // State property getters
    
    public BooleanProperty hasValidationErrorsProperty() {
        return hasValidationErrors;
    }
    
    public BooleanProperty isSubmittingProperty() {
        return isSubmitting;
    }
    
    // Feedback property getters
    
    public StringProperty confirmationMessageProperty() {
        return confirmationMessage;
    }
    
    public BooleanProperty showConfirmationProperty() {
        return showConfirmation;
    }
    
    public StringProperty xpGainMessageProperty() {
        return xpGainMessage;
    }
    
    public BooleanProperty showXpGainProperty() {
        return showXpGain;
    }
    
    // Convenience getters
    
    public double getStudyHours() {
        return studyHours.get();
    }
    
    public double getWaterIntake() {
        return waterIntake.get();
    }
    
    public double getStepsTaken() {
        return stepsTaken.get();
    }
    
    public double getSleepHours() {
        return sleepHours.get();
    }
    
    public double getMoneySpent() {
        return moneySpent.get();
    }
    
    public double getGoalsCompleted() {
        return goalsCompleted.get();
    }
    
    public boolean hasValidationErrors() {
        return hasValidationErrors.get();
    }
    
    public boolean isSubmitting() {
        return isSubmitting.get();
    }
    
    // Convenience setters
    
    public void setStudyHours(double hours) {
        studyHours.set(hours);
    }
    
    public void setWaterIntake(double liters) {
        waterIntake.set(liters);
    }
    
    public void setStepsTaken(double steps) {
        stepsTaken.set(steps);
    }
    
    public void setSleepHours(double hours) {
        sleepHours.set(hours);
    }
    
    public void setMoneySpent(double amount) {
        moneySpent.set(amount);
    }
    
    public void setGoalsCompleted(double goals) {
        goalsCompleted.set(goals);
    }
}