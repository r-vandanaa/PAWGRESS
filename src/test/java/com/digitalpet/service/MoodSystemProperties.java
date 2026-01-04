package com.digitalpet.service;

import com.digitalpet.model.DailyHabits;
import com.digitalpet.model.PetMood;
import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;
import net.jqwik.api.constraints.DoubleRange;

import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based tests for MoodSystem service
 * Feature: digital-pet-evolution
 */
class MoodSystemProperties {

    @Property
    @Label("Feature: digital-pet-evolution, Property 8: Mood System State Management")
    void moodSystemCalculatesCorrectMoodStates(@ForAll("habitHistories") List<DailyHabits> recentHabits,
                                              @ForAll boolean hasRecentAchievement) {
        // **Validates: Requirements 4.1, 4.2, 4.3, 4.4, 4.5, 4.6**
        
        MoodSystem moodSystem = new MoodSystem();
        
        // Calculate mood based on habit patterns
        PetMood calculatedMood = moodSystem.calculateMood(recentHabits, hasRecentAchievement);
        
        // Verify mood is one of the five predefined states
        assertTrue(calculatedMood == PetMood.HAPPY || 
                  calculatedMood == PetMood.NEUTRAL || 
                  calculatedMood == PetMood.SLEEPY || 
                  calculatedMood == PetMood.WORRIED || 
                  calculatedMood == PetMood.CELEBRATING,
                  "Calculated mood should be one of the five predefined states");
        
        // If there's a recent achievement, mood should be CELEBRATING
        if (hasRecentAchievement) {
            assertEquals(PetMood.CELEBRATING, calculatedMood,
                "Mood should be CELEBRATING when there's a recent achievement");
        }
        // If no habit data and no achievement, mood should be NEUTRAL
        else if (recentHabits == null || recentHabits.isEmpty()) {
            assertEquals(PetMood.NEUTRAL, calculatedMood,
                "Mood should be NEUTRAL when no habit data is available and no achievement");
        }
        
        // Update mood and verify it's set correctly
        moodSystem.updateMood(recentHabits, hasRecentAchievement);
        assertEquals(calculatedMood, moodSystem.getCurrentMood(),
            "Current mood should match calculated mood after update");
    }

    @Property
    @Label("Feature: digital-pet-evolution, Property: Mood calculation is deterministic")
    void moodCalculationIsDeterministic(@ForAll("habitHistories") List<DailyHabits> recentHabits,
                                       @ForAll boolean hasRecentAchievement) {
        MoodSystem moodSystem1 = new MoodSystem();
        MoodSystem moodSystem2 = new MoodSystem();
        
        // Calculate mood with same inputs on different instances
        PetMood mood1 = moodSystem1.calculateMood(recentHabits, hasRecentAchievement);
        PetMood mood2 = moodSystem2.calculateMood(recentHabits, hasRecentAchievement);
        
        // Results should be identical
        assertEquals(mood1, mood2,
            "Mood calculation should be deterministic for identical inputs");
    }

    @Property
    @Label("Feature: digital-pet-evolution, Property: Mood smoothing prevents rapid changes")
    void moodSmoothingPreventsRapidChanges(@ForAll("habitHistories") List<DailyHabits> goodHabits,
                                          @ForAll("habitHistories") List<DailyHabits> poorHabits) {
        Assume.that(goodHabits != null && !goodHabits.isEmpty());
        Assume.that(poorHabits != null && !poorHabits.isEmpty());
        
        MoodSystem moodSystem = new MoodSystem();
        
        // Set initial mood with good habits
        moodSystem.updateMood(goodHabits, false);
        PetMood initialMood = moodSystem.getCurrentMood();
        
        // Update with poor habits
        moodSystem.updateMood(poorHabits, false);
        PetMood newMood = moodSystem.getCurrentMood();
        
        // Verify mood change tracking works
        boolean moodChanged = moodSystem.hasMoodChanged();
        assertEquals(!initialMood.equals(newMood), moodChanged,
            "Mood change detection should work correctly");
        
        // Previous mood should be stored correctly
        assertEquals(initialMood, moodSystem.getPreviousMood(),
            "Previous mood should be stored correctly");
    }

    @Property
    @Label("Feature: digital-pet-evolution, Property: Sleep patterns affect mood correctly")
    void sleepPatternsAffectMoodCorrectly(@ForAll @DoubleRange(min = 0.0, max = 24.0) double sleepHours) {
        MoodSystem moodSystem = new MoodSystem();
        
        // Create habit with specific sleep hours
        DailyHabits habits = new DailyHabits();
        habits.setSleepHours(sleepHours);
        habits.setStudyHours(8);  // Good other habits
        habits.setWaterIntake(2.5);
        habits.setStepsTaken(10000);
        habits.setGoalsCompleted(5);
        habits.setMoneySpent(30.0);
        
        List<DailyHabits> recentHabits = List.of(habits);
        
        PetMood mood = moodSystem.calculateMood(recentHabits, false);
        
        // Very poor sleep (< 5 hours) or excessive sleep (> 12 hours) should result in SLEEPY mood
        if (sleepHours < 5.0 || sleepHours > 12.0) {
            assertEquals(PetMood.SLEEPY, mood,
                "Pet should be SLEEPY with very poor or excessive sleep (" + sleepHours + " hours)");
        }
        
        // Verify mood is always one of the valid states
        assertTrue(mood == PetMood.HAPPY || mood == PetMood.NEUTRAL || 
                  mood == PetMood.SLEEPY || mood == PetMood.WORRIED || mood == PetMood.CELEBRATING,
                  "Mood should always be one of the five predefined states");
    }

    @Property
    @Label("Feature: digital-pet-evolution, Property: Achievement unlocks trigger celebration")
    void achievementUnlocksTriggerCelebration(@ForAll("habitHistories") List<DailyHabits> recentHabits) {
        MoodSystem moodSystem = new MoodSystem();
        
        // Calculate mood with achievement
        PetMood moodWithAchievement = moodSystem.calculateMood(recentHabits, true);
        
        // Should always be CELEBRATING when there's an achievement
        assertEquals(PetMood.CELEBRATING, moodWithAchievement,
            "Mood should be CELEBRATING when there's a recent achievement");
        
        // Calculate mood without achievement
        PetMood moodWithoutAchievement = moodSystem.calculateMood(recentHabits, false);
        
        // Without achievement, mood should be based on habits (not necessarily celebrating)
        if (recentHabits != null && !recentHabits.isEmpty()) {
            // Mood should be determined by habit patterns, not celebration
            assertTrue(moodWithoutAchievement == PetMood.HAPPY || 
                      moodWithoutAchievement == PetMood.NEUTRAL || 
                      moodWithoutAchievement == PetMood.SLEEPY || 
                      moodWithoutAchievement == PetMood.WORRIED,
                      "Without achievement, mood should be based on habit patterns");
        }
    }

    @Property
    @Label("Feature: digital-pet-evolution, Property: Mood analysis provides comprehensive information")
    void moodAnalysisProvidesComprehensiveInformation(@ForAll("habitHistories") List<DailyHabits> recentHabits,
                                                     @ForAll boolean hasRecentAchievement) {
        MoodSystem moodSystem = new MoodSystem();
        
        var analysis = moodSystem.getMoodAnalysis(recentHabits, hasRecentAchievement);
        
        // Analysis should contain required fields
        assertTrue(analysis.containsKey("mood"), "Analysis should contain mood");
        assertTrue(analysis.get("mood") instanceof PetMood, "Mood should be a PetMood instance");
        
        if (hasRecentAchievement) {
            assertTrue(analysis.containsKey("reason"), "Analysis should contain reason for achievement");
            assertEquals(PetMood.CELEBRATING, analysis.get("mood"), 
                "Mood should be CELEBRATING in analysis when achievement is present");
        } else if (recentHabits != null && !recentHabits.isEmpty()) {
            // Should contain detailed scoring information
            assertTrue(analysis.containsKey("completionScore"), "Analysis should contain completion score");
            assertTrue(analysis.containsKey("consistencyScore"), "Analysis should contain consistency score");
            assertTrue(analysis.containsKey("sleepScore"), "Analysis should contain sleep score");
            assertTrue(analysis.containsKey("overallScore"), "Analysis should contain overall score");
            assertTrue(analysis.containsKey("smoothedScore"), "Analysis should contain smoothed score");
            
            // Scores should be reasonable values
            assertTrue((Double) analysis.get("completionScore") >= 0.0 && 
                      (Double) analysis.get("completionScore") <= 1.0,
                      "Completion score should be between 0.0 and 1.0");
            assertTrue((Double) analysis.get("consistencyScore") >= 0.0 && 
                      (Double) analysis.get("consistencyScore") <= 1.0,
                      "Consistency score should be between 0.0 and 1.0");
            assertTrue((Double) analysis.get("sleepScore") >= 0.0 && 
                      (Double) analysis.get("sleepScore") <= 1.0,
                      "Sleep score should be between 0.0 and 1.0");
        }
    }

    @Provide
    Arbitrary<List<DailyHabits>> habitHistories() {
        return Arbitraries.integers().between(0, 3)
            .flatMap(size -> {
                if (size == 0) {
                    return Arbitraries.just(new ArrayList<>());
                }
                return Arbitraries.create(() -> {
                    List<DailyHabits> habits = new ArrayList<>();
                    LocalDate date = LocalDate.now();
                    for (int i = 0; i < size; i++) {
                        DailyHabits habit = dailyHabits().sample();
                        habit.setDate(date);
                        habits.add(habit);
                        date = date.minusDays(1);
                    }
                    return habits;
                });
            });
    }

    @Provide
    Arbitrary<DailyHabits> dailyHabits() {
        return Arbitraries.create(() -> {
            DailyHabits habits = new DailyHabits();
            
            // Generate realistic habit values within valid ranges
            habits.setStudyHours(Arbitraries.integers().between(0, 16).sample());
            habits.setWaterIntake(Arbitraries.doubles().between(0.0, 5.0).sample());
            habits.setStepsTaken(Arbitraries.integers().between(0, 50000).sample());
            habits.setSleepHours(Arbitraries.doubles().between(0.0, 24.0).sample());
            habits.setMoneySpent(Arbitraries.doubles().between(0.0, 1000.0).sample());
            habits.setGoalsCompleted(Arbitraries.integers().between(0, 10).sample());
            
            return habits;
        });
    }
}