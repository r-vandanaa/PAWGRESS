package com.digitalpet.service;

import com.digitalpet.model.Achievement;
import com.digitalpet.model.AchievementCategory;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for Achievement model.
 * Handles all database operations for achievement data persistence.
 */
public class AchievementDAO {
    
    private final LocalDataStorage storage;
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    
    public AchievementDAO(LocalDataStorage storage) {
        this.storage = storage;
    }
    
    /**
     * Saves an achievement to the database (insert or update)
     */
    public void save(Achievement achievement) throws SQLException {
        if (achievement == null) {
            throw new IllegalArgumentException("Achievement cannot be null");
        }
        
        Optional<Achievement> existing = findById(achievement.getId());
        if (existing.isPresent()) {
            update(achievement);
        } else {
            insert(achievement);
        }
    }
    
    /**
     * Inserts a new achievement into the database
     */
    private void insert(Achievement achievement) throws SQLException {
        String sql = """
            INSERT INTO achievements (id, title, description, category, target_value, 
                                    current_value, progress, is_unlocked, unlocked_date, 
                                    created_date, updated_date)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        
        String now = LocalDateTime.now().format(DATETIME_FORMATTER);
        
        try (PreparedStatement stmt = storage.getConnection().prepareStatement(sql)) {
            stmt.setString(1, achievement.getId());
            stmt.setString(2, achievement.getTitle());
            stmt.setString(3, achievement.getDescription());
            stmt.setString(4, achievement.getCategory().name());
            stmt.setInt(5, achievement.getTargetValue());
            stmt.setInt(6, achievement.getCurrentValue());
            stmt.setDouble(7, achievement.getProgress());
            stmt.setInt(8, achievement.isUnlocked() ? 1 : 0);
            
            if (achievement.getUnlockedDate() != null) {
                stmt.setString(9, achievement.getUnlockedDate().format(DATETIME_FORMATTER));
            } else {
                stmt.setNull(9, Types.VARCHAR);
            }
            
            stmt.setString(10, now);
            stmt.setString(11, now);
            
            stmt.executeUpdate();
        }
    }
    
    /**
     * Updates an existing achievement in the database
     */
    private void update(Achievement achievement) throws SQLException {
        String sql = """
            UPDATE achievements 
            SET title = ?, description = ?, category = ?, target_value = ?, 
                current_value = ?, progress = ?, is_unlocked = ?, unlocked_date = ?, 
                updated_date = ?
            WHERE id = ?
            """;
        
        try (PreparedStatement stmt = storage.getConnection().prepareStatement(sql)) {
            stmt.setString(1, achievement.getTitle());
            stmt.setString(2, achievement.getDescription());
            stmt.setString(3, achievement.getCategory().name());
            stmt.setInt(4, achievement.getTargetValue());
            stmt.setInt(5, achievement.getCurrentValue());
            stmt.setDouble(6, achievement.getProgress());
            stmt.setInt(7, achievement.isUnlocked() ? 1 : 0);
            
            if (achievement.getUnlockedDate() != null) {
                stmt.setString(8, achievement.getUnlockedDate().format(DATETIME_FORMATTER));
            } else {
                stmt.setNull(8, Types.VARCHAR);
            }
            
            stmt.setString(9, LocalDateTime.now().format(DATETIME_FORMATTER));
            stmt.setString(10, achievement.getId());
            
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Achievement not found for update: " + achievement.getId());
            }
        }
    }
    
    /**
     * Finds an achievement by ID
     */
    public Optional<Achievement> findById(String id) throws SQLException {
        if (id == null || id.trim().isEmpty()) {
            return Optional.empty();
        }
        
        String sql = """
            SELECT id, title, description, category, target_value, current_value, 
                   progress, is_unlocked, unlocked_date
            FROM achievements 
            WHERE id = ?
            """;
        
        try (PreparedStatement stmt = storage.getConnection().prepareStatement(sql)) {
            stmt.setString(1, id.trim());
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToAchievement(rs));
                }
            }
        }
        
        return Optional.empty();
    }
    
    /**
     * Finds all achievements by category
     */
    public List<Achievement> findByCategory(AchievementCategory category) throws SQLException {
        if (category == null) {
            return new ArrayList<>();
        }
        
        String sql = """
            SELECT id, title, description, category, target_value, current_value, 
                   progress, is_unlocked, unlocked_date
            FROM achievements 
            WHERE category = ?
            ORDER BY is_unlocked DESC, progress DESC, title ASC
            """;
        
        List<Achievement> achievements = new ArrayList<>();
        
        try (PreparedStatement stmt = storage.getConnection().prepareStatement(sql)) {
            stmt.setString(1, category.name());
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    achievements.add(mapResultSetToAchievement(rs));
                }
            }
        }
        
        return achievements;
    }
    
    /**
     * Finds all unlocked achievements
     */
    public List<Achievement> findUnlocked() throws SQLException {
        String sql = """
            SELECT id, title, description, category, target_value, current_value, 
                   progress, is_unlocked, unlocked_date
            FROM achievements 
            WHERE is_unlocked = 1
            ORDER BY unlocked_date DESC
            """;
        
        List<Achievement> achievements = new ArrayList<>();
        
        try (PreparedStatement stmt = storage.getConnection().prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                achievements.add(mapResultSetToAchievement(rs));
            }
        }
        
        return achievements;
    }
    
    /**
     * Finds all locked achievements
     */
    public List<Achievement> findLocked() throws SQLException {
        String sql = """
            SELECT id, title, description, category, target_value, current_value, 
                   progress, is_unlocked, unlocked_date
            FROM achievements 
            WHERE is_unlocked = 0
            ORDER BY progress DESC, title ASC
            """;
        
        List<Achievement> achievements = new ArrayList<>();
        
        try (PreparedStatement stmt = storage.getConnection().prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                achievements.add(mapResultSetToAchievement(rs));
            }
        }
        
        return achievements;
    }
    
    /**
     * Finds all achievements ordered by category and unlock status
     */
    public List<Achievement> findAll() throws SQLException {
        String sql = """
            SELECT id, title, description, category, target_value, current_value, 
                   progress, is_unlocked, unlocked_date
            FROM achievements 
            ORDER BY category ASC, is_unlocked DESC, progress DESC, title ASC
            """;
        
        List<Achievement> achievements = new ArrayList<>();
        
        try (PreparedStatement stmt = storage.getConnection().prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                achievements.add(mapResultSetToAchievement(rs));
            }
        }
        
        return achievements;
    }
    
    /**
     * Updates achievement progress and current value
     */
    public void updateProgress(String id, int currentValue, double progress) throws SQLException {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Achievement ID cannot be null or empty");
        }
        
        String sql = """
            UPDATE achievements 
            SET current_value = ?, progress = ?, updated_date = ?
            WHERE id = ?
            """;
        
        try (PreparedStatement stmt = storage.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, currentValue);
            stmt.setDouble(2, progress);
            stmt.setString(3, LocalDateTime.now().format(DATETIME_FORMATTER));
            stmt.setString(4, id.trim());
            
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Achievement not found for progress update: " + id);
            }
        }
    }
    
    /**
     * Unlocks an achievement
     */
    public void unlock(String id) throws SQLException {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Achievement ID cannot be null or empty");
        }
        
        String sql = """
            UPDATE achievements 
            SET is_unlocked = 1, unlocked_date = ?, updated_date = ?
            WHERE id = ? AND is_unlocked = 0
            """;
        
        String now = LocalDateTime.now().format(DATETIME_FORMATTER);
        
        try (PreparedStatement stmt = storage.getConnection().prepareStatement(sql)) {
            stmt.setString(1, now);
            stmt.setString(2, now);
            stmt.setString(3, id.trim());
            
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                // Achievement might already be unlocked or not exist
                System.out.println("Achievement not unlocked (already unlocked or not found): " + id);
            }
        }
    }
    
    /**
     * Deletes an achievement by ID
     */
    public boolean delete(String id) throws SQLException {
        if (id == null || id.trim().isEmpty()) {
            return false;
        }
        
        String sql = "DELETE FROM achievements WHERE id = ?";
        
        try (PreparedStatement stmt = storage.getConnection().prepareStatement(sql)) {
            stmt.setString(1, id.trim());
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        }
    }
    
    /**
     * Counts achievements by category
     */
    public int countByCategory(AchievementCategory category) throws SQLException {
        if (category == null) {
            return 0;
        }
        
        String sql = "SELECT COUNT(*) FROM achievements WHERE category = ?";
        
        try (PreparedStatement stmt = storage.getConnection().prepareStatement(sql)) {
            stmt.setString(1, category.name());
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        
        return 0;
    }
    
    /**
     * Counts unlocked achievements
     */
    public int countUnlocked() throws SQLException {
        String sql = "SELECT COUNT(*) FROM achievements WHERE is_unlocked = 1";
        
        try (PreparedStatement stmt = storage.getConnection().prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        
        return 0;
    }
    
    /**
     * Counts total achievements
     */
    public int count() throws SQLException {
        String sql = "SELECT COUNT(*) FROM achievements";
        
        try (PreparedStatement stmt = storage.getConnection().prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        
        return 0;
    }
    
    /**
     * Checks if an achievement exists
     */
    public boolean exists(String id) throws SQLException {
        if (id == null || id.trim().isEmpty()) {
            return false;
        }
        
        String sql = "SELECT 1 FROM achievements WHERE id = ? LIMIT 1";
        
        try (PreparedStatement stmt = storage.getConnection().prepareStatement(sql)) {
            stmt.setString(1, id.trim());
            
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }
    
    /**
     * Gets achievement statistics
     */
    public AchievementStats getStats() throws SQLException {
        String sql = """
            SELECT 
                COUNT(*) as total,
                SUM(CASE WHEN is_unlocked = 1 THEN 1 ELSE 0 END) as unlocked,
                AVG(progress) as avg_progress,
                COUNT(CASE WHEN category = 'CONSISTENCY' THEN 1 END) as consistency_count,
                COUNT(CASE WHEN category = 'MILESTONE' THEN 1 END) as milestone_count,
                COUNT(CASE WHEN category = 'EVOLUTION' THEN 1 END) as evolution_count
            FROM achievements
            """;
        
        try (PreparedStatement stmt = storage.getConnection().prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                return new AchievementStats(
                    rs.getInt("total"),
                    rs.getInt("unlocked"),
                    rs.getDouble("avg_progress"),
                    rs.getInt("consistency_count"),
                    rs.getInt("milestone_count"),
                    rs.getInt("evolution_count")
                );
            }
        }
        
        return new AchievementStats(0, 0, 0.0, 0, 0, 0);
    }
    
    /**
     * Maps a ResultSet row to an Achievement object
     * Note: This creates an Achievement without unlock conditions since those are functional
     */
    private Achievement mapResultSetToAchievement(ResultSet rs) throws SQLException {
        String id = rs.getString("id");
        String title = rs.getString("title");
        String description = rs.getString("description");
        
        String categoryStr = rs.getString("category");
        AchievementCategory category;
        try {
            category = AchievementCategory.valueOf(categoryStr);
        } catch (IllegalArgumentException e) {
            System.err.println("Invalid achievement category in database: " + categoryStr);
            category = AchievementCategory.MILESTONE;
        }
        
        int targetValue = rs.getInt("target_value");
        
        // Create achievement with null unlock condition (will need to be set by caller)
        Achievement achievement = new Achievement(id, title, description, category, targetValue, null);
        
        // Set current state
        achievement.setCurrentValue(rs.getInt("current_value"));
        achievement.setProgress(rs.getDouble("progress"));
        achievement.setUnlocked(rs.getInt("is_unlocked") == 1);
        
        // Set unlocked date if present
        String unlockedDateStr = rs.getString("unlocked_date");
        if (unlockedDateStr != null && !unlockedDateStr.isEmpty()) {
            try {
                LocalDateTime unlockedDate = LocalDateTime.parse(unlockedDateStr, DATETIME_FORMATTER);
                achievement.setUnlockedDate(unlockedDate);
            } catch (Exception e) {
                System.err.println("Invalid unlocked date in database: " + unlockedDateStr);
            }
        }
        
        return achievement;
    }
    
    /**
     * Record class for achievement statistics
     */
    public record AchievementStats(
        int totalAchievements,
        int unlockedAchievements,
        double averageProgress,
        int consistencyCount,
        int milestoneCount,
        int evolutionCount
    ) {
        public double getUnlockPercentage() {
            return totalAchievements > 0 ? (double) unlockedAchievements / totalAchievements * 100 : 0.0;
        }
    }
}