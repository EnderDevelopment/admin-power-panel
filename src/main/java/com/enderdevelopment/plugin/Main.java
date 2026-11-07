package com.enderdevelopment.plugin;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin implements CommandExecutor {
    private static final String ADMIN_PERMISSION = "admin.panel";
    private AdminPanelListener adminPanelListener;

    @Override
    public void onEnable() {
        adminPanelListener = new AdminPanelListener(this);
        Bukkit.getPluginManager().registerEvents(adminPanelListener, this);
        getCommand("admin").setExecutor(this);
        getCommand("setspawn").setExecutor(this);
        getCommand("spawn").setExecutor(this);
        getCommand("fly").setExecutor(this);
        getCommand("speed").setExecutor(this);
    }

    @Override
    public void onDisable() {
        if (adminPanelListener != null) {
            adminPanelListener.closeOpenPanels();
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }
        if (!player.hasPermission(ADMIN_PERMISSION)) {
            player.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
            return true;
        }

        switch (command.getName().toLowerCase()) {
            case "admin" -> {
                if (args.length != 0) {
                    player.sendMessage(ChatColor.YELLOW + "Usage: /admin");
                    return true;
                }
                adminPanelListener.openPanel(player);
            }
            case "setspawn" -> setSpawn(player);
            case "spawn" -> teleportToSpawn(player);
            case "fly" -> toggleFlight(player);
            case "speed" -> setSpeed(player, args);
            default -> {
                return false;
            }
        }
        return true;
    }

    private void setSpawn(Player player) {
        Location location = player.getLocation();
        getConfig().set("spawn.world", location.getWorld().getName());
        getConfig().set("spawn.x", location.getX());
        getConfig().set("spawn.y", location.getY());
        getConfig().set("spawn.z", location.getZ());
        getConfig().set("spawn.yaw", location.getYaw());
        getConfig().set("spawn.pitch", location.getPitch());
        saveConfig();
        player.sendMessage(ChatColor.GREEN + "Spawn location saved.");
    }

    public void teleportToSpawn(Player player) {
        Location spawn = getSpawn();
        if (spawn == null) {
            player.sendMessage(ChatColor.RED + "No spawn location has been set. Use /setspawn first.");
            return;
        }
        player.teleport(spawn);
        player.sendMessage(ChatColor.GREEN + "Teleported to spawn.");
    }

    private Location getSpawn() {
        String worldName = getConfig().getString("spawn.world");
        if (worldName == null || Bukkit.getWorld(worldName) == null) {
            return null;
        }
        return new Location(
                Bukkit.getWorld(worldName),
                getConfig().getDouble("spawn.x"),
                getConfig().getDouble("spawn.y"),
                getConfig().getDouble("spawn.z"),
                (float) getConfig().getDouble("spawn.yaw"),
                (float) getConfig().getDouble("spawn.pitch")
        );
    }

    public void toggleFlight(Player player) {
        boolean enabled = !player.getAllowFlight();
        player.setAllowFlight(enabled);
        player.setFlying(enabled);
        player.sendMessage(enabled ? ChatColor.GREEN + "Flight enabled." : ChatColor.YELLOW + "Flight disabled.");
    }

    public void setSpeed(Player player, String[] args) {
        if (args.length != 1) {
            player.sendMessage(ChatColor.YELLOW + "Usage: /speed <1-10>");
            return;
        }
        int level;
        try {
            level = Integer.parseInt(args[0]);
        } catch (NumberFormatException exception) {
            player.sendMessage(ChatColor.RED + "Speed must be a number from 1 to 10.");
            return;
        }
        if (level < 1 || level > 10) {
            player.sendMessage(ChatColor.RED + "Speed must be between 1 and 10.");
            return;
        }
        float speed = level / 10.0f;
        player.setWalkSpeed(speed);
        player.setFlySpeed(speed);
        player.sendMessage(ChatColor.GREEN + "Speed set to level " + level + ".");
    }
}
