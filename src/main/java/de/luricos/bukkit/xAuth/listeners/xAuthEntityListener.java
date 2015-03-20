/*
 * xAuth for Bukkit
 * Copyright (C) 2012 Lycano <https://github.com/lycano/xAuth/>
 *
 * Copyright (C) 2011 CypherX <https://github.com/CypherX/xAuth/>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.luricos.bukkit.xAuth.listeners;

import de.luricos.bukkit.xAuth.event.entity.xAuthEntityTargetEvent;
import de.luricos.bukkit.xAuth.event.player.*;
import de.luricos.bukkit.xAuth.event.xAuthEventProperties;
import de.luricos.bukkit.xAuth.xAuthPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.*;

public class xAuthEntityListener extends xAuthEventListener {

    public xAuthEntityListener() {
    }

    public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent event) {
        Entity damager = event.getDamager();
        if (!(damager instanceof Player))
            return;

        Player damagee = (Player) damager;
        if (this.isAllowed(damagee, event, damagee))
            return;

        xAuthPlayer xp = this.playerManager.getPlayer(damagee.getName());
        this.playerManager.sendNotice(xp);
        event.setCancelled(true);

        xAuthEventProperties properties = new xAuthEventProperties();
        properties.setProperty("action", xAuthPlayerDamageByEntityEvent.Action.DAMAGE_BY_ENTITY_CANCELLED);
        properties.setProperty("status", xp.getStatus());
        properties.setProperty("damagee", damagee.getName());
        properties.setProperty("damager", ((Player) damager).getPlayer().getName());
        properties.setProperty("playername", xp.getName());
        this.callEvent(new xAuthPlayerDamageByEntityEvent(properties));
    }


    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof Player))
            return;

        Player player = (Player) entity;
        xAuthPlayer xp = this.playerManager.getPlayer(player.getName());

        if ((!this.isAllowed(player, event, player)) || this.playerManager.hasGodMode(xp, event.getCause())) {
            event.setCancelled(true);

            xAuthEventProperties properties = new xAuthEventProperties();
            properties.setProperty("action", xAuthPlayerDamageEvent.Action.PLAYER_DAMAGE_CANCELLED);
            properties.setProperty("status", xp.getStatus());
            properties.setProperty("damage", event.getDamage());
            properties.setProperty("playername", player.getName());
            this.callEvent(new xAuthPlayerDamageEvent(properties));
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onEntityTarget(EntityTargetEvent event) {
        Entity target = event.getTarget();
        if (!(target instanceof Player))
            return;

        Player player = (Player) target;
        if (this.isAllowed(player, event, target))
            return;

        event.setCancelled(true);

        xAuthEventProperties properties = new xAuthEventProperties();
        properties.setProperty("action", xAuthEntityTargetEvent.Action.ENTITY_TARGET_CANCELLED);
        properties.setProperty("reason", event.getReason());
        properties.setProperty("playername", player.getName());
        this.callEvent(new xAuthEntityTargetEvent(properties));
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof Player))
            return;

        Player player = (Player) entity;
        if (this.isAllowed(player, event, player))
            return;

        event.setCancelled(true);

        xAuthEventProperties properties = new xAuthEventProperties();
        properties.setProperty("action", xAuthPlayerFoodLevelChangeEvent.Action.FOODLEVEL_CHANGE_CANCELLED);
        properties.setProperty("status", this.playerManager.getPlayer(player.getName()).getStatus());
        properties.setProperty("foodlevel", event.getFoodLevel());
        properties.setProperty("playername", player.getName());
        this.callEvent(new xAuthPlayerFoodLevelChangeEvent(properties));
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPotionSplash(PotionSplashEvent event) {
        for (LivingEntity entity : event.getAffectedEntities()) {
            if (!(entity instanceof Player))
                continue;

            Player player = (Player) entity;
            if (this.isAllowed(player, event, player))
                continue;

            // dont allow splashes (set to 0) when the entity does not have the permission
            event.setIntensity(entity, 0);

            xAuthEventProperties properties = new xAuthEventProperties();
            properties.setProperty("action", xAuthPlayerPotionSplashEvent.Action.POTION_SPLASH_CANCELLED);
            properties.setProperty("status", this.playerManager.getPlayer(player.getName()).getStatus());
            properties.setProperty("potiontype", event.getPotion().getItem().getData().getItemType().name());
            properties.setProperty("playername", player.getName());
            this.callEvent(new xAuthPlayerPotionSplashEvent(properties));
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onEntityRegainHealth(EntityRegainHealthEvent event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof Player))
            return;

        Player player = (Player) entity;
        if (this.isAllowed(player, event, player))
            return;

        event.setCancelled(true);

        xAuthEventProperties properties = new xAuthEventProperties();
        properties.setProperty("action", xAuthPlayerRegainHealthEvent.Action.REGAIN_HEALTH_CANCELLED);
        properties.setProperty("status", this.playerManager.getPlayer(player.getName()).getStatus());
        properties.setProperty("amount", event.getAmount());
        properties.setProperty("regainreason", event.getRegainReason());
        properties.setProperty("playername", player.getName());
        this.callEvent(new xAuthPlayerRegainHealthEvent(properties));
    }
}