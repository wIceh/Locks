package me.wiceh.locks.database;

import me.wiceh.locks.Locks;
import me.wiceh.locks.utils.LogUtils;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {

    private File dbFile;

    public DatabaseManager() {
        createFile(Locks.getInstance().getDataFolder().getAbsolutePath());
    }

    private void createFile(String path) {
        File dataFolder = new File(path);
        if (!dataFolder.exists()) dataFolder.mkdirs();

        dbFile = new File(dataFolder, "locks.db");
        if (!dbFile.exists()) {
            try {
                dbFile.createNewFile();
            } catch (IOException e) {
                LogUtils.logError(e, "Unable to create database file");
            }
        }
    }

    public Connection getConnection() throws SQLException {
        Connection connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());
        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys = ON;");
        } catch (SQLException e) {
            LogUtils.logError(e, "Failed to enable foreign keys");
        }

        return connection;
    }
}
