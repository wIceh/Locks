package me.wiceh.locks.database.tables;

import me.wiceh.locks.database.DatabaseManager;
import me.wiceh.locks.database.DatabaseTable;
import me.wiceh.locks.utils.LogUtils;
import org.intellij.lang.annotations.Language;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class TrustedPlayersTable extends DatabaseTable {

    @Language("SQL")
    private static final String TRUSTED_PLAYERS_TABLE = """
            CREATE TABLE IF NOT EXISTS trusted_players(
                id          INTEGER         PRIMARY KEY     AUTOINCREMENT,
                lock_id     INTEGER         NOT NULL,
                player      VARCHAR(36)     NOT NULL,
            
                FOREIGN KEY(lock_id) REFERENCES locks(id)
                    ON UPDATE CASCADE
                    ON DELETE CASCADE
            );
            """;

    @Language("SQL")
    private static final String SELECT_TRUSTED_PLAYERS = """
            SELECT * FROM trusted_players
            WHERE lock_id = ?
            """;

    @Language("SQL")
    private static final String REMOVE_TRUSTED_PLAYER = """
            DELETE FROM trusted_players
            WHERE lock_id = ? AND player = ?
            """;

    @Language("SQL")
    private static final String INSERT_TRUSTED_PLAYER = """
            INSERT INTO trusted_players(lock_id, player)
            VALUES (?, ?)
            """;

    public TrustedPlayersTable(DatabaseManager dbManager) {
        super(dbManager, TRUSTED_PLAYERS_TABLE);
    }

    public Set<UUID> getTrustedPlayers(Connection connection, int lockId) {
        Set<UUID> trustedPlayers = new HashSet<>();

        try (PreparedStatement statement = connection.prepareStatement(SELECT_TRUSTED_PLAYERS)) {
            statement.setInt(1, lockId);

            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next())
                trustedPlayers.add(UUID.fromString(resultSet.getString("player")));
        } catch (SQLException e) {
            LogUtils.logError(e, "Unable to get trusted players of lock #" + lockId);
        }

        return trustedPlayers;
    }

    public CompletableFuture<Boolean> removeTrustedPlayer(int lockId, String player) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = dbManager.getConnection();
                 PreparedStatement statement = connection.prepareStatement(REMOVE_TRUSTED_PLAYER)) {
                statement.setInt(1, lockId);
                statement.setString(2, player);
                return statement.executeUpdate() > 0;
            } catch (SQLException e) {
                LogUtils.logError(e, "Unable to remove trusted player " + player + " from lock #" + lockId);
            }

            return false;
        });
    }

    public CompletableFuture<Boolean> addTrustedPlayer(int lockId, String player) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = dbManager.getConnection();
                 PreparedStatement statement = connection.prepareStatement(INSERT_TRUSTED_PLAYER)) {
                statement.setInt(1, lockId);
                statement.setString(2, player);
                return statement.executeUpdate() > 0;
            } catch (SQLException e) {
                LogUtils.logError(e, "Unable to add trusted player " + player + " to lock #" + lockId);
            }

            return false;
        });
    }
}
