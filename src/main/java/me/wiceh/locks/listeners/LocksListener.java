package me.wiceh.locks.listeners;

import me.wiceh.locks.Locks;
import me.wiceh.locks.inventories.TrustedPlayersInventory;
import me.wiceh.locks.managers.LocksManager;
import me.wiceh.locks.objects.Lock;
import me.wiceh.locks.permissions.LocksPermission;
import me.wiceh.locks.utils.LocationUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import static net.kyori.adventure.text.Component.text;

public class LocksListener implements Listener {

    private final LocksManager locksManager;

    public LocksListener() {
        this.locksManager = Locks.getInstance().getLocksManager();
    }

    private boolean isLockable(Block block) {
        return block.getType() == Material.CHEST
                || block.getType().name().contains("DOOR");
    }

    @EventHandler
    public void onBlockInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getHand() != EquipmentSlot.HAND) return;

        Block block = event.getClickedBlock();
        if (block == null) return;
        if (!isLockable(block)) return;

        Location location = LocationUtils.normalize(block);

        if (player.isSneaking()) {
            ItemStack item = event.getItem();
            if (locksManager.isLock(item)) {
                event.setCancelled(true);

                if (locksManager.getLock(location).isPresent()) {
                    player.sendMessage(text("\n §cᴇʀʀᴏʀᴇ \n §7Questo blocco è già bloccato.\n"));
                    return;
                }

                locksManager.addLock(player, location).thenAccept(success -> {
                    if (!success) {
                        player.sendMessage(text("\n §cᴇʀʀᴏʀᴇ \n §7Abbiamo riscontrato dei problemi nel aggiungere il lock al database.\n"));
                        return;
                    }

                    item.setAmount(item.getAmount() - 1);
                    player.sendMessage(text("\n §aʟᴏᴄᴋ ᴀɢɢɪᴜɴᴛᴏ \n §7Hai bloccato con successo questo blocco.\n"));
                });
            } else {
                Lock lock = locksManager.getLock(location).orElse(null);
                if (lock == null) return;

                event.setCancelled(true);

                if (lock.owner().equals(player.getUniqueId()) || LocksPermission.BYPASS.has(player))
                    new TrustedPlayersInventory(lock.id()).open(player);
            }
        } else {
            Lock lock = locksManager.getLock(location).orElse(null);
            if (lock == null) return;

            if (!lock.owner().equals(player.getUniqueId()) && !lock.trustedPlayers().contains(player.getUniqueId())
                    && !LocksPermission.BYPASS.has(player)) {
                event.setCancelled(true);
                player.sendMessage(text("\n §cᴇʀʀᴏʀᴇ \n §7Non puoi interagire con questo blocco.\n"));
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Location location = LocationUtils.normalize(event.getBlock());

        Lock lock = locksManager.getLock(location).orElse(null);
        if (lock == null) return;

        event.setCancelled(true);

        if (player.isSneaking()) {
            if (!lock.owner().equals(player.getUniqueId()) && !LocksPermission.BYPASS.has(player)) {
                player.sendMessage(text("\n §cᴇʀʀᴏʀᴇ \n §7Non sei il proprietario di questo lock.\n"));
                return;
            }

            locksManager.removeLock(lock).thenAccept(success -> {
                if (!success) {
                    player.sendMessage(text("\n §cᴇʀʀᴏʀᴇ \n §7Abbiamo riscontrato dei problemi nel rimuovere il lock al database.\n"));
                    return;
                }

                player.getInventory().addItem(Locks.getInstance().getLocksManager().getLock());
                player.sendMessage(text("\n §aʟᴏᴄᴋ ʀɪᴍᴏѕѕᴏ \n §7Hai rimosso il lock con successo.\n"));
            });
        } else {
            player.sendMessage(text("\n §cɪᴍᴘᴏѕѕɪʙɪʟᴇ ѕᴘᴀᴄᴄᴀʀᴇ \n §7Per spaccare questo blocco devi prima rimuovere il lock shiftando.\n"));
        }
    }
}
