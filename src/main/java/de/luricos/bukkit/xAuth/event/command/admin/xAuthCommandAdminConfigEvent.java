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
package de.luricos.bukkit.xAuth.event.command.admin;

import de.luricos.bukkit.xAuth.event.xAuthEvent;
import de.luricos.bukkit.xAuth.event.xAuthEventProperties;
import org.bukkit.event.HandlerList;

/**
 * @author lycano
 */
public class xAuthCommandAdminConfigEvent extends xAuthEvent {

    private static final HandlerList handlers = new HandlerList();

    public xAuthCommandAdminConfigEvent(xAuthEventProperties properties) {
        super(properties);
    }

    public Action getAction() {
        return (Action) this.getProperty("action");
    }

    public String getNode() {
        return (String) this.getProperty("node");
    }

    public String getIssuedBy() {
        return (String) this.getProperty("issuedby");
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public enum Action {
        ERROR_NODE_INT, ERROR_NODE_INVALID, CONFIG_SUCCESS, ERROR_IS_SECTION, ERROR_NODE_EXIST
    }
}
