package com.kiwifisher.mobstacker.listeners;

import java.util.List;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTargetEvent;

import com.kiwifisher.mobstacker.MobStacker;

public class EntityTrackListener implements Listener {

    MobStacker plugin;

    public EntityTrackListener(MobStacker plugin) {

        this.plugin = plugin;

    }

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


