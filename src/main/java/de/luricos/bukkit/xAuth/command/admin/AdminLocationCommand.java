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
import de.luricos.bukkit.xAuth.event.command.admin.xAuthCommandAdminLocationEvent;
import de.luricos.bukkit.xAuth.event.xAuthEventProperties;
import de.luricos.bukkit.xAuth.utils.xAuthLog;
import de.luricos.bukkit.xAuth.xAuth;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

/**
 * @author lycano
 */
public class AdminLocationCommand extends xAuthAdminCommand {

    public AdminLocationCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof ConsoleCommandSender) {
            xAuthLog.info("This command cannot be executed from the console!");
            this.setResult(true);
            return;
        }

        Player player = (Player) sender;
        if (!this.isAllowedCommand(player, "admin.permission", "xauth.location")) {
            this.setResult(true);
            return;
        }

        if (args.length < 2 || !(args[1].equals("set") || args[1].equals("remove"))) {
            this.getMessageHandler().sendMessage("admin.location.usage", player);
            this.setResult(true);
            return;
        }

        String action = args[1];
        boolean global = args.length > 2 && args[2].equals("global");
        String response;

        xAuthEventProperties properties = new xAuthEventProperties();
        properties.setProperty("issuedby", player.getName());
        if (action.equals("set")) {
            if (!global && player.getWorld().getUID().equals(xAuth.getPlugin().getLocationManager().getGlobalUID())) {
                this.getMessageHandler().sendMessage("admin.location.set.error.global", player);
                this.setResult(true);

                properties.setProperty("action", xAuthCommandAdminLocationEvent.Action.ERROR_SET_GLOBAL);
                this.callEvent(new xAuthCommandAdminLocationEvent(properties));
                return;
            }

            boolean success = xAuth.getPlugin().getLocationManager().setLocation(player.getLocation(), global);
            if (success)
                response = "admin.location.set.success." + (global ? "global" : "regular");
            else
                response = "admin.location.set.error.general";
        } else {
            if (global) {
                if (xAuth.getPlugin().getLocationManager().getGlobalUID() == null) {
                    this.getMessageHandler().sendMessage("admin.location.remove.error.noglobal", player);
                    this.setResult(true);

                    properties.setProperty("action", xAuthCommandAdminLocationEvent.Action.ERROR_NO_GLOBAL);
                    this.callEvent(new xAuthCommandAdminLocationEvent(properties));
                    return;
                }
            } else {
                if (!xAuth.getPlugin().getLocationManager().isLocationSet(player.getWorld())) {
                    this.getMessageHandler().sendMessage("admin.location.remove.error.notset", player);
                    this.setResult(true);

                    properties.setProperty("action", xAuthCommandAdminLocationEvent.Action.ERROR_NOT_SET);
                    this.callEvent(new xAuthCommandAdminLocationEvent(properties));
                    return;
                } else if (player.getWorld().getUID().equals(xAuth.getPlugin().getLocationManager().getGlobalUID())) {
                    this.getMessageHandler().sendMessage("admin.location.remove.error.global", player);
                    this.setResult(true);

                    properties.setProperty("action", xAuthCommandAdminLocationEvent.Action.ERROR_REMOVE_GLOBAL);
                    this.callEvent(new xAuthCommandAdminLocationEvent(properties));
                    return;
                }
            }

            boolean success = xAuth.getPlugin().getLocationManager().removeLocation(player.getWorld());
            if (success) {
                if (global) {
                    response = "admin.location.remove.success.global";
                    properties.setProperty("action", xAuthCommandAdminLocationEvent.Action.SUCCESS_REMOVE_GLOBAL);
                } else {
                    response = "admin.location.remove.success.regular";
                    properties.setProperty("action", xAuthCommandAdminLocationEvent.Action.SUCCESS_REMOVE_REGULAR);
                }
            } else {
                response = "admin.location.remove.error.general";
                properties.setProperty("action", xAuthCommandAdminLocationEvent.Action.ERROR_GENERAL);
            }
        }

        this.getMessageHandler().sendMessage(response, player);

        this.callEvent(new xAuthCommandAdminLocationEvent(properties));
        this.setResult(true);
    }
}
