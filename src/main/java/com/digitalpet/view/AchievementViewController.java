package com.digitalpet.view;

import com.digitalpet.model.Achievement;
import com.digitalpet.model.AchievementCategory;
import com.digitalpet.viewmodel.AchievementViewModel;
import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controller for the Achievement View, managing achievement display with badge-style cards.
 * Implements locked/unlocked states, progress indicators, and celebration animations.
 * 
 * Requirements addressed:
 * - 8.1: Achievement display with locked/unlocked states
 * - 8.2: Progress indicators for locked achievements
 * - 8.5: Celebration animations for unlocks
 */
public class AchievementViewController implements Initializable {
    
    // FXML injected components
    @FXML private Label achievementCountLabel;
    @FXML private ProgressBar completionProgressBar;
    @FXML private Label completionPercentageLabel;
    
    // Category filter buttons
    @FXML private Button allCategoryButton;
    @FXML private Button consistencyCategoryButton;
    @FXML private Button milestoneCategoryButton;
    @FXML private Button evolutionCategoryButton;
    
    // Achievement container
    @FXML private ScrollPane achievementScrollPane;
    @FXML private VBox achievementContainer;
    
    // Celebration overlay
    @FXML private StackPane celebrationOverlay;
    @FXML private Label celebrationTitle;
    @FXML private Label celebrationAchievementName;
    @FXML private Label celebrationDescription;
    @FXML private Button celebrationCloseButton;
    
    // Bottom navigation
    @FXML private HBox bottomNavigationBar;
    @FXML private Button petTabButton;
    @FXML private Button habitsTabButton;
    @FXML private Button statsTabButton;
    @FXML private Button achievementsTabButton;
    
    // ViewModel
    private AchievementViewModel viewModel;
    
    // Current filter state
    private AchievementCategory currentFilter = null; // null means "All"
    
    // Animation for celebrations
    private Animation celebrationAnimation;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupUI();
        setupEventHandlers();
    }
    
    /**
     * Sets the view model and binds properties
     */
    public void setViewModel(AchievementViewModel viewModel) {
        this.viewModel = viewModel;
        bindProperties();
        refreshAchievementDisplay();
        
        // Listen for new unlocks to trigger celebrations
        viewModel.hasRecentUnlockProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                showCelebrationForRecentUnlocks();
            }
        });
    }
    
    /**
     * Sets up initial UI state
     */
    private void setupUI() {
        // Set initial category button state
        updateCategoryButtonStates();
        
        // Configure scroll pane
        achievementScrollPane.setFitToWidth(true);
        achievementScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        achievementScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        
        // Hide celebration overlay initially
        celebrationOverlay.setVisible(false);
        celebrationOverlay.setManaged(false);
    }
    
    /**
     * Sets up event handlers for UI components
     */
    private void setupEventHandlers() {
        // Category filter buttons
        allCategoryButton.setOnAction(e -> onAllCategoryClicked());
        consistencyCategoryButton.setOnAction(e -> onConsistencyCategoryClicked());
        milestoneCategoryButton.setOnAction(e -> onMilestoneCategoryClicked());
        evolutionCategoryButton.setOnAction(e -> onEvolutionCategoryClicked());
        
        // Celebration close button
        celebrationCloseButton.setOnAction(e -> onCelebrationClose());
        
        // Navigation buttons
        petTabButton.setOnAction(e -> onPetTabClicked());
        habitsTabButton.setOnAction(e -> onHabitsTabClicked());
        statsTabButton.setOnAction(e -> onStatsTabClicked());
        achievementsTabButton.setOnAction(e -> onAchievementsTabClicked());
    }
    
    /**
     * Binds properties to the view model
     */
    private void bindProperties() {
        if (viewModel == null) return;
        
        // Bind achievement count and progress
        achievementCountLabel.textProperty().bind(viewModel.achievementSummaryTextProperty());
        completionProgressBar.progressProperty().bind(viewModel.completionPercentageProperty());
        completionPercentageLabel.textProperty().bind(viewModel.completionPercentageTextProperty());
    }
    
    /**
     * Refreshes the achievement display based on current filter
     */
    private void refreshAchievementDisplay() {
        if (viewModel == null) return;
        
        achievementContainer.getChildren().clear();
        
        List<Achievement> achievements;
        if (currentFilter == null) {
            achievements = viewModel.getAllAchievements();
        } else {
            achievements = viewModel.getAchievementsByCategory(currentFilter);
        }
        
        // Sort achievements: unlocked first, then by progress
        achievements.sort((a, b) -> {
            if (a.isUnlocked() && !b.isUnlocked()) return -1;
            if (!a.isUnlocked() && b.isUnlocked()) return 1;
            if (a.isUnlocked() && b.isUnlocked()) {
                // Both unlocked, sort by unlock date (most recent first)
                if (a.getUnlockedDate() != null && b.getUnlockedDate() != null) {
                    return b.getUnlockedDate().compareTo(a.getUnlockedDate());
                }
                return 0;
            }
            // Both locked, sort by progress (highest first)
            return Double.compare(b.getProgress(), a.getProgress());
        });
        
        // Create achievement cards
        for (Achievement achievement : achievements) {
            VBox achievementCard = createAchievementCard(achievement);
            achievementContainer.getChildren().add(achievementCard);
        }
    }
    
    /**
     * Creates a badge-style card for an achievement
     */
    private VBox createAchievementCard(Achievement achievement) {
        VBox card = new VBox(12);
        card.getStyleClass().add("achievement-card");
        
        // Add unlock state styling
        if (achievement.isUnlocked()) {
            card.getStyleClass().add("achievement-card-unlocked");
        } else if (achievement.getProgress() >= 0.75) {
            card.getStyleClass().add("achievement-card-almost-complete");
        } else {
            card.getStyleClass().add("achievement-card-locked");
        }
        
        // Header section with icon, title, and category badge
        HBox header = createAchievementHeader(achievement);
        
        // Description
        Label description = createAchievementDescription(achievement);
        
        // Progress section (only for locked achievements)
        VBox progressSection = null;
        if (!achievement.isUnlocked()) {
            progressSection = createProgressSection(achievement);
        } else {
            // Show unlock date for unlocked achievements
            Label unlockDate = createUnlockDateLabel(achievement);
            if (unlockDate != null) {
                progressSection = new VBox(unlockDate);
            }
        }
        
        // Add components to card
        card.getChildren().addAll(header, description);
        if (progressSection != null) {
            card.getChildren().add(progressSection);
        }
        
        // Add click handler for achievement details
        card.setOnMouseClicked(e -> onAchievementClicked(achievement));
        
        return card;
    }
    
    /**
     * Creates the header section with icon, title, and category badge
     */
    private HBox createAchievementHeader(Achievement achievement) {
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);
        header.getStyleClass().add("achievement-header");
        
        // Achievement icon (category icon)
        Label icon = new Label(achievement.getCategory().getIcon());
        icon.getStyleClass().add("achievement-icon");
        icon.setFont(Font.font(24));
        
        // Title and category badge container
        VBox titleContainer = new VBox(4);
        HBox.setHgrow(titleContainer, Priority.ALWAYS);
        
        // Achievement title
        Label title = new Label(achievement.getTitle());
        title.getStyleClass().add("achievement-title");
        if (achievement.isUnlocked()) {
            title.getStyleClass().add("achievement-title-unlocked");
        } else {
            title.getStyleClass().add("achievement-title-locked");
        }
        title.setFont(Font.font(null, FontWeight.BOLD, 16));
        
        // Category badge
        Label categoryBadge = new Label(achievement.getCategory().getDisplayText().toUpperCase());
        categoryBadge.getStyleClass().addAll("achievement-category-badge", 
            "achievement-category-badge-" + achievement.getCategory().name().toLowerCase());
        categoryBadge.setFont(Font.font(10));
        
        titleContainer.getChildren().addAll(title, categoryBadge);
        header.getChildren().addAll(icon, titleContainer);
        
        return header;
    }
    
    /**
     * Creates the achievement description label
     */
    private Label createAchievementDescription(Achievement achievement) {
        Label description = new Label(achievement.getDescription());
        description.getStyleClass().add("achievement-description");
        if (!achievement.isUnlocked()) {
            description.getStyleClass().add("achievement-description-locked");
        }
        description.setWrapText(true);
        description.setFont(Font.font(14));
        
        return description;
    }
    
    /**
     * Creates the progress section for locked achievements
     */
    private VBox createProgressSection(Achievement achievement) {
        VBox progressSection = new VBox(8);
        progressSection.getStyleClass().add("achievement-progress-section");
        
        // Progress bar and text container
        HBox progressContainer = new HBox(12);
        progressContainer.setAlignment(Pos.CENTER_LEFT);
        
        // Progress bar
        ProgressBar progressBar = new ProgressBar(achievement.getProgress());
        progressBar.getStyleClass().add("achievement-progress-bar");
        if (!achievement.isUnlocked()) {
            progressBar.getStyleClass().add("achievement-progress-bar-locked");
        }
        progressBar.setPrefHeight(8);
        HBox.setHgrow(progressBar, Priority.ALWAYS);
        
        // Progress text
        Label progressText = new Label(achievement.getProgressWithValues());
        progressText.getStyleClass().add("achievement-progress-text");
        if (!achievement.isUnlocked()) {
            progressText.getStyleClass().add("achievement-progress-text-locked");
        }
        progressText.setFont(Font.font(null, FontWeight.BOLD, 12));
        
        progressContainer.getChildren().addAll(progressBar, progressText);
        progressSection.getChildren().add(progressContainer);
        
        return progressSection;
    }
    
    /**
     * Creates unlock date label for unlocked achievements
     */
    private Label createUnlockDateLabel(Achievement achievement) {
        if (achievement.getUnlockedDate() == null) {
            return null;
        }
        
        String dateText = "Unlocked on " + 
            achievement.getUnlockedDate().format(DateTimeFormatter.ofPattern("MMM d, yyyy"));
        
        Label unlockDate = new Label(dateText);
        unlockDate.getStyleClass().add("achievement-unlock-date");
        unlockDate.setFont(Font.font(null, FontWeight.NORMAL, 12));
        
        return unlockDate;
    }
    
    /**
     * Shows celebration animation for recent unlocks
     */
    private void showCelebrationForRecentUnlocks() {
        if (viewModel == null) return;
        
        List<Achievement> recentUnlocks = viewModel.getRecentUnlocks();
        if (recentUnlocks.isEmpty()) return;
        
        // Show celebration for the first recent unlock
        Achievement achievement = recentUnlocks.get(0);
        showCelebration(achievement);
    }
    
    /**
     * Shows celebration overlay for a specific achievement
     */
    private void showCelebration(Achievement achievement) {
        // Update celebration content
        celebrationAchievementName.setText(achievement.getDisplayText());
        celebrationDescription.setText(achievement.getDescription());
        
        // Show overlay
        celebrationOverlay.setVisible(true);
        celebrationOverlay.setManaged(true);
        
        // Create celebration animation
        createCelebrationAnimation();
    }
    
    /**
     * Creates the celebration animation sequence
     */
    private void createCelebrationAnimation() {
        if (celebrationAnimation != null) {
            celebrationAnimation.stop();
        }
        
        // Scale and fade in animation
        ScaleTransition scaleIn = new ScaleTransition(Duration.millis(300), celebrationOverlay);
        scaleIn.setFromX(0.8);
        scaleIn.setFromY(0.8);
        scaleIn.setToX(1.0);
        scaleIn.setToY(1.0);
        
        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), celebrationOverlay);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        
        ParallelTransition showAnimation = new ParallelTransition(scaleIn, fadeIn);
        
        // Pulse animation for the title
        ScaleTransition pulse = new ScaleTransition(Duration.millis(600), celebrationTitle);
        pulse.setFromX(1.0);
        pulse.setFromY(1.0);
        pulse.setToX(1.1);
        pulse.setToY(1.1);
        pulse.setAutoReverse(true);
        pulse.setCycleCount(3);
        
        // Combine animations
        celebrationAnimation = new SequentialTransition(showAnimation, pulse);
        celebrationAnimation.play();
    }
    
    /**
     * Updates category button states based on current filter
     */
    private void updateCategoryButtonStates() {
        // Remove active state from all buttons
        allCategoryButton.getStyleClass().remove("category-button-active");
        consistencyCategoryButton.getStyleClass().remove("category-button-active");
        milestoneCategoryButton.getStyleClass().remove("category-button-active");
        evolutionCategoryButton.getStyleClass().remove("category-button-active");
        
        // Add active state to current filter
        if (currentFilter == null) {
            allCategoryButton.getStyleClass().add("category-button-active");
        } else {
            switch (currentFilter) {
                case CONSISTENCY -> consistencyCategoryButton.getStyleClass().add("category-button-active");
                case MILESTONE -> milestoneCategoryButton.getStyleClass().add("category-button-active");
                case EVOLUTION -> evolutionCategoryButton.getStyleClass().add("category-button-active");
            }
        }
    }
    
    // Event Handlers
    
    @FXML
    private void onAllCategoryClicked() {
        currentFilter = null;
        updateCategoryButtonStates();
        refreshAchievementDisplay();
    }
    
    @FXML
    private void onConsistencyCategoryClicked() {
        currentFilter = AchievementCategory.CONSISTENCY;
        updateCategoryButtonStates();
        refreshAchievementDisplay();
    }
    
    @FXML
    private void onMilestoneCategoryClicked() {
        currentFilter = AchievementCategory.MILESTONE;
        updateCategoryButtonStates();
        refreshAchievementDisplay();
    }
    
    @FXML
    private void onEvolutionCategoryClicked() {
        currentFilter = AchievementCategory.EVOLUTION;
        updateCategoryButtonStates();
        refreshAchievementDisplay();
    }
    
    @FXML
    private void onCelebrationClose() {
        // Hide celebration overlay
        celebrationOverlay.setVisible(false);
        celebrationOverlay.setManaged(false);
        
        // Stop animation if running
        if (celebrationAnimation != null) {
            celebrationAnimation.stop();
        }
        
        // Clear recent unlocks in view model
        if (viewModel != null) {
            viewModel.clearRecentUnlocks();
        }
        
        // Refresh display to show updated achievement states
        refreshAchievementDisplay();
    }
    
    private void onAchievementClicked(Achievement achievement) {
        // Could show detailed achievement information in a popup
        // For now, just refresh the display
        refreshAchievementDisplay();
    }
    
    // Navigation Event Handlers
    
    @FXML
    private void onPetTabClicked() {
        // Navigation will be handled by the main application controller
        System.out.println("Navigate to Pet tab");
    }
    
    @FXML
    private void onHabitsTabClicked() {
        // Navigation will be handled by the main application controller
        System.out.println("Navigate to Habits tab");
    }
    
    @FXML
    private void onStatsTabClicked() {
        // Navigation will be handled by the main application controller
        System.out.println("Navigate to Stats tab");
    }
    
    @FXML
    private void onAchievementsTabClicked() {
        // Already on achievements tab - refresh display
        refreshAchievementDisplay();
    }
    
    /**
     * Refreshes the entire view (called when data changes)
     */
    public void refresh() {
        refreshAchievementDisplay();
    }
    
    /**
     * Gets the current achievement filter
     */
    public AchievementCategory getCurrentFilter() {
        return currentFilter;
    }
    
    /**
     * Sets the achievement filter programmatically
     */
    public void setFilter(AchievementCategory filter) {
        this.currentFilter = filter;
        updateCategoryButtonStates();
        refreshAchievementDisplay();
    }
}