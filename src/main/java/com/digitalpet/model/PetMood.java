package com.digitalpet.model;

import javafx.scene.paint.Color;

/**
 * Represents the pet's emotional state based on habit patterns
 * Each mood affects environment effects and pet animations
 */
public enum PetMood {
    /**
     * Pet is content and energetic - triggered by good habit patterns
     */
    HAPPY(0.8, "😊", Color.GOLD, "Happy"),
    
    /**
     * Pet is in a default state - neutral habit patterns
     */
    NEUTRAL(0.5, "😐", Color.LIGHTBLUE, "Neutral"),
    
    /**
     * Pet is tired - triggered by poor sleep habits
     */
    SLEEPY(0.3, "😴", Color.LAVENDER, "Sleepy"),
    
    /**
     * Pet is concerned - triggered by declining habit patterns
     */
    WORRIED(0.2, "😟", Color.LIGHTCORAL, "Worried"),
    
    /**
     * Pet is excited - triggered by achievements and milestones
     */
    CELEBRATING(1.0, "🎉", Color.LIME, "Celebrating");

    private final double energyMultiplier;
    private final String emoji;
    private final Color environmentTint;
    private final String displayName;

    PetMood(double energyMultiplier, String emoji, Color environmentTint, String displayName) {
        this.energyMultiplier = energyMultiplier;
        this.emoji = emoji;
        this.environmentTint = environmentTint;
        this.displayName = displayName;
    }

    public double getEnergyMultiplier() {
        return energyMultiplier;
    }

    public String getEmoji() {
        return emoji;
    }

    public Color getEnvironmentTint() {
        return environmentTint;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Gets the mood display text with emoji
     */
    public String getDisplayText() {
        return emoji + " " + displayName;
    }
}