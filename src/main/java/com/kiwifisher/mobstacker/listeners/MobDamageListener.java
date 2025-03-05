package com.kiwifisher.mobstacker.listeners;

import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftLivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

public class MobDamageListener extends MobStackerListener {

    @EventHandler (priority = EventPriority.MONITOR)
    public void onMobDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof CraftLivingEntity)) {
            return;
        }
        CraftLivingEntity entity = (CraftLivingEntity) event.getEntity();
        double damage = event.getFinalDamage();
        if (entity.getHealth() > damage) {
            return;
        }
        if (entity.getCustomName() != null && entity.getCustomName().substring(0, 4).equalsIgnoreCase(plugin.getConfig().getString("stack-naming").substring(0, 4).replace("&","§"))) {
            entity.setCustomName(null);
            entity.setCustomNameVisible(false);
            event.setCancelled(true);
            entity.damage(event.getFinalDamage(),event.getDamager());
        }
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void onOtherMobDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof CraftLivingEntity)) {
            return;
        }
        CraftLivingEntity entity = (CraftLivingEntity) event.getEntity();
        double damage = event.getFinalDamage();
        if (entity.getHealth() > damage) {
            return;
        }
        if (entity.getCustomName() != null && entity.getCustomName().substring(0, 4).equalsIgnoreCase(plugin.getConfig().getString("stack-naming").substring(0, 4).replace("&","§"))) {
            entity.setCustomName(null);
            entity.setCustomNameVisible(false);
        }
    }
}
