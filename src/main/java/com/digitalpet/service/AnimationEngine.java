package com.digitalpet.service;

import com.digitalpet.model.EvolutionStage;
import com.digitalpet.model.PetMood;
import javafx.animation.*;
import javafx.beans.property.*;
import javafx.scene.Node;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * JavaFX Timeline-based animation system for the Digital Pet Evolution application.
 * Provides sprite animation, particle effects, performance monitoring, and 60fps optimization.
 * 
 * Requirements addressed:
 * - 9.1: Smooth screen transitions with easing functions (300ms max)
 * - 9.2: Button hover effects and touch feedback (100ms response)
 * - 9.3: Continuous idle animation cycles based on mood and evolution stage
 * - 9.5: 60fps performance with reduced motion accessibility support
 */
public class AnimationEngine {
    
    // Performance monitoring
    private final AtomicLong frameCount = new AtomicLong(0);
    private final AtomicLong lastFpsCheck = new AtomicLong(System.currentTimeMillis());
    private final DoubleProperty currentFps = new SimpleDoubleProperty(60.0);
    private final BooleanProperty reducedMotionEnabled = new SimpleBooleanProperty(false);
    
    // Animation management
    private final Map<String, Timeline> activeAnimations = new ConcurrentHashMap<>();
    private final Map<String, SpriteAnimation> spriteAnimations = new ConcurrentHashMap<>();
    private final List<ParticleSystem> activeParticleSystems = new ArrayList<>();
    
    // Animation pools for performance
    private final Queue<Timeline> timelinePool = new LinkedList<>();
    private final Queue<FadeTransition> fadePool = new LinkedList<>();
    private final Queue<ScaleTransition> scalePool = new LinkedList<>();
    
    // Constants for performance optimization
    private static final int MAX_PARTICLES = 50;
    private static final double TARGET_FPS = 60.0;
    private static final Duration TRANSITION_DURATION = Duration.millis(300);
    private static final Duration FEEDBACK_DURATION = Duration.millis(100);
    
    /**
     * Initializes the animation engine with performance monitoring
     */
    public AnimationEngine() {
        setupPerformanceMonitoring();
        setupAnimationPools();
    }
    
    /**
     * Sets up FPS monitoring and performance optimization
     */
    private void setupPerformanceMonitoring() {
        Timeline fpsMonitor = new Timeline(new KeyFrame(Duration.seconds(1), e -> updateFpsCounter()));
        fpsMonitor.setCycleCount(Timeline.INDEFINITE);
        fpsMonitor.play();
    }
    
    /**
     * Initializes animation object pools for memory efficiency
     */
    private void setupAnimationPools() {
        // Pre-populate pools with commonly used animations
        for (int i = 0; i < 10; i++) {
            timelinePool.offer(new Timeline());
            fadePool.offer(new FadeTransition());
            scalePool.offer(new ScaleTransition());
        }
    }
    
    /**
     * Updates FPS counter and adjusts performance if needed
     */
    private void updateFpsCounter() {
        long currentTime = System.currentTimeMillis();
        long frames = frameCount.getAndSet(0);
        long timeDiff = currentTime - lastFpsCheck.getAndSet(currentTime);
        
        if (timeDiff > 0) {
            double fps = (frames * 1000.0) / timeDiff;
            currentFps.set(fps);
            
            // Adjust performance if FPS drops below target
            if (fps < TARGET_FPS * 0.8) {
                optimizePerformance();
            }
        }
    }
    
    /**
     * Optimizes performance by reducing particle count and animation complexity
     */
    private void optimizePerformance() {
        // Reduce particle systems
        activeParticleSystems.forEach(ParticleSystem::reduceParticleCount);
        
        // Pause non-essential animations
        activeAnimations.entrySet().removeIf(entry -> {
            if (entry.getKey().startsWith("particle_") || entry.getKey().startsWith("decoration_")) {
                entry.getValue().pause();
                return true;
            }
            return false;
        });
    }
    
    // Sprite Animation System
    
    /**
     * Creates a sprite animation for pet character
     * @param imageView The ImageView to animate
     * @param spriteSheet The sprite sheet image
     * @param frameWidth Width of each frame
     * @param frameHeight Height of each frame
     * @param frameCount Number of frames in the animation
     * @param duration Duration for complete animation cycle
     * @return SpriteAnimation instance
     */
    public SpriteAnimation createSpriteAnimation(ImageView imageView, Image spriteSheet, 
                                               int frameWidth, int frameHeight, 
                                               int frameCount, Duration duration) {
        SpriteAnimation sprite = new SpriteAnimation(imageView, spriteSheet, 
                                                   frameWidth, frameHeight, frameCount, duration);
        return sprite;
    }
    
    /**
     * Plays a sprite animation with specified loop behavior
     * @param animationId Unique identifier for the animation
     * @param sprite The sprite animation to play
     * @param loop Whether to loop the animation
     */
    public void playSpriteAnimation(String animationId, SpriteAnimation sprite, boolean loop) {
        stopAnimation(animationId);
        
        Timeline timeline = getTimelineFromPool();
        timeline.getKeyFrames().clear();
        
        Duration frameDuration = sprite.getDuration().divide(sprite.getFrameCount());
        
        for (int i = 0; i < sprite.getFrameCount(); i++) {
            final int frameIndex = i;
            KeyFrame keyFrame = new KeyFrame(frameDuration.multiply(i), 
                e -> sprite.showFrame(frameIndex));
            timeline.getKeyFrames().add(keyFrame);
        }
        
        timeline.setCycleCount(loop ? Timeline.INDEFINITE : 1);
        timeline.setOnFinished(e -> returnTimelineToPool(timeline));
        
        activeAnimations.put(animationId, timeline);
        timeline.play();
        
        frameCount.incrementAndGet();
    }
    
    // Particle Effect System
    
    /**
     * Creates a particle system for celebrations and mood effects
     * @param parentNode The node to attach particles to
     * @param particleCount Number of particles to create
     * @param mood The mood determining particle behavior
     * @return ParticleSystem instance
     */
    public ParticleSystem createParticleSystem(Node parentNode, int particleCount, PetMood mood) {
        // Ensure parent is a Pane
        if (!(parentNode instanceof javafx.scene.layout.Pane)) {
            throw new IllegalArgumentException("Parent node must be a Pane for particle system");
        }
        
        int adjustedCount = reducedMotionEnabled.get() ? 
            Math.max(1, Math.min(particleCount / 2, 10)) : Math.min(particleCount, MAX_PARTICLES);
            
        ParticleSystem particles = new ParticleSystem(parentNode, adjustedCount, mood);
        activeParticleSystems.add(particles);
        return particles;
    }
    
    /**
     * Starts a celebration particle effect
     * @param parentNode The node to attach particles to
     * @param duration Duration of the effect
     */
    public void startCelebrationEffect(Node parentNode, Duration duration) {
        if (reducedMotionEnabled.get()) {
            // Simple glow effect for reduced motion
            Glow glow = new Glow(0.8);
            parentNode.setEffect(glow);
            
            Timeline glowTimeline = getTimelineFromPool();
            glowTimeline.getKeyFrames().clear();
            glowTimeline.getKeyFrames().add(new KeyFrame(duration, e -> parentNode.setEffect(null)));
            glowTimeline.play();
            return;
        }
        
        // Only create particle system if parent is a Pane
        if (parentNode instanceof javafx.scene.layout.Pane) {
            ParticleSystem celebration = createParticleSystem(parentNode, 30, PetMood.CELEBRATING);
            celebration.start();
            
            Timeline stopTimer = getTimelineFromPool();
            stopTimer.getKeyFrames().clear();
            stopTimer.getKeyFrames().add(new KeyFrame(duration, e -> {
                celebration.stop();
                activeParticleSystems.remove(celebration);
            }));
            stopTimer.play();
        } else {
            // Fallback to glow effect if not a Pane
            Glow glow = new Glow(0.8);
            parentNode.setEffect(glow);
            
            Timeline glowTimeline = getTimelineFromPool();
            glowTimeline.getKeyFrames().clear();
            glowTimeline.getKeyFrames().add(new KeyFrame(duration, e -> parentNode.setEffect(null)));
            glowTimeline.play();
        }
    }
    
    // Interactive Response Animations
    
    /**
     * Creates a click/tap response animation with randomized effects
     * @param target The node that was clicked
     * @param mood Current pet mood affecting animation style
     */
    public void playInteractionResponse(Node target, PetMood mood) {
        String animationId = "interaction_" + System.currentTimeMillis();
        
        // Random selection of response animations
        Random random = new Random();
        int responseType = random.nextInt(3);
        
        switch (responseType) {
            case 0:
                playBounceAnimation(animationId, target, mood);
                break;
            case 1:
                playPulseAnimation(animationId, target, mood);
                break;
            case 2:
                playWiggleAnimation(animationId, target, mood);
                break;
        }
        
        frameCount.incrementAndGet();
    }
    
    /**
     * Creates a bounce animation for interaction response
     */
    private void playBounceAnimation(String animationId, Node target, PetMood mood) {
        ScaleTransition scale = getScaleTransitionFromPool();
        scale.setNode(target);
        scale.setDuration(FEEDBACK_DURATION);
        scale.setFromX(1.0);
        scale.setFromY(1.0);
        scale.setToX(1.2);
        scale.setToY(1.2);
        scale.setAutoReverse(true);
        scale.setCycleCount(2);
        
        scale.setOnFinished(e -> returnScaleTransitionToPool(scale));
        scale.play();
    }
    
    /**
     * Creates a pulse animation for interaction response
     */
    private void playPulseAnimation(String animationId, Node target, PetMood mood) {
        Timeline pulse = getTimelineFromPool();
        pulse.getKeyFrames().clear();
        
        DropShadow glow = new DropShadow();
        glow.setColor(mood.getEnvironmentTint());
        glow.setRadius(10);
        
        pulse.getKeyFrames().addAll(
            new KeyFrame(Duration.ZERO, new KeyValue(target.effectProperty(), null)),
            new KeyFrame(FEEDBACK_DURATION.divide(2), new KeyValue(target.effectProperty(), glow)),
            new KeyFrame(FEEDBACK_DURATION, new KeyValue(target.effectProperty(), null))
        );
        
        pulse.setOnFinished(e -> returnTimelineToPool(pulse));
        activeAnimations.put(animationId, pulse);
        pulse.play();
    }
    
    /**
     * Creates a wiggle animation for interaction response
     */
    private void playWiggleAnimation(String animationId, Node target, PetMood mood) {
        Timeline wiggle = getTimelineFromPool();
        wiggle.getKeyFrames().clear();
        
        double originalRotate = target.getRotate();
        
        wiggle.getKeyFrames().addAll(
            new KeyFrame(Duration.ZERO, new KeyValue(target.rotateProperty(), originalRotate)),
            new KeyFrame(FEEDBACK_DURATION.divide(4), new KeyValue(target.rotateProperty(), originalRotate + 5)),
            new KeyFrame(FEEDBACK_DURATION.divide(2), new KeyValue(target.rotateProperty(), originalRotate - 5)),
            new KeyFrame(FEEDBACK_DURATION.multiply(0.75), new KeyValue(target.rotateProperty(), originalRotate + 3)),
            new KeyFrame(FEEDBACK_DURATION, new KeyValue(target.rotateProperty(), originalRotate))
        );
        
        wiggle.setOnFinished(e -> returnTimelineToPool(wiggle));
        activeAnimations.put(animationId, wiggle);
        wiggle.play();
    }
    
    // Mood-based Idle Animations
    
    /**
     * Starts mood-based idle animation cycle
     * @param target The pet node to animate
     * @param mood Current pet mood
     * @param stage Current evolution stage
     */
    public void startIdleAnimation(Node target, PetMood mood, EvolutionStage stage) {
        stopAnimation("idle_animation");
        
        if (reducedMotionEnabled.get()) {
            return; // Skip idle animations for reduced motion
        }
        
        Timeline idle = createIdleAnimationForMood(target, mood, stage);
        activeAnimations.put("idle_animation", idle);
        idle.play();
    }
    
    /**
     * Creates idle animation based on mood and evolution stage
     */
    private Timeline createIdleAnimationForMood(Node target, PetMood mood, EvolutionStage stage) {
        Timeline idle = getTimelineFromPool();
        idle.getKeyFrames().clear();
        
        Duration cycleDuration = Duration.seconds(3 + stage.ordinal()); // Longer cycles for higher stages
        
        switch (mood) {
            case HAPPY:
                return createHappyIdleAnimation(target, cycleDuration);
            case SLEEPY:
                return createSleepyIdleAnimation(target, cycleDuration);
            case CELEBRATING:
                return createCelebratingIdleAnimation(target, cycleDuration);
            case WORRIED:
                return createWorriedIdleAnimation(target, cycleDuration);
            default:
                return createNeutralIdleAnimation(target, cycleDuration);
        }
    }
    
    private Timeline createHappyIdleAnimation(Node target, Duration duration) {
        Timeline happy = getTimelineFromPool();
        happy.getKeyFrames().clear();
        
        double originalY = target.getTranslateY();
        
        happy.getKeyFrames().addAll(
            new KeyFrame(Duration.ZERO, new KeyValue(target.translateYProperty(), originalY)),
            new KeyFrame(duration.divide(2), new KeyValue(target.translateYProperty(), originalY - 5)),
            new KeyFrame(duration, new KeyValue(target.translateYProperty(), originalY))
        );
        
        happy.setCycleCount(Timeline.INDEFINITE);
        return happy;
    }
    
    private Timeline createSleepyIdleAnimation(Node target, Duration duration) {
        Timeline sleepy = getTimelineFromPool();
        sleepy.getKeyFrames().clear();
        
        sleepy.getKeyFrames().addAll(
            new KeyFrame(Duration.ZERO, new KeyValue(target.scaleXProperty(), 1.0)),
            new KeyFrame(duration.divide(2), new KeyValue(target.scaleXProperty(), 0.95)),
            new KeyFrame(duration, new KeyValue(target.scaleXProperty(), 1.0))
        );
        
        sleepy.setCycleCount(Timeline.INDEFINITE);
        return sleepy;
    }
    
    private Timeline createCelebratingIdleAnimation(Node target, Duration duration) {
        Timeline celebrating = getTimelineFromPool();
        celebrating.getKeyFrames().clear();
        
        celebrating.getKeyFrames().addAll(
            new KeyFrame(Duration.ZERO, new KeyValue(target.rotateProperty(), 0)),
            new KeyFrame(duration.divide(4), new KeyValue(target.rotateProperty(), 2)),
            new KeyFrame(duration.divide(2), new KeyValue(target.rotateProperty(), -2)),
            new KeyFrame(duration.multiply(0.75), new KeyValue(target.rotateProperty(), 1)),
            new KeyFrame(duration, new KeyValue(target.rotateProperty(), 0))
        );
        
        celebrating.setCycleCount(Timeline.INDEFINITE);
        return celebrating;
    }
    
    private Timeline createWorriedIdleAnimation(Node target, Duration duration) {
        Timeline worried = getTimelineFromPool();
        worried.getKeyFrames().clear();
        
        worried.getKeyFrames().addAll(
            new KeyFrame(Duration.ZERO, new KeyValue(target.opacityProperty(), 1.0)),
            new KeyFrame(duration.divide(2), new KeyValue(target.opacityProperty(), 0.8)),
            new KeyFrame(duration, new KeyValue(target.opacityProperty(), 1.0))
        );
        
        worried.setCycleCount(Timeline.INDEFINITE);
        return worried;
    }
    
    private Timeline createNeutralIdleAnimation(Node target, Duration duration) {
        Timeline neutral = getTimelineFromPool();
        neutral.getKeyFrames().clear();
        
        double originalScale = target.getScaleX();
        
        neutral.getKeyFrames().addAll(
            new KeyFrame(Duration.ZERO, new KeyValue(target.scaleXProperty(), originalScale)),
            new KeyFrame(duration.divide(2), new KeyValue(target.scaleXProperty(), originalScale + 0.02)),
            new KeyFrame(duration, new KeyValue(target.scaleXProperty(), originalScale))
        );
        
        neutral.setCycleCount(Timeline.INDEFINITE);
        return neutral;
    }
    
    // Screen Transition Animations
    
    /**
     * Creates smooth screen transition with easing functions
     * @param fromNode The node to transition from
     * @param toNode The node to transition to
     * @param onComplete Callback when transition completes
     */
    public void createScreenTransition(Node fromNode, Node toNode, Runnable onComplete) {
        String transitionId = "screen_transition_" + System.currentTimeMillis();
        
        Duration duration = reducedMotionEnabled.get() ? 
            Duration.millis(100) : TRANSITION_DURATION;
        
        // Fade out current screen
        FadeTransition fadeOut = getFadeTransitionFromPool();
        fadeOut.setNode(fromNode);
        fadeOut.setDuration(duration.divide(2));
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        
        // Fade in new screen
        FadeTransition fadeIn = getFadeTransitionFromPool();
        fadeIn.setNode(toNode);
        fadeIn.setDuration(duration.divide(2));
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        
        SequentialTransition sequence = new SequentialTransition(fadeOut, fadeIn);
        sequence.setOnFinished(e -> {
            returnFadeTransitionToPool(fadeOut);
            returnFadeTransitionToPool(fadeIn);
            if (onComplete != null) {
                onComplete.run();
            }
        });
        
        sequence.play();
        frameCount.incrementAndGet();
    }
    
    // Animation Management
    
    /**
     * Stops a specific animation by ID
     * @param animationId The ID of the animation to stop
     */
    public void stopAnimation(String animationId) {
        Timeline animation = activeAnimations.remove(animationId);
        if (animation != null) {
            animation.stop();
            returnTimelineToPool(animation);
        }
    }
    
    /**
     * Stops all active animations
     */
    public void stopAllAnimations() {
        activeAnimations.values().forEach(Timeline::stop);
        activeAnimations.clear();
        activeParticleSystems.forEach(ParticleSystem::stop);
        activeParticleSystems.clear();
    }
    
    /**
     * Pauses all animations (for performance optimization)
     */
    public void pauseAllAnimations() {
        activeAnimations.values().forEach(Timeline::pause);
        activeParticleSystems.forEach(ParticleSystem::pause);
    }
    
    /**
     * Resumes all paused animations
     */
    public void resumeAllAnimations() {
        activeAnimations.values().forEach(Timeline::play);
        activeParticleSystems.forEach(ParticleSystem::resume);
    }
    
    // Object Pool Management
    
    private Timeline getTimelineFromPool() {
        Timeline timeline = timelinePool.poll();
        return timeline != null ? timeline : new Timeline();
    }
    
    private void returnTimelineToPool(Timeline timeline) {
        timeline.getKeyFrames().clear();
        timeline.setOnFinished(null);
        timeline.setCycleCount(1);
        if (timelinePool.size() < 20) {
            timelinePool.offer(timeline);
        }
    }
    
    private FadeTransition getFadeTransitionFromPool() {
        FadeTransition fade = fadePool.poll();
        return fade != null ? fade : new FadeTransition();
    }
    
    private void returnFadeTransitionToPool(FadeTransition fade) {
        fade.setNode(null);
        fade.setOnFinished(null);
        if (fadePool.size() < 20) {
            fadePool.offer(fade);
        }
    }
    
    private ScaleTransition getScaleTransitionFromPool() {
        ScaleTransition scale = scalePool.poll();
        return scale != null ? scale : new ScaleTransition();
    }
    
    private void returnScaleTransitionToPool(ScaleTransition scale) {
        scale.setNode(null);
        scale.setOnFinished(null);
        if (scalePool.size() < 20) {
            scalePool.offer(scale);
        }
    }
    
    // Property accessors for monitoring and configuration
    
    public DoubleProperty currentFpsProperty() {
        return currentFps;
    }
    
    public double getCurrentFps() {
        return currentFps.get();
    }
    
    public BooleanProperty reducedMotionEnabledProperty() {
        return reducedMotionEnabled;
    }
    
    public boolean isReducedMotionEnabled() {
        return reducedMotionEnabled.get();
    }
    
    public void setReducedMotionEnabled(boolean enabled) {
        this.reducedMotionEnabled.set(enabled);
        if (enabled) {
            // Simplify existing animations
            activeAnimations.entrySet().removeIf(entry -> {
                if (entry.getKey().startsWith("idle_") || entry.getKey().startsWith("particle_")) {
                    entry.getValue().stop();
                    return true;
                }
                return false;
            });
        }
    }
    
    /**
     * Gets the number of currently active animations
     * @return Number of active animations
     */
    public int getActiveAnimationCount() {
        return activeAnimations.size() + activeParticleSystems.size();
    }
    
    /**
     * Cleanup method to be called when shutting down the application
     */
    public void shutdown() {
        stopAllAnimations();
        timelinePool.clear();
        fadePool.clear();
        scalePool.clear();
    }
}