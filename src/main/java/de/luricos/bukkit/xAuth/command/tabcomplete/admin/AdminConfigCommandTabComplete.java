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
package de.luricos.bukkit.xAuth.command.tabcomplete.admin;

import de.luricos.bukkit.xAuth.command.tabcomplete.xAuthCommandTabCompletion;
import de.luricos.bukkit.xAuth.command.tabcomplete.xAuthTabCompleteComperator;
import de.luricos.bukkit.xAuth.utils.xAuthLog;
import de.luricos.bukkit.xAuth.xAuth;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;

import java.util.*;

/**
 * @author lycano
 */
public class AdminConfigCommandTabComplete extends xAuthCommandTabCompletion {

    private Configuration config;
    private String senderName;
    private String command;
    private int nodeLevel = 0;
    private String[] nodeList;
    private String delimiter = ".";
    private String splitDelimiter = "\\.";

    public AdminConfigCommandTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        super(sender, command, alias, args);

        this.config = xAuth.getPlugin().getConfig();
        this.senderName = sender.getName();
    }

    public List<String> tabComplete(CommandSender sender, Command command, String alias, String[] args) {
        try {
            this.command = args[1].replaceFirst("\\+", "").toLowerCase();
            String node;

            this.nodeList = this.command.split(this.splitDelimiter);
            this.nodeLevel = this.nodeList.length - 1;

            if (this.command.endsWith(this.delimiter))
                this.nodeLevel++;

            Set<String> keys;
            if (this.nodeLevel > 0) {
                node = this.basePath(this.command);
                keys = this.config.getConfigurationSection(node).getKeys(false);

                Set<String> prefixedKeys = new HashSet<String>();
                for (String key: keys) {
                    prefixedKeys.add(this.basePath(this.command) + this.delimiter + key);
                }
                keys = prefixedKeys;
            } else {
                keys = this.config.getKeys(false);
            }

            this.nodeList = keys.toArray(new String[keys.size()]);
            Arrays.sort(this.nodeList, new xAuthTabCompleteComperator(this.command));

            if (this.nodeLevel > 0) {
                if (this.command.endsWith(".")) {
                    List<String> modifiedList = new ArrayList<String>(Arrays.asList(this.command));
                    modifiedList.addAll(new ArrayList<String>(Arrays.asList(this.nodeList)));

                    this.translateChildNodes(modifiedList);

                    this.nodeList = modifiedList.toArray(new String[modifiedList.size()]);
                } else if (!(this.command.equals(this.nodeList[0]))) {
                    String tmp = this.nodeList[0];
                    this.nodeList = new String[1];
                    this.nodeList[0] = this.translateChildNode(tmp);
                }
            }

            return new ArrayList<String>(Arrays.asList(this.nodeList));
        } catch (Exception e) {
            xAuthLog.warning(String.format("Error TabCompleting command '%s' for sender %s", this.command, this.senderName));
        }

        return new ArrayList<String>();
    }

    private String basePath(String path) {
        return (path.lastIndexOf(this.delimiter) > 0) ? path.substring(0, path.lastIndexOf(this.delimiter)) : path;
    }

    private String translateChildNode(String node) {
        List<String> childNode = new ArrayList<String>(Arrays.asList(node));
        this.translateChildNodes(childNode);
        return childNode.get(0);
    }

    private void translateChildNodes(List<String> nodeList) {
        ConfigurationSection configurationSection;
        for (int i=0; i < nodeList.size(); i++) {
            String path = nodeList.get(i);
            configurationSection = this.config.getConfigurationSection(path);
            if (configurationSection != null) {
                nodeList.set(i, String.format("%s%s", "+", path));
            }
        }
    }
}
