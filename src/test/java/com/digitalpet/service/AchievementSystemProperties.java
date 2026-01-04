package com.digitalpet.service;

import com.digitalpet.model.*;
import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;
import net.jqwik.api.constraints.StringLength;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based tests for AchievementSystem service
 * Feature: digital-pet-evolution
 * 
 * Tests Property 14: Achievement System Functionality
 * Validates: Requirements 8.1, 8.2, 8.4, 8.5
 */
class AchievementSystemProperties {

    @Property
    @Label("Feature: digital-pet-evolution, Property 14: Achievement System Functionality")
    void achievementSystemTracksProgressAndUnlocksCorrectly(
            @ForAll @StringLength(min = 1, max = 20) String petName,
            @ForAll @IntRange(min = 0, max = 1500) int totalXP,
            @ForAll("evolutionStages") EvolutionStage petStage,
            @ForAll("habitHistoryList") List<DailyHabits> habitHistory) {
        
        // Create achievement system
        AchievementSystem achievementSystem = new AchievementSystem();
        
        // Create digital pet with given parameters
        DigitalPet pet = new DigitalPet(petName);
        pet.setExperiencePoints(totalXP);
        pet.setCurrentStage(petStage);
        
        // Create user progress
        UserProgress userProgress = new UserProgress(pet, habitHistory);
        
        // Requirement 8.1: Achievement display with locked/unlocked states
        List<Achievement> allAchievements = achievementSystem.getAllAchievements();
        assertFalse(allAchievements.isEmpty(), "Achievement system must have predefined achievements");
        
        // Verify all achievements have valid states
        for (Achievement achievement : allAchievements) {
            assertNotNull(achievement.getId(), "Achievement must have valid ID");
            assertNotNull(achievement.getTitle(), "Achievement must have valid title");
            assertNotNull(achievement.getDescription(), "Achievement must have valid description");
            assertNotNull(achievement.getCategory(), "Achievement must have valid category");
            
            // Progress should be between 0.0 and 1.0
            double progress = achievement.getProgress();
            assertTrue(progress >= 0.0 && progress <= 1.0, 
                    "Achievement progress must be between 0.0 and 1.0");
            
            // Current and target values should be non-negative
            assertTrue(achievement.getCurrentValue() >= 0, 
                    "Achievement current value must be non-negative");
            assertTrue(achievement.getTargetValue() > 0, 
                    "Achievement target value must be positive");
        }
        
        // Requirement 8.2: Progress indicators for locked achievements
        List<Achievement> lockedAchievements = achievementSystem.getLockedAchievements();
        for (Achievement locked : lockedAchievements) {
            assertFalse(locked.isUnlocked(), "Locked achievements must not be unlocked");
            
            // Progress indicators should be meaningful
            String progressText = locked.getProgressText();
            assertNotNull(progressText, "Locked achievements must have progress text");
            assertTrue(progressText.contains("%") || progressText.equals("Unlocked"), 
                    "Progress text must show percentage or completion status");
            
            String progressWithValues = locked.getProgressWithValues();
            assertNotNull(progressWithValues, "Locked achievements must have progress with values");
        }
        
        // Requirement 8.4: Achievement unlock detection and progress tracking
        int initialUnlockedCount = achievementSystem.getUnlockedAchievementCount();
        
        // Check for unlocks based on user progress
        List<Achievement> newUnlocks = achievementSystem.checkForUnlocks(userProgress);
        assertNotNull(newUnlocks, "checkForUnlocks must return non-null list");
        
        int finalUnlockedCount = achievementSystem.getUnlockedAchievementCount();
        
        // Unlocked count should increase by the number of new unlocks
        assertEquals(initialUnlockedCount + newUnlocks.size(), finalUnlockedCount,
                "Unlocked count should increase by number of new unlocks");
        
        // All newly unlocked achievements should be marked as unlocked
        for (Achievement newUnlock : newUnlocks) {
            assertTrue(newUnlock.isUnlocked(), "Newly unlocked achievements must be marked as unlocked");
            assertNotNull(newUnlock.getUnlockedDate(), "Unlocked achievements must have unlock date");
        }
        
        // Requirement 8.4: Three achievement categories must be represented
        Map<AchievementCategory, Integer> categoryBreakdown = 
                (Map<AchievementCategory, Integer>) achievementSystem.getAchievementStatistics().get("categoryBreakdown");
        
        assertTrue(categoryBreakdown.containsKey(AchievementCategory.CONSISTENCY),
                "System must have consistency achievements");
        assertTrue(categoryBreakdown.containsKey(AchievementCategory.MILESTONE),
                "System must have milestone achievements");
        assertTrue(categoryBreakdown.containsKey(AchievementCategory.EVOLUTION),
                "System must have evolution achievements");
        
        // Each category should have at least one achievement
        List<Achievement> consistencyAchievements = achievementSystem.getAchievementsByCategory(AchievementCategory.CONSISTENCY);
        List<Achievement> milestoneAchievements = achievementSystem.getAchievementsByCategory(AchievementCategory.MILESTONE);
        List<Achievement> evolutionAchievements = achievementSystem.getAchievementsByCategory(AchievementCategory.EVOLUTION);
        
        assertFalse(consistencyAchievements.isEmpty(), "Must have consistency achievements");
        assertFalse(milestoneAchievements.isEmpty(), "Must have milestone achievements");
        assertFalse(evolutionAchievements.isEmpty(), "Must have evolution achievements");
        
        // Verify category filtering works correctly
        for (Achievement achievement : consistencyAchievements) {
            assertEquals(AchievementCategory.CONSISTENCY, achievement.getCategory(),
                    "Consistency category filter must return only consistency achievements");
        }
        for (Achievement achievement : milestoneAchievements) {
            assertEquals(AchievementCategory.MILESTONE, achievement.getCategory(),
                    "Milestone category filter must return only milestone achievements");
        }
        for (Achievement achievement : evolutionAchievements) {
            assertEquals(AchievementCategory.EVOLUTION, achievement.getCategory(),
                    "Evolution category filter must return only evolution achievements");
        }
        
        // Requirement 8.5: Achievement statistics and completion tracking
        Map<String, Object> stats = achievementSystem.getAchievementStatistics();
        
        assertNotNull(stats.get("total"), "Statistics must include total count");
        assertNotNull(stats.get("unlocked"), "Statistics must include unlocked count");
        assertNotNull(stats.get("locked"), "Statistics must include locked count");
        assertNotNull(stats.get("completionPercentage"), "Statistics must include completion percentage");
        
        int totalStats = (Integer) stats.get("total");
        int unlockedStats = (Integer) stats.get("unlocked");
        int lockedStats = (Integer) stats.get("locked");
        double completionPercentageStats = (Double) stats.get("completionPercentage");
        
        assertEquals(totalStats, unlockedStats + lockedStats,
                "Total achievements must equal unlocked plus locked");
        
        if (totalStats > 0) {
            double expectedPercentage = (double) unlockedStats / totalStats;
            assertEquals(expectedPercentage, completionPercentageStats, 0.001,
                    "Completion percentage must be calculated correctly");
        } else {
            assertEquals(0.0, completionPercentageStats, 0.001,
                    "Completion percentage must be 0 when no achievements exist");
        }
        
        // Verify completion percentage property binding
        double systemCompletionPercentage = achievementSystem.getCompletionPercentage();
        assertEquals(completionPercentageStats, systemCompletionPercentage, 0.001,
                "System completion percentage must match statistics");
    }

    @Property
    @Label("Feature: digital-pet-evolution, Property 14: Achievement Progress Updates")
    void achievementProgressUpdatesCorrectlyWithUserProgress(
            @ForAll @IntRange(min = 0, max = 50) int studyHours,
            @ForAll @IntRange(min = 0, max = 100000) int totalSteps,
            @ForAll @IntRange(min = 0, max = 100) int totalGoals,
            @ForAll @IntRange(min = 0, max = 30) int streakLength) {
        
        AchievementSystem achievementSystem = new AchievementSystem();
        
        // Create mock user progress with specific values
        DigitalPet pet = new DigitalPet("Test Pet");
        pet.setExperiencePoints(500); // Adult stage
        
        // Create habit history that would generate the specified totals and streaks
        List<DailyHabits> habitHistory = createHabitHistoryWithTotals(
                studyHours, totalSteps, totalGoals, streakLength);
        
        UserProgress userProgress = new UserProgress(pet, habitHistory);
        
        // Check for unlocks and verify progress updates
        achievementSystem.checkForUnlocks(userProgress);
        
        // Verify specific achievement progress based on user data
        Achievement fitnessChamp = achievementSystem.getAchievement("fitness_champ");
        if (fitnessChamp != null) {
            assertEquals(totalSteps, fitnessChamp.getCurrentValue(),
                    "Fitness Champ achievement should track total steps correctly");
            
            double expectedProgress = Math.min(1.0, (double) totalSteps / fitnessChamp.getTargetValue());
            assertEquals(expectedProgress, fitnessChamp.getProgress(), 0.001,
                    "Fitness Champ progress should be calculated correctly");
        }
        
        Achievement knowledgeSeeker = achievementSystem.getAchievement("knowledge_seeker");
        if (knowledgeSeeker != null) {
            assertEquals(studyHours, knowledgeSeeker.getCurrentValue(),
                    "Knowledge Seeker achievement should track study hours correctly");
        }
        
        Achievement goalMaster = achievementSystem.getAchievement("goal_master");
        if (goalMaster != null) {
            assertEquals(totalGoals, goalMaster.getCurrentValue(),
                    "Goal Master achievement should track total goals correctly");
        }
        
        // Verify streak-based achievements
        Achievement consistencyChampion = achievementSystem.getAchievement("consistency_champion");
        if (consistencyChampion != null && streakLength > 0) {
            assertTrue(consistencyChampion.getCurrentValue() >= 0,
                    "Consistency Champion should track streak progress");
        }
    }

    @Property
    @Label("Feature: digital-pet-evolution, Property 14: Achievement System Integrity")
    void achievementSystemMaintainsIntegrityAcrossOperations(
            @ForAll @IntRange(min = 1, max = 10) int operationCount) {
        
        AchievementSystem achievementSystem = new AchievementSystem();
        
        // Verify initial system integrity
        assertTrue(achievementSystem.validateAchievementSystem(),
                "Achievement system must be valid after initialization");
        
        int initialTotal = achievementSystem.getTotalAchievements();
        assertTrue(initialTotal > 0, "System must have predefined achievements");
        
        // Perform multiple operations and verify integrity is maintained
        for (int i = 0; i < operationCount; i++) {
            // Create varying user progress
            DigitalPet pet = new DigitalPet("Pet" + i);
            pet.setExperiencePoints(i * 100);
            
            List<DailyHabits> habits = createRandomHabitHistory(i + 1);
            UserProgress progress = new UserProgress(pet, habits);
            
            // Check for unlocks
            List<Achievement> unlocks = achievementSystem.checkForUnlocks(progress);
            
            // Verify system integrity after each operation
            assertTrue(achievementSystem.validateAchievementSystem(),
                    "Achievement system integrity must be maintained after operations");
            
            // Verify counts are consistent
            int total = achievementSystem.getTotalAchievements();
            int unlocked = achievementSystem.getUnlockedAchievementCount();
            
            assertEquals(initialTotal, total,
                    "Total achievement count should remain constant");
            assertTrue(unlocked >= 0 && unlocked <= total,
                    "Unlocked count must be within valid range");
            
            // Verify completion percentage is valid
            double completion = achievementSystem.getCompletionPercentage();
            assertTrue(completion >= 0.0 && completion <= 1.0,
                    "Completion percentage must be between 0.0 and 1.0");
        }
    }

    // Helper methods for generating test data

    private List<DailyHabits> createHabitHistoryWithTotals(int studyHours, int totalSteps, 
                                                          int totalGoals, int streakLength) {
        List<DailyHabits> history = new ArrayList<>();
        LocalDate currentDate = LocalDate.now();
        
        // Create enough days to achieve the totals
        int daysNeeded = Math.max(10, streakLength);
        
        // Calculate daily values to achieve the exact totals
        int dailyStudyHours = daysNeeded > 0 ? studyHours / daysNeeded : 0;
        int remainingStudyHours = daysNeeded > 0 ? studyHours % daysNeeded : 0;
        
        int dailySteps = daysNeeded > 0 ? totalSteps / daysNeeded : 0;
        int remainingSteps = daysNeeded > 0 ? totalSteps % daysNeeded : 0;
        
        int dailyGoals = daysNeeded > 0 ? totalGoals / daysNeeded : 0;
        int remainingGoals = daysNeeded > 0 ? totalGoals % daysNeeded : 0;
        
        for (int i = 0; i < daysNeeded; i++) {
            DailyHabits habits = new DailyHabits(currentDate.minusDays(i));
            
            // Distribute the totals evenly, with remainder in first few days
            int dayStudy = dailyStudyHours + (i < remainingStudyHours ? 1 : 0);
            int daySteps = dailySteps + (i < remainingSteps ? 1 : 0);
            int dayGoals = dailyGoals + (i < remainingGoals ? 1 : 0);
            
            if (i < streakLength) {
                // For streak days, use calculated values but ensure they meet streak thresholds if > 0
                habits.setStudyHours(dayStudy);
                habits.setStepsTaken(daySteps);
                habits.setGoalsCompleted(dayGoals);
                habits.setWaterIntake(2.5);
                habits.setSleepHours(8.0);
                habits.setMoneySpent(25.0);
            } else {
                // For non-streak days, use exact calculated values
                habits.setStudyHours(dayStudy);
                habits.setStepsTaken(daySteps);
                habits.setGoalsCompleted(dayGoals);
                habits.setWaterIntake(1.0);
                habits.setSleepHours(6.0);
                habits.setMoneySpent(10.0);
            }
            
            history.add(habits);
        }
        
        return history;
    }

    private List<DailyHabits> createRandomHabitHistory(int days) {
        List<DailyHabits> history = new ArrayList<>();
        LocalDate currentDate = LocalDate.now();
        
        for (int i = 0; i < days; i++) {
            DailyHabits habits = new DailyHabits(currentDate.minusDays(i));
            
            // Set reasonable random values
            habits.setStudyHours(i % 8 + 1);
            habits.setStepsTaken((i % 10 + 1) * 1000);
            habits.setGoalsCompleted(i % 5 + 1);
            habits.setWaterIntake(1.5 + (i % 3));
            habits.setSleepHours(6.0 + (i % 4));
            habits.setMoneySpent(20.0 + (i % 30));
            
            history.add(habits);
        }
        
        return history;
    }

    @Provide
    Arbitrary<EvolutionStage> evolutionStages() {
        return Arbitraries.of(EvolutionStage.values());
    }

    @Provide
    Arbitrary<List<DailyHabits>> habitHistoryList() {
        return Arbitraries.integers().between(1, 30)
                .flatMap(size -> {
                    List<DailyHabits> habits = new ArrayList<>();
                    LocalDate currentDate = LocalDate.now();
                    
                    for (int i = 0; i < size; i++) {
                        DailyHabits dailyHabits = new DailyHabits(currentDate.minusDays(i));
                        
                        // Set reasonable values for property testing
                        dailyHabits.setStudyHours(i % 12);
                        dailyHabits.setStepsTaken((i % 15 + 1) * 1000);
                        dailyHabits.setGoalsCompleted(i % 8);
                        dailyHabits.setWaterIntake(1.0 + (i % 4) * 0.5);
                        dailyHabits.setSleepHours(5.0 + (i % 6));
                        dailyHabits.setMoneySpent(10.0 + (i % 50));
                        
                        habits.add(dailyHabits);
                    }
                    
                    return Arbitraries.just(habits);
                });
    }
}