package com.digitalpet.service;

import com.digitalpet.model.*;
import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;
import net.jqwik.api.constraints.StringLength;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based tests for Achievement Celebration functionality
 * Feature: digital-pet-evolution
 * 
 * Tests Property 16: Achievement Celebration Consistency
 * Validates: Requirements 9.4
 */
class AchievementCelebrationProperties {

    @Property
    @Label("Feature: digital-pet-evolution, Property 16: Achievement Celebration Consistency")
    void achievementCelebrationsAreTriggeredConsistentlyForUnlocks(
            @ForAll @StringLength(min = 1, max = 20) String petName,
            @ForAll @IntRange(min = 0, max = 1500) int totalXP,
            @ForAll("evolutionStages") EvolutionStage petStage,
            @ForAll @IntRange(min = 1, max = 30) int habitDays) {
        
        // Create achievement system
        AchievementSystem achievementSystem = new AchievementSystem();
        
        // Create digital pet with given parameters
        DigitalPet pet = new DigitalPet(petName);
        pet.setExperiencePoints(totalXP);
        pet.setCurrentStage(petStage);
        
        // Create habit history that might trigger achievements
        List<DailyHabits> habitHistory = createProgressiveHabitHistory(habitDays);
        UserProgress userProgress = new UserProgress(pet, habitHistory);
        
        // Requirement 9.4: Achievement celebration consistency
        
        // Record initial state
        boolean initialHasRecentUnlock = achievementSystem.hasRecentUnlocks();
        List<Achievement> initialRecentUnlocks = new ArrayList<>(achievementSystem.getRecentUnlocks());
        LocalDateTime beforeCheck = LocalDateTime.now().minusSeconds(1);
        
        // Check for unlocks (this should trigger celebrations for any new unlocks)
        List<Achievement> newUnlocks = achievementSystem.checkForUnlocks(userProgress);
        
        LocalDateTime afterCheck = LocalDateTime.now().plusSeconds(1);
        
        // Verify celebration state consistency
        if (!newUnlocks.isEmpty()) {
            // If there are new unlocks, celebration state should be updated
            assertTrue(achievementSystem.hasRecentUnlocks(),
                    "Achievement system must indicate recent unlocks when achievements are unlocked");
            
            List<Achievement> currentRecentUnlocks = achievementSystem.getRecentUnlocks();
            assertFalse(currentRecentUnlocks.isEmpty(),
                    "Recent unlocks list must not be empty when achievements are unlocked");
            
            // All new unlocks should be in the recent unlocks list
            for (Achievement newUnlock : newUnlocks) {
                assertTrue(currentRecentUnlocks.contains(newUnlock),
                        "All newly unlocked achievements must be in recent unlocks list");
                
                // Verify unlock properties are set correctly
                assertTrue(newUnlock.isUnlocked(),
                        "Newly unlocked achievements must be marked as unlocked");
                
                assertNotNull(newUnlock.getUnlockedDate(),
                        "Newly unlocked achievements must have unlock date set");
                
                LocalDateTime unlockDate = newUnlock.getUnlockedDate();
                assertTrue(unlockDate.isAfter(beforeCheck) || unlockDate.isEqual(beforeCheck),
                        "Unlock date should be after or equal to before check time");
                assertTrue(unlockDate.isBefore(afterCheck) || unlockDate.isEqual(afterCheck),
                        "Unlock date should be before or equal to after check time");
                
                // Progress should be 1.0 for unlocked achievements
                assertEquals(1.0, newUnlock.getProgress(), 0.001,
                        "Unlocked achievements must have 100% progress");
            }
            
            // Recent unlocks should include previous unlocks plus new ones
            assertTrue(currentRecentUnlocks.size() >= newUnlocks.size(),
                    "Recent unlocks count should include at least the new unlocks");
            
        } else {
            // If no new unlocks, celebration state should remain unchanged or be cleared
            // (depending on whether there were previous recent unlocks)
            
            if (!initialHasRecentUnlock) {
                // If there were no initial recent unlocks and no new unlocks,
                // there should still be no recent unlocks
                assertFalse(achievementSystem.hasRecentUnlocks(),
                        "No recent unlocks should remain when no achievements are unlocked");
            }
            
            // Recent unlocks list should not grow
            List<Achievement> currentRecentUnlocks = achievementSystem.getRecentUnlocks();
            assertEquals(initialRecentUnlocks.size(), currentRecentUnlocks.size(),
                    "Recent unlocks list size should not change when no new achievements are unlocked");
        }
        
        // Verify celebration clearing functionality
        if (achievementSystem.hasRecentUnlocks()) {
            List<Achievement> recentUnlocksBeforeClear = new ArrayList<>(achievementSystem.getRecentUnlocks());
            assertFalse(recentUnlocksBeforeClear.isEmpty(),
                    "Recent unlocks list should not be empty when hasRecentUnlocks is true");
            
            // Clear recent unlocks
            achievementSystem.clearRecentUnlocks();
            
            // Verify celebration state is cleared
            assertFalse(achievementSystem.hasRecentUnlocks(),
                    "hasRecentUnlocks should be false after clearing recent unlocks");
            
            assertTrue(achievementSystem.getRecentUnlocks().isEmpty(),
                    "Recent unlocks list should be empty after clearing");
            
            // Verify that previously unlocked achievements remain unlocked
            for (Achievement previousUnlock : recentUnlocksBeforeClear) {
                assertTrue(previousUnlock.isUnlocked(),
                        "Previously unlocked achievements should remain unlocked after clearing celebrations");
                assertNotNull(previousUnlock.getUnlockedDate(),
                        "Previously unlocked achievements should retain their unlock date after clearing celebrations");
            }
        }
    }

    @Property
    @Label("Feature: digital-pet-evolution, Property 16: Achievement Celebration Timing Consistency")
    void achievementCelebrationTimingIsConsistent(
            @ForAll @IntRange(min = 1, max = 10) int unlockSequenceLength) {
        
        AchievementSystem achievementSystem = new AchievementSystem();
        
        // Create a sequence of user progress states that will unlock achievements
        List<LocalDateTime> unlockTimes = new ArrayList<>();
        List<Achievement> unlockedInSequence = new ArrayList<>();
        
        for (int i = 0; i < unlockSequenceLength; i++) {
            // Create progressively better user progress
            DigitalPet pet = new DigitalPet("TestPet");
            pet.setExperiencePoints(i * 200); // Increase XP to trigger evolution achievements
            
            // Create habit history with increasing totals
            List<DailyHabits> habits = createHabitHistoryWithTotals(
                    i * 20,      // study hours
                    i * 10000,   // steps
                    i * 10,      // goals
                    i + 1        // streak length
            );
            
            UserProgress progress = new UserProgress(pet, habits);
            
            LocalDateTime beforeUnlock = LocalDateTime.now();
            List<Achievement> newUnlocks = achievementSystem.checkForUnlocks(progress);
            LocalDateTime afterUnlock = LocalDateTime.now();
            
            // Record timing for any new unlocks
            for (Achievement unlock : newUnlocks) {
                unlockTimes.add(unlock.getUnlockedDate());
                unlockedInSequence.add(unlock);
                
                // Verify unlock timing is within reasonable bounds
                assertTrue(unlock.getUnlockedDate().isAfter(beforeUnlock.minusSeconds(1)),
                        "Unlock date should be after operation start time");
                assertTrue(unlock.getUnlockedDate().isBefore(afterUnlock.plusSeconds(1)),
                        "Unlock date should be before operation end time");
            }
            
            // Small delay to ensure time progression
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        // Verify unlock times are in chronological order (or at least not decreasing)
        for (int i = 1; i < unlockTimes.size(); i++) {
            LocalDateTime previousTime = unlockTimes.get(i - 1);
            LocalDateTime currentTime = unlockTimes.get(i);
            
            assertTrue(currentTime.isAfter(previousTime) || currentTime.isEqual(previousTime),
                    "Achievement unlock times should be in chronological order");
        }
        
        // Verify all unlocked achievements maintain their unlock dates
        for (Achievement achievement : unlockedInSequence) {
            assertNotNull(achievement.getUnlockedDate(),
                    "All unlocked achievements should maintain their unlock dates");
            assertTrue(achievement.isUnlocked(),
                    "All achievements in unlock sequence should remain unlocked");
        }
    }

    @Property
    @Label("Feature: digital-pet-evolution, Property 16: Achievement Celebration State Management")
    void achievementCelebrationStateIsProperlyManaged(
            @ForAll @IntRange(min = 1, max = 5) int operationCount) {
        
        AchievementSystem achievementSystem = new AchievementSystem();
        
        // Perform multiple operations and verify celebration state consistency
        for (int i = 0; i < operationCount; i++) {
            // Create user progress that might trigger unlocks
            DigitalPet pet = new DigitalPet("Pet" + i);
            pet.setExperiencePoints(i * 300);
            
            List<DailyHabits> habits = createHabitHistoryWithTotals(
                    i * 15,      // study hours  
                    i * 8000,    // steps
                    i * 8,       // goals
                    i + 2        // streak length
            );
            
            UserProgress progress = new UserProgress(pet, habits);
            
            // Record state before operation
            boolean hadRecentUnlocksBefore = achievementSystem.hasRecentUnlocks();
            int recentUnlocksCountBefore = achievementSystem.getRecentUnlocks().size();
            
            // Perform unlock check
            List<Achievement> newUnlocks = achievementSystem.checkForUnlocks(progress);
            
            // Verify state consistency after operation
            boolean hasRecentUnlocksAfter = achievementSystem.hasRecentUnlocks();
            int recentUnlocksCountAfter = achievementSystem.getRecentUnlocks().size();
            
            if (!newUnlocks.isEmpty()) {
                // If new unlocks occurred, recent unlock state should be true
                assertTrue(hasRecentUnlocksAfter,
                        "hasRecentUnlocks should be true when new achievements are unlocked");
                
                assertTrue(recentUnlocksCountAfter >= newUnlocks.size(),
                        "Recent unlocks count should include at least the new unlocks");
                
                // Verify each new unlock is properly recorded
                for (Achievement newUnlock : newUnlocks) {
                    assertTrue(achievementSystem.getRecentUnlocks().contains(newUnlock),
                            "Each new unlock should be in the recent unlocks list");
                }
            }
            
            // Test clearing functionality
            if (hasRecentUnlocksAfter) {
                achievementSystem.clearRecentUnlocks();
                
                assertFalse(achievementSystem.hasRecentUnlocks(),
                        "hasRecentUnlocks should be false after clearing");
                
                assertEquals(0, achievementSystem.getRecentUnlocks().size(),
                        "Recent unlocks list should be empty after clearing");
            }
        }
    }

    // Helper methods for generating test data

    private List<DailyHabits> createProgressiveHabitHistory(int days) {
        List<DailyHabits> history = new ArrayList<>();
        LocalDate currentDate = LocalDate.now();
        
        for (int i = 0; i < days; i++) {
            DailyHabits habits = new DailyHabits(currentDate.minusDays(i));
            
            // Create progressive improvement over time
            int dayProgress = days - i; // Earlier days have higher values
            
            habits.setStudyHours(Math.min(12, dayProgress * 2));
            habits.setStepsTaken(Math.min(20000, dayProgress * 1000));
            habits.setGoalsCompleted(Math.min(8, dayProgress));
            habits.setWaterIntake(Math.min(4.0, 1.5 + dayProgress * 0.3));
            habits.setSleepHours(Math.min(10.0, 6.0 + dayProgress * 0.5));
            habits.setMoneySpent(Math.max(10.0, 50.0 - dayProgress * 2));
            
            history.add(habits);
        }
        
        return history;
    }

    private List<DailyHabits> createHabitHistoryWithTotals(int studyHours, int totalSteps, 
                                                          int totalGoals, int streakLength) {
        List<DailyHabits> history = new ArrayList<>();
        LocalDate currentDate = LocalDate.now();
        
        // Create enough days to achieve the totals and streaks
        int daysNeeded = Math.max(10, streakLength);
        
        for (int i = 0; i < daysNeeded; i++) {
            DailyHabits habits = new DailyHabits(currentDate.minusDays(i));
            
            if (i < streakLength) {
                // For streak days, ensure good completion
                habits.setStudyHours(Math.max(0, studyHours / Math.max(1, streakLength)));
                habits.setStepsTaken(Math.max(0, totalSteps / Math.max(1, streakLength)));
                habits.setGoalsCompleted(Math.max(0, totalGoals / Math.max(1, streakLength)));
                habits.setWaterIntake(2.5);
                habits.setSleepHours(8.0);
                habits.setMoneySpent(25.0);
            } else {
                // For non-streak days, minimal completion
                habits.setStudyHours(0);
                habits.setStepsTaken(1000);
                habits.setGoalsCompleted(0);
                habits.setWaterIntake(1.0);
                habits.setSleepHours(6.0);
                habits.setMoneySpent(10.0);
            }
            
            history.add(habits);
        }
        
        return history;
    }

    @Provide
    Arbitrary<EvolutionStage> evolutionStages() {
        return Arbitraries.of(EvolutionStage.values());
    }
}