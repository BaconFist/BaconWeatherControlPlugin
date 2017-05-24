/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bratler.minecraft.baconweathercontrolplugin;

import java.util.Iterator;
import java.util.Random;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.weather.ThunderChangeEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author xklos
 */
public class Main extends JavaPlugin implements CommandExecutor, Listener, Runnable {

    private static final int TICKS_PER_SECOND = 20;

    private static final String COMMAND_LABEL_INFO = "bwc-info";
    private static final String COMMAND_LABEL_RAININESS = "bwc-raininess";
    private static final String COMMAND_LABEL_THUNDERNESS = "bwc-thunderness";
    private static final String COMMAND_LABEL_UPDATEINTERVAL = "bwc-updateinterval";

    private static final String CONF_UID_THUNDERNESS_REQUIRED = "ThundernessRequired";
    private static final String CONF_UID_RAININESS_REQUIRED = "RaininessRequired";
    private static final String CONF_UID_UPDATE_INTERVAL = "UpdateInterval";

    private static final int CONF_DEFAULT_THUNDERNESS_REQUIRED = 80;
    private static final int CONF_DEFAULT_RAININESS_REQUIRED = 70;
    private static final int CONF_DEFAULT_UPDATE_INTERVAL = 600;

    private boolean canThunder;
    private boolean canRain;

    private int schedulerTaskId;
    
    public Main() {
        this.schedulerTaskId = -1;
        this.canRain = false;
        this.canThunder = false;
    }

    @Override
    public void onEnable() {
        registerCommands();
        setConfigDefaults();
        getServer().getPluginManager().registerEvents(this, this);
        updateScheduler();
    }

    private void registerCommands() {
        getCommand(COMMAND_LABEL_RAININESS).setExecutor(this);
        getCommand(COMMAND_LABEL_THUNDERNESS).setExecutor(this);
        getCommand(COMMAND_LABEL_UPDATEINTERVAL).setExecutor(this);
        getCommand(COMMAND_LABEL_INFO).setExecutor(this);
    }

    private void setConfigDefaults() {
        getConfig().addDefault(CONF_UID_UPDATE_INTERVAL, CONF_DEFAULT_UPDATE_INTERVAL);
        getConfig().addDefault(CONF_UID_THUNDERNESS_REQUIRED, CONF_DEFAULT_THUNDERNESS_REQUIRED);
        getConfig().addDefault(CONF_UID_RAININESS_REQUIRED, CONF_DEFAULT_RAININESS_REQUIRED);

        getConfig().options().copyDefaults();
    }

    public void updateScheduler() {
        getServer().getScheduler().cancelTask(schedulerTaskId);
        int interval = getConfig().getInt(CONF_UID_UPDATE_INTERVAL) * TICKS_PER_SECOND;
        schedulerTaskId = getServer().getScheduler().scheduleSyncRepeatingTask(this, this, interval, interval);
    }

    @Override
    public void onDisable() {
        getConfig().options().copyDefaults();
        saveConfig();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        boolean success = false;
        switch (label) {
            case COMMAND_LABEL_INFO:
                success = onCommandInfo(sender, command, label, args);
                break;
            case COMMAND_LABEL_RAININESS:
                success = onCommandRaininess(sender, command, label, args);
                break;
            case COMMAND_LABEL_THUNDERNESS:
                success = onCommandThunderness(sender, command, label, args);
                break;
            case COMMAND_LABEL_UPDATEINTERVAL:
                success = onCommandUpdateInterval(sender, command, label, args);
                break;
            default:
                success = false;
                break;
        }
        return success;
    }

    private boolean onCommandInfo(CommandSender sender, Command command, String label, String[] args) {
        int rain = 100 - getConfig().getInt(CONF_UID_RAININESS_REQUIRED);
        int thunder = 100 - getConfig().getInt(CONF_UID_THUNDERNESS_REQUIRED);
        int interval = getConfig().getInt(CONF_UID_UPDATE_INTERVAL);
        String slug = "BaconWeatherPlugin status: \n"
                + "The probability of rain is " + rain + "%.\n"
                + "The is a thunderstorm at " + thunder + "%.\n"
                + "weather may change all " + interval + " seconds.";
        sender.sendMessage(slug);

        return true;
    }

    private boolean onCommandRaininess(CommandSender sender, Command command, String label, String[] args) {
        boolean success = false;
        if (args.length == 1) {
            try {
                int value = Integer.parseInt(args[0]);
                if (0 <= value && value <= 100) {
                    getConfig().set(CONF_UID_RAININESS_REQUIRED, value);
                    success = true;
                } else {
                    success = false;
                }
            } catch (NumberFormatException e) {
                success = false;
            }
        }
        if (success) {
            Bukkit.broadcastMessage("required raininess has been set to " + getConfig().getInt(CONF_UID_RAININESS_REQUIRED) + "% by " + sender.getName() + ".");
        } else {
            sender.sendMessage("make sure the first argument is a number inbetween 0 and 100.");
        }

        return success;
    }

    private boolean onCommandThunderness(CommandSender sender, Command command, String label, String[] args) {
        boolean success = false;
        if (args.length == 1) {
            try {
                int value = Integer.parseInt(args[0]);
                if (0 <= value && value <= 100) {
                    getConfig().set(CONF_UID_THUNDERNESS_REQUIRED, value);
                    success = true;
                } else {
                    success = false;
                }
            } catch (NumberFormatException e) {
                success = false;
            }
        }
        if (success) {
            Bukkit.broadcastMessage("required thunderness has been set to " + getConfig().getInt(CONF_UID_THUNDERNESS_REQUIRED) + "% by " + sender.getName() + ".");
        } else {
            sender.sendMessage("make sure the first argument is a number inbetween 0 and 100.");
        }

        return success;
    }

    private boolean onCommandUpdateInterval(CommandSender sender, Command command, String label, String[] args) {
        boolean success = false;
        if (args.length == 1) {
            try {
                int value = Integer.parseInt(args[0]);
                if (0 < value) {
                    getConfig().set(CONF_UID_UPDATE_INTERVAL, value);
                    success = true;
                } else {
                    success = false;
                }
            } catch (NumberFormatException e) {
                success = false;
            }
        }
        if (success) {
            Bukkit.broadcastMessage("weather update interval has been set to " + getConfig().getInt(CONF_UID_UPDATE_INTERVAL) + " seconds by " + sender.getName() + ".");
        } else {
            sender.sendMessage("make sure the first argument is a number greater than 0.");
        }
        return success;
    }

    @EventHandler
    public void onWeatherChange(WeatherChangeEvent event) {
        event.setCancelled(event.toWeatherState() != canRain);
    }

    @EventHandler
    public void onThunderChange(ThunderChangeEvent event) {
        event.setCancelled(event.toThunderState() != canThunder);
    }

    @Override
    public void run() {
        updateRaininess();
        updateThunderness();
        int duration = getConfig().getInt(CONF_UID_UPDATE_INTERVAL);
        for (World world : getServer().getWorlds()) {
            if (world.getEnvironment().compareTo(World.Environment.NORMAL) == 0) {
                world.setWeatherDuration(duration);
                world.setStorm(canRain);
                world.setThundering(canThunder);
            }
        }
    }

    private void updateThunderness() {
        int thunderness = (new Random()).nextInt(100) + 1;
        int required = getConfig().getInt(CONF_UID_THUNDERNESS_REQUIRED);
        canThunder = required < thunderness;
    }

    private void updateRaininess() {
        int raininess = (new Random()).nextInt(100) + 1;
        int required = getConfig().getInt(CONF_UID_RAININESS_REQUIRED);
        canRain = required < raininess;
    }
}
