package com.digitalpet.viewmodel;

import com.digitalpet.model.DailyHabits;
import com.digitalpet.model.DigitalPet;
import com.digitalpet.model.PetMood;
import javafx.beans.property.*;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * ViewModel for the statistics view, handling data aggregation and chart binding.
 * Manages time range filtering, data aggregation, and property binding for UI updates.
 * 
 * Requirements addressed:
 * - 7.4: Data aggregation logic for chart display
 * - 7.5: Time range filtering with persistent preferences
 */
public class StatisticsViewModel {
    
    // Core data
    private final DigitalPet pet;
    private final List<DailyHabits> habitHistory;
    private final Map<LocalDate, PetMood> moodHistory;
    
    // Properties for UI binding
    private final IntegerProperty selectedTimeRange;
    private final StringProperty xpSummaryText;
    private final StringProperty moodSummaryText;
    
    // Data cache for performance
    private final Map<Integer, List<XPDataPoint>> xpDataCache;
    private final Map<Integer, Map<String, HabitPerformance>> habitDataCache;
    private final Map<Integer, List<MoodDataPoint>> moodDataCache;
    
    /**
     * Creates a new StatisticsViewModel
     * @param pet The digital pet to track statistics for
     */
    public StatisticsViewModel(DigitalPet pet) {
        this.pet = pet;
        this.habitHistory = new ArrayList<>();
        this.moodHistory = new HashMap<>();
        
        // Initialize properties
        this.selectedTimeRange = new SimpleIntegerProperty(this, "selectedTimeRange", 7);
        this.xpSummaryText = new SimpleStringProperty(this, "xpSummaryText", "Total XP: 0");
        this.moodSummaryText = new SimpleStringProperty(this, "moodSummaryText", "Current mood streak: 0 days");
        
        // Initialize caches
        this.xpDataCache = new HashMap<>();
        this.habitDataCache = new HashMap<>();
        this.moodDataCache = new HashMap<>();
        
        // Generate sample data for demonstration
        generateSampleData();
        
        // Set up property listeners
        setupPropertyListeners();
        
        // Initial data load
        updateSummaryTexts();
    }
    
    /**
     * Sets up property listeners for automatic updates
     */
    private void setupPropertyListeners() {
        selectedTimeRange.addListener((observable, oldValue, newValue) -> {
            updateSummaryTexts();
        });
    }
    
    /**
     * Generates sample data for demonstration purposes
     * In a real application, this would load from persistent storage
     */
    private void generateSampleData() {
        LocalDate today = LocalDate.now();
        Random random = new Random(42); // Fixed seed for consistent demo data
        
        // Generate 90 days of sample habit data
        for (int i = 89; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            DailyHabits habits = new DailyHabits(date);
            
            // Generate realistic habit data with some trends
            double trendFactor = 1.0 - (i / 180.0); // Gradual improvement over time
            double randomFactor = 0.3 + (random.nextDouble() * 0.4); // 30-70% base completion
            double completionRate = Math.min(1.0, randomFactor + trendFactor * 0.5);
            
            // Set habit values based on completion rate with some variation
            habits.setStudyHours((int) (completionRate * 8 + random.nextGaussian() * 2));
            habits.setWaterIntake(completionRate * 2.5 + random.nextGaussian() * 0.5);
            habits.setStepsTaken((int) (completionRate * 10000 + random.nextGaussian() * 2000));
            habits.setSleepHours(completionRate * 8 + random.nextGaussian() * 1);
            habits.setMoneySpent(Math.max(0, 50 - completionRate * 30 + random.nextGaussian() * 15));
            habits.setGoalsCompleted((int) (completionRate * 5 + random.nextGaussian() * 1));
            
            habitHistory.add(habits);
            
            // Generate corresponding mood based on habit completion
            PetMood mood = generateMoodFromHabits(habits, random);
            moodHistory.put(date, mood);
        }
        
        // Sort habit history by date (oldest first)
        habitHistory.sort(Comparator.comparing(DailyHabits::getDate));
    }
    
    /**
     * Generates a realistic mood based on habit completion
     */
    private PetMood generateMoodFromHabits(DailyHabits habits, Random random) {
        double completion = habits.getCompletionPercentage();
        
        if (completion >= 0.8) {
            return random.nextDouble() < 0.8 ? PetMood.HAPPY : PetMood.CELEBRATING;
        } else if (completion >= 0.6) {
            return PetMood.HAPPY;
        } else if (completion >= 0.4) {
            return PetMood.NEUTRAL;
        } else if (completion >= 0.2) {
            return random.nextDouble() < 0.7 ? PetMood.WORRIED : PetMood.SLEEPY;
        } else {
            return PetMood.WORRIED;
        }
    }
    
    /**
     * Gets XP progression data for the specified time range
     * @param days Number of days to include
     * @return List of XP data points
     */
    public List<XPDataPoint> getXPProgressData(int days) {
        // Check cache first
        if (xpDataCache.containsKey(days)) {
            return xpDataCache.get(days);
        }
        
        List<XPDataPoint> dataPoints = new ArrayList<>();
        LocalDate startDate = LocalDate.now().minusDays(days - 1);
        
        int cumulativeXP = 0;
        
        for (DailyHabits habits : habitHistory) {
            if (habits.getDate().isBefore(startDate)) {
                // Add XP but don't include in chart (before our range)
                cumulativeXP += calculateDailyXP(habits);
                continue;
            }
            
            if (habits.getDate().isAfter(LocalDate.now())) {
                break; // Future dates
            }
            
            // Calculate XP for this day
            int dailyXP = calculateDailyXP(habits);
            cumulativeXP += dailyXP;
            
            dataPoints.add(new XPDataPoint(habits.getDate(), cumulativeXP));
        }
        
        // Cache the result
        xpDataCache.put(days, dataPoints);
        
        return dataPoints;
    }
    
    /**
     * Gets habit performance data for the specified time range
     * @param days Number of days to include
     * @return Map of habit names to performance data
     */
    public Map<String, HabitPerformance> getHabitPerformanceData(int days) {
        // Check cache first
        if (habitDataCache.containsKey(days)) {
            return habitDataCache.get(days);
        }
        
        LocalDate startDate = LocalDate.now().minusDays(days - 1);
        
        // Filter habits within time range
        List<DailyHabits> relevantHabits = habitHistory.stream()
                .filter(h -> !h.getDate().isBefore(startDate) && !h.getDate().isAfter(LocalDate.now()))
                .collect(Collectors.toList());
        
        Map<String, HabitPerformance> performanceMap = new HashMap<>();
        
        // Calculate performance for each habit category
        String[] habitNames = {"studyHours", "waterIntake", "stepsTaken", "sleepHours", "moneySpent", "goalsCompleted"};
        
        for (String habitName : habitNames) {
            List<Double> percentages = new ArrayList<>();
            
            for (DailyHabits habits : relevantHabits) {
                double percentage = calculateHabitPercentage(habits, habitName);
                percentages.add(percentage);
            }
            
            if (!percentages.isEmpty()) {
                double average = percentages.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
                double best = percentages.stream().mapToDouble(Double::doubleValue).max().orElse(0.0);
                double worst = percentages.stream().mapToDouble(Double::doubleValue).min().orElse(0.0);
                
                performanceMap.put(habitName, new HabitPerformance(average, best, worst, percentages.size()));
            }
        }
        
        // Cache the result
        habitDataCache.put(days, performanceMap);
        
        return performanceMap;
    }
    
    /**
     * Gets mood history data for the specified time range
     * @param days Number of days to include
     * @return List of mood data points
     */
    public List<MoodDataPoint> getMoodHistoryData(int days) {
        // Check cache first
        if (moodDataCache.containsKey(days)) {
            return moodDataCache.get(days);
        }
        
        LocalDate startDate = LocalDate.now().minusDays(days - 1);
        List<MoodDataPoint> dataPoints = new ArrayList<>();
        
        for (Map.Entry<LocalDate, PetMood> entry : moodHistory.entrySet()) {
            LocalDate date = entry.getKey();
            if (!date.isBefore(startDate) && !date.isAfter(LocalDate.now())) {
                dataPoints.add(new MoodDataPoint(date, entry.getValue()));
            }
        }
        
        // Sort by date (most recent first for timeline display)
        dataPoints.sort((a, b) -> b.getDate().compareTo(a.getDate()));
        
        // Cache the result
        moodDataCache.put(days, dataPoints);
        
        return dataPoints;
    }
    
    /**
     * Calculates daily XP based on habit completion
     */
    private int calculateDailyXP(DailyHabits habits) {
        // Simple XP calculation based on completion percentage
        double completion = habits.getCompletionPercentage();
        
        if (completion >= 0.8) {
            return 20; // Excellent
        } else if (completion >= 0.6) {
            return 15; // Good
        } else if (completion >= 0.4) {
            return 10; // Fair
        } else if (completion >= 0.2) {
            return 5;  // Poor
        } else {
            return 2;  // Minimal
        }
    }
    
    /**
     * Calculates completion percentage for a specific habit
     */
    private double calculateHabitPercentage(DailyHabits habits, String habitName) {
        // Define target values for percentage calculation
        Map<String, Double> targets = Map.of(
            "studyHours", 8.0,
            "waterIntake", 2.5,
            "stepsTaken", 10000.0,
            "sleepHours", 8.0,
            "moneySpent", 50.0,  // Lower is better
            "goalsCompleted", 5.0
        );
        
        double target = targets.getOrDefault(habitName, 1.0);
        double value = switch (habitName) {
            case "studyHours" -> habits.getStudyHours();
            case "waterIntake" -> habits.getWaterIntake();
            case "stepsTaken" -> habits.getStepsTaken();
            case "sleepHours" -> habits.getSleepHours();
            case "moneySpent" -> habits.getMoneySpent();
            case "goalsCompleted" -> habits.getGoalsCompleted();
            default -> 0.0;
        };
        
        if (habitName.equals("moneySpent")) {
            // For spending, lower is better
            return Math.max(0.0, Math.min(1.0, 1.0 - (value - target) / target));
        } else {
            // For other habits, higher is better
            return Math.min(1.0, value / target);
        }
    }
    
    /**
     * Updates summary text properties
     */
    private void updateSummaryTexts() {
        int days = selectedTimeRange.get();
        
        // Update XP summary
        List<XPDataPoint> xpData = getXPProgressData(days);
        if (!xpData.isEmpty()) {
            int totalXP = xpData.get(xpData.size() - 1).getXp();
            int startXP = xpData.isEmpty() ? 0 : xpData.get(0).getXp();
            int gainedXP = totalXP - startXP;
            xpSummaryText.set(String.format("Total XP: %d (+%d in last %d days)", totalXP, gainedXP, days));
        }
        
        // Update mood summary
        List<MoodDataPoint> moodData = getMoodHistoryData(days);
        if (!moodData.isEmpty()) {
            PetMood currentMood = moodData.get(0).getMood(); // Most recent
            int streakLength = calculateMoodStreak(moodData, currentMood);
            moodSummaryText.set(String.format("Current mood: %s (streak: %d days)", 
                currentMood.getDisplayName(), streakLength));
        }
    }
    
    /**
     * Calculates the length of the current mood streak
     */
    private int calculateMoodStreak(List<MoodDataPoint> moodData, PetMood targetMood) {
        int streak = 0;
        for (MoodDataPoint dataPoint : moodData) {
            if (dataPoint.getMood() == targetMood) {
                streak++;
            } else {
                break;
            }
        }
        return streak;
    }
    
    /**
     * Clears all data caches to force refresh
     */
    public void clearCache() {
        xpDataCache.clear();
        habitDataCache.clear();
        moodDataCache.clear();
    }
    
    /**
     * Adds new habit data and updates caches
     */
    public void addHabitData(DailyHabits habits, PetMood mood) {
        if (habits != null) {
            habitHistory.add(habits);
            habitHistory.sort(Comparator.comparing(DailyHabits::getDate));
        }
        
        if (mood != null && habits != null) {
            moodHistory.put(habits.getDate(), mood);
        }
        
        // Clear caches to force refresh
        clearCache();
        updateSummaryTexts();
    }
    
    // Property getters and setters
    
    public IntegerProperty selectedTimeRangeProperty() {
        return selectedTimeRange;
    }
    
    public int getSelectedTimeRange() {
        return selectedTimeRange.get();
    }
    
    public void setSelectedTimeRange(int days) {
        selectedTimeRange.set(days);
    }
    
    public StringProperty xpSummaryTextProperty() {
        return xpSummaryText;
    }
    
    public String getXpSummaryText() {
        return xpSummaryText.get();
    }
    
    public StringProperty moodSummaryTextProperty() {
        return moodSummaryText;
    }
    
    public String getMoodSummaryText() {
        return moodSummaryText.get();
    }
    
    // Data classes
    
    /**
     * Represents a data point for XP progression charts
     */
    public static class XPDataPoint {
        private final LocalDate date;
        private final int xp;
        
        public XPDataPoint(LocalDate date, int xp) {
            this.date = date;
            this.xp = xp;
        }
        
        public LocalDate getDate() {
            return date;
        }
        
        public int getXp() {
            return xp;
        }
    }
    
    /**
     * Represents habit performance statistics
     */
    public static class HabitPerformance {
        private final double averagePercentage;
        private final double bestPercentage;
        private final double worstPercentage;
        private final int dataPoints;
        
        public HabitPerformance(double averagePercentage, double bestPercentage, double worstPercentage, int dataPoints) {
            this.averagePercentage = averagePercentage;
            this.bestPercentage = bestPercentage;
            this.worstPercentage = worstPercentage;
            this.dataPoints = dataPoints;
        }
        
        public double getAveragePercentage() {
            return averagePercentage;
        }
        
        public double getBestPercentage() {
            return bestPercentage;
        }
        
        public double getWorstPercentage() {
            return worstPercentage;
        }
        
        public int getDataPoints() {
            return dataPoints;
        }
    }
    
    /**
     * Represents a data point for mood history timeline
     */
    public static class MoodDataPoint {
        private final LocalDate date;
        private final PetMood mood;
        
        public MoodDataPoint(LocalDate date, PetMood mood) {
            this.date = date;
            this.mood = mood;
        }
        
        public LocalDate getDate() {
            return date;
        }
        
        public PetMood getMood() {
            return mood;
        }
    }
}