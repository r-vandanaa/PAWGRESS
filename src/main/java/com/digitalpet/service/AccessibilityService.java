package com.digitalpet.service;

import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Slider;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

/**
 * Service for ensuring WCAG 2.1 AA accessibility compliance
 * Requirements: 6.5, 10.2, 10.5, 9.5
 */
public class AccessibilityService {
    
    // WCAG 2.1 AA minimum contrast ratio
    private static final double MIN_CONTRAST_RATIO = 4.5;
    
    // WCAG 2.1 AA minimum touch target size
    private static final double MIN_TOUCH_TARGET_SIZE = 44.0;
    
    // Minimum font size for accessibility
    private static final double MIN_FONT_SIZE = 14.0;
    
    /**
     * Validates color contrast ratio meets WCAG 2.1 AA standards
     * @param foreground Foreground color
     * @param background Background color
     * @return true if contrast ratio meets minimum requirements
     */
    public static boolean validateColorContrast(Color foreground, Color background) {
        double contrastRatio = calculateContrastRatio(foreground, background);
        return contrastRatio >= MIN_CONTRAST_RATIO;
    }
    
    /**
     * Calculates contrast ratio between two colors
     * @param color1 First color
     * @param color2 Second color
     * @return Contrast ratio (1:1 to 21:1)
     */
    public static double calculateContrastRatio(Color color1, Color color2) {
        double luminance1 = calculateRelativeLuminance(color1);
        double luminance2 = calculateRelativeLuminance(color2);
        
        double lighter = Math.max(luminance1, luminance2);
        double darker = Math.min(luminance1, luminance2);
        
        return (lighter + 0.05) / (darker + 0.05);
    }
    
    /**
     * Calculates relative luminance of a color
     * @param color Color to calculate luminance for
     * @return Relative luminance (0.0 to 1.0)
     */
    private static double calculateRelativeLuminance(Color color) {
        double r = color.getRed();
        double g = color.getGreen();
        double b = color.getBlue();
        
        // Convert to linear RGB
        r = (r <= 0.03928) ? r / 12.92 : Math.pow((r + 0.055) / 1.055, 2.4);
        g = (g <= 0.03928) ? g / 12.92 : Math.pow((g + 0.055) / 1.055, 2.4);
        b = (b <= 0.03928) ? b / 12.92 : Math.pow((b + 0.055) / 1.055, 2.4);
        
        // Calculate luminance
        return 0.2126 * r + 0.7152 * g + 0.0722 * b;
    }
    
    /**
     * Ensures interactive element meets minimum touch target size
     * @param node Interactive node to validate
     * @return true if meets minimum size requirements
     */
    public static boolean validateTouchTargetSize(Node node) {
        return node.getBoundsInLocal().getWidth() >= MIN_TOUCH_TARGET_SIZE &&
               node.getBoundsInLocal().getHeight() >= MIN_TOUCH_TARGET_SIZE;
    }
    
    /**
     * Applies accessibility properties to a button
     * @param button Button to enhance
     * @param accessibleText Alternative text description
     */
    public static void enhanceButtonAccessibility(Button button, String accessibleText) {
        // Set accessible text
        button.setAccessibleText(accessibleText);
        
        // Ensure minimum size
        if (button.getMinHeight() < MIN_TOUCH_TARGET_SIZE) {
            button.setMinHeight(MIN_TOUCH_TARGET_SIZE);
            button.setPrefHeight(MIN_TOUCH_TARGET_SIZE);
        }
        
        // Add focus traversable
        button.setFocusTraversable(true);
        
        // Add accessible role
        button.setAccessibleRole(javafx.scene.AccessibleRole.BUTTON);
    }
    
    /**
     * Applies accessibility properties to a slider
     * @param slider Slider to enhance
     * @param accessibleText Alternative text description
     * @param minValue Minimum value for screen readers
     * @param maxValue Maximum value for screen readers
     */
    public static void enhanceSliderAccessibility(Slider slider, String accessibleText, 
                                                double minValue, double maxValue) {
        // Set accessible text
        slider.setAccessibleText(accessibleText);
        
        // Ensure minimum height for touch targets
        if (slider.getMinHeight() < MIN_TOUCH_TARGET_SIZE) {
            slider.setMinHeight(MIN_TOUCH_TARGET_SIZE);
        }
        
        // Add focus traversable
        slider.setFocusTraversable(true);
        
        // Set accessible role and help text
        slider.setAccessibleRole(javafx.scene.AccessibleRole.SLIDER);
        slider.setAccessibleHelp(String.format("Slider from %.1f to %.1f", minValue, maxValue));
    }
    
    /**
     * Applies accessibility properties to a progress bar
     * @param progressBar Progress bar to enhance
     * @param accessibleText Alternative text description
     */
    public static void enhanceProgressBarAccessibility(ProgressBar progressBar, String accessibleText) {
        // Set accessible text
        progressBar.setAccessibleText(accessibleText);
        
        // Set accessible role
        progressBar.setAccessibleRole(javafx.scene.AccessibleRole.PROGRESS_INDICATOR);
        
        // Add value description
        progressBar.progressProperty().addListener((obs, oldVal, newVal) -> {
            int percentage = (int) (newVal.doubleValue() * 100);
            progressBar.setAccessibleHelp(String.format("%s: %d%% complete", accessibleText, percentage));
        });
    }
    
    /**
     * Applies accessibility properties to a label
     * @param label Label to enhance
     * @param accessibleText Alternative text description
     */
    public static void enhanceLabelAccessibility(Label label, String accessibleText) {
        // Set accessible text
        label.setAccessibleText(accessibleText);
        
        // Set accessible role
        label.setAccessibleRole(javafx.scene.AccessibleRole.TEXT);
        
        // Ensure minimum font size
        if (label.getFont().getSize() < MIN_FONT_SIZE) {
            label.setStyle(label.getStyle() + "; -fx-font-size: " + MIN_FONT_SIZE + "px;");
        }
    }
    
    /**
     * Applies accessibility properties to text elements
     * @param text Text element to enhance
     * @param accessibleText Alternative text description
     */
    public static void enhanceTextAccessibility(Text text, String accessibleText) {
        // Set accessible text
        text.setAccessibleText(accessibleText);
        
        // Set accessible role
        text.setAccessibleRole(javafx.scene.AccessibleRole.TEXT);
        
        // Ensure minimum font size
        if (text.getFont().getSize() < MIN_FONT_SIZE) {
            text.setStyle(text.getStyle() + "; -fx-font-size: " + MIN_FONT_SIZE + "px;");
        }
    }
    
    /**
     * Checks if reduced motion is preferred (system setting)
     * @return true if reduced motion should be used
     */
    public static boolean isReducedMotionPreferred() {
        // In a real implementation, this would check system accessibility settings
        // For now, we'll return false but the CSS media queries handle this
        return Boolean.parseBoolean(System.getProperty("javafx.accessibility.reducedMotion", "false"));
    }
    
    /**
     * Checks if high contrast mode is preferred (system setting)
     * @return true if high contrast should be used
     */
    public static boolean isHighContrastPreferred() {
        // In a real implementation, this would check system accessibility settings
        // For now, we'll return false but the CSS media queries handle this
        return Boolean.parseBoolean(System.getProperty("javafx.accessibility.highContrast", "false"));
    }
    
    /**
     * Validates that all color combinations in the design system meet WCAG standards
     * @return true if all combinations are compliant
     */
    public static boolean validateDesignSystemContrast() {
        // Define design system colors
        Color primaryBlue = Color.web("#A8DADC");
        Color primaryGreen = Color.web("#B8E6B8");
        Color primaryCream = Color.web("#F1FAEE");
        Color secondaryNavy = Color.web("#1D3557");
        Color white = Color.WHITE;
        
        // Test critical text/background combinations that are actually used
        boolean navyOnCream = validateColorContrast(secondaryNavy, primaryCream);
        boolean whiteOnNavy = validateColorContrast(white, secondaryNavy);
        boolean navyOnGreen = validateColorContrast(secondaryNavy, primaryGreen);
        boolean navyOnBlue = validateColorContrast(secondaryNavy, primaryBlue);
        
        return navyOnCream && whiteOnNavy && navyOnGreen && navyOnBlue;
    }
}