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
package de.luricos.bukkit.xAuth.commands.admin;

import de.luricos.bukkit.xAuth.commands.xAuthAdminCommand;
import de.luricos.bukkit.xAuth.xAuth;
import de.luricos.bukkit.xAuth.xAuthPlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * @author lycano
 */
public class AdminLogoutCommand extends xAuthAdminCommand {

    public AdminLogoutCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!this.isAllowedCommand(sender, "admin.permission", "xauth.logout")) {
            this.setResult(true);
            return;
        }

        if (args.length < 2) {
            this.getMessageHandler().sendMessage("admin.logout.usage", sender);
            this.setResult(true);
            return;
        }

        String targetName = args[1];
        xAuthPlayer xp = xAuth.getPlugin().getPlayerManager().getPlayer(targetName);

        if (!xp.isAuthenticated()) {
            this.getMessageHandler().sendMessage("admin.logout.error.logged", sender, targetName);
            this.setResult(true);
            return;
        }

        boolean success = xAuth.getPlugin().getPlayerManager().deleteSession(xp.getAccountId());
        if (success) {
            xp.setStatus(xAuthPlayer.Status.REGISTERED);

            // a forced logout will set resetMode to false as the user does not had any chance to reset his password
            if (xp.isReset()) {
                xp.setReset(false);
                xAuth.getPlugin().getPlayerManager().unSetReset(xp.getAccountId());
            }

            xAuth.getPlugin().getAuthClass(xp).offline(xp.getName());
            this.getMessageHandler().sendMessage("admin.logout.success.player", sender, targetName);

            Player target = xp.getPlayer();
            if (target != null) {
                xAuth.getPlugin().getPlayerManager().protect(xp);
                this.getMessageHandler().sendMessage("admin.logout.success.target", target);
            }
        } else
            this.getMessageHandler().sendMessage("admin.logout.error.general", sender);

        this.setResult(true);
    }

}
