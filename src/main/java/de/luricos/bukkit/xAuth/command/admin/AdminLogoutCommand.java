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
import de.luricos.bukkit.xAuth.event.command.admin.xAuthCommandAdminLogoutEvent;
import de.luricos.bukkit.xAuth.event.xAuthEventProperties;
import de.luricos.bukkit.xAuth.xAuth;
import de.luricos.bukkit.xAuth.xAuthPlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * @author lycano
 */
public class AdminLogoutCommand extends xAuthAdminCommand {

    public AdminLogoutCommand() {

    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String commandNode = "xauth.logout";
        if (!(this.isAllowedCommand(sender, "admin.permission", commandNode))) {
            return true;
        }

        if (args.length < 2) {
            this.getMessageHandler().sendMessage("admin.logout.usage", sender);
            return true;
        }

        String targetName = args[1];
        if (this.isDeniedCommandTarget(sender, "admin.target-permission", targetName, commandNode)) {
            return true;
        }

        xAuthPlayer xp = this.getPlayerManager().getPlayer(targetName);

        xAuthEventProperties properties = new xAuthEventProperties();
        properties.setProperty("issuedby", sender.getName());
        properties.setProperty("targetid", xp.getAccountId());
        properties.setProperty("targetname", xp.getName());

        if (!xp.isAuthenticated()) {
            this.getMessageHandler().sendMessage("admin.logout.error.logged", sender, targetName);

            properties.setProperty("action", xAuthCommandAdminLogoutEvent.Action.ERROR_LOGOUT_AUTHENTICATED);
            this.callEvent(new xAuthCommandAdminLogoutEvent(properties));
            return true;
        }

        boolean success = this.getPlayerManager().deleteSession(xp.getAccountId());
        if (success) {
            xp.setStatus(xAuthPlayer.Status.REGISTERED);

            // a forced logout will set resetMode to false as the user does not had any chance to reset his password
            if (xp.isReset()) {
                xp.setReset(false);
                this.getPlayerManager().unSetReset(xp.getAccountId());
            }


            // if player is logged in log him out
            Player targetPlayer = xp.getPlayer();
            if (targetPlayer != null) {
                this.getPlayerManager().protect(xp);
                this.getMessageHandler().sendMessage("admin.logout.success.target", targetPlayer);
            }

            // notify authclass that the player was logged out
            xAuth.getPlugin().getAuthClass(xp).offline(xp.getName());
            this.getMessageHandler().sendMessage("admin.logout.success.player", sender, targetName);

            properties.setProperty("action", xAuthCommandAdminLogoutEvent.Action.SUCCESS_LOGOUT_PLAYER);
        } else {
            this.getMessageHandler().sendMessage("admin.logout.error.general", sender);
            properties.setProperty("action", xAuthCommandAdminLogoutEvent.Action.ERROR_GENERAL);
        }

        this.callEvent(new xAuthCommandAdminLogoutEvent(properties));
        return true;
    }

}
