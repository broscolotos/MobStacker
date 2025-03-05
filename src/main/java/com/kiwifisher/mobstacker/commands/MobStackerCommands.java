package com.kiwifisher.mobstacker.commands;

import com.kiwifisher.mobstacker.MobStacker;
import net.minecraft.util.org.apache.commons.io.FileUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Scanner;

public class MobStackerCommands implements CommandExecutor {

    private final MobStacker plugin = MobStacker.getInstance();

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        if (command.getLabel().equalsIgnoreCase("mobstacker") && commandSender instanceof Player) {
            Player player = (Player) commandSender;
            if (args.length == 0 || (args.length == 1 && args[0].equalsIgnoreCase("help"))) {
                if (plugin.usesWorldGuard()) {player.sendMessage(ChatColor.GREEN + "Region Flags: " + ChatColor.YELLOW + "/mobstacker region <regionID> <true || false>"
                        + ChatColor.GRAY + " - True meaning mobs do stack.");}
                player.sendMessage(ChatColor.GREEN + "Toggle: " + ChatColor.YELLOW + "/mobstacker toggle" + ChatColor.GRAY + " - Toggles whether mobs stack globally");
                player.sendMessage(ChatColor.GREEN + "Toggle: " + ChatColor.YELLOW + "/mobstacker reload" + ChatColor.GRAY + " - Reloads the config");
                player.sendMessage(ChatColor.GREEN + "KillAll: " + ChatColor.YELLOW + "/mobstacker killall" + ChatColor.GRAY + " - Removes all stacks in all worlds");
                return true;
            }
            else if (args.length == 1 && args[0].equalsIgnoreCase("reload") && player.hasPermission("mobstacker.reload")) {
                plugin.reloadConfig();
                player.sendMessage(ChatColor.GREEN + "Reloaded the config for MobStacker");
                return true;
            }
            else if (args.length == 1 && args[0].equalsIgnoreCase("toggle") && player.hasPermission("mobstacker.toggle")) {
                plugin.setStacking(!plugin.isStacking());

                player.sendMessage(ChatColor.GREEN + "Mob stacking is now " + (plugin.isStacking() ? ChatColor.GREEN + "enabled" : ChatColor.RED + "disabled"));
                return true;
            }
            else if (args.length == 3 && args[0].equalsIgnoreCase("region") && player.hasPermission("mobstacker.setregions")
                    && (args[2].equalsIgnoreCase("true") || args[2].equalsIgnoreCase("false"))) {
                if (!plugin.usesWorldGuard()) {
                    player.sendMessage(ChatColor.RED + "WorldGuard integration is not enabled. Please add WorldGuard to your server");
                    return false;
                }
                String regionName = args[1];
                boolean nowStacking;
                nowStacking = args[2].equalsIgnoreCase("false");
                File excludedFile =  new File(plugin.getWorldGuard().getDataFolder() + "/mobstacker-excluded-regions.yml");
                if (plugin.getWorldGuard().getRegionManager(player.getWorld()).hasRegion(regionName) && nowStacking) {
                    try {
                        FileUtils.writeStringToFile(excludedFile, (regionName + "\n"), Charset.defaultCharset(), true);
                        player.sendMessage(ChatColor.GREEN + "Mobs are now" + ChatColor.RED + " not " + ChatColor.GREEN + "stacking in " + regionName);

                        plugin.updateExcludedRegions();

                    }
                    catch (IOException ex) {
                        plugin.log("Couldn't save the region exclusion! Please contact the author of this plugin");
                        player.sendMessage(ChatColor.DARK_RED + "An error occurred. Please report this ErrCode: 1");
                    }
                }
                else if (!nowStacking) {
                    File tempFile = plugin.loadResource(plugin, "temp.yml");
                    boolean removedFromFile = false;
                    try {
                        Scanner reader = new Scanner(excludedFile);
                        while (reader.hasNextLine()) {
                            String line = reader.nextLine();
                            if (!line.contains(regionName)) {
                                FileUtils.writeStringToFile(tempFile, line + "\n", Charset.defaultCharset(), true);
                            }
                            else {
                                removedFromFile = true;
                            }
                        }
                        if (removedFromFile) {
                            FileUtils.copyFile(tempFile, excludedFile);
                            player.sendMessage(ChatColor.GREEN + "Mobs are now stacking in " + regionName);
                        }
                        else {
                            player.sendMessage(ChatColor.RED + "Mobs are already stacking in " + regionName);
                        }
                        FileUtils.forceDelete(tempFile);
                        reader.close();
                        plugin.updateExcludedRegions();
                    }
                    catch (IOException e) {
                        player.sendMessage(ChatColor.DARK_RED + "An error occurred. Please report this ErrCode: 2");
                        e.printStackTrace();
                    }
                }
                else {
                    player.sendMessage(ChatColor.RED + regionName + ChatColor.YELLOW + " isn't a valid region name");
                }
                return true;
            }
            if (args.length == 1 && args[0].equalsIgnoreCase("killall") && player.hasPermission("mobstacker.killall")) {
                plugin.removeAllStacks();
                player.sendMessage(ChatColor.GREEN + "All stacks were successfully removed");
                return true;
            }
            else {
                player.sendMessage(ChatColor.RED + "Unrecognised command. Please check /mobstacker help");
            }

        }
        else if (command.getLabel().equalsIgnoreCase("mobstacker") && commandSender instanceof ConsoleCommandSender) {
            if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
                plugin.reloadConfig();
                plugin.log("Reloaded the config for MobStacker");
                return true;
            }
            if (args.length == 1 && args[0].equalsIgnoreCase("toggle")) {
                plugin.setStacking(!plugin.isStacking());
                plugin.log("Mob stacking is now " + (plugin.isStacking() ? "enabled" : "disabled"));
                return true;
            }
        }
        return false;
    }

}
