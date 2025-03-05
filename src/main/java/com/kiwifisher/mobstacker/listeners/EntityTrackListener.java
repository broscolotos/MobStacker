package com.kiwifisher.mobstacker.listeners;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityTargetEvent;

import java.util.List;

public class EntityTrackListener extends MobStackerListener {


    @EventHandler
    public void onEntityTargetEvent(EntityTargetEvent event) {
        List<String> nerfedSpawnTypes = plugin.getConfig().getStringList("mob-nerfing");
        Entity entity = event.getEntity();

        if (event.getTarget() instanceof Player && entity.hasMetadata("spawn-reason")) {
            String spawnReason = entity.getMetadata("spawn-reason").get(0).asString();
            if (nerfedSpawnTypes.contains(spawnReason)) {
                event.setCancelled(true);
            }
        }
    }
}


