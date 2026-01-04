package com.digitalpet.model;

import javafx.beans.property.*;
import java.time.LocalDateTime;
import java.util.function.Predicate;

/**
 * Model representing an achievement with unlock conditions and progress tracking.
 * Uses JavaFX properties for reactive UI binding and supports three achievement categories.
 * 
 * Requirements addressed:
 * - 8.3: Three achievement categories (consistency, milestone, evolution)
 * - 8.4: Achievement unlock detection and progress tracking
 */
public class Achievement {
    
    // Core achievement properties
    private final StringProperty id;
    private final StringProperty title;
    private final StringProperty description;
    private final ObjectProperty<AchievementCategory> category;
    
    // Progress and unlock tracking
    private final BooleanProperty isUnlocked;
    private final ObjectProperty<LocalDateTime> unlockedDate;
    private final DoubleProperty progress; // 0.0 to 1.0
    private final IntegerProperty currentValue;
    private final IntegerProperty targetValue;
    
    // Unlock condition (not a property since it's functional)
    private Predicate<UserProgress> unlockCondition;
    
    /**
     * Creates a new Achievement with specified parameters
     * @param id Unique identifier for the achievement
     * @param title Display title for the achievement
     * @param description Detailed description of the achievement
     * @param category The achievement category
     * @param targetValue The target value needed to unlock (for progress tracking)
     * @param unlockCondition Predicate that determines if achievement should be unlocked
     */
    public Achievement(String id, String title, String description, 
                      AchievementCategory category, int targetValue,
                      Predicate<UserProgress> unlockCondition) {
        this.id = new SimpleStringProperty(this, "id", validateId(id));
        this.title = new SimpleStringProperty(this, "title", validateTitle(title));
        this.description = new SimpleStringProperty(this, "description", validateDescription(description));
        this.category = new SimpleObjectProperty<>(this, "category", category != null ? category : AchievementCategory.MILESTONE);
        
        this.isUnlocked = new SimpleBooleanProperty(this, "isUnlocked", false);
        this.unlockedDate = new SimpleObjectProperty<>(this, "unlockedDate", null);
        this.progress = new SimpleDoubleProperty(this, "progress", 0.0);
        this.currentValue = new SimpleIntegerProperty(this, "currentValue", 0);
        this.targetValue = new SimpleIntegerProperty(this, "targetValue", Math.max(1, targetValue));
        
        this.unlockCondition = unlockCondition != null ? unlockCondition : (progress) -> false;
        
        // Set up property listeners
        setupPropertyListeners();
    }
    
    /**
     * Creates a new Achievement with default target value of 1
     */
    public Achievement(String id, String title, String description, 
                      AchievementCategory category, Predicate<UserProgress> unlockCondition) {
        this(id, title, description, category, 1, unlockCondition);
    }
    
    /**
     * Sets up property validation and automatic progress calculation
     */
    private void setupPropertyListeners() {
        // Update progress when current or target value changes
        currentValue.addListener((observable, oldValue, newValue) -> updateProgress());
        targetValue.addListener((observable, oldValue, newValue) -> updateProgress());
        
        // Validate progress bounds
        progress.addListener((observable, oldValue, newValue) -> {
            double progressValue = newValue.doubleValue();
            if (progressValue < 0.0) {
                progress.set(0.0);
            } else if (progressValue > 1.0) {
                progress.set(1.0);
            }
        });
        
        // Validate current value bounds
        currentValue.addListener((observable, oldValue, newValue) -> {
            if (newValue.intValue() < 0) {
                currentValue.set(0);
            }
        });
        
        // Validate target value bounds
        targetValue.addListener((observable, oldValue, newValue) -> {
            if (newValue.intValue() < 1) {
                targetValue.set(1);
            }
        });
    }
    
    /**
     * Updates progress based on current and target values
     */
    private void updateProgress() {
        int current = getCurrentValue();
        int target = getTargetValue();
        
        if (target <= 0) {
            progress.set(0.0);
        } else {
            double newProgress = Math.min(1.0, (double) current / target);
            progress.set(newProgress);
        }
    }
    
    /**
     * Validates achievement ID
     */
    private String validateId(String id) {
        if (id == null || id.trim().isEmpty()) {
            return "unknown_achievement";
        }
        return id.trim().toLowerCase().replaceAll("[^a-z0-9_]", "_");
    }
    
    /**
     * Validates achievement title
     */
    private String validateTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            return "Untitled Achievement";
        }
        String trimmed = title.trim();
        return trimmed.length() > 50 ? trimmed.substring(0, 50) : trimmed;
    }
    
    /**
     * Validates achievement description
     */
    private String validateDescription(String description) {
        if (description == null || description.trim().isEmpty()) {
            return "No description available";
        }
        String trimmed = description.trim();
        return trimmed.length() > 200 ? trimmed.substring(0, 200) : trimmed;
    }
    
    // Property getters for JavaFX binding
    
    public StringProperty idProperty() {
        return id;
    }
    
    public StringProperty titleProperty() {
        return title;
    }
    
    public StringProperty descriptionProperty() {
        return description;
    }
    
    public ObjectProperty<AchievementCategory> categoryProperty() {
        return category;
    }
    
    public BooleanProperty isUnlockedProperty() {
        return isUnlocked;
    }
    
    public ObjectProperty<LocalDateTime> unlockedDateProperty() {
        return unlockedDate;
    }
    
    public DoubleProperty progressProperty() {
        return progress;
    }
    
    public IntegerProperty currentValueProperty() {
        return currentValue;
    }
    
    public IntegerProperty targetValueProperty() {
        return targetValue;
    }
    
    // Convenience getters
    
    public String getId() {
        return id.get();
    }
    
    public String getTitle() {
        return title.get();
    }
    
    public String getDescription() {
        return description.get();
    }
    
    public AchievementCategory getCategory() {
        return category.get();
    }
    
    public boolean isUnlocked() {
        return isUnlocked.get();
    }
    
    public LocalDateTime getUnlockedDate() {
        return unlockedDate.get();
    }
    
    public double getProgress() {
        return progress.get();
    }
    
    public int getCurrentValue() {
        return currentValue.get();
    }
    
    public int getTargetValue() {
        return targetValue.get();
    }
    
    public Predicate<UserProgress> getUnlockCondition() {
        return unlockCondition;
    }
    
    // Convenience setters
    
    public void setId(String id) {
        this.id.set(id);
    }
    
    public void setTitle(String title) {
        this.title.set(title);
    }
    
    public void setDescription(String description) {
        this.description.set(description);
    }
    
    public void setCategory(AchievementCategory category) {
        if (category != null) {
            this.category.set(category);
        }
    }
    
    public void setUnlocked(boolean unlocked) {
        this.isUnlocked.set(unlocked);
        if (unlocked && getUnlockedDate() == null) {
            setUnlockedDate(LocalDateTime.now());
        }
    }
    
    public void setUnlockedDate(LocalDateTime date) {
        this.unlockedDate.set(date);
    }
    
    public void setProgress(double progress) {
        this.progress.set(progress);
    }
    
    public void setCurrentValue(int value) {
        this.currentValue.set(value);
    }
    
    public void setTargetValue(int value) {
        this.targetValue.set(value);
    }
    
    public void setUnlockCondition(Predicate<UserProgress> condition) {
        this.unlockCondition = condition != null ? condition : (progress) -> false;
    }
    
    // Utility methods
    
    /**
     * Checks if this achievement should be unlocked based on user progress
     * @param userProgress The current user progress data
     * @return true if achievement should be unlocked
     */
    public boolean shouldUnlock(UserProgress userProgress) {
        if (isUnlocked()) {
            return false; // Already unlocked
        }
        
        if (unlockCondition == null || userProgress == null) {
            return false;
        }
        
        return unlockCondition.test(userProgress);
    }
    
    /**
     * Attempts to unlock this achievement based on user progress
     * @param userProgress The current user progress data
     * @return true if achievement was unlocked (newly unlocked)
     */
    public boolean tryUnlock(UserProgress userProgress) {
        if (shouldUnlock(userProgress)) {
            setUnlocked(true);
            return true;
        }
        return false;
    }
    
    /**
     * Updates the current progress value and checks for unlock
     * @param newValue The new current value
     * @param userProgress The user progress for unlock checking
     * @return true if achievement was unlocked
     */
    public boolean updateProgress(int newValue, UserProgress userProgress) {
        setCurrentValue(newValue);
        return tryUnlock(userProgress);
    }
    
    /**
     * Gets progress as a percentage string
     * @return Progress as "X%" or "Unlocked" if completed
     */
    public String getProgressText() {
        if (isUnlocked()) {
            return "Unlocked";
        }
        return String.format("%.0f%%", getProgress() * 100);
    }
    
    /**
     * Gets progress with values as text
     * @return Progress as "current/target" or "Unlocked"
     */
    public String getProgressWithValues() {
        if (isUnlocked()) {
            return "Unlocked";
        }
        return String.format("%d/%d", getCurrentValue(), getTargetValue());
    }
    
    /**
     * Gets the display text with category icon
     * @return Formatted display text
     */
    public String getDisplayText() {
        return getCategory().getIcon() + " " + getTitle();
    }
    
    /**
     * Checks if achievement is completed (progress >= 1.0)
     * @return true if completed
     */
    public boolean isCompleted() {
        return getProgress() >= 1.0;
    }
    
    /**
     * Gets estimated completion time based on current progress rate
     * @param dailyProgressRate Average daily progress rate
     * @return Estimated days to completion, or 0 if already completed
     */
    public int getEstimatedDaysToCompletion(double dailyProgressRate) {
        if (isCompleted() || dailyProgressRate <= 0) {
            return 0;
        }
        
        double remainingProgress = 1.0 - getProgress();
        return (int) Math.ceil(remainingProgress / dailyProgressRate);
    }
    
    @Override
    public String toString() {
        return String.format("Achievement{id='%s', title='%s', category=%s, progress=%.2f, unlocked=%s}", 
                getId(), getTitle(), getCategory(), getProgress(), isUnlocked());
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        Achievement that = (Achievement) obj;
        return getId().equals(that.getId());
    }
    
    @Override
    public int hashCode() {
        return getId().hashCode();
    }
}