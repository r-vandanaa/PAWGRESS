package com.digitalpet;

import com.digitalpet.service.ApplicationLifecycleManager;
import com.digitalpet.service.DigitalPetIntegrationService;
import com.digitalpet.service.NavigationManager;
import com.digitalpet.service.NavigationStateManager;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

/**
 * Main application class for Digital Pet Evolution
 * Integrates all components through the DigitalPetIntegrationService and ApplicationLifecycleManager.
 * 
 * Requirements addressed:
 * - All requirements (integration): Complete system integration
 * - Non-functional requirements: Application lifecycle management
 * - 6.4: Application lifecycle management with navigation state
 * - 6.3: Persistent bottom navigation throughout application
 */
public class DigitalPetApplication extends Application {

    private NavigationManager navigationManager;
    private DigitalPetIntegrationService integrationService;
    private ApplicationLifecycleManager lifecycleManager;

    @Override
    public void start(Stage primaryStage) {
        try {
            // Initialize integration service first
            integrationService = new DigitalPetIntegrationService();
            
            // Initialize lifecycle manager
            lifecycleManager = new ApplicationLifecycleManager(integrationService);
            
            // Handle startup sequence
            lifecycleManager.handleStartup().thenAccept(success -> {
                if (success) {
                    Platform.runLater(() -> initializeUI(primaryStage));
                } else {
                    Platform.runLater(() -> {
                        System.err.println("Application startup failed");
                        Platform.exit();
                    });
                }
            }).exceptionally(throwable -> {
                Platform.runLater(() -> {
                    System.err.println("Application startup error: " + throwable.getMessage());
                    Platform.exit();
                });
                return null;
            });
            
        } catch (Exception e) {
            System.err.println("Critical startup error: " + e.getMessage());
            e.printStackTrace();
            Platform.exit();
        }
    }
    
    /**
     * Initializes the UI after successful startup
     */
    private void initializeUI(Stage primaryStage) {
        try {
            // Configure primary stage
            primaryStage.setTitle("Digital Pet Evolution");
            primaryStage.setWidth(800);
            primaryStage.setHeight(600);
            primaryStage.setMinWidth(600);
            primaryStage.setMinHeight(500);
            
            // Initialize navigation manager
            navigationManager = NavigationManager.getInstance();
            navigationManager.initialize(primaryStage);
            
            // Inject ViewModels into navigation manager
            navigationManager.setViewModels(
                integrationService.getMainPetViewModel(),
                integrationService.getHabitInputViewModel(),
                integrationService.getAchievementViewModel(),
                integrationService.getStatisticsViewModel()
            );
            
            // Setup keyboard shortcuts for accessibility
            navigationManager.setupKeyboardShortcuts();
            
            // Handle application close with proper lifecycle management
            primaryStage.setOnCloseRequest(event -> {
                // Prevent default close to handle shutdown properly
                event.consume();
                
                // Initiate graceful shutdown
                lifecycleManager.handleShutdown().thenAccept(success -> {
                    Platform.runLater(() -> {
                        // Save navigation state
                        NavigationStateManager stateManager = NavigationStateManager.getInstance();
                        stateManager.updateFromNavigationManager(navigationManager);
                        stateManager.saveNavigationState();
                        
                        // Exit application
                        Platform.exit();
                    });
                }).exceptionally(throwable -> {
                    Platform.runLater(() -> {
                        System.err.println("Shutdown error: " + throwable.getMessage());
                        // Force exit if graceful shutdown fails
                        lifecycleManager.handleEmergencyShutdown();
                    });
                    return null;
                });
            });
            
            // Show the stage
            primaryStage.show();
            
            // Restore navigation state if available
            // In a full implementation, this would load from preferences
            // For now, we start with the default PET screen
            
        } catch (Exception e) {
            System.err.println("UI initialization error: " + e.getMessage());
            e.printStackTrace();
            lifecycleManager.handleEmergencyShutdown();
        }
    }
    
    @Override
    public void stop() throws Exception {
        // This is called when Platform.exit() is invoked
        // Ensure proper cleanup if not already done
        if (lifecycleManager != null && !lifecycleManager.isShutdownInProgress()) {
            lifecycleManager.handleEmergencyShutdown();
        }
        
        super.stop();
    }
    
    /**
     * Gets the integration service for external access
     * @return The integration service
     */
    public DigitalPetIntegrationService getIntegrationService() {
        return integrationService;
    }
    
    /**
     * Gets the lifecycle manager for external access
     * @return The lifecycle manager
     */
    public ApplicationLifecycleManager getLifecycleManager() {
        return lifecycleManager;
    }

    public static void main(String[] args) {
        launch(args);
    }
}