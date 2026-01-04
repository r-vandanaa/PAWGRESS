package com.digitalpet.service;

import com.digitalpet.model.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Local data storage service using embedded SQLite database.
 * Provides data persistence for all application models with automatic backup and integrity validation.
 * 
 * Requirements addressed:
 * - Non-functional requirements (data reliability)
 * - Automatic backup and data integrity validation
 * - Embedded SQLite database for habit history
 */
public class LocalDataStorage {
    
    private static final String DATABASE_NAME = "digitalpet.db";
    private static final String BACKUP_SUFFIX = "_backup";
    private static final String DATA_DIRECTORY = "data";
    
    private Connection connection;
    private final ScheduledExecutorService backupScheduler;
    private final Path dataDirectory;
    private final Path databasePath;
    private final Path backupPath;
    
    /**
     * Creates a new LocalDataStorage instance and initializes the database
     */
    public LocalDataStorage() {
        this.backupScheduler = Executors.newSingleThreadScheduledExecutor();
        this.dataDirectory = createDataDirectory();
        this.databasePath = dataDirectory.resolve(DATABASE_NAME);
        this.backupPath = dataDirectory.resolve(DATABASE_NAME + BACKUP_SUFFIX);
        
        initializeDatabase();
        scheduleAutomaticBackup();
    }
    
    /**
     * Creates the data directory if it doesn't exist
     */
    private Path createDataDirectory() {
        try {
            Path dir = Paths.get(System.getProperty("user.home"), ".digitalpet", DATA_DIRECTORY);
            Files.createDirectories(dir);
            return dir;
        } catch (IOException e) {
            // Fallback to current directory
            Path fallback = Paths.get(DATA_DIRECTORY);
            try {
                Files.createDirectories(fallback);
                return fallback;
            } catch (IOException ex) {
                throw new RuntimeException("Failed to create data directory", ex);
            }
        }
    }
    
    /**
     * Initializes the SQLite database and creates tables if they don't exist
     */
    private void initializeDatabase() {
        try {
            // Create database connection
            String url = "jdbc:sqlite:" + databasePath.toString();
            connection = DriverManager.getConnection(url);
            
            // Enable foreign keys
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("PRAGMA foreign_keys = ON");
                stmt.execute("PRAGMA journal_mode = WAL"); // Better concurrency
            }
            
            createTables();
            validateDataIntegrity();
            
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize database", e);
        }
    }
    
    /**
     * Creates all necessary database tables
     */
    private void createTables() throws SQLException {
        String[] createTableStatements = {
            // Digital Pet table
            """
            CREATE TABLE IF NOT EXISTS digital_pet (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                current_stage TEXT NOT NULL,
                current_mood TEXT NOT NULL,
                experience_points INTEGER NOT NULL DEFAULT 0,
                energy_level INTEGER NOT NULL DEFAULT 50,
                last_interaction TEXT NOT NULL,
                created_date TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
                updated_date TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
            )
            """,
            
            // Daily Habits table
            """
            CREATE TABLE IF NOT EXISTS daily_habits (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                date TEXT NOT NULL UNIQUE,
                study_hours INTEGER NOT NULL DEFAULT 0,
                water_intake REAL NOT NULL DEFAULT 0.0,
                steps_taken INTEGER NOT NULL DEFAULT 0,
                sleep_hours REAL NOT NULL DEFAULT 0.0,
                money_spent REAL NOT NULL DEFAULT 0.0,
                goals_completed INTEGER NOT NULL DEFAULT 0,
                completion_percentage REAL NOT NULL DEFAULT 0.0,
                created_date TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
                updated_date TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
            )
            """,
            
            // Achievements table
            """
            CREATE TABLE IF NOT EXISTS achievements (
                id TEXT PRIMARY KEY,
                title TEXT NOT NULL,
                description TEXT NOT NULL,
                category TEXT NOT NULL,
                target_value INTEGER NOT NULL DEFAULT 1,
                current_value INTEGER NOT NULL DEFAULT 0,
                progress REAL NOT NULL DEFAULT 0.0,
                is_unlocked INTEGER NOT NULL DEFAULT 0,
                unlocked_date TEXT,
                created_date TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
                updated_date TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
            )
            """,
            
            // User Progress snapshots table (for historical tracking)
            """
            CREATE TABLE IF NOT EXISTS user_progress_snapshots (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                snapshot_date TEXT NOT NULL,
                total_study_hours INTEGER NOT NULL DEFAULT 0,
                total_water_liters INTEGER NOT NULL DEFAULT 0,
                total_steps INTEGER NOT NULL DEFAULT 0,
                total_sleep_hours INTEGER NOT NULL DEFAULT 0,
                total_money_spent INTEGER NOT NULL DEFAULT 0,
                total_goals_completed INTEGER NOT NULL DEFAULT 0,
                max_streak INTEGER NOT NULL DEFAULT 0,
                current_xp INTEGER NOT NULL DEFAULT 0,
                evolution_stage TEXT NOT NULL,
                created_date TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
            )
            """,
            
            // Application settings table
            """
            CREATE TABLE IF NOT EXISTS app_settings (
                key TEXT PRIMARY KEY,
                value TEXT NOT NULL,
                updated_date TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
            )
            """
        };
        
        try (Statement stmt = connection.createStatement()) {
            for (String sql : createTableStatements) {
                stmt.execute(sql);
            }
        }
        
        // Create indexes for better performance
        createIndexes();
    }
    
    /**
     * Creates database indexes for better query performance
     */
    private void createIndexes() throws SQLException {
        String[] indexStatements = {
            "CREATE INDEX IF NOT EXISTS idx_daily_habits_date ON daily_habits(date)",
            "CREATE INDEX IF NOT EXISTS idx_achievements_category ON achievements(category)",
            "CREATE INDEX IF NOT EXISTS idx_achievements_unlocked ON achievements(is_unlocked)",
            "CREATE INDEX IF NOT EXISTS idx_progress_snapshots_date ON user_progress_snapshots(snapshot_date)"
        };
        
        try (Statement stmt = connection.createStatement()) {
            for (String sql : indexStatements) {
                stmt.execute(sql);
            }
        }
    }
    
    /**
     * Validates data integrity on startup
     */
    private void validateDataIntegrity() {
        try {
            // Check if tables exist and have expected structure
            DatabaseMetaData metaData = connection.getMetaData();
            
            String[] requiredTables = {"digital_pet", "daily_habits", "achievements", "user_progress_snapshots", "app_settings"};
            for (String tableName : requiredTables) {
                try (ResultSet rs = metaData.getTables(null, null, tableName, null)) {
                    if (!rs.next()) {
                        throw new RuntimeException("Required table missing: " + tableName);
                    }
                }
            }
            
            // Validate data consistency
            validateHabitDataConsistency();
            
        } catch (SQLException e) {
            throw new RuntimeException("Data integrity validation failed", e);
        }
    }
    
    /**
     * Validates habit data consistency
     */
    private void validateHabitDataConsistency() throws SQLException {
        String sql = """
            SELECT date, study_hours, water_intake, steps_taken, sleep_hours, money_spent, goals_completed
            FROM daily_habits
            WHERE study_hours < 0 OR study_hours > 16
               OR water_intake < 0 OR water_intake > 5.0
               OR steps_taken < 0 OR steps_taken > 50000
               OR sleep_hours < 0 OR sleep_hours > 24.0
               OR money_spent < 0 OR money_spent > 1000.0
               OR goals_completed < 0 OR goals_completed > 10
            """;
        
        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                System.err.println("Warning: Found invalid habit data for date: " + rs.getString("date"));
                // Could implement automatic data correction here
            }
        }
    }
    
    /**
     * Schedules automatic daily backup
     */
    private void scheduleAutomaticBackup() {
        // Schedule backup every 24 hours
        backupScheduler.scheduleAtFixedRate(this::performAutomaticBackup, 1, 24, TimeUnit.HOURS);
    }
    
    /**
     * Performs automatic backup of the database
     */
    private void performAutomaticBackup() {
        try {
            createBackup();
        } catch (Exception e) {
            System.err.println("Automatic backup failed: " + e.getMessage());
        }
    }
    
    /**
     * Creates a backup of the current database
     */
    public void createBackup() throws SQLException, IOException {
        // Close current connection temporarily
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
        
        try {
            // Copy database file to backup location
            Files.copy(databasePath, backupPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            
            // Reopen connection
            String url = "jdbc:sqlite:" + databasePath.toString();
            connection = DriverManager.getConnection(url);
            
            // Re-enable foreign keys
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("PRAGMA foreign_keys = ON");
                stmt.execute("PRAGMA journal_mode = WAL");
            }
            
        } catch (IOException e) {
            // Reopen connection even if backup failed
            String url = "jdbc:sqlite:" + databasePath.toString();
            connection = DriverManager.getConnection(url);
            throw e;
        }
    }
    
    /**
     * Restores database from backup
     */
    public void restoreFromBackup() throws SQLException, IOException {
        if (!Files.exists(backupPath)) {
            throw new IOException("Backup file not found");
        }
        
        // Close current connection
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
        
        // Replace current database with backup
        Files.copy(backupPath, databasePath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        
        // Reinitialize database connection
        initializeDatabase();
    }
    
    /**
     * Closes the database connection and shuts down the backup scheduler
     */
    public void close() {
        try {
            if (backupScheduler != null && !backupScheduler.isShutdown()) {
                backupScheduler.shutdown();
                backupScheduler.awaitTermination(5, TimeUnit.SECONDS);
            }
            
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException | InterruptedException e) {
            System.err.println("Error closing database: " + e.getMessage());
        }
    }
    
    /**
     * Gets the database connection for DAO operations
     */
    Connection getConnection() {
        return connection;
    }
    
    /**
     * Gets the data directory path
     */
    public Path getDataDirectory() {
        return dataDirectory;
    }
    
    /**
     * Checks if backup exists
     */
    public boolean hasBackup() {
        return Files.exists(backupPath);
    }
    
    /**
     * Gets backup file size in bytes
     */
    public long getBackupSize() {
        try {
            return Files.size(backupPath);
        } catch (IOException e) {
            return 0;
        }
    }
    
    /**
     * Gets database file size in bytes
     */
    public long getDatabaseSize() {
        try {
            return Files.size(databasePath);
        } catch (IOException e) {
            return 0;
        }
    }
}