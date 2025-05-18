package me.wiceh.locks.inventories;

import dev.triumphteam.gui.builder.item.ItemBuilder;
import me.wiceh.locks.Locks;
import me.wiceh.locks.managers.LocksManager;
import me.wiceh.locks.objects.Lock;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

import static net.kyori.adventure.text.Component.text;

public class AddTrustedPlayerInventory {

    private final int lockId;
    private final LocksManager locksManager;

    public AddTrustedPlayerInventory(int lockId) {
        this.lockId = lockId;
        this.locksManager = Locks.getInstance().getLocksManager();
    }

    public void open(Player player) {
        new AnvilGUI.Builder()
                .onClick((slot, stateSnapshot) -> {
                    if (slot != AnvilGUI.Slot.OUTPUT) return Collections.emptyList();

                    Lock lock = locksManager.getLocks().get(lockId);
                    if (lock == null) {
                        player.sendMessage(text("§4Errore."));
                        return List.of(AnvilGUI.ResponseAction.close());
                    }

                    String text = stateSnapshot.getText().trim();

                    OfflinePlayer target = Bukkit.getOfflinePlayerIfCached(text);
                    if (target == null || !target.hasPlayedBefore()) {
                        player.sendMessage(text("\n §cᴇʀʀᴏʀᴇ \n §7Cittadino non trovato.\n"));
                        return List.of(AnvilGUI.ResponseAction.close());
                    }

                    if (lock.trustedPlayers().contains(target.getUniqueId()) || lock.owner().equals(target.getUniqueId())) {
                        player.sendMessage(text("\n §cᴇʀʀᴏʀᴇ \n §7Questo cittadino ha già l'accesso a questo lock.\n"));
                        return List.of(AnvilGUI.ResponseAction.close());
                    }

                    locksManager.addTrustedPlayer(lock, target).thenAccept(success -> {
                        if (!success) {
                            player.sendMessage(text("\n §cᴇʀʀᴏʀᴇ \n §7Abbiamo riscontrato dei problemi nel dare l'accesso al lock a questo cittadino.\n"));
                            return;
                        }

                        player.sendMessage(text("\n §aᴀᴄᴄᴇѕѕᴏ ᴀɢɢɪᴜɴᴛᴏ \n §7Hai dato l'accesso a questo lock a §f" + target.getName() + "§7.\n"));
                    });

                    return List.of(AnvilGUI.ResponseAction.close());
                })
                .text(" ")
                .title("")
                .itemLeft(ItemBuilder.from(Material.PAPER).model(4).build())
                .plugin(Locks.getInstance())
                .open(player);
    }
}
