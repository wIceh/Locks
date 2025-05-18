package me.wiceh.locks.inventories;

import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import dev.triumphteam.gui.guis.PaginatedGui;
import me.wiceh.locks.Locks;
import me.wiceh.locks.objects.Lock;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static net.kyori.adventure.text.Component.empty;
import static net.kyori.adventure.text.Component.text;

public class TrustedPlayersInventory {

    private final int lockId;

    public TrustedPlayersInventory(int lockId) {
        this.lockId = lockId;
    }

    public void open(Player player) {
        PaginatedGui gui = Gui.paginated()
                .title(text("Lista cittadini con accesso"))
                .rows(4)
                .disableAllInteractions()
                .create();

        addNavigationItems(gui);

        Lock lock = Locks.getInstance().getLocksManager().getLocks().get(lockId);
        Set<UUID> trustedPlayers = lock.trustedPlayers();
        for (UUID trustedPlayerId : trustedPlayers) {
            OfflinePlayer trustedPlayer = Bukkit.getOfflinePlayer(trustedPlayerId);

            gui.addItem(
                    ItemBuilder.from(Material.PLAYER_HEAD).model(1)
                            .name(text(trustedPlayer.getName() != null
                                    ? trustedPlayer.getName()
                                    : "Sconosciuto", NamedTextColor.GRAY))
                            .lore(List.of(
                                    empty(),
                                    text("ᴄʟɪᴄᴋ ᴘᴇʀ ʀɪᴍᴜᴏᴠᴇʀᴇ ʟ'ᴀᴄᴄᴇѕѕᴏ", NamedTextColor.YELLOW)
                            ))
                            .setSkullOwner(trustedPlayer)
                            .asGuiItem(event -> {
                                if (event.getCurrentItem() == null) return;

                                Locks.getInstance().getLocksManager().removeTrustedPlayer(lock, trustedPlayer).thenAccept(success -> {
                                    Bukkit.getScheduler().runTask(Locks.getInstance(), () -> {
                                        if (!success) {
                                            player.sendMessage(text("\n §cᴇʀʀᴏʀᴇ \n §7Abbiamo riscontrato dei problemi nel rimuovere l'accesso di questo cittadino dal lock.\n"));
                                            return;
                                        }

                                        gui.removeItem(event.getCurrentItem());
                                        gui.update();
                                        player.sendMessage(text("\n §aᴀᴄᴄᴇѕѕᴏ ʀɪᴍᴏѕѕᴏ \n §7Accesso rimosso con successo.\n"));
                                    });
                                });
                            })
            );
        }

        gui.open(player);
    }

    private void addNavigationItems(PaginatedGui gui) {
        gui.setItem(4, 1, ItemBuilder.from(Material.PAPER).model(12)
                .name(text("ᴘᴀɢɪɴᴀ §lᴘʀᴇᴄᴇᴅᴇɴᴛᴇ", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false))
                .asGuiItem(event -> gui.previous()));

        gui.setItem(4, 5, ItemBuilder.from(Material.PAPER).model(43)
                .name(text("ᴀɢɢɪᴜɴɢɪ ᴄɪᴛᴛᴀᴅɪɴᴏ", NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, false))
                .lore(List.of(
                        empty(),
                        text("Aggiungi un cittadino alla lista degli accessi.", NamedTextColor.GRAY),
                        empty(),
                        text("ᴄʟɪᴄᴋ ᴘᴇʀ ᴀɢɢɪᴜɴɢᴇʀᴇ ᴜɴ ᴄɪᴛᴛᴀᴅɪɴᴏ", NamedTextColor.YELLOW)
                ))
                .asGuiItem(event -> {
                    if (!(event.getWhoClicked() instanceof Player player)) return;
                    new AddTrustedPlayerInventory(lockId).open(player);
                }));

        gui.setItem(4, 9, ItemBuilder.from(Material.PAPER).model(13)
                .name(text("ᴘᴀɢɪɴᴀ §lѕᴜᴄᴄᴇѕѕɪᴠᴀ", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false))
                .asGuiItem(event -> gui.next()));

        int[] indexes = new int[]{28, 29, 30, 32, 33, 34};
        for (int index : indexes) gui.setItem(index, new GuiItem(Material.GRAY_STAINED_GLASS_PANE));
    }
}
