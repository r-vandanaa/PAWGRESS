package com.digitalpet.view;

import com.digitalpet.model.DailyHabits;
import com.digitalpet.model.DigitalPet;
import com.digitalpet.model.PetMood;
import com.digitalpet.service.NavigationManager;
import com.digitalpet.viewmodel.StatisticsViewModel;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Controller for the statistics view screen.
 * Handles chart display, time range selection, and data visualization.
 * 
 * Requirements addressed:
 * - 7.1: XP progression line chart with time periods and gridlines
 * - 7.2: Color-coded bar charts for habit performance with legends
 * - 7.3: Mood history timeline with visual indicators
 * - 7.5: Time range selection controls (7, 30, 90 days)
 */
public class StatisticsViewController implements Initializable {
    
    // Time range controls
    @FXML private ToggleGroup timeRangeGroup;
    @FXML private ToggleButton sevenDayButton;
    @FXML private ToggleButton thirtyDayButton;
    @FXML private ToggleButton ninetyDayButton;
    
    // XP Progress Chart
    @FXML private LineChart<String, Number> xpProgressChart;
    @FXML private CategoryAxis xpChartXAxis;
    @FXML private NumberAxis xpChartYAxis;
    @FXML private Label xpSummaryLabel;
    
    // Habit Performance Chart
    @FXML private BarChart<String, Number> habitPerformanceChart;
    @FXML private CategoryAxis habitChartXAxis;
    @FXML private NumberAxis habitChartYAxis;
    
    // Mood Timeline
    @FXML private VBox moodTimelineContainer;
    @FXML private Label moodSummaryLabel;
    
    // Navigation buttons
    @FXML private Button petTabButton;
    @FXML private Button habitsTabButton;
    @FXML private Button statsTabButton;
    @FXML private Button achievementsTabButton;
    
    private StatisticsViewModel viewModel;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd");
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize with default pet for now
        // In a full application, this would be injected or loaded from storage
        DigitalPet defaultPet = new DigitalPet("My Pet");
        this.viewModel = new StatisticsViewModel(defaultPet);
        
        setupTimeRangeControls();
        setupCharts();
        setupPropertyBindings();
        updateNavigationState();
        
        // Load initial data for 7 days
        loadStatisticsData(7);
    }
    
    /**
     * Sets up time range selection controls
     */
    private void setupTimeRangeControls() {
        timeRangeGroup.selectedToggleProperty().addListener((observable, oldToggle, newToggle) -> {
            if (newToggle != null) {
                int days = getDaysFromToggle((ToggleButton) newToggle);
                loadStatisticsData(days);
                
                // Save preference
                if (viewModel != null) {
                    viewModel.setSelectedTimeRange(days);
                }
            }
        });
        
        // Set initial selection
        sevenDayButton.setSelected(true);
    }
    
    /**
     * Gets the number of days from the selected toggle button
     */
    private int getDaysFromToggle(ToggleButton toggle) {
        if (toggle == sevenDayButton) return 7;
        if (toggle == thirtyDayButton) return 30;
        if (toggle == ninetyDayButton) return 90;
        return 7; // Default
    }
    
    /**
     * Sets up chart configurations
     */
    private void setupCharts() {
        // Configure XP Progress Chart
        xpProgressChart.setTitle("");
        xpProgressChart.setLegendVisible(false);
        xpProgressChart.setCreateSymbols(true);
        xpProgressChart.setAnimated(false);
        
        xpChartXAxis.setLabel("Date");
        xpChartYAxis.setLabel("Experience Points");
        xpChartYAxis.setAutoRanging(true);
        
        // Configure Habit Performance Chart
        habitPerformanceChart.setTitle("");
        habitPerformanceChart.setLegendVisible(false);
        habitPerformanceChart.setAnimated(false);
        habitPerformanceChart.setCategoryGap(8.0);
        
        habitChartXAxis.setLabel("Habit Categories");
        habitChartYAxis.setLabel("Completion Percentage");
        habitChartYAxis.setAutoRanging(false);
        habitChartYAxis.setLowerBound(0);
        habitChartYAxis.setUpperBound(100);
        habitChartYAxis.setTickUnit(20);
    }
    
    /**
     * Sets up property bindings between UI elements and ViewModel
     */
    private void setupPropertyBindings() {
        if (viewModel != null) {
            // Bind summary labels
            xpSummaryLabel.textProperty().bind(viewModel.xpSummaryTextProperty());
            moodSummaryLabel.textProperty().bind(viewModel.moodSummaryTextProperty());
        }
    }
    
    /**
     * Loads and displays statistics data for the specified time range
     */
    private void loadStatisticsData(int days) {
        if (viewModel == null) return;
        
        // Update view model time range
        viewModel.setSelectedTimeRange(days);
        
        // Load XP progression data
        updateXPProgressChart(days);
        
        // Load habit performance data
        updateHabitPerformanceChart(days);
        
        // Load mood history data
        updateMoodTimeline(days);
    }
    
    /**
     * Updates the XP progression line chart
     */
    private void updateXPProgressChart(int days) {
        List<StatisticsViewModel.XPDataPoint> xpData = viewModel.getXPProgressData(days);
        
        XYChart.Series<String, Number> xpSeries = new XYChart.Series<>();
        xpSeries.setName("XP Progress");
        
        for (StatisticsViewModel.XPDataPoint dataPoint : xpData) {
            String dateLabel = dataPoint.getDate().format(DATE_FORMATTER);
            xpSeries.getData().add(new XYChart.Data<>(dateLabel, dataPoint.getXp()));
        }
        
        xpProgressChart.getData().clear();
        xpProgressChart.getData().add(xpSeries);
        
        // Update axis labels based on time range
        if (days <= 7) {
            xpChartXAxis.setLabel("Date (Last 7 Days)");
        } else if (days <= 30) {
            xpChartXAxis.setLabel("Date (Last 30 Days)");
        } else {
            xpChartXAxis.setLabel("Date (Last 90 Days)");
        }
    }
    
    /**
     * Updates the habit performance bar chart
     */
    private void updateHabitPerformanceChart(int days) {
        Map<String, StatisticsViewModel.HabitPerformance> habitData = viewModel.getHabitPerformanceData(days);
        
        // Create series for different performance levels
        XYChart.Series<String, Number> excellentSeries = new XYChart.Series<>();
        XYChart.Series<String, Number> goodSeries = new XYChart.Series<>();
        XYChart.Series<String, Number> needsImprovementSeries = new XYChart.Series<>();
        
        excellentSeries.setName("Excellent");
        goodSeries.setName("Good");
        needsImprovementSeries.setName("Needs Improvement");
        
        for (Map.Entry<String, StatisticsViewModel.HabitPerformance> entry : habitData.entrySet()) {
            String habitName = formatHabitName(entry.getKey());
            StatisticsViewModel.HabitPerformance performance = entry.getValue();
            
            double avgPercentage = performance.getAveragePercentage() * 100;
            
            // Categorize performance level
            if (avgPercentage >= 80) {
                excellentSeries.getData().add(new XYChart.Data<>(habitName, avgPercentage));
                goodSeries.getData().add(new XYChart.Data<>(habitName, 0));
                needsImprovementSeries.getData().add(new XYChart.Data<>(habitName, 0));
            } else if (avgPercentage >= 60) {
                excellentSeries.getData().add(new XYChart.Data<>(habitName, 0));
                goodSeries.getData().add(new XYChart.Data<>(habitName, avgPercentage));
                needsImprovementSeries.getData().add(new XYChart.Data<>(habitName, 0));
            } else {
                excellentSeries.getData().add(new XYChart.Data<>(habitName, 0));
                goodSeries.getData().add(new XYChart.Data<>(habitName, 0));
                needsImprovementSeries.getData().add(new XYChart.Data<>(habitName, avgPercentage));
            }
        }
        
        habitPerformanceChart.getData().clear();
        habitPerformanceChart.getData().addAll(excellentSeries, goodSeries, needsImprovementSeries);
    }
    
    /**
     * Updates the mood history timeline
     */
    private void updateMoodTimeline(int days) {
        List<StatisticsViewModel.MoodDataPoint> moodData = viewModel.getMoodHistoryData(days);
        
        moodTimelineContainer.getChildren().clear();
        
        for (StatisticsViewModel.MoodDataPoint dataPoint : moodData) {
            HBox moodItem = createMoodTimelineItem(dataPoint);
            moodTimelineContainer.getChildren().add(moodItem);
        }
        
        // If no data, show placeholder
        if (moodData.isEmpty()) {
            Label noDataLabel = new Label("No mood data available for this time period");
            noDataLabel.getStyleClass().add("chart-summary");
            moodTimelineContainer.getChildren().add(noDataLabel);
        }
    }
    
    /**
     * Creates a mood timeline item for display
     */
    private HBox createMoodTimelineItem(StatisticsViewModel.MoodDataPoint dataPoint) {
        HBox moodItem = new HBox();
        moodItem.getStyleClass().add("mood-timeline-item");
        moodItem.setSpacing(12);
        moodItem.setPadding(new Insets(8, 12, 8, 12));
        
        // Mood indicator circle
        Label indicator = new Label();
        indicator.getStyleClass().addAll("mood-timeline-indicator", "mood-" + dataPoint.getMood().name().toLowerCase());
        
        // Date label
        Label dateLabel = new Label(dataPoint.getDate().format(DATE_FORMATTER));
        dateLabel.getStyleClass().add("mood-timeline-date");
        
        // Mood label
        Label moodLabel = new Label(dataPoint.getMood().getDisplayText());
        moodLabel.getStyleClass().add("mood-timeline-mood");
        
        moodItem.getChildren().addAll(indicator, dateLabel, moodLabel);
        
        return moodItem;
    }
    
    /**
     * Formats habit names for display
     */
    private String formatHabitName(String habitName) {
        return switch (habitName) {
            case "studyHours" -> "Study";
            case "waterIntake" -> "Water";
            case "stepsTaken" -> "Steps";
            case "sleepHours" -> "Sleep";
            case "moneySpent" -> "Spending";
            case "goalsCompleted" -> "Goals";
            default -> habitName;
        };
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
        
        // Set stats tab as active
        if (!statsTabButton.getStyleClass().contains("nav-button-active")) {
            statsTabButton.getStyleClass().add("nav-button-active");
        }
    }
    
    // Navigation event handlers
    
    @FXML
    private void onPetTabClicked() {
        NavigationManager.getInstance().navigateTo(NavigationManager.Screen.PET);
    }
    
    @FXML
    private void onHabitsTabClicked() {
        NavigationManager.getInstance().navigateTo(NavigationManager.Screen.HABITS);
    }
    
    @FXML
    private void onStatsTabClicked() {
        updateNavigationState();
        // Already on stats tab, no navigation needed
    }
    
    @FXML
    private void onAchievementsTabClicked() {
        NavigationManager.getInstance().navigateTo(NavigationManager.Screen.ACHIEVEMENTS);
    }
    
    /**
     * Sets the ViewModel for this controller
     * @param viewModel The StatisticsViewModel to use
     */
    public void setViewModel(StatisticsViewModel viewModel) {
        this.viewModel = viewModel;
        if (viewModel != null) {
            setupPropertyBindings();
            // Reload data with current time range
            ToggleButton selected = (ToggleButton) timeRangeGroup.getSelectedToggle();
            if (selected != null) {
                int days = getDaysFromToggle(selected);
                loadStatisticsData(days);
            }
        }
    }
    
    /**
     * Gets the current ViewModel
     * @return The current StatisticsViewModel
     */
    public StatisticsViewModel getViewModel() {
        return viewModel;
    }
}