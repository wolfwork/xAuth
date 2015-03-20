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
package de.luricos.bukkit.xAuth.command;

import de.luricos.bukkit.xAuth.MessageHandler;
import de.luricos.bukkit.xAuth.PlayerManager;
import de.luricos.bukkit.xAuth.event.xAuthEvent;
import de.luricos.bukkit.xAuth.xAuth;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

/**
 * @author lycano
 */
public abstract class xAuthCommand {

    protected PlayerManager playerManager = null;

    public PlayerManager getPlayerManager() {
        if (this.playerManager == null)
            this.playerManager = xAuth.getPlugin().getPlayerManager();

        return this.playerManager;
    }

    private MessageHandler messageHandler = xAuth.getPlugin().getMessageHandler();

    protected void callEvent(final xAuthEvent event) {
        Bukkit.getPluginManager().callEvent(event);
    }

    protected abstract boolean isAllowedCommand(CommandSender sender, String messageNode, String... command);

    protected MessageHandler getMessageHandler() {
        return this.messageHandler;
    }

}
