package com.digitalpet.model;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.paint.Color;

import java.util.List;
import java.util.Map;

/**
 * Represents the visual environment that evolves with the pet and responds to mood changes.
 * Each evolution stage has a corresponding environment theme with mood-responsive effects.
 * 
 * Requirements addressed:
 * - 5.1: Environment themes for each evolution stage
 * - 4.2-4.6: Mood-based lighting and particle effects
 */
public class Environment {
    
    private final ObjectProperty<EvolutionStage> currentStage;
    private final ObjectProperty<PetMood> currentMood;
    private final StringProperty backgroundTheme;
    private final ListProperty<String> decorativeElements;
    
    // Mood-responsive properties
    private final ObjectProperty<Color> lightingTint;
    private final DoubleProperty lightingIntensity;
    private final ListProperty<String> particleEffects;
    private final BooleanProperty hasSpecialEffects;
    private final DoubleProperty animationSpeed;
    
    // Predefined environment configurations for each evolution stage
    private static final Map<EvolutionStage, EnvironmentConfig> THEME_CONFIGS = Map.of(
        EvolutionStage.EGG, new EnvironmentConfig("soft-gradient.svg", List.of()),
        EvolutionStage.BABY, new EnvironmentConfig("nursery.svg", List.of("plant-small.svg")),
        EvolutionStage.TEEN, new EnvironmentConfig("study-room.svg", List.of("desk.svg", "books.svg")),
        EvolutionStage.ADULT, new EnvironmentConfig("office.svg", List.of("bookshelf.svg", "window.svg")),
        EvolutionStage.LEGENDARY, new EnvironmentConfig("magical.svg", List.of("particles.svg", "aura.svg"))
    );
    
    // Mood-responsive effect configurations
    private static final Map<PetMood, MoodEffectConfig> MOOD_EFFECTS = Map.of(
        PetMood.HAPPY, new MoodEffectConfig(Color.GOLD, 1.0, List.of("sparkle.svg"), true, 1.2),
        PetMood.NEUTRAL, new MoodEffectConfig(Color.LIGHTBLUE, 0.8, List.of(), false, 1.0),
        PetMood.SLEEPY, new MoodEffectConfig(Color.LAVENDER, 0.4, List.of("floating-slow.svg"), false, 0.6),
        PetMood.WORRIED, new MoodEffectConfig(Color.LIGHTCORAL, 0.6, List.of(), false, 0.8),
        PetMood.CELEBRATING, new MoodEffectConfig(Color.LIME, 1.2, List.of("confetti.svg", "sparkle.svg"), true, 1.5)
    );
    
    public Environment() {
        this.currentStage = new SimpleObjectProperty<>(EvolutionStage.EGG);
        this.currentMood = new SimpleObjectProperty<>(PetMood.NEUTRAL);
        this.backgroundTheme = new SimpleStringProperty();
        this.decorativeElements = new SimpleListProperty<>(FXCollections.observableArrayList());
        
        // Initialize mood-responsive properties
        this.lightingTint = new SimpleObjectProperty<>(Color.LIGHTBLUE);
        this.lightingIntensity = new SimpleDoubleProperty(0.8);
        this.particleEffects = new SimpleListProperty<>(FXCollections.observableArrayList());
        this.hasSpecialEffects = new SimpleBooleanProperty(false);
        this.animationSpeed = new SimpleDoubleProperty(1.0);
        
        // Initialize with EGG stage environment and NEUTRAL mood
        updateEnvironmentForStage(EvolutionStage.EGG);
        updateMoodEffects(PetMood.NEUTRAL);
        
        // Listen for stage changes and update environment accordingly
        this.currentStage.addListener((obs, oldStage, newStage) -> {
            if (newStage != null) {
                updateEnvironmentForStage(newStage);
            }
        });
        
        // Listen for mood changes and update effects accordingly
        this.currentMood.addListener((obs, oldMood, newMood) -> {
            if (newMood != null) {
                updateMoodEffects(newMood);
            }
        });
    }
    
    /**
     * Updates the environment to match the given evolution stage
     */
    public void updateEnvironmentForStage(EvolutionStage stage) {
        EnvironmentConfig config = THEME_CONFIGS.get(stage);
        if (config != null) {
            this.backgroundTheme.set(config.getBackgroundTheme());
            this.decorativeElements.setAll(config.getDecorativeElements());
        }
    }
    
    /**
     * Updates mood-responsive effects based on the current mood
     */
    public void updateMoodEffects(PetMood mood) {
        MoodEffectConfig effectConfig = MOOD_EFFECTS.get(mood);
        if (effectConfig != null) {
            this.lightingTint.set(effectConfig.getLightingTint());
            this.lightingIntensity.set(effectConfig.getLightingIntensity());
            this.particleEffects.setAll(effectConfig.getParticleEffects());
            this.hasSpecialEffects.set(effectConfig.hasSpecialEffects());
            this.animationSpeed.set(effectConfig.getAnimationSpeed());
        }
    }
    
    /**
     * Sets the current evolution stage, triggering environment update
     */
    public void setCurrentStage(EvolutionStage stage) {
        this.currentStage.set(stage);
    }
    
    /**
     * Sets the current mood, triggering mood effects update
     */
    public void setCurrentMood(PetMood mood) {
        this.currentMood.set(mood);
    }
    
    /**
     * Updates both stage and mood simultaneously for smooth transitions
     */
    public void updateEnvironment(EvolutionStage stage, PetMood mood) {
        if (stage != null) {
            setCurrentStage(stage);
        }
        if (mood != null) {
            setCurrentMood(mood);
        }
    }
    
    // Getters for evolution stage properties
    public EvolutionStage getCurrentStage() {
        return currentStage.get();
    }
    
    public ObjectProperty<EvolutionStage> currentStageProperty() {
        return currentStage;
    }
    
    public String getBackgroundTheme() {
        return backgroundTheme.get();
    }
    
    public StringProperty backgroundThemeProperty() {
        return backgroundTheme;
    }
    
    public ObservableList<String> getDecorativeElements() {
        return decorativeElements.get();
    }
    
    public ListProperty<String> decorativeElementsProperty() {
        return decorativeElements;
    }
    
    // Getters for mood-responsive properties
    public PetMood getCurrentMood() {
        return currentMood.get();
    }
    
    public ObjectProperty<PetMood> currentMoodProperty() {
        return currentMood;
    }
    
    public Color getLightingTint() {
        return lightingTint.get();
    }
    
    public ObjectProperty<Color> lightingTintProperty() {
        return lightingTint;
    }
    
    public double getLightingIntensity() {
        return lightingIntensity.get();
    }
    
    public DoubleProperty lightingIntensityProperty() {
        return lightingIntensity;
    }
    
    public ObservableList<String> getParticleEffects() {
        return particleEffects.get();
    }
    
    public ListProperty<String> particleEffectsProperty() {
        return particleEffects;
    }
    
    public boolean hasSpecialEffects() {
        return hasSpecialEffects.get();
    }
    
    public BooleanProperty hasSpecialEffectsProperty() {
        return hasSpecialEffects;
    }
    
    public double getAnimationSpeed() {
        return animationSpeed.get();
    }
    
    public DoubleProperty animationSpeedProperty() {
        return animationSpeed;
    }
    
    /**
     * Checks if the environment has the expected theme for the current stage
     */
    public boolean hasCorrectThemeForStage() {
        EnvironmentConfig expectedConfig = THEME_CONFIGS.get(getCurrentStage());
        if (expectedConfig == null) {
            return false;
        }
        
        boolean backgroundMatches = expectedConfig.getBackgroundTheme().equals(getBackgroundTheme());
        boolean elementsMatch = expectedConfig.getDecorativeElements().equals(getDecorativeElements());
        
        return backgroundMatches && elementsMatch;
    }
    
    /**
     * Checks if the environment has the expected mood effects for the current mood
     */
    public boolean hasCorrectMoodEffects() {
        MoodEffectConfig expectedConfig = MOOD_EFFECTS.get(getCurrentMood());
        if (expectedConfig == null) {
            return false;
        }
        
        boolean tintMatches = expectedConfig.getLightingTint().equals(getLightingTint());
        boolean intensityMatches = Math.abs(expectedConfig.getLightingIntensity() - getLightingIntensity()) < 0.01;
        boolean effectsMatch = expectedConfig.getParticleEffects().equals(getParticleEffects());
        boolean specialEffectsMatch = expectedConfig.hasSpecialEffects() == hasSpecialEffects();
        boolean speedMatches = Math.abs(expectedConfig.getAnimationSpeed() - getAnimationSpeed()) < 0.01;
        
        return tintMatches && intensityMatches && effectsMatch && specialEffectsMatch && speedMatches;
    }
    
    /**
     * Gets the expected environment configuration for a given stage
     */
    public static EnvironmentConfig getConfigForStage(EvolutionStage stage) {
        return THEME_CONFIGS.get(stage);
    }
    
    /**
     * Gets the expected mood effect configuration for a given mood
     */
    public static MoodEffectConfig getMoodEffectConfig(PetMood mood) {
        return MOOD_EFFECTS.get(mood);
    }
    
    /**
     * Configuration class for environment themes
     */
    public static class EnvironmentConfig {
        private final String backgroundTheme;
        private final List<String> decorativeElements;
        
        public EnvironmentConfig(String backgroundTheme, List<String> decorativeElements) {
            this.backgroundTheme = backgroundTheme;
            this.decorativeElements = List.copyOf(decorativeElements); // Immutable copy
        }
        
        public String getBackgroundTheme() {
            return backgroundTheme;
        }
        
        public List<String> getDecorativeElements() {
            return decorativeElements;
        }
        
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            EnvironmentConfig that = (EnvironmentConfig) obj;
            return backgroundTheme.equals(that.backgroundTheme) && 
                   decorativeElements.equals(that.decorativeElements);
        }
        
        @Override
        public int hashCode() {
            return backgroundTheme.hashCode() + decorativeElements.hashCode();
        }
    }
    
    /**
     * Configuration class for mood-responsive effects
     */
    public static class MoodEffectConfig {
        private final Color lightingTint;
        private final double lightingIntensity;
        private final List<String> particleEffects;
        private final boolean hasSpecialEffects;
        private final double animationSpeed;
        
        public MoodEffectConfig(Color lightingTint, double lightingIntensity, 
                               List<String> particleEffects, boolean hasSpecialEffects, 
                               double animationSpeed) {
            this.lightingTint = lightingTint;
            this.lightingIntensity = lightingIntensity;
            this.particleEffects = List.copyOf(particleEffects); // Immutable copy
            this.hasSpecialEffects = hasSpecialEffects;
            this.animationSpeed = animationSpeed;
        }
        
        public Color getLightingTint() {
            return lightingTint;
        }
        
        public double getLightingIntensity() {
            return lightingIntensity;
        }
        
        public List<String> getParticleEffects() {
            return particleEffects;
        }
        
        public boolean hasSpecialEffects() {
            return hasSpecialEffects;
        }
        
        public double getAnimationSpeed() {
            return animationSpeed;
        }
        
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            MoodEffectConfig that = (MoodEffectConfig) obj;
            return Double.compare(that.lightingIntensity, lightingIntensity) == 0 &&
                   hasSpecialEffects == that.hasSpecialEffects &&
                   Double.compare(that.animationSpeed, animationSpeed) == 0 &&
                   lightingTint.equals(that.lightingTint) &&
                   particleEffects.equals(that.particleEffects);
        }
        
        @Override
        public int hashCode() {
            return lightingTint.hashCode() + 
                   Double.hashCode(lightingIntensity) + 
                   particleEffects.hashCode() + 
                   Boolean.hashCode(hasSpecialEffects) + 
                   Double.hashCode(animationSpeed);
        }
    }
}