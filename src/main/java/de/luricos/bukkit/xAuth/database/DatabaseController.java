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

import de.luricos.bukkit.xAuth.utils.xAuthLog;
import de.luricos.bukkit.xAuth.xAuth;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DatabaseController {
    private final xAuth plugin;
    private ConnectionPool connPool;
    private List<DatabaseTables> activeDatabaseTables = new ArrayList<DatabaseTables>();
    private DBMS dbms = DBMS.H2;
    private enum DBMS {
        H2, MySQL
    };

    public DatabaseController(final xAuth plugin) {
        this.plugin = plugin;
        this.initializeDatabase();
    }

    private void initializeDatabase() {
        // Initialize connection pool
        String driver, url, user, pass;

        if (plugin.getConfig().getBoolean("mysql.enabled")) { // MySQL
            this.dbms = DBMS.MySQL;
            ConfigurationSection cs = plugin.getConfig().getConfigurationSection("mysql");
            String host = cs.getString("host");
            int port = cs.getInt("port");
            String db = cs.getString("database");

            driver = "com.mysql.jdbc.Driver";
            url = "jdbc:mysql://" + host + ":" + port + "/" + db + "?zeroDateTimeBehavior=convertToNull";
            user = cs.getString("user");
            pass = cs.getString("password");
        } else { // H2
            driver = "org.h2.Driver";
            url = "jdbc:h2:" + plugin.getDataFolder() + File.separator + "xAuth;MODE=MySQL;IGNORECASE=TRUE";
            user = "sa";
            pass = "";
        }

        try {
            this.connPool = new ConnectionPool(driver, url, user, pass);
        } catch (ClassNotFoundException e) {
            xAuthLog.severe("Failed to create instance of " + this.getDatabaseManagerName() + " JDBC Driver!", e);
        }

        // Register tables
        this.activeDatabaseTables.add(DatabaseTables.ACCOUNT);
        this.activeDatabaseTables.add(DatabaseTables.PLAYERDATA);

        // Activate session table only if session length is higher than zero
        if (this.getConfig().getInt("session.length") > 0)
            this.activeDatabaseTables.add(DatabaseTables.SESSION);

        // Activate location table only if location protection is enabled
        if (this.getConfig().getBoolean("guest.protect-location"))
            this.activeDatabaseTables.add(DatabaseTables.LOCATION);

        // Activate lockout table only if lockouts are enabled
        if (this.getConfig().getInt("strikes.lockout-length") > 0)
            this.activeDatabaseTables.add(DatabaseTables.LOCKOUT);
    }

    public boolean isConnectable() {
        Connection conn = null;

        try {
            conn = getConnection();
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            return false;
        } finally {
            close(conn);
        }
    }

    public Connection getConnection() {
        try {
            return this.connPool.leaseConn();
        } catch (Exception e) {
            xAuthLog.severe("Failed to borrow " + getDatabaseManagerName() + " connection from pool!", e);
            return null;
        }
    }

    public void close(Connection conn, PreparedStatement ps) {
        close(conn, ps, null);
    }

    public void close(Connection conn, PreparedStatement ps, ResultSet rs) {
        this.close(rs);
        this.close(ps);
        this.close(conn);
    }

    public void close(Connection conn) {
        if (conn != null) {
            try {
                conn.setAutoCommit(true);
                this.connPool.returnConn(conn);
            } catch (Exception e) {
                xAuthLog.warning("Failed to return connection to pool!", e);
            }
        }
    }

    public void close(PreparedStatement ps) {
        if (ps != null) {
            try {
                ps.close();
            } catch (SQLException e) {
                xAuthLog.warning("Failed to close PreparedStatement object!", e);
            }
        }
    }

    private void close(ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                xAuthLog.warning("Failed to close ResultSet object!", e);
            }
        }
    }

    public void close() {
        try {
            this.connPool.close();
        } catch (Exception e) {
            xAuthLog.severe("Failed to close " + getDatabaseManagerName() + " connection pool!", e);
        }
    }

    public void runUpdater() {
        DatabaseUpdater dbUpdater = new DatabaseUpdater(plugin, this);
        dbUpdater.runUpdate();
    }

    public boolean isTableActive(DatabaseTables tbl) {
        return activeDatabaseTables.contains(tbl);
    }

    public String getTable(DatabaseTables tbl) {
        if (this.dbms == DBMS.H2)
            return tbl.getName();

        return this.getConfig().getString("mysql.tables." + tbl.toString().toLowerCase());
    }

    public String getRow(DatabaseRows row) {
        return row.getName();
    }

    public FileConfiguration getConfig() {
        return this.plugin.getConfig();
    }

    public List<DatabaseTables> getActiveDatabaseTables() {
        return this.activeDatabaseTables;
    }

    public String getDatabaseManagerName() {
        return this.dbms.toString();
    }

    public boolean isMySQL() {
        return this.dbms == DBMS.MySQL;
    }

}