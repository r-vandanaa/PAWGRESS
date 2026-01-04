package com.digitalpet.model;

import net.jqwik.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based tests for Environment class
 * Feature: digital-pet-evolution
 */
class EnvironmentProperties {

    @Property
    @Label("Feature: digital-pet-evolution, Property 9: Environment Evolution Synchronization")
    void environmentEvolvesWithPetStage(@ForAll("evolutionStages") EvolutionStage stage) {
        // **Validates: Requirements 5.1**
        
        // Create a new environment
        Environment environment = new Environment();
        
        // Set the evolution stage
        environment.setCurrentStage(stage);
        
        // Verify the environment has the correct theme for the stage
        assertTrue(environment.hasCorrectThemeForStage(), 
            "Environment should have correct theme for stage " + stage);
        
        // Verify the current stage is set correctly
        assertEquals(stage, environment.getCurrentStage(), 
            "Environment current stage should match the set stage");
        
        // Verify the background theme matches the expected configuration
        Environment.EnvironmentConfig expectedConfig = Environment.getConfigForStage(stage);
        assertNotNull(expectedConfig, "Expected configuration should exist for stage " + stage);
        
        assertEquals(expectedConfig.getBackgroundTheme(), environment.getBackgroundTheme(),
            "Background theme should match expected configuration for stage " + stage);
        
        // Verify decorative elements match the expected configuration
        assertEquals(expectedConfig.getDecorativeElements(), environment.getDecorativeElements(),
            "Decorative elements should match expected configuration for stage " + stage);
        
        // Verify that all five predefined themes exist
        assertNotNull(Environment.getConfigForStage(EvolutionStage.EGG), 
            "EGG stage should have environment configuration");
        assertNotNull(Environment.getConfigForStage(EvolutionStage.BABY), 
            "BABY stage should have environment configuration");
        assertNotNull(Environment.getConfigForStage(EvolutionStage.TEEN), 
            "TEEN stage should have environment configuration");
        assertNotNull(Environment.getConfigForStage(EvolutionStage.ADULT), 
            "ADULT stage should have environment configuration");
        assertNotNull(Environment.getConfigForStage(EvolutionStage.LEGENDARY), 
            "LEGENDARY stage should have environment configuration");
    }

    @Property
    @Label("Feature: digital-pet-evolution, Property: Environment themes are distinct for each stage")
    void environmentThemesAreDistinctForEachStage(@ForAll("evolutionStages") EvolutionStage stage1,
                                                  @ForAll("evolutionStages") EvolutionStage stage2) {
        Assume.that(!stage1.equals(stage2));
        
        Environment.EnvironmentConfig config1 = Environment.getConfigForStage(stage1);
        Environment.EnvironmentConfig config2 = Environment.getConfigForStage(stage2);
        
        assertNotNull(config1, "Configuration should exist for stage " + stage1);
        assertNotNull(config2, "Configuration should exist for stage " + stage2);
        
        // Different stages should have different environment configurations
        assertNotEquals(config1, config2, 
            "Different evolution stages should have distinct environment configurations");
    }

    @Property
    @Label("Feature: digital-pet-evolution, Property: Environment updates synchronously with stage changes")
    void environmentUpdatesSynchronouslyWithStageChanges(@ForAll("evolutionStages") EvolutionStage initialStage,
                                                        @ForAll("evolutionStages") EvolutionStage newStage) {
        Environment environment = new Environment();
        
        // Set initial stage
        environment.setCurrentStage(initialStage);
        assertTrue(environment.hasCorrectThemeForStage(), 
            "Environment should have correct theme for initial stage " + initialStage);
        
        // Change to new stage
        environment.setCurrentStage(newStage);
        
        // Verify environment immediately reflects the new stage
        assertEquals(newStage, environment.getCurrentStage(), 
            "Environment should immediately reflect new stage");
        assertTrue(environment.hasCorrectThemeForStage(), 
            "Environment should have correct theme for new stage " + newStage);
        
        // Verify the theme actually changed if stages are different
        if (!initialStage.equals(newStage)) {
            Environment.EnvironmentConfig initialConfig = Environment.getConfigForStage(initialStage);
            Environment.EnvironmentConfig newConfig = Environment.getConfigForStage(newStage);
            
            // The current environment should match the new config, not the initial one
            assertEquals(newConfig.getBackgroundTheme(), environment.getBackgroundTheme(),
                "Background theme should match new stage configuration");
            assertEquals(newConfig.getDecorativeElements(), environment.getDecorativeElements(),
                "Decorative elements should match new stage configuration");
        }
    }

    @Provide
    Arbitrary<EvolutionStage> evolutionStages() {
        return Arbitraries.of(EvolutionStage.values());
    }
}