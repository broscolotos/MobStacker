package com.kiwifisher.mobstacker.listeners;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.metadata.FixedMetadataValue;

public class MobSpawnListener extends MobStackerListener {


    @EventHandler
    public void mobSpawnEvent(CreatureSpawnEvent event) {
        final LivingEntity spawnedCreature = event.getEntity();
        final CreatureSpawnEvent.SpawnReason spawnReason = event.getSpawnReason();
        // Check if the spawned entity is stackable.
        if (!plugin.getStackUtils().isStackable(spawnedCreature, spawnReason, false)) {
            return;
        }
        /*
        Set stack size to 1 and max stack to false;
         */
        plugin.getStackUtils().setStackSize(spawnedCreature, 1);
        plugin.getStackUtils().setMaxStack(spawnedCreature, false);
        /*
        Check if the mob is from a spawner, and add the tag so that when it dies we can have continuity for
        nerf-spawner-mobs
         */
        if (!spawnedCreature.hasMetadata("spawn-reason")) {
            spawnedCreature.setMetadata("spawn-reason", new FixedMetadataValue(plugin, spawnReason.name()));
        }
        /*
        Make sure search time is positive and try to stack.
         */
        if (plugin.getSearchTime() >= -20) {
            plugin.getStackUtils().attemptToStack(plugin.getSearchTime(), spawnedCreature, spawnReason);
        }
    }
}
