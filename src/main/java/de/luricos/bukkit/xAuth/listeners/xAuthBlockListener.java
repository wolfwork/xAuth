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

import de.luricos.bukkit.xAuth.event.block.xAuthBlockBreakEvent;
import de.luricos.bukkit.xAuth.event.block.xAuthBlockPlaceEvent;
import de.luricos.bukkit.xAuth.event.xAuthEventProperties;
import de.luricos.bukkit.xAuth.xAuthPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

public class xAuthBlockListener extends xAuthEventListener {

    public xAuthBlockListener() {
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (this.isAllowed(player, event, event.getBlock()))
            return;

        xAuthPlayer xp = playerManager.getPlayer(player.getName());
        playerManager.sendNotice(xp);
        event.setCancelled(true);

        xAuthEventProperties properties = new xAuthEventProperties();
        properties.setProperty("action", xAuthBlockBreakEvent.Action.BLOCK_BREAK_CANCELLED);
        properties.setProperty("status", xp.getStatus());
        properties.setProperty("playername", player.getName());
        properties.setProperty("blocktype", event.getBlock().getType().name());
        this.callEvent(new xAuthBlockBreakEvent(properties));
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        if (this.isAllowed(player, event, event.getBlock()))
            return;

        xAuthPlayer xp = playerManager.getPlayer(player.getName());
        playerManager.sendNotice(xp);
        event.setCancelled(true);

        xAuthEventProperties properties = new xAuthEventProperties();
        properties.setProperty("action", xAuthBlockPlaceEvent.Action.BLOCK_PLACE_CANCELLED);
        properties.setProperty("status", xp.getStatus());
        properties.setProperty("playername", player.getName());
        properties.setProperty("blocktype", event.getBlock().getType().name());
        this.callEvent(new xAuthBlockPlaceEvent(properties));
    }
}