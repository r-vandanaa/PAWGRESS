package com.digitalpet.service;

import com.digitalpet.model.PetMood;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Particle effect system for celebrations and mood-based environmental effects.
 * Creates and manages animated particles with physics-based movement.
 */
public class ParticleSystem {
    
    private final Pane parentPane;
    private final List<Particle> particles;
    private final PetMood mood;
    private final Random random = new Random();
    private Timeline animationTimeline;
    private boolean isActive = false;
    
    /**
     * Creates a new particle system
     * @param parentNode The node to attach particles to (must be a Pane)
     * @param particleCount Number of particles to create
     * @param mood The mood determining particle behavior and appearance
     */
    public ParticleSystem(Node parentNode, int particleCount, PetMood mood) {
        if (!(parentNode instanceof Pane)) {
            throw new IllegalArgumentException("Parent node must be a Pane for particle system");
        }
        
        this.parentPane = (Pane) parentNode;
        this.mood = mood;
        this.particles = new ArrayList<>();
        
        createParticles(particleCount);
        setupAnimation();
    }
    
    /**
     * Creates particles based on mood and count
     */
    private void createParticles(int count) {
        for (int i = 0; i < count; i++) {
            Particle particle = createParticleForMood();
            particles.add(particle);
            parentPane.getChildren().add(particle.getNode());
        }
    }
    
    /**
     * Creates a particle appropriate for the current mood
     */
    private Particle createParticleForMood() {
        switch (mood) {
            case HAPPY:
                return createSparkleParticle();
            case CELEBRATING:
                return createConfettiParticle();
            case SLEEPY:
                return createFloatingParticle();
            case WORRIED:
                return createSubtleParticle();
            default:
                return createNeutralParticle();
        }
    }
    
    /**
     * Creates a sparkle particle for happy mood
     */
    private Particle createSparkleParticle() {
        Circle sparkle = new Circle(2 + random.nextDouble() * 3);
        sparkle.setFill(Color.GOLD.deriveColor(0, 1, 1, 0.7));
        
        double x = random.nextDouble() * parentPane.getWidth();
        double y = random.nextDouble() * parentPane.getHeight();
        double vx = (random.nextDouble() - 0.5) * 20;
        double vy = (random.nextDouble() - 0.5) * 20;
        
        return new Particle(sparkle, x, y, vx, vy, Duration.seconds(2 + random.nextDouble() * 3));
    }
    
    /**
     * Creates a confetti particle for celebrating mood
     */
    private Particle createConfettiParticle() {
        Rectangle confetti = new Rectangle(4 + random.nextDouble() * 4, 8 + random.nextDouble() * 8);
        Color[] colors = {Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW, Color.PURPLE, Color.ORANGE};
        confetti.setFill(colors[random.nextInt(colors.length)].deriveColor(0, 1, 1, 0.8));
        
        double x = random.nextDouble() * parentPane.getWidth();
        double y = -20; // Start above screen
        double vx = (random.nextDouble() - 0.5) * 40;
        double vy = 30 + random.nextDouble() * 50; // Fall down
        
        return new Particle(confetti, x, y, vx, vy, Duration.seconds(3 + random.nextDouble() * 2));
    }
    
    /**
     * Creates a floating particle for sleepy mood
     */
    private Particle createFloatingParticle() {
        Circle bubble = new Circle(3 + random.nextDouble() * 2);
        bubble.setFill(Color.LIGHTBLUE.deriveColor(0, 1, 1, 0.4));
        
        double x = random.nextDouble() * parentPane.getWidth();
        double y = parentPane.getHeight() + 20; // Start below screen
        double vx = (random.nextDouble() - 0.5) * 10;
        double vy = -15 - random.nextDouble() * 10; // Float up slowly
        
        return new Particle(bubble, x, y, vx, vy, Duration.seconds(4 + random.nextDouble() * 3));
    }
    
    /**
     * Creates a subtle particle for worried mood
     */
    private Particle createSubtleParticle() {
        Circle dot = new Circle(1 + random.nextDouble() * 2);
        dot.setFill(Color.LIGHTCORAL.deriveColor(0, 1, 1, 0.3));
        
        double x = random.nextDouble() * parentPane.getWidth();
        double y = random.nextDouble() * parentPane.getHeight();
        double vx = (random.nextDouble() - 0.5) * 5;
        double vy = (random.nextDouble() - 0.5) * 5;
        
        return new Particle(dot, x, y, vx, vy, Duration.seconds(5 + random.nextDouble() * 2));
    }
    
    /**
     * Creates a neutral particle for default mood
     */
    private Particle createNeutralParticle() {
        Circle dot = new Circle(2);
        dot.setFill(Color.LIGHTBLUE.deriveColor(0, 1, 1, 0.5));
        
        double x = random.nextDouble() * parentPane.getWidth();
        double y = random.nextDouble() * parentPane.getHeight();
        double vx = (random.nextDouble() - 0.5) * 15;
        double vy = (random.nextDouble() - 0.5) * 15;
        
        return new Particle(dot, x, y, vx, vy, Duration.seconds(3 + random.nextDouble() * 2));
    }
    
    /**
     * Sets up the animation timeline for particle movement
     */
    private void setupAnimation() {
        animationTimeline = new Timeline(new KeyFrame(Duration.millis(16), e -> updateParticles()));
        animationTimeline.setCycleCount(Timeline.INDEFINITE);
    }
    
    /**
     * Updates all particle positions and handles lifecycle
     */
    private void updateParticles() {
        particles.removeIf(particle -> {
            particle.update();
            
            // Remove particles that are out of bounds or expired
            if (particle.isExpired() || isOutOfBounds(particle)) {
                parentPane.getChildren().remove(particle.getNode());
                return true;
            }
            
            return false;
        });
        
        // Stop animation if no particles remain
        if (particles.isEmpty() && isActive) {
            stop();
        }
    }
    
    /**
     * Checks if a particle is out of the visible bounds
     */
    private boolean isOutOfBounds(Particle particle) {
        double x = particle.getX();
        double y = particle.getY();
        double margin = 50; // Allow some margin for smooth exit
        
        return x < -margin || x > parentPane.getWidth() + margin ||
               y < -margin || y > parentPane.getHeight() + margin;
    }
    
    /**
     * Starts the particle system animation
     */
    public void start() {
        if (!isActive) {
            isActive = true;
            animationTimeline.play();
        }
    }
    
    /**
     * Stops the particle system and removes all particles
     */
    public void stop() {
        if (isActive) {
            isActive = false;
            animationTimeline.stop();
            
            // Remove all remaining particles
            particles.forEach(particle -> parentPane.getChildren().remove(particle.getNode()));
            particles.clear();
        }
    }
    
    /**
     * Pauses the particle system animation
     */
    public void pause() {
        if (isActive) {
            animationTimeline.pause();
        }
    }
    
    /**
     * Resumes the particle system animation
     */
    public void resume() {
        if (isActive) {
            animationTimeline.play();
        }
    }
    
    /**
     * Reduces particle count for performance optimization
     */
    public void reduceParticleCount() {
        int targetCount = Math.max(1, particles.size() / 2);
        
        while (particles.size() > targetCount) {
            Particle particle = particles.remove(particles.size() - 1);
            parentPane.getChildren().remove(particle.getNode());
        }
    }
    
    /**
     * Gets the current number of active particles
     */
    public int getParticleCount() {
        return particles.size();
    }
    
    /**
     * Checks if the particle system is currently active
     */
    public boolean isActive() {
        return isActive;
    }
    
    /**
     * Individual particle class for managing particle state and behavior
     */
    private static class Particle {
        private final Node node;
        private double x, y;
        private double velocityX, velocityY;
        private final Duration lifetime;
        private final long startTime;
        
        public Particle(Node node, double x, double y, double vx, double vy, Duration lifetime) {
            this.node = node;
            this.x = x;
            this.y = y;
            this.velocityX = vx;
            this.velocityY = vy;
            this.lifetime = lifetime;
            this.startTime = System.currentTimeMillis();
            
            updateNodePosition();
        }
        
        /**
         * Updates particle position and applies physics
         */
        public void update() {
            // Apply gravity for confetti-like particles
            if (node instanceof Rectangle) {
                velocityY += 0.5; // Gravity
                velocityX *= 0.99; // Air resistance
            }
            
            // Update position
            x += velocityX * 0.016; // Assuming 60fps (16ms per frame)
            y += velocityY * 0.016;
            
            updateNodePosition();
            updateOpacity();
        }
        
        /**
         * Updates the visual node position
         */
        private void updateNodePosition() {
            node.setTranslateX(x);
            node.setTranslateY(y);
        }
        
        /**
         * Updates particle opacity based on lifetime
         */
        private void updateOpacity() {
            long elapsed = System.currentTimeMillis() - startTime;
            double progress = elapsed / lifetime.toMillis();
            
            if (progress > 0.7) {
                // Fade out in the last 30% of lifetime
                double fadeProgress = (progress - 0.7) / 0.3;
                double opacity = 1.0 - fadeProgress;
                node.setOpacity(Math.max(0, opacity));
            }
        }
        
        /**
         * Checks if the particle has expired
         */
        public boolean isExpired() {
            long elapsed = System.currentTimeMillis() - startTime;
            return elapsed >= lifetime.toMillis();
        }
        
        public Node getNode() { return node; }
        public double getX() { return x; }
        public double getY() { return y; }
    }
}