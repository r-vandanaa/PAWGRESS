package com.digitalpet.service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * NavigationStateManager handles persistence of navigation state across application sessions.
 * Stores user preferences for screen navigation and restores them on application startup.
 * 
 * Requirements addressed:
 * - 6.4: Navigation state persistence across application sessions
 */
public class NavigationStateManager {
    
    private static final String NAVIGATION_PREFERENCES_FILE = "navigation.properties";
    private static final String USER_HOME_DIR = System.getProperty("user.home");
    private static final String APP_DATA_DIR = ".digitalpet";
    
    private static NavigationStateManager instance;
    private Properties navigationProperties;
    private Path preferencesPath;
    
    /**
     * Private constructor for singleton pattern
     */
    private NavigationStateManager() {
        initializePreferencesPath();
        loadNavigationState();
    }
    
    /**
     * Gets the singleton instance
     */
    public static NavigationStateManager getInstance() {
        if (instance == null) {
            instance = new NavigationStateManager();
        }
        return instance;
    }
    
    /**
     * Initializes the preferences file path
     */
    private void initializePreferencesPath() {
        try {
            Path appDataDir = Paths.get(USER_HOME_DIR, APP_DATA_DIR);
            
            // Create app data directory if it doesn't exist
            if (!Files.exists(appDataDir)) {
                Files.createDirectories(appDataDir);
            }
            
            preferencesPath = appDataDir.resolve(NAVIGATION_PREFERENCES_FILE);
            
        } catch (IOException e) {
            System.err.println("Warning: Could not create preferences directory: " + e.getMessage());
            // Fall back to current directory
            preferencesPath = Paths.get(NAVIGATION_PREFERENCES_FILE);
        }
    }
    
    /**
     * Loads navigation state from preferences file
     */
    private void loadNavigationState() {
        navigationProperties = new Properties();
        
        if (Files.exists(preferencesPath)) {
            try (InputStream input = Files.newInputStream(preferencesPath)) {
                navigationProperties.load(input);
            } catch (IOException e) {
                System.err.println("Warning: Could not load navigation preferences: " + e.getMessage());
            }
        }
        
        // Set default values if not present
        setDefaultValues();
    }
    
    /**
     * Sets default navigation preferences
     */
    private void setDefaultValues() {
        if (!navigationProperties.containsKey("lastScreen")) {
            navigationProperties.setProperty("lastScreen", NavigationManager.Screen.PET.name());
        }
        if (!navigationProperties.containsKey("rememberLastScreen")) {
            navigationProperties.setProperty("rememberLastScreen", "true");
        }
        if (!navigationProperties.containsKey("enableTransitions")) {
            navigationProperties.setProperty("enableTransitions", "true");
        }
        if (!navigationProperties.containsKey("transitionDuration")) {
            navigationProperties.setProperty("transitionDuration", "300");
        }
    }
    
    /**
     * Saves navigation state to preferences file
     */
    public void saveNavigationState() {
        try (OutputStream output = Files.newOutputStream(preferencesPath)) {
            navigationProperties.store(output, "Digital Pet Evolution - Navigation Preferences");
        } catch (IOException e) {
            System.err.println("Warning: Could not save navigation preferences: " + e.getMessage());
        }
    }
    
    /**
     * Gets the last active screen
     * @return The last active screen, or PET if not set
     */
    public NavigationManager.Screen getLastScreen() {
        String screenName = navigationProperties.getProperty("lastScreen", NavigationManager.Screen.PET.name());
        try {
            return NavigationManager.Screen.valueOf(screenName);
        } catch (IllegalArgumentException e) {
            return NavigationManager.Screen.PET; // Default fallback
        }
    }
    
    /**
     * Sets the last active screen
     * @param screen The screen to remember
     */
    public void setLastScreen(NavigationManager.Screen screen) {
        navigationProperties.setProperty("lastScreen", screen.name());
    }
    
    /**
     * Gets whether to remember the last screen on startup
     * @return true if should remember last screen, false otherwise
     */
    public boolean shouldRememberLastScreen() {
        return Boolean.parseBoolean(navigationProperties.getProperty("rememberLastScreen", "true"));
    }
    
    /**
     * Sets whether to remember the last screen on startup
     * @param remember true to remember last screen, false otherwise
     */
    public void setRememberLastScreen(boolean remember) {
        navigationProperties.setProperty("rememberLastScreen", String.valueOf(remember));
    }
    
    /**
     * Gets whether transitions are enabled
     * @return true if transitions are enabled, false otherwise
     */
    public boolean areTransitionsEnabled() {
        return Boolean.parseBoolean(navigationProperties.getProperty("enableTransitions", "true"));
    }
    
    /**
     * Sets whether transitions are enabled
     * @param enabled true to enable transitions, false otherwise
     */
    public void setTransitionsEnabled(boolean enabled) {
        navigationProperties.setProperty("enableTransitions", String.valueOf(enabled));
    }
    
    /**
     * Gets the transition duration in milliseconds
     * @return The transition duration
     */
    public int getTransitionDuration() {
        try {
            return Integer.parseInt(navigationProperties.getProperty("transitionDuration", "300"));
        } catch (NumberFormatException e) {
            return 300; // Default fallback
        }
    }
    
    /**
     * Sets the transition duration in milliseconds
     * @param duration The transition duration (must be between 100 and 1000ms)
     */
    public void setTransitionDuration(int duration) {
        // Clamp duration to reasonable bounds
        duration = Math.max(100, Math.min(1000, duration));
        navigationProperties.setProperty("transitionDuration", String.valueOf(duration));
    }
    
    /**
     * Gets all navigation preferences as a map
     * @return Map containing all navigation preferences
     */
    public Map<String, String> getAllPreferences() {
        Map<String, String> preferences = new HashMap<>();
        for (String key : navigationProperties.stringPropertyNames()) {
            preferences.put(key, navigationProperties.getProperty(key));
        }
        return preferences;
    }
    
    /**
     * Resets all navigation preferences to defaults
     */
    public void resetToDefaults() {
        navigationProperties.clear();
        setDefaultValues();
        saveNavigationState();
    }
    
    /**
     * Updates navigation state from NavigationManager
     * @param navigationManager The NavigationManager instance
     */
    public void updateFromNavigationManager(NavigationManager navigationManager) {
        if (navigationManager.getCurrentScreen() != null) {
            setLastScreen(navigationManager.getCurrentScreen());
        }
    }
    
    /**
     * Applies saved preferences to NavigationManager
     * @param navigationManager The NavigationManager instance
     */
    public void applyToNavigationManager(NavigationManager navigationManager) {
        if (shouldRememberLastScreen()) {
            NavigationManager.Screen lastScreen = getLastScreen();
            if (lastScreen != null) {
                // Restore navigation state
                Map<String, Object> state = new HashMap<>();
                state.put("currentScreen", lastScreen.name());
                navigationManager.restoreNavigationState(state);
            }
        }
    }
}