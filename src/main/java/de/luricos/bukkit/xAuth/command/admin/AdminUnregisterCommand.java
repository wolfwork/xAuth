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
import de.luricos.bukkit.xAuth.event.command.admin.xAuthCommandAdminUnregisterEvent;
import de.luricos.bukkit.xAuth.event.xAuthEventProperties;
import de.luricos.bukkit.xAuth.xAuth;
import de.luricos.bukkit.xAuth.xAuthPlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * @author lycano
 */
public class AdminUnregisterCommand extends xAuthAdminCommand {

    public AdminUnregisterCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(this.isAllowedCommand(sender, "admin.permission", "xauth.unregister"))) {
            this.setResult(true);
            return;
        }

        if (args.length < 2) {
            this.getMessageHandler().sendMessage("admin.unregister.usage", sender);
            this.setResult(true);
            return;
        }

        String targetName = args[1];
        xAuthPlayer xp = xAuth.getPlugin().getPlayerManager().getPlayer(targetName);

        xAuthEventProperties properties = new xAuthEventProperties();
        properties.setProperty("issuedby", sender.getName());
        properties.setProperty("targetid", xp.getAccountId());
        properties.setProperty("targetname", xp.getName());
        properties.setProperty("status", xp.getStatus());

        if (!xp.isRegistered()) {
            this.getMessageHandler().sendMessage("admin.unregister.error.registered", sender, targetName);
            this.setResult(true);

            properties.setProperty("action", xAuthCommandAdminUnregisterEvent.Action.ERROR_REGISTERED);
            this.callEvent(new xAuthCommandAdminUnregisterEvent(properties));
            return;
        }

        boolean success = xAuth.getPlugin().getPlayerManager().deleteAccount(xp.getAccountId());
        if (success) {
            xp.setStatus(xAuthPlayer.Status.GUEST);
            xAuth.getPlugin().getAuthClass(xp).offline(xp.getName());
            this.getMessageHandler().sendMessage("admin.unregister.success.player", sender, targetName);

            Player target = xp.getPlayer();
            if (target != null) {
                xAuth.getPlugin().getPlayerManager().protect(xp);
                this.getMessageHandler().sendMessage("admin.unregister.success.target", target);
            }

            xAuth.getPlugin().getPlayerManager().initAccount(xp.getAccountId());

            properties.setProperty("action", xAuthCommandAdminUnregisterEvent.Action.SUCCESS_UNREGISTER);
        } else {
            properties.setProperty("action", xAuthCommandAdminUnregisterEvent.Action.ERROR_GENERAL);
            this.getMessageHandler().sendMessage("admin.unregister.error.general", sender);
        }

        this.callEvent(new xAuthCommandAdminUnregisterEvent(properties));
        this.setResult(true);
    }

}
