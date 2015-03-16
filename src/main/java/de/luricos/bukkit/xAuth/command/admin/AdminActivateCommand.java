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
import de.luricos.bukkit.xAuth.event.command.admin.xAuthCommandAdminActivateEvent;
import de.luricos.bukkit.xAuth.event.xAuthEventProperties;
import de.luricos.bukkit.xAuth.xAuthPlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

/**
 * @author lycano
 */
public class AdminActivateCommand extends xAuthAdminCommand {

    public AdminActivateCommand() {

    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String commandNode = "xauth.activate";
        if (!(this.isAllowedCommand(sender, "admin.permission", commandNode))) {
            return true;
        }

        if (args.length < 2) {
            this.getMessageHandler().sendMessage("admin.activate.usage", sender);
            return true;
        }

        String targetName = args[1];
        if (this.isDeniedCommandTarget(sender, "admin.target-permission", targetName, commandNode)) {
            return true;
        }

        boolean force = ((args.length > 2) && (args[2].equals("force")));
        xAuthPlayer xp = this.getPlayerManager().getPlayer(targetName);

        xAuthEventProperties properties = new xAuthEventProperties();
        properties.setProperty("issuedby", sender.getName());
        if (targetName.equals("*")) {
            Integer countState = this.getPlayerManager().countLocked();
            boolean success = this.getPlayerManager().setAllActiveStates(true, null);

            this.getMessageHandler().sendMessage(success ? "admin.activate.successM" : "admin.activate.error.generalM", sender, countState.toString());

            properties.setProperty("action", xAuthCommandAdminActivateEvent.Action.ACTIVATED_ALL);
            properties.setProperty("targetname", "*");
            this.callEvent(new xAuthCommandAdminActivateEvent(properties));
            return true;
        }

        if (!xp.isRegistered()) {
            this.getMessageHandler().sendMessage("admin.activate.error.registered", sender, targetName);

            properties.setProperty("action", xAuthCommandAdminActivateEvent.Action.ACVTIVATION_ERROR_REGISTERED);
            properties.setProperty("targetid", xp.getAccountId());
            properties.setProperty("targetname", xp.getName());
            this.callEvent(new xAuthCommandAdminActivateEvent(properties));

            return true;
        } else if ((!force) && (this.getPlayerManager().isActive(xp.getAccountId()))) {
            this.getMessageHandler().sendMessage("admin.activate.error.active", sender, targetName);

            properties.setProperty("action", xAuthCommandAdminActivateEvent.Action.ACTIVATION_ERROR_ACTIVE);
            properties.setProperty("target", xp.getAccountId());
            properties.setProperty("targetname", xp.getName());
            this.callEvent(new xAuthCommandAdminActivateEvent(properties));

            return true;
        }

        boolean success = this.getPlayerManager().activateAcc(xp.getAccountId());
        if (!success) {
            this.getMessageHandler().sendMessage("admin.activate.error.general", sender, targetName);

            properties.setProperty("action", xAuthCommandAdminActivateEvent.Action.ACTIVATION_ERROR_GENERAL);
            properties.setProperty("targetid", xp.getAccountId());
            properties.setProperty("targetname", xp.getName());
        } else {
            this.getMessageHandler().sendMessage("admin.activate.success", sender, targetName);

            properties.setProperty("action", xAuthCommandAdminActivateEvent.Action.ACTIVATION_SUCCESS);
            properties.setProperty("targetid", xp.getAccountId());
            properties.setProperty("targetname", xp.getName());
        }
        this.callEvent(new xAuthCommandAdminActivateEvent(properties));

        return true;
    }

}
