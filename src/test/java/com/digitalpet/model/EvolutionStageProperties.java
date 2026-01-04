package com.digitalpet.model;

import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based tests for EvolutionStage enum
 * Feature: digital-pet-evolution
 */
class EvolutionStageProperties {

    @Property
    @Label("Feature: digital-pet-evolution, Property: XP thresholds are monotonically increasing")
    void xpThresholdsAreMonotonicallyIncreasing(@ForAll("evolutionStages") EvolutionStage stage) {
        EvolutionStage next = stage.getNext();
        if (next != null) {
            assertTrue(next.getXpThreshold() > stage.getXpThreshold());
        }
    }

    @Property
    @Label("Feature: digital-pet-evolution, Property: fromXP returns correct stage for any XP value")
    void fromXpReturnsCorrectStage(@ForAll @IntRange(min = 0, max = 2000) int xp) {
        EvolutionStage stage = EvolutionStage.fromXP(xp);
        
        // Verify the returned stage is appropriate for the XP
        assertTrue(xp >= stage.getXpThreshold());
        
        // Verify next stage (if exists) requires more XP
        EvolutionStage next = stage.getNext();
        if (next != null) {
            assertTrue(xp < next.getXpThreshold());
        }
    }

    @Provide
    Arbitrary<EvolutionStage> evolutionStages() {
        return Arbitraries.of(EvolutionStage.values());
    }
}