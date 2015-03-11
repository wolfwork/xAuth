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
package de.luricos.bukkit.xAuth.command.admin;

import de.luricos.bukkit.xAuth.command.xAuthAdminCommand;
import de.luricos.bukkit.xAuth.event.command.admin.xAuthCommandAdminDebugEvent;
import de.luricos.bukkit.xAuth.event.xAuthEventProperties;
import de.luricos.bukkit.xAuth.utils.xAuthLog;
import de.luricos.bukkit.xAuth.xAuth;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.logging.Level;

/**
 * @author lycano
 */
public class AdminDebugCommand extends xAuthAdminCommand {

    public AdminDebugCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!this.isAllowedCommand(sender, "admin.permission", "xauth.config")) {
            this.setResult(true);
            return;
        }

        if (!xAuth.getPermissionManager().has(sender, "xauth.allow.player.command.xauth.config")) {
            this.getMessageHandler().sendMessage("admin.permission", sender);
            this.setResult(true);
            return;
        }

        if (args.length == 1) {
            this.getMessageHandler().sendMessage(String.format(this.getMessageHandler().getNode("admin.debug"), xAuthLog.getLevel().toString()), sender);
            this.setResult(true);
            return;
        }

        Level toLevel = Level.INFO;
        if  (!(args[1] == null) && (!(args[1].isEmpty()))) {
            toLevel = Level.parse(args[1].toUpperCase());
            xAuthLog.setLevel(toLevel);

            xAuthEventProperties properties = new xAuthEventProperties();
            properties.setProperty("action", xAuthCommandAdminDebugEvent.Action.LOGLEVEL_CHANGED);
            properties.setProperty("issuedby", sender.getName());
            properties.setProperty("changedto", toLevel);
            this.callEvent(new xAuthCommandAdminDebugEvent(properties));
        }

        this.getMessageHandler().sendMessage(String.format(this.getMessageHandler().getNode("admin.debug"), toLevel.toString()), sender);
        this.setResult(true);
    }

}
