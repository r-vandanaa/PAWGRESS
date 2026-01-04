package com.digitalpet.service;

import com.digitalpet.model.DailyHabits;
import com.digitalpet.model.PetMood;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * Service class responsible for calculating and managing pet mood based on habit patterns.
 * Implements mood calculation using five predefined states with habit pattern analysis
 * and mood transition smoothing algorithm.
 * 
 * Requirements addressed:
 * - 4.1: Mood calculation using five predefined states
 * - 4.2-4.6: Mood-based environment effects
 */
public class MoodSystem {
    
    private final ObjectProperty<PetMood> currentMood;
    private PetMood previousMood;
    
    // Mood calculation weights
    private static final double COMPLETION_WEIGHT = 0.4;
    private static final double CONSISTENCY_WEIGHT = 0.3;
    private static final double SLEEP_WEIGHT = 0.2;
    private static final double ACHIEVEMENT_WEIGHT = 0.1;
    
    // Mood thresholds for calculation
    private static final double HAPPY_THRESHOLD = 0.75;
    private static final double NEUTRAL_THRESHOLD = 0.5;
    private static final double SLEEPY_THRESHOLD = 0.3;
    private static final double WORRIED_THRESHOLD = 0.15;
    
    // Smoothing factor for mood transitions (0.0 = no smoothing, 1.0 = full smoothing)
    private static final double SMOOTHING_FACTOR = 0.3;
    
    public MoodSystem() {
        this.currentMood = new SimpleObjectProperty<>(PetMood.NEUTRAL);
        this.previousMood = PetMood.NEUTRAL;
    }
    
    /**
     * Calculates pet mood based on recent habit patterns
     * @param recentHabits List of DailyHabits for the last 3 days (most recent first)
     * @param hasRecentAchievement Whether an achievement was unlocked recently
     * @return The calculated mood
     */
    public PetMood calculateMood(List<DailyHabits> recentHabits, boolean hasRecentAchievement) {
        // If there's a recent achievement, celebrate!
        if (hasRecentAchievement) {
            return PetMood.CELEBRATING;
        }
        
        if (recentHabits == null || recentHabits.isEmpty()) {
            return PetMood.NEUTRAL;
        }
        
        // Calculate mood score based on multiple factors
        double moodScore = calculateMoodScore(recentHabits);
        
        // Apply smoothing with previous mood
        double smoothedScore = applyMoodSmoothing(moodScore);
        
        // Determine mood based on smoothed score
        PetMood newMood = determineMoodFromScore(smoothedScore, recentHabits);
        
        return newMood;
    }
    
    /**
     * Updates the current mood and stores previous mood for smoothing
     * @param recentHabits List of recent daily habits
     * @param hasRecentAchievement Whether there's a recent achievement
     */
    public void updateMood(List<DailyHabits> recentHabits, boolean hasRecentAchievement) {
        this.previousMood = getCurrentMood();
        PetMood newMood = calculateMood(recentHabits, hasRecentAchievement);
        this.currentMood.set(newMood);
    }
    
    /**
     * Calculates the base mood score from habit patterns
     */
    private double calculateMoodScore(List<DailyHabits> recentHabits) {
        // 1. Calculate habit completion percentage for last 3 days
        double completionScore = calculateCompletionScore(recentHabits);
        
        // 2. Apply consistency bonus for streak maintenance
        double consistencyScore = calculateConsistencyScore(recentHabits);
        
        // 3. Determine sleep-based mood adjustments
        double sleepScore = calculateSleepScore(recentHabits);
        
        // Weighted combination of factors
        return (completionScore * COMPLETION_WEIGHT) + 
               (consistencyScore * CONSISTENCY_WEIGHT) + 
               (sleepScore * SLEEP_WEIGHT);
    }
    
    /**
     * Calculates completion score based on habit completion percentages
     */
    private double calculateCompletionScore(List<DailyHabits> recentHabits) {
        double totalCompletion = 0.0;
        int count = 0;
        
        for (DailyHabits habits : recentHabits) {
            if (habits != null) {
                totalCompletion += habits.getCompletionPercentage();
                count++;
            }
        }
        
        return count > 0 ? totalCompletion / count : 0.0;
    }
    
    /**
     * Calculates consistency score based on habit tracking regularity
     */
    private double calculateConsistencyScore(List<DailyHabits> recentHabits) {
        if (recentHabits.size() < 2) {
            return 0.5; // Neutral score for insufficient data
        }
        
        double consistencyScore = 0.0;
        int comparisons = 0;
        
        // Compare consecutive days for consistency
        for (int i = 0; i < recentHabits.size() - 1; i++) {
            DailyHabits current = recentHabits.get(i);
            DailyHabits previous = recentHabits.get(i + 1);
            
            if (current != null && previous != null) {
                double currentCompletion = current.getCompletionPercentage();
                double previousCompletion = previous.getCompletionPercentage();
                
                // Reward improvement or maintaining good habits
                if (currentCompletion >= previousCompletion) {
                    consistencyScore += 1.0;
                } else {
                    // Penalize decline, but not too harshly
                    consistencyScore += Math.max(0.3, 1.0 - (previousCompletion - currentCompletion));
                }
                comparisons++;
            }
        }
        
        return comparisons > 0 ? consistencyScore / comparisons : 0.5;
    }
    
    /**
     * Calculates sleep-based mood score
     */
    private double calculateSleepScore(List<DailyHabits> recentHabits) {
        double totalSleepScore = 0.0;
        int count = 0;
        
        for (DailyHabits habits : recentHabits) {
            if (habits != null) {
                double sleepHours = habits.getSleepHours();
                
                // Optimal sleep is 7-9 hours
                double sleepScore;
                if (sleepHours >= 7.0 && sleepHours <= 9.0) {
                    sleepScore = 1.0; // Perfect sleep
                } else if (sleepHours >= 6.0 && sleepHours <= 10.0) {
                    sleepScore = 0.8; // Good sleep
                } else if (sleepHours >= 5.0 && sleepHours <= 11.0) {
                    sleepScore = 0.6; // Acceptable sleep
                } else if (sleepHours >= 4.0 && sleepHours <= 12.0) {
                    sleepScore = 0.4; // Poor sleep
                } else {
                    sleepScore = 0.2; // Very poor sleep
                }
                
                totalSleepScore += sleepScore;
                count++;
            }
        }
        
        return count > 0 ? totalSleepScore / count : 0.5;
    }
    
    /**
     * Applies mood transition smoothing to prevent rapid mood swings
     */
    private double applyMoodSmoothing(double newMoodScore) {
        double previousMoodScore = getMoodScore(previousMood);
        
        // Apply weighted average with previous mood
        return (newMoodScore * (1.0 - SMOOTHING_FACTOR)) + 
               (previousMoodScore * SMOOTHING_FACTOR);
    }
    
    /**
     * Gets the numeric score for a mood (for smoothing calculations)
     */
    private double getMoodScore(PetMood mood) {
        return switch (mood) {
            case CELEBRATING -> 1.0;
            case HAPPY -> 0.8;
            case NEUTRAL -> 0.5;
            case SLEEPY -> 0.3;
            case WORRIED -> 0.2;
        };
    }
    
    /**
     * Determines the final mood based on the calculated score and special conditions
     */
    private PetMood determineMoodFromScore(double score, List<DailyHabits> recentHabits) {
        // Check for special sleep-based mood (SLEEPY)
        if (shouldBeSleepy(recentHabits)) {
            return PetMood.SLEEPY;
        }
        
        // Determine mood based on score thresholds
        if (score >= HAPPY_THRESHOLD) {
            return PetMood.HAPPY;
        } else if (score >= NEUTRAL_THRESHOLD) {
            return PetMood.NEUTRAL;
        } else if (score >= SLEEPY_THRESHOLD) {
            return PetMood.NEUTRAL; // Still neutral, not worried yet
        } else if (score >= WORRIED_THRESHOLD) {
            return PetMood.WORRIED;
        } else {
            return PetMood.WORRIED; // Very low scores still result in worried
        }
    }
    
    /**
     * Checks if the pet should be in SLEEPY mood based on sleep patterns
     */
    private boolean shouldBeSleepy(List<DailyHabits> recentHabits) {
        if (recentHabits.isEmpty()) {
            return false;
        }
        
        // Check most recent day's sleep
        DailyHabits mostRecent = recentHabits.get(0);
        if (mostRecent != null) {
            double sleepHours = mostRecent.getSleepHours();
            
            // Pet is sleepy if sleep is very poor (< 5 hours) or excessive (> 12 hours)
            return sleepHours < 5.0 || sleepHours > 12.0;
        }
        
        return false;
    }
    
    /**
     * Gets the current mood
     */
    public PetMood getCurrentMood() {
        return currentMood.get();
    }
    
    /**
     * Gets the current mood property for binding
     */
    public ObjectProperty<PetMood> currentMoodProperty() {
        return currentMood;
    }
    
    /**
     * Sets the current mood directly (for testing or special cases)
     */
    public void setCurrentMood(PetMood mood) {
        if (mood != null) {
            this.previousMood = getCurrentMood();
            this.currentMood.set(mood);
        }
    }
    
    /**
     * Gets the previous mood (for transition analysis)
     */
    public PetMood getPreviousMood() {
        return previousMood;
    }
    
    /**
     * Checks if the mood has changed from the previous update
     */
    public boolean hasMoodChanged() {
        return !getCurrentMood().equals(previousMood);
    }
    
    /**
     * Gets mood analysis for debugging/display purposes
     */
    public Map<String, Object> getMoodAnalysis(List<DailyHabits> recentHabits, boolean hasRecentAchievement) {
        Map<String, Object> analysis = new HashMap<>();
        
        if (hasRecentAchievement) {
            analysis.put("reason", "Recent achievement unlocked");
            analysis.put("mood", PetMood.CELEBRATING);
            return analysis;
        }
        
        if (recentHabits == null || recentHabits.isEmpty()) {
            analysis.put("reason", "No habit data available");
            analysis.put("mood", PetMood.NEUTRAL);
            return analysis;
        }
        
        double completionScore = calculateCompletionScore(recentHabits);
        double consistencyScore = calculateConsistencyScore(recentHabits);
        double sleepScore = calculateSleepScore(recentHabits);
        double overallScore = calculateMoodScore(recentHabits);
        double smoothedScore = applyMoodSmoothing(overallScore);
        
        analysis.put("completionScore", completionScore);
        analysis.put("consistencyScore", consistencyScore);
        analysis.put("sleepScore", sleepScore);
        analysis.put("overallScore", overallScore);
        analysis.put("smoothedScore", smoothedScore);
        analysis.put("mood", determineMoodFromScore(smoothedScore, recentHabits));
        analysis.put("previousMood", previousMood);
        analysis.put("moodChanged", hasMoodChanged());
        
        return analysis;
    }
    
    /**
     * Resets the mood system to initial state
     */
    public void reset() {
        this.previousMood = PetMood.NEUTRAL;
        this.currentMood.set(PetMood.NEUTRAL);
    }
}