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

import de.luricos.bukkit.xAuth.auth.AuthMethod;
import de.luricos.bukkit.xAuth.commands.xAuthAdminCommand;
import de.luricos.bukkit.xAuth.events.xAuthRegisterEvent;
import de.luricos.bukkit.xAuth.utils.xAuthLog;
import de.luricos.bukkit.xAuth.xAuth;
import de.luricos.bukkit.xAuth.xAuthPlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

/**
 * @author lycano
 */
public class AdminRegisterCommand extends xAuthAdminCommand {

    public AdminRegisterCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!this.isAllowedCommand(sender, "admin.permission", "xauth.register")) {
            this.setResult(true);
            return;
        }

        if (args.length < 3) {
            this.getMessageHandler().sendMessage("admin.register.usage", sender);
            this.setResult(true);
            return;
        }

        String targetName = args[1];
        String password = args[2];
        String email = args.length > 3 ? args[3] : null;
        xAuthPlayer xp = xAuth.getPlugin().getPlayerManager().getPlayer(targetName);

        AuthMethod a = xAuth.getPlugin().getAuthClass(xp);
        boolean success = a.adminRegister(targetName, password, email);

        String response = a.getResponse();
        if (response != null)
            this.getMessageHandler().sendMessage(response, sender, targetName);

        if (success) {
            // set registered user to target group
            boolean autoAssignGroup = xAuth.getPlugin().getConfig().getBoolean("groups.auto-assign", false);
            String joinGroupName = xAuth.getPlugin().getConfig().getString("groups.move-on-register", null);
            if ((autoAssignGroup) && (joinGroupName != null)) {
                xAuth.getPermissionManager().joinGroup(targetName, joinGroupName);

                this.callEvent(xAuthRegisterEvent.Action.PLAYER_GROUP_CHANGED, xp.getStatus());
            }

            xAuthLog.info(sender.getName() + " has registered an account for " + targetName);
        }

        this.setResult(true);
    }
}
