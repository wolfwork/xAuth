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
package de.luricos.bukkit.xAuth.permissions.provider;

import org.bukkit.entity.Player;

/**
 * @author lycano
 */
public class GuestPlayerPermissionNode extends PlayerPermissionNode {

    public GuestPlayerPermissionNode(final String message) {
        super(message);
    }

    @Override
    public String getPermissionNode(Object[] arguments) {
        return this.assemblePermission(getPermissionNode(), this.filterGuestPermissionObj(arguments));
    }

    private Object[] filterGuestPermissionObj(Object[] arguments) {
        Object objectType = arguments[0];

        if (objectType instanceof Player) {
            return new Object[0];
        }

        return arguments;
    }
}
