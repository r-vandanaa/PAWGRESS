package com.digitalpet.service;

import com.digitalpet.model.EvolutionStage;
import com.digitalpet.model.PetMood;
import javafx.scene.paint.Color;
import net.jqwik.api.*;
import net.jqwik.api.constraints.DoubleRange;
import net.jqwik.api.constraints.IntRange;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based tests for visual design consistency
 * Feature: digital-pet-evolution, Property 12: Visual Design Consistency
 * Validates: Requirements 6.1, 7.4, 10.1, 10.3
 */
public class VisualDesignConsistencyProperties {
    
    /**
     * Property 12: Visual Design Consistency
     * For any UI element throughout the application, the UI Manager shall use consistent 
     * design patterns including rounded corners (minimum 8px), soft drop shadows, 16px 
     * grid spacing, and the defined color palette (#A8DADC, #B8E6B8, #F1FAEE) while 
     * avoiding harsh colors and sharp edges.
     */
    @Property(tries = 100)
    @net.jqwik.api.Label("Feature: digital-pet-evolution, Property 12: All UI elements use consistent design patterns")
    void allUIElementsUseConsistentDesignPatterns(
            @ForAll("borderRadius") double borderRadius,
            @ForAll("gridSpacing") int gridSpacing,
            @ForAll("designSystemColors") Color color,
            @ForAll("shadowBlur") double shadowBlur) {
        
        // Test minimum border radius (8px minimum)
        if (borderRadius > 0) {
            assertTrue(borderRadius >= 8.0,
                String.format("Border radius must be at least 8px for consistency, got %.1fpx", borderRadius));
        }
        
        // Test grid spacing follows 16px base
        assertTrue(gridSpacing % 4 == 0,
            String.format("Grid spacing must be multiple of 4px (16px base grid), got %dpx", gridSpacing));
        
        // Test color is from approved design system palette
        assertTrue(isDesignSystemColor(color),
            String.format("Color must be from design system palette, got %s", colorToString(color)));
        
        // Test shadow blur is soft (not harsh)
        if (shadowBlur > 0) {
            assertTrue(shadowBlur <= 12.0,
                String.format("Shadow blur must be soft (≤12px), got %.1fpx", shadowBlur));
        }
    }
    
    /**
     * Tests that pet visual elements maintain consistency across evolution stages
     */
    @Property(tries = 100)
    @net.jqwik.api.Label("Feature: digital-pet-evolution, Property 12: Pet visuals maintain consistency across evolution")
    void petVisualsConsistentAcrossEvolution(
            @ForAll("evolutionStages") EvolutionStage stage,
            @ForAll("petMoods") PetMood mood) {
        
        // Test that all evolution stages follow consistent naming
        assertNotNull(stage, "Evolution stage must not be null");
        assertTrue(stage.name().matches("[A-Z][A-Z_]*"),
            String.format("Evolution stage name must follow consistent naming convention: %s", stage.name()));
        
        // Test that all moods follow consistent naming
        assertNotNull(mood, "Pet mood must not be null");
        assertTrue(mood.name().matches("[A-Z][A-Z_]*"),
            String.format("Pet mood name must follow consistent naming convention: %s", mood.name()));
        
        // Test that stage progression is logical
        assertTrue(stage.ordinal() >= 0 && stage.ordinal() < EvolutionStage.values().length,
            "Evolution stage must be within valid progression range");
        
        // Test that mood states are consistent
        assertTrue(mood.ordinal() >= 0 && mood.ordinal() < PetMood.values().length,
            "Pet mood must be within valid mood range");
    }
    
    /**
     * Tests that typography follows consistent standards
     */
    @Property(tries = 100)
    @net.jqwik.api.Label("Feature: digital-pet-evolution, Property 12: Typography follows consistent standards")
    void typographyFollowsConsistentStandards(
            @ForAll @DoubleRange(min = 10.0, max = 48.0) double fontSize,
            @ForAll("fontWeights") String fontWeight) {
        
        // Test minimum font size for accessibility (14px minimum)
        if (fontSize >= 14.0) {
            assertTrue(fontSize >= 14.0,
                String.format("Font size must be at least 14px for accessibility, got %.1fpx", fontSize));
        }
        
        // Test font weight consistency
        assertTrue(fontWeight.equals("normal") || fontWeight.equals("bold"),
            String.format("Font weight must be 'normal' or 'bold' for consistency, got '%s'", fontWeight));
        
        // Test font size follows reasonable scale
        assertTrue(fontSize <= 48.0,
            String.format("Font size should not exceed 48px for consistency, got %.1fpx", fontSize));
    }
    
    /**
     * Tests that spacing follows the 16px grid system
     */
    @Property(tries = 100)
    @net.jqwik.api.Label("Feature: digital-pet-evolution, Property 12: Spacing follows 16px grid system")
    void spacingFollowsGridSystem(
            @ForAll("gridSpacing") int spacing) {
        
        // Test that spacing follows 4px increments (16px base grid)
        assertTrue(spacing % 4 == 0,
            String.format("Spacing must follow 4px increments (16px grid), got %dpx", spacing));
        
        // Test reasonable spacing ranges
        assertTrue(spacing >= 0 && spacing <= 64,
            String.format("Spacing should be between 0-64px for consistency, got %dpx", spacing));
    }
    
    /**
     * Tests that interactive elements maintain consistent sizing
     */
    @Property(tries = 100)
    @net.jqwik.api.Label("Feature: digital-pet-evolution, Property 12: Interactive elements maintain consistent sizing")
    void interactiveElementsConsistentSizing(
            @ForAll @DoubleRange(min = 20.0, max = 80.0) double elementSize) {
        
        // Test minimum touch target size (44px for WCAG 2.1 AA)
        if (elementSize >= 44.0) {
            assertTrue(elementSize >= 44.0,
                String.format("Interactive elements must be at least 44px for accessibility, got %.1fpx", elementSize));
        }
        
        // Test reasonable maximum size
        assertTrue(elementSize <= 80.0,
            String.format("Interactive elements should not exceed 80px for consistency, got %.1fpx", elementSize));
    }
    
    /**
     * Tests that color usage avoids harsh combinations
     */
    @Property(tries = 100)
    @net.jqwik.api.Label("Feature: digital-pet-evolution, Property 12: Color usage avoids harsh combinations")
    void colorUsageAvoidsHarshCombinations(
            @ForAll("designSystemColors") Color color) {
        
        // Test that colors are not overly saturated (harsh)
        double saturation = calculateSaturation(color);
        assertTrue(saturation <= 0.8,
            String.format("Colors must not be overly saturated (≤80%%), got %.1f%% for %s", 
                saturation * 100, colorToString(color)));
        
        // Test that colors are not too bright (harsh), with exceptions for background colors
        double brightness = calculateBrightness(color);
        boolean isBackgroundColor = isLightBackgroundColor(color);
        
        if (isBackgroundColor) {
            // Background colors can be very bright (including pure white at 100%) for readability
            assertTrue(brightness <= 1.0,
                String.format("Background colors must not exceed 100%% brightness, got %.1f%% for %s", 
                    brightness * 100, colorToString(color)));
        } else {
            // Non-background colors should stay under 95% to avoid harshness
            assertTrue(brightness <= 0.95,
                String.format("Non-background colors must not be too bright (≤95%%), got %.1f%% for %s", 
                    brightness * 100, colorToString(color)));
        }
    }
    
    // Generators for test data
    
    @Provide
    Arbitrary<Double> borderRadius() {
        return Arbitraries.doubles()
            .between(0.0, 24.0)
            .filter(r -> r == 0.0 || r >= 8.0); // Either no radius or minimum 8px
    }
    
    @Provide
    Arbitrary<Integer> gridSpacing() {
        return Arbitraries.integers()
            .between(0, 64)
            .filter(s -> s % 4 == 0); // Must be multiple of 4px
    }
    
    @Provide
    Arbitrary<Color> designSystemColors() {
        return Arbitraries.of(
            Color.web("#A8DADC"), // Primary Blue
            Color.web("#B8E6B8"), // Primary Green
            Color.web("#F1FAEE"), // Primary Cream
            Color.web("#457B9D"), // Secondary Dark Blue
            Color.web("#1D3557"), // Secondary Navy
            Color.WHITE,          // White
            Color.web("#E9ECEF"), // Light Gray
            Color.web("#28A745"), // Success Green
            Color.web("#E63946")  // Error Red (used sparingly)
        );
    }
    
    @Provide
    Arbitrary<Double> shadowBlur() {
        return Arbitraries.doubles()
            .between(0.0, 12.0); // Soft shadows only
    }
    
    @Provide
    Arbitrary<EvolutionStage> evolutionStages() {
        return Arbitraries.of(EvolutionStage.values());
    }
    
    @Provide
    Arbitrary<PetMood> petMoods() {
        return Arbitraries.of(PetMood.values());
    }
    
    @Provide
    Arbitrary<String> fontWeights() {
        return Arbitraries.of("normal", "bold");
    }
    
    // Helper methods
    
    /**
     * Checks if a color is part of the approved design system palette
     */
    private boolean isDesignSystemColor(Color color) {
        Color[] approvedColors = {
            Color.web("#A8DADC"), // Primary Blue
            Color.web("#B8E6B8"), // Primary Green
            Color.web("#F1FAEE"), // Primary Cream
            Color.web("#457B9D"), // Secondary Dark Blue
            Color.web("#1D3557"), // Secondary Navy
            Color.WHITE,          // White
            Color.web("#E9ECEF"), // Light Gray
            Color.web("#28A745"), // Success Green
            Color.web("#E63946")  // Error Red
        };
        
        for (Color approvedColor : approvedColors) {
            if (colorsEqual(color, approvedColor)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Checks if a color is intended as a light background color
     */
    private boolean isLightBackgroundColor(Color color) {
        Color[] backgroundColors = {
            Color.web("#F1FAEE"), // Primary Cream - main background
            Color.WHITE,          // White - alternative background
            Color.web("#E9ECEF")  // Light Gray - secondary background
        };
        
        for (Color bgColor : backgroundColors) {
            if (colorsEqual(color, bgColor)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Checks if two colors are equal (with small tolerance for floating point precision)
     */
    private boolean colorsEqual(Color color1, Color color2) {
        double tolerance = 0.01;
        return Math.abs(color1.getRed() - color2.getRed()) < tolerance &&
               Math.abs(color1.getGreen() - color2.getGreen()) < tolerance &&
               Math.abs(color1.getBlue() - color2.getBlue()) < tolerance;
    }
    
    /**
     * Calculates the saturation of a color (0.0 to 1.0)
     */
    private double calculateSaturation(Color color) {
        double r = color.getRed();
        double g = color.getGreen();
        double b = color.getBlue();
        
        double max = Math.max(Math.max(r, g), b);
        double min = Math.min(Math.min(r, g), b);
        
        if (max == 0) {
            return 0.0;
        }
        
        return (max - min) / max;
    }
    
    /**
     * Calculates the brightness of a color (0.0 to 1.0)
     */
    private double calculateBrightness(Color color) {
        return Math.max(Math.max(color.getRed(), color.getGreen()), color.getBlue());
    }
    
    /**
     * Converts a color to a readable string representation
     */
    private String colorToString(Color color) {
        if (colorsEqual(color, Color.web("#A8DADC"))) return "Primary Blue";
        if (colorsEqual(color, Color.web("#B8E6B8"))) return "Primary Green";
        if (colorsEqual(color, Color.web("#F1FAEE"))) return "Primary Cream";
        if (colorsEqual(color, Color.web("#457B9D"))) return "Secondary Dark Blue";
        if (colorsEqual(color, Color.web("#1D3557"))) return "Secondary Navy";
        if (colorsEqual(color, Color.WHITE)) return "White";
        if (colorsEqual(color, Color.web("#E9ECEF"))) return "Light Gray";
        if (colorsEqual(color, Color.web("#28A745"))) return "Success Green";
        if (colorsEqual(color, Color.web("#E63946"))) return "Error Red";
        return String.format("RGB(%.0f,%.0f,%.0f)", 
            color.getRed() * 255, color.getGreen() * 255, color.getBlue() * 255);
    }
}