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
package de.luricos.bukkit.xAuth.permissions;

import de.luricos.bukkit.xAuth.event.system.xAuthSystemEvent;
import de.luricos.bukkit.xAuth.event.xAuthEventProperties;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.Configuration;
import org.bukkit.entity.Player;

/**
 * @author lycano
 */
public class PermissionManager {

    protected PermissionBackend backend = null;
    protected Configuration config;

    public PermissionManager(Configuration config) {
        this.config = config;
        this.initBackend();
    }

    private void initBackend() {
        String backendName = this.config.getString("permissions.backend");

        if (backendName == null || backendName.isEmpty()) {
            backendName = PermissionBackend.defaultBackend;
            this.config.set("permissions.backend", backendName);
        }

        this.setBackend(backendName);
    }

    /**
     * Return current backend
     *
     * @return current backend object
     */
    public PermissionBackend getBackend() {
        return this.backend;
    }

    public void setBackend(String backendName) {
        xAuthEventProperties properties = new xAuthEventProperties();
        properties.setProperty("backendname", backendName);
        properties.setProperty("action", xAuthSystemEvent.Action.PERMISSION_BACKEND_LOADING);
        this.callEvent(new xAuthSystemEvent(properties));

        synchronized (this) {
            this.backend = PermissionBackend.getBackend(backendName, this, this.config);
            this.backend.setProviderState(PermissionBackend.PROVIDER_STATE.STARTUP);
            this.backend.initialize();
        }

        properties.setProperty("action", xAuthSystemEvent.Action.PERMISSION_BACKEND_READY);
        this.callEvent(new xAuthSystemEvent(properties));

        properties.setProperty("action", xAuthSystemEvent.Action.PERMISSION_BACKEND_CHANGED);
        this.callEvent(new xAuthSystemEvent(properties));

        this.backend.setProviderState(PermissionBackend.PROVIDER_STATE.READY);
    }

    protected void callEvent(xAuthSystemEvent event) {
        Bukkit.getServer().getPluginManager().callEvent(event);
    }

    public void reset() {
        xAuthEventProperties properties = new xAuthEventProperties();
        properties.setProperty("action", xAuthSystemEvent.Action.PERMISSION_BACKEND_RELOADING);
        this.callEvent(new xAuthSystemEvent(properties));

        if (this.backend != null) {
            this.backend.setProviderState(PermissionBackend.PROVIDER_STATE.RELOAD);
            this.backend.reload();
            properties.setProperty("backendname", this.backend.getProviderName());
        }

        properties.setProperty("action", xAuthSystemEvent.Action.PERMISSION_BACKEND_RELOADED);
        this.callEvent(new xAuthSystemEvent(properties));
    }

    public void end() {
        if (this.backend != null) {
            this.backend.setProviderState(PermissionBackend.PROVIDER_STATE.END);
            this.backend.end();
        }

        xAuthEventProperties properties = new xAuthEventProperties();
        properties.setProperty("action", xAuthSystemEvent.Action.PERMISSION_BACKEND_ENDED);
        this.callEvent(new xAuthSystemEvent(properties));
    }

    public PermissionBackend.PROVIDER_STATE getBackendState(String state) {
        return this.backend.getProviderState();
    }

    public boolean has(CommandSender sender, String permissionString) {
        return !(sender instanceof Player) || backend.has((Player) sender, permissionString);
    }

    public boolean hasPermission(CommandSender sender, String permissionString) {
        return !(sender instanceof Player) || backend.hasPermission((Player) sender, permissionString);
    }

    public boolean has(Player player, String permissionString) {
        return backend.has(player, permissionString);
    }

    public boolean hasPermission(Player player, String permissionString) {
        return backend.hasPermission(player, permissionString);
    }

    public void joinGroup(Player player, String groupName) {
        backend.joinGroup(player, groupName);
    }

    public void joinGroup(String playerName, String groupName) {
        backend.joinGroup(playerName, groupName);
    }
}
