package com.digitalpet.model;

/**
 * Categories for the achievement system
 * Each category tracks different types of user progress
 */
public enum AchievementCategory {
    /**
     * Achievements based on daily habit streaks and consistency
     * Examples: Water Warrior (7-day water intake streak), Study Streak (14-day study streak)
     */
    CONSISTENCY("Daily habit streaks", "🔥"),
    
    /**
     * Achievements based on total progress markers and cumulative goals
     * Examples: Fitness Champ (total steps milestone), Knowledge Seeker (total study hours)
     */
    MILESTONE("Total progress markers", "🏆"),
    
    /**
     * Achievements tied to pet development stages
     * Examples: First Evolution, Legendary Status, Pet Master
     */
    EVOLUTION("Pet development stages", "⭐");

    private final String description;
    private final String icon;

    AchievementCategory(String description, String icon) {
        this.description = description;
        this.icon = icon;
    }

    public String getDescription() {
        return description;
    }

    public String getIcon() {
        return icon;
    }

    /**
     * Gets the display text with icon
     */
    public String getDisplayText() {
        return icon + " " + name().toLowerCase();
    }
}