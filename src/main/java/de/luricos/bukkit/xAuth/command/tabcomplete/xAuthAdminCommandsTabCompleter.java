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
package de.luricos.bukkit.xAuth.command.tabcomplete;

import de.luricos.bukkit.xAuth.exceptions.xAuthException;
import de.luricos.bukkit.xAuth.permissions.provider.PlayerPermissionHandler;
import de.luricos.bukkit.xAuth.utils.xAuthLog;
import de.luricos.bukkit.xAuth.xAuth;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author lycano
 */
public class xAuthAdminCommandsTabCompleter implements TabCompleter {

    private static final Map<String, Class<? extends xAuthCommandTabCompletion>> COMPLETER_CACHE = new HashMap<String, Class<? extends xAuthCommandTabCompletion>>();

    public xAuthAdminCommandsTabCompleter() {
    }

    /**
     * Requests a list of possible completions for a command argument.
     *
     * @param sender  Source of the command
     * @param command Command which was executed
     * @param alias   The alias used
     * @param args    The arguments passed to the command, including final
     *                partial argument to be completed and command label
     * @return A List of possible completions for the final argument, or null
     * to default to the command executor
     */
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!((sender instanceof Player || sender instanceof ConsoleCommandSender || sender instanceof RemoteConsoleCommandSender))) {
            return null;
        }

        StringBuilder sb = new StringBuilder(command.getName());
        String subCommand = "";
        if (args.length > 1) {
            subCommand = this.capitalizeFirst(args[0]);
            sb.append(".").append(subCommand);
        }
        String node = sb.toString().toLowerCase();

        String tabCompleterName = this.getTabCompleterName(subCommand);
        boolean isValidTabCompleter = this.validateTabCompleter(tabCompleterName, sender, command, alias, args);
        if (!(isValidTabCompleter))
            return new ArrayList<String>();

        if (!this.isAllowedCommand(sender, "admin.permission", node)) {
            return new ArrayList<String>();
        }

        xAuthCommandTabCompletion commandClass = getCompleterClass(tabCompleterName, sender, command, alias, args);
        return commandClass.tabComplete(sender, command, alias, args);
    }

    private boolean validateTabCompleter(String tabCompleterName, CommandSender sender, Command command, String alias, String[] args) {
        return getCompleterClass(tabCompleterName, sender, command, alias, args) != null;
    }

    public String getTabCompleterName(String command) {
        return "Admin" + command + "CommandTabComplete";
    }

    public Class<? extends xAuthCommandTabCompletion> getCompleterClass(String tabCompleterName) throws ClassNotFoundException {
        if (!COMPLETER_CACHE.containsKey(tabCompleterName)) {
            xAuthLog.debug("Class '" + tabCompleterName + "' not found in cache ... loading");

            Class<?> clazz = Class.forName(String.format("%s.admin.%s", this.getClass().getPackage().getName(), tabCompleterName));
            if (!xAuthCommandTabCompletion.class.isAssignableFrom(clazz)) {
                throw new IllegalArgumentException("Class " + tabCompleterName + " is not a subclass of xAuthAdminCommandTabCompletion.");
            }

            COMPLETER_CACHE.put(tabCompleterName, clazz.asSubclass(xAuthCommandTabCompletion.class));
            return clazz.asSubclass(xAuthCommandTabCompletion.class);
        }

        xAuthLog.debug("Class '" + tabCompleterName + "' fetched from cache.");
        return COMPLETER_CACHE.get(tabCompleterName);
    }

    public xAuthCommandTabCompletion getCompleterClass(String completerName, CommandSender sender, Command command, String alias, String[] args) throws xAuthException {
        try {
            Class<? extends xAuthCommandTabCompletion> completerClass = getCompleterClass(completerName);
            Constructor<? extends xAuthCommandTabCompletion> constructor = completerClass.getConstructor(CommandSender.class, Command.class, String.class, String[].class);
            return constructor.newInstance(sender, command, alias, args);
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

    protected boolean isAllowedCommand(final CommandSender sender, final String messageNode, final String... command) {
        if (sender instanceof Player) {
            return this.isAllowedCommand((Player) sender, messageNode, command);
        }

        return (sender instanceof ConsoleCommandSender) || (sender instanceof RemoteConsoleCommandSender);
    }

    protected boolean isAllowedCommand(final Player player, final String messageNode, final String... command) {
        boolean allowed = new PlayerPermissionHandler(player, "PlayerCommandPreProcessEvent", command).hasPermission();
        if (!allowed)
            xAuth.getPlugin().getMessageHandler().sendMessage(messageNode, player);

        return allowed;
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


}
