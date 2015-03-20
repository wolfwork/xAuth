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
import de.luricos.bukkit.xAuth.event.command.admin.xAuthCommandAdminUpgradeEvent;
import de.luricos.bukkit.xAuth.event.xAuthEventProperties;
import de.luricos.bukkit.xAuth.updater.Updater;
import de.luricos.bukkit.xAuth.utils.xAuthLog;
import de.luricos.bukkit.xAuth.xAuth;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

/**
 * @author lycano
 */
public class AdminUpgradeCommand extends xAuthAdminCommand {

    public AdminUpgradeCommand() {

    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(this.isAllowedCommand(sender, "admin.permission", "xauth.upgrade"))) {
            return true;
        }

        xAuthEventProperties properties = new xAuthEventProperties();
        properties.setProperty("issuedby", sender.getName());

        xAuthLog.info("Update command executed... trying to upgrade plugin.");

        Updater updater = xAuth.getUpdater();
        updater.setType(Updater.UpdateType.NO_VERSION_CHECK);
        updater.setAnnounce(true);
        updater.run();

        properties.setProperty("action", xAuthCommandAdminUpgradeEvent.Action.SUCCESS_UPGRADE_CHECK);
        properties.setProperty("issuedby", sender.getName());
        properties.setProperty("upgraderesult", updater.getResult());
        properties.setProperty("upgraderesultmessages", updater.getResultMessages());
        this.callEvent(new xAuthCommandAdminUpgradeEvent(properties));

        String[] messages = updater.getResultMessages().split("\n");
        for (String message: messages) {
            this.getMessageHandler().sendMessage(message, sender);
        }

        xAuthLog.info("Upgrade check finished. Message was: " + updater.getResultMessages());
        return true;
    }

}
