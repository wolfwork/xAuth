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
import de.luricos.bukkit.xAuth.event.command.admin.xAuthCommandAdminConfigEvent;
import de.luricos.bukkit.xAuth.event.xAuthEventProperties;
import de.luricos.bukkit.xAuth.xAuth;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.List;

/**
 * @author lycano
 */
public class AdminConfigCommand extends xAuthAdminCommand {

    public AdminConfigCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(this.isAllowedCommand(sender, "admin.permission", "xauth.config"))) {
            this.setResult(true);
            return;
        }

        if (args.length < 2) {
            this.getMessageHandler().sendMessage("admin.config.usage", sender);
            this.setResult(true);
            return;
        }

        String node = args[1].toLowerCase();
        Object configValue = xAuth.getPlugin().getConfig().getDefaults().get(node);

        xAuthEventProperties properties = new xAuthEventProperties();
        properties.setProperty("node", node);
        properties.setProperty("issuedby", sender.getName());

        if (xAuth.getPlugin().getConfig().getConfigurationSection(node) != null) {
            this.getMessageHandler().sendMessage("admin.config.error.is-section", sender);
            this.setResult(true);

            properties.setProperty("action", xAuthCommandAdminConfigEvent.Action.ERROR_IS_SECTION);
            this.callEvent(new xAuthCommandAdminConfigEvent(properties));
            return;
        }

        if (configValue == null) {
            this.getMessageHandler().sendMessage("admin.config.error.exist", sender);
            this.setResult(true);

            properties.setProperty("action", xAuthCommandAdminConfigEvent.Action.ERROR_NODE_EXIST);
            this.callEvent(new xAuthCommandAdminConfigEvent(properties));
            return;
        }

        boolean getVal = false;
        Object nodeVal = null;
        String value = null;
        if (args.length > 2) {
            value = args[2];
        }

        if ((value == null) || value.isEmpty())
            getVal = true;

        try {
            if (configValue instanceof String) {
                if (getVal) {
                    nodeVal = xAuth.getPlugin().getConfig().getString(node);
                } else {
                    xAuth.getPlugin().getConfig().set(node, value);
                }
            } else if (configValue instanceof Integer) {
                if (getVal) {
                    nodeVal = xAuth.getPlugin().getConfig().getInt(node);
                } else {
                    xAuth.getPlugin().getConfig().set(node, Integer.parseInt(value));
                }
            } else if (configValue instanceof Boolean) {
                if (getVal) {
                    nodeVal = xAuth.getPlugin().getConfig().getBoolean(node);
                } else {
                    xAuth.getPlugin().getConfig().set(node, Boolean.parseBoolean(value));
                }
            } else if (configValue instanceof List<?>) {
                if (getVal) {
                    nodeVal = xAuth.getPlugin().getConfig().getList(node).toString();
                } else {
                    throw new IllegalArgumentException();
                }
            } else {
                throw new IllegalArgumentException();
            }
        } catch (NumberFormatException e) {
            this.getMessageHandler().sendMessage("admin.config.error.int", sender);
            this.setResult(true);

            properties.setProperty("action", xAuthCommandAdminConfigEvent.Action.ERROR_NODE_INT);
            this.callEvent(new xAuthCommandAdminConfigEvent(properties));
            return;
        } catch (IllegalArgumentException e) {
            this.getMessageHandler().sendMessage("admin.config.error.invalid", sender);
            this.setResult(true);

            properties.setProperty("action", xAuthCommandAdminConfigEvent.Action.ERROR_NODE_INVALID);
            this.callEvent(new xAuthCommandAdminConfigEvent(properties));
            return;
        }

        if (!getVal) {
            xAuth.getPlugin().saveConfig();
            this.getMessageHandler().sendMessage("admin.config.success", sender);

            properties.setProperty("action", xAuthCommandAdminConfigEvent.Action.CONFIG_SUCCESS);
        } else {
            this.getMessageHandler().sendMessage(String.format(this.getMessageHandler().getNode("admin.config.value"), node, nodeVal), sender);
        }

        this.callEvent(new xAuthCommandAdminConfigEvent(properties));
        this.setResult(true);
    }

}
