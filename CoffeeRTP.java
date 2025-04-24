package org.exampleelo.coffeeRTP;

import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class CoffeeRTP extends JavaPlugin {

    private FileConfiguration config;
    private final Map<UUID, Long> cooldowns = new HashMap<>();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        config = getConfig();
        Objects.requireNonNull(getCommand("rtp")).setExecutor(new RTPCommand(this));
        getLogger().info("[CoffeeRTP] Plugin został włączony.");
    }

    @Override
    public void onDisable() {
        getLogger().info("[CoffeeRTP] Plugin został wyłączony.");
    }

    public FileConfiguration getPluginConfig() {
        return config;
    }

    public boolean isOnCooldown(Player player) {
        int cooldownTime = config.getInt("settings.cooldown", 12);
        long lastUse = cooldowns.getOrDefault(player.getUniqueId(), 0L);
        long timeLeft = ((lastUse + cooldownTime * 1000L) - System.currentTimeMillis()) / 1000;
        return timeLeft > 0;
    }

    public long getCooldownTimeLeft(Player player) {
        int cooldownTime = config.getInt("settings.cooldown", 12);
        long lastUse = cooldowns.getOrDefault(player.getUniqueId(), 0L);
        return ((lastUse + cooldownTime * 1000L) - System.currentTimeMillis()) / 1000;
    }

    public void setCooldown(Player player) {
        cooldowns.put(player.getUniqueId(), System.currentTimeMillis());
    }

    public void teleportPlayer(Player player) {
        String worldName = config.getString("settings.world", "world");
        int min = config.getInt("settings.min", 100);
        int max = config.getInt("settings.max", 1000);
        int attempts = config.getInt("settings.attempts", 10);

        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    config.getString("messages.invalid-world", "&cŚwiat &f%world% &cnie istnieje.").replace("%world%", worldName)));
            return;
        }

        player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                config.getString("messages.searching", "&eWyszukiwanie bezpiecznej lokacji...")));

        new BukkitRunnable() {
            int tries = 0;

            @Override
            public void run() {
                if (tries >= attempts) {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                            config.getString("messages.fail", "&cNie udało się znaleźć bezpiecznego miejsca.")));
                    cancel();
                    return;
                }

                Random rand = new Random();
                int x = rand.nextInt((max - min) * 2) - (max - min);
                int z = rand.nextInt((max - min) * 2) - (max - min);
                Location loc = new Location(world, x, world.getHighestBlockYAt(x, z), z);

                Material blockType = loc.getBlock().getType();
                if (!blockType.name().contains("WATER") && !blockType.name().contains("LAVA") && blockType.isSolid()) {
                    loc.add(0.5, 1.0, 0.5);
                    player.teleport(loc);
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                            config.getString("messages.success", "&aTeleportacja zakończona sukcesem!")));

                    world.spawnParticle(Particle.PORTAL, loc, 100, 0.5, 1, 0.5, 0.2);
                    world.playSound(loc, Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f);

                    setCooldown(player);
                    cancel();
                }
                tries++;
            }
        }.runTaskTimer(this, 0L, 20L);
    }
}
