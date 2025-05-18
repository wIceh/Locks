package me.wiceh.locks.objects;

import org.bukkit.Location;

import java.util.Set;
import java.util.UUID;

public record Lock(int id, UUID owner, Location location, Set<UUID> trustedPlayers) {
}
