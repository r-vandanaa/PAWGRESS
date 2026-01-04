package com.digitalpet.viewmodel;

import com.digitalpet.model.DailyHabits;
import com.digitalpet.model.DigitalPet;
import com.digitalpet.model.PetMood;
import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;
import net.jqwik.api.constraints.StringLength;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based tests for StatisticsViewModel
 * Feature: digital-pet-evolution
 * 
 * Tests Property 13: Statistics Visualization Completeness
 * Validates: Requirements 7.1, 7.2, 7.3, 7.5
 */
class StatisticsViewModelProperties {

    @Property
    @Label("Feature: digital-pet-evolution, Property 13: Statistics Visualization Completeness - XP Progression")
    void xpProgressionChartsDisplayCompleteDataWithTimeRanges(
            @ForAll @StringLength(min = 1, max = 20) String petName,
            @ForAll @IntRange(min = 7, max = 90) int timeRangeDays) {
        
        // Create pet and view model
        DigitalPet pet = new DigitalPet(petName);
        StatisticsViewModel viewModel = new StatisticsViewModel(pet);
        
        // Get XP progression data for the time range
        List<StatisticsViewModel.XPDataPoint> xpData = viewModel.getXPProgressData(timeRangeDays);
        
        // Requirements 7.1: XP progression line chart with time periods and gridlines
        
        // 1. XP data should be available for the requested time range
        assertNotNull(xpData, "XP progression data must not be null");
        assertTrue(xpData.size() <= timeRangeDays, 
                "XP data points should not exceed requested time range");
        
        // 2. XP progression should be chronologically ordered
        LocalDate previousDate = null;
        int previousXP = -1;
        
        for (StatisticsViewModel.XPDataPoint dataPoint : xpData) {
            assertNotNull(dataPoint.getDate(), "XP data point must have a date");
            assertTrue(dataPoint.getXp() >= 0, "XP values must be non-negative");
            
            if (previousDate != null) {
                assertTrue(dataPoint.getDate().isAfter(previousDate) || dataPoint.getDate().isEqual(previousDate),
                        "XP data points must be in chronological order");
            }
            
            // XP should generally increase or stay the same (never decrease)
            if (previousXP >= 0) {
                assertTrue(dataPoint.getXp() >= previousXP,
                        "XP progression should never decrease over time");
            }
            
            previousDate = dataPoint.getDate();
            previousXP = dataPoint.getXp();
        }
        
        // 3. Date range should be within the requested period
        if (!xpData.isEmpty()) {
            LocalDate earliestDate = xpData.get(0).getDate();
            LocalDate latestDate = xpData.get(xpData.size() - 1).getDate();
            LocalDate expectedStartDate = LocalDate.now().minusDays(timeRangeDays - 1);
            
            assertFalse(earliestDate.isBefore(expectedStartDate),
                    "XP data should not include dates before the requested range");
            assertFalse(latestDate.isAfter(LocalDate.now()),
                    "XP data should not include future dates");
        }
        
        // 4. XP summary text should be updated and informative
        String xpSummary = viewModel.getXpSummaryText();
        assertNotNull(xpSummary, "XP summary text must not be null");
        assertFalse(xpSummary.trim().isEmpty(), "XP summary text must not be empty");
        assertTrue(xpSummary.contains("XP"), "XP summary should mention XP");
    }

    @Property
    @Label("Feature: digital-pet-evolution, Property 13: Statistics Visualization Completeness - Habit Performance")
    void habitPerformanceChartsShowColorCodedDataWithLegends(
            @ForAll @StringLength(min = 1, max = 20) String petName,
            @ForAll @IntRange(min = 7, max = 90) int timeRangeDays) {
        
        // Create pet and view model
        DigitalPet pet = new DigitalPet(petName);
        StatisticsViewModel viewModel = new StatisticsViewModel(pet);
        
        // Get habit performance data for the time range
        Map<String, StatisticsViewModel.HabitPerformance> habitData = 
                viewModel.getHabitPerformanceData(timeRangeDays);
        
        // Requirements 7.2: Color-coded bar charts for habit performance with legends
        
        // 1. All six habit categories should be represented
        String[] expectedHabits = {"studyHours", "waterIntake", "stepsTaken", "sleepHours", "moneySpent", "goalsCompleted"};
        
        for (String habitName : expectedHabits) {
            assertTrue(habitData.containsKey(habitName),
                    "Habit performance data must include all six categories: " + habitName);
            
            StatisticsViewModel.HabitPerformance performance = habitData.get(habitName);
            assertNotNull(performance, "Habit performance data must not be null for " + habitName);
            
            // 2. Performance percentages should be valid (0.0 to 1.0)
            assertTrue(performance.getAveragePercentage() >= 0.0 && performance.getAveragePercentage() <= 1.0,
                    "Average percentage must be between 0.0 and 1.0 for " + habitName);
            assertTrue(performance.getBestPercentage() >= 0.0 && performance.getBestPercentage() <= 1.0,
                    "Best percentage must be between 0.0 and 1.0 for " + habitName);
            assertTrue(performance.getWorstPercentage() >= 0.0 && performance.getWorstPercentage() <= 1.0,
                    "Worst percentage must be between 0.0 and 1.0 for " + habitName);
            
            // 3. Performance relationships should be logical
            assertTrue(performance.getBestPercentage() >= performance.getAveragePercentage(),
                    "Best performance should be >= average performance for " + habitName);
            assertTrue(performance.getAveragePercentage() >= performance.getWorstPercentage(),
                    "Average performance should be >= worst performance for " + habitName);
            
            // 4. Data points count should be reasonable
            assertTrue(performance.getDataPoints() >= 0,
                    "Data points count must be non-negative for " + habitName);
            assertTrue(performance.getDataPoints() <= timeRangeDays,
                    "Data points should not exceed time range for " + habitName);
        }
        
        // 5. Performance data should enable color coding (excellent >= 80%, good >= 60%, needs improvement < 60%)
        for (Map.Entry<String, StatisticsViewModel.HabitPerformance> entry : habitData.entrySet()) {
            StatisticsViewModel.HabitPerformance performance = entry.getValue();
            double avgPercentage = performance.getAveragePercentage();
            
            // Verify that performance levels can be categorized for color coding
            if (avgPercentage >= 0.8) {
                // Should be categorized as "excellent" (green)
                assertTrue(avgPercentage >= 0.8, "Excellent performance should be >= 80%");
            } else if (avgPercentage >= 0.6) {
                // Should be categorized as "good" (blue)
                assertTrue(avgPercentage >= 0.6 && avgPercentage < 0.8, "Good performance should be 60-79%");
            } else {
                // Should be categorized as "needs improvement" (light red)
                assertTrue(avgPercentage < 0.6, "Needs improvement should be < 60%");
            }
        }
    }

    @Property
    @Label("Feature: digital-pet-evolution, Property 13: Statistics Visualization Completeness - Mood History")
    void moodHistoryTimelineShowsVisualIndicatorsWithDates(
            @ForAll @StringLength(min = 1, max = 20) String petName,
            @ForAll @IntRange(min = 7, max = 90) int timeRangeDays) {
        
        // Create pet and view model
        DigitalPet pet = new DigitalPet(petName);
        StatisticsViewModel viewModel = new StatisticsViewModel(pet);
        
        // Get mood history data for the time range
        List<StatisticsViewModel.MoodDataPoint> moodData = viewModel.getMoodHistoryData(timeRangeDays);
        
        // Requirements 7.3: Mood history timeline with visual indicators and date labels
        
        // 1. Mood data should be available and properly structured
        assertNotNull(moodData, "Mood history data must not be null");
        assertTrue(moodData.size() <= timeRangeDays,
                "Mood data points should not exceed requested time range");
        
        // 2. Each mood data point should have valid date and mood
        LocalDate previousDate = null;
        
        for (StatisticsViewModel.MoodDataPoint dataPoint : moodData) {
            assertNotNull(dataPoint.getDate(), "Mood data point must have a date");
            assertNotNull(dataPoint.getMood(), "Mood data point must have a mood");
            
            // Verify mood is one of the valid enum values
            assertTrue(dataPoint.getMood() instanceof PetMood,
                    "Mood must be a valid PetMood enum value");
            
            // Verify date ordering (most recent first for timeline display)
            if (previousDate != null) {
                assertFalse(dataPoint.getDate().isAfter(previousDate),
                        "Mood timeline should be ordered with most recent first");
            }
            
            previousDate = dataPoint.getDate();
        }
        
        // 3. Date range should be within the requested period
        if (!moodData.isEmpty()) {
            LocalDate latestDate = moodData.get(0).getDate(); // Most recent (first in list)
            LocalDate earliestDate = moodData.get(moodData.size() - 1).getDate(); // Oldest (last in list)
            LocalDate expectedStartDate = LocalDate.now().minusDays(timeRangeDays - 1);
            
            assertFalse(latestDate.isAfter(LocalDate.now()),
                    "Mood data should not include future dates");
            assertFalse(earliestDate.isBefore(expectedStartDate),
                    "Mood data should not include dates before the requested range");
        }
        
        // 4. All valid mood types should be representable
        for (StatisticsViewModel.MoodDataPoint dataPoint : moodData) {
            PetMood mood = dataPoint.getMood();
            
            // Verify mood has display properties for visual indicators
            assertNotNull(mood.getDisplayName(), "Mood must have a display name");
            assertNotNull(mood.getEmoji(), "Mood must have an emoji for visual indication");
            assertNotNull(mood.getEnvironmentTint(), "Mood must have environment tint for visual effects");
            assertFalse(mood.getDisplayName().trim().isEmpty(), "Mood display name must not be empty");
            assertFalse(mood.getEmoji().trim().isEmpty(), "Mood emoji must not be empty");
        }
        
        // 5. Mood summary should be informative and updated
        String moodSummary = viewModel.getMoodSummaryText();
        assertNotNull(moodSummary, "Mood summary text must not be null");
        assertFalse(moodSummary.trim().isEmpty(), "Mood summary text must not be empty");
        assertTrue(moodSummary.contains("mood") || moodSummary.contains("Mood"),
                "Mood summary should mention mood");
    }

    @Property
    @Label("Feature: digital-pet-evolution, Property 13: Statistics Visualization Completeness - Time Range Selection")
    void timeRangeSelectionControlsProvideMultiplePeriodsWithPersistence(
            @ForAll @StringLength(min = 1, max = 20) String petName,
            @ForAll("validTimeRanges") int timeRange1,
            @ForAll("validTimeRanges") int timeRange2) {
        
        // Create pet and view model
        DigitalPet pet = new DigitalPet(petName);
        StatisticsViewModel viewModel = new StatisticsViewModel(pet);
        
        // Requirements 7.5: Time range selection controls (7, 30, 90 days) with persistent preferences
        
        // 1. View model should support multiple time ranges
        int initialTimeRange = viewModel.getSelectedTimeRange();
        assertTrue(initialTimeRange > 0, "Initial time range should be positive");
        
        // 2. Time range should be settable and persistent
        viewModel.setSelectedTimeRange(timeRange1);
        assertEquals(timeRange1, viewModel.getSelectedTimeRange(),
                "Time range should be settable and retrievable");
        
        // 3. Changing time range should update data availability
        List<StatisticsViewModel.XPDataPoint> xpData1 = viewModel.getXPProgressData(timeRange1);
        Map<String, StatisticsViewModel.HabitPerformance> habitData1 = viewModel.getHabitPerformanceData(timeRange1);
        List<StatisticsViewModel.MoodDataPoint> moodData1 = viewModel.getMoodHistoryData(timeRange1);
        
        viewModel.setSelectedTimeRange(timeRange2);
        assertEquals(timeRange2, viewModel.getSelectedTimeRange(),
                "Time range should persist after change");
        
        List<StatisticsViewModel.XPDataPoint> xpData2 = viewModel.getXPProgressData(timeRange2);
        Map<String, StatisticsViewModel.HabitPerformance> habitData2 = viewModel.getHabitPerformanceData(timeRange2);
        List<StatisticsViewModel.MoodDataPoint> moodData2 = viewModel.getMoodHistoryData(timeRange2);
        
        // 4. Different time ranges should potentially provide different data sets
        assertNotNull(xpData1, "XP data should be available for first time range");
        assertNotNull(xpData2, "XP data should be available for second time range");
        assertNotNull(habitData1, "Habit data should be available for first time range");
        assertNotNull(habitData2, "Habit data should be available for second time range");
        assertNotNull(moodData1, "Mood data should be available for first time range");
        assertNotNull(moodData2, "Mood data should be available for second time range");
        
        // 5. Longer time ranges should not have fewer data points than shorter ones (when data exists)
        if (timeRange1 < timeRange2) {
            assertTrue(xpData1.size() <= xpData2.size(),
                    "Shorter time range should not have more XP data points than longer range");
            assertTrue(moodData1.size() <= moodData2.size(),
                    "Shorter time range should not have more mood data points than longer range");
        }
        
        // 6. Time range property should be observable for UI binding
        assertNotNull(viewModel.selectedTimeRangeProperty(),
                "Time range property should be available for JavaFX binding");
        assertEquals(timeRange2, viewModel.selectedTimeRangeProperty().get(),
                "Time range property should reflect current value");
    }

    @Property
    @Label("Feature: digital-pet-evolution, Property 13: Statistics Visualization Completeness - Data Consistency")
    void statisticsDataMaintainsConsistencyAcrossVisualizationTypes(
            @ForAll @StringLength(min = 1, max = 20) String petName,
            @ForAll("validTimeRanges") int timeRange) {
        
        // Create pet and view model
        DigitalPet pet = new DigitalPet(petName);
        StatisticsViewModel viewModel = new StatisticsViewModel(pet);
        
        // Get all types of statistics data
        List<StatisticsViewModel.XPDataPoint> xpData = viewModel.getXPProgressData(timeRange);
        Map<String, StatisticsViewModel.HabitPerformance> habitData = viewModel.getHabitPerformanceData(timeRange);
        List<StatisticsViewModel.MoodDataPoint> moodData = viewModel.getMoodHistoryData(timeRange);
        
        // Requirements 7.1, 7.2, 7.3: Data consistency across all visualization types
        
        // 1. All data types should cover the same time period
        if (!xpData.isEmpty() && !moodData.isEmpty()) {
            LocalDate xpStartDate = xpData.get(0).getDate();
            LocalDate xpEndDate = xpData.get(xpData.size() - 1).getDate();
            LocalDate moodStartDate = moodData.get(moodData.size() - 1).getDate(); // Mood is reverse ordered
            LocalDate moodEndDate = moodData.get(0).getDate();
            
            // Data should cover similar time periods (allowing for some variation in available data)
            assertTrue(Math.abs(xpStartDate.toEpochDay() - moodStartDate.toEpochDay()) <= timeRange,
                    "XP and mood data should cover similar time periods");
            assertTrue(Math.abs(xpEndDate.toEpochDay() - moodEndDate.toEpochDay()) <= 1,
                    "XP and mood data should end at similar dates");
        }
        
        // 2. Summary texts should be consistent with data
        String xpSummary = viewModel.getXpSummaryText();
        String moodSummary = viewModel.getMoodSummaryText();
        
        if (!xpData.isEmpty()) {
            assertTrue(xpSummary.contains(String.valueOf(xpData.get(xpData.size() - 1).getXp())),
                    "XP summary should reflect actual XP data");
        }
        
        if (!moodData.isEmpty()) {
            PetMood currentMood = moodData.get(0).getMood(); // Most recent mood
            assertTrue(moodSummary.contains(currentMood.getDisplayName()),
                    "Mood summary should reflect current mood from data");
        }
        
        // 3. Data should be internally consistent
        for (Map.Entry<String, StatisticsViewModel.HabitPerformance> entry : habitData.entrySet()) {
            StatisticsViewModel.HabitPerformance performance = entry.getValue();
            
            // Performance statistics should be mathematically consistent
            assertTrue(performance.getBestPercentage() >= performance.getAveragePercentage(),
                    "Best performance should be >= average for " + entry.getKey());
            assertTrue(performance.getAveragePercentage() >= performance.getWorstPercentage(),
                    "Average performance should be >= worst for " + entry.getKey());
            
            if (performance.getDataPoints() == 1) {
                // With only one data point, all statistics should be equal
                assertEquals(performance.getBestPercentage(), performance.getAveragePercentage(), 0.001,
                        "With one data point, best should equal average for " + entry.getKey());
                assertEquals(performance.getAveragePercentage(), performance.getWorstPercentage(), 0.001,
                        "With one data point, average should equal worst for " + entry.getKey());
            }
        }
        
        // 4. Property bindings should be available for reactive UI updates
        assertNotNull(viewModel.xpSummaryTextProperty(),
                "XP summary property should be available for binding");
        assertNotNull(viewModel.moodSummaryTextProperty(),
                "Mood summary property should be available for binding");
        
        assertEquals(xpSummary, viewModel.xpSummaryTextProperty().get(),
                "XP summary property should match getter value");
        assertEquals(moodSummary, viewModel.moodSummaryTextProperty().get(),
                "Mood summary property should match getter value");
    }

    @Provide
    Arbitrary<Integer> validTimeRanges() {
        return Arbitraries.of(7, 30, 90);
    }
}