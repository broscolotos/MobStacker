package com.kiwifisher.mobstacker.listeners;

import com.kiwifisher.mobstacker.MobStacker;
import com.kiwifisher.mobstacker.utils.StackUtils;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

import java.util.List;

public class ChunkChangeListener extends MobStackerListener {


    @EventHandler
    public void mobUnloadEvent(ChunkUnloadEvent event) {
        for (Entity entity : event.getChunk().getEntities()) {
            if (StackUtils.hasRequiredData(entity)) {
                int quantity = StackUtils.getStackSize((LivingEntity) entity);
                ((LivingEntity) entity).setCustomName(quantity + "-" + MobStacker.RELOAD_UUID + "-" + entity.getMetadata("spawn-reason").get(0).asString());
            }
        }
    }

    @EventHandler
    public void mobLoadEvent(ChunkLoadEvent event) {
        // Check if loading stacks and stacking is enabled in this world.
        if (!plugin.getConfig().getBoolean("load-existing-stacks.enabled") || !plugin.getStackUtils().isStackable(event.getWorld())) {
            return;
        }
        // Get acceptable mob types
        List<String> types = plugin.getConfig().getStringList("load-existing-stacks.mob-types");
        // Check if any mob types are acceptable
        if (types.isEmpty()) {
            return;
        }
        plugin.getStackUtils().reviveStacks(event.getChunk().getEntities());
    }
}
