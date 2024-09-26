package de.turboman.wdl;

import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class WorldDownloader extends JavaPlugin {
    public static final BossBar progressBossbar = BossBar.bossBar(Component.empty(), 0, BossBar.Color.GREEN, BossBar.Overlay.PROGRESS);
    public static final String backendURL = "http://localhost:8080/api/v1";

    @Override
    public void onEnable() {
        Objects.requireNonNull(getCommand("wdl")).setExecutor(new WDLCommand());

        getServer().getPluginManager().registerEvents(new PlayerJoinEvent(), this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
