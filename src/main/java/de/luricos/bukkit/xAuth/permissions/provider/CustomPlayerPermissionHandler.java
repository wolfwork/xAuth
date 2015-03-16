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

import de.luricos.bukkit.xAuth.permissions.PermissionType;
import org.bukkit.entity.Player;

/**
 * @author lycano
 */
public class CustomPlayerPermissionHandler extends AbstractPlayerPermissionHandler {

    private String subSection = "internals";

    public CustomPlayerPermissionHandler(Player player, PermissionType permissionType) {
        this(player, permissionType.getPermissionNode(), null);
    }

    public CustomPlayerPermissionHandler(Player player, String permission) {
        this(player, permission, null);
    }

    public CustomPlayerPermissionHandler(Player player, PermissionType permissionType, String target) {
        this(player, permissionType.getPermissionNode(), target);
    }

    public CustomPlayerPermissionHandler(Player player, String permission, String target) {
        this.xauthPlayer = getPlayerManager().getPlayer(player.getName());
        this.playerStatus = this.xauthPlayer.getStatus();

        if (this.isAuthenticated()) {
            this.primaryNode = PermissionProviderPrimaryNode.XAUTH;
        }

        String permissionFormat = "%s.%s";
        if (target != null)
            permissionFormat = "%s.%s.%s";

        this.permissionString = String.format(permissionFormat, this.getPrimaryNode().getName(), permission, target);
        this.subSection = this.buildSubSection();
    }

    public String buildSubSection() {
        return this.camelCaseFirst(this.getPermissionString().split("\\.")[1]);
    }

    public String getSubSection() {
        return this.subSection;
    }

    @Override
    public boolean hasPermission() {
        boolean result = ((this.isAuthenticated()) ? this.hasAuthenticateAccess() : this.hasGuestAccess());
        this.sendDelayedDebugMessage(String.format("[HQ %s %s] Node: '%s',  result: %s",
                this.getPrimaryNode().getPrettyName(), this.getSubSection(), this.getPermissionString(), result
        ));

        return result;
    }

    @Override
    protected String getGuestConfigurationString() {
        return String.format("%s.%s", this.getPrimaryNode().getName(), this.getPermissionString());
    }

    /**
     * Player is restricted via permissions
     * Note: This system does not depend on guest permission node configuration
     *
     * @return boolean true if the player has access to that node
     *                 false if not found (no permission set) or denied via permissions
     */
    protected boolean hasAuthenticateAccess() {
        // check if the user is allowed to do so else check for denied flag if nothing found allow actions, restrict = false
        return getPermissionManager().has(getPlayer(), this.getPermissionString());
    }
}
