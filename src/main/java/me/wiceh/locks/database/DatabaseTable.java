package me.wiceh.locks.database;

import me.wiceh.locks.utils.LogUtils;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public abstract class DatabaseTable {
    protected final DatabaseManager dbManager;
    protected final String tableQuery;

    protected DatabaseTable(DatabaseManager dbManager, String tableQuery) {
        this.dbManager = dbManager;
        this.tableQuery = tableQuery;
        createTable();
    }

    private void createTable() {
        try (Connection connection = dbManager.getConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate(tableQuery);
        } catch (SQLException e) {
            LogUtils.logError(e, "Unable to create table");
        }
    }
}
