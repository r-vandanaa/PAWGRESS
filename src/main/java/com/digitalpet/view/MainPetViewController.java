package com.digitalpet.view;

import com.digitalpet.model.DigitalPet;
import com.digitalpet.model.EvolutionStage;
import com.digitalpet.model.PetMood;
import com.digitalpet.service.AccessibilityService;
import com.digitalpet.service.NavigationManager;
import com.digitalpet.viewmodel.MainPetViewModel;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller for the main pet view screen.
 * Handles UI interactions and binds to MainPetViewModel for data.
 * 
 * Requirements addressed:
 * - 6.2: Main pet screen with pet display, name, stage, mood, progress bars
 * - 6.3: Bottom navigation bar with access to all sections
 * - 1.5: Pet interaction responses to clicks/taps
 */
public class MainPetViewController implements Initializable {
    
    // Pet display elements
    @FXML private Label petNameLabel;
    @FXML private Label evolutionStageLabel;
    @FXML private StackPane petDisplayArea;
    @FXML private Label petPlaceholder;
    @FXML private Label moodLabel;
    
    // Progress elements
    @FXML private ProgressBar xpProgressBar;
    @FXML private Label xpLabel;
    @FXML private ProgressBar energyProgressBar;
    @FXML private Label energyLabel;
    
    // Navigation elements
    @FXML private Button petTabButton;
    @FXML private Button habitsTabButton;
    @FXML private Button statsTabButton;
    @FXML private Button achievementsTabButton;
    
    private MainPetViewModel viewModel;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize with default pet for now
        // In a full application, this would be injected or loaded from storage
        DigitalPet defaultPet = new DigitalPet("My Pet");
        this.viewModel = new MainPetViewModel(defaultPet);
        
        setupPropertyBindings();
        setupPetInteraction();
        setupAccessibility();
        updateNavigationState();
    }
    
    /**
     * Sets up property bindings between UI elements and ViewModel
     */
    private void setupPropertyBindings() {
        // Bind pet information
        petNameLabel.textProperty().bind(viewModel.petNameProperty());
        evolutionStageLabel.textProperty().bind(viewModel.evolutionStageDisplayProperty());
        moodLabel.textProperty().bind(viewModel.moodDisplayProperty());
        
        // Bind progress bars
        xpProgressBar.progressProperty().bind(viewModel.xpProgressProperty());
        xpLabel.textProperty().bind(viewModel.xpDisplayTextProperty());
        
        energyProgressBar.progressProperty().bind(viewModel.energyProgressProperty());
        energyLabel.textProperty().bind(viewModel.energyDisplayTextProperty());
        
        // Update pet visual based on evolution stage
        viewModel.evolutionStageProperty().addListener((observable, oldStage, newStage) -> {
            updatePetVisual(newStage);
        });
        
        // Initial pet visual update
        updatePetVisual(viewModel.getEvolutionStage());
    }
    
    /**
     * Sets up accessibility features for all UI elements
     * Requirements: 6.5, 10.2, 10.5, 9.5
     */
    private void setupAccessibility() {
        // Enhance pet display area accessibility
        petDisplayArea.setAccessibleText("Digital pet display area. Click or press Enter to interact with your pet.");
        petDisplayArea.setAccessibleRole(javafx.scene.AccessibleRole.BUTTON);
        petDisplayArea.setAccessibleHelp("Interactive pet that responds to your daily habits and evolves over time.");
        
        // Enhance labels with accessible text
        AccessibilityService.enhanceLabelAccessibility(petNameLabel, "Pet name");
        AccessibilityService.enhanceLabelAccessibility(evolutionStageLabel, "Current evolution stage");
        AccessibilityService.enhanceLabelAccessibility(moodLabel, "Pet's current mood");
        
        // Enhance progress bars with accessible descriptions
        AccessibilityService.enhanceProgressBarAccessibility(xpProgressBar, "Experience points progress");
        AccessibilityService.enhanceProgressBarAccessibility(energyProgressBar, "Pet energy level");
        
        // Enhance navigation buttons
        AccessibilityService.enhanceButtonAccessibility(petTabButton, "Pet view - currently active");
        AccessibilityService.enhanceButtonAccessibility(habitsTabButton, "Navigate to daily habits input");
        AccessibilityService.enhanceButtonAccessibility(statsTabButton, "Navigate to statistics and progress");
        AccessibilityService.enhanceButtonAccessibility(achievementsTabButton, "Navigate to achievements");
        
        // Set up dynamic accessibility updates
        setupDynamicAccessibility();
    }
    
    /**
     * Sets up dynamic accessibility updates that change based on state
     */
    private void setupDynamicAccessibility() {
        // Update pet interaction accessibility based on evolution stage
        viewModel.evolutionStageProperty().addListener((observable, oldStage, newStage) -> {
            String stageDescription = switch (newStage) {
                case EGG -> "Your pet is in the egg stage, waiting to hatch";
                case BABY -> "Your pet is a baby, just starting its journey";
                case TEEN -> "Your pet is a teenager, growing and learning";
                case ADULT -> "Your pet is an adult, mature and accomplished";
                case LEGENDARY -> "Your pet has reached legendary status!";
            };
            petDisplayArea.setAccessibleHelp(stageDescription + ". Click to interact.");
        });
        
        // Update mood accessibility
        viewModel.moodDisplayProperty().addListener((observable, oldMood, newMood) -> {
            moodLabel.setAccessibleText("Pet mood: " + newMood);
        });
        
        // Update XP progress accessibility
        viewModel.xpProgressProperty().addListener((observable, oldProgress, newProgress) -> {
            int percentage = (int) (newProgress.doubleValue() * 100);
            xpProgressBar.setAccessibleHelp(String.format("Experience progress: %d%% to next evolution", percentage));
        });
        
        // Update energy progress accessibility
        viewModel.energyProgressProperty().addListener((observable, oldProgress, newProgress) -> {
            int percentage = (int) (newProgress.doubleValue() * 100);
            energyProgressBar.setAccessibleHelp(String.format("Pet energy level: %d%%", percentage));
        });
    }
    
    /**
     * Sets up pet interaction handling for clicks/taps
     */
    private void setupPetInteraction() {
        petDisplayArea.setOnMouseClicked(this::handlePetInteraction);
        petPlaceholder.setOnMouseClicked(this::handlePetInteraction);
        
        // Make pet area focusable for accessibility
        petDisplayArea.setFocusTraversable(true);
        
        // Handle keyboard interaction for accessibility
        petDisplayArea.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case ENTER:
                case SPACE:
                    handlePetInteraction(null);
                    event.consume();
                    break;
            }
        });
    }
    
    /**
     * Handles pet interaction (click/tap/keyboard)
     */
    private void handlePetInteraction(MouseEvent event) {
        if (viewModel != null) {
            viewModel.handlePetInteraction();
        }
    }
    
    /**
     * Updates pet visual representation based on evolution stage
     */
    private void updatePetVisual(EvolutionStage stage) {
        if (stage == null) return;
        
        // For now, use emoji representations
        // In a full implementation, this would load actual sprite animations
        String petEmoji = switch (stage) {
            case EGG -> "🥚";
            case BABY -> "🐣";
            case TEEN -> "🐤";
            case ADULT -> "🐦";
            case LEGENDARY -> "🦅";
        };
        
        petPlaceholder.setText(petEmoji);
        
        // Add visual effects based on mood
        PetMood currentMood = viewModel.getCurrentMood();
        if (currentMood != null) {
            updateMoodEffects(currentMood);
        }
    }
    
    /**
     * Updates visual effects based on pet mood
     */
    private void updateMoodEffects(PetMood mood) {
        // Reset any existing effects
        petDisplayArea.setStyle("-fx-background-color: #A8DADC; -fx-background-radius: 16px; -fx-border-radius: 16px; -fx-border-color: #457B9D; -fx-border-width: 2px;");
        
        // Apply mood-specific effects
        switch (mood) {
            case HAPPY:
                petDisplayArea.setStyle(petDisplayArea.getStyle() + " -fx-effect: dropshadow(gaussian, gold, 12, 0.3, 0, 0);");
                break;
            case CELEBRATING:
                petDisplayArea.setStyle(petDisplayArea.getStyle() + " -fx-effect: dropshadow(gaussian, lime, 16, 0.5, 0, 0);");
                break;
            case WORRIED:
                petDisplayArea.setStyle(petDisplayArea.getStyle() + " -fx-background-color: #E8C5C5;");
                break;
            case SLEEPY:
                petDisplayArea.setStyle(petDisplayArea.getStyle() + " -fx-background-color: #D8D8F0;");
                break;
            case NEUTRAL:
            default:
                // Keep default styling
                break;
        }
    }
    
    /**
     * Updates navigation button states
     */
    private void updateNavigationState() {
        // Reset all buttons
        petTabButton.getStyleClass().removeAll("nav-button-active");
        habitsTabButton.getStyleClass().removeAll("nav-button-active");
        statsTabButton.getStyleClass().removeAll("nav-button-active");
        achievementsTabButton.getStyleClass().removeAll("nav-button-active");
        
        // Set pet tab as active
        if (!petTabButton.getStyleClass().contains("nav-button-active")) {
            petTabButton.getStyleClass().add("nav-button-active");
        }
    }
    
    // Navigation event handlers
    
    @FXML
    private void onPetTabClicked() {
        updateNavigationState();
        // Already on pet tab, no navigation needed
    }
    
    @FXML
    private void onHabitsTabClicked() {
        NavigationManager.getInstance().navigateTo(NavigationManager.Screen.HABITS);
    }
    
    @FXML
    private void onStatsTabClicked() {
        NavigationManager.getInstance().navigateTo(NavigationManager.Screen.STATISTICS);
    }
    
    @FXML
    private void onAchievementsTabClicked() {
        NavigationManager.getInstance().navigateTo(NavigationManager.Screen.ACHIEVEMENTS);
    }
    
    /**
     * Sets the ViewModel for this controller
     * @param viewModel The MainPetViewModel to use
     */
    public void setViewModel(MainPetViewModel viewModel) {
        this.viewModel = viewModel;
        if (viewModel != null) {
            setupPropertyBindings();
        }
    }
    
    /**
     * Gets the current ViewModel
     * @return The current MainPetViewModel
     */
    public MainPetViewModel getViewModel() {
        return viewModel;
    }
}