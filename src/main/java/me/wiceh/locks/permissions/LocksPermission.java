package me.wiceh.locks.permissions;

import org.bukkit.entity.Player;

public enum LocksPermission {
    LOCK("command.lock"),
    GET("command.lock.get"),
    BYPASS("bypass");

    private final String permission;

    LocksPermission(String permission) {
        this.permission = "ice.locks." + permission;
    }

    public String getPermission() {
        return permission;
    }

    public boolean has(Player player) {
        return player.hasPermission(permission);
    }
}
