package com.digitalpet.service;

import com.digitalpet.model.DailyHabits;
import com.digitalpet.model.DigitalPet;
import com.digitalpet.model.EvolutionStage;
import net.jqwik.api.*;
import net.jqwik.api.constraints.DoubleRange;
import net.jqwik.api.constraints.IntRange;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based tests for XPSystem service
 * Feature: digital-pet-evolution
 * 
 * Tests Property 4: XP Calculation Transparency
 * Validates: Requirements 2.1
 */
class XPSystemProperties {

    private final XPSystem xpSystem = new XPSystem();

    @Property
    @Label("Feature: digital-pet-evolution, Property 4: XP Calculation Transparency")
    void xpCalculationIsTransparentAndConsistent(
            @ForAll("validDailyHabits") DailyHabits habits,
            @ForAll @DoubleRange(min = 0.0, max = 1.0) double consistencyScore) {
        
        // Calculate XP multiple times with same inputs
        int xp1 = xpSystem.calculateXP(habits, consistencyScore);
        int xp2 = xpSystem.calculateXP(habits, consistencyScore);
        int xp3 = xpSystem.calculateXP(habits, consistencyScore);
        
        // XP calculation should be deterministic and consistent
        assertEquals(xp1, xp2, "XP calculation should be deterministic");
        assertEquals(xp2, xp3, "XP calculation should be consistent across calls");
        
        // XP should always be non-negative
        assertTrue(xp1 >= 0, "XP calculation should never return negative values");
        
        // XP should correlate with completion percentage
        double completionPercentage = habits.getCompletionPercentage();
        
        // Higher completion should generally yield higher base XP
        if (completionPercentage > 0.0) {
            assertTrue(xp1 > 0, "Non-zero completion should yield positive XP");
        }
        
        // Test consistency bonus effect
        int baseXP = xpSystem.calculateBaseXP(habits);
        assertTrue(xp1 >= baseXP, "XP with consistency should be >= base XP");
        
        // Higher consistency should yield higher XP (when base XP > 0 and consistency is significant)
        if (baseXP > 0 && consistencyScore >= 0.6) {
            assertTrue(xp1 > baseXP, "High consistency should increase XP above base");
        }
        
        // Test transparency: XP should be predictable based on completion tiers
        if (completionPercentage >= 0.8) {
            // Excellent completion should yield highest base XP
            assertTrue(baseXP >= 15, "Excellent completion (80%+) should yield high base XP");
        } else if (completionPercentage >= 0.6) {
            // Good completion should yield good base XP
            assertTrue(baseXP >= 10, "Good completion (60-79%) should yield decent base XP");
        } else if (completionPercentage >= 0.4) {
            // Fair completion should yield fair base XP
            assertTrue(baseXP >= 5, "Fair completion (40-59%) should yield some base XP");
        }
        
        // Test consistency multiplier transparency
        if (baseXP > 0) {
            double expectedMultiplier = getExpectedConsistencyMultiplier(consistencyScore);
            int expectedXP = (int) Math.round(baseXP * expectedMultiplier);
            assertEquals(expectedXP, xp1, 
                "XP should match expected calculation: baseXP * consistencyMultiplier");
        }
    }

    @Property
    @Label("Feature: digital-pet-evolution, Property 4: XP Calculation Edge Cases")
    void xpCalculationHandlesEdgeCases(
            @ForAll @DoubleRange(min = 0.0, max = 1.0) double consistencyScore) {
        
        // Test null habits
        int xpNull = xpSystem.calculateXP(null, consistencyScore);
        assertEquals(0, xpNull, "Null habits should yield 0 XP");
        
        // Test empty habits (all zeros)
        DailyHabits emptyHabits = new DailyHabits();
        int xpEmpty = xpSystem.calculateXP(emptyHabits, consistencyScore);
        // Note: Empty habits still get some XP because $0 spending is considered good
        assertTrue(xpEmpty >= 0, "Empty habits should yield non-negative XP");
        
        // Test maximum habits
        DailyHabits maxHabits = createMaxHabits();
        int xpMax = xpSystem.calculateXP(maxHabits, consistencyScore);
        assertTrue(xpMax > 0, "Maximum habits should yield positive XP");
        
        // Test consistency score bounds
        DailyHabits goodHabits = createGoodHabits();
        
        // Minimum consistency (0.0)
        int xpMinConsistency = xpSystem.calculateXP(goodHabits, 0.0);
        
        // Maximum consistency (1.0)
        int xpMaxConsistency = xpSystem.calculateXP(goodHabits, 1.0);
        
        assertTrue(xpMaxConsistency >= xpMinConsistency, 
            "Higher consistency should yield equal or higher XP");
        
        // Test consistency score outside bounds (should be clamped)
        int xpNegativeConsistency = xpSystem.calculateXP(goodHabits, -0.5);
        int xpHighConsistency = xpSystem.calculateXP(goodHabits, 1.5);
        
        assertEquals(xpMinConsistency, xpNegativeConsistency,
            "Negative consistency should be treated as 0.0");
        assertEquals(xpMaxConsistency, xpHighConsistency,
            "Consistency > 1.0 should be treated as 1.0");
    }

    @Property
    @Label("Feature: digital-pet-evolution, Property 4: XP Calculation Basic Monotonicity")
    void xpCalculationBasicMonotonicity(
            @ForAll("validDailyHabits") DailyHabits habits,
            @ForAll @DoubleRange(min = 0.0, max = 1.0) double consistency1,
            @ForAll @DoubleRange(min = 0.0, max = 1.0) double consistency2) {
        
        int xp1 = xpSystem.calculateXP(habits, consistency1);
        int xp2 = xpSystem.calculateXP(habits, consistency2);
        
        // Basic monotonicity: higher consistency should never yield lower XP for same habits
        if (consistency1 > consistency2) {
            assertTrue(xp1 >= xp2, 
                "Higher consistency should never yield lower XP for identical habits");
        }
    }

    @Property
    @Label("Feature: digital-pet-evolution, Property 4: Consistency Score Calculation")
    void consistencyScoreCalculationIsValid(
            @ForAll("habitsList") List<DailyHabits> recentHabits,
            @ForAll @IntRange(min = 1, max = 30) int daysToConsider) {
        
        double consistencyScore = xpSystem.calculateConsistencyScore(recentHabits, daysToConsider);
        
        // Consistency score should be within valid range
        assertTrue(consistencyScore >= 0.0 && consistencyScore <= 1.0,
            "Consistency score should be between 0.0 and 1.0");
        
        // Empty or null list should yield 0 consistency
        double emptyConsistency = xpSystem.calculateConsistencyScore(new ArrayList<>(), daysToConsider);
        assertEquals(0.0, emptyConsistency, "Empty habits list should yield 0 consistency");
        
        double nullConsistency = xpSystem.calculateConsistencyScore(null, daysToConsider);
        assertEquals(0.0, nullConsistency, "Null habits list should yield 0 consistency");
        
        // Invalid days should yield 0 consistency
        double invalidDaysConsistency = xpSystem.calculateConsistencyScore(recentHabits, 0);
        assertEquals(0.0, invalidDaysConsistency, "Zero days to consider should yield 0 consistency");
        
        // If all habits have high completion, consistency should be high
        if (!recentHabits.isEmpty()) {
            boolean allHighCompletion = recentHabits.stream()
                .allMatch(h -> h != null && h.getCompletionPercentage() >= 0.8);
            
            if (allHighCompletion && recentHabits.size() >= 3) {
                assertTrue(consistencyScore >= 0.5,
                    "High completion across multiple days should yield good consistency");
            }
        }
    }

    /**
     * Helper method to get expected consistency multiplier based on score
     */
    private double getExpectedConsistencyMultiplier(double consistencyScore) {
        double score = Math.max(0.0, Math.min(1.0, consistencyScore));
        
        if (score >= 0.8) {
            return 2.0;  // CONSISTENCY_MULTIPLIER_EXCELLENT
        } else if (score >= 0.6) {
            return 1.5;  // CONSISTENCY_MULTIPLIER_GOOD
        } else if (score >= 0.4) {
            return 1.2;  // CONSISTENCY_MULTIPLIER_FAIR
        } else {
            return 1.0;  // CONSISTENCY_MULTIPLIER_NONE
        }
    }

    /**
     * Creates habits with maximum valid values
     */
    private DailyHabits createMaxHabits() {
        DailyHabits habits = new DailyHabits();
        habits.setStudyHours(16);
        habits.setWaterIntake(5.0);
        habits.setStepsTaken(50000);
        habits.setSleepHours(24.0);
        habits.setMoneySpent(0.0);  // Lower is better for spending
        habits.setGoalsCompleted(10);
        return habits;
    }

    /**
     * Creates habits with good values
     */
    private DailyHabits createGoodHabits() {
        DailyHabits habits = new DailyHabits();
        habits.setStudyHours(8);
        habits.setWaterIntake(2.5);
        habits.setStepsTaken(10000);
        habits.setSleepHours(8.0);
        habits.setMoneySpent(25.0);
        habits.setGoalsCompleted(5);
        return habits;
    }

    @Provide
    Arbitrary<DailyHabits> validDailyHabits() {
        return Arbitraries.create(() -> {
            DailyHabits habits = new DailyHabits();
            habits.setStudyHours(Arbitraries.integers().between(0, 16).sample());
            habits.setWaterIntake(Arbitraries.doubles().between(0.0, 5.0).sample());
            habits.setStepsTaken(Arbitraries.integers().between(0, 50000).sample());
            habits.setSleepHours(Arbitraries.doubles().between(0.0, 24.0).sample());
            habits.setMoneySpent(Arbitraries.doubles().between(0.0, 1000.0).sample());
            habits.setGoalsCompleted(Arbitraries.integers().between(0, 10).sample());
            return habits;
        });
    }

    @Provide
    Arbitrary<List<DailyHabits>> habitsList() {
        return Arbitraries.create(() -> {
            int size = Arbitraries.integers().between(0, 14).sample();
            List<DailyHabits> habits = new ArrayList<>();
            
            LocalDate currentDate = LocalDate.now();
            for (int i = 0; i < size; i++) {
                DailyHabits dailyHabits = validDailyHabits().sample();
                dailyHabits.setDate(currentDate.minusDays(i));
                habits.add(dailyHabits);
            }
            
            return habits;
        });
    }

    @Property
    @Label("Feature: digital-pet-evolution, Property 5: Evolution Progression Integrity")
    void evolutionProgressionFollowsForwardOnlyConstraint(
            @ForAll @IntRange(min = 0, max = 2000) int initialXP,
            @ForAll @IntRange(min = 0, max = 1000) int xpToAdd) {
        
        // Create pet with initial XP
        DigitalPet pet = new DigitalPet("Test Pet");
        pet.setExperiencePoints(initialXP);
        
        EvolutionStage initialStage = pet.getCurrentStage();
        int initialStageOrdinal = initialStage.ordinal();
        
        // Check for evolution before adding XP
        EvolutionStage expectedInitialStage = EvolutionStage.fromXP(initialXP);
        assertEquals(expectedInitialStage, initialStage,
            "Pet should start at correct evolution stage for initial XP");
        
        // Add XP using XPSystem
        pet.addExperiencePoints(xpToAdd);
        int finalXP = pet.getExperiencePoints();
        
        EvolutionStage finalStage = pet.getCurrentStage();
        int finalStageOrdinal = finalStage.ordinal();
        
        // Verify forward-only progression constraint
        assertTrue(finalStageOrdinal >= initialStageOrdinal,
            "Evolution should never regress - final stage should be >= initial stage");
        
        // Verify final stage matches XP level
        EvolutionStage expectedFinalStage = EvolutionStage.fromXP(finalXP);
        assertEquals(expectedFinalStage, finalStage,
            "Pet should be at correct evolution stage for final XP amount");
        
        // Verify XP thresholds are respected
        assertTrue(finalXP >= finalStage.getXpThreshold(),
            "Pet XP should meet or exceed current stage threshold");
        
        // If not at legendary stage, verify XP is below next threshold
        if (finalStage != EvolutionStage.LEGENDARY) {
            EvolutionStage nextStage = finalStage.getNext();
            assertNotNull(nextStage, "Non-legendary stages should have a next stage");
            assertTrue(finalXP < nextStage.getXpThreshold(),
                "Pet XP should be below next stage threshold when not evolved");
        }
        
        // Test XPSystem evolution checking
        EvolutionStage systemCheckStage = xpSystem.checkForEvolution(pet);
        assertNull(systemCheckStage, 
            "XPSystem should not suggest further evolution when pet is at correct stage");
        
        // Verify evolution progress calculation
        double progress = pet.getEvolutionProgress();
        assertTrue(progress >= 0.0 && progress <= 1.0,
            "Evolution progress should be between 0.0 and 1.0");
        
        if (finalStage == EvolutionStage.LEGENDARY) {
            assertEquals(1.0, progress, 0.001,
                "Legendary pets should have 100% evolution progress");
        }
        
        // Verify XP to next evolution calculation
        int xpToNext = pet.getXpToNextEvolution();
        assertTrue(xpToNext >= 0, "XP to next evolution should be non-negative");
        
        if (finalStage == EvolutionStage.LEGENDARY) {
            assertEquals(0, xpToNext, "Legendary pets should need 0 XP for next evolution");
        } else {
            assertTrue(xpToNext > 0, "Non-legendary pets should need positive XP for next evolution");
        }
    }

    @Property
    @Label("Feature: digital-pet-evolution, Property 5: Evolution Thresholds Validation")
    void evolutionThresholdsAreProperlyOrdered() {
        
        // Verify XPSystem threshold validation
        assertTrue(xpSystem.validateEvolutionThresholds(),
            "Evolution thresholds should be properly ordered");
        
        // Verify all thresholds are accessible
        Map<EvolutionStage, Integer> thresholds = xpSystem.getAllThresholds();
        assertEquals(5, thresholds.size(), "Should have thresholds for all 5 evolution stages");
        
        // Verify specific threshold values match requirements
        assertEquals(0, xpSystem.getXPThreshold(EvolutionStage.EGG),
            "Egg stage should require 0 XP");
        assertEquals(100, xpSystem.getXPThreshold(EvolutionStage.BABY),
            "Baby stage should require 100 XP");
        assertEquals(300, xpSystem.getXPThreshold(EvolutionStage.TEEN),
            "Teen stage should require 300 XP");
        assertEquals(600, xpSystem.getXPThreshold(EvolutionStage.ADULT),
            "Adult stage should require 600 XP");
        assertEquals(1000, xpSystem.getXPThreshold(EvolutionStage.LEGENDARY),
            "Legendary stage should require 1000 XP");
        
        // Verify thresholds are strictly increasing
        EvolutionStage[] stages = EvolutionStage.values();
        for (int i = 1; i < stages.length; i++) {
            int currentThreshold = xpSystem.getXPThreshold(stages[i]);
            int previousThreshold = xpSystem.getXPThreshold(stages[i - 1]);
            
            assertTrue(currentThreshold > previousThreshold,
                String.format("Stage %s threshold (%d) should be greater than %s threshold (%d)",
                    stages[i], currentThreshold, stages[i - 1], previousThreshold));
        }
    }

    @Property
    @Label("Feature: digital-pet-evolution, Property 5: Evolution System Integration")
    void evolutionSystemIntegratesCorrectlyWithPet(
            @ForAll @IntRange(min = 0, max = 1500) int targetXP) {
        
        DigitalPet pet = new DigitalPet("Evolution Test");
        
        // Award XP gradually and verify evolution happens correctly
        int currentXP = 0;
        EvolutionStage lastStage = pet.getCurrentStage();
        
        while (currentXP < targetXP) {
            int xpToAdd = Math.min(50, targetXP - currentXP);
            
            // Create dummy habits for XP calculation
            DailyHabits habits = createGoodHabits();
            
            // Award XP through XPSystem
            int actualXPAwarded = xpSystem.awardXP(pet, habits, 0.5);
            assertTrue(actualXPAwarded > 0, "XPSystem should award positive XP for good habits");
            
            // Manually add remaining XP to reach target
            if (actualXPAwarded < xpToAdd) {
                pet.addExperiencePoints(xpToAdd - actualXPAwarded);
            }
            
            currentXP = pet.getExperiencePoints();
            EvolutionStage currentStage = pet.getCurrentStage();
            
            // Verify evolution only progresses forward
            assertTrue(currentStage.ordinal() >= lastStage.ordinal(),
                "Evolution should only progress forward during XP accumulation");
            
            // Verify stage matches XP level
            EvolutionStage expectedStage = EvolutionStage.fromXP(currentXP);
            assertEquals(expectedStage, currentStage,
                "Pet stage should always match XP level");
            
            lastStage = currentStage;
        }
        
        // Final verification - allow for some variance due to XP system awarding additional XP
        assertTrue(pet.getExperiencePoints() >= targetXP,
            "Pet should have at least target XP amount (may have more due to XP system awards)");
        
        EvolutionStage finalExpectedStage = EvolutionStage.fromXP(pet.getExperiencePoints());
        assertEquals(finalExpectedStage, pet.getCurrentStage(),
            "Pet should be at correct final evolution stage for actual XP");
        
        // Verify no further evolution is possible at current XP
        assertFalse(xpSystem.applyEvolution(pet),
            "No further evolution should be possible when pet is at correct stage");
    }

    @Property
    @Label("Feature: digital-pet-evolution, Property 5: Evolution Celebration Triggers")
    void evolutionTriggersArePredictable(
            @ForAll @IntRange(min = 0, max = 999) int initialXP,
            @ForAll @IntRange(min = 1, max = 500) int xpToAdd) {
        
        DigitalPet pet = new DigitalPet("Celebration Test");
        pet.setExperiencePoints(initialXP);
        
        EvolutionStage initialStage = pet.getCurrentStage();
        
        // Check if evolution should occur
        int finalXP = initialXP + xpToAdd;
        EvolutionStage expectedFinalStage = EvolutionStage.fromXP(finalXP);
        boolean shouldEvolve = expectedFinalStage.ordinal() > initialStage.ordinal();
        
        // Apply evolution through XPSystem
        pet.addExperiencePoints(xpToAdd);
        boolean didEvolve = pet.getCurrentStage() != initialStage;
        
        assertEquals(shouldEvolve, didEvolve,
            "Evolution should occur if and only if XP crosses a threshold");
        
        if (shouldEvolve) {
            assertEquals(expectedFinalStage, pet.getCurrentStage(),
                "Pet should evolve to expected stage when threshold is crossed");
            
            // Verify celebration should be triggered (this would be handled by animation system)
            assertTrue(pet.getCurrentStage().ordinal() > initialStage.ordinal(),
                "Evolution should result in higher stage ordinal for celebration trigger");
        }
        
        // Test XPSystem evolution detection
        EvolutionStage systemSuggestedStage = xpSystem.checkForEvolution(pet);
        assertNull(systemSuggestedStage,
            "XPSystem should not suggest evolution when pet is at correct stage");
        
        // Verify evolution progress is consistent
        double progress = xpSystem.getEvolutionProgress(pet);
        assertEquals(pet.getEvolutionProgress(), progress, 0.001,
            "XPSystem and Pet should report same evolution progress");
    }
}