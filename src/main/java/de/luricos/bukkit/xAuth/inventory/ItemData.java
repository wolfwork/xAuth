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

package de.luricos.bukkit.xAuth.inventory;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * @author lycano
 */
public class ItemData {

    public ItemData() {
    }

    public String toString(ItemStack[] itemStack) {
        ConfigurationSection container = new YamlConfiguration();

        for (int i = 0; i < itemStack.length; i++) {
            ItemStack item = itemStack[i];

            // don't store empty entries
            if (item != null) {
                container.set(Integer.toString(i), item);
            }
        }

        return ((YamlConfiguration) container).saveToString();
    }

    public String toString(Inventory inventory) {
        ConfigurationSection container = new YamlConfiguration();

        for (int i = 0; i < inventory.getSize(); i++) {
            ItemStack item = inventory.getItem(i);

            // don't store empty entries
            if (item != null) {
                container.set(Integer.toString(i), item);
            }
        }

        return ((YamlConfiguration) container).saveToString();
    }

    public ItemStack[] fromString(String data) throws InvalidConfigurationException {
        if (data == null)
            return null;

        ConfigurationSection container = new YamlConfiguration();
        ((YamlConfiguration) container).loadFromString(data);

        List<ItemStack> stacks = new ArrayList<ItemStack>();
        try {
            // try to parse inventory
            for (String key : container.getKeys(false)) {
                int number = Integer.parseInt(key);

                // fill empty entries
                while (stacks.size() <= number) {
                    stacks.add(null);
                }

                stacks.set(number, (ItemStack) container.get(key));
            }
        } catch (NumberFormatException e) {
            throw new InvalidConfigurationException("Expected a number.", e);
        }

        // Return result
        return stacks.toArray(new ItemStack[0]);
    }

}
