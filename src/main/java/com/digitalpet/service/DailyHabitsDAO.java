package com.digitalpet.service;

import com.digitalpet.model.DailyHabits;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for DailyHabits model.
 * Handles all database operations for daily habit data persistence.
 */
public class DailyHabitsDAO {
    
    private final LocalDataStorage storage;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    
    public DailyHabitsDAO(LocalDataStorage storage) {
        this.storage = storage;
    }
    
    /**
     * Saves daily habits to the database (insert or update)
     */
    public void save(DailyHabits habits) throws SQLException {
        if (habits == null) {
            throw new IllegalArgumentException("Habits cannot be null");
        }
        
        Optional<DailyHabits> existing = findByDate(habits.getDate());
        if (existing.isPresent()) {
            update(habits);
        } else {
            insert(habits);
        }
    }
    
    /**
     * Inserts new daily habits into the database
     */
    private void insert(DailyHabits habits) throws SQLException {
        String sql = """
            INSERT INTO daily_habits (date, study_hours, water_intake, steps_taken, 
                                    sleep_hours, money_spent, goals_completed, 
                                    completion_percentage, created_date, updated_date)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        
        String now = LocalDateTime.now().format(DATETIME_FORMATTER);
        
        try (PreparedStatement stmt = storage.getConnection().prepareStatement(sql)) {
            stmt.setString(1, habits.getDate().format(DATE_FORMATTER));
            stmt.setInt(2, habits.getStudyHours());
            stmt.setDouble(3, habits.getWaterIntake());
            stmt.setInt(4, habits.getStepsTaken());
            stmt.setDouble(5, habits.getSleepHours());
            stmt.setDouble(6, habits.getMoneySpent());
            stmt.setInt(7, habits.getGoalsCompleted());
            stmt.setDouble(8, habits.getCompletionPercentage());
            stmt.setString(9, now);
            stmt.setString(10, now);
            
            stmt.executeUpdate();
        }
    }
    
    /**
     * Updates existing daily habits in the database
     */
    private void update(DailyHabits habits) throws SQLException {
        String sql = """
            UPDATE daily_habits 
            SET study_hours = ?, water_intake = ?, steps_taken = ?, 
                sleep_hours = ?, money_spent = ?, goals_completed = ?, 
                completion_percentage = ?, updated_date = ?
            WHERE date = ?
            """;
        
        try (PreparedStatement stmt = storage.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, habits.getStudyHours());
            stmt.setDouble(2, habits.getWaterIntake());
            stmt.setInt(3, habits.getStepsTaken());
            stmt.setDouble(4, habits.getSleepHours());
            stmt.setDouble(5, habits.getMoneySpent());
            stmt.setInt(6, habits.getGoalsCompleted());
            stmt.setDouble(7, habits.getCompletionPercentage());
            stmt.setString(8, LocalDateTime.now().format(DATETIME_FORMATTER));
            stmt.setString(9, habits.getDate().format(DATE_FORMATTER));
            
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Habits not found for update: " + habits.getDate());
            }
        }
    }
    
    /**
     * Finds daily habits by date
     */
    public Optional<DailyHabits> findByDate(LocalDate date) throws SQLException {
        if (date == null) {
            return Optional.empty();
        }
        
        String sql = """
            SELECT date, study_hours, water_intake, steps_taken, 
                   sleep_hours, money_spent, goals_completed, completion_percentage
            FROM daily_habits 
            WHERE date = ?
            """;
        
        try (PreparedStatement stmt = storage.getConnection().prepareStatement(sql)) {
            stmt.setString(1, date.format(DATE_FORMATTER));
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToHabits(rs));
                }
            }
        }
        
        return Optional.empty();
    }
    
    /**
     * Finds daily habits for a date range (inclusive)
     */
    public List<DailyHabits> findByDateRange(LocalDate startDate, LocalDate endDate) throws SQLException {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Start and end dates cannot be null");
        }
        
        String sql = """
            SELECT date, study_hours, water_intake, steps_taken, 
                   sleep_hours, money_spent, goals_completed, completion_percentage
            FROM daily_habits 
            WHERE date BETWEEN ? AND ?
            ORDER BY date DESC
            """;
        
        List<DailyHabits> habitsList = new ArrayList<>();
        
        try (PreparedStatement stmt = storage.getConnection().prepareStatement(sql)) {
            stmt.setString(1, startDate.format(DATE_FORMATTER));
            stmt.setString(2, endDate.format(DATE_FORMATTER));
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    habitsList.add(mapResultSetToHabits(rs));
                }
            }
        }
        
        return habitsList;
    }
    
    /**
     * Finds the most recent N days of habits
     */
    public List<DailyHabits> findRecentDays(int days) throws SQLException {
        if (days <= 0) {
            return new ArrayList<>();
        }
        
        String sql = """
            SELECT date, study_hours, water_intake, steps_taken, 
                   sleep_hours, money_spent, goals_completed, completion_percentage
            FROM daily_habits 
            ORDER BY date DESC 
            LIMIT ?
            """;
        
        List<DailyHabits> habitsList = new ArrayList<>();
        
        try (PreparedStatement stmt = storage.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, days);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    habitsList.add(mapResultSetToHabits(rs));
                }
            }
        }
        
        return habitsList;
    }
    
    /**
     * Finds all daily habits ordered by date (most recent first)
     */
    public List<DailyHabits> findAll() throws SQLException {
        String sql = """
            SELECT date, study_hours, water_intake, steps_taken, 
                   sleep_hours, money_spent, goals_completed, completion_percentage
            FROM daily_habits 
            ORDER BY date DESC
            """;
        
        List<DailyHabits> habitsList = new ArrayList<>();
        
        try (PreparedStatement stmt = storage.getConnection().prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                habitsList.add(mapResultSetToHabits(rs));
            }
        }
        
        return habitsList;
    }
    
    /**
     * Deletes daily habits by date
     */
    public boolean delete(LocalDate date) throws SQLException {
        if (date == null) {
            return false;
        }
        
        String sql = "DELETE FROM daily_habits WHERE date = ?";
        
        try (PreparedStatement stmt = storage.getConnection().prepareStatement(sql)) {
            stmt.setString(1, date.format(DATE_FORMATTER));
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        }
    }
    
    /**
     * Deletes habits older than the specified date
     */
    public int deleteOlderThan(LocalDate cutoffDate) throws SQLException {
        if (cutoffDate == null) {
            return 0;
        }
        
        String sql = "DELETE FROM daily_habits WHERE date < ?";
        
        try (PreparedStatement stmt = storage.getConnection().prepareStatement(sql)) {
            stmt.setString(1, cutoffDate.format(DATE_FORMATTER));
            
            return stmt.executeUpdate();
        }
    }
    
    /**
     * Counts the total number of habit records
     */
    public int count() throws SQLException {
        String sql = "SELECT COUNT(*) FROM daily_habits";
        
        try (PreparedStatement stmt = storage.getConnection().prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        
        return 0;
    }
    
    /**
     * Checks if habits exist for a specific date
     */
    public boolean exists(LocalDate date) throws SQLException {
        if (date == null) {
            return false;
        }
        
        String sql = "SELECT 1 FROM daily_habits WHERE date = ? LIMIT 1";
        
        try (PreparedStatement stmt = storage.getConnection().prepareStatement(sql)) {
            stmt.setString(1, date.format(DATE_FORMATTER));
            
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }
    
    /**
     * Gets the date range of available habit data
     */
    public Optional<DateRange> getDateRange() throws SQLException {
        String sql = """
            SELECT MIN(date) as min_date, MAX(date) as max_date 
            FROM daily_habits
            """;
        
        try (PreparedStatement stmt = storage.getConnection().prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                String minDateStr = rs.getString("min_date");
                String maxDateStr = rs.getString("max_date");
                
                if (minDateStr != null && maxDateStr != null) {
                    LocalDate minDate = LocalDate.parse(minDateStr, DATE_FORMATTER);
                    LocalDate maxDate = LocalDate.parse(maxDateStr, DATE_FORMATTER);
                    return Optional.of(new DateRange(minDate, maxDate));
                }
            }
        }
        
        return Optional.empty();
    }
    
    /**
     * Calculates total values across all habit records
     */
    public HabitTotals calculateTotals() throws SQLException {
        String sql = """
            SELECT 
                SUM(study_hours) as total_study,
                SUM(water_intake) as total_water,
                SUM(steps_taken) as total_steps,
                SUM(sleep_hours) as total_sleep,
                SUM(money_spent) as total_money,
                SUM(goals_completed) as total_goals,
                AVG(completion_percentage) as avg_completion
            FROM daily_habits
            """;
        
        try (PreparedStatement stmt = storage.getConnection().prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                return new HabitTotals(
                    rs.getInt("total_study"),
                    rs.getDouble("total_water"),
                    rs.getInt("total_steps"),
                    rs.getDouble("total_sleep"),
                    rs.getDouble("total_money"),
                    rs.getInt("total_goals"),
                    rs.getDouble("avg_completion")
                );
            }
        }
        
        return new HabitTotals(0, 0.0, 0, 0.0, 0.0, 0, 0.0);
    }
    
    /**
     * Maps a ResultSet row to a DailyHabits object
     */
    private DailyHabits mapResultSetToHabits(ResultSet rs) throws SQLException {
        String dateStr = rs.getString("date");
        LocalDate date = LocalDate.parse(dateStr, DATE_FORMATTER);
        
        DailyHabits habits = new DailyHabits(date);
        habits.setStudyHours(rs.getInt("study_hours"));
        habits.setWaterIntake(rs.getDouble("water_intake"));
        habits.setStepsTaken(rs.getInt("steps_taken"));
        habits.setSleepHours(rs.getDouble("sleep_hours"));
        habits.setMoneySpent(rs.getDouble("money_spent"));
        habits.setGoalsCompleted(rs.getInt("goals_completed"));
        
        return habits;
    }
    
    /**
     * Record class for date ranges
     */
    public record DateRange(LocalDate startDate, LocalDate endDate) {}
    
    /**
     * Record class for habit totals
     */
    public record HabitTotals(
        int totalStudyHours,
        double totalWaterLiters,
        int totalSteps,
        double totalSleepHours,
        double totalMoneySpent,
        int totalGoalsCompleted,
        double averageCompletion
    ) {}
}