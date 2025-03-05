package com.kiwifisher.mobstacker.listeners;

import com.kiwifisher.mobstacker.algorithms.AlgorithmEnum;
import com.kiwifisher.mobstacker.utils.StackUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.material.Colorable;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.RegisteredListener;

import java.util.List;

public class MobDeathListener extends MobStackerListener {


    @EventHandler (ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void mobDeathListener(EntityDeathEvent event) {
        //check if the plugin is active
        if (plugin.isStacking()) {
            LivingEntity entity = event.getEntity();

            //if the entity was a stack:
            if (StackUtils.hasRequiredData(entity)) {
                List<String> validDeathReasons = plugin.getConfig().getStringList("kill-whole-stack-on-death.reasons");
                //if the damage source was a fall:
                if (event.getEntity().getLastDamageCause() != null && plugin.getConfig().getBoolean("kill-whole-stack-on-death.enable") && validDeathReasons.contains(event.getEntity().getLastDamageCause().getCause().name())) {
                    int quantity = StackUtils.getStackSize(entity);
                    /*
                    If we are dropping proportionate loot, then follow.
                     */
                    if (plugin.getConfig().getBoolean("kill-whole-stack-on-death.multiply-loot") && quantity > 1) {
                            /*
                            Try to drop the proportionate loot.
                            */
                        try {
                            /**
                             * If it's a magma cube and is too small to drop loot then we don't drop anything
                             */
                            if (entity.getType() == EntityType.MAGMA_CUBE && ((MagmaCube) entity).getSize() <= 1) {
                                return;
                            }
                            if (entity.getType() == EntityType.MAGMA_CUBE && ((MagmaCube) entity).getSize() > 1) {
                                MagmaCube magmaCube = (MagmaCube) entity;
                                magmaCube.setSize(1);
                            }
                            event.getDrops().addAll(AlgorithmEnum.valueOf(entity.getType().name()).getLootAlgorithm().getRandomLoot(entity, quantity - 1));
                            /*
                            If this fails, then log which entity and request its implementation.
                             */
                        }
                        catch (Exception e) {
                            plugin.log(e.getMessage());
                            plugin.log(entity.getType().name() + " doesn't have proportionate loot implemented - please request it be added if you need it");
                            /*
                            Regardless of it failing, drop the proportionate EXP.
                             */
                        }
                        finally {
                            event.setDroppedExp(event.getDroppedExp() * quantity);
                        }
                    }
                    return;
                }

                //any other damage source:
                int newStackSize = StackUtils.getStackSize(entity) - 1;
                Location entityLocation = entity.getLocation();
                EntityType entityType = entity.getType();
                boolean maxStack = entity.getMetadata("max-stack").get(0).asBoolean();

                if (newStackSize > 0) {
                    //remove the meta that allows mobs to stack to the entity that is dying; Entity.isDead() doesn't work yet
                    entity.removeMetadata("quantity", plugin);

                    if (maxStack) {
                        plugin.setSearchTime(-50);
                    }

                    //spawn the replacement entity
                    LivingEntity newEntity = (LivingEntity) entity.getLocation().getWorld().spawnEntity(entityLocation, entityType);
                    /**
                     * Mobs spawned by a blacklisted method will not give mcmmo points.
                     */
                    List<String> noMcmmoSpawnReasons = plugin.getConfig().getStringList("no-mcmmo-exp");
                    if (plugin.usesmcMMO() && noMcmmoSpawnReasons.contains(entity.getMetadata("spawn-reason").get(0).toString())) {
                        newEntity.setMetadata("mcMMO: Spawned Entity", new FixedMetadataValue(Bukkit.getPluginManager().getPlugin("mcMMO"), true));
                    }

                    //Continue spawning the stack.
                    if (entity.hasMetadata("spawn-reason")) {
                        String oldSpawnReason = entity.getMetadata("spawn-reason").get(0).asString();
                        newEntity.removeMetadata("spawn-reason", plugin);
                        newEntity.setMetadata("spawn-reason", new FixedMetadataValue(plugin, oldSpawnReason));
                    }

                    //If the entity was in fire, or burning, then any remaining ticks left on the previous mob will be passed on to the new one.
                    if (entity.getLastDamageCause() != null && (entity.getLastDamageCause().getCause() == EntityDamageEvent.DamageCause.FIRE ||
                            entity.getLastDamageCause().getCause() == EntityDamageEvent.DamageCause.FIRE_TICK) && plugin.getConfig().getBoolean("carry-over-fire.enabled")) {
                        if(!plugin.getConfig().getBoolean("carry-over-fire.start-new-burn")) {
                            newEntity.setFireTicks(entity.getFireTicks());
                        }
                        else if(plugin.getConfig().getBoolean("carry-over-fire.start-new-burn")) {
                            newEntity.setFireTicks(entity.getMaxFireTicks());
                        }
                    }

                    //assign attributes so the mob looks the same as the one that died.
                    if (newEntity instanceof Ageable) {
                        ((Ageable) newEntity).setAge(((Ageable) event.getEntity()).getAge());
                    }
                    if (newEntity instanceof Colorable) {
                        ((Colorable) newEntity).setColor(((Colorable) event.getEntity()).getColor());
                    }
                    if (newEntity instanceof Sheep) {
                        ((Sheep) newEntity).setSheared(((Sheep) event.getEntity()).isSheared());
                    }

                    //set new meta
                    plugin.getStackUtils().setMaxStack(newEntity, maxStack);
                    plugin.getStackUtils().setStackSize(newEntity, newStackSize);

                    if (newStackSize > 1) {
                        plugin.getStackUtils().renameStack(newEntity, newStackSize);
                    }

                    //reset search time
                    plugin.setSearchTime(plugin.getConfig().getInt("seconds-to-try-stack") * 20);
                }
            }
        }
    }
}