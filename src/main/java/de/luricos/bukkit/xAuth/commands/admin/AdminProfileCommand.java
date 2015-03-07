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
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.text.SimpleDateFormat;

/**
 * @author lycano
 */
public class AdminProfileCommand extends xAuthAdminCommand {

    public AdminProfileCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!this.isAllowedCommand(sender, "admin.permission", "xauth.profile")) {
            this.setResult(true);
            return;
        }

        if (args.length > 2) {
            this.getMessageHandler().sendMessage("admin.profile.usage", sender);
            this.setResult(true);
            return;
        }

        if ((!(sender instanceof Player)) && (args.length < 2)) {
            this.getMessageHandler().sendMessage("admin.profile.error.console", sender);
            this.setResult(true);
            return;
        }

        String targetName = (args.length > 1) ? args[1] : sender.getName();

        xAuthPlayer xp;
        try {
            Integer accountId = Integer.parseInt(targetName);
            xp = xAuth.getPlugin().getPlayerManager().getPlayerById(accountId);
        } catch (Exception e) {
            xp = xAuth.getPlugin().getPlayerManager().getPlayer(targetName);
        }

        StringBuilder sb = new StringBuilder("------ xAuth Profile ------").append("\n");
        String message = "";

        sb.append(ChatColor.WHITE + "Account-Id : ").append(xp.getAccountId()).append("\n");
        sb.append(ChatColor.WHITE + "Registered : ").append(((xp.isRegistered()) ? "{true}" : "{false}"));

        if (xp.isRegistered()) {
            sb.append("\n");

            if (xp.isOnline()) {
                sb.append(ChatColor.WHITE + "Operator: ").append((xp.isOp()) ? "{true}" : "{false}").append("\n");
            }

            sb.append(ChatColor.WHITE + "Name : ").append(xp.getName());
            if ((xp.isOnline()) && xp.isAuthenticated()) {
                sb.append(", " + ChatColor.WHITE + "DisplayName : ").append(((xp.isAuthenticated()) ? xp.getPlayer().getDisplayName() : xp.getName())).append("\n");
            } else {
                sb.append("\n");
            }

            sb.append(ChatColor.WHITE + "Authenticated : ").append(((xp.isAuthenticated()) ? "{true}" : "{false}")).append("\n");
            sb.append(ChatColor.WHITE + "Premium : ").append(((xp.isPremium()) ? "{true}" : ChatColor.RED + "{false}")).append("\n");
            sb.append(ChatColor.WHITE + "Locked : ").append(((xp.isLocked()) ? "{true}" : "{false}")).append(", ");
            sb.append(ChatColor.WHITE + "ResetPw : ").append(((xp.isReset()) ? "{true}" : "{false}")).append(", ");
            sb.append(ChatColor.WHITE + "PWType : ").append(xp.getPasswordType().getName()).append("\n");

            if (xp.isOnline())
                sb.append(ChatColor.WHITE + "GameMode : ").append(xp.getGameMode()).append("\n");

            if (xp.getLoginTime() != null)
                sb.append(ChatColor.WHITE + "Last login: ").append(new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(xp.getLoginTime()));
        }

        message = sb.toString()
                .replace("{true}", ChatColor.GREEN + "true")
                .replace("{false}", ChatColor.RED + "false");

        sender.sendMessage(message);

        this.setResult(true);
    }

}
