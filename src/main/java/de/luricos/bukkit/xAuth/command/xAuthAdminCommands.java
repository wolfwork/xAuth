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

import de.luricos.bukkit.xAuth.exceptions.xAuthException;
import de.luricos.bukkit.xAuth.utils.CommandLineTokenizer;
import de.luricos.bukkit.xAuth.utils.xAuthLog;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public class xAuthAdminCommands extends xAuthAdminCommand implements CommandExecutor {

    private static final Map<String, Class<? extends xAuthAdminCommand>> COMMANDS_CACHE = new HashMap<String, Class<? extends xAuthAdminCommand>>();
    private static final Map<String, String> COMMAND_ALIASES = new HashMap<String, String>();

    public xAuthAdminCommands() {
    }

    public Class<? extends xAuthAdminCommand> getCommandClass(String commandClassName) throws ClassNotFoundException {
        if (!COMMANDS_CACHE.containsKey(commandClassName)) {
            xAuthLog.debug("Class '" + commandClassName + "' not found in cache ... loading");

            Class<?> clazz = Class.forName(String.format("%s.admin.%s", this.getClass().getPackage().getName(), commandClassName));
            if (!xAuthAdminCommand.class.isAssignableFrom(clazz)) {
                throw new IllegalArgumentException("Class " + commandClassName + " is not a subclass of xAuthAdminCommand.");
            }

            COMMANDS_CACHE.put(commandClassName, clazz.asSubclass(xAuthAdminCommand.class));
            return clazz.asSubclass(xAuthAdminCommand.class);
        }

        xAuthLog.debug("Class '" + commandClassName + "' fetched from cache.");
        return COMMANDS_CACHE.get(commandClassName);
    }

    public xAuthAdminCommand getCommandClass(String commandClassName, Command command) throws xAuthException {
        try {
            Class<? extends xAuthAdminCommand> commandClass = getCommandClass(commandClassName);
            Constructor<? extends xAuthAdminCommand> constructor = commandClass.getConstructor();
            return constructor.newInstance();
        } catch (ClassNotFoundException e) {
            return null;
        } catch (Throwable e) {
            if (e instanceof InvocationTargetException) {
                e = e.getCause();
                if (e instanceof xAuthException) {
                    throw ((xAuthException) e);
                }
            }

            throw new RuntimeException(e);
        }
    }

    /**
     * Set CommandAlias for specific command e.g. version can be aliased with ver
     *
     * @param command String the command
     * @param alias String the alias
     */
    public void setAliasCommand(String command, String alias) {
        alias = alias.toLowerCase();
        if (COMMAND_ALIASES.containsKey(alias)) {
            return;
        }

        COMMAND_ALIASES.put(alias, command.toLowerCase());
    }

    /**
     * Get commandClassName for Alias
     * @param alias String alias
     * @return String alias if nothing found
     */
    public String getAliasCommand(String alias) {
        alias = alias.toLowerCase();
        if (COMMAND_ALIASES.containsKey(alias))
            return COMMAND_ALIASES.get(alias);

        return alias;
    }

    /**
     * Capitalize first letter
     *
     * @param str String the string
     * @return String the first letter capitalized
     */
    private String capitalizeFirst(String str) {
        return (Character.toUpperCase(str.charAt(0)) + str.substring(1).toLowerCase());
    }


    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!((sender instanceof Player || sender instanceof ConsoleCommandSender || sender instanceof RemoteConsoleCommandSender))) {
            return false;
        }

        if (args.length < 1)
            return false;

        args = CommandLineTokenizer.tokenize(args);
        String subCommand = this.getAliasCommand(args[0]);

        String commandClassName = "Admin" + this.capitalizeFirst(subCommand) + "Command";
        xAuthAdminCommand commandClass = getCommandClass(commandClassName, command);

        boolean result = commandClass != null;
        if (result) {
            return commandClass.onCommand(sender, command, label, args);
        }

        this.getMessageHandler().sendMessage("misc.invalid-command", sender, subCommand);
        return true;
    }

}