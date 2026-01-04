package com.digitalpet.viewmodel;

import com.digitalpet.model.DigitalPet;
import com.digitalpet.model.EvolutionStage;
import com.digitalpet.model.PetMood;
import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;
import net.jqwik.api.constraints.StringLength;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based tests for MainPetViewModel UI responsiveness and feedback.
 * 
 * **Feature: digital-pet-evolution, Property 7: UI Responsiveness and Feedback**
 * **Validates: Requirements 3.2, 3.3, 3.5**
 */
class MainPetViewModelProperties {

    /**
     * Property 7: UI Responsiveness and Feedback
     * For any Daily Habits submission, the UI Manager shall display sliders with live value updates, 
     * trigger pet reactions matching current mood, update XP within 1 second, and persist data across application sessions.
     * 
     * This test focuses on the ViewModel's reactive property binding and real-time updates.
     */
    @Property(tries = 100)
    @Label("Property 7: UI Responsiveness and Feedback - Property bindings update immediately when model changes")
    void uiResponsivenessAndFeedback(
            @ForAll @StringLength(min = 1, max = 20) String petName,
            @ForAll @IntRange(min = 0, max = 2000) int experiencePoints,
            @ForAll @IntRange(min = 0, max = 100) int energyLevel,
            @ForAll EvolutionStage evolutionStage,
            @ForAll PetMood petMood) {
        
        // Create pet with initial values
        DigitalPet pet = new DigitalPet(petName);
        MainPetViewModel viewModel = new MainPetViewModel(pet);
        
        // Update pet model properties
        pet.setExperiencePoints(experiencePoints);
        pet.setEnergyLevel(energyLevel);
        pet.setCurrentStage(evolutionStage);
        pet.setCurrentMood(petMood);
        
        // Verify immediate UI property updates (Requirements 3.2, 3.3)
        // Pet name should be reflected immediately
        assertEquals(pet.getName(), viewModel.getPetName(), 
                "Pet name should be immediately reflected in ViewModel");
        
        // Evolution stage should be reflected immediately
        assertEquals(evolutionStage, viewModel.getEvolutionStage(),
                "Evolution stage should be immediately reflected in ViewModel");
        
        // Mood should be reflected immediately
        assertEquals(petMood, viewModel.getCurrentMood(),
                "Pet mood should be immediately reflected in ViewModel");
        
        // XP should be reflected immediately
        assertEquals(experiencePoints, viewModel.getExperiencePoints(),
                "Experience points should be immediately reflected in ViewModel");
        
        // Energy should be reflected immediately
        assertEquals(energyLevel, viewModel.getEnergyLevel(),
                "Energy level should be immediately reflected in ViewModel");
        
        // Verify derived properties update correctly (Requirements 3.5)
        // Evolution stage display should include "Stage" suffix
        String expectedStageDisplay = evolutionStage.getDisplayName() + " Stage";
        assertEquals(expectedStageDisplay, viewModel.evolutionStageDisplayProperty().get(),
                "Evolution stage display should format correctly");
        
        // Mood display should include emoji and name
        String expectedMoodDisplay = petMood.getDisplayText();
        assertEquals(expectedMoodDisplay, viewModel.moodDisplayProperty().get(),
                "Mood display should include emoji and name");
        
        // Energy progress should be normalized to 0.0-1.0 range
        double expectedEnergyProgress = energyLevel / 100.0;
        assertEquals(expectedEnergyProgress, viewModel.energyProgressProperty().get(), 0.001,
                "Energy progress should be normalized correctly");
        
        // Energy display text should show current/max format
        String expectedEnergyText = energyLevel + " / 100";
        assertEquals(expectedEnergyText, viewModel.energyDisplayTextProperty().get(),
                "Energy display text should show current/max format");
        
        // XP progress should be calculated correctly based on evolution stage
        double expectedXpProgress = calculateExpectedXpProgress(evolutionStage, experiencePoints);
        assertEquals(expectedXpProgress, viewModel.xpProgressProperty().get(), 0.001,
                "XP progress should be calculated correctly for evolution stage");
    }
    
    /**
     * Tests that pet interaction handling triggers appropriate responses
     */
    @Property(tries = 100)
    @Label("Pet interaction handling triggers model updates")
    void petInteractionHandling(
            @ForAll @StringLength(min = 1, max = 20) String petName,
            @ForAll @IntRange(min = 0, max = 1000) int initialXp,
            @ForAll EvolutionStage initialStage,
            @ForAll PetMood initialMood) {
        
        // Create pet and viewmodel
        DigitalPet pet = new DigitalPet(petName);
        pet.setExperiencePoints(initialXp);
        pet.setCurrentStage(initialStage);
        pet.setCurrentMood(initialMood);
        
        MainPetViewModel viewModel = new MainPetViewModel(pet);
        
        // Record initial state
        int initialEnergy = pet.getEnergyLevel();
        var initialInteraction = pet.getLastInteraction();
        
        // Add a small delay to ensure time difference
        try {
            Thread.sleep(1);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Simulate pet interaction
        viewModel.handlePetInteraction();
        
        // Verify interaction was recorded (Requirements 1.5)
        assertNotEquals(initialInteraction, pet.getLastInteraction(),
                "Pet interaction should update last interaction time");
        
        // Verify energy was boosted by interaction
        assertTrue(pet.getEnergyLevel() >= initialEnergy,
                "Pet interaction should maintain or boost energy level");
        
        // Verify ViewModel still reflects current state
        assertEquals(pet.getName(), viewModel.getPetName(),
                "ViewModel should maintain correct pet name after interaction");
        assertEquals(pet.getCurrentStage(), viewModel.getEvolutionStage(),
                "ViewModel should maintain correct evolution stage after interaction");
        assertEquals(pet.getCurrentMood(), viewModel.getCurrentMood(),
                "ViewModel should maintain correct mood after interaction");
    }
    
    /**
     * Tests XP addition and evolution triggering through ViewModel
     */
    @Property(tries = 100)
    @Label("XP addition through ViewModel triggers appropriate evolution")
    void xpAdditionAndEvolution(
            @ForAll @StringLength(min = 1, max = 20) String petName,
            @ForAll @IntRange(min = 0, max = 500) int initialXp,
            @ForAll @IntRange(min = 1, max = 600) int xpToAdd) {
        
        // Create pet with initial XP
        DigitalPet pet = new DigitalPet(petName);
        pet.setExperiencePoints(initialXp);
        
        MainPetViewModel viewModel = new MainPetViewModel(pet);
        
        // Record initial state
        EvolutionStage initialStage = viewModel.getEvolutionStage();
        int expectedFinalXp = initialXp + xpToAdd;
        
        // Add XP through ViewModel
        viewModel.addExperiencePoints(xpToAdd);
        
        // Verify XP was added correctly
        assertEquals(expectedFinalXp, viewModel.getExperiencePoints(),
                "XP should be added correctly through ViewModel");
        
        // Verify evolution occurred if thresholds were met
        EvolutionStage expectedStage = EvolutionStage.fromXP(expectedFinalXp);
        assertEquals(expectedStage, viewModel.getEvolutionStage(),
                "Evolution should occur automatically when XP thresholds are met");
        
        // Verify XP progress calculation is correct for new stage
        double expectedProgress = calculateExpectedXpProgress(expectedStage, expectedFinalXp);
        assertEquals(expectedProgress, viewModel.xpProgressProperty().get(), 0.001,
                "XP progress should be recalculated correctly after evolution");
    }
    
    /**
     * Helper method to calculate expected XP progress for a given stage and XP amount
     */
    private double calculateExpectedXpProgress(EvolutionStage stage, int currentXp) {
        EvolutionStage nextStage = stage.getNext();
        if (nextStage == null) {
            return 1.0; // At max stage
        }
        
        int currentThreshold = stage.getXpThreshold();
        int nextThreshold = nextStage.getXpThreshold();
        
        if (nextThreshold == currentThreshold) {
            return 1.0;
        }
        
        double progress = (double) (currentXp - currentThreshold) / (nextThreshold - currentThreshold);
        return Math.max(0.0, Math.min(1.0, progress));
    }
    
    /**
     * Provides arbitrary EvolutionStage values for property testing
     */
    @Provide
    Arbitrary<EvolutionStage> evolutionStages() {
        return Arbitraries.of(EvolutionStage.values());
    }
    
    /**
     * Provides arbitrary PetMood values for property testing
     */
    @Provide
    Arbitrary<PetMood> petMoods() {
        return Arbitraries.of(PetMood.values());
    }
}