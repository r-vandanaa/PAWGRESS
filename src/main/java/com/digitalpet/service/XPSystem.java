package com.digitalpet.service;

import com.digitalpet.model.DailyHabits;
import com.digitalpet.model.DigitalPet;
import com.digitalpet.model.EvolutionStage;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

/**
 * Service class responsible for XP calculation and evolution progression logic.
 * Implements transparent XP calculation based on habit completion and consistency.
 * 
 * Requirements addressed:
 * - 2.1: XP calculation algorithm based on habit completion and consistency
 * - 2.2: Evolution progression with forward-only constraint
 * - 2.5: Progressive XP thresholds (100, 300, 600, 1000)
 */
public class XPSystem {
    
    // Base XP values for different completion levels
    private static final int BASE_XP_EXCELLENT = 20;  // 80-100% completion
    private static final int BASE_XP_GOOD = 15;       // 60-79% completion
    private static final int BASE_XP_FAIR = 10;       // 40-59% completion
    private static final int BASE_XP_POOR = 5;        // 20-39% completion
    private static final int BASE_XP_MINIMAL = 2;     // 1-19% completion
    
    // Consistency bonus multipliers
    private static final double CONSISTENCY_MULTIPLIER_EXCELLENT = 2.0;  // 7+ day streak
    private static final double CONSISTENCY_MULTIPLIER_GOOD = 1.5;       // 4-6 day streak
    private static final double CONSISTENCY_MULTIPLIER_FAIR = 1.2;       // 2-3 day streak
    private static final double CONSISTENCY_MULTIPLIER_NONE = 1.0;       // 0-1 day streak
    
    // Evolution thresholds as defined in requirements
    public static final Map<EvolutionStage, Integer> EVOLUTION_THRESHOLDS = Map.of(
        EvolutionStage.EGG, 0,
        EvolutionStage.BABY, 100,
        EvolutionStage.TEEN, 300,
        EvolutionStage.ADULT, 600,
        EvolutionStage.LEGENDARY, 1000
    );
    
    /**
     * Calculates XP to award based on daily habits completion and consistency score.
     * Uses transparent algorithm that considers both completion percentage and streak length.
     * 
     * @param habits The daily habits data
     * @param consistencyScore The consistency score (0.0 to 1.0) based on recent streak
     * @return The XP amount to award (always non-negative)
     */
    public int calculateXP(DailyHabits habits, double consistencyScore) {
        if (habits == null) {
            return 0;
        }
        
        // Get completion percentage (0.0 to 1.0)
        double completionPercentage = habits.getCompletionPercentage();
        
        // Determine base XP based on completion level
        int baseXP = getBaseXPForCompletion(completionPercentage);
        
        // Apply consistency multiplier
        double consistencyMultiplier = getConsistencyMultiplier(consistencyScore);
        
        // Calculate final XP (rounded to nearest integer)
        int finalXP = (int) Math.round(baseXP * consistencyMultiplier);
        
        return Math.max(0, finalXP);
    }
    
    /**
     * Calculates XP based on habit completion percentage only (no consistency bonus).
     * Useful for testing and when consistency data is not available.
     * 
     * @param habits The daily habits data
     * @return The base XP amount (before consistency multiplier)
     */
    public int calculateBaseXP(DailyHabits habits) {
        if (habits == null) {
            return 0;
        }
        
        // Get completion percentage (0.0 to 1.0)
        double completionPercentage = habits.getCompletionPercentage();
        
        // Return base XP without any consistency multiplier
        return getBaseXPForCompletion(completionPercentage);
    }
    
    /**
     * Determines base XP amount based on completion percentage.
     * 
     * @param completionPercentage Completion percentage (0.0 to 1.0)
     * @return Base XP amount
     */
    private int getBaseXPForCompletion(double completionPercentage) {
        double percentage = Math.max(0.0, Math.min(1.0, completionPercentage));
        
        if (percentage >= 0.8) {
            return BASE_XP_EXCELLENT;
        } else if (percentage >= 0.6) {
            return BASE_XP_GOOD;
        } else if (percentage >= 0.4) {
            return BASE_XP_FAIR;
        } else if (percentage >= 0.2) {
            return BASE_XP_POOR;
        } else if (percentage > 0.0) {
            return BASE_XP_MINIMAL;
        } else {
            return 0;
        }
    }
    
    /**
     * Determines consistency multiplier based on consistency score.
     * 
     * @param consistencyScore Consistency score (0.0 to 1.0)
     * @return Multiplier to apply to base XP
     */
    private double getConsistencyMultiplier(double consistencyScore) {
        double score = Math.max(0.0, Math.min(1.0, consistencyScore));
        
        if (score >= 0.8) {
            return CONSISTENCY_MULTIPLIER_EXCELLENT;
        } else if (score >= 0.6) {
            return CONSISTENCY_MULTIPLIER_GOOD;
        } else if (score >= 0.4) {
            return CONSISTENCY_MULTIPLIER_FAIR;
        } else {
            return CONSISTENCY_MULTIPLIER_NONE;
        }
    }
    
    /**
     * Calculates consistency score based on recent habit tracking history.
     * Considers both streak length and completion quality.
     * 
     * @param recentHabits List of recent daily habits (should be in chronological order)
     * @param daysToConsider Number of recent days to consider for consistency
     * @return Consistency score (0.0 to 1.0)
     */
    public double calculateConsistencyScore(List<DailyHabits> recentHabits, int daysToConsider) {
        if (recentHabits == null || recentHabits.isEmpty() || daysToConsider <= 0) {
            return 0.0;
        }
        
        // Limit to requested number of days
        int daysToCheck = Math.min(daysToConsider, recentHabits.size());
        
        // Calculate average completion percentage over the period
        double totalCompletion = 0.0;
        int validDays = 0;
        
        for (int i = 0; i < daysToCheck; i++) {
            DailyHabits habits = recentHabits.get(i);
            if (habits != null) {
                totalCompletion += habits.getCompletionPercentage();
                validDays++;
            }
        }
        
        if (validDays == 0) {
            return 0.0;
        }
        
        double averageCompletion = totalCompletion / validDays;
        
        // Apply streak bonus based on consecutive days with reasonable completion
        int streakLength = calculateStreakLength(recentHabits, 0.3); // 30% minimum for streak
        double streakBonus = Math.min(1.0, streakLength / 7.0); // Max bonus at 7 days
        
        // Combine average completion with streak bonus
        return Math.min(1.0, averageCompletion * 0.7 + streakBonus * 0.3);
    }
    
    /**
     * Calculates the length of current streak with minimum completion threshold.
     * 
     * @param recentHabits List of recent daily habits (most recent first)
     * @param minimumCompletion Minimum completion percentage to count toward streak
     * @return Length of current streak in days
     */
    private int calculateStreakLength(List<DailyHabits> recentHabits, double minimumCompletion) {
        if (recentHabits == null || recentHabits.isEmpty()) {
            return 0;
        }
        
        int streakLength = 0;
        for (DailyHabits habits : recentHabits) {
            if (habits != null && habits.getCompletionPercentage() >= minimumCompletion) {
                streakLength++;
            } else {
                break; // Streak broken
            }
        }
        
        return streakLength;
    }
    
    /**
     * Determines if a pet should evolve based on current XP.
     * Implements forward-only evolution constraint.
     * 
     * @param pet The digital pet to check
     * @return The new evolution stage if evolution should occur, null otherwise
     */
    public EvolutionStage checkForEvolution(DigitalPet pet) {
        if (pet == null) {
            return null;
        }
        
        int currentXP = pet.getExperiencePoints();
        EvolutionStage currentStage = pet.getCurrentStage();
        
        // Find the highest stage the pet qualifies for
        EvolutionStage targetStage = EvolutionStage.fromXP(currentXP);
        
        // Only evolve if target stage is higher than current (forward-only)
        if (targetStage.ordinal() > currentStage.ordinal()) {
            return targetStage;
        }
        
        return null; // No evolution needed
    }
    
    /**
     * Applies evolution to a pet if XP thresholds are met.
     * Returns true if evolution occurred.
     * 
     * @param pet The digital pet to potentially evolve
     * @return true if evolution occurred, false otherwise
     */
    public boolean applyEvolution(DigitalPet pet) {
        EvolutionStage newStage = checkForEvolution(pet);
        if (newStage != null) {
            pet.setCurrentStage(newStage);
            return true;
        }
        return false;
    }
    
    /**
     * Awards XP to a pet and handles automatic evolution.
     * 
     * @param pet The pet to award XP to
     * @param habits The daily habits that earned the XP
     * @param consistencyScore The consistency score for bonus calculation
     * @return The amount of XP awarded
     */
    public int awardXP(DigitalPet pet, DailyHabits habits, double consistencyScore) {
        if (pet == null || habits == null) {
            return 0;
        }
        
        int xpToAward = calculateXP(habits, consistencyScore);
        pet.addExperiencePoints(xpToAward);
        
        // Evolution is handled automatically by the DigitalPet model
        // through its XP property listener
        
        return xpToAward;
    }
    
    /**
     * Gets the XP threshold for a specific evolution stage.
     * 
     * @param stage The evolution stage
     * @return The XP threshold, or 0 if stage is null
     */
    public int getXPThreshold(EvolutionStage stage) {
        if (stage == null) {
            return 0;
        }
        return EVOLUTION_THRESHOLDS.getOrDefault(stage, 0);
    }
    
    /**
     * Gets all evolution thresholds as a map.
     * 
     * @return Map of evolution stages to XP thresholds
     */
    public Map<EvolutionStage, Integer> getAllThresholds() {
        return Map.copyOf(EVOLUTION_THRESHOLDS);
    }
    
    /**
     * Calculates XP needed for next evolution stage.
     * 
     * @param pet The digital pet
     * @return XP needed for next evolution, or 0 if at max stage
     */
    public int getXPToNextEvolution(DigitalPet pet) {
        if (pet == null) {
            return 0;
        }
        return pet.getXpToNextEvolution();
    }
    
    /**
     * Gets evolution progress as a percentage.
     * 
     * @param pet The digital pet
     * @return Progress percentage (0.0 to 1.0)
     */
    public double getEvolutionProgress(DigitalPet pet) {
        if (pet == null) {
            return 0.0;
        }
        return pet.getEvolutionProgress();
    }
    
    /**
     * Validates that evolution thresholds are properly ordered.
     * Used for testing and system validation.
     * 
     * @return true if thresholds are valid, false otherwise
     */
    public boolean validateEvolutionThresholds() {
        EvolutionStage[] stages = EvolutionStage.values();
        
        for (int i = 1; i < stages.length; i++) {
            int currentThreshold = EVOLUTION_THRESHOLDS.get(stages[i]);
            int previousThreshold = EVOLUTION_THRESHOLDS.get(stages[i - 1]);
            
            if (currentThreshold <= previousThreshold) {
                return false; // Thresholds must be strictly increasing
            }
        }
        
        return true;
    }
}