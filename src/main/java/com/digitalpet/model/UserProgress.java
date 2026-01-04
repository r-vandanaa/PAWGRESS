package com.digitalpet.model;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * Model representing comprehensive user progress data for achievement evaluation.
 * Aggregates data from pet evolution, habit tracking, and consistency metrics.
 * 
 * Requirements addressed:
 * - 8.4: Achievement unlock detection based on user progress
 */
public class UserProgress {
    
    // Pet-related progress
    private final DigitalPet pet;
    private final List<DailyHabits> habitHistory;
    
    // Calculated metrics
    private final Map<String, Integer> totalValues;
    private final Map<String, Integer> streakCounts;
    private final Map<AchievementCategory, Integer> categoryProgress;
    
    // Time-based tracking
    private final LocalDate startDate;
    private final LocalDate currentDate;
    private final int totalDaysTracked;
    
    /**
     * Creates a new UserProgress instance
     * @param pet The digital pet
     * @param habitHistory List of daily habits (chronological order, most recent first)
     */
    public UserProgress(DigitalPet pet, List<DailyHabits> habitHistory) {
        this.pet = pet;
        this.habitHistory = habitHistory != null ? habitHistory : List.of();
        this.currentDate = LocalDate.now();
        
        // Calculate derived metrics
        this.totalValues = calculateTotalValues();
        this.streakCounts = calculateStreakCounts();
        this.categoryProgress = calculateCategoryProgress();
        
        // Determine tracking period
        this.startDate = determineStartDate();
        this.totalDaysTracked = calculateTotalDaysTracked();
    }
    
    /**
     * Calculates total values for each habit category
     */
    private Map<String, Integer> calculateTotalValues() {
        Map<String, Integer> totals = new HashMap<>();
        
        // Initialize totals
        totals.put("totalStudyHours", 0);
        totals.put("totalWaterLiters", 0);
        totals.put("totalSteps", 0);
        totals.put("totalSleepHours", 0);
        totals.put("totalMoneySpent", 0);
        totals.put("totalGoalsCompleted", 0);
        
        // Sum up all habit values
        for (DailyHabits habits : habitHistory) {
            if (habits != null) {
                totals.put("totalStudyHours", totals.get("totalStudyHours") + habits.getStudyHours());
                totals.put("totalWaterLiters", totals.get("totalWaterLiters") + (int) habits.getWaterIntake());
                totals.put("totalSteps", totals.get("totalSteps") + habits.getStepsTaken());
                totals.put("totalSleepHours", totals.get("totalSleepHours") + (int) habits.getSleepHours());
                totals.put("totalMoneySpent", totals.get("totalMoneySpent") + (int) habits.getMoneySpent());
                totals.put("totalGoalsCompleted", totals.get("totalGoalsCompleted") + habits.getGoalsCompleted());
            }
        }
        
        return totals;
    }
    
    /**
     * Calculates current streak counts for each habit category
     */
    private Map<String, Integer> calculateStreakCounts() {
        Map<String, Integer> streaks = new HashMap<>();
        
        // Initialize streaks
        streaks.put("studyStreak", 0);
        streaks.put("waterStreak", 0);
        streaks.put("stepsStreak", 0);
        streaks.put("sleepStreak", 0);
        streaks.put("goalsStreak", 0);
        streaks.put("overallStreak", 0);
        
        // Calculate streaks (most recent first)
        for (DailyHabits habits : habitHistory) {
            if (habits == null) {
                break; // Streak broken by missing data
            }
            
            // Define minimum thresholds for streak counting
            boolean studyMet = habits.getStudyHours() >= 4; // 4+ hours study
            boolean waterMet = habits.getWaterIntake() >= 2.0; // 2+ liters water
            boolean stepsMet = habits.getStepsTaken() >= 8000; // 8k+ steps
            boolean sleepMet = habits.getSleepHours() >= 6.0 && habits.getSleepHours() <= 10.0; // 6-10 hours sleep
            boolean goalsMet = habits.getGoalsCompleted() >= 3; // 3+ goals
            
            // Update individual streaks
            if (studyMet) {
                streaks.put("studyStreak", streaks.get("studyStreak") + 1);
            } else if (streaks.get("studyStreak") == 0) {
                // Only break if we haven't started counting yet
            } else {
                break; // Streak broken
            }
            
            if (waterMet) {
                streaks.put("waterStreak", streaks.get("waterStreak") + 1);
            }
            
            if (stepsMet) {
                streaks.put("stepsStreak", streaks.get("stepsStreak") + 1);
            }
            
            if (sleepMet) {
                streaks.put("sleepStreak", streaks.get("sleepStreak") + 1);
            }
            
            if (goalsMet) {
                streaks.put("goalsStreak", streaks.get("goalsStreak") + 1);
            }
            
            // Overall streak requires reasonable completion (50%+)
            if (habits.getCompletionPercentage() >= 0.5) {
                streaks.put("overallStreak", streaks.get("overallStreak") + 1);
            } else {
                break; // Overall streak broken
            }
        }
        
        return streaks;
    }
    
    /**
     * Calculates progress for each achievement category
     */
    private Map<AchievementCategory, Integer> calculateCategoryProgress() {
        Map<AchievementCategory, Integer> progress = new HashMap<>();
        
        // Consistency category: based on current streaks
        int maxStreak = streakCounts.values().stream().mapToInt(Integer::intValue).max().orElse(0);
        progress.put(AchievementCategory.CONSISTENCY, maxStreak);
        
        // Milestone category: based on total achievements
        int totalMilestones = 0;
        if (getTotalStudyHours() >= 100) totalMilestones++;
        if (getTotalSteps() >= 100000) totalMilestones++;
        if (getTotalGoalsCompleted() >= 50) totalMilestones++;
        if (getTotalWaterLiters() >= 100) totalMilestones++;
        progress.put(AchievementCategory.MILESTONE, totalMilestones);
        
        // Evolution category: based on pet evolution stage
        int evolutionProgress = pet != null ? pet.getCurrentStage().ordinal() : 0;
        progress.put(AchievementCategory.EVOLUTION, evolutionProgress);
        
        return progress;
    }
    
    /**
     * Determines the start date of habit tracking
     */
    private LocalDate determineStartDate() {
        if (habitHistory.isEmpty()) {
            return currentDate;
        }
        
        // Find the earliest date in habit history
        return habitHistory.stream()
                .filter(habits -> habits != null && habits.getDate() != null)
                .map(DailyHabits::getDate)
                .min(LocalDate::compareTo)
                .orElse(currentDate);
    }
    
    /**
     * Calculates total days of habit tracking
     */
    private int calculateTotalDaysTracked() {
        if (habitHistory.isEmpty()) {
            return 0;
        }
        
        return (int) java.time.temporal.ChronoUnit.DAYS.between(startDate, currentDate) + 1;
    }
    
    // Getters for pet information
    
    public DigitalPet getPet() {
        return pet;
    }
    
    public List<DailyHabits> getHabitHistory() {
        return habitHistory;
    }
    
    // Getters for calculated totals
    
    public int getTotalStudyHours() {
        return totalValues.getOrDefault("totalStudyHours", 0);
    }
    
    public int getTotalWaterLiters() {
        return totalValues.getOrDefault("totalWaterLiters", 0);
    }
    
    public int getTotalSteps() {
        return totalValues.getOrDefault("totalSteps", 0);
    }
    
    public int getTotalSleepHours() {
        return totalValues.getOrDefault("totalSleepHours", 0);
    }
    
    public int getTotalMoneySpent() {
        return totalValues.getOrDefault("totalMoneySpent", 0);
    }
    
    public int getTotalGoalsCompleted() {
        return totalValues.getOrDefault("totalGoalsCompleted", 0);
    }
    
    // Getters for streak information
    
    public int getStudyStreak() {
        return streakCounts.getOrDefault("studyStreak", 0);
    }
    
    public int getWaterStreak() {
        return streakCounts.getOrDefault("waterStreak", 0);
    }
    
    public int getStepsStreak() {
        return streakCounts.getOrDefault("stepsStreak", 0);
    }
    
    public int getSleepStreak() {
        return streakCounts.getOrDefault("sleepStreak", 0);
    }
    
    public int getGoalsStreak() {
        return streakCounts.getOrDefault("goalsStreak", 0);
    }
    
    public int getOverallStreak() {
        return streakCounts.getOrDefault("overallStreak", 0);
    }
    
    public int getMaxStreak() {
        return streakCounts.values().stream().mapToInt(Integer::intValue).max().orElse(0);
    }
    
    // Getters for evolution information
    
    public EvolutionStage getCurrentEvolutionStage() {
        return pet != null ? pet.getCurrentStage() : EvolutionStage.EGG;
    }
    
    public int getCurrentXP() {
        return pet != null ? pet.getExperiencePoints() : 0;
    }
    
    public boolean hasReachedStage(EvolutionStage stage) {
        return getCurrentEvolutionStage().ordinal() >= stage.ordinal();
    }
    
    // Getters for time-based information
    
    public LocalDate getStartDate() {
        return startDate;
    }
    
    public LocalDate getCurrentDate() {
        return currentDate;
    }
    
    public int getTotalDaysTracked() {
        return totalDaysTracked;
    }
    
    // Getters for category progress
    
    public int getCategoryProgress(AchievementCategory category) {
        return categoryProgress.getOrDefault(category, 0);
    }
    
    public Map<AchievementCategory, Integer> getAllCategoryProgress() {
        return new HashMap<>(categoryProgress);
    }
    
    // Utility methods for achievement conditions
    
    /**
     * Checks if user has maintained a streak of specified length in any category
     */
    public boolean hasStreakOfLength(int days) {
        return getMaxStreak() >= days;
    }
    
    /**
     * Checks if user has maintained a specific category streak
     */
    public boolean hasSpecificStreak(String category, int days) {
        return streakCounts.getOrDefault(category, 0) >= days;
    }
    
    /**
     * Checks if user has reached a total milestone in any category
     */
    public boolean hasTotalMilestone(String category, int target) {
        return totalValues.getOrDefault(category, 0) >= target;
    }
    
    /**
     * Checks if user has been tracking for a minimum number of days
     */
    public boolean hasTrackedForDays(int days) {
        return getTotalDaysTracked() >= days;
    }
    
    /**
     * Gets average completion percentage over all tracked days
     */
    public double getAverageCompletionPercentage() {
        if (habitHistory.isEmpty()) {
            return 0.0;
        }
        
        double total = habitHistory.stream()
                .filter(habits -> habits != null)
                .mapToDouble(DailyHabits::getCompletionPercentage)
                .sum();
        
        return total / habitHistory.size();
    }
    
    /**
     * Gets completion percentage for recent days
     */
    public double getRecentCompletionPercentage(int days) {
        if (habitHistory.isEmpty()) {
            return 0.0;
        }
        
        int daysToCheck = Math.min(days, habitHistory.size());
        double total = 0.0;
        
        for (int i = 0; i < daysToCheck; i++) {
            DailyHabits habits = habitHistory.get(i);
            if (habits != null) {
                total += habits.getCompletionPercentage();
            }
        }
        
        return total / daysToCheck;
    }
    
    @Override
    public String toString() {
        return String.format("UserProgress{pet=%s, daysTracked=%d, maxStreak=%d, totalStudy=%d, evolutionStage=%s}", 
                pet != null ? pet.getName() : "null", 
                getTotalDaysTracked(), 
                getMaxStreak(), 
                getTotalStudyHours(), 
                getCurrentEvolutionStage());
    }
}