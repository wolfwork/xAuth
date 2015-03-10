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
package de.luricos.bukkit.xAuth.listeners;

import de.luricos.bukkit.xAuth.command.xAuthCommandMap;
import de.luricos.bukkit.xAuth.command.xAuthCommandProvider;
import de.luricos.bukkit.xAuth.event.player.*;
import de.luricos.bukkit.xAuth.event.xAuthEventProperties;
import de.luricos.bukkit.xAuth.permissions.provider.PlayerPermissionHandler;
import de.luricos.bukkit.xAuth.utils.xAuthUtils;
import de.luricos.bukkit.xAuth.xAuth;
import de.luricos.bukkit.xAuth.xAuthPlayer;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.player.PlayerLoginEvent.Result;

import java.sql.Timestamp;

public class xAuthPlayerListener extends xAuthEventListener {

    public xAuthPlayerListener() {
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerLogin(final PlayerLoginEvent event) {
        if (!event.getResult().equals(Result.ALLOWED))
            return;

        Player player = event.getPlayer();
        String message;
        xAuthEventProperties properties = new xAuthEventProperties();
        properties.setProperty("playername", player.getName());

        if (player.isOnline()) {
            xAuthPlayer xp = playerManager.getPlayer(player);
            boolean reverse = xAuth.getPlugin().getConfig().getBoolean("single-session.reverse");

            if (reverse && !xp.isAuthenticated()) {
                if (!xAuth.getPlugin().getConfig().getBoolean("single-session.guests.reverse")) {
                    Timestamp expireTime = new Timestamp(xp.getConnectTime().getTime() +
                            (xAuth.getPlugin().getConfig().getInt("single-session.guests.immunity-length") * 1000));
                    reverse = !(expireTime.compareTo(new Timestamp(System.currentTimeMillis())) < 0);
                }
            }

            if (reverse) {
                message = xAuth.getPlugin().getMessageHandler().getNode("join.error.online");
                event.disallow(Result.KICK_OTHER, message);

                properties.setProperty("action", xAuthPlayerLoginEvent.Action.PLAYER_KICK);
                properties.setProperty("message", message);
                this.callEvent(new xAuthPlayerLoginEvent(properties));
            }
        }

        String ipAddress = event.getAddress().getHostAddress();
        if (xAuthUtils.isIPAddress(ipAddress)) {
            if (xAuth.getPlugin().getStrikeManager().isLockedOut(ipAddress, player.getName())) {
                message = xAuth.getPlugin().getMessageHandler().getNode("join.error.lockout");
                event.disallow(Result.KICK_OTHER, message);

                properties.setProperty("action", xAuthPlayerLoginEvent.Action.PLAYER_KICK);
                properties.setProperty("message", message);
                this.callEvent(new xAuthPlayerLoginEvent(properties));
            }
        }

        if (!isValidName(player.getName())) {
            event.disallow(Result.KICK_OTHER, xAuth.getPlugin().getMessageHandler().getNode("join.error.name"));
        }

        properties.setProperty("action", xAuthPlayerLoginEvent.Action.PLAYER_LOGGED_IN);
        properties.setProperty("message", "");
        this.callEvent(new xAuthPlayerLoginEvent(properties));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(final PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (player == null || !player.isOnline())
            return;

        String playerName = player.getName();

        xAuthPlayer xp = playerManager.getPlayer(player, xAuth.getPlugin().getConfig().getBoolean("main.reload-on-join"));
        xp.setConnectTime(new Timestamp(System.currentTimeMillis()));

        String node = "";
        boolean protect = false;

        if (xp.isRegistered() || xAuth.getPlugin().isAuthURL()) {
            if ((!xp.isLocked()) && (playerManager.checkSession(xp))) {
                xp.setStatus(xAuthPlayer.Status.AUTHENTICATED);
                xp.setGameMode(player.getGameMode()); // update xp gamemode
                xAuth.getPlugin().getAuthClass(xp).online(playerName);
                node = "join.resume";
            } else {
                xp.setStatus(xAuthPlayer.Status.REGISTERED);
                node = "join.login";
                protect = true;
                //playerManager.protect(xp);
            }
        } else if (playerManager.mustRegister(player)) {
            xp.setStatus(xAuthPlayer.Status.GUEST);
            node = "join.register";
            protect = true;
        }

        if (protect) {
            xp.setProtected(true);
            playerManager.getTasks().scheduleDelayedProtectTask(playerName);
        }

        if (!node.isEmpty())
            playerManager.getTasks().scheduleDelayedMessageTask(playerName, node);

        playerManager.getTasks().scheduleDelayedPremiumCheck(playerName);

        xAuthEventProperties properties = new xAuthEventProperties();
        properties.setProperty("action", xAuthPlayerJoinEvent.Action.PLAYER_JOINED);
        properties.setProperty("status", xp.getStatus());
        properties.setProperty("playername", playerName);
        this.callEvent(new xAuthPlayerJoinEvent(properties));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        String playerName = event.getPlayer().getName();
        xAuthPlayer xp = playerManager.getPlayer(playerName);

        // player did not reset his password so we lock his account for security reasons.
        if (xp.isReset()) {
            xp.lockPlayer();
            playerManager.lockAcc(xp.getAccountId());
            xp.setReset(false);
            xAuth.getPlugin().getPlayerManager().getPlayer(playerName).setReset(false);
        }

        // @TODO update player object
        // @TODO update logout time (to implement)

        if (xp.isProtected())
            playerManager.unprotect(xp);

        xAuth plugin = xAuth.getPlugin();

        plugin.getAuthClass(xp).offline(playerName);
        plugin.getPlayerManager().releasePlayer(playerName);
        plugin.getPlayerManager().getTasks().cancelTasks(playerName);

        xAuthEventProperties properties = new xAuthEventProperties();
        properties.setProperty("action", xAuthPlayerQuitEvent.Action.PLAYER_DISCONNECTED);
        properties.setProperty("playername", playerName);
        properties.setProperty("message", event.getQuitMessage());
        this.callEvent(new xAuthPlayerQuitEvent(properties));
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPlayerChat(PlayerChatEvent event) {
        if (this.hasPlayerChatPermission(event.getPlayer(), event))
            return;

        event.setCancelled(true);

        xAuthEventProperties properties = new xAuthEventProperties();
        properties.setProperty("action", xAuthPlayerChatEvent.Action.CHAT_CANCELLED);
        properties.setProperty("status", playerManager.getPlayer(event.getPlayer().getName()).getStatus());
        properties.setProperty("playername", event.getPlayer().getName());
        properties.setProperty("message", event.getMessage());
        this.callEvent(new xAuthPlayerChatEvent(properties));
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
        if (this.hasPlayerChatPermission(event.getPlayer(), event))
            return;

        event.setCancelled(true);

        xAuthEventProperties properties = new xAuthEventProperties();
        properties.setProperty("action", xAuthPlayerChatEvent.Action.CHAT_CANCELLED);
        properties.setProperty("status", playerManager.getPlayer(event.getPlayer().getName()).getStatus());
        properties.setProperty("playername", event.getPlayer().getName());
        properties.setProperty("message", event.getMessage());
        this.callEvent(new xAuthPlayerChatEvent(properties));
    }

    private boolean hasPlayerChatPermission(Player player, Event event) {
        return new PlayerPermissionHandler(player, "PlayerChatEvent", player).hasPermission();
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        String[] commands = event.getMessage().replaceFirst("/", "").split("\\s");
        String command = commands[0].toLowerCase();

        // init commandProvider
        xAuthCommandProvider commandProvider = xAuth.getPlugin().getCommandProvider();
        xAuthCommandMap xauthCommand = commandProvider.getCommandMap("xauth");

        // map alias to command if we are responsible for that command
        // exclude xauth and x alias so we can block it
        if (commandProvider.isResponsible(command)) {
            String aliasCommand = commandProvider.getAliasCommand(command);
            if (aliasCommand != null) {
                event.setMessage(event.getMessage().replaceFirst(command, aliasCommand));
                command = aliasCommand;
                commands[0] = command;
            }

            if (!((xauthCommand.getCommand().equals(command) || xauthCommand.getAlias().equals(command))))
                return;
        }

        Player player = event.getPlayer();
        PlayerPermissionHandler permissionHandler = new PlayerPermissionHandler(player, event.getEventName(), commands);

        xAuthPlayer xp = playerManager.getPlayer(player.getName());

        xAuthEventProperties properties = new xAuthEventProperties();
        properties.setProperty("playername", player.getName());
        properties.setProperty("status", xp.getStatus());
        properties.setProperty("command", command);
        if (this.isAllowedCommand(player, commands)) {
            properties.setProperty("action", xAuthPlayerExecuteCommandEvent.Action.COMMAND_ALLOWED);
        } else {
            this.sendCommandRestrictedMessage(xp, event, permissionHandler, commands);
            properties.setProperty("action", xAuthPlayerExecuteCommandEvent.Action.COMMAND_DENIED);
        }
        this.callEvent(new xAuthPlayerExecuteCommandEvent(properties));
    }

    private void sendCommandRestrictedMessage(final xAuthPlayer xp, final PlayerCommandPreprocessEvent event, final PlayerPermissionHandler permissionHandler, String[] commands) {
        playerManager.sendNotice(xp, permissionHandler.getPermissionNode().getAction() + '.' + commands[0]);
        event.setMessage("/");
        event.setCancelled(true);
    }


    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerDropItem(final PlayerDropItemEvent event) {
        if (xAuth.getPlugin().getConfig().getBoolean("guest.hide-inventory"))
            return;

        if (this.isAllowed(event.getPlayer(), event, event))
            return;

        xAuthPlayer xp = playerManager.getPlayer(event.getPlayer());
        playerManager.sendNotice(xp);
        event.setCancelled(true);

        xAuthEventProperties properties = new xAuthEventProperties();
        properties.setProperty("action", xAuthPlayerDropItemEvent.Action.ITEMDROP_DENIED);
        properties.setProperty("status", xp.getStatus());
        properties.setProperty("playername", xp.getName());
        properties.setProperty("itemtype", event.getItemDrop().getItemStack().getData().getItemType().name());
        this.callEvent(new xAuthPlayerDropItemEvent(properties));
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Entity interactWithEntity = event.getRightClicked();
        Player player = event.getPlayer();

        if ((this.isAllowed(player, event, interactWithEntity)) || (this.isAllowed(player, event, player)))
            return;

        xAuthPlayer xp = playerManager.getPlayer(player);
        playerManager.sendNotice(xp);
        event.setCancelled(true);

        xAuthEventProperties properties = new xAuthEventProperties();
        properties.setProperty("action", xAuthPlayerInteractEntityEvent.Action.INTERACT_WITH_ENTITY_DENIED);
        properties.setProperty("status", xp.getStatus());
        properties.setProperty("playername", xp.getName());
        properties.setProperty("interactwithentity", interactWithEntity.getClass().getSimpleName());
        this.callEvent(new xAuthPlayerInteractEntityEvent(properties));
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (this.isAllowed(player, event, event.getClickedBlock()))
            return;

        xAuthPlayer xp = playerManager.getPlayer(player.getName());
        playerManager.sendNotice(xp);
        event.setCancelled(true);

        xAuthEventProperties properties = new xAuthEventProperties();
        properties.setProperty("action", xAuthPlayerInteractEvent.Action.INTERACT_DENIED);
        properties.setProperty("status", xp.getStatus());
        properties.setProperty("playername", xp.getName());
        properties.setProperty("clickedblock", event.getClickedBlock().getClass().getSimpleName());
        this.callEvent(new xAuthPlayerInteractEvent(properties));
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        // do nothing if player hasn't moved a block or world.
        // This will also filter out any player that only moves his mouse
        if (!this.hasChangedBlockCoordinates(event.getFrom(), event.getTo()))
            return;

        if (this.isAllowed(event.getPlayer(), event, event.getPlayer()))
            return;

        xAuthPlayer xp = playerManager.getPlayer(event.getPlayer());

        World w = xp.getPlayer().getWorld();
        Location loc = (xAuth.getPlugin().getConfig().getBoolean("guest.protect-location")) ? xAuth.getPlugin().getLocationManager().getLocation(w) : xp.getPlayerData().getLocation();
        Location testLoc = new Location(loc.getWorld(), loc.getX(), loc.getY(), loc.getZ());

        // @TODO check if this is causing PlayerDeath
        while ((w.getBlockAt(testLoc).isEmpty() || w.getBlockAt(testLoc).isLiquid()) && testLoc.getY() >= 0) {
            testLoc.setY((int) testLoc.getY() - 1);
        }

        // @TODO this would set you one block higher then before (eye level), still needed in later builds?
        if (testLoc.getY() > 0) {
            loc.setY(testLoc.getY() + 1);
        }

        event.setTo(loc);
        playerManager.sendNotice(xp);

        xAuthEventProperties properties = new xAuthEventProperties();
        properties.setProperty("action", xAuthPlayerMoveEvent.Action.MOVE_DENIED);
        properties.setProperty("status", xp.getStatus());
        properties.setProperty("playername", xp.getName());
        this.callEvent(new xAuthPlayerMoveEvent(properties));
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerPickupItem(PlayerPickupItemEvent event) {
        if (this.isAllowed(event.getPlayer(), event, event))
            return;

        event.setCancelled(true);

        xAuthEventProperties properties = new xAuthEventProperties();
        properties.setProperty("action", xAuthPlayerPickupItemEvent.Action.PICKUP_DENIED);
        properties.setProperty("status", playerManager.getPlayer(event.getPlayer().getName()).getStatus());
        properties.setProperty("itemtype", event.getItem().getItemStack().getData().getItemType().name());
        properties.setProperty("playername", event.getPlayer().getName());
        this.callEvent(new xAuthPlayerPickupItemEvent(properties));
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerShootBowEvent(EntityShootBowEvent event) {
        if (!(event.getEntity() instanceof Player))
            return;

        Player player = (Player) event.getEntity();

        if (this.isAllowed(player, event, player))
            return;

        event.setCancelled(true);

        xAuthEventProperties properties = new xAuthEventProperties();
        properties.setProperty("action", xAuthPlayerShootBowEvent.Action.SHOOT_BOW_DENIED);
        properties.setProperty("status", playerManager.getPlayer(player.getName()).getStatus());
        properties.setProperty("playername", player.getName());
        this.callEvent(new xAuthPlayerShootBowEvent(properties));
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void onPlayerGameModeChange(final PlayerGameModeChangeEvent event) {
        // update gamemode settings for xAuthPlayer class
        Player player = event.getPlayer();
        xAuthPlayer xp = playerManager.getPlayer(player);

        xAuthEventProperties properties = new xAuthEventProperties();
        properties.setProperty("oldgamemode", xp.getGameMode());
        xp.setGameMode(event.getNewGameMode());

        properties.setProperty("newgamemode", event.getNewGameMode());
        properties.setProperty("action", xAuthPlayerGameModeChangeEvent.Action.GAMEMODE_CHANGED);
        properties.setProperty("status", xp.getStatus());
        properties.setProperty("playername", xp.getName());
        this.callEvent(new xAuthPlayerGameModeChangeEvent(properties));
    }

    private boolean isValidName(String pName) {
        if (pName.length() < xAuth.getPlugin().getConfig().getInt("filter.min-length"))
            return false;

        String allowed = xAuth.getPlugin().getConfig().getString("filter.allowed");
        if (allowed.length() > 0) {
            for (int i = 0; i < pName.length(); i++) {
                if (allowed.indexOf(pName.charAt(i)) == -1)
                    return false;
            }
        }

        String disallowed = xAuth.getPlugin().getConfig().getString("filter.disallowed");
        if (disallowed.length() > 0) {
            for (int i = 0; i < pName.length(); i++) {
                if (disallowed.indexOf(pName.charAt(i)) >= 0) {
                    return false;
                }
            }
        }

        return !(xAuth.getPlugin().getConfig().getBoolean("filter.blank-name") && xAuthUtils.isWhitespace(pName));
    }

    /**
     * Check if the player has moved a block to prevent PlayerMoveEvent to become spammy
     * Also allow a one block jump to not reset on every jump
     *
     * @param fromLoc Location from
     * @param toLoc Location to
     *
     * @return boolean true if the location is different from its source
     */
    private boolean hasChangedBlockCoordinates(final Location fromLoc, final Location toLoc) {
        return !(fromLoc.getWorld().equals(toLoc.getWorld())
                && fromLoc.getBlockX() == toLoc.getBlockX()
                && (fromLoc.getBlockY() <= (toLoc.getBlockY() + 1))
                && fromLoc.getBlockZ() == toLoc.getBlockZ());
    }
}