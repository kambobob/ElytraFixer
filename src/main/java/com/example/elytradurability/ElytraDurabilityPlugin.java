package com.example.elytradurability;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.ThreadLocalRandom;

public final class ElytraDurabilityPlugin extends JavaPlugin implements Listener {

    private double keepFraction;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadSettings();
        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("ElytraDurability enabled. Reduction: "
                + String.format("%.1f", (1.0 - keepFraction) * 100) + "%");
    }

    private void loadSettings() {
        reloadConfig();
        double percent = getConfig().getDouble("damage-reduction-percent", 75.0);

        if (percent < 0.0 || percent > 100.0) {
            double clamped = Math.max(0.0, Math.min(100.0, percent));
            getLogger().warning("damage-reduction-percent must be between 0 and 100. "
                    + "Got " + percent + ", using " + clamped + " instead.");
            percent = clamped;
        }

        keepFraction = 1.0 - (percent / 100.0);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onItemDamage(PlayerItemDamageEvent event) {
        if (event.getItem().getType() != Material.ELYTRA) {
            return;
        }

        int original = event.getDamage();
        if (original <= 0) {
            return;
        }

        double scaled = original * keepFraction;
        int whole = (int) Math.floor(scaled);
        double remainder = scaled - whole;

        if (remainder > 0.0 && ThreadLocalRandom.current().nextDouble() < remainder) {
            whole++;
        }

        event.setDamage(whole);
    }
}
