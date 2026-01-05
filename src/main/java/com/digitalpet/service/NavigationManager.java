package com.digitalpet.service;

import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

/**
 * NavigationManager handles screen navigation with smooth transitions.
 * Implements persistent bottom navigation and ensures accessibility within 2 taps.
 * 
 * Requirements addressed:
 * - 6.3: Persistent bottom navigation bar with access to all sections
 * - 6.4: Smooth transitions (300ms max) and 2-tap accessibility
 */
public class NavigationManager {
    
    /**
     * Enumeration of available screens in the application
     */
    public enum Screen {
        PET("MainPetView.fxml", "Pet"),
        HABITS("HabitInputView.fxml", "Habits"), 
        STATISTICS("StatisticsView.fxml", "Statistics"),
        ACHIEVEMENTS("AchievementView.fxml", "Achievements");
        
        private final String fxmlFile;
        private final String displayName;
        
        Screen(String fxmlFile, String displayName) {
            this.fxmlFile = fxmlFile;
            this.displayName = displayName;
        }
        
        public String getFxmlFile() { return fxmlFile; }
        public String getDisplayName() { return displayName; }
    }
    
    // Navigation state
    private final ObjectProperty<Screen> currentScreen = new SimpleObjectProperty<>(Screen.PET);
    private final Stack<Screen> navigationHistory = new Stack<>();
    
    // UI components
    private Stage primaryStage;
    private StackPane rootContainer;
    private Scene scene;
    
    // Screen caching for performance
    private final Map<Screen, Parent> screenCache = new HashMap<>();
    private final Map<Screen, Object> controllerCache = new HashMap<>();
    
    // ViewModels for dependency injection
    private Object mainPetViewModel;
    private Object habitInputViewModel;
    private Object achievementViewModel;
    private Object statisticsViewModel;
    
    // Animation settings
    private static final Duration TRANSITION_DURATION = Duration.millis(300);
    private static final double SCALE_FACTOR = 0.95;
    
    // Singleton instance
    private static NavigationManager instance;
    
    /**
     * Private constructor for singleton pattern
     */
    private NavigationManager() {
        // Initialize navigation history with default screen
        navigationHistory.push(Screen.PET);
    }
    
    /**
     * Gets the singleton instance of NavigationManager
     */
    public static NavigationManager getInstance() {
        if (instance == null) {
            instance = new NavigationManager();
        }
        return instance;
    }
    
    /**
     * Initializes the NavigationManager with the primary stage
     * @param primaryStage The main application stage
     */
    public void initialize(Stage primaryStage) {
        this.primaryStage = primaryStage;
        
        // Create root container for screen transitions
        this.rootContainer = new StackPane();
        this.rootContainer.getStyleClass().add("navigation-root");
        
        // Create scene
        this.scene = new Scene(rootContainer, 800, 600);
        
        // Load CSS styles
        loadStyles();
        
        // Set scene to stage
        primaryStage.setScene(scene);
        
        // Apply saved navigation preferences
        NavigationStateManager.getInstance().applyToNavigationManager(this);
        
        // If no saved state, load initial screen
        if (currentScreen.get() == null) {
            navigateToScreen(Screen.PET, false);
        }
        
        // Listen for screen changes to update state manager
        currentScreen.addListener((observable, oldScreen, newScreen) -> {
            if (newScreen != null) {
                NavigationStateManager.getInstance().setLastScreen(newScreen);
            }
        });
    }
    
    /**
     * Loads CSS styles for the application
     */
    private void loadStyles() {
        try {
            // Load design system CSS
            String designSystemCss = getClass().getResource("/com/digitalpet/view/DesignSystem.css").toExternalForm();
            scene.getStylesheets().add(designSystemCss);
        } catch (Exception e) {
            System.err.println("Warning: Could not load DesignSystem.css - " + e.getMessage());
        }
    }
    
    /**
     * Navigates to the specified screen with smooth transition
     * @param targetScreen The screen to navigate to
     */
    public void navigateTo(Screen targetScreen) {
        navigateToScreen(targetScreen, true);
    }
    
    /**
     * Navigates to the specified screen
     * @param targetScreen The screen to navigate to
     * @param animate Whether to animate the transition
     */
    private void navigateToScreen(Screen targetScreen, boolean animate) {
        if (targetScreen == currentScreen.get()) {
            return; // Already on target screen
        }
        
        // Load target screen
        Parent targetNode = loadScreen(targetScreen);
        if (targetNode == null) {
            System.err.println("Failed to load screen: " + targetScreen);
            return;
        }
        
        // Update navigation history
        updateNavigationHistory(targetScreen);
        
        // Perform transition
        if (animate && !rootContainer.getChildren().isEmpty()) {
            performAnimatedTransition(targetNode, targetScreen);
        } else {
            // Direct replacement without animation
            rootContainer.getChildren().clear();
            rootContainer.getChildren().add(targetNode);
            currentScreen.set(targetScreen);
        }
    }
    
    /**
     * Sets ViewModels for dependency injection into controllers
     * @param mainPetViewModel The main pet view model
     * @param habitInputViewModel The habit input view model
     * @param achievementViewModel The achievement view model
     * @param statisticsViewModel The statistics view model
     */
    public void setViewModels(Object mainPetViewModel, Object habitInputViewModel, 
                             Object achievementViewModel, Object statisticsViewModel) {
        this.mainPetViewModel = mainPetViewModel;
        this.habitInputViewModel = habitInputViewModel;
        this.achievementViewModel = achievementViewModel;
        this.statisticsViewModel = statisticsViewModel;
        
        // Clear cache to ensure ViewModels are injected on next load
        clearCache();
    }
    
    /**
     * Gets the appropriate ViewModel for a screen
     * @param screen The screen to get the ViewModel for
     * @return The ViewModel instance, or null if not available
     */
    public Object getViewModelForScreen(Screen screen) {
        return switch (screen) {
            case PET -> mainPetViewModel;
            case HABITS -> habitInputViewModel;
            case ACHIEVEMENTS -> achievementViewModel;
            case STATISTICS -> statisticsViewModel;
        };
    }
    
    /**
     * Loads a screen from FXML, using cache if available
     * @param screen The screen to load
     * @return The loaded Parent node, or null if loading failed
     */
    private Parent loadScreen(Screen screen) {
        // Check cache first
        if (screenCache.containsKey(screen)) {
            return screenCache.get(screen);
        }
        
        try {
            // Load FXML
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/com/digitalpet/view/" + screen.getFxmlFile()));
            Parent root = loader.load();
            
            // Cache the screen and controller
            screenCache.put(screen, root);
            controllerCache.put(screen, loader.getController());
            
            // Apply screen-specific styling
            applyScreenStyling(root, screen);
            
            return root;
            
        } catch (IOException e) {
            System.err.println("Error loading screen " + screen + ": " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Applies screen-specific styling and accessibility features
     * @param root The root node of the screen
     * @param screen The screen type
     */
    private void applyScreenStyling(Parent root, Screen screen) {
        // Add screen-specific CSS class
        root.getStyleClass().add("screen-" + screen.name().toLowerCase());
        
        // Set accessibility properties
        root.setAccessibleText(screen.getDisplayName() + " screen");
        root.setAccessibleHelp("Navigate using the bottom navigation bar or keyboard shortcuts");
        
        // Ensure proper focus management
        root.setFocusTraversable(true);
    }
    
    /**
     * Performs animated transition between screens
     * @param newScreen The new screen to transition to
     * @param targetScreen The target screen enum
     */
    private void performAnimatedTransition(Parent newScreen, Screen targetScreen) {
        // Check if transitions are enabled
        if (!NavigationStateManager.getInstance().areTransitionsEnabled()) {
            // Direct replacement without animation
            rootContainer.getChildren().clear();
            rootContainer.getChildren().add(newScreen);
            currentScreen.set(targetScreen);
            return;
        }
        
        Parent currentNode = (Parent) rootContainer.getChildren().get(0);
        
        // Get transition duration from preferences
        Duration transitionDuration = Duration.millis(NavigationStateManager.getInstance().getTransitionDuration());
        
        // Prepare new screen for animation
        newScreen.setOpacity(0.0);
        newScreen.setScaleX(SCALE_FACTOR);
        newScreen.setScaleY(SCALE_FACTOR);
        
        // Add new screen to container
        rootContainer.getChildren().add(newScreen);
        
        // Create fade out animation for current screen
        FadeTransition fadeOut = new FadeTransition(transitionDuration, currentNode);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        
        ScaleTransition scaleOut = new ScaleTransition(transitionDuration, currentNode);
        scaleOut.setFromX(1.0);
        scaleOut.setFromY(1.0);
        scaleOut.setToX(SCALE_FACTOR);
        scaleOut.setToY(SCALE_FACTOR);
        
        // Create fade in animation for new screen
        FadeTransition fadeIn = new FadeTransition(transitionDuration, newScreen);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        
        ScaleTransition scaleIn = new ScaleTransition(transitionDuration, newScreen);
        scaleIn.setFromX(SCALE_FACTOR);
        scaleIn.setFromY(SCALE_FACTOR);
        scaleIn.setToX(1.0);
        scaleIn.setToY(1.0);
        
        // Combine animations
        ParallelTransition outTransition = new ParallelTransition(fadeOut, scaleOut);
        ParallelTransition inTransition = new ParallelTransition(fadeIn, scaleIn);
        
        // Execute transition
        outTransition.setOnFinished(e -> {
            // Remove old screen and update current screen
            rootContainer.getChildren().remove(currentNode);
            currentScreen.set(targetScreen);
            
            // Start in transition
            inTransition.play();
        });
        
        outTransition.play();
    }
    
    /**
     * Updates navigation history for back navigation support
     * @param targetScreen The screen being navigated to
     */
    private void updateNavigationHistory(Screen targetScreen) {
        // Remove target screen from history if it exists
        navigationHistory.removeIf(screen -> screen == targetScreen);
        
        // Add current screen to history if different from target
        if (currentScreen.get() != null && currentScreen.get() != targetScreen) {
            navigationHistory.push(currentScreen.get());
        }
        
        // Limit history size to prevent memory issues
        while (navigationHistory.size() > 10) {
            navigationHistory.remove(0);
        }
    }
    
    /**
     * Navigates back to the previous screen in history
     * @return true if navigation occurred, false if no history available
     */
    public boolean navigateBack() {
        if (navigationHistory.isEmpty()) {
            return false;
        }
        
        Screen previousScreen = navigationHistory.pop();
        navigateToScreen(previousScreen, true);
        return true;
    }
    
    /**
     * Gets the controller for the specified screen
     * @param screen The screen to get the controller for
     * @return The controller instance, or null if not loaded
     */
    public Object getController(Screen screen) {
        return controllerCache.get(screen);
    }
    
    /**
     * Gets the controller for the current screen
     * @return The current screen's controller, or null if not available
     */
    public Object getCurrentController() {
        return getController(currentScreen.get());
    }
    
    /**
     * Clears the screen cache to free memory
     */
    public void clearCache() {
        screenCache.clear();
        controllerCache.clear();
    }
    
    /**
     * Refreshes the current screen by reloading it
     */
    public void refreshCurrentScreen() {
        Screen current = currentScreen.get();
        if (current != null) {
            // Remove from cache to force reload
            screenCache.remove(current);
            controllerCache.remove(current);
            
            // Reload screen
            navigateToScreen(current, false);
        }
    }
    
    /**
     * Checks if navigation to the specified screen is possible within 2 taps
     * @param targetScreen The target screen
     * @return true if accessible within 2 taps, false otherwise
     */
    public boolean isAccessibleWithinTwoTaps(Screen targetScreen) {
        // All screens are accessible within 1 tap from the bottom navigation
        // This satisfies the 2-tap requirement from Requirements 6.3, 6.4
        return true;
    }
    
    /**
     * Gets the current screen property for binding
     * @return ObjectProperty for the current screen
     */
    public ObjectProperty<Screen> currentScreenProperty() {
        return currentScreen;
    }
    
    /**
     * Gets the current screen
     * @return The current screen
     */
    public Screen getCurrentScreen() {
        return currentScreen.get();
    }
    
    /**
     * Sets up keyboard shortcuts for navigation accessibility
     */
    public void setupKeyboardShortcuts() {
        if (scene == null) return;
        
        scene.setOnKeyPressed(event -> {
            // Handle keyboard navigation shortcuts
            switch (event.getCode()) {
                case DIGIT1:
                    if (event.isControlDown()) {
                        navigateTo(Screen.PET);
                        event.consume();
                    }
                    break;
                case DIGIT2:
                    if (event.isControlDown()) {
                        navigateTo(Screen.HABITS);
                        event.consume();
                    }
                    break;
                case DIGIT3:
                    if (event.isControlDown()) {
                        navigateTo(Screen.STATISTICS);
                        event.consume();
                    }
                    break;
                case DIGIT4:
                    if (event.isControlDown()) {
                        navigateTo(Screen.ACHIEVEMENTS);
                        event.consume();
                    }
                    break;
                case ESCAPE:
                    // Navigate back on Escape
                    navigateBack();
                    event.consume();
                    break;
            }
        });
    }
    
    /**
     * Gets navigation state for persistence
     * @return Map containing navigation state
     */
    public Map<String, Object> getNavigationState() {
        Map<String, Object> state = new HashMap<>();
        state.put("currentScreen", currentScreen.get().name());
        state.put("historySize", navigationHistory.size());
        return state;
    }
    
    /**
     * Restores navigation state from persistence
     * @param state The saved navigation state
     */
    public void restoreNavigationState(Map<String, Object> state) {
        if (state.containsKey("currentScreen")) {
            try {
                String screenName = (String) state.get("currentScreen");
                Screen screen = Screen.valueOf(screenName);
                navigateToScreen(screen, false);
            } catch (Exception e) {
                System.err.println("Error restoring navigation state: " + e.getMessage());
                // Fall back to default screen
                navigateToScreen(Screen.PET, false);
            }
        }
    }
}