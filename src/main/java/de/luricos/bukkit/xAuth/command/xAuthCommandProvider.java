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
package de.luricos.bukkit.xAuth.command;

import de.luricos.bukkit.xAuth.xAuth;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author lycano
 */
public class xAuthCommandProvider {

    private Map<String, List<xAuthCommandMap>> commandAliasMap = new HashMap<String, List<xAuthCommandMap>>();
    private Map<String, String> aliasCommandMap = new HashMap<String, String>();

    public xAuthCommandProvider() {
    }

    public void initialize() {
        for (String pluginCommand: this.getPluginCommands()) {
            List<String> aliases = xAuth.getPlugin().getCommand(pluginCommand).getAliases();

            if ((aliases.size() == 0)) {
                this.addCommandMap(pluginCommand, new xAuthCommandMap(pluginCommand, new xAuthCommandAlias(null)));
                continue;
            }

            for (String alias: aliases) {
                this.addCommandMap(pluginCommand, new xAuthCommandMap(pluginCommand, new xAuthCommandAlias(alias)));
            }
        }
    }

    public List<String> getPluginCommands() {
        return new ArrayList<String>(xAuth.getPlugin().getDescription().getCommands().keySet());
    }

    public boolean hasAlias(String alias) {
        return this.aliasCommandMap.containsKey(alias.toLowerCase());
    }

    public boolean hasCommand(String command) {
        return this.commandAliasMap.containsKey(command.toLowerCase());
    }

    /**
     * Set command for alias to keyring
     *
     * @param alias String the alias
     * @param command String the command
     */
    public void setAliasCommand(String alias, String command) {
        this.aliasCommandMap.put(alias.toLowerCase(), command.toLowerCase());
    }

    /**
     * Get the linked command for specified alias
     *
     * @param alias String the alias
     * @return null if not found otherwise the command as String
     */
    public String getAliasCommand(String alias) {
        if (!(this.hasAlias(alias)))
            return null;

        return this.aliasCommandMap.get(alias.toLowerCase());
    }

    /**
     * Get CommandMap from specified alias
     *
     * @param alias String the assigned alias
     * @return xAuthCommandMap if alias is assigned to a command or null if not found
     */
    public xAuthCommandMap getAliasCommandMap(String alias) {
        if (!(this.hasAlias(alias)))
            return null;

        return this.getCommandMap(this.getAliasCommand(alias));
    }

    /**
     * Adds a CommandAlias to the command pool if not exist in alias list
     *
     * @param command String the command
     * @param commandMap xAuthCommandMap the CommandMap
     */
    public void addCommandMap(String command, xAuthCommandMap commandMap) {
        if (commandMap.getAlias() != null) {
            this.setAliasCommand(commandMap.getAlias(), commandMap.getCommand());
        }

        List<xAuthCommandMap> commandMapList = this.getCommandMappings(command);
        if (commandMapList == null) {
            commandMapList = new ArrayList<xAuthCommandMap>();
        }

        // is there an alias present? Add to keyring
        if (!(commandMapList.contains(commandMap))) {
            commandMapList.add(commandMap);
        }

        this.commandAliasMap.put(command, commandMapList);
    }

    /**
     * Get a list of existing CommandMappings for a specified command
     *
     * @param command String command to lookup
     * @return null if no mapping exists otherweise a list of CommandMappings
     */
    public List<xAuthCommandMap> getCommandMappings(String command) {
        if (!(this.commandAliasMap.size() > 0))
            return null;

        return this.commandAliasMap.get(command);
    }

    /**
     * Get specific CommandMap for specified command
     *
     * @param command the command for that mapping
     * @return null if no mapping exists otherwise the mapping
     */
    public xAuthCommandMap getCommandMap(String command) {
        List<xAuthCommandMap> commandMappings = this.getCommandMappings(command);
        if (commandMappings == null) {
            return null;
        }

        for (xAuthCommandMap commandMap: commandMappings) {
            if (!(commandMap.getCommand().equals(command.toLowerCase())))
                continue;

            return commandMap;
        }

        return null;
    }

    /**
     * Lookup a command or alias and retunr the commandMap
     *
     * @param commandOrAlias String the command or the alias to lookup
     */
    public xAuthCommandMap lookup(String commandOrAlias) {
        if (this.hasAlias(commandOrAlias)) {
            return this.getAliasCommandMap(commandOrAlias);
        }

        if (this.hasCommand(commandOrAlias)) {
            return this.getCommandMap(commandOrAlias);
        }

        return null;
    }

    public boolean isResponsible(String commandOrAlias) {
        return this.hasCommand(commandOrAlias) || this.hasAlias(commandOrAlias);
    }

}
