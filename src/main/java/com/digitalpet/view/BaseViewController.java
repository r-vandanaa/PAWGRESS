package com.digitalpet.view;

import com.digitalpet.service.NavigationManager;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

/**
 * Base controller class providing common navigation functionality.
 * Ensures consistent navigation behavior across all view controllers.
 * 
 * Requirements addressed:
 * - 6.3: Persistent bottom navigation bar functionality
 * - 6.4: Consistent navigation state management
 */
public abstract class BaseViewController {
    
    // Navigation buttons (should be present in all FXML files)
    @FXML protected Button petTabButton;
    @FXML protected Button habitsTabButton;
    @FXML protected Button statsTabButton;
    @FXML protected Button achievementsTabButton;
    
    /**
     * Initializes navigation functionality.
     * Should be called by subclasses in their initialize method.
     */
    protected void initializeNavigation() {
        setupNavigationButtons();
        updateNavigationState();
        
        // Listen for navigation changes to update button states
        NavigationManager.getInstance().currentScreenProperty().addListener(
            (observable, oldScreen, newScreen) -> updateNavigationState()
        );
    }
    
    /**
     * Sets up navigation button event handlers
     */
    private void setupNavigationButtons() {
        if (petTabButton != null) {
            petTabButton.setOnAction(e -> onPetTabClicked());
        }
        if (habitsTabButton != null) {
            habitsTabButton.setOnAction(e -> onHabitsTabClicked());
        }
        if (statsTabButton != null) {
            statsTabButton.setOnAction(e -> onStatsTabClicked());
        }
        if (achievementsTabButton != null) {
            achievementsTabButton.setOnAction(e -> onAchievementsTabClicked());
        }
    }
    
    /**
     * Updates navigation button states based on current screen
     */
    protected void updateNavigationState() {
        NavigationManager.Screen currentScreen = NavigationManager.getInstance().getCurrentScreen();
        
        // Reset all button states
        resetNavigationButtonStates();
        
        // Set active state for current screen
        switch (currentScreen) {
            case PET:
                if (petTabButton != null) {
                    petTabButton.getStyleClass().add("nav-button-active");
                }
                break;
            case HABITS:
                if (habitsTabButton != null) {
                    habitsTabButton.getStyleClass().add("nav-button-active");
                }
                break;
            case STATISTICS:
                if (statsTabButton != null) {
                    statsTabButton.getStyleClass().add("nav-button-active");
                }
                break;
            case ACHIEVEMENTS:
                if (achievementsTabButton != null) {
                    achievementsTabButton.getStyleClass().add("nav-button-active");
                }
                break;
        }
    }
    
    /**
     * Resets all navigation button states
     */
    private void resetNavigationButtonStates() {
        if (petTabButton != null) {
            petTabButton.getStyleClass().removeAll("nav-button-active");
        }
        if (habitsTabButton != null) {
            habitsTabButton.getStyleClass().removeAll("nav-button-active");
        }
        if (statsTabButton != null) {
            statsTabButton.getStyleClass().removeAll("nav-button-active");
        }
        if (achievementsTabButton != null) {
            achievementsTabButton.getStyleClass().removeAll("nav-button-active");
        }
    }
    
    // Navigation event handlers
    
    protected void onPetTabClicked() {
        NavigationManager.getInstance().navigateTo(NavigationManager.Screen.PET);
    }
    
    protected void onHabitsTabClicked() {
        NavigationManager.getInstance().navigateTo(NavigationManager.Screen.HABITS);
    }
    
    protected void onStatsTabClicked() {
        NavigationManager.getInstance().navigateTo(NavigationManager.Screen.STATISTICS);
    }
    
    protected void onAchievementsTabClicked() {
        NavigationManager.getInstance().navigateTo(NavigationManager.Screen.ACHIEVEMENTS);
    }
    
    /**
     * Gets the screen type that this controller represents.
     * Should be overridden by subclasses.
     * @return The screen type for this controller
     */
    protected abstract NavigationManager.Screen getScreenType();
    
    /**
     * Called when this screen becomes active.
     * Can be overridden by subclasses for screen-specific activation logic.
     */
    protected void onScreenActivated() {
        // Default implementation does nothing
    }
    
    /**
     * Called when this screen becomes inactive.
     * Can be overridden by subclasses for screen-specific deactivation logic.
     */
    protected void onScreenDeactivated() {
        // Default implementation does nothing
    }
}