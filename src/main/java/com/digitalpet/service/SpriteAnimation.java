package com.digitalpet.service;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.util.Duration;

/**
 * Sprite animation support for pet character animations.
 * Handles sprite sheet parsing and frame-based animation display.
 */
public class SpriteAnimation {
    
    private final ImageView imageView;
    private final Image spriteSheet;
    private final int frameWidth;
    private final int frameHeight;
    private final int frameCount;
    private final Duration duration;
    private final WritableImage[] frames;
    
    /**
     * Creates a new sprite animation from a sprite sheet
     * @param imageView The ImageView to display frames in
     * @param spriteSheet The sprite sheet image containing all frames
     * @param frameWidth Width of each individual frame
     * @param frameHeight Height of each individual frame
     * @param frameCount Total number of frames in the animation
     * @param duration Total duration for one complete animation cycle
     */
    public SpriteAnimation(ImageView imageView, Image spriteSheet, 
                          int frameWidth, int frameHeight, 
                          int frameCount, Duration duration) {
        this.imageView = imageView;
        this.spriteSheet = spriteSheet;
        this.frameWidth = frameWidth;
        this.frameHeight = frameHeight;
        this.frameCount = frameCount;
        this.duration = duration;
        this.frames = new WritableImage[frameCount];
        
        extractFrames();
    }
    
    /**
     * Extracts individual frames from the sprite sheet
     */
    private void extractFrames() {
        PixelReader reader = spriteSheet.getPixelReader();
        int framesPerRow = (int) (spriteSheet.getWidth() / frameWidth);
        
        for (int i = 0; i < frameCount; i++) {
            int col = i % framesPerRow;
            int row = i / framesPerRow;
            
            int x = col * frameWidth;
            int y = row * frameHeight;
            
            frames[i] = new WritableImage(reader, x, y, frameWidth, frameHeight);
        }
    }
    
    /**
     * Shows a specific frame of the animation
     * @param frameIndex The frame index to display (0-based)
     */
    public void showFrame(int frameIndex) {
        if (frameIndex >= 0 && frameIndex < frameCount) {
            imageView.setImage(frames[frameIndex]);
        }
    }
    
    /**
     * Gets the total number of frames in this animation
     * @return Frame count
     */
    public int getFrameCount() {
        return frameCount;
    }
    
    /**
     * Gets the duration of the complete animation cycle
     * @return Animation duration
     */
    public Duration getDuration() {
        return duration;
    }
    
    /**
     * Gets the width of each frame
     * @return Frame width in pixels
     */
    public int getFrameWidth() {
        return frameWidth;
    }
    
    /**
     * Gets the height of each frame
     * @return Frame height in pixels
     */
    public int getFrameHeight() {
        return frameHeight;
    }
    
    /**
     * Gets the ImageView this animation is bound to
     * @return The target ImageView
     */
    public ImageView getImageView() {
        return imageView;
    }
}