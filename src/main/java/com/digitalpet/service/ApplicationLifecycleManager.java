package com.digitalpet.service;

import com.digitalpet.model.DigitalPet;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Manages application lifecycle including startup, shutdown, error recovery, and graceful degradation.
 * Handles data persistence, error recovery, and ensures proper cleanup of resources.
 * 
 * Requirements addressed:
 * - Non-functional requirements: Application lifecycle management
 * - Data persistence on application close
 * - Error recovery and graceful degradation
 */
public class ApplicationLifecycleManager {
    
    // Lifecycle state
    private final BooleanProperty applicationStarted;
    private final BooleanProperty shutdownInProgress;
    private final StringProperty lifecycleStatus;
    private final AtomicBoolean emergencyShutdown;
    
    // Services
    private final DigitalPetIntegrationService integrationService;
    private final LocalDataStorage dataStorage;
    
    // Error handling
    private int errorCount;
    private LocalDateTime lastErrorTime;
    private static final int MAX_ERRORS_PER_MINUTE = 5;
    
    // Shutdown timeout
    private static final long SHUTDOWN_TIMEOUT_SECONDS = 10;
    
    public ApplicationLifecycleManager(DigitalPetIntegrationService integrationService) {
        this.integrationService = integrationService;
        this.dataStorage = new LocalDataStorage();
        
        // Initialize properties
        this.applicationStarted = new SimpleBooleanProperty(false);
        this.shutdownInProgress = new SimpleBooleanProperty(false);
        this.lifecycleStatus = new SimpleStringProperty("Initializing");
        this.emergencyShutdown = new AtomicBoolean(false);
        
        // Initialize error tracking
        this.errorCount = 0;
        this.lastErrorTime = LocalDateTime.now();
        
        // Set up error handling
        setupErrorHandling();
    }
    
    /**
     * Handles application startup sequence
     */
    public CompletableFuture<Boolean> handleStartup() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                lifecycleStatus.set("Starting application...");
                
                // 1. Initialize data storage
                if (!initializeDataStorage()) {
                    lifecycleStatus.set("Failed to initialize data storage");
                    return false;
                }
                
                // 2. Load saved data
                if (!loadSavedData()) {
                    lifecycleStatus.set("Warning: Could not load saved data, starting fresh");
                    // Continue anyway - this is not a fatal error
                }
                
                // 3. Initialize integration service
                if (!initializeIntegrationService()) {
                    lifecycleStatus.set("Failed to initialize integration service");
                    return false;
                }
                
                // 4. Verify system integrity
                if (!verifySystemIntegrity()) {
                    lifecycleStatus.set("System integrity check failed");
                    return false;
                }
                
                // 5. Set up periodic data saving
                setupPeriodicDataSaving();
                
                // 6. Set up shutdown hooks
                setupShutdownHooks();
                
                // Mark as started
                Platform.runLater(() -> {
                    applicationStarted.set(true);
                    lifecycleStatus.set("Application started successfully");
                });
                
                return true;
                
            } catch (Exception e) {
                handleStartupError(e);
                return false;
            }
        });
    }
    
    /**
     * Handles application shutdown sequence
     */
    public CompletableFuture<Boolean> handleShutdown() {
        return CompletableFuture.supplyAsync(() -> {
            if (shutdownInProgress.get()) {
                return true; // Already shutting down
            }
            
            Platform.runLater(() -> {
                shutdownInProgress.set(true);
                lifecycleStatus.set("Shutting down...");
            });
            
            try {
                // 1. Save all current data
                if (!saveAllData()) {
                    System.err.println("Warning: Failed to save some data during shutdown");
                }
                
                // 2. Stop background services
                stopBackgroundServices();
                
                // 3. Clean up resources
                cleanupResources();
                
                // 4. Final data persistence
                finalDataPersistence();
                
                Platform.runLater(() -> {
                    lifecycleStatus.set("Shutdown complete");
                });
                
                return true;
                
            } catch (Exception e) {
                handleShutdownError(e);
                return false;
            }
        });
    }
    
    /**
     * Handles emergency shutdown (when normal shutdown fails)
     */
    public void handleEmergencyShutdown() {
        if (emergencyShutdown.getAndSet(true)) {
            return; // Already in emergency shutdown
        }
        
        System.err.println("Initiating emergency shutdown...");
        
        try {
            // Try to save critical data only
            saveCriticalDataOnly();
            
            // Force cleanup
            forceCleanup();
            
        } catch (Exception e) {
            System.err.println("Emergency shutdown error: " + e.getMessage());
        } finally {
            // Force exit
            Platform.exit();
            System.exit(1);
        }
    }
    
    /**
     * Initializes data storage system
     */
    private boolean initializeDataStorage() {
        try {
            dataStorage.initialize();
            return true;
        } catch (Exception e) {
            System.err.println("Failed to initialize data storage: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Loads saved data from storage
     */
    private boolean loadSavedData() {
        try {
            // This is handled by the integration service
            // We just verify it completed successfully
            return integrationService.isSystemInitialized();
        } catch (Exception e) {
            System.err.println("Failed to load saved data: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Initializes the integration service
     */
    private boolean initializeIntegrationService() {
        try {
            // Integration service is already initialized in constructor
            // We just verify it's working
            return integrationService.isSystemInitialized();
        } catch (Exception e) {
            System.err.println("Failed to initialize integration service: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Verifies system integrity after startup
     */
    private boolean verifySystemIntegrity() {
        try {
            // Check that all critical components are working
            DigitalPet pet = integrationService.getDigitalPet();
            if (pet == null) {
                System.err.println("Digital pet is null");
                return false;
            }
            
            // Verify ViewModels are initialized
            if (integrationService.getMainPetViewModel() == null ||
                integrationService.getHabitInputViewModel() == null ||
                integrationService.getAchievementViewModel() == null ||
                integrationService.getStatisticsViewModel() == null) {
                System.err.println("One or more ViewModels are null");
                return false;
            }
            
            // Verify services are working
            if (integrationService.getXpSystem() == null ||
                integrationService.getMoodSystem() == null ||
                integrationService.getAchievementSystem() == null) {
                System.err.println("One or more services are null");
                return false;
            }
            
            return true;
            
        } catch (Exception e) {
            System.err.println("System integrity check failed: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Sets up periodic data saving
     */
    private void setupPeriodicDataSaving() {
        // In a real implementation, this would use a ScheduledExecutorService
        // For now, we rely on the integration service's auto-save functionality
    }
    
    /**
     * Sets up shutdown hooks for graceful termination
     */
    private void setupShutdownHooks() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (!shutdownInProgress.get()) {
                System.out.println("Shutdown hook triggered - performing graceful shutdown");
                
                try {
                    // Use a timeout to prevent hanging
                    CompletableFuture<Boolean> shutdownFuture = handleShutdown();
                    shutdownFuture.get(SHUTDOWN_TIMEOUT_SECONDS, TimeUnit.SECONDS);
                } catch (Exception e) {
                    System.err.println("Graceful shutdown failed, forcing emergency shutdown: " + e.getMessage());
                    handleEmergencyShutdown();
                }
            }
        }));
    }
    
    /**
     * Sets up global error handling
     */
    private void setupErrorHandling() {
        // Set up JavaFX error handler
        Thread.setDefaultUncaughtExceptionHandler((thread, exception) -> {
            handleUncaughtException(thread, exception);
        });
    }
    
    /**
     * Handles uncaught exceptions
     */
    private void handleUncaughtException(Thread thread, Throwable exception) {
        System.err.println("Uncaught exception in thread " + thread.getName() + ": " + exception.getMessage());
        exception.printStackTrace();
        
        // Track error frequency
        LocalDateTime now = LocalDateTime.now();
        if (lastErrorTime.plusMinutes(1).isAfter(now)) {
            errorCount++;
        } else {
            errorCount = 1;
            lastErrorTime = now;
        }
        
        // If too many errors, initiate emergency shutdown
        if (errorCount >= MAX_ERRORS_PER_MINUTE) {
            System.err.println("Too many errors detected, initiating emergency shutdown");
            handleEmergencyShutdown();
        } else {
            // Try to recover gracefully
            attemptErrorRecovery(exception);
        }
    }
    
    /**
     * Attempts to recover from errors gracefully
     */
    private void attemptErrorRecovery(Throwable exception) {
        try {
            Platform.runLater(() -> {
                lifecycleStatus.set("Recovering from error: " + exception.getMessage());
                
                // Try to save current state
                try {
                    integrationService.updateAllSystems();
                } catch (Exception e) {
                    System.err.println("Failed to save state during error recovery: " + e.getMessage());
                }
                
                lifecycleStatus.set("Error recovery completed");
            });
            
        } catch (Exception e) {
            System.err.println("Error recovery failed: " + e.getMessage());
            // If recovery fails, consider emergency shutdown
            if (errorCount >= MAX_ERRORS_PER_MINUTE / 2) {
                handleEmergencyShutdown();
            }
        }
    }
    
    /**
     * Handles startup errors
     */
    private void handleStartupError(Exception e) {
        System.err.println("Startup error: " + e.getMessage());
        e.printStackTrace();
        
        Platform.runLater(() -> {
            lifecycleStatus.set("Startup failed: " + e.getMessage());
        });
        
        // Try graceful degradation
        attemptGracefulDegradation();
    }
    
    /**
     * Handles shutdown errors
     */
    private void handleShutdownError(Exception e) {
        System.err.println("Shutdown error: " + e.getMessage());
        e.printStackTrace();
        
        // If normal shutdown fails, try emergency shutdown
        handleEmergencyShutdown();
    }
    
    /**
     * Attempts graceful degradation when startup fails
     */
    private void attemptGracefulDegradation() {
        try {
            // Try to start with minimal functionality
            Platform.runLater(() -> {
                lifecycleStatus.set("Starting in safe mode...");
                
                // Reset integration service to minimal state
                integrationService.resetSystem();
                
                applicationStarted.set(true);
                lifecycleStatus.set("Started in safe mode - some features may be limited");
            });
            
        } catch (Exception e) {
            System.err.println("Graceful degradation failed: " + e.getMessage());
            handleEmergencyShutdown();
        }
    }
    
    /**
     * Saves all application data
     */
    private boolean saveAllData() {
        try {
            integrationService.updateAllSystems();
            return true;
        } catch (Exception e) {
            System.err.println("Failed to save all data: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Saves only critical data (for emergency situations)
     */
    private void saveCriticalDataOnly() {
        try {
            // Save pet data
            DigitalPet pet = integrationService.getDigitalPet();
            if (pet != null) {
                dataStorage.savePet(pet);
            }
            
            // Save habit history
            if (!integrationService.getHabitHistory().isEmpty()) {
                dataStorage.saveHabitHistory(integrationService.getHabitHistory());
            }
            
        } catch (Exception e) {
            System.err.println("Failed to save critical data: " + e.getMessage());
        }
    }
    
    /**
     * Stops background services
     */
    private void stopBackgroundServices() {
        // In a real implementation, this would stop any background threads,
        // timers, or scheduled tasks
    }
    
    /**
     * Cleans up resources
     */
    private void cleanupResources() {
        try {
            // Clear caches
            NavigationManager.getInstance().clearCache();
            
            // Clear integration service caches
            integrationService.clearRecentActivity();
            
        } catch (Exception e) {
            System.err.println("Error during resource cleanup: " + e.getMessage());
        }
    }
    
    /**
     * Performs final data persistence
     */
    private void finalDataPersistence() {
        try {
            // Ensure all data is written to disk
            dataStorage.flush();
        } catch (Exception e) {
            System.err.println("Error during final data persistence: " + e.getMessage());
        }
    }
    
    /**
     * Forces cleanup during emergency shutdown
     */
    private void forceCleanup() {
        try {
            // Force close any open resources
            dataStorage.forceClose();
        } catch (Exception e) {
            // Ignore errors during force cleanup
        }
    }
    
    // Property getters
    
    public BooleanProperty applicationStartedProperty() {
        return applicationStarted;
    }
    
    public BooleanProperty shutdownInProgressProperty() {
        return shutdownInProgress;
    }
    
    public StringProperty lifecycleStatusProperty() {
        return lifecycleStatus;
    }
    
    public boolean isApplicationStarted() {
        return applicationStarted.get();
    }
    
    public boolean isShutdownInProgress() {
        return shutdownInProgress.get();
    }
    
    public String getLifecycleStatus() {
        return lifecycleStatus.get();
    }
    
    public boolean isEmergencyShutdown() {
        return emergencyShutdown.get();
    }
    
    /**
     * Gets error statistics
     */
    public String getErrorStatistics() {
        return String.format("Errors in last minute: %d, Last error: %s", 
                errorCount, lastErrorTime);
    }
    
    /**
     * Manually triggers a system health check
     */
    public boolean performHealthCheck() {
        try {
            return verifySystemIntegrity();
        } catch (Exception e) {
            System.err.println("Health check failed: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Forces a data save (for manual backup)
     */
    public boolean forceSave() {
        return saveAllData();
    }
}