package com.digitalpet.viewmodel;

import com.digitalpet.model.*;
import com.digitalpet.service.AchievementSystem;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.List;

/**
 * ViewModel for the Achievement View, managing achievement data and UI state.
 * Provides property binding for reactive UI updates and handles achievement system integration.
 * 
 * Requirements addressed:
 * - 8.1: Achievement display with locked/unlocked states
 * - 8.2: Progress indicators for locked achievements
 * - 8.4: Achievement unlock detection and progress tracking
 */
public class AchievementViewModel {
    
    // Achievement system service
    private final AchievementSystem achievementSystem;
    
    // Observable properties for UI binding
    private final StringProperty achievementSummaryText;
    private final DoubleProperty completionPercentage;
    private final StringProperty completionPercentageText;
    private final BooleanProperty hasRecentUnlock;
    
    // Achievement collections
    private final ObservableList<Achievement> allAchievements;
    
    // Current user progress (for achievement evaluation)
    private UserProgress currentUserProgress;
    
    public AchievementViewModel(AchievementSystem achievementSystem) {
        this.achievementSystem = achievementSystem;
        
        // Initialize properties
        this.achievementSummaryText = new SimpleStringProperty(this, "achievementSummaryText", "0 / 0 Unlocked");
        this.completionPercentage = new SimpleDoubleProperty(this, "completionPercentage", 0.0);
        this.completionPercentageText = new SimpleStringProperty(this, "completionPercentageText", "0%");
        this.hasRecentUnlock = new SimpleBooleanProperty(this, "hasRecentUnlock", false);
        
        // Initialize collections
        this.allAchievements = FXCollections.observableArrayList();
        
        // Bind to achievement system properties
        bindToAchievementSystem();
        
        // Initialize data
        refreshAchievements();
    }
    
    /**
     * Binds properties to the achievement system
     */
    private void bindToAchievementSystem() {
        // Bind completion percentage
        completionPercentage.bind(achievementSystem.completionPercentageProperty());
        
        // Bind recent unlock status
        hasRecentUnlock.bind(achievementSystem.hasRecentUnlockProperty());
        
        // Update text properties when counts change
        achievementSystem.totalAchievementsProperty().addListener((observable, oldValue, newValue) -> updateSummaryText());
        achievementSystem.unlockedAchievementsProperty().addListener((observable, oldValue, newValue) -> updateSummaryText());
        achievementSystem.completionPercentageProperty().addListener((observable, oldValue, newValue) -> updatePercentageText());
        
        // Initial update
        updateSummaryText();
        updatePercentageText();
    }
    
    /**
     * Updates the achievement summary text
     */
    private void updateSummaryText() {
        int unlocked = achievementSystem.getUnlockedAchievementCount();
        int total = achievementSystem.getTotalAchievements();
        achievementSummaryText.set(String.format("%d / %d Unlocked", unlocked, total));
    }
    
    /**
     * Updates the completion percentage text
     */
    private void updatePercentageText() {
        double percentage = achievementSystem.getCompletionPercentage();
        completionPercentageText.set(String.format("%.0f%%", percentage * 100));
    }
    
    /**
     * Refreshes achievement data from the system
     */
    public void refreshAchievements() {
        allAchievements.clear();
        allAchievements.addAll(achievementSystem.getAllAchievements());
    }
    
    /**
     * Updates user progress and checks for new unlocks
     */
    public void updateUserProgress(UserProgress userProgress) {
        this.currentUserProgress = userProgress;
        
        if (userProgress != null) {
            // Check for new unlocks
            List<Achievement> newUnlocks = achievementSystem.checkForUnlocks(userProgress);
            
            // Refresh achievements if there were unlocks
            if (!newUnlocks.isEmpty()) {
                refreshAchievements();
            }
        }
    }
    
    /**
     * Gets all achievements
     */
    public List<Achievement> getAllAchievements() {
        return List.copyOf(allAchievements);
    }
    
    /**
     * Gets achievements by category
     */
    public List<Achievement> getAchievementsByCategory(AchievementCategory category) {
        return achievementSystem.getAchievementsByCategory(category);
    }
    
    /**
     * Gets unlocked achievements
     */
    public List<Achievement> getUnlockedAchievements() {
        return achievementSystem.getUnlockedAchievements();
    }
    
    /**
     * Gets locked achievements
     */
    public List<Achievement> getLockedAchievements() {
        return achievementSystem.getLockedAchievements();
    }
    
    /**
     * Gets achievements sorted by progress
     */
    public List<Achievement> getAchievementsByProgress() {
        return achievementSystem.getAchievementsByProgress();
    }
    
    /**
     * Gets achievements that are close to completion
     */
    public List<Achievement> getAlmostCompleteAchievements() {
        return achievementSystem.getAlmostCompleteAchievements();
    }
    
    /**
     * Gets recent unlocks for celebration
     */
    public List<Achievement> getRecentUnlocks() {
        return achievementSystem.getRecentUnlocks();
    }
    
    /**
     * Clears recent unlock status
     */
    public void clearRecentUnlocks() {
        achievementSystem.clearRecentUnlocks();
    }
    
    /**
     * Gets a specific achievement by ID
     */
    public Achievement getAchievement(String achievementId) {
        return achievementSystem.getAchievement(achievementId);
    }
    
    /**
     * Gets achievement statistics
     */
    public java.util.Map<String, Object> getAchievementStatistics() {
        return achievementSystem.getAchievementStatistics();
    }
    
    // Property getters for UI binding
    
    public StringProperty achievementSummaryTextProperty() {
        return achievementSummaryText;
    }
    
    public DoubleProperty completionPercentageProperty() {
        return completionPercentage;
    }
    
    public StringProperty completionPercentageTextProperty() {
        return completionPercentageText;
    }
    
    public BooleanProperty hasRecentUnlockProperty() {
        return hasRecentUnlock;
    }
    
    public ObservableList<Achievement> allAchievementsProperty() {
        return allAchievements;
    }
    
    // Convenience getters
    
    public String getAchievementSummaryText() {
        return achievementSummaryText.get();
    }
    
    public double getCompletionPercentage() {
        return completionPercentage.get();
    }
    
    public String getCompletionPercentageText() {
        return completionPercentageText.get();
    }
    
    public boolean hasRecentUnlock() {
        return hasRecentUnlock.get();
    }
    
    public UserProgress getCurrentUserProgress() {
        return currentUserProgress;
    }
    
    /**
     * Forces a refresh of all achievement data
     */
    public void forceRefresh() {
        refreshAchievements();
        updateSummaryText();
        updatePercentageText();
    }
    
    /**
     * Simulates achievement unlock for testing purposes
     */
    public void simulateAchievementUnlock(String achievementId) {
        Achievement achievement = getAchievement(achievementId);
        if (achievement != null && !achievement.isUnlocked()) {
            achievement.setUnlocked(true);
            refreshAchievements();
        }
    }
    
    /**
     * Resets all achievements for testing purposes
     */
    public void resetAllAchievements() {
        achievementSystem.resetAllAchievements();
        refreshAchievements();
    }
    
    /**
     * Gets the underlying achievement system
     */
    public AchievementSystem getAchievementSystem() {
        return achievementSystem;
    }
}