package com.digitalpet.service;

import com.digitalpet.model.*;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Service class responsible for managing achievements, tracking progress, and detecting unlocks.
 * Implements three achievement categories with unlock detection and celebration triggers.
 * 
 * Requirements addressed:
 * - 8.3: Three achievement categories (consistency, milestone, evolution)
 * - 8.4: Achievement unlock detection and celebration triggers
 */
public class AchievementSystem {
    
    // Achievement storage
    private final ObservableList<Achievement> allAchievements;
    private final Map<String, Achievement> achievementMap;
    
    // Recent unlock tracking for celebrations
    private final List<Achievement> recentUnlocks;
    private LocalDateTime lastUnlockCheck;
    
    // Properties for UI binding
    private final IntegerProperty totalAchievements;
    private final IntegerProperty unlockedAchievements;
    private final DoubleProperty completionPercentage;
    private final BooleanProperty hasRecentUnlock;
    
    public AchievementSystem() {
        this.allAchievements = FXCollections.observableArrayList();
        this.achievementMap = new HashMap<>();
        this.recentUnlocks = new ArrayList<>();
        this.lastUnlockCheck = LocalDateTime.now();
        
        // Initialize properties
        this.totalAchievements = new SimpleIntegerProperty(this, "totalAchievements", 0);
        this.unlockedAchievements = new SimpleIntegerProperty(this, "unlockedAchievements", 0);
        this.completionPercentage = new SimpleDoubleProperty(this, "completionPercentage", 0.0);
        this.hasRecentUnlock = new SimpleBooleanProperty(this, "hasRecentUnlock", false);
        
        // Initialize predefined achievements
        initializePredefinedAchievements();
        
        // Set up property listeners
        setupPropertyListeners();
    }
    
    /**
     * Initializes all predefined achievements for the system
     */
    private void initializePredefinedAchievements() {
        // Consistency-based achievements
        addAchievement(createWaterWarriorAchievement());
        addAchievement(createStudyStreakAchievement());
        addAchievement(createFitnessStreakAchievement());
        addAchievement(createSleepMasterAchievement());
        addAchievement(createGoalCrusherAchievement());
        addAchievement(createOverallConsistencyAchievement());
        
        // Milestone-based achievements
        addAchievement(createFitnessChampAchievement());
        addAchievement(createKnowledgeSeekerAchievement());
        addAchievement(createHydrationHeroAchievement());
        addAchievement(createGoalMasterAchievement());
        addAchievement(createDedicatedTrackerAchievement());
        
        // Evolution-based achievements
        addAchievement(createFirstEvolutionAchievement());
        addAchievement(createTeenAgeAchievement());
        addAchievement(createAdulthoodAchievement());
        addAchievement(createLegendaryStatusAchievement());
        addAchievement(createPetMasterAchievement());
    }
    
    // Consistency Achievement Creators
    
    private Achievement createWaterWarriorAchievement() {
        Predicate<UserProgress> condition = progress -> progress.getWaterStreak() >= 7;
        return new Achievement(
            "water_warrior",
            "Water Warrior",
            "Maintain proper hydration for 7 consecutive days",
            AchievementCategory.CONSISTENCY,
            7,
            condition
        );
    }
    
    private Achievement createStudyStreakAchievement() {
        Predicate<UserProgress> condition = progress -> progress.getStudyStreak() >= 14;
        return new Achievement(
            "study_streak",
            "Study Streak",
            "Study for 4+ hours daily for 14 consecutive days",
            AchievementCategory.CONSISTENCY,
            14,
            condition
        );
    }
    
    private Achievement createFitnessStreakAchievement() {
        Predicate<UserProgress> condition = progress -> progress.getStepsStreak() >= 10;
        return new Achievement(
            "fitness_streak",
            "Fitness Streak",
            "Walk 8000+ steps daily for 10 consecutive days",
            AchievementCategory.CONSISTENCY,
            10,
            condition
        );
    }
    
    private Achievement createSleepMasterAchievement() {
        Predicate<UserProgress> condition = progress -> progress.getSleepStreak() >= 7;
        return new Achievement(
            "sleep_master",
            "Sleep Master",
            "Maintain healthy sleep (6-10 hours) for 7 consecutive days",
            AchievementCategory.CONSISTENCY,
            7,
            condition
        );
    }
    
    private Achievement createGoalCrusherAchievement() {
        Predicate<UserProgress> condition = progress -> progress.getGoalsStreak() >= 5;
        return new Achievement(
            "goal_crusher",
            "Goal Crusher",
            "Complete 3+ daily goals for 5 consecutive days",
            AchievementCategory.CONSISTENCY,
            5,
            condition
        );
    }
    
    private Achievement createOverallConsistencyAchievement() {
        Predicate<UserProgress> condition = progress -> progress.getOverallStreak() >= 21;
        return new Achievement(
            "consistency_champion",
            "Consistency Champion",
            "Maintain 50%+ completion rate for 21 consecutive days",
            AchievementCategory.CONSISTENCY,
            21,
            condition
        );
    }
    
    // Milestone Achievement Creators
    
    private Achievement createFitnessChampAchievement() {
        Predicate<UserProgress> condition = progress -> progress.getTotalSteps() >= 100000;
        return new Achievement(
            "fitness_champ",
            "Fitness Champ",
            "Walk a total of 100,000 steps",
            AchievementCategory.MILESTONE,
            100000,
            condition
        );
    }
    
    private Achievement createKnowledgeSeekerAchievement() {
        Predicate<UserProgress> condition = progress -> progress.getTotalStudyHours() >= 100;
        return new Achievement(
            "knowledge_seeker",
            "Knowledge Seeker",
            "Study for a total of 100 hours",
            AchievementCategory.MILESTONE,
            100,
            condition
        );
    }
    
    private Achievement createHydrationHeroAchievement() {
        Predicate<UserProgress> condition = progress -> progress.getTotalWaterLiters() >= 100;
        return new Achievement(
            "hydration_hero",
            "Hydration Hero",
            "Drink a total of 100 liters of water",
            AchievementCategory.MILESTONE,
            100,
            condition
        );
    }
    
    private Achievement createGoalMasterAchievement() {
        Predicate<UserProgress> condition = progress -> progress.getTotalGoalsCompleted() >= 100;
        return new Achievement(
            "goal_master",
            "Goal Master",
            "Complete a total of 100 daily goals",
            AchievementCategory.MILESTONE,
            100,
            condition
        );
    }
    
    private Achievement createDedicatedTrackerAchievement() {
        Predicate<UserProgress> condition = progress -> progress.hasTrackedForDays(30);
        return new Achievement(
            "dedicated_tracker",
            "Dedicated Tracker",
            "Track habits for 30 days total",
            AchievementCategory.MILESTONE,
            30,
            condition
        );
    }
    
    // Evolution Achievement Creators
    
    private Achievement createFirstEvolutionAchievement() {
        Predicate<UserProgress> condition = progress -> progress.hasReachedStage(EvolutionStage.BABY);
        return new Achievement(
            "first_evolution",
            "First Evolution",
            "Evolve your pet from Egg to Baby stage",
            AchievementCategory.EVOLUTION,
            1,
            condition
        );
    }
    
    private Achievement createTeenAgeAchievement() {
        Predicate<UserProgress> condition = progress -> progress.hasReachedStage(EvolutionStage.TEEN);
        return new Achievement(
            "teen_age",
            "Teen Age",
            "Evolve your pet to Teen stage",
            AchievementCategory.EVOLUTION,
            1,
            condition
        );
    }
    
    private Achievement createAdulthoodAchievement() {
        Predicate<UserProgress> condition = progress -> progress.hasReachedStage(EvolutionStage.ADULT);
        return new Achievement(
            "adulthood",
            "Adulthood",
            "Evolve your pet to Adult stage",
            AchievementCategory.EVOLUTION,
            1,
            condition
        );
    }
    
    private Achievement createLegendaryStatusAchievement() {
        Predicate<UserProgress> condition = progress -> progress.hasReachedStage(EvolutionStage.LEGENDARY);
        return new Achievement(
            "legendary_status",
            "Legendary Status",
            "Evolve your pet to Legendary stage",
            AchievementCategory.EVOLUTION,
            1,
            condition
        );
    }
    
    private Achievement createPetMasterAchievement() {
        Predicate<UserProgress> condition = progress -> 
            progress.hasReachedStage(EvolutionStage.LEGENDARY) && 
            progress.getMaxStreak() >= 30 &&
            progress.getTotalDaysTracked() >= 60;
        return new Achievement(
            "pet_master",
            "Pet Master",
            "Reach Legendary stage with 30+ day streak and 60+ days tracked",
            AchievementCategory.EVOLUTION,
            1,
            condition
        );
    }
    
    /**
     * Sets up property listeners for automatic updates
     */
    private void setupPropertyListeners() {
        // Update counts when achievements list changes
        allAchievements.addListener((javafx.collections.ListChangeListener<Achievement>) change -> {
            updateAchievementCounts();
        });
    }
    
    /**
     * Updates achievement count properties
     */
    private void updateAchievementCounts() {
        int total = allAchievements.size();
        int unlocked = (int) allAchievements.stream().filter(Achievement::isUnlocked).count();
        
        totalAchievements.set(total);
        unlockedAchievements.set(unlocked);
        
        if (total > 0) {
            completionPercentage.set((double) unlocked / total);
        } else {
            completionPercentage.set(0.0);
        }
    }
    
    /**
     * Adds an achievement to the system
     */
    public void addAchievement(Achievement achievement) {
        if (achievement != null && !achievementMap.containsKey(achievement.getId())) {
            allAchievements.add(achievement);
            achievementMap.put(achievement.getId(), achievement);
            updateAchievementCounts();
        }
    }
    
    /**
     * Removes an achievement from the system
     */
    public void removeAchievement(String achievementId) {
        Achievement achievement = achievementMap.remove(achievementId);
        if (achievement != null) {
            allAchievements.remove(achievement);
            updateAchievementCounts();
        }
    }
    
    /**
     * Gets an achievement by ID
     */
    public Achievement getAchievement(String achievementId) {
        return achievementMap.get(achievementId);
    }
    
    /**
     * Checks all achievements for potential unlocks and updates progress
     * @param userProgress Current user progress data
     * @return List of newly unlocked achievements
     */
    public List<Achievement> checkForUnlocks(UserProgress userProgress) {
        if (userProgress == null) {
            return List.of();
        }
        
        List<Achievement> newUnlocks = new ArrayList<>();
        
        for (Achievement achievement : allAchievements) {
            if (!achievement.isUnlocked()) {
                // Update progress values for display
                updateAchievementProgress(achievement, userProgress);
                
                // Check for unlock
                if (achievement.tryUnlock(userProgress)) {
                    newUnlocks.add(achievement);
                    recentUnlocks.add(achievement);
                }
            }
        }
        
        // Update recent unlock status
        if (!newUnlocks.isEmpty()) {
            hasRecentUnlock.set(true);
            lastUnlockCheck = LocalDateTime.now();
        }
        
        updateAchievementCounts();
        return newUnlocks;
    }
    
    /**
     * Updates progress values for an achievement based on user progress
     */
    private void updateAchievementProgress(Achievement achievement, UserProgress userProgress) {
        String achievementId = achievement.getId();
        
        // Update current values based on achievement type
        switch (achievementId) {
            case "water_warrior" -> achievement.setCurrentValue(userProgress.getWaterStreak());
            case "study_streak" -> achievement.setCurrentValue(userProgress.getStudyStreak());
            case "fitness_streak" -> achievement.setCurrentValue(userProgress.getStepsStreak());
            case "sleep_master" -> achievement.setCurrentValue(userProgress.getSleepStreak());
            case "goal_crusher" -> achievement.setCurrentValue(userProgress.getGoalsStreak());
            case "consistency_champion" -> achievement.setCurrentValue(userProgress.getOverallStreak());
            case "fitness_champ" -> achievement.setCurrentValue(userProgress.getTotalSteps());
            case "knowledge_seeker" -> achievement.setCurrentValue(userProgress.getTotalStudyHours());
            case "hydration_hero" -> achievement.setCurrentValue(userProgress.getTotalWaterLiters());
            case "goal_master" -> achievement.setCurrentValue(userProgress.getTotalGoalsCompleted());
            case "dedicated_tracker" -> achievement.setCurrentValue(userProgress.getTotalDaysTracked());
            case "first_evolution", "teen_age", "adulthood", "legendary_status", "pet_master" -> {
                // Evolution achievements are binary (0 or 1)
                achievement.setCurrentValue(achievement.shouldUnlock(userProgress) ? 1 : 0);
            }
        }
    }
    
    /**
     * Gets all achievements
     */
    public ObservableList<Achievement> getAllAchievements() {
        return allAchievements;
    }
    
    /**
     * Gets achievements by category
     */
    public List<Achievement> getAchievementsByCategory(AchievementCategory category) {
        return allAchievements.stream()
                .filter(achievement -> achievement.getCategory() == category)
                .collect(Collectors.toList());
    }
    
    /**
     * Gets unlocked achievements
     */
    public List<Achievement> getUnlockedAchievements() {
        return allAchievements.stream()
                .filter(Achievement::isUnlocked)
                .collect(Collectors.toList());
    }
    
    /**
     * Gets locked achievements
     */
    public List<Achievement> getLockedAchievements() {
        return allAchievements.stream()
                .filter(achievement -> !achievement.isUnlocked())
                .collect(Collectors.toList());
    }
    
    /**
     * Gets achievements sorted by progress (closest to completion first)
     */
    public List<Achievement> getAchievementsByProgress() {
        return allAchievements.stream()
                .sorted((a, b) -> Double.compare(b.getProgress(), a.getProgress()))
                .collect(Collectors.toList());
    }
    
    /**
     * Gets recent unlocks for celebration purposes
     */
    public List<Achievement> getRecentUnlocks() {
        return new ArrayList<>(recentUnlocks);
    }
    
    /**
     * Checks if there are recent unlocks (within last check)
     */
    public boolean hasRecentUnlocks() {
        return hasRecentUnlock.get();
    }
    
    /**
     * Clears recent unlock status (call after celebrations are shown)
     */
    public void clearRecentUnlocks() {
        recentUnlocks.clear();
        hasRecentUnlock.set(false);
    }
    
    /**
     * Gets the time of last unlock check
     */
    public LocalDateTime getLastUnlockCheck() {
        return lastUnlockCheck;
    }
    
    // Property getters for UI binding
    
    public IntegerProperty totalAchievementsProperty() {
        return totalAchievements;
    }
    
    public IntegerProperty unlockedAchievementsProperty() {
        return unlockedAchievements;
    }
    
    public DoubleProperty completionPercentageProperty() {
        return completionPercentage;
    }
    
    public BooleanProperty hasRecentUnlockProperty() {
        return hasRecentUnlock;
    }
    
    // Convenience getters
    
    public int getTotalAchievements() {
        return totalAchievements.get();
    }
    
    public int getUnlockedAchievementCount() {
        return unlockedAchievements.get();
    }
    
    public double getCompletionPercentage() {
        return completionPercentage.get();
    }
    
    /**
     * Gets achievement statistics for display
     */
    public Map<String, Object> getAchievementStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        stats.put("total", getTotalAchievements());
        stats.put("unlocked", getUnlockedAchievementCount());
        stats.put("locked", getTotalAchievements() - getUnlockedAchievementCount());
        stats.put("completionPercentage", getCompletionPercentage());
        
        // Category breakdown
        Map<AchievementCategory, Integer> categoryStats = new HashMap<>();
        for (AchievementCategory category : AchievementCategory.values()) {
            List<Achievement> categoryAchievements = getAchievementsByCategory(category);
            int unlocked = (int) categoryAchievements.stream().filter(Achievement::isUnlocked).count();
            categoryStats.put(category, unlocked);
        }
        stats.put("categoryBreakdown", categoryStats);
        
        // Recent activity
        stats.put("recentUnlocks", recentUnlocks.size());
        stats.put("hasRecentActivity", hasRecentUnlocks());
        
        return stats;
    }
    
    /**
     * Gets achievements that are close to completion (>= 75% progress)
     */
    public List<Achievement> getAlmostCompleteAchievements() {
        return allAchievements.stream()
                .filter(achievement -> !achievement.isUnlocked() && achievement.getProgress() >= 0.75)
                .sorted((a, b) -> Double.compare(b.getProgress(), a.getProgress()))
                .collect(Collectors.toList());
    }
    
    /**
     * Resets all achievements to locked state (for testing or new user)
     */
    public void resetAllAchievements() {
        for (Achievement achievement : allAchievements) {
            achievement.setUnlocked(false);
            achievement.setUnlockedDate(null);
            achievement.setCurrentValue(0);
        }
        clearRecentUnlocks();
        updateAchievementCounts();
    }
    
    /**
     * Validates achievement system integrity
     */
    public boolean validateAchievementSystem() {
        // Check for duplicate IDs
        Set<String> ids = new HashSet<>();
        for (Achievement achievement : allAchievements) {
            if (!ids.add(achievement.getId())) {
                return false; // Duplicate ID found
            }
        }
        
        // Check that all categories are represented
        Set<AchievementCategory> categories = allAchievements.stream()
                .map(Achievement::getCategory)
                .collect(Collectors.toSet());
        
        return categories.containsAll(Arrays.asList(AchievementCategory.values()));
    }
    
    @Override
    public String toString() {
        return String.format("AchievementSystem{total=%d, unlocked=%d, completion=%.1f%%}", 
                getTotalAchievements(), getUnlockedAchievementCount(), getCompletionPercentage() * 100);
    }
}