package com.enderdevelopment.plugin;

import java.util.HashSet;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public final class AdminPanelListener implements Listener {
    private static final String TITLE = ChatColor.DARK_GRAY + "Admin Panel";
    private final Main plugin;
    private final Set<Player> openPanels = new HashSet<>();

    public AdminPanelListener(Main plugin) {
        this.plugin = plugin;
    }

    public void openPanel(Player player) {
        Inventory inventory = Bukkit.createInventory(null, 27, TITLE);
        inventory.setItem(10, item(Material.FEATHER, ChatColor.AQUA + "Toggle Flight", "Click to enable or disable flight."));
        inventory.setItem(12, item(Material.SUGAR, ChatColor.YELLOW + "Increase Speed", "Raises speed by one level."));
        inventory.setItem(14, item(Material.SOUL_SAND, ChatColor.GOLD + "Decrease Speed", "Lowers speed by one level."));
        inventory.setItem(16, item(Material.COMPASS, ChatColor.GREEN + "Teleport to Spawn", "Click to teleport to the saved spawn."));
        inventory.setItem(22, item(Material.BARRIER, ChatColor.RED + "Close", "Close this panel."));
        openPanels.add(player);
        player.openInventory(inventory);
    }

    private ItemStack item(Material material, String name, String lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(java.util.List.of(ChatColor.GRAY + lore));
        item.setItemMeta(meta);
        return item;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player) || !openPanels.contains(player)) {
            return;
        }
        if (!event.getView().getTitle().equals(TITLE)) {
            return;
        }
        event.setCancelled(true);
        if (event.getClickedInventory() == null || event.getRawSlot() >= event.getView().getTopInventory().getSize()) {
            return;
        }
        switch (event.getRawSlot()) {
            case 10 -> plugin.toggleFlight(player);
            case 12 -> changeSpeed(player, 1);
            case 14 -> changeSpeed(player, -1);
            case 16 -> plugin.teleportToSpawn(player);
            case 22 -> player.closeInventory();
            default -> {
                return;
            }
        }
        if (event.getRawSlot() != 22) {
            openPanel(player);
        }
    }

    private void changeSpeed(Player player, int change) {
        int current = Math.round(player.getWalkSpeed() * 10.0f);
        int next = Math.max(1, Math.min(10, current + change));
        plugin.setSpeed(player, new String[]{String.valueOf(next)});
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getPlayer() instanceof Player player) {
            openPanels.remove(player);
        }
    }

    public void closeOpenPanels() {
        for (Player player : Set.copyOf(openPanels)) {
            player.closeInventory();
        }
        openPanels.clear();
    }
}
