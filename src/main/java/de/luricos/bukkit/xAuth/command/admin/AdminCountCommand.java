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
import de.luricos.bukkit.xAuth.command.xAuthPlayerCountType;
import de.luricos.bukkit.xAuth.event.command.admin.xAuthCommandAdminCountEvent;
import de.luricos.bukkit.xAuth.event.xAuthEventProperties;
import de.luricos.bukkit.xAuth.xAuth;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

/**
 * @author lycano
 */
public class AdminCountCommand extends xAuthAdminCommand {

    public AdminCountCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(this.isAllowedCommand(sender, "admin.permission", "xauth.count"))) {
            this.setResult(true);
            return;
        }

        if (args.length < 2) {
            this.getMessageHandler().sendMessage("admin.count.usage", sender);
            this.setResult(true);
            return;
        }

        Integer count = 0;
        String modeName = args[1].replace("-", "_");

        xAuthPlayerCountType playerCountType = xAuthPlayerCountType.getType(modeName);
        if (playerCountType == null) {
            this.getMessageHandler().sendMessage("admin.count.usage", sender);
            this.setResult(true);
            return;
        }

        xAuthEventProperties properties = new xAuthEventProperties();
        properties.setProperty("issuedby", sender.getName());

        switch(playerCountType) {
            case ALL:
                count = xAuth.getPlugin().getPlayerManager().countAll();
                this.getMessageHandler().sendMessage("admin.count.success.all", sender, count.toString());
                break;
            case ACTIVE:
                count = xAuth.getPlugin().getPlayerManager().countActive();
                this.getMessageHandler().sendMessage("admin.count.success.active", sender, count.toString());
                break;
            case LOCKED:
                count = xAuth.getPlugin().getPlayerManager().countLocked();
                this.getMessageHandler().sendMessage("admin.count.success.locked", sender, count.toString());
                break;
            case PREMIUM:
                count = xAuth.getPlugin().getPlayerManager().countPremium();
                this.getMessageHandler().sendMessage("admin.count.success.premium", sender, count.toString());
                break;
            case NON_PREMIUM:
                count = xAuth.getPlugin().getPlayerManager().countNonPremium();
                this.getMessageHandler().sendMessage("admin.count.success.non-premium", sender, count.toString());
                break;
            default:
                this.getMessageHandler().sendMessage("admin.count.usage", sender);
                break;
        }

        properties.setProperty("action", xAuthCommandAdminCountEvent.Action.SUCCESS);
        properties.setProperty("counttype", playerCountType);
        properties.setProperty("count", count);
        this.callEvent(new xAuthCommandAdminCountEvent(properties));

        this.setResult(true);
    }

}
