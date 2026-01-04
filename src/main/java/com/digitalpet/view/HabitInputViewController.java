package com.digitalpet.view;

import com.digitalpet.model.DailyHabits;
import com.digitalpet.viewmodel.HabitInputViewModel;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.util.StringConverter;

import java.net.URL;
import java.text.DecimalFormat;
import java.util.ResourceBundle;

/**
 * Controller for the habit input view screen.
 * Handles UI interactions for daily habit tracking with sliders and validation.
 * 
 * Requirements addressed:
 * - 3.1: Six habit categories with sliders and live value display
 * - 3.2: Clear category icons and live numerical value display
 * - 3.4: Range validation with error messaging
 * - 3.5: Submit button with confirmation feedback
 */
public class HabitInputViewController implements Initializable {
    
    // Habit input sliders
    @FXML private Slider studyHoursSlider;
    @FXML private Slider waterIntakeSlider;
    @FXML private Slider stepsTakenSlider;
    @FXML private Slider sleepHoursSlider;
    @FXML private Slider moneySpentSlider;
    @FXML private Slider goalsCompletedSlider;
    
    // Value display labels
    @FXML private Label studyHoursValue;
    @FXML private Label waterIntakeValue;
    @FXML private Label stepsTakenValue;
    @FXML private Label sleepHoursValue;
    @FXML private Label moneySpentValue;
    @FXML private Label goalsCompletedValue;
    
    // Error message labels
    @FXML private Label studyHoursError;
    @FXML private Label waterIntakeError;
    @FXML private Label stepsTakenError;
    @FXML private Label sleepHoursError;
    @FXML private Label moneySpentError;
    @FXML private Label goalsCompletedError;
    
    // Submit and feedback elements
    @FXML private Button submitButton;
    @FXML private Label confirmationMessage;
    @FXML private Label xpGainMessage;
    
    // Navigation elements
    @FXML private Button petTabButton;
    @FXML private Button habitsTabButton;
    @FXML private Button statsTabButton;
    @FXML private Button achievementsTabButton;
    
    private HabitInputViewModel viewModel;
    private final DecimalFormat decimalFormat = new DecimalFormat("#.#");
    private final DecimalFormat currencyFormat = new DecimalFormat("$#.00");
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize ViewModel
        this.viewModel = new HabitInputViewModel();
        
        setupSliders();
        setupPropertyBindings();
        setupValidation();
        updateNavigationState();
    }
    
    /**
     * Sets up slider configurations and value formatters
     */
    private void setupSliders() {
        // Study Hours Slider (0-16 hours, integer steps)
        studyHoursSlider.setMin(0);
        studyHoursSlider.setMax(16);
        studyHoursSlider.setBlockIncrement(1);
        studyHoursSlider.setMajorTickUnit(4);
        studyHoursSlider.setMinorTickCount(3);
        studyHoursSlider.setSnapToTicks(true);
        studyHoursSlider.setShowTickLabels(true);
        studyHoursSlider.setShowTickMarks(true);
        
        // Water Intake Slider (0-5.0 liters, 0.1 steps)
        waterIntakeSlider.setMin(0);
        waterIntakeSlider.setMax(5.0);
        waterIntakeSlider.setBlockIncrement(0.1);
        waterIntakeSlider.setMajorTickUnit(1.0);
        waterIntakeSlider.setMinorTickCount(9);
        waterIntakeSlider.setShowTickLabels(true);
        waterIntakeSlider.setShowTickMarks(true);
        
        // Steps Taken Slider (0-50000 steps, 1000 steps)
        stepsTakenSlider.setMin(0);
        stepsTakenSlider.setMax(50000);
        stepsTakenSlider.setBlockIncrement(1000);
        stepsTakenSlider.setMajorTickUnit(10000);
        stepsTakenSlider.setMinorTickCount(9);
        stepsTakenSlider.setShowTickLabels(true);
        stepsTakenSlider.setShowTickMarks(true);
        
        // Sleep Hours Slider (0-24.0 hours, 0.5 steps)
        sleepHoursSlider.setMin(0);
        sleepHoursSlider.setMax(24.0);
        sleepHoursSlider.setBlockIncrement(0.5);
        sleepHoursSlider.setMajorTickUnit(6.0);
        sleepHoursSlider.setMinorTickCount(5);
        sleepHoursSlider.setShowTickLabels(true);
        sleepHoursSlider.setShowTickMarks(true);
        
        // Money Spent Slider (0-1000 dollars, 10 dollar steps)
        moneySpentSlider.setMin(0);
        moneySpentSlider.setMax(1000);
        moneySpentSlider.setBlockIncrement(10);
        moneySpentSlider.setMajorTickUnit(200);
        moneySpentSlider.setMinorTickCount(9);
        moneySpentSlider.setShowTickLabels(true);
        moneySpentSlider.setShowTickMarks(true);
        
        // Goals Completed Slider (0-10 goals, integer steps)
        goalsCompletedSlider.setMin(0);
        goalsCompletedSlider.setMax(10);
        goalsCompletedSlider.setBlockIncrement(1);
        goalsCompletedSlider.setMajorTickUnit(2);
        goalsCompletedSlider.setMinorTickCount(1);
        goalsCompletedSlider.setSnapToTicks(true);
        goalsCompletedSlider.setShowTickLabels(true);
        goalsCompletedSlider.setShowTickMarks(true);
        
        // Set up custom label formatters for better readability
        setupSliderLabelFormatters();
    }
    
    /**
     * Sets up custom label formatters for sliders
     */
    private void setupSliderLabelFormatters() {
        // Study hours: show as integers
        studyHoursSlider.setLabelFormatter(new StringConverter<Double>() {
            @Override
            public String toString(Double value) {
                return String.valueOf(value.intValue());
            }
            
            @Override
            public Double fromString(String string) {
                return Double.valueOf(string);
            }
        });
        
        // Water intake: show with 1 decimal place
        waterIntakeSlider.setLabelFormatter(new StringConverter<Double>() {
            @Override
            public String toString(Double value) {
                return decimalFormat.format(value);
            }
            
            @Override
            public Double fromString(String string) {
                return Double.valueOf(string);
            }
        });
        
        // Steps: show in thousands for readability
        stepsTakenSlider.setLabelFormatter(new StringConverter<Double>() {
            @Override
            public String toString(Double value) {
                if (value >= 1000) {
                    return (value.intValue() / 1000) + "k";
                }
                return String.valueOf(value.intValue());
            }
            
            @Override
            public Double fromString(String string) {
                if (string.endsWith("k")) {
                    return Double.valueOf(string.substring(0, string.length() - 1)) * 1000;
                }
                return Double.valueOf(string);
            }
        });
        
        // Sleep hours: show with 1 decimal place
        sleepHoursSlider.setLabelFormatter(new StringConverter<Double>() {
            @Override
            public String toString(Double value) {
                return decimalFormat.format(value);
            }
            
            @Override
            public Double fromString(String string) {
                return Double.valueOf(string);
            }
        });
        
        // Money: show as currency
        moneySpentSlider.setLabelFormatter(new StringConverter<Double>() {
            @Override
            public String toString(Double value) {
                return "$" + value.intValue();
            }
            
            @Override
            public Double fromString(String string) {
                return Double.valueOf(string.replace("$", ""));
            }
        });
        
        // Goals: show as integers
        goalsCompletedSlider.setLabelFormatter(new StringConverter<Double>() {
            @Override
            public String toString(Double value) {
                return String.valueOf(value.intValue());
            }
            
            @Override
            public Double fromString(String string) {
                return Double.valueOf(string);
            }
        });
    }
    
    /**
     * Sets up property bindings between UI elements and ViewModel
     */
    private void setupPropertyBindings() {
        // Bind slider values to ViewModel properties
        viewModel.studyHoursProperty().bind(studyHoursSlider.valueProperty());
        viewModel.waterIntakeProperty().bind(waterIntakeSlider.valueProperty());
        viewModel.stepsTakenProperty().bind(stepsTakenSlider.valueProperty());
        viewModel.sleepHoursProperty().bind(sleepHoursSlider.valueProperty());
        viewModel.moneySpentProperty().bind(moneySpentSlider.valueProperty());
        viewModel.goalsCompletedProperty().bind(goalsCompletedSlider.valueProperty());
        
        // Bind submit button state to validation
        submitButton.disableProperty().bind(viewModel.hasValidationErrorsProperty());
        
        // Bind feedback messages
        confirmationMessage.textProperty().bind(viewModel.confirmationMessageProperty());
        confirmationMessage.visibleProperty().bind(viewModel.showConfirmationProperty());
        
        xpGainMessage.textProperty().bind(viewModel.xpGainMessageProperty());
        xpGainMessage.visibleProperty().bind(viewModel.showXpGainProperty());
        
        // Set up live value display updates
        setupValueDisplayBindings();
    }
    
    /**
     * Sets up live value display bindings for all sliders
     */
    private void setupValueDisplayBindings() {
        // Study hours value display
        studyHoursSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            int hours = newValue.intValue();
            studyHoursValue.setText(hours + (hours == 1 ? " hour" : " hours"));
        });
        
        // Water intake value display
        waterIntakeSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            waterIntakeValue.setText(decimalFormat.format(newValue.doubleValue()) + " L");
        });
        
        // Steps taken value display
        stepsTakenSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            int steps = newValue.intValue();
            stepsTakenValue.setText(String.format("%,d steps", steps));
        });
        
        // Sleep hours value display
        sleepHoursSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            double hours = newValue.doubleValue();
            sleepHoursValue.setText(decimalFormat.format(hours) + (hours == 1.0 ? " hour" : " hours"));
        });
        
        // Money spent value display
        moneySpentSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            moneySpentValue.setText(currencyFormat.format(newValue.doubleValue()));
        });
        
        // Goals completed value display
        goalsCompletedSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            int goals = newValue.intValue();
            goalsCompletedValue.setText(goals + (goals == 1 ? " goal" : " goals"));
        });
        
        // Initialize displays with current values
        studyHoursValue.setText("0 hours");
        waterIntakeValue.setText("0.0 L");
        stepsTakenValue.setText("0 steps");
        sleepHoursValue.setText("0.0 hours");
        moneySpentValue.setText("$0.00");
        goalsCompletedValue.setText("0 goals");
    }
    
    /**
     * Sets up validation error display bindings
     */
    private void setupValidation() {
        // Bind error messages and visibility
        studyHoursError.textProperty().bind(viewModel.studyHoursErrorProperty());
        studyHoursError.visibleProperty().bind(viewModel.studyHoursErrorProperty().isNotEmpty());
        
        waterIntakeError.textProperty().bind(viewModel.waterIntakeErrorProperty());
        waterIntakeError.visibleProperty().bind(viewModel.waterIntakeErrorProperty().isNotEmpty());
        
        stepsTakenError.textProperty().bind(viewModel.stepsTakenErrorProperty());
        stepsTakenError.visibleProperty().bind(viewModel.stepsTakenErrorProperty().isNotEmpty());
        
        sleepHoursError.textProperty().bind(viewModel.sleepHoursErrorProperty());
        sleepHoursError.visibleProperty().bind(viewModel.sleepHoursErrorProperty().isNotEmpty());
        
        moneySpentError.textProperty().bind(viewModel.moneySpentErrorProperty());
        moneySpentError.visibleProperty().bind(viewModel.moneySpentErrorProperty().isNotEmpty());
        
        goalsCompletedError.textProperty().bind(viewModel.goalsCompletedErrorProperty());
        goalsCompletedError.visibleProperty().bind(viewModel.goalsCompletedErrorProperty().isNotEmpty());
    }
    
    /**
     * Updates navigation button states
     */
    private void updateNavigationState() {
        // Reset all buttons
        petTabButton.getStyleClass().removeAll("nav-button-active");
        habitsTabButton.getStyleClass().removeAll("nav-button-active");
        statsTabButton.getStyleClass().removeAll("nav-button-active");
        achievementsTabButton.getStyleClass().removeAll("nav-button-active");
        
        // Set habits tab as active
        if (!habitsTabButton.getStyleClass().contains("nav-button-active")) {
            habitsTabButton.getStyleClass().add("nav-button-active");
        }
    }
    
    // Event handlers
    
    /**
     * Handles habit submission
     */
    @FXML
    private void onSubmitHabits() {
        if (viewModel != null) {
            viewModel.submitHabits();
        }
    }
    
    // Navigation event handlers
    
    @FXML
    private void onPetTabClicked() {
        // TODO: Navigate to pet view when navigation system is implemented
        System.out.println("Navigate to Pet view");
    }
    
    @FXML
    private void onHabitsTabClicked() {
        updateNavigationState();
        // Already on habits tab, no navigation needed
    }
    
    @FXML
    private void onStatsTabClicked() {
        // TODO: Navigate to statistics view when implemented
        System.out.println("Navigate to Stats view");
    }
    
    @FXML
    private void onAchievementsTabClicked() {
        // TODO: Navigate to achievements view when implemented
        System.out.println("Navigate to Achievements view");
    }
    
    /**
     * Sets the ViewModel for this controller
     * @param viewModel The HabitInputViewModel to use
     */
    public void setViewModel(HabitInputViewModel viewModel) {
        this.viewModel = viewModel;
        if (viewModel != null) {
            setupPropertyBindings();
            setupValidation();
        }
    }
    
    /**
     * Gets the current ViewModel
     * @return The current HabitInputViewModel
     */
    public HabitInputViewModel getViewModel() {
        return viewModel;
    }
    
    /**
     * Resets all sliders to their default values
     */
    public void resetForm() {
        studyHoursSlider.setValue(0);
        waterIntakeSlider.setValue(0);
        stepsTakenSlider.setValue(0);
        sleepHoursSlider.setValue(0);
        moneySpentSlider.setValue(0);
        goalsCompletedSlider.setValue(0);
        
        if (viewModel != null) {
            viewModel.clearMessages();
        }
    }
    
    /**
     * Loads habit data into the form
     * @param habits The daily habits to load
     */
    public void loadHabits(DailyHabits habits) {
        if (habits != null) {
            studyHoursSlider.setValue(habits.getStudyHours());
            waterIntakeSlider.setValue(habits.getWaterIntake());
            stepsTakenSlider.setValue(habits.getStepsTaken());
            sleepHoursSlider.setValue(habits.getSleepHours());
            moneySpentSlider.setValue(habits.getMoneySpent());
            goalsCompletedSlider.setValue(habits.getGoalsCompleted());
        }
    }
}