package com.kiwifisher.mobstacker.listeners;

import com.kiwifisher.mobstacker.MobStacker;

import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.metadata.FixedMetadataValue;

public class MobSpawnListener implements Listener {

    private final MobStacker plugin;

    public MobSpawnListener(MobStacker plugin) {
        this.plugin = plugin;
    }


    @EventHandler
    public void mobSpawnEvent(CreatureSpawnEvent event) {

        final LivingEntity spawnedCreature = event.getEntity();
        final CreatureSpawnEvent.SpawnReason spawnReason = event.getSpawnReason();

        // Check if the spawned entity is stackable.
        if (!getPlugin().getStackUtils().isStackable(spawnedCreature, spawnReason, false)) {
            return;
        }

        /*
        Set stack size to 1 and max stack to false;
         */
        getPlugin().getStackUtils().setStackSize(spawnedCreature, 1);
        getPlugin().getStackUtils().setMaxStack(spawnedCreature, false);

        /*
        Check if the mob is from a spawner, and add the tag so that when it dies we can have continuity for
        nerf-spawner-mobs
         */
        if (!spawnedCreature.hasMetadata("spawn-reason")) {

            spawnedCreature.setMetadata("spawn-reason", new FixedMetadataValue(getPlugin(), spawnReason.name()));

        }

        /*
        Make sure search time is positive and try to stack.
         */
        if (getPlugin().getSearchTime() >= -20) {
            getPlugin().getStackUtils().attemptToStack(getPlugin().getSearchTime(), spawnedCreature, spawnReason);
        }

    }





    public MobStacker getPlugin() {
        return plugin;
    }
}
