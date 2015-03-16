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
package de.luricos.bukkit.xAuth;

import de.luricos.bukkit.xAuth.auth.AuthMethod;
import de.luricos.bukkit.xAuth.command.xAuthPlayerCountType;
import de.luricos.bukkit.xAuth.database.DatabaseController;
import de.luricos.bukkit.xAuth.database.DatabaseRows;
import de.luricos.bukkit.xAuth.database.DatabaseTables;
import de.luricos.bukkit.xAuth.event.player.xAuthPlayerProtectEvent;
import de.luricos.bukkit.xAuth.event.player.xAuthPlayerUnProtectEvent;
import de.luricos.bukkit.xAuth.event.xAuthEventProperties;
import de.luricos.bukkit.xAuth.exceptions.xAuthPlayerUnprotectException;
import de.luricos.bukkit.xAuth.permissions.provider.PlayerPermissionHandler;
import de.luricos.bukkit.xAuth.tasks.xAuthTask;
import de.luricos.bukkit.xAuth.tasks.xAuthTasks;
import de.luricos.bukkit.xAuth.updater.HTTPRequest;
import de.luricos.bukkit.xAuth.utils.xAuthLog;
import de.luricos.bukkit.xAuth.utils.xAuthUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import java.sql.*;
import java.util.*;

public class PlayerManager {
    private final xAuth plugin;
    private final Map<String, xAuthPlayer> players = new HashMap<String, xAuthPlayer>();
    private Map<Integer, String> playerIds = new HashMap<Integer, String>();
    private Map<Integer, String> offlinePlayerIdMap = new HashMap<Integer, String>();
    private xAuthTasks tasks;

    public PlayerManager(final xAuth plugin, xAuthTasks tasks) {
        this.plugin = plugin;
        this.tasks = tasks;
    }
    
    public xAuthPlayer getPlayer(Player player) {
        return getPlayer(player.getName(), false);
    }

    public xAuthPlayer getPlayer(Player player, boolean reload) {
        return getPlayer(player.getName(), reload);
    }

    public xAuthPlayer getPlayer(String playerName) {
        return this.getPlayer(playerName, false);
    }

    private xAuthPlayer getPlayer(String playerName, boolean reload) {
        String lowPlayerName = playerName.toLowerCase();

        if (this.players.containsKey(lowPlayerName) && !reload) {
            return this.players.get(lowPlayerName);
        }

        xAuthPlayer xp = this.loadPlayer(playerName);

        if (xp == null) {
            xp = new xAuthPlayer(playerName);
        }

        this.players.put(lowPlayerName, xp);
        return xp;
    }

    private void addPlayerId(int id, String playerName) {
        if (!(this.hasAccountId(id)))
            this.playerIds.put(id, playerName.toLowerCase());
    }

    public xAuthPlayer getPlayerById(int id) {
        return this.getPlayerById(id, false);
    }

    public xAuthPlayer getPlayerById(int id, boolean reload) {
        if (this.hasAccountId(id))
            return this.getPlayer(this.playerIds.get(id), reload);

        return null;
    }

    public List<xAuthPlayer> getPlayers(List<String> playerNames) {
        List<xAuthPlayer> xPlayers = new ArrayList<xAuthPlayer>();
        for (String playerName: playerNames) {
            xPlayers.add(this.getPlayer(playerName));
        }

        return xPlayers;
    }

    public List<xAuthPlayer> getPlayersByIds(List<Integer> accountIds) {
        List<xAuthPlayer> xPlayers = new ArrayList<xAuthPlayer>();
        for (int accountId: accountIds) {
            xPlayers.add(getPlayerById(accountId));
        }

        return xPlayers;
    }

    public boolean hasAccountId(int id) {
        return this.playerIds.containsKey(id);
    }

    /**
     * Get a list of all playerNames directly from the database
     *
     * @return List<String> a list of playerNames
     */
    private List<String> getOfflinePlayerNames() {

        Connection conn = this.getDatabaseController().getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            String sql = String.format("SELECT `%s` FROM `%s`",
                    this.getRow(DatabaseRows.ACCOUNT_PLAYERNAME), this.getTable(DatabaseTables.ACCOUNT));
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            if (!rs.next())
                return null;

            List<String> playerNames = new ArrayList<String>();
            while(rs.next()) {
                playerNames.add(rs.getString(this.getRow(DatabaseRows.ACCOUNT_PLAYERNAME)));
            }

            return playerNames;
        } catch (SQLException e) {
            xAuthLog.severe(String.format("Failed to fetch playerNames"), e);
            return null;
        } finally {
            this.getDatabaseController().close(conn, ps, rs);
        }
    }

    /**
     * Get a list of all playerIds directly from the database if forced
     *
     * @param live boolean set to fetch from database - use with care!
     * @return a list of playerNames as string or null of no players exist in db or in cache
     */
    //@TODO needs caching; Update when player join/leave via xAuthEvent
    public List<Integer> getOfflinePlayerIds(boolean live) {
        if (live) {
            return Arrays.asList(this.getOfflinePlayerData(true).keySet().toArray(new Integer[this.offlinePlayerIdMap.size()]));
        }

        if (this.offlinePlayerIdMap.size() > 0) {
            return Arrays.asList(this.offlinePlayerIdMap.keySet().toArray(new Integer[this.offlinePlayerIdMap.size()]));
        }

        return null;
    }

    /**
     * Get a list of all playerNames directly from database if forced
     * @see PlayerManager#getOfflinePlayerIds
     */
    //@TODO needs caching; Update when player join/leave via xAuthEvent
    public List<String> getOfflinePlayerNames(boolean live) {
        if (live) {
            return Arrays.asList(this.getOfflinePlayerData(true).values().toArray(new String[this.offlinePlayerIdMap.size()]));
        }

        if (this.offlinePlayerIdMap.size() > 0) {
            return Arrays.asList(this.offlinePlayerIdMap.values().toArray(new String[this.offlinePlayerIdMap.size()]));
        }

        return null;
    }

    private Map<Integer, String> getOfflinePlayerData(boolean live) {
        Connection conn = this.getDatabaseController().getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        Map<Integer, String> playerData = new HashMap<Integer, String>();

        try {
            String sql = String.format("SELECT `%s`,`%s` FROM `%s`",
                    this.getRow(DatabaseRows.ACCOUNT_ID), this.getRow(DatabaseRows.ACCOUNT_PLAYERNAME), this.getTable(DatabaseTables.ACCOUNT));
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            if (!rs.next())
                return null;

            while(rs.next()) {
                playerData.put(rs.getInt(this.getRow(DatabaseRows.ACCOUNT_ID)), rs.getString(this.getRow(DatabaseRows.ACCOUNT_PLAYERNAME)));
            }
        } catch (SQLException e) {
            xAuthLog.severe(String.format("Failed to fetch playerIds"), e);
            return null;
        } finally {
            this.getDatabaseController().close(conn, ps, rs);
        }

        return playerData;
    }


    private xAuthPlayer loadPlayer(String playerName) {
        Connection conn = this.getDatabaseController().getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            String sql = String.format("SELECT * FROM `%s` WHERE `%s` = ?",
                    this.getTable(DatabaseTables.ACCOUNT), this.getRow(DatabaseRows.ACCOUNT_PLAYERNAME));
            ps = conn.prepareStatement(sql);
            ps.setString(1, playerName);
            rs = ps.executeQuery();
            if (!rs.next())
                return null;

            this.addPlayerId(rs.getInt(this.getRow(DatabaseRows.ACCOUNT_ID)), playerName);

            return new xAuthPlayer(playerName,
                    rs.getInt(this.getRow(DatabaseRows.ACCOUNT_ID)),
                    !rs.getBoolean(this.getRow(DatabaseRows.ACCOUNT_ACTIVE)),
                    rs.getBoolean(this.getRow(DatabaseRows.ACCOUNT_RESETPW)),
                    xAuthPlayer.Status.REGISTERED,
                    rs.getInt(this.getRow(DatabaseRows.ACCOUNT_PWTYPE)),
                    rs.getBoolean(this.getRow(DatabaseRows.ACCOUNT_PREMIUM)),
                    GameMode.valueOf(this.getConfig().getString("guest.gamemode", Bukkit.getDefaultGameMode().name())));
        } catch (SQLException e) {
            xAuthLog.severe(String.format("Failed to load player: %s", playerName), e);
            return null;
        } finally {
            this.getDatabaseController().close(conn, ps, rs);
        }
    }

    public void reload() {
        this.players.clear();
        this.playerIds.clear();

        Player[] players = Bukkit.getServer().getOnlinePlayers();
        if (players.length > 0)
            this.handleReload(players);
    }

    public void releasePlayer(String playerName) {
        xAuthPlayer xp = getPlayer(playerName);
        this.playerIds.remove(xp.getAccountId());
        this.players.remove(playerName.toLowerCase());
    }

    public xAuthTasks getTasks() {
        return this.tasks;
    }

    public void handleReload(Player[] players) {
        for (Player p : players) {
            xAuthPlayer xp = getPlayer(p.getName());
            boolean mustLogin = false;

            if (xp.isRegistered()) {
                if (!checkSession(xp)) {
                    mustLogin = true;
                    this.getAuthClass(xp).offline(p.getName());
                } else {
                    xp.setStatus(xAuthPlayer.Status.AUTHENTICATED);
                    // remove xp.setGameMode(Bukkit.getDefaultGameMode()) - Moved to xAuthPlayer constructor
                    this.getAuthClass(xp).online(p.getName());
                }
            } else if (mustRegister(p)) {
                mustLogin = true;
                this.getAuthClass(xp).offline(p.getName());
            }

            if (mustLogin) {
                this.protect(xp);
                this.getMessageHandler().sendMessage("misc.reloaded", p);
            }
        }
    }

    public boolean mustRegister(Player player) {
        if (this.getConfig().getBoolean("authurl.enabled"))
            return this.getConfig().getBoolean("authurl.registration");

        return ((this.getConfig().getBoolean("registration.forced")) || (this.isAllowedCommand(player, "register.permission", "register")));
    }

    /**
     * Check player session by accountId
     *
     * @param accountId the account id of that player
     * @return boolean true if session exists false otherwise
     */
    public boolean checkSession(final int accountId) {
        return this.checkSession(this.getPlayerById(accountId));
    }

    /**
     * Check player ssseion by xAuthPlayer reference
     *
     * @param player the xAuthPlayer
     * @return boolean true if session exists false otherwise
     */
    public boolean checkSession(final xAuthPlayer player) {
        if (!(this.getDatabaseController().isTableActive(DatabaseTables.SESSION)))
            return false;

        Connection conn = this.getDatabaseController().getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            String sql = String.format("SELECT `%s`, `%s` FROM `%s` WHERE `accountid` = ?",
                    this.getRow(DatabaseRows.SESSION_IPADDRESS), this.getRow(DatabaseRows.SESSION_LOGINTIME), this.getTable(DatabaseTables.SESSION));
            ps = conn.prepareStatement(sql);
            ps.setInt(1, player.getAccountId());
            rs = ps.executeQuery();
            if (!rs.next())
                return false;

            String ipAddress = rs.getString(this.getRow(DatabaseRows.SESSION_IPADDRESS));
            Timestamp loginTime = rs.getTimestamp(this.getRow(DatabaseRows.SESSION_LOGINTIME));

            boolean valid = this.isSessionValid(player, ipAddress, loginTime);
            if (valid)
                return true;

            this.deleteSession(player.getAccountId());
            return false;
        } catch (SQLException e) {
            xAuthLog.severe(String.format("Failed to load session for account: %d", player.getAccountId()), e);
            return false;
        } finally {
            this.getDatabaseController().close(conn, ps, rs);
        }
    }

    private boolean isSessionValid(final xAuthPlayer xp, String ipAddress, Timestamp loginTime) {
        if (this.getConfig().getBoolean("session.verifyip") && !ipAddress.equals(xp.getIPAddress()))
            return false;

        Timestamp expireTime = new Timestamp(loginTime.getTime() + (plugin.getConfig().getInt("session.length") * 1000));
        return expireTime.compareTo(new Timestamp(System.currentTimeMillis())) > 0;
    }

    public void protect(final xAuthPlayer xp) {
        Player p = xp.getPlayer();
        if (p == null)
            return;

        this.getPlayerDataHandler().storeData(xp, p);

        // set GameMode to configured guest gamemode
        p.setGameMode(GameMode.valueOf(this.getConfig().getString("guest.gamemode", Bukkit.getDefaultGameMode().name())));

        int timeout = this.getConfig().getInt("guest.timeout");
        if (timeout > 0 && xp.isRegistered())
            this.getTasks().scheduleKickTimeoutTask(p.getName(), timeout);

        xp.setProtected(true);

        xAuthEventProperties properties = new xAuthEventProperties();
        properties.setProperty("action", xAuthPlayerProtectEvent.Action.PLAYER_PROTECTED);
        this.callEvent(new xAuthPlayerProtectEvent(properties));
    }

    public void unprotect(final xAuthPlayer xp) {
        //@TODO redesign
        // order is getPlayer(), restoreData, setCreativeMode when needed, cancelTask, setProtected(false)
        Player p = xp.getPlayer();
        try {
            if (p == null)
                throw new xAuthPlayerUnprotectException("Could not unprotect Player during fetch Player object from xAuthPlayer.");
        } catch (final xAuthPlayerUnprotectException e) {
            xAuthLog.severe(e.getMessage());
            return;
        }

        this.getPlayerDataHandler().restoreData(xp, p.getName());

        // moved p.setGameMode(xp.getGameMode()) to doLogin

        // guest protection cancel task. See @PlayerManager.protect(final xAuthPlayer p)
        int timeoutTaskId = this.getTasks().getPlayerTask(p.getName(), xAuthTask.xAuthTaskType.KICK_TIMEOUT).getTaskId();
        if (timeoutTaskId > -1) {
            this.getTasks().cancelTasks(p.getName());
        }

        xp.setProtected(false);

        xAuthEventProperties properties = new xAuthEventProperties();
        properties.setProperty("action", xAuthPlayerUnProtectEvent.Action.PLAYER_UNPROTECTED);
        this.callEvent(new xAuthPlayerUnProtectEvent(properties));
    }

    public boolean isLocked(final xAuthPlayer xp) {
        return xp.isLocked();
    }

    public boolean hasResetMode(final xAuthPlayer xp) {
        return xp.isReset();
    }

    public boolean isPremiumUser(final xAuthPlayer xp) {
        return xp.isPremium();
    }

    public boolean setPremium(final int id, final boolean premium) {
        Connection conn = this.getDatabaseController().getConnection();
        PreparedStatement ps = null;

        try {
            String sql = String.format("UPDATE `%s` SET `%s` = %d WHERE `id` = ?",
                    this.getTable(DatabaseTables.ACCOUNT), this.getRow(DatabaseRows.ACCOUNT_PREMIUM), ((premium) ? 1 : 0));
            ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            ps.executeUpdate();

            this.getPlayerById(id).setPremium(premium);

            return true;
        } catch (SQLException e) {
            xAuthLog.severe("Failed to set premium state for account: " + id, e);
            return false;
        } finally {
            this.getDatabaseController().close(conn, ps);
        }
    }

    /**
     * Checks if the given username has paid for his account
     *
     * @param userName String name
     * @return
     */
    public boolean checkPremiumUser(String userName) {
        if (this.getPlayer(userName).isPremium())
            return true;

        // since whe are async use 115s for connection timeout and 250 for read timeout. Better for slow connections
        HTTPRequest httpRequest = new HTTPRequest(String.format("https://minecraft.net/haspaid.jsp?user=%s", userName), 115, 250);
        return Boolean.parseBoolean(httpRequest.getContent());
    }

    public void sendNotice(final xAuthPlayer player) {
        this.sendNotice(player, null);
    }

    public void sendNotice(final xAuthPlayer xp, String node) {
        if (!canNotify(xp))
            return;

        if (node != null) {
            this.getMessageHandler().sendMessage("misc.access-denied", xp.getPlayer(), node);
        } else {
            this.getMessageHandler().sendMessage("misc.illegal", xp.getPlayer());
        }

        // only if not authenticated
        if (!(xp.isAuthenticated()))
            xp.setLastNotifyTime(new Timestamp(System.currentTimeMillis()));
    }

    /**
     * Notification limiter is for guest only
     *
     * @param xp xAuthPlayer the xp player
     * @return true if guest or cooldown is reached, false otherwise
     */
    private boolean canNotify(final xAuthPlayer xp) {
        if (xp.isAuthenticated())
            return true;

        Timestamp lastNotifyTime = xp.getLastNotifyTime();
        if (lastNotifyTime == null)
            return true;

        Timestamp nextNotifyTime = new Timestamp(lastNotifyTime.getTime() + (plugin.getConfig().getInt("guest.notify-cooldown") * 1000));
        return nextNotifyTime.compareTo(new Timestamp(System.currentTimeMillis())) < 0;
    }

    public boolean hasGodMode(final xAuthPlayer player, DamageCause cause) {
        int godmodeLength = plugin.getConfig().getInt("session.godmode-length");
        Timestamp loginTime = player.getLoginTime();
        if (godmodeLength < 1 || loginTime == null || cause == DamageCause.FIRE_TICK || cause == DamageCause.DROWNING)
            return false;

        Timestamp expireTime = new Timestamp(loginTime.getTime() + (godmodeLength * 1000));
        return expireTime.compareTo(new Timestamp(System.currentTimeMillis())) > 0;
    }

    public boolean isActive(int id) {
        Connection conn = this.getDatabaseController().getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            String sql = String.format("SELECT `%s` FROM `%s` WHERE `id` = ?",
                    this.getRow(DatabaseRows.ACCOUNT_ACTIVE), this.getTable(DatabaseTables.ACCOUNT));
            ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            rs = ps.executeQuery();

            return rs.next() && rs.getBoolean(this.getRow(DatabaseRows.ACCOUNT_ACTIVE));
        } catch (SQLException e) {
            xAuthLog.severe("Failed to check active status of account: " + id, e);
            return false;
        } finally {
            this.getDatabaseController().close(conn, ps, rs);
        }
    }

    public boolean activateAcc(int id) {
        return this.setActiveState(id, true);
    }

    public boolean lockAcc(int id) {
        return this.setActiveState(id, false);
    }

    private boolean setActiveState(int id, boolean active) {
        Connection conn = this.getDatabaseController().getConnection();
        PreparedStatement ps = null;

        try {
            String sql = String.format("UPDATE `%s` SET `%s` = %d WHERE `id` = ?",
                    this.getTable(DatabaseTables.ACCOUNT), this.getRow(DatabaseRows.ACCOUNT_ACTIVE), ((active) ? 1 : 0));
            ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            ps.executeUpdate();

            this.getPlayerById(id).setIsLocked(!active);

            return true;
        } catch (SQLException e) {
            xAuthLog.severe("Failed to " + ((active) ? "activate" : "lock") + " account: " + id, e);
            return false;
        } finally {
            this.getDatabaseController().close(conn, ps);
        }
    }

    public boolean setReset(int id) {
        return this.setResetState(id, true);
    }

    public boolean unSetReset(int id) {
        return this.setResetState(id, false);
    }

    private boolean setResetState(int id, boolean reset) {
        xAuthPlayer xp = this.getPlayerById(id);
        return this.getAuthClass(xp).unSetResetPw(xp.getName());
    }

    public boolean activateAll() {
        return this.setAllActiveStates(true, null);
    }

    public boolean lockAll() {
        return this.setAllActiveStates(false, null);
    }

    public boolean setAllActiveStates(boolean state, Integer[] excludeIds) {
        Connection conn = this.getDatabaseController().getConnection();
        PreparedStatement ps = null;

        try {
            String query = "UPDATE `%s` SET `%s` = %d";
            if ((excludeIds != null) && (excludeIds.length > 0))
                query = "UPDATE `%s` SET `%s` = %d WHERE `id` NOT IN (" + xAuthUtils.join(excludeIds) + ")";

            String sql = String.format(query, this.getTable(DatabaseTables.ACCOUNT), this.getRow(DatabaseRows.ACCOUNT_ACTIVE), ((state) ? 1 : 0));
            ps = conn.prepareStatement(sql);
            ps.executeUpdate();

            // clear cache
            reload();

            return true;
        } catch (SQLException e) {
            xAuthLog.severe("Failed to " + ((state) ? "activate" : "lock") + " accounts", e);
            return false;
        } finally {
            this.getDatabaseController().close(conn, ps);
        }
    }

    public Integer countAll() {
        return this.getActiveStatesCount(false, true);
    }

    public Integer countActive() {
        return this.getActiveStatesCount(true, false);
    }

    public Integer countLocked() {
        return this.getActiveStatesCount(false, false);
    }

    private Integer getActiveStatesCount(boolean state, boolean bypassState) {
        Connection conn = this.getDatabaseController().getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            String query = "SELECT COUNT(*) AS `%s` FROM `%s` WHERE `%s` = %d";
            if (bypassState)
                query = "SELECT COUNT(*) AS `%s` FROM `%s`";

            String sql = String.format(query, this.getRow(DatabaseRows.ACCOUNT_ACTIVE), this.getTable(DatabaseTables.ACCOUNT),
                    this.getRow(DatabaseRows.ACCOUNT_ACTIVE),
                    ((state) ? 1 : 0));

            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            rs.next();

            return rs.getInt(this.getRow(DatabaseRows.ACCOUNT_ACTIVE));
        } catch (SQLException e) {
            xAuthLog.severe("Failed to check " + ((state) ? "active" : "lock") + " state", e);
            return null;
        } finally {
            this.getDatabaseController().close(conn, ps);
        }
    }

    public Integer countPremium() {
        return this.getPremiumStatesCount(true);
    }

    public Integer countNonPremium() {
        return this.getPremiumStatesCount(false);
    }

    private Integer getPremiumStatesCount(boolean state) {
        Connection conn = this.getDatabaseController().getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            String query = "SELECT COUNT(*) AS `%s` FROM `%s` WHERE `%s` = %d";

            String sql = String.format(query,
                    this.getRow(DatabaseRows.ACCOUNT_PREMIUM), this.getTable(DatabaseTables.ACCOUNT),
                    this.getRow(DatabaseRows.ACCOUNT_PREMIUM),
                    ((state) ? 1 : 0));

            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            rs.next();

            return rs.getInt(this.getRow(DatabaseRows.ACCOUNT_PREMIUM));
        } catch (SQLException e) {
            xAuthLog.severe("Failed to check " + ((state) ? xAuthPlayerCountType.PREMIUM.getName() : xAuthPlayerCountType.NON_PREMIUM.getName()) + " state", e);
            return null;
        } finally {
            this.getDatabaseController().close(conn, ps);
        }
    }

    public boolean doLogin(final xAuthPlayer xp) {
        int accountId = xp.getAccountId();
        String ipAddress = xp.getIPAddress();
        Timestamp currentTime = new Timestamp(System.currentTimeMillis());

        try {
            // create account if one does not exist (for AuthMethodURL only)
            if (plugin.getConfig().getBoolean("authurl.enabled") && accountId < 1) {
                accountId = this.createAccount(xp.getName(), "authURL", null, ipAddress);
                xp.setAccountId(accountId);
                xp.setStatus(xAuthPlayer.Status.REGISTERED);
            }

            if (plugin.getConfig().getBoolean("account.track-last-login"))
                this.updateLastLogin(accountId, ipAddress, currentTime);

            // insert session if session.length > 0
            if (plugin.getDatabaseController().isTableActive(DatabaseTables.SESSION))
                this.createSession(accountId, ipAddress);

            // clear strikes
            this.plugin.getStrikeManager().getRecord(ipAddress).clearStrikes(xp.getName());

            // clear reset flag
            this.plugin.getPlayerManager().setResetState(accountId, false);

            this.unprotect(xp);
            xp.setLoginTime(currentTime);
            xp.setStatus(xAuthPlayer.Status.AUTHENTICATED);
            xp.setReset(false);

            return true;
        } catch (SQLException e) {
            xAuthLog.severe("Something went wrong while logging in player: " + xp.getName(), e);
            return false;
        }
    }

    public int createAccount(String user, String pass, String email, String ipaddress) throws SQLException {
        Connection conn = this.getDatabaseController().getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        int id = -1;

        try {
            String sql = String.format("INSERT INTO `%s` (`%s`, `%s`, `%s`, `%s`, `%s`) VALUES (?, ?, ?, ?, ?)",
                    this.getTable(DatabaseTables.ACCOUNT),
                    this.getRow(DatabaseRows.ACCOUNT_PLAYERNAME),
                    this.getRow(DatabaseRows.ACCOUNT_PASSWORD),
                    this.getRow(DatabaseRows.ACCOUNT_EMAIL),
                    this.getRow(DatabaseRows.ACCOUNT_REGISTERDATE),
                    this.getRow(DatabaseRows.ACCOUNT_REGISTERIP));

            ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, user);
            ps.setString(2, this.plugin.getPasswordHandler().hash(pass));
            ps.setString(3, email);
            ps.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
            ps.setString(5, ipaddress);
            ps.executeUpdate();
            rs = ps.getGeneratedKeys();

            // set lastId
            id = rs.next() ? rs.getInt(1) : -1;

            // add the user to id/player keyring
            this.playerIds.put(id, user.toLowerCase());

            // activate user if registration.activation is set to false in config
            if ((id > 0) && (!plugin.getConfig().getBoolean("registration.activation"))) {
                this.activateAcc(id);
            }

            return id;
        } finally {
            this.getDatabaseController().close(conn, ps, rs);
        }
    }

    public boolean updateLastLogin(int accountId, String ipAddress, Timestamp currentTime) throws SQLException {
        Connection conn = this.getDatabaseController().getConnection();
        PreparedStatement ps = null;

        try {
            String sql = String.format("UPDATE `%s` SET `%s` = ?, `%s` = ? WHERE `%s` = ?",
                    this.getTable(DatabaseTables.ACCOUNT),
                    this.getRow(DatabaseRows.ACCOUNT_LASTLOGINDATE),
                    this.getRow(DatabaseRows.ACCOUNT_LASTLOGINIP),
                    this.getRow(DatabaseRows.ACCOUNT_ID));

            ps = conn.prepareStatement(sql);
            ps.setTimestamp(1, currentTime);
            ps.setString(2, ipAddress);
            ps.setInt(3, accountId);
            ps.executeUpdate();
            return true;
        } finally {
            this.getDatabaseController().close(conn, ps);
        }
    }

    public boolean deleteAccount(int accountId) {
        Connection conn = this.getDatabaseController().getConnection();
        PreparedStatement ps = null;

        try {
            String sql = String.format("DELETE FROM `%s` WHERE `%s` = ?",
                    this.getTable(DatabaseTables.ACCOUNT),
                    this.getRow(DatabaseRows.ACCOUNT_ID));

            ps = conn.prepareStatement(sql);
            ps.setInt(1, accountId);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            xAuthLog.severe("Something went wrong while deleting account: " + accountId, e);
            return false;
        } finally {
            this.getDatabaseController().close(conn, ps);
        }
    }

    public void initAccount(int accountId) {
        if (this.players.remove(this.playerIds.get(accountId)) != null)
            this.playerIds.remove(accountId);
    }

    public boolean createSession(int accountId, String ipAddress) throws SQLException {
        Connection conn = this.getDatabaseController().getConnection();
        PreparedStatement ps = null;

        boolean sessionAlreadyExists = this.checkSession(accountId);
        try {
            String sql;
            if (sessionAlreadyExists) {
                sql = String.format("UPDATE `%s` SET `%s` = ?, `%s` = ?, `%s` = ? WHERE `%s` = ?",
                        this.getTable(DatabaseTables.SESSION),
                        this.getRow(DatabaseRows.SESSION_ACCOUNTID),
                        this.getRow(DatabaseRows.SESSION_IPADDRESS),
                        this.getRow(DatabaseRows.SESSION_LOGINTIME),
                        this.getRow(DatabaseRows.SESSION_ACCOUNTID));

                ps = conn.prepareStatement(sql);
                ps.setInt(1, accountId);
                ps.setString(2, ipAddress);
                ps.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
                ps.setInt(4, accountId);
                ps.executeUpdate();

                return true;
            }

            // insert if session does not exist
            sql = String.format("INSERT INTO `%s` VALUES (?, ?, ?)",
                    this.getTable(DatabaseTables.SESSION));

            ps = conn.prepareStatement(sql);
            ps.setInt(1, accountId);
            ps.setString(2, ipAddress);
            ps.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
            ps.executeUpdate();

            return true;
        } finally {
            this.getDatabaseController().close(conn, ps);
        }
    }

    public boolean deleteSession(int accountId) {
        if (!(this.getDatabaseController().isTableActive(DatabaseTables.SESSION)))
            return true;

        Connection conn = this.getDatabaseController().getConnection();
        PreparedStatement ps = null;

        try {
            String sql = String.format("DELETE FROM `%s` WHERE `%s` = ?",
                    this.getTable(DatabaseTables.SESSION),
                    this.getRow(DatabaseRows.SESSION_ACCOUNTID));
            ps = conn.prepareStatement(sql);
            ps.setInt(1, accountId);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            xAuthLog.severe("Something went wrong while deleting session for account: " + accountId, e);
            return false;
        } finally {
            this.getDatabaseController().close(conn, ps);
        }
    }

    public String getTable(DatabaseTables table) {
        return this.getDatabaseController().getTable(table);
    }

    public String getRow(DatabaseRows row) {
        return this.getDatabaseController().getRow(row);
    }

    public DatabaseController getDatabaseController() {
        return this.plugin.getDatabaseController();
    }

    public AuthMethod getAuthClass(xAuthPlayer xp) {
        return this.plugin.getAuthClass(xp);
    }

    public MessageHandler getMessageHandler() {
        return this.plugin.getMessageHandler();
    }

    public PlayerDataHandler getPlayerDataHandler() {
        return this.plugin.getPlayerDataHandler();
    }

    public FileConfiguration getConfig() {
        return this.plugin.getConfig();
    }

    protected void callEvent(final xAuthPlayerProtectEvent event) {
        Bukkit.getPluginManager().callEvent(event);
    }

    protected void callEvent(final xAuthPlayerUnProtectEvent event) {
        Bukkit.getPluginManager().callEvent(event);
    }

    protected boolean isAllowedCommand(final Player player, final String messageNode, final String... command) {
        boolean allowed = new PlayerPermissionHandler(player, "PlayerCommandPreProcessEvent", command).checkPermission();
        if (!allowed)
            xAuth.getPlugin().getMessageHandler().sendMessage(messageNode, player);

        return allowed;
    }
}