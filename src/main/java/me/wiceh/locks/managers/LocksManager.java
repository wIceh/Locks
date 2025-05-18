package me.wiceh.locks.managers;

import dev.triumphteam.gui.builder.item.ItemBuilder;
import me.wiceh.locks.Locks;
import me.wiceh.locks.objects.Lock;
import me.wiceh.locks.utils.LocationUtils;
import me.wiceh.locks.utils.Utils;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class LocksManager {

    private final Map<Integer, Lock> locks;
    private static final NamespacedKey ITEM_KEY = new NamespacedKey(Locks.getInstance(), "item");

    public LocksManager() {
        this.locks = new HashMap<>();
        Locks.getInstance().getLocksTable().getLocks().thenAccept(locks -> locks.forEach(lock -> this.locks.put(lock.id(), lock)));
    }

    public ItemStack getLock() {
        FileConfiguration config = Locks.getInstance().getConfig();

        String material = config.getString("lock.material", "STICK");
        int modelData = config.getInt("lock.model-data", 0);
        String displayName = config.getString("lock.display-name", "<gray>Lucchetto");
        List<String> lore = config.getStringList("lock.lore");

        return ItemBuilder.from(Material.valueOf(material)).model(modelData)
                .name(MiniMessage.miniMessage().deserialize(displayName))
                .lore(Utils.parseComponentList(lore))
                .pdc(pdc -> pdc.set(ITEM_KEY, PersistentDataType.STRING, "lock"))
                .build();
    }

    public boolean isLock(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return false;

        ItemMeta meta = item.getItemMeta();
        if (meta == null || !item.hasItemMeta()) return false;

        return meta.getPersistentDataContainer().has(ITEM_KEY, PersistentDataType.STRING) &&
                meta.getPersistentDataContainer().get(ITEM_KEY, PersistentDataType.STRING).equals("lock");
    }

    public CompletableFuture<Boolean> addLock(Player owner, Location location) {
        return Locks.getInstance().getLocksTable().addLock(owner.getUniqueId().toString(), location).thenApply(lockOpt -> {
            lockOpt.ifPresent(lock -> locks.put(lock.id(), lock));
            return lockOpt.isPresent();
        });
    }

    public Optional<Lock> getLock(Location location) {
        return locks.values().stream()
                .filter(lock -> LocationUtils.isSimilar(lock.location(), location))
                .findAny();
    }

    public CompletableFuture<Boolean> removeLock(Lock lock) {
        return Locks.getInstance().getLocksTable().removeLock(lock.location()).thenApply(success -> {
            if (success) locks.remove(lock.id());
            return success;
        });
    }

    public CompletableFuture<Boolean> removeTrustedPlayer(Lock lock, OfflinePlayer player) {
        return Locks.getInstance().getTrustedPlayersTable().removeTrustedPlayer(lock.id(), player.getUniqueId().toString()).thenApply(success -> {
            if (success) lock.trustedPlayers().remove(player.getUniqueId());
            return success;
        });
    }

    public CompletableFuture<Boolean> addTrustedPlayer(Lock lock, OfflinePlayer player) {
        return Locks.getInstance().getTrustedPlayersTable().addTrustedPlayer(lock.id(), player.getUniqueId().toString()).thenApply(success -> {
            if (success) lock.trustedPlayers().add(player.getUniqueId());
            return success;
        });
    }

    public Map<Integer, Lock> getLocks() {
        return locks;
    }
}
