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

import java.util.Comparator;

/**
 * @author lycano
 */
public class xAuthTabCompleteComperator implements Comparator<String> {

    private final String keyword;

    public xAuthTabCompleteComperator(String keyword) {
        this.keyword = keyword;
    }

    @Override
    public int compare(String o1, String o2) {
        if (o1.startsWith(keyword)) {
            return o2.startsWith(keyword) ? o1.compareTo(o2) : -1;
        } else {
            return o2.startsWith(keyword) ? 1 : o1.compareTo(o2);
        }
    }
}
