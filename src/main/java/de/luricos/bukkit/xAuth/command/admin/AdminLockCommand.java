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
import de.luricos.bukkit.xAuth.event.command.admin.xAuthCommandAdminLockEvent;
import de.luricos.bukkit.xAuth.event.xAuthEventProperties;
import de.luricos.bukkit.xAuth.xAuthPlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * @author lycano
 */
public class AdminLockCommand extends xAuthAdminCommand {

    public AdminLockCommand() {

    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String commandNode = "xauth.lock";
        if (!(this.isAllowedCommand(sender, "admin.permission", commandNode))) {
            return true;
        }

        if (args.length < 2) {
            this.getMessageHandler().sendMessage("admin.lock.usage", sender);
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
            Integer countState = this.getPlayerManager().countActive();
            if (sender instanceof Player) {
                xp = this.getPlayerManager().getPlayer(sender.getName());
                if (countState > 0)
                    countState--;
            }

            boolean success = this.getPlayerManager().setAllActiveStates(false, new Integer[]{xp.getAccountId()});

            String node;
            if (success) {
                properties.setProperty("action", xAuthCommandAdminLockEvent.Action.SUCCESS_LOCKED_ALL);
                node = "admin.lock.successM";
            } else {
                properties.setProperty("action", xAuthCommandAdminLockEvent.Action.ERROR_LOCK_ALL);
                node = "admin.lock.error.generalM";
            }
            this.getMessageHandler().sendMessage(node, sender, countState.toString());

            properties.setProperty("target", "*");
            this.callEvent(new xAuthCommandAdminLockEvent(properties));
            return true;
        }

        if (!xp.isRegistered()) {
            this.getMessageHandler().sendMessage("admin.lock.error.registered", sender);

            properties.setProperty("action", xAuthCommandAdminLockEvent.Action.ERROR_REGISTERED);
            this.callEvent(new xAuthCommandAdminLockEvent(properties));
            return true;
        } else if ((!force) && (!this.getPlayerManager().isActive(xp.getAccountId()))) {
            this.getMessageHandler().sendMessage("admin.lock.error.locked", sender);

            properties.setProperty("action", xAuthCommandAdminLockEvent.Action.ERROR_LOCKED);
            this.callEvent(new xAuthCommandAdminLockEvent(properties));
            return true;
        }

        boolean success = this.getPlayerManager().lockAcc(xp.getAccountId());
        String node;
        if (success) {
            properties.setProperty("action", xAuthCommandAdminLockEvent.Action.SUCCESS_LOCK_PLAYER);
            properties.setProperty("targetid", xp.getAccountId());
            properties.setProperty("targetName", xp.getName());
            node = "admin.lock.success";
        } else {
            properties.setProperty("action", xAuthCommandAdminLockEvent.Action.ERROR_REGISTERED);
            properties.setProperty("targetid", xp.getAccountId());
            properties.setProperty("targetName", xp.getName());
            node = "admin.lock.error.general";
        }

        this.getMessageHandler().sendMessage(node, sender, targetName);

        this.callEvent(new xAuthCommandAdminLockEvent(properties));
        return true;
    }

}
