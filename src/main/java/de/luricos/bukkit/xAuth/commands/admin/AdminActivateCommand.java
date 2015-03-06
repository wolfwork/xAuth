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

/**
 * @author lycano
 */
public class AdminActivateCommand extends xAuthAdminCommand {

    public AdminActivateCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!this.isAllowedCommand(sender, "admin.permission", "xauth.activate")) {
            this.setResult(true);
            return;
        }

        if (args.length < 2) {
            this.getMessageHandler().sendMessage("admin.activate.usage", sender);
            this.setResult(true);
            return;
        }

        String targetName = args[1];
        boolean force = ((args.length > 2) && (args[2].equals("force")));
        xAuthPlayer xp = xAuth.getPlugin().getPlayerManager().getPlayer(targetName);
        if (targetName.equals("*")) {
            Integer countState = xAuth.getPlugin().getPlayerManager().countLocked();
            boolean success = xAuth.getPlugin().getPlayerManager().setAllActiveStates(true, null);

            this.getMessageHandler().sendMessage(success ? "admin.activate.successM" : "admin.activate.error.generalM", sender, countState.toString());
            this.setResult(true);
            return;
        }

        if (!xp.isRegistered()) {
            this.getMessageHandler().sendMessage("admin.activate.error.registered", sender, targetName);
            this.setResult(true);
            return;
        } else if ((!force) && (xAuth.getPlugin().getPlayerManager().isActive(xp.getAccountId()))) {
            this.getMessageHandler().sendMessage("admin.activate.error.active", sender, targetName);
            this.setResult(true);
            return;
        }

        boolean success = xAuth.getPlugin().getPlayerManager().activateAcc(xp.getAccountId());
        this.getMessageHandler().sendMessage(success ? "admin.activate.success" : "admin.activate.error.general", sender, targetName);

        this.setResult(true);
    }

}
