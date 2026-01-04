package com.digitalpet.service;

import com.digitalpet.model.DigitalPet;
import com.digitalpet.model.EvolutionStage;
import com.digitalpet.model.PetMood;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for DigitalPet model.
 * Handles all database operations for pet data persistence.
 */
public class DigitalPetDAO {
    
    private final LocalDataStorage storage;
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    
    public DigitalPetDAO(LocalDataStorage storage) {
        this.storage = storage;
    }
    
    /**
     * Saves a digital pet to the database (insert or update)
     */
    public void save(DigitalPet pet) throws SQLException {
        if (pet == null) {
            throw new IllegalArgumentException("Pet cannot be null");
        }
        
        Optional<DigitalPet> existing = findByName(pet.getName());
        if (existing.isPresent()) {
            update(pet);
        } else {
            insert(pet);
        }
    }
    
    /**
     * Inserts a new digital pet into the database
     */
    private void insert(DigitalPet pet) throws SQLException {
        String sql = """
            INSERT INTO digital_pet (name, current_stage, current_mood, experience_points, 
                                   energy_level, last_interaction, created_date, updated_date)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;
        
        String now = LocalDateTime.now().format(DATETIME_FORMATTER);
        
        try (PreparedStatement stmt = storage.getConnection().prepareStatement(sql)) {
            stmt.setString(1, pet.getName());
            stmt.setString(2, pet.getCurrentStage().name());
            stmt.setString(3, pet.getCurrentMood().name());
            stmt.setInt(4, pet.getExperiencePoints());
            stmt.setInt(5, pet.getEnergyLevel());
            stmt.setString(6, pet.getLastInteraction().format(DATETIME_FORMATTER));
            stmt.setString(7, now);
            stmt.setString(8, now);
            
            stmt.executeUpdate();
        }
    }
    
    /**
     * Updates an existing digital pet in the database
     */
    private void update(DigitalPet pet) throws SQLException {
        String sql = """
            UPDATE digital_pet 
            SET current_stage = ?, current_mood = ?, experience_points = ?, 
                energy_level = ?, last_interaction = ?, updated_date = ?
            WHERE name = ?
            """;
        
        try (PreparedStatement stmt = storage.getConnection().prepareStatement(sql)) {
            stmt.setString(1, pet.getCurrentStage().name());
            stmt.setString(2, pet.getCurrentMood().name());
            stmt.setInt(3, pet.getExperiencePoints());
            stmt.setInt(4, pet.getEnergyLevel());
            stmt.setString(5, pet.getLastInteraction().format(DATETIME_FORMATTER));
            stmt.setString(6, LocalDateTime.now().format(DATETIME_FORMATTER));
            stmt.setString(7, pet.getName());
            
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Pet not found for update: " + pet.getName());
            }
        }
    }
    
    /**
     * Finds a digital pet by name
     */
    public Optional<DigitalPet> findByName(String name) throws SQLException {
        if (name == null || name.trim().isEmpty()) {
            return Optional.empty();
        }
        
        String sql = """
            SELECT name, current_stage, current_mood, experience_points, 
                   energy_level, last_interaction
            FROM digital_pet 
            WHERE name = ?
            """;
        
        try (PreparedStatement stmt = storage.getConnection().prepareStatement(sql)) {
            stmt.setString(1, name.trim());
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToPet(rs));
                }
            }
        }
        
        return Optional.empty();
    }
    
    /**
     * Finds the most recently updated digital pet
     */
    public Optional<DigitalPet> findMostRecent() throws SQLException {
        String sql = """
            SELECT name, current_stage, current_mood, experience_points, 
                   energy_level, last_interaction
            FROM digital_pet 
            ORDER BY updated_date DESC 
            LIMIT 1
            """;
        
        try (PreparedStatement stmt = storage.getConnection().prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                return Optional.of(mapResultSetToPet(rs));
            }
        }
        
        return Optional.empty();
    }
    
    /**
     * Finds all digital pets in the database
     */
    public List<DigitalPet> findAll() throws SQLException {
        String sql = """
            SELECT name, current_stage, current_mood, experience_points, 
                   energy_level, last_interaction
            FROM digital_pet 
            ORDER BY updated_date DESC
            """;
        
        List<DigitalPet> pets = new ArrayList<>();
        
        try (PreparedStatement stmt = storage.getConnection().prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                pets.add(mapResultSetToPet(rs));
            }
        }
        
        return pets;
    }
    
    /**
     * Deletes a digital pet by name
     */
    public boolean delete(String name) throws SQLException {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        
        String sql = "DELETE FROM digital_pet WHERE name = ?";
        
        try (PreparedStatement stmt = storage.getConnection().prepareStatement(sql)) {
            stmt.setString(1, name.trim());
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        }
    }
    
    /**
     * Counts the total number of pets in the database
     */
    public int count() throws SQLException {
        String sql = "SELECT COUNT(*) FROM digital_pet";
        
        try (PreparedStatement stmt = storage.getConnection().prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        
        return 0;
    }
    
    /**
     * Checks if a pet with the given name exists
     */
    public boolean exists(String name) throws SQLException {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        
        String sql = "SELECT 1 FROM digital_pet WHERE name = ? LIMIT 1";
        
        try (PreparedStatement stmt = storage.getConnection().prepareStatement(sql)) {
            stmt.setString(1, name.trim());
            
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }
    
    /**
     * Updates only the XP and evolution stage of a pet (for performance)
     */
    public void updateXPAndStage(String name, int xp, EvolutionStage stage) throws SQLException {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Pet name cannot be null or empty");
        }
        
        String sql = """
            UPDATE digital_pet 
            SET experience_points = ?, current_stage = ?, updated_date = ?
            WHERE name = ?
            """;
        
        try (PreparedStatement stmt = storage.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, xp);
            stmt.setString(2, stage.name());
            stmt.setString(3, LocalDateTime.now().format(DATETIME_FORMATTER));
            stmt.setString(4, name.trim());
            
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Pet not found for XP update: " + name);
            }
        }
    }
    
    /**
     * Updates only the mood and energy of a pet (for performance)
     */
    public void updateMoodAndEnergy(String name, PetMood mood, int energy) throws SQLException {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Pet name cannot be null or empty");
        }
        
        String sql = """
            UPDATE digital_pet 
            SET current_mood = ?, energy_level = ?, updated_date = ?
            WHERE name = ?
            """;
        
        try (PreparedStatement stmt = storage.getConnection().prepareStatement(sql)) {
            stmt.setString(1, mood.name());
            stmt.setInt(2, energy);
            stmt.setString(3, LocalDateTime.now().format(DATETIME_FORMATTER));
            stmt.setString(4, name.trim());
            
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Pet not found for mood update: " + name);
            }
        }
    }
    
    /**
     * Records an interaction timestamp for a pet
     */
    public void recordInteraction(String name) throws SQLException {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Pet name cannot be null or empty");
        }
        
        String sql = """
            UPDATE digital_pet 
            SET last_interaction = ?, updated_date = ?
            WHERE name = ?
            """;
        
        String now = LocalDateTime.now().format(DATETIME_FORMATTER);
        
        try (PreparedStatement stmt = storage.getConnection().prepareStatement(sql)) {
            stmt.setString(1, now);
            stmt.setString(2, now);
            stmt.setString(3, name.trim());
            
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Pet not found for interaction update: " + name);
            }
        }
    }
    
    /**
     * Maps a ResultSet row to a DigitalPet object
     */
    private DigitalPet mapResultSetToPet(ResultSet rs) throws SQLException {
        String name = rs.getString("name");
        DigitalPet pet = new DigitalPet(name);
        
        // Set evolution stage
        String stageStr = rs.getString("current_stage");
        try {
            EvolutionStage stage = EvolutionStage.valueOf(stageStr);
            pet.setCurrentStage(stage);
        } catch (IllegalArgumentException e) {
            System.err.println("Invalid evolution stage in database: " + stageStr);
            pet.setCurrentStage(EvolutionStage.EGG);
        }
        
        // Set mood
        String moodStr = rs.getString("current_mood");
        try {
            PetMood mood = PetMood.valueOf(moodStr);
            pet.setCurrentMood(mood);
        } catch (IllegalArgumentException e) {
            System.err.println("Invalid mood in database: " + moodStr);
            pet.setCurrentMood(PetMood.NEUTRAL);
        }
        
        // Set numeric properties
        pet.setExperiencePoints(rs.getInt("experience_points"));
        pet.setEnergyLevel(rs.getInt("energy_level"));
        
        // Set last interaction
        String interactionStr = rs.getString("last_interaction");
        try {
            LocalDateTime interaction = LocalDateTime.parse(interactionStr, DATETIME_FORMATTER);
            pet.setLastInteraction(interaction);
        } catch (Exception e) {
            System.err.println("Invalid last interaction date in database: " + interactionStr);
            pet.setLastInteraction(LocalDateTime.now());
        }
        
        return pet;
    }
}