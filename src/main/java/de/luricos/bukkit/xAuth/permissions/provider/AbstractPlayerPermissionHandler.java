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

import de.luricos.bukkit.xAuth.utils.xAuthLog;
import de.luricos.bukkit.xAuth.xAuth;
import de.luricos.bukkit.xAuth.xAuthPlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * @author lycano
 */
public abstract class AbstractPlayerPermissionHandler extends PlayerPermissionProvider {

    protected xAuthPlayer xauthPlayer;
    protected Object[] obj;
    protected xAuthPlayer.Status playerStatus;

    protected PlayerPermissionNode permissionNode;
    protected String permissionString;
    protected PermissionProviderPrimaryNode primaryNode = PermissionProviderPrimaryNode.GUEST;

    protected boolean debugPermissions = false;
    protected boolean guestAccessDefault = false; // default deny for guests

    public AbstractPlayerPermissionHandler() {
        this.debugPermissions = getConfig().getBoolean("permissions.debug", debugPermissions);
    }

    /**
     * Get xAuthPlayer
     *
     * @return xAuthPlayer
     */
    public xAuthPlayer getAuthPlayer() {
        return this.xauthPlayer;
    }

    /**
     * Fetch player via xAuthPlayer class
     *
     * @return Player Bukkit.getPlayerExact(String playerName)
     */
    public Player getPlayer() {
        return this.xauthPlayer.getPlayer();
    }

    public Object getObject() {
        return this.obj;
    }

    public xAuthPlayer.Status getPlayerStatus() {
        return this.playerStatus;
    }

    public PermissionProviderPrimaryNode getPrimaryNode() {
        return this.primaryNode;
    }

    public boolean isGuest() {
        return this.getPlayerStatus().equals(xAuthPlayer.Status.GUEST);
    }

    public boolean isAuthenticated() {
        return this.getPlayerStatus().equals(xAuthPlayer.Status.AUTHENTICATED);
    }

    public boolean isRegistered() {
        return getAuthPlayer().isRegistered();
    }

    public PlayerPermissionNode getPermissionNode() {
        return this.permissionNode;
    }

    public boolean isDebug() {
        return this.debugPermissions;
    }

    public String getPermissionString() {
        return this.permissionString;
    }

    /**
     * Use this to check to check permissions depending on the players status
     *
     * @return boolean true if not restricted false otherwise
     */
    public abstract boolean hasPermission();

    protected void sendDelayedDebugMessage(final String msg) {
        if (!this.isDebug())
            return;

        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(xAuth.getPlugin(), new Runnable() {
            public void run() {
                xAuthLog.info(msg);
            }
        }, 3);
    }

    protected String camelCaseFirst(String s) {
        return s.substring(0,1).toUpperCase() + s.substring(1);
    }
}
