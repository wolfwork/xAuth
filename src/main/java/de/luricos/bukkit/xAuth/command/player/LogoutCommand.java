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

import de.luricos.bukkit.xAuth.command.xAuthPlayerCommand;
import de.luricos.bukkit.xAuth.event.command.player.xAuthCommandLogoutEvent;
import de.luricos.bukkit.xAuth.event.xAuthEventProperties;
import de.luricos.bukkit.xAuth.utils.CommandLineTokenizer;
import de.luricos.bukkit.xAuth.xAuth;
import de.luricos.bukkit.xAuth.xAuthPlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LogoutCommand extends xAuthPlayerCommand implements CommandExecutor {

    public LogoutCommand() {
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        args = CommandLineTokenizer.tokenize(args);

        if (!this.isAllowedCommand(sender, "logout.permission", "logout"))
            return true;

        Player player = (Player) sender;
        xAuthPlayer xp = this.getPlayerManager().getPlayer(player);
        String response;

        if (xp.isAuthenticated()) {
            boolean success = this.getPlayerManager().deleteSession(xp.getAccountId());
            if (success) {
                this.getPlayerManager().protect(xp);
                xp.setStatus(xAuthPlayer.Status.REGISTERED);
                xAuth.getPlugin().getAuthClass(xp).offline(xp.getName());
                response = "logout.success";

                xAuthEventProperties properties = new xAuthEventProperties();
                properties.setProperty("action", xAuthCommandLogoutEvent.Action.PLAYER_LOGOUT);
                properties.setProperty("status", xp.getStatus());
                properties.setProperty("playername", xp.getName());
                this.callEvent(new xAuthCommandLogoutEvent(properties));
            } else
                response = "logout.error.general";
        } else {
            response = "logout.error.logged";
        }

        this.getMessageHandler().sendMessage(response, player);
        //@TODO do we need idle kick after logout? Should it also respect mustRegister?

        return true;
    }
}