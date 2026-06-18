package com.tomkeuper.bedwars.addon;

import com.tomkeuper.bedwars.addon.integrations.BedWars2023;
import com.tomkeuper.bedwars.addon.integrations.IIntegration;
import com.tomkeuper.bedwars.addon.listener.QuickBuyListener;
import com.tomkeuper.bedwars.addon.support.bstats.Metrics;
import com.tomkeuper.bedwars.api.BedWars;
import com.tomkeuper.bedwars.api.arena.IArena;
import com.tomkeuper.bedwars.api.arena.team.ITeam;
import lombok.Getter;
import com.tomkeuper.bedwars.addon.command.CompassMenuCommand;
import com.tomkeuper.bedwars.addon.data.MainConfig;
import com.tomkeuper.bedwars.addon.data.MessagesData;
import com.tomkeuper.bedwars.addon.listener.GameListener;
import com.tomkeuper.bedwars.addon.listener.MenuListener;
import com.tomkeuper.bedwars.addon.support.vault.VaultSupport;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;
import java.util.logging.Level;

public class Compass extends JavaPlugin {

    @Getter private static Compass instance;
    @Getter private static BedWars bedWars;
    @Getter private static MainConfig mainConfig;
    @Getter private static final HashMap<IArena, HashMap<UUID, ITeam>> trackingArenaMap = new HashMap<>();
    @Getter private static boolean isUsingVaultChat = false, isUsingPapi = false;
    @Getter private static VaultSupport vault;

    public static final String VERSION = Bukkit.getServer().getClass().getName().split("\\.")[3];
    public static String PLUGIN_VERSION;

    @Override
    public void onLoad() {
        instance = this;
    }

    @Override
    public void onEnable() {
        long start = System.currentTimeMillis();

        PLUGIN_VERSION = getDescription().getVersion();

        vault = new VaultSupport();
        isUsingVaultChat = vault.setupChat();
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) isUsingPapi = true;

        populateIntegrations(new BedWars2023(this, bedWars = Bukkit.getServicesManager().getRegistration(BedWars.class).getProvider()));

        new CompassMenuCommand(bedWars.getBedWarsCommand(), "compass");
        mainConfig = new MainConfig(this, "config", bedWars.getAddonsPath().getPath()+File.separator+"Compass");
        mainConfig.reload();
        new MessagesData();

        registerEvents();
    }

    private void populateIntegrations(IIntegration... integrations) {
        for (IIntegration integration : integrations) {
            if (!integration.enable()) {
                continue;
            }
        }
    }

    public static void registerEvents() {
        Arrays.asList(new MenuListener(), new GameListener(), new QuickBuyListener()).forEach(l -> Bukkit.getPluginManager().registerEvents(l, instance));
    }

    @Override
    public void onDisable() {
        Bukkit.getScheduler().cancelTasks(this);
        trackingArenaMap.clear();
    }

    public static void setTrackingTeam(IArena arena, UUID uuid, ITeam team) {
        if (trackingArenaMap.get(arena) != null) {
            getTrackingTeamMap(arena).put(uuid, team);
            return;
        }
        HashMap<UUID, ITeam> map = new HashMap<>();
        map.put(uuid, team);
        trackingArenaMap.put(arena, map);
    }

    public static boolean isTracking(IArena arena, UUID uuid) {
        if (trackingArenaMap.containsKey(arena)) {
            return trackingArenaMap.get(arena).containsKey(uuid);
        }
        return false;
    }

    public static HashMap<UUID, ITeam> getTrackingTeamMap(IArena arena) {
        return trackingArenaMap.get(arena);
    }

    public static ITeam getTrackingTeam(IArena arena, UUID uuid) {
        return trackingArenaMap.get(arena).get(uuid);
    }

    public static void removeTrackingTeam(IArena arena, UUID uuid) {
        trackingArenaMap.get(arena).remove(uuid);
    }

    public static void removeTrackingArena(IArena arena) {
        trackingArenaMap.remove(arena);
    }

    public static BedWars getBedWars() {
        return bedWars;
    }

    public static Compass getInstance(){
        return instance;
    }

    public static void debug(String msg){
        instance.getLogger().info("[DEBUG] - " + msg);
    }
}
