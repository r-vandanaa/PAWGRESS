package com.digitalpet.model;

import javafx.beans.property.*;
import java.time.LocalDateTime;

/**
 * Core model representing the digital pet with JavaFX properties for reactive UI binding.
 * Manages pet state including evolution, mood, experience, energy, and interaction tracking.
 * 
 * Requirements addressed:
 * - 1.1: Pet visual consistency across evolution stages
 * - 2.1: XP-based evolution system with transparent calculation
 * - 2.3: Forward-only evolution progression through defined stages
 */
public class DigitalPet {
    
    // Core identity properties
    private final StringProperty name;
    private final ObjectProperty<EvolutionStage> currentStage;
    private final ObjectProperty<PetMood> currentMood;
    
    // Progress tracking properties
    private final IntegerProperty experiencePoints;
    private final IntegerProperty energyLevel;
    
    // Interaction tracking
    private final ObjectProperty<LocalDateTime> lastInteraction;
    
    /**
     * Creates a new DigitalPet with default values
     * Starts at EGG stage with NEUTRAL mood
     */
    public DigitalPet() {
        this("My Pet");
    }
    
    /**
     * Creates a new DigitalPet with specified name
     * @param petName The name for the pet
     */
    public DigitalPet(String petName) {
        this.name = new SimpleStringProperty(this, "name", validateName(petName));
        this.currentStage = new SimpleObjectProperty<>(this, "currentStage", EvolutionStage.EGG);
        this.currentMood = new SimpleObjectProperty<>(this, "currentMood", PetMood.NEUTRAL);
        this.experiencePoints = new SimpleIntegerProperty(this, "experiencePoints", 0);
        this.energyLevel = new SimpleIntegerProperty(this, "energyLevel", 50);
        this.lastInteraction = new SimpleObjectProperty<>(this, "lastInteraction", LocalDateTime.now());
        
        // Add validation listeners
        setupValidation();
    }
    
    /**
     * Sets up property validation and automatic evolution checking
     */
    private void setupValidation() {
        // Validate XP changes and trigger evolution if needed
        experiencePoints.addListener((observable, oldValue, newValue) -> {
            if (newValue.intValue() < 0) {
                experiencePoints.set(0);
                return;
            }
            
            // Check for evolution opportunity
            EvolutionStage newStage = EvolutionStage.fromXP(newValue.intValue());
            if (newStage != getCurrentStage() && newStage.ordinal() > getCurrentStage().ordinal()) {
                setCurrentStage(newStage);
            }
        });
        
        // Validate energy level bounds
        energyLevel.addListener((observable, oldValue, newValue) -> {
            int energy = newValue.intValue();
            if (energy < 0) {
                energyLevel.set(0);
            } else if (energy > 100) {
                energyLevel.set(100);
            }
        });
        
        // Validate name changes
        name.addListener((observable, oldValue, newValue) -> {
            String validatedName = validateName(newValue);
            if (!validatedName.equals(newValue)) {
                name.set(validatedName);
            }
        });
    }
    
    /**
     * Validates pet name input
     * @param petName The name to validate
     * @return A valid pet name
     */
    private String validateName(String petName) {
        if (petName == null || petName.trim().isEmpty()) {
            return "My Pet";
        }
        
        String trimmed = petName.trim();
        if (trimmed.length() > 20) {
            return trimmed.substring(0, 20);
        }
        
        return trimmed;
    }
    
    // Property getters for JavaFX binding
    
    public StringProperty nameProperty() {
        return name;
    }
    
    public ObjectProperty<EvolutionStage> currentStageProperty() {
        return currentStage;
    }
    
    public ObjectProperty<PetMood> currentMoodProperty() {
        return currentMood;
    }
    
    public IntegerProperty experiencePointsProperty() {
        return experiencePoints;
    }
    
    public IntegerProperty energyLevelProperty() {
        return energyLevel;
    }
    
    public ObjectProperty<LocalDateTime> lastInteractionProperty() {
        return lastInteraction;
    }
    
    // Convenience getters
    
    public String getName() {
        return name.get();
    }
    
    public EvolutionStage getCurrentStage() {
        return currentStage.get();
    }
    
    public PetMood getCurrentMood() {
        return currentMood.get();
    }
    
    public int getExperiencePoints() {
        return experiencePoints.get();
    }
    
    public int getEnergyLevel() {
        return energyLevel.get();
    }
    
    public LocalDateTime getLastInteraction() {
        return lastInteraction.get();
    }
    
    // Convenience setters
    
    public void setName(String name) {
        this.name.set(name);
    }
    
    public void setCurrentStage(EvolutionStage stage) {
        if (stage != null) {
            this.currentStage.set(stage);
        }
    }
    
    public void setCurrentMood(PetMood mood) {
        if (mood != null) {
            this.currentMood.set(mood);
        }
    }
    
    public void setExperiencePoints(int xp) {
        this.experiencePoints.set(xp);
    }
    
    public void setEnergyLevel(int energy) {
        this.energyLevel.set(energy);
    }
    
    public void setLastInteraction(LocalDateTime interaction) {
        if (interaction != null) {
            this.lastInteraction.set(interaction);
        }
    }
    
    // Utility methods
    
    /**
     * Adds experience points and triggers evolution if thresholds are met
     * @param xpToAdd The amount of XP to add (must be non-negative)
     */
    public void addExperiencePoints(int xpToAdd) {
        if (xpToAdd > 0) {
            setExperiencePoints(getExperiencePoints() + xpToAdd);
        }
    }
    
    /**
     * Updates energy level based on mood multiplier
     * @param baseEnergy The base energy value before mood adjustment
     */
    public void updateEnergyFromMood(int baseEnergy) {
        double moodMultiplier = getCurrentMood().getEnergyMultiplier();
        int adjustedEnergy = (int) Math.round(baseEnergy * moodMultiplier);
        setEnergyLevel(adjustedEnergy);
    }
    
    /**
     * Records a user interaction with the pet
     */
    public void recordInteraction() {
        setLastInteraction(LocalDateTime.now());
    }
    
    /**
     * Gets XP needed for next evolution, or 0 if at max stage
     * @return XP needed for next evolution
     */
    public int getXpToNextEvolution() {
        EvolutionStage nextStage = getCurrentStage().getNext();
        if (nextStage == null) {
            return 0; // Already at max stage
        }
        return Math.max(0, nextStage.getXpThreshold() - getExperiencePoints());
    }
    
    /**
     * Gets evolution progress as percentage (0.0 to 1.0)
     * @return Progress toward next evolution
     */
    public double getEvolutionProgress() {
        EvolutionStage currentStage = getCurrentStage();
        EvolutionStage nextStage = currentStage.getNext();
        
        if (nextStage == null) {
            return 1.0; // At max stage
        }
        
        int currentXp = getExperiencePoints();
        int currentThreshold = currentStage.getXpThreshold();
        int nextThreshold = nextStage.getXpThreshold();
        
        if (nextThreshold == currentThreshold) {
            return 1.0;
        }
        
        return (double) (currentXp - currentThreshold) / (nextThreshold - currentThreshold);
    }
    
    @Override
    public String toString() {
        return String.format("DigitalPet{name='%s', stage=%s, mood=%s, xp=%d, energy=%d}", 
                getName(), getCurrentStage(), getCurrentMood(), 
                getExperiencePoints(), getEnergyLevel());
    }
}