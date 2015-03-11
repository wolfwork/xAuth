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

import com.google.common.collect.Maps;

import java.util.Map;

/**
 * @author lycano
 */
public enum xAuthPlayerCountType {
    ALL(0, "all"),
    ACTIVE(1, "active"),
    LOCKED(2, "locked"),
    PREMIUM(3, "premium"),
    NON_PREMIUM(4, "non-premium");

    private final int typeId;
    private final String commonName;
    private static xAuthPlayerCountType[] byId = new xAuthPlayerCountType[5];
    private final static Map<String, xAuthPlayerCountType> BY_NAME = Maps.newHashMap();

    xAuthPlayerCountType(int typeId, String commonName) {
        this.typeId = typeId;
        this.commonName = commonName;
    }

    static {
        for (xAuthPlayerCountType countType : values()) {
            byId[countType.typeId] = countType;
            BY_NAME.put(countType.name(), countType);
        }
    }

    /**
     * Gets the type ID of this xAuthPlayerCountType
     *
     * @return ID of this xAuthPlayerCountType
     */
    public int getTypeId() {
        return typeId;
    }

    /**
     * Get the CountType's name
     *
     * @return String name of the xAuthPlayerCountType
     */
    public String getName() {
        return byId[this.getTypeId()].name();
    }

    public String getCommonName() {
        return this.commonName;
    }

    /**
     * Attempts to get the xAuthPlayerCountType with the given ID
     *
     * @param id ID of the type to get
     * @return xAuthPlayerCountType if found, or null
     */
    public static xAuthPlayerCountType getType(final int id) {
        if (byId.length > id && id >= 0) {
            return byId[id];
        }
        return null;
    }

    /**
     * Get the xAuthPlayerCountType via name
     *
     * @param name Name of the type to get
     * @return xAuthPlayerCountType
     */
    public static xAuthPlayerCountType getType(final String name) {
        return BY_NAME.get(name.toUpperCase());
    }
}
