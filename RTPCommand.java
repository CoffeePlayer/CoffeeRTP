package org.exampleelo.coffeeRTP;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class RTPCommand implements CommandExecutor {

    private final CoffeeRTP plugin;

    public RTPCommand(CoffeeRTP plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        FileConfiguration config = plugin.getPluginConfig();

        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("coffeertp.reload")) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        config.getString("messages.no-permission", "&cNie masz uprawnień, aby użyć tej komendy.")));
                return true;
            }

            plugin.reloadConfig();
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    plugin.getPluginConfig().getString("messages.reloaded", "&aKonfiguracja została przeładowana!")));
            return true;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    config.getString("messages.only-player", "&cTylko gracze mogą używać tej komendy.")));
            return true;
        }

        if (!player.hasPermission("coffeertp.use")) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    config.getString("messages.no-permission", "&cNie masz uprawnień, aby użyć tej komendy.")));
            return true;
        }

        if (plugin.isOnCooldown(player)) {
            long timeLeft = plugin.getCooldownTimeLeft(player);
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    config.getString("messages.cooldown", "&cMusisz zaczekać jeszcze &f%seconds%s &csekund przed kolejną teleportacją.")
                            .replace("%seconds%", String.valueOf(timeLeft))));
            return true;
        }

        plugin.teleportPlayer(player);
        return true;
    }
}
