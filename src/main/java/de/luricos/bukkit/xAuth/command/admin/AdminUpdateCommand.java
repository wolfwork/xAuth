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
import de.luricos.bukkit.xAuth.event.command.admin.xAuthCommandAdminUpdateEvent;
import de.luricos.bukkit.xAuth.event.xAuthEventProperties;
import de.luricos.bukkit.xAuth.updater.Updater;
import de.luricos.bukkit.xAuth.utils.xAuthLog;
import de.luricos.bukkit.xAuth.xAuth;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

/**
 * @author lycano
 */
public class AdminUpdateCommand extends xAuthAdminCommand {

    public AdminUpdateCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!this.isAllowedCommand(sender, "admin.permission", "xauth.update")) {
            this.setResult(true);
            return;
        }

        xAuthEventProperties properties = new xAuthEventProperties();
        properties.setProperty("issuedby", sender.getName());

        xAuthLog.info("Update command executed... checking for existing update.");

        Updater updater = xAuth.getUpdater();
        updater.setType(Updater.UpdateType.NO_DOWNLOAD);
        updater.setAnnounce(true);
        updater.run();

        properties.setProperty("action", xAuthCommandAdminUpdateEvent.Action.SUCCESS_UPDATE_CHECK);
        properties.setProperty("issuedby", sender.getName());
        properties.setProperty("updateresult", updater.getResult());
        properties.setProperty("updateresultmessages", updater.getResultMessages());
        this.callEvent(new xAuthCommandAdminUpdateEvent(properties));

        String[] messages = updater.getResultMessages().split("\n");
        for (String message: messages) {
            this.getMessageHandler().sendMessage(message, sender);
        }

        xAuthLog.info("Update check finished. Message was: " + updater.getResultMessages());
        this.setResult(true);
    }

}
