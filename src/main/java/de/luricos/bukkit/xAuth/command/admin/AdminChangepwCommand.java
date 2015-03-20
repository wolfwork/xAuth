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

import de.luricos.bukkit.xAuth.auth.AuthMethod;
import de.luricos.bukkit.xAuth.command.xAuthAdminCommand;
import de.luricos.bukkit.xAuth.event.command.admin.xAuthCommandAdminChangepwEvent;
import de.luricos.bukkit.xAuth.event.xAuthEventProperties;
import de.luricos.bukkit.xAuth.utils.xAuthLog;
import de.luricos.bukkit.xAuth.xAuth;
import de.luricos.bukkit.xAuth.xAuthPlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

/**
 * @author lycano
 */
public class AdminChangepwCommand extends xAuthAdminCommand {

    public AdminChangepwCommand() {

    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String commandNode = "xauth.changepw";
        if (!(this.isAllowedCommand(sender, "admin.permission", commandNode))) {
            return true;
        }

        if (args.length < 3) {
            this.getMessageHandler().sendMessage("admin.changepw.usage", sender);
            return true;
        }

        String targetName = args[1];
        if (this.isDeniedCommandTarget(sender, "admin.target-permission", targetName, commandNode)) {
            return true;
        }

        String newPassword = args[2];
        xAuthPlayer xp = this.getPlayerManager().getPlayer(targetName);

        int pwType = xp.getPasswordType().getTypeId();
        if (args.length > 3)
            pwType = Integer.parseInt(args[3]);

        AuthMethod a = xAuth.getPlugin().getAuthClass(xp);
        boolean success = a.adminChangePassword(targetName, newPassword, pwType);

        xAuthEventProperties properties = new xAuthEventProperties();
        properties.setProperty("issuedby", sender.getName());
        properties.setProperty("targetid", xp.getAccountId());
        properties.setProperty("targetname", xp.getName());
        if (success) {
            properties.setProperty("action", xAuthCommandAdminChangepwEvent.Action.PASSWORD_CHANGED_SUCCESS);
        } else {
            properties.setProperty("action", xAuthCommandAdminChangepwEvent.Action.PASSWORD_CHANGED_ERROR);
        }


        String response = a.getResponse();
        if (response != null) {
            this.getMessageHandler().sendMessage(response, sender, targetName);
        }

        if (success) {
            xAuthLog.info(sender.getName() + " changed " + targetName + "'s password");
        }

        properties.setProperty("response", response);
        this.callEvent(new xAuthCommandAdminChangepwEvent(properties));
        return true;
    }
}
