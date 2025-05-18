package me.wiceh.locks.commands;

import me.wiceh.locks.Locks;
import me.wiceh.locks.permissions.LocksPermission;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import static net.kyori.adventure.text.Component.text;

public class LockCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) return true;

        if (!player.hasPermission(LocksPermission.LOCK.getPermission())) {
            player.sendMessage(text("\n §cᴇʀʀᴏʀᴇ \n §7Non hai il permesso per eseguire questo comando.\n"));
            return true;
        }

        if (args.length == 0) {
            sendHelp(player, label);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "get":
                if (!player.hasPermission(LocksPermission.GET.getPermission())) {
                    player.sendMessage(text("\n §cᴇʀʀᴏʀᴇ \n §7Non hai il permesso per eseguire questo comando.\n"));
                    break;
                } else {
                    player.getInventory().addItem(Locks.getInstance().getLocksManager().getLock());
                    player.sendMessage(text("\n §aʟᴜᴄᴄʜᴇᴛᴛᴏ ᴏᴛᴛᴇɴᴜᴛᴏ \n §7Hai ottenuto un lucchetto con successo.\n"));
                }
                break;
            default:
                sendHelp(player, label);
                break;
        }

        return true;
    }

    private void sendHelp(Player player, String label) {
        player.sendMessage(text("§8» §6ʟɪѕᴛᴀ §lᴄᴏᴍᴀɴᴅɪ §7(/" + label + ") §8«"));
        player.sendMessage(text(" §7• §e/" + label + " ")
                .append(text("§eget")
                        .hoverEvent(HoverEvent.showText(text("§8» §7Ottieni un lucchetto")))
                        .clickEvent(ClickEvent.runCommand("/" + label + " get"))));
    }
}
