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
package de.luricos.bukkit.xAuth.commands;

import de.luricos.bukkit.xAuth.restrictions.PlayerRestrictionHandler;
import de.luricos.bukkit.xAuth.xAuth;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.RemoteConsoleCommandSender;
import org.bukkit.entity.Player;

/**
 * @author lycano
 */
public class xAuthPlayerCommand extends xAuthCommand {

    protected boolean isAllowedCommand(final CommandSender sender, final String messageNode, final String... command) {
        if (sender instanceof Player) {
            return this.isAllowedCommand((Player) sender, messageNode, command);
        }

        if ((sender instanceof ConsoleCommandSender) || (sender instanceof RemoteConsoleCommandSender)) {
            this.getMessageHandler().sendMessage("misc.command-from-console", sender, command[0]);
        }

        return (!((sender instanceof ConsoleCommandSender) || (sender instanceof RemoteConsoleCommandSender)));
    }

    protected boolean isAllowedCommand(final Player player, final String messageNode, final String... command) {
        boolean allowed = new PlayerRestrictionHandler(player, "PlayerCommandPreProcessEvent", command).hasPermission();
        if (!allowed)
            xAuth.getPlugin().getMessageHandler().sendMessage(messageNode, player);

        return allowed;
    }

}
