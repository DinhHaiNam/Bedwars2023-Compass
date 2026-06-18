package com.tomkeuper.bedwars.addon.menu.menus.communicationMenus;

import com.tomkeuper.bedwars.addon.Compass;
import com.tomkeuper.bedwars.addon.data.MainConfig;
import com.tomkeuper.bedwars.addon.data.MessagesData;
import com.tomkeuper.bedwars.addon.menu.Menu;
import com.tomkeuper.bedwars.addon.menu.menus.CommunicationsMenu;
import com.tomkeuper.bedwars.api.arena.IArena;
import com.tomkeuper.bedwars.api.arena.team.ITeam;
import com.tomkeuper.bedwars.addon.util.MessagingUtil;
import com.tomkeuper.bedwars.addon.util.NBTItem;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.HashMap;

public class ResourceSelector extends Menu {

    final YamlConfiguration yml;
    final String path;
    final ITeam team;
    final String[] resources = {"iron", "gold", "diamond", "emerald"};
    final HashMap<Integer, String> resourceMap = new HashMap<>();

    public ResourceSelector(Player player, ITeam team, String path) {
        super(player);
        this.team = team;
        this.path = path;
        this.yml = MessagesData.getYml(player);
    }

    @Override
    public String getMenuName() {
        return yml.getString(MessagesData.COMMUNICATIONS_MENU_RESOURCES_TITLE);
    }

    @Override
    public int getSlots() {
        return Compass.getMainConfig().getInt(MainConfig.COMMUNICATIONS_MENU_RESOURCES+".size");
    }

    @Override
    public void handleMenu(InventoryClickEvent e) {
        NBTItem nbtItem = new NBTItem(e.getCurrentItem());
        Player player = (Player) e.getWhoClicked();
        if (!Compass.getBedWars().getArenaUtil().isPlaying(player)) return;
        IArena arena = Compass.getBedWars().getArenaUtil().getArenaByPlayer(player);
        if (arena.isSpectator(player)) return;
        switch (nbtItem.getString("data")) {
            case "back-item":
                new CommunicationsMenu(player, team.getArena()).open();
                break;
            case "resource-item":
                MessagingUtil.resourceMessage(player, team, nbtItem.getString("path"), resourceMap.get(e.getSlot()));
                break;
        }
    }

    @Override
    public void setMenuItems() {
        for (String s : resources) {
            NBTItem nbtItem = new NBTItem(Compass.getMainConfig().getResourceItem(player, s, path));
            inventory.setItem(nbtItem.getInteger("slot"), nbtItem.getItem());
            resourceMap.put(nbtItem.getInteger("slot"), s);
        }
        NBTItem nbtItem = new NBTItem(Compass.getMainConfig().getItem(player, MainConfig.COMMUNICATIONS_MENU_RESOURCES+".back-item", true, "back-item"));
        inventory.setItem(nbtItem.getInteger("slot"), nbtItem.getItem());
    }

}
