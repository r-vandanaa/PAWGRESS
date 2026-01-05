package com.digitalpet.integration;

import com.digitalpet.model.*;
import com.digitalpet.service.*;
import com.digitalpet.viewmodel.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Integration tests for the complete Digital Pet Evolution system.
 * Tests complete user journeys from pet creation to legendary evolution
 * and verifies cross-component interactions work correctly.
 * 
 * Requirements addressed:
 * - All requirements (integration): Test complete user journeys
 * - Cross-component interactions verification
 */
public class DigitalPetIntegrationTest {
    
    private DigitalPetIntegrationService integrationService;
    private DigitalPet pet;
    private MainPetViewModel mainPetViewModel;
    private HabitInputViewModel habitInputViewModel;
    private AchievementViewModel achievementViewModel;
    private StatisticsViewModel statisticsViewModel;
    
    @BeforeEach
    void setUp() {
        // Skip JavaFX initialization for headless testing
        // Create integration service that works without JavaFX
        integrationService = new DigitalPetIntegrationService();
        
        // Get components
        pet = integrationService.getDigitalPet();
        mainPetViewModel = integrationService.getMainPetViewModel();
        habitInputViewModel = integrationService.getHabitInputViewModel();
        achievementViewModel = integrationService.getAchievementViewModel();
        statisticsViewModel = integrationService.getStatisticsViewModel();
        
        // Reset system to clean state
        integrationService.resetSystem();
    }
    
    @Test
    @DisplayName("Complete User Journey: Pet Creation to Legendary Evolution")
    void testCompleteUserJourney() {
        // Verify initial state
        assertEquals(EvolutionStage.EGG, pet.getCurrentStage());
        assertEquals(PetMood.NEUTRAL, pet.getCurrentMood());
        assertEquals(0, pet.getExperiencePoints());
        assertTrue(pet.getEnergyLevel() >= 10 && pet.getEnergyLevel() <= 100);
        
        // Journey Phase 1: Egg to Baby (100 XP needed)
        simulateHabitSubmissionPeriod(5, 0.8); // 5 days of 80% completion
        
        // Verify evolution to Baby stage
        assertTrue(pet.getExperiencePoints() >= 100, 
            "Should have enough XP for Baby stage: " + pet.getExperiencePoints());
        assertEquals(EvolutionStage.BABY, pet.getCurrentStage());
        
        // Journey Phase 2: Baby to Teen (300 XP total needed)
        simulateHabitSubmissionPeriod(10, 0.75); // 10 more days of 75% completion
        
        // Verify evolution to Teen stage
        assertTrue(pet.getExperiencePoints() >= 300,
            "Should have enough XP for Teen stage: " + pet.getExperiencePoints());
        assertEquals(EvolutionStage.TEEN, pet.getCurrentStage());
        
        // Journey Phase 3: Teen to Adult (600 XP total needed)
        simulateHabitSubmissionPeriod(15, 0.85); // 15 more days of 85% completion
        
        // Verify evolution to Adult stage
        assertTrue(pet.getExperiencePoints() >= 600,
            "Should have enough XP for Adult stage: " + pet.getExperiencePoints());
        assertEquals(EvolutionStage.ADULT, pet.getCurrentStage());
        
        // Journey Phase 4: Adult to Legendary (1000 XP total needed)
        simulateHabitSubmissionPeriod(20, 0.9); // 20 more days of 90% completion
        
        // Verify evolution to Legendary stage
        assertTrue(pet.getExperiencePoints() >= 1000,
            "Should have enough XP for Legendary stage: " + pet.getExperiencePoints());
        assertEquals(EvolutionStage.LEGENDARY, pet.getCurrentStage());
        
        // Verify achievements were unlocked during journey
        List<Achievement> unlockedAchievements = achievementViewModel.getUnlockedAchievements();
        assertFalse(unlockedAchievements.isEmpty(), "Should have unlocked some achievements");
        
        // Verify statistics reflect the journey
        assertTrue(integrationService.getHabitHistory().size() >= 50, 
            "Should have substantial habit history");
        
        // Verify mood system responded appropriately
        // With high completion rates, pet should be happy or celebrating
        PetMood finalMood = pet.getCurrentMood();
        assertTrue(finalMood == PetMood.HAPPY || finalMood == PetMood.CELEBRATING,
            "Pet should be happy after successful journey, but was: " + finalMood);
    }
    
    @Test
    @DisplayName("Cross-Component Interaction: Habit Submission Triggers All Systems")
    void testHabitSubmissionIntegration() {
        // Record initial state
        int initialXP = pet.getExperiencePoints();
        EvolutionStage initialStage = pet.getCurrentStage();
        PetMood initialMood = pet.getCurrentMood();
        int initialAchievements = achievementViewModel.getUnlockedAchievements().size();
        
        // Submit excellent habits
        DailyHabits excellentHabits = createExcellentHabits();
        
        // Simulate habit submission through ViewModel
        habitInputViewModel.setStudyHours(excellentHabits.getStudyHours());
        habitInputViewModel.setWaterIntake(excellentHabits.getWaterIntake());
        habitInputViewModel.setStepsTaken(excellentHabits.getStepsTaken());
        habitInputViewModel.setSleepHours(excellentHabits.getSleepHours());
        habitInputViewModel.setMoneySpent(excellentHabits.getMoneySpent());
        habitInputViewModel.setGoalsCompleted(excellentHabits.getGoalsCompleted());
        
        // Submit habits
        habitInputViewModel.submitHabitsSync();
        
        // Wait for async processing
        waitForAsyncProcessing();
        
        // Verify XP system was triggered
        assertTrue(pet.getExperiencePoints() > initialXP, 
            "XP should have increased from " + initialXP + " to " + pet.getExperiencePoints());
        
        // Verify mood system was triggered
        // With excellent habits, mood should improve or stay positive
        PetMood newMood = pet.getCurrentMood();
        assertTrue(newMood == PetMood.HAPPY || newMood == PetMood.CELEBRATING || newMood == PetMood.NEUTRAL,
            "Mood should be positive after excellent habits, but was: " + newMood);
        
        // Verify habit history was updated
        assertFalse(integrationService.getHabitHistory().isEmpty(),
            "Habit history should contain the submitted habits");
        
        // Verify statistics were updated
        statisticsViewModel.refreshData(pet, integrationService.getHabitHistory());
        assertNotNull(statisticsViewModel.getXpSummaryText());
        assertNotNull(statisticsViewModel.getMoodSummaryText());
        
        // Verify achievement system was checked
        // (May or may not unlock achievements, but should have been checked)
        assertTrue(integrationService.getAchievementSystem().getTotalAchievements() > 0,
            "Achievement system should be initialized with achievements");
    }
    
    @Test
    @DisplayName("Pet Interaction Integration")
    void testPetInteractionIntegration() {
        // Record initial state
        int initialEnergy = pet.getEnergyLevel();
        LocalDateTime initialInteraction = pet.getLastInteraction();
        
        // Interact with pet through ViewModel
        mainPetViewModel.handlePetInteraction();
        
        // Verify interaction was recorded
        assertTrue(pet.getLastInteraction().isAfter(initialInteraction),
            "Last interaction time should be updated");
        
        // Verify energy was affected
        assertTrue(pet.getEnergyLevel() >= initialEnergy,
            "Energy should increase or stay same after interaction");
        
        // Verify integration service tracked the activity
        assertTrue(integrationService.hasRecentActivity(),
            "Integration service should track recent activity");
        
        assertNotNull(integrationService.getLastActionMessage(),
            "Should have an action message");
    }
    
    @Test
    @DisplayName("Achievement System Integration")
    void testAchievementSystemIntegration() {
        // Submit habits to trigger achievement checks
        for (int i = 0; i < 7; i++) {
            DailyHabits habits = createGoodWaterHabits(); // Focus on water achievement
            simulateHabitSubmission(habits);
        }
        
        // Check if water warrior achievement was unlocked
        Achievement waterWarrior = achievementViewModel.getAchievement("water_warrior");
        assertNotNull(waterWarrior, "Water warrior achievement should exist");
        
        // If unlocked, verify celebration was triggered
        if (waterWarrior.isUnlocked()) {
            assertTrue(integrationService.hasRecentActivity(),
                "Should have recent activity from achievement unlock");
        }
        
        // Verify achievement progress is tracked
        assertTrue(waterWarrior.getCurrentValue() > 0,
            "Water warrior achievement should have progress");
    }
    
    @Test
    @DisplayName("Mood System Integration with Environment")
    void testMoodSystemIntegration() {
        // Submit poor habits to trigger worried mood
        for (int i = 0; i < 3; i++) {
            DailyHabits poorHabits = createPoorHabits();
            simulateHabitSubmission(poorHabits);
        }
        
        // Verify mood system responded
        PetMood currentMood = pet.getCurrentMood();
        assertTrue(currentMood == PetMood.WORRIED || currentMood == PetMood.SLEEPY,
            "Pet should be worried or sleepy after poor habits, but was: " + currentMood);
        
        // Verify energy was affected by mood
        assertTrue(pet.getEnergyLevel() <= 80,
            "Energy should be lower due to poor mood");
        
        // Now submit excellent habits to improve mood
        for (int i = 0; i < 3; i++) {
            DailyHabits excellentHabits = createExcellentHabits();
            simulateHabitSubmission(excellentHabits);
        }
        
        // Verify mood improved
        PetMood improvedMood = pet.getCurrentMood();
        assertTrue(improvedMood == PetMood.HAPPY || improvedMood == PetMood.CELEBRATING,
            "Pet should be happy after excellent habits, but was: " + improvedMood);
    }
    
    @Test
    @DisplayName("Data Persistence Integration")
    void testDataPersistenceIntegration() {
        // Submit some habits and interact with pet
        DailyHabits habits = createExcellentHabits();
        simulateHabitSubmission(habits);
        mainPetViewModel.handlePetInteraction();
        
        // Trigger system update (which includes persistence)
        integrationService.updateAllSystems();
        
        // Verify no errors occurred during persistence
        assertTrue(integrationService.isSystemInitialized(),
            "System should remain initialized after persistence");
        
        // Verify data is still accessible
        assertFalse(integrationService.getHabitHistory().isEmpty(),
            "Habit history should be preserved");
        
        assertEquals(habits.getDate(), integrationService.getHabitHistory().get(0).getDate(),
            "Most recent habit should match submitted habit");
    }
    
    @Test
    @DisplayName("Error Recovery Integration")
    void testErrorRecoveryIntegration() {
        // Submit valid habits first
        DailyHabits validHabits = createExcellentHabits();
        simulateHabitSubmission(validHabits);
        
        // Verify system is working
        assertTrue(integrationService.isSystemInitialized(),
            "System should be initialized");
        
        // Try to trigger error recovery by resetting system
        integrationService.resetSystem();
        
        // Verify system recovered
        assertTrue(integrationService.isSystemInitialized(),
            "System should recover after reset");
        
        // Verify basic functionality still works
        mainPetViewModel.handlePetInteraction();
        assertTrue(integrationService.hasRecentActivity(),
            "System should still respond to interactions after recovery");
    }
    
    @Test
    @DisplayName("Statistics Integration with All Data Sources")
    void testStatisticsIntegration() {
        // Generate varied habit data
        simulateHabitSubmissionPeriod(10, 0.7); // 10 days of 70% completion
        
        // Refresh statistics
        statisticsViewModel.refreshData(pet, integrationService.getHabitHistory());
        
        // Verify statistics reflect the data
        assertNotNull(statisticsViewModel.getXpSummaryText(),
            "XP summary should be available");
        
        assertNotNull(statisticsViewModel.getMoodSummaryText(),
            "Mood summary should be available");
        
        // Verify statistics can handle different time ranges
        statisticsViewModel.setSelectedTimeRange(7);
        assertNotNull(statisticsViewModel.getXpSummaryText(),
            "Should handle 7-day range");
        
        statisticsViewModel.setSelectedTimeRange(30);
        assertNotNull(statisticsViewModel.getXpSummaryText(),
            "Should handle 30-day range");
    }
    
    // Helper methods
    
    private void simulateHabitSubmissionPeriod(int days, double averageCompletion) {
        LocalDate startDate = LocalDate.now().minusDays(days - 1);
        
        for (int i = 0; i < days; i++) {
            LocalDate date = startDate.plusDays(i);
            DailyHabits habits = createHabitsWithCompletion(date, averageCompletion);
            simulateHabitSubmission(habits);
        }
    }
    
    private void simulateHabitSubmission(DailyHabits habits) {
        // Set values in ViewModel
        habitInputViewModel.setStudyHours(habits.getStudyHours());
        habitInputViewModel.setWaterIntake(habits.getWaterIntake());
        habitInputViewModel.setStepsTaken(habits.getStepsTaken());
        habitInputViewModel.setSleepHours(habits.getSleepHours());
        habitInputViewModel.setMoneySpent(habits.getMoneySpent());
        habitInputViewModel.setGoalsCompleted(habits.getGoalsCompleted());
        
        // Submit directly through integration service to avoid JavaFX async issues
        integrationService.getHabitInputViewModel().setCustomSubmissionHandler(
            integrationService::handleHabitSubmission);
        
        // Create habits object and submit directly
        DailyHabits submissionHabits = new DailyHabits(habits.getDate());
        submissionHabits.setStudyHours(habits.getStudyHours());
        submissionHabits.setWaterIntake(habits.getWaterIntake());
        submissionHabits.setStepsTaken(habits.getStepsTaken());
        submissionHabits.setSleepHours(habits.getSleepHours());
        submissionHabits.setMoneySpent(habits.getMoneySpent());
        submissionHabits.setGoalsCompleted(habits.getGoalsCompleted());
        
        // Call the integration service handler directly
        integrationService.handleHabitSubmission(submissionHabits);
        
        // Wait for processing
        waitForAsyncProcessing();
    }
    
    private DailyHabits createExcellentHabits() {
        return createHabitsWithCompletion(LocalDate.now(), 0.9);
    }
    
    private DailyHabits createGoodWaterHabits() {
        DailyHabits habits = new DailyHabits(LocalDate.now());
        habits.setStudyHours(6);
        habits.setWaterIntake(2.5); // Excellent water intake
        habits.setStepsTaken(8000);
        habits.setSleepHours(7.5);
        habits.setMoneySpent(30);
        habits.setGoalsCompleted(3);
        return habits;
    }
    
    private DailyHabits createPoorHabits() {
        return createHabitsWithCompletion(LocalDate.now(), 0.2);
    }
    
    private DailyHabits createHabitsWithCompletion(LocalDate date, double completionRate) {
        DailyHabits habits = new DailyHabits(date);
        
        // Scale values based on completion rate
        habits.setStudyHours((int) (8 * completionRate));
        habits.setWaterIntake(2.5 * completionRate);
        habits.setStepsTaken((int) (10000 * completionRate));
        habits.setSleepHours(6 + (2 * completionRate)); // 6-8 hours range
        habits.setMoneySpent(50 - (20 * completionRate)); // Less spending is better
        habits.setGoalsCompleted((int) (5 * completionRate));
        
        return habits;
    }
    
    private void waitForAsyncProcessing() {
        // In a real implementation, this would wait for actual async operations
        // For now, we'll just ensure any immediate processing is complete
        try {
            Thread.sleep(100); // Small delay to allow processing
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    @AfterEach
    void tearDown() {
        // Clean up after each test
        if (integrationService != null) {
            integrationService.clearRecentActivity();
        }
    }
}