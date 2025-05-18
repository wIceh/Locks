package me.wiceh.locks.database.tables;

import me.wiceh.locks.Locks;
import me.wiceh.locks.database.DatabaseManager;
import me.wiceh.locks.database.DatabaseTable;
import me.wiceh.locks.objects.Lock;
import me.wiceh.locks.utils.LocationUtils;
import me.wiceh.locks.utils.LogUtils;
import org.bukkit.Location;
import org.intellij.lang.annotations.Language;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class LocksTable extends DatabaseTable {

    @Language("SQL")
    private static final String LOCKS_TABLE = """
            CREATE TABLE IF NOT EXISTS locks(
                id      INTEGER         PRIMARY KEY     AUTOINCREMENT,
                owner   VARCHAR(36)     NOT NULL,
                world   VARCHAR(255)    NOT NULL,
                x       INTEGER         NOT NULL,
                y       INTEGER         NOT NULL,
                z       INTEGER         NOT NULL
            );
            """;

    @Language("SQL")
    private static final String SELECT_LOCKS = """
            SELECT * FROM locks
            """;

    @Language("SQL")
    private static final String INSERT_LOCK = """
            INSERT INTO locks(owner, world, x, y, z)
            VALUES (?, ?, ?, ?, ?)
            """;

    @Language("SQL")
    private static final String SELECT_LOCK = """
            SELECT * FROM locks
            WHERE world = ?
                AND x = ?
                AND y = ?
                AND z = ?
            """;

    @Language("SQL")
    private static final String DELETE_LOCK = """
            DELETE FROM locks
            WHERE world = ?
                AND x = ?
                AND y = ?
                AND z = ?
            """;

    public LocksTable(DatabaseManager dbManager) {
        super(dbManager, LOCKS_TABLE);
    }

    public CompletableFuture<Set<Lock>> getLocks() {
        return CompletableFuture.supplyAsync(() -> {
            Set<Lock> locks = new HashSet<>();

            try (Connection connection = dbManager.getConnection();
                 PreparedStatement statement = connection.prepareStatement(SELECT_LOCKS)) {
                ResultSet resultSet = statement.executeQuery();
                while (resultSet.next()) {
                    int id = resultSet.getInt("id");
                    Set<UUID> trustedPlayers = Locks.getInstance().getTrustedPlayersTable().getTrustedPlayers(connection, id);

                    locks.add(new Lock(
                            id,
                            UUID.fromString(resultSet.getString("owner")),
                            LocationUtils.locationFromResultSet(resultSet),
                            trustedPlayers
                    ));
                }
            } catch (SQLException e) {
                LogUtils.logError(e, "Unable to get locks");
            }

            return locks;
        });
    }

    public CompletableFuture<Optional<Lock>> addLock(String owner, Location location) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = dbManager.getConnection();
                 PreparedStatement statement = connection.prepareStatement(INSERT_LOCK, PreparedStatement.RETURN_GENERATED_KEYS)) {
                statement.setString(1, owner);
                LocationUtils.locationToStatement(statement, 2, location);
                statement.executeUpdate();

                ResultSet generatedKeys = statement.getGeneratedKeys();
                if (generatedKeys.next()) {
                    return Optional.of(new Lock(
                            generatedKeys.getInt(1),
                            UUID.fromString(owner),
                            location,
                            new HashSet<>()
                    ));
                }
            } catch (SQLException e) {
                LogUtils.logError(e, "Unable to add lock");
            }

            return Optional.empty();
        });
    }

    public CompletableFuture<Optional<Lock>> getLock(Location location) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = dbManager.getConnection();
                 PreparedStatement statement = connection.prepareStatement(SELECT_LOCK)) {
                LocationUtils.locationToStatement(statement, 1, location);

                ResultSet resultSet = statement.executeQuery();
                if (resultSet.next()) {
                    int id = resultSet.getInt("id");
                    Set<UUID> trustedPlayers = Locks.getInstance().getTrustedPlayersTable().getTrustedPlayers(connection, id);

                    return Optional.of(new Lock(
                            id,
                            UUID.fromString(resultSet.getString("owner")),
                            LocationUtils.locationFromResultSet(resultSet),
                            trustedPlayers
                    ));
                }
            } catch (SQLException e) {
                LogUtils.logError(e, "Unable to get lock in location " + LocationUtils.locationToString(location));
            }

            return Optional.empty();
        });
    }

    public CompletableFuture<Boolean> removeLock(Location location) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = dbManager.getConnection();
                 PreparedStatement statement = connection.prepareStatement(DELETE_LOCK)) {
                LocationUtils.locationToStatement(statement, 1, location);
                return statement.executeUpdate() > 0;
            } catch (SQLException e) {
                LogUtils.logError(e, "Unable to remove lock in location " + LocationUtils.locationToString(location));
            }

            return false;
        });
    }
}
