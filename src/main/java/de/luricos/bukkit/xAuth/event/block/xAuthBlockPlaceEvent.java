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
package de.luricos.bukkit.xAuth.event.block;

import de.luricos.bukkit.xAuth.event.xAuthEvent;
import de.luricos.bukkit.xAuth.event.xAuthEventProperties;
import de.luricos.bukkit.xAuth.xAuthPlayer;
import org.bukkit.event.HandlerList;

/**
 * @author lycano
 */
public class xAuthBlockPlaceEvent extends xAuthEvent {

    private static final HandlerList handlers = new HandlerList();

    public xAuthBlockPlaceEvent(xAuthEventProperties properties) {
        super(properties);
    }

    public Action getAction() {
        return (Action) this.getProperty("action");
    }

    public xAuthPlayer.Status getStatus() {
        return (xAuthPlayer.Status) this.getProperty("status");
    }

    public String getPlayerName() {
        return (String) this.getProperty("playername");
    }

    public String getBlockType(){
        return (String) this.getProperty("blocktype");
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public enum Action {
        BLOCK_PLACE_CANCELLED
    }
}
