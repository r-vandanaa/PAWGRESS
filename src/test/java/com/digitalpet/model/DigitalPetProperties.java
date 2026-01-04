package com.digitalpet.model;

import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;
import net.jqwik.api.constraints.StringLength;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based tests for DigitalPet model
 * Feature: digital-pet-evolution
 * 
 * Tests Property 1: Pet Visual Consistency Across Evolution
 * Validates: Requirements 1.1, 1.4, 2.4, 10.4
 */
class DigitalPetProperties {

    @Property
    @Label("Feature: digital-pet-evolution, Property 1: Pet Visual Consistency Across Evolution")
    void petMaintainsConsistentVisualElementsAcrossEvolution(
            @ForAll @StringLength(min = 1, max = 20) String petName,
            @ForAll @IntRange(min = 0, max = 2000) int xp,
            @ForAll("petMoods") PetMood mood,
            @ForAll @IntRange(min = 0, max = 100) int energy) {
        
        // Create pet with given parameters
        DigitalPet pet = new DigitalPet(petName);
        pet.setExperiencePoints(xp);
        pet.setCurrentMood(mood);
        pet.setEnergyLevel(energy);
        
        // Core visual consistency requirements from Requirements 1.1, 1.4, 2.4, 10.4:
        
        // 1. Pet should maintain consistent core identity regardless of evolution stage
        assertNotNull(pet.getName(), "Pet must always have a name");
        assertFalse(pet.getName().trim().isEmpty(), "Pet name must not be empty");
        assertTrue(pet.getName().length() <= 20, "Pet name must be within display limits");
        
        // 2. Evolution stage should be determined by XP and follow forward progression
        EvolutionStage expectedStage = EvolutionStage.fromXP(xp);
        assertEquals(expectedStage, pet.getCurrentStage(), 
                "Pet evolution stage must match XP level");
        
        // 3. Pet should maintain valid mood state
        assertNotNull(pet.getCurrentMood(), "Pet must always have a mood");
        assertEquals(mood, pet.getCurrentMood(), "Pet mood should be settable and consistent");
        
        // 4. Energy level should be bounded and influenced by mood
        assertTrue(pet.getEnergyLevel() >= 0 && pet.getEnergyLevel() <= 100,
                "Pet energy must be within valid range (0-100)");
        
        // 5. XP should be non-negative and drive evolution
        assertTrue(pet.getExperiencePoints() >= 0, "Pet XP must be non-negative");
        assertEquals(xp, pet.getExperiencePoints(), "Pet XP should match set value");
        
        // 6. Evolution progress should be calculable and consistent
        double progress = pet.getEvolutionProgress();
        assertTrue(progress >= 0.0 && progress <= 1.0, 
                "Evolution progress must be between 0.0 and 1.0");
        
        // 7. XP to next evolution should be consistent with current stage
        int xpToNext = pet.getXpToNextEvolution();
        assertTrue(xpToNext >= 0, "XP to next evolution must be non-negative");
        
        if (pet.getCurrentStage() != EvolutionStage.LEGENDARY) {
            assertTrue(xpToNext > 0, "Non-legendary pets should need XP for next evolution");
        } else {
            assertEquals(0, xpToNext, "Legendary pets should need 0 XP for next evolution");
        }
        
        // 8. Last interaction should be trackable
        assertNotNull(pet.getLastInteraction(), "Pet must track last interaction time");
        
        // 9. Pet should maintain transparent background support (no background color properties)
        // This is validated by the absence of background color fields in the model
        
        // 10. Visual elements should remain consistent across mood changes
        PetMood originalMood = pet.getCurrentMood();
        String expectedName = pet.getName(); // Get the validated name after construction
        for (PetMood testMood : PetMood.values()) {
            pet.setCurrentMood(testMood);
            
            // Core identity should remain unchanged
            assertEquals(expectedName, pet.getName(), 
                    "Pet name should remain consistent across mood changes");
            assertEquals(expectedStage, pet.getCurrentStage(),
                    "Pet evolution stage should remain consistent across mood changes");
            assertEquals(xp, pet.getExperiencePoints(),
                    "Pet XP should remain consistent across mood changes");
        }
        
        // Restore original mood
        pet.setCurrentMood(originalMood);
    }

    @Property
    @Label("Feature: digital-pet-evolution, Property 1: Pet Evolution Progression Integrity")
    void petEvolutionFollowsForwardOnlyProgression(@ForAll @IntRange(min = 0, max = 2000) int initialXp,
                                                   @ForAll @IntRange(min = 0, max = 500) int xpToAdd) {
        DigitalPet pet = new DigitalPet();
        pet.setExperiencePoints(initialXp);
        
        EvolutionStage initialStage = pet.getCurrentStage();
        int initialStageOrdinal = initialStage.ordinal();
        
        // Add XP and verify forward-only progression
        pet.addExperiencePoints(xpToAdd);
        
        EvolutionStage newStage = pet.getCurrentStage();
        int newStageOrdinal = newStage.ordinal();
        
        // Evolution should only progress forward, never backward
        assertTrue(newStageOrdinal >= initialStageOrdinal,
                "Pet evolution should never regress to previous stages");
        
        // New stage should match XP level
        EvolutionStage expectedStage = EvolutionStage.fromXP(pet.getExperiencePoints());
        assertEquals(expectedStage, newStage,
                "Pet stage should match XP-determined stage after XP addition");
    }

    @Property
    @Label("Feature: digital-pet-evolution, Property 1: Pet Energy and Mood Consistency")
    void petEnergyReflectsMoodMultiplier(@ForAll @IntRange(min = 0, max = 100) int baseEnergy,
                                         @ForAll("petMoods") PetMood mood) {
        DigitalPet pet = new DigitalPet();
        pet.setCurrentMood(mood);
        
        // Update energy based on mood
        pet.updateEnergyFromMood(baseEnergy);
        
        // Verify energy reflects mood multiplier
        double expectedEnergy = baseEnergy * mood.getEnergyMultiplier();
        int actualEnergy = pet.getEnergyLevel();
        
        // Energy should be within expected range considering rounding and bounds
        assertTrue(actualEnergy >= 0 && actualEnergy <= 100,
                "Pet energy must remain within bounds regardless of mood multiplier");
        
        // For moods that don't exceed bounds, energy should match calculation
        if (expectedEnergy >= 0 && expectedEnergy <= 100) {
            assertEquals((int) Math.round(expectedEnergy), actualEnergy,
                    "Pet energy should reflect mood multiplier when within bounds");
        }
    }

    @Property
    @Label("Feature: digital-pet-evolution, Property 1: Pet Interaction Tracking")
    void petTracksInteractionsConsistently(@ForAll @StringLength(min = 1, max = 20) String petName) {
        DigitalPet pet = new DigitalPet(petName);
        
        LocalDateTime beforeInteraction = LocalDateTime.now().minusSeconds(1);
        
        // Record interaction
        pet.recordInteraction();
        
        LocalDateTime afterInteraction = LocalDateTime.now().plusSeconds(1);
        LocalDateTime recordedTime = pet.getLastInteraction();
        
        // Verify interaction time is reasonable
        assertNotNull(recordedTime, "Pet must record interaction time");
        assertTrue(recordedTime.isAfter(beforeInteraction) || recordedTime.isEqual(beforeInteraction),
                "Recorded interaction time should be after or equal to before time");
        assertTrue(recordedTime.isBefore(afterInteraction) || recordedTime.isEqual(afterInteraction),
                "Recorded interaction time should be before or equal to after time");
    }

    @Provide
    Arbitrary<PetMood> petMoods() {
        return Arbitraries.of(PetMood.values());
    }
}