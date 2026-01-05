package com.digitalpet.service;

import com.digitalpet.model.*;
import com.digitalpet.viewmodel.*;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Central integration service that wires all components together.
 * Manages the complete digital pet ecosystem including pet reactions, 
 * habit submissions, achievement unlocks, and cross-system interactions.
 * 
 * Requirements addressed:
 * - All requirements (integration): Connects ViewModels to services
 * - Pet reactions with habit submissions
 * - Achievement system linked with XP and evolution systems
 * - Property bindings work correctly across all components
 */
public class DigitalPetIntegrationService {
    
    // Core model and services
    private final DigitalPet digitalPet;
    private final XPSystem xpSystem;
    private final MoodSystem moodSystem;
    private final AchievementSystem achievementSystem;
    private final AnimationEngine animationEngine;
    private final PetInteractionSystem petInteractionSystem;
    
    // ViewModels
    private final MainPetViewModel mainPetViewModel;
    private final HabitInputViewModel habitInputViewModel;
    private final AchievementViewModel achievementViewModel;
    private final StatisticsViewModel statisticsViewModel;
    
    // Data storage
    private final ObservableList<DailyHabits> habitHistory;
    private final LocalDataStorage dataStorage;
    
    // Integration state properties
    private final BooleanProperty systemInitialized;
    private final BooleanProperty hasRecentActivity;
    private final StringProperty lastActionMessage;
    
    /**
     * Creates a new integration service with all components
     */
    public DigitalPetIntegrationService() {
        // Initialize core model
        this.digitalPet = new DigitalPet("My Digital Pet");
        
        // Initialize services
        this.xpSystem = new XPSystem();
        this.moodSystem = new MoodSystem();
        this.achievementSystem = new AchievementSystem();
        this.animationEngine = new AnimationEngine();
        this.petInteractionSystem = new PetInteractionSystem(animationEngine, digitalPet);
        
        // Initialize data storage
        this.habitHistory = FXCollections.observableArrayList();
        this.dataStorage = new LocalDataStorage();
        
        // Initialize ViewModels with proper dependencies
        this.mainPetViewModel = new MainPetViewModel(digitalPet);
        this.habitInputViewModel = new HabitInputViewModel(digitalPet);
        this.achievementViewModel = new AchievementViewModel();
        this.statisticsViewModel = new StatisticsViewModel();
        
        // Initialize integration state
        this.systemInitialized = new SimpleBooleanProperty(false);
        this.hasRecentActivity = new SimpleBooleanProperty(false);
        this.lastActionMessage = new SimpleStringProperty("");
        
        // Wire all components together
        wireComponents();
        
        // Load initial data
        loadInitialData();
        
        // Mark system as initialized
        systemInitialized.set(true);
    }
    
    /**
     * Wires all components together with proper event handling and data flow
     */
    private void wireComponents() {
        // 1. Connect habit input to XP and mood systems
        wireHabitInputSystem();
        
        // 2. Connect XP system to evolution and achievements
        wireXPAndEvolutionSystem();
        
        // 3. Connect mood system to pet reactions
        wireMoodAndReactionSystem();
        
        // 4. Connect achievement system to celebrations
        wireAchievementSystem();
        
        // 5. Connect statistics to all data sources
        wireStatisticsSystem();
        
        // 6. Set up cross-component property bindings
        setupPropertyBindings();
        
        // 7. Set up data persistence
        setupDataPersistence();
    }
    
    /**
     * Wires habit input system to XP calculation and mood updates
     */
    private void wireHabitInputSystem() {
        // Override the habit input submission to integrate with all systems
        HabitInputViewModel habitVM = habitInputViewModel;
        
        // Create a custom submission handler that integrates all systems
        habitVM.setCustomSubmissionHandler(this::handleHabitSubmission);
        
        // Set up habit history for consistency calculations
        habitVM.setHabitHistory(new ArrayList<>(habitHistory));
    }
    
    /**
     * Handles habit submission with full system integration
     */
    public void handleHabitSubmission(DailyHabits newHabits) {
        if (newHabits == null) return;
        
        // 1. Add to habit history
        habitHistory.add(0, newHabits); // Most recent first
        
        // 2. Calculate consistency score
        double consistencyScore = xpSystem.calculateConsistencyScore(
            new ArrayList<>(habitHistory), 7);
        
        // 3. Calculate and award XP
        int xpGained = xpSystem.awardXP(digitalPet, newHabits, consistencyScore);
        
        // 4. Update mood based on recent habits
        boolean hasRecentAchievement = achievementSystem.hasRecentUnlocks();
        moodSystem.updateMood(new ArrayList<>(habitHistory), hasRecentAchievement);
        
        // 5. Update pet mood from mood system
        digitalPet.setCurrentMood(moodSystem.getCurrentMood());
        
        // 6. Update energy based on mood and habits
        updateEnergyFromHabits(newHabits);
        
        // 7. Check for achievement unlocks
        UserProgress userProgress = new UserProgress(digitalPet, new ArrayList<>(habitHistory));
        List<Achievement> newUnlocks = achievementSystem.checkForUnlocks(userProgress);
        
        // 8. Trigger celebrations for achievements and evolution
        if (!newUnlocks.isEmpty()) {
            triggerAchievementCelebrations(newUnlocks);
        }
        
        // 9. Check for evolution and trigger celebration if needed
        EvolutionStage previousStage = digitalPet.getCurrentStage();
        // Evolution is handled automatically by the DigitalPet model when XP is added
        if (digitalPet.getCurrentStage() != previousStage) {
            triggerEvolutionCelebration(previousStage, digitalPet.getCurrentStage());
        }
        
        // 10. Update statistics
        statisticsViewModel.refreshData(digitalPet, new ArrayList<>(habitHistory));
        
        // 11. Persist data
        persistCurrentState();
        
        // 12. Update integration state
        hasRecentActivity.set(true);
        lastActionMessage.set(String.format("Habits submitted! +%d XP gained", xpGained));
        
        // 13. Schedule activity flag reset
        scheduleActivityReset();
    }
    
    /**
     * Wires XP system to evolution and achievement systems
     */
    private void wireXPAndEvolutionSystem() {
        // Listen for XP changes to trigger evolution checks
        digitalPet.experiencePointsProperty().addListener((obs, oldXP, newXP) -> {
            // Evolution is handled automatically by DigitalPet model
            // But we can trigger additional effects here
            
            // Update achievement system with new progress
            UserProgress userProgress = new UserProgress(digitalPet, new ArrayList<>(habitHistory));
            achievementSystem.checkForUnlocks(userProgress);
        });
        
        // Listen for evolution stage changes
        digitalPet.currentStageProperty().addListener((obs, oldStage, newStage) -> {
            if (oldStage != newStage && newStage != null) {
                // Trigger evolution celebration
                triggerEvolutionCelebration(oldStage, newStage);
                
                // Update mood system (evolution can affect mood)
                boolean hasRecentAchievement = achievementSystem.hasRecentUnlocks();
                moodSystem.updateMood(new ArrayList<>(habitHistory), hasRecentAchievement);
                digitalPet.setCurrentMood(moodSystem.getCurrentMood());
            }
        });
    }
    
    /**
     * Wires mood system to pet reactions and environment
     */
    private void wireMoodAndReactionSystem() {
        // Listen for mood changes to trigger pet reactions
        digitalPet.currentMoodProperty().addListener((obs, oldMood, newMood) -> {
            if (oldMood != newMood && newMood != null) {
                // Trigger mood-based pet reaction through animation engine
                // In a real UI implementation, the pet node would be available
                // For now, we just update energy based on mood
                updateEnergyFromMood(newMood);
                
                lastActionMessage.set("Pet mood changed to " + newMood.getDisplayText());
            }
        });
        
        // Set up pet interaction handling
        mainPetViewModel.setPetInteractionHandler(this::handlePetInteraction);
    }
    
    /**
     * Handles pet interaction with integrated response
     */
    private void handlePetInteraction() {
        // Record interaction in pet model
        digitalPet.recordInteraction();
        
        // Trigger interaction through pet interaction system
        // In a real UI implementation, the pet node would be passed
        petInteractionSystem.handlePetClick(null); // Gracefully handles null node
        
        // Small energy boost from interaction
        int currentEnergy = digitalPet.getEnergyLevel();
        digitalPet.setEnergyLevel(Math.min(100, currentEnergy + 2));
        
        // Update activity state
        hasRecentActivity.set(true);
        lastActionMessage.set("Pet interaction! Energy +2");
        scheduleActivityReset();
    }
    
    /**
     * Wires achievement system to celebration triggers
     */
    private void wireAchievementSystem() {
        // Set up achievement data source
        achievementViewModel.setAchievementSystem(achievementSystem);
        
        // Listen for achievement unlocks
        achievementSystem.hasRecentUnlockProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue) {
                // Get recent unlocks and trigger celebrations
                List<Achievement> recentUnlocks = achievementSystem.getRecentUnlocks();
                if (!recentUnlocks.isEmpty()) {
                    triggerAchievementCelebrations(recentUnlocks);
                    
                    // Update mood to celebrating
                    moodSystem.setCurrentMood(PetMood.CELEBRATING);
                    digitalPet.setCurrentMood(PetMood.CELEBRATING);
                }
            }
        });
    }
    
    /**
     * Wires statistics system to all data sources
     */
    private void wireStatisticsSystem() {
        // Set up data sources for statistics
        statisticsViewModel.setDataSources(digitalPet, habitHistory);
        
        // Listen for habit history changes to update statistics
        habitHistory.addListener((javafx.collections.ListChangeListener<DailyHabits>) change -> {
            statisticsViewModel.refreshData(digitalPet, new ArrayList<>(habitHistory));
        });
    }
    
    /**
     * Sets up property bindings between components
     */
    private void setupPropertyBindings() {
        // Bind habit input ViewModel to the same pet
        habitInputViewModel.setCurrentPet(digitalPet);
        
        // Update habit history in habit input ViewModel when it changes
        habitHistory.addListener((javafx.collections.ListChangeListener<DailyHabits>) change -> {
            habitInputViewModel.setHabitHistory(new ArrayList<>(habitHistory));
        });
        
        // Ensure all ViewModels stay synchronized with the pet model
        // (This is mostly handled by JavaFX property binding, but we can add additional sync here)
    }
    
    /**
     * Sets up data persistence for all components
     */
    private void setupDataPersistence() {
        // Auto-save when important data changes
        digitalPet.experiencePointsProperty().addListener((obs, oldVal, newVal) -> {
            scheduleDataSave();
        });
        
        habitHistory.addListener((javafx.collections.ListChangeListener<DailyHabits>) change -> {
            scheduleDataSave();
        });
        
        achievementSystem.unlockedAchievementsProperty().addListener((obs, oldVal, newVal) -> {
            scheduleDataSave();
        });
    }
    
    /**
     * Updates pet energy based on habit completion
     */
    private void updateEnergyFromHabits(DailyHabits habits) {
        if (habits == null) return;
        
        // Base energy from habit completion (0-100)
        double completionPercentage = habits.getCompletionPercentage();
        int baseEnergy = (int) (completionPercentage * 80) + 20; // 20-100 range
        
        // Apply mood multiplier
        PetMood currentMood = digitalPet.getCurrentMood();
        double moodMultiplier = currentMood.getEnergyMultiplier();
        int finalEnergy = (int) Math.round(baseEnergy * moodMultiplier);
        
        digitalPet.setEnergyLevel(Math.max(10, Math.min(100, finalEnergy)));
    }
    
    /**
     * Updates pet energy based on mood change
     */
    private void updateEnergyFromMood(PetMood mood) {
        int currentEnergy = digitalPet.getEnergyLevel();
        double moodMultiplier = mood.getEnergyMultiplier();
        int adjustedEnergy = (int) Math.round(currentEnergy * moodMultiplier);
        
        digitalPet.setEnergyLevel(Math.max(10, Math.min(100, adjustedEnergy)));
    }
    
    /**
     * Triggers achievement celebration animations and effects
     */
    private void triggerAchievementCelebrations(List<Achievement> achievements) {
        for (Achievement achievement : achievements) {
            // Trigger celebration animation through pet interaction system
            // In a real UI implementation, the pet node would be available
            petInteractionSystem.triggerAchievementCelebration(null, achievement.getId());
            
            // Log celebration
            lastActionMessage.set("🎉 Achievement unlocked: " + achievement.getTitle());
        }
        
        // Set celebrating mood temporarily
        moodSystem.setCurrentMood(PetMood.CELEBRATING);
        digitalPet.setCurrentMood(PetMood.CELEBRATING);
        
        // Schedule mood reset after celebration
        scheduleMoodReset();
    }
    
    /**
     * Triggers evolution celebration
     */
    private void triggerEvolutionCelebration(EvolutionStage oldStage, EvolutionStage newStage) {
        // Trigger evolution celebration through pet interaction system
        // In a real UI implementation, the pet node would be available
        petInteractionSystem.triggerAchievementCelebration(null, "evolution_" + newStage.name());
        
        // Set celebrating mood
        moodSystem.setCurrentMood(PetMood.CELEBRATING);
        digitalPet.setCurrentMood(PetMood.CELEBRATING);
        
        // Update activity state
        hasRecentActivity.set(true);
        lastActionMessage.set("🌟 Evolution! " + oldStage.getDisplayName() + " → " + newStage.getDisplayName());
        
        // Schedule mood reset
        scheduleMoodReset();
    }
    
    /**
     * Loads initial data from storage
     */
    private void loadInitialData() {
        try {
            // Load pet data
            DigitalPet savedPet = dataStorage.loadPet();
            if (savedPet != null) {
                digitalPet.setName(savedPet.getName());
                digitalPet.setCurrentStage(savedPet.getCurrentStage());
                digitalPet.setCurrentMood(savedPet.getCurrentMood());
                digitalPet.setExperiencePoints(savedPet.getExperiencePoints());
                digitalPet.setEnergyLevel(savedPet.getEnergyLevel());
                digitalPet.setLastInteraction(savedPet.getLastInteraction());
            }
            
            // Load habit history
            List<DailyHabits> savedHabits = dataStorage.loadHabitHistory();
            if (savedHabits != null) {
                habitHistory.setAll(savedHabits);
            }
            
            // Load achievement progress
            // (Achievement system initializes its own predefined achievements)
            
        } catch (Exception e) {
            // If loading fails, start with defaults
            System.err.println("Failed to load saved data, starting fresh: " + e.getMessage());
        }
    }
    
    /**
     * Persists current state to storage
     */
    private void persistCurrentState() {
        try {
            // Save pet data
            dataStorage.savePet(digitalPet);
            
            // Save habit history
            dataStorage.saveHabitHistory(new ArrayList<>(habitHistory));
            
            // Save achievement progress
            // (Handled by achievement system internally)
            
        } catch (Exception e) {
            System.err.println("Failed to save data: " + e.getMessage());
        }
    }
    
    /**
     * Schedules data save after a short delay to batch multiple changes
     */
    private void scheduleDataSave() {
        // In a real implementation, this would use a timer to batch saves
        // For now, save immediately
        persistCurrentState();
    }
    
    /**
     * Schedules activity flag reset
     */
    private void scheduleActivityReset() {
        // In a real implementation, this would use a timer
        // For now, we'll leave the flag set until manually cleared
    }
    
    /**
     * Schedules mood reset after celebration
     */
    private void scheduleMoodReset() {
        // In a real implementation, this would reset mood after a delay
        // For now, mood will be updated on next habit submission
    }
    
    // Public API for accessing components
    
    public DigitalPet getDigitalPet() {
        return digitalPet;
    }
    
    public MainPetViewModel getMainPetViewModel() {
        return mainPetViewModel;
    }
    
    public HabitInputViewModel getHabitInputViewModel() {
        return habitInputViewModel;
    }
    
    public AchievementViewModel getAchievementViewModel() {
        return achievementViewModel;
    }
    
    public StatisticsViewModel getStatisticsViewModel() {
        return statisticsViewModel;
    }
    
    public XPSystem getXpSystem() {
        return xpSystem;
    }
    
    public MoodSystem getMoodSystem() {
        return moodSystem;
    }
    
    public AchievementSystem getAchievementSystem() {
        return achievementSystem;
    }
    
    public AnimationEngine getAnimationEngine() {
        return animationEngine;
    }
    
    public ObservableList<DailyHabits> getHabitHistory() {
        return habitHistory;
    }
    
    // Integration state properties
    
    public BooleanProperty systemInitializedProperty() {
        return systemInitialized;
    }
    
    public BooleanProperty hasRecentActivityProperty() {
        return hasRecentActivity;
    }
    
    public StringProperty lastActionMessageProperty() {
        return lastActionMessage;
    }
    
    public boolean isSystemInitialized() {
        return systemInitialized.get();
    }
    
    public boolean hasRecentActivity() {
        return hasRecentActivity.get();
    }
    
    public String getLastActionMessage() {
        return lastActionMessage.get();
    }
    
    /**
     * Clears recent activity flag
     */
    public void clearRecentActivity() {
        hasRecentActivity.set(false);
        lastActionMessage.set("");
    }
    
    /**
     * Manually triggers a system update (useful for testing)
     */
    public void updateAllSystems() {
        // Update mood
        boolean hasRecentAchievement = achievementSystem.hasRecentUnlocks();
        moodSystem.updateMood(new ArrayList<>(habitHistory), hasRecentAchievement);
        digitalPet.setCurrentMood(moodSystem.getCurrentMood());
        
        // Check achievements
        UserProgress userProgress = new UserProgress(digitalPet, new ArrayList<>(habitHistory));
        achievementSystem.checkForUnlocks(userProgress);
        
        // Update statistics
        statisticsViewModel.refreshData(digitalPet, new ArrayList<>(habitHistory));
        
        // Persist state
        persistCurrentState();
    }
    
    /**
     * Resets the entire system to initial state (for testing)
     */
    public void resetSystem() {
        // Reset pet
        digitalPet.setName("My Digital Pet");
        digitalPet.setCurrentStage(EvolutionStage.EGG);
        digitalPet.setCurrentMood(PetMood.NEUTRAL);
        digitalPet.setExperiencePoints(0);
        digitalPet.setEnergyLevel(50);
        digitalPet.setLastInteraction(LocalDateTime.now());
        
        // Clear habit history
        habitHistory.clear();
        
        // Reset achievements
        achievementSystem.resetAllAchievements();
        
        // Reset mood system
        moodSystem.reset();
        
        // Clear integration state
        hasRecentActivity.set(false);
        lastActionMessage.set("");
        
        // Persist reset state
        persistCurrentState();
    }
    
    @Override
    public String toString() {
        return String.format("DigitalPetIntegrationService{pet=%s, habits=%d, initialized=%s}", 
                digitalPet.getName(), habitHistory.size(), systemInitialized.get());
    }
}