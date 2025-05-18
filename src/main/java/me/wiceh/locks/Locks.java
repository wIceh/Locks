package me.wiceh.locks;

import me.wiceh.locks.commands.LockCommand;
import me.wiceh.locks.database.DatabaseManager;
import me.wiceh.locks.database.tables.LocksTable;
import me.wiceh.locks.database.tables.TrustedPlayersTable;
import me.wiceh.locks.listeners.LocksListener;
import me.wiceh.locks.managers.LocksManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class Locks extends JavaPlugin {

    private static Locks instance;

    private DatabaseManager dbManager;
    private LocksTable locksTable;
    private TrustedPlayersTable trustedPlayersTable;

    private LocksManager locksManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        dbManager = new DatabaseManager();
        locksTable = new LocksTable(dbManager);
        trustedPlayersTable = new TrustedPlayersTable(dbManager);

        locksManager = new LocksManager();

        getCommand("lock").setExecutor(new LockCommand());
        getServer().getPluginManager().registerEvents(new LocksListener(), this);
    }

    public static Locks getInstance() {
        return instance;
    }

    public DatabaseManager getDatabaseManager() {
        return dbManager;
    }

    public LocksTable getLocksTable() {
        return locksTable;
    }

    public TrustedPlayersTable getTrustedPlayersTable() {
        return trustedPlayersTable;
    }

    public LocksManager getLocksManager() {
        return locksManager;
    }
}
