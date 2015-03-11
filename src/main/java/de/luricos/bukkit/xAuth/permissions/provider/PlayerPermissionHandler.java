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

    private xAuthPlayer xauthPlayer;
    private Object[] obj;
    private xAuthPlayer.Status playerStatus;

    private PlayerPermissionNode permissionNode;
    private String permissionString;
    private String primaryNode = "guest";

    private boolean debugPermissions = false;
    private boolean guestAccessDefault = false; // default deny for guests

    public PlayerPermissionHandler(final Player player, final String eventName, Object... obj) {
        this.xauthPlayer = getPlayerManager().getPlayer(player.getName());
        this.obj = obj;
        this.playerStatus = this.xauthPlayer.getStatus();
        this.debugPermissions = getConfig().getBoolean("permissions.debug", debugPermissions);

        if (this.isAuthenticated()) {
            this.permissionNode = new AuthenticatedPlayerPermissionNode(eventName);
            this.primaryNode = "xauth";
            return;
        }

        this.permissionNode = new GuestPlayerPermissionNode(eventName);
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

    public String getPrimaryNode() {
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

    /**
     * Build permission string from node and store it for later use
     *
     * @return String permission node
     */
    public void buildPermissionString() {
        this.permissionString = this.buildPermissionNode();
    }

    public String buildPermissionNode() {
        return (String.format("%s.%s", this.getPrimaryNode(), this.getPermissionNode().getPermissionNode(this.obj)));
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
        boolean result = ((this.isAuthenticated()) ? this.hasAuthenticateAccess() : this.hasGuestAccess());
        this.sendDelayedDebugMessage(String.format("[HQ %s] ConfigNode: '%s',  result: %s\nEvent: '%s', Section: '%s', Action: '%s'",
                this.getPrimaryNode(), this.getGuestConfigurationString(), result, this.getPermissionNode().getEventName(), this.getPermissionNode().getEventType(), this.getPermissionNode().getAction()
        ));

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
        return String.format("%s.%s", this.getPrimaryNode(), this.getPermissionNode().getPermissionNode(this.obj));
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
