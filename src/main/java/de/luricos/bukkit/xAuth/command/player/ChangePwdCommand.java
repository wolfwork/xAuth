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
package de.luricos.bukkit.xAuth.command.player;

import de.luricos.bukkit.xAuth.auth.AuthMethod;
import de.luricos.bukkit.xAuth.command.xAuthPlayerCommand;
import de.luricos.bukkit.xAuth.event.command.player.xAuthCommandChangePasswordEvent;
import de.luricos.bukkit.xAuth.event.command.player.xAuthCommandResetPasswordEvent;
import de.luricos.bukkit.xAuth.event.xAuthEventProperties;
import de.luricos.bukkit.xAuth.utils.CommandLineTokenizer;
import de.luricos.bukkit.xAuth.utils.xAuthLog;
import de.luricos.bukkit.xAuth.xAuth;
import de.luricos.bukkit.xAuth.xAuthPlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ChangePwdCommand extends xAuthPlayerCommand implements CommandExecutor {

    public ChangePwdCommand() {
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        args = CommandLineTokenizer.tokenize(args);

        if (!this.isAllowedCommand(sender, "changepw.permission", "changepw"))
            return true;

        Player player = (Player) sender;
        xAuthPlayer xp = xAuth.getPlugin().getPlayerManager().getPlayer(player);

        if (xp.isGuest()) {
            this.getMessageHandler().sendMessage("changepw.error.logged", xp.getPlayer());
            return true;
        }

        if (xp.isLocked()) {
            this.getMessageHandler().sendMessage("misc.active", xp.getPlayer());
            return true;
        }

        if (xp.isReset())
            return resetPwCommand(xp, player, args);

        return changePwCommand(xp, player, args);
    }

    private boolean changePwCommand(xAuthPlayer xp, Player p, String[] args) {
        if (!xp.isAuthenticated()) {
            this.getMessageHandler().sendMessage("changepw.error.logged", xp.getPlayer());
            return true;
        }

        if (args.length < 2) {
            this.getMessageHandler().sendMessage("changepw.usage", xp.getPlayer());
            return true;
        }

        if (!xAuth.getPermissionManager().has(p, "xauth.allow.player.command.changepw")) {
            this.getMessageHandler().sendMessage("changepw.permission", p);
            return true;
        }

        AuthMethod a = xAuth.getPlugin().getAuthClass(xp);

        String oldPassword = args[0];
        String newPassword = args[1];
        boolean success = a.changePassword(xp.getName(), oldPassword, newPassword);

        String response = a.getResponse();
        if (response != null)
            this.getMessageHandler().sendMessage(response, xp.getPlayer());

        if (success) {
            xAuthLog.info(xp.getName() + " has changed their password");

            xAuthEventProperties properties = new xAuthEventProperties();
            properties.setProperty("action", xAuthCommandChangePasswordEvent.Action.PLAYER_CHANGED_PASSWORD);
            properties.setProperty("status", xp.getStatus());
            properties.setProperty("playername", xp.getName());
            this.callEvent(new xAuthCommandChangePasswordEvent(properties));
        }

        return true;
    }

    private boolean resetPwCommand(xAuthPlayer xp, Player p, String[] args) {
        if (args.length != 1) {
            this.getMessageHandler().sendMessage("resetpw.reset-usage", xp.getPlayer());
            return true;
        }

        if (!xAuth.getPermissionManager().has(p, "xauth.allow.player.command.resetpw")) {
            this.getMessageHandler().sendMessage("resetpw.permission", p);
            return true;
        }

        AuthMethod a = xAuth.getPlugin().getAuthClass(xp);
        String newPassword = args[0];
        boolean success = a.resetPassword(xp.getName(), newPassword);

        String response = a.getResponse();
        if (response != null)
            this.getMessageHandler().sendMessage(response, xp.getPlayer());

        if (success) {
            xAuthLog.info(xp.getName() + " has changed their password");

            xAuthEventProperties properties = new xAuthEventProperties();
            properties.setProperty("action", xAuthCommandResetPasswordEvent.Action.PLAYER_PASSWORD_RESETTED);
            properties.setProperty("status", xp.getStatus());
            properties.setProperty("playername", xp.getName());
            this.callEvent(new xAuthCommandResetPasswordEvent(properties));
        }

        return true;
    }
}