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
import de.luricos.bukkit.xAuth.command.xAuthPlayerCountType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author lycano
 */
public class AdminCountCommandTabComplete extends xAuthCommandTabCompletion {

    private List<String> arguments = new ArrayList<String>();

    public AdminCountCommandTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        super(sender, command, alias, args);

        for (xAuthPlayerCountType type: xAuthPlayerCountType.values()) {
            this.arguments.add(type.getName());
        }
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
    public List<String> tabComplete(CommandSender sender, Command command, String alias, String[] args) {
        // sort arguments
        String[] sortedArguments = this.arguments.toArray(new String[this.arguments.size()]);
        Arrays.sort(sortedArguments, new xAuthTabCompleteComperator(args[1]));

        // walk through list and match against contains. remove non matching
        List<String> matchingArguments = new ArrayList<String>();
        for (String argument: sortedArguments) {
            if (argument.toLowerCase().contains(args[1].toLowerCase()))
                matchingArguments.add(argument);
        }

        return matchingArguments;
    }
}
