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
package de.luricos.bukkit.xAuth.database;

public enum DatabaseRows {

    /* ACCOUNT_TABLE */
    ACCOUNT_ID("id"),
    ACCOUNT_PLAYERNAME("playername"),
    ACCOUNT_PASSWORD("password"),
    ACCOUNT_PWTYPE("pwtype"),
    ACCOUNT_EMAIL("email"),
    ACCOUNT_REGISTERDATE("registerdate"),
    ACCOUNT_REGISTERIP("registerip"),
    ACCOUNT_LASTLOGINDATE("lastlogindate"),
    ACCOUNT_LASTLOGINIP("lastloginip"),
    ACCOUNT_ACTIVE("active"),
    ACCOUNT_RESETPW("resetpw"),
    ACCOUNT_PREMIUM("premium"),

    /* LOCATION TABLE */
    LOCATION_UID("uid"),
    LOCATION_X("x"),
    LOCATION_Y("y"),
    LOCATION_Z("z"),
    LOCATION_YAW("yaw"),
    LOCATION_PITCH("pitch"),
    LOCATION_GLOBAL("global"),

    /* LOCKOUT_TABLE */
    LOCKOUT_IP("ip"),
    LOCKOUT_IPADDRESS("ipaddress"),
    LOCKOUT_PLAYERNAME("playername"),
    LOCKOUT_TIME("time"),

    /* PLAYERDATA_TABLE */
    PLAYERDATA_PLAYERNAME("playername"),
    PLAYERDATA_ITEMS("items"),
    PLAYERDATA_ARMOR("armor"),
    PLAYERDATA_LOCATION("location"),
    PLAYERDATA_POTIONEFFECTS("potioneffects"),
    PLAYERDATA_FIRETICKS("fireticks"),
    PLAYERDATA_REMAININGAIR("remainingair"),
    PLAYERDATA_GAMEMODE("gamemode"),

    /* SESSION_TABLE */
    SESSION_ACCOUNTID("accountid"),
    SESSION_IPADDRESS("ipaddress"),
    SESSION_LOGINTIME("logintime");

    private String name;

    DatabaseRows(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }
}