package com.kiwifisher.mobstacker;


import java.io.*;
import java.util.logging.Logger;
import java.util.*;

import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.plugin.Plugin;

import com.google.common.io.ByteStreams;
import com.kiwifisher.mobstacker.commands.MobStackerCommands;
import com.kiwifisher.mobstacker.listeners.*;
import com.kiwifisher.mobstacker.listeners.EntityTrackListener;
import com.kiwifisher.mobstacker.utils.StackUtils;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;

public final class MobStacker extends JavaPlugin implements Listener {

    private static final Logger LOGGER = Logger.getLogger("MobStacker");

    private static FileConfiguration config;
    public static MobStacker plugin;

    private boolean stacking = true;
    private boolean worldGuardEnabled;
    private WorldGuardPlugin worldGuard;
    private ArrayList<String> regionsArray = new ArrayList<>();
    private int searchTime = getConfig().getInt("seconds-to-try-stack") * 20;
    private StackUtils stackUtils;
    private boolean mcMMO = false;
    public final static String RELOAD_UUID = UUID.randomUUID().toString().replaceAll("-", "").substring(0, 12);
    private String LAST_USED_UUID;
    private YamlConfiguration uuidConfig = new YamlConfiguration();

    final String uid = "%%__USER__%%";
    final String rid = "%%__RESOURCE__%%";
    final String nonce = "%%__NONCE__%%";

    HashMap<UUID, Integer> killsHash = new HashMap<>();

    @Override
    public void onEnable() {
        plugin = this;

        log("MobStacker is starting");
        loadResource(this, "config.yml");

        try {
            this.uuidConfig.load(loadResource(this, "uuid.yml"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        this.stackUtils = new StackUtils(this);

        this.LAST_USED_UUID = uuidConfig.getString("last-used-UUID");

        this.mcMMO = getServer().getPluginManager().isPluginEnabled("mcMMO");

        if (usesmcMMO()) {
            log("Hooked in to mcMMO successfully!");
        }

        worldGuard = initialiseWorldGuard();
        if (worldGuard == null) {
            log("Didn't hook in to WorldGuard");
            setUsesWorldGuard(false);

        } else {
            loadResource(worldGuard, "mobstacker-excluded-regions.yml");
            log("Successfully hooked in to WorldGuard!");
            setUsesWorldGuard(true);
            updateExcludedRegions();
        }

        this.getServer().getPluginManager().registerEvents(new EntityTrackListener(), this);
        this.getServer().getPluginManager().registerEvents(new MobSpawnListener(), this);
        this.getServer().getPluginManager().registerEvents(new MobDeathListener(), this);
        this.getServer().getPluginManager().registerEvents(new PlayerRenameEntityListener(), this);
        this.getServer().getPluginManager().registerEvents(new EntityTameListener(), this);
        this.getServer().getPluginManager().registerEvents(new EntityExplodeListener(), this);
        this.getServer().getPluginManager().registerEvents(new PlayerLeashEntityListener(), this);
        this.getServer().getPluginManager().registerEvents(new PlayerShearEntityListener(), this);
        this.getServer().getPluginManager().registerEvents(new SheepDyeListener(), this);
        this.getServer().getPluginManager().registerEvents(new SheepRegrowWoolListener(), this);
        this.getServer().getPluginManager().registerEvents(new ChunkChangeListener(), this);
        this.getServer().getPluginManager().registerEvents(new MobDamageListener(), this);

        getCommand("mobstacker").setExecutor(new MobStackerCommands());

        for (World world : getServer().getWorlds()) {

            Entity[] entities = new Entity[world.getEntities().size()];
            getStackUtils().reviveStacks(world.getEntities().toArray(entities));

        }

        log("MobStacker has successfully started!");

    }

    @Override
    public void onDisable() {
        for (World world : getServer().getWorlds()) {
            for (Entity entity : world.getEntities()) {
                if (StackUtils.hasRequiredData(entity) && !entity.getMetadata("spawn-reason").isEmpty()) {
                    int quantity = StackUtils.getStackSize((LivingEntity) entity);
                    ((LivingEntity)entity).setCustomName(quantity + "-" + MobStacker.RELOAD_UUID + "-" + entity.getMetadata("spawn-reason").get(0).asString());
                }
            }
        }
        this.uuidConfig.set("last-used-UUID", RELOAD_UUID);
        try {
            this.uuidConfig.save(getDataFolder().getAbsolutePath() + "/uuid.yml");
        } catch (Exception e) {
            e.printStackTrace();
        }
        log("Thanks for using MobStacker!");
    }

    public static MobStacker getInstance() { return plugin; }

    public void log(String string) { LOGGER.info(string); }

    public File loadResource(Plugin plugin, String resource) {
        File folder = plugin.getDataFolder();
        if (!folder.exists())
            folder.mkdir();
        File resourceFile = new File(folder, resource);

        try {
            if (!resourceFile.exists() && resourceFile.createNewFile()) {
                try (InputStream in = plugin.getResource(resource);
                     OutputStream out = new FileOutputStream(resourceFile)) {
                    ByteStreams.copy(in, out);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return resourceFile;
    }

    public boolean usesmcMMO() { return this.mcMMO; }

    private WorldGuardPlugin initialiseWorldGuard() {

        Plugin worldGuard = getServer().getPluginManager().getPlugin("WorldGuard");

        if (worldGuard == null || !(worldGuard instanceof WorldGuardPlugin) || !Bukkit.getVersion().contains("1.8")) {
            return null;
        }

        return (WorldGuardPlugin) worldGuard;
    }

    public void setUsesWorldGuard(boolean status) { worldGuardEnabled = status; }

    public void updateExcludedRegions() {
        try {
            Scanner scanner = new Scanner(new File(getWorldGuard().getDataFolder() + "/mobstacker-excluded-regions.yml"));
            regionsArray.clear();

            while (scanner.hasNextLine()) {
                String region = scanner.nextLine();
                regionsArray.add(region);
            }

            scanner.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        for (String id : regionsArray) {
            log("Loaded in exclusions for region: " + id);
        }
    }

    public StackUtils getStackUtils() { return stackUtils; }

    public WorldGuardPlugin getWorldGuard() { return worldGuard; }

    public void setSearchTime(int searchTime) { this.searchTime = searchTime; }

    public boolean isStacking() { return stacking; }

    public boolean usesWorldGuard() { return worldGuardEnabled; }

    public boolean regionAllowedToStack(String regionID) { return !regionsArray.contains(regionID); }

    public String getLAST_USED_UUID() { return this.LAST_USED_UUID; }

    public int getSearchTime() { return searchTime; }

    public void setStacking(boolean bool) { stacking = bool; }

    public void removeAllStacks() {
        for (World world : getServer().getWorlds()) {
            for (LivingEntity entity : world.getLivingEntities()) {
                if (StackUtils.hasRequiredData(entity)) {
                    entity.remove();
                }
            }
        }
    }
}
