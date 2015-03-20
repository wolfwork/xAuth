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
import de.luricos.bukkit.xAuth.event.command.player.xAuthCommandRegisterEvent;
import de.luricos.bukkit.xAuth.event.xAuthEventProperties;
import de.luricos.bukkit.xAuth.utils.CommandLineTokenizer;
import de.luricos.bukkit.xAuth.utils.xAuthLog;
import de.luricos.bukkit.xAuth.xAuth;
import de.luricos.bukkit.xAuth.xAuthPlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RegisterCommand extends xAuthPlayerCommand implements CommandExecutor {

    public RegisterCommand() {
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        args = CommandLineTokenizer.tokenize(args);

        if (!this.isAllowedCommand(sender, "register.permission", "register"))
            return true;

        Player player = (Player) sender;
        xAuthPlayer xp = this.getPlayerManager().getPlayer(player);

        if ((xAuth.getPlugin().getConfig().getBoolean("registration.require-email") && args.length < 2) || args.length < 1) {
            this.getMessageHandler().sendMessage("register.usage", xp.getPlayer());
            return true;
        }

        String playerName = xp.getName();
        String password = args[0];
        String email = args.length > 1 ? args[1] : null;

        AuthMethod a = xAuth.getPlugin().getAuthClass(xp);
        boolean success = a.register(playerName, password, email);

        String response = a.getResponse();
        if (response != null)
            this.getMessageHandler().sendMessage(response, xp.getPlayer());

        if (success) {
            if ((!(xAuth.getPlugin().getConfig().getBoolean("registration.activation"))) && (!(xAuth.getPlugin().getConfig().getBoolean("registration.require-login"))))
                this.getPlayerManager().doLogin(xp);

            // set registered user to target group
            boolean autoAssignGroup = xAuth.getPlugin().getConfig().getBoolean("groups.auto-assign", false);
            String joinGroupName = xAuth.getPlugin().getConfig().getString("groups.move-on-register", null);

            xAuthEventProperties properties = new xAuthEventProperties();
            properties.setProperty("status", xp.getStatus());
            properties.setProperty("playername", xp.getName());

            if ((autoAssignGroup) && (joinGroupName != null)) {
                xAuth.getPermissionManager().joinGroup(player, joinGroupName);

                properties.setProperty("action", xAuthCommandRegisterEvent.Action.PLAYER_GROUP_CHANGED);
                this.callEvent(new xAuthCommandRegisterEvent(properties));
            }

            properties.setProperty("action", xAuthCommandRegisterEvent.Action.PLAYER_REGISTERED);
            this.callEvent(new xAuthCommandRegisterEvent(properties));

            xAuthLog.info(playerName + " has registered");
        }

        return true;
    }
}