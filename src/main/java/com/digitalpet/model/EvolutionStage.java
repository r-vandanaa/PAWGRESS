package com.digitalpet.model;

/**
 * Represents the distinct phases of pet development
 * Each stage has specific XP requirements and visual characteristics
 */
public enum EvolutionStage {
    /**
     * Initial stage - Simple oval shape with soft pulsing animation
     */
    EGG(0, "Egg"),
    
    /**
     * First evolution - Small rounded body with large eyes and basic accessories
     * Requires 100 XP to reach
     */
    BABY(100, "Baby"),
    
    /**
     * Second evolution - Medium size with school accessories and expressive animations
     * Requires 300 XP to reach
     */
    TEEN(300, "Teen"),
    
    /**
     * Third evolution - Full size with professional accessories
     * Requires 600 XP to reach
     */
    ADULT(600, "Adult"),
    
    /**
     * Final evolution - Magical aura effects and enhanced visual presence
     * Requires 1000 XP to reach
     */
    LEGENDARY(1000, "Legendary");

    private final int xpThreshold;
    private final String displayName;

    EvolutionStage(int xpThreshold, String displayName) {
        this.xpThreshold = xpThreshold;
        this.displayName = displayName;
    }

    public int getXpThreshold() {
        return xpThreshold;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Gets the next evolution stage, or null if already at maximum
     */
    public EvolutionStage getNext() {
        EvolutionStage[] stages = values();
        int currentIndex = this.ordinal();
        return currentIndex < stages.length - 1 ? stages[currentIndex + 1] : null;
    }

    /**
     * Determines evolution stage based on current XP
     */
    public static EvolutionStage fromXP(int xp) {
        EvolutionStage[] stages = values();
        for (int i = stages.length - 1; i >= 0; i--) {
            if (xp >= stages[i].getXpThreshold()) {
                return stages[i];
            }
        }
        return EGG;
    }
}