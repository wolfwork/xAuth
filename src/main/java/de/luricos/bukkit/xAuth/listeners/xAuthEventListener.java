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

import de.luricos.bukkit.xAuth.PlayerManager;
import de.luricos.bukkit.xAuth.event.xAuthEvent;
import de.luricos.bukkit.xAuth.permissions.provider.PlayerPermissionHandler;
import de.luricos.bukkit.xAuth.xAuth;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;

public class xAuthEventListener implements Listener {
    protected final PlayerManager playerManager;

    public xAuthEventListener() {
        this.playerManager = xAuth.getPlugin().getPlayerManager();
    }

    protected void callEvent(final xAuthEvent event) {
        Bukkit.getPluginManager().callEvent(event);
    }

    protected boolean isAllowed(final Player player, final Event event, final Object... obj) {
        return new PlayerPermissionHandler(player, event.getEventName(), obj).hasPermission();
    }

    protected boolean isAllowedCommand(final Player player, final String... command) {
        return new PlayerPermissionHandler(player, "PlayerCommandPreProcessEvent", command).hasPermission();
    }

}