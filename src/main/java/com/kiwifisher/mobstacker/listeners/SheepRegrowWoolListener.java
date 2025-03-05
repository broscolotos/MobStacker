package com.kiwifisher.mobstacker.listeners;

import com.kiwifisher.mobstacker.MobStacker;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.SheepRegrowWoolEvent;

public class SheepRegrowWoolListener extends MobStackerListener {

    @EventHandler
    public void regrowEvent(SheepRegrowWoolEvent event) {
        /*
        When a sheep regrows it's wool, try to stack it.
         */
        plugin.getStackUtils().attemptToStack(0, event.getEntity(), CreatureSpawnEvent.SpawnReason.CUSTOM);
    }
}
