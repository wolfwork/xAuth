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
public class PlayerPermissionHandler extends PlayerPermissionProvider {

    private xAuthPlayer xPlayer;
    private Object[] obj;
    private xAuthPlayer.Status playerStatus;

    private PlayerPermissionNode permissionNode;
    private String permissionString;

    private boolean debugPermissions = false;
    private boolean guestAccessDefault = false; // default deny for guests

    public PlayerPermissionHandler(final Player player, final String eventName, Object... obj) {
        this.xPlayer = getPlayerManager().getPlayer(player.getName());
        this.obj = obj;
        this.permissionNode =  new PlayerPermissionNode(eventName);
        this.playerStatus = xPlayer.getStatus();
        this.debugPermissions = getConfig().getBoolean("permissions.debug", debugPermissions);
    }

    /**
     * Get xAuthPlayer
     *
     * @return xAuthPlayer
     */
    public xAuthPlayer getAuthPlayer() {
        return this.xPlayer;
    }

    /**
     * Fetch player via xAuthPlayer class
     *
     * @return Player Bukkit.getPlayerExact(String playerName)
     */
    public Player getPlayer() {
        return this.xPlayer.getPlayer();
    }

    public Object getObject() {
        return this.obj;
    }

    public xAuthPlayer.Status getPlayerStatus() {
        return playerStatus;
    }

    public boolean isGuest() {
        return getAuthPlayer().isGuest();
    }

    public boolean isAuthenticated() {
        return getAuthPlayer().isAuthenticated();
    }

    public boolean isRegistered() {
        return getAuthPlayer().isRegistered();
    }

    public PlayerPermissionNode getPermissionNode() {
        return this.permissionNode;
    }

    /**
     * Build permission string from node and store it for later use
     *
     * @return String permission node
     */
    public void buildPermissionString() {
        this.permissionString = this.buildPermissionNode();
    }

    public String buildPermissionNode() {
          return ((isAuthenticated() ? "xauth." : "guest.") + this.getPermissionNode().getPermissionNode(obj));
    }

    public String getPermissionString() {
        return this.permissionString;
    }

    /**
     * Use this to check to check permissions depending on the players status
     *
     * @return boolean true if not restricted false otherwise
     */
    public boolean hasPermission() {
        boolean result = false;
        switch (playerStatus) {
            case GUEST:
            case REGISTERED:
                result = this.hasGuestAccess();

                sendDelayedDebugMessage("[HQ Guest] ConfigNode: '" + this.getGuestConfigurationString() + "',  result: " + result + "\n" +
                                  "Event: '" + this.getPermissionNode().getEventName() + "', Section: '" + this.getPermissionNode().getEventType() + "', Action: '" + this.getPermissionNode().getAction() +"'");
                break;
            case AUTHENTICATED:
                result = this.hasAuthenticateAccess();

                sendDelayedDebugMessage("[HQ Authed] PermissionNode: '" + this.getPermissionString() + "',  result: " + result + "\n" +
                                  "Event: '" + this.getPermissionNode().getEventName() + "', Section: '" + this.getPermissionNode().getEventType() + "', Action: '" + this.getPermissionNode().getAction() + "'");
                break;
        }

        return result;
    }

    /**
     * Guest has restrictions enabled
     *
     * @return boolean true if guest node is allowed
     */
    private boolean hasGuestAccess() {
        return this.getGuestConfigurationNode();
    }

    private boolean getGuestConfigurationNode() {
        return this.getConfig().getBoolean(this.getGuestConfigurationString(), this.guestAccessDefault);
    }

    public String getGuestConfigurationString() {
        return "guest." + this.getPermissionNode().getPermissionNode(obj);
    }

    /**
     * Player is restricted via permissions
     * Note: This system does not depend on guest permission node configuration
     *
     * @return boolean true if the player has access to that node
     *                 false if not found (no permission set) or denied via permissions
     */
    private boolean hasAuthenticateAccess() {
        // check if the user is allowed to do so else check for denied flag if nothing found allow actions, restrict = false
        this.buildPermissionString();
        return getPermissionManager().has(getPlayer(), this.getPermissionString());
    }



    private void sendDelayedDebugMessage(final String msg) {
        if (!debugPermissions)
            return;

        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(xAuth.getPlugin(), new Runnable() {
            public void run() {
                xAuthLog.info(msg);
            }
        }, 3);
    }
}
